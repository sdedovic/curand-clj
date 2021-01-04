(defproject clojure-curand "0.11.1"
  :description "A cuRAND wrapper for Clojure"
  :url "https://github.com/sdedovic/curand-clj"
  :license {:name "Eclipse Public License 2.0"
            :url  "http://www.eclipse.org/legal/epl-2.0"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.match "1.0.0"]
                 [org.jcuda/jcurand "11.1.1"]
                 [uncomplicate/clojurecuda "0.11.0"]
                 [uncomplicate/commons "0.11.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}}

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :resource-paths ["resources"]

  :deploy-repositories [["releases" :clojars]])
