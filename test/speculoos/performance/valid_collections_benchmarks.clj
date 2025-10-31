(ns speculoos.performance.valid-collections-benchmarks
  (:require
   [fastester.define :refer [defbench]]
   [fastester.display :refer [generate-documents]]
   [fastester.measure :refer [range-pow-10
                              run-benchmarks
                              run-one-defined-benchmark]]
   [speculoos.core :refer [valid-collections?]]
   [speculoos.performance.benchmark-structures :refer [list-of-n-rand-ints
                                                       map-of-n-key-vals
                                                       nested-list
                                                       nested-map
                                                       nested-seq
                                                       nested-vec
                                                       seq-of-n-rand-ints
                                                       vec-of-n-rand-ints

                                                       list-coll-spec
                                                       map-coll-spec
                                                       seq-coll-spec
                                                       vec-coll-spec

                                                       nested-list-coll-spec
                                                       nested-map-coll-spec
                                                       nested-seq-coll-spec
                                                       nested-vec-coll-spec]]))


(defbench
  flat-vecs
  "Vectors"
  (fn [n] (valid-collections? (vec-of-n-rand-ints n) (vec-coll-spec n)))
  (range-pow-10 3))


(defbench
  nested-vecs
  "Vectors"
  (fn [n] (valid-collections? (nested-vec n) (nested-vec-coll-spec n)))
  [1 2 3 4])


(defbench
  flat-seqs
  "Sequences"
  (fn [n] (valid-collections? (seq-of-n-rand-ints n) (seq-coll-spec n)))
  (range-pow-10 3))


(defbench
  nested-seqs
  "Sequences"
  (fn [n] (valid-collections? (nested-seq n) (nested-seq-coll-spec n)))
  [1 2 3 4])


(defbench
  flat-lists
  "Lists"
  (fn [n] (valid-collections? (list-of-n-rand-ints n) (list-coll-spec n)))
  (range-pow-10 2))


(defbench
  nested-lists
  "Lists"
  (fn [n] (valid-collections? (nested-list n) (nested-list-coll-spec n)))
  [1 2 3 4])


(defbench
  flat-maps
  "Hashmaps"
  (fn [n] (valid-collections? (map-of-n-key-vals n) (map-coll-spec n)))
  (range-pow-10 3))


(defbench
  nested-maps
  "Hashmaps"
  (fn [n] (valid-collections? (nested-map n) (nested-map-coll-spec n)))
  [1 2 3 4])


#_(run-one-defined-benchmark nested-maps :lightning)
#_(run-benchmarks "resources/valid_collections_benchmark_options.edn")
#_(generate-documents "resources/valid_collections_benchmark_options.edn")

