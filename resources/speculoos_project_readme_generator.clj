(ns speculoos-project-readme-generator
  "CIDER eval buffer C-c C-k generates an html page and a markdown chunk."
  {:no-doc true}
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]
   [speculoos.core :refer [all-paths validate-scalars validate-collections
                           validate]]))


(def readme-UUID #uuid "b7613e59-3656-411e-8be6-f3cb8b5d8107")
(alter-var-root #'speculoos-hiccup/*wrap-at* (constantly 80))
(declare predicate-1 predicate-2 predicate-3 predicate-4)
(def ^:dynamic *eval-separator* ";; => ")


(def sections
  [{:section-name "Setup"}
   #_{:section-name "Videos" :section-href "example.com" :skip-section-load? true}
   {:section-name "API" :section-href "https://blosavio.github.io/speculoos/index.html" :skip-section-load? true}
   #_{:section-name "Introduction" :section-href "intro"}
   {:section-name "Mantras"}
   #_{:section-name "Mechanics"}
   #_{:section-name "Validating Scalars" :section-href "scalar-validation"}
   #_{:section-name "Validating Collections" :section-href "collection-validation"}
   #_{:section-name "Validation Summaries and Thorough Validations" :section-href "valid-thorough"}
   {:section-name "Validating Functions" :section-href "function-validation"}
   #_{:section-name "Generating Random Samples and Exercising" :section-href "exercising"}
   #_{:section-name "Utilities"}
   #_{:section-name "Predicates"}
   #_{:section-name "Non-terminating Sequences" :section-href "non-terminating-sequences"}
   #_{:section-name "Sets"}
   #_{:section-name "Comparison to spec.alpha" :section-href "diff.html" :skip-section-load? true}
   #_{:section-name "Maybe so" :section-href "maybe_so.html" :skip-section-load? true}
   #_{:section-name "Recipes" :section-href "recipes.clj" :skip-section-load? true}
   #_{:section-name "Troubleshooting"}
   #_{:section-name "Case Study" :section-href "case-study"}
   #_{:section-name "Alternatives"}
   #_{:section-name "Glossary"}
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
          escape-markdowners))
