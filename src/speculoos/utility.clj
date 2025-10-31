(ns speculoos.utility
  "Utility functions to make using and interacting with Speculoos nicer.

  1. **Constructive** functions create conforming data or specifications, e.g.,
  synthesizing valid data when given a specification.

      * [[data-from-spec]]
      * [[spec-from-data]]
      * [[exercise]]
      * [[basic-collection-spec-from-data]]

  2. \"**Destructive**\" functions return new versions of data and/or
  specification with non-conforming datums or un-satisfied predicates altered or
  removed so that validation fully succeeds, e.g., replacing all invalid pairs
  with `nil`/`nil?`.

      * [[apathetic]]
      * [[swap-non-predicates]]

      All data structures remain immutable. These functions are only
      \"destructive\" in the sense that if you forward the new version of data
      and/or specification, the recipient will have lost some information.

  3. **Reporting** functions highlight some aspect of a data set or
  specification, e.g., returning non-predicates within a specification.

      * [[thoroughly-valid-scalars?]]
      * [[thoroughly-valid-collections?]]
      * [[thoroughly-valid?]]
      * [[all-specs-okay]]
      * [[scalars-without-predicates]]
      * [[scalars-with-predicates]]
      * [[collections-without-predicates]]
      * [[non-predicates]]
      * [[sore-thumb]]
      * [[predicates-without-scalars]]
      * [[unfindable-generators]]
      * [[validate-predicate->generator]]

  4. **Helper** functions decrease keypresses, e.g., _get fourth element of a
  sequence_.

      * [[=1st]] through [[=12th]]
      * [[clamp-in*]] and [[clamp-every]]
      * [[defpred]]
      * [[in?]]
      * [[lazy-seq?]]
      * [[seq-regex]]"
  (:require
   [clojure.test :as test]
   [clojure.set :as set]
   [clojure.pprint :as pprint]
   [clojure.test.check.generators :as gen]
   [fn-in.core :refer [get* get-in* assoc-in* dissoc-in* concat-list]]
   [re-rand :refer [re-rand]]
   [speculoos.core :refer [all-paths only-non-collections only-valid regex?
                           only-invalid validate-scalars valid-scalars?
                           validate-collections valid-collections? valid?
                           expand-and-clamp recover-literal-path]]))


(load "collection_hierarchy")


(defn only-values
  "Returns a vector of non-collections items in form, a heterogeneous,
  arbitrarily-nested data structure."
  {:UUIDv4 #uuid "2c71389a-d024-4f24-92d3-7af10d342587"
   :no-doc true}
  [form]
  (map #(:value %) (all-paths form)))


(defn only-paths
  "Returns a vector of paths to all nodes in form, a heterogeneous,
  arbitrarily-nested data structure. Paths are suitable for consumption by
  (get-in), (update-in), (assoc-in), and the like."
  {:UUIDv4 #uuid "4eeb0e11-a74b-40f5-a166-e09577e06f09"
   :no-doc true}
  [form]
  (map #(:path %) (all-paths form)))


(defn set-op-sub-fn
  "Generic version of (paths-only-in-A-not-in-B) and (paths-in-both-A-and-B)."
  {:UUIDv4 #uuid "79146319-8a47-4e9b-9b88-da81f04f343c"
   :no-doc true}
  [A B set-op]
  (set-op (set (only-paths A))
          (set (only-paths B))))


(defn paths-only-in-A-not-in-B
  "Returns a set of paths that are in form A, but not in form B, both
  heterogeneous, arbitrarily-nested data structurs."
  {:UUIDv4 #uuid "edb1d4d4-ca1c-4051-a5c2-d83db5e031e2"
   :no-doc true}
  [A B]
  (set-op-sub-fn A B set/difference))


(defn paths-in-both-A-and-B
  "Returns a set of paths that are only in both A's and B's all-paths."
  {:UUIDv4 #uuid "cf05ad11-0160-41e1-9731-4d56341e923a"
   :no-doc true}
  [A B]
  (set-op-sub-fn A B set/intersection))


(defn keep-only-elements-in-set
  "Keep only the {:path :value} elements whose value associated with keyword kw
  is a member of set st."
  {:UUIDv4 #uuid "ce8b7c12-2f09-487a-97ef-f1b067bb1272"
   :no-doc true}
  [form st kw]
  (filter #(st (kw %)) form))


(defn data-spec-set-analysis
  "Generate a map of information that describes entities contained within sets
  of a heterogeneous, arbitrarily-nested data structure `d`."
  {:UUIDv4 #uuid "24ea3ffe-a019-4c37-9bd7-b520a6e6927a"
   :no-doc true}
  [d]
  (let [all-paths-d (all-paths d)
        all-sets-in-d (filter #(set? (:value %)) all-paths-d)
        d-set-paths (set (map #(:path %) all-sets-in-d))
        entities-in-a-set (filter #(d-set-paths (drop-last (:path %))) (only-non-collections all-paths-d))]
    {:all-paths all-paths-d
     :all-sets all-sets-in-d
     :set-paths d-set-paths
     :entities-in-a-set entities-in-a-set}))


(defn data-scalars-in-a-set-*-a-predicate
  "Helper function for `data-scalars-in-a-set-with-a-predicate` and
  `data-scalars-in-a-set-without-a-predicate`.

  `f` should be `contains?` for _with_
  `f` should be `(complement contains?)` for _without_"
  {:UUIDv4 #uuid "20a1a956-4b24-42e1-b049-8974d40d8048"
   :no-doc true}
  [data spec f]
  (let [data-analysis (data-spec-set-analysis data)
        spec-analysis (data-spec-set-analysis spec)
        predicates-in-spec (filter #(fn? (:value %)) (spec-analysis :entities-in-a-set))
        paths-of-predicates-in-a-spec-set (set (map #(drop-last (:path %)) predicates-in-spec))
        entities-in-a-data-set (data-analysis :entities-in-a-set)]
    (filter #(f paths-of-predicates-in-a-spec-set (drop-last (:path %))) (data-analysis :entities-in-a-set))))


(defn data-scalars-in-a-set-with-a-predicate
  "Returns `data` all-paths scalar elements that are members of a set and which
  have a corresponding predicate in `spec`.

  See also [[data-scalars-in-a-set-without-a-predicate]]."
  {:UUIDv4 #uuid "cf209dcf-7686-4c51-8280-9dd7fc0899ff"
   :no-doc true}
  [data spec]
  (data-scalars-in-a-set-*-a-predicate data spec contains?))


(defn data-scalars-in-a-set-without-a-predicate
  "Returns `data` scalars that are members of a set but which do *not* have a
  corresponding predicate in `spec`.

  See also [[data-scalars-in-a-set-with-a-predicate]]."
  {:UUIDv4 #uuid "953db3b5-0654-4128-89fc-efac545d8578"
   :no-doc true}
  [data spec]
  (data-scalars-in-a-set-*-a-predicate data spec (complement contains?)))


(defn scalars-without-predicates
  "Returns a set of `data` all-paths scalar elements which lack corresponding
  predicates in scalar specification `spec`. See also
  [[scalars-with-predicates]] and [[collections-without-predicates]].

  Examples:
  ```clojure
  (scalars-without-predicates [42 :foo 22/7] [int? keyword?]) ;; => #{{:path [2], :value 22/7}}
  (scalars-without-predicates {:a 42 :b 'foo} {:a int?}) ;; => #{{:path [:b], :value foo}}
  ```"
  {:UUIDv4 #uuid "cfb2d897-cf07-49d8-b0e2-b4da701cce6c"}
  [data spec]
  (let [scalars-without-specs-possibly-set-elements (keep-only-elements-in-set
                                                     (only-non-collections (all-paths data))
                                                     (paths-only-in-A-not-in-B data spec)
                                                     :path)
        scalars-in-a-set-with-a-pred (set (data-scalars-in-a-set-with-a-predicate data spec))]
    (into #{} (remove #(scalars-in-a-set-with-a-pred %)
                      scalars-without-specs-possibly-set-elements))))


(defn scalars-with-predicates
  "Returns `data` all-paths scalar elements which have a corresponding predicate
  in scalar specification `spec`. See also [[scalars-without-predicates]] and
  [[collections-without-predicates]].

  Examples:
  ```clojure
  (scalars-with-predicates [42 :foo 22/7] [int?]) ;; => ({:path [0], :value 42})
  (scalars-with-predicates {:a 42 :b 'foo} {:a int?}) ;; => ({:path [:a], :value 42})
  ```"
  {:UUIDv4 #uuid "669dbe43-82ab-47f4-bf91-6e74100c0cda"}
  [data spec]
  (let [scalars-with-pred-possibly-missing-set-elements (keep-only-elements-in-set
                                                         (only-non-collections (all-paths data))
                                                         (paths-in-both-A-and-B data spec)
                                                         :path)
        scalars-in-a-set-with-a-pred (data-scalars-in-a-set-with-a-predicate data spec)]
    (concat scalars-with-pred-possibly-missing-set-elements
            scalars-in-a-set-with-a-pred)))


(defn predicates-without-scalars
  "Returns only scalar specification `spec` elements which lack a corresponding
  element in `data`.

  Examples:
  ```clojure
  (predicates-without-scalars [42 :foo] [int? keyword? ratio?]) ;; => ({:path [2], :value ratio?})
  (predicates-without-scalars {:a 42} {:a int? :b symbol?}) ;; => ({:path [:b], :value symbol?})
  ```"
  {:UUIDv4 #uuid "7e36fcb9-183e-4b33-bd69-8267cd65aece"}
  [data spec]
  (let [preds-without-scalars-possibly-in-sets (keep-only-elements-in-set
                                                (only-non-collections (all-paths spec))
                                                (paths-only-in-A-not-in-B spec data)
                                                :path)
        predicates-in-set-with-scalars (set (map #(drop-last (:path %)) (data-scalars-in-a-set-with-a-predicate data spec)))]
    (remove #(contains? predicates-in-set-with-scalars (drop-last (:path %))) preds-without-scalars-possibly-in-sets)))


(defn non-predicates
  "Returns [[all-paths]] entries `{:path _ :value _}` for all elements in
  specification `spec` that are not functions.

  This function name is possibly mis-leading: It tests only `fn?`, not if the
  function is a competent predicate. Sets are flagged as 'non-predicates'.

  Examples:
  ```clojure
  (non-predicates [int? 42 decimal?]) ;; => ({:path [1], :value 42})

  (non-predicates {:a int? :b 'foo}) ;; => ({:path [:b], :value foo})
  ```

  Sets may serve as a membership predicate for a scalar specification, but are
  flagged as 'non-predicates'.

  Demonstration:
  ```clojure
  ;; `validate-scalars` considers #{:red} as a predicate-like thing, but ...
  ;; ... `non-predicates` is unable to distinguish without data to accompany spec
  (non-predicates [int? #{:red} ratio?]) ;; => ({:path [1 :red], :value :red})
  ```"
  {:UUIDv4 #uuid "7147f695-3852-4a1b-bd60-92854b000919"}
  [spec]
  (remove #(fn? (:value %)) (only-non-collections (all-paths spec))))


(defn all-specs-okay
  "Returns `true` if all entries in specification `spec` are a function.
  Otherwise, returns [[all-paths]] entries `{:path _ :element _}` to
  non-functions. See [[non-predicates]] for limitations.

  Examples:
  ```clojure
  (all-specs-okay [int? keyword? ratio?]) ;; => true
  (all-specs-okay [int? keyword? 9.87]) ;; => ({:path [2], :value 9.87})
  ```"
  {:future-feature "check for one-arity by inspecting :arglists"
   :UUIDv4 #uuid "24a4f419-3256-4f29-addc-7894c6fb1de7"}
  [spec]
  (let [non-fns (non-predicates spec)]
    (if (empty? non-fns)
      true
      non-fns)))


(defn adjust
  "Generalized process to adjust values of form, a heterogeneous,
  arbitrarily-nested collection.
   red-fun is a function supplied to (reduce).
   form is the initial state of (reduce)'s accumulator.
   xs is the collection fed into (reduce).
   See (swap-non-predicates), (nil-out), (apathetic), etc., for example usage."
  {:UUIDv4 #uuid "1dc47838-1f4f-4863-86b7-6d576c546b67"
   :no-doc true}
  [form red-fun xs]
  (reduce red-fun form xs))


(defn adjust-demonstration
  "Demonstration of of (adjust) utility that replicats the functionailty of
  (swap-non-predicates)."
  {:UUIDv4 #uuid "87e8c34b-2539-4ed4-a140-603e54433579"
   :no-doc true}
  [spec pred]
  (let [non-fn-paths (non-predicates spec)
        red-fun #(assoc-in* %1 (%2 :path) pred)]
    (adjust spec red-fun non-fn-paths)))


(defn swap-non-predicates
  "Returns a new scalar specification `spec` with all non-predicate elements
  swapped for [`any?`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/any?)
  (default) or optional supplied predicate `pred`.

  Examples:
  ```clojure
  ;; default predicate replacement
  (swap-non-predicates [int? 99 ratio?]) ;; => [int? any? ratio?]

  ;; non-default predicate replacement
  (swap-non-predicates {:a int? :b 99 :c 'foo} number?) ;; => {:a int?, :b number?, :c number?}
  ```"
  {:UUIDv4 #uuid "44af1761-3e01-4fa5-8106-5737706103d1"
   :destructive true}
  ([spec] (swap-non-predicates spec any?))
  ([spec pred] (let [non-fn-paths (non-predicates spec)]
                 (reduce #(assoc-in* %1 (%2 :path) pred)
                         spec
                         non-fn-paths))))


(defn apathetic
  "Return a scalar specification `spec` for which any specification predicate
  which returns invalid, is transmorgrified to `any?`.

  Examples:
  ```clojure
  (apathetic [42 :foo 22/7] [int? symbol? ratio?]) ;; => [int? any? ratio?]
  (apathetic {:a 42 :b 'foo} {:a int? :b keyword?}) ;; => {:a int?, :b any?}
  ```"
  {:UUIDv4 #uuid "efac7c1d-6c5c-4023-96bb-3d1ef31947b5"
   :destructive true
   :alternative-fname "relax-all-unsatisfied-predicates"}
  [data spec]  (reduce (fn [spc invalids] (assoc-in* spc (:path invalids) any?))
                       spec
                       (only-invalid (validate-scalars data spec))))


(defn sore-thumb
  "Prints `*out*` a version of `data` and `spec` that highlights where the
  datums+predicates invalidate. Non-invalids (i.e., datums that satisfy their own
  predicates) are de-emphasized with `replacement`, defaulting to `'_`.

  `'…` is also kinda nice.

  Examples:
  ```clojure
  ;; default replacement '_
  (with-out-str (sore-thumb [42 :foo 22/7] [int? symbol? ratio?]))
  ;; printed to *out*: data: [_ :foo _] spec: [_ symbol? _]

  ;; optional replacement '…
  (with-out-str (sore-thumb {:a 42 :b 'foo :c 22/7} {:a int? :b keyword? :c ratio?} '…))
  ;; printed to *out*: data: {:a …, :b foo, :c …} spec: {:a …, :b keyword?, :c …}
  ```"
  {:UUIDv4 #uuid "70288ca6-74b8-444e-83a4-b1e2f1d5fd27"
   :future-work "Direct the REPL to print the unqualified names of core functions so the predicates display cleaner."}
  ([data spec] (sore-thumb data spec '_))
  ([data spec replacement]
   (let [valids (only-valid (validate-scalars data
                                                  spec))
         replace-fn (fn [form] (reduce #(assoc-in* %1 (:path %2) replacement)
                                       form
                                       valids))]
     (do (pprint/pprint (str "data: " (replace-fn data)))
         (pprint/pprint (str "spec: " (replace-fn spec)))))))


;; Consider using (map-invert) to look up value and return the key for two-way associativity.

(def ^{:UUIDv4 #uuid "4dd280ce-ba05-4bd6-9846-132a047c714f"
       :no-doc true
       :doc "Quick and dirty linkages between atomic types, predicates, cononical
 examples, and `test.check` generated randoms.  Listed in the order they appear
 at [`clojure.test.check`](https://clojure.github.io/test.check/clojure.test.check.generators.html)
retrieved on 2024Mar12."}
  type-predicate-canonical
  {java.lang.Boolean {:predicate boolean? :canonical true :gen #(gen/generate gen/boolean)}
   java.lang.Byte {:predicate #(= java.lang.Byte (type %)) :canonical (byte 0x43) :gen #(gen/generate gen/byte)}
   java.lang.Character {:predicate char? :canonical \c :gen #(gen/generate gen/char-alphanumeric)}
   java.lang.Double {:predicate double? :canonical 1.0E32 :gen #(gen/generate gen/double)}
   clojure.lang.Keyword {:predicate keyword? :canonical :kw :gen #(gen/generate gen/keyword)}
   java.lang.Long {:predicate int? :canonical 42 :gen #(gen/generate gen/int 1000)}
   :nat-int {:predicate nat-int? :canonical 5 :gen #(gen/generate gen/nat)}
   :neg-int {:predicate neg-int? :canonical -10 :gen #(gen/generate (gen/fmap (comp dec -) gen/nat))}
   :pos {:predicate pos? :canonical 1 :gen #(gen/generate (gen/such-that pos? gen/nat))} ; occasionally emits zero, which fails (pos?)
   clojure.lang.Ratio {:predicate ratio? :canonical 22/7 :gen #(gen/generate (gen/such-that ratio? gen/ratio))} ; occasionally emits an integer, which fails (ratio?)
   java.lang.String {:predicate string? :canonical "abc" :gen #(gen/generate gen/string-alphanumeric)}
   clojure.lang.Symbol {:predicate symbol? :canonical 'speculoos/canonical-symbol :gen #(gen/generate gen/symbol-ns)}
   java.util.UUID {:predicate uuid? :canonical #uuid "57f6924b-71ef-4180-afb4-1b2d2fac8f57" :gen #(gen/generate gen/uuid)}

   ;; others not listed on test.check
   java.lang.Float {:predicate float? :canonical 1.23}
   java.math.BigDecimal {:predicate decimal? :canonical 1M}
   :zero {:predicate zero? :canonical 0}
   :neg {:predicate neg? :canonical -1 :gen #(gen/generate (gen/large-integer* {:min -1000 :max -1}))}
   :even {:predicate even? :canonical 2}
   :odd {:predicate odd? :canonical 3}
   :number {:predicate number? :canonical 1E6}
   :pos-int {:predicate pos-int? :canonical 10 :gen #(gen/generate (gen/fmap inc gen/nat))}
   :fn {:predicate fn? :canonical +}
   :function {:predicate test/function? :canonical reduce}
   :NaN {:predicate NaN? :canonical ##NaN :gen (constantly ##NaN)}
   :infinity {:predicate infinite? :canonical ##Inf :gen (constantly ##Inf)}
   :nil {:predicate nil? :canonical nil :gen (constantly nil)}
   :true {:predicate true? :canonical true :gen (constantly true)}
   :false {:predicate false? :canonical false :gen (constantly false)}
   })


(defn test-type-predicate-canonical
  "Test if each canonical example satisfies its predicate.
   Returns {:predicate :canonical :result} for each entry."
  {:UUIDv4 #uuid "890f5005-fd05-4105-b9ea-8c389dbac307"
   :no-doc true}
  [tpc]
  (map #(let [{pred :predicate can :canonical} (second %)]
          {:predicate pred
           :canonical can
           :result (pred can)})
       tpc))


(defn all-valid?-type-predicate-canonical
  "Returns true if all canonical examples satisfy their respective predicates."
  {:UUIDv4 #uuid "adb1508e-24e1-4073-9a0a-53e278f9cd6d"
   :no-doc true}
  [tpc]
  (every? true? (map #(:result %) (test-type-predicate-canonical tpc))))


(defn element->predicate
  "Given an atomic datum, return its predicate function appropriate for its
  type."
  {:UUIDv4 #uuid "7bad60e6-cc56-4d6e-bf4d-870472403f23"
   :no-doc true}
  [t]
  (cond
    (symbol? t) (:predicate (type-predicate-canonical clojure.lang.Symbol))
    (keyword? t) (:predicate (type-predicate-canonical clojure.lang.Keyword)) ;; Note: kw are IFn, so must handle them first
    (instance? clojure.lang.IFn t) (:predicate (type-predicate-canonical :fn))
    :else (:predicate (type-predicate-canonical (type t)))))


(def ^{:UUIDv4 #uuid "2cc354f8-94f2-4c0a-a9b4-935fadc30cb9"
       :no-doc true}
  predicate->canonical
  (reduce #(assoc %1 (:predicate (second %2)) (:canonical (second %2)))
          {}
          type-predicate-canonical))


(defn predicate->rand-generated
  "Returns a clojure.test.check.generator-ed value for a given predicate.
   Note: Where test.check does not provide specific generators (e.g., decimals,
  floats), defaults to the 'canonical' examples."
  {:UUIDv4 #uuid "8584aa40-0ae2-42a2-b5bc-d9d63cb13917"
   :no-doc true}
  [pred]
  (let [rand-generated-val
        (get (reduce #(assoc %1 (:predicate (second %2)) (:gen (second %2)))
                     {}
                     type-predicate-canonical) pred)]
    (if rand-generated-val
      (rand-generated-val)
      (predicate->canonical pred))))


(defn compare-vec-common-elements
  "Given two vectors v1 and v2 of possibly different lengths,
   return true if every value in both is equal."
  {:UUIDv4 #uuid "004e4c31-6627-4e43-9691-3e2655d8e6b9"
   :no-doc true}
  [v1 v2]
  (every? true? (map = v1 v2)))


(defn set-descendant?
  "Given an all-paths and one of its elements x, returns true if x is a
   descendant of a set."
  {:UUIDv4 #uuid "62742ae0-149f-4d9d-a03e-143970e310ad"
   :no-doc true}
  [paths x]
  (let [root (first (filter #(= [] (:path %)) paths))]
    (if (set? (:value root))
      true
      (let [sets (filterv #(set? (:value %)) paths)
            set-paths (reduce #(conj %1 (:path %2)) [] sets)
            element-vs-set-paths (map #(and (compare-vec-common-elements % (:path x))
                                            (> (count (:path x)) (count %))) set-paths)]
        (some true? element-vs-set-paths)))))


;;;; Creative functions


(declare clamp-every)


(defn spec-from-data
  "Given heterogeneous, arbitrarily-nested collection `data`, create a
  scalar specification whose predicates validate. Non-terminating sequences are
  clamped at `clamp-at`, defaults to 7.

  Examples:
  ```clojure
  (spec-from-data [42 [:foo] 22/7]) ;; => [int? [keyword?] ratio?]
  (spec-from-data {:a 42 :b {:c 'foo :d 22/7}}) ;; => {:a int?, :b {:c symbol?, :d ratio?}}

  ;; non-terminating data sequence, optional clamp-at supplied
  (spec-from-data [:foo (cycle [42 22/7 'baz])] 5) ;; => [keyword? [int? ratio? symbol? int? ratio?]]
  ```"
  {:UUIDv4 #uuid "6de1fdab-5bfa-47a1-94bc-5353fd543d51"}
  ([data] (spec-from-data data 7))
  ([data clamp-at]
   (let [clamped-data (clamp-every data clamp-at)]
     (reduce #(assoc-in* %1 (:path %2) (element->predicate (:value %2)))
             clamped-data
             (only-non-collections (all-paths clamped-data))))))


(defn data-from-spec
  "Given heterogeneous, arbitrarily-nested scalar specification `spec`, create a
  sample data structure whose values validate to that specification. Works
  for singular, clojure.core predicates, e.g., `int?`, `string?`, or for
  `#(and (int? %) (< 5 %))` with generators at metadata key
  `:speculoos/predicate->generator`. Unknown predicates yield `nil`. Defaults
  to canonical values, i.e., `'abc'` for `string?`.

  Optional trailing arg `:random` uses `clojure.test.check` generators when
  available. Trailing arg `:canonical` (default) uses lookup table to supply
  aesthetic values, i.e., `42` for `int?`.

  Sets are assumed to be a predicate. `:canonical` uses whatever
  `clojure.core/first` returns (consider a sorted set with the first element an
  exemplar); `:random` yields a random selection from the set.

  Bare regular expressions yield random strings that match.

  Examples:
  ```clojure
  ;; default canonical datums
  (data-from-spec [int? keyword? ratio?]) ;; => [42 :kw 22/7]
  (data-from-spec {:a symbol? :b decimal?}) ;; => {:a speculoos/canonical-symbol, :b 1M}

  ;; optional random values
  (data-from-spec [int? [keyword? [ratio?]]] :random) ;; => [-715 [:! [15/2]]]
  (data-from-spec {:a symbol? :b {:c boolean? :d pos-int?}} :random) ;; => {:a x2_qV/x, :b {:c true, :d 26}}

  ;; set as a predicate, defaults to (first #{...})
  (data-from-spec [int? #{:red :green :blue} ratio?]) ;; => [42 :green 22/7]

  ;; set as a predicate, optional random selection
  (data-from-spec [int? #{:red :green :blue} ratio?] :random) ;; => [-208 :red 7/2]

  ;; regular expression as a predicate
  (data-from-spec [#\"Q\\d{3}Y\\d{1,2}\" #\"foo[2468]\"])
  ;; => [\"Q690Y61\" \"foo6\"]
  ```
  If the metadata of a non-set, non-regex predicate contains either key
  `:speculoos/predicate->generator` or key `:speculoos/canonical-sample`, the
  associated value is presumed to be a competent generator or canonical sample,
  respectively, and will be preferred. See [[validate-predicate->generator]].

  Note: Presence of either metadata key implies that a default generator will
  not be found in the lookup table, and therefore requires a customized
  generator for invoking with the `:random` option. But it _also_ implies that
  a canonical value will also not be located, and therefore requires a custom
  canonical value, which is referenced when explicitly invoked with
  `:canonical`, _but also the default, no-option invocation_.

  ```clojure
  ;; as is, Speculoos cannot generate a sample datum from this predicate
  (def opaque-predicate (fn [x] (and (int? x) (pos? x) (< x 99))))

  ;; Speculoos can however retrieve a generator at metadata key :speculoos/predicate->generator
  (data-from-spec {:a string? :b (with-meta opaque-predicate {:speculoos/predicate->generator #(rand-int 100)})} :random)
  ;; => {:a \"Q56BPs2ownU08ExMOy3yVD37M6Q\", :b 15}

  ;; predicates not found in the lookup table must explicitly provide a canonical value when invoking without `opt`
  (data-from-spec [ratio? (with-meta int? {:speculoos/canonical-sample 987654321})])
  ;; => [22/7 987654321]
  ```
  See [[defpred]] for a helper to set up a predicate with a generator.

  ```clojure
  (defpred f4 int? #(rand-int 99)) ;; optional canonical value not supplied
  (defpred f5 number? #(rand 999) 1.234)

  (data-from-spec {:a f4 :b [f5]} :random)
  ;; => {:a 31, :b [147.28249222486664]}

  (data-from-spec {:a f4 :b [f5]} :canonical)
  ;; => {:a :f4-canonical-sample, :b [1.234]}
  ```"
  {:UUIDv4 #uuid "b602cf68-c830-453f-bc5d-d1a3ebd0882a"}
  ([spec] (data-from-spec spec :canonical))
  ([spec opt]
   (let [clamped-spec (clamp-every spec 5)
         all-paths-clamped (all-paths clamped-spec)
         rand-set #(rand-nth (vec %))
         contains-any? (fn [m] (some #(contains? m %) #{:speculoos/canonical-sample :speculoos/predicate->generator}))
         replace-predicate (fn [structure path-element]
                             (assoc-in* structure
                                        (:path path-element)
                                        (if (contains-any? (meta (:value path-element)))
                                          (case opt
                                            :canonical (:speculoos/canonical-sample (meta (:value path-element)))
                                            :random ((:speculoos/predicate->generator (meta (:value path-element)))))
                                          (case opt
                                            :canonical (predicate->canonical (:value path-element))
                                            :random (predicate->rand-generated (:value path-element))))))
         replace-set (fn [structure path-element]
                       (assoc-in* structure
                                  (:path path-element)
                                  (case opt
                                    :canonical (first (:value path-element))
                                    :random (rand-set (:value path-element)))))
         replace-regex (fn [structure path-element]
                         (assoc-in* structure
                                    (:path path-element)
                                    (re-rand (:value path-element))))
         new-set (fn [original-set] (case opt
                                      :canonical (first original-set)
                                      :random (rand-set  original-set)))]
     (if (set? spec)
       (new-set spec)
       (reduce #(cond
                  (and ((complement coll?) (:value %2))
                       ((complement set-descendant?) all-paths-clamped %2))
                  (if (regex? (:value %2))
                    (replace-regex %1 %2)
                    (replace-predicate %1 %2))

                  (and (set? (:value %2))
                       ((complement set-descendant?) all-paths-clamped %2))
                  (replace-set %1 %2)

                  :else %1)
               clamped-spec
               all-paths-clamped)))))


(defn validate-predicate->generator
  "Repeatedly invoke predicate function `f`, with sample arguments produced by
  generator located at key `:speculoos/predicate->generator` in the function
  metadata. `n` invocations (default `7`). Returns a sequence of vectors
  containing the generated value and the result of the validation.

  Useful for checking manually-injected generators consulted by
  [[data-from-spec]].

  Example:
  ```clojure
  ;; basic integer predicate and generator
  (validate-predicate->generator (with-meta #(int? %) {:speculoos/predicate->generator #(rand-int 99)}))
  ;; => ([64 true] [84 true] [21 true] [74 true] [88 true] [13 true] [33 true])

  ;; compound integer predicate with compound integer generator
  (require '[clojure.test.check.generators :as gen])

  ;; positive, even integers, less than one hundred; warm up generator by 25 calls and peeling off last
  (def gen-1 #(last (gen/sample (gen/such-that even? (gen/large-integer* {:min 0 :max 99})) 25)))

  (validate-predicate->generator (with-meta #(and (int? %) (pos? %) (even? %) (< % 100)) {:speculoos/predicate->generator gen-1}))
  ;; => ([72 true] [52 true] [2 true] [82 true] [64 true] [56 true] [10 true])

  ;; same thing, but intentionally wrong generator that produces odd integers
  (def incorrect-gen-1 #(last (gen/sample (gen/such-that odd? (gen/large-integer* {:min 0 :max 99})) 25)))

  (validate-predicate->generator (with-meta #(and (int? %) (pos? %) (even? %) (< % 100)) {:speculoos/predicate->generator incorrect-gen-1}))
  ;; => ([61 false] [57 false] [3 false] [97 false] [53 false] [77 false] [63 false])

  ;; validating regular expression generator: capital Z, followed by one to three digits, followed by a, b, or c
  (def re-spec #\"Z\\d{1,3}[abc]\")

  ;; re-rand generates random strings that satisfy a given regex [https://github.com/weavejester/re-rand]
  (require '[re-rand :refer [re-rand]])

  (validate-predicate->generator (with-meta #(boolean (re-matches re-spec %)) {:speculoos/predicate->generator #(re-rand re-spec)}))
  ;; ([\"Z919b\" true] [\"Z99b\" true] [\"Z5a\" true] [\"Z210a\" true] [\"Z711c\" true] [\"Z319a\" true] [\"Z81c\" true])
  ```"
  {:UUIDv4 #uuid "32f71117-a484-478d-9933-0a9d0e3935b2"}
  ([f] (validate-predicate->generator f 7))
  ([f n] (repeatedly n #(let [generator (:speculoos/predicate->generator (meta f))
                              value (generator)]
                          (vector value (f value))))))


(defn unfindable-generators
  "Given scalar specification `spec`, check that a random sample generator can
  be located for each predicate-like thing within `spec`. Returns a sequence of
  any thingy that does not, and a path to its location within `spec`.

  * Many basic predicates such as `int?`, `string?`, `ratio?`, etc., are
    provided by `clojure.test.check.generators`.
  * Sets and regular expressions are competent generators.
  * Compound predicates such as `#(and (number? %) (< % 99))` may be
    explicitly supplied with a custom generator located within its metadata
    map at key `:speculoos/predicate->generator`.

  Note: Being able to _locate_ a random sample generator does not imply that it
  works properly.

  ```clojure
  ;; all good generators
  (unfindable-generators [int? #{:red :green :blue} #\"foo\" (with-meta #(int? %) {:speculoos/predicate->generator #(rand-int 99)})])
  ;; => []

  ;; all bad generators
  (unfindable-generators {:no-meta-data #(int? %)
                          :no-generator +
                          :improper-key (with-meta #(int? %) {:speculoos/oops #(rand-int 99)})})
  ;; => [{:path [:no-meta-data], :value #fn--34610]}
  ;;     {:path [:no-generator], :value clojure.core/+}
  ;;     {:path [:improper-key], :value #function[clojure.lang.AFunction/1]]}]
  ```"
  {:UUIDv4 #uuid "3850a53d-7b50-4ba1-b84a-17a9816500ff"}
  [spec]
  (let [all (all-paths spec)
        non-colls (only-non-collections all)
        ;;set-descendants (map #(set-descendant? all %) non-colls)
        ;;_ (println "set-descendants:" set-descendants)
        ;;v (fn [{p :value}] p)
        ;;q (fn [{p :value}] (:speculoos/predicate->generator (meta p)))
        no-gen? (fn [{p :value}] (not (or (set? p)
                                          (regex? p)
                                          (predicate->rand-generated p)
                                          (= nil? p)
                                          (:speculoos/predicate->generator (meta p)))))]
    (reduce #(if (and (no-gen? %2) (not (set-descendant? all %2)))
               (conj %1 %2) %1) [] non-colls)))


(defn exercise
  "Generates a number `n` (default 10) of values compatible with scalar
  specification `spec` and maps [[valid-scalars?]] over them, returning a
  sequence of `[val valid?]` tuples.

  If `n` is `:canonical`, only one data set is produced, consisting of the
  predicates' canonical values.

  See [[data-from-spec]] for details on predicates.

  Examples, passing optional count `n` `3` for brevity:
  ```clojure
  (exercise [int? [keyword? ratio?] char?] 3)
  ;; => ([[-282 [:_1z9L -17/21] \\G] true]
  ;;     [[ 469 [:c*2y   -7/11] \\9 true]
  ;;     [[-293 [:*C     -3/7 ] \\i] true])

  (exercise {:a symbol? :b [pos-int? decimal? char?]} 3)
  ;; => ([{:a U8D_/gs+H,   :b [17 1M \\Z]} true]
  ;;     [{:a b-187+/Xv,   :b [27 1M \\3]} true]
  ;;     [{:a ?l4zv/Y.Ro!, :b [22 1M \\G]} true])
  ```

  Example with canonical values:
  ```clojure
  (exercise [int? char? ratio? string? double?] :canonical)
  ;; => ([[42 \\c 22/7 \"abc\" 1.0E32] true])
  ```"
  {:UUIDv4 #uuid "aaa11058-7781-4b60-bedf-2e623b01c0ee"}
  ([spec] (exercise spec 10))
  ([spec n]
   (letfn [(f [] (let [d-from-s (data-from-spec spec (if (= n :canonical) :canonical :random))
                       v? (valid-scalars? d-from-s spec)]
                   (vector d-from-s v?)))]
     (repeatedly (if (= n :canonical) 1 n) f))))


;; Collection spec utilities


(defn make-empty
  "Remove all non-collection elements in form, a heterogeneous arbitrarily-nested collection.
   Map keys to descendant collections are preserved."
  {:UUIDv4 #uuid "7ff0187e-52ba-488c-9355-407078575463"
   :no-doc true
   :implementation-details "This function changes the collection on each pass, therefore,
                            typical strategy of (reduce)-ing over a pre-calculated vector of paths
                            won't work because the vector of paths becomes stale on each pass."}
  [form]
  (let [paths-to-elements (only-non-collections (all-paths form))]
    (if ((complement empty?) paths-to-elements)
      (make-empty (dissoc-in* form (:path (get paths-to-elements 0))))
      form)))


(defn remove-paths-with-value
  "Return only paths vector whose value is not vl."
  {:UUIDv4 #uuid "e2a59b24-3617-48ef-94bc-2ec2eeeafce7"
   :no-doc true}
  [paths vl]
  (into [] (filter #(not= vl (:value %)) paths)))


(defn replace-non-colls
  "Replace all non-collection elements in form, a heterogeneous
  arbitratrily-nested collection, with rplc. If rplc is not supplied, elements
  are removed, returning only the emptied collections."
  {:UUIDv4 #uuid "5715605f-be54-464c-a966-e8977c6a2c89"
   :no-doc true}
  ([form] (make-empty form))
  ([form rplc]
   (let [paths-to-non-rplc-elements (remove-paths-with-value
                                     (only-non-collections
                                      (all-paths form)) rplc)]
     (if ((complement empty?) paths-to-non-rplc-elements)
       (replace-non-colls (assoc-in* form
                                     (:path (get paths-to-non-rplc-elements 0))
                                     rplc)
                          rplc)
       form))))


(defn append-coll-type-predicate
  "Returns c with a predicate on its own type."
  {:UUIDv4 #uuid "cd00fda8-a037-4bc2-aad5-b1ccc607d76c"
   :no-doc true}
  [c]
  (cond
    (map? c) (assoc c ::collection-predicate map?)
    (vector? c) (vec (concat c [vector?]))
    (list? c) (concat-list c '(list?))
    (set? c) (conj c set?)))


(defn coll->pred
  "Given a collection, returns a predicate that returns true for that collection
  type."
  {:UUIDv4 #uuid "ebca73b4-9228-4d73-ad36-1e8c953196e4"
   :no-doc true}
  [c]
  (cond
    (vector? c) vector?
    (map? c) map?
    (list? c) list?
    (set? c) set?))


(defn append
  "Kinda like (conj), but maintains the vector/list's order and adds the new
  value to the tail."
  {:UUIDv4 #uuid "e70e9d48-63fc-4c1b-a3d8-a847b3dd7ea3"
   :no-doc true}
  ([c k v] (assoc c k v))
  ([c x]
   (cond
     (vector? c) (conj c x)
     (list? c) (reverse (cons x (reverse c)))
     (set? c) (conj c x))))


;; (ns-unmap *ns* 'inject-coll-preds)
;; (remove-all-methods inject-coll-preds)


(defmulti inject-coll-preds
  "Given a empty heterogeneous, arbitrarily nested collection `form`, returns a
  structurally-similar nested collection, with each entry a predicate that tests
  `true` for that containing collection. Suitable for a very basic collection
  spec."
  {:UUIDv4 #uuid "bb5a50c6-54c7-4820-aa2a-49438a3bd912"
   :no-doc true}
  (fn [c] (type c)))

(defmethod inject-coll-preds ::map [m]
  (reduce (fn [acc [k v]] (assoc acc k (inject-coll-preds v)))
          (assoc m ::collection-predicate (coll->pred m))
          m))

(defmethod inject-coll-preds ::non-map [x]
  (append (reduce (fn [acc v] (append acc (inject-coll-preds v)))
                  (empty x)
                  x)
          (coll->pred x)))

(defmethod inject-coll-preds nil [x] nil)


(defn basic-collection-spec-from-data
  "Given `data`, a heterogeneous, arbitrarily-nested collection,
  returns a similar structure that can serve as basic collection specification.
  Non-terminating sequences are clamped to length `3` and converted to vectors.

  Note: Speculoos validates maps with predicates at keys which do not exist in
  `data`, so a pseudo-qualified key `:speculoos.utility/collection-predicate`
  is created that is applied by [[validate-collections]].

  Examples:
  ```clojure
  (basic-collection-spec-from-data [42 [:foo [22/7]]])
  ;; => [[[vector?] vector] vector?]

  (basic-collection-spec-from-data {:a 42 :b {:c ['foo true]}})
  ;; => {:b {:c [vector?], :speculoos.utility/collection-predicate map?},
  ;;     :speculoos.utility/collection-predicate map?}

  (basic-collection-spec-from-data [42 #{:foo} (list 22/7) (range)])
  ;; => [#{set?} (list?) [vector] vector]
  ```"
  {:UUIDv4 #uuid "07f274d9-7daf-4f74-a998-0feb25697e9f"}
  [data]
  (-> (clamp-every data 3)
      make-empty
      inject-coll-preds))


(defn collections-without-predicates
  "Given `data` a heterogeneous, arbitrarily-nested data structure, returns a
  set of `data`'s all-paths elements for collections which lack at least one
  corresponding predicate in collection specification `spec`. See also
  [[scalars-without-predicates]] and [[scalars-with-predicates]].

  Examples:
  ```clojure
  ;; all collections have a corresponding collection predicate; returns an empty set
  (collections-without-predicates [11 [22] 33] [vector? [vector?]])
  ;; => #{}

  ;; missing predicate(s)
  (collections-without-predicates [11 [22] 33] [vector? []])
  ;; => #{{:path [1], :value [22]}}

  (collections-without-predicates {:a [11 {:b (list 22 33)}]} {:a [vector? {:is-a-map? map?}]})
  ;; => #{{:path [:a 1 :b], :value (22 33)}
  ;;      {:path [], :value {:a [11 {:b (22 33)}]}}}

  (collections-without-predicates [11 [22] 33 (cycle [44 55 66])] [vector? [vector?]])
  ;; => #{{:path [3], :value []}}
  ```"
  {:UUIDv4 #uuid "3cced132-72aa-42b4-8ac1-4488d358572a"}
  [data spec]
  (let [[data spec] (expand-and-clamp data spec)
        all (all-paths data)
        validation (validate-collections data spec)
        colls-only (set (filter #(coll? (:value %)) all))
        red-fn (fn [acc vl] (let [entry (select-keys vl [:ordinal-path-datum :datum])]
                              (disj acc {:path (recover-literal-path data (:ordinal-path-datum entry))
                                         :value (:datum entry)})))]
    (reduce red-fn colls-only validation)))


(defn predicates-without-collections
  "Given `data` at heterogeneous, arbitrarily-nested data structure and a
  collection specification `spec`, returns a set of all-paths elements for
  predicates in `spec` which cannnot be paired with a collection element in
  `data`. See also [[collections-without-predicates]],
  [[scalars-without-predicates]], and [[predicates-without-scalars]].

  Examples:
  ```clojure
  ;; all predicates are paired
  (predicates-without-collections [42] [vector?])
  ;; => #{}

  ;; one un-paired predicate
  (predicates-without-collections [42] [vector? [map?]])
  ;; => #{{:path [1 0], :value map?}}

  ;; one un-paired predicate
  (predicates-without-collections {:a 42} {:is-map? map? :b [vector?]})
  ;; => #{{:path [:b 0], :value vector?}}

  ;; one un-paired predicate <-- collection validation only applies predicates to non-scalars
  (predicates-without-collections {:a 42 :b 99} {:is-map? map? :b [vector?]})
  ;; => #{{:path [:b 0], :value vector?}}
  ```"
  {:UUIDv4 #uuid "15f87b04-f05b-40a3-a308-9d10515409a8"}
  [data spec]
  (let [[data spec] (expand-and-clamp data spec)
        all-spec (all-paths spec)
        validation (validate-collections data spec)
        fns-only (set (filter #(fn? (:value %)) all-spec))
        red-fn (fn [acc vl] (let [entry (select-keys vl [:path-predicate :predicate])]
                              (disj acc {:path (:path-predicate entry)
                                         :value (:predicate entry)})))]
    (reduce red-fn fns-only validation)))


(defn thoroughly-valid-scalars?
  "Given a heterogeneous, arbitrarily-nested data structure `data` and scalar
  specification `spec`, returns `true` if every scalar contained in `data` has a
  corresponding predicate in `spec`, and every scalar satisfies its predicate.

  See [[valid-scalars?]], [[scalars-without-predicates]], and
  [[thoroughly-valid?]].

  Examples:
  ```clojure
  ;; all scalars have satisfied predicates
  (thoroughly-valid-scalars? [11 {:x 22/7 :y 'foo}] [int? {:x ratio? :y symbol?}])
  ;; => true

  ;; all scalars have predicates, but not all satisfied
  (thoroughly-valid-scalars? [11 {:x 22/7 :y 'foo}] [char? {:x ratio? :y symbol?}])
  ;; => false

  ;; all predicates satisfied, but one scalar is missing a predicate
  (thoroughly-valid-scalars? [11 {:x 22/7 :y 'foo}] [int? {:x ratio?}])
  ;; => false
  ```"
  {:UUIDv4 #uuid "ab6d091d-82d2-4b2f-965d-1de630d320c1"}
  [data spec]
  (and (empty? (scalars-without-predicates data spec))
       (valid-scalars? data spec)))


(defn thoroughly-valid-collections?
  "Given a heterogeneous, arbitrarily-nested data structure `data` and
  collection specification `spec`, returns `true` if every collection contained
  in `data` has a corresponding predicate in `spec`, and every collection
  satisfies its predicate.

  See [[valid-collections?]], [[collections-without-predicates]], and
  [[thoroughly-valid?]].

  Examples:
  ```clojure
  ;; all collections have satisfied predicates
  (thoroughly-valid-collections? [11 {:x 22/7 :y 'foo}] [vector? {:is-map? map?}])
  ;; => true

  ;; all collections have predicates, but not all satisfied
  (thoroughly-valid-collections? [11 {:x 22/7 :y 'foo}] [list? {:is-map? map?}])
  ;; => false

  ;; all predicates satisfied, but one collection is missing a predicate
  (thoroughly-valid-collections? [11 {:x 22/7 :y 'foo}] [{:is-map? map?}])
  ;; => false
  ```"
  {:UUIDv4 #uuid "81283b57-2543-4fa2-9d92-72ed1cbd7bbc"}
  [data spec]
  (and (empty? (collections-without-predicates data spec))
       (valid-collections? data spec)))


(defn thoroughly-valid?
  "Given a heterogeneous, arbitrarily-nested data structure `data`, returns
  `true` if every scalar in `data` has a predicate in `scalar-spec` and
  every collection has at least one predicate in `collection-spec`, and all
  predicates are satisfied.

  See [[valid?]], [[scalars-without-predicates]], and
  [[collections-without-predicates]].

  Examples:
  ```clojure
  ;; all scalars and colls have satisfied predicates
  (thoroughly-valid? [11 {:x 22/7 :y 'foo}] [int? {:x ratio? :y symbol?}] [vector? {:is-map? map?}])
  ;; => true

  ;; all scalars and colls have predicates, but not all satisfied
  (thoroughly-valid? [11 {:x 22/7 :y 'foo}] [char? {:x ratio? :y symbol?}] [vector? {:is-map? map?}])
  ;; => false

  ;; all predicates satisfied, but one scalar is missing a predicate
  (thoroughly-valid? [11 {:x 22/7 :y 'foo}] [int? {:x ratio?}] [vector? {:is-map? map?}])
  ;; => false

  ;; all predicates satisfied, but one coll is missing a predicate
  (thoroughly-valid? [11 {:x 22/7 :y 'foo}] [int? {:x ratio? :y symbol?}] [{:is-map? map?}])
  ;; => false
  ```"
  {:UUIDv4 #uuid "43b126ce-c684-4358-b5ec-bba6f4c46fc5"}
  [data scalar-spec collection-spec]
  (and (thoroughly-valid-scalars? data scalar-spec)
       (thoroughly-valid-collections? data collection-spec)))


(defn clamp-in*
  "Given a heterogeneous, arbitrarily-nested collection `coll`, 'clamp'
  (possibly) non-terminating sequence at `path` to length `x`. Sequences are
  converted to vectors.

  Beware: Behaves like `take`, so will also possibly shorten vectors, lists,
  sets, and maps if located at `path`.

  Examples:
  ```clojure
  (clamp-in* [:foo (range) 'bar] [1] 3)
  ;; => [:foo [0 1 2] bar]

  (clamp-in* {:a [42 'foo {:b (cycle [:x :y :z])}]} [:a 2 :b] 5)
  ;; => {:a [42 foo {:b [:x :y :z :x :y]}]}

  ;; does not discriminate; operates on terminating sequences, too
  (clamp-in* [:foo 42 [:x :y :z :a :b :c]] [2] 4)
  ;; => [:foo 42 [:x :y :z :a]]

  ;; sequences that are shorter than `x` are unchanged
  (clamp-in* {:a (take 3 (range))} [:a] 6)
  ;; => {:a [0 1 2]}
  ```"
  {:UUIDv4 #uuid "6ed6ed36-b5ea-41ae-b752-d351aa25d716"
   :implmentation "Consider renaming to (take-in*)"}
  [coll path x]
  (if (= [] path)
    (vec (take x coll))
    (assoc-in* coll path (vec (take x (get-in* coll path))))))


(defn clamp-every
  "Given a heterogeneous, arbitrarily-nested collection `coll`, 'clamp' every
  (possibly) non-terminating sequence at length `x`. Sequences are converted to
  vectors. See [[clamp-in*]] for particulars on behavior.

  Examples:
  ```clojure
  (clamp-every [42 (range) 99 (repeat :foo) 33] 3)
  ;; => [42 [0 1 2] 99 [:foo :foo :foo] 33]

  (clamp-every {:a (cycle ['foo 'bar 'baz]) :b (iterate inc 100)} 5)
  ;; => {:a [foo bar baz foo bar], :b [100 101 102 103 104]}
  ```"
  {:UUIDv4 #uuid "8418871e-449a-477a-8377-490e65cfbfd6"}
  [coll x]
  (if-let [non-terminating (->> coll
                                all-paths
                                (filter #(:non-terminating? %))
                                not-empty)]
    (clamp-every (clamp-in* coll (:path (first non-terminating)) x) x)
    coll))


(defn in?
  "Returns `true` if `item` is found somewhere in collection `coll`. This
  utility function is a replacement for what the name
  [`clojure.core/contains?`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/contains?)
  suggests it might do. Works on all collection types, but map elements are
  checked as `MapEntry` key-value pairs. Pass `(vals m)` if you want to only
  check values. Properly handles `nil` and `false` membership.

  Examples:
  ```clojure
  ;; `contains?` tests for existence of a sequence's index, not its values
  (contains? [:a :b :c] 1) ;; => true

  ;; `in?` tests for existence of a sequence's values
  (in? [:a :b :c] 1) ;; => false

  (in? [:a :b :c] :b) ;; true
  (in? (list 'foo 'bar 'baz) 'foo) ;; => true
  (in? #{:red :green :blue} :green) ;; => true
  (in? (range) 3) ;; => true

  ;; elements of a map are tested as MapEntries
  (in? {:a 1 :b 2 :c 3} 3) ;; => false

  ;; passing just the vals of a map
  (in? (vals {:a 1 :b 2 :c 3}) 3) ;; => true

  ;; testing existence of a MapEntry
  (in? {:a 1 :b 2 :c 3} [:c 3]) ;; => true

  ;; nil presence
  (in? [42 nil 22/7] nil) ;; => true

  ;; false presence
  (in? (vals {:a 42 :b false}) false) ;; => true
  ```"
  {:UUIDv4 #uuid "2110049f-ca4f-4be0-8c39-4efc265f403d"}
  [coll item]
  (boolean (some #(= item %) coll)))


(defn seq-regex
  "Returns `true` if sequence `s` fully satisfies `pred-regex`, pairs of
  predicates and regex-like operators. Predicates partition the sequence, regex
  operators check the quantity, according to the following.

  * `:?` zero-or-one
  * `:.` exactly-one
  * `:+` one-or-more
  * `:*` zero-or-more
  * `[m n]` between `m` and `n` (integers), inclusive
  * `i` exactly `i` (integer)

  Examples:
  ```clojure
  (seq-regex [1 2 3 :a :b 'foo] int? :* keyword? :+ symbol? :.) ;; => true
  ```
  Tests _zero-or-more integers, followed by one-or-more keywords,
  followed by exactly-one symbol_.

  You must supply predicate and regexes to exhaust the sequence, otherwise
  returns `false`. If you don't care about entities beyond a particular index,
  use the duplet `any? :*`.

  ```clojure
  (seq-regex [1 2 :a :b true false] int? 2 keyword? [1 3] any? :*) ;; => true
  ```
  Tests _exactly two integers, followed by one to three keywords, followed by
  any number of anything_.

  Failing example:
  ```clojure
  (seq-regex [1 2 :a :b] int? 2) ;; => false
  ```
  Tests _exactly two integers_; example fails because trailing keywords are not
  matched.

  Any unused pred-regex pairs are ignored.
  ```clojure
  (seq-regex [1 2 3] int? 3 keyword? 3) ;; => true
  ```

  If your first regex is _zero-or-more_ `:*`, then it will match an empty
  sequence, regardless of any trailing pred-regex pairs.
  ```clojure
  (seq-regex [] int? :* keyword :+) ;; => true
  ```

  Do not stack the _one-or-more_ `:.` regex operator in an attempt to get an
  integer `>1`.
  ```clojure
  (seq-regex [1 2 3] int? :. int? :. int? :.) ;; => false
  ```

  Instead, use the _exactly-integer_ or the range ops.
  ```clojure
  (seq-regex [1 2 3] int? 3) ;; => true
  (seq-regex [1 2 3] int? [1 4]) ;; => true
  ```

  Predicates must be specific enough so that they aren't consumed further than
  you intend. This is treacherous when, e.g., converting to string.
  Here's possibly surprising failing example:
  ```clojure
  (seq-regex [:a :b :c 'fo 'br 'bz] #(= 2 (count (str %))) 3 symbol? 3)
  ;; => false
  ```
  `'fo`, `'br`, and `'bz` all satisfy `length=2` when converted to string,
  leaving `symbol?` no values to test. Instead, insert a guarding predicate.
  ```clojure
  (seq-regex [:a :b :c 'fo 'br 'bz] #(and (keyword? %)
                                          (= 2 (count (str %)))) 3 symbol? 3)
  ;; => true
  ```"
  {:UUIDv4 #uuid "9a47dc36-4c8f-4adc-acc1-ca5e1c197384"}
  [s & pred-regexes]
  (if (odd? (count pred-regexes))
    (throw (AssertionError. "seq-regex requires pairs of predicate/regex-op. Odd number supplied."))
    (let [pred (or (first pred-regexes) (constantly false))
          regex-op (fnext pred-regexes)
          front (take-while pred s)
          rear (drop-while pred s)
          remainder-pred-regexes (nnext pred-regexes)
          qty (cond
                (int? regex-op) [regex-op regex-op]
                (vector? regex-op) regex-op
                (keyword? regex-op) ({:. [1 1]
                                      :? [0 1]
                                      :+ [1 ##Inf]
                                      :* [0 ##Inf]} regex-op)
                (nil? regex-op) [##Inf ##Inf])
          expected-count? ((fn [[mn mx]] #(<= mn (count %) mx)) qty)]
      (and (expected-count? front)
           (or (empty? rear)
               (apply seq-regex rear remainder-pred-regexes))))))


;; Symbols can't have a leading numeral, so (1st), (2nd), (3rd), etc, won't work.
;; Naming question: What is the most clear/visual pleasing? Easiest to keyboard?
;; (@1st), (@2nd), (@3rd), etc. 'at' conveys a location semantic
;; (=1st), (=2nd), (=3rd), etc. 'equals' conveys some semantics, but is it the correct semantic?
;; (_1st), (_2nd), (_3rd), etc
;; something else?
;; Should (1st), (2nd), (3rd), etc match the semantics of clojure.core/nth or clojure.core/first?
;; (nth c 1) is the element at index 1, the second element.
;; In casual usage, '1st' would be the element at index 0.
;; For now, the least surprising thing to me is one-based indexing, but it settable.


(def ^{:dynamic true
       :doc "Governs behavior of [[=1st]], [[=2nd]], through [[=12th]]. Bind to
 `-1` (default) for one-based indexing. Bind to `0` for zero-based indexing."}
  *ordinal-offset* -1)


(defn- th
  "Helper function to produce functions like (1st), (3rd), which access the 1st
  and 3rd elements. `t` is the ordinal position (i.e., `t=3` implies third).
  `coll` is the passed collection. `*ordinal-offset*` is a possibly
  globally-settable var. Defaults `-1`, which results in 4th corresponding to
  one-based 'every-day usage', not the computer science notion of the zero-based
  fifth element."
  {:UUIDv4 #uuid "0240082e-fd62-401d-8a30-d5d5833ea3a1"
   :no-doc true}
  [t]
  (fn [coll] (get* coll (+ t *ordinal-offset*))))

(def ^{:no-doc true}
  =1st-docstring
  "`(=1st s)` <br>
   `(=2nd s)` <br>
   `(=3rd s)` <br>
   `(=4th s)` <br>
   `(=5th s)` <br>
   `(=6th s)` <br>
   `(=7th s)` <br>
   `(=8th s)` <br>
   `(=9th s)` <br>
   `(=10th s)` <br>
   `(=11th s)` <br>
   `(=12th s)`

  Convenience functions to access elements of a sequence `s`. Uses one-based
  indexing by default.

Examples:
```clojure
(=1st [:one :two :three]) ;; => :one
(=2nd [:one :two :three]) ;; => :two
(=3rd [:one :two :three]) ;; => :three

(=2nd (list 'foo 'bar 'baz)) ;; => bar
```
Re-bind [[*ordinal-offset*]] for zero-based indexing.

Example:
```clojure
(binding [*ordinal-offset* 0]
    (=1st [:one :two :three])) ;; => :two
```")


(def ^{:no-doc true} ordinal-docstring "See [[=1st]].")


(def ^{:doc    =1st-docstring} =1st  (th  1))
(def ^{:doc ordinal-docstring} =2nd  (th  2))
(def ^{:doc ordinal-docstring} =3rd  (th  3))
(def ^{:doc ordinal-docstring} =4th  (th  4))
(def ^{:doc ordinal-docstring} =5th  (th  5))
(def ^{:doc ordinal-docstring} =6th  (th  6))
(def ^{:doc ordinal-docstring} =7th  (th  7))
(def ^{:doc ordinal-docstring} =8th  (th  8))
(def ^{:doc ordinal-docstring} =9th  (th  9))
(def ^{:doc ordinal-docstring} =10th (th 10))
(def ^{:doc ordinal-docstring} =11th (th 11))
(def ^{:doc ordinal-docstring} =12th (th 12))


;; defpred, a utility macro for defining predicates and automatically creating
;; random sample generators


(def pred-sym->gen-sym
  (with-meta
    {'ratio? '(clojure.test.check.generators/such-that ratio? clojure.test.check.generators/ratio)
     'boolean? 'clojure.test.check.generators/boolean
     'char? 'clojure.test.check.generators/char-alphanumeric
     'double? 'clojure.test.check.generators/double
     'int? 'clojure.test.check.generators/small-integer
     'keyword? 'clojure.test.check.generators/keyword
     'simple-keyword? 'clojure.test.check.generators/keyword
     'qualified-keyword? 'clojure.test.check.generators/keyword-ns
     'neg-int? '(clojure.test.check.generators/fmap - clojure.test.check.generators/nat)
     'pos-int? '(clojure.test.check.generators/nat)
     'string? 'clojure.test.check.generators/string-alphanumeric
     'symbol? 'clojure.test.check.generators/symbol
     'simple-symbol? 'clojure.test.check.generators/symbol
     'qualified-symbol? 'clojure.test.check.generators/symbol-ns
     'uuid? 'clojure.test.check.generators/uuid}
    {:UUIDv4 #uuid "7a28e369-2a17-4a62-a1c9-ad1da404befd"
     :doc "Maps predicate symbols to clojure.test.check.generators symbols."
     :no-doc true}))


(defn predicate-scalar-symbol
  "Given s-exp `f` that represents a 1-arity predicate function, return the
  symbol that represents the scalar being tested.

  Examples:
  ```clojure
  (predicate-scalar-symbol '(fn [x] (int? x))) ;; => x

  (predicate-scalar-symbol '#(int? %)) ;; => p1__9877#

  (predicate-scalar-symbol '(fn [foo] (and (number? foo) (< foo 99)))) ;; => foo
  ```"
  {:UUIDv4 #uuid "d1611c53-da0d-42d5-af99-b82a20426252"
   :no-doc true}
  [f]
  (first (second f)))


(defn valid-fn-form?
  "Returns `true` if form `f` represents a valid function, `false` otherwise."
  {:UUIDv4 #uuid "86f56c2a-0286-4061-8ee1-f68e2ed8b0d3"
   :no-doc true}
  [f]
  (boolean (#{'fn 'fn*} (first f))))


(def predicate->generator
  (with-meta
    (reduce #(assoc %1 (:predicate (get %2 1)) (:gen (get %2 1)))
            {}
            speculoos.utility/type-predicate-canonical)
    {:UUIDv4 #uuid "824f18f6-9822-424a-a6e9-82c8bcfeadfb"
     :no-doc true}))


(def ^:dynamic *such-that-max-tries* 50)


(defn and-branch
  "Given a sequence `s` of predicate s-exps, returns a vector containing a
  generator symbol and that generator symbol evaled. Argument symbol
  `arg-sym` must be supplied in order for
  `clojure.test.check.generators/such-that` modifiers can be constructed."
  {:UUIDv4 #uuid "9e193537-7a30-4fe8-9484-5c0f41610780"
   :no-doc true}
  [s arg-sym]
  (let [exclude-and (next s)
        base-pred (ffirst exclude-and)
        modifiers (cons 'and (next exclude-and))
        such-that `(fn [~arg-sym] ~modifiers)
        generator (pred-sym->gen-sym base-pred)
        generator-sym (if (next modifiers)
                        `(clojure.test.check.generators/such-that ~such-that ~generator {:max-tries *such-that-max-tries*})
                        generator)]
    [generator-sym (eval generator-sym)]))


(defn and-branch-self-check
  "Check the returned generator against a manually-supplied predicate. `s` and
  `arg-sym` correspond the args in [[and-branch]], `pred` is the predicate to
  test against."
  {:UUIDv4 #uuid "64b14e4d-ef96-48b4-8fa8-89980cb5e70a"
   :no-doc true}
  [s arg-sym pred]
  (let [[gen-sym gen-obj] (and-branch s arg-sym)
        samples (clojure.test.check.generators/sample gen-obj)
        valids (map #(pred %) samples)]
    (every? true? valids)))


(comment
  (and-branch-self-check '(and (int? i) (even? i))
                         'i
                         int?)

  (and-branch-self-check '(and (string? s) (< 3 (count s)))
                         's
                         string?)

  (and-branch-self-check '(and (keyword? k) (< 3 (count (str k))))
                         'k
                         keyword?)

  (and-branch-self-check '(and (ratio? r) (< 1/9 r))
                         'r
                         ratio?)
  )


(defn or-branch
  "Given a sequence `s` of predicate s-exps, returns generator symbol and that
  generator symbol evaled, which would produce one or more scalar types with
  equal probability. Argument symbol `arg-sym` must be supplied in order for
  `clojure.test.check.generators/such-that` modifiers can be constructed."
  {:UUIDv4 #uuid "75fdfaac-502d-4ed2-b768-24d7673ed14d"
   :no-doc true}
  [s arg-sym]
  (let [exclude-or (next s)
        f (fn [x] (case (first x)
                    and (and-branch x arg-sym)
                    [(pred-sym->gen-sym (first x)) (eval (pred-sym->gen-sym (first x)))]))
        branches (map f exclude-or)]
    [(reverse (conj '(clojure.test.check.generators/one-of) (vec (map #(get % 0) branches))))
     (clojure.test.check.generators/one-of (map #(get % 1) branches))]))


(comment
  (or-branch '(or) 'z)
  (or-branch '(or (int? x)) 'x)
  (or-branch '(or (string? y) (keyword? y)) 'y)
  (or-branch '(or (int? z) (string? z) (boolean? z)) 'z)

  (or-branch '(or (and (int? i) (even? i)))
             'i)

  (or-branch '(or (and (int? q) (pos? q))
                  (string? q))
             'q)

  (or-branch '(or (and (int? w) (odd? w) (>= w 2))
                  (and (string? w) (< 3 (count w)))
                  (and (ratio? w) (<= 1/9 w) (< w 1)))
             'w)

  (gen/sample (get (or-branch '(or (int? a)
                                   (string? a)
                                   (ratio? a))
                              'a)
                   1))

  (gen/sample (get (or-branch '(or (and (int? b) (even? b) (<= 1 b))
                                   (and (string? b) (<= 3 (count b)))
                                   (and (ratio? b) (<= 1/9 b) (< b 1/1))
                                   (and (boolean? b) (true? b)))
                              'b)
                   1))
  )


(defn inspect-fn
  "Inspect expression `f` to attempt to create a random sample generator. If
  unable to do so, returns `nil`. `f` must be of the form `(fn [...] ...)`
  or `#(...)`. Returns a vector whose first element is the symbolic
  representation of the generator and whose second element is an invocable
  generator object.

  *This implementation is a proof-of-concept* that only descends to a maximum
  depth of two levels. It does not generically handle arbitrary nesting depths
  nor all possible Clojure data types

  The predicate body must fulfill these properties:

  1. The first symbol must be `and`, `or`, or a basic predicate for a Clojure
     built-in scalar, such as `int?` that is registered at
     `speculoos.utility/predicate->generator`.
  2. The first clause after `and` or `or` must contain a `clojure.core`
     predicate for a scalar, such as `int?`.
  3. Subsequent clauses of `and` will be injected into a
     `clojure.test.check.generators/such-that` filter.
  4. Direct descendants of a top-level `or` will produce `n` separate random
     sample generators, each with `1/n` probability for any one invocation.

  Examples:
  ```clojure
  (inspect-fn '(fn [i] (int? i)))
  (inspect-fn '(fn [i] (and (int? i) (even? i))))

  (inspect-fn '#(int? %))
  (inspect-fn '#(and (int? %) (even? %)))

  (inspect-fn '(fn [x] (or (int? x) (ratio? x))))

  (inspect-fn '#(or (and (int? %) (odd? %))
                    (and (string? %) (<= 3 (count %)))))
  ```"
  {:UUIDv4 #uuid "88eaaa10-489b-4f8c-8a09-6d7b89e89c9e"}
  [f]
  (if (valid-fn-form? f)
    (let [fn-body (nth f 2)
          arg-symbol (predicate-scalar-symbol f)]
      (case (first fn-body) ;; symbols should not be quoted [https://ask.clojure.org/index.php/9508/incorrect-difficult-comprehend-behavior-matching-clojure]
        and (and-branch fn-body arg-symbol)
        or (or-branch fn-body arg-symbol)
        [(pred-sym->gen-sym (first fn-body)) (eval (pred-sym->gen-sym (first fn-body)))]))
    "Argument `f` is not a valid function form such as (fn [x] ...) or #(...)"))


(comment
  ;; invalid form
  (inspect-fn '())

  ;; empty
  (inspect-fn '(fn [] ()))
  (inspect-fn '#())

  ;; (fn [...] ...) patterns
  ;;;; single predicates, no modifiers
  (inspect-fn '(fn [i] (int? i)))
  (inspect-fn '(fn [s] (string? s)))
  (inspect-fn '(fn [k] (keyword? k)))

  ;;;; single predicates with 'and, but no modifiers
  (inspect-fn '(fn [i] (and (int? i))))
  (inspect-fn '(fn [r] (and (ratio? r))))
  (inspect-fn '(fn [c] (and (char? c))))

  ;;;; single predicates, 'and, one modifier
  (inspect-fn '(fn [i] (and (int? i) (even? i))))
  (inspect-fn '(fn [s] (and (string? s) (<= 3 (count s)))))
  (inspect-fn '(fn [d] (and (double? d) (<= 3 d))))

  ;;;; single predicates, 'and, multiple modifiers
  (inspect-fn '(fn [r] (and (ratio? r) (< r 99) (>= r 0))))
  (inspect-fn '(fn [b] (and (boolean? b) (true? b) (not (false? b)))))
  (inspect-fn '(fn [c] (and (char? c) (not= c \A) (not= c \B))))
  (inspect-fn '(fn [d] (and (double? d) (< d 999) (>= d 0))))
  (inspect-fn '(fn [i] (and (int? i) (even? i) (< i 99) (not= i 5))))
  (inspect-fn '(fn [k] (and (keyword? k) (not= k :foo) (not= :bar k))))
  (inspect-fn '(fn [sk] (and (simple-keyword? sk) (not= sk :foo) (not= :bar sk))))
  (inspect-fn '(fn [qk] (and (qualified-keyword? qk) (not= qk :foo) (not= :bar qk))))
  (inspect-fn '(fn [ni] (and (neg-int? ni) (not= ni 5) (<= ki 0))))
  (inspect-fn '(fn [pi] (and (pos-int? pi) (not= pi -1) (>= pi 0))))
  (inspect-fn '(fn [s] (and (string? s) (not= s "foobarbaz") (not= 1 (count s)))))
  (inspect-fn '(fn [s] (and (symbol? s) (not= s 'foo) (not= 'bar s))))
  (inspect-fn '(fn [ss] (and (simple-symbol? ss) (not= ss 'foo) (not= 'baz ss))))
  (inspect-fn '(fn [qs] (and (qualified-symbol? qs) (not= qs 'foo) (not= 'bar qs))))
  (inspect-fn '(fn [u] (and (uuid? u) (not= u "foo") (not= u :foo))))

  ;; #(... %) patterns
  ;;;; single predicates, no modifiers
  (inspect-fn '#(int? %))
  (inspect-fn '#(string? %))
  (inspect-fn '#(keyword? %))

  ;;;; single predicates with 'and, but no modifiers
  (inspect-fn '#(and (int? %)))
  (inspect-fn '#(and (ratio? %)))
  (inspect-fn '#(and (char? %)))

  ;;;; single predicates, 'and, one modifer
  (inspect-fn '#(and (int? %) (even? %)))
  (inspect-fn '#(and (string? %) (<= 3 (count %))))
  (inspect-fn '#(and (double? %) (<= 3 %)))

  ;;;; single predicats, 'and, multiple modifiers
  (inspect-fn '#(and (ratio? %) (< % 99) (>= % 0)))
  (inspect-fn '#(and (boolean? %) (true? %) (not (false? %))))
  (inspect-fn '#(and (double? %) (< % 999) (>= % 0)))
  (inspect-fn '#(and (int? %) (even? %) (< % 99) (not= % 5)))
  (inspect-fn '#(and (string? %) (not= % "foobarbaz") (not= 1 (count %))))

  ;; 'or
  (inspect-fn '(fn [i] (or (int? i))))
  (inspect-fn '(fn [i] (or (int? i) (string? i))))
  (inspect-fn '(fn [x] (or (and (int? x) (even? x))
                           (and (string? x) (<= 3 (count x))))))

  (inspect-fn '#(or (int? %)))
  (inspect-fn '#(or (int? %) (string? %)))
  (inspect-fn '#(or (and (int? %) (odd? %))
                    (and (string? %) (<= 3 (count %)))))
)


(defn inspect-fn-self-check
  "Check the results of `inspect-fn` by applying the supplied predicate `p`
  against the random sample returned by the generator."
  {:UUIDv4 #uuid "129d53fc-f39a-418c-9617-4a7ef5cdc43e"
   :no-doc true}
  [p]
  (let [[generator-form generator-obj] (inspect-fn p)
        sample (clojure.test.check.generators/sample generator-obj 10)]
    {:predicate-form p
     :generator-form generator-form
     :generator-object generator-obj
     :generated-sample sample
     :all-valid? (every? #(true? ((eval p) %)) sample)}))


(comment
  (inspect-fn-self-check '#(int? %))
  (inspect-fn-self-check '#(string? %))

  (inspect-fn-self-check '#(and (int? %)))
  (inspect-fn-self-check '#(and (string? %)))

  (inspect-fn-self-check '#(and (int? %) (even? %)))
  (inspect-fn-self-check '#(and (string? %) (< 3 (count %))))

  (inspect-fn-self-check '(fn [k] (keyword? k)))
  (inspect-fn-self-check '(fn [s] (string? s)))
  (inspect-fn-self-check '(fn [i] (int? i)))

  (inspect-fn-self-check '(fn [i] (and (int? i) (even? i))))
  (inspect-fn-self-check '(fn [s] (and (string? s) (<= 3 (count s)))))

  (inspect-fn-self-check '(fn [x] (or (int? x))))
  (inspect-fn-self-check '(fn [x] (or (int? x) (string? x))))
  (inspect-fn-self-check '(fn [x] (or (int? x) (string? x) (ratio? x))))

  (inspect-fn-self-check '(fn [x] (or (and (int? x)
                                           (even? x)))))

  (inspect-fn-self-check '(fn [x] (or (and (int? x)
                                           (odd? x))
                                      (and (string? x)
                                           (<= 3 (count x))))))

  (inspect-fn-self-check '(fn [x] (or (and (int? x)
                                           (pos? x)
                                           (even? x))
                                      (and (string? x)
                                           (<= 2 (count x)))
                                      (and (ratio? x)
                                           (<= 1/8 x)))))

  (inspect-fn-self-check '#(or (int? %)))
  (inspect-fn-self-check '#(or (int? %) (string? %)))
  (inspect-fn-self-check '#(or (int? %) (string? %) (ratio? %)))

  (inspect-fn-self-check '#(or (and (int? %)
                                    (neg? %))))

  (inspect-fn-self-check '#(or (and (int? %)
                                    (even? %))
                               (and (string? %)
                                    (<= 2 (count %)))))

  (inspect-fn-self-check '#(or (and (int? %)
                                    (even? %))
                               (and (string? %)
                                    (<= 2 (count %)))
                               (and (ratio? %)
                                    (<= 1/9 %))))
  )


(defmacro defpred
  "*def*ine a *pred*icate by binding symbol `predname` to function `f` while
  associating a random sample generator.

  1. With only a name `predname` and function `f`, `defpred` will attempt to
     create a random sample generator as outlined by [[inspect-fn]].
  2. An explicitly supplied `generator` will be preferred.
  3. If `generator` is explicitly provided, a canonical value may also be
     supplied. If the optional canonical value is not supplied, a keyword is
     automatically generated using the symbol `predname` appended with
     `-canonical-sample`.

  Useful for defining predicates consumed by [[data-from-spec]], [[exercise]],
  and friends.

  `*such-that-max-tries*` is bound to an integer (default `50`) that governs the
  number of attempts the random sample generator will make to create a valid
  sample.

  Note: Can not use bare predicates such as `int?`; they must be 'wrapped', like
  `#(int? %)`. See examples.

  Examples:
  ```clojure
  (macroexpand-1 '(defpred foo (fn [i] (int? i)) (fn [] (rand-int 99)) 42))
  ;; => (def foo (clojure.core/with-meta (fn [i] (int? i)) #:speculoos{:canonical-sample 42, :predicate->generator (fn [] (rand-int 99))}))

  (defpred f1 #(<= % 99) #(rand-int 99) 42)
  (defpred f2 #(<= % 5)  #(- (rand-int 99))) ;; no canonical value supplied
  (defpred f3 #(> % 999) #(rand 999) 1.23)

  (data-from-spec [f1 f2 f3] :random)
  ;; => [86 -77 971.3462532541377]

  (data-from-spec [f1 f2 f3] :canonical)
  ;; => [42 :f2-canonical-sample 1.23]

  ;; using (and...) to modify a core predicate
  (defpred even-int? #(and (int? %) (even? %) (< 3 %)))
  (data-from-spec [even-int? even-int? even-int? even-int?] :random)
  ;; => [14 24 16 30]

  ;; using (or...) to generate alternatives
  (defpred int-or-kw? #(or (int? %) (keyword? %)))
  (data-from-spec [int-or-kw? int-or-kw? int-or-kw? int-or-kw?] :random)
  ;; => [:J0+ -25 22 :o_6W.l:?]

  ;; using both (and...) and (or...)
  (defpred odd-int-or-short-kw? (fn [x] (or (and (int? x)
                                                 (odd? x))
                                            (and (keyword? x)
                                                 (>= 3 (count (str x)))))))

  (data-from-spec [odd-int-or-short-kw?
                   odd-int-or-short-kw?
                   odd-int-or-short-kw?
                   odd-int-or-short-kw?] :random)
  ;; => [:Y9.c :i. -3 :xxk]
  ```"
  {:UUIDv4 #uuid "f49dc35b-b3e9-4e4a-b636-75e38b6a40e0"}
  ([predname f]
   (let [f-symbol f]
     `(def ~predname (with-meta ~f {:speculoos/predicate->generator #(clojure.test.check.generators/generate (get (inspect-fn '~f-symbol) 1))
                                    :speculoos/canonical-sample ~(keyword (str predname "-canonical-sample"))}))))
  ([predname f generator]
   `(def ~predname (with-meta ~f {:speculoos/predicate->generator ~generator
                                  :speculoos/canonical-sample ~(keyword (str predname "-canonical-sample"))})))
  ([predname f generator canonical]
   `(def ~predname (with-meta ~f {:speculoos/predicate->generator ~generator
                                  :speculoos/canonical-sample ~canonical}))))


(comment

  (macroexpand-1 '(defpred foo (fn [i] (int? i))))
  (macroexpand-1 '(defpred foo (fn [i] (and (int? i)))))
  (macroexpand-1 '(defpred foo (fn [i] (and (int? i) (even? i)))))

  (macroexpand-1 '(defpred foo #(int? %)))
  (macroexpand-1 '(defpred foo #(and (int? %))))
  (macroexpand-1 '(defpred foo #(and (int? %) (even? %))))

  (macroexpand-1 '(defpred foo #(or (and (int? %) (odd? %))
                                    (and (string? %) (<= 5 (count %))))))

  (defpred a1 #(and (int? %) (even? %)))
  (defpred b2 (fn [x] (or (ratio? x) (string? x))))
  (defpred c3 #(or (and (int? %) (neg? %))
                   (keyword? %)))

  (data-from-spec {:a a1
                   :b b2
                   :c c3
                   :d a1
                   :e b2
                   :f [a1 b2 c3]} :random)

  (data-from-spec [a1 b2 (list c3 {:x a1 :b b2})] :random)

  (data-from-spec {:a a1
                   :b b2
                   :c c3
                   :d a1
                   :e b2
                   :f [a1 b2 c3]} :canonical)

  (data-from-spec [a1 b2 (list c3 {:x a1 :b b2})] :canonical)

  (def spec-defpred [a1 b2 c3 {:x a1 :y b2 :z c3}])
  (valid-scalars? (data-from-spec spec-defpred :random) spec-defpred)
  )


(defn lazy-seq?
  "Returns `true` if `x` is a lazy sequence."
  {:UUIDv4 #uuid "19c24236-a9fe-4066-87ae-321d0cfb319a"}
  [x]
  (isa? clojure.lang.LazySeq (type x)))