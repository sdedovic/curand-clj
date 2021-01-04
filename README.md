# cuRAND-clj
[![Clojars Project](https://img.shields.io/clojars/v/com.dedovic/curand-clj.svg)](https://clojars.org/com.dedovic/curand-clj)

A cuRAND wrapper for Clojure. 

Inspired by (and built using) [`uncomplicate/clojurecuda`](https://github.com/uncomplicate/clojurecuda). This library intends to be an idiomatic wrapper of the JCurand API, itself being a 1-to-1 mapping of the cuRAND C/C++ API.

## Installing
![](https://clojars.org/com.dedovic/curand-clj/latest-version.svg)

Add this to your Lein `project.clj`:
```clj
[com.dedovic/curand-clj "0.11.1"]
```

## Usage
### Basic Example (Generate 1000 Floats)

```clojure
(require 'com.dedovic.curand [core :as curand])
(require 'uncomplicate.clojurecuda [core :as cuda])
(require 'uncomplicate.commons.core :refer [with-release])

(cuda/init)

(with-release [;; CUDA setup. See uncomplicate/clojurecuda.
               ;; https://github.com/uncomplicate/clojurecuda 
               device (cuda/device)
               ctx (cuda/context device)
               
               ;; cuRAND RNG setup
               ;; Note: cuRAND-clj objects are releasable, 
               ;;   implementing the protocols defined
               ;;   in the uncomplicate/commons library
               rng-buffer (cuda/mem-alloc (* 1000 Float/BYTES))
               rng (curand/set-pseudo-random-generator-seed 
                     (curand/create-generator) 
                     42)

               ;; Generate some random values and place them in the buffer
               rng-buffer (curand/generate-uniform rng rng-buffer 1000)]
  
  ;; copy from GPU (device) memory to CPU (host) memory
  (let [random-numbers (cuda/memcpy-host! (float-array 1000) rng-buffer)]
    random-numbers))

;; Because we used the with-release macro, all resources are cleaned up once out of scope
```

### More Examples and Documentation
... is severely lacking. Read the source code.

The biggest changes were to wrap things using the utilities provided in [`uncomplicate/clojurecuda`](https://github.com/uncomplicate/clojurecuda) and [`uncomplicate/commons`](https://github.com/uncomplicate/commons) for thread-safety and automatic release of GPU resources. See the above example. 

## License

Copyright © 2020 Stevan Dedovic

Distributed under the Eclipse Public License v2.0.
