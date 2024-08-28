(ns speculoos.core
  "This namespace provides functions to validate Clojure data. They
  operate on any heterogeneous, arbitrarily-nested data structure.

  Terminology:

  * `data`: A heterogeneous, arbitrarily-nested Clojure data structure that
    represents information.
  * `scalar`: A non-divisible datum, such as a number, string, boolean, etc.
  * `collection`: A composite data structure, such as a vector, list, map, set,
  lazy-sequence, etc., composed of scalars and other collections.
  * `specification`: A human- and machine-readable description of data.
  * `validate`: To systematically apply predicates to datums.
  * `valid`: All datums satisfy their corresponding predicates; more
     specifically, *zero invalid datum-predicate pairs*.
  * `path`: A vector of indexes/keys that uniquely locate a datum.
  * `predicate`: A function that returns `true`/`false`, usually 1-arity, but in
  particular circumstances may be more.
  * `ordinal`: A mode of operation wherein a nested collection's path considers
  only its ordering relative to other collections.

  Remember three mantras:

  1. Validate scalars separately from validating collections.
  2. Make the specification mimic the shape of the data.
  3. Validation ignores un-paired predicates and un-paired datums."
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [fn-in.core :refer [get-in* assoc-in*]]))


(load "/fn_in/collection_hierarchy")


(defn assoc-vector-tail
  "Assign value x to the final existing element of vector v.
   If v is empty, x becomes its sole content."
  {:UUIDv4 #uuid "6dce9a25-5526-42bf-9bd8-162b092d504d"
   :no-doc true}
  [v x]
  (conj (vec (butlast v)) x))


(defn new-accumulator
  "Produce an updated accumulator vector given
   acc the 'old' accumulator vector
   pth the path
   ky the key
   val the value."
  {:UUIDv4 #uuid "22b24bad-3510-46c9-8b7f-fcb957a1c53f"
   :no-doc true}
  [acc pth ky vl] (conj acc {:path (assoc-vector-tail pth ky)
                             :value vl}))

(defn new-path
  "Produce a new path given
   pth the old path
   ky the key."
  {:UUIDv4 #uuid "6237feec-d53c-4478-aa37-b657d0bfa34e"
   :no-doc true}
  [pth ky] (assoc-vector-tail pth ky))


(defn new-path-plus-depth
  "Produce a new path for diving down into a sub-collection, given
  pth the old path
  ky the key."
  {:UUIDv4 #uuid "fb513bfa-a1fa-4598-a8a7-837692fa6c56"
   :no-doc true}
  [pth ky] (conj (new-path pth ky) nil))


(defn clamp
  "Given two sequences `c1` and `c2`, if either
  `(isa? (type _) :speculoos/non-terminating)`, clamp its size at the count of
  the other, stuff its contents into a vector, and return both as
  `[new-c1 new-c2]`. If neither are `:speculoos/non-terminating`, return
  `[c1 c2]` unchanged. Supplying two non-terminating sequences throws.

  Examples:
  ```clojure
  (clamp [:a :b :c :d :e] (range)) ;; => [[:a :b :c :d :e] [0 1 2 3 4]]
  (clamp [] (repeat 42)) ;; ==> [[] []]
  (clamp (iterate dec 0) (list 'foo 'bar 'baz)) ;; => [[0 -1 -2] (foo bar baz)]

  ;; neither non-terminating; args pass through unchanged
  (clamp [1 2 3] (list :a \\z)) ;; [[1 2 3] (:a \\z)]

  ;; sequence is only possibly non-terminating; actual argument `c2` is shorter
  (clamp [1 2 3 4 5] (range 3)) ;; => [[1 2 3 4 5] [0 1 2]]
  ```"
  {:UUIDv4 #uuid "60310b3d-0469-48b0-af04-0009648a3e1e"}
  [c1 c2]
  (cond
    (and (isa? (type c1) ::non-terminating)
         (isa? (type c2) ::non-terminating)) (throw (Exception. "Two collections that are :speculoos/non-terminating were supplied. One must be obviously terminating."))
    (isa? (type c1) ::non-terminating) (vector (vec (take (count c2) c1)) c2)
    (isa? (type c2) ::non-terminating) (vector c1 (vec (take (count c1) c2)))
    :else [c1 c2]))


;;  (ns-unmap *ns* 'all-paths-sub-function) ; (defmulti) macro only binds the var within (when-not), so must manually unmap it to update it
;;  (remove-all-methods all-paths-sub-function)


(defn reduce-indexed
  "Systematically apply `f` to elements of `coll`, carrying an index along with
  the accumulating value, analogous to how
  [map-indexed](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map-indexed)
  relates to [map](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map).
  Function `f` should be a function of 3 arguments: zero-indexed integer, the
  accumulating value, and the next element of `coll`. `coll` may be any Clojure
  collection.

  If `val` is not supplied:

    * `reduce-indexed` returns the result of applying `f` to the first 2 items
    coll, then applying `f` to that result and the 3rd item, etc.
    * If `coll` contains zero items, `f` must accept no arguments as well, and
    `reduce-indexed` returns the result of calling `f` with no arguments.
    * If `coll` has only one item, it is returned and `f` is not called.

  If `val` is supplied:

    * `reduce-indexed` returns the result of applying `f` to `val` and the
      first item in `coll`, then applying `f` to that result and the 2nd item,
      etc.
    * If `coll` contains zero items, returns `val` and `f` is not called.

  Note:

  1. Implemented with only `first`, `next`, etc., and therefore currently makes
     no consideration for performance.
  2. Hash-map and array-map elements are peeled off as instances of
     `clojure.lang.MapEntry`, a pseudo-vector of `[key value]`.
  3. Set elements are consumed in an un-defined order.

  Examples:
  ```clojure
  (reduce-indexed #(conj %2 (vector %1 %3)) [:initial] [:item1 :item2 :item3])
  ;; => [:initial [0 :item1] [1 :item2] [2 :item3]]

  (reduce-indexed #(assoc %2 %3 %1) {:init-val 99} [:a :b :c])
  ;; => {:init-val 99, :a 0, :b 1, :c 2}
  ```"
  {:UUIDv4 #uuid "f40f7bde-c704-47b5-bafe-26874f3e033e"}
  ([f coll]
   (let [s (seq coll)]
     (if s
       (reduce-indexed f (first s) (next s))
       (f))))
  ([f val coll]
   ((fn reduce-i [f idx val coll]
      (if-let [s (seq coll)]
        (reduce-i f
                  (inc idx)
                  (f idx val (first s))
                  (next s))
        val))
    f 0 val coll)))


(defmulti all-paths-sub-function
  "Sub-function in service to (all-paths) to dispatch on collection type."
  {:UUIDv4 #uuid "ca992418-70a4-40f8-9331-a2ebe6130b46"
   :no-doc true}
  (fn [_ _ c _ _] (type c)))


(declare all-paths)


(defmethod all-paths-sub-function ::non-map-entry-collection [ky acc vl pth c-type] (all-paths vl
                                                                                               (new-accumulator acc pth ky vl)
                                                                                               (new-path-plus-depth pth ky)
                                                                                               (type vl)))

(defmethod all-paths-sub-function ::map-entry [ky acc vl pth c-type] (all-paths-sub-function (first vl) acc (second vl) pth c-type))

(defmethod all-paths-sub-function ::non-terminating [ky acc vl pth c-type] (let [acc (new-accumulator acc pth ky vl)
                                                                                 updated-tail (assoc (peek acc) :non-terminating? true)]
                                                                             (conj (pop acc) updated-tail)))

(defmethod all-paths-sub-function :default [ky acc vl pth c-type] (new-accumulator acc pth ky vl))


(defn all-paths
  "Returns a vector of `{:path _ :value _}` to all values in `form`, a
  heterogeneous, arbitrarily-nested data structure, including both scalars
  (e.g., numbers, strings, etc.) and collections (e.g., lists, vectors, maps,
  sets). `paths` are suitable for consumption by [[get-in*]], [[update-in*]],
  [[assoc-in*]], and the like. Outermost root element is located by `MapEntry`
  `[:path []]`.

  Note: The 4-arity version is a recursion target and not intended to be called.

  Examples:
  ```clojure
  ;; vector path elements are zero-indexed integers
  (all-paths [42 :foo 22/7])
  ;; => [{:path [], :value [42 :foo 22/7]}
  ;;     {:path [0], :value 42}
  ;;     {:path [1], :value :foo}
  ;;     {:path [2], :value 22/7}]

  ;; map path elements are keys (often keywords, but not always)
  (all-paths {:a 11 :b 22})
  ;; => [{:path [], :value {:a 11, :b 22}}
  ;;     {:path [:a], :value 11}
  ;;     {:path [:b], :value 22}]

  ;; list path elements are zero-indexed integers
  (all-paths (list 11 22))
  ;; => [{:path [], :value (11 22)}
  ;;     {:path [0], :value 11}
  ;;     {:path [1], :value 22}]

  ;; set path elements are the values themselves
  (all-paths #{:red :blue})
  ;; => [{:path [], :value #{:red :blue}}
  ;;     {:path [:red], :value :red}
  ;;     {:path [:blue], :value :blue}]

  ;; heterogeneous, nested collections; multi-element paths composed of integer indexes and keys
  (all-paths [42 {:a 'foo}])
  ;; => [{:path [], :value [42 {:a foo}]}
  ;;     {:path [0], :value 42}
  ;;     {:path [1], :value {:a foo}}
  ;;     {:path [1 :a], :value foo}]

  (all-paths {:x #{99}})
  ;; => [{:path [], :value {:x #{99}}}
  ;;     {:path [:x], :value #{99}}
  ;;     {:path [:x 99], :value 99}]

  ```"
  {:implementation-details "(reduce-indexed) is intended to transparently consume all collections."
   :UUIDv4 #uuid "f09bd6bf-5d5c-40ab-b8db-647a5a75cd09"}
  ([form]
   (let [base [{:path []
                :value form}]]
     (if (isa? (type form) ::non-terminating)
       (assoc-in* base [0 :non-terminating?] true)
       (all-paths form base [] (type form)))))
  ([form accumulator path container-type]
   (reduce-indexed (fn [ky acc vl] (all-paths-sub-function
                                    (if (isa? container-type ::set) vl ky)
                                    acc vl path container-type))
                   accumulator
                   form)))


(defn only-non-collections
  "Return only path entries that are non-collections"
  {:UUIDv4 #uuid "271a92b5-9dd2-48e6-a7c8-f0d85a8918c8"
   :no-doc true}
  [p]
  (filterv #((complement coll?) (% :value)) p))


(defn only-fns
  "Return only path entries that are functions."
  {:UUIDv4 #uuid "61c7e16c-933e-48bd-b4e3-4810efed731a"
   :no-doc true}
  [p]
  (filterv #(fn? (:value %)) p))


(defn intersection-of-paths
  "Return only the paths in both coll-1 and coll-2"
  {:UUIDv4 #uuid "07b1c360-28b1-4ee7-a568-b161cb82b981"
   :no-doc true}
  [coll-1 coll-2]
  (let [paths-1 (reduce #(conj %1 (:path %2)) #{} coll-1)
        paths-2 (reduce #(conj %1 (:path %2)) #{} coll-2)]
    (set/intersection paths-1 paths-2)))


(defn intersection-of-paths-3
  "Returns only the paths in all coll-1, coll-2, and coll-3"
  {:UUIDv4 #uuid "938e1a91-b32c-4469-862c-d8777b567ee9"
   :no-doc true}
  [coll-1 coll-2 coll-3]
  (let [paths-1 (reduce #(conj %1 (:path %2)) #{} coll-1)
        paths-2 (reduce #(conj %1 (:path %2)) #{} coll-2)
        paths-3 (reduce #(conj %1 (:path %2)) #{} coll-3)]
    (set/intersection paths-1 paths-2 paths-3)))


(defn only-sets
  "Returns only paths to sets."
  {:UUIDv4 #uuid "dc574210-67db-4873-8a8c-1485baace34d"
   :no-doc true}
  [p]
  (filterv #(set? (:value %)) p))


(defn only-non-terminating
  "Returns only paths to non-terminating sequences.
   clojure.lang.{Cycle,Iterate,LazySeq,LongRange,Range,Repeat}"
  {:UUIDv4 #uuid "1b5a761b-5917-4c4e-bebd-6cf81365bbec"
   :no-doc true}
  [p]
  (filterv #(true? (:non-terminating? %)) p))


;;;; Scalar Specs


(declare validate-scalars)
(declare expand-and-clamp)


(defn validate-set-as-predicate
  "For all non-collection datums in data for which there is a corresponding set in spec,
   use the spec as a predicate to validate the datum."
  {:UUIDv4 #uuid "ee04739f-fbe9-486d-a497-02ea36204632"
   :no-doc true}
  ([data spec] (validate-set-as-predicate data spec []))
  ([data spec accumulator]
   (let [[clamped-data clamped-spec] (expand-and-clamp data spec)
         paths-to-sets-in-spec (reduce #(conj %1 (:path %2)) #{} (only-sets (all-paths clamped-spec)))
         non-sets-in-data (reduce #(if (and (paths-to-sets-in-spec (:path %2))
                                            ((complement coll?) (:value %2)))
                                     (conj %1 %2)
                                     %1)
                                  []
                                  (all-paths clamped-data))]
     (reduce #(conj %1 {:path (:path %2)
                        :datum (:value %2)
                        :predicate (get-in* clamped-spec (:path %2))
                        :valid? ((get-in* clamped-spec (:path %2)) (:value %2))})
             accumulator
             non-sets-in-data))))


(defn apply-all-predicates-within-spec-set
  "Helper function for (validate-set-elements). Applies every predicate
  contained within spec-set to every scalar within data-set. Returns
  validation maps with keys :path, :datums-set, :predicate, :valid?, one only
  for each predicate. Order of validation results is an implementation detail
  and is not guaranteed."
  {:UUIDv4 #uuid "92b8993c-d591-470c-8b6b-bc3e992450de"
   :no-doc true}
  [data-set spec-set path]
  (if (and (not-empty data-set) (not-empty spec-set))
    (reduce (fn [acc pred] (conj acc {:path path
                                      :datums-set data-set
                                      :predicate pred
                                      :valid? (every? pred data-set)}))
            []
            spec-set)))


(defn validate-set-elements
  "For each corresponding set in both data and spec, apply every predicate
   *contained within* the spec set to each scalar of the set in data.
   Uses (every?), so :valid? is true iff all elements of the set in data satisfy
   each predicate. Validating sets a whole (e.g., count, membership, etc.)
   properly belongs in a collction specification."
  {:UUIDv4 #uuid "5f9b3413-1e54-41b5-a447-c9aa0e4b412d"
   :no-doc true}
  ([data spec] (validate-set-elements data spec []))
  ([data spec accumulator]
   (let [[clamped-data clamped-spec] (expand-and-clamp data spec)
         data-paths (only-sets (all-paths clamped-data))
         spec-paths (only-sets (all-paths clamped-spec))
         data-spec-intersec (intersection-of-paths data-paths spec-paths)]
     (reduce (fn [acc dpath] (if (data-spec-intersec (:path dpath))
                               (let [data-set (get-in* clamped-data (:path dpath))
                                     spec-set (get-in* clamped-spec (:path dpath))]
                                 (concat acc (apply-all-predicates-within-spec-set data-set spec-set (:path dpath))))
                               acc))
             (validate-set-as-predicate data spec accumulator)
             data-paths))))


(defn validate-non-terminating-sequence-elements
  "For each corresponding (possibly) non-terminating sequence (i.e.,
   clojure.lang.{Cycle,Iterate,LazySeq,LongRange,Range,Repeat}), in either data
   or spec, 'clamp' the length of the non-terminating sequence at the length of
   the other, then apply the scalar predicates contained in spec to the values
   in data."
  {:UUIDv4 #uuid "4b5f0668-2249-41ac-a840-ea07ecc6815a"
   :candidate-for-deletion true
   :no-doc true}
  [data spec]
  (let [data-paths (only-non-terminating (all-paths data))
        spec-paths (only-non-terminating (all-paths spec))
        data-spec-union (concat data-paths spec-paths)]
    (reduce (fn [acc upath] (let [[clamped-data clamped-spec] (clamp (get-in* data (:path upath))
                                                                     (get-in* spec (:path upath)))]
                              (conj acc (validate-scalars clamped-data clamped-spec))))
            []
            data-spec-union)))


(defn any-non-terminating-paths?
  "Given an all-paths vector v, return entries that contain
   {:non-terminating? true}, nil otherwise."
  {:UUIDv4 #uuid "cf9763be-3fb3-4711-b264-4e30edb2711f"
   :no-doc true}
  [v]
  (filter #(true? (:non-terminating? %)) v))


(defn expand-and-clamp-1
  "Helper function for (expand-and-clamp) that expands+clamps c1 versus c2.
   Returns a version of c1 that has all non-terminating sequences 'clamped'
   at the length of the corresponding element in c2."
  {:UUIDv4 #uuid "85abeee6-990e-4375-ac96-e2a8c8552865"
   :no-doc true}
  [c1 c2]
  (let [paths-1 (all-paths c1)
        first-non-terminating (first (any-non-terminating-paths? paths-1))]
    (if first-non-terminating
      (let [sibling-val (get-in* c2 (:path first-non-terminating))
            [clamped-val _] (clamp (:value first-non-terminating) sibling-val)
            path (:path first-non-terminating)
            clamped-data (if (empty? path)
                           clamped-val
                           (assoc-in* c1 path clamped-val))]
        (expand-and-clamp-1 clamped-data c2))
      c1)))


(defn expand-and-clamp
  "Given two heterogeneous, arbitrarily-nested collections coll-1 and coll-2,
   for each in turn, expand the non-terminating sequences, and clamp them at the
   size of the corresponding terminating sequence in the other collection.
   Returns [clamped-coll-1 clamped-coll-2].

   (all-paths) operates on only a single collection at one time and is
   agnositc towards (possibly) non-terminating sequences. It merely tags them
   with {:non-terminating? true} and continues without further processing. Only
   with the context provided by a second collection is it possible to make a
   decision about how many elements to take from a non-terminating sequence."
  {:UUIDv4 #uuid "b935b4de-f8c2-4ea1-8a3d-d1bbef81c0ed"
   :no-doc true}
  [coll-1 coll-2]
  (vector (expand-and-clamp-1 coll-1 coll-2)
          (expand-and-clamp-1 coll-2 coll-1)))


(defn regex?
  "Returns true if x is an instance of java.util.regex.Pattern."
  {:UUIDv4 #uuid "2ac23a57-cb91-411e-8085-c764f752d6c4"
   :no-doc true}
  [x]
  (instance? java.util.regex.Pattern x))


(def ^:dynamic ^:no-doc *notice-on-validation-bare-scalar* true)


(defn- prerr
  "println to `*err*`."
  {:UUIDv4 #uuid "2eab623f-8073-40ea-9a6a-d19d6a94f60f"
   :no-doc true}
  [& args]
  (binding [*out* *err*]
    (apply println args)))


(defn validate-bare-scalar
  "**Undocumented Feature**

  Returns result of applying `predicate` to `scalar`, a non-collection.
  `predicate` is a either a 1-arity function that returns truthy/falsey, a set,
  or a regex. Validation result is a vector containining a hash-map
  `{:path nil :datum _ :predicate _ :valid? _ :speculoos/undocumented-feature <notice>}`
  that mimics the return of [[validate-scalars]]. Key `:path` is associated to
  nil to indicate that scalar and predicate are not contained in a collection.

  Bind `*notice-on-validation-bare-scalar*` to `false` to suppress notices to
  *out* and associated key-val.

  ```clojure
  (validate-bare-scalar 42 int?)
  ;; => [{:path nil, :datum 42, :predicate #function[clojure.core/int?], :valid? true, :speculoos/undocumented-feature \"See *err* for details\"}]

  (binding [*notice-on-validation-bare-scalar* false]
    (validate-bare-scalar :red #{:red :green :blue}))
  ;; => [{:path nil, :datum :red, :predicate #{:green :red :blue}, :valid? :red}]

  (binding [*notice-on-validation-bare-scalar* false]
    (validate-bare-scalar \"abc\" #\"a.c\"))
  ;; => [{:path nil, :datum \"abc\", :predicate #\"a.c\", :valid? \"abc\"}]
  ```"
  {:UUIDv4 #uuid "b7604d74-55e7-4083-b73a-d243d91490d6"
   :no-doc true}
  [scalar predicate]
  (let [result [{:path nil
                 :datum scalar
                 :predicate predicate
                 :valid? (if (regex? predicate)
                           (re-matches predicate scalar)
                           (predicate scalar))}]
        short-notice "See *err* for details."
        long-notice "Validating 'bare' scalars with 'bare' predicates is an undocumented feature provided for convenience, but is not guarnanteed in future versions. Bind *notice-on-validation-bare-scalar* to false to suppress this notice and associated key-val of the returned map."]
    (if *notice-on-validation-bare-scalar*
      (do
        (prerr long-notice)
        (assoc-in result [0 :speculoos/undocumented-feature] short-notice))
      result)))


(defn validate-scalars-dispatch
  "Dispatch target for `validate-scalars` multimethod."
  {:UUIDv4 #uuid "da127bee-6f28-4943-a76c-6bb2b0819ca6"
   :no-doc true
   :implementation-notes
   "Algorithm: Generate [all-paths] for both data and spec.
               Calc the intersection of their path elements (i.e., do not attempt to validate unless there is both a datum and predicate at the same address).
               Cycle through data's paths, if it's in the intersection set, then conj on a map of {:path :datum :predicat :valid?},
                                           otherwise, continue on to data's next path element.
               Sets are handled in the beginning by initializing the accumulator vector with the results of (validate-set-elements).
               Then, non-terminating sets are clamped, coerced to vectors, and then validated."
   :implementation-comments "Making the sets+predicate results as the initial accumulator state is clean,
                            but for visual inspection, I'd prefer them to be at the end."}
  [data spec]
  (let [[clamped-data clamped-spec] (expand-and-clamp data spec)
        data-paths (only-non-collections (all-paths clamped-data))
        spec-paths (only-non-collections (all-paths clamped-spec))
        data-spec-intersec (intersection-of-paths data-paths spec-paths)]
    (reduce (fn [acc dpath] (if (data-spec-intersec (:path dpath))
                              (let [datum (get-in* data (:path dpath))
                                    predicate (get-in* spec (:path dpath))]
                                (conj acc {:path (:path dpath)
                                           :datum datum
                                           :predicate predicate
                                           :valid? (if (regex? predicate)
                                                     (re-matches predicate datum)
                                                     (predicate datum))}))
                              acc))
            (validate-set-elements data spec)
            data-paths)))


(defmulti validate-scalars
  "Returns a sequence of `{:path _ :datum _ :predicate _ :valid? _}` hash-maps
  for every **scalar** datum in `data` with a corresponding predicate in
  scalar specification `spec`. `data` is a heterogeneous, arbitrarily-nested
  data structure of arbitrary values. `spec` is a corresponding 'shape', i.e.,
  all nested structures are of the same type and length, containing predicates
  to test against. `validate-scalars` recursively descends into all nested
  collections. Only validates complete datum-predicate pairs, i.e., only nodes
  that are in both `data` and in `spec`. See [[valid-scalars?]] and
  [[thoroughly-valid-scalars?]] for high-level summaries of scalar validation.

  * `:path` is a vector suitable for sending to [[get-in*]], [[assoc-in*]],
    [[update-in*]], and friends.
  * `:datum` is the scalar entity in `data`.
  * `:predicate` is a 1-arity function-like thing that returns truthy/falsey,
    i.e, regexes and sets may serve as predicates.
  * `:valid?` is the result of invoking the predicate with the scalar datum.

  The ordering of results is an implementation detail and not specified.

  Remember two mantras:

  1. Shape your specification (mostly) like your data.
  2. Validation looks at *either* scalars *or* collections, not both.
     `validate-scalars` only looks at scalars.

  Examples:
  ```clojure
  (validate-scalars [42   :foo     \\c   ] ;; <-- data
                    [int? keyword? char?]) ;; <-- specification
  ;; => [{:path [0], :datum 42, :predicate int?, :valid? true}
  ;;     {:path [1], :datum :foo, :predicate keyword?, :valid? true}
  ;;     {:path [2], :datum \\c, :predicate char?, :valid? true}]

  (validate-scalars {:a 42    :b 'foo  }  ;; <-- data
                    {:a int? :b symbol?}) ;; <-- specification
  ;; => [{:path [:a], :datum 42, :predicate int?, :valid? true}
  ;;     {:path [:b], :datum foo, :predicate symbol?, :valid? true}]

  ;; nested data and specification
  (validate-scalars [42     {:z 'baz}    ]
                    [ratio? {:z keyword?}])
  ;; => [{:path [0], :datum 42, :predicate ratio?, :valid? false}
  ;;     {:path [1 :z], :datum baz, :predicate keyword?], :valid? false}]

  ;; data and specification not same length
  (validate-scalars [42 :foo 22/7]
                    [int?        ])
  ;; => [{:path [0], :datum 42, :predicate int?, :valid? true}]

  (validate-scalars [42                     ]
                    [decimal? keyword? char?])
  ;; => [{:path [0], :datum 42, :predicate decimal?, :valid? false}]

  ;; regular expression predicate
  (validate-scalars [ \"foo\"]
                    [#\"f..\"])
  ;; => [{:path [0], :datum \"foo\", :predicate #\"f..\", :valid? \"foo\"}]

  ;; set as a membership predicate
  (validate-scalars [:green              ]
                    [#{:red :green :blue}])
  ;; => [{:path [0], :datum :green, :predicate #{:green :red :blue}, :valid? :green}]
  ```

  Within a scalar specification, a bare regular expression literal `#\"...\"` is
  automatically treated as `#(re-matches #\"...\")`.

  ```clojure
  (validate-scalars [\"abc\" \"xyz\"]
                    [#\"a.c\" #\"^[wxyz]{3}$\"])
  ;; => [{:path [0], :datum \"abc\", :predicate #\"a.c\", :valid? \"abc\"}
  ;;     {:path [1], :datum \"xyz\", :predicate #\"^[wxyz]{3}$\", :valid? \"xyz\"}]
  ```

  Within a scalar specification, a set is treated as a membership predicate when
  the data at that same path in the data contains a scalar...

  ```clojure
  (validate-scalars [11    :red]
                    [int? #{:blue :green :red}]) ;; <-- set in specification, but not in data
  ;; => [{:path [1], :datum :red, :predicate #{:green :red :blue}, :valid? :red}
  ;;     {:path [0], :datum 11, :predicate int?, :valid? true}]
  ```

  ... whereas a set in the scalar specification at the same path as a set in the
  data is treated as a regular nested collection: The set mimics a container in
  the data. Any predicates within the specification set are applied to *all*
  scalars contained in the data as if `#(every? keyword? %)`. The key is changed
  from `:datum` to `:datums-set` to emphasize this behavior.

  ```clojure
  (validate-scalars [11   #{:tea :coffee :water}]  ;; <-- sets in both data...
                    [int? #{:keyword?}          ]) ;; <-- ... and in specification
  ;; => ({:path [0], :datum 11, :predicate #function[clojure.core/int?], :valid? true}
  ;;     {:path [1], :datums-set #{:coffee :tea :water}, :predicate :keyword?, :valid? false})
  ```

  Non-terminating sequences in `data` are acceptable as long as the
  corresponding sequence (i.e., at the same path) in `spec` terminates, and
  _vice versa_.
  ```clojure
  (validate-scalars (cycle [42 'foo 22/7])   ;; <-- data is an infinite sequence
                    [int? keyword? ratio?])  ;; <-- specification terminates
  ;; => [{:path [0], :datum 42, :predicate int?, :valid? true}
  ;;     {:path [1], :datum foo, :predicate keyword?, :valid? false}
  ;;     {:path [2], :datum 22/7, :predicate ratio?, :valid? true}]

  (validate-scalars [11 22 33]     ;; <-- data terminates
                    (repeat int?)) ;; <-- specification is an infinite sequence
  ;; => [{:path [0], :datum 11, :predicate int?, :valid? true}
  ;;     {:path [1], :datum 22, :predicate int?, :valid? true}
  ;;     {:path [2], :datum 33, :predicate int?, :valid? true}]
  ```

  Overview of the algorithm.

  1. Run [[all-paths]] on the data.
      ```clojure
      ;; data is a three-element vector composed of an integer, a symbol, and a ratio
      (all-paths [42 'foo 22/7])
      ;; => [{:path [], :value [42 foo 22/7]}
      ;;     {:path [0], :value 42}
      ;;     {:path [1], :value foo}
      ;;     {:path [2], :value 22/7}]

      ;; Four total elements: one vector and three scalars.
      ```

  2. Run [[all-paths]] on the specification.
       ```clojure
      ;; specification is a two-element vector composed of an `int?` predicate and a `keyword?` predicate
      ;; will only validate first and second elements of data
      (all-paths [int? keyword?])
      ;; => [{:path [], :value [int? keyword?]}
      ;;     {:path [0], :value int?}
      ;;     {:path [1], :value keyword?}

      ;; Three total elements: one vector and two scalars (i.e., predicate functions).
      ```

  3. Remove collections elements from each result.
      ```clojure
      ;; remove non-collections from data's all-paths sequence
      (only-non-collections [{:path [], :value [42 'foo 22/7]}
                             {:path [0], :value 42}
                             {:path [1], :value 'foo}
                             {:path [2], :value 22/7}])
      ;; => [{:path [0], :value 42}
      ;;     {:path [1], :value foo}
      ;;     {:path [2], :value 22/7}]

      ;; remove non-collections from specification's all-paths sequence
      (only-non-collections [{:path [], :value [int? keyword?]}
                             {:path [0], :value int?}
                             {:path [1], :value keyword?}])
      ;; => [{:path [0], :value int?}
      ;;     {:path [1], :value keyword?}]
      ```

  4. Remove from data scalars that lack a predicate in the specification.
      ```clojure
      ;; => [{:path [0], :value 42}
      ;;     {:path [1], :value foo}

      ;; third element of data vector does not have a corresponding predicate
      ```

  5. Remove from specification predicates that lack a scalar in the data.
      ```clojure
      ;; => [{:path [0], :value int?}
      ;;     {:path [1], :value keyword?}]

      ;; both predicates contained in specification have a corresponding scalar
      ```

  6. For each scalar-predicate pair, apply the predicate to the scalar.
      ```clojure
      (int? 42) ;; => true
      (keyword? 'foo) ;; => false

      ;; or, all at once
      (validate-scalars [42   'foo     22/7]  ;; <-- data, a three-element vector
                        [int? keyword?     ]) ;; <-- specification, a two-element vector
      ;; [{:path [0], :datum 42, :predicate int?, :valid? true}
          {:path [1], :datum foo, :predicate keyword?, :valid? false}]
      ```"
  {:UUIDv4 #uuid "50c58e7b-27cc-4d0a-b367-e740166a4e35"}
  (fn [data spec] [(coll? data) (coll? spec)]))


#_ (ns-unmap *ns* 'validate-scalars) ;; dev time convenience


(defmethod validate-scalars [false false] [scalar predicate] (validate-bare-scalar scalar predicate))
(defmethod validate-scalars [false true] [scalar predicate] (validate-bare-scalar scalar predicate))
(defmethod validate-scalars [true false] [_ _] []) ;; supply an empty seq for combo-valid? in the cases when `spec` is nil
(defmethod validate-scalars [true true] [data spec] (validate-scalars-dispatch data spec))
#_(defmethod validate-scalars :default [_ _] (println "Invalid args: `data` and `spec` must both be collections.")) ;; fail with stack trace


(defn dual-validate-scalars
  "Analogous to (validate-scalars), but with two input data sets,
   data-1 and data-2. The 'predicates' in spec must accept two arguments, an
   element from each respective data set. data-1 is priveledged in that it is
   used in the 'clamp' procedure with spec.
   Note: Sets are not validated, in constrast to (validate-scalars)."
  {:UUIDv4 #uuid "205476af-591d-4403-b28a-f3d260d1ef5b"
   :no-doc true}
  [data-1 data-2 spec]
  (let [[clamped-data-1 clamped-spec] (expand-and-clamp data-1 spec)
        [clamped-data-2 _] (expand-and-clamp data-2 spec)
        data-1-paths (only-non-collections (all-paths clamped-data-1))
        data-2-paths (only-non-collections (all-paths clamped-data-2))
        spec-paths (only-non-collections (all-paths clamped-spec))
        data-spec-intersec (intersection-of-paths-3 data-1-paths data-2-paths spec-paths)]
    (reduce (fn [acc dpath] (if (data-spec-intersec (:path dpath))
                              (let [datum-1 (get-in* data-1 (:path dpath))
                                    datum-2 (get-in* data-2 (:path dpath))
                                    predicate (get-in* spec (:path dpath))]
                                (conj acc {:path (:path dpath)
                                           :datum-1 datum-1
                                           :datum-2 datum-2
                                           :predicate predicate
                                           :valid? (predicate datum-1 datum-2)}))
                              acc))
            [] ;;(validate-set-elements data spec)
            data-1-paths)))


(defn truthy
  {:UUIDv4 #uuid "ee872c9f-73da-4a26-b572-31e562727c3a"
   :no-doc true}
  [x] x)


(defn falsey
  {:UUIDv4 #uuid "60d46aad-11f2-4077-97f2-2ebb97e1d2b0"
   :no-doc true}
  [x] (not x))


(defn filter-validation
  "When flt set to truthy, returns only valid (predicate datum) entries.
   When flt set to false, returns only invalid (predicate datum) entries."
  {:UUIDv4 #uuid "44ff3d1c-ed7b-47f4-bca6-d8bbbaf699e4"
   :no-doc true}
  [validations flt]
  (filter #(flt (:valid? %)) validations))


(defn only-valid
  "Returns only validation entries where `:datum` satisfies `:predicate`, i.e.,
  `:valid?` is neither `false` nor `nil`.

  Examples:
  ```clojure
  (only-valid (validate-scalars [42       :foo    22/7  ]
                                [decimal? symbol? ratio?]))
  ;; => ({:path [2], :datum 22/7, :predicate ratio?, :valid? true})

  (only-valid (validate-collections [42    (list :foo)]
                                    [list? [list?]    ]))
  ;; => ({:path [1 0], :value list?, :datum (:foo), :ordinal-parent-path [0], :valid? true})
  ```"
  {:UUIDv4 #uuid "7f5912e8-52e5-4aab-820a-3bac2739cc65"}
  [validations]
  (filter-validation validations truthy))


(defn only-invalid
  "Returns only validation entries where `:datum` does **not** satisfy
  `:predicate`, i.e., `:valid?` is `false` or `nil`.

  Examples:
  ```clojure
  (only-invalid (validate-scalars [42   :foo     22/7   ]
                                  [int? keyword? symbol?]))
  ;; => ({:path [2], :datum 22/7, :predicate symbol?, :valid? false})

  (only-invalid (validate-collections [42    (list :foo)]
                                      [list? [list?]    ]))
  ;; => ({:path [0], :value list?, :datum [42 (:foo)], :ordinal-parent-path [], :valid? false})
  ```"
  {:UUIDv4 #uuid "8c1a8a78-20b4-473f-ade8-b09804c92a31"}
  [validations]
  (filter-validation validations falsey))


(defn valid-scalars?
  "Following validation with [[validate-scalars]], returns `true` if every
  **scalar** element in `data` satisfies every corresponding predicate in scalar
  specification `spec`, `false` otherwise.

  Note: `valid-scalars?` returns `true` if validation returns zero
  `{:valid? falsey}` results.

  Note: If a corresponding specification predicate does not exist, that element
  of data will not be checked. Use [[scalars-without-predicates]] to locate
  elements of `data` that lack corresponding predicates in `spec`. Use
  [[thoroughly-valid-scalars?]] to require that every scalar in `data` is
  validated.

  See [[validate-scalars]] for details on the mechanics of scalar validation.

  Examples:
  ```clojure
  (valid-scalars? [42   :foo     22/7  ]  ;; <-- data
                  [int? keyword? ratio?]) ;; <-- specification
  ;; => true

  (valid-scalars? {:a 42 :b 'foo}
                  {:a string? :b symbol?}) ;; => false

  ;; un-paired datums
  (valid-scalars? [42 :foo 22/7]
                  [int?        ]) ;; => true

  ;; un-paried predicates
  (valid-scalars? {:a 42     }
                  {:b symbol?}) ;; => true
  ```"
  {:UUIDv4 #uuid "563d6131-b607-46d3-8a16-a7d2e249e288"}
  [data spec]
  (empty? (only-invalid (validate-scalars data spec))))


(defn valid-dual-scalar-spec?
  "`true` if each element in `data-1` and `data-2` both satisfy corresponding
  predicate in specification `spec`. Analgous to [[valid-scalars?]]."
  {:UUIDv4 #uuid "74043f31-8afc-4b65-a8ed-8aa372a0c9bf"
   :no-doc true}
  [data-1 data-2 spec]
  (let [validate-results (dual-validate-scalars data-1 data-2 spec)
        invalids (only-invalid validate-results)]
    (empty? invalids)))


;;;; Collection Specs


(defn only-colls
  "Returns only the elements that satisfy coll?.
   Relevant to only vectors and lists."
  {:UUIDv4 #uuid "80040732-caa8-4da1-8d98-a10ec3f0cc00"
   :no-doc true}
  [x]
  (let [filtered (into (empty x) (filter coll? x))]
    (if (list? x)
      (reverse filtered)
      filtered)))


(comment ;; dev-time convenience functions
;;  (ns-unmap *ns* 'ordinal-get) ; (defmulti) macro only binds the var within (when-not), so must manually unmap it to update it
;;  (remove-all-methods ordinal-get)
  )

(defmulti ordinal-get
  "`(ordinal-get coll i)`

  Performs the same task as [[get*]], but when encountering a vector or list,
  considers only elements that are collections. The element is addressed by an
  _ordinal path_. Map elements are addressed by keys, as usual. (Keys may
  themselves be integers, or a composite value.) Set elements are addressed by
  their identities.

  Examples:
  ```clojure
  (ordinal-get [11 [22] 33 [44] [55] 66 [77]] 0) ;; => [22]
  (ordinal-get [11 [22] 33 [44] [55] 66 [77]] 2) ;; => [55]
  (ordinal-get [11 [22] 33 [44] [55] 66 [77]] 3) ;; => [77]
  ```"
  {:UUIDv4 #uuid "999b4364-f7f5-4508-9fa9-1d3ddd0cd73b"}
  (fn [c _] (type c)))


(defmethod ordinal-get ::map [c i] (c i))
(defmethod ordinal-get ::vector [c i] (get (only-colls c) i))
(defmethod ordinal-get ::list [c i] (nth (only-colls c) i nil))
(defmethod ordinal-get ::set [c x] (if (c x) x nil))
(defmethod ordinal-get nil [c i] nil)


(defn ordinal-get-in
  "A [[get-in*]] that, when encountering a vector or list, considers only
  elements that are collections. Map and set addresses work as usual.

  Examples:
  ```clojure
  (ordinal-get-in [42 [:foo] 99 [:bar] 33 [:baz]] [2]) ;; => [:baz]
  (ordinal-get-in {:a [[42] [77] ['hello]]} [:a 2]) ;; => [hello]
  ```"
  {:UUIDv4 #uuid "e7b0c1ab-4e81-4058-976a-b0df6d74b115"}
  [c keys]
  (reduce ordinal-get c keys))

;; perhaps don't need these following two...
;;(defn ordinal-update-in [])
;;(defn ordinal-assoc-in [])


(defn parent-literal-path
  "Given an element's literal path, returns the parent's literal path.
   If the element is directly contained in the root container, then [] is
   returned as the root path."
  {:UUIDv4 #uuid "43f74037-2153-4bfc-964f-8eb44617ffa2"
   :no-doc true}
  [lit-path]
  (if-let [p (butlast lit-path)] (into [] p) []))


(defn find-ordinal
  "Returns the zero-based index of the final element, counting only elements that satisfy (coll?)."
  {:UUIDv4 #uuid "c382c2ee-31e3-47f0-8b92-e51d6c1545c3"
   :no-doc true}
  [d]
  (- (count (filter coll? d)) 1))


(defn sub
  "Returns a sequence of items in a sequence from start (inclusive) to end
  (exclusive). If end is not supplied, defaults to (count s). No performance
  guarantees."
  {:UUIDv4 #uuid "ef337156-a0e1-48b4-b6fb-8b8ebb208b6b"
   :no-doc true}
  ([s start] (sub s start (count s)))
  ([s start end] (keep-indexed #(if (and (<= start %1)
                                         (< %1 end)) %2) s)))

(defn head-of-path
  "Returns portion of a path up to depth."
  {:UUIDv4 #uuid "6a9c9ad2-603a-484c-8169-2465a0d2b2bf"
   :no-doc true}
  [path depth]
  (subvec path 0 depth))


(defn container-at-this-depth
  "Return the containing collection at this depth, addressed by the nth element of the path, including the root address.
   For example, a path of [4 3 1 0] at a 'depth' of the third element, would yield the container collection at [4 3]."
  {:UUIDv4 #uuid "536d0a74-00db-4778-a523-4275e41d0eee"
   :no-doc true}
  [form path depth]
  (get-in* form (head-of-path path depth)))


(defn chop-container-at-target
  "Truncate the container at the target zero-based index."
  {:UUIDv4 #uuid "896dd454-9779-4b2a-a8d3-33d38597f874"
   :no-doc true}
  [form target-idx]
  (sub form 0 (+ 1 target-idx)))


(defn target-index
  "Return element within literal path at the index."
  {:UUIDv4 #uuid "f9972286-8c46-4096-86f6-7264b14cff27"
   :no-doc true}
  [path-literal idx]
  (path-literal idx))


(defn ordinal-at-a-depth
  "Returns ordinal path component, given a form and a literal path to a target element at depth.
   For example, for form [11 [22] 33 [44 [55]]] and a target element of 55, the literal path is
   [3 1 0]. The containing element [44 [55]] is at a depth of 3 from the root. At that depth,
   [44 [55]] is the second collection, thus the ordinal at that depth is returned as 2.
   This function provides one compenent while building up an ordinal path."
  {:UUIDv4 #uuid "a20d775e-b7c2-4f1c-8620-ffbfee1bb6d1"
   :no-doc true}
  [form literal-path depth]
  (let [container (container-at-this-depth form literal-path depth)]
    (if (or (vector? container)
            (list? container))
      (-> form
          (container-at-this-depth literal-path depth)
          (chop-container-at-target (target-index literal-path depth))
          (find-ordinal))
      (target-index literal-path depth))))


(defn ordinal-path-of-parent
  "Given a heterogeneous, arbitrarily-nested data structure form and an absolute, literal path (get-in, update-in, etc style),
   returns an 'ordinal path' of the containing parent, where the non-key indexes represent the ordering (zero-based) of
   the nested structures, ignoring the non-coll elements. The results may be checked with
   (ordinal-get-in form (ordinal-path-of-parent literal-path)

   This 'ordinal path' is used to apply a predicate element in a collection spec to the corresponding containing parent coll
   in a target data set. [Note: The ordinal path may be calculated with no knowledge of that target data set.]

   For example, given [:a :b [:c] :d [[:e]]], element :e is located at the literal path [4 0 0], which is valid for providing to (get-in).
   But since it is contained in the one-th sub-vector of the root structure, then the zero-th element of the sub-sub-vector, its
   parent's ordinal path is [1 0].

   Only vectors and lists have ordinal paths that might differ from their literal paths.
   Map elements are stable-ly and keyed un-ordered.
   Set elements are addressed by their identities."
  {:UUIDv4 #uuid "83ee4f61-8943-481c-a4c9-2de5f1724e63"
   :implementation-details "I don't love how this (ordinal-path-of-parent) and (ordinal-at-a-depth) calc and
                            re-calc the depth and back-calculate the target-index, but I found it useful to be
                            able to tap-in to an individual line of code."
   :no-doc true}
  [form literal-path]
  (let [plp (parent-literal-path literal-path)
        ticker (range (count plp))]
    (into [] (map #(conj (ordinal-at-a-depth form plp %)) ticker))))


(defn apply-one-coll-spec
  "Given data, an arbitrarily-nested, heterogeneous collection, apply the predicate located within coll-spec at literal path."
  {:UUIDv4 #uuid "76d2c938-c59c-43a8-91df-f68d440e57ee"
   :no-doc true}
  [data coll-spec literal-path]
  (let [pred (get-in* coll-spec literal-path)
        datum (ordinal-get-in data (ordinal-path-of-parent coll-spec literal-path))
        ordinal-parent-path  (ordinal-path-of-parent coll-spec literal-path)]
    {:datum datum
     :ordinal-parent-path ordinal-parent-path
     :valid? (pred datum)}))


(defn re-key
  "Re-name keys of [[validate-collections]] output `val-out` to be consistent
  with the output of [[validate-scalars]], and also more understandable. Did not
  re-name keys during processing to retain internal consistency."
  {:UUIDv4 #uuid "71d59178-9a45-44b1-b02c-8a715786c1b0"
   :no-doc true}
  [val-out]
  (map #(clojure.set/rename-keys % {:path :path-predicate
                                    :value :predicate
                                    :ordinal-parent-path :ordinal-path-datum})
       val-out))


(defn partition-between
  "Applies `pred` to successive values in `coll`, splitting it each time
  `(pred prev-item item)` returns logical `true`.

  Unlike original, this version is hollowed out to not return a transducer when
  no collection is provided.

  Adapted from [James Reeves' Medley](https://github.com/weavejester/medley)
  [source](https://github.com/weavejester/medley/blob/1.8.0/src/medley/core.cljc#L443)"
  {:added "1.7.0"
   :UUIDv4 #uuid "34793280-6d1c-4bf1-beb7-bf53d5fe506a"
   :no-doc true}
  [pred coll]
  (lazy-seq
   (letfn [(take-part [prev coll]
             (lazy-seq
              (when-let [[x & xs] (seq coll)]
                (when-not (pred prev x)
                  (cons x (take-part x xs))))))]
     (when-let [[x & xs] (seq coll)]
       (let [run (take-part x xs)]
         (cons (cons x run)
               (partition-between pred
                                  (lazy-seq (drop (count run) xs)))))))))


(defn partition-after
  "Returns a lazy sequence of partitions, splitting after `(pred item)` returns
  true. Unlike original, this version does not return a transducer when no
  collection is provided.

  Adapted from [James Reeves' Medley](https://github.com/weavejester/medley)
  [source](https://github.com/weavejester/medley/blob/1.8.0/src/medley/core.cljc#L485)"
  {:added "1.5.0"
   :UUIDv4 #uuid "cb373c2e-a28f-412d-af05-073f9157f17a"
   :no-doc true}
  [pred coll]
  (partition-between (fn [x _] (pred x)) coll))


(defn flatten-one-level
  "Given sequence `v` containing strictly collections, 'flatten', but only one
  level deep."
  {:UUIDv4 #uuid "6066f9b5-0925-43ad-9362-4c475555bd1d"
   :no-doc true}
  [v]
  (apply concat v))


(defn recover-literal-path-1
  "Given collection `form` and ordinal index `ord-idx`, returns the literal path
  index to the nested child collection addressed by `ord-idx`. Only operates on
  sequences; maps and sets are passed through."
  {:UUIDv4 #uuid "232e6aa0-7305-4d7b-86c5-6e235042cdc2"
   :no-doc true}
  [form ord-idx]
  (if (sequential? form)
    (->> form
         (partition-after coll?)
         (take (inc ord-idx))
         flatten-one-level
         count
         dec)
    ord-idx))


(defn recover-literal-path
  "Given heterogeneous, arbitrarily-nested data structure `form` and ordinal
  collection path `ord-path`, returns the literal path to the nested child
  collection.

  Examples:
  ```clojure
  (recover-literal-path [11 [22] 33 [44] 55 [66]] [2])
  ;; => [5]

  (recover-literal-path {:a {:b [11 [22] 33 [44]]}}
                        [:a :b 1])
  ;; => [:a :b 3]

  (recover-literal-path (list 11 22 [33 [44] [55] [66]])
                        [1 2])
  ;; => [2 3]
  ```"
  {:UUIDv4 #uuid "d892eda9-4369-432c-b0c4-2c2b3ee929b7"}
  [form ord-path]
  (reduce
   (fn [accumulating-literal-path ordinal-index]
     (conj accumulating-literal-path
           (recover-literal-path-1 (get-in* form accumulating-literal-path) ordinal-index)))
   []
   ord-path))


(defn validate-collections
  "Returns a sequence of
  `{:path-predicate _ :predicate _ :path-datum _ :datum _ :ordinal-path-datum _ :valid? _}`
  hash-maps for every **collection** datum in `data` with a corresponding
  predicate in collection specification `spec`. `data` is an arbitrarily-nested,
  heterogeneous data structure. `spec` is a corresponding 'shape', i.e., all
  nested structures are of the same type and position. Only elements of `spec`
  that satisfy `fn?` are used. `validate-collections` descends into all neseted
  collections. `validate-collections` only validates complete datum-predicate
  pairs., i.e., only collections in `data` that have a corresponding predicate
  in `spec`. See [[valid-collections?]] and [[thoroughly-valid-collections?]]
  for high-level summaries of collection validation.

  * `:path-predicate` is a vector suitable for sending to [[get-in*]],
    [[assoc-in*]], [[update-in*]], etc., that locates the predicate within the
    specification.
  * `:path-datum` is the literal path to the datum within `data` to which the
    the collection predicate is applied.
  * `:ordinal-path-datum` is a vector suitable for sending to [[ordinal-get]]
    and [[ordinal-get-in]], which locates the collection within `data` to which
    the collection predicate is applied.
  * `:predicate` is a 1-arity function which returns truthy/falsey.
  * `:datum` is the collection entity in `data`.
  * `:valid?` is the result of invoking the predicate with the collection datum.

  The ordering of results is an implementation detail and not specified.

  Remember two mantras:

  1. Shape your specification (mostly) like your data.
  2. Validation looks at *either* scalars *or* collections, not both.
     `validate-collections` only looks at collections.

  Predicates at `path` within the collection specification are applied to the
  collection located at `(drop-last path)` within `data`. Generally, the
  predicate is applied to the 'parent' collection that contains it.

  Examples:
  ```clojure
  (validate-collections [42      [99     ]]  ;; <-- data
                        [vector? [vector?]]) ;; <-- specification
  ;; => ({:datum [42 [99]], :valid? true, :path-predicate [0], :predicate vector?, :ordinal-path-datum [], :path-datum []}
  ;;     {:datum [99], :valid? true, :path-predicate [1 0], :predicate vector?, :ordinal-path-datum [0], :path-datum [1]})

  ;; predicate `vector?` at path [0] in specification is applied to the collection at path (drop-last [0]) in data
  ;; predicate `vector?` at path [1 0] in specification is applied to the collection at path (drop-last [1 0]) in data

  (validate-collections {                :a 42 :b {                   :c 99}}
                        {:root-coll map?       :b {:child-coll? list?      }})
  ;; => ({:datum {:a 42, :b {:c 99}}, :valid? true, :path-predicate [:root-coll], :predicate map?, :ordinal-path-datum [], :path-datum []}
  ;;     {:datum {:c 99}, :valid? false, :path-predicate [:b :child-coll?], :predicate list?, :ordinal-path-datum [:b], :path-datum [:b]})

  ;; predicate `map?` at path [:root-coll] in specification is applied to the collection at path (drop-last [:root-coll]) in data
  ;; predicate `list?` at path [:b :child-coll?] in specification is applied to the collection at path (drop-last [:b :child-coll?]) in data
  ```

  Only complete collection-predicate pairs are validated. Un-paired collections
  and un-paired predicates are ignored.

  ```clojure
  ;; nested vector in data is not paired with a corresponding predicate in specification
  (validate-collections [11 22 33 [44 55 66]] ;;  <-- data
                        [vector?  [        ]]) ;; <-- specification
  ;; => ({:datum [11 22 33 [44 55 66]], :valid? true, :path-predicate [0], :predicate vector?, :ordinal-path-datum [], :path-datum []})

  ;; specification's map does not contain a predicate that corresponds to data's outer map
  (validate-collections {:a 11 :b [22 33]}  ;; <-- data
                        {      :b [list?]}) ;; <-- specification
  ;; => ({:datum [22 33], :valid? false, :path-predicate [:b 0], :predicate list?, :ordinal-path-datum [:b], :path-datum [:b]})

  ;; un-paired list? and set? predicates in collection specification are ignored
  (validate-collections [99] [vector? [list?] [set?]])
  ;; => ({:datum [99], :valid? true, :path-predicate [0], :predicate vector?, :ordinal-path-datum [], :path-datum []})
  ```

  Note: (Possibly) non-terminating sequences are clamped at the length of the
  corresponding element in the other collection. Therefore, if there are
  fewer `spec` predicates than elements to be tested in `data`, you must pad
  the collection specification, e.g., `(constantly true)`, to force validation
  of those datums. (I don't like this policy, but I don't have a better
  heuristic by which to clamp a non-terminating sequence with in-band
  information.)

  ```clojure
  ;; non-terminating specification is clamped at the length of the data
  (validate-collections [[11] [22] [33]]
                        (repeat [vector?]))
  ;; => ({:datum [11], :valid? true, :path-predicate [0 0], :predicate vector?, :ordinal-path-datum [0], :path-datum [0]}
  ;;     {:datum [22], :valid? true, :path-predicate [1 0], :predicate vector?, :ordinal-path-datum [1], :path-datum [1]}
  ;;     {:datum [33], :valid? true, :path-predicate [2 0], :predicate vector?, :ordinal-path-datum [2], :path-datum [2]})

  ;; non-terminating data is clamped at the length of the specification
  (validate-collections (cycle [[11] [22] [33]])
                        [[vector?]])
  ;; => ({:datum [11], :valid? true, :path-predicate [0 0], :predicate vector?, :ordinal-path-datum [0], :path-datum [0]})

  ;; only the first nested vector is validated because the data's non-terminating sequence
  ;; was clamped to the length of the specification

  ;; padding the specification to catch the full cycle of the data
  (validate-collections (cycle [[11] [22] [33]])
                        [[vector?] [any?] [any?]])
  ;; => ({:datum [11], :valid? true, :path-predicate [0 0], :predicate vector?, :ordinal-path-datum [0], :path-datum [0]}
  ;;     {:datum [22], :valid? true, :path-predicate [1 0], :predicate any?, :ordinal-path-datum [1], :path-datum [1]}
  ;;     {:datum [33], :valid? true, :path-predicate [2 0], :predicate any?, :ordinal-path-datum [2], :path-datum [2]})
  ```

  Overview of the algorithm.

  1. Run [[all-paths]] on the data.
      ```clojure
      (all-paths [11 {:b 22} [[33]]])
      ;; => [{:path [], :value [11 {:b 22} [[33]]]}
      ;;     {:path [0], :value 11}
      ;;     {:path [1], :value {:b 22}}
      ;;     {:path [1 :b], :value 22}
      ;;     {:path [2], :value [[33]]}
      ;;     {:path [2 0], :value [33]}
      ;;     {:path [2 0 0], :value 33}]

      ;; Seven total elements: four collections, three scalars.
      ```
  2. Run [[all-paths]] on the specification.
      ```clojure
      (all-paths [vector? {:coll-type? map?} [[list?]]])
      ;; => [{:path [], :value [vector? {:coll-type? map?} [[list?]]]}
      ;;     {:path [0], :value vector?}
      ;;     {:path [1], :value {:coll-type? map?}}
      ;;     {:path [1 :coll-type?], map?}
      ;;     {:path [2], :value [[list?]]}
      ;;     {:path [2 0], :value [list?]}
      ;;     {:path [2 0 0], :value list?}]

      ;; Seven total elements: four collections, three predicates.
      ```

  3. Remove **scalar** elements from the data.
      ```clojure
      (filter #(coll? (:value %)) (all-paths [11 {:b 22} [[33]]]))
      ;; => ({:path [], :value [11 {:b 22} [[33]]]}
      ;;     {:path [1], :value {:b 22}}
      ;;     {:path [2], :value [[33]]}
      ;;     {:path [2 0], :value [33]})

      ;; Four collections in data.
      ```

  4. Remove **collections** from the specification.
      ```clojure
      (only-fns (all-paths [vector? {:coll-type? map?} [[list?]]]))
      ;; => [{:path [0], :value vector?}
      ;;     {:path [1 :coll-type?], :value map?}
      ;;     {:path [2 0 0], :value list?}]

      ;; Three predicates in specification.
      ```

  5. Associate predicates in the specification with collections in the data. The
     collections in the data correspond to the *containers/parent* of the
     predicate within the specification. Basically, the predicate at `path` in
     the specification will be applied to the collection at `(drop-last)` in the
     data.
      ```clojure
      ;; predicate `vector?` at path [0] in spec is paired with entity at path (drop-last [0]) in data
      ;; predicate `map?` at path [1 :coll-type?] in spec is paired with entity at path (drop-last [1 :coll-type?]) in data
      ;; predicate `list?` at path [2 0 0] in spec is paired with entity at path (drop-last [2 0 0]) in data
      ;; the nested vector at path [2] in data does not have a corresponding predicate in specification; it will not be validated
      ```

  6. For each collection-predicate pair, apply the predicate to the collection.
      ```clojure
      (vector? [11 {:b 22} [[33]]]) ;; true
      (map? {:b 22}) ;; true
      (list? [33]) ;; false

      ;; or, all at once             v-----------------------v-----v--------- these scalars in data are ignored
      (validate-collections [        11 {                 :b 22} [[33   ]]]  ;; <-- data
                            [vector?    {:coll-type? map?      } [[list?]]]) ;; <-- specification
      ;;                     ^-----------------------^-------------^--------- these predicates in spec are applied
      ;;                                                                      to the parent containers in data

      ;; => ({:datum [11 {:b 22} [[33]]], :valid? true, :path-predicate [0], :predicate vector?, :ordinal-path-datum [], :path-datum []}
      ;;     {:datum {:b 22}, :valid? true, :path-predicate [1 :coll-type?], :predicate map?, :ordinal-path-datum [0], :path-datum [1]}
      ;;     {:datum [33], :valid? false, :path-predicate [2 0 0], :predicate list?, :ordinal-path-datum [1 0], :path-datum [2 0]})
      ```"
  {:UUIDv4 #uuid "e0308c59-272d-4eeb-b42c-f63a20173420"}
  [data spec]
  (let [[clamped-data clamped-spec] (expand-and-clamp data spec)
        results-orig-keys (map #(merge % (apply-one-coll-spec clamped-data clamped-spec (:path %)))
                               (only-fns (all-paths clamped-spec)))
        results-orig-keys-unvalidated-removed (filter #(coll? (:datum %)) results-orig-keys)
        results-re-keyed (re-key results-orig-keys-unvalidated-removed)]
    (map #(assoc % :path-datum (recover-literal-path data (:ordinal-path-datum %))) results-re-keyed)))


(defn valid-collections?
  "Following validation with [[validate-collections]], returns `true` if every
  **collection** element in `data` satisfies every corresponding predicate in
  collection specification `spec`, `false` otherwise.

  Note: `valid-collections?` returns `true` if validation returns zero
  `{:valid? falsey}` results.

  Note: If a corresponding specification predicate does not exist, that element
  of data will not be checked. Use [[collections-without-predicates]] to locate
  elements of `data` that lack corresponding predicates in `spec`. Use
  [[thoroughly-valid-collections?]] to require that every collection in `data`
  is validated.

  See [[validate-collections]] for details on the mechanics of collection
  validation.

  Examples:
  ```clojure
  (valid-collections? [      42 [        :foo]]  ;; <-- data
                      [list?    [vector?     ]]) ;; <-- specification
  ;; => false

  (valid-collections? {                 :a 42 :b {                 :c 'foo}} ;; <-- data
                      {:outer-coll map?       :b {:inner-coll map?        }});; <-- specification
  ;; => true

  ;; un-paired datum; nested vector in data not tested
  (valid-collections? [        11     [22]]
                      [vector?            ])
  ;; => true
  ```"
  {:UUIDv4 #uuid "bd03894b-c45a-4875-972a-d5be8a092920"}
  [data spec]
  (every? #(true? (:valid?  %)) (validate-collections data spec)))


;;;; Combined specs

(defn validate
  "Perform a scalar validation of `data` using scalar specification
  `scalar-spec`, then immediately perform a collection validation of `data`
  using collection specification `collection-spec`, then return the merged
  vector of each result. See [[validate-scalars]] and [[validate-collections]].

  Remember two mantras:

  1. Shape your specification (mostly) like your data.
  2. Validation looks at *either* scalars *or* collections, not both.

  `validate` performs two separate validations, in two distinct steps, then
   returns a single summary that merges both results. First, `data`'s scalars
   are validated, then `data`'s collections are validated. Finally, the results
  of those two distinct validations are merged into a comprehensive summary.

  Examples:
  ```clojure
  ;; only scalar validation with `validate-scalars`
  (validate-scalars [42]    ;; data
                    [int?]) ;; scalar specification
  ;; => [{:path [0], :datum 42, :predicate int?, :valid? true}]

  ;; only collection validation with `validate-collections`
  (validate-collections [42]       ;; data
                        [vector?]) ;; collection specification
  ;; => ({:path [0], :value vector?, :datum [42], :ordinal-parent-path [], :valid? true})

  ;; scalar validation, then collection validation, with a single invocation
  (validate [42]       ;; data
            [int?]     ;; scalar specification
            [vector?]) ;; collection specification
  ;; => ({:path [0], :datum 42, :predicate int?, :valid? true}
  ;;     {:path [0], :value vector?, :datum [42], :ordinal-parent-path [], :valid? true})


  ;; only scalar validation with `validate-scalars`
  (validate-scalars {:a 11}       ;; data
                    {:a string?}) ;; scalar specification
  ;; [{:path [:a], :datum 11, :predicate #function[clojure.core/string?--5475], :valid? false}]

  ;; only collection validation with `validate-collections`
  (validate-collections {                 :a 11}  ;; data
                        {:coll-type? map?      }) ;; collection specification
  ;; ({:path [:coll-type?], :value map?, :datum {:a 11}, :ordinal-parent-path [], :valid? true})

  ;; scalar validation, then collection validation, with a single invocation
  (validate {:a 11}             ;; data
            {:a string?}        ;; scalar specfication
            {:coll-type? map?}) ;; collection specification
  ;; => ({:path [:a], :datum 11, :predicate string?, :valid? false}
  ;;     {:path [:coll-type?], :value map?, :datum {:a 11}, :ordinal-parent-path [], :valid? true})
  ```"
  {:UUIDv4 #uuid "4c7c9c1c-f8a6-49f1-abcb-946a0c820cf2"}
  [data scalar-spec collection-spec]
  (concat (validate-scalars data scalar-spec)
          (validate-collections data collection-spec)))


(defn valid?
  "Following validations with [[validate-scalars]] and then with
  [[validate-collections]], returns `true` if `data` satisfies every
  corresponding predicate in scalar specification `scalar-spec` and every
  corresponding predicate in collection specification `colleciton-spec`,
  `false` otherwise.

  `valid?` provides a combined interface to [[valid-scalars?]] and
  [[valid-collections?]]. Scalar validation and collection validation are
  performed in completely distinct operations. Their results are merely combined
  into a single `true`/`false` high-level summary. Use [[validate]] to generate
  a detailed report of scalar and collection validation.

  Note: `valid?` returns `true` if validations return zero `{:valid? falsey}`
  results.

  See [[validate-scalars]] and [[validate-collections]] for details on the the
  mechanics of validation.

  Examples:
  ```clojure
  (valid? [42 [:foo [22/7]]]             ;; data
          [int? [keyword? [ratio?]]]     ;; scalar specification
          [vector? [vector? [vector?]]]) ;; collections specification
  ;; => true

  (valid? {:a 42 :b {:c ['foo true]}}           ;; data
          {:a int? :b {:c [keyword? boolean?]}} ;; scalar specification
          {:root-coll map? :b {:c [vector?]}})  ;; collection specification
  ;; => false
  ```"
  {:UUIDv4 #uuid "2f95850d-9cc5-4f02-8020-a86f3e64cbfe"}
  [data scalar-spec collection-spec]
  (and (valid-scalars? data scalar-spec)
       (valid-collections? data collection-spec)))


(defn apply-one-coll-spec-to-two
  "Given data-1 and data-2, arbitrarily-nested, heterogeneous collections, apply
   the predicate located within coll-spec at literal path."
  {:UUIDv4 #uuid "ec60d38e-7827-46ec-8e2b-6097ef3b7e93"
   :no-doc true}
  [data-1 data-2 coll-spec literal-path]
  (let [pred (get-in* coll-spec literal-path)
        datum-1 (ordinal-get-in data-1 (ordinal-path-of-parent coll-spec literal-path))
        datum-2 (ordinal-get-in data-2 (ordinal-path-of-parent coll-spec literal-path))
        ordinal-parent-path (ordinal-path-of-parent coll-spec literal-path)]
    {:predicate pred
     :datum-1 datum-1
     :datum-2 datum-2
     :ordinal-parent-path ordinal-parent-path
     :valid? (pred datum-1 datum-2)}))


(defn dual-validate-collections
  "Two-collection version of (validate-collections). Predicates contained
   in spec must accept two arguments, one from each data collection.
   data-1 is priviledged to be the reference by which non-terminating
   sequences are clamped."
  {:UUIDv4 #uuid "b893bbd0-2022-4f5e-b045-77d0782f5e27"
   :no-doc true}
  [data-1 data-2 spec]
  (let [[clamped-data-1 clamped-spec] (expand-and-clamp data-1 spec)
        [clamped-data-2 _] (expand-and-clamp data-2 spec)]
    (map #(merge % (apply-one-coll-spec-to-two clamped-data-1 clamped-data-2 clamped-spec (:path %)))
         (only-fns (all-paths clamped-spec)))))


(defn valid-dual-collection-spec?
  "Two-collection version of (valid-collections?). Returns true if data-1
   and data-2 both validate against spec."
  {:UUIDv4 #uuid "3f6ba989-3283-4e55-88bf-b7a46c104a83"
   :no-doc true}
  [data-1 data-2 spec]
  (every? #(true? (:valid? %)) (dual-validate-collections data-1 data-2 spec)))


;;;; macro specs

(defn validate-macro-with
  "Returns results of validating the macroexpansion of a macro and arguments
   against scalar specification `spec`. Supply `macro-args` as if to
  `macroexpand-1` itself, i.e., `` `(macro-name arg1 arg 2...)``.

  Note: Many entities that appear to be a function in a macro expansion are, in
  fact, symbols.

  Use [[valid-macro?]] to produce a high-level summary result.

  Example:
  ```clojure
  (defmacro example-macro [f & args] `(~f ~@args))

  (macroexpand-1 `(example-macro + 1 2 3)) ;; => (clojure.core/+ 1 2 3)

  (def example-macro-spec (list symbol? number? number? number?))

  (validate-macro-with `(example-macro + 1 2 3) example-macro-spec)
  ;; => [{:path [0], :datum clojure.core/+, :predicate symbol?, :valid? true}
  ;;     {:path [1], :datum 1, :predicate number?, :valid? true}
  ;;     {:path [2], :datum 2, :predicate number?, :valid? true}
  ;;     {:path [3], :datum 3, :predicate number?, :valid? true}]
  ```"
  {:UUIDv4 #uuid "a821bfd8-fd47-4d5c-bc62-21f999600df1"}
  [macro-args spec]
  (validate-scalars (macroexpand-1 macro-args) spec))


(defn valid-macro?
  "Returns `true` if macroexpansion fully satisfies scalar specification `spec`.
  Supply `macro` and `args` as if to `macroexpand-1` itself, i.e.,
  `` `(macro-name arg1 arg 2...)``.

   Note 1: Many entities that appear to be a function in a macro expansion are
   in fact symbols.

   Note 2: Macro expansion works subtly different between the CIDER nREPL and
   from the CLI, e.g. `$ lein test`. Use syntax quote ` as a workaround.

  Use [[validate-macro-with]] to produce a detailed validation report.

  Example:
  ```clojure
  (defmacro example-macro [f & args] `(~f ~@args))

  (macroexpand-1 `(example-macro + 1 2 3)) ;; => (clojure.core/+ 1 2 3)

  (def example-macro-spec (list symbol? number? number? number?))

  (valid-macro? `(example-macro + 1 2 3) example-macro-spec) ;; => true
  ```"
  {:UUIDv4 #uuid "b084eeaf-288a-4d4c-b095-6cf08fe1d88f"}
  [macro-args spec]
  (empty? (filter #(false? (:valid? %)) (validate-macro-with macro-args spec))))


;;;; path specs


(defn validate-with-path-spec
  "Given a heterogeneous, arbitrarily-nested structure `data`, validate
  against path specification vector `spec`. Each entry in `spec` is a map with
  keys `:paths` and `:predicate`. `:paths` is a vector to [[get-in*]] paths to
  elements (scalar and/or collections) in `data`, supplied in-order to the
  function associated with `:predicate`, whose arity matches the number of paths
  in `:paths`. The function should return truthy or falsey values (strict
  `true`/`false` is recommended, but not required).

  Examples:
  ```clojure
  ;; relating one scalar to another (predicate is 2-arity)
  (validate-with-path-spec [11 :foo 22] [{:paths [[2] [0]] :predicate #(= %2 (/ %1 2))}])
  ;; => ({:args (22 11), :valid? true, :paths [[2] [0]], :predicate fn--47025]})

  ;; relating one scalar to another, different depths of the sequence (predicate is 3-arity)
  (validate-with-path-spec {:a 42 :b [42 {:c 42}]} [{:paths [[:b 0] [:a] [:b 1 :c]] :predicate #(= %1 %2 %3)}])
  ;; => ({:args (42 42 42), :valid? true, :paths [[:b 0] [:a] [:b 1 :c]], :predicate fn--47045]})

  ;; specification containing two validations
  (validate-with-path-spec [:foo [42 22/7]] [{:paths [[1 0]] :predicate int?} {:paths [[1]] :predicate vector?}])
  ;; => ({:args (42), :valid? true, :paths [[1 0]], :predicate int?]}
  ;;     {:args ([42 22/7]), :valid? true, :paths [[1]], :predicate vector?]})
  ```"
  {:UUIDv4 #uuid "6cd6cfaa-724d-4ae4-8615-ada9d48ff594"}
  [data spec]
  (let [arg-list (fn [spec-entry] (map #(get-in* data %) (:paths spec-entry)))
        validation (fn [spec-entry] (hash-map :paths (:paths spec-entry)
                                              :predicate (:predicate spec-entry)
                                              :args (arg-list spec-entry)
                                              :valid? (apply
                                                       (:predicate spec-entry)
                                                       (arg-list spec-entry))))]
    (map #(validation %) spec)))


;;;; collection spec illustrative examples

  (comment ;; Illustrative example #1
    (ordinal-path-of-parent ddd [4 3 1 0]) ; [4 3 1]
    (ordinal-get-in ddd [4 3 1])
    ;; Illustration of the algorithm
    (def ddd [[11] [22] [33] [44] [[55] [66] [77] [[88] [99] [111] [222] [333]] [444] [555]]])
    (def ppp [4 3 1 0])
    (get-in* ddd ppp)

    ;; by eye inspection, the ordinal path should be [4 3 1]

    ;; check the literal path
    (get-in* wrapped-ddd ppp) ;; 99

    ;; set the parent container's literal path and check
    (def par-lit-pth (parent-literal-path ppp)) ; [:root 4 3 1]
    (get-in* wrapped-ddd par-lit-pth)           ; [99]

    ;; dive into the data structure, capturing the containing element at each step
    (get-in* wrapped-ddd (subvec par-lit-pth 0 1)) ; [[11] [22] [33] [44] [[55] [66] [77] [[88] [99] [111] [222] [333]] [444] [555]]]
    (get-in* wrapped-ddd (subvec par-lit-pth 0 2)) ; [[55] [66] [77] [[88] [99] [111] [222] [333]] [444] [555]]
    (get-in* wrapped-ddd (subvec par-lit-pth 0 3)) ; [[88] [99] [111] [222] [333]]
    (get-in* wrapped-ddd (subvec par-lit-pth 0 4)) ; [99]

    ;; chop the data so it 'stops' @ target element       v--- these stop indexes are +1 from the parent's literal path b/c (sub) stop index is non-inclusive
    (sub (get-in* wrapped-ddd (subvec par-lit-pth 0 1)) 0 5) ; ([11] [22] [33] [44] [[55] [66] [77] [[88] [99] [111] [222] [333]] [444] [555]])
    (sub (get-in* wrapped-ddd (subvec par-lit-pth 0 2)) 0 4) ; ([55] [66] [77] [[88] [99] [111] [222] [333]])
    (sub (get-in* wrapped-ddd (subvec par-lit-pth 0 3)) 0 2) ; ([88] [99])

    ;; find the ordingal by counting only colls along the the way
    (find-ordinal (sub (get-in* wrapped-ddd (subvec par-lit-pth 0 1)) 0 5)) ; 4
    (find-ordinal (sub (get-in* wrapped-ddd (subvec par-lit-pth 0 2)) 0 4)) ; 3
    (find-ordinal (sub (get-in* wrapped-ddd (subvec par-lit-pth 0 3)) 0 2)) ; 1
    ;; [4 3 1] is my predicted ordinal path (coincidentally matches part of the literal path in this example because all the intervening elements are colls)

    ;; thread-it
    (-> wrapped-ddd
        (get-in* (subvec par-lit-pth 0 1))
        (sub 0 5)
        (find-ordinal))

    (-> wrapped-ddd
        (get-in* (subvec par-lit-pth 0 2))
        (sub 0 4)
        (find-ordinal))

    (-> wrapped-ddd
        (get-in* (subvec par-lit-pth 0 3))
        (sub 0 2)
        (find-ordinal))
    ;; building up the outputs of these threading ops produces the ordinal path
    )


(comment ;; Illustrative example #2
  (def a [[11] 22 33 [44] [55] [66 [77] [88] [99] [111 [222] [333]]]])
  (def b [5 4 2 0])
  (get-in* a b)
  (def c (parent-literal-path b))
  (get-in* a c) ; [333]
  ;; my eyeball guess at the ordinal path [3 3 1]
  (ordinal-get-in a [3 3 1]) ; [333]
  ;; yup, it checks out

  ;; dive into the data structure, capturing the target's containing parent at each level
  (container-at-this-depth a c 0) ; [[11] 22 33 [44] [55] [66 [77] [88] [99] [111 [222] [333]]]]
  (container-at-this-depth a c 1) ; [66 [77] [88] [99] [111 [222] [333]]]
  (container-at-this-depth a c 2) ; [111 [222] [333]]
  (container-at-this-depth a c 3) ; [333]

  (target-index c 0) ; 5
  (target-index c 1) ; 4
  (target-index c 2) ; 2

  (chop-container-at-target [[11] 22 33 [44] [55] [66 [77] [88] [99] [111 [222] [333]]]] 5) ; ([11] 22 33 [44] [55] [66 [77] [88] [99] [111 [222] [333]]])
  (chop-container-at-target [66 [77] [88] [99] [111 [222] [333]]] 4) ; (66 [77] [88] [99] [111 [222] [333]])
  (chop-container-at-target [111 [222] [333]] 2) ; (111 [222] [333])

  (find-ordinal '([11] 22 33 [44] [55] [66 [77] [88] [99] [111 [222] [333]]])) ; 3
  (find-ordinal '(66 [77] [88] [99] [111 [222] [333]])) ; 3
  (find-ordinal '(111 [222] [333])) ; 1

  ;; This sequence is neatly threaded
  ;; e.g., depth=1
  (-> a
      (container-at-this-depth c 1)
      (chop-container-at-target (target-index c 1))
      (find-ordinal)) ; 3

  ;; function (ordinal-at-a-depth) encapsulates this threading
  (ordinal-at-a-depth a c 0) ; 3
  (ordinal-at-a-depth a c 1) ; 3
  (ordinal-at-a-depth a c 2) ; 1 <-- stack these into an ordinal path [3 3 1]

  ;; build up an ordinal path
  (map #(conj (ordinal-at-a-depth a c %)) (range (count c)))

  ;; function (ordinal-path-of-parent) encapsulates that (map) build-up
  (ordinal-path-of-parent a b) ; [3 3 1]

  ;; Finally, check the results...
  (ordinal-get-in a [3 3 1]) ; [333]
  ;; ...is indeed the parent container.
)

  ;; Prose algorithm
  ;; example collection spec
  ;; [A? B? [C? {:key1 D? :key2 E?}] '() [F? [G?]] H?]
  ;; A? through H? represent some kind of specification of the relationship between two elements of the containing collection,
  ;; or specification on the containing collection itself.
  ;; A?, B?, and H? are predicates that apply to the outermost ('root') collection and it's descendent elements.
  ;; They would all have ordinal paths of [:root].
  ;; [C? {:key1 D? :key2 E?}] is a nested collection appearing first, so it contains predicates that apply to the target data's first nested vector.
  ;; In this case, that nested vector contains one spec, C?, that would specifiy relationships between elements of that first nested vector, or specs that would apply to the nested vector itself.
  ;; The ordinal path of this vector would be [:root 0], because within the root container, it's the zero-th appearing collection.
  ;; The next element at the top level, the empty list () is also a collection, so it's ordinal path would be [:root 1] because it's the one-th appearing collection.
  ;; In this case, it does not contain any specs, and thus doesnspecify any properties of the second list within the target data. It serves as a ordinal placeholder.
  ;; [F? [G?]] is the last top-level nested container, appearing two-th out of the trio, therefore it's ordinal path is [:root 2].
  ;; This principle is extended further with each nesting level:
  ;; Predicate E? is at a literal path of [2 1 :key2] with a ordinal path [:root 0 0].
  ;; Predicate G? is at a literal path of [4 1 0] with a ordinal path [:root 2 0].