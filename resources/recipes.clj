(ns recipes
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as hutil]
   [speculoos-hiccup :refer :all]))


(def recipes-UUID #uuid "256d193f-cd45-4a67-bf67-abbe6eb1776c")
(def ^:dynamic *eval-separator* ";; => ")


(spit "doc/recipes.html"
      (page-template
       "Speculoos — Recipes"
       recipes-UUID
       [:body
        (nav-bar "Recipes")
        (into [:article
               [:h1 "Speculoos Recipes"]
               [:section#intro
                [:p "Example usage, not too many words."]]]

              (section-nav
               [:section#valid-vs-validation
                [:h2 "Valid versus Validation"]
                [:p [:em "Validation"] " is the process of systematically applying predicates to scalars and collections. The validation process returns an exhaustive sequence of every datum-predicate pair and the truthy/falsey result of the validation."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.core :refer [validate-scalars valid-scalars? only-valid only-invalid]])")
                 [:br]
                 (print-form-then-eval "(validate-scalars [42 :foo \"abc\"] [int? keyword? boolean?])")]
                [:p "Speculoos provides some helper functions to filter the results."]
                [:pre
                 (print-form-then-eval "(only-valid (validate-scalars [42 :foo \"abc\"] [int? keyword? boolean?]))")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars [42 :foo \"abc\"] [int? keyword? boolean?]))")]
                [:p [:em "Valid"] " is a high level " [:code "true/false"] " summary of a validation. If the " [:code "only-invalids"] " sequence is empty, then " [:code "valid?"] " is " [:code "true"] "; " [:code "false"] " otherwise."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? keyword? string?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? keyword? boolean?])")]]


               [:section#scalar-validation
                [:h2 "Scalar validation"]
                [:p "Scalar validation applies predicates to basic Clojure datums like numbers, strings, booleans, characters, keywords, etc."]

                [:h3 "Empty collections"]
                [:p "Speculoos can validate in the absence of scalars. Predicates are simply ignored."]
                [:pre
                 (print-form-then-eval "(validate-scalars [] [int?])")
                 (print-form-then-eval "(validate-scalars '() [int?])")
                 (print-form-then-eval "(validate-scalars {} [int?])")
                 (print-form-then-eval "(validate-scalars #{} [int?])")]

                [:h3 "Empty specifications"]
                [:p "Validating may also proceed without predicates. Not much happens."]
                [:pre
                 (print-form-then-eval "(validate-scalars [42 :foo \"abc\"] [])")
                 (print-form-then-eval "(validate-scalars '(42 :foo \"abc\") [])")
                 (print-form-then-eval "(validate-scalars {:a 42 :b :foo :c \"abc\"} [])")
                 (print-form-then-eval "(validate-scalars #{42 :foo \"abc\"} [])")]

                [:h3 "Vectors"]
                [:p "Speculoos validates a vector's scalars in order."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42] [int?])")
                 (print-form-then-eval "(valid-scalars? [42 :foo] [int? keyword?])")
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? keyword? string?])")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars [42 :foo \"abc\"] [int? keyword? boolean?]))")]

                [:h3 "Lists"]
                [:p "Speculoos validates a list's scalars in order."]
                [:pre
                 (print-form-then-eval "(valid-scalars? (list 42) (list int?))")
                 (print-form-then-eval "(valid-scalars? (list 42 :foo) (list int? keyword?))")
                 (print-form-then-eval "(valid-scalars? (list 42 :foo \"abc\") (list int? keyword? string?))")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars (list 42 :foo \"abc\") (list int? keyword? boolean?)))")]

                [:h3 "Maps"]
                [:p "Speculoos validates a map's scalars by key."]
                [:pre
                 (print-form-then-eval "(valid-scalars? {:a 42} {:a int?})")
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} {:a int? :b symbol?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo :c \"abc\"} {:a int? :b symbol? :c string?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo :c \"abc\"} {:b symbol? :c string? :a int? })")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars {:a 42 :b 'foo :c \"abc\"} {:a int? :b boolean? :c string?}))")]

                [:h3 "Sets"]
                [:p "Speculoos validates a set's scalars by applying " [:em "all"] " predicates to " [:em "every"] " scalar. If all predicates return " [:code "true"] " for every scalar, then the data and specification are valid. If any scalar does not satisfy a single predicate, then it's invalid."]
                [:pre
                 (print-form-then-eval "(valid-scalars? #{42} #{int?})")
                 (print-form-then-eval "(valid-scalars? #{42 0 99} #{int?})")
                 (print-form-then-eval "(valid-scalars? #{42 'foo 99} #{int?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? #{42} #{int? number? pos?})")
                 (print-form-then-eval "(valid-scalars? #{42} #{int? string?})")
                 (print-form-then-eval "(valid-scalars? #{42 99 -11} #{int? number? pos?})")
                 [:br]
                 (print-form-then-eval "(defn int-or-str? [x] ((some-fn int? string?) x))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? #{42 \"abc\" 99} #{int-or-str?})")]


                [:h3 "Non-terminating sequences"]
                [:p "Speculoos can handle non-terminating sequences in two situations. First, Speculoos can validate a non-terminating sequence of scalars as long as the corresponding specification is not also a non-terminating sequence. Validation will proceed until the predicates run out."]
                [:pre
                 (print-form-then-eval "(valid-scalars? (repeat 42) [int?])")
                 (print-form-then-eval "(valid-scalars? (repeat 42) [int? int? int?])")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars (repeat 42) [int? string? int?]))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (cycle [42 :foo \"abc\"]) [int?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (cycle [42 :foo \"abc\"]) [int? keyword? string?])")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars (cycle [42 :foo \"abc\"]) [int? boolean? string?]))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (range) [int?])")
                 (print-form-then-eval "(valid-scalars? (range) [int? int? int?])")
                 (print-form-then-eval "(valid-scalars? (range) [int? boolean? int?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (iterate inc 1) [int?])")
                 (print-form-then-eval "(valid-scalars? (iterate inc 1) [int? int? int?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (iterate inc 1) [int? boolean? int?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (lazy-seq [1 2 3]) [int?])")
                 (print-form-then-eval "(valid-scalars? (lazy-seq [1 2 3]) [int? int? int?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (lazy-seq [1 2 3]) [int? boolean? int?])")]
                [:p "Second, Speculoos can use a specification that is composed of a non-terminating sequence as long as the data is not also a non-terminating sequence."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42] (repeat int?))")
                 (print-form-then-eval "(valid-scalars? [42 0 99] (repeat int?))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] (cycle [int? keyword? string?]))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] (cycle [int? boolean? string?]))")]

                [:h3 "Nested collections"]
                [:p "Speculoos can validate any scalar in a heterogeneous, arbitrarily-nested data structure. The scalar and predicate share a path into the data and specification, respectively. Another way to think about it is that the specification mimics the data's structure, and the predicate and scalar are located at the same position within each."]
                [:p "Examples of homogeneous, nested collections."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 [:foo] \"abc\"] [int? [keyword?] string?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 [:foo [\"abc\"]]] [int? [keyword? [string?]]])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [[42] [:foo] [\"abc\"]] [[int?] [keyword?] [string?]])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (list 42 (list :foo (list \"abc\"))) (list int? (list keyword? (list string?))))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b {:c 'foo :d \"abc\"}} {:a int? :b {:c symbol? :d string?}})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b {:c {:d 'foo :e \"abc\"}}} {:a int? :b {:c {:d symbol? :e string?}}})")]
                [:p "Examples of heterogeneous, nested collections."]
                [:pre
                 (print-form-then-eval "(validate-scalars [42 {:a 'foo :b \"abc\"}] [int? {:a symbol? :b string?}])")
                 [:br]
                 (print-form-then-eval "(validate-scalars [42 {:a ['foo {:b (list \"abc\")}]}] [int? {:a [symbol? {:b (list string?)}]}])")
                 [:br]
                 (print-form-then-eval "(validate-scalars [[[[42]]] [[[:foo]]] [[[\"abc\"]]]] [[[[int?]]] [[[keyword?]]] [[[string?]]]])")]
                [:p "Scalars contained in a nested set can be validated."]
                [:pre (print-form-then-eval "(valid-scalars? [#{42 0 99} #{'foo} #{:foo :bar :baz}] [#{int?} #{symbol?} #{keyword?}])")]

                [:h3 "Adjusting specifications"]
                [:p "What if the specification in your hand is close, but not exactly, what you need? You can relax a specification with a couple of strategies. First strategy: Speculoos will only validate if it has a predicate, so you could simply " [:em "remove"] " the offending predicate."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? keyword? boolean?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] (pop [int? keyword? boolean?]))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} {:a int? :b boolean?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} (dissoc {:a int? :b boolean?} :b))")]
                [:p "Second strategy: " [:em "Replace"] " the offending predicate with a more permissive predicate."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? keyword? boolean?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? keyword? any?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} {:a int? :b boolean?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} (assoc {:a int? :b boolean?} :b any?))")]
                [:p "If you need to dive into a nested structure to adjust the specification, Speculoos has a " [:a {:href "#fn-in"} "few functions"] " that'll help. Speculoos also provides a few " [:a {:href "#utility"} "utilities"] " specifically for adjusting specifications."]

                [:h3 "Repairing data"]
                [:p "What if you have a specification, and some data that almost satisfies it? You could adjust the data so that it satisfies the specification. The crude way would be to remove the offending datum."]
                [:pre
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} {:a int? :b string?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? (dissoc {:a 42 :b 'foo} :b) {:a int? :b string?})")]
                [:p "Another option involves associating a new value to satisfy the predicate."]
                [:pre (print-form-then-eval "(valid-scalars? (assoc {:a 42 :b 'foo} :b \"foo\") {:a int? :b string?})")]

                [:h3 "Optionality"]
                [:p "Speculoos provides a handful of ways to indicate optionality, depending on the situation. Speculoos does not infer nor assume; it only validates pairs of datums and predicates. Supplying predicates to a specification says " [:em "These datums may or may not exist. If the datums exist, they must satisfy these predicates. If the datums do not exist, the predicates are ignored."]]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 :foo] [int? keyword? string? boolean? ratio? char?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\" true] [int? keyword? string? boolean? ratio? char?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo} {:a int? :b symbol? :c string? :d boolean? :e ratio? :f char?})")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 42 :b 'foo :d true :f \\c} {:a int? :b symbol? :c string? :d boolean? :e ratio? :f char?})")]
                [:p "Within a sequence, if you'd like to validate some of the values, but skip over others, you have a few options to maintain the correspondence between the data and specification: " [:code "any?"] ", " [:code "(constantly true)"] ", or " [:code "(fn [_] true)"] "."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? any? string?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? (constantly true) string?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? (fn [_] true) string?])")]
                [:p "The results are the same. Pick the option that best signals your intent."]
                [:p "Maps are easier: Any key in both the specification and the data will be validated. Otherwise, unpaired datums or predicates are ignored."]
                [:pre (print-form-then-eval "(valid-scalars? {:validated 42 :ignored-datum 'foo} {:validated int? :ignored-predicate char?})")]
                [:p "Considering a single datum, you can bestow optionality by composing a predicate with " [:code "or"] ", or, my preference, " [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/some-fn"} [:code "clojure.core/some-fn"]] "."]
                [:pre (print-form-then-eval "(valid-scalars? [42 :foo \"abc\"] [int? #(or (symbol? %) (keyword? %)) (some-fn char? string?)])")]
                ] ;; end of Scalar Validation section


               [:section#collection-validation
                [:h2 "Collection validation"]
                [:p "Collection validation applies predicates to Clojure data structures like vectors, lists, maps, sets, lazy sequences, etc."]
                [:h3 "Empty specifications"]
                [:p "Speculoos will consume an empty specification, and return."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.core :refer [validate-collections valid-collections?]])")
                 [:br]
                 (print-form-then-eval "(validate-collections [] [])")
                 (print-form-then-eval "(validate-collections '() '())")
                 (print-form-then-eval "(validate-collections {} {})")
                 (print-form-then-eval "(validate-collections #{} #{})")]
                [:p "There is nothing particularly special about validating an empty collection. In fact, that's a pretty useful task."]
                [:pre
                 (print-form-then-eval "(valid-collections? [] [empty?])")
                 (print-form-then-eval "(valid-collections? '() '(empty?))")
                 (print-form-then-eval "(valid-collections? {} {:s empty?})")
                 (print-form-then-eval "(valid-collections? #{} #{empty?})")]

                [:h3#coll-vec "Vectors"]
                [:p "Collection predicates apply to the vectors that contain them."]
                [:pre
                 (print-form-then-eval "(valid-collections? [99 []] [(complement empty?) [empty?]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [1 2 3] [#(= 3 (count %))])")
                 [:br]
                 (print-form-then-eval "(defn length-1? [v] (= 1 (count v)))")
                 [:br]
                 (print-form-then-eval "(valid-collections? [[1] [2] [3]] [[length-1?] [length-1?] [length-1?]])")]
                [:p "Extending that last example: intervening collection predicates all apply to the root, parent vector."]
                [:pre
                 (print-form-then-eval "(valid-collections? [[1] [2] [3]] [vector? [length-1?] (complement empty?) [length-1?] any? [length-1?]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [[1] [2] [3]] [[length-1?] vector? (complement empty?) [length-1?] any? [length-1?]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [[1] [2] [3]] [[length-1?] [length-1?] any? (complement empty?) vector? [length-1?]])")]
                [:p "As seen above, any number of predicates apply to their parent collection, and in any order."]

                [:h3 "Lists"]
                [:p "Speculoos validates lists exactly the same as vectors, so " [:a {:href "#coll-vec"} "everything above"] " holds for lists. A few quick examples."]
                [:pre
                 (print-form-then-eval "(valid-collections? '() '(list?))")
                 [:br]
                 (print-form-then-eval "(valid-collections? '(1 2 3) '(#(= 3 (count %))))")
                 [:br]
                 (print-form-then-eval "(valid-collections? '((1) (2) (3)) '((length-1?) (length-1?) (length-1?)))")
                 [:br]
                 (print-form-then-eval "(valid-collections? '((1) (2) (3)) '((length-1?) list? (length-1?) (length-1?)))")]

                [:h3 "Maps"]
                [:p "Collection predicates apply to the maps that contain them, as long as the key does not appear in the data's corresponding map. Any number of predicates may be supplied. Since the specification's keys don't matter, you can name them something informative."]
                [:pre
                 (print-form-then-eval "(valid-collections? {:a 42} {:map-spec empty?})")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 42} {:coll-test map?})")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 42} {:coll-test map? :coll-size #(= 1 (count %))})")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 42} {:has-key? #(contains? % :a)})")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 42 :b 'foo :c 99} {:all-vals-ints? #(every? int? (vals %))})")]

                [:h3 "Sets"]
                [:p "Predicates within a set apply to the set itself as a whole."]
                [:pre
                 (print-form-then-eval "(valid-collections? #{1 2 3} #{#(every? int? %)})")
                 [:br]
                 (print-form-then-eval "(valid-collections? #{1 2 :foo} #{#(every? int? %)})")
                 [:br]
                 (print-form-then-eval "(valid-collections? #{1 2 3} #{#(every? int? %)
                                                                                #(every? pos? %)})")
                 [:br]
                 (print-form-then-eval "(valid-collections? #{1 2 3} #{#(every? int? %)
                                                                                #(every? even? %)})")]
                [:p "All elements not something."]
                [:pre (print-form-then-eval "(valid-collections? #{1 2 3} #{#(every? (complement keyword?) %)})")]
                [:p "For every vector contained in a set, does it contain one integer?"]
                [:pre
                 (print-form-then-eval "(valid-collections? #{:a [1] :b [42] :c [99]} #{(fn [s] (every? #(= 1 (count %)) (filter vector? s)))})")
                 [:br]
                 (print-form-then-eval "(valid-collections? #{:a [1 2 3] :b } #{(fn [s] (every? #(= 1 (count %)) (filter vector? s)))})")]

                [:h3 "Non-terminating Sequences"]
                [:p "You may supply a (possibly) non-terminating sequence in the data, or in the specification, but not both. During collection validation, the non-terminating sequence is clamped to a length of the other corresponding sequence."]
                [:pre
                 (print-form-then-eval "(set! *print-length* 99)")
                 [:br]
                 (print-form-then-eval "(valid-collections? (range 99) [#(every? int? %)])")
                 [:br]
                 (print-form-then-eval "(valid-collections? (range 99) [#(every? ratio? %)])")
                 [:br]
                 (print-form-then-eval "(valid-collections? (cycle [42 :foo \"abc\"]) [#(valid-scalars? % [int? keyword? string?])])")
                 [:br]
                 (print-form-then-eval "(valid-collections? (iterate #(+ 3 %) 1) [(fn [s] (every? true? (map #(= (+ 3 %1) %2) s (next s)))) (constantly true) (constantly true)])")
                 [:br]
                 (print-form-then-eval "(valid-collections? (repeat :na) [(fn [s] (every? #(= :na %) s)) (constantly true) (constantly true)])")]

                [:h3 "Nested collections"]
                [:p "Same rule, recursively: predicates apply to their parent collection."]
                [:pre
                 (print-form-then-eval "(valid-collections? [[1] [:a] ['foo]] [[#(every? int? %)] [#(every? keyword? %)] [#(every? symbol? %)]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [[1 22/7] [:a] ['foo]] [[#(every? int? %)] [#(every? keyword? %)] [#(every? symbol? %)]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [11 [22 33 44]] [vector? [#(= 3 (count %))] #(= 2 (count %))])")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 11 :b {:c 22}} {:has-keys? #(and (contains? % :a)
                                                                                                           (contains? % :b))
                                                                                          :b {:is-map? map?
                                                                                              :one-MapEntry? #(= 1 (count %))}})")
                 [:br]
                 (print-form-then-eval "(valid-collections? (list 11 (list 22 33)) (list list? (list #(<= 2 (count %))) #(= 2 (count %))))")]
                [:p "Heterogeneous, nested collections work the same way."]
                [:pre
                 (print-form-then-eval "(valid-collections? [{:a 1 :b 2} 33 {:d 4 :e 5} 66] [#(= 2 (count (filter map? %)))
                                                                                                       {:has? #(contains? % :b)}
                                                                                                       {:has? #(contains? % :d)}
                                                                                                       (fn [v] (every? int? (filter #((complement map?) %) v)))])")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a [1 2 3] :b (list \\z \\q \\w) :c {:x \"abc\" :y 42}}
                                                                      {:expected-count? #(= 3 (count %))
                                                                       :a {:type vector?
                                                                           :length #(<= 2 (count %))}
                                                                       :c {:contains? #(contains? % :x)}
                                                                       :b {:kind list?
                                                                           :all-same? (fn [s] (apply = (map type s)))}})")
                 [:br]
                 (print-form-then-eval "(valid-collections? [[[[]]]] [[[[empty?]]]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a {:b {:c {:d {}}}}} {:a {:b {:c {:d {:contains-anything empty?}}}}})")]

                [:h3 "Sequence regexes"]
                [:p "Replicating " [:code "spec.alpha"] "'s regular expression facilities."]
                [:p "One or more integers, followed by zero or more strings."]
                [:pre
                 (print-form-then-eval "(defn int+string* [s]
                                           (let [[front rear] (split-with int? s)
                                                 ints #(<= 1 (count front))
                                                 strs (every? string? rear)]
                                             (and ints strs)))")
                 [:br]
                 (print-form-then-eval "(int+string* [1 2 3 \"a\" \"b\"])")
                 (print-form-then-eval "(int+string* [1 2 3])")
                 (print-form-then-eval "(int+string* [1 2 3 'a 'b])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [1 2 3 \"a\" \"b\"] [int+string*])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [1 2 3 'a 'b] [int+string*])")]
                [:p "Exactly three keywords, followed by anything."]
                [:pre
                 (print-form-then-eval "(defn three-kw-and? [s] (= 3 (count (take-while keyword? s))))")
                 [:br]
                 (print-form-then-eval "(valid-collections? [:a :b :c] [three-kw-and?])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [:a :b] [three-kw-and?])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [:a :b :c 1 2 3] [three-kw-and?])")]
                [:p [:code "seq-regex"] ", a little toy utility that streamlines this task. One or more integers, followed by zero or more strings."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.utility :refer [seq-regex]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [1 2 3 \"a\" \"b\" \"c\"] [#(seq-regex % int? :+ string? :*)])")]
                [:p "Exactly three keywords, followed by anything."]
                [:pre (print-form-then-eval "(valid-collections? [:a :b :c 'foo \"bar\" \\z] [#(seq-regex % keyword? 3 any? :*)])")]
                [:p "See also " [:a {:href "https://github.com/miner/herbert"} "Herbert"] " and " [:a {:href "https://github.com/cgrand/seqexp"} "Seqexp"] "."]

                [:h3 "Adjusting specifications"]
                [:p "Speculoos specifications are just Clojure data structures, containing Clojure predicates. Adjust specifications with your favorite tools. " [:code "clojure.core"] " provides some nice ones."]
                [:pre
                 (print-form-then-eval "(valid-collections? [[11] [22] [33]] [[#(every? int? %)] [#(= 1 (count %))] [#(not-empty %)]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [[11] [22] [33]] (drop-last [[#(every? int? %)] [#(= 1 (count %))] [#(not-empty %)]]))")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a [1 2 3] :b ['foo 'bar 'baz]} {:a [#(every? int? %)] :b [#(every? int? %)]})")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a [1 2 3] :b ['foo 'bar 'baz]}
                                                                      (assoc-in {:a [#(every? int? %)] :b [#(every? int? %)]} [:b 0] #(every? symbol? %)))")]
                [:p "Speculoos provides some specialized functions for manipulating heterogeneous, arbitrarily-nested data structures. Changing a predicate with " [:code "assoc-in*"] "."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.fn-in :refer [get-in* assoc-in* update-in* dissoc-in*]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [99 {:a 1 :b (list 42 22/7 [\"abc\" {:c [\\z \\q]}])}]
                                                                      [{:b (list [{:c [map?]}])}])")
                 [:br]
                 (print-form-then-eval "(valid-collections? [99 {:a 1 :b (list 42 22/7 [\"abc\" {:c [\\z \\q]}])}]
                                                                      (assoc-in* [{:b (list [{:c [map?]}])}] [0 :b 0 0 :c 0] vector?))")]
                [:p "Removing a predicate with " [:code "dissoc-in*"] "."]
                [:pre
                 (print-form-then-eval "(valid-collections? {:a [99 (list {:b [42]})]}
                                                                      {:coll-test map? :a [(list {:b [map?]})]})")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a [99 (list {:b [42]})]}
                                                                      (dissoc-in* {:coll-test map? :a [(list {:b [map?]})]} [:a 0 0 :b 0]))")]

                [:h3 "Repairing data"]
                [:p "Adjust or remove datums such that the data collection satisfies your specification. Using " [:code "clojure.core"] " functions."]
                [:pre
                 (print-form-then-eval "(valid-collections? [[1] [2 3] [4 5 6]] [[vector?] [vector?] [map?]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? (update-in [[1] [2 3] [4 5 6]] [2] #(zipmap [:a :b :c] %)) [[vector?] [vector?] [map?]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 42 :b {:c 99}} {:b {:has-anything? #(= 0 (count %))}})")
                 [:br]
                 (print-form-then-eval "(valid-collections? (assoc-in {:a 42 :b {:c 99}} [:b] {}) {:b {:has-anything? #(= 0 (count %))}})")]
                [:p "Using Speculoos' " [:code "fn-in*"] " functions."]
                [:pre
                 (print-form-then-eval "(valid-collections? [11 [22 33 [44 55 66 [77]]]] [[[[list?]]]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? (update-in* [11 [22 33 [44 55 66 [77]]]] [1 2 3] #(into '() %)) [[[[list?]]]])")
                 [:br]
                 (print-form-then-eval "(valid-collections? {:a 42 :b {:c {:d [99 0 'foo]}}} {:b {:c {:d [empty?]}}})")
                 [:br]
                 (print-form-then-eval "(valid-collections? (dissoc-in* {:a 42 :b {:c {:d [99]}}} [:b :c :d 0]) {:b {:c {:d [empty?]}}})")
                 ]] ;; end of Collection Validation section

               [:section#comprehensive-validation
                [:h2 "Comprehensive validation"]
                [:p "Perform independent scalar and collection validations with a single function call. " [:code "valid?"] " returns " [:code "true/false"] "."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.core :refer [valid? validate]])")
                 [:br]
                 (print-form-then-eval "(def no-strings? #(every? (complement string?) %))")
                 [:br]
                 (print-form-then-eval "(valid? [42 :foo \\z] [int? keyword? char?] [no-strings?])")
                 [:br]
                 (print-form-then-eval "(valid? {:a 42 :b ['foo \"abc\"]} {:a int? :b [symbol? string?]} {:has? #(contains? % :a) :b [#(<= 2 (count %))]})")]
                [:p [:code "validate"] " returns the validation report."]
                [:pre (print-form-then-eval "(validate [42 :foo \\z] [int? keyword? char?] [no-strings?])")]
                [:p "Highlight invalid results with " [:code "only-invalid"] "."]
                [:pre
                 (print-form-then-eval "(def map-predicate {:a int? :b string?})")
                 [:br]
                 (print-form-then-eval "(def no-chars? #(every? (complement char?) (vals %)))")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate [{:a 42 :b \"abc\"} {:a 99 :b \\z}] [map-predicate map-predicate] [vector? {} {:any-chars? no-chars?}]))")]
                [:p "Require that every scalar and every collection have at least one predicate, and all those predicates are satisfied."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.utility :refer [thoroughly-valid?]])")
                 [:br]
                 [:code ";; all scalars and collections have a predicate, all predicates satisfied"]
                 (print-form-then-eval "(thoroughly-valid? [42 {:x 'foo :y 22/7}]
                                                            [int? {:x symbol? :y ratio?}]
                                                            [vector? {:what-am-i? map?}])")
                 [:br]
                 [:code ";; all scalars and collections have a predicate, ratio predicate not satisfied"]
                 (print-form-then-eval "(thoroughly-valid? [42 {:x 'foo :y 99}]
                                                            [int? {:x symbol? :y ratio?}]
                                                            [vector? {:what-am-i? map?}])")
                 [:br]
                 [:code ";; all predicates satisfied, but nested map lacks a predicate"]
                 (print-form-then-eval "(thoroughly-valid? [42 {:x 'foo :y 22/7}]
                                                            [int? {:x symbol? :y ratio?}]
                                                            [vector? {}])")]
                ] ;; end of Comprehensive validation section


               [:section#function-validation
                [:h2 "Function validation"]
                [:p "Check supplied arguments and/or the function's return value."]
                [:h3 "Validating functions with external metadata"]
                [:p [:em "ad hoc"] " function specification by wrapping function with metadata."]
                [:pre
                 (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-with validate-fn-meta-spec inject-specs! validate-higher-order-fn exercise-fn instrument unstrument]])")
                 [:br]
                 (print-form-then-eval "(validate-fn-with + {:speculoos/arg-scalar-spec [int? int?]} 1 2)")
                 [:br]
                 (print-form-then-eval "(validate-fn-with + {:speculoos/arg-scalar-spec [int? int?]} 1 9.87)")]
                [:h3 "Injecting metadata"]
                [:p "Altering the function " [:code "var"] "'s metadata to provide the specification."]
                [:pre
                 (print-form-then-eval "(inject-specs! + {:speculoos/arg-scalar-spec [int? int?]})")
                 [:br]
                 (print-form-then-eval "(validate-fn-meta-spec + 1 2)")
                 [:br]
                 (print-form-then-eval "(validate-fn-meta-spec + 1 9.87)")]
                [:p "Metadata specifications are consulted only when Speculoos explicitly validates."]
                [:pre (print-form-then-eval "(+ 1 9.87)")]
                [:p "Argument sequence validated as a collection."]
                [:pre
                 (print-form-then-eval "(def all-numbers? #(every? number? %))")
                 [:br]
                 (print-form-then-eval "(validate-fn-with + {:speculoos/arg-collection-spec [all-numbers?]} 1 9.87 22/7)")
                 [:br]
                 (print-form-then-eval "(validate-fn-with + {:speculoos/arg-collection-spec [all-numbers?]} 1 9.87 \"abc\")")]
                [:p "Performing both scalar and collection argument validation."]
                [:pre
                 (print-form-then-eval "(def length-at-most-2? #(>= 2 (count %)))")
                 (print-form-then-eval "(validate-fn-with + {:speculoos/arg-scalar-spec [int? int?]
                                                                       :speculoos/arg-collection-spec [length-at-most-2?]} 1 2)")
                 [:br]
                 (print-form-then-eval "(validate-fn-with + {:speculoos/arg-scalar-spec [int? int? int?]
                                                                       :speculoos/arg-collection-spec [length-at-most-2?]} 1 2 3.45)")]
                [:h3 "Validating functions with internal metadata"]
                [:p "Supply specifications during function definition."]
                [:pre
                 (print-form-then-eval "(defn add-two-things {:speculoos/arg-scalar-spec [float? ratio?]} [x y] (+ x y))")
                 [:br]
                 (print-form-then-eval "(add-two-things 1.23 22/7)")
                 (print-form-then-eval "(add-two-things 1 2)")
                 [:br]
                 (print-form-then-eval "(validate-fn-meta-spec add-two-things 1.23 22/7)")
                 [:br]
                 (print-form-then-eval "(validate-fn-meta-spec add-two-things 1 2)")]
                [:h3 "Validating higher-order functions"]
                [:p "Supply enough arguments so that the " [:span.small-caps "hof"] " returns a non-function."]
                [:pre
                 (print-form-then-eval "(defn example-hof [a b] (fn [c d] (fn [e f] (+ (* a c e) (* b d f)))))")
                 [:br]
                 (print-form-then-eval "(((example-hof 20 2) 30 3) 40 4)")
                 [:br]
                 (print-form-then-eval "(inject-specs! example-hof {:speculoos/arg-scalar-spec [int? int?]
                                                                              :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? int?]
                                                                                                    :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? int?]}}})")
                 [:br]
                 (print-form-then-eval "(validate-higher-order-fn example-hof [20 2] [30 3] [40 4])")
                 [:br]
                 (print-form-then-eval "(validate-higher-order-fn example-hof [20 2.0] [30 3.0] [40 4.0])")]
                [:h3 "Instrumenting functions"]
                [:p "Arguments and returns implicitly validated on every function invocation."]
                [:pre
                 (print-form-then-eval "(defn multiplier {:speculoos/arg-scalar-spec [int? pos-int? ratio?]
                                                           :speculoos/arg-return-spec [number? string?]}
                                           [x y z]
                                           [(* x y z) (str y)])")
                 [:br]
                 [:code "(multiplier 2 3 8/2) " *eval-separator* "[24 \"3\"]"]
                 [:code "(multiplier 2 3 4) " *eval-separator* "[24 \"3\"]"]
                 [:br]
                 [:code "(instrument multiplier)"]
                 [:code "(multiplier 2 3 8/2) " *eval-separator* "[24 \"3\"]"]
                 [:code "(multiplier 2 3 4) " *eval-separator* "[24 \"3\"]"]
                 [:br]
                 [:code "(with-out-str (multiplier 2 3 4))\n" *eval-separator* "({:path [2], :datum 4, :predicate #function[clojure.core/ratio?], :valid? false})\n"]
                 [:br]
                 [:code "(unstrument multiplier) " *eval-separator* "{}"]
                 [:code "(multiplier 2 3 8/2) " *eval-separator* "[24 \"3\"]"]
                 [:code "(multiplier 2 3 4) " *eval-separator* "[24 \"3\"]"]]
                [:h3 "Exercising functions"]
                [:p "Invoke function repeatedly with sample data generated from argument scalar specification."]
                [:pre
                 (print-form-then-eval "(defn shoe {:speculoos/arg-scalar-spec [pos-int? pos-int? string?]} [x y z] (str x \", \" y \", buckle my \" z))")
                 [:br]
                 (print-form-then-eval "(shoe 3 4 \"sandal\")")
                 [:br]
                 (print-form-then-eval "(exercise-fn shoe 3)")]
                [:h3 "Validating macros"]
                [:p "Speculoos validates macros by applying specifications against the macro expansion."]
                [:pre
                 (print-form-then-eval "(defmacro infix [form] (list (second form) (first form) (last form)))")
                 [:br]
                 (print-form-then-eval "(infix (2 + 3))")
                 (print-form-then-eval "(macroexpand-1 '(infix (2 + 3)))")
                 [:br]
                 (print-form-then-eval "(def infix-spec (list symbol? int? int?))")
                 [:br]
                 (print-form-then-eval "(require '[speculoos.core :refer [valid-macro?]])")
                 [:br]
                 (print-form-then-eval "(valid-macro? '(infix (2 + 3)) infix-spec)")
                 [:br]
                 (print-form-then-eval "(valid-macro? '(infix (2.2 + 3.3)) infix-spec)")]
                ] ;; end of Function Validation section


               [:section#fn-in
                [:h2 (h2/html [:code "fn*"] " and " [:code "fn-in*"])]
                [:p "Starred functions inspect and manipulate values buried in heterogeneous, arbitrarily-nested data structures."]
                [:h3 "Inspecting values"]
                [:pre
                 (print-form-then-eval "(require '[speculoos.fn-in :refer [get-in* assoc-in* update-in* dissoc-in*]])")
                 [:br]
                 (print-form-then-eval "(get-in* [11 22 [33 44 55 [66 [77 [88 99]]]]] [2 3 1 1 1])")
                 [:br]
                 (print-form-then-eval "(get-in* {:a {:b {:c {:d 99}}}} [:a :b :c :d])")
                 [:br]
                 (print-form-then-eval "(get-in* (list 11 22 33 (list 44 (list 55))) [3 1 0])")
                 [:br]
                 (print-form-then-eval "(get-in* #{11 #{22}} [#{22} 22])")
                 [:br]
                 (print-form-then-eval "(get-in* [11 22 {:a 33 :b [44 55 66 {:c [77 88 99]}]}] [2 :b 3 :c 2])")
                 [:br]
                 (print-form-then-eval "(get-in* {:a (list {} {:b [11 #{33}]})} [:a 1 :b 1 33])")]
                [:h3 "Associating values"]
                [:pre
                 (print-form-then-eval "(assoc-in* [11 [22 [33 [44 55 66]]]] [1 1 1 2] :new-val)")
                 [:br]
                 (print-form-then-eval "(assoc-in* {:a {:b {:c 42}}} [:a :b :c] 99)")
                 [:br]
                 (print-form-then-eval "(assoc-in* {:a [11 22 33 [44 55 {:b [66 {:c {:d 77}}]}]]} [:a 3 2 :b 1 :c :d] \"foo\")")]
                [:h3 "Updating values"]
                [:pre
                 (print-form-then-eval "(update-in* [11 22 33 [44 [55 66 [77 88 99]]]] [3 1 2 2] inc)")
                 [:br]
                 (print-form-then-eval "(update-in* {:a [11 22 {:b 33 :c [44 55 66 77]}]} [:a 2 :c 1] #(+ 5500 %))")]
                [:h3 "Dissociating values"]
                [:pre
                 (print-form-then-eval "(dissoc-in* [11 22 [33 [44 55 66]]] [2 1 1])")
                 [:br]
                 (print-form-then-eval "(dissoc-in* {:a [11 22 33 {:b 44 :c [55 66 77]}]} [:a 3 :c 0])")]
                [:h3 "Clamping non-terminating sequences"]
                [:pre
                 (print-form-then-eval "(require '[speculoos.utility :refer [clamp-in*]])")
                 [:br]
                 (print-form-then-eval "(clamp-in* [11 22 33 [44 55 [66 (repeat 42)]]] [3 2 1] 3)")
                 [:br]
                 (print-form-then-eval "(clamp-in* {:a [11 22 33 {:b (cycle ['foo \"bar\" \\z])}]} [:a 3 :b] 5)")]
                ] ;; end of fn-in* section


               [:section#utility
                [:h2 "Utility functions"]
                [:p "Functions to get things done with Speculoos."]
                [:h3 "All paths"]
                [:p "Exhaustive sequence of paths of all values in a heterogeneous, arbitrarily-nested data structure."]
                [:pre (print-form-then-eval "(speculoos.core/all-paths [11 22 {:a 'foo :b \\z}])")]
                [:h3 "Data from scalar specification"]
                [:pre
                 (print-form-then-eval "(require '[speculoos.utility :refer [data-from-spec spec-from-data basic-collection-spec-from-data all-specs-okay scalars-without-predicates collections-without-predicates exercise in? defpred unfindable-generators validate-predicate->generator]])")
                 [:br]
                 (print-form-then-eval "(data-from-spec [int? {:a string? :b ratio?}])")]
                [:h3 "Scalar specification from data"]
                [:pre (print-form-then-eval "(spec-from-data {:a 42 :b [\\z 'foo \"xyz\"] :c 2/3})")]
                [:p "Collection specification from data"]
                [:pre
                 [:code
                  "(basic-collection-spec-from-data
  ['ignored-val {:ignored-key 'another-ignored-val} (list)])
;; => [{:collection-predicate map?} (list?) vector?]"]]
                [:h3 "Checking specifications"]
                [:pre
                 (print-form-then-eval "(all-specs-okay [int? string? {:a char?}])")
                 [:br]
                 (print-form-then-eval "(all-specs-okay [int? :not-a-predicate {:a char?}])")]
                [:h3 "Data without a corresponding scalar predicate"]
                [:pre (print-form-then-eval "(scalars-without-predicates [11 {:a 22 :b \"abc\"}] [int? {:b string?}])")]
                [:h3 "Collections without a corresponding collection predicate"]
                [:pre (print-form-then-eval "(collections-without-predicates [42 {:a (list 22/7)} ['foo]] [vector? {:a (list list?) :is-map? map?}])")]
                [:h3 "Filtering validation results"]
                [:pre (print-form-then-eval "(only-invalid (validate-scalars [11 [\"abc\" [:foo]]] [int? [string? [symbol?]]]))")]
                [:h3 "Exercising specifications"]
                [:pre (print-form-then-eval "(exercise {:a int? :b [char? keyword?]} 3)")]
                [:h3 "Advanced random sample generators"]
                [:p "Built-in basic generator, set generator, and string regex generator."]
                [:pre (print-form-then-eval "(data-from-spec [int? #{:water :coffee :tea :milk} #\"fo{3,5}!\"] :random)")]
                [:p "Custom generators attached to predicate's metadata."]
                [:pre
                 (print-form-then-eval "(def manual-predicate (with-meta #(int? %) {:speculoos/predicate->generator #(rand-int 99)}))")
                 [:br]
                 (print-form-then-eval "(defpred easier-predicate #(int? %) #(rand-int 99))")
                 [:br]
                 (print-form-then-eval "(data-from-spec [manual-predicate easier-predicate] :random)")]
                [:p "Automatic random sample generators. " [:code "or"] " generators emit multiple types."]
                [:pre
                 (print-form-then-eval "(defpred or-predicate #(or (int? %) (string? %) (keyword? %)))")
                 [:br]
                 (print-form-then-eval "(data-from-spec (repeat 5 or-predicate) :random)")]
                [:p [:code "and"] " refines a basic generator."]
                [:pre
                 (print-form-then-eval "(defpred and-predicate (fn [i] (and (int? i) (odd? i) (<= 3 i))))")
                 [:br]
                 (print-form-then-eval "(data-from-spec (repeat 5 and-predicate) :random)")]
                [:p "Combining " [:code "and"] "/" [:code "or"] "."]
                [:pre
                 (print-form-then-eval "(defpred and-or-predicate #(or (and (int? %)
                                                                           (pos? %))
                                                                      (and (string? %)
                                                                           (<= 3 (count %)))
                                                                      (and (ratio? %)
                                                                           (<= 1/9 %))))")
                 [:br]
                 (print-form-then-eval "(data-from-spec (repeat 4 and-or-predicate) :random)")]
                [:p "Identifying predicates without generators."]
                [:pre [:code "(unfindable-generators
  [manual-predicate (fn no-gen [] (rand-int 99)) easier-predicate])
;; => [{:path [1], :value no-gen}]"]]
                [:p "Checking a generator against its host predicate."]
                [:pre
                 (print-form-then-eval "(defpred pred-with-broken-gen #(neg? %) #(rand-int 99))")
                 [:br]
                 (print-form-then-eval "(validate-predicate->generator pred-with-broken-gen 5)")]
                [:h3 "Validate with path specification"]
                [:p "Alternative validation function that uses explicit paths to any value — scalar or collection — and multi-arity predicates."]
                [:pre
                 (print-form-then-eval "(def path-predicate #(= (/ %1 %2) %3))")
                 [:br]
                 (print-form-then-eval "(speculoos.core/validate-with-path-spec [11 {:b [\\z \"abc\" 22]} 2]
                                                                            [{:paths [[1 :b 2] [0] [2]]
                                                                              :predicate path-predicate}])")]
                [:h3 "Item in collection?"]
                [:p "Alternative to " [:code "clojure.core/contains?"] "."]
                [:pre
                 [:code ";; probably not what you expected"]
                 (print-form-then-eval "(contains? [:a :b :c] 1)")
                 [:br]
                 [:code  ";; use `in?` instead"]
                 (print-form-then-eval "(in? [:a :b :c] 1)")
                 (print-form-then-eval "(in? [:a :b :c] :b)")
                 (print-form-then-eval "(in? (list 'foo 'bar) 'bar)")]
                ] ;; end of Utility secton

               [:section#predicates
                [:h2 "Predicates"]
                [:p "Speculoos predicates are regular Clojure functions and compose the same way."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 \"abc\"] [#(and (int? %) (even? %)) #(and (string? %) (= 3 (count %)))])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? {:a 'foo} {:a #(or (char? %) (symbol? %))})")]
                [:p [:code "clojure.core"] " provides some nice higher-order functions for composing predicates: " [:code "every-pred"] " and " [:code "some-fn"] "."]
                [:pre (print-form-then-eval "(valid-scalars? [42 \"abc\"] [(every-pred int? even?) (some-fn symbol? string?)])")]
                [:h3 "Placeholders, ambivalence"]
                [:p "Maintain a specification's correspondence to a sequence."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [42 \"abc\" :foo] [int? symbol? keyword?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 \"abc\" :foo] [int? any? keyword?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 \"abc\" :foo] [int? (constantly true) keyword?])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [42 \"abc\" :foo] [int? (fn [_] true) keyword?])")]
                [:h3 "Regular expression predicates"]
                [:p [:code "nil"] " returned from a regular expression failure can indicate an invalid string."]
                [:pre
                 (print-form-then-eval "(def example-regex #\"\\w{3}\\d{3}\")")
                 [:br]
                 (print-form-then-eval "(re-matches example-regex \"abc123\")")
                 (print-form-then-eval "(re-matches example-regex \"qr89\")")
                 [:br]
                 (print-form-then-eval "(def regex-predicate #(re-matches example-regex %))")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [\"abc123\" \"xyz789\"] [regex-predicate regex-predicate])")
                 [:br]
                 (print-form-then-eval "(only-invalid (validate-scalars [\"abc123\" \"qr89\"] [regex-predicate regex-predicate]))")]
                [:p "Speculoos treats free-floating regexes as predicates."]
                [:pre (print-form-then-eval "(validate-scalars [\"abc123\" \"qr89\"] [#\"\\w{3}\\d{3}\" #\"[qrs]{2}[789]{2}\"])")]
                [:h3 "Sequence regexes"]
                [:p "Mimicking " [:code "spec.alpha"] "'s sequence regexes, use " [:code "speculoos.utility/seq-regex"] " during a collection validation."]
                [:pre
                 (print-form-then-eval "(def three-ints-two-kw-and-syms? #(seq-regex % int? 3 keyword? 2 symbol? :*))")
                 [:br]
                 (print-form-then-eval "(valid-collections? [1 2 3 :a :b 'foo 'bar 'baz 'qux] [three-ints-two-kw-and-syms?])")
                 [:br]
                 (print-form-then-eval "(def sym-str-opt-int? #(seq-regex % symbol? :. string? :. int? :?))")
                 [:br]
                 (print-form-then-eval "(valid-collections? (list 'foo \"abc\" 42) (list sym-str-opt-int?))")]
                [:h3 "Sets as predicates"]
                [:p "Within a scalar specification, a set creates a predicate that tests for membership."]
                [:pre
                 (print-form-then-eval "(valid-scalars? [:orange] [#{:red :orange :yellow}])")
                 [:br]
                 (print-form-then-eval "(valid-scalars? [:pantheon] [#{:red :orange :yellow}])")]
                [:h3 "Testing functions as datums"]
                [:p "When the data contains a function, predicates can perform " [:em "property testing"] " to validate the datum."]
                [:pre
                 (print-form-then-eval "(defn one-something-two-equals-three? [f] (= 3 (f 1 2)))")
                 [:br]
                 (print-form-then-eval "(one-something-two-equals-three? +)")
                 [:br]
                 (print-form-then-eval "(validate-scalars [+ -] [one-something-two-equals-three? one-something-two-equals-three?])")
                 ]] ;; end of Predicates section

               ))]))    ;; compile target