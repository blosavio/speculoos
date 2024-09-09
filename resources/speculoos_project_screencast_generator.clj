(ns speculoos-project-screencast-generator
  "CIDER eval buffer C-c C-k generates a screencast page and a
  screencast+comments page."
  {:no-doc true}
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]
   [speculoos.core :refer [all-paths validate-scalars validate-collections
                           validate]]
   [speculoos.utility :refer [*such-that-max-tries*]]))



(alter-var-root #'speculoos-hiccup/*wrap-at* (constantly 80))
(alter-var-root #'speculoos.utility/*such-that-max-tries* (constantly 100))
(def ^:dynamic *eval-separator* ";; => ")


(def screencast-filename-bases
  [#_"setup"
   {:screencast-filename "intro" :screencast-uuid #uuid "4e8947fe-756c-42a6-b930-76559fb88372"}
   #_"mantras"
   {:screencast-filename "mechanics" :screencast-uuid #uuid "a230a730-7d15-43a0-abd1-e3cce92ca598"}
   #_"scalar_validation"
   #_"collection_validation"
   #_"valid-thorough"
   #_"function_validation"
   #_"exercising"
   #_"utilities"
   #_"predicates"
   #_"non_terminating_sequences"
   #_"sets"
   ;; later ... #_"diff_spec_alpha"
   ;; later ... #_"maybe_so"
   #_{:screencast-filename "troubleshooting" :screencast-uuid #uuid "07d8a61d-9f9c-4167-9c03-adbf915eb2aa"}
   #_"case-study"
   #_"glossary"])


(def license-section
  [[:h2 "License"]
   [:p "This program and the accompanying materials are made available under the terms of the " [:a {:href "https://opensource.org/license/MIT"} "MIT License"] "."]])



(defn generate-screencast
  "Given file-name base entry `fnbe`, generate an html screencast page."
  {:UUIDv4 #uuid "b07a9fbd-0ad1-4ae3-96a9-01937ab053e6"}
  [fnbe]
  (spit (str "doc/screencast_slides/" (:screencast-filename fnbe) ".html")
        (revert-fn-obj-rendering (screencast-template
                                  "Speculoos — An experimental Clojure lib for data specification"
                                  (:screencast-uuid fnbe)
                                  (load-file (str "resources/screencast_sections/" (:screencast-filename fnbe) ".clj"))))))


(defn generate-all-screencasts
  "Given a sequence of `filename-bases`, generate a screencast html doc for all filenames."
  {:UUIDv4 #uuid "d856e482-bf0e-4ad6-8056-095f49c84f42"}
  [fnb]
  (map #(generate-screencast %) fnb))

(generate-all-screencasts screencast-filename-bases)
