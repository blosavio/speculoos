(ns speculoos.performance.all-paths-benchmarks
  (:require
   [fastester.define :refer [defbench]]
   [fastester.display :refer [generate-documents]]
   [fastester.measure :refer [range-pow-10
                              run-benchmarks
                              run-one-defined-benchmark]]
   [speculoos.core :refer [all-paths]]
   [speculoos.performance.benchmark-structures :refer [list-of-n-rand-ints
                                                       map-of-n-key-vals
                                                       nested-list
                                                       nested-map
                                                       nested-seq
                                                       nested-vec
                                                       seq-of-n-rand-ints
                                                       vec-of-n-rand-ints]]))


(defbench
  flat-seqs
  "Sequences"
  (fn [n] (all-paths (seq-of-n-rand-ints n)))
  (range-pow-10 3))


(defbench
  nested-seqs
  "Sequences"
  (fn [n] (all-paths (nested-seq n)))
  (range 7))


(defbench
  flat-vecs
  "Vectors"
  (fn [n] (all-paths (vec-of-n-rand-ints n)))
  (range-pow-10 3))


(defbench
  nested-vecs
  "Vectors"
  (fn [n] (all-paths (nested-vec n)))
  (range 7))


(defbench
  flat-maps
  "Hashmaps"
  (fn [n] (all-paths (map-of-n-key-vals n)))
  (range-pow-10 3))


(defbench
  nested-maps
  "Hashmaps"
  (fn [n] (all-paths (into (hash-map) (nested-map n))))
  (range 7))


(defbench
  flat-lists
  "Lists"
  (fn [n] (all-paths (list-of-n-rand-ints n)))
  (range-pow-10 3))


(defbench
  nested-lists
  "Lists"
  (fn [n] (all-paths (nested-list n)))
  (range 5))


#_(run-one-defined-benchmark nested-lists :lightning)
#_(run-benchmarks "resources/all_paths_benchmark_options.edn")
#_(generate-documents "resources/all_paths_benchmark_options.edn")

