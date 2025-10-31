(ns speculoos.performance.valid-scalars-benchmarks
  (:require
   [fastester.define :refer [defbench]]
   [fastester.display :refer [generate-documents]]
   [fastester.measure :refer [range-pow-10
                              run-benchmarks
                              run-one-defined-benchmark]]
   [speculoos.core :refer [valid-scalars?]]
   [speculoos.performance.benchmark-structures :refer [list-of-n-rand-ints
                                                       map-of-n-key-vals
                                                       nested-list
                                                       nested-map
                                                       nested-seq
                                                       nested-vec
                                                       seq-of-n-rand-ints
                                                       vec-of-n-rand-ints

                                                       list-spec
                                                       map-spec
                                                       seq-spec
                                                       vec-spec

                                                       nested-list-spec
                                                       nested-map-spec
                                                       nested-seq-spec
                                                       nested-vec-spec]]))


(defbench
  flat-vecs
  "Vectors"
  (fn [n] (valid-scalars? (vec-of-n-rand-ints n) (vec-spec n)))
  (range-pow-10 3))


(defbench
  flat-seqs
  "Sequences"
  (fn [n] (valid-scalars? (seq-of-n-rand-ints n) (seq-spec n)))
  (range-pow-10 3))


(defbench
  flat-lists
  "Lists"
  (fn [n] (valid-scalars? (list-of-n-rand-ints n) (list-spec n)))
  (range-pow-10 2))


(defbench
  flat-maps
  "Hashmaps"
  (fn [n] (valid-scalars? (map-of-n-key-vals n) (map-spec n)))
  (range-pow-10 3))


(defbench
  nested-vecs
  "Vectors"
  (fn [n] (valid-scalars? (nested-vec n) (nested-vec-spec n)))
  (range 1 6))


(defbench
  nested-seqs
  "Sequences"
  (fn [n] (valid-scalars? (nested-seq n) (nested-seq-spec n)))
  (range 1 6))


(defbench
  nested-lists
  "Lists"
  (fn [n] (valid-scalars? (nested-list n) (nested-list-spec n)))
  (range 1 5))


(defbench
  nested-maps
  "Hashmaps"
  (fn [n] (valid-scalars? (nested-map n) (nested-map-spec n)))
  (range 1 6))


#_(run-one-defined-benchmark nested-maps :lightning)
#_(run-benchmarks "resources/valid_scalars_benchmark_options.edn")
#_(generate-documents "resources/valid_scalars_benchmark_options.edn")

