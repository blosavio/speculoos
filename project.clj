(defproject com.sagevisuals/speculoos "7-SNAPSHOT0"
  :description "A library for validating Clojure data."
  :url "https://github.com/blosavio/speculoos"
  :license {:name "MIT License"
            :url "https://opensource.org/license/mit"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/test.check "1.1.1"]
                 [re-rand "0.1.0"]
                 [com.sagevisuals/fn-in "4-SNAPSHOT0"]]
  :repl-options {:init-ns speculoos.core}
  :plugins []
  :profiles {:dev {:dependencies [[hiccup "2.0.0-RC3"]
                                  [zprint "1.2.9"]
                                  [com.sagevisuals/chlog "0-SNAPSHOT2"]
                                  [com.sagevisuals/readmoi "2-SNAPSHOT1"]
                                  [com.sagevisuals/screedcast "0-SNAPSHOT0"]]
                   :plugins [[dev.weavejester/lein-cljfmt "0.12.0"]
                             [lein-codox "0.10.8"]]}
             :repl {}}
  :codox {:metadata {:doc/format :markdown}
          :namespaces [#"^speculoos\.(?!scratch)(?!generators)"]
          :target-path "doc"
          :output-path "doc"
          :doc-files []
          :source-uri "https://github.com/blosavio/speculoos/blob/main/{filepath}#L{line}"
          :html {:transforms [[:div.sidebar.primary] [:append [:ul.index-link [:li.depth-1 [:a {:href "https://github.com/blosavio/speculoos"} "Project home"]]]]]}
          :project {:name "Speculoos" :version "version 7-SNAPSHOT0"}}
  :scm {:name "git" :url "https://github.com/blosavio/speculoos"})
