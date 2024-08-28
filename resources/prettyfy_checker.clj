(ns prettyfy-checker
  "CIDER eval buffer C-c C-k generates an html page and a markdown chunk."
  {:no-doc true}
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def checker-UUID #uuid "a3b33ff1-7dcd-4b9c-a34f-58b39042bbb5")
(alter-var-root #'speculoos-hiccup/*wrap-at* (constantly 80))
(def ^:dynamic *eval-separator* ";; => ")


(def page-body

  [:article

   [:h1 "Speculoos " [:code "prettyfy"] " check"]
   [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."]

   [:pre
    (print-form-then-eval "(+ 1 2 3)")
    [:br]
    [:br]
    (print-form-then-eval "(map inc [11 22 33])")
    [:br]
    [:br]
    (print-form-then-eval "(defn foo [x y] (vector x y (+ x y) (* x y)))")
    ]

   [:h2 [:code "all-paths"]]
   [:p "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."]

   [:pre
    (print-form-then-eval "(require '[speculoos.core :refer [all-paths]])")
    [:br]
    [:br]
    (print-form-then-eval "(all-paths [42 :foo 22/7])")
    [:br]
    [:br]
    (print-form-then-eval "(all-paths {:a 42 :b 'foo :c 22/7})" 80 45)
    [:br]
    [:br]
    (print-form-then-eval "(all-paths [11 [22 [33] 44] 55 66 77 88 99])" 80 35)]

   [:h2 [:code "validate-scalars"]]
   [:p "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."]

   [:pre
    (print-form-then-eval "(require '[speculoos.core :refer [validate-scalars]])")
    [:br]
    [:br]
    (print-form-then-eval "(validate-scalars [42 :foo 22/7] [int? string? ratio?])" 40 60)
    [:br]
    [:br]
    (print-form-then-eval "(validate-scalars {:a 42 :b 'foo :c 22/7} {:a int? :b string? :c ratio?})" 55 60)]

   [:h2 [:code "validate-collections"]]
   [:p "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]

   [:pre
    (print-form-then-eval "(require '[speculoos.core :refer [validate-collections]])")
    [:br]
    [:br]
    (print-form-then-eval "(validate-collections [11 [22 [33]]] [vector? [set? [list?]]])" 60 80)
    [:br]
    [:br]
    (print-form-then-eval "(validate-collections {:a 11 :b {:c [22]}} {:is-map? map? :b {:is-set? set? :c [vector?]}})" 80 80)]

   [:h2 [:code "valid-scalars?"] " and " [:code "valid-collections?"]]
   [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."]

   [:pre
    (print-form-then-eval "(require '[speculoos.core :refer [valid-scalars? valid-collections?]])")
    [:br]
    [:br]
    (print-form-then-eval "(valid-scalars? [42 :foo 22/7] [int? keyword? ratio?])" 40 80)
    [:br]
    [:br]
    (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo :c 22/7} {:a int? :b symbol? :c ratio?})" 50 80)
    [:br]
    [:br]
    (print-form-then-eval "(valid-collections? [11 [22 [33]]] [vector? [set? [map?]]])" 50 80)
    [:br]
    [:br]
    (print-form-then-eval "(valid-collections? {:a 42 :b {:c ['foo]}} {:is-map? map? :b {:is-map? map? :c [vector?]}})")]

   ])


(spit "doc/prettyfy_check.html"
      (page-template
       "Speculoos — check `prettyfy` settings"
       checker-UUID
       (conj [:body] page-body)))


(spit "doc/prettyfy_check.md"
      (-> page-body
          h2/html
          str
          (clojure.string/replace #"</?article>" "")))
