(ns speculoos.utility-test
  (:require
   [clojure.test :refer [are is deftest testing run-tests function?]]
   [clojure.test.check.generators :as gen]
   [re-rand :refer [re-rand]]
   [speculoos.core :refer [all-paths valid-scalars? valid-collections? valid?
                           only-non-collections]]
   [speculoos.fn-in :refer [get*]]
   [speculoos.utility :refer :all]))


(def test-data-1 [11 "abc" :foo])
(def test-spec-1 [int? string? keyword?])


(deftest only-values-tests
  (are [x y] (= x y)
    [[]] (only-values [])
    [[11 "abc" :foo] 11 "abc" :foo] (only-values test-data-1)
    [[int? string? keyword?] int? string? keyword?] (only-values test-spec-1)
    [[11 [22 [33]]] 11 [22 [33]] 22 [33] 33] (only-values [11 [22 [33]]])))


(deftest only-paths-tests
  (are [x y] (= x y)
    [[]] (only-paths [])
    [[] [0] [1] [2]] (only-paths test-data-1)
    [[] [0] [1] [2]] (only-paths test-spec-1)
    [[] [0] [1] [1 0] [1 1] [1 1 0]] (only-paths [11 [22 [33]]])))


(deftest paths-only-in-A-not-in-B-tests
  (are [x y] (= x y)
    #{} (paths-only-in-A-not-in-B [] [])
    #{[0]} (paths-only-in-A-not-in-B [11] [])
    #{} (paths-only-in-A-not-in-B [] [22])
    #{[1 1] [1 1 0]} (paths-only-in-A-not-in-B [11 [22 [33]]] [44 [55] 66])
    #{[2]} (paths-only-in-A-not-in-B [44 [55] 66] [11 [22 [33]]])))


(deftest paths-in-both-A-and-B-tests
  (are [x y] (= x y)
    #{[]} (paths-in-both-A-and-B [] [])
    #{[]} (paths-in-both-A-and-B [] [11])
    #{[] [0]} (paths-in-both-A-and-B [11] [22])
    #{[] [0] [1] [1 0] [2]} (paths-in-both-A-and-B [11 [22] 33] [44 [55] 66])))


(deftest keep-only-elements-in-set-tests
  (are [x y] (= x y)
    (keep-only-elements-in-set (all-paths [11 22 33 44 55 66]) #{[1] [3] [5]} :path)
    [{:path [1], :value 22} {:path [3], :value 44} {:path [5], :value 66}]))


(deftest data-spec-set-analysis-tests
  (are [x y] (= x y)
    #{} ((data-spec-set-analysis []) :set-paths)
    #{[1] [3]} ((data-spec-set-analysis [11 #{22} 33 #{44}]) :set-paths)
    #{[]} ((data-spec-set-analysis #{11 22 33}) :set-paths)
    #{[1 :y] [2] [4]} ((data-spec-set-analysis [11 {:x 22 :y #{33}} #{44} 55 #{66}]) :set-paths)))


(deftest data-scalars-in-a-set-with-a-predicate-tests
  (testing "no scalars within a set"
    (are [x] (empty? x)
      (data-scalars-in-a-set-with-a-predicate #{} #{})
      (data-scalars-in-a-set-with-a-predicate [] [])
      (data-scalars-in-a-set-with-a-predicate [11   22   [33   #{}]]
                                              [int? int? [int? #{}]])))
  (testing "some don't have a predicate"
    (are [x y] (= x y)
      (data-scalars-in-a-set-with-a-predicate #{11} #{})
      '()

      (data-scalars-in-a-set-with-a-predicate [11   #{22}     33]
                                              [int? #{int?}     ])
      '({:path [1 22], :value 22})

      (data-scalars-in-a-set-with-a-predicate [11   #{22} 33   #{44}  ]
                                              [int? #{}   int? #{int?}])
      '({:path [3 44], :value 44})

      (data-scalars-in-a-set-with-a-predicate [11   #{22}   33   #{44}   55   {:x 66   :y [77   #{88}  ]}]
                                              [int? #{}     int? #{int?} int? {:x int? :y [int? #{int?}]}])
      '({:path [3 44], :value 44}
        {:path [5 :y 1 88], :value 88})))

  (testing "all do have a predicate"
    (are [x y] (= x y)
      (data-scalars-in-a-set-with-a-predicate #{11  }
                                              #{int?})
      '({:path [11], :value 11})

      (data-scalars-in-a-set-with-a-predicate [11   #{22}   33  ]
                                              [int? #{int?} int?])
      '({:path [1 22], :value 22})

      (data-scalars-in-a-set-with-a-predicate [11   #{22}   33   #{44}]
                                              [int? #{int?} int? #{int?}])
      '({:path [1 22], :value 22}
        {:path [3 44], :value 44})

      (data-scalars-in-a-set-with-a-predicate [11   #{22}   33   #{44}   55   {:x 66   :y [77   #{88}  ]}]
                                              [int? #{int?} int? #{int?} int? {:x int? :y [int? #{int?}]}])
      '({:path [1 22], :value 22}
        {:path [3 44], :value 44}
        {:path [5 :y 1 88], :value 88}))))


(deftest data-scalars-in-a-set-without-a-predicate-tests
  (testing "no scalars within a set"
    (are [x] (empty? x)
      (data-scalars-in-a-set-without-a-predicate #{} #{})
      (data-scalars-in-a-set-without-a-predicate [] [])
      (data-scalars-in-a-set-without-a-predicate [11   22   [33   #{}]]
                                                 [int? int? [int? #{}]])))

  (testing "some don't have a predicate"
    (are [x y] (= x y)
      (data-scalars-in-a-set-without-a-predicate #{11} #{})
      '({:path [11], :value 11})

      (data-scalars-in-a-set-without-a-predicate [11   #{22} 33   #{44}  ]
                                                 [int? #{}   int? #{int?}])
      '({:path [1 22], :value 22})

      (data-scalars-in-a-set-without-a-predicate [11   #{22}   33   #{44}   55   {:x 66   :y [77   #{88}]}]
                                                 [int? #{}     int? #{int?} int? {:x int? :y [int? #{}  ]}])
      '({:path [1 22], :value 22}
        {:path [5 :y 1 88], :value 88})))

  (testing "all do have a predicate"
    (are [x] (empty? x)
      (data-scalars-in-a-set-without-a-predicate #{11} #{int?})
      (data-scalars-in-a-set-without-a-predicate [11 #{22} 33] [int? #{int?}     ])
      (data-scalars-in-a-set-without-a-predicate [11 #{22} 33] [int? #{int?} int?])
      (data-scalars-in-a-set-without-a-predicate [11   #{22}   33   #{44}  ]
                                                 [int? #{int?} int? #{int?}])
      (data-scalars-in-a-set-without-a-predicate [11   #{22}   33   #{44}   55   {:x 66   :y [77   #{88}  ]}]
                                                 [int? #{int?} int? #{int?} int? {:x int? :y [int? #{int?}]}]))))


(deftest scalars-without-predicates-tests
  (testing "no scalars"
    (are [x] (empty? x)
      (scalars-without-predicates #{} #{})
      (scalars-without-predicates [] [])
      (scalars-without-predicates '() '())
      (scalars-without-predicates {} {})))
  (testing "all scalars do have a predicate"
    (are [x] (empty? x)
      (scalars-without-predicates [11 #{22}] [int? #{int?}])
      (scalars-without-predicates #{11 22 33} #{int?})
      (scalars-without-predicates [11   {:x 22   :y [33   44   #{55  }]} #{66  }]
                          [int? {:x int? :y [int? int? #{int?}]} #{int?}])))
  (testing "no scalars within a set"
    (are [x] (empty? x)
      (scalars-without-predicates [11 22 33] [int? int? int?])
      (scalars-without-predicates [11 22 #{}] [int? int? #{}])))
  (testing "some scalars within a set do not have a predicate"
    (are [x y] (= x y)
      (scalars-without-predicates [11 22 33] [int? int?])
      [{:path [2], :value 33}]

      (scalars-without-predicates {:a 11 :b [22 [33]]} {:a int? :b [int?]})
      [{:path [:b 1 0], :value 33}]

      (scalars-without-predicates [11 [22 [33]]] [int? [int?] int?])
      [{:path [1 1 0], :value 33}]

      (scalars-without-predicates #{11 22 33} #{})
      [{:path [33], :value 33} {:path [22], :value 22} {:path [11], :value 11}]

      (scalars-without-predicates [11   {:x 22   :y [33   44   #{55}]} #{66}]
                          [int? {:x int? :y [int? int? #{}  ]} #{}  ])
      [{:path [1 :y 2 55], :value 55} {:path [2 66], :value 66}])))


(deftest scalars-with-predicates-tests
  (testing "empty colls"
    (are [x] (empty? x)
      (scalars-with-predicates [] [])
      (scalars-with-predicates {} {})
      (scalars-with-predicates '() '())
      (scalars-with-predicates #{} #{})))
  (testing "predicates for non-existing scalars"
    (are [x] (empty? x)
      (scalars-with-predicates [] [int?])
      (scalars-with-predicates {} {:x int?})
      (scalars-with-predicates '() '())
      (scalars-with-predicates #{} #{int?})))
  (testing "all scalars have a predicate"
    (are [x y] (= x y)
      (scalars-with-predicates [11] [int?])
      [{:path [0], :value 11}]

      (scalars-with-predicates [11 22 33] [int? int? int?])
      [{:path [0], :value 11}
       {:path [1], :value 22}
       {:path [2], :value 33}]

      (scalars-with-predicates [11 [22]] [int? [int?]])
      [{:path [0], :value 11}
       {:path [1 0], :value 22}]

      (scalars-with-predicates {:a 11 :b 22} {:a int? :b int?})
      [{:path [:a], :value 11}
       {:path [:b], :value 22}]

      (scalars-with-predicates [11] [int?])
      [{:path [0], :value 11}]

      (scalars-with-predicates #{99} #{int?})
      [{:path [99], :value 99}]

      (scalars-with-predicates [11   #{22}  ]
                       [int? #{int?}])
      [{:path [0], :value 11}
       {:path [1 22], :value 22}]

      (scalars-with-predicates #{11 22 33} #{int?})
      [{:path [33], :value 33}
       {:path [22], :value 22}
       {:path [11], :value 11}]

      (scalars-with-predicates [11   {:x [22   #{33}  ]}]
                       [int? {:x [int? #{int?}]}])
      [{:path [0], :value 11}
       {:path [1 :x 0], :value 22}
       {:path [1 :x 1 33], :value 33}]))
  (testing "some scalars lack a predicate"
    (are [x y] (= x y)
      (scalars-with-predicates [11 22 33] [int?])
      [{:path [0], :value 11}]

      (scalars-with-predicates {:x 11 :y 22} {:x int?})
      [{:path [:x], :value 11}]

      (scalars-with-predicates [11 #{22} 33]
                       [int? #{} int?])
      [{:path [0], :value 11}
       {:path [2], :value 33}]

      (scalars-with-predicates #{11 22 33} #{})
      [])))


(deftest predicates-without-scalars-tests
  (testing "empty data and spec"
    (are [x] (empty? x)
      (predicates-without-scalars [] [])
      (predicates-without-scalars {} {})
      (predicates-without-scalars '() '())
      (predicates-without-scalars #{} #{})))
  (testing "some scalars do not have a predicate"
    (are [x] (empty? x)
      (predicates-without-scalars [11] [])
      (predicates-without-scalars {:x 99} {})
      (predicates-without-scalars #{99} #{})
      (predicates-without-scalars [11 #{22}] [#{}])))
  (testing "all predicates have a scalar"
    (are [x] (empty? x)
      (predicates-without-scalars [11] [int?])
      (predicates-without-scalars {:x 11 :y 22}
                                  {:x int? :y int?})
      (predicates-without-scalars #{11 22 33} #{int?})
      (predicates-without-scalars [11   [22    #{33 44 55}]]
                                  [int? [char? #{string? }]])))
  (testing "some predicates lack a scalar"
    (are [x y] (= x y)
      (predicates-without-scalars [] [int?])
      [{:path [0], :value int?}]

      (predicates-without-scalars [11   22   33       ]
                                  [int? int? int? int?])
      [{:path [3], :value int?}]

      (predicates-without-scalars [11   [22   33]         ]
                                  [int? [int? int?] [int?]])
      [{:path [2 0], :value int?}]

      (predicates-without-scalars {:a 11          }
                                  {:a int? :b int?})
      [{:path [:b], :value int?}]

      (predicates-without-scalars [] [int?])
      [{:path [0], :value int?}]

      (predicates-without-scalars {} {:x int?})
      [{:path [:x], :value int?}]

      (predicates-without-scalars [11           ]
                                  [int? int? int])
      [{:path [1], :value int?}
       {:path [2], :value int}]

      (predicates-without-scalars {:x 11          }
                                  {:x int? :y int?})
      [{:path [:y], :value int?}]

      (predicates-without-scalars #{} #{int?})
      [{:path [int?], :value int?}]

      (predicates-without-scalars [11   [    ]]
                                  [int? [int?]])
      [{:path [1 0], :value int?}]

      (predicates-without-scalars [] [int? {:x char? :y [string? #{boolean?}]}])
      [{:path [0], :value int?}
       {:path [1 :x], :value char?}
       {:path [1 :y 0], :value string?}
       {:path [1 :y 1 boolean?], :value boolean?}]

      (predicates-without-scalars [11   #{}      [#{        }]]
                                  [int? #{char?} [#{string?}]])
      [{:path [1 char?], :value char?}
       {:path [2 0 string?], :value string?}])))


(deftest non-predicates-tests
  (are [x y] (= x y)
    [] (non-predicates [])
    [] (non-predicates [int?])
    [{:path [1], :value :kw}] (non-predicates [int? :kw])
    [{:path [:b], :value "foo"}] (non-predicates {:a int? :b "foo"})
    [{:path [1 :b 1], :value :problem}] (non-predicates [int? {:a char? :b [boolean? :problem]}])
    [{:path [2 :b 1 1 :c 0], :value :nope}] (non-predicates [int? string? {:a boolean? :b [int? [string? {:c [:nope]}]]}])))


(deftest all-specs-okay-tests
  (testing "specs are all (fn?)"
    (are [x] (true? x)
      (all-specs-okay [])
      (all-specs-okay [int?])
      (all-specs-okay [int? string? boolean?])
      (all-specs-okay {:a int? :b string?})
      (all-specs-okay [int? {:a string? :b [keyword? char?]}])))
  (testing "some specs aren't okay"
    (are [x y] (= x y)
      [{:path [0], :value :kw}] (all-specs-okay [:kw])
      [{:path [:b], :value :nah}] (all-specs-okay {:a int? :b :nah})
      [{:path [2 1 :b 1 0], :value :nope}] (all-specs-okay [int? {:a int?} [boolean? {:b [int? [:nope]]} char?]]))))


(deftest swap-non-predicates-tests
  (are [x y] (= x y)
    [] (swap-non-predicates [])
    [int?] (swap-non-predicates [int?])
    [any?] (swap-non-predicates [:kw])
    [int?] (swap-non-predicates [:kw] int?)
    [any? any?] (swap-non-predicates [:nope :nah])
    [int? boolean? string?](swap-non-predicates [int? :problem string?] boolean?)
    {:a int? :b any?} (swap-non-predicates {:a int? :b :kw})
    [int? [string? keyword?] {:a char? :b [keyword?]}] (swap-non-predicates [int? [string? :its-a-me] {:a char? :b [:this-one-too]}] keyword?)))


(deftest nil-out-tests
  (are [x y] (= x y)
    {:data [] :spec []} (nil-out [] [])
    {:data [11] :spec [int?]} (nil-out [11] [int?])
    {:data [11 nil] :spec [int? nil?]} (nil-out [11 22] [int? string?])
    {:data [11 [nil [33 "abc"]]] :spec [int? [nil? [int? string?]]]} (nil-out [11 [22 [33 "abc"]]] [int? [string? [int? string?]]])))


(deftest bed-of-procrustes-tests
  (are [x y] (= x y)
    {:data [], :spec []} (bed-of-procrustes [] [])
    {:data [11], :spec [int?]} (bed-of-procrustes [11] [int?])
    {:data [], :spec []} (bed-of-procrustes [11] [string?])
    {:data [11], :spec [int?]} (bed-of-procrustes [11 22] [int? string?])
    {:data [11 {:b 33}], :spec [int? {:b int?}]} (bed-of-procrustes [11 {:a 22 :b 33} 44] [int? {:a string? :b int?} string?])
    {:data {:a [11 []] :b [[44]]}, :spec {:a [int? []] :b [[int?]]}} (bed-of-procrustes {:a [11 [22]] :b [33 [44]]} {:a [int? [string?]] :b [string? [int?]]})))


(deftest apathetic-tests
  (are [x y] (= x y)
    [] (apathetic [] [])
    [int?] (apathetic [22] [int?])
    [any?] (apathetic [22] [string?])
    [int? any? int?] (apathetic [11 22 33] [int? string? int?])
    [int? [any? [int? [any?]]]] (apathetic [11 [22 [33 [44]]]] [int? [char? [int? [boolean?]]]])
    {:a int? :b [int? [any?]] :c any?} (apathetic {:a 11 :b [22 [33]] :c 44} {:a int? :b [int? [char?]] :c boolean?})))


(deftest adjust-demonstation-tests
  (are [x y] (= x y)
    [int? int? any?] (adjust-demonstration [int? int? :nope] any?)
    [int? int? int?] (adjust-demonstration [1 2 3] int?)
    {:a char? :b int? :c char?} (adjust-demonstration {:a 11 :b int? :c 22} char?)))


(comment
  (deftest sore-thumb-tests
    (testing "Beware: These test results may be dependent on the particular REPL pretty printing.
            I've seen suggestions that CIDER nREPL has a distinct way to print function names.
            ref: https://metaredux.com/posts/2019/12/05/pimp-my-print-method-prettier-clojure-built-in-types.html"
      (are [x y] (= x y)
        (with-out-str (sore-thumb [] []))
        "\"data: []\"\n\"spec: []\"\n"

        (with-out-str (sore-thumb [11] [int?]))
        "\"data: [_]\"\n\"spec: [_]\"\n"

        (with-out-str (sore-thumb [11] [char?]))
        "\"data: [11]\"\n\"spec: [#function[clojure.core/char?--5473]]\"\n"))))


(comment ;; Some nice (sore-thumb) examples, but doesn't make too much sense to test them.
  (sore-thumb [11 22 33] [int? char? int?])
  (sore-thumb [11 [22 [33 {:a 44 :b [55 66]}]]]
              [int? [int? [int? {:a int? :b [char? int?]}]]] '...)
  (sore-thumb [11 22 33] [int? char? int?] '...))


(deftest type-predicate-canonical-mappings-all-valid?
  (is (all-valid?-type-predicate-canonical type-predicate-canonical)))


(deftest element->predicate-tests
  (are [x y] (= x y)
    boolean? (element->predicate true)
    ;;#(= java.lang.Byte (type %)) (element->predicate (byte 0x67))
    char? (element->predicate \z)
    double? (element->predicate 1.0E32)
    keyword? (element->predicate :test)
    int? (element->predicate 1)
    ratio? (element->predicate 1/3)
    string? (element->predicate  "abc")
    symbol? (element->predicate 'foo)
    uuid? (element->predicate #uuid "06d9360b-aa54-429e-b0f0-0fdc43e75d75")
    fn? (element->predicate *)
    decimal? (element->predicate 2M)
    fn? (element->predicate identity)))


(deftest predicate->canonical-tests
  (are [x y] (= x y)
    true (predicate->canonical boolean?)
    true (predicate->canonical true?)
    \c (predicate->canonical char?)
    1.0E32 (predicate->canonical double?)
    :kw (predicate->canonical keyword?)
    42 (predicate->canonical int?)
    22/7 (predicate->canonical ratio?)
    "abc" (predicate->canonical string?)
    'speculoos/canonical-symbol (predicate->canonical symbol?)

    1.23 (predicate->canonical float?)
    1M (predicate->canonical decimal?)
    0 (predicate->canonical zero?)
    -1 (predicate->canonical neg?)
    2 (predicate->canonical even?)
    1E6 (predicate->canonical number?)
    10 (predicate->canonical pos-int?)
    3 (predicate->canonical odd?)
    reduce (predicate->canonical function?)
    + (predicate->canonical fn?)
    ;; ##NaN (predicate->canonical NaN?)
    ##Inf (predicate->canonical infinite?)
    nil (predicate->canonical nil?)
    true (predicate->canonical true?)
    false (predicate->canonical false?)))


(deftest predicate->rand-generated-tests
  (are [x] (true? x)
    (boolean? (predicate->rand-generated boolean?))
    (char? (predicate->rand-generated char?))
    (double? (predicate->rand-generated double?))
    (keyword? (predicate->rand-generated keyword?))
    (int? (predicate->rand-generated int?))
    (nat-int? (predicate->rand-generated nat-int?))
    (neg-int? (predicate->rand-generated neg-int?))
    (pos? (predicate->rand-generated pos?))
    (ratio? (predicate->rand-generated ratio?))
    (string? (predicate->rand-generated string?))
    (symbol? (predicate->rand-generated symbol?))
    (uuid? (predicate->rand-generated uuid?))

    (zero? (predicate->rand-generated zero?))
    (neg? (predicate->rand-generated neg?))
    (even? (predicate->rand-generated even?))
    (odd? (predicate->rand-generated odd? ))
    (pos-int? (predicate->rand-generated pos-int?))
    (NaN? (predicate->rand-generated NaN?))
    (infinite? (predicate->rand-generated infinite?))
    (nil? (predicate->rand-generated nil?))
    (true? (predicate->rand-generated true?))
    (false? (predicate->rand-generated false? ))

    (float? (predicate->rand-generated float?)) ;; Note: test.check does not appear to provide specific generators ...
    (decimal? (predicate->rand-generated decimal?)) ;; ...for non-double-float nor decimal; these default to the canonical examples
    ))


(deftest compare-vec-common-elements-tests
  (are [x] (true? x)
    (compare-vec-common-elements [] [])
    (compare-vec-common-elements [1 2 3] [1 2 3])
    (compare-vec-common-elements [1 2 3] [1])
    (compare-vec-common-elements [1] [1 2 3])
    (compare-vec-common-elements [1 2 3] [])
    (compare-vec-common-elements [] [1 2 3])
    (compare-vec-common-elements [1 2 3] [1 2 3 99])
    (compare-vec-common-elements [1 2 3] [1 2 3 nil nil nil nil nil 99]))
  (are [x] (false? x)
    (compare-vec-common-elements [1 2 3] [1 99 3])
    (compare-vec-common-elements [1] [99])))


;; round-tripping (spec-from-data)
(def data-no-lists-no-sets [1 2 3 [4 [5]] {:a "foo" :b \b :c true}])


(deftest spec-from-data-tests
  (testing "basic output"
    (are [x y] (= x y)
      []
      (spec-from-data [])

      [int?]
      (spec-from-data [11])

      [int? int? int?]
      (spec-from-data [11 22 33])

      [int? string? keyword?]
      (spec-from-data [11 "abc" :foo])

      {:a int? :b string? :c keyword? :d [int? int?]}
      (spec-from-data {:a 11 :b "foo" :c :bar :d [22 33]})

      [int? [int? [int? keyword?]]]
      (spec-from-data [11 [22 [33 :foo]]])

      [int? [] int? keyword? #{int?}]
      (spec-from-data [11 [] 22 :foo #{33}])

      [int? keyword? {:a string? :b boolean?} [[keyword?]]]
      (spec-from-data [1 :two {:a "abc", :b true} [[:c]]])))
  (testing "round-tripping"
    (is (valid-scalars? data-no-lists-no-sets (spec-from-data data-no-lists-no-sets))))
  (testing "non-terminating sequences"
    (are [x y] (= x y)
      (spec-from-data (range 11))
      [int? int? int? int? int? int? int?]

      (spec-from-data (range 11) 0)
      []

      (spec-from-data (range 11) 2)
      [int? int?]

      (spec-from-data (take 33 (cycle [11 "abc" :new])) 4)
      [int? string? keyword? int?]

      (spec-from-data [11 (take 33 (repeat 22)) 33 (take 33 (repeat 44)) 55] 1)
      [int? [int?] int? [int?] int?]

      (spec-from-data {:a 11 :b ["abc" (range 11) (iterate inc 55)] :c (lazy-seq [11 22 33 44 55])} 2)
      {:a int? :b [string? [int? int?] [int? int?]] :c [int? int?]})))


;; round-tripping (data-from-spec)
(def spec-no-lists-no-sets [int? char? {:a string? :b [boolean? keyword?]} double? [[float? decimal? ratio?]]])

(deftest data-from-spec-tests
  (testing "basic output"
    (are [x y] (= x y)
      []
      (data-from-spec [])

      [42]
      (data-from-spec [int?])

      [42 :kw "abc"]
      (data-from-spec [int? keyword? string?])

      [42 [42 [42]]]
      (data-from-spec [int? [int? [int?]]])

      {:a 42 :b "abc" :c true}
      (data-from-spec {:a int? :b string? :c boolean?})

      {:a [42 true] :b [[\c]] :c {:d 1M :e +}}
      (data-from-spec {:a [int? boolean?] :b [[char?]] :c {:d decimal? :e fn?}})

      [42 :Fri :kw :blue]
      (data-from-spec [int? (sorted-set :Sun :Mon :Tue :Wed :Thu :Fri :Sat) keyword? (sorted-set :red :yellow :green :blue)])

      :a
      (data-from-spec (sorted-set :a :b :c :d :e))))

  (testing "optional trailing arg :canonical"
    (are [x y] (= x y)
      [42] (data-from-spec [int?] :canonical)
      [42 :kw "abc"] (data-from-spec [int? keyword? string?] :canonical)))

  (testing "round-tripping"
    (is (valid-scalars? (data-from-spec spec-no-lists-no-sets) spec-no-lists-no-sets)))

  (testing "random, test.check generated data"
    (valid-scalars? (data-from-spec [int? string? boolean? char?] :random)
                        [int? string? boolean? char?])
    (valid-scalars? (data-from-spec [int? [char? [boolean? {:a float? :b neg-int? :c [keyword? ratio?]}]]] :random)
                        [int? [char? [boolean? {:a float? :b neg-int? :c [keyword? ratio?]}]]])
    (valid-scalars? (data-from-spec {:a int? :b float? :c [double? string? char?] :d [boolean? decimal?] :e [ratio? keyword? zero?]} :random)
                        {:a int? :b float? :c [double? string? char?] :d [boolean? decimal?] :e [ratio? keyword? zero?]}))

  (testing "non-terminating sequences"
    (are [x y] (= x y)
      (data-from-spec (take 9 (repeat int?)))
      [42 42 42 42 42]

      (data-from-spec [string? (take 9 (repeat int?)) boolean? (take 9 (cycle [keyword? char?])) ratio?])
      ["abc" [42 42 42 42 42] true [:kw \c :kw \c :kw] 22/7]

      (data-from-spec {:a (take 9 (cycle [float? char? fn?])) :b [int? (take 9 (repeat char?))]})
      {:a [1.23 \c + 1.23 \c] :b [42 [\c \c \c \c \c]]}))

  (testing "regex-as-predicates"
    (are [x y] (= x y)
      (data-from-spec [int? keyword? #{'foo 'bar 'baz} #"foo"])
      [42 :kw 'baz "foo"]

      (data-from-spec {:a #"[Z]{4}" :b [keyword? #"Q{3}[9]w"]})
      {:a "ZZZZ", :b [:kw "QQQ9w"]}))

  (testing "sets-as-predicates"
    (are [x y] (= x y)
      (data-from-spec [int? #{:red}])
      [42 :red]

      (data-from-spec {:a #{:blue} :b #{:green}})
      {:a :blue, :b :green}))

  (testing "metadata generators"
    (def test-regex #"foo[7]{2}")
    (def p1 (with-meta int? {:speculoos/predicate->generator #(str "pred-1 generator")}))
    (def p2 (with-meta (fn [s] (and (string? s))) {:speculoos/predicate->generator #(str "pred-2 generator")}))
    (def p3 ^{:speculoos/predicate->generator #(str "pred-3 generator")} #(+))

    (def p4 (with-meta #(int? %) {:speculoos/canonical-sample 987 :speculoos/predicate->generator #(identity 654)}))
    (def p5 (with-meta #(string? %) {:speculoos/canonical-sample "xyz" :speculoos/predicate->generator #(str "qrs")}))

    (are [x y] (= x y)
      (data-from-spec [p1 p2 p3] :random)
      ["pred-1 generator" "pred-2 generator" "pred-3 generator"]

      (data-from-spec [p4 p5] :random)
      [654 "qrs"]

      (data-from-spec {:a [p4 {:b p5}]} :canonical)
      {:a [987 {:b "xyz"}]})))


(comment
  (def example-re #"XYZ\d{1,5}")
  (def pred-1 (with-meta int? {:speculoos/predicate->generator #(rand-int 9999)}))
  (def pred-2 (with-meta (fn [s] (and (string? s) (re-matches example-re s))) {:speculoos/predicate->generator #(re-rand example-re)}))
  (def pred-3 ^{:speculoos/predicate->generator #(rand-int 9999)} #{:red :orange :yellow :green :blue})
  (data-from-spec [pred-1 pred-2 pred-3] :random)
  )


(deftest validate-predicate->generator-tests
  (are [x] (true? x)
    (every? #(valid-scalars? % [int? boolean?]) (validate-predicate->generator (with-meta #(int? %) {:speculoos/predicate->generator #(rand-int 99)})))
    (every? #(valid-scalars? % [string? boolean?]) (validate-predicate->generator (with-meta #(boolean (re-matches #"H\d{1,3}\w" %)) {:speculoos/predicate->generator #(re-rand #"H\d{1,3}\w")})))
    (every? #(valid-scalars? % [number? false?]) (validate-predicate->generator (with-meta #(< % 10) {:speculoos/predicate->generator #(+ 10 (rand-int 99))})))))


(def maybe-findable {:good-1 int?
                     :good-2 keyword?
                     :good-3 #{:red :green :blue}
                     :good-4 nil?
                     :good-5 (with-meta #(int? %) {:speculoos/predicate->generator #(rand-int 99)})
                     :good-6 #"foo"

                     :bad-1 #(int? %)
                     :bad-2 +
                     :bad-3 #(< 100 %)
                     :bad-4 (with-meta #(int? %) {:speculoos/wrong-key #(rand-int 99)})})


(deftest unfindable-generators-tests
  (is (->> maybe-findable
           unfindable-generators
           (map #(first (:path %)))
           (every? #(and (clojure.string/starts-with? % ":bad")
                         (not (clojure.string/starts-with? % ":good")))))))


(deftest exercise-tests
  (are [x y] (= x y)
    '() (exercise [int? string? char?] 0)
    true (every? #(true? (second %)) (exercise [int? string? char?]))
    3 (count (exercise [int? string? char?] 3))))


;; test data for (bazooka) function
(def all-correct-data [4 [3.3 [22/7]] "abc" {:first \e :second true} :kw])
(def all-incorrect-spec [fn? [int? [int?]] int? {:first int? :second int?} int?])

(deftest bazooka-swatting-flies-tests
  (testing "basic output"
    (are [x y] (= x y)
      [] (bazooka-swatting-flies [] [])
      [int?] (bazooka-swatting-flies [11] [char?])
      [int? string? keyword?] (bazooka-swatting-flies [11 "abc" :kw] [int? boolean? keyword?])
      {:a int? :b string? :c char?} (bazooka-swatting-flies {:a 11 :b "abc" :c \z} {:a string? :b string? :c char?})
      [int? [string? [boolean? [keyword?]]]] (bazooka-swatting-flies [11 ["abc" [true [:kw]]]] [string? [string? [boolean? [char?]]]])

      {:a [int? keyword?] :b {:c string? :d [[boolean?] int?]}}
      (bazooka-swatting-flies {:a [99 :foo] :b {:c "yay" :d [[false ] 55]}}
                              {:a [boolean? keyword?] :b {:c char? :d [[int?] int?]}})))
  (testing "round-tripping"
    (is (valid-scalars? all-correct-data (bazooka-swatting-flies all-correct-data all-incorrect-spec)))))


(deftest smash-data-tests
  (are [x y] (= x y)
    [] (smash-data [] [])
    ["abc"] (smash-data [11] [string?])
    [11 "abc" 33] (smash-data [11 22 33] [int? string? int?])

    [11 ["abc" [\c [true]]]]
    (smash-data [11 ["abc" [22 [true]]]]
                [int? [string? [char? [boolean?]]]])

    {:a 11 :b 1M :c true}
    (smash-data {:a 11 :b "abc" :c true} {:a int? :b decimal? :c boolean?})

    {99 :foo 88 \c 77 true 66 [11 true 33]}
    (smash-data {99 :foo 88 "abc" 77 true 66 [11 22 33]}
                {99 keyword? 88 char? 77 boolean? 66 [int? boolean? int?]})

    {:a {:b {:c "abc" :d "blah" :e 99}}}
    (smash-data {:a {:b {:c :foo :d "blah" :e 99}}} {:a {:b {:c string? :d string? :e int?}}})))


(deftest make-empty-test
  (are [x y] (= x y)
    [] (make-empty [])
    {} (make-empty {})
    '() (make-empty '())
    #{} (make-empty #{})

    [] (make-empty [11])
    {} (make-empty {:a 11})
    '() (make-empty '(11))
    #{} (make-empty #{11})

    [] (make-empty [11 22 33])
    {} (make-empty {:a 11 :b 22 :c 33})
    '() (make-empty '(11 22 33))
    #{} (make-empty #{11 22 33})

    [[] [[[]]] []]
    (make-empty [[11] [22 [33 [44]] 55] [66]])

    {:b {:d {}}}
    (make-empty {:a 11 :b {:c 22 :d {:e 33 :f 44}}})

    '(() ((())))
    (make-empty '(11 (22) 33 (44 (55 (66) 77)) 88))

    #{[[]] #{} {}}
    (make-empty #{11 [22 [33]] {:a 44} #{55}})

    [{:b #{}} () []]
    (make-empty [{:a 11 :b #{22}} '(33 :foo) 44 [55]])))


(deftest remove-paths-with-value-tests
  (are [x y] (= x y)
    [] (remove-paths-with-value (only-non-collections (all-paths [])) :foo)
    [] (remove-paths-with-value (only-non-collections (all-paths {})) :foo)
    [] (remove-paths-with-value (only-non-collections (all-paths '())) :foo)
    [] (remove-paths-with-value (only-non-collections (all-paths #{})) :foo)

    [] (remove-paths-with-value (only-non-collections (all-paths [:foo])) :foo)
    [] (remove-paths-with-value (only-non-collections (all-paths {:a :foo})) :foo)
    [] (remove-paths-with-value (only-non-collections (all-paths '(:foo))) :foo)
    [] (remove-paths-with-value (only-non-collections (all-paths #{:foo})) :foo)

    [{:path [0], :value 11}]
    (remove-paths-with-value (only-non-collections (all-paths [11])) :foo)

    [{:path [:a], :value 11}]
    (remove-paths-with-value (only-non-collections (all-paths {:a 11})) :foo)

    [{:path [0], :value 11}]
    (remove-paths-with-value (only-non-collections (all-paths '(11))) :foo)

    [{:path [11], :value 11}]
    (remove-paths-with-value (only-non-collections (all-paths #{11})) :foo)

    [{:path [0], :value 11} {:path [2], :value 22} {:path [4], :value 33}]
    (remove-paths-with-value (only-non-collections (all-paths [11 :foo 22 :foo 33])) :foo)

    [{:path [:a], :value 11} {:path [:c], :value 22}]
    (remove-paths-with-value (only-non-collections (all-paths {:a 11 :b :foo :c 22})) :foo)

    [{:path [0], :value 11} {:path [2], :value 22}]
    (remove-paths-with-value (only-non-collections (all-paths '(11 :foo 22))) :foo)

    [{:path [22], :value 22} {:path [11], :value 11}]
    (remove-paths-with-value (only-non-collections (all-paths #{11 :foo 22})) :foo)

    [{:path [0], :value 11} {:path [1 1 0], :value 22}]
    (remove-paths-with-value (only-non-collections (all-paths [11 [:foo [22]]])) :foo)

    [{:path [:a], :value 11} {:path [:c :d], :value 22}]
    (remove-paths-with-value (only-non-collections (all-paths {:a 11 :b :foo :c {:d 22}})) :foo)

    [{:path [0], :value 11} {:path [2 0], :value 22} {:path [2 2 0], :value 33}]
    (remove-paths-with-value (only-non-collections (all-paths '(11 :foo (22 :foo (33))))) :foo)

    [{:path [11], :value 11} {:path [[22 '(33)] 0], :value 22} {:path [[22 '(33)] 1 0], :value 33}]
    (remove-paths-with-value (only-non-collections (all-paths #{11 :foo [22 '(33)]})) :foo)))


(deftest replace-non-colls-tests
  (are [x y] (= x y)
    [] (replace-non-colls [11 22 33])
    {} (replace-non-colls {:a 11 :b 22 :c 33})
    '() (replace-non-colls '(11 22 33))
    #{} (replace-non-colls #{11 22 33})

    [] (replace-non-colls [] :foo)
    {} (replace-non-colls {} :foo)
    #{} (replace-non-colls #{} :foo)
    '() (replace-non-colls '() :foo)

    [:foo] (replace-non-colls [:foo] :foo)
    {:a :foo} (replace-non-colls {:a :foo} :foo)
    #{:foo} (replace-non-colls #{:foo} :foo)
    '(:foo) (replace-non-colls '(:foo) :foo)

    [:foo] (replace-non-colls [11] :foo)
    {:a :foo} (replace-non-colls {:a 11} :foo)
    '(:foo) (replace-non-colls '(11) :foo)
    #{:foo} (replace-non-colls #{11} :foo)

    [:foo :foo :foo] (replace-non-colls [11 22 33] :foo)
    {:a "nah", :b "nah", :c "nah"} (replace-non-colls {:a 11 :b 22 :c 33} "nah")

    #{"hey"} (replace-non-colls #{11 22 33} "hey")

    '(:list-element-replacement :list-element-replacement :list-element-replacement)
    (replace-non-colls '(11 22 33) :list-element-replacement)

    [:foo [:foo [:foo]]]
    (replace-non-colls [11 [22 [33]]] :foo)

    {:a {:b {:c :foo, :d :foo, :e :foo}}}
    (replace-non-colls {:a {:b {:c 11 :d 22 :e 33}}} :foo)

    #{{:a :foo, :b :foo} [:foo] :foo}
    (replace-non-colls #{11 22 [33] {:a 44 :b 55}} :foo)

    '(:foo (:foo (:foo)))
    (replace-non-colls '(11 (22 (33))) :foo)

    [:foo {:a :foo, :b '(:foo :foo)} [:foo #{:foo}] :foo]
    (replace-non-colls [11 {:a 22 :b '(33 44)} [55 #{66}] 77] :foo)

    {:a :foo, :b '(:foo (:foo)), :c #{[:foo] :foo}}
    (replace-non-colls {:a 11 :b '(22 (33)) :c #{44 [55]}} :foo)

    '(:foo [:foo] {:a :foo, [:this-is-a-vec-key] :foo, :c (:foo)})
    (replace-non-colls '(11 [22] {:a 33 [:this-is-a-vec-key] 44 :c (55)}) :foo)

    #{{:a :foo, :b :foo} [:foo] :foo}
    (replace-non-colls #{11 [22] {:a 99 :b 33} '(44)} :foo)

    [:foo :foo [:foo :foo [:foo :foo]]]
    (replace-non-colls [:foo 11 [22 :foo [33 :foo]]] :foo)

    #{{:a :foo, :b :foo} '(:foo) :foo [:foo :foo]}
    (replace-non-colls #{11 :foo [22 :foo] {:a :foo :b 33} '(44)} :foo)

    {:a :foo, :b :foo, :c [:foo :foo]}
    (replace-non-colls {:a 11 :b :foo :c [22 :foo]} :foo)

    '(:foo :foo (:foo :foo (:foo :foo)))
    (replace-non-colls '(11 :foo (22 :foo (33 :foo))) :foo)))


(deftest append-coll-type-predicate-tests
  (are [x y] (= x y)
    [11 22 33 vector?] (append-coll-type-predicate [11 22 33])
    [vector?] (append-coll-type-predicate [])
    [[] [] {} vector?] (append-coll-type-predicate [[] [] {}])

    '(list?) (append-coll-type-predicate '())
    '(11 22 33 list?) (append-coll-type-predicate '(11 22 33))
    '(() [] {} list?) (append-coll-type-predicate '(() [] {}))

    #{set?} (append-coll-type-predicate #{})
    #{11 set?} (append-coll-type-predicate #{11})
    #{{} [] :foo set?} (append-coll-type-predicate #{{} [] :foo})

    {:speculoos.utility/collection-predicate map?}
    (append-coll-type-predicate {})

    {:a 11, :speculoos.utility/collection-predicate map?}
    (append-coll-type-predicate {:a 11})

    {:a [], :b {}, :c (), :speculoos.utility/collection-predicate map?}
    (append-coll-type-predicate {:a [] :b {} :c '()})))


(deftest coll->pred-tests
  (are [x y] (= x y)
    vector? (coll->pred [])
    map? (coll->pred {})
    list? (coll->pred '())
    set? (coll->pred #{})))


(deftest append-tests
  (are [x y] (= x y)
    [99] (append [] 99)
    '(99) (append '() 99)
    {:foo 99} (append {} :foo 99)
    #{99} (append #{} 99)

    [11 99] (append [11] 99)
    '(11 99) (append '(11) 99)
    {:a 11 :foo 99} (append {:a 11} :foo 99)
    #{11 99} (append #{11} 99)

    [11 22 33 99] (append [11 22 33] 99)
    '(11 22 33 99) (append '(11 22 33) 99)
    {:a 11, :b 22, :c 33, :foo 99} (append {:a 11 :b 22 :c 33} :foo 99)
    #{11 22 33 99} (append #{11 22 33} 99)))


(deftest inject-coll-preds-tests
  (are [x y] (= x y)
    [vector?]
    (inject-coll-preds [])

    [[vector?] vector?]
    (inject-coll-preds [[]])

    [[vector?] [vector?] vector?]
    (inject-coll-preds [[] []])

    [[[vector?] vector?] vector?]
    (inject-coll-preds [[[]]])

    [[vector?] [vector?] [vector?] vector?]
    (inject-coll-preds [ [] [] [] ])

    [(list list?) [vector?] (list list?) [vector?] {:speculoos.utility/collection-predicate map?} vector?]
    (inject-coll-preds [ '() [] '() [] {}])

    [[[vector?] vector?] vector?]
    (inject-coll-preds [ [[]] ])

    [[[vector?] vector?] [[vector?] vector?] vector?]
    (inject-coll-preds [ [[]] [[]] ])

    [[[[vector?] vector?] vector?] vector?]
    (inject-coll-preds [[[[]]]])

    (list list?)
    (inject-coll-preds '())

    (list (list list?) list?)
    (inject-coll-preds '(()))

    (list (list list?) (list list?) list?)
    (inject-coll-preds '(() ()))

    (list [vector?] (list list?) [vector?] (list list?) list?)
    (inject-coll-preds '([] () [] ()))

    (list (list (list list?) list?) list?)
    (inject-coll-preds '((())))

    (list [vector?] [vector?] list?)
    (inject-coll-preds '([] []))

    (list [vector?] list?)
    (inject-coll-preds '([]))

    (list [vector?] [vector?] [vector?] list?)
    (inject-coll-preds '([] [] []))

    (list (list list?) (list list?) list?)
    (inject-coll-preds '( () ()))

    {:speculoos.utility/collection-predicate map?}
    (inject-coll-preds {})

    {:a [vector?] :speculoos.utility/collection-predicate map?}
    (inject-coll-preds {:a []})

    {:a [vector?] :b (list list?) :c #{set?} :d {:speculoos.utility/collection-predicate map?} :speculoos.utility/collection-predicate map?}
    (inject-coll-preds {:a [] :b '() :c #{} :d {}})

    {:a {:b {:c {:speculoos.utility/collection-predicate map?} :speculoos.utility/collection-predicate map?} :speculoos.utility/collection-predicate map? :d [vector?]} :e (list list?) :speculoos.utility/collection-predicate map?}
    (inject-coll-preds {:a {:b {:c {}} :d []} :e '()})

    #{[vector?] set?}
    (inject-coll-preds #{ [] })

    #{set?}
    (inject-coll-preds #{})

    #{[vector?] [[vector?] vector?] [[[vector?] vector?] vector?] set?}
    (inject-coll-preds #{[] [[]] [[[]]]})

    #{#{set?} [vector?] set?}
    (inject-coll-preds #{#{} []})

    #{(list list?) (list (list list?) list?) (list (list (list list?) list?) list?) set?}
    (inject-coll-preds #{'() '(()) '((()))})

    #{[vector?] [[vector?] vector?] [[vector?] [vector?] vector?] [(list list?) (list list?) (list list?) vector?] set?}
    (inject-coll-preds #{[] [[]] [[] []] ['() '() '()]})

    [vector?]
    (inject-coll-preds (make-empty [11]))

    [[[vector?] [vector?] [vector?] vector?] vector?]
    (inject-coll-preds (make-empty [11 [22 [33] [] [] 44] 55]))))


(def one-three-five #(and (int? (=1st %))
                          (int? (=3rd %))
                          (int? (=5th %))))
(def fourth-a-list? #(list? (=4th %)))
(def fourth-22-more-than-second? #(= (+ 22 (get* (=2nd %) 0))
                                     (get* (=4th %) 0)))

(def test-data-2 [11 [22] 33 '(44) 55])
(def test-data-2-collection-spec [one-three-five fourth-a-list? fourth-22-more-than-second?])
(valid-collections? test-data-2 test-data-2-collection-spec)

(def test-data-3 [11 [22 [33] 44] '(55)])

(deftest basic-collection-spec-from-data-test
  (is (valid-collections? test-data-2 (basic-collection-spec-from-data test-data-2)))
  (is (valid-collections? test-data-3 (basic-collection-spec-from-data test-data-3)))
  (testing "non-terminating sequences"
    (are [x y] (= x y)
    (basic-collection-spec-from-data [11 [22 [33] 44] 55])
    [[[vector?] vector?] vector?]

    (basic-collection-spec-from-data (range 11))
    [vector?]

    (basic-collection-spec-from-data (take 9 (cycle [["foo"] {:foo "bar"} '(1/3)])))
    [[vector?] {:speculoos.utility/collection-predicate map?} (list list?) vector?]

    (basic-collection-spec-from-data [11 (range 9) {:a 5} '(33 44)])
    [[vector?] {:speculoos.utility/collection-predicate map?} (list list?) vector?])))


(deftest partition-after-tests
  (are [x y] (= x y)
    (partition-after coll? [11 [22] 33 44 [55] 66])
    '((11 [22]) (33 44 [55]) (66))

    (partition-after coll? '(11 22 (33) 44 55 [66] 88 #{77}))
    '((11 22 (33)) (44 55 [66]) (88 #{77}))))


(deftest flatten-one-level-tests
  (are [x y] (= x y)
    (flatten-one-level [])
    '()

    (flatten-one-level [[11]])
    '(11)

    (flatten-one-level [[11] [22] [33]])
    '(11 22 33)

    (flatten-one-level '())
    '()

    (flatten-one-level '([11]))
    '(11)

    (flatten-one-level '((11) [22] (33)))
    '(11 22 33)

    (flatten-one-level (take 3 (cycle [[11] [22] [33]])))
    '(11 22 33)))


(deftest recover-literal-path-1-tests
  (are [x y] (= x y)
    6 (recover-literal-path-1 [11 [22] 33 44 [55] 66 [[77]] 88 99]  2)
    6 (recover-literal-path-1 '(11 (22) 33 44 [55] 66 [(77)] 88 99) 2)

    4 (recover-literal-path-1 [11 [22] 33 44 [55] 66 [[77]] 88 99]  1)
    4 (recover-literal-path-1 '(11 (22) 33 44 [55] 66 [(77) 88 99]) 1)

    1 (recover-literal-path-1 [11 [22] 33 44 [55] 66 [[77]] 88 99]  0)
    1 (recover-literal-path-1 '(11 (22) 33 44 [55] 66 [(77) 88 99]) 0)

    0 (recover-literal-path-1 [[22] 33 44 [55] 66 [[77]] 88 99]  0)
    0 (recover-literal-path-1 '((22) 33 44 [55] 66 [(77) 88 99]) 0)

    :a (recover-literal-path-1 {:a []} :a)
    [:a] (recover-literal-path-1 #{[:a]} [:a])
    2 (recover-literal-path-1 (take 5 (repeat [77])) 2)
    4 (recover-literal-path-1 (take 6 (cycle [[77] 44 {:a 22} 55 #{33}])) 2)))


(deftest recover-literal-path-tests
  (testing "empties"
    (are [x] (= x [])
      (recover-literal-path [] [])
      (recover-literal-path {} [])
      (recover-literal-path [11 [22] 33] [])
      (recover-literal-path {:a [11]} [])))
  (testing "non-empty ordinal-parent-paths"
    (are [x y] (= x y)
      ;; targeting [444]
      (recover-literal-path [11 22 [33] 44 [55 [66] 77 [88] 99 [111 [222] [333] [444]]]]
                            [1 2 2])
      [4 5 3]

      ;; targeting [33]
      (recover-literal-path [11 22 [33] 44 [55 [66] 77 [88] 99 [111 [222] [333] [444]]]]
                            [0])
      [2]

      ;; targeting [55 [66]...]
      (recover-literal-path [11 22 [33] 44 [55 [66] 77 [88] 99 [111 [222] [333] [444]]]]
                            [1])
      [4]

      ;; targeting [66]
      (recover-literal-path [11 22 [33] 44 [55 [66] 77 [88] 99 [111 [222] [333] [444]]]]
                            [1 0])
      [4 1]

      ;; targeting [88]
      (recover-literal-path [11 22 [33] 44 [55 [66] 77 [88] 99 [111 [222] [333] [444]]]]
                            [1 1])
      [4 3]

      ;; targeting [333]
      (recover-literal-path [11 22 [33] 44 [55 [66] 77 [88] 99 [111 [222] [333] [444]]]]
                            [1 2 1])
      [4 5 2]

      ;; targeting [22]
      (recover-literal-path [11 [22]] [0])
      [1]

      ;; targeting [99]
      (recover-literal-path {:a [11 [22] 33 {:b [44 [55] 66 [77] 88 [99]]}]}
                            [:a 1 :b 2])
      [:a 3 :b 5]

      ;; targeting [222]
      (recover-literal-path '(11 22 (33) 44 {:a 55 :b [66 [77] 88 [99] 111 [222]]})
                            [1 :b 2])
      [4 :b 5]

      ;; targeting [55]
      (recover-literal-path #{11 [[22] 33 44 [55]]}
                            [[[22] 33 44 [55]] 1])
      [[[22] 33 44 [55]] 3]

      ;; targeting [77] ; look to the right...--->                                                                     >>>>----vvvv
      (recover-literal-path [11 22 [33] 44 [55] 66 (take 9 (cycle [[77] 88 [99]]))] ;; => [11 22 [33] 44 [55] 66 ([77] 88 [99] [77] 88 [99] [77] 88 [99])]
                            [2 2])
      [6 3])))


(deftest collections-without-predicates-tests
  (testing "empty data, empty specifications"
    (are [x y] (= x y)
      (collections-without-predicates [] [])
      #{{:path [], :value []}}

      (collections-without-predicates {} {})
      #{{:path [], :value {}}}

      (collections-without-predicates '() '())
      #{{:path [], :value ()}}

      (collections-without-predicates #{} #{})
      #{{:path [], :value #{}}}))

  (testing "empty data, with a collection predicate"
    (are [x] (= x #{})
      (collections-without-predicates [] [vector?])
      (collections-without-predicates {} {:is-a-map? map?})
      (collections-without-predicates (list) (list list?))
      (collections-without-predicates #{} #{set?})
      (collections-without-predicates (range 9) [seq?])
      (collections-without-predicates (cycle [11 22 33]) [#(isa? % ::non-terminating)])))

  (testing "nested collections, missing collection predicates"
    (are [x y] (= x y)
      (collections-without-predicates [11 [22] 33 [44] 55 {:a 66 :b #{77}}] [[vector?] [] {:test map?}])
      #{{:path [5 :b], :value #{77}}
        {:path [], :value [11 [22] 33 [44] 55 {:a 66, :b #{77}}]}
        {:path [3], :value [44]}}

      (collections-without-predicates [11 [22] 33 [44]] [[vector?]])
      #{{:path [], :value [11 [22] 33 [44]]}
        {:path [3], :value [44]}}

      (collections-without-predicates [11 [22] 33 [44] 55 [66]] [[] [] [list?]])
      #{{:path [1], :value [22]}
        {:path [], :value [11 [22] 33 [44] 55 [66]]}
        {:path [3], :value [44]}}

      (collections-without-predicates [[11] [22] [33]] [[map?] [list?] [set?]])
      #{{:path [], :value [[11] [22] [33]]}}

      (collections-without-predicates {:a {:b {:c 33}}} {})
      #{{:path [:a], :value {:b {:c 33}}}
        {:path [:a :b], :value {:c 33}}
        {:path [], :value {:a {:b {:c 33}}}}}

      (collections-without-predicates (list 11 (list 22 (list 33))) (list list? (list list? (list))))
      #{{:path [1 1], :value (list 33)}}

      (collections-without-predicates #{11 [22]} #{set?})
      #{{:path [[22]], :value [22]}})))


(def thorough-data [11 :foo {:x 22/7 :y 'bar} (list 33 44)])
(def thorough-scalar-spec [int? keyword? {:x ratio? :y symbol?} (list int? int?)])
(def thorough-coll-spec [vector? {:is-map map?} (list list?)])

(comment
  (valid-scalars? thorough-data thorough-scalar-spec)
  (valid-collections? thorough-data thorough-coll-spec)
  (valid? thorough-data thorough-scalar-spec thorough-coll-spec)
  (scalars-without-predicates thorough-data thorough-scalar-spec)
  (collections-without-predicates thorough-data thorough-coll-spec)
  )


(deftest thoroughly-valid?-tests
  (testing "thoroughly valid example"
    (are [x] (true? x)
      (thoroughly-valid? [] [] [vector?])
      (thoroughly-valid? thorough-data
                         thorough-scalar-spec
                         thorough-coll-spec)))
  (testing "not thoroughly valid examples"
    (are [x] (false? x)
      (thoroughly-valid? [] [] [])
      (thoroughly-valid? thorough-data
                         (speculoos.fn-in/dissoc-in* thorough-scalar-spec [3])
                         thorough-coll-spec)
      (thoroughly-valid? thorough-data
                         thorough-scalar-spec
                         (speculoos.fn-in/dissoc-in* thorough-coll-spec [2]))
      (thoroughly-valid? thorough-data
                         (speculoos.fn-in/assoc-in* thorough-scalar-spec [1] int?)
                         thorough-coll-spec)
      (thoroughly-valid? thorough-data
                         thorough-scalar-spec
                         (speculoos.fn-in/assoc-in* thorough-coll-spec [1 :is-map?] list?)))))


(deftest clamp-in*-tests
  (are [x y] (= x y)
    (clamp-in* [11 (range 9) 22 33] [1] 0)
    [11 [] 22 33]

    (clamp-in* [11 (range 9) 22 33] [1] 3)
    [11 [0 1 2] 22 33]

    (clamp-in* [11 (range 9) 22 33] [1] 99)
    [11 [0 1 2 3 4 5 6 7 8] 22 33]

    (clamp-in* {:a (take 99 (cycle [11 22 33]))} [:a] 6)
    {:a [11 22 33 11 22 33]}

    (clamp-in* [11 [22 33 {:a 44 :b [55 66 (take 99 (repeat 77))]}]] [1 2 :b 2] 3)
    [11 [22 33 {:a 44, :b [55 66 [77 77 77]]}]]

    (clamp-in* [] [] 0)
    []

    (clamp-in* (range 99) [] 3)
    [0 1 2]

    (clamp-in* [11 [22 {:a 33 :b 44 :c 55 :d 66 :e 77}]] [1 1] 3)
    [11 [22 [[:a 33] [:b 44] [:c 55]]]]))


(deftest clamp-every-tests
  (testing "zero length clamps, empty collections"
    (are [x y] (= x y)
      (clamp-every (range 9) 0)
      []

      (clamp-every [11 (range 9) 22 33] 0)
      [11 [] 22 33]

      (clamp-every [] 3)
      []))

  (testing "basic operation"
    (are [x y] (= x y)
      (clamp-every (take 11 (cycle [11 22 33])) 5)
      [11 22 33 11 22]

      (clamp-every [11 (take 11 (repeat 22)) 33 (take 11 (repeat 44)) 55] 3)
      [11 [22 22 22] 33 [44 44 44] 55]

      (clamp-every {:a 11 :b (take 11 (repeat 22)) :c 33 :d [44 [55 (take 11 (repeat 66))]]} 3)
      {:a 11, :b [22 22 22], :c 33, :d [44 [55 [66 66 66]]]}))

  (testing "unchanged vectors (i.e., terminating sequences) pass through unchanged"
    (are [x y] (= x y)
      (clamp-every [11 22 33] 0)
      [11 22 33]

      (clamp-every [11 22 33] 2)
      [11 22 33]

      (clamp-every [11 [22 [33 44 55]]] 3)
      [11 [22 [33 44 55]]]))

  (testing "nested non-terminating sequences"
    (are [x y] (= x y)
      (clamp-every [11 22 (take 9 (repeat (take 9 (repeat 33))))] 3)
      [11 22 [[33 33 33] [33 33 33] [33 33 33]]]

      (clamp-every {:a 11
                    :b (take 9 (cycle [(take 9 (range 9))
                                       (take 9 (iterate inc 11))
                                       (take 9 (lazy-seq [55 66 77 88 99]))]))
                    :c (take 6 (repeat 111))
                    } 5)
      {:a 11, :b [[0 1 2 3 4] [11 12 13 14 15] [55 66 77 88 99] [0 1 2 3 4] [11 12 13 14 15]], :c [111 111 111 111 111]})))


(deftest in?-tests
  (testing "empty collections"
    (are [x] (false? x)
      (in? [] 0)
      (in? '() 0)
      (in? {} 0)
      (in? #{} 0)))
  (testing "found item"
    (are [x] (true? x)
      (in? [1 2 3] 3)
      (in? '(1 2 3) 3)
      (in? #{1 2 3} 3)
      (in? {:a 1 :b 2 :c 3} [:c 3])
      (in? (vals {:a 1 :b 2 :c 3}) 3)
      (in? (cycle [1 2 3]) 3)
      (in? (iterate inc 1) 3)
      (in? (lazy-seq [1 2 3]) 3)
      (in? (range 9) 3)
      (in? (repeat 9 3) 3)))
  (testing "not-found item"
    (are [x] (false? x)
      (in? [1 2 3] 99)
      (in? '(1 2 3) 99)
      (in? #{1 2 3} 99)
      (in? {:a 1 :b 2 :c 3} [:c 99])
      (in? (vals {:a 1 :b 2 :c 3}) 99)
      (in? (take 9 (cycle [1 2 3])) 99)
      (in? (take 9 (iterate inc 1)) 99)
      (in? (lazy-seq [1 2 3]) 99)
      (in? (range 9) 99)
      (in? (repeat 9 3) 99))))


(deftest seq-regex-tests
  (testing "non-even regex args"
   (is (thrown? AssertionError (seq-regex [1 2 3] int?))))
  (testing "empty seq"
   (are [x y] (= x y)
     false (seq-regex [])
     true (seq-regex [] nil? :*)))
  (testing "empty regex args, and un-specified trailing values"
   (are [x] (false? x)
     (seq-regex [1 2 3])
     (seq-regex [1 2 3 :a :b :c] int? 3)
     (seq-regex [1 2 3 :a :b :c 'foo] int? :* keyword? 3)))
  (testing "exactly-one regex :."
   (are [x y] (= x y)
     false (seq-regex [1 2] int? :.)
     true (seq-regex [1] int? :.)
     true (seq-regex [:a] keyword? :.)))
  (testing "zero-or-one regex :?"
   (are [x y] (= x y)
     false (seq-regex [1 2 3] int? :?)
     true (seq-regex [1] int? :?)
     true (seq-regex [] int? :?)))
  (testing "one-or-more regex :+"
   (are [x y] (= x y)
     false (seq-regex [] int? :+)
     true (seq-regex [1] int? :+)
     true (seq-regex [1 2] int? :+)))
  (testing "zero-or-more regex :*"
   (are [x y] (= x y)
     true (seq-regex [] int? :*)
     true (seq-regex [1] int? :*)
     true (seq-regex [1 2 3] int? :*)
     false (seq-regex [1 2 3] keyword? :*)))
  (testing "exactly i integer qty"
   (are [x y] (= x y)
     false (seq-regex [] int? 1)
     false (seq-regex [1] int? 0)
     true (seq-regex [] int? 0)
     true (seq-regex [1] int? 1)
     true (seq-regex [1 2] int? 2)
     true (seq-regex [1 2 3] int? 3)))
  (testing "range [mn mx] qty"
   (are [x y] (= x y)
     true (seq-regex [] int? [0 0])
     true (seq-regex [1] int? [1 1])
     true (seq-regex [] int? [0 0])
     true (seq-regex [1] int? [0 2])
     false (seq-regex [1 2 3 4] int? [0 3])))
  (testing "mixed values, predicates, and regexes"
   (are [x] (true? x)
     (seq-regex [] int? :* string? :. symbol? :+ boolean? :?)
     (seq-regex [1 2 3 :a :b :c 'foo 'bar 'baz] int? 3 keyword? [0 5] symbol? :+)
     (seq-regex [1 2 3 :a 'foo] int? :* keyword? :. symbol? :+)
     (seq-regex [1 2 3 :a :b :c 4 5 6] int? 3 keyword? [0 5] int? :*)
     (seq-regex [1 2 3 :a :b :c 'foo 'bar 'baz]
                #(and (int? %) (pos? %)) 3
                #(and (simple-keyword? %) (= 2 (count (str %)))) 3
                #(= 3 (count (str %))) 3)
     (seq-regex [1 2 3 :a :b :c 4 5 6] (complement keyword?) 3 keyword? :+ int? :*))))


(deftest ordinal-access-tests
  (are [x y] (= x (y (into [] (range 1 13))))
    1 =1st
    2 =2nd
    3 =3rd
    4 =4th
    5 =5th
    6 =6th
    7 =7th
    8 =8th
    9 =9th
    10 =10th
    11 =11th
    12 =12th)
  (are [x y] (= x (y [:a "foo" \c]))
    :a =1st
    "foo" =2nd
    \c =3rd)
  (are [x y] (= x y)
    :one (=1st (list :one :two :three))
    :two (=2nd (list :one :two :three))
    :three (=3rd (list :one :two :three))
    'bar (=2nd (list 'foo 'bar 'baz))
    'two (binding [*ordinal-offset* 0] (=1st ['one 'two 'three]))
    11 (=12th (range))
    42 (=5th (repeat 42))))


(deftest predicate-scalar-symbol-tests
  (are [x y] (= x y)
    'x (predicate-scalar-symbol '(fn [x] (int? x)))
    'foo (predicate-scalar-symbol '(fn [foo] (and (number? foo) (< foo 99))))

    ;; The following relies on an implementation detail of the Reader, and is
    ;; thus not guaranteed to be stable.
    #_ true #_ (->> '#(int? %)
                    predicate-scalar-symbol
                    str
                    (re-find #"^p\d__\d*#$")
                    boolean)))


(deftest valid-fn-form?-tests
  (are [x] (true? x)
    (valid-fn-form? '(fn [x] :foo))
    (valid-fn-form? '#(int? %)))
  (are [y] (false? y)
    (valid-fn-form? '(:foo))
    (valid-fn-form? '[])))


(deftest predicate->generator-tests
  (are [q] (true? q)
    (let [x int?] (x ((predicate->generator x))))
    (let [y string?] (y ((predicate->generator y))))
    (let [z boolean?] (z ((predicate->generator z))))))


#_ (ns-unmap *ns* 'inspect-fn-tests)


(deftest inspect-fn-tests
  (are [f] (true? (:all-valid? (inspect-fn-self-check f)))

    '#(int? %)
    '#(string? %)
    '#(and (int? %))
    '#(and (string? %))
    '#(and (int? %) (int? %))
    '#(and (string? %) (< 3 (count %)))
    #_ '#(and (decimal? %) (< % 999999) (>= % 0)) ;; lein test has trouble here
    '#(or (int? %))
    '#(or (int? %) (string? %))
    '#(or (int? %) (string? %) (ratio? %))

    '(fn [k] (keyword? k))
    '(fn [s] (string? s))
    '(fn [i] (int? i))
    '(fn [i] (and (int? i) (int? i)))
    '(fn [s] (and (string? s) (<= 3 (count s))))
    '(fn [r] (and (ratio? r) (< r 99) (>= r 1/99)))
    '(fn [x] (or (int? x)))
    '(fn [x] (or (int? x) (string? x)))
    '(fn [x] (or (int? x) (string? x) (ratio? x)))

    '#(or (and (int? %)
               (neg? %)))

    '#(or (and (int? %)
               (even? %))
          (and (string? %)
               (<= 2 (count %))))

    '#(or (and (int? %)
               (even? %))
          (and (string? %)
               (<= 2 (count %)))
          (and (ratio? %)
               (<= 1/9 %)))

    '(fn [x] (or (and (int? x)
                      (even? x))))

    '(fn [x] (or (and (int? x)
                      (odd? x))
                 (and (string? x)
                      (<= 3 (count x)))))

    '(fn [x] (or (and (int? x)
                      (even? x))
                 (and (string? x)
                      (<= 2 (count x)))
                 (and (ratio? x)
                      (<= 1/8 x))))))


(deftest defpred-tests
  (testing "macroexpansion (lein test does not like this)"
    #_ (are [x y] (= x y)
         (macroexpand-1 '(defpred foo :predicate :generator))
         '(def foo (clojure.core/with-meta :predicate #:speculoos{:canonical-sample :foo-canonical-sample, :predicate->generator :generator}))
         ))
  (testing "basic results"
    (defpred foo1 'pred1 #(rand-int 99))
    (defpred foo2 'pred2 #(- (rand-int 12)))
    (defpred foo3 'pred3 #(rand))
    (is (valid-scalars? (data-from-spec [foo1 foo2 foo3] :random) [int? int? number?])))

  (testing "more advanced features"
    (defpred a1 #(and (int? %) (even? %)))
    (defpred b2 (fn [x] (or (ratio? x) (string? x))))
    (defpred c3 #(or (and (int? %) (neg? %))
                     (keyword? %)))
    (def spec-defpred [a1 b2 c3 {:x a1 :y b2 :z c3}])
    (is (valid-scalars? (data-from-spec spec-defpred :random) spec-defpred))))


(deftest lazy-seq?-tests
  (are [x] (true? x)
    (lazy-seq? (interleave [:a :b :c] [1 2 3]))
    (lazy-seq? (interpose :foo ["a" "b" "c"]))
    (lazy-seq? (lazy-cat [1 2 3] [4 5 6]))
    (lazy-seq? (mapcat reverse [[3 2 1] [6 5 4] [9 8 7]])))
  (are [x] (false? x)
    (lazy-seq? [1 2 3])
    (lazy-seq? '(1 2 3))
    (lazy-seq? 3)
    (lazy-seq? (zipmap [:a :b :c] [1 2 3]))))


(run-tests)