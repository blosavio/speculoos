(ns home
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]
   [speculoos.core :refer [valid-scalar-spec? valid-collection-spec?
                           only-invalid validate-scalar-spec]]
   [speculoos.fn-in :refer [update-in*]]))


(def home-UUID #uuid "8bb02c87-c3bf-4f58-bb76-269040d3e8c4")


(ns-unmap *ns* 'valid?)
(def valid? valid-scalar-spec?)


(spit "doc/home.html"
      (page-template
       "Speculoos — Home"
       home-UUID
       [:body
        (nav-bar "Home")
        [:article
         [:h1 "Speculoos"]
         [:p.subtitle "An experiment in Clojure specification literals"]
         [:section
          [:p "I thought it might be nice if I could "
           [:a {:href "https://clojure.org/guides/spec"} "specify"]
           " some "
           [:a {:href "https://clojure.org/"} "Clojure"]
           " data like this"]
          [:pre (print-form-then-eval "(valid? [42 \"abc\" \\z] [int? string? char?])")]
          [:p "That idea blossomed into Speculoos, an experiment in specifying data. It felt natural to compose a specification by its shape, so Speculoos can also do maps"]
          [:pre (print-form-then-eval "(valid? {:a 22/7 :b true :c nil} {:a ratio? :b boolean? :c nil?})")]
          [:p "or any other " [:em "heterogenous, arbitrarily-nested Clojure data structure"] ". And when a datum doesn't conform"]
          [:pre (print-form-then-eval "(only-invalid (validate-scalar-spec [:foo 1.23 (list 0 4)] [keyword? float? (list zero? odd?)]))")]
          [:p "Speculoos returns why and where with a path that can be used to inspect, maniupulate, or delete invalid datums with " [:code "get-in"] " style tools. Speculoos can " [:code "update"] " that invalid " [:code "4"] " at path " [:code "[2 1]"] " by diving into the data structure and incrementing it to an odd integer."]
          [:pre (print-form-then-eval "(valid? (update-in* [:foo 1.23 (list 0 4)] [2 1] inc) [keyword? float? (list zero? odd?)])")]
          [:p "Speculoos avoids new syntax by separately specifying the scalars (i.e., integers, strings, booleans, etc.) and the Clojure collections that contain those scalars (i.e., vectors, maps, sets, and lists). Speculoos distinguishes a " [:em "scalar specification"] " (seen in the previous examples), and a " [:em "collection specification"]]
          [:pre (print-form-then-eval "(valid-collection-spec? [11 :foo 22 \\z] [vector? #(= 4 (count %))])")]
          [:p "which only concerns properties of the collection itself."]

          [:p "Speculoos explores " [:a {:href "ideas.html"} "three ideas"] "."
           [:ol
            [:li "Is shape-based data specification merely a gimmick, or is it powerful enough to do what " [:code "clojure.spec.alpha"] " does? "]
            [:li "Is " [:code "get-in"] " addressing sufficient to inspect, manipulate, add, and delete Clojure data and its specifications?"]
            [:li "Does separately specifying scalars and collections strike a good balance between mental clarity and requiring extra work?"]]

           [:p "I think the answer to all three questions is " [:em "Yes"] ". Speculoos is an experiment to see just how far I could push those ideas. I arbitrarily set a goal of performing the same tasks as " [:code "clojure.spec.alpha"] ". In my hands, Speculoos can do a flimsy version of most of those tasks. In the hands of someone experienced, a better implementation of these ideas might go even further." ]
           [:p [:a {:href "contact.html"} "Let me know"]" what you think."]]]]]))