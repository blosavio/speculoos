(defproject com.sagevisuals/speculoos "version 0"
  :description "A library for validating Clojure data."
  :url "http://uloos.net"
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
          #_ :doc-files #_ ["resources/html/home.html" "resources/html/diff.html"]
          :themes [:speculoos]
          #_ :source-uri #_"https://github.com/sage_visuals/speculoos/{version}"})
