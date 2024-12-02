(require '[screedcast.core :refer [panel
                                   prettyfy-form-prettyfy-eval
                                   screencast-title
                                   whats-next-panel]])


(def compare-index 14)


[:body.smaller
 (panel
  (screencast-title compare-index (hiccup2.core/html [:code "(diff spec.alpha speculoos)"]))

  [:div.note.smaller
   [:p "The " [:a {:href "https://github.com/blosavio/speculoos"} "Speculoos library"]
    " is an experiment to see if it is possible to perform the same tasks as "
    [:code "clojure.spec.alpha"]
    " using literal specifications. As a rough measure, I tried to replicate the features outlined in the "
    [:a {:href "https://clojure.org/guides/spec"} [:em " spec Guide"]]
    ". I think Speculoos manages to check off most every feature to some degree, so I feel the project's ideas have merit."]

   [:p "If you're familiar with " [:code "clojure.spec.alpha"] " and are curious about how Speculoos compares, here's a side-by-side demonstration. Find the full documentation " [:a {:href "https://github.com/blosavio/speculoos"} "here"] "."]

   [:p "A warning/apology: For the other Speculoos screencasts, I tried to make the examples small and/or short. The original 'diff' webpage's goal was to show side-by-side how Speculoos compares to spec.alpha. It does that by replicating The Spec Guide, which wasn't originally written for showing a screencast. Instead of altering the examples, I merely include them here as is. The examples are therefore dense and lengthy. The original webpage is the definitive version, but if you prefer to watch and listen, I hope you find this screencast useful."]])


 (panel
  [:h3 "Predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:p [:code "spec.alpha"]]

    (prettyfy-form-prettyfy-eval "(require '[clojure.spec.alpha :as s])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/conform even? 1000)")]

   [:div.side-by-side
    [:p "Speculoos"]

    (prettyfy-form-prettyfy-eval "(even? 1000)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(nil? nil)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(#(< % 5) 4)")]]

  [:div.note.smaller
   [:p "Throughout this comparison screencast, spec.alpha will appear on the left, Speculoos on the right."]

   [:p [:code "spec.alpha"] " predicates are tested with " [:code "(s/conform)"] " or " [:code "(s/valid?)"]]

   [:p "Speculoos specifications are bare, unadorned Clojure predicates."]])


 (panel
  [:h3 "Defining and storing specifications and predicates"]

  [:div.side-by-side-container
   [:div.side-by-side

    (prettyfy-form-prettyfy-eval "(s/def :order/date inst?)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/def :deck/suit #{:club :diamond :heart :spade})")]

   [:div.side-by-side

    (prettyfy-form-prettyfy-eval "(def date inst?)")

    (prettyfy-form-prettyfy-eval "(def suit #{:club :diamond :heart :spade})" 40 40)

    (prettyfy-form-prettyfy-eval "(import java.util.Date)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(date (Date.))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(suit :club)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(suit :shovel)")]]

  [:div.note.smaller
   [:p [:code "spec.alpha/def"]  " stores the spec in a central registry."]
   [:p [:code "clojure.core/def"] " live in your namespace"]
   [:p [:code "spec.alpha"] " provides a special " [:code "def"] " which stores the spec in a central registry."]
   [:p "Speculoos specifications are def-ed, and live in, your namespace, and are therefore automatically namespace-qualified."]
   [:p "If you like the idea of a spec registry, toss 'em into your own hashmap to create an *ad hoc* registry; Speculoos specifications are just predicates and can be used anywhere"]])


 (panel
  [:h3 "Niceties"]

  [:div.side-by-side-container
  [:div.side-by-side "Automatically-created spec docstrings"]
  [:div.side-by-side "(nothing)"]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " has some slick facilities for automatically creating spec docstrings."]
   [:p "Speculoos specifications do not have any special docstring features beyond what you explicitly add to your function " [:code "def"] "s."]])


 (panel
  [:h3 "Composing Predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :num/big-even (s/and int? even? #(> % 1000)))")

    (prettyfy-form-prettyfy-eval "(s/valid? :num/big-even :foo)")

    (prettyfy-form-prettyfy-eval "(s/valid? :num/big-even 10)")

    (prettyfy-form-prettyfy-eval "(s/valid? :num/big-even 100000)")

    (prettyfy-form-prettyfy-eval "(s/def :domain/name-or-id (s/or :name string? :id   int?))")

    (prettyfy-form-prettyfy-eval "(s/valid? :domain/name-or-id \"abc\")")

    (prettyfy-form-prettyfy-eval "(s/valid? :domain/name-or-id 100)")

    (prettyfy-form-prettyfy-eval "(s/valid? :domain/name-or-id :foo)")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def big-even #(and (int? %) (even? %) (> % 1000)))")

    (prettyfy-form-prettyfy-eval "(big-even :foo)")

    (prettyfy-form-prettyfy-eval "(big-even 10)")

    (prettyfy-form-prettyfy-eval "(big-even 10000)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(def name-or-id #(or (string? %) (int? %)))")

    (prettyfy-form-prettyfy-eval "(name-or-id \"abc\")")

    (prettyfy-form-prettyfy-eval "(name-or-id 100)")

    (prettyfy-form-prettyfy-eval "(name-or-id :foo)")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " specs are composed with special functions " [:code "(s/and)"] " and " [:code "(s/or)"] "."]

   [:p "Speculoos specifications are composed with " [:code "clojure.core/and"] " and " [:code "clojure.core/or"] "."]

   [:p "Also note: the two have different ways to refer to datums. " [:code "spec.alpha"] " annotates branches with keywords (" [:code ":name"] " and " [:code ":id"] "), used to return " [:em "conformed"] " data. Speculoos uses a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mechanics"} " different strategy using paths"] " to refer to datums."]])


 (panel
  [:h3 [:code "nil"] "-abling"]

  [:div.side-by-side-container
   [:div.side-by-side

    (prettyfy-form-prettyfy-eval "(s/valid? string? nil)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? (s/nilable string?) nil)")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(#(or (string? %) (nil? %)) nil)")]]

  [:div.vspace]

  [:p "…but don't. (See " [:em "Perhaps so"] " webpage or screencast.)"]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " provides a helper to include " [:code "nil"] " as a valid value"]

   [:p "Simply compose to make a Speculoos predicate nilable."]

   [:p [:code "spec.alpha"] "'s " [:code "explain"] " provides a nice report for non-conforming simple predicates."]

   [:p "Speculoos returns only " [:code "true/false"] " for simple predicates. Later, we'll see how Speculoos "
    [:em "does"] " produce a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#valid-thorough"} "detailed report"] " for composite values."]])


 (panel
  [:h3 "Entity Maps"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def email-regex #\"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,63}$\")")

    (prettyfy-form-prettyfy-eval "(s/def :acct/email-type (s/and string? #(re-matches email-regex %)))")

    (prettyfy-form-prettyfy-eval "(s/def :acct/acctid int?)")

    (prettyfy-form-prettyfy-eval "(s/def :acct/first-name string?)")

    (prettyfy-form-prettyfy-eval "(s/def :acct/last-name string?)")

    (prettyfy-form-prettyfy-eval "(s/def :acct/email :acct/email-type)")

    (prettyfy-form-prettyfy-eval "(s/def :acct/person (s/keys :req [:acct/first-name :acct/last-name :acct/email] :opt [:acct/phone]))")]
   [:div.side-by-side]]

  [:div.vspace]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def email-spec #(and (string? %) (re-matches email-regex %)))" 45 45)

    (prettyfy-form-prettyfy-eval "(def person-spec {:first-name string? :last-name string? :email email-spec :phone any?})" 45 45)

    (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-scalars? validate-scalars only-invalid]])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"bugs@example.com\"} person-spec)" 45 45)]]

  [:div.note.smaller
   [:p "Here is " [:code "spec.alpha"] " in action."]

   [:p "Here is the same process in Speculoos, re-using the regex. (The " [:em "spec Guide"] " does not appear to use " [:code ":acct/acctid"] ", so I will skip it.)"]])


 (panel
  [:h3 "Speculoos: Optionality, flexibility, and permissiveness #1"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"not@even@close@to@a@valid@email\"} (dissoc person-spec :email))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email :not-an-email} (assoc person-spec :email #(string? %)))" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"}  person-spec)" 45 45)]]

  [:div.note.smaller
   [:p "Speculoos checks only keys that are in both the data and the specification. If you don't want to validate a particular entry, you can, on-the-fly, dissociate that key-val from the specification."]

   [:p "If you want to merely relax a specification, simply associate a new, more permissive predicate."]

   [:p "Note the function name: Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "distinguishes"] " validating " [:em "scalars"] " (i.e., numbers, strings, characters, etc.) from " [:em "collections"] " (vectors, lists, maps, sets). Speculoos provides a corresponding group of functions specifying collection counts, presence of keys, set membership, etc."]

   [:p "Instead of using " [:code "valid…?"] " and friends, Speculoos' " [:code "validate…*"] " family of functions show the details of the validating each datum."]

   [:p "The validation results can grow unwieldy with large data and specifications, so Speculoos provides some helper functions to quickly focus on points of interest, i.e., non-valid specs."]])

 (panel
  [:h3 "Speculoos: Optionality, flexibility, and permissiveness #2"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(validate-scalars {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"} person-spec)" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars {:first-name \"Bugs\" :last-name \"Bunny\" :email \"n/a\"} person-spec))" 45 45)]]

  [:div.note.smaller
   [:p "Speculoos checks only keys that are in both the data and the specification. If you don't want to validate a particular entry, you can, on-the-fly, dissociate that key-val from the specification."]

   [:p "If you want to merely relax a specification, simply associate a new, more permissive predicate."]

   [:p "Note the function name: Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "distinguishes"] " validating " [:em "scalars"] " (i.e., numbers, strings, characters, etc.) from " [:em "collections"] " (vectors, lists, maps, sets). Speculoos provides a corresponding group of functions specifying collection counts, presence of keys, set membership, etc."]

   [:p "Instead of using " [:code "valid…?"] " and friends, Speculoos' " [:code "validate…*"] " family of functions show the details of the validating each datum."]

   [:p "The validation results can grow unwieldy with large data and specifications, so Speculoos provides some helper functions to quickly focus on points of interest, i.e., non-valid specs."]])


 (panel
  [:h3 "Namespaced keys"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:p [:code "spec.alpha"] " uses " [:code ":req"] " & " [:code ":req-un"] ", " [:code ":opt"] " and " [:code ":opt-un"]]]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(= :k ::k)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-scalars {::a 42 ::b \"abc\" ::c :foo} {:a int? :b string? :c keyword?})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? {::a 42 ::b \"abc\" ::c :foo} {::a int? ::b string? ::c keyword?})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? {:a 42 :b \"abc\" :c :foo} {:a int? :b string? :c keyword?})" 55 45)]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " distinguishes unqualified keys and fully-namespaced keys, and allows you to explicitly declare one or the other."]

   [:p "Speculoos implicitly distinguishes qualified from unqualified keys because " [:code "(not= :k ::k)."]]

   [:p "Observe: Qualified keys in data, unqualified keys in specification, no matches…"]

   [:p "…qualified keys in both data and specification, validation succeeds…"]

   [:p "…unqualified keys in both data and specification, validation succeeds."]])


 (panel
  [:h3 "Keyword args"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :my.config/port number?)")

    (prettyfy-form-prettyfy-eval "(s/def :my.config/host string?)")

    (prettyfy-form-prettyfy-eval "(s/def :my.config/id keyword?)")

    (prettyfy-form-prettyfy-eval "(s/def :my.config/server (s/keys* :req [:my.config/id :my.config/host] :opt [:my.config/port]))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/conform :my.config/server [:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555])")]
   [:div.side-by-side]]
  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def port number?)")

    (prettyfy-form-prettyfy-eval "(def host string?)")

    (prettyfy-form-prettyfy-eval "(def id keyword?)")

    (prettyfy-form-prettyfy-eval "(def server-spec {:my.config/id id :my.config/host host :my.config/port port})" 45 55)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? {:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555} server-spec)" 45 45)]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " handles keyword args like this:"]
   [:p "Speculoos does it this way:"]])


 (panel
  [:h3 "Does " [:em "thing"] " exist?"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def server-data-2 [:my.config/id :s1 :my.config/host \"example.com\" :my.config/port 5555])" 45 45)

    (prettyfy-form-prettyfy-eval "(def server-spec-2 [#(= % :my.config/id) id #(= % :my.config/host) host #(= % :my.config/port) port])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? server-data-2 server-spec-2)" 45 45)]]

  [:div.note.smaller
   [:p "The principle of Speculoos' validation is that if the key exists in both the data and specification, then Speculoos will apply the predicate to the datum. This fulfills the criteria of " [:em "Thing may or may not exist, but if Thing " [:strong "does"] " exist, it must satisfy this predicate."]]

   [:p "If we want to similarly validate a sequential data structure, it goes like this:"]])


 (panel
  [:h3 "Merging"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :animal/kind string?)")

    (prettyfy-form-prettyfy-eval "(s/def :animal/says string?)")

    (prettyfy-form-prettyfy-eval "(s/def :animal/common (s/keys :req [:animal/kind :animal/says]))")

    (prettyfy-form-prettyfy-eval "(s/def :dog/tail? boolean?)")

    (prettyfy-form-prettyfy-eval "(s/def :dog/breed string?)")

    (prettyfy-form-prettyfy-eval "(s/def :animal/dog (s/merge :animal/common (s/keys :req [:dog/tail? :dog/breed])))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :animal/dog {:animal/kind \"dog\" :animal/says \"woof\" :dog/tail? true :dog/breed \"retriever\"})")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def animal-kind string?)")

    (prettyfy-form-prettyfy-eval "(def animal-says string?)")

    (prettyfy-form-prettyfy-eval "(def animal-spec {:kind animal-kind :says animal-says})")

    (prettyfy-form-prettyfy-eval "(def dog-spec (merge animal-spec {:tail boolean? :breed string?}))")

    (prettyfy-form-prettyfy-eval "(def dog-data {:kind \"dog\" :says \"woof\" :tail true :breed \"retriever\"})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? dog-data dog-spec)")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " has a " [:code "merge"] " function."]
   [:p "Speculoos simply uses Clojure's powerful data manipulation functions."]])


 (panel
  [:h3 "Multi-spec #1"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :event/type keyword?)")

    (prettyfy-form-prettyfy-eval "(s/def :event/timestamp int?)")

    (prettyfy-form-prettyfy-eval "(s/def :search/url string?)")

    (prettyfy-form-prettyfy-eval "(s/def :error/message string?)")

    (prettyfy-form-prettyfy-eval "(s/def :error/code int?)")

    (prettyfy-form-prettyfy-eval "(defmulti event-type :event/type)")

    (prettyfy-form-prettyfy-eval "(defmethod event-type :event/search [_] (s/keys :req [:event/type :event/timestamp :search/url]))")

    (prettyfy-form-prettyfy-eval "(defmethod event-type :event/error [_] (s/keys :req [:event/type :event/timestamp :error/message :error/code]))")

    (prettyfy-form-prettyfy-eval "(s/def :event/event (s/multi-spec event-type :event/type))")

    (prettyfy-form-prettyfy-eval "(s/valid? :event/event {:event/type :event/search :event/timestamp 1463970123000 :search/url \"https://clojure.org\"})")

    (prettyfy-form-prettyfy-eval "(s/valid? :event/event {:event/type :event/error :event/timestamp 1463970123000 :error/message \"Invalid host\" :error/code 500})")

    (prettyfy-form-prettyfy-eval "(s/explain :event/event {:event/type :event/restart})")]
   [:div.side-by-side]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " has the capability to dispatch validation paths according to an in-band key. Here's the " [:em "Guide"] "'s demo."]

   [:p "Since Speculoos consumes regular old Clojure data structures and functions, they work similarly. Instead of " [:code "def"] "-ing a series of separate predicates, for brevity, I'll inject them directly into the specification definition, but Speculoos could handle any level of indirection."]

   [:p "Here we see a significant difference between " [:code "spec.alpha"] " and Speculoos: the former fails the validation because " [:code "event-4"] " is missing the " [:code ":timestamp"] " key. Speculoos considers the presence or absence of a map's key to be a property of the collection. Within that philosophy, such a specification would properly belong in a Speculoos " [:em "collection specification"] "."]])


  (panel
  [:h3 "Multi-spec #2"]
  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defmulti event-type :event/type)")

    (prettyfy-form-prettyfy-eval "(defmethod event-type :event/search [_] {:event/type keyword? :event/timestamp int? :search/url string?})")

    (prettyfy-form-prettyfy-eval "(defmethod event-type :event/error [_] {:event/type keyword? :event/timestamp int? :error/message string? :error/code int?})")

    (prettyfy-form-prettyfy-eval "(def event-1 {:event/type :event/search :event/timestamp 1463970123000 :event/url \"https://clojure.org\"})")

    (prettyfy-form-prettyfy-eval "(valid-scalars? event-1 (event-type event-1))")

    (prettyfy-form-prettyfy-eval "(def event-2 {:event/type :event/error :event/timestamp 1463970123000 :error/message \"Invalid host\" :code 500})")

    (prettyfy-form-prettyfy-eval "(valid-scalars? event-2 (event-type event-2))")

    (prettyfy-form-prettyfy-eval "(def event-3 {:event/type :restart})")

    (prettyfy-form-prettyfy-eval "(try (valid-scalars? event-3 (event-type event-3)) (catch Exception e (.getMessage e)))")

    (prettyfy-form-prettyfy-eval "(def event-4 {:event/type :event/search :search/url 200})")

    (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars event-4 (event-type event-4)))")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " has the capability to dispatch validation paths according to an in-band key. Here's the " [:em "Guide"] "'s demo."]

   [:p "Since Speculoos consumes regular old Clojure data structures and functions, they work similarly. Instead of " [:code "def"] "-ing a series of separate predicates, for brevity, I'll inject them directly into the specification definition, but Speculoos could handle any level of indirection."]

   [:p "Here we see a significant difference between " [:code "spec.alpha"] " and Speculoos: the former fails the validation because " [:code "event-4"] " is missing the " [:code ":timestamp"] " key. Speculoos considers the presence or absence of a map's key to be a property of the collection. Within that philosophy, such a specification would properly belong in a Speculoos " [:em "collection specification"] "."]])


 (panel
  [:h3 "Collections #1"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/conform (s/coll-of keyword?) [:a :b :c])")

    (prettyfy-form-prettyfy-eval "(s/conform (s/coll-of number?) #{5 10 2})")

    (prettyfy-form-prettyfy-eval "(s/def :ex/vnum3 (s/coll-of number? :kind vector? :count 3 :distinct true :into #{}))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/conform :ex/vnum3 [1 2 3])")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? [:a :b :c] [keyword? keyword? keyword?])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-collections? validate-collections]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-collections? [:a :b :c] [#(every? keyword? %)])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-collections? #{5 10 2} #{#(every? number? %)})" 45 45)]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " provides a trio of helper functions for collections. First, " [:code "coll-of"] "."]

   [:p "Speculoos was designed from the start to specify collections. Speculoos validates collections in two different ways: it can validate groupings of " [:em "scalars"] ", atomic, indivisible values (i.e., numbers, booleans, etc.) and it can separately validate the properties of a " [:em "collection"] " (i.e., vector, map, list, set, etc.) itself, such as its size, the position of particular elements, and the relationships between elements, etc."]

   [:p "This example could certainly be validated as we've seen before."]

   [:p "Speculoos can also consider the vector as a whole with its collection validation facility."]

   [:p "In a collection spec, the predicate applies to the collection that contains that predicate."]

   [:p "Speculoos collection specs work on just about any type of collection."]])


 (panel
  [:h3 "Collections #2"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :geom/point (s/tuple double? double? double?))")

    (prettyfy-form-prettyfy-eval "(s/conform :geom/point [1.5 2.5 -0.5])")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? [1.5 2.5 -0.5] [double? double? double?])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-collections? [1.5 2.5 -0.5] [#(every? double? %)])" 45 45)]]

  [:div.note.smaller
   [:p "Next, " [:code "spec.alpha"] "'s " [:code "tuple"] "."]
   [:p "Tuples are Speculoos' bread and butter."]])


 (panel
  [:h3 "Collections #3"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :game/scores (s/map-of string? int?))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/conform :game/scores {\"Sally\" 1000, \"Joe\" 500})")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-collections? {\"Sally\" 1000, \"Joe\" 500} {:check-keys #(every? string? (keys %)) :check-vals #(every? int? (vals %))})" 45 45)]]

  [:div.note.smaller
   [:p "Finally, " [:code "spec.alpha"] "'s " [:code "map-of"] "."]

   [:p "Where Speculoos really takes flight is heterogeneous, arbitrarily-nested collections, but since this document is a comparison to " [:code "spec.alpha"] ", see the Speculoos " [:a {:href "https://github.com/blosavio/speculoos/blob/main/resources/recipes.clj"} "recipes"] " for examples."]

   [:p "Speculoos collection validation works on maps, too."]])


 (panel
  [:h3 "Sequences"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :cook/ingredient (s/cat :quantity number? :unit keyword?))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :cook/ingredient [2 :teaspoon])")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def ingredient-spec [number? keyword?])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [2 :teaspoon] ingredient-spec)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars [11 \"peaches\"] ingredient-spec))")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " uses regex syntax to describe the structure of sequential data."]

   [:p "Speculoos uses a literal."]

   [:p "Invalid datums are reported like this."]])


 (panel
  [:h3 "Presence of a datum"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def is-second-kw? #(keyword? (get % 1)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-collections [2] [is-second-kw?])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [2] ingredient-spec)")]]

  [:div.note.smaller
   [:p "Note, 'missing' scalars are not validated as they would be with " [:code "spec.alpha"] "."]

   [:p "Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "ignores"] " predicates without a corresponding datum. Presence/absence of a datum is a property of the collection, and is thus handled with a collection specification. Like so…"]])


 (panel
  [:h3 "A vector of keywords"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/seq-of-keywords (s/* keyword?))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/seq-of-keywords [:a :b :c])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/explain-str :ex/seq-of-keywords [10 20])")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def inf-seq-of-keywords-spec (repeat keyword?))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [:a :b :c] inf-seq-of-keywords-spec)" 45 45)

    [:div.vspace]
    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-scalars [10 20] inf-seq-of-keywords-spec)" 45 45)]]

  [:div.note.smaller
   [:p "Let's look at another " [:code "spec.alpha"] " example."]
   [:p "Now, the Speculoos " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#non-terminating-sequences"} "way"] "."]])


 (panel
  [:h3 "Odds, then maybe even"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/odds-then-maybe-even (s/cat :odds (s/+ odd?) :even (s/? even?)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/odds-then-maybe-even [1 3 5 100])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/odds-then-maybe-even [1])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/explain-str :ex/odds-then-maybe-even [100])")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def odds-then-maybe-even-spec #(and (<= 2 (count (partition-by odd? %))) (every? odd? (first (partition-by odd? %)))))")

    (prettyfy-form-prettyfy-eval "(valid-collections? [1 3 5 100] [odds-then-maybe-even-spec])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-collections [1] [odds-then-maybe-even-spec])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-collections [100] [odds-then-maybe-even-spec])" 45 45)]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] "…"]
   [:p "Speculoos…"]])


 (panel
  [:h3 "Alternating keywords and booleans"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/opts (s/* (s/cat :opt keyword? :val boolean?)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/opts [:silent? false :verbose true])")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def alt-kw-bool-spec (cycle [keyword? boolean?]))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [:silent? false :verbose true] alt-kw-bool-spec)" 46 45)]]

  [:div.note.smaller
   [:p "Here's a " [:code "spec.alpha"] " demonstration of opts that are alternating keywords and booleans."]
   [:p "Speculoos' way to do the same."]])


 (panel
  [:h3 "Specifying alternatives"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/config (s/* (s/cat :prop string? :val  (s/alt :s string? :b boolean?))))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/config [\"-server\" \"foo\" \"-verbose\" true \"-user\" \"joe\"])" 33 45)]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def config-spec (cycle [string? #(or (string? %) (boolean? %))]))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [\"-server\" \"foo\" \"-verbose\" true \"-user\" \"joe\"] config-spec)" 38 45)]]

  [:div.note.smaller
   [:p "Finally, " [:code "spec.alpha"] " specifies alternatives like this."]

   [:p "We'd do this in Speculoos."]

   [:p [:code "spec.alpha"] " provides the " [:code "describe"] " function to retrieve a spec's description. Speculoos trusts your dev environment to find and show you the definitions."]])


 (panel
  [:h3 "&"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/even-strings (s/& (s/* string?) #(even? (count %))))")

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/even-strings [\"a\"])")

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/even-strings [\"a\" \"b\"])")

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/even-strings [\"a\" \"b\" \"c\"])")

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/even-strings [\"a\" \"b\" \"c\" \"d\"])")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def even-string-spec #(and (even? (count %)) (every? string? %)))")

    (prettyfy-form-prettyfy-eval "(valid-collections? [\"a\"] [even-string-spec])")

    (prettyfy-form-prettyfy-eval "(valid-collections? [\"a\" \"b\"] [even-string-spec])")

    (prettyfy-form-prettyfy-eval "(valid-collections? [\"a\" \"b\" \"c\"] [even-string-spec])")

    (prettyfy-form-prettyfy-eval "(valid-collections? [\"a\" \"b\" \"c\" \"d\"] [even-string-spec])")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " created a provincial " [:code "&"] "."]

   [:p "Speculoos uses " [:code "clojure.core/and"] "."]

   [:p "This example reveals a philosophical difference between " [:code "spec.alpha"] " and Speculoos. Here, " [:code "spec.alpha"] " has combined specifying the values of a collection and the count of the collection, a property of the container. Speculoos' opinion is that specifying scalars and collections are separate concerns. For the sake of the compare and contrast, I combined the two validation tests into a single collection predicate, " [:code "even-string-spec"] ", abusing the fact that the container has access to its own contents. But this improperly combines two conceptually distinct operations."]])


 (panel
  [:h3 "Distinguishing scalars and collections"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? [\"a\" \"b\" \"c\" \"d\"] (repeat string?))" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-collections? [\"a\" \"b\" \"c\" \"d\"] [#(even? (count %))])" 45 45)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid?]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid? [\"a\" \"b\" \"c\" \"d\"] (repeat string?) [#(even? (count %))])" 45 45)]]

  [:div.note.smaller
   [:p "If I weren't contrasting with the " [:code "spec.alpha"] " " [:em "Guide"] ", I would have written this."]

   [:p "Because we'll often want to validate both a scalar specification and a collection specification at the same time, Speculoos provides a convenience function that does both."]

   [:p "As a testament to my belief in the importance of separating scalar and collection specifications, I reserved the shortest and most mnemonic name, " [:code "valid?"] ", for doing what I contend to be the proper way."]])


 (panel
  [:h3 "Nested collections"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/nested (s/cat :names-kw #{:names} :names (s/spec (s/* string?)) :nums-kw #{:nums} :nums (s/spec (s/* number?))))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/nested [:names [\"a\" \"b\"] :nums [1 2 3]])" 45 45)]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def scalar-nested-spec [#{:names} (repeat string?) #{:nums} (repeat number?)])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [:names [\"a\" \"b\"] :nums [1 2 3]] scalar-nested-spec)" 65 45)]]

  [:div.note.smaller
   [:p "Nested collections provide another nice point of comparison. Quoting the " [:a {:href "https://clojure.org/guides/spec#_sequences"} [:em "spec Guide"]]]

   [:blockquote "When [spec.alpha] regex ops are combined, they describe a single sequence. If you need to spec a nested sequential collection, you must use an explicit call to spec to start a new nested regex context."]

   [:p "Speculoos was designed from the outset to straightforwardly handle nested collections. We don't have to do anything special."]])


 (panel
  [:h3 "Keeping only valid datums"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    [:pre [:code "(if (valid? data spec) data :invalid)"]]]]

  [:div.note.smaller
   [:p "Because " [:code "spec.alpha/conform"] " passes through valid data, you can use its output to filter out data, as seen in the configuration example. In its current implementation, Speculoos' family of " [:code "valid?"] " functions only return " [:code "true/false"] ", so to emulate " [:code "spec.alpha"] ", you'd have to use a pattern such as…"]])


 (panel
  [:h3 "Spec'ing functions #1: spec.alpha"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defn ranged-rand \"Returns random int in range start <= rand < end\" [start end] (+ start (long (rand (- end start)))))")

    (prettyfy-form-prettyfy-eval "(s/fdef ranged-rand :args (s/and (s/cat :start int? :end int?) #(< (:start %) (:end %))) :ret int? :fn (s/and #(>= (:ret %) (-> % :args :start)) #(< (:ret %) (-> % :args :end))))")

    (prettyfy-form-prettyfy-eval "(require '[clojure.spec.test.alpha :as stest])")

    (prettyfy-form-prettyfy-eval "(stest/instrument `ranged-rand)")

    (prettyfy-form-prettyfy-eval "(try (ranged-rand 8 5) (catch Exception e (.getMessage e)))")

    (prettyfy-form-prettyfy-eval "(stest/unstrument `ranged-rand)")]
   [:div.side-by-side]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " can define specifications for a function, like this example, which I've merged with a later section of the " [:em "spec Guide"] ", titled " [:em "Instrumentation and Testing"] "."]

   [:p "Speculoos provides a pair of corresponding utilities for testing functions. First, " [:code "validate-fn-with"] " wraps a function on-the-fly without mutating the function's " [:code "var"] ". First, I'll demonstrate a valid invocation."]])


 (panel
  [:h3 "Spec'ing functions #2: Speculoos (manual)"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[speculoos.function-specs :refer [validate-fn-with]])")

    (prettyfy-form-prettyfy-eval "(def second-is-larger-than-first? #(< (get % 0) (get % 1)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-fn-with ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?} 2 12)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-fn-with ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?} 8 5)")]]

  [:div.note.smaller
   [:p "Here, we'll intentionally violate the function's argument collection specification by reversing the order of the arguments, and observe the report."]])


 (panel
  [:h3 "Spec'ing functions #3: Speculoos (instrumented)"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[speculoos.function-specs :refer [inject-specs! instrument unstrument]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(inject-specs! ranged-rand {:speculoos/arg-scalar-spec [int? int?] :speculoos/arg-collection-spec [second-is-larger-than-first?] :speculoos/ret-scalar-spec int?})")

    [:div.vspace]

    [:pre [:code "(instrument ranged-rand)"]]

    (prettyfy-form-prettyfy-eval "(ranged-rand 5 8)")

    [:div.vspace]

    [:pre
     [:code.form "(with-out-str (ranged-rand 8 5))"]
     [:br]
     [:code.eval ";;=> ({:path [0],\n       :value second-is-larger-than-first?,\n       :datum [8 5],\n       :ordinal-parent-path [],\n       :valid? false})\n"]]

    [:pre [:code "(unstrument ranged-rand)"]]]]

  [:div.note.smaller
   [:p "For testing with a higher degree of integration, Speculoos' second function validation option mimics " [:code "spec.alpha/instrument"] ". Instrumented function specifications are gathered from the function's metadata. Speculoos provides a convenience function for injecting specs."]
   [:p "Now, we instrument the function…"]
   [:p "…and then test it. Valid inputs return as normal."]
   [:p "Invalid arguments return without halting if the function can successfully complete (as in this scenario), but the invalid message is tossed to " [:code "*out*"] "."]
   [:p "Later, we can return the function to it's original state."]
   [:p "(My compliments to whoever invented the " [:code "unstrument"] " term to compliment " [:code "instrument"] "."]])


 (panel
  [:h3 "Higher order functions"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defn adder [x] #(+ x %))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/fdef adder :args (s/cat :x number?) :ret (s/fspec :args (s/cat :y number?) :ret number?) :fn #(= (-> % :args :x) ((:ret %) 0)))")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(inject-specs! adder {:speculoos/arg-scalar-spec number? :speculoos/ret-scalar-spec fn? :speculoos/hof-specs {:speculoos/arg-scalar-spec [int?] :speculoos/ret-scalar-spec number?}})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-higher-order-fn adder [5] [10])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-higher-order-fn adder [5] [22/7])")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " supports validating " [:span.small-caps"hof"] "s like this."]
   [:p "Speculoos' version looks like this."]
   [:p "Speculoos can recursively specify and validate the arguments and returns value of any depth of higher-order functions."]])


 (panel
  [:h3 "Macros"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/fdef clojure.core/declare :args (s/cat :names (s/* simple-symbol?)) :ret any?)")

    [:div.vspace]

    [:code.form "(declare 100)"]
    [:br]
    [:code.eval ";; => Call to clojure.core/declare did not conform to spec..."]]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defmacro silly-macro [f & args] `(~f ~@args))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(silly-macro + 1 2)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-macro?]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(def silly-macro-spec (list symbol? number? number?))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-macro? `(silly-macro + 1 2) silly-macro-spec)")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] "'s macro analysis is nicely integrated into Clojure's macroexpander."]
   [:p "Speculoos is more " [:em "ad hoc"] ": macro output is tested the same as any other function."]
   [:p "Speculoos validates macro expansion like this."]
   [:p "(" [:code "valid-macro?"] " is a placeholder: I haven't written enough macros to know if it's of any use."]])


 (panel
  [:h3 "Game of cards: " [:code "spec.alpha"] " #1"]

  (prettyfy-form-prettyfy-eval "(def suit? #{:club :diamond :heart :spade})")

  (prettyfy-form-prettyfy-eval "(def rank? (into #{:jack :queen :king :ace} (range 2 11)))")

  (prettyfy-form-prettyfy-eval "(def deck (for [suit suit? rank rank?] [rank suit]))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(s/def :game/card (s/tuple rank? suit?))")

  (prettyfy-form-prettyfy-eval "(s/def :game/hand (s/* :game/card))")

  (prettyfy-form-prettyfy-eval "(s/def :game/name string?)")

  (prettyfy-form-prettyfy-eval "(s/def :game/score int?)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(s/def :game/player (s/keys :req [:game/name :game/score :game/hand]))")

  (prettyfy-form-prettyfy-eval "(s/def :game/players (s/* :game/player))")

  (prettyfy-form-prettyfy-eval "(s/def :game/deck (s/* :game/card))")

  (prettyfy-form-prettyfy-eval "(s/def :game/game (s/keys :req [:game/players :game/deck]))")

  (prettyfy-form-prettyfy-eval "(def kenny {:game/name \"Kenny Rogers\" :game/score 100 :game/hand []})")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(s/valid? :game/player kenny)")

  [:div.note.smaller [:p "The " [:em "Guide"] " presents a card game to demonstrate " [:code "spec.alpha"] "."]])


 (panel
  [:h3 "Game of cards: " [:code "spec.alpha"] " #2"]

  (prettyfy-form-prettyfy-eval "(s/explain-data :game/game {:game/deck deck :game/players [{:game/name \"Kenny Rogers\" :game/score 100 :game/hand [[2 :banana]]}]})" 55 60)
  [:div.note.smaller
   [:p ""]])


 (panel
  [:h3 "Game of cards: Speculoos #1"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def suits #{:club :diamond :heart :spade})")

    (prettyfy-form-prettyfy-eval "(def ranks (into #{:jack :queen :king :ace} (range 2 11)))")

    (prettyfy-form-prettyfy-eval "(def deck (vec (for [s suits r ranks] [r s])))")

    (prettyfy-form-prettyfy-eval "(def card-spec [ranks suits])")

    (prettyfy-form-prettyfy-eval "(def deck-spec (repeat card-spec))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? deck deck-spec)")]]

  [:div.note.smaller
   [:p "Let's follow along, slowly building up the Speculoos specification."]])


 (panel
  [:h3 "Game of cards: Speculoos #2"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def player-spec {:name string? :score int? :hand (repeat card-spec)})")

    (prettyfy-form-prettyfy-eval "(def kenny {:name \"Kenny Rogers\" :score 100 :hand []})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? kenny player-spec)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(defn draw-hand [] (vec (take 5 (repeatedly #(first (shuffle deck))))))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(def players-spec (repeat player-spec))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(def players [kenny {:name \"Humprey Bogart\" :score 188 :hand (draw-hand)} {:name \"Julius Caesar\" :score 77 :hand (draw-hand)}])")]]

  [:div.note.smaller
   [:p ""]])


 (panel
  [:h3 "Game of cards: Speculoos #3"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? (:hand (players 1)) (repeat card-spec))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? players players-spec)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def game [deck players])")

  (prettyfy-form-prettyfy-eval "(def game-spec [deck-spec players-spec])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? game game-spec)")

  [:div.note.smaller
   [:p ""]])

 (panel
  [:h3 "Game of cards: Speculoos #4"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def corrupted-game (assoc-in game [1 0 :hand 0] [2 :banana]))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars corrupted-game game-spec))" 55 55)]]

  [:div.note.smaller
   [:p "What happens when we have bad data?"]

   [:p "Speculoos reports an invalid datum " [:code ":banana"] " according to predicate " [:code "suits"] " located at path " [:code "[1 0 :hand 0 1]"] ", which you can inspect with " [:code "get-in*"] " and similar functions."]])


 (panel
  [:h3 "Generators: " [:code "spec.alpha"] " #1"]

  (prettyfy-form-prettyfy-eval "(gen/sample (s/gen #{:club :diamond :heart :spade}))" 75 10)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))" 75 55)

  [:div.note.smaller
   [:p "The " [:em "spec Guide"] " emphasizes that one of " [:code "spec.alpha"] "'s explicit design goals is to facilitate property-based testing. " [:code "spec.alpha"] " does this by closely cooperating with " [:code "test.check"] ", which generates sample data that conforms to the spec. Next, we'll see a few examples of these capabilities by generating sample data from the card game specs."]])

 (panel
  [:h3 "Generators: " [:code "spec.alpha"] " #1"]

  (prettyfy-form-prettyfy-eval "(gen/generate (s/gen :game/player))")

  [:div.note.smaller
   [:p ""]])


 (panel
  [:h3 "Generators: Speculoos #1"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[speculoos.utility :refer [data-from-spec]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(data-from-spec game-spec :random)")]]

  [:div.note.smaller
   [:p "Speculoos provides a very rudimentary version that mimics this functionality. Because " [:code "game-spec"] " is composed of infinitely-repeating sequences, let's create a simplified version that terminates, using the basic " [:code "test.check"] " generators. Speculoos cannot in all instances automatically pull apart a compound predicate such as " [:code "#(and (int? %) (< % 10))"] " in order to compose a generator."]])


 (panel
  [:h3 "Generators: Speculoos #2"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(data-from-spec [int?] :random)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(data-from-spec [nil?])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(repeatedly 5 #(data-from-spec [string?] :random))")]]

  [:div.note.smaller
   [:p "Automatically setting up generators and property-based testing is the main area where Speculoos lags " [:code "spec.alpha"] ". I do not yet have a great idea on how to automatically pull apart compound, composed predicates. See the " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#exercising"} "docs"] ", " [:a {:href "https://blosavio.github.io/speculoos/speculoos.utility.html#var-inspect-fn"} [:span.small-caps "api"]] " and a " [:a {:href "#and"} "later subsection"] " to see how to manually or semi-automatically add generators into the predicate metadata."]

   [:p "Let's follow along as best as we can…"]])


 (panel
  [:h3 "Generators: Speculoos #3"]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(repeatedly 3 #(data-from-spec (into [keyword?] (repeat 3 double?)) :random))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(data-from-spec player-spec :random)")]]

  [:div.note.smaller
   [:p "The card game specifications refer to earlier sections."]])


 (panel
  [:h3 "Exercise"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/exercise (s/cat :k keyword? :ns (s/+ number?)) 5)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/exercise (s/or :k keyword? :s string? :n number?) 5)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/exercise-fn `ranged-rand)")]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[speculoos.utility :refer [exercise]])")
    (prettyfy-form-prettyfy-eval "(require '[speculoos.function-specs :refer [exercise-fn]])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(exercise [int? string? boolean? char?] 5)" 55 55)

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(inject-specs! ranged-rand {:speculoos/arg-scalar-spec [int? int?]})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(exercise-fn ranged-rand 10)")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] "'s data-generating capabilities allows you to " [:em "exercise"] " a function by invoking it with generated arguments."]
   [:p "Speculoos mimics the " [:code "exercise"] " function, but (for now) only exercises a scalar specification."]
   [:p "Speculoos also mimics " [:code "spec.alpha"] "'s " [:code "exercise-fn"] ", again only for scalar specifications, on the function's arguments."]])


 (panel
  [:h3 [:code "and"] " generators #1"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(gen/generate (s/gen (s/and int? even?)))")

    (prettyfy-form-prettyfy-eval "(defn divisible-by [n] #(zero? (mod % n)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(gen/sample (s/gen (s/and int? #(> % 0) (divisible-by 3))))")]
   [:div.side-by-side]]

  [:div.note.smaller
   [:p "In certain cases, a spec will require the data to fall within a very small range of possible values, such as " [:em "an even positive integer, divisible by three, less than 31, greater than 12."] " The generators are not likely to be able to produce multiple conforming samples using only " [:code "(s/gen int?)"] ", so we construct predicates with " [:code "spec.alpha"] "'s " [:code "and"] "."]

   [:p "Right now, Speculoos cannot " [:em "automatically"] " dive into a compound predicate such as " [:code "#(and (int? %) (even? %))"] " to create a competent generator, but it does offer a few options. First, you could manually compose a random sample generator and attach it the predicate's metadata. You may use whatever generator you prefer; " [:code "test.check.generators"] " work well."]])


  (panel
   [:h3 [:code "and"] " generators #2: " [:code "defpred"] " manual generator"]

   [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[speculoos.utility :refer [defpred validate-predicate->generator]]
                                              '[clojure.test.check.generators :as tc-gen])")

    (prettyfy-form-prettyfy-eval "(defn gen-int-pos-div-by-3 [] (last (tc-gen/sample (tc-gen/fmap #(* % 3) tc-gen/nat) 50)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(def pred-1 (with-meta #(and (int? %) (> % 0) ((divisible-by 3) %)) {:speculoos/predicate->generator gen-int-pos-div-by-3}))")

    (prettyfy-form-prettyfy-eval "(defpred pred-2 #(and (int? %) (> % 0) ((divisible-by 3) %)) gen-int-pos-div-by-3)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-predicate->generator pred-1 5)")

    (prettyfy-form-prettyfy-eval "(validate-predicate->generator pred-2 5)")]]

   [:div.note.smaller
    [:p "The " [:code "defpred"] " utility macro does the equivalent when you explicitly supply a sample generator."]

    [:p "`validate->...` verifies that the samples produced by generator satisfy the predicate"]])


  (panel
   [:h3 [:code "and"] " generators #3: " [:code "defpred"] " automatic generator"]

   [:div.side-by-side-container
    [:div.side-by-side]
    [:div.side-by-side
     (prettyfy-form-prettyfy-eval "(defpred pred-3 #(and (int? %) (pos? %) ((divisible-by 3) %)))")

     [:div.vspace]

     (prettyfy-form-prettyfy-eval "(validate-predicate->generator pred-3 5)")]]

   [:div.note.smaller
    [:p "However, if you write your predicate in a way that conforms to " [:code "defpred"] "'s assumptions, it " [:em "will"] " compose a generator automatically."]

    [:p "This is another area where " [:code "spec.alpha"] "'s approach outclasses Speculoos. Because you write a " [:code "spec.alpha"] " spec in an already 'pulled-apart' state, it can compose a generator starting with the first branch of that compound predicate and then use the following predicates as filters to refine the generated values."]

    [:p "Speculoos consumes predicates as already-defined functions, and it appears fiendishly involved to inspect the internal structure of a function — whose source may not be available — in order to generically extract individual components to an arbitrary nesting depth."]

    [:p "Three questions"
     [:ol
      [:li "Is this why " [:code "spec.alpha"] " specs are written that way?"]
      [:li "Would it be possible at all to decompose a predicate function object without access to the source?"]
      [:li "If Speculoos never offers fully-automatic sample generation from a given compound predicate, is that deal-breaker for the entire approach?"]]]])


 (panel
  [:h3 "Custom generators #1"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/kws (s/with-gen (s/and keyword? #(= (namespace %) \"my.domain\")) #(s/gen #{:my.domain/name :my.domain/occupation :my.domain/id})))")

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/kws :my.domain/name)")

    (prettyfy-form-prettyfy-eval "(gen/sample (s/gen :ex/kws))")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def kw-pred (into #{} (map #(keyword \"my.domain\" %) [\"name\" \"occupation\" \"id\"])))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(valid-scalars? [:my.domain/name] [kw-pred])")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(exercise [kw-pred] 5)")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " acknowledges that you may want to generate values by some other means, and thus allows custom generators via " [:code "with-gen"] "."]

   [:p "Speculoos considers a free-floating set to be a membership predicate. Speculoos generates sample values by randomly selecting from such a set. We can compose an equivalent set to generate qualified keywords."]])



 (panel
  [:h3 "Custom generators #2"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(require '[clojure.spec.gen.alpha :as sgen])")

    (prettyfy-form-prettyfy-eval "(def kw-gen-3 (sgen/fmap #(keyword \"my.domain\" %) (sgen/such-that #(not= % \"\") (sgen/string-alphanumeric))))")

    [:div.vspace]

    [:pre
     [:code.form "(sgen/sample kw-gen-3 5)"]
     [:br]
     [:code.eval ";; => (:my.domain/k
;;     :my.domain/xfm
;;     :my.domain/ey
;;     :my.domain/UkH
;;     :my.domain/UY6)"]]]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def kw-pred-2 (into #{} (map #(keyword \"my.domain\" %) (sgen/sample (sgen/such-that #(not= % \"\") (sgen/string-alphanumeric))))))")

    [:div.vspace]

    [:pre
     [:code.form "(exercise [kw-pred-2] 5)"]
     [:br]
     [:code.eval ";; => ([[:my.domain/9r6] true]
;;     [[:my.domain/djkv9] true]
;;     [[:my.domain/L1i] true]
;;     [[:my.domain/K] true]
;;     [[:my.domain/f] true])"]]]]


  [:div.note.smaller
   [:p [:code "spec.alpha"] " provides combinators for creating more complicated generators."]
   [:p "Speculoos merely relies on " [:code "clojure.core"] " and " [:code "test.check.generators"] " for that."]])


 (panel
  [:h3 "Custom generators #3"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/hello (s/with-gen #(clojure.string/includes? % \"hello\") #(sgen/fmap (fn [[s1 s2]] (str s1 \"hello\" s2)) (sgen/tuple (sgen/string-alphanumeric) (sgen/string-alphanumeric)))))")

    (prettyfy-form-prettyfy-eval "(sgen/sample (s/gen :ex/hello))")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(exercise [#\"\\w{0,3}hello\\w{1,5}\"])")]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " making a " [:em "hello"] "-string generator."]
   [:p "You could certainly copy-paste that generator and use it as is. Speculoos could also generate a sample string via a regular expression predicate."]])


 (panel
  [:h3 "Range specs"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :bowling/roll (s/int-in 0 11))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(gen/sample (s/gen :bowling/roll))")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defpred bowling-roll #(and (int? %) (<= 0 % 10)) #(last (gen/sample (gen/large-integer* {:min 0 :max 10}))))")

    (prettyfy-form-prettyfy-eval "(validate-predicate->generator bowling-roll)")

    (prettyfy-form-prettyfy-eval "(defpred bowling-roll-2 #(and (int? %) (<= 0 % 10)) #(rand-int 11))")

    (prettyfy-form-prettyfy-eval "(validate-predicate->generator bowling-roll-2)")

    (prettyfy-form-prettyfy-eval "(exercise [(set (range 11))])")]]

  [:div.note.smaller
   [:p "Spec-ing and generating a range of integers in " [:code "spec.alpha"] "."]
   [:p "Speculoos has a few different ways to do that."]
   [:p "But for integers, nothing beats the succinctness of " [:code "rand-int"] "."]
   [:p "For small group sizes, a set-as-predicate might feel more natural."]])


 (panel
  [:h3 "Range of instants"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/the-aughts (s/inst-in #inst \"2000\" #inst \"2010\"))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(drop 50 (gen/sample (s/gen :ex/the-aughts) 55))")]
   [:div.side-by-side]]

  [:div.side-by-side-container
   [:div.side-by-side]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defpred the-aughts #(instance? java.util.Date %) #(last (gen/sample (s/gen :ex/the-aughts) 55)))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-predicate->generator the-aughts 5)" 65 65)]]

  [:div.note.smaller
   [:p [:code "spec.alpha"] " does a range of " [:code "instant"] "s."]
   [:p "Unfortunately, " [:code "test.check"] " does not provide an instance generator for Speculoos to borrow. So I'll shamelessly steal " [:code "spec.alpha"] "'s."]])


 (panel
  [:h3 "Range of doubles"]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(s/def :ex/dubs (s/double-in :min -100.0 :max 100.0 :NaN? false :infinite? false))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/dubs 2.9)")

    (prettyfy-form-prettyfy-eval "(s/valid? :ex/dubs Double/POSITIVE_INFINITY)")

    (prettyfy-form-prettyfy-eval "(gen/sample (s/gen :ex/dubs))")]

   [:div.side-by-side

    [:div.vspace]
    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(defpred dubs #(and (<= -100 % 100) (not (NaN? %)) (not (infinite? %))) #(gen/generate (gen/double* {:min -100 :max 100 :infinite? false \"NaN?\" true})))")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-predicate->generator dubs 10)")]]

  [:div.note.smaller
   [:p "Finally, " [:em "The spec Guide"] " illustrates generating doubles with specific conditions."]
   [:p "Speculoos leans on " [:code "test.check.generators"] " for that flexibility."]])



 (whats-next-panel
  compare-index
  [:div.note.smaller
   [:p "Frankly, when I started writing Speculoos, I would have guessed that it could mimic only some fraction of " [:code "spec.alpha"] ". I think this page demonstrates that it can fulfill a decent chunk. Perhaps somebody else beyond me feels that composing specifications the Speculoos way is more intuitive."]

   [:p "Still, Speculoos is very much a proof-of-concept, experimental prototype. Function instrumentation is really rough. Custom generators need more polishing. Many of the bottom-level functions could use attention to performance."]

   [:p [:a {:href "https://github.com/blosavio"} "Let me know"] " what you think."]])
 ]