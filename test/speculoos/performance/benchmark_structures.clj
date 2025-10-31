(ns speculoos.performance.benchmark-structures
  "Example heterogeneous, arbitrarily-nested data structures used for
  benchmark measurements."
  (:require
   [fastester.measure :refer [range-pow-10]]
   [fn-in.core :refer [update*]]
   [speculoos.performance.benchmark-utils :refer [coll-of-n-rand-ints
                                                  narrow-deep
                                                  nested]]
   [speculoos.utility :refer [basic-collection-spec-from-data
                              spec-from-data]]))


(def max-seq-length 6)
(def seq-of-n-rand-ints (coll-of-n-rand-ints :sequence max-seq-length))
(def vec-of-n-rand-ints (coll-of-n-rand-ints :vector max-seq-length))


(def max-list-length 4)
(def list-of-n-rand-ints (coll-of-n-rand-ints :list max-list-length))


(def max-hashmap-length 6)
(def map-of-n-key-vals (coll-of-n-rand-ints :map max-hashmap-length))


(def max-in 7)


(def nested-vec
  (persistent!
   (reduce
    (fn [m k] (assoc! m k (nested k :vector)))
    (transient {})
    (range 1 max-in))))


(def n-levels 3)


(def narrow-deep-vec
  (persistent!
   (reduce
    (fn [m k] (assoc! m k (narrow-deep :vector k n-levels)))
    (transient {})
    (range-pow-10 5))))


(def nested-seq
  (persistent!
   (reduce
    (fn [m k] (assoc! m k (nested k :sequence)))
    (transient {})
    (range 1 max-in))))


(def max-list 5)


(def nested-list
  (doall
   (reduce
    (fn [m k] (assoc m k (nested k :list)))
    {}
    (range 1 max-list))))


(def nested-map
  (persistent!
   (reduce
    (fn [m k] (assoc! m k (nested k :map)))
    (transient {})
    (range 1 max-in))))


(def path-seq
  (let [f (fn [m k] (assoc m k (repeat k (dec k))))]
    (reduce f {} (range 1 max-in))))


(def path-nested-vec path-seq)


(def path-narrow-deep-vec
  (let [f (fn [m k] (assoc m k (update* (repeat (inc n-levels) k) n-levels dec)))]
    (reduce f {} (range-pow-10 5))))


(def path-list path-seq)


(def path-map
  (let [f (fn [m k] (assoc m k (repeat k 0)))]
    (reduce f {} (range 1 max-in))))


(def seq-spec (update-vals (select-keys seq-of-n-rand-ints [1 10 100 1000]) spec-from-data))
(def vec-spec (update-vals (select-keys vec-of-n-rand-ints [1 10 100 1000]) spec-from-data))
(def list-spec (update-vals (select-keys list-of-n-rand-ints [1 10 100]) spec-from-data))
(def map-spec (update-vals (select-keys map-of-n-key-vals [1 10 100 1000]) spec-from-data))


(def nested-vec-spec (update-vals (select-keys nested-vec [1 2 3 4 5 6]) spec-from-data))
(def nested-seq-spec (update-vals (select-keys nested-seq [1 2 3 4 5 6]) spec-from-data))
(def nested-list-spec (update-vals (select-keys nested-list [1 2 3 4 5 6]) spec-from-data))
(def nested-map-spec (update-vals (select-keys nested-map [1 2 3 4 5 6]) #(->> %
                                                                               (into (hash-map))
                                                                               spec-from-data)))


(def vec-coll-spec (update-vals (select-keys vec-of-n-rand-ints [1 10 100 1000]) basic-collection-spec-from-data))
(def seq-coll-spec (update-vals (select-keys seq-of-n-rand-ints [1 10 100 1000]) basic-collection-spec-from-data))
(def list-coll-spec (update-vals (select-keys list-of-n-rand-ints [1 10 100]) basic-collection-spec-from-data))
(def map-coll-spec (update-vals (select-keys map-of-n-key-vals [1 10 100 1000]) basic-collection-spec-from-data))


(def nested-vec-coll-spec (update-vals (select-keys nested-vec [1 2 3 4]) basic-collection-spec-from-data))
(def nested-seq-coll-spec (update-vals (select-keys nested-seq [1 2 3 4]) basic-collection-spec-from-data))
(def nested-list-coll-spec (update-vals (select-keys nested-list [1 2 3 4]) basic-collection-spec-from-data))
(def nested-map-coll-spec (update-vals (select-keys nested-map [1 2 3 4]) #(->> %
                                                                              (into (hash-map))
                                                                              basic-collection-spec-from-data)))

