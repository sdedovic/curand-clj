(ns com.dedovic.curand.core
  (:require [clojure.core.match :refer [match]]
            [uncomplicate.clojurecuda.internal.utils :refer [with-check]]
            [uncomplicate.clojurecuda.internal.impl :refer [native-pointer]]
            [uncomplicate.commons.core :refer [Info info Wrappable wrap Wrapper extract Releaseable release let-release]])
  (:import [jcuda.jcurand JCurand curandRngType curandOrdering curandGenerator]
           [jcuda.driver CUdeviceptr]
           [uncomplicate.clojurecuda.internal.impl CULinearMemory CUStream]))

;; ==================== Release resources =======================

(deftype cuRANDGenerator [ref]
  Object
  (hashCode [this]
    (hash (deref ref)))
  (equals [this other]
    (= (deref ref) (extract other)))
  (toString [this]
    (format "#cuRANDGenerator[0x%s]" (Long/toHexString (native-pointer (deref ref)))))
  Wrapper
  (extract [this]
    (deref ref))
  Releaseable
  (release [this]
    (locking ref
      (when-let [d (deref ref)]
        (locking d
          (with-check (JCurand/curandDestroyGenerator d) (vreset! ref nil)))))
    true))

(extend-type curandGenerator
  Info
  (info [this]
    (info (wrap this)))
  Wrappable
  (wrap [ctx]
    (->cuRANDGenerator (volatile! ctx))))

;; ====================== Misc. Utilities ========================================

(defn get-version
  "Return the version number of the library.

  [See curandGetVersion](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  []
  (let [res (int-array 1)]
    (with-check (JCurand/curandGetVersion res) (aget res 0))))

(defn set-stream
  "Set the current stream for CURAND kernel launches.

  [See curandSetStream](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^cuRANDGenerator [^cuRANDGenerator gen ^CUStream hstream]
  (with-check
    (JCurand/curandSetStream (extract gen) (extract hstream))
    gen))

;; ====================== Generators ========================================

(defn- ->curandRngType
  [rng-type]
  (match rng-type
         :test                    curandRngType/CURAND_RNG_TEST
         :pseudo-default          curandRngType/CURAND_RNG_PSEUDO_DEFAULT
         :pseudo-mrg32k3a         curandRngType/CURAND_RNG_PSEUDO_MRG32K3A
         :pseudo-xorwow           curandRngType/CURAND_RNG_PSEUDO_XORWOW
         :pseudo-mtgp32           curandRngType/CURAND_RNG_PSEUDO_MTGP32
         :pseudo-mt19937          curandRngType/CURAND_RNG_PSEUDO_MT19937
         :pseudo-philox4-32-10    curandRngType/CURAND_RNG_PSEUDO_PHILOX4_32_10
         :quasi-default           curandRngType/CURAND_RNG_QUASI_DEFAULT
         :quasi-sobol32           curandRngType/CURAND_RNG_QUASI_SOBOL32
         :quasi-scrambled-sobol32 curandRngType/CURAND_RNG_QUASI_SCRAMBLED_SOBOL32
         :quasi-sobol64           curandRngType/CURAND_RNG_QUASI_SOBOL64
         :quasi-scrambled-sobol64 curandRngType/CURAND_RNG_QUASI_SCRAMBLED_SOBOL64
         :else (throw (str "Unknown rng-type: " rng-type ))))

(defn- ->curandOrdering
  [order]
  (match order
         :pseudo-best             curandOrdering/CURAND_ORDERING_PSEUDO_BEST
         :pseudo-default          curandOrdering/CURAND_ORDERING_PSEUDO_DEFAULT
         :pseudo-legacy           curandOrdering/CURAND_ORDERING_PSEUDO_LEGACY
         :pseudo-seeded           curandOrdering/CURAND_ORDERING_PSEUDO_SEEDED
         :pseudo-quasi-default    curandOrdering/CURAND_ORDERING_QUASI_DEFAULT
         :else (throw (str "Unknown ordering: " order ))))

(defn create-generator
  "Create new random number generator.

  Valid values for rng-type are:
    :test
    :pseudo-default
    :pseudo-mrg32k3a
    :pseudo-xorwow
    :pseudo-mtgp32
    :pseudo-mt19937
    :pseudo-philox4-32-10
    :quasi-default
    :quasi-sobol32
    :quasi-scrambled-sobol32
    :quasi-sobol64
    :quasi-scrambled-sobol64

  Default is :psuedo-default.

  [See curandCreateGenerator](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  (^cuRANDGenerator [] (create-generator :pseudo-default))
  (^cuRANDGenerator [rng-type]
   (let [rng-type (->curandRngType rng-type)]
     (let-release [gen (new curandGenerator)]
                  (with-check (JCurand/curandCreateGenerator gen rng-type) (wrap gen))))))

(defn create-generator-host
  "Create new host CPU random number generator.

  Valid values for rng-type are:
    :test
    :pseudo-default
    :pseudo-mrg32k3a
    :pseudo-xorwow
    :pseudo-mtgp32
    :pseudo-mt19937
    :pseudo-philox4-32-10
    :quasi-default
    :quasi-sobol32
    :quasi-scrambled-sobol32
    :quasi-sobol64
    :quasi-scrambled-sobol64

  Default is :psuedo-default.

  [See curandCreateGeneratorHost](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  (^cuRANDGenerator [] (create-generator :pseudo-default))
  (^cuRANDGenerator [rng-type]
   (let [rng-type (->curandRngType rng-type)]
     (let-release [gen (new curandGenerator)]
                  (with-check (JCurand/curandCreateGeneratorHost gen rng-type) (wrap gen))))))

(defn set-pseudo-random-generator-seed
  "Set the seed value of the pseudo-random number generator.

  [See curandSetPseudoRandomGeneratorSeed](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^curandGenerator [^cuRANDGenerator gen seed]
  (with-check
    (JCurand/curandSetPseudoRandomGeneratorSeed (extract gen) (long seed))
    gen))

(defn set-generator-offset
  "Set the absolute offset of the pseudo or quasirandom number generator.

  [See curandSetGeneratorOffset](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^curandGenerator [^cuRANDGenerator gen offset]
  (with-check
    (JCurand/curandSetGeneratorOffset (extract gen) (long offset))
    gen))

(defn set-generator-ordering
  "Set the ordering of results of the pseudo or quasirandom number generator.

  Valid values for order are:
    :pseudo-best
    :pseudo-default
    :pseudo-legacy
    :pseudo-seeded
    :pseudo-quasi-default

  [See curandSetGeneratorOrdering](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^curandGenerator [^cuRANDGenerator gen order]
  (let [order (->curandOrdering order)]
    (with-check
      (JCurand/curandSetGeneratorOrdering (extract gen) order)
      gen)))

(defn set-quasi-random-generator-dimensions
  "Set the number of dimensions to be generated by the quasirandom number generator.

  Legal values for num-dimensions are 1 to 20000.

  [See curandSetQuasiRandomGeneratorDimensions](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^curandGenerator [^curandGenerator gen num-dimensions]
  (with-check
    (JCurand/curandSetQuasiRandomGeneratorDimensions (extract gen) (int num-dimensions))
    gen))

;; ====================== Psuedo-Random Number Generation ========================================

(defn generate
  "Use gen to generate n 32-bit results into the device memory at output-ptr.

  Results are 32-bit values with every bit random.

  [See curandGenerate](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CUdeviceptr [^cuRANDGenerator gen ^CULinearMemory output-ptr n]
  (with-check
    (JCurand/curandGenerate (extract gen) (extract output-ptr) (long n))
    output-ptr))

(defn generate-long-long
  "Use gen to generate n 64-bit results into the device memory at output-ptr.

  Results are 64-bit values with every bit random.

  [See curandGenerateLongLong](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CUdeviceptr [^cuRANDGenerator gen ^CULinearMemory output-ptr n]
  (with-check
    (JCurand/curandGenerateLongLong (extract gen) (extract output-ptr) (long n))
    output-ptr))

(defn generate-uniform
  "Use gen to generate n float results into the device memory at output-ptr.

  Results are 32-bit floating point values between 0.0f and 1.0f,
  excluding 0.0f and including 1.0f.

  [See curandGenerateUniform](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CUdeviceptr [^cuRANDGenerator gen ^CULinearMemory output-ptr n]
  (with-check
    (JCurand/curandGenerateUniform (extract gen) (extract output-ptr) (long n))
    output-ptr))

(defn generate-uniform-double
  "Use gen to generate n double results into the device memory at output-ptr.

  Results are 64-bit double precision floating point values between
  0.0 and 1.0, excluding 0.0 and including 1.0.

  [See curandGenerateUniformDouble](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CUdeviceptr [^cuRANDGenerator gen ^CULinearMemory output-ptr n]
  (with-check
    (JCurand/curandGenerateUniformDouble (extract gen) (extract output-ptr) (long n))
    output-ptr))

(defn generate-normal
  "Use gen to generate n float results into the device memory at output-ptr.

  Results are 32-bit floating point values with mean mean and standard standard
  deviation stddev.

  [See curandGenerateNormal](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CULinearMemory [^cuRANDGenerator gen ^CULinearMemory output-ptr n mean stddev]
  (with-check
    (JCurand/curandGenerateNormal (extract gen) (extract output-ptr) (long n) (float mean) (float stddev))
    output-ptr))

(defn generate-normal-double
  "Use gen to generate n double results into the device memory at output-ptr.

  Results are 64-bit floating point values with mean mean and standard standard 
  deviation stddev

  [See curandGenerateNormalDouble](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CULinearMemory [^cuRANDGenerator gen ^CULinearMemory output-ptr n mean stddev]
  (with-check
    (JCurand/curandGenerateNormalDouble (extract gen) (extract output-ptr) (long n) (double mean) (double stddev))
    output-ptr))

(defn generate-log-normal
  "Use gen to generate n float results into the device memory at output-ptr.

  Results are 32-bit floating point values with log-normal distribution based on
  an associated normal distribution with mean mean and standard deviation stddev.

  [See curandGenerateLogNormal](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CULinearMemory [^cuRANDGenerator gen ^CULinearMemory output-ptr n mean stddev]
  (with-check
    (JCurand/curandGenerateLogNormal (extract gen) (extract output-ptr) (long n) (float mean) (float stddev))
    output-ptr))

(defn generate-log-normal-double
  "Use gen to generate n double results into the device memory at output-ptr.

  Results are 64-bit floating point values with log-normal distribution based on
  an associated normal distribution with mean mean and standard deviation stddev.

  [See curandGenerateLogNormalDouble](https://docs.nvidia.com/cuda/curand/group__HOST.html#group__HOST)
  "
  ^CULinearMemory [^cuRANDGenerator gen ^CULinearMemory output-ptr n mean stddev]
  (with-check
    (JCurand/curandGenerateLogNormalDouble (extract gen) (extract output-ptr) (long n) (double mean) (double stddev))
    output-ptr))

;; TODO: curandCreatePoissonDistribution
;; TODO: curandDestroyDistribution
;; TODO: curandGeneratePoisson
;; TODO: curandGenerateSeeds
;; TODO: curandGetDirectionVectors32
;; TODO: curandGetScrambleConstants32
;; TODO: curandGetDirectionVectors64
;; TODO: curandGetScrambleConstants64

