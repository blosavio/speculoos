(ns speculoos.property-tests
  (:require [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [speculoos.core :refer :all]
            [speculoos.utility :refer :all]))


(comment
  (def d1 [0 true -1/2 "" \Á])
  (def s1 (spec-from-data d1))
  (valid-scalars? d1 s1)
  )


;; scalars that are handled by speculoos.utility
(def scalars-to-check [gen/boolean
                       gen/char
                       gen/double
                       gen/int
                       gen/keyword
                       gen/nat
                       (gen/such-that #(< % 0) gen/neg-int)
                       (gen/such-that #(< 0 %) gen/pos-int)
                       (gen/such-that ratio? gen/ratio)
                       gen/string])


;; simple scalar spec on vector
(gen/sample (gen/vector (gen/one-of scalars-to-check)))

(def simple-scalar-spec (prop/for-all [v (gen/vector (gen/one-of scalars-to-check))]
                                      (valid-scalars? v (spec-from-data v))))

(tc/quick-check 100 simple-scalar-spec)


;; simple scalar spec on map
(gen/sample (gen/map gen/keyword (gen/one-of scalars-to-check)))

(def simple-map-spec (prop/for-all [m (gen/map gen/keyword (gen/one-of scalars-to-check))]
                                   (valid-scalars? m (spec-from-data m))))

(tc/quick-check 100 simple-map-spec)


;; simple scalar spec on set
;; note: speculoos only checks one predicate from a set, the one undefined-ly returned by (first #{_})
;;       therefore, make test sets homogeneous
(gen/sample (gen/set (rand-nth scalars-to-check)))

(def simple-set-spec (prop/for-all [s (gen/set (rand-nth scalars-to-check))]
                                   (valid-scalars? s (spec-from-data s))))

(tc/quick-check 100 simple-set-spec)


;; simple scalar spec on list
(gen/sample (gen/list (gen/one-of scalars-to-check)))

(def simple-list-spec (prop/for-all [l (gen/list (gen/one-of scalars-to-check))]
                                    (valid-scalars? l (spec-from-data l))))

(tc/quick-check 100 simple-list-spec)



;; recursive generators for nested collections
;; nested, heterogeneous scalar specs

(def compound (fn [inner] (gen/one-of [(gen/vector inner)
                                       (gen/map inner inner)])))

(def scalars (gen/one-of scalars-to-check))
(def recursive-coll (gen/vector (gen/recursive-gen compound scalars)))

(def scalar-spec-on-recursive-colls (prop/for-all [c recursive-coll]
                                                  (valid-scalars? c (spec-from-data c))))

(tc/quick-check 15 scalar-spec-on-recursive-colls)

;; nested, heterogeneous collection specs

(comment
  (gen/sample (gen/recursive-gen compound scalars) 25)
  (last (gen/sample (gen/recursive-gen compound scalars) 10))
  (def b1 (last (gen/sample (gen/recursive-gen compound scalars) 100)))
  (basic-collection-spec-from-data b1)
  (valid-collections? b1 (basic-collection-spec-from-data b1))
  )


(def collection-spec-on-recursive-colls (prop/for-all [c recursive-coll]
                                                      (valid-collections? c (basic-collection-spec-from-data c))))

(tc/quick-check 15 collection-spec-on-recursive-colls)




(comment
  (gen/sample simple-collection-gen)
  (take-last 10 (gen/sample simple-collection-gen 100))
  (make-empty (gen/sample simple-collection-gen 1))
  (basic-collection-spec-from-data [11 22 33])
  (valid-collections? [11 22 33] (basic-collection-spec-from-data [11 22 33]))
  (basic-collection-spec-from-data [#{11 #{22} #{33 #{44}}}])

  ;; note: collection specs can only nest max one-level further into a set; after that,
  ;; the little analysis machine runs out of grasp-able handles on the contents of the set


  (def a01 [[#{[]}]])
  (valid-collections? a01 (basic-collection-spec-from-data a01)) ; false


  (def a02 [[#{}]])
  (valid-collections? a02 (basic-collection-spec-from-data a02)) ; true


  (def a1 [11 #{22}])
  (valid-collections? a1 (basic-collection-spec-from-data a1)) ; true


  (def a2 [11 #{22 [33]}])
  (valid-scalars? a2 (basic-collection-spec-from-data a2)) ; true


  (def a3 [11 #{22 [33 #{44}]}])
  (valid-collections? a3 (basic-collection-spec-from-data a3)) ; false


  (def a4 [[11 #{22 [33 #{44}]}] 99])
  (valid-collections? a4 (basic-collection-spec-from-data a4)) ; false
  )