(defproject com.sagevisuals/speculoos "version1"
  :description "A library for validating Clojure data."
  :url "https://blosavio.github.io/speculoos/home.html"
  :license {:name "MIT License"
            :url "https://opensource.org/license/mit"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.clojure/test.check "1.1.1"]
                 [re-rand "0.1.0"]]
  :repl-options {:init-ns speculoos.core}
  :plugins []
  :profiles {:dev {:dependencies [[hiccup "2.0.0-RC3"]
                                  [zprint "1.2.9"]]
                   :plugins [[dev.weavejester/lein-cljfmt "0.12.0"]
                             [lein-codox "0.10.8"]]}
             :repl {}}
  :codox {:metadata {:doc/format :markdown}
          :namespaces [#"^speculoos\.(?!scratch)(?!generators)"]
          :output-path "doc"
          :themes [:speculoos]
          :source-uri "https://github.com/blosavio/speculoos/blob/main/{filepath}#L{line}"})
