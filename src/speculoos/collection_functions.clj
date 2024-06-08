(ns speculoos.collection-functions
  "Hacked up versions of `map` and `reduce` that attempt to provide a unified
  interface to processing elements within maps, vectors, lists, sets, and
  (possibly) non-terminating sequences (i.e., `clojure.lang.{Cycle,Iterate,LazySeq,Range}`)
  . These functions use only `first`, `rest`, etc., and therefore make no
  consideration for performance.

  Implementations are heavily inspired by their `clojure.core` counterparts.")


(defn reduce-2
  "Reduce elements of `coll` by applying function `f`, with optional initial
  value `val`. `coll` may any Clojure collection.

  Consult [clojure.core/reduce](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce)
  docstring for usage.

  Examples:
  ```clojure
  (reduce-2 + [1 2 3]) ;; => 6
  (reduce-2 #(conj %1 %2) {:a 1 :b 2 :c 3}) ;; => [:a 1 [:b 2] [:c 3]]
  (reduce-2 * (list 3 4 5)) ;; => 60
  (reduce-2 #(conj %1 %2) [] #{:x :y :z}) ;; => [:y :z :x]
  (reduce-2 #(%2 %1) 99 (take 3 (cycle [inc dec inc]))) ;; => 100

  (reduce-2 + []) ;; => 0
  (reduce-2 + [] []) ;; => []
  ```

  Modifications of [reduce version 1.1.0](https://github.com/clojure/clojure/blob/5293929c99c7e1b1b3bcdea3d451108c5774b3d1/src/clj/clojure/core.clj#L646)."
  {:source "clojure.core v1.1.0"
   :url "https://github.com/clojure/clojure/blob/5293929c99c7e1b1b3bcdea3d451108c5774b3d1/src/clj/clojure/core.clj#L646"
   :UUIDv4 #uuid "1f8d3639-c86f-4242-89a6-2a6018b147c6"}
  ([f coll]
   (let [s (seq coll)]
     (if s
       (reduce-2 f (first s) (next s))
       (f))))
  ([f val coll]
     (let [s (seq coll)]
       (if s
         (recur f (f val (first s)) (next s))
         val))))


(defn map-2
  "Return a sequence by applying function `f` to each element of collection
  `coll`. `coll` may be any Clojure collection.

  Consult [clojure.core/map](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map)
  docstring for usage. This version accepts only one collection.

  Examples:
  ```clojure
  (map-2 symbol [:a :b :c]) ;; => (a b c)
  (map-2 identity {:a 1 :b 2 :c 3}) ;; => ([:a 1] [:b 2] [:c 3])
  (map-2 inc (list 1 2 3)) ;; => (2 3 4)
  (map-2 identity #{1 2 3}) ;; => (1 3 2)
  (map-2 - (range 7 10)) ;; => (-7 -8 -9)

  (map-2 inc []) ;; => []
  ```

  Modications of [map version 1.0](https://github.com/clojure/clojure/blob/f85444e6f890eb585e598efefdbd84727427e0a4/src/clj/clojure/core.clj#L1494C1-L1522C56)."
  {:UUIDv4 #uuid "e4da723c-8b54-4036-9207-900553a20bd5"
   :source "clojure.core v1.0"
   :url "https://github.com/clojure/clojure/blob/f85444e6f890eb585e598efefdbd84727427e0a4/src/clj/clojure/core.clj#L1494C1-L1522C56"}
  [f coll]
  (if-let [s (seq coll)]
    (cons (f (first s)) (map-2 f (rest s)))
    coll))


(defn map-indexed-2
  "Returns a sequence consisting of the result of applying function `f` to 0
  and the first item of `coll`, followed by applying `f` to 1 and the second
  item in `coll`, etc. `f` should accept 2 arguments, index and item. `coll` may
  be any Clojure collection.

  Consult [clojure.core/map-indexed](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map-indexed)
  docstring for usage.

  Examples:
  ```clojure
  (map-indexed-2 #(vector %1 %2) [:a :b :c]) ;; => ([0 :a] [1 :b] [2 :c])
  (map-indexed-2 #(vector %1 %2) {:a 1 :b 2}) ;; => ([0 [:a 1]] [1 [:b 2]])
  (map-indexed-2 #(vector %1 %2) (list 'a :b \\c)) ;; ([0 a] [1 :b] [2 \\c])
  (map-indexed-2 #(vector %1 %2) #{11 22 33}) ;; => ([0 33] [1 22] [2 11])

  (map-indexed-2 (constantly :foo) []) ;; => []
  ```

  Modifications of [map-indexed version 1.2.0](https://github.com/clojure/clojure/blob/0436abdb51ae6d53517b74bf84a33162397744b0/src/clj/clojure/core.clj#L5618C1-L5636C20)."
  {:UUIDv4 #uuid "70de571d-9f54-4143-a6af-1d24418f9610"
   :source "clojure.core v 1.2.0"
   :url "https://github.com/clojure/clojure/blob/0436abdb51ae6d53517b74bf84a33162397744b0/src/clj/clojure/core.clj#L5618C1-L5636C20"}
  [f coll]
  (letfn [(mapi [idx coll]
            (if-let [s (seq coll)]
              (cons (f idx (first s)) (mapi (inc idx) (rest s)))
              coll))]
    (mapi 0 coll)))


(defn reduce-indexed
  "Systematically apply `f` to elements of `coll`, analagous to
  [[map-indexed-2]]. Function `f` should be a function of 3 arguments: index,
  the accumulating value, and the next element of `coll`. `coll` may be any
  Clojure collection.

  If `val` is not supplied:

    * `reduce-indexed` returns the result of applying `f` to the first 2 items
    coll, then applying `f` to that result and the 3rd item, etc.
    * If `coll` contains no items, `f` must accept no arguments as well, and
    `reduce-indexed` returns the result of calling `f` with no arguments.
    * If `coll` has only 1 item, it is returned and `f` is not called.

  If `val` is supplied:

    * `reduce-indexed` returns the result of applying `f` to `val` and the first
    item in `coll`, then applying `f` to that result and the 2nd item, etc.
    * If `coll` contains no items, returns `val` and `f` is not called.

  Hash/array-map elements are peeled off as `clojure.lang.MapEntry`, a
  pseudo vector of `[:key :value]`.

  Set elements are consumed in an un-defined order.

  Examples:
  ```clojure
  (reduce-indexed #(conj %2 (vector %1 %3)) [:initial] [:item1 :item2 :item3])
  ;; => [:initial [0 :item1] [1 :item2] [2 :item3]]

  (reduce-indexed #(assoc %2 %3 %1) {:init-val 99} [:a :b :c])
  ;; => {:init-val 99, :a 0, :b 1, :c 2}
  ```

  Note: This version does not involve lazy seqs nor chunking, so performance
  could be possibly pathological."
  {:UUIDv4 #uuid "f40f7bde-c704-47b5-bafe-26874f3e033e"}
  ([f coll]
   (let [s (seq coll)]
     (if s
       (reduce-indexed f (first s) (next s))
       (f))))
  ([f val coll]
   ((fn reduce-i [f idx val coll]
      (if-let [s (seq coll)]
        (reduce-i  f
                   (inc idx)
                   (f idx val (first s))
                   (next s))
        val))
    f 0 val coll)))


(comment
  ;; hash/array-map entries are peeled off as a clojure.lang.MapEntry pseudo-vector
  (first {:a 1 :b 2})                   ; [:a 1]
  (type (first {:a 1 :b 2}))            ; clojure.lang.MapEntry
  )