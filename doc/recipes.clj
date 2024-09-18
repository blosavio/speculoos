(ns speculoos.recipes
  "Example usage, not too many words.

  https://github.com/blosavio/speculoos")


;;;; 'valid' versus 'validation'

;;; _Validation_ is the process of systematically applying predicates to scalars
;;; and collections. The validation process returns an exhaustive sequence of
;;; every datum-predicate pair and the truthy/falsey result of the validation.

(require '[speculoos.core :refer
           [validate-scalars valid-scalars? only-valid only-invalid]])


(validate-scalars [42 :foo "abc"] [int? keyword? boolean?])


;; Speculoos provides some helper functions to filter the results.

(only-valid (validate-scalars [42 :foo "abc"]
                              [int? keyword? boolean?]))


(only-invalid (validate-scalars [42 :foo "abc"]
                                [int? keyword? boolean?]))

;; _valid_ is a high level true/false summary of a validation. If the
;; `only-invalids` sequence is empty, then `valid?` is `true`; `false` otherwise.

(valid-scalars? [42 :foo "abc"]
                [int? keyword? string?])

(valid-scalars? [42 :foo "abc"]
                [int? keyword? boolean?])




;;;; Scalar validation

;;; Scalar validation applies predicates to basic Clojure datums like numbers,
;;; strings, booleans, characters, keywords, etc.

;; Empty collections. Speculoos can validate in the absence of scalars.
;; Predicates are simply ignored.

(validate-scalars []
                  [int?])



;; Empty specifications. Validating may also proceed without predicates. Not
;; much happens.

(validate-scalars [42 :foo "abc"]
                  [])

;; Vectors: Speculoos validates a vector's scalars in order.

(valid-scalars? [42]
                [int?])

(valid-scalars? [42 :foo]
                [int? keyword?])

(valid-scalars? [42 :foo "abc"]
                [int? keyword? string?])

(only-invalid (validate-scalars [42 :foo "abc"]
                                [int? keyword? boolean?]))


;;Lists: Speculoos validates a list's scalars in order.

(valid-scalars? (list 42)
                (list int?))

(valid-scalars? (list 42 :foo)
                (list int? keyword?))

(valid-scalars? (list 42 :foo "abc")
                (list int? keyword? string?))

(only-invalid (validate-scalars
               (list 42 :foo "abc")
               (list int? keyword? boolean?)))


;; Maps: Speculoos validates a map's scalars by key.

(valid-scalars? {:a 42}
                {:a int?})

(valid-scalars? {:a 42, :b 'foo}
                {:a int?, :b symbol?})

(valid-scalars? {:a 42, :b 'foo, :c "abc"}
                {:a int?, :b symbol?, :c string?})

(valid-scalars? {:a 42, :b 'foo, :c "abc"}
                {:b symbol?, :c string?, :a int?})

(only-invalid (validate-scalars
                {:a 42, :b 'foo, :c "abc"}
                {:a int?, :b boolean?, :c string?}))


;; Sets: Speculoos validates a set's scalars by applying all predicates to every
;; scalar. If all predicates return `true` for every scalar, then the data and
;; specification are valid. If any scalar does not satisfy a single predicate,
;; then it's invalid.

(valid-scalars? #{42}
                #{int?})

(valid-scalars? #{42 0 99}
                #{int?})

(valid-scalars? #{42 'foo 99}
                #{int?})

(valid-scalars? #{42}
                #{int? number? pos?})

(valid-scalars? #{42}
                #{int? string?})

(valid-scalars? #{42 99 -11}
                #{int? number? pos?})


(defn int-or-str? [x] ((some-fn int? string?) x))

(valid-scalars? #{42 "abc" 99}
                #{int-or-str?})


;; Non-terminating sequences: Speculoos can handle non-terminating sequences in
;; two situations.

;; Situation 1: Speculoos can validate a non-terminating sequence of
;; scalars as long as the corresponding specification is not also a
;; non-terminating sequence. Validation will proceed until the predicates run
;; out.

(valid-scalars? (repeat 42)
                [int?])

(valid-scalars? (repeat 42)
                [int? int? int?])

(only-invalid (validate-scalars (repeat 42)
                                [int? string? int?]))

(valid-scalars? (cycle [42 :foo "abc"])
                [int?])

(valid-scalars? (cycle [42 :foo "abc"])
                [int? keyword? string?])

(only-invalid (validate-scalars (cycle [42 :foo "abc"])
                                [int? boolean? string?]))

(valid-scalars? (range)
                [int?])

(valid-scalars? (range)
                [int? int? int?])

(valid-scalars? (range)
                [int? boolean? int?])

(valid-scalars? (iterate inc 1)
                [int?])

(valid-scalars? (iterate inc 1)
                [int? int? int?])

(valid-scalars? (iterate inc 1)
                [int? boolean? int?])

(valid-scalars? (lazy-seq [1 2 3])
                [int?])

(valid-scalars? (lazy-seq [1 2 3])
                [int? int? int?])

(valid-scalars? (lazy-seq [1 2 3])
                [int? boolean? int?])


;; Situation 2: Speculoos can use a specification that is composed of a
;; non-terminating sequence as long as the data is not also a non-terminating
;; sequence.

(valid-scalars? [42]
                (repeat int?))

(valid-scalars? [42 0 99]
                (repeat int?))

(valid-scalars? [42 :foo "abc"]
                (cycle [int? keyword? string?]))

(valid-scalars? [42 :foo "abc"]
                (cycle [int? boolean? string?]))



;; Nested collections: Speculoos can validate any scalar in a heterogeneous,
;; arbitrarily-nested data structure. The scalar and predicate share a path into
;; the data and specification, respectively. Another way to think about it is
;; that the specification mimics the data's structure, and the predicate and
;; scalar are located at the same position within each.

;; Examples of homogeneous, nested collections.

(valid-scalars? [42 [:foo] "abc"]
                [int? [keyword?] string?])

(valid-scalars? [42 [:foo ["abc"]]]
                [int? [keyword? [string?]]])

(valid-scalars? [[42] [:foo] ["abc"]]
                [[int?] [keyword?] [string?]])

(valid-scalars? (list 42 (list :foo (list "abc")))
                (list int? (list keyword? (list string?))))

(valid-scalars? {:a 42, :b {:c 'foo, :d "abc"}}
                {:a int?, :b {:c symbol?, :d string?}})

(valid-scalars? {:a 42, :b {:c {:d 'foo, :e "abc"}}}
                {:a int?, :b {:c {:d symbol?, :e string?}}})


;; Examples of heterogeneous, nested collections.

(validate-scalars [42 {:a 'foo, :b "abc"}]
                  [int? {:a symbol?, :b string?}])

(validate-scalars [42 {:a ['foo {:b (list "abc")}]}]
                  [int? {:a [symbol? {:b (list string?)}]}])

(validate-scalars [[[[42]]] [[[:foo]]] [[["abc"]]]]
                  [[[[int?]]] [[[keyword?]]] [[[string?]]]])


;; Scalars contained in a nested set can be validated.

(valid-scalars? [#{42 0 99} #{'foo} #{:foo :bar :baz}]
                [#{int?} #{symbol?} #{keyword?}])


;; Adjusting specifications: What if the specification in your hand is close,
;; but not exactly, what you need? You can relax a specification with a couple
;; of strategies.

;; First strategy: Speculoos will only validate if it has a predicate, so you
;; could simply remove the offending predicate.

(valid-scalars? [42 :foo "abc"]
                [int? keyword? boolean?])

(valid-scalars? [42 :foo "abc"]
                (pop [int? keyword? boolean?]))

(valid-scalars? {:a 42, :b 'foo}
                {:a int?, :b boolean?})

(valid-scalars? {:a 42, :b 'foo}
                (dissoc {:a int?, :b boolean?} :b))


;; Second strategy: Replace the offending predicate with a more permissive
;; predicate.

(valid-scalars? [42 :foo "abc"]
                [int? keyword? boolean?])

(valid-scalars? [42 :foo "abc"]
                [int? keyword? any?])

(valid-scalars? {:a 42, :b 'foo}
                {:a int?, :b boolean?})

(valid-scalars? {:a 42, :b 'foo}
                (assoc {:a int?, :b boolean?} :b any?))


;; If you need to dive into a nested structure to adjust the specification,
;; `github.com/blosavio/fn-in` has a few functions that'll help.
;; `speculoos.utility` also provides a few utilities specifically for adjusting
;; specifications.


;; Repairing data: What if you have a specification, and some data that almost
;; satisfies it? You could adjust the data so that it satisfies the
;; specification. The crude way would be to remove the offending datum.

(valid-scalars? {:a 42, :b 'foo}
                {:a int?, :b string?})

(valid-scalars? (dissoc {:a 42, :b 'foo} :b)
                {:a int?, :b string?})

;; Another option involves associating a new value to satisfy the predicate.

(valid-scalars? (assoc {:a 42, :b 'foo} :b "foo")
                {:a int?, :b string?})

;; Optionality: Speculoos provides a handful of ways to indicate optionality,
;; depending on the situation. Speculoos does not infer nor assume; it only
;; validates pairs of datums and predicates. Supplying predicates to a
;; specification says _These datums may or may not exist. If the datums exist,
;; they must satisfy these predicates. If the datums do not exist, the
;; predicates are ignored_.

(valid-scalars? [42 :foo]
                [int? keyword? string? boolean? ratio? char?])

(valid-scalars? [42 :foo "abc" true]
                [int? keyword? string? boolean? ratio? char?])

(valid-scalars? {:a 42, :b 'foo}
                {:a int?,
                 :b symbol?,
                 :c string?,
                 :d boolean?,
                 :e ratio?,
                 :f char?})

(valid-scalars? {:a 42, :b 'foo, :d true, :f \c}
                {:a int?,
                 :b symbol?,
                 :c string?,
                 :d boolean?,
                 :e ratio?,
                 :f char?})


;; Within a sequence, if you'd like to validate some of the values, but skip
;; over others, you have a few options to maintain the correspondence between
;; the data and specification: `any?`, `(constantly true)`, or `(fn [_] true)`.

(valid-scalars? [42 :foo "abc"]
                [int? any? string?])

(valid-scalars? [42 :foo "abc"]
                [int? (constantly true) string?])

(valid-scalars? [42 :foo "abc"]
                [int? (fn [_] true) string?])


;; The results are the same. Pick the option that best signals your intent.


;; Maps are easier: Any key in both the specification and the data will be
;; validated. Otherwise, unpaired datums or predicates are ignored.

(valid-scalars? {:validated 42, :ignored-datum 'foo}
                {:validated int?, :ignored-predicate char?})


;; Considering a single datum, you can bestow optionality by composing a
;; predicate with or, or, my preference, clojure.core/some-fn.

(valid-scalars? [42 :foo "abc"]
                [int? #(or (symbol? %) (keyword? %)) (some-fn char? string?)])




;;; Collection validation

;; Collection validation applies predicates to Clojure data structures like
;; vectors, lists, maps, sets, lazy sequences, etc.

;; Empty specifications: Speculoos will consume an empty specification, and
;; return.

(require '[speculoos.core :refer [validate-collections valid-collections?]])

(validate-collections []
                      [])

(validate-collections '()
                      '())

(validate-collections {}
                      {})

(validate-collections #{}
                      #{})


;; There is nothing particularly special about validating an empty collection.
;; In fact, that's a pretty useful task.

(valid-collections? []
                    [empty?])

(valid-collections? '()
                    '(empty?))

(valid-collections? {}
                    {:s empty?})

(valid-collections? #{}
                    #{empty?})


;; Vectors: Collection predicates apply to the vectors that contain them.

(valid-collections? [99 []]
                    [(complement empty?) [empty?]])

(valid-collections? [1 2 3]
                    [#(= 3 (count %))])


(defn length-1? [v] (= 1 (count v)))

(valid-collections? [[1] [2] [3]]
                    [[length-1?] [length-1?] [length-1?]])


;; Extending that last example: intervening collection predicates all apply to
;; the root, parent vector.

(valid-collections? [[1] [2] [3]]
                    [vector? [length-1?] (complement empty?) [length-1?] any? [length-1?]])

(valid-collections? [[1] [2] [3]]
                    [[length-1?] vector? (complement empty?) [length-1?] any? [length-1?]])

(valid-collections? [[1] [2] [3]]
                    [[length-1?] [length-1?] any? (complement empty?) vector?  [length-1?]])


;; As seen above, any number of predicates apply to their parent collection, and
;; in any order.

;; Lists: Speculoos validates lists exactly the same as vectors, so everything
;; above holds for lists. A few quick examples.

(valid-collections? '()
                    '(list?))

(valid-collections? '(1 2 3)
                    '(#(= 3 (count %))))

(valid-collections? '((1) (2) (3))
                    '((length-1?) (length-1?) (length-1?)))

(valid-collections? '((1) (2) (3))
                    '((length-1?) list? (length-1?) (length-1?)))


;; Maps: Collection predicates apply to the maps that contain them. I strongly
;; recommend that the collection predicate be associated to a key does not
;; appear in the data's corresponding map. Any number of predicates may be
;; supplied. Since the specification's keys don't matter, you can name them
;; something informative.

(valid-collections? {:a 42}
                    {:map-spec empty?})

(valid-collections? {:a 42}
                    {:coll-test map?})

(valid-collections? {:a 42}
                    {:coll-test map? :coll-size #(= 1 (count %))})

(valid-collections? {:a 42}
                    {:has-key? #(contains? % :a)})

(valid-collections? {:a 42, :b 'foo, :c 99}
                    {:all-vals-ints? #(every? int? (vals %))})


;; Sets: Predicates within a set apply to the set itself as a whole.

(valid-collections? #{1 2 3}
                    #{#(every? int? %)})

(valid-collections? #{1 2 :foo}
                    #{#(every? int? %)})

(valid-collections? #{1 2 3}
                    #{#(every? int? %) #(every? pos? %)})

(valid-collections? #{1 2 3}
                    #{#(every? int? %) #(every? even? %)})


;; All elements not something.

(valid-collections? #{1 2 3}
                    #{#(every? (complement keyword?) %)})


;; For every vector contained in a set, does it contain one integer?

(valid-collections? #{:a [1] :b [42] :c [99]}
                    #{(fn [s] (every? #(= 1 (count %)) (filter vector? s)))})

(valid-collections? #{:a [1 2 3] :b}
                    #{(fn [s] (every? #(= 1 (count %)) (filter vector? s)))})


;; Non-terminating Sequences: You may supply a (possibly) non-terminating
;; sequence in the data, or in the specification, but not both. During
;; collection validation, the non-terminating sequence is clamped to a length of
;; the other corresponding sequence.

(set! *print-length* 99)

(valid-collections? (range 99)
                    [#(every? int? %)])

(valid-collections? (range 99)
                    [#(every? ratio? %)])

(valid-collections? (cycle [42 :foo "abc"])
                    [#(valid-scalars? % [int? keyword? string?])])

(valid-collections? (iterate #(+ 3 %) 1)
                    [(fn [s] (every? true? (map #(= (+ 3 %1) %2) s (next s)))) (constantly true) (constantly true)])

(valid-collections? (repeat :na)
                    [(fn [s] (every? #(= :na %) s)) (constantly true) (constantly true)])


;; Nested collections: Same rule, recursively: predicates apply to their parent collection.

(valid-collections? [[1] [:a] ['foo]]
                    [[#(every? int? %)]
                     [#(every? keyword? %)]
                     [#(every? symbol? %)]])

(valid-collections? [[1 22/7] [:a] ['foo]]
                    [[#(every? int? %)]
                     [#(every? keyword? %)]
                     [#(every? symbol? %)]])

(valid-collections? [11 [22 33 44]]
                    [vector? ;; applies to root vector
                     [#(= 3 (count %))] ;; applies to nested vector
                     #(= 2 (count %))]) ;; applies to root vector

(valid-collections? {:a 11, :b {:c 22}}
                    {:has-keys? #(and (contains? % :a) (contains? % :b)) ;; applies to root map
                     :b {:is-map? map?, :one-MapEntry? #(= 1 (count %))}}) ;; applies to nested map at :b

(valid-collections? (list 11 (list 22 33))
                    (list list? (list #(<= 2 (count %))) #(= 2 (count %))))


;; Heterogeneous, nested collections work the same way.

(valid-collections? [{:a 1, :b 2} 33 {:d 4, :e 5} 66]
                    [#(= 2 (count (filter map? %))) {:has? #(contains? % :b)}
                     {:has? #(contains? % :d)}
                     (fn [v] (every? int? (filter #((complement map?) %) v)))])

(valid-collections? {:a [1 2 3], :b (list \z \q \w), :c {:x "abc", :y 42}}
                    {:expected-count? #(= 3 (count %)),
                     :a {:type vector?, :length #(<= 2 (count %))},
                     :c {:contains? #(contains? % :x)},
                     :b {:kind list?,
                         :all-same? (fn [s] (apply = (map type s)))}})

(valid-collections? [[[[]]]]
                    [[[[empty?]]]])

(valid-collections? {:a {:b {:c {:d {}}}}}
                    {:a {:b {:c {:d {:contains-anything
                                     empty?}}}}})


;; Sequence regexes: Replicating spec.alpha's regular expression facilities.

;; One or more integers, followed by zero or more strings.

(defn int+string*
  [s]
  (let [[front rear] (split-with int? s)
        ints #(<= 1 (count front))
        strs (every? string? rear)]
    (and ints strs)))

(int+string* [1 2 3 "a" "b"])
(int+string* [1 2 3])
(int+string* [1 2 3 'a 'b])

(valid-collections? [1 2 3 "a" "b"]
                    [int+string*])

(valid-collections? [1 2 3 'a 'b]
                    [int+string*])


;; Exactly three keywords, followed by anything.

(defn three-kw-and?
  [s]
  (= 3 (count (take-while keyword? s))))

(valid-collections? [:a :b :c]
                    [three-kw-and?])

(valid-collections? [:a :b]
                    [three-kw-and?])

(valid-collections? [:a :b :c 1 2 3]
                    [three-kw-and?])


;; `seq-regex`, a little toy utility that streamlines this task.

;; One or more integers, followed by zero or more strings.

(require '[speculoos.utility :refer [seq-regex]])

(valid-collections? [1 2 3 "a" "b" "c"]
                    [#(seq-regex % int? :+ string? :*)])


;; Exactly three keywords, followed by anything.

(valid-collections? [:a :b :c 'foo "bar" \z]
                    [#(seq-regex % keyword? 3 any? :*)])

;; See also
;; https://github.com/miner/herbert
;; and
;; https://github.com/cgrand/seqexp


;;; Adjusting specifications

;; Speculoos specifications are just Clojure data structures, containing Clojure
;; predicates. Adjust specifications with your favorite tools. `clojure.core`
;; provides some nice ones.

;; [33] fails to satisfy
(valid-collections? [[11] [22] [33]]
                    [[#(every? int? %)]
                     [#(= 1 (count %))]
                     [#(not-empty %)]])

;; removing predicate keeps [33] from being validated
(valid-collections? [[11] [22] [33]]
                    (drop-last [[#(every? int? %)]
                                [#(= 1 (count %))]
                                [#(not-empty %)]]))


;; `:b`s vector contains all symbols, so it fails to satisfy collection predicate
(valid-collections? {:a [1 2 3], :b ['foo 'bar 'baz]}
                    {:a [#(every? int? %)] :b [#(every? int? %)]})

;; altering collection predicate to check to symbols instead
(valid-collections? {:a [1 2 3], :b ['foo 'bar 'baz]}
                    (assoc-in {:a [#(every? int? %)],
                               :b [#(every? int? %)]}
                              [:b 0]
                              #(every? symbol? %)))


;; `github.com/blosavio/fn-in` provides some specialized functions for
;; manipulating heterogeneous, arbitrarily-nested data structures.

;; Changing a predicate with assoc-in*.

(require '[fn-in.core :refer [get-in* assoc-in* update-in* dissoc-in*]])

;; thing at `:c` is not a map
(valid-collections? [99 {:a 1, :b (list 42 22/7 ["abc" {:c [\z \q]}])}]
                    [{:b (list [{:c [map?]}])}])

;; adjust specification to chech that the thing at `:c` is a vector, which satisfies
(valid-collections? [99 {:a 1, :b (list 42 22/7 ["abc" {:c [\z \q]}])}]
                    (assoc-in* [{:b (list [{:c [map?]}])}]
                               [0 :b 0 0 :c 0]
                               vector?))


;; Removing a predicate with dissoc-in*.

;; thing at `:b` is not a map
(valid-collections? {:a [99 (list {:b [42]})]}
                    {:coll-test map? :a [(list {:b [map?]})]})

;; dissociate `map?` collection predicate, so that [42] is not validated
(valid-collections? {:a [99 (list {:b [42]})]}
                    (dissoc-in* {:coll-test map? :a [(list {:b [map?]})]}
                                [:a 0 0 :b 0]))


;; Repairing data: Adjust or remove datums such that the data collection
;; satisfies your specification.

;; Using clojure.core functions.

;; final nested vector is not a map
(valid-collections? [[1] [2 3] [4 5 6]]
                    [[vector?] [vector?] [map?]])

;; adjusting data so that `map?` is satisfied
(valid-collections? (update-in [[1] [2 3] [4 5 6]] [2] #(zipmap [:a :b :c] %))
                    [[vector?] [vector?] [map?]])


;; thing at `:b` has more than zero entries
(valid-collections? {:a 42, :b {:c 99}}
                    {:b {:has-anything? #(= 0 (count %))}})

;; make data at `:b` empty
(valid-collections? (assoc-in {:a 42, :b {:c 99}} [:b] {})
                    {:b {:has-anything? #(= 0 (count %))}})


;; Same examples as above, but using fn-in* functions.

(valid-collections? [11 [22 33 [44 55 66 [77]]]]
                    [[[[list?]]]])

(valid-collections? (update-in* [11 [22 33 [44 55 66 [77]]]]
                                [1 2 3]
                                #(into '() %))
                    [[[[list?]]]])

(valid-collections? {:a 42, :b {:c {:d [99 0 'foo]}}}
                    {:b {:c {:d [empty?]}}})

(valid-collections? (dissoc-in* {:a 42, :b {:c {:d [99]}}}
                                [:b :c :d 0])
                    {:b {:c {:d [empty?]}}})


;;;Comprehensive validation

;; Perform independent scalar and collection validations with a single function
;; call. `valid?` returns `true/false`.

(require '[speculoos.core :refer [valid? validate]])

(def no-strings? #(every? (complement string?) %))

(valid? [42 :foo \z]
        [int? keyword? char?]
        [no-strings?])

(valid? {:a 42, :b ['foo "abc"]}
        {:a int?, :b [symbol? string?]}
        {:has? #(contains? % :a), :b [#(<= 2 (count %))]})


;; `validate` returns a detailed validation report.

(validate [42 :foo \z]
          [int? keyword? char?]
          [no-strings?])


;;Highlight invalid results with `only-invalid`.

(def map-predicate {:a int?, :b string?})

(def no-chars? #(every? (complement char?) (vals %)))

(only-invalid (validate [{:a 42, :b "abc"} {:a 99, :b \z}]
                        [map-predicate map-predicate]
                        [vector? {} {:any-chars? no-chars?}]))


;; Require that every scalar and every collection have at least one predicate,
;; and all those predicates are satisfied.

(require '[speculoos.utility :refer [thoroughly-valid?]])

;; all scalars and collections have a predicate, all predicates satisfied

(thoroughly-valid? [42 {:x 'foo, :y 22/7}]
                   [int? {:x symbol?, :y ratio?}]
                   [vector? {:what-am-i? map?}])

;; all scalars and collections have a predicate, ratio predicate not satisfied
(thoroughly-valid? [42 {:x 'foo, :y 99}]
                   [int? {:x symbol?, :y ratio?}]
                   [vector? {:what-am-i? map?}])

;; all predicates satisfied, but nested map lacks a predicate
(thoroughly-valid? [42 {:x 'foo, :y 22/7}]
                   [int? {:x symbol?, :y ratio?}]
                   [vector? {}])



;;; Function validation: Check supplied arguments and/or the function's return value.


;; Validating functions with external metadata: _ad hoc_ function specification
;; by wrapping function with metadata.

(require '[speculoos.function-specs :refer [validate-fn-with
                                            validate-fn inject-specs!
                                            validate-higher-order-fn exercise-fn
                                            instrument unstrument]])

(validate-fn-with +
                  {:speculoos/arg-scalar-spec [int? int?]}
                  1
                  2)

(validate-fn-with +
                  {:speculoos/arg-scalar-spec [int? int?]}
                  1
                  9.87)


;; Injecting metadata: Altering the function var's metadata to provide the specification.

(inject-specs! + {:speculoos/arg-scalar-spec [int? int?]})

(validate-fn + 1 2)

(validate-fn + 1 9.87)


;; Metadata specifications are consulted only when Speculoos explicitly validates.

;; second arg would not satisfy metadata's argument specification, but plain
;; function invocation does not involve validation, so `+` returns as normal.
(+ 1 9.87)


;; Argument sequence validated as a collection.

(def all-numbers? #(every? number? %))

(validate-fn-with +
                  {:speculoos/arg-collection-spec [all-numbers?]}
                  1
                  9.87
                  22/7)

(validate-fn-with +
                  {:speculoos/arg-collection-spec [all-numbers?]}
                  1
                  9.87
                  "abc")


;; Performing both scalar and collection argument validation.

(def length-at-most-2? #(>= 2 (count %)))

(validate-fn-with +
                  {:speculoos/arg-scalar-spec [int? int?]
                   :speculoos/arg-collection-spec [length-at-most-2?]}
                  1
                  2)

(validate-fn-with +
                  {:speculoos/arg-scalar-spec [int? int? int?]
                   :speculoos/arg-collection-spec [length-at-most-2?]}
                  1
                  2
                  3.45)


;; Validating functions with internal metadata

;; Supply specifications during function definition.

(defn add-two-things
  {:speculoos/arg-scalar-spec [float? ratio?]}
  [x y]
  (+ x y))

(add-two-things 1.23 22/7)

(add-two-things 1 2)

(validate-fn add-two-things 1.23 22/7)

(validate-fn add-two-things 1 2)


;; Validating higher-order functions

;; Supply enough arguments so that the hof returns a non-function.

(defn example-hof
  [a b]
  (fn [c d] (fn [e f] (+ (* a c e) (* b d f)))))

(((example-hof 20 2) 30 3) 40 4)

(inject-specs! example-hof
               {:speculoos/arg-scalar-spec [int? int?]
                :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? int?]
                                      :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? int?]}}})

(validate-higher-order-fn example-hof [20 2] [30 3] [40 4])

(validate-higher-order-fn example-hof
                          [20 2.0]
                          [30 3.0]
                          [40 4.0])


;; Instrumenting functions: Arguments and returns implicitly validated on every
;; function invocation.

(defn multiplier
  {:speculoos/arg-scalar-spec [int? pos-int? ratio?],
   :speculoos/arg-return-spec [number? string?]}
  [x y z]
  [(* x y z) (str y)])

(multiplier 2 3 8/2)
(multiplier 2 3 4)

(instrument multiplier)

(multiplier 2 3 8/2)
(multiplier 2 3 4)

(with-out-str (multiplier 2 3 4))

(unstrument multiplier) ;; => {}

(multiplier 2 3 8/2) ;; => [24 "3"]
(multiplier 2 3 4) ;; => [24 "3"]


;; Exercising functions: Invoke function repeatedly with sample data generated
;; from argument scalar specification.

(defn shoe
  {:speculoos/arg-scalar-spec [pos-int? pos-int? string?]}
  [x y z]
  (str x ", " y ", buckle my " z))

(shoe 3 4 "sandal")

(exercise-fn shoe 3)


;; Validating macros: Speculoos validates macros by applying specifications
;; against the macro expansion.

(defmacro infix
  [form]
  (list (second form) (first form) (last form)))

(infix (2 + 3))

(macroexpand-1 '(infix (2 + 3)))

(def infix-spec (list symbol? int? int?))

(require '[speculoos.core :refer [valid-macro?]])

(valid-macro? '(infix (2 + 3)) infix-spec)
(valid-macro? '(infix (2.2 + 3.3)) infix-spec)



;; Clamping non-terminating sequences

(require '[speculoos.utility :refer [clamp-in*]])

(clamp-in* [11 22 33 [44 55 [66 (repeat 42)]]]
           [3 2 1]
           3)

(clamp-in* {:a [11 22 33 {:b (cycle ['foo "bar" \z])}]}
           [:a 3 :b]
           5)


;; All paths: Exhaustive sequence of paths of all values in a heterogeneous,
;; arbitrarily-nested data structure.

(speculoos.core/all-paths [11 22 {:a 'foo, :b \z}])


;;; Utility functions: Functions to get things done with Speculoos.

(require '[speculoos.utility :refer [data-from-spec
                                     spec-from-data
                                     basic-collection-spec-from-data
                                     all-specs-okay
                                     scalars-without-predicates
                                     collections-without-predicates
                                     exercise
                                     in?
                                     defpred
                                     unfindable-generators
                                     validate-predicate->generator]])

;; Data from scalar specification
(data-from-spec [int? {:a string?, :b ratio?}])


;;Scalar specification from data
(spec-from-data {:a 42, :b [\z 'foo "xyz"], :c 2/3})

;; Collection specification from data
(basic-collection-spec-from-data ['ignored-val {:ignored-key 'another-ignored-val} (list)])

;; Checking specifications
(all-specs-okay [int? string? {:a char?}])
(all-specs-okay [int? :not-a-predicate {:a char?}])


;; Data without a corresponding scalar predicate
(scalars-without-predicates [11 {:a 22, :b "abc"}]
                            [int? {:b string?}])

;; Collections without a corresponding collection predicate
(collections-without-predicates [42 {:a (list 22/7)} ['foo]]
                                [vector? {:a (list list?), :is-map? map?}])


;; Filtering validation results
(only-invalid (validate-scalars [11 ["abc" [:foo]]]
                                [int? [string? [symbol?]]]))

;; Exercising specifications
(exercise {:a int?, :b [char? keyword?]} 3)


;;; Advanced random sample generators

;; Built-in basic generator, set generator, and string regex generator.
(data-from-spec [int? #{:water :coffee :tea :milk}  #"fo{3,5}!"] :random)

;; Custom generators attached to predicate's metadata.

(def manual-predicate (with-meta #(int? %)
                        {:speculoos/predicate->generator #(rand-int 99)}))

(defpred easier-predicate #(int? %) #(rand-int 99))

(data-from-spec [manual-predicate easier-predicate] :random)


;; Automatic random sample generators. `or` generators emit multiple types.

(defpred or-predicate
         #(or (int? %) (string? %) (keyword? %)))

(data-from-spec (repeat 5 or-predicate) :random)


;; `and` refines a basic generator.

(defpred and-predicate
         (fn [i] (and (int? i) (odd? i) (<= 3 i))))

(data-from-spec (repeat 5 and-predicate) :random)


;; Combining `and`/`or`.

(defpred and-or-predicate
         #(or (and (int? %) (pos? %))
              (and (string? %) (<= 3 (count %)))
              (and (ratio? %) (<= 1/9 %))))

(data-from-spec (repeat 4 and-or-predicate) :random)


;; Identifying predicates without generators.
(unfindable-generators [manual-predicate (fn no-gen [] (rand-int 99)) easier-predicate])


;; Checking a generator against its host predicate.
(defpred pred-with-broken-gen #(neg? %) #(rand-int 99))

(validate-predicate->generator pred-with-broken-gen 5)


;; Item in collection? Alternative to clojure.core/contains?.

;; probably not what you expected
(contains? [:a :b :c] 1)

;; use `speculoos.utility/in?` instead
(in? [:a :b :c] 1)
(in? [:a :b :c] :b)
(in? (list 'foo 'bar) 'bar)


;;; Predicates

;; Speculoos predicates are regular Clojure functions and compose the same way.

(valid-scalars? [42 "abc"]
                [#(and (int? %) (even? %))
                 #(and (string? %) (= 3 (count %)))])

(valid-scalars? {:a 'foo}
                {:a #(or (char? %) (symbol? %))})


;; `clojure.core` provides some nice higher-order functions for composing
;; predicates: `every-pred` and `some-fn`.

(valid-scalars? [42 "abc"]
                [(every-pred int? even?)
                 (some-fn symbol? string?)])


;; Placeholders, ambivalence

;; Maintain a specification's correspondence to a sequence.

(valid-scalars? [42 "abc" :foo]
                [int? symbol? keyword?])

(valid-scalars? [42 "abc" :foo]
                [int? any? keyword?])

(valid-scalars? [42 "abc" :foo]
                [int? (constantly true) keyword?])

(valid-scalars? [42 "abc" :foo]
                [int? (fn [_] true) keyword?])


;; Regular expression predicates: nil returned from a regular expression failure
;; can indicate an invalid string.

(def example-regex #"\w{3}\d{3}")

(re-matches example-regex "abc123")
(re-matches example-regex "qr89")

(def regex-predicate #(re-matches example-regex %))

(valid-scalars? ["abc123" "xyz789"]
                [regex-predicate regex-predicate])

(only-invalid (validate-scalars ["abc123" "qr89"]
                                [regex-predicate regex-predicate]))


;; Speculoos treats free-floating regexes as predicates.

(validate-scalars ["abc123" "qr89"]
                  [#"\w{3}\d{3}" #"[qrs]{2}[789]{2}"])


;;; Sequence regexes

;; Mimicking spec.alpha's sequence regexes, use `speculoos.utility/seq-regex`
;; during a collection validation.

(def three-ints-two-kw-and-syms? #(seq-regex % int? 3 keyword? 2 symbol? :*))

(valid-collections? [1 2 3 :a :b 'foo 'bar 'baz 'qux]
                    [three-ints-two-kw-and-syms?])

(def sym-str-opt-int? #(seq-regex % symbol? :. string? :. int? :?))

(valid-collections? (list 'foo "abc" 42)
                    (list sym-str-opt-int?))


;; Sets as predicates: Within a scalar specification, a set creates a predicate
;; that tests for membership.

(valid-scalars? [:orange]
                [#{:red :orange :yellow}])

(valid-scalars? [:pantheon]
                [#{:red :orange :yellow}])


;; Testing functions as datums: When the data contains a function, predicates
;; can perform property testing to validate the datum.

(defn one-something-two-equals-three? [f] (= 3 (f 1 2)))

(one-something-two-equals-three? +)

(validate-scalars [+ -]
                  [one-something-two-equals-three? one-something-two-equals-three?])


;; Copyright © 2024 Brad Losavio.
;; {:UUIDv4 #uuid "256d193f-cd45-4a67-bf67-abbe6eb1776c"}