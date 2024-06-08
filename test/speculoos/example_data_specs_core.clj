(ns speculoos.example-data-specs-core
  (:require [speculoos.core :refer :all]
            [speculoos.utility :refer :all]))


;;;; examples for scalar spec testing


(def simple-coll [11 "abc" :foo])
(def simple-spec [int? string? keyword?])


(def nested-coll [11 ["abc" [:foo]] 22 \c])
(def nested-spec [int? [string? [keyword?]] int? char?])


(def heterogeneous-coll [11 {:a 22 :b "foo" :c (list 33 true)} #{44} #{"abc" "foo" "hello"}])
(def heterogeneous-spec [int? {:a int? :b string? :c (list int? boolean?)} #{int?} #{string?}])


(def data-with-set-1 {:a [11 22 33] :b #{33} :c {:d [44 {:e "abc"} 55] :f {:g :foo}}})
(def spec-with-set-1 {:a [int? int? int?] :b #{int?} :c {:d [int? {:e string?} int?] :f {:g keyword?}}})


(def data-with-set-2 (list [11 :foo] (list 22 :foo) {:a "abc" :b \c} #{33}))
(def spec-with-set-2 (list [int? keyword?] (list int? keyword?) {:a string? :b char?} #{int?}))


;; running (valid?) on set elements [perhaps not recommended]
(def data-with-set-3 [11 #{[22 "abc" \c]
                           [33 "def" \d]
                           [44 "ghi" \e]} :kw])
(def spec-for-set-members [int? string? char?])
(def spec-with-set-3 [int? #{#(valid-scalar-spec? % spec-for-set-members)} keyword?])


(def third-is-11-more-than-first #(= (+ 11 (=1st %))
                                     (=3rd %)))
(def data-1 [11 [22 "abc" 33] \c])
(def scalar-spec-1 [int? [int? string? int?] char?])
(def coll-spec-1 [[third-is-11-more-than-first]])

(def int-kw-int-str? [int? keyword? int? string?])
(def all-greater-than-ten? (fn [v] (every? #(> % 10) v)))
(def data-2 {:a 11 :b [22 :foo 33 "abc"] :c [22 :foo 33 "abc"] :d {:e [44 55 66]}})
(def scalar-spec-2 {:a int? :b int-kw-int-str? :c int-kw-int-str? :d {:e [int? int? int?]}})
(def coll-spec-2 {:two-eq-vecs? #(= (:b %) (:c %)) :d {:e [all-greater-than-ten?]}})

(def data-list-1 '(11 (22 "abc" :foo)))
(def spec-list-1 (list int? (list int? string? keyword?)))

(def data-list-2 '(11 [22 "abc"] (:foo \c) {:a nil} 33))
(def spec-list-2 (list int? [int? string?] (list keyword? char?) {:a nil?} int?))

(def days #{:Sunday :Monday :Tuesday :Wednesday :Thursday :Friday :Saturday})
(def colors #{:red :orange :yellow :green :blue :indigo :violet})
(def card-suits #{:clubs :diamonds :hearts :spades})

(def data-1-set-predicate [11 :Monday \c :green "foo"])
(def spec-1-set-predicate [int? days char? colors string?])

(def data-2-set-predicate [99 :Monday #{:a :b :c} :Bibturday #{:ignored}])
(def spec-2-set-predicate [int? days #{keyword?} days #{:keyword?}])


;;;; examples for collection spec testing

(def ord-vec-example [[11 [22] 33 [44]]
                      [[55] 66 [77]]
                      88
                      [[[99]]]])
(def ord-list-example '((11) 22 (33 (44) (55) (66)) (77)))
(def ord-map-example {:a 11 :b {:c 22 :d {:e 33}}})
(def ord-set-example #{11 [22 33 [44]]})
(def ord-heterogeneous-example ['(11) 22 [33 [44] 55 [66]] 77 {:a 88 :b [99 [111] 222 [333]]}])


(def homogeneous-list-test '(11 (22) 33 (44) 55 (66 (77) (88) (99))))
(def homogeneous-list-path [5 3 0])
(def vector-list-mix [11 '(22) 33 [44] 55 '(66 (77) [88] (99))])
(def vector-list-mix-path [5 3 0])
(def homogeneous-map {:a 11 :b 22 :c {:d 33 :e {:f 44 :g 55}}})
(def homogeneous-map-path [:c :e :g])
(def heterogeneous-coll-1 [11 {:a 22} '(33) 44 [55 '(66) {:b 77} [88 [99]]]])
(def heterogeneous-coll-1-path [4 3 1 0])
(def heterogeneous-coll-2 [11 '(22) 33 {:a 44} {:b "foo" :c [55 '(66) [77] 88 {:d 99}]}])
(def heterogeneous-coll-2-path [4 :c 4 :d])
(def heterogeneous-coll-3 [11 '(22) {:a 22 :b '([33] 44 [55 [66] [77] [88] [[99]] 333] 444)} 111 [222]])
(def heterogeneous-coll-3-path [2 :b 2 4 0 0])
(def map-with-integer-indexes {11 :val11 22 :val22 33 {44 :val44 55 {66 :val66} 77 :val77} 88 :val88})
(def map-with-integer-indexes-path [33 55 66])
(def map-with-composite-indexes {[11 22] :val1122 [33 44] :val3344 [55 66] {[77 88] :val7788}})
(def map-with-composite-indexes-path [[55 66] [77 88]])
(def set-not-along-path [#{11} 22 #{33} [#{44} 55 [66 [77] [88] [99]]]])
(def set-not-along-path-path [3 2 3 0])
(def set-along-the-way [11 '(22) 33 '((44) 55 [66] 77 #{88 [[99]]})])
(def set-along-the-way-path [3 4 [[99]] 0 0])

(def combo-data-1 [[11] 22 [33] 44 [[55] 66 [77] 88 [99]]])
(def combo-path-1 [4 4 0])

(def combo-data-2 {:a 11 :b ['(22) 33 '(33) {:c 44 :d [[55] 66 [77] 88 [99]]}]})
(def combo-path-2 [:b 3 :d 4 0])


;; sample set 1
(def test-data-1 [11 22 33 [44 :foo :bar :foo] 55])

(def third-is-3X-first? #(= (* 3 (=1st %)) (=3rd %)))
(def second-and-fourth-same-keyword? #(and (keyword? (=2nd %))
                                           (keyword? (=4th %))
                                           (= (=2nd %)
                                              (=4th %))))
(def fifth-is-33-more-than-second? #(= (+ 33 (=2nd %))
                                       (=5th %)))

(def test-data-1-coll-spec [third-is-3X-first? fifth-is-33-more-than-second? [second-and-fourth-same-keyword?]])


;; sample set 2
(def test-data-2 [[11] 22 [33] 44 '(55) [66 [77] 88 [99]]])

(def first-and-third-vecs? #(and (vector? (=1st %))
                                 (vector? (=3rd %))))
(def fifth-a-list? #(list? (=5th %)))
(def sixth-a-vec? #(vector? (=6th %)))
(def sixth-has-four-elements? #(= 4 (count (=6th %))))
(def has-two-lists? #(= 2 (count (filter list? %))))
(def has-two-vecs? #(= 2 (count (filter vector? %))))
(def third-is-22-more-than-first? #(= (+ 22 (=1st %))
                                      (=3rd %)))

(def test-data-2-coll-spec [fifth-a-list?
                            sixth-a-vec?
                            []
                            []
                            '()
                            [has-two-vecs?
                             has-two-vecs?
                             third-is-22-more-than-first?]
                            sixth-has-four-elements?])


;; sample set 3
(def test-data-3  [11 '(22) {:a 33 :b [44 55]} 66 [77 [88 [99]]]])

(def third-a-map? #(map? (=3rd %)))
(def two-element-vec? #(and (vector? %)
                            (= 2 (count %))))
(def fourth-is-6X-first? #(= (* 4 (=1st %))
                             (=4th %)))
(def second-is-11-more-than-first? #(= (+ 11 (=1st %))
                                      (=2nd %)))

(def test-data-3-coll-spec ['()
                            {:b [two-element-vec?
                                 second-is-11-more-than-first?]}
                            third-a-map?
                            [two-element-vec?
                             [two-element-vec?]]])


;; sample set 4
(def test-data-4 [11 (list 22) 33 (list 44)])
(def test-data-4-coll-spec [#(and (list? (=2nd %))
                        (list? (=4th %)))
                  #(= (* 3 (=1st %))
                      (=3rd %))
                  #(> 5 (count %))
                  ])

;; sample set 5
(def test-data-5 {:a [11 22 {:b 33} [44 55]] :c [66 #{77}] :d (list 88 99)})
(def test-data-5-coll-spec {:a [#(= 4 (count %))
                      #(map? (=3rd %))
                      #(= 33 (:b (get % 2)))
                      {}
                      [#(= (+ 11 (=1st %))
                           (=2nd %))]
                      ]
                  :c [#(= 66 (=1st %))]
                  :both-vecs? #(every?  #{:a :c :d} (keys %))
                  :d '(#(= 2 (count %)))})

;; data set 6
(def test-data-6 {:a [11 22 33] :b [[44] [55] [66]]})
(def test-data-6-coll-spec {:a [#(= 3 (count %))
                                #(= (* 3 (=1st %))
                                    (=3rd %))]
                            :b [#(every? vector? %)
                                (fn [v] (every? #(= 1 (count %)) v))]
                            :check-vals #(= (type (:a %))
                                            (type (:b %)))})

;; (validate-collection-spec test-data-6 test-data-6-coll-spec)
  

;; data set 7
(def test-data-7 ['(11) 22 '(33) 44 '(55)])
(def test-data-7-coll-spec [#(= 3 (count (filter list? %)))
                    #(and (even? (=2nd %))
                          (even? (=4th %)))
                    #(< (count %) 10)])

;; (validate-collection-spec test-data-7 test-data-7-coll-spec)


;; data set 8
(def test-data-8 [nil '() 11 [] "abc" {:a [22 33]
                                       [44] [55 66]}])

(def second-fourth-sixth-colls? #(and (coll? (=2nd %))
                                      (coll? (=4th %))
                                      (coll? (=6th %))))

(def map-vals-vecs? #(and (vector? (:a %))
                          (vector? (get % [44]))))

(def test-data-8-coll-spec [second-fourth-sixth-colls?
                            '()
                            []
                            {:check-map-vals map-vals-vecs?}])

;; (validate-collection-spec test-data-8 test-data-8-coll-spec)


;; data set 9, maps with non-keyword indexes
(def test-data-9 {99 :val99
                  [11] :valeleven
                  [22 33] :val4vec
                  #{"set-as-a-key"} :val4set
                  [44] [55 66 77]
                  [88] [99 111 222]})


(def test-data-9-coll-spec {:check-eleven  #(keyword? (get % [11]))
                            :check-set-key #(keyword? (get % #{"set-as-a-key"}))
                            :check-vec-as-keys #(and (vector? (get % [44]))
                                                     (vector? (get % [88])))})

;; (validate-collection-spec test-data-9 test-data-9-coll-spec)


;; data set 10, more maps with non-keyword indexes, spec has order shuffled
(def test-data-10 {99 [:ninety-nine]
                   [] [:empty-vec]
                   [11] [:eleven]
                   [22 33] [:twenty-two-twenty-three]
                   #{44} [:set-as-a-key]
                   "abc" [:string-as-a-key]})

(def test-data-10-coll-spec {#{44} [#(= :set-as-a-key (=1st %))]
                             [11] [#(and (< (count %) 2)
                                         (= :eleven (=1st %)))]
                             [] [#(and (vector? %)
                                       (keyword? (get % 0)))]
                             99 [#(and (vector? %)
                                       (= 1 (count %)))]

                             "abc" [#((complement int?) (=1st %))]
                             
                             [22 33] [#(and (vector? %)
                                            (= :twenty-two-twenty-three (=1st %)))]})

;; (validate-collection-spec test-data-10 test-data-10-coll-spec)


;; data set 11, focus on handling lists
(def test-data-11 '(11 (22) 33 (44) 55 (66) 77))
(def test-data-11-coll-spec '(#(and (int? (=1st %))
                                    (int? (=3rd %))
                                    (int? (=5th %))
                                    (int? (=7th %)))
                              #(and (list? (=2nd %))
                                    (list? (=4th %))
                                    (list? (=6th %)))))

;; (valid-collection-spec? test-data-11 test-data-11-coll-spec)


;; test data for non-terminating sequences
(def test-non-terminating-data-1 [[11] [22] [33] [44] [55]])
(def test-non-terminating-spec-1 (repeat 9 [#(and (= 1 (count  %))
                                                  (int? (=1st %)))]))

(def test-non-terminating-data-2 (range 1 99))
(def test-non-terminating-spec-2 [#(every? int? %) (constantly true) (constantly true)])

(def all-non-colls-string? (fn [x] (every? string? (filter #((complement coll?) %) x))))
(def length-3-and-all-ints? #(and (= 3 (count %))
                                  (every? int? %)))

(def test-non-terminating-data-3 (take 15 (cycle ["abc" [11 22 33] "xyz" [44 55 66] "foo"])))
(def test-non-terminating-spec-3 [all-non-colls-string?
                                  [length-3-and-all-ints?]
                                  (constantly true)
                                  (constantly true)
                                  (constantly true)])