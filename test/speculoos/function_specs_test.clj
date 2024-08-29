(ns speculoos.function-specs-test
  "Functions to apply and validate speculoos specs against functions."
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :refer [are is deftest testing run-tests]]
   [speculoos.core :refer [valid-scalars?]]
   [speculoos.utility :refer [=1st =2nd =3rd]]
   [speculoos.function-specs :refer :all]))


(defn fn-spec-test-example
  "Example function for testing speculoos.function-specs ns."
  {:UUIDv4 #uuid "1768a9b0-cb3d-447e-9153-8407616eee9e"}
  [x y] (vector "z" (+ x y) \z))


(def fn-example-specs (with-meta {:speculoos/arg-scalar-spec     [int? double?]
                                  :speculoos/arg-collection-spec [#(not= (=1st %) (=2nd %))]
                                  :speculoos/ret-scalar-spec     [string? double? char?]
                                  :speculoos/ret-collection-spec [#(= 3 (count %))
                                                                  #(= (=1st %)
                                                                      (str (=3rd %)))]}
                        {:UUIDv4 #uuid "71e60bd9-51bc-4b77-9fa6-836f2e7f278d"}))

(defn fn-spec-test-example-2 [x y] (vector (+ x y) (* x y) (* x x y y)))

(def count-is-one? #(= 1 (count %)))
(def first-second-third-equal? #(= (=1st %) (=2nd %) (=3rd %)))
(def first-second-third-less-than? #(< (=1st %) (=2nd %) (=3rd %)))
(def first-equals-second? #(= %1 %2))
(def first-not-equal-second? #(not= %1 %2))
(def count-plus-two? #(= (+ 2 (count %1)) (count %2)))


(defn exceptional-fn
  {:UUIDv4 #uuid "9b08faff-b5be-44de-9c00-8290d6a368be"}
  [x y]
  (vector (+ x y)))

(def exceptional-specs (with-meta {:speculoos/arg-scalar-spec [int? int?]}
                         {:UUIDv4 #uuid "82a19311-f319-48af-b79c-2230bf89fc26"}))


(deftest fn-var-tests
  (is (=  (fn-var fn-spec-test-example)
          #'speculoos.function-specs-test/fn-spec-test-example)))


(deftest fn-meta-tests
  (is (= (meta #'fn-spec-test-example)
         (fn-meta fn-spec-test-example))))


(deftest fn-meta*-tests
  (is (= (meta #'fn-spec-test-example)
         (fn-meta* #'fn-spec-test-example))))


(deftest assoc-metadata-f!-tests
  (is (=  "foo!"
       (do (assoc-metadata-f! fn-spec-test-example :assoced "foo!")
           (:assoced (fn-meta fn-spec-test-example))))))


(deftest dissoc-metadata-f!-tests
  (is (do (assoc-metadata-f! fn-spec-test-example :to-be-dissoced "baz!")
          (dissoc-metadata-f! fn-spec-test-example :to-be-dissoced)
          (nil? (:to-be-dissoced (fn-meta fn-spec-test-example))))))


(deftest inject-specs!-tests
  (is (let [_ (inject-specs! fn-spec-test-example {:speculoos/arg-scalar-spec     "arg-sca"
                                                   :speculoos/arg-collection-spec "arg-coll"
                                                   :speculoos/ret-scalar-spec     "ret-sca"
                                                   :speculoos/ret-collection-spec "ret-coll"})
            mdata (fn-meta fn-spec-test-example)]
        (and (= "arg-sca"  (:speculoos/arg-scalar-spec mdata))
             (= "arg-coll" (:speculoos/arg-collection-spec mdata))
             (= "ret-sca"  (:speculoos/ret-scalar-spec mdata))
             (= "ret-coll" (:speculoos/ret-collection-spec mdata))))))


(deftest unject-specs!-tests
  (is (let [_ (inject-specs! fn-spec-test-example {:speculoos/arg-scalar-spec     "foo"
                                                   :speculoos/arg-collection-spec "bar"
                                                   :speculoos/ret-scalar-spec     "baz"
                                                   :speculoos/ret-collection-spec "zap"})
            _ (unject-specs! fn-spec-test-example)
            mdata (fn-meta fn-spec-test-example)]
        (and (nil?  (:speculoos/arg-scalar-spec mdata))
             (nil? (:speculoos/arg-collection-spec mdata))
             (nil?  (:speculoos/ret-scalar-spec mdata))
             (nil? (:speculoos/ret-collection-spec mdata))))))


(deftest valid?-or-report-tests
  (are [x y] (= x y)
    true
    (valid?-or-report [3 "abc" :foo \c]
                      [int? string? keyword? char?]
                      [#(= 4 (count %))])

    [{:path [3], :datum \c, :predicate boolean?, :valid? false}]
    (valid?-or-report [3 "abc" :foo \c]
                      [int? string? keyword? boolean?]
                      [#(= 4 (count %))])
    ))


;; test functions
(defn my-summation-er [v] (apply + v))
(defn my-summation-er-broken [v] (+ 100 (my-summation-er v)))
(defn my-reverser [v] (vec (reverse v)))
(defn my-reverser-broken [v] (-> v pop pop (conj 42)))
(defn swap-key-vals [m] {:x (m :y) :y (m :x)})
(defn swap-key-vals-broken [m] m)
(defn sum-vals [m] (apply + (vals m)))
(defn sum-vals-broken [m] (- (sum-vals m)))
(defn plus [x y] (+ x y))
(defn plus-broken [x y] (- x y))


;; example relationship functions
(defn reversed? [v1 v2] (= v2 (reverse v1)))
(defn doubled? [x y] (= y (* 2 x)))
(defn same-elements? [v1 v2] (= (sort v1) (sort v2)))
(defn equal-count? [v1 v2] (= (count v1) (count v2)))
(defn is-count? [v n] (= n (count v)))
(defn correct-sum? [v n] (= n (apply + v)))
(defn four-elements? [v _] (= 4 (count v)))
(defn twice-plus-two? [v3 n] (= n (+ 2 (* 2 v3))))
(defn swapped? [m1 m2] (and (= (m1 :x) (m2 :y))
                            (= (m1 :y) (m2 :x))))
(defn is-2? [_ i] (= 2 i))
(defn correct-val-sum? [m n] (= n (sum-vals m)))
(defn correct-sign? [_ n] (pos? n))
(defn correct-plus? [v y] (= y (apply + v)))


(deftest validate-relationship-tests
  (testing "valid relationships, 'bare' return"
    (are [x y] (= x y)
      (validate-argument-return-relationship [42] 84 {:path-argument [0]
                                                      :path-return nil
                                                      :relationship-fn doubled?})
      {:path-argument [0], :path-return nil, :relationship-fn doubled?, :datum-argument 42, :datum-return 84, :valid? true}

      ;; can get cute and omit :path-return so that it defaults to `nil` (not recommended, because it won't show up in validation report)
      (validate-argument-return-relationship [42] 84 {:path-argument [0]
                                                      :relationship-fn doubled?})
      {:path-argument [0], :relationship-fn doubled?, :datum-argument 42, :datum-return 84, :valid? true}

      (validate-argument-return-relationship [11 22 33] 3 {:path-argument []
                                                           :path-return nil
                                                           :relationship-fn is-count?})
      {:path-argument [], :path-return nil, :relationship-fn is-count?, :datum-argument [11 22 33], :datum-return 3, :valid? true}))

  (testing "invalid relationships, 'bare' return"
    (are [x y] (= x y)
      (validate-argument-return-relationship [42] 42 {:path-argument [0]
                                                      :path-return nil
                                                      :relationship-fn doubled?})
      {:path-argument [0], :path-return nil, :relationship-fn doubled?, :datum-argument 42, :datum-return 42, :valid? false}

      (validate-argument-return-relationship [11 22 33] 2 {:path-argument []
                                                           :path-return nil
                                                           :relationship-fn is-count?})
      {:path-argument [], :path-return nil, :relationship-fn is-count?, :datum-argument [11 22 33], :datum-return 2, :valid? false}))

  (testing "valid relationships"
    (are [x y] (= x y)
      (validate-argument-return-relationship [1 2 3] [3 2 1] {:path-argument []
                                                              :path-return []
                                                              :relationship-fn reversed?})
      {:path-argument [], :path-return [], :relationship-fn reversed?, :datum-argument [1 2 3], :datum-return [3 2 1], :valid? true}

      (validate-argument-return-relationship ['foo 'bar 'baz [:x :y :z [11 22 33]]]
                                             {:q {:w [11 22 33]}}
                                             {:path-argument [3 3]
                                              :path-return [:q :w]
                                              :relationship-fn same-elements?})
      {:path-argument [3 3], :path-return [:q :w], :relationship-fn same-elements?, :datum-argument [11 22 33], :datum-return [11 22 33], :valid? true}

      (validate-argument-return-relationship {:x [11 22 33]}
                                             ['foo 'bar [77 88 99]]
                                             {:path-argument [:x]
                                              :path-return [2]
                                              :relationship-fn equal-count?})
      {:path-argument [:x], :path-return [2], :relationship-fn equal-count?, :datum-argument [11 22 33], :datum-return [77 88 99], :valid? true}))

  (testing "invalid relationships"
    (are [x y] (= x y)
      (validate-argument-return-relationship [1 2 3] [1 2 3] {:path-argument []
                                                              :path-return []
                                                              :relationship-fn reversed?})
      {:path-argument [], :path-return [], :relationship-fn reversed?, :datum-argument [1 2 3], :datum-return [1 2 3], :valid? false}

      (validate-argument-return-relationship ['foo 'bar 'baz [:x :y :z [11 22 22]]]
                                             {:q {:w [11 33 33]}}
                                             {:path-argument [3 3]
                                              :path-return [:q :w]
                                              :relationship-fn same-elements?})
      {:path-argument [3 3], :path-return [:q :w], :relationship-fn same-elements?, :datum-argument [11 22 22], :datum-return [11 33 33], :valid? false}

      (validate-argument-return-relationship  {:x [11 22 33]}
                                              ['foo 'bar [99]]
                                              {:path-argument [:x]
                                               :path-return [2]
                                               :relationship-fn equal-count?})
      {:path-argument [:x], :path-return [2], :relationship-fn equal-count?, :datum-argument [11 22 33], :datum-return [99], :valid? false})))


;; definitions for testing (validate-fn-with-tests)
(defn one-arity [x] (vector (* 2 x) (+ 100 x)))

(def second-bigger-than-first? #(< (=1st %) (=2nd %)))

(def one-arity-specs {:speculoos/arg-scalar-spec [int?]
                      :speculoos/arg-collection-spec [#(= 1 (count %))]
                      :speculoos/ret-scalar-spec [int? int?]
                      :speculoos/ret-collection-spec [vector?
                                                      second-bigger-than-first?]})


(defn two-arity [x y]  {:k1 (* 3 x) :k2 (vec (take 3 (repeat (+ 10 y))))})

(def two-arity-specs {:speculoos/arg-scalar-spec [double? double?]
                      :speculoos/arg-collection-spec [#(= 2 (count %))]
                      :speculoos/ret-scalar-spec {:k1 double?
                                                  :k2 [double? double? double?]}
                      :speculoos/ret-collection-spec {:k2 [vector?
                                                           #(apply = %)]}})


(deftest validate-fn-with-tests
  (testing "basic operations"
    (are [x y] (= x y)
      ["z" 7.4 \z]
      (validate-fn-with fn-spec-test-example fn-example-specs 3 4.4)

      [{:path [0], :datum 3.1, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}]
      (validate-fn-with fn-spec-test-example fn-example-specs 3.1 4.4)

      [{:path [1], :datum 22/3, :predicate double?, :valid? false, :fn-spec-type :speculoos/argument}
       {:path [1], :datum 31/3, :predicate double?, :valid? false, :fn-spec-type :speculoos/return}]
      (validate-fn-with fn-spec-test-example fn-example-specs 3 22/3)
      ))
  (testing "multiple arities"
    (are [x y] (= x y)
      []
      (validate-fn-with (fn [] []) {:speculoos/ret-scalar-spec []
                                    :speculoos/ret-collection-spec [vector?]})

      [22 111]
      (validate-fn-with one-arity one-arity-specs 11)

      [{:path-predicate [1], :predicate second-bigger-than-first?, :datum [2222 1211], :ordinal-path-datum [], :path-datum [], :valid? false, :fn-spec-type :speculoos/return}]
      (validate-fn-with one-arity one-arity-specs 1111)

      [{:path [0], :datum 22, :predicate double?, :valid? false, :fn-spec-type :speculoos/argument}
       {:path [:k1], :datum 66, :predicate double?, :valid? false, :fn-spec-type :speculoos/return}]
      (validate-fn-with two-arity two-arity-specs 22 24.5)

      {:k1 66.6, :k2 [34.5 34.5 34.5]}
      (validate-fn-with two-arity two-arity-specs 22.2 24.5)))
  (testing "partially-supplied specs"
    (are [x] (= {:k1 66.6, :k2 [34.5 34.5 34.5]})
      (validate-fn-with two-arity (dissoc two-arity-specs :speculoos/ret-scalar-spec) 22.2 24.5)
      (validate-fn-with two-arity (dissoc two-arity-specs :speculoos/ret-collection-spec) 22.2 24.5)
      (validate-fn-with two-arity (dissoc two-arity-specs :speculoos/ret-scalar-spec :speculoos/ret-collection-spec) 22.2 24.5)
      (validate-fn-with two-arity (dissoc two-arity-specs :speculoos/ret-scalar-spec :speculoos/ret-collection-spec :speculoos/arg-collection-spec) 22.2 24.5)
      (validate-fn-with two-arity (dissoc two-arity-specs :speculoos/ret-scalar-spec :speculoos/ret-collection-spec :speculoos/arg-collection-spec :speculoos/arg-scalar-spec) 22.2 24.5))))


(deftest validate-fn-with-tests-extended
  (testing "all validate"
    (are [x] (= [7 12 144])
      (validate-fn-with fn-spec-test-example-2 {} 3 4)

      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]} 3 4)

      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]
                                                :speculoos/arg-collection-spec [#(= 2 (count %))]} 3 4)

      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]
                                                :speculoos/arg-collection-spec [#(= 2 (count %))]
                                                :speculoos/ret-scalar-spec [int? int? int?]} 3 4)

      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]
                                                :speculoos/arg-collection-spec [#(= 2 (count %))]
                                                :speculoos/ret-scalar-spec [int? int? int?]
                                                :speculoos/ret-collection-spec [#(< (=1st %) (=2nd %) (=3rd %))]} 3 4)))

  (testing "all invalid"
    (are [x y] (= x y)
      [{:path [0], :datum 3/2, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}]
      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]} 3/2 4)

      [{:path-predicate [0], :predicate count-is-one?, :datum [3 4], :ordinal-path-datum [], :path-datum [], :valid? false, :fn-spec-type :speculoos/argument}]
      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]
                                                :speculoos/arg-collection-spec [count-is-one?]} 3 4)

      [{:path [2], :datum 144, :predicate string?, :valid? false, :fn-spec-type :speculoos/return}]
      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]
                                                :speculoos/arg-collection-spec [#(= 2 (count %))]
                                                :speculoos/ret-scalar-spec [int? int? string?]} 3 4)

      [{:path-predicate [0], :predicate first-second-third-equal?, :datum [7 12 144], :ordinal-path-datum [], :path-datum [], :valid? false, :fn-spec-type :speculoos/return}]
      (validate-fn-with fn-spec-test-example-2 {:speculoos/arg-scalar-spec [int? int?]
                                                :speculoos/arg-collection-spec [#(= 2 (count %))]
                                                :speculoos/ret-scalar-spec [int? int? int?]
                                                :speculoos/ret-collection-spec [first-second-third-equal?]} 3 4)))

  (testing "lone return collection specification, GitHub Issue #4"
    (are [x y] (= x y)
      (validate-fn-with fn-spec-test-example-2 {:speculoos/ret-collection-spec [first-second-third-equal?]} 3 4)
      [{:datum [7 12 144], :valid? false, :path-predicate [0], :predicate first-second-third-equal?, :ordinal-path-datum [], :path-datum [], :fn-spec-type :speculoos/return}]

      (validate-fn-with #(vector (inc %)) {:speculoos/ret-collection-spec [map?]} 99)
      [{:datum [100], :valid? false, :path-predicate [0], :predicate map?, :ordinal-path-datum [], :path-datum [], :fn-spec-type :speculoos/return}])))


(deftest validate-fn-with-test-exceptions
  (testing "valid args"
    (is (= [9] (validate-fn-with exceptional-fn exceptional-specs 4 5))))
  (testing "args are consumable, but not spec valid"
    (is (= [{:path [1], :datum 5.5, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}]
         (validate-fn-with exceptional-fn exceptional-specs 4 5.5))))
  (testing "non-consumable args, throws exception"
    (is ((complement nil?) (re-find #"Exception: class java\.lang\.String cannot be cast to class java\.lang\.Number"
                                    (with-out-str (validate-fn-with exceptional-fn exceptional-specs 4 "5")))))))


(deftest validate-fn-with-test-scalar-return
  (are [x y] (= x y)
    9
    (validate-fn-with (fn [x] (inc x)) {:speculoos/ret-scalar-spec int?} 8)

    9
    (validate-fn-with (fn [x] (inc x)) {} 8)

    [{:path nil, :datum 9.8, :predicate int?, :valid? false, :fn-spec-type :speculoos/return}]
    (validate-fn-with (fn [x] (inc x)) {:speculoos/ret-scalar-spec int?} 8.8)

    "Exception: class java.lang.String cannot be cast to class java.lang.Number"
    (subs (with-out-str (validate-fn-with (fn [x] (inc x)) {:speculoos/ret-scalar-spec int?} "a")) 0 74)))


(deftest validate-fn-with-test-regex-predicate
  (are [x y] (= x y)
    "5" (validate-fn-with (fn [x] (str x)) {:speculoos/ret-scalar-spec #"\d"} 5)
    true (nil? (:valid? (validate-fn-with (fn [x] (str x)) {:speculoos/ret-scalar-spec #"\d"} "a")))))


(deftest validate-fn-with-individual-specs-tests
    (testing "individual spec supplied (or none), bare scalar return, predicate satisfied"
      (are [x] (= x 100)
        (validate-fn-with inc {} 99)
        (validate-fn-with inc {:speculoos/arg-scalar-spec int?} 99)
        (validate-fn-with inc {:speculoos/ret-scalar-spec int?} 99)))
    (testing "individual spec supplied, bare scalar, predicate not satisfied"
      (are [x y] (= x y)
        (validate-fn-with inc {:speculoos/arg-scalar-spec string?} 99)
        [{:path nil, :datum 99, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}]

        (validate-fn-with inc {:speculoos/ret-scalar-spec string?} 99)
        [{:path nil, :datum 100, :predicate string?, :valid? false, :fn-spec-type :speculoos/return}]))
    (testing "individual spec supplied (or none), return value is a collection, predicate satisfied"
      (are [x] (= x [100])
        (validate-fn-with #(vector (inc %)) {} 99)
        (validate-fn-with #(vector (inc %)) {:speculoos/arg-scalar-spec [int?]} 99)
        (validate-fn-with #(vector (inc %)) {:speculoos/arg-collection-spec [vector?]} 99)
        (validate-fn-with #(vector (inc %)) {:speculoos/ret-scalar-spec [int?]} 99)
        (validate-fn-with #(vector (inc %)) {:speculoos/ret-collection-spec [vector?]} 99)))
    (testing "individual spec supplied, return value is a collection, predicate not satisfied"
      (are [x y] (= x y)
        (validate-fn-with #(vector (inc %)) {:speculoos/arg-scalar-spec [string?]} 99)
        [{:path [0], :datum 99, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}]

        (validate-fn-with #(vector (inc %)) {:speculoos/arg-collection-spec [set?]} 99)
        [{:datum [99], :valid? false, :path-predicate [0], :predicate set?, :ordinal-path-datum [], :path-datum [], :fn-spec-type :speculoos/argument}]

        (validate-fn-with #(vector (inc %)) {:speculoos/ret-scalar-spec [string?]} 99)
        [{:path [0], :datum 100, :predicate string?, :valid? false, :fn-spec-type :speculoos/return}]

        (validate-fn-with #(vector (inc %)) {:speculoos/ret-collection-spec [set?]} 99)
        [{:datum [100], :valid? false, :path-predicate [0], :predicate set?, :ordinal-path-datum [], :path-datum [], :fn-spec-type :speculoos/return}])))


(deftest validate-fn-with-combo-tests
    (testing "bare arg scalar spec, regular return scalar spec"
      (are [x y] (= x y)
        (validate-fn-with #(vector (inc %)) {:speculoos/arg-scalar-spec [string?]
                                             :speculoos/ret-scalar-spec [char?]} 99)
        [{:path [0], :datum 99, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path [0], :datum 100, :predicate char?, :valid? false, :fn-spec-type :speculoos/return}]))
    (testing "regular arg scalar spec, bare arg return scalar spec"
      (are [x y] (= x y)
        (validate-fn-with #(inc (first %)) {:speculoos/arg-scalar-spec [[string?]]
                                            :speculoos/ret-scalar-spec char?} [99])
        [{:path [0 0], :datum 99, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path nil, :datum 100, :predicate char?, :valid? false, :fn-spec-type :speculoos/return}]))
    (testing "bare arg scalar spec, bare ret scalar spec"
      (are [x y] (= x y)
        (validate-fn-with inc {:speculoos/arg-scalar-spec [string?]
                               :speculoos/ret-scalar-spec char?} 99)
        [{:path [0], :datum 99, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path nil, :datum 100, :predicate char?, :valid? false, :fn-spec-type :speculoos/return}]))
    (testing "regular arg scalar spec, regular ret scalar spec"
      (are [x y] (= x y)
        (validate-fn-with #(-> (first %) inc vector) {:speculoos/arg-scalar-spec [[string?]]
                                                      :speculoos/ret-scalar-spec [char?]}
                          [99])
        [{:path [0 0], :datum 99, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path [0], :datum 100, :predicate char?, :valid? false, :fn-spec-type :speculoos/return}]

        (validate-fn-with identity {:speculoos/arg-scalar-spec [[string? char? boolean?]]
                                    :speculoos/ret-scalar-spec [string? char? boolean?]} [1 2 3])
        [{:path [0 0], :datum 1, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path [0 1], :datum 2, :predicate char?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path [0 2], :datum 3, :predicate boolean?, :valid? false, :fn-spec-type :speculoos/argument}
         {:path [0], :datum 1, :predicate string?, :valid? false, :fn-spec-type :speculoos/return}
         {:path [1], :datum 2, :predicate char?, :valid? false, :fn-spec-type :speculoos/return}
         {:path [2], :datum 3, :predicate boolean?, :valid? false, :fn-spec-type :speculoos/return}])))


;; example relationship specifications

(def relationship-spec-1 [{:path-argument [0]
                           :path-return []
                           :relationship-fn reversed?}
                          {:path-argument [0]
                           :path-return []
                           :relationship-fn same-elements?}
                          {:path-argument [0]
                           :path-return []
                           :relationship-fn equal-count?}])

(def relationship-spec-2 [{:path-argument [0]
                           :path-return nil
                           :relationship-fn correct-sum?}
                          {:path-argument [0]
                           :path-return nil
                           :relationship-fn four-elements?}
                          {:path-argument [0 3]
                           :path-return nil
                           :relationship-fn twice-plus-two?}])

(def relationship-spec-3 [{:path-argument [0]
                           :path-return []
                           :relationship-fn swapped?}
                          {:path-argument [0]
                           :path-return [:x]
                           :relationship-fn is-2?}])

(def relationship-spec-4 [{:path-argument [0]
                           :path-return nil
                           :relationship-fn correct-val-sum?}
                          {:path-argument [0]
                           :path-return nil
                           :relationship-fn correct-sign?}])

(def relationship-spec-5 [{:path-argument []
                           :path-return nil
                           :relationship-fn correct-plus?}])


(deftest validate-fn-with-relationship-tests
  (testing "vector argument, vector return"
    (are [x y] (= x y)
      (validate-fn-with my-reverser {:speculoos/argument-return-relationships relationship-spec-1} [11 22 33 44 55])
      [55 44 33 22 11]

      (validate-fn-with my-reverser-broken {:speculoos/argument-return-relationships relationship-spec-1} [11 22 33 44 55])
      [{:path-argument [0], :path-return [], :relationship-fn reversed?, :datum-argument [11 22 33 44 55], :datum-return [11 22 33 42], :valid? false, :fn-spec-type :speculoos/argument-return-relationship}
       {:path-argument [0], :path-return [], :relationship-fn same-elements?, :datum-argument [11 22 33 44 55], :datum-return [11 22 33 42], :valid? false, :fn-spec-type :speculoos/argument-return-relationship}
       {:path-argument [0], :path-return [], :relationship-fn equal-count?, :datum-argument [11 22 33 44 55], :datum-return [11 22 33 42], :valid? false, :fn-spec-type :speculoos/argument-return-relationship}]))
  (testing "vector argument, scalar return"
    (are [x y] (= x y)
      (validate-fn-with my-summation-er {:speculoos/argument-return-relationships relationship-spec-2} [1 2 3 4])
      10

      (validate-fn-with my-summation-er-broken {:speculoos/argument-return-relationships relationship-spec-2} [1 2 3 5 4])
      [{:path-argument [0], :path-return nil, :relationship-fn correct-sum?, :datum-argument [1 2 3 5 4], :datum-return 115, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}
       {:path-argument [0], :path-return nil, :relationship-fn four-elements?, :datum-argument [1 2 3 5 4], :datum-return 115, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}
       {:path-argument [0 3], :path-return nil, :relationship-fn twice-plus-two?, :datum-argument 5, :datum-return 115, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}]))
  (testing "map argument, map return"
    (are [x y] (= x y)
      (validate-fn-with swap-key-vals {:speculoos/argument-return-relationships relationship-spec-3} {:x 1 :y 2})
      {:x 2, :y 1}

      (validate-fn-with swap-key-vals-broken {:speculoos/argument-return-relationships relationship-spec-3} {:x 1 :y 2})
      [{:path-argument [0], :path-return [], :relationship-fn swapped?, :datum-argument {:x 1, :y 2}, :datum-return {:x 1, :y 2}, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}
       {:path-argument [0], :path-return [:x], :relationship-fn is-2?, :datum-argument {:x 1, :y 2}, :datum-return 1, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}]))
  (testing "map argument, scalar return"
    (are [x y] (= x y)
      (validate-fn-with sum-vals {:speculoos/argument-return-relationships relationship-spec-4} {:x 1 :y 2})
      3

      (validate-fn-with sum-vals-broken {:speculoos/argument-return-relationships relationship-spec-4} {:x 1 :y 2})
      [{:path-argument [0], :path-return nil, :relationship-fn correct-val-sum?, :datum-argument {:x 1, :y 2}, :datum-return -3, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}
       {:path-argument [0], :path-return nil, :relationship-fn correct-sign?, :datum-argument {:x 1, :y 2}, :datum-return -3, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}])
    (testing "scalar arguments, scalar return"
      (are [x y] (= x y)
        (validate-fn-with plus {:speculoos/argument-return-relationships relationship-spec-5} 1 2)
        3

        (validate-fn-with plus-broken {:speculoos/argument-return-relationships relationship-spec-5} 1 2)
        [{:path-argument [], :path-return nil, :relationship-fn correct-plus?, :datum-argument [1 2], :datum-return -1, :valid? false, :fn-spec-type :speculoos/argument-return-relationship}]))))


(defn fn-with-meta-specs
  {:speculoos/arg-scalar-spec [int? string? int? keyword?]
   :speculoos/arg-collection-spec [#(= 4 (count %)) #(= (=1st %) (=3rd %))]
   :speculoos/ret-scalar-spec [int? keyword? string?]
   :speculoos/ret-collection-spec [vector? #(= 3 (count %))]
   :UUIDv4 #uuid "02345f98-5f6a-4244-9cea-4ec82c83a220"}
  [w x y z]
  (vector (* w y) z (apply str (reverse x))))


(defn fn-with-meta-specs-scalar-return
  {:speculoos/arg-scalar-spec [int? double? ratio?]
   :speculoos/ret-scalar-spec number?
   :UUIDv4 #uuid "b9440df4-6d76-4aa6-8f16-7b17c49232cb"}
  [x y z] (+ x y z))


(defn fn-without-meta-specs
  {:UUIDv4 #uuid "e8344b71-3e0a-4fc6-85de-ef7dad36a5f2"}
  [x y z] (* x y z))


(comment
  (                 fn-with-meta-specs-scalar-return    2 3 4)
  (validate-fn-with fn-with-meta-specs-scalar-return {} 2 3 4)
  (validate-fn      fn-with-meta-specs-scalar-return    2 3 4)

  (                 fn-without-meta-specs    2 3 4)
  (validate-fn-with fn-without-meta-specs {} 2 3 4)
  (validate-fn      fn-without-meta-specs    2 3 4)
  (validate-fn-with fn-without-meta-specs {:speculoos/arg-scalar-spec [int? int? int?]} 2 3 4)
  (validate-fn-with fn-without-meta-specs {:speculoos/arg-scalar-spec [int? int? double?]} 2 3 4)
  (validate-fn-with fn-without-meta-specs {:speculoos/arg-scalar-spec [int? int? double?]
                                           :speculoos/ret-scalar-spec double?} 2 3 4)
  )


(deftest validate-fn-tests
  (testing "'container'-ed return"
    (are [x y] (= x y)
      [25 :baz "oof"] (validate-fn fn-with-meta-specs 5 "foo" 5 :baz)
      false (empty? (filter #(false? (:valid? %)) (validate-fn fn-with-meta-specs 5 "foo" 5 \z)))
      false (empty? (filter #(false? (:valid? %)) (validate-fn fn-with-meta-specs 5 "foo" 7 :baz)))))
  (testing "'bare' scalar return"
    (are [x y] (= x y)
      10.0 (validate-fn fn-with-meta-specs-scalar-return 3 4.5 5/2)
      false (empty? (filter #(false? (:valid? %)) (validate-fn fn-with-meta-specs-scalar-return 3.0 4.5 5/2)))
      false (empty? (filter #(false? (:valid? %)) (validate-fn fn-with-meta-specs-scalar-return 3 4 5/2)))
      false (empty? (filter #(false? (:valid? %)) (validate-fn fn-with-meta-specs-scalar-return 3 4.5 5)))
      true (= 3 (count  (filter #(false? (:valid? %)) (validate-fn fn-with-meta-specs-scalar-return 3.0 4 5)))))))


(defn HOF-1 [] 99)
(defn HOF-2 [a] (fn [b] (+ a b)))
(defn HOF-3 [a b] (+ a b))

(defn HOF-4
  [a b]
  (fn [c d]
    (+ a b c d)))

(defn HOF-5
  [a b]
  (fn [c d]
    (fn [e f]
      (fn [g h]
        (+ a b c d e f g h )))))


(deftest exhaust-higher-order-fn-tests
  (are [x y] (= x y)
    (exhaust-higher-order-fn HOF-1) 99
    (exhaust-higher-order-fn HOF-2 [5] [50]) 55
    (exhaust-higher-order-fn HOF-3 [1 20]) 21
    (exhaust-higher-order-fn HOF-4 [1 20] [300 4000]) 4321
    (exhaust-higher-order-fn HOF-5 [1 20] [300 4000] [50000 600000] [7000000 80000000]) 87654321))


(defn X0 [a] a)
(defn X1 {} [a] a)
(defn X2 {:speculoos/arg-scalar-spec :A} [a] a)
(defn X3 {:speculoos/arg-scalar-spec :A
          :speculoos/arg-collection-spec :B} [a] a)
(defn X4 {:speculoos/arg-scalar-spec :A
          :speculoos/arg-collection-spec :B
          :speculoos/hof-specs {}} [a] a)
(defn X5 {:speculoos/arg-scalar-spec :A
          :speculoos/arg-collection-spec :B
          :speculoos/hof-specs {:speculoos/arg-scalar-spec :C}} [a] a)
(defn X6 {:speculoos/arg-scalar-spec :A
          :speculoos/arg-collection-spec :B
          :speculoos/hof-specs {:speculoos/arg-scalar-spec :C
                                :speculoos/arg-collection-spec :D
                                :speculoos/not-recognized :shouldnt-appear}} [a] a)
(defn X7 {:speculoos/arg-scalar-spec :A
          :speculoos/arg-collection-spec :B
          :speculoos/hof-specs {:speculoos/arg-scalar-spec :C
                                :speculoos/arg-collection-spec :D
                                :speculoos/not-recognized :shouldnt-appear
                                :speculoos/hof-specs {:speculoos/arg-scalar-spec :E
                                                      :speculoos/arg-collection-spec :F
                                                      :speculoos/ret-scalar-spec :G
                                                      :speculoos/ret-collection-spec :H}}} [a] a)


(deftest flatten-nested-hof-fn-specs-tests
  (are [x y] (= x y)
    (flatten-nested-hof-fn-specs X0)
    [{}]

    (flatten-nested-hof-fn-specs X1)
    [{}]

    (flatten-nested-hof-fn-specs X2)
    [#:speculoos{:arg-scalar-spec :A}]

    (flatten-nested-hof-fn-specs X3)
    [#:speculoos{:arg-scalar-spec :A, :arg-collection-spec :B}]

    (flatten-nested-hof-fn-specs X4)
    [#:speculoos{:arg-scalar-spec :A, :arg-collection-spec :B} {}]

    (flatten-nested-hof-fn-specs X5)
    [#:speculoos{:arg-scalar-spec :A, :arg-collection-spec :B}
     #:speculoos{:arg-scalar-spec :C}]

    (flatten-nested-hof-fn-specs X6)
    [#:speculoos{:arg-scalar-spec :A, :arg-collection-spec :B}
     #:speculoos{:arg-scalar-spec :C, :arg-collection-spec :D}]

    (flatten-nested-hof-fn-specs X7)
    [#:speculoos{:arg-scalar-spec :A, :arg-collection-spec :B}
     #:speculoos{:arg-scalar-spec :C, :arg-collection-spec :D}
     #:speculoos{:arg-scalar-spec :E, :arg-collection-spec :F, :ret-scalar-spec :G, :ret-collection-spec :H}]))


(defn Y0 [a] a)

(defn Y1 {:speculoos/arg-scalar-spec [char?]} [a] a)

(defn Y2 {:speculoos/arg-scalar-spec [char? string?]} [a] a)

(defn Y3 {:speculoos/arg-scalar-spec [int? string? char?]} [a _ _] a)

(defn Y4
  {:speculoos/hof-specs {:speculoos/arg-scalar-spec [keyword? boolean? symbol?]}}
  [_ _ _]
  (fn [_ _ a] a))

(defn Y5
  {:speculoos/arg-scalar-spec [int?]
   :speculoos/hof-specs {:speculoos/arg-scalar-spec [string?]
                         :speculoos/hof-specs {:speculoos/arg-scalar-spec [keyword? boolean? symbol?]}}}
  [_ _ _]
  (fn [_ _ _]
    (fn [_ _ _]
      (fn [_ _ a] a))))


(deftest validate-flattened-fn-meta-specs-tests
  (are [x y] (= x y)
    (validate-flattened-fn-meta-specs Y0 [:foo])
    []

    (validate-flattened-fn-meta-specs Y1 [:baz])
    [{:path [0 0], :datum :baz, :predicate char?, :valid? false}]

    (validate-flattened-fn-meta-specs Y2 [:baz])
    [{:path [0 0], :datum :baz, :predicate char?, :valid? false}]

    (validate-flattened-fn-meta-specs Y3 [99 "abc" :not-a-char])
    [{:path [0 0], :datum 99, :predicate int?, :valid? true}
     {:path [0 1], :datum "abc", :predicate string?, :valid? true}
     {:path [0 2], :datum :not-a-char, :predicate char?, :valid? false}]

    (validate-flattened-fn-meta-specs Y4 [11 22 33] [:foo true 'Q])
    [{:path [1 0], :datum :foo, :predicate keyword?, :valid? true}
     {:path [1 1], :datum true, :predicate boolean?, :valid? true}
     {:path [1 2], :datum 'Q, :predicate symbol?, :valid? true}]

    (validate-flattened-fn-meta-specs Y5 [11 22 33] ["a" "b" "c"] [:foo true 'Q])
    [{:path [0 0], :datum 11, :predicate int?, :valid? true}
     {:path [1 0], :datum "a", :predicate string?, :valid? true}
     {:path [2 0], :datum :foo, :predicate keyword?, :valid? true}
     {:path [2 1], :datum true, :predicate boolean?, :valid? true}
     {:path [2 2], :datum 'Q, :predicate symbol?, :valid? true}]))


(def count-is-two? #(= 2 (count %)))
(def first-bigger-than-second? #(> %1 %2))
(defn Z0 {} [a b] (fn [c d] (vector (+ a b c d))))
(defn Z1 {:speculoos/arg-scalar-spec [int? int?]} [a b] (fn [c d] (vector (+ a b c d))))
(defn Z2 {:speculoos/arg-scalar-spec [int? int?]
          :speculoos/hof-specs {:speculoos/ret-scalar-spec [ratio?]}} [a b] (fn [c d] (vector (+ a b c d))))
(defn Z3 {:speculoos/arg-scalar-spec [int? int?]
          :speculoos/hof-specs {:speculoos/ret-scalar-spec [string?]
                                :speculoos/ret-collection-spec [count-is-two?]}} [a b] (fn [c d] (vector (+ a b c d))))


(deftest validate-hof-return-spec-tests
  (are [x y] (= x y)
    (validate-hof-return-spec Z0 [1 20] [300 4000])
    [4321]

    (validate-hof-return-spec Z1 [1 20] [300 4000])
    [4321]

    (validate-hof-return-spec Z2 [1 20] [300 4000])
    [{:path [0], :datum 4321, :predicate ratio?, :valid? false, :fn-spec-type :speculoos/return}]

    (validate-hof-return-spec Z3 [1 20] [300 4000])
    [{:path [0], :datum 4321, :predicate string?, :valid? false, :fn-spec-type :speculoos/return}
     {:path-predicate [0], :predicate count-is-two?, :datum [4321], :ordinal-path-datum [], :path-datum [], :valid? false, :fn-spec-type :speculoos/return}]))


;; test functions of higher-order-function validation
(def arg1-equals-arg2? #(= (=1st %) (=2nd %)))

(defn Q0 [a] (fn [b] (+ a b)))
(defn Q1
  {:speculoos/arg-scalar-spec [int?]
   :speculoos/hof-specs {:speculoos/arg-scalar-spec [int?]
                         :speculoos/ret-scalar-spec int?}}
  [a]
  (fn [b] (+ a b)))

(defn Q2
  {:speculoos/arg-scalar-spec [int?]
              :speculoos/hof-specs {:speculoos/arg-scalar-spec [int?]
                                    :speculoos/ret-scalar-spec [int?]}}
  [a]
  (fn [b] (vector (+ a b))))

(defn Q3
  {:speculoos/arg-scalar-spec [int? int?]
                :speculoos/arg-collection-spec [arg1-equals-arg2?]
                :speculoos/hof-specs {:speculoos/ret-scalar-spec [int? int?]
                                      :speculoos/ret-collection-spec [arg1-equals-arg2?]}}
  [a b]
  (fn [c d] (vector (* a c) (+ b d))))

(defn Q4
  {:speculoos/arg-scalar-spec [int?]
              :speculoos/hof-specs {:speculoos/arg-scalar-spec [double?]
                                    :speculoos/ret-scalar-spec [int?]}}
  [a]
  (fn [b] (vector (int (+ a b)))))

(defn Q5
  "Q5 docstring"
  {:Q5-metadata "yippee!"
   :speculoos/arg-scalar-spec [int? int?]
   :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? double?]
                         :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? number?]
                                               :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? double?]
                                                                     :speculoos/ret-scalar-spec int?}}}}
  [a b]
  (fn Qa [c d]
    (fn Qb [e f]
      (fn Qc [g h] (int (+ a b c d e f g h))))))

(comment ;; sanity check
  ((Q0 3.0) 4.0)
  ((Q1 3.0) 4.0)
  ((Q2 3.0) 4.0)
  ((Q3 3.0 4.0) 5.0 6.0)
  ((Q4 3.0) 4.0)
  ((((Q5 1 20) 300 4000) 50000 600000) 7000000 80000000)
  )


(defn hof-bare-return
  "Demo with bare return."
  {:UUIDv4 #uuid "40f3fc37-bdb0-45c2-a0f7-fff0f795a8eb"
   :speculoos/arg-scalar-spec [int?]
   :speculoos/hof-specs {:speculoos/arg-scalar-spec [string?]
                         :speculoos/ret-scalar-spec #"[\d][\w]"}}
  [x]
  (fn [y] (str x y)))


(deftest validate-higher-order-fn-tests
  (testing "all valid"
    (are [x y] (= x y)
      7.0 (validate-higher-order-fn Q0 [3.0] [4.0])
      7 (validate-higher-order-fn Q1 [3] [4])
      [7] (validate-higher-order-fn Q2 [3] [4])
      [3 3] (validate-higher-order-fn Q3 [1 1] [3 2])
      [7] (validate-higher-order-fn Q4 [3] [4.0])
      87654321 (validate-higher-order-fn Q5 [1 20] [300 4000.0] [50000 600000] [7000000 8E7])))
  (testing "some invalids"
    (are [x y] (= x y)
      3 (count (validate-higher-order-fn Q1 [3.0] [4.0]))
      2 (count (validate-higher-order-fn Q1 [3] [4.0]))
      2 (count (validate-higher-order-fn Q1 [3.0] [4]))

      3 (count (validate-higher-order-fn Q2 [3.0] [4.0]))
      2 (count (validate-higher-order-fn Q2 [3] [4.0]))
      2 (count (validate-higher-order-fn Q2 [3.0] [4]))

      4 (count (validate-higher-order-fn Q3 [1.0 1.0] [3.0 2.0]))
      1 (count (validate-higher-order-fn Q3 [3 3] [4 4]))
      2 (count (validate-higher-order-fn Q3 [3 4] [5 6]))
      6 (count (validate-higher-order-fn Q3 [3.0 4.0] [5.0 6.0]))

      1 (count (validate-higher-order-fn Q4 [3] [4]))
      1 (count (validate-higher-order-fn Q4 [3.0] [4.0]))
      2 (count (validate-higher-order-fn Q4 [3.0] [4]))

      2 (count (validate-higher-order-fn Q5 [1 20] [300 4000] [50000 600000] [7000000 80000000]))))

  (testing "bare returns"
    (are [x y] (= x y)
      "7q" (validate-higher-order-fn hof-bare-return [7] ["q"])
      2 (count (keep #(not (:valid? %)) (validate-higher-order-fn hof-bare-return ["q"] ["q"]))))))


(defn adder
  "Canonical example of a function-returning function."
  {:speculoos/arg-scalar-spec [int?]
   :speculoos/ret-scalar-spec fn?
   :speculoos/hof-specs {:speculoos/arg-scalar-spec [int?]}}
  [x]
  (with-meta (fn [y] (+ x y))
    {:adder-metadata "foobar!"}))


(def count-is-three? #(= 3 (count %)))
(defn multiplier
  "Fancier demo of a function-returning function.
   Returns a function that accepts a vector and multiplies its elements."
  {:UUIDv4 #uuid "9f90ce25-9322-4738-8aa2-e5a98fcab120"
   :speculoos/arg-scalar-spec [int?]
   :speculoos/arg-collection-spec []
   :speculoos/ret-scalar-spec fn?
   :speculoos/hof-specs {:speculoos/arg-collection-spec [[count-is-three?
                                                          vector?]]
                         :speculoos/arg-scalar-spec [[int? int? int?]]
                         :speculoos/ret-scalar-spec [int? int? int?]}}
  [x]
  (with-meta (fn [v] (map #(* % x) v))
    {:multiplier-metadata "multiplier go!"}))


(comment
  ;; sanity check
  (adder 5)
  ((adder 5) 10)
  (fn? (adder 5))
  (type (adder 5))
  (isa? clojure.lang.AFunction$1 (type (adder 5)))
  (:speculoos/hof-specs (fn-meta adder))
  (validate-fn adder 5)
  (validate-fn adder 5.5)
  ((validate-fn adder 5) 10)
  (rearrange-specs (flatten-nested-hof-fn-specs adder) :speculoos/arg-scalar-spec)

  (ns-unmap *ns* 'multiplier)
  (fn-meta multiplier)
  ((multiplier 10) [11 22 33])
  (validate-fn multiplier 10)
  ((validate-fn multiplier 10) [11 22 33])
  (validate-fn multiplier "a")
  )


(deftest validate-higher-order-fn-tests
  (testing "simple HOF"
    (are [x y] (= x y)
      15 (validate-higher-order-fn adder [5] [10])
      true (some #(false? (:valid? %)) (validate-higher-order-fn adder [5.5] [10]))
      true (some #(false? (:valid? %)) (validate-higher-order-fn adder [5] [10.5]))
      true (some #(false? (:valid? %)) (validate-higher-order-fn adder [5.5] [10.5]))))
  (testing "slightly more sophisticated HOF"
    (are [x y] (= x y)
      [110 220 330] (validate-higher-order-fn multiplier [10] [[11 22 33]])
      true (some #(false? (:valid? %)) (validate-higher-order-fn multiplier [10] ['(11 22 33)]))
      true (some #(false? (:valid? %)) (validate-higher-order-fn multiplier [0.1] [[11 22 33]]))
      true (some #(false? (:valid? %)) (validate-higher-order-fn multiplier [10] [[100 200.0 300]])))))


(defn fn-no-args [])
(defn fn-varargs [& args])

(deftest fn-args-tests
  (are [x y] (= x y)
    '([x]) (fn-args inc)
    '([]) (fn-args fn-no-args)
    '([& args]) (fn-args fn-varargs)
    (fn-args map) '([f] [f coll] [f c1 c2] [f c1 c2 c3] [f c1 c2 c3 & colls])))


(comment ;; I can't seem to make this block true; not smart enough to handle the mutability.
  (ns-unmap *ns* 'update-dissoc-old-new-fn-mappings-tests)

  (reset! old-new-fn {})

  (deftest update-dissoc-old-new-fn-mappings-tests
    (testing "updating function registry"
      (is (= {#'speculoos.function-specs-test/fn-spec-test-example speculoos.function-specs-test/fn-spec-test-example}
             (do (update-old-new-fn-mappings fn-spec-test-example)
                 @old-new-fn))
          (= fn-spec-test-example
             (get @old-new-fn (resolve 'fn-spec-test-example)))))
    (testing "dissoc-ing function registry"
      (is (= {}
             (do (dissoc-old-new-fn-mappings fn-spec-test-example)
                 @old-new-fn)))))
  )


(deftest revert-fn-tests
  (is (= (macroexpand-1 `(revert-fn fn-spec-test-example))
         '(clojure.core/alter-var-root (var speculoos.function-specs-test/fn-spec-test-example)
                                       (clojure.core/constantly (clojure.core/get (clojure.core/deref speculoos.function-specs/old-new-fn)
                                                                                  (clojure.core/resolve (quote speculoos.function-specs-test/fn-spec-test-example))))))))



(comment ;; I guess I'm not smart enough to make good tests for mutable features.

  (inject-specs! fn-spec-test-example fn-example-specs)

  (deftest wrapping-fn-test
    (is (= ["z" 7.0 \z]
           ((wrapping-fn fn-spec-test-example) 4 3.0)))

    (is (= "({:path [1], :datum 3/2, :predicate #function[clojure.core/double?], :valid? false})\n({:path [1], :datum 11/2, :predicate #function[clojure.core/double?], :valid? false})\n"
           (with-out-str ((wrapping-fn fn-spec-test-example) 4 3/2))))

    (is (= "({:path [0], :datum 4.1, :predicate #function[clojure.core/int?], :valid? false} {:path [1], :datum 3/2, :predicate #function[clojure.core/double?], :valid? false})\n"
           (with-out-str ((wrapping-fn fn-spec-test-example) 4.1 3/2)))))
  )


(comment ;; manual demonstation of sequence
  (inject-specs! fn-spec-test-example fn-example-specs)

  ((wrapping-fn fn-spec-test-example) 4   3.1)
  ((wrapping-fn fn-spec-test-example) 4   3/2)
  ((wrapping-fn fn-spec-test-example) 4.3 3.1)

  (fn-spec-test-example 4   3.0)
  (fn-spec-test-example 4   3/2)
  (fn-spec-test-example 4.1 3/2)

  (instrument fn-spec-test-example)

  (fn-spec-test-example 4   3.0)
  (fn-spec-test-example 4   3/2)
  (fn-spec-test-example 4.1 3/2)
  (fn-spec-test-example 4 "z")

  (unstrument fn-spec-test-example)

  (fn-spec-test-example 4   3.0)
  (fn-spec-test-example 4   3/2)
  (fn-spec-test-example 4.1 3/2)
  )



(comment ;; more testing incompetance

  (instrument fn-spec-test-example)

  (deftest instrument-tests
    (is (= ["z" 7.0 \z]
           (fn-spec-test-example 4 3.0)))
    (is (= "({:path [0], :datum 4.1, :predicate #function[clojure.core/int?], :valid? false})\n"
           (with-out-str (fn-spec-test-example 4.1 3.0))))
    (is (= "({:path [0], :datum 4.1, :predicate #function[clojure.core/int?], :valid? false} {:path [1], :datum 3/2, :predicate #function[clojure.core/double?], :valid? false})\n"
           (with-out-str (fn-spec-test-example 4.1 3/2)))))

  (unstrument fn-spec-test-example)

  (deftest unstrument-tests
    (is (= ["z" 7.0 \z]
           (fn-spec-test-example 4 3.0)))
    (is (= ""
           (with-out-str (fn-spec-test-example 4.1 3.0))))
    (is (= ""
           (with-out-str (fn-spec-test-example 4.1 3/2)))))
  )


(comment
  ;; (instrument) and (unstrument) demonstration with non-collection return value and a bare predicate spec

  (ns-unmap *ns* 't2)
  (defn t2 [x] (inc x))
  (t2 99)

  (def t2-spec-valid {:speculoos/ret-scalar-spec int?})
  (def t2-spec-invalid {:speculoos/ret-scalar-spec string?})

  (validate-fn-with t2 t2-spec-valid 99)
  (validate-fn-with t2 t2-spec-invalid 99)

  (inject-specs! t2 t2-spec-valid)
  (meta #'t2)
  ((wrapping-fn t2) 99)
  ((wrapping-fn t2) "a")

  (instrument t2)
  (t2 99)
  (t2 9.9)
  (t2 "a")

  (unstrument t2)
  (t2 99)
  (t2 9.9)
  )


(defn exercise-fn-example-fn
  {:speculoos/arg-scalar-spec [int? string? keyword? #"F\dQ[a-z]"]
   :speculoos/ret-scalar-spec #"^third keyword is :\S*, first integer is \d*, middle string is \S*, last string is F\dQ[a-z]$"}
  [x s k r]
  (str "third keyword is " k ", first integer is " x ", middle string is " s ", last string is " r))


(comment
  (exercise-fn-example-fn 7 "Hello!" :Happy-Day "F5Qv")
  (validate-fn exercise-fn-example-fn 7 "Hello!" :Happy-Day "F5Qv")

  (exercise-fn-example-fn 'foo "Hello!" :Happy-Day "F7Qy")
  (validate-fn exercise-fn-example-fn 'foo "Hello!" :Happy-Day "F5Q9")
  )


(deftest exercise-fn-tests
  (are [x] (true? x)
    (every? #(valid-scalars? (first %) [int? string? keyword? #"F\dQ[a-z]"]) (exercise-fn exercise-fn-example-fn 9))
    (every? #(string? (second %)) (exercise-fn exercise-fn-example-fn 7))
    (= 10 (count (exercise-fn exercise-fn-example-fn)))))


(run-tests)