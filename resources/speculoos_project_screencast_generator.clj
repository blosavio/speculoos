(ns speculoos-project-screencast-generator
  "CIDER eval buffer C-c C-k generates a screencast page and a
  screencast+comments page."
  {:no-doc true}
  (:require
   [fn-in.core :refer [get-in*]]
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]
   [speculoos.core :refer [all-paths
                           expand-and-clamp-1
                           only-invalid
                           valid-collections?
                           valid-scalars?
                           valid?
                           validate
                           validate-collections
                           validate-scalars]]
   [speculoos.utility :refer [*such-that-max-tries*
                              basic-collection-spec-from-data
                              clamp-in*
                              collections-without-predicates
                              data-from-spec
                              defpred
                              exercise
                              inspect-fn
                              in?
                              predicates-without-collections
                              predicates-without-scalars
                              scalars-without-predicates
                              sore-thumb
                              spec-from-data
                              thoroughly-valid?
                              unfindable-generators
                              validate-predicate->generator
                              thoroughly-valid-scalars?]]
   [speculoos.function-specs :refer [exercise-fn
                                     inject-specs!
                                     instrument unstrument
                                     unject-specs!
                                     validate-fn
                                     validate-fn-with
                                     validate-higher-order-fn]]))


(alter-var-root #'speculoos-hiccup/*wrap-at* (constantly 80))
(alter-var-root #'speculoos.utility/*such-that-max-tries* (constantly 100))
(def ^:dynamic *eval-separator* ";; => ")


(def screencast-filename-bases
  [{:screencast-filename "intro"           :screencast-title "Introduction"                           :screencast-uuid #uuid "09ec291a-23f7-400b-9c3b-694facdb64ae"}
   {:screencast-filename "mechanics"       :screencast-title "Mechanics"                              :screencast-uuid #uuid "a230a730-7d15-43a0-abd1-e3cce92ca598"}
   {:screencast-filename "scalar"          :screencast-title "Validating Scalars"                     :screencast-uuid #uuid "8d2be8eb-1e1b-46a1-9038-c9042bd96b79"}
   {:screencast-filename "collectionA"     :screencast-title "Validating Collections, Basics"         :screencast-uuid #uuid "f7ab1ff8-766b-4e56-b47a-3aa10010a29a"}
   {:screencast-filename "collectionB"     :screencast-title "Validating Collections, Advanced"       :screencast-uuid #uuid "a1b90c85-fc65-4065-a21a-8f5baba2e05d"}
   {:screencast-filename "collectionC"     :screencast-title "Validating Collections, Extras"         :screencast-uuid #uuid "e0a713c4-ff30-4f33-b8ba-d7f6f13734f3"}
   {:screencast-filename "thorough"        :screencast-title "Validation Summaries & Thoroughness"    :screencast-uuid #uuid "d24219fb-c3b0-40d4-a3d2-ceb5d80384ba"}
   {:screencast-filename "function"        :screencast-title "Function Validation"                    :screencast-uuid #uuid "688bb1c8-1158-4ab8-a478-46986c0ff340"}
   {:screencast-filename "exercising"      :screencast-title "Generating Random Samples & Exercising" :screencast-uuid #uuid "af1affe5-9d7e-4adb-9593-543309993bad"}
   {:screencast-filename "utility"         :screencast-title "Utilities"                              :screencast-uuid #uuid "6ec60ee9-1a10-4975-a270-1263256c8245"}
   {:screencast-filename "predicate"       :screencast-title "Predicates"                             :screencast-uuid #uuid "553e7bfd-5e43-4774-b9ff-b5334a4ac821"}
   {:screencast-filename "sequence"        :screencast-title "Non-Terminating Sequences"              :screencast-uuid #uuid "3b8a296e-9837-4ea8-9d53-40e9f82024c1"}
   {:screencast-filename "set"             :screencast-title "Sets"                                   :screencast-uuid #uuid "5cebfc31-4519-4bbc-9de9-8fac2841a8ef"}
   {:screencast-filename "troubleshooting" :screencast-title "Troubleshooting"                        :screencast-uuid #uuid "07d8a61d-9f9c-4167-9c03-adbf915eb2aa"}
   {:screencast-filename "compare"         :screencast-title "Comparing spec.alpha to Speculoos"      :screencast-uuid #uuid "0c99bbbe-b977-4e5b-8e37-32c2a8f07b0d"}
   {:screencast-filename "perhaps"         :screencast-title "Perhaps So"                             :screencast-uuid #uuid "7060304f-2f6e-4453-ab1c-7797821e7edf"}
   #_"case-study"
   #_"glossary"])


(defn whats-next-panel
  "Generate a `what's next` panel, with the next `idx` screencast highlighted,
  optional presentation `notes` to be appended."
  {:UUIDv4 #uuid "6ab9ae17-8942-4bc4-ae4e-7c520f243929"}
  [idx & notes]
  (let [screencast-topics (mapv #(:screencast-title %) screencast-filename-bases)]
    (panel
     [:h3 "What's next."]
     (reduce-kv (fn [v i val] (conj v [(if (= i (inc idx)) :li.highlight :li) val])) [:ol.de-highlight.whats-next] screencast-topics)
     notes)))


(def license-section
  [[:h2 "License"]
   [:p "This program and the accompanying materials are made available under the terms of the " [:a {:href "https://opensource.org/license/MIT"} "MIT License"] "."]])


(defn generate-screencast
  "Given file-name base entry `fnbe`, generate an html screencast page."
  {:UUIDv4 #uuid "b07a9fbd-0ad1-4ae3-96a9-01937ab053e6"}
  [fnbe]
  (let [screencast-body (screencast-template
                         "Speculoos — An experimental Clojure lib for data specification"
                         (:screencast-uuid fnbe)
                         (load-file (str "resources/screencast_sections/" (:screencast-filename fnbe) ".clj")))
        screencast-body-fn-reverted (revert-fn-obj-rendering screencast-body)]
    (spit (str "doc/screencast_slides/" (:screencast-filename fnbe) ".html") screencast-body-fn-reverted)))


(defn generate-all-screencasts
  "Given a sequence of `filename-bases`, generate a screencast html doc for all filenames."
  {:UUIDv4 #uuid "d856e482-bf0e-4ad6-8056-095f49c84f42"}
  [fnb]
  (map #(generate-screencast %) fnb))


(generate-all-screencasts screencast-filename-bases)