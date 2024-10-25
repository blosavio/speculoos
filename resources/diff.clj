(ns diff
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def diff-UUID #uuid "8a4f6c1d-cd36-4753-8fea-7afeb8faa049")


(spit "doc/diff.html"
      (page-template
       "Speculoos Comparison to clojure.spec.alpha"
       diff-UUID
       [:body.wide-body
        [:article
         [:h1.wide-title [:code "(diff spec.alpha speculoos)"]]
         [:p "The " [:a {:href "https://github.com/blosavio/speculoos"} "Speculoos library"]
          " is an experiment to see if it is possible to perform the same tasks as "
          [:code "clojure.spec.alpha"]
          " using literal specifications. As a rough measure, I tried to replicate the features outlined in the "
          [:a {:href "https://clojure.org/guides/spec"} [:em " spec Guide"]]
          ". I think Speculoos manages to check off most every feature to some degree, so I feel the project's ideas have merit."]

         [:p "If you're familiar with " [:code "clojure.spec.alpha"] " and are curious about how Speculoos compares, here's a side-by-side demonstration. Find the full documentation " [:a {:href "https://github.com/blosavio/speculoos"} "here"] "."]

         [:p [:em "Related:"] " How Speculoos " [:a {:href "https://blosavio.github.io/speculoos/perhaps_so.html"} "addresses issues"] " presented in " [:em "Maybe Not"] " (Rich Hickey, 2018)."]

         [:section
          [:h2 "Predicates"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " predicates are tested with "
             [:code "s/conform"] " or " [:code "s/valid?"]]
            [:pre
             (print-form-then-eval "(require '[clojure.spec.alpha :as s])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/conform even? 1000)")]]

           [:div.side-by-side
            [:p "Speculoos specifications are bare, unadorned Clojure predicates."]
            [:pre
             (print-form-then-eval "(even? 1000)")
             [:br]
             [:br]
             (print-form-then-eval "(nil? nil)")
             [:br]
             [:br]
             (print-form-then-eval "(#(< % 5) 4)")]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] " provides a special " [:code "def"] " which stores the spec in a central registry."]
            [:pre
             (print-form-then-eval "(s/def :order/date inst?)")
             [:br]
             [:br]
             (print-form-then-eval "(s/def :deck/suit #{:club :diamond :heart :spade})")]]

           [:div.side-by-side
            [:p "Speculoos specifications are plain Clojure functions. They are " [:code "def"] "-ed and live in our namespace, and are therefore automatically namespace-qualified."]
            [:pre
             (print-form-then-eval "(def date inst?)")
             [:br]
             [:br]
             (print-form-then-eval "(def suit #{:club :diamond :heart :spade})" 40 40)]

            [:p "If you like the idea of a spec registry, toss 'em into your own hashmap; Speculoos specifications are just predicates and can be used anywhere"]
            [:pre
             (print-form-then-eval "(import java.util.Date)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(date (Date.))")
             [:br]
             [:br]
             (print-form-then-eval "(suit :club)")
             [:br]
             [:br]
             (print-form-then-eval "(suit :shovel)")]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] " has some slick facilities for automatically creating spec docstrings."]]
           [:div.side-by-side [:p "Speculoos specifications do not have any special docstring features beyond what we explicitly add to our function " [:code "def"] "s."]]]]

         [:section
          [:h2 "Composing Predicates"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " specs are composed with special functions " [:code "s/and"] " and " [:code "s/or"] "."]
            [:pre
             (print-form-then-eval "(s/def :num/big-even (s/and int? even? #(> % 1000)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :num/big-even :foo)")
             [:br]
             (print-form-then-eval "(s/valid? :num/big-even 10)")
             [:br]
             (print-form-then-eval "(s/valid? :num/big-even 100000)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :domain/name-or-id (s/or :name string? :id   int?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :domain/name-or-id \"abc\")")
             [:br]
             (print-form-then-eval "(s/valid? :domain/name-or-id 100)")
             [:br]
             (print-form-then-eval "(s/valid? :domain/name-or-id :foo)")]]

           [:div.side-by-side
            [:p "Speculoos specifications are composed with " [:code "clojure.core/and"] " and " [:code "clojure.core/or"] "."]
            [:pre
             (print-form-then-eval "(def big-even #(and (int? %) (even? %) (> % 1000)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(big-even :foo)")
             [:br]
             (print-form-then-eval "(big-even 10)")
             [:br]
             (print-form-then-eval "(big-even 10000)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def name-or-id #(or (string? %) (int? %)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(name-or-id \"abc\")")
             [:br]
             (print-form-then-eval "(name-or-id 100)")
             [:br]
             (print-form-then-eval "(name-or-id :foo)")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " annotates branches with keywords (e.g., " [:code ":name"] " and " [:code ":id"] "), used to return " [:em "conformed"] " data."]]

           [:div.side-by-side
            [:p "Speculoos uses a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mechanics"} "different strategy using paths"] " to refer to datums within an validation report."]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " provides a helper to include " [:code "nil"] " as a valid value"]
            [:pre
             (print-form-then-eval "(s/valid? string? nil)")
             [:br]
             (print-form-then-eval "(s/valid? (s/nilable string?) nil)")]]

           [:div.side-by-side
            [:p "Simply compose to make a Speculoos predicate nilable."]
            [:pre (print-form-then-eval "(#(or (string? %) (nil? %)) nil)")]]]

          [:p "However, it's probably better to " [:a {:href "https://blosavio.github.io/speculoos/perhaps_so.html"} "avoid nilable"] " altogether."]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] "'s " [:code "explain"] " provides a nice report for non-conforming simple predicates."]]
           [:div.side-by-side [:p "Speculoos returns only " [:code "true/false"] " for simple predicates. " [:a {:href "#validation-report"} "Later"] ", we'll see how Speculoos "
                               [:em "does"] " produce a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#valid-thorough"} "detailed report"] " for composite values."]]]]

         [:section
          [:h2 "Entity Maps"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Here is " [:code "spec.alpha"] " in action."]
            [:pre
             (print-form-then-eval "(def email-regex #\"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,63}$\")")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :acct/email-type (s/and string? #(re-matches email-regex %)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :acct/acctid int?)")
             [:br]
             (print-form-then-eval "(s/def :acct/first-name string?)")
             [:br]
             (print-form-then-eval "(s/def :acct/last-name string?)")
             [:br]
             (print-form-then-eval "(s/def :acct/email :acct/email-type)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :acct/person (s/keys :req [:acct/first-name :acct/last-name :acct/email] :opt [:acct/phone]))")]]

           [:div.side-by-side
            [:p "Here is the same process in Speculoos, re-using the regex. (The " [:em "spec Guide"] " does not appear to use " [:code ":acct/acctid"] ", so I will skip it.)"
]
            [:pre
             (print-form-then-eval "(def email-spec #(and (string? %) (re-matches email-regex %)))" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def person-spec {:first-name string? :last-name string? :email email-spec :phone any?})" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(require '[speculoos.core :refer [valid-scalars? validate-scalars only-invalid]])" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"bugs@example.com\"} person-spec)" 45 45)]

            [:p "Speculoos checks only keys that are in both the data and the specification. If we don't want to validate a particular entry, we can, on-the-fly, " [:strong "dissoc"] "iate that key-val from the specification."]

            [:pre (print-form-then-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"not@even@close@to@a@valid@email\"} (dissoc person-spec :email))")]

            [:p "If we want to merely relax a specification, simply " [:strong "assoc"] "iate a new, more permissive predicate."]

            [:pre (print-form-then-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email :not-an-email} (assoc person-spec :email #(string? %)))" 45 45)]

            [:p "Note the function name: Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "distinguishes"] " validating " [:em "scalars"] " (i.e., numbers, strings, characters, etc.) from " [:em "collections"] " (vectors, lists, maps, sets). Speculoos provides a corresponding group of functions for specifying and validating collection counts, presence of keys, set membership, etc."]

            [:pre (print-form-then-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"}  person-spec)" 45 45)]

            [:p#validation-report "Instead of using " [:code "valid…?"] " and friends, Speculoos' " [:code "validate…*"] " family of functions show the details of the validating each datum."]
            [:pre (print-form-then-eval "(validate-scalars {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"} person-spec)" 45 45)]


            [:p "The validation results can grow unwieldy with large data and specifications, so Speculoos provides some helper functions to quickly focus on points of interest, i.e., non-valid datums."]
            [:pre (print-form-then-eval "(only-invalid (validate-scalars {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"} person-spec))" 45 45)]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] " distinguishes unqualified keys and fully-namespaced keys, and allows us to explicitly declare one or the other."]]
           [:div.side-by-side
            [:p "Speculoos implicitly distinguishes qualified from unqualified keys because " [:code "(not= :k ::k)"] "."]

            [:p "Observe: Qualified keys in data, unqualified keys in specification, no matches…"]
            [:pre (print-form-then-eval "(validate-scalars {::a 42 ::b \"abc\" ::c :foo} {:a int? :b string? :c keyword?})")]

            [:p "…qualified keys in both data and specification, validation succeeds…"]
            [:pre (print-form-then-eval "(valid-scalars? {::a 42 ::b \"abc\" ::c :foo} {::a int? ::b string? ::c keyword?})")]

            [:p "…unqualified keys in both data and specification, validation succeeds."]
            [:pre (print-form-then-eval "(valid-scalars? {:a 42 :b \"abc\" :c :foo} {:a int? :b string? :c keyword?})" 55 45)]]]]

         [:div.side-by-side-container
          [:div.side-by-side
           [:p [:code "spec.alpha"] " handles keyword args like this:"]
           [:pre
            (print-form-then-eval "(s/def :my.config/port number?)")
            [:br]
            (print-form-then-eval "(s/def :my.config/host string?)")
            [:br]
            (print-form-then-eval "(s/def :my.config/id keyword?)")
            [:br]
            (print-form-then-eval "(s/def :my.config/server (s/keys* :req [:my.config/id :my.config/host] :opt [:my.config/port]))")
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(s/conform :my.config/server [:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555])")]]

          [:div.side-by-side
           [:p "Speculoos does it this way:"]
           [:pre
            (print-form-then-eval "(def port number?)")
            [:br]
            (print-form-then-eval "(def host string?)")
            [:br]
            (print-form-then-eval "(def id keyword?)")
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(def server-spec {:my.config/id id :my.config/host host :my.config/port port})" 45 55)
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(valid-scalars? {:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555} server-spec)" 45 45)]

           [:p "One principle of Speculoos' validation is that if the key exists in both the data and specification, then Speculoos will apply the predicate to the datum. This fulfills the criteria of " [:em "Thing may or may not exist, but if Thing " [:strong "does"] " exist, it must satisfy this predicate."]]

           [:p "If we want to similarly validate a sequential data structure, it goes like this:"]
           [:pre
            (print-form-then-eval "(def server-data-2 [:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555])" 45 45)
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(def server-spec-2 [#(= % :my.config/id) id #(= % :my.config/host) host #(= % :my.config/port) port])" 45 45)
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(valid-scalars? server-data-2 server-spec-2)" 45 45)]]]

         [:div.side-by-side-container
          [:div.side-by-side
           [:p [:code "spec.alpha"] " has a " [:code "merge"] " function."]
           [:pre
            (print-form-then-eval "(s/def :animal/kind string?)")
            [:br]
            (print-form-then-eval "(s/def :animal/says string?)")
            [:br]
            (print-form-then-eval "(s/def :animal/common (s/keys :req [:animal/kind :animal/says]))")
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(s/def :dog/tail? boolean?)")
            [:br]
            (print-form-then-eval "(s/def :dog/breed string?)")
            [:br]
            (print-form-then-eval "(s/def :animal/dog (s/merge :animal/common (s/keys :req [:dog/tail? :dog/breed])))")
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(s/valid? :animal/dog {:animal/kind \"dog\" :animal/says \"woof\" :dog/tail? true :dog/breed \"retriever\"})")]]

          [:div.side-by-side
           [:p "Speculoos simply uses Clojure's powerful data manipulation functions."]
           [:pre
            (print-form-then-eval "(def animal-kind string?)")
            [:br]
            (print-form-then-eval "(def animal-says string?)")
            [:br]
            (print-form-then-eval "(def animal-spec {:kind animal-kind :says animal-says})")
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(def dog-spec (merge animal-spec {:tail boolean? :breed string?}))")
            [:br]
            [:br]
            (print-form-then-eval "(def dog-data {:kind \"dog\" :says \"woof\" :tail true :breed \"retriever\"})")
            [:br]
            [:br]
            [:br]
            (print-form-then-eval "(valid-scalars? dog-data dog-spec)")]]]

         [:section
          [:h2 "Multi-spec"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " has the capability to dispatch validation paths according to an in-band key. Here's the " [:em "Guide"] "'s demo."]
            [:pre
             (print-form-then-eval "(s/def :event/type keyword?)")
             [:br]
             (print-form-then-eval "(s/def :event/timestamp int?)")
             [:br]
             (print-form-then-eval "(s/def :search/url string?)")
             [:br]
             (print-form-then-eval "(s/def :error/message string?)")
             [:br]
             (print-form-then-eval "(s/def :error/code int?)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defmulti event-type :event/type)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defmethod event-type :event/search [_] (s/keys :req [:event/type :event/timestamp :search/url]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defmethod event-type :event/error [_] (s/keys :req [:event/type :event/timestamp :error/message :error/code]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :event/event (s/multi-spec event-type :event/type))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :event/event {:event/type :event/search :event/timestamp 1463970123000 :search/url \"https://clojure.org\"})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :event/event {:event/type :event/error :event/timestamp 1463970123000 :error/message \"Invalid host\" :error/code 500})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/explain :event/event {:event/type :event/restart})")]]

           [:div.side-by-side
            [:p "Since Speculoos consumes regular old Clojure data structures and functions, they work similarly. Instead of " [:code "def"] "-ing a series of separate predicates, for brevity, I'll inject them directly into the specification definition, but Speculoos could handle any level of indirection."]
            [:pre
             (print-form-then-eval "(defmulti event-type :event/type)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defmethod event-type :event/search [_] {:event/type keyword? :event/timestamp int? :search/url string?})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defmethod event-type :event/error [_] {:event/type keyword? :event/timestamp int? :error/message string? :error/code int?})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def event-1 {:event/type :event/search :event/timestamp 1463970123000 :event/url \"https://clojure.org\"})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? event-1 (event-type event-1))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def event-2 {:event/type :event/error :event/timestamp 1463970123000 :error/message \"Invalid host\" :code 500})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? event-2 (event-type event-2))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def event-3 {:event/type :restart})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(try (valid-scalars? event-3 (event-type event-3)) (catch Exception e (.getMessage e)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def event-4 {:event/type :event/search :search/url 200})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(only-invalid (validate-scalars event-4 (event-type event-4)))")]]]

          [:p "Here we see a significant difference between " [:code "spec.alpha"] " and Speculoos: the former fails the validation because " [:code "event-4"] " is missing the " [:code ":timestamp"] " key. Speculoos considers the presence or absence of a map's key to be a property of the collection. Within that philosophy, such a specification would properly belong in a Speculoos " [:em "collection specification"] "."]]

         [:section
          [:h2 "Collections"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " provides a trio of helper functions for collections. First, " [:code "coll-of"] "."]
            [:pre
             (print-form-then-eval "(s/conform (s/coll-of keyword?) [:a :b :c])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/conform (s/coll-of number?) #{5 10 2})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :ex/vnum3 (s/coll-of number? :kind vector? :count 3 :distinct true :into #{}))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/conform :ex/vnum3 [1 2 3])")]]

           [:div.side-by-side
            [:p "Speculoos was designed from the beginning to specify collections. Speculoos validates collections in two different ways: it can validate groupings of " [:em "scalars"] ", atomic, indivisible values (i.e., numbers, booleans, etc.) and it can separately validate the properties of a " [:em "collection"] " (i.e., vector, map, list, set, etc.) itself, such as its size, the position of particular elements, and the relationships between elements, etc."]

            [:p "This example could certainly be validated as we've seen before."]

            [:pre (print-form-then-eval "(valid-scalars? [:a :b :c] [keyword? keyword? keyword?])" 45 45)]

            [:p "Speculoos could also consider the vector as a whole with its collection validation facility."]
            [:pre
             (print-form-then-eval "(require '[speculoos.core :refer [valid-collections? validate-collections]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? [:a :b :c] [#(every? keyword? %)])" 45 45)]

            [:p "In a collection specification, the predicate applies to the collection that contains that predicate."]

            [:p "Speculoos collection specifications work on just about any type of collection."]

            [:pre (print-form-then-eval "(valid-collections? #{5 10 2} #{#(every? number? %)})" 45 45)]

            [:p "Speculoos is not limited in the kinds of predicates we might apply to the collection; any Clojure predicate works."]
            [:pre
             (print-form-then-eval "(def all-vector-entries-distinct? #(apply distinct? %))")
             [:br]
             (print-form-then-eval "(def all-vector-entries-numbers? #(every? number? %))")
             [:br]
             (print-form-then-eval "(def vector-length-3? #(= 3 (count %)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def combo-coll-spec [all-vector-entries-numbers? vector? vector-length-3? all-vector-entries-distinct?])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? [1 2 3] combo-coll-spec)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? #{1 2 3} combo-coll-spec)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? [1 1 1] combo-coll-spec)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(only-invalid (validate-collections [1 2 :a] combo-coll-spec))")]

            [:p "The last example above highlights how " [:code "def"] "-ing our predicates with informative names makes the validation results easier understand. Instead of something inscrutable like " [:code "fn--10774"] ", we'll see the name we gave it, presumably carrying some useful meaning. Helps our future selves understand our present selves' intent, and we just might be able to re-use that specification in " [:a {:href "https://blosavio.github.io/speculoos/perhaps_so.html"} "other contexts"] "."]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Next, " [:code "spec.alpha"] "'s " [:code "tuple"] "."]
            [:pre
             (print-form-then-eval "(s/def :geom/point (s/tuple double? double? double?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/conform :geom/point [1.5 2.5 -0.5])")]]

           [:div.side-by-side
            [:p "Tuples are Speculoos' bread and butter."]
            [:pre (print-form-then-eval "(valid-scalars? [1.5 2.5 -0.5] [double? double? double?])" 45 45)]
            [:p "or"]
            [:pre (print-form-then-eval "(valid-collections? [1.5 2.5 -0.5] [#(every? double? %)])" 45 45)]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Finally, " [:code "spec.alpha"] "'s " [:code "map-of"] "."]
            [:pre
             (print-form-then-eval "(s/def :game/scores (s/map-of string? int?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/conform :game/scores {\"Sally\" 1000, \"Joe\" 500})")]]

           [:div.side-by-side
            [:p "Where Speculoos really takes flight is heterogeneous, arbitrarily-nested collections, but since this document is a comparison to " [:code "spec.alpha"] ", see the Speculoos " [:a {:href "https://github.com/blosavio/speculoos/blob/main/doc/recipes.clj"} "recipes"] " for examples."]

            [:p "Speculoos collection validation works on maps, too."]

            [:pre (print-form-then-eval "(valid-collections? {\"Sally\" 1000, \"Joe\" 500} {:check-keys #(every? string? (keys %)) :check-vals #(every? int? (vals %))})" 45 45)]]]]

         [:section
          [:h2 "Sequences"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " uses regex syntax to describe the structure of sequential data."]
            [:pre
             (print-form-then-eval "(s/def :cook/ingredient (s/cat :quantity number? :unit keyword?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :cook/ingredient [2 :teaspoon])")]]

           [:div.side-by-side
            [:p "Speculoos uses a literal."]
            [:pre
             (print-form-then-eval "(def ingredient-spec [number? keyword?])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? [2 :teaspoon] ingredient-spec)")]

            [:p "Invalid datums are reported like this."]

            [:pre (print-form-then-eval "(only-invalid (validate-scalars [11 \"peaches\"] ingredient-spec))")]

            [:p "Note, 'missing' scalars are not validated as they would be with " [:code "spec.alpha"] "."]

            [:pre (print-form-then-eval "(valid-scalars? [2] ingredient-spec)")]

            [:p "Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "ignores"] " predicates without a corresponding datum. Presence/absence of a datum is a property of the collection, and is therefore handled with a collection specification. Like so…"]

            [:pre
             (print-form-then-eval "(def is-second-kw? #(keyword? (get % 1)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-collections [2] [is-second-kw?])")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Let's look at another " [:code "spec.alpha"] " example."]
            [:pre
             (print-form-then-eval "(s/def :ex/seq-of-keywords (s/* keyword?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/seq-of-keywords [:a :b :c])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/explain :ex/seq-of-keywords [10 20])")]]

           [:div.side-by-side
            [:p "Now, the Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#non-terminating-sequences"} "way"] "."]
            [:pre
             (print-form-then-eval "(def inf-seq-of-keywords-spec (repeat keyword?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? [:a :b :c] inf-seq-of-keywords-spec)" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-scalars [10 20] inf-seq-of-keywords-spec)" 45 45)]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] "…"]
            [:pre
             (print-form-then-eval "(s/def :ex/odds-then-maybe-even (s/cat :odds (s/+ odd?) :even (s/? even?)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/odds-then-maybe-even [1 3 5 100])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/odds-then-maybe-even [1])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/explain :ex/odds-then-maybe-even [100])")]]

           [:div.side-by-side
            [:p "Speculoos…"]
            [:pre
             (print-form-then-eval "(def odds-then-maybe-even-spec #(and (<= 2 (count (partition-by odd? %))) (every? odd? (first (partition-by odd? %)))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? [1 3 5 100] [odds-then-maybe-even-spec])" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-collections [1] [odds-then-maybe-even-spec])" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-collections [100] [odds-then-maybe-even-spec])" 45 45)]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Here's a " [:code "spec.alpha"] " demonstration of opts that are alternating keywords and booleans."]
            [:pre
             (print-form-then-eval "(s/def :ex/opts (s/* (s/cat :opt keyword? :val boolean?)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/opts [:silent? false :verbose true])")]]

           [:div.side-by-side
            [:p "Speculoos' way to do the same."]
            [:pre
             (print-form-then-eval "(def alt-kw-bool-spec (cycle [keyword? boolean?]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? [:silent false :verbose true] alt-kw-bool-spec)" 45 45)]]]


          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Finally, " [:code "spec.alpha"] " specifies alternatives like this."]
            [:pre
             (print-form-then-eval "(s/def :ex/config (s/* (s/cat :prop string? :val  (s/alt :s string? :b boolean?))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/config [\"-server\" \"foo\" \"-verbose\" true \"-user\" \"joe\"])" 33 45)]]

           [:div.side-by-side
            [:p "We'd do this in Speculoos."]
            [:pre
             (print-form-then-eval "(def config-spec (cycle [string? #(or (string? %) (boolean? %))]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? [\"-server\" \"foo\" \"-verbose\" true \"-user\" \"joe\"] config-spec)" 38 45)]]]

          [:p [:code "spec.alpha"] " provides the " [:code "describe"] " function to retrieve a spec's description. Speculoos trusts our dev environment to find and show us the definitions."]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " created a provincial " [:code "&"] "."]
            [:pre
             (print-form-then-eval "(s/def :ex/even-strings (s/& (s/* string?) #(even? (count %))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\"])")
             [:br]
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\" \"b\"])")
             [:br]
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\" \"b\" \"c\"])")
             [:br]
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\" \"b\" \"c\" \"d\"])")]]

           [:div.side-by-side
            [:p "Speculoos uses " [:code "clojure.core/and"] "."]
            [:pre
             (print-form-then-eval "(def even-string-spec #(and (even? (count %)) (every? string? %)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? [\"a\"] [even-string-spec])")
             [:br]
             (print-form-then-eval "(valid-collections? [\"a\" \"b\"] [even-string-spec])")
             [:br]
             (print-form-then-eval "(valid-collections? [\"a\" \"b\" \"c\"] [even-string-spec])")
             [:br]
             (print-form-then-eval "(valid-collections? [\"a\" \"b\" \"c\" \"d\"] [even-string-spec])")]]]

          [:p "This example reveals a philosophical difference between " [:code "spec.alpha"] " and Speculoos. Here, " [:code "spec.alpha"] " has combined specifying the values of a collection and the count of the collection, a property of the container. Speculoos' opinion is that specifying scalars and collections are separate concerns. For the sake of the compare and contrast, I combined the two validation tests into a single collection predicate, " [:code "even-string-spec"] ", abusing the fact that the container has access to its own contents. But this improperly combines two conceptually distinct operations."]

          [:div.side-by-side-container
           [:div.side-by-side]
           [:div.side-by-side
            [:p "If I weren't trying closely follow along with the " [:code "spec.alpha"] " " [:em "Guide"] " for the sake of a compare-and-constrast, I would have written this."]
            [:pre
             (print-form-then-eval "(valid-scalars? [\"a\" \"b\" \"c\" \"d\"] (repeat string?))" 45 45)
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-collections? [\"a\" \"b\" \"c\" \"d\"] [#(even? (count %))])" 45 45)]

            [:p "Because we'll often want to validate both a scalar specification and a collection specification at the same time, Speculoos provides a convenience function that does both. With a single invocation, " [:code "valid?"] " performs a scalar validation, followed immediately by a collection validation, and then merges the results."]
            [:pre
             (print-form-then-eval "(require '[speculoos.core :refer [valid?]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid? [\"a\" \"b\" \"c\" \"d\"] (repeat string?) [#(even? (count %))])" 45 45)]

            [:p "To entice people to this mindset, I reserved the shortest and most mnemonic function name, " [:code "valid?"] ", for specifying and validating scalars separately from collections."]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Nested collections provide another nice point of comparison. Quoting the " [:a {:href "https://clojure.org/guides/spec#_sequences"} [:em "spec Guide"]] ":"]

            [:blockquote "When [spec.alpha] regex ops are combined, they describe a single sequence. If you need to spec a nested sequential collection, you must use an explicit call to " [:a {:href "https://clojure.github.io/spec.alpha/clojure.spec.alpha-api.html#clojure.spec.alpha/spec"} [:code "spec"]] " to start a new nested regex context."]

            [:pre
             (print-form-then-eval "(s/def :ex/nested (s/cat :names-kw #{:names} :names (s/spec (s/* string?)) :nums-kw #{:nums} :nums (s/spec (s/* number?))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/nested [:names [\"a\" \"b\"] :nums [1 2 3]])" 45 45)]]

           [:div.side-by-side
            [:p "Speculoos was designed from the outset to straightforwardly handle nested collections."]
            [:pre
             (print-form-then-eval "(def scalar-nested-spec [#{:names} (repeat string?) #{:nums} (repeat number?)])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? [:names [\"a\" \"b\"] :nums [1 2 3]] scalar-nested-spec)" 65 45)]]]]

         [:section
          [:h2 "Using spec for validation"]

          [:p "Because " [:code "spec.alpha/conform"] " passes through valid data, we can use its output to filter out data, as seen in the configuration example. In its current implementation, Speculoos' family of " [:code "valid?"] " functions only return " [:code "true/false"] ", so to emulate " [:code "spec.alpha"] ", we'd have to use a pattern such as…"]]

         [:pre [:code "(if (valid? data spec) data :invalid)."]]

         [:section
          [:h2 "Spec'ing functions"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " can define specifications for a function, like this example, which I've merged with a later section of the " [:em "spec Guide"] ", titled " [:em "Instrumentation and Testing"] "."]

            [:pre
             (print-form-then-eval "(defn ranged-rand \"Returns random int in range start <= rand < end\" [start end] (+ start (long (rand (- end start)))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/fdef ranged-rand :args (s/and (s/cat :start int? :end int?) #(< (:start %) (:end %))) :ret int? :fn (s/and #(>= (:ret %) (-> % :args :start)) #(< (:ret %) (-> % :args :end))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(stest/instrument `ranged-rand)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(try (ranged-rand 8 5) (catch Exception e (.getMessage e)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(stest/unstrument `ranged-rand)")]]

           [:div.side-by-side
            [:p "Speculoos provides a pair of corresponding utilities for testing functions. First, " [:code "validate-fn-with"] " wraps a function on-the-fly without mutating the function's " [:code "var"] ". First, I'll demonstrate a valid invocation."]
            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-with]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def second-is-larger-than-first? #(< (get % 0) (get % 1)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-fn-with ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?} 2 12)")]

            [:p "Here, we'll intentionally violate the function's argument collection specification by reversing the order of the arguments, and observe the report."]

            [:pre (print-form-then-eval "(validate-fn-with ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?} 8 5)")]

            [:p "For testing with a higher degree of integration, Speculoos' second function validation option mimics " [:code "spec.alpha/instrument"] ". Instrumented function specifications are gathered from the function's metadata. Speculoos provides a convenience function for injecting specs."]

            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [inject-specs! instrument unstrument]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(inject-specs! ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?})")]

            [:p "Now, we instrument the function…"]

            [:pre [:code "(instrument ranged-rand)"]]

            [:p "…and then test it. Valid inputs return as normal."]

            [:pre (print-form-then-eval "(ranged-rand 5 8)")]

            [:p "Invalid arguments return without halting if the function can successfully complete (as in this scenario), but the invalid message is tossed to " [:code "*out*"] "."]

            [:pre [:code "(with-out-str (ranged-rand 8 5))\n;;=> ({:path [0],\n       :value second-is-larger-than-first?,\n       :datum [8 5],\n       :ordinal-parent-path [],\n       :valid? false})\n"]]

            [:p "Later, we can return the function to it's original state."]

            [:pre [:code "(unstrument ranged-rand)"]]

            [:p "(Compliments to whoever invented the " [:code "unstrument"] " term to compliment " [:code "instrument"] ".)"]]]]


         [:section
          [:h2 "Higher order functions"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " supports validating " [:span.small-caps"hof"] "s like this."]
            [:pre
             (print-form-then-eval "(defn adder [x] #(+ x %))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/fdef adder :args (s/cat :x number?) :ret (s/fspec :args (s/cat :y number?) :ret number?) :fn #(= (-> % :args :x) ((:ret %) 0)))")]]

           [:div.side-by-side
            [:p "Speculoos' version looks like this."]
            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(inject-specs! adder {:speculoos/arg-scalar-spec number? :speculoos/ret-scalar-spec fn? :speculoos/hof-specs {:speculoos/arg-scalar-spec [int?] :speculoos/ret-scalar-spec number?}})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-higher-order-fn adder [5] [10])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-higher-order-fn adder [5] [22/7])")]

            [:p "Speculoos can specify and validate a higher-order-function's arguments and return values to any depth."]]]]

         [:section
          [:h2 "Macros"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] "'s macro analysis is nicely integrated into Clojure's macroexpander."]
            [:pre
             (print-form-then-eval "(s/fdef clojure.core/declare :args (s/cat :names (s/* simple-symbol?)) :ret any?)")
             [:br]
             [:br]
             [:br]
             [:code "(declare 100)\n;; => Call to clojure.core/declare did not conform to spec..."]]]

           [:div.side-by-side
            [:p "Speculoos is more " [:em "ad hoc"] ": macro output is tested the same as any other function."]
            [:pre
             (print-form-then-eval "(defmacro silly-macro [f & args] `(~f ~@args))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(silly-macro + 1 2)")]

            [:p "Speculoos validates macro expansion like this."]

            [:pre
             (print-form-then-eval "(require '[speculoos.core :refer [valid-macro?]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def silly-macro-spec (list symbol? number? number?))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-macro? `(silly-macro + 1 2) silly-macro-spec)")]

            [:p "(" [:code "valid-macro?"] " is a placeholder: I've not written enough macros to know if it's of any use.)"]]]]

         [:section
          [:h2 "Game of cards"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "The " [:em "Guide"] " presents a card game to demonstrate " [:code "spec.alpha"] "."]
            [:pre
             (print-form-then-eval "(def suit? #{:club :diamond :heart :spade})")
             [:br]
             (print-form-then-eval "(def rank? (into #{:jack :queen :king :ace} (range 2 11)))")
             [:br]
             (print-form-then-eval "(def deck (for [suit suit? rank rank?] [rank suit]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :game/card (s/tuple rank? suit?))")
             [:br]
             (print-form-then-eval "(s/def :game/hand (s/* :game/card))")
             [:br]
             (print-form-then-eval "(s/def :game/name string?)")
             [:br]
             (print-form-then-eval "(s/def :game/score int?)")
             [:br]
             (print-form-then-eval "(s/def :game/player (s/keys :req [:game/name :game/score :game/hand]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/def :game/players (s/* :game/player))")
             [:br]
             (print-form-then-eval "(s/def :game/deck (s/* :game/card))")
             [:br]
             (print-form-then-eval "(s/def :game/game (s/keys :req [:game/players :game/deck]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def kenny {:game/name \"Kenny Rogers\" :game/score 100 :game/hand []})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :game/player kenny)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/explain-data :game/game {:game/deck deck :game/players [{:game/name \"Kenny Rogers\" :game/score 100 :game/hand [[2 :banana]]}]})" 55 60)]]

           [:div.side-by-side
            [:p "Let's follow along, methodically building up the equivalent Speculoos specification."]
            [:pre
             (print-form-then-eval "(def suits #{:club :diamond :heart :spade})")
             [:br]
             (print-form-then-eval "(def ranks (into #{:jack :queen :king :ace} (range 2 11)))")
             [:br]
             (print-form-then-eval "(def deck (vec (for [s suits r ranks] [r s])))")
             [:br]
             (print-form-then-eval "(def card-spec [ranks suits])")
             [:br]
             (print-form-then-eval "(def deck-spec (repeat card-spec))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? deck deck-spec)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def player-spec {:name string? :score int? :hand (repeat card-spec)})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def kenny {:name \"Kenny Rogers\" :score 100 :hand []})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? kenny player-spec)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defn draw-hand [] (vec (take 5 (repeatedly #(first (shuffle deck))))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def players-spec (repeat player-spec))")
             [:br]
             (print-form-then-eval "(def players [kenny {:name \"Humphrey Bogart\" :score 188 :hand (draw-hand)} {:name \"Julius Caesar\" :score 77 :hand (draw-hand)}])")
             [:br]
             [:br]
             [:br]
             [:code "(validate-scalars (:hand (players 1)) (repeat card-spec)) ;; => lengthy output..."]
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? (:hand (players 1)) (repeat card-spec))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? players players-spec)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def game [deck players])")
             [:br]
             (print-form-then-eval "(def game-spec [deck-spec players-spec])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? game game-spec)")]

            [:p "What happens when we have bad data?"]
            [:pre
             (print-form-then-eval "(def corrupted-game (assoc-in game [1 0 :hand 0] [2 :banana]))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(only-invalid (validate-scalars corrupted-game game-spec))" 55 55)]

            [:p "Speculoos reports an invalid datum " [:code ":banana"] " according to predicate " [:code "suits"] " located at path " [:code "[1 0 :hand 0 1]"] ", which we can inspect with " [:a {:href "https://github.com/blosavio/fn-in"} [:code "get-in*"]] " and similar functions."]]]]

         [:section
          [:h2 "Generators"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "The " [:em "spec Guide"] " emphasizes that one of " [:code "spec.alpha"] "'s explicit design goals is to facilitate property-based testing. " [:code "spec.alpha"] " does this by closely cooperating with " [:code "test.check"] ", which generates sample data that conforms to the spec. Next, we'll see a few examples of these capabilities by generating sample data from the card game specs."]

            [:pre
             (print-form-then-eval "(gen/sample (s/gen #{:club :diamond :heart :spade}))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/generate (s/gen :game/player))")]]

           [:div.side-by-side
            [:p "Speculoos provides a rudimentary version that mimics this functionality. Because " [:code "game-spec"] " is composed of infinitely-repeating sequences, let's create a simplified version that terminates, using the basic " [:code "test.check"] " generators. Speculoos cannot in all instances automatically pull apart a compound predicate such as " [:code "#(and (int? %) (< % 10))"] " in order to compose a generator."]

            [:pre
             (print-form-then-eval "(require '[speculoos.utility :refer [data-from-spec]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(data-from-spec game-spec :random)")]

            [:p "Automatically setting up generators and property-based testing is the main area where Speculoos lags " [:code "spec.alpha"] ". I do not yet have a great idea on how to automatically pull apart compound, composed predicates. See the " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#exercising"} "docs"] ", " [:a {:href "https://blosavio.github.io/speculoos/speculoos.utility.html#var-inspect-fn"} [:span.small-caps "api"]] " and a " [:a {:href "#and"} "later subsection"] " to see how to manually or semi-automatically add generators into the predicate metadata."]

            [:p "Let's follow along as best as we can…"]
            [:pre
             (print-form-then-eval "(data-from-spec [int?] :random)")
             [:br]
             (print-form-then-eval "(data-from-spec [nil?])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(repeatedly 5 #(data-from-spec [string?] :random))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(repeatedly 3 #(data-from-spec (into [keyword?] (repeat 3 double?)) :random))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(data-from-spec player-spec :random)")]

            [:p "The card game specifications refer to earlier sections."]]]]

         [:section
          [:h2 "Exercise"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] "'s data-generating capabilities allows us to " [:em "exercise"] " a function by invoking it with generated arguments."]
            [:pre
             (print-form-then-eval "(s/exercise (s/cat :k keyword? :ns (s/+ number?)) 5)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/exercise (s/or :k keyword? :s string? :n number?) 5)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/exercise-fn `ranged-rand)")]]

           [:div.side-by-side
            [:p "Speculoos mimics the " [:code "exercise"] " function, but (for now) only exercises a scalar specification."]
            [:pre
             (print-form-then-eval "(require '[speculoos.utility :refer [exercise]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(exercise [int? string? boolean? char?] 5)" 55 55)]

            [:p "Speculoos also mimics " [:code "spec.alpha"] "'s " [:code "exercise-fn"] ", again only for scalar specifications, on the function's arguments."]

            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [exercise-fn]])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(inject-specs! ranged-rand {:speculoos/arg-scalar-spec [int? int?]})")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(exercise-fn ranged-rand 5)")]]]]


         [:section#and
          [:h2 [:code "s/and"] " generators"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "In certain cases, a spec will require the data to fall within a very small range of possible values, such as " [:em "an even positive integer, divisible by three, less than 31, greater than 12."] " The generators are not likely to be able to produce multiple conforming samples using only " [:code "(s/gen int?)"] ", so we construct predicates with " [:code "spec.alpha"] "'s " [:code "and"] "."]

            [:pre
             (print-form-then-eval "(gen/generate (s/gen (s/and int? even?)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defn divisible-by [n] #(zero? (mod % n)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/sample (s/gen (s/and int? #(> % 0) (divisible-by 3))))")]]

           [:div.side-by-side
            [:p "Right now, Speculoos cannot " [:em "automatically"] " dive into a compound predicate such as " [:code "#(and (int? %) (even? %))"] " to create a competent generator, but it does offer a few options. First, we could manually compose a random sample generator and attach it the predicate's metadata. We may use whatever generator we prefer; " [:code "test.check.generators"] " work well."]

            [:pre
             (print-form-then-eval "(require '[speculoos.utility :refer [defpred validate-predicate->generator]]
                                              '[clojure.test.check.generators :as tc-gen])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(defn gen-int-pos-div-by-3 [] (last (tc-gen/sample (tc-gen/fmap #(* % 3) tc-gen/nat) 50)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(def pred-1 (with-meta #(and (int? %) (> % 0) ((divisible-by 3) %)) {:speculoos/predicate->generator gen-int-pos-div-by-3}))")]

            [:p "The " [:code "defpred"] " utility macro does the equivalent when we explicitly supply a sample generator."]

            [:pre
             (print-form-then-eval "(defpred pred-2 #(and (int? %) (> % 0) ((divisible-by 3) %)) gen-int-pos-div-by-3)")
             [:br]
             [:br]
             [:br]
             [:code ";; verify that the samples produced by generator satisfy the predicate"]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator pred-1 5)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator pred-2 5)")]

            [:p "However, if we write our predicate in a way that conforms to " [:code "defpred"] "'s assumptions, it " [:em "will"] " compose a generator automatically."]

            [:pre
             (print-form-then-eval "(defpred pred-3 #(and (int? %) (pos? %) ((divisible-by 3) %)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator pred-3 5)")]

            [:p "This is another area where " [:code "spec.alpha"] "'s approach outclasses Speculoos. Because we write a " [:code "spec.alpha"] " spec in an already 'pulled-apart' state, it can compose a generator starting with the first branch of that compound predicate and then use the following predicates as filters to refine the generated values."]

            [:p "Speculoos consumes predicates as already-defined functions, and it appears fiendishly involved to inspect the internal structure of a function — whose source may not be available — in order to generically extract individual components to an arbitrary nesting depth."]

            [:p "Three questions:"
             [:ol
              [:li "Is this why " [:code "spec.alpha"] " specs are written that way?"]
              [:li "Would it be possible at all to decompose a predicate function object without access to the source?"]
              [:li "If Speculoos never offers fully-automatic sample generation from a given compound predicate, is that deal-breaker for the entire approach?"]]]]]]

         [:section
          [:h2 "Custom generators"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " acknowledges that we may want to generate values by some other means, and thus allows custom generators via " [:code "with-gen"] "."]

            [:pre
             (print-form-then-eval "(s/def :ex/kws (s/with-gen (s/and keyword? #(= (namespace %) \"my.domain\")) #(s/gen #{:my.domain/name :my.domain/occupation :my.domain/id})))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/kws :my.domain/name)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :ex/kws))")]]

           [:div.side-by-side
            [:p "Speculoos considers a free-floating set to be a membership predicate. Speculoos generates sample values by randomly selecting from such a set. We can compose an equivalent set to generate qualified keywords."]

            [:pre
             (print-form-then-eval "(def kw-pred (into #{} (map #(keyword \"my.domain\" %) [\"name\" \"occupation\" \"id\"])))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(valid-scalars? [:my.domain/name] [kw-pred])")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(exercise [kw-pred] 5)")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " provides combinators for creating more complicated generators."]
            [:pre
             (print-form-then-eval "(def kw-gen-3 (gen/fmap #(keyword \"my.domain\" %) (gen/such-that #(not= % \"\") (gen/string-alphanumeric))))")
             [:br]
             [:br]
             [:br]
             [:pre
              [:code "(gen/sample kw-gen-3 5)"]
              [:br]
              [:code ";; => (:my.domain/k
;;     :my.domain/xfm
;;     :my.domain/ey
;;     :my.domain/UkH
;;     :my.domain/UY6)"]]
             ]]

           [:div.side-by-side
            [:p "Speculoos merely relies on " [:code "clojure.core"] " and " [:code "test.check.generators"] " for that task."]

            [:pre
             (print-form-then-eval "(def kw-pred-2 (into #{} (map #(keyword \"my.domain\" %) (gen/sample (gen/such-that #(not= % \"\") (gen/string-alphanumeric))))))")
             [:br]
             [:br]
             [:br]
             [:code "(exercise [kw-pred-2] 5)
;; => ([[:my.domain/9r6] true]
;;     [[:my.domain/djkv9] true]
;;     [[:my.domain/L1i] true]
;;     [[:my.domain/K] true]
;;     [[:my.domain/f] true])"]]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " making a " [:em "hello"] "-string generator."]

            [:pre
             (print-form-then-eval "(s/def :ex/hello (s/with-gen #(clojure.string/includes? % \"hello\") #(gen/fmap (fn [[s1 s2]] (str s1 \"hello\" s2)) (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :ex/hello))")]]

           [:div.side-by-side
            [:p "We could certainly copy-paste that generator and use it as is. Speculoos could also generate a sample string via a regular expression predicate."]

            [:pre (print-form-then-eval "(exercise [#\"\\w{0,3}hello\\w{1,5}\"])")]]]]

         [:section
          [:h2 "Range specs"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Spec-ing and generating a range of integers in " [:code "spec.alpha"] "."]

            [:pre
             (print-form-then-eval "(s/def :bowling/roll (s/int-in 0 11))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :bowling/roll))")]]

           [:div.side-by-side
            [:p "Similar thing in Speculoos."]
            [:pre
             (print-form-then-eval "(defpred bowling-roll #(and (int? %) (<= 0 % 10)) #(last (gen/sample (gen/large-integer* {:min 0 :max 10}))))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator bowling-roll)")]

            [:p "But for integers, nothing beats the succinctness of " [:code "rand-int"] "."]

            [:pre
             (print-form-then-eval "(defpred bowling-roll-2 #(and (int? %) (<= 0 % 10)) #(rand-int 11))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator bowling-roll-2)")]

            [:p "For small group sizes, a set-as-predicate might feel more natural."]

            [:pre (print-form-then-eval "(exercise [(set (range 11))])")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " does a range of " [:code "instant"] "s."]

            [:pre
             (print-form-then-eval "(s/def :ex/the-aughts (s/inst-in #inst \"2000\" #inst \"2010\"))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(drop 50 (gen/sample (s/gen :ex/the-aughts) 55))")]]

           [:div.side-by-side
            [:p "Well, hello. " [:code "test.check"] " does not provide an instance generator for Speculoos to borrow. Lemme reach over into the left-hand column and steal " [:code "spec.alpha"] "'s."]

            [:pre
             (print-form-then-eval "(defpred the-aughts #(instance? java.util.Date %) #(last (gen/sample (s/gen :ex/the-aughts) 55)))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator the-aughts 5)")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Finally, " [:em "The spec Guide"] " illustrates generating doubles with specific conditions."]

            [:pre
             (print-form-then-eval "(s/def :ex/dubs (s/double-in :min -100.0 :max 100.0 :NaN? false :infinite? false))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/dubs 2.9)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(s/valid? :ex/dubs Double/POSITIVE_INFINITY)")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :ex/dubs))")]]

           [:div.side-by-side
            [:p "Speculoos leans on " [:code "test.check.generators"] " for that flexibility."]

            [:pre (print-form-then-eval "(defpred dubs #(and (<= -100 % 100) (not (NaN? %)) (not (infinite? %))) #(gen/generate (gen/double* {:min -100 :max 100 :infinite? false \"NaN?\" true})))")
             [:br]
             [:br]
             [:br]
             (print-form-then-eval "(validate-predicate->generator dubs 10)")]]]]

         [:section
          [:p "Frankly, when I started writing Speculoos, I would have guessed that it could mimic only some fraction of " [:code "spec.alpha"] ". I think this page demonstrates that it can fulfill a decent chunk. Perhaps somebody else beyond me feels that composing specifications the Speculoos way is more intuitive."]
          [:p "Still, Speculoos is very much a proof-of-concept, experimental prototype. Function instrumentation is really rough. Custom generators need more polishing. Many of the bottom-level functions could use attention to performance."]
          [:p [:a {:href "https://github.com/blosavio"} "Let me know"] " what you think."]]
         ]]))