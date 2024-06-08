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
       [:body
        (nav-bar [:code "diff"])
        [:article
         [:h1.wide-title [:code "(diff spec.alpha speculoos)"]]
         [:p "My original goal for the Speculoos project was something like "
          [:br]
          [:em "79.3% the power of spec, but at least the composition is simpler!"]
          [:br]
          "Here's a side-by-side follow along of the "
          [:a {:href "https://clojure.org/guides/spec"} "spec " [:em "Guide"]]
          "."]

         [:section
          [:h2 "Predicates"]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " predicates are tested with "
             [:br]
             [:code "(s/conform)"] " or " [:code "(s/valid?)"]]
            [:pre
             (print-form-then-eval "(require '[clojure.spec.alpha :as s])")
             [:br]
             (print-form-then-eval "(s/conform even? 1000)")]]

           [:div.side-by-side
            [:p "Speculoos specifications are bare, unadorned Clojure predicates."]
            [:pre
             (print-form-then-eval "(even? 1000)")
             (print-form-then-eval "(nil? nil)")
             (print-form-then-eval "(#(< % 5) 4)")]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] " provides a special " [:code "def"] " which stores the spec in a central registry."]
            [:pre
             (print-form-then-eval "(s/def :order/date inst?)")
             (print-form-then-eval "(s/def :deck/suit #{:club :diamond :heart :spade})")]]
           [:div.side-by-side
            [:p "Speculoos specifications are " [:code "def"] "-ed and live in your namespace, and are therefore automatically namespace-qualified."]
            [:pre
             (print-form-then-eval "(def date inst?)")
             (print-form-then-eval "(def suit #{:club :diamond :heart :spade})")] 

            [:p "If you like the idea of a spec registry, toss 'em into your own hashmap; Speculoos specifications are just predicates and can be used anywhere"]
            [:pre
             (print-form-then-eval "(import java.util.Date)")
             [:br]
             (print-form-then-eval "(date (Date.))")
             (print-form-then-eval "(suit :club)")
             (print-form-then-eval "(suit :shovel)")]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] " has some slick facilities for automatically creating spec docstrings."]]
           [:div.side-by-side [:p "Speculoos specifications do not have any special docstring features beyond what you explicitly add to your function " [:code "def"] "s."]]]]

         [:section
          [:h2 "Composing Predicates"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " specs are composed with special functions "
             [:br] [:code "(s/and)"] " and " [:code "(s/or)"] "."]
            [:pre
             (print-form-then-eval "(s/def :num/big-even (s/and int? even? #(> % 1000)))")
             [:br]
             (print-form-then-eval "(s/valid? :num/big-even :foo)")
             (print-form-then-eval "(s/valid? :num/big-even 10)")
             (print-form-then-eval "(s/valid? :num/big-even 100000)")
             [:br]
             (print-form-then-eval "(s/def :domain/name-or-id (s/or :name string? :id   int?))")
             [:br]
             (print-form-then-eval "(s/valid? :domain/name-or-id \"abc\")")
             (print-form-then-eval "(s/valid? :domain/name-or-id 100)")
             (print-form-then-eval "(s/valid? :domain/name-or-id :foo)")]]

           [:div.side-by-side
            [:p "Speculoos specifications are composed with " [:code "clojure.core/and"] " and "
             [:br] [:code "clojure.core/or"] "."]
            [:pre
             (print-form-then-eval "(def big-even #(and (int? %) (even? %) (> % 1000)))")
             [:br]
             (print-form-then-eval "(big-even :foo)")
             (print-form-then-eval "(big-even 10)")
             (print-form-then-eval "(big-even 10000)")
             [:br]
             (print-form-then-eval "(def name-or-id #(or (string? %) (int? %)))")
             [:br]
             (print-form-then-eval "(name-or-id \"abc\")")
             (print-form-then-eval "(name-or-id 100)")
             (print-form-then-eval "(name-or-id :foo)")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " annotates branches with keywords (" [:code ":name"] " and " [:code ":id"] "), used to return " [:em "conformed"] " data."]]

           [:div.side-by-side
            [:p "Speculoos uses a " [:a {:href "documentation.html#fn-in*"} "different strategy"] " to refer to datums."]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " provides a helper to include " [:code "nil"] " as a valid value"]
            [:pre
             (print-form-then-eval "(s/valid? string? nil)")
             (print-form-then-eval "(s/valid? (s/nilable string?) nil)")]]

           [:div.side-by-side
            [:p "Simply compose to make a speculoos predicate nilable."]
            [:pre (print-form-then-eval "(#(or (string? %) (nil? %)) nil)")]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] "'s " [:code "explain"] " provides a nice report for non-conforming simple predicates."]]
           [:div.side-by-side [:p "Speculoos returns only " [:code "true/false"] " for simple predicates. Later, we'll see how Speculoos "
                               [:em "does"] " produce a detailed report for composite values."]]]]

         [:section
          [:h2 "Entity Maps"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Here is " [:code "spec.alpha"] " in action."]
            [:pre
             (print-form-then-eval "(def email-regex #\"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,63}$\")")
             [:br]
             (print-form-then-eval "(s/def :acct/email-type (s/and string? #(re-matches email-regex %)))")
             [:br]
             (print-form-then-eval "(s/def :acct/acctid int?)")
             (print-form-then-eval "(s/def :acct/first-name string?)")
             (print-form-then-eval "(s/def :acct/last-name string?)")
             (print-form-then-eval "(s/def :acct/email :acct/email-type)")
             [:br]
             (print-form-then-eval "(s/def :acct/person (s/keys :req [:acct/first-name :acct/last-name :acct/email] :opt [:acct/phone]))")]]

           [:div.side-by-side
            [:p "Here is the same process in Speculoos, re-using the regex."
             (label "skip")
             (side-note "skip" (h2/html "The Spec 'Guide' does not appear to use " [:code ":acct/acctid"] ", so I will skip it."))]
            [:pre
             (print-form-then-eval "(def email-spec #(and (string? %) (re-matches email-regex %)))")
             [:br]
             (print-form-then-eval "(def person-spec {:first-name string? :last-name string? :email email-spec :phone any?})")
             [:br]
             (print-form-then-eval "(require '[speculoos.core :refer [valid-scalar-spec? validate-scalar-spec only-invalid]])")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"bugs@example.com\"} person-spec)")]

            [:p "Speculoos checks only keys that are in both the data and the specification. If you don't want to validate a particular entry, you can, on-the-fly, dissociate that key-val from the specification."]

            [:pre (print-form-then-eval "(valid-scalar-spec? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"not@even@close@to@a@valid@email\"} (dissoc person-spec :email))")]

            [:p "If you want to merely relax a specification, simply associate a new, more permissive predicate."]
            [:pre (print-form-then-eval "(valid-scalar-spec? {:first-name \"Bugs\" :last-name \"Bunny\" :email :not-an-email} (assoc person-spec :email #(string? %)))")]

            [:p "Note the function name: Speculoos " [:a {:href "ideas.html#separate"} "distinguishes"] " validating " [:em "scalars"] " (i.e., numbers, strings, characters, etc.) from " [:em "collections"] " (vectors, lists, maps, sets). Speculoos provides a corresponding group of functions specifying collection counts, presence of keys, set memebership, etc."]

            [:pre (print-form-then-eval "(valid-scalar-spec? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"}  person-spec)")]

            [:p "Instead of using " [:code "valid…?"] " and friends, Speculoos' " [:code "validate…*"] " family of functions show the details of the validating each datum."]
            [:pre (print-form-then-eval "(validate-scalar-spec {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"} person-spec)")]


            [:p "The validation results can grow unwieldy with large data and specifications, so Speculoos provides some helper functions to quickly focus on points of interest, i.e., non-valid specs."]
            [:pre (print-form-then-eval "(only-invalid (validate-scalar-spec {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"} person-spec))")]]]

          [:div.side-by-side-container
           [:div.side-by-side [:p [:code "spec.alpha"] " distinguishes unqualified keys and fully-namespaced keys, and allows you to explicitly declare one or the other."]]
           [:div.side-by-side
            [:p "Speculoos implicitly distinguishes qualified from unqualified keys because " [:code "(not= :k ::k)."]]

            [:p "Observe. Qualified keys in data, unqualified keys in specification, no matches…"]
            [:pre (print-form-then-eval "(validate-scalar-spec {::a 42 ::b \"abc\" ::c :foo} {:a int? :b string? :c keyword?})")]

            [:p "…qualified keys in both data and specification, validation succeeds…"]
            [:pre (print-form-then-eval "(valid-scalar-spec? {::a 42 ::b \"abc\" ::c :foo} {::a int? ::b string? ::c keyword?})")]

            [:p "…unqualified keys in both data and specification, validation succeeds."]
            [:pre (print-form-then-eval "(valid-scalar-spec? {:a 42 :b \"abc\" :c :foo} {:a int? :b string? :c keyword?})")]]]]

         [:div.side-by-side-container
          [:div.side-by-side
           [:p [:code "spec.alpha"] " handles keyword args like this:"]
           [:pre
            (print-form-then-eval "(s/def :my.config/port number?)")
            (print-form-then-eval "(s/def :my.config/host string?)")
            (print-form-then-eval "(s/def :my.config/id keyword?)")
            (print-form-then-eval "(s/def :my.config/server (s/keys* :req [:my.config/id :my.config/host] :opt [:my.config/port]))")
            [:br]
            (print-form-then-eval "(s/conform :my.config/server [:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555])")]]
          [:div.side-by-side
           [:p "Speculoos does it this way:"]
           [:pre
            (print-form-then-eval "(def port number?)")
            (print-form-then-eval "(def host string?)")
            (print-form-then-eval "(def id keyword?)")
            (print-form-then-eval "(def server-spec {:my.config/id id :my.config/host host :my.config/port port})")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? {:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555} server-spec)")]

           [:p "The principle of Speculoos' validation is that if the key exists in both the data and specification, then Speculoos will apply the predicate to the datum. This fulfills the criteria of " [:em "Thing may or may not exist, but if Thing " [:strong "does"] " exist, it must satisfy this predicate."]]

           [:p "If we want to similarly validate a sequential data structure, it goes like this:"]
           [:pre
            (print-form-then-eval "(def server-data-2 [:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555])")
            [:br]
            (print-form-then-eval "(def server-spec-2 [#(= % :my.config/id) id #(= % :my.config/host) host #(= % :my.config/port) port])")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? server-data-2 server-spec-2)")]]]

         [:div.side-by-side-container
          [:div.side-by-side
           [:p [:code "spec.alpha"] " has a " [:code "merge"] " function."]
           [:pre
            (print-form-then-eval "(s/def :animal/kind string?)")
            (print-form-then-eval "(s/def :animal/says string?)")
            (print-form-then-eval "(s/def :animal/common (s/keys :req [:animal/kind :animal/says]))")
            [:br]
            (print-form-then-eval "(s/def :dog/tail? boolean?)")
            (print-form-then-eval "(s/def :dog/breed string?)")
            (print-form-then-eval "(s/def :animal/dog (s/merge :animal/common (s/keys :req [:dog/tail? :dog/breed])))")
            [:br]
            (print-form-then-eval "(s/valid? :animal/dog {:animal/kind \"dog\" :animal/says \"woof\" :dog/tail? true :dog/breed \"retriever\"})")]]

          [:div.side-by-side
           [:p "Speculoos simply uses Clojure's powerful data manipulation functions."]
           [:pre
            (print-form-then-eval "(def animal-kind string?)")
            (print-form-then-eval "(def animal-says string?)")
            (print-form-then-eval "(def animal-spec {:kind animal-kind :says animal-says})")
            [:br]
            (print-form-then-eval "(def dog-spec (merge animal-spec {:tail boolean? :breed string?}))")
            [:br]
            (print-form-then-eval "(def dog-data {:kind \"dog\" :says \"woof\" :tail true :breed \"retriever\"})")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? dog-data dog-spec)")]]]

         [:section
          [:h2 "Multi-spec"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " has the capability to dispatch validation paths according to an in-band key. Here's the " [:em "Guide"] "'s demo."]
            [:pre
             (print-form-then-eval "(s/def :event/type keyword?)")
             (print-form-then-eval "(s/def :event/timestamp int?)")
             (print-form-then-eval "(s/def :search/url string?)")
             (print-form-then-eval "(s/def :error/message string?)")
             (print-form-then-eval "(s/def :error/code int?)")
             [:br]
             (print-form-then-eval "(defmulti event-type :event/type)")
             (print-form-then-eval "(defmethod event-type :event/search [_] (s/keys :req [:event/type :event/timestamp :search/url]))")
             [:br]
             (print-form-then-eval "(defmethod event-type :event/error [_] (s/keys :req [:event/type :event/timestamp :error/message :error/code]))")
             [:br]
             (print-form-then-eval "(s/def :event/event (s/multi-spec event-type :event/type))")
             [:br]
             (print-form-then-eval "(s/valid? :event/event {:event/type :event/search :event/timestamp 1463970123000 :search/url \"https://clojure.org\"})")
             [:br]
             (print-form-then-eval "(s/valid? :event/event {:event/type :event/error :event/timestamp 1463970123000 :error/message \"Invalid host\" :error/code 500})")
             [:br]
             (print-form-then-eval "(s/explain :event/event {:event/type :event/restart})")]]

           [:div.side-by-side
            [:p "Since Speculoos consumes regular old Clojure data structures and functions, they work similarly. Instead of " [:code "def"] "-ing a series of separate predicates, for brevity, I'll inject them directly into the specification definition, but Speculoos could handle any level of indirection."]
            [:pre
             (print-form-then-eval "(defmulti event-type :event/type)")
             (print-form-then-eval "(defmethod event-type :event/search [_] {:event/type keyword? :event/timestamp int? :search/url string?})")
             [:br]
             (print-form-then-eval "(defmethod event-type :event/error [_] {:event/type keyword? :event/timestamp int? :error/message string? :error/code int?})")
             [:br]
             (print-form-then-eval "(def event-1 {:event/type :event/search :event/timestamp 1463970123000 :event/url \"https://clojure.org\"})")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? event-1 (event-type event-1))")
             [:br]
             (print-form-then-eval "(def event-2 {:event/type :event/error :event/timestamp 1463970123000 :error/message \"Invalid host\" :code 500})")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? event-2 (event-type event-2))")
             [:br]
             (print-form-then-eval "(def event-3 {:event/type :restart})")
             [:br]
             (print-form-then-eval "(try (valid-scalar-spec? event-3 (event-type event-3)) (catch Exception e (.getMessage e)))")
             [:br]
             (print-form-then-eval "(def event-4 {:event/type :event/search :search/url 200})")
             [:br]
             (print-form-then-eval "(only-invalid (validate-scalar-spec event-4 (event-type event-4)))")]]]

          [:p "Here we see a significant difference between " [:code "spec.alpha"] " and Speculoos: the former fails the validation because " [:code "event-4"] " is missing the " [:code ":timestamp"] " key. Speculoos considers the presence or absence of a map's key to be a property of the collection. Within that philosophy, such a specification would properly belong in a Speculoos " [:em "collection spec"] "."]]

         [:section
          [:h2 "Collections"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " provides a trio of helper functions for collections. First, " [:code "coll-of"] "."]
            [:pre
             (print-form-then-eval "(s/conform (s/coll-of keyword?) [:a :b :c])")
             (print-form-then-eval "(s/conform (s/coll-of number?) #{5 10 2})")
             [:br]
             (print-form-then-eval "(s/def :ex/vnum3 (s/coll-of number? :kind vector? :count 3 :distinct true :into #{}))")
             [:br]
             (print-form-then-eval "(s/conform :ex/vnum3 [1 2 3])")]]

           [:div.side-by-side
            [:p "Speculoos was designed from the start to specify collections. Speculoos validates collections in two different ways: it can validate " [:em "scalars"] ", atomic, inidvisible values (i.e., numbers, booleans, etc.) and it can separately validate the properties of a " [:em "collection"] " (i.e., vector, map, list, set, etc.) itself, such as its size, the position of particular elements, and the relationships between elements, etc."]
            [:p "This example could certainly be validated as we've seen before."]
            [:pre (print-form-then-eval "(valid-scalar-spec? [:a :b :c] [keyword? keyword? keyword?])")]

            [:p "Speculoos can also consider the vector as a whole with its collection validation facility."]
            [:pre
             (print-form-then-eval "(require '[speculoos.core :refer [valid-collection-spec? validate-collection-spec]])")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [:a :b :c] [#(every? keyword? %)])")]

            [:p "In a collection spec, the predicate applies to the collection that contains that predicate."]
            
            [:p "Speculoos collection specs work on just about any type of collection."]
            [:pre (print-form-then-eval "(valid-collection-spec? #{5 10 2} #{#(every? number? %)})")]

            [:p "Speculoos is not limited in the kinds of predicates you might apply to the collection; any Clojure predicate works."]
            [:pre
             (print-form-then-eval "(def all-vector-entries-distinct? #(apply distinct? %))")
             (print-form-then-eval "(def all-vector-entries-numbers? #(every? number? %))")
             (print-form-then-eval "(def vector-length-3? #(= 3 (count %)))")
             [:br]
             (print-form-then-eval "(def combo-coll-spec [all-vector-entries-numbers? vector? vector-length-3? all-vector-entries-distinct?])")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [1 2 3] combo-coll-spec)")
             [:br]
             (print-form-then-eval "(valid-collection-spec? #{1 2 3} combo-coll-spec)")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [1 1 1] combo-coll-spec)")
             [:br]
             (print-form-then-eval "(only-invalid (validate-collection-spec [1 2 :a] combo-coll-spec))")]

            [:p "The last example above highlights how " [:code "def"] "-ing your predicates with informative names makes the validation results easier understand. Instead of something inscrutable like " [:code "fn--10774"] ", you'll see the name you gave it, presumably carring some useful meaning. Helps your future self understand your present self's intent, and you just might be able to re-use that specification in other contexts."]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Next, " [:code "spec.alpha"] "'s " [:code "tuple"] "."]
            [:pre
             (print-form-then-eval "(s/def :geom/point (s/tuple double? double? double?))")
             [:br]
             (print-form-then-eval "(s/conform :geom/point [1.5 2.5 -0.5])")]]
           [:div.side-by-side
            [:p "Tuples are Speculoos' bread and butter."]
            [:pre (print-form-then-eval "(valid-scalar-spec? [1.5 2.5 -0.5] [double? double? double?])")]
            [:p "or"]
            [:pre (print-form-then-eval "(valid-collection-spec? [1.5 2.5 -0.5] [#(every? double? %)])")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Finally, " [:code "spec.alpha"] "'s " [:code "map-of"] "."]
            [:pre
             (print-form-then-eval "(s/def :game/scores (s/map-of string? int?))")
             [:br]
             (print-form-then-eval "(s/conform :game/scores {\"Sally\" 1000, \"Joe\" 500})")]]

           [:div.side-by-side
            [:p "Where Speculoos really takes flight is heterogenous collections, but since this document is a comparison to " [:code "spec.alpha"] ", see the Speculoos " [:a {:href "recipes.html"} "recipes"] " for examples."]
            [:p "Speculoos collection validation works on maps, too."]
            [:pre (print-form-then-eval "(valid-collection-spec? {\"Sally\" 1000, \"Joe\" 500} {:check-keys #(every? string? (keys %)) :check-vals #(every? int? (vals %))})")]]]]

         [:section
          [:h2 "Sequences"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " uses regex syntax to describe the structure of sequential data."]
            [:pre
             (print-form-then-eval "(s/def :cook/ingredient (s/cat :quantity number? :unit keyword?))")
             [:br]
             (print-form-then-eval "(s/valid? :cook/ingredient [2 :teaspoon])")]]

           [:div.side-by-side
            [:p "Speculoos uses a literal."]
            [:pre
             (print-form-then-eval "(def ingredient-spec [number? keyword?])")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? [2 :teaspoon] ingredient-spec)")]
            [:p "Invalid datums are reported."]
            [:pre (print-form-then-eval "(only-invalid (validate-scalar-spec [11 \"peaches\"] ingredient-spec))")]
            [:p "Note, 'missing' scalars are not validated as they would be with " [:code "spec.alpha"] "."]
            [:pre (print-form-then-eval "(valid-scalar-spec? [2] ingredient-spec)")]
            [:p "Speculoos assumes that you didn't want to test non-present predicates. Presence/absence of a datum is a property of the collection, and is thus handled with a collection spec."]
            [:pre
             (print-form-then-eval "(def is-second-kw? #(keyword? (get % 1)))")
             [:br]
             (print-form-then-eval "(validate-collection-spec [2] [is-second-kw?])")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Let's look at another " [:code "spec.alpha"] " example."]
            [:pre
             (print-form-then-eval "(s/def :ex/seq-of-keywords (s/* keyword?))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/seq-of-keywords [:a :b :c])")
             (print-form-then-eval "(s/explain :ex/seq-of-keywords [10 20])")]]

           [:div.side-by-side
            [:p "Now, the Speculoos way."]
            [:pre
             (print-form-then-eval "(def inf-seq-of-keywords-spec (repeat keyword?))")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? [:a :b :c] inf-seq-of-keywords-spec)")
             [:br]
             (print-form-then-eval "(validate-scalar-spec [10 20] inf-seq-of-keywords-spec)")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] "…"]
            [:pre
             (print-form-then-eval "(s/def :ex/odds-then-maybe-even (s/cat :odds (s/+ odd?) :even (s/? even?)))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/odds-then-maybe-even [1 3 5 100])")
             (print-form-then-eval "(s/valid? :ex/odds-then-maybe-even [1])")
             (print-form-then-eval "(s/explain :ex/odds-then-maybe-even [100])")]]

           [:div.side-by-side
            [:p "Speculoos…"]
            [:pre
             (print-form-then-eval "(def odds-then-maybe-even-spec #(and (<= 2 (count (partition-by odd? %))) (every? odd? (first (partition-by odd? %)))))")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [1 3 5 100] [odds-then-maybe-even-spec])")
             [:br]
             (print-form-then-eval "(validate-collection-spec [1] [odds-then-maybe-even-spec])")
             [:br]
             (print-form-then-eval "(validate-collection-spec [100] [odds-then-maybe-even-spec])")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Here's a " [:code "spec.alpha"] " demonstration of opts that are alternating keywords and booleans."]
            [:pre
             (print-form-then-eval "(s/def :ex/opts (s/* (s/cat :opt keyword? :val boolean?)))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/opts [:silent? false :verbose true])")]]

           [:div.side-by-side
            [:p "Speculoos' way to do the same."]
            [:pre
             (print-form-then-eval "(def alt-kw-bool-spec (cycle [keyword? boolean?]))")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? [:silent false :verbose true] alt-kw-bool-spec)")]]]


          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Finally, " [:code "spec.alpha"] " specifies alternatives like this."]
            [:pre
             (print-form-then-eval "(s/def :ex/config (s/* (s/cat :prop string? :val  (s/alt :s string? :b boolean?))))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/config [\"-server\" \"foo\" \"-verbose\" true \"-user\" \"joe\"])")]]

           [:div.side-by-side
            [:p "We'd do this in Speculoos."]
            [:pre
             (print-form-then-eval "(def config-spec (cycle [string? #(or (string? %) (boolean? %))]))")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? [\"-server\" \"foo\" \"-verbose\" true \"-user\" \"joe\"] config-spec)")]]]

          [:p [:code "spec.alpha"] " provides the " [:code "describe"] " function to retrieve a spec's description. Speculoos trusts your dev environment to find and show you the definitions."]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " created a provincial " [:code "&"] "."]
            [:pre
             (print-form-then-eval "(s/def :ex/even-strings (s/& (s/* string?) #(even? (count %))))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\"])")
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\" \"b\"])")
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\" \"b\" \"c\"])")
             (print-form-then-eval "(s/valid? :ex/even-strings [\"a\" \"b\" \"c\" \"d\"])")]]

           [:div.side-by-side
            [:p "Speculoos uses " [:code "clojure.core/and"] "."]
            [:pre
             (print-form-then-eval "(def even-string-spec #(and (even? (count %)) (every? string? %)))")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [\"a\"] [even-string-spec])")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [\"a\" \"b\"] [even-string-spec])")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [\"a\" \"b\" \"c\"] [even-string-spec])")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [\"a\" \"b\" \"c\" \"d\"] [even-string-spec])")]]]

          [:p "This example exposes a philosophical difference between " [:code "spec.alpha"] " and Speculoos. Here, " [:code "spec.alpha"] " has combined specifying the values of a collection and the count of the collection, a property of the container. Speculoos' opinion is that specifying values and collections are separate concerns. For the sake of the compare and constrast, I combined the two validation tests into a single collection specification, abusing the fact that the container has access to its own contents. But this improperly combines two conceptually distinct operations."]


          [:div.side-by-side-container
           [:div.side-by-side]
           [:div.side-by-side
            [:p "If I weren't constrasting with the " [:code "spec.alpha"] " " [:em "Guide"] ", I would have written this."]
            [:pre
             (print-form-then-eval "(valid-scalar-spec? [\"a\" \"b\" \"c\" \"d\"] (repeat string?))")
             [:br]
             (print-form-then-eval "(valid-collection-spec? [\"a\" \"b\" \"c\" \"d\"] [#(even? (count %))])")]

            [:p "Because we'll often want to validate both a scalar specification and a collection specification at the same time, Speculoos provides a convenience function that does both."]
            [:pre
             (print-form-then-eval "(require '[speculoos.core :refer [valid?]])")
             [:br]
             (print-form-then-eval "(valid? [\"a\" \"b\" \"c\" \"d\"] (repeat string?) [#(even? (count %))])")]

            [:p "As a testament to my belief in the importance of separating scalar and collection specifications, I reserved the shortest and most mnemonic name, " [:code "valid?"] ", for doing what I contend to be the proper way."]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Nested collections provide another nice point of comparison. Quoting the " [:a {:href "https://clojure.org/guides/spec#_sequences"} [:em "spec Guide"]]]
            [:blockquote "When [spec.alpha] regex ops are combined, they describe a single sequence. If you need to spec a nested sequential collection, you must use an explicit call to spec to start a new nested regex context."]
            [:pre
             (print-form-then-eval "(s/def :ex/nested (s/cat :names-kw #{:names} :names (s/spec (s/* string?)) :nums-kw #{:nums} :nums (s/spec (s/* number?))))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/nested [:names [\"a\" \"b\"] :nums [1 2 3]])")]]

           [:div.side-by-side
            [:p "Speculoos was designed from the outset to straightforwardly handle nested collections."]
            [:pre
             (print-form-then-eval "(def scalar-nested-spec [#{:names} (repeat string?) #{:nums} (repeat number?)])")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? [:names [\"a\" \"b\"] :nums [1 2 3]] scalar-nested-spec)")]]]]

         [:section
          [:h2 "Using spec for validation"]
          [:p "Because " [:code "spec.alpha/conform"] " passes through valid data, you can use its output to filter out data, as seen in the configuration example. In its current implementation, Speculoos' family of " [:code "valid?"] " functions only return " [:code "true/false"] ", so you'd have to use a pattern such as "
           [:br]
           [:code "(if (valid? data spec) data :invalid)."]]]

         [:section
          [:h2 "Spec'ing functions"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " can define specifications for a function, like this example, which I've merged with a later section of the " [:em "Guide"] " titled " [:em "Instrumentation and Testing"] "."]
            [:pre
             (print-form-then-eval "(defn ranged-rand \"Returns random int in range start <= rand < end\" [start end] (+ start (long (rand (- end start)))))")
             [:br]
             (print-form-then-eval "(s/fdef ranged-rand :args (s/and (s/cat :start int? :end int?) #(< (:start %) (:end %))) :ret int? :fn (s/and #(>= (:ret %) (-> % :args :start)) #(< (:ret %) (-> % :args :end))))")
             [:br]
             (print-form-then-eval "(stest/instrument `ranged-rand)")
             [:br]
             (print-form-then-eval "(try (ranged-rand 8 5) (catch Exception e (.getMessage e)))")
             [:br]
             (print-form-then-eval "(stest/unstrument `ranged-rand)")]]

           [:div.side-by-side
            [:p "Speculoos provides a pair of corresponding utilities for testing functions. First, " [:code "validate-fn-with"] " wraps a function on-the-fly without mutating the function's " [:code "var"] ". First, I'll demonstrate a valid invocation."]
            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-with]])")
             [:br]
             (print-form-then-eval "(def second-is-larger-than-first? #(< (get % 0) (get % 1)))")
             [:br]
             (print-form-then-eval "(validate-fn-with ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?} 2 12)")]

            [:p "Here, we'll inentionally violate the function's argument collection specification by reversing the order of the arguments, and observe the report."]
            [:pre (print-form-then-eval "(validate-fn-with ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?} 8 5)")]

            [:p "For testing with a higher degree of integration, Speculoos' second function validation option mimics " [:code "spec.alpha/instrument"] ". Instrumented function specifications are gathered from the function's metadata. Speculoos provides a convenience function for injecting specs."]
            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [inject-specs! instrument unstrument]])")
             [:br]
             (print-form-then-eval "(inject-specs! ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?})")]
            [:p "Now, we instrument the function…"]
            [:pre [:code "(instrument ranged-rand)"]]
            [:p "…and then test it. Valid inputs return as normal."]
            [:pre (print-form-then-eval "(ranged-rand 5 8)")]
            [:p "Invalid arguments possibly return without halting if the function can successfully complete (as in this scenario), but the invalid message is tossed to " [:code "*out*"] "."]
            [:pre [:code "(with-out-str (ranged-rand 8 5))\n;;=> ({:path [0], :value #function[speculoos.compare-spec-alpha/second-is-larger-than-first?], :datum [8 5], :ordinal-parent-path [], :valid? false})\n"]]
            [:p "Later, we can return the function to it's original state."
             (label "compliment")
             (side-note "compliment" (h2/html "My highest compliments to whoever invented the " [:code "unstrument"] " term to compliment " [:code "instrument"] "."))]
            [:pre [:code "(unstrument ranged-rand)"]]]]]

         [:section
          [:h2 "Higher order functions"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " supports validating " [:span.small-caps"hof"] "s like this."]
            [:pre
             (print-form-then-eval "(defn adder [x] #(+ x %))")
             [:br]
             (print-form-then-eval "(s/fdef adder :args (s/cat :x number?) :ret (s/fspec :args (s/cat :y number?) :ret number?) :fn #(= (-> % :args :x) ((:ret %) 0)))")]]

           [:div.side-by-side
            [:p "Speculoos' version looks like this."]
            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")
             [:br]
             (print-form-then-eval "(inject-specs! adder {:speculoos/arg-scalar-spec number? :speculoos/ret-scalar-spec fn? :speculoos/hof-specs {:speculoos/arg-scalar-spec [int?] :speculoos/ret-scalar-spec number?}})")
             [:br]
             (print-form-then-eval "(validate-higher-order-fn adder [5] [10])")
             [:br]
             (print-form-then-eval "(validate-higher-order-fn adder [5] [22/7])")]
            [:p "Speculoos can recursively specify and validate the arguments and returns value of any depth of higher-order functions."]]]]

         [:section
          [:h2 "Macros"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] "'s macro analysis is nicely integrated into Clojure's macroexpander."]
            [:pre
             (print-form-then-eval "(s/fdef clojure.core/declare :args (s/cat :names (s/* simple-symbol?)) :ret any?)")
             [:br]
             [:code "(declare 100)\n;; => Call to clojure.core/declare did not conform to spec..."]]]

           [:div.side-by-side
            [:p "Speculoos is more " [:em "ad hoc"] ": macro output is tested the same as any other function."]
            [:pre
             (print-form-then-eval "(defmacro silly-macro [f & args] `(~f ~@args))")
             [:br]
             (print-form-then-eval "(silly-macro + 1 2)")]
            [:p "Speculoos validates macro expansion like this."
             (label "macro")
             (side-note "macro" (h2/html "(I haven't written enough macros to know if this is of any use. I merely wrote " [:code "valid-macro-spec?"] " as a placeholder.)"))]
            [:pre
             (print-form-then-eval "(require '[speculoos.core :refer [valid-macro-spec?]])")
             [:br]
             (print-form-then-eval "(def silly-macro-spec (list symbol? number? number?))")
             [:br]
             (print-form-then-eval "(valid-macro-spec? `(silly-macro + 1 2) silly-macro-spec)")]]]]

         [:section
          [:h2 "Game of cards"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p "The " [:em "Guide"] " presents a card game to demonstrate " [:code "spec.alpha"] "."]
            [:pre
             (print-form-then-eval "(def suit? #{:club :diamond :heart :spade})")
             (print-form-then-eval "(def rank? (into #{:jack :queen :king :ace} (range 2 11)))")
             (print-form-then-eval "(def deck (for [suit suit? rank rank?] [rank suit]))")
             [:br]
             (print-form-then-eval "(s/def :game/card (s/tuple rank? suit?))")
             (print-form-then-eval "(s/def :game/hand (s/* :game/card))")
             (print-form-then-eval "(s/def :game/name string?)")
             (print-form-then-eval "(s/def :game/score int?)")
             (print-form-then-eval "(s/def :game/player (s/keys :req [:game/name :game/score :game/hand]))")
             [:br]
             (print-form-then-eval "(s/def :game/players (s/* :game/player))")
             (print-form-then-eval "(s/def :game/deck (s/* :game/card))")
             (print-form-then-eval "(s/def :game/game (s/keys :req [:game/players :game/deck]))")
             [:br]
             (print-form-then-eval "(def kenny {:game/name \"Kenny Rogers\" :game/score 100 :game/hand []})")
             [:br]
             (print-form-then-eval "(s/valid? :game/player kenny)")
             [:br]
             (print-form-then-eval "(with-out-str (s/explain :game/game {:game/deck deck :game/players [{:game/name \"Kenny Rogers\" :game/score 100 :game/hand [[2 :banana]]}]}))")]]

           [:div.side-by-side
            [:p "Let's follow along, slowly building up the Speculoos specification."]
            [:pre
             (print-form-then-eval "(def suits #{:club :diamond :heart :spade})")
             (print-form-then-eval "(def ranks (into #{:jack :queen :king :ace} (range 2 11)))")
             (print-form-then-eval "(def deck (vec (for [s suits r ranks] [r s])))")
             (print-form-then-eval "(def card-spec [ranks suits])")
             (print-form-then-eval "(def deck-spec (repeat card-spec))")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? deck deck-spec)")
             [:br]
             (print-form-then-eval "(def player-spec {:name string? :score int? :hand (repeat card-spec)})")
             [:br]
             (print-form-then-eval "(def kenny {:name \"Kenny Rogers\" :score 100 :hand []})")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? kenny player-spec)")
             [:br]
             (print-form-then-eval "(defn draw-hand [] (vec (take 5 (repeatedly #(first (shuffle deck))))))")
             [:br]
             (print-form-then-eval "(def players-spec (repeat player-spec))")
             (print-form-then-eval "(def players [kenny {:name \"Humprey Bogart\" :score 188 :hand (draw-hand)} {:name \"Julius Caesar\" :score 77 :hand (draw-hand)}])")
             [:br]
             [:code "(validate-scalar-spec (:hand (players 1)) (repeat card-spec)) ;; => lengthy output..."]
             [:br]
             (print-form-then-eval "(valid-scalar-spec? (:hand (players 1)) (repeat card-spec))")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? players players-spec)")
             [:br]
             (print-form-then-eval "(def game [deck players])")
             (print-form-then-eval "(def game-spec [deck-spec players-spec])")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? game game-spec)")]

            [:p "What happens when we have bad data?"]
            [:pre
             (print-form-then-eval "(def corrupted-game (assoc-in game [1 0 :hand 0] [2 :banana]))")
             [:br]
             (print-form-then-eval "(only-invalid (validate-scalar-spec corrupted-game game-spec))")]

            [:p "Speculoos reports an invalid datum " [:code ":banana"] " according to predicate " [:code "suits"] " located at path " [:code "[1 0 :hand 0]"] ", which you can inspect with " [:code "get-in*"] " and similar functions."]]]]

         [:section
          [:h2 "Generators"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p "The " [:em "Guide"] " emphasizes that one of " [:code "spec.alpha"] "'s explicit design goals is to facilitate property-based testing. " [:code "spec.alpha"] " does this by tightly cooperating with " [:code "test.check"] ", which generates sample data that conforms to the spec. Next, we'll see a few examples of these capabilities by generating sample data from the card game specs."]
            [:pre
             (print-form-then-eval "(gen/sample (s/gen #{:club :diamond :heart :spade}))")
             [:br]
             (print-form-then-eval "(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))")
             [:br]
             (print-form-then-eval "(gen/generate (s/gen :game/player))")]]

           [:div.side-by-side
            [:p "Speculoos provides a very rudimentary version that mimics this functionality. Because " [:code "game-spec"] " is composed of infinitely-repeating sequences, let's create a simplified version that terminates, using the basic " [:code "test.check"] " generators. Speculoos does not have the ability to pull apart a compound predicate such as " [:code "#(and (int? %) (< % 10))"] " in order to compose a generator."
             (label "dissertation")
             (side-note "dissertation" "Is it even possible? Seems like it would be a PhD dissertation topic…or three.")]
            [:pre
             (print-form-then-eval "(require '[speculoos.utility :refer [data-from-spec]])")
             [:br]
             (print-form-then-eval "(data-from-spec game-spec :random)")]

            [:p "Automatically setting up generators and property-based testing is the main area where Speculoos lags " [:code "spec.alpha"] ". I do not yet have a great idea on how to pull apart compound, composed predicates. Maybe put some example usage into the predicate metadata which would put bounds on the generator search space…"]

            [:p "Let's follow along as best as we can…"]
            [:pre
             (print-form-then-eval "(data-from-spec [int?] :random)")
             (print-form-then-eval "(data-from-spec [nil?])")
             [:br]
             (print-form-then-eval "(repeatedly 5 #(data-from-spec [string?] :random))")
             [:br]
             (print-form-then-eval "(repeatedly 3 #(data-from-spec (into [keyword?] (repeat 3 double?)) :random))")
             [:br]
             (print-form-then-eval "(data-from-spec player-spec :random)")]

            [:p "The card game specifications refer to earlier sections."]]]]

         [:section
          [:h2 "Exercise"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] "'s data-generating capabilities allows you to " [:em "exercise"] " a function by invoking it with generated arguments."]
            [:pre
             (print-form-then-eval "(s/exercise (s/cat :k keyword? :ns (s/+ number?)) 5)")
             [:br]
             (print-form-then-eval "(s/exercise (s/or :k keyword? :s string? :n number?) 5)")
             [:br]
             (print-form-then-eval "(s/exercise-fn `ranged-rand)")
             ]
            ]
           [:div.side-by-side
            [:p "Speculoos mimics the " [:code "exercise"] " function, but (for now) only exercises a scalar specification."]
            [:pre
             (print-form-then-eval "(require '[speculoos.utility :refer [exercise]])")
             [:br]
             (print-form-then-eval "(exercise [int? string? boolean? char?] 5)")]
            [:p "Speculoos also mimics " [:code "spec.alpha"] "'s " [:code "exercise-fn"] ", again only for scalar specifications on the function's arguments."]
            [:pre
             (print-form-then-eval "(require '[speculoos.function-specs :refer [exercise-fn]])")
             [:br]
             (print-form-then-eval "(inject-specs! ranged-rand {:speculoos/arg-scalar-spec [int? int?]})")
             [:br]
             (print-form-then-eval "(exercise-fn ranged-rand 5)")]]]]


         [:section
          [:h2 [:code "s/and"] " generators"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p "In certain cases, a spec will require the data to fall within a very small range of possible values, such as " [:em "an even positive integer, divisible by three, less than 31, greater than 12."] " The generators are not likely to be able to produce multiple conforming samples using only " [:code "(s/gen int?)"] ", so we construct predicates with " [:code "spec.alpha"] "'s " [:code "and"] "."]
            [:pre
             (print-form-then-eval "(gen/generate (s/gen (s/and int? even?)))")
             [:br]
             (print-form-then-eval "(defn divisible-by [n] #(zero? (mod % n)))")
             [:br]
             (print-form-then-eval "(gen/sample (s/gen (s/and int? #(> % 0) (divisible-by 3))))")]]

           [:div.side-by-side
            [:p "Right now, Speculoos cannot " [:em "automatically"] " dive into a compound predicate such as " [:code "#(and (int? %) (even? %))"] " to create a competent generator. You must create it manually and add it the predicate's metadata. You may use whatever generator you prefer; " [:code "test.check.generators"] " works well."]
            [:pre
             (print-form-then-eval "(require '[speculoos.utility :refer [defpred validate-predicate->generator]]
                                              '[clojure.test.check.generators :as tc-gen])")
             [:br]
             (print-form-then-eval "(defn gen-int-pos-div-by-3 [] (last (tc-gen/sample (tc-gen/fmap #(* % 3) tc-gen/nat) 50)))")
             [:br]
             (print-form-then-eval "(def pred-1 (with-meta #(and (int? %) (> % 0) ((divisible-by 3) %)) {:speculoos/predicate->generator gen-int-pos-div-by-3}))")
             [:br]
             [:code ";; helper macro that does the equivalent"]
             (print-form-then-eval "(defpred pred-2 #(and (int? %) (> % 0) ((divisible-by 3) %)) gen-int-pos-div-by-3)")
             [:br]
             [:code ";; verify that the samples prodcued by generator satisfy the predicate"]
             (print-form-then-eval "(validate-predicate->generator pred-1 5)")
             [:br]
             (print-form-then-eval "(validate-predicate->generator pred-2 5)")]

            [:p "This is another area where " [:code "spec.alpha"] "'s approach outclasses Speculoos. Because you write a " [:code "spec.alpha"] " spec in an already 'pulled-apart' state, it can compose a generator starting with the first branch of that compound predicate and then use the following predicates as filters to refine the generated values."]
            [:p "Speculoos consumes predicates as already-defined functions, and it's not apparent to me how to inspect the internal structure of a function — whose source may not be available — to extract individual components of a predicate if I wanted to set bounds on a generator."]
            [:p "Three questions"
             [:ol
              [:li "Is this why " [:code "spec.alpha"] " specs are written that way?"]
              [:li "Would it be possible at all to decompose a defined predicate function?"]
              [:li "If Speculoos never offers fully-automatic sample generation from a given compound predicate, is that deal-breaker for the entire approach?"]]]]]]

         [:section
          [:h2 "Custom generators"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " acknowledges that you may want to generate values by some other means, and thus allows custom generators via " [:code "with-gen"] "."]
            [:pre
             (print-form-then-eval "(s/def :ex/kws (s/with-gen (s/and keyword? #(= (namespace %) \"my.domain\")) #(s/gen #{:my.domain/name :my.domain/occupation :my.domain/id})))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/kws :my.domain/name)")
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :ex/kws))")]]
           [:div.side-by-side
            [:p "Speculoos considers a free-floating set to be a membership predicate. Speculoos generates sample values by randomly selecting from such a set. We can compose an equivalent set to generate qualified keywords."]
            [:pre
             (print-form-then-eval "(def kw-pred (into #{} (map #(keyword \"my.domain\" %) [\"name\" \"occupation\" \"id\"])))")
             [:br]
             (print-form-then-eval "(valid-scalar-spec? [:my.domain/name] [kw-pred])")
             [:br]
             (print-form-then-eval "(exercise [kw-pred] 5)")]]]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " provides combinators for creating more complicated generators."]
            [:pre
             (print-form-then-eval "(def kw-gen-3 (gen/fmap #(keyword \"my.domain\" %) (gen/such-that #(not= % \"\") (gen/string-alphanumeric))))")
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
            [:p "Speculoos merely relies on " [:code "clojure.core"] " and " [:code "test.check.generators"] " for that."]
            [:pre
             (print-form-then-eval "(def kw-pred-2 (into #{} (map #(keyword \"my.domain\" %) (gen/sample (gen/such-that #(not= % \"\") (gen/string-alphanumeric))))))")
             [:br]
             [:code "(exercise [kw-pred-2] 5)
;; => ([[:my.domain/9r6] true]
;;     [[:my.domain/djkv9] true]
;;     [[:my.domain/L1i] true]
;;     [[:my.domain/K] true]
;;     [[:my.domain/f] true])"]]]
           ]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p [:code "spec.alpha"] " making a " [:em "hello"] "-string generator."]
            [:pre
             (print-form-then-eval "(s/def :ex/hello (s/with-gen #(clojure.string/includes? % \"hello\") #(gen/fmap (fn [[s1 s2]] (str s1 \"hello\" s2)) (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))))")
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :ex/hello))")]]
           [:div.side-by-side
            [:p "You could certainly copy-paste that generator and use it as is. Speculoos could also generate a sample string via a regular expression predicate."]
            [:pre (print-form-then-eval "(exercise [#\"\\w{0,3}hello\\w{1,5}\"])")]]]]

         [:section
          [:h2 "Range specs"]
          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Spec-ing and generating a range of integers in " [:code "spec.alpha"] "."]
            [:pre
             (print-form-then-eval "(s/def :bowling/roll (s/int-in 0 11))")
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :bowling/roll))")]]
           
           [:div.side-by-side
            [:p "Similar thing in Speculoos."]
            [:pre
             (print-form-then-eval "(defpred bowling-roll #(and (int? %) (<= 0 % 10)) #(last (gen/sample (gen/large-integer* {:min 0 :max 10}))))")
             [:br]
             (print-form-then-eval "(validate-predicate->generator bowling-roll)")]
            [:p "But for integers, nothing beats the succinctness of " [:code "rand-int"] "."]
            [:pre
             (print-form-then-eval "(defpred bowling-roll-2 #(and (int? %) (<= 0 % 10)) #(rand-int 11))")
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
             (print-form-then-eval "(drop 50 (gen/sample (s/gen :ex/the-aughts) 55))")]]
           [:div.side-by-side
            [:p "Well, hello. " [:code "test.check"] " does not provide an instance generator for Speculoos to borrow. Lemme reach over and steal " [:code "spec.alpha"] "'s."]
            [:pre
             (print-form-then-eval "(defpred the-aughts #(instance? java.util.Date %) #(last (gen/sample (s/gen :ex/the-aughts) 55)))")
             [:br]
             (print-form-then-eval "(validate-predicate->generator the-aughts 5)")]]]

          [:div.side-by-side-container
           [:div.side-by-side
            [:p "Finally, " [:em "The spec Guide"] " illustrates generating doubles with specific conditions."]
            [:pre
             (print-form-then-eval "(s/def :ex/dubs (s/double-in :min -100.0 :max 100.0 :NaN? false :infinite? false))")
             [:br]
             (print-form-then-eval "(s/valid? :ex/dubs 2.9)")
             [:br]
             (print-form-then-eval "(s/valid? :ex/dubs Double/POSITIVE_INFINITY)")
             [:br]
             (print-form-then-eval "(gen/sample (s/gen :ex/dubs))")]]
           [:div.side-by-side
            [:p "Speculoos leans on " [:code "test.check.generators"] " for that flexibility."]
            [:pre
             (print-form-then-eval "(defpred dubs #(and (<= -100 % 100) (not (NaN? %)) (not (infinite? %))) #(gen/generate (gen/double* {:min -100 :max 100 :infinite? false \"NaN?\" true})))")
             [:br]
             (print-form-then-eval "(validate-predicate->generator dubs 10)")]]]]

         [:section
          [:p "Honestly, when I started writing Speculoos, I wouldn't have guessed that it could mimic " [:code "spec.alpha"] ". Perhaps the " [:a {:href "ideas.html"} "three ideas"] " have some merit. Still, Speculoos is very much a proof-of-concept, experimental prototype. Function instrumentation is really rough. I don't like how much manual work custom generators require. Many of the bottom-level functions could use attention to performance."]
          [:p [:a {:href "contact.html"} "Let me know"] " what you think."]]
         ]]))