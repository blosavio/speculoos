(ns speculoos-project-readme-generator
  "CIDER eval buffer C-c C-k generates an html page and a markdown chunk."
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


(def readme-UUID #uuid "b7613e59-3656-411e-8be6-f3cb8b5d8107")

(alter-var-root #'speculoos-hiccup/*wrap-at* (constantly 80))
(alter-var-root #'speculoos.utility/*such-that-max-tries* (constantly 100))
(def ^:dynamic *eval-separator* ";; => ")


(def sections
  [{:section-name "Setup"}
   ;; later ... #_{:section-name "Screencasts" :section-href "example.com" :skip-section-load? true}
   {:section-name "API" :section-href "https://blosavio.github.io/speculoos/index.html" :skip-section-load? true}
   {:section-name "Introduction" :section-href "intro"}
   {:section-name "Mottos"}
   {:section-name "Mechanics"}
   {:section-name "Validating Scalars" :section-href "scalar-validation"}
   {:section-name "Validating Collections" :section-href "collection-validation"}
   {:section-name "Validation Summaries and Thorough Validations" :section-href "valid-thorough"}
   {:section-name "Validating Functions" :section-href "function-validation"}
   {:section-name "Generating Random Samples and Exercising" :section-href "exercising"}
   {:section-name "Utilities"}
   {:section-name "Predicates"}
   {:section-name "Non-terminating Sequences" :section-href "non-terminating-sequences"}
   {:section-name "Sets"}
   {:section-name "Comparison to spec.alpha" :section-href "https://blosavio.github.io/speculoos/diff.html" :skip-section-load? true}
   {:section-name "Perhaps So" :section-href "https://blosavio.github.io/speculoos/perhaps_so.html" :skip-section-load? true}
   {:section-name "Recipes" :section-href "https://github.com/blosavio/speculoos/tree/main/doc/recipes.clj" :skip-section-load? true}
   {:section-name "Troubleshooting"}
   #_{:section-name "Case Study" :section-href "case-study"}
   {:section-name "Alternatives"}
   {:section-name "Glossary"}
   {:section-name "Contact" :section-href "https://github.com/blosavio" :skip-section-load? true}])


(def clojars-badge
  [[:a {:href "https://clojars.org/com.sagevisuals/speculoos"}
    (element/image "https://img.shields.io/clojars/v/com.sagevisuals/speculoos.svg")]])


(def title-section
  [[:h1 "Speculoos"]
   [:em "An experiment with Clojure specification literals"]])


(def license-section
  [[:h2 "License"]
   [:p "This program and the accompanying materials are made available under the terms of the " [:a {:href "https://opensource.org/license/MIT"} "MIT License"] "."]])

(def page-body (concat clojars-badge [[:br]] (nav sections) title-section [[:br]] (section-blocks sections) [[:br]] license-section))


(spit "doc/readme.html"
      (revert-fn-obj-rendering (page-template
                                "Speculoos — An experimental Clojure lib for data specification"
                                readme-UUID
                                (conj [:body] page-body))))


(spit "README.md"
      (-> page-body
          h2/html
          str
          (clojure.string/replace #"</?article>" "")
          non-breaking-space-ize
          revert-fn-obj-rendering
          #_escape-markdowners))
