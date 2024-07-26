(ns speculoos.speculoos-recipes
  (:require [clojure.test :as test]
            [clojure.set :as set]
            [speculoos.core :refer [assoc-vector-tail validate-scalars valid-scalars?]]))


(validate-scalars [] []) ; []
(validate-scalars [5] []) ; []


(validate-scalars [1 2 3 {:a "one" :b "two"}]
          [int? int? int? {:b int? :a string? }])
;;[{:path [0], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [1], :value 2, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2], :value 3, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3 :a], :value "one", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [3 :b], :value "two", :predicate #function[clojure.core/int?], :valid? false}]


(validate-scalars [{:a 1, :b "bee", :c true}]
               [{:a int? :b string? :c boolean?}])
;;[{:path [0 :a], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [0 :b], :value "bee", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [0 :c], :value true, :predicate #function[clojure.core/boolean?], :valid? true}]


(validate-scalars [:a :b :c [:d :e [:f]] :g [[:h]] '(:i :j)]
               [keyword? keyword? keyword? [keyword? keyword? [keyword?]] keyword? [[keyword?]] (list keyword? keyword?)])
;;[{:path [0], :value :a, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [1], :value :b, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [2], :value :c, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [3 0], :value :d, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [3 1], :value :e, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [3 2 0], :value :f, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [4], :value :g, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [5 0 0], :value :h, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [6 0], :value :i, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [6 1], :value :j, :predicate #function[clojure.core/keyword?], :valid? true}]


(validate-scalars [1.1 2.2 {:a 3.3 :b 4.4} 5.5]
               [float? string? {:a float?
                                :b float?} float?])
;;[{:path [0], :value 1.1, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [1], :value 2.2, :predicate #function[clojure.core/string?--5475], :valid? false}
;; {:path [2 :a], :value 3.3, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [2 :b], :value 4.4, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [3], :value 5.5, :predicate #function[clojure.core/float?], :valid? true}]


(validate-scalars {:a [1.1 2.2]
                :b {:c 3.3
                    :d 4.4}
                :e (list 5.5 6.6 7.7)
                :f "foo"
                :g [8.8 {:h "aych"
                         :i "eye"}]}
               {:a [float? int?]
                :b {:c float?
                    :d float?}
                :e (list float? float? int?)
                :f string?
                :g [float? {:h string?
                            :i int?}]})
;;[{:path [:a 0], :value 1.1, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:a 1], :value 2.2, :predicate #function[clojure.core/int?], :valid? false}
;; {:path [:b :c], :value 3.3, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:b :d], :value 4.4, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:e 0], :value 5.5, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:e 1], :value 6.6, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:e 2], :value 7.7, :predicate #function[clojure.core/int?], :valid? false}
;; {:path [:f], :value "foo", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [:g 0], :value 8.8, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:g 1 :h], :value "aych", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [:g 1 :i], :value "eye", :predicate #function[clojure.core/int?], :valid? false}]



(validate-scalars [0.1 [[1.2 3.4] 5.6] 7.8 [[[9.0]] {:one "one" :two "two"}]]
               [float? [[float? float?] float?] float? [[[float?]] {:one string? :two string?}]])
;;[{:path [0], :value 0.1, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [1 0 0], :value 1.2, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [1 0 1], :value 3.4, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [1 1], :value 5.6, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [2], :value 7.8, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [3 0 0 0], :value 9.0, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [3 1 :one], :value "one", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [3 1 :two], :value "two", :predicate #function[clojure.core/string?--5475], :valid? true}]


 (validate-scalars [[[1.1] 2.2] 3.3]
                [[[float?] float?] float?])
;;[{:path [0 0 0], :value 1.1, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [0 1], :value 2.2, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [1], :value 3.3, :predicate #function[clojure.core/float?], :valid? true}]


(validate-scalars [[[1.1] 2.2] 3.3 {:one "one"}]
               [[[float?] float?] float? {:one string?}])
;;[{:path [0 0 0], :value 1.1, :predicate #function[clojure.core/float?], :valid? true}
;;{:path [0 1], :value 2.2, :predicate #function[clojure.core/float?], :valid? true}
;;{:path [1], :value 3.3, :predicate #function[clojure.core/float?], :valid? true}
;;{:path [2 :one], :value "one", :predicate #function[clojure.core/string?--5475], :valid? true}]


;;; Item in one, but not the other...
;; deliberately short spec
(validate-scalars [1 2 3]
               [int? int?])
;;[{:path [0], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [1], :value 2, :predicate #function[clojure.core/int?], :valid? true}]


;; deliberately short spec on a nested element (Note: the nested spec terminates, but the outer mapping continues to test the predicates at the higher levels.)
(validate-scalars [1 2 [3 4] 5 6]
               [int? int? [int?] int? int?])
;; [{:path [0], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [1], :value 2, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2 0], :value 3, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3], :value 5, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [4], :value 6, :predicate #function[clojure.core/int?], :valid? true}]


;; deliberately over-long spec
(validate-scalars [1 2 [3 4] 5 6]
               [int? int? [int? int?] int? int? string? boolean])
;;[{:path [0], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [1], :value 2, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2 0], :value 3, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2 1], :value 4, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3], :value 5, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [4], :value 6, :predicate #function[clojure.core/int?], :valid? true}]


;; explore compound predicates
(def greater-than-one #(>= % 1))
(def equal-to-three #(= % 3))
(def intgr? #(int? %))
(def odd-num? #(odd? %))
(def float-element? #(float? %))
(def string-element? #(string? %))


(def int-and-odd? #(and (int? %)
                        (odd? %)))
(def float-greater-than-one #(and (float? %)
                                  (>= % 1)))
(def string-or-equal-three #(or (string? %)
                                (= % 3)))

(validate-scalars [1 2.0 3]
               [int-and-odd?
                float-greater-than-one
                string-or-equal-three])
;;[{:path [0], :value 1, :predicate #function[speculoos.map-indexed/int-and-odd?], :valid? true}
;; {:path [1], :value 2.0, :predicate #function[speculoos.map-indexed/float-greater-than-one], :valid? true}
;; {:path [2], :value 3, :predicate #function[speculoos.map-indexed/string-or-equal-three], :valid? true}]


(validate-scalars [1.0 "abc"]
               [#(and (float? %)
                      (= 1.0))
                #(or (int? %)
                     (string? %))])
;;[{:path [0], :value 1.0, :predicate #function[speculoos.map-indexed/eval9314/fn--9315], :valid? true}
;; {:path [1], :value "abc", :predicate #function[speculoos.map-indexed/eval9314/fn--9318], :valid? true}]


;; composing: Look, Ma, no macros!
(def vec-a [1 2 3])
(def vec-b [4 5 6 vec-a])

(def spec-a [int? int? int?])
(def spec-b [int? int? int? spec-a])

(validate-scalars vec-b spec-b)
; [{:path [0], :value 4, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [1], :value 5, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2], :value 6, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3 0], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3 1], :value 2, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3 2], :value 3, :predicate #function[clojure.core/int?], :valid? true}]


(comment 
  (def sub-vec (map #(* % %) (range 0 5)))
  (def sub-vec-specs (repeat int?))

  (validate-scalars sub-vec sub-vec-specs))
;;[{:path [0], :value 0, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [1], :value 1, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2], :value 4, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [3], :value 9, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [4], :value 16, :predicate #function[clojure.core/int?], :valid? true}]


;; don't care what the thing is
(validate-scalars [:a "abc" 3.14159 {:c 2.3 :d 4.5} true]
               [keyword? string? any? {:c any? :d float?} true?])
;;[{:path [0], :value :a, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [1], :value "abc", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [2], :value 3.14159, :predicate #function[clojure.core/any?], :valid? true}
;; {:path [3 :c], :value 2.3, :predicate #function[clojure.core/any?], :valid? true}
;; {:path [3 :d], :value 4.5, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [4], :value true, :predicate #function[clojure.core/true?], :valid? true}]



;; data and spec hash-map keys created in different order
(validate-scalars [1.1 "abc" {:a 4 :b true} [:c 5.5]]
               [float? string? {:b boolean? :a int?} [keyword? float?]])
;;[{:path [0], :value 1.1, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [1], :value "abc", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [2 :a], :value 4, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [2 :b], :value true, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [3 0], :value :c, :predicate #function[clojure.core/keyword?], :valid? true}
;; {:path [3 1], :value 5.5, :predicate #function[clojure.core/float?], :valid? true}]


;; What if data and spec keys don't match?
(validate-scalars {:a 1.1 :b 2 :x "foo"}
               {:c float? :d int? :y string?}) ;; NullPointerException

(validate-scalars {:a 1.1 :b 2 :x "foo"}
               {:b int? :x string? :a float?})
;;[{:path [:a], :value 1.1, :predicate #function[clojure.core/float?], :valid? true}
;; {:path [:b], :value 2, :predicate #function[clojure.core/int?], :valid? true}
;; {:path [:x], :value "foo", :predicate #function[clojure.core/string?--5475], :valid? true}]

(comment
  ;; modifying a spec using the full power of Clojure's extensive data structure library
  (valid-scalars? (butlast [1 2 3 4 5])
          (butlast [int? int? int? int? string?])) ; true
  ;; or more succintly, taking advantage of the fact that 'missing' specs aren't checked
  (valid-scalars? [1 2 3 4 5]
          (butlast [int? int? int? int? string?]))) ; true

;; speculoos leverages three powerful concepts
;; 1. Clojurists existing knowledge, experience, and judgement in using Clojure's extensive data maniuplation library
;; 2. a small set of conventions that can be conveyed by writting on a business card
;; 3. a single 21-line function based on a naive map+reduce that simulataneously walks through a data form and a spec form,
;;      at each node, applying a predicate from spec to a datum element from data

(comment 
  ;; illustrative example
  ;; simple data composition, defined sometime during the distant past...
  (def crazy-data (as-> m (repeat 5.5)
                    (into [] m)
                    (assoc m 42 :life-universe-spice-cookies)
                    (assoc m 1025 {:a "foo" :b ["bar"]})))

  (def x (into [] (repeat 5.5)))

  (def crazy-spec (assoc-in (repeat float?)) 12 boolean))
;; both bog-standard Clojure data structures that can be lazy, and arbitrarily nested

;; later, during production use, straight-forward Clojore-style manipulation





;;; optionality in vectors
;; optionality in hash-maps is trivial: simply leave out the :key in the spec; if the key exists in the spec, that datum must conform
;; vectors have a more complex notion of optionality: What does it mean for the 'third elemenet, if it exists, must be an int?'
;; most common case is simply to have the optional elements at the tail
;; the spec then becomes...
(def actual-vector [7 8 9 10])
(def vector-spec-1 [int? int? int? int?])
(def vector-spec-2 [int? int? int?])
(or (valid-scalars? actual-vector vector-spec-1)
    (valid-scalars? actual-vector vector-spec-2))

;; For those cases when you need to optionally spec the interior (or the head) of a vector, you have have to full power of Clojure

;; some data with possible booleans at indexes 1-2
(def vector-data-optional-middle [3 7 true false 3.3 7.7])

;; some components
(def vector-spec-head [int? int?])
(def vector-spec-tail [float? float?])
(def vector-spec-optional-gooey-middle [boolean? boolean?])

(def vector-spec-option-1 (concat vector-spec-head
                                  vector-spec-tail))

(def vector-spec-option-2 (concat vector-spec-head
                                  vector-spec-optional-gooey-middle
                                  vector-spec-tail))


(defn middle-booleans?
  [data spec]
  (or (valid-scalars? vector-data-optional-middle vector-spec-option-1)
      (valid-scalars? vector-data-optional-middle vector-spec-option-2)))

;; Here, we've written these specs out by hand, but you could easily imagine programmatically generating these. It's straight-up Clojure data structures.
;; Could wrap this expression in a nice little named function.
;; And! You can inspect the data and spec with your eyeballs: there's no weirdo opaque 'spec' object that can't be viewed nor manipulated.
