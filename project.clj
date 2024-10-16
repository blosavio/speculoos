(defproject com.sagevisuals/speculoos "2-SNAPSHOT4"
  :description "A library for validating Clojure data."
  :url "https://blosavio.github.io/speculoos/home.html"
  :license {:name "MIT License"
            :url "https://opensource.org/license/mit"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.clojure/test.check "1.1.1"]
                 [re-rand "0.1.0"]
                 [com.sagevisuals/fn-in "1"]]
  :repl-options {:init-ns speculoos.core}
  :plugins []
  :profiles {:dev {:dependencies [[hiccup "2.0.0-RC3"]
                                  [zprint "1.2.9"]]
                   :plugins [[dev.weavejester/lein-cljfmt "0.12.0"]
                             [lein-codox "0.10.8"]]}
             :repl {}}
  :codox {:metadata {:doc/format :markdown}
          :namespaces [#"^speculoos\.(?!scratch)(?!generators)"]
          :target-path "doc"
          :output-path "doc"
          :doc-files []
          :source-uri "https://github.com/blosavio/speculoos/blob/main/{filepath}#L{line}"
          :themes [:speculoos]
          :project {:name "Speculoos" :version "version 2-SNAPSHOT4"}}
  :scm {:name "git" :url "https://github.com/blosavio/speculoos"})
