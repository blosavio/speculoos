(ns speculoos.core-test
  (:require
   [clojure.test :refer [deftest are is run-tests testing]]
   [speculoos.core :refer :all]
   [speculoos.fn-in :refer [get-in* assoc-in*]]
   [speculoos.example-data-specs-core :refer :all]))


(deftest assoc-vector-tail-test
  (are [x y] (= x y)
    [33 44]
    (assoc-vector-tail [33 :to-be-replaced] 44)

    [11 22 33 55]
    (assoc-vector-tail [11 22 33 44] 55)

    [55]
    (assoc-vector-tail [] 55)

    [11 22 []]
    (assoc-vector-tail [11 22 33] [])

    [11 22 nil]
    (assoc-vector-tail [11 22 3] nil)
    ))


(deftest new-accumulator-test
  (are [x y] (= x y)
    [{:path [0] :value :root} {:path [1 :b :z] :value :next-value}]
    (new-accumulator [{:path [0] :value :root}] [1 :b nil] :z :next-value)

    [{:path [1 :a 5] :value :new-value}]
    (new-accumulator [] [1 :a 4] 5 :new-value)
    ))


(deftest new-path-test
  (are [x y] (= x y)
    [:a]
    (new-path [] :a)

    [11 22 33]
    (new-path [11 22 nil] 33)

    [:a 2 :b]
    (new-path [:a 2 :a] :b)
    ))

(deftest new-path-plus-depth-test
  (are [x y] (= x y)
    [:a nil]
    (new-path-plus-depth [] :a)

    [:b nil]
    (new-path-plus-depth [:a] :b)

    [:a :c nil]
    (new-path-plus-depth [:a :b] :c)
    ))


(deftest clamp-tests
  (testing "typical behavior"
    (are [x y] (= x y)
      (clamp [1 2 3 4 5 6] (cycle [11 22 33])) [[1 2 3 4 5 6] [11 22 33 11 22 33]]
      (clamp (iterate inc 100) [1 2 3 4 5]) [[100 101 102 103 104] [1 2 3 4 5]]
      (clamp [:a :b :c :d :e] (lazy-seq [11 22 33 44 55 66 77 88 99])) [[:a :b :c :d :e] [11 22 33 44 55]]
      (clamp (range 0 10) [1 2 3]) [[0 1 2] [1 2 3]]
      (clamp (range) [1 2 3]) [[0 1 2] [1 2 3]]
      (clamp #{1 2 3} (repeat 99)) [#{1 3 2} [99 99 99]]
      (clamp [] (range)) [[] []]
      (clamp (range) [99]) [[0] [99]]))
  (testing "two terminating collections (i.e., pass-through)"
    (is (= (clamp [1 2 3] [\a \b \c \d \e]) [[1 2 3] [\a \b \c \d \e]])))
  (testing "two non-terminating collections"
    (is (thrown? Exception (clamp (range) (cycle [11 22 33]))))))


(deftest all-paths-sub-function-test
  (testing "scalar element, vector container"
    (are [x y] (= x y)
      [{:path [:b :c :a] :value 99}]
      (all-paths-sub-function :a [] 99 [:b :c nil] (type []))

      [:prev-paths {:path [:k1 :k2 :new-key] :value :new-value}]
      (all-paths-sub-function :new-key [:prev-paths] :new-value [:k1 :k2 :old-key] (type []))))

  (testing "scalar element, map container"
    (are [x y] (= x y)
      [{:path [1 0 3] :value :foo}]
      (all-paths-sub-function 3 [] :foo [1 0 2] (type {}))))

  (testing "scalar element, set container"
    (are [x y] (= x y)
      [{:path [0 0 :new-key] :value :new-value}]
      (all-paths-sub-function :new-key [] :new-value [0 0 nil] (type #{}))))

  (testing "non-terminating collection"
    (are [x y] (= x y)
      [:prev-paths {:path [1 2 3 :new-key], :value '(0 1 2), :non-terminating? true}]
      (all-paths-sub-function :new-key [:prev-paths] (range 0 3) [1 2 3 nil] (type (range))))))


(deftest all-paths-test
  (testing "empty collections"
    (are [x y] (= x y)
      [{:path [] :value []}]
      (all-paths [])

      [{:path [] :value '()}]
      (all-paths '())

      [{:path [] :value {}}]
      (all-paths {})

      [{:path [] :value #{}}]
      (all-paths #{})

      [{:path [], :value '(), :non-terminating? true}]
      (all-paths (take 0 (cycle [])))

      [{:path [], :value '(), :non-terminating? true}]
      (all-paths (take 0 (iterate inc 1)))

      [{:path [], :value '(), :non-terminating? true}]
      (all-paths (lazy-seq []))

      [{:path [], :value '(), :non-terminating? true}]
      (all-paths (take 0 (range)))

      [{:path [], :value '(), :non-terminating? true}]
      (all-paths (take 0 (range 0 10)))

      [{:path [], :value '(), :non-terminating? true}]
      (all-paths (take 0 (repeat 99)))))

  (testing "single element collections"
    (are [x y] (= x y)
      [{:path [] :value [99]} {:path [0] :value 99}]
      (all-paths [99])

      [{:path [] :value '(99)} {:path [0] :value 99}]
      (all-paths '(99))

      [{:path [] :value {:a 99}} {:path [:a] :value 99}]
      (all-paths {:a 99})

      [{:path [] :value #{99}} {:path [99] :value 99}]
      (all-paths #{99})

      [{:path [], :value '(99), :non-terminating? true}]
      (all-paths (take 1 (cycle [99])))

      [{:path [], :value '(99), :non-terminating? true}]
      (all-paths (take 1 (iterate inc 99)))

      [{:path [], :value '(99), :non-terminating? true}]
      (all-paths (lazy-seq [99]))

      [{:path [], :value '(0), :non-terminating? true}]
      (all-paths (take 1 (range)))

      [{:path [], :value '(0), :non-terminating? true}]
      (all-paths (take 1 (range 0 10)))

      [{:path [], :value '(99), :non-terminating? true}]
      (all-paths (take 1 (repeat 99)))))

  (testing "two elements, one nested, homogeneous collection"
    (are [x y] (= x y)
      [{:path [] :value [11 [22]]} {:path [0] :value 11} {:path [1] :value [22]} {:path [1 0] :value 22}]
      (all-paths [11 [22]])

      [{:path [] :value '(11 (22))} {:path [0] :value 11} {:path [1] :value '(22)} {:path [1 0] :value 22}]
      (all-paths '(11 (22)))

      [{:path [] :value {:a {:b 99}}} {:path [:a] :value {:b 99}} {:path [:a :b] :value 99}]
      (all-paths {:a {:b 99}})

      (into #{} [{:path [] :value #{11 #{22}}}
                 {:path [11] :value 11}
                 {:path [#{22}] :value #{22}}
                 {:path [#{22} 22] :value 22}])
      (into #{} (all-paths #{11 #{22}}))
      ))

  (testing "multi-element homogeneous collection"
    (are [x y] (= x y)
      [{:path [], :value [11 22 33]} {:path [0], :value 11} {:path [1], :value 22} {:path [2], :value 33}]
      (all-paths [11 22 33])

      [{:path [], :value {:a 11, :b 22, :c 33}} {:path [:a], :value 11} {:path [:b], :value 22} {:path [:c], :value 33}]
      (all-paths {:a 11 :b 22 :c 33})

      [{:path [], :value '(11 22 33)} {:path [0], :value 11} {:path [1], :value 22} {:path [2], :value 33}]
      (all-paths '(11 22 33))

      [{:path [], :value #{33 22 11}} {:path [33], :value 33} {:path [22], :value 22} {:path [11], :value 11}]
      (all-paths #{11 22 33})

      [{:path [], :value '(11 22 33), :non-terminating? true}]
      (all-paths (take 3 (cycle [11 22 33])))

      [{:path [], :value '(11 22 33), :non-terminating? true}]
      (all-paths (take 3 (iterate #(+ 11 %) 11)))

      [{:path [], :value '(11 22 33), :non-terminating? true}]
      (all-paths (lazy-seq [11 22 33]))

      [{:path [], :value '(0 1 2), :non-terminating? true}]
      (all-paths (take 3 (range)))

      [{:path [], :value '(0 1 2), :non-terminating? true}]
      (all-paths (take 3 (range 0 10)))

      [{:path [], :value '(99 99 99), :non-terminating? true}]
      (all-paths (take 3 (repeat 99)))))

  (testing "simple, heterogeneous nested collection"
    (testing "maps"
      (are [x y] (= x y)
        [{:path [], :value {:a [11 [22]]}} {:path [:a], :value [11 [22]]} {:path [:a 0], :value 11} {:path [:a 1], :value [22]} {:path [:a 1 0], :value 22}]
        (all-paths {:a [11 [22]]})

        [{:path [], :value {:a {:b 11}}} {:path [:a], :value {:b 11}} {:path [:a :b], :value 11}]
        (all-paths {:a {:b 11}})

        [{:path [], :value {:a [11 22 {:b 33}]}} {:path [:a], :value [11 22 {:b 33}]} {:path [:a 0], :value 11} {:path [:a 1], :value 22} {:path [:a 2], :value {:b 33}} {:path [:a 2 :b], :value 33}]
        (all-paths {:a [11 22 {:b 33}]})

        [{:path [], :value {:a {:b {:c 33}}}} {:path [:a], :value {:b {:c 33}}} {:path [:a :b], :value {:c 33}} {:path [:a :b :c], :value 33}]
        (all-paths {:a {:b {:c 33}}})))

    (testing "vectors"
      (are [x y] (= x y)
        [{:path [], :value [11 [22 [33]] 44 [55]]} {:path [0], :value 11} {:path [1], :value [22 [33]]} {:path [1 0], :value 22} {:path [1 1], :value [33]} {:path [1 1 0], :value 33} {:path [2], :value 44} {:path [3], :value [55]} {:path [3 0], :value 55}]
        (all-paths [11 [22 [33]] 44 [55]])

        [{:path [], :value [#{11} 22 33]} {:path [0], :value #{11}} {:path [0 11], :value 11} {:path [1], :value 22} {:path [2], :value 33}]
        (all-paths [#{11} 22 33])

        [{:path [], :value [11 '(22) #{33}]} {:path [0], :value 11} {:path [1], :value '(22)} {:path [1 0], :value 22} {:path [2], :value #{33}} {:path [2 33], :value 33}]
        (all-paths [11 '(22) #{33}])))

    (testing "lists"
      (are [x y] (= x y)
        [{:path [], :value '(11 (22 (33)))} {:path [0], :value 11} {:path [1], :value '(22 (33))} {:path [1 0], :value 22} {:path [1 1], :value '(33)} {:path [1 1 0], :value 33}]
        (all-paths '(11 (22 (33))))))

    (testing "sets"
      (are [x y] (= x y)
        (into #{} [{:path [], :value #{:L1 #{:L2 #{:L3}}}} {:path [:L1], :value :L1} {:path [#{:L2 #{:L3}}], :value #{:L2 #{:L3}}} {:path [#{:L2 #{:L3}} :L2], :value :L2} {:path [#{:L2 #{:L3}} #{:L3}], :value #{:L3}} {:path [#{:L2 #{:L3}} #{:L3} :L3], :value :L3}])
        (into #{} (all-paths #{:L1 #{:L2 #{:L3}}})))
      ))

  (testing "big hairy nested, heterogeneous collections"
    (testing "don't contain a set"
      (are [x y] (= x y)
        [{:path [], :value {:a [11 22], :b '(33 44), :c 55}} {:path [:a], :value [11 22]} {:path [:a 0], :value 11} {:path [:a 1], :value 22} {:path [:b], :value '(33 44)} {:path [:b 0], :value 33} {:path [:b 1], :value 44} {:path [:c], :value 55}]
        (all-paths {:a [11 22] :b '(33 44) :c 55})

        [{:path [], :value [11 22 {:a 33, :b [44 55]} 66 [[77]] '(88 99)]} {:path [0], :value 11} {:path [1], :value 22} {:path [2], :value {:a 33, :b [44 55]}} {:path [2 :a], :value 33} {:path [2 :b], :value [44 55]} {:path [2 :b 0], :value 44} {:path [2 :b 1], :value 55} {:path [3], :value 66} {:path [4], :value [[77]]} {:path [4 0], :value [77]} {:path [4 0 0], :value 77} {:path [5], :value '(88 99)} {:path [5 0], :value 88} {:path [5 1], :value 99}]
        (all-paths [11 22 {:a 33 :b [44 55]} 66 [[77]] '(88 99)])

        [{:path [], :value {:a 11, :b [22 33], :c [44 55 [66 77]], :d 88, :e '(99 (111) 222), :f []}} {:path [:a], :value 11} {:path [:b], :value [22 33]} {:path [:b 0], :value 22} {:path [:b 1], :value 33} {:path [:c], :value [44 55 [66 77]]} {:path [:c 0], :value 44} {:path [:c 1], :value 55} {:path [:c 2], :value [66 77]} {:path [:c 2 0], :value 66} {:path [:c 2 1], :value 77} {:path [:d], :value 88} {:path [:e], :value '(99 (111) 222)} {:path [:e 0], :value 99} {:path [:e 1], :value '(111)} {:path [:e 1 0], :value 111} {:path [:e 2], :value 222} {:path [:f], :value []}]
        (all-paths {:a 11 :b [22 33] :c [44 55 [66 77]] :d 88 :e '(99 (111) 222) :f []})

        [{:path [], :value [:a :b [:e :f] :c :d {:g 11, :h 33} :i '(:j) :k {:p [[1.1] 2.2 [3.3]], :m [{:n '(4.4 5.5 6.6)}]}]} {:path [0], :value :a} {:path [1], :value :b} {:path [2], :value [:e :f]} {:path [2 0], :value :e} {:path [2 1], :value :f} {:path [3], :value :c} {:path [4], :value :d} {:path [5], :value {:g 11, :h 33}} {:path [5 :g], :value 11} {:path [5 :h], :value 33} {:path [6], :value :i} {:path [7], :value '(:j)} {:path [7 0], :value :j} {:path [8], :value :k} {:path [9], :value {:p [[1.1] 2.2 [3.3]], :m [{:n '(4.4 5.5 6.6)}]}} {:path [9 :p], :value [[1.1] 2.2 [3.3]]} {:path [9 :p 0], :value [1.1]} {:path [9 :p 0 0], :value 1.1} {:path [9 :p 1], :value 2.2} {:path [9 :p 2], :value [3.3]} {:path [9 :p 2 0], :value 3.3} {:path [9 :m], :value [{:n '(4.4 5.5 6.6)}]} {:path [9 :m 0], :value {:n '(4.4 5.5 6.6)}} {:path [9 :m 0 :n], :value '(4.4 5.5 6.6)} {:path [9 :m 0 :n 0], :value 4.4} {:path [9 :m 0 :n 1], :value 5.5} {:path [9 :m 0 :n 2], :value 6.6}]
        (all-paths [:a :b [:e :f] :c :d {:g 11 :h 33} :i '(:j) :k {:p [[1.1] 2.2 [3.3]] :m [{:n '(4.4 5.5 6.6)}]}])))

    (testing "*do* contain a set"
      (are [x y] (= x y)
        [{:path [], :value {:a 11, :b [22 33], :c [44 55 [66 77]], :d 88, :e #{222 99 111}, :f []}} {:path [:a], :value 11} {:path [:b], :value [22 33]} {:path [:b 0], :value 22} {:path [:b 1], :value 33} {:path [:c], :value [44 55 [66 77]]} {:path [:c 0], :value 44} {:path [:c 1], :value 55} {:path [:c 2], :value [66 77]} {:path [:c 2 0], :value 66} {:path [:c 2 1], :value 77} {:path [:d], :value 88} {:path [:e], :value #{222 99 111}} {:path [:e 222], :value 222} {:path [:e 99], :value 99} {:path [:e 111], :value 111} {:path [:f], :value []}]
        (all-paths {:a 11 :b [22 33] :c [44 55 [66 77]] :d 88 :e #{99 111 222} :f []})

        [{:path [], :value [#{:j} {:m [{:n #{5.5 6.6 4.4}}]}]} {:path [0], :value #{:j}} {:path [0 :j], :value :j} {:path [1], :value {:m [{:n #{5.5 6.6 4.4}}]}} {:path [1 :m], :value [{:n #{5.5 6.6 4.4}}]} {:path [1 :m 0], :value {:n #{5.5 6.6 4.4}}} {:path [1 :m 0 :n], :value #{5.5 6.6 4.4}} {:path [1 :m 0 :n 5.5], :value 5.5} {:path [1 :m 0 :n 6.6], :value 6.6} {:path [1 :m 0 :n 4.4], :value 4.4}]
        (all-paths [#{:j} {:m [{:n #{4.4 5.5 6.6}}]}])

        [{:path [], :value [:a :b [:e :f] :c :d {:g 1, :h 3} :i #{:j} :k {:p [[1.1] 2.2 [3.3]], :m [{:n #{5.5 6.6 4.4}}]}]} {:path [0], :value :a} {:path [1], :value :b} {:path [2], :value [:e :f]} {:path [2 0], :value :e} {:path [2 1], :value :f} {:path [3], :value :c} {:path [4], :value :d} {:path [5], :value {:g 1, :h 3}} {:path [5 :g], :value 1} {:path [5 :h], :value 3} {:path [6], :value :i} {:path [7], :value #{:j}} {:path [7 :j], :value :j} {:path [8], :value :k} {:path [9], :value {:p [[1.1] 2.2 [3.3]], :m [{:n #{5.5 6.6 4.4}}]}} {:path [9 :p], :value [[1.1] 2.2 [3.3]]} {:path [9 :p 0], :value [1.1]} {:path [9 :p 0 0], :value 1.1} {:path [9 :p 1], :value 2.2} {:path [9 :p 2], :value [3.3]} {:path [9 :p 2 0], :value 3.3} {:path [9 :m], :value [{:n #{5.5 6.6 4.4}}]} {:path [9 :m 0], :value {:n #{5.5 6.6 4.4}}} {:path [9 :m 0 :n], :value #{5.5 6.6 4.4}} {:path [9 :m 0 :n 5.5], :value 5.5} {:path [9 :m 0 :n 6.6], :value 6.6} {:path [9 :m 0 :n 4.4], :value 4.4}]
        (all-paths [:a :b [:e :f] :c :d {:g 1 :h 3} :i #{:j} :k {:p [[1.1] 2.2 [3.3]] :m [{:n #{4.4 5.5 6.6}}]}])

        (into #{} [{:path [], :value #{[:v1 :v2] '(:L1 :L2) {:m1 11, :m2 22} #{:s1}}} {:path [[:v1 :v2]], :value [:v1 :v2]} {:path [[:v1 :v2] 0], :value :v1} {:path [[:v1 :v2] 1], :value :v2} {:path ['(:L1 :L2)], :value '(:L1 :L2)} {:path ['(:L1 :L2) 0], :value :L1} {:path ['(:L1 :L2) 1], :value :L2} {:path [{:m1 11, :m2 22}], :value {:m1 11, :m2 22}} {:path [{:m1 11, :m2 22} :m1], :value 11} {:path [{:m1 11, :m2 22} :m2], :value 22} {:path [#{:s1}], :value #{:s1}} {:path [#{:s1} :s1], :value :s1}])
        (into #{} (all-paths #{[:v1 :v2] {:m1 11 :m2 22} '(:L1 :L2) #{:s1}}))
        ))

    (testing "nested non-terminating sequences"
      (are [x y] (= x y)
        [{:path [], :value [11 '(0 1 2) 22]} {:path [0], :value 11} {:path [1], :value '(0 1 2), :non-terminating? true} {:path [2], :value 22}]
        (all-paths [11 (range 0 3) 22])

        [{:path [], :value {:a '(11 22 33), :b 4}} {:path [:a], :value '(11 22 33), :non-terminating? true} {:path [:b], :value 4}]
        (all-paths {:a (lazy-seq [11 22 33]) :b 4})))))


(deftest only-non-collections-tests
  (are [x y] (= x y)
    [] (only-non-collections (all-paths []))
    [{:path [0], :value 11}] (only-non-collections (all-paths [11]))
    [{:path [0], :value 11}] (only-non-collections (all-paths [11 []]))
    [{:path [0], :value 11} {:path [2 0], :value 22}] (only-non-collections (all-paths [11 [] [22]]))))


(deftest only-fns-tests
  (are [x y] (= x y)
    [] (only-fns (all-paths []))
    [] (only-fns (all-paths [11]))
    [{:path [1], :value int?}] (only-fns (all-paths [11 int?]))))


(deftest intersection-of-paths-tests
  (are [x y] (= x y)
    #{:A :B :C}
    (intersection-of-paths [{:path :A}
                            {:path :B}
                            {:path :C}]
                           [{:path :A}
                            {:path :B}
                            {:path :C}])
    #{}
    (intersection-of-paths [{:path :A}
                            {:path :B}
                            {:path :C}]
                           [{:path :D}
                            {:path :E}
                            {:path :F}])
    #{:A :C}
    (intersection-of-paths [{:path :A}
                            {:path :B}
                            {:path :C}]
                           [{:path :A}
                            {:path :Z}
                            {:path :C}])))


(deftest intersection-of-paths-3-tests
  (are [x y] (= x y)
    #{:A :B :C}
    (intersection-of-paths-3 [{:path :A}
                              {:path :B}
                              {:path :C}]
                             [{:path :A}
                              {:path :B}
                              {:path :C}]
                             [{:path :A}
                              {:path :B}
                              {:path :C}])
    #{}
    (intersection-of-paths-3 [{:path :A}
                              {:path :B}
                              {:path :C}]
                             [{:path :D}
                              {:path :E}
                              {:path :F}]
                             [{:path :G}
                              {:path :H}
                              {:path :I}])
    #{:A :C}
    (intersection-of-paths-3 [{:path :A}
                              {:path :B}
                              {:path :C}]
                             [{:path :A}
                              {:path :D}
                              {:path :C}]
                             [{:path :A}
                              {:path :E}
                              {:path :C}])))


(deftest only-sets-tests
  (are [x y] (= x y)
    [] (only-sets (all-paths []))
    [] (only-sets (all-paths [[11] {:a 22} (list 33)]))
    [{:path [0], :value #{11}}] (only-sets (all-paths [#{11}]))
    [{:path [1], :value #{22}}] (only-sets (all-paths [11 #{22} 33]))
    [{:path [0], :value #{11}} {:path [2], :value #{33}}] (only-sets (all-paths [#{11} 22 #{33} 44]))))


(deftest only-non-terminating-tests
  (testing "empty coll"
    (are [x y] (= x y)
      [] (only-non-terminating (all-paths []))))
  (testing "terminating coll"
    (are [x y] (= x y)
      [] (only-non-terminating (all-paths [11 22 33]))))
  (testing "un-nested non-terminating sequences"
    (are [x y] (= x y)
      [{:path [], :value '(0 1 2), :non-terminating? true}]
      (only-non-terminating (all-paths (take 3 (range))))

      [{:path [], :value '(\a \b \c), :non-terminating? true}]
      (only-non-terminating (all-paths (take 3 (cycle [\a \b \c]))))))
  (testing "non-terminating sequence, nested in a regular coll"
    (are [x y] (= x y)
      [{:path [1], :value '(0 1 2), :non-terminating? true}]
      (only-non-terminating (all-paths [11 (range 0 3) 22]))

      [{:path [:a], :value '(0 1 2), :non-terminating? true} {:path [:c], :value '(11 22 33), :non-terminating? true}]
      (only-non-terminating (all-paths {:a (range 0 3) :b 22 :c (take 3 (iterate #(+ 11 %) 11))}))

      [{:path [:b 1 1 2], :value '(99 99 99), :non-terminating? true}]
      (only-non-terminating (all-paths {:a 11 :b [22 (list 33 [44 55 (take 3 (repeat 99))])]})))))


(deftest validate-non-terminating-sequence-elements-tests
  (testing "both fully-terminating data and spec"
    (is (= [] (validate-non-terminating-sequence-elements [99 "abc" :foo] [int? string? keyword?]))))
  (testing "non-terminating data sequences"
    (are [x y] (= x y)
      (validate-non-terminating-sequence-elements (cycle [11 "abc" :foo]) [int? string? keyword?])
      [[{:path [0], :datum 11, :predicate int?, :valid? true}
        {:path [1], :datum "abc", :predicate string?, :valid? true}
        {:path [2], :datum :foo, :predicate keyword?, :valid? true}]]

      (validate-non-terminating-sequence-elements (iterate inc 1) [int? int? int?])
      [[{:path [0], :datum 1, :predicate int?, :valid? true}
        {:path [1], :datum 2, :predicate int?, :valid? true}
        {:path [2], :datum 3, :predicate int?, :valid? true}]]

      (validate-non-terminating-sequence-elements (lazy-seq [11 true \c 22/7]) [int? boolean? char? ratio?])
      [[{:path [0], :datum 11, :predicate int?, :valid? true}
        {:path [1], :datum true, :predicate boolean?, :valid? true}
        {:path [2], :datum \c, :predicate char?, :valid? true}
        {:path [3], :datum 22/7, :predicate ratio?, :valid? true}]]

      (validate-non-terminating-sequence-elements (range) [int? int? int?])
      [[{:path [0], :datum 0, :predicate int?, :valid? true}
        {:path [1], :datum 1, :predicate int?, :valid? true}
        {:path [2], :datum 2, :predicate int?, :valid? true}]]

      (validate-non-terminating-sequence-elements (range 0 3) [int? int? int?])
      [[{:path [0], :datum 0, :predicate int?, :valid? true}
        {:path [1], :datum 1, :predicate int?, :valid? true}
        {:path [2], :datum 2, :predicate int?, :valid? true}]]

      (validate-non-terminating-sequence-elements (repeat "xyz") [string? string? string?])
      [[{:path [0], :datum "xyz", :predicate string?, :valid? true}
        {:path [1], :datum "xyz", :predicate string?, :valid? true}
        {:path [2], :datum "xyz", :predicate string?, :valid? true}]]

      (validate-non-terminating-sequence-elements (repeat [11 ["abc" [:foo]]]) [[int? [string? [keyword?]]]])
      [[{:path [0 0], :datum 11, :predicate int?, :valid? true}
        {:path [0 1 0], :datum "abc", :predicate string?, :valid? true}
        {:path [0 1 1 0], :datum :foo, :predicate keyword?, :valid? true}]]))
  (testing "non-terminating specs sequences"
    (are [x y] (= x y)
      (validate-non-terminating-sequence-elements [1 "abc" :foo] (cycle [int? string? keyword?]))
      [[{:path [0], :datum 1, :predicate int?, :valid? true}
        {:path [1], :datum "abc", :predicate string?, :valid? true}
        {:path [2], :datum :foo, :predicate keyword?, :valid? true}]]

      (validate-non-terminating-sequence-elements [1 "abc" :foo] (lazy-seq [int? string? keyword?]))
      [[{:path [0], :datum 1, :predicate int?, :valid? true}
        {:path [1], :datum "abc", :predicate string?, :valid? true}
        {:path [2], :datum :foo, :predicate keyword?, :valid? true}]]

      (validate-non-terminating-sequence-elements [\a \b \c] (repeat char?))
      [[{:path [0], :datum \a, :predicate char?, :valid? true}
        {:path [1], :datum \b, :predicate char?, :valid? true}
        {:path [2], :datum \c, :predicate char?, :valid? true}]])))


(deftest any-non-terminating-paths?-tests
  (are [x y] (= x y)
    (any-non-terminating-paths? (all-paths [11 22 (range 0 3) 44]))
    '({:path [2], :value (0 1 2), :non-terminating? true})

    (any-non-terminating-paths? (all-paths [55 (range 4 7) 66 77]))
    '({:path [1], :value (4 5 6), :non-terminating? true})

    (any-non-terminating-paths? (all-paths {:a 11 :b [22 [33 [44 (range 55 58)]]]}))
    '({:path [:b 1 1 1], :value (55 56 57), :non-terminating? true})

    (any-non-terminating-paths? (all-paths {:a 66 :b [77 [88 [(range 222 225) 99]]]}))
    '({:path [:b 1 1 0], :value (222 223 224), :non-terminating? true})

    (any-non-terminating-paths? (all-paths [11 (range 0 2) 22 (range 100 102)]))
    '({:path [1], :value (0 1), :non-terminating? true}
      {:path [3], :value (100 101), :non-terminating? true})

    (any-non-terminating-paths? (all-paths [(range 3 5) 33 (range 400 402) 55]))
    '({:path [0], :value (3 4), :non-terminating? true}
      {:path [2], :value (400 401), :non-terminating? true})

    (any-non-terminating-paths? (all-paths [1 2 3]))
    '()

    (any-non-terminating-paths? (all-paths [11 {:a 22 :b #{33} :c [44 55]}]))
    '()))


(deftest expand-and-clamp-1-tests
  (testing "empty collections"
    (are [x y] (= x y)
      [] (expand-and-clamp-1 [] [])))

  (testing "zero non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp-1 [1 2 3] [4 5 6])
      [1 2 3]
      ))

  (testing "simple non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp-1 [11 (range 22 33)]
                          [44 [55 66]])
      [11 [22 23]]

      (expand-and-clamp-1 [11 22 (range 0 100) 33 44]
                          [55 66 [77 88 99] 111 222])
      [11 22 [0 1 2] 33 44]

      (expand-and-clamp-1 [11 (range 10) 22 (range 10)]
                          [33 [44 55] 66 [77 88 99] 111])
      [11 [0 1] 22 [0 1 2]]

      (expand-and-clamp-1 {:a 11 :b [22 (range 10) (range 10) :c (range 10)]}
                          {:a 33 :b [44 [55] [66 77 88 99]] :c []})
      {:a 11, :b [22 [0] [0 1 2 3] :c []]}

      (expand-and-clamp-1 [11 (range 10) 22]
                          [(range 10) [33 44 55] (range 10)])
      [11 [0 1 2] 22]

      (expand-and-clamp-1 [(range 10) (range 10) (range 10) (range 10) (range 10)]
                          [[11] [22 33] [44 55 66] [77 88 99 111] [222 333 444 555 666]])
      [[0] [0 1] [0 1 2] [0 1 2 3] [0 1 2 3 4]]))

  (testing "variety of non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp-1 [11 (take 99 (cycle [22 33 44])) 55]
                          [66 [77 88 99 111] 222])
      [11 [22 33 44 22] 55]

      (expand-and-clamp-1 [11 (take 99 (iterate inc 11)) 22]
                          [33 [44 55 66] 77])
      [11 [11 12 13] 22]

      (expand-and-clamp-1 [11 (take 99 (lazy-seq [22 33 44 55 66 77 88 99])) 111]
                          [222 [333 444 555] 666])
      [11 [22 33 44] 111]

      (expand-and-clamp-1 [11 (take 99 (range)) 22]
                          [33 [44 55 66] 77])
      [11 [0 1 2] 22]

      (expand-and-clamp-1 [11 (take 99 (range 0 100))22]
                          [33 [44 55 66] 77])
      [11 [0 1 2] 22]

      (expand-and-clamp-1 [11 (take 99 (repeat 22)) 33]
                          [44 [55 66 77] 88])
      [11 [22 22 22] 33]))

  (testing "bare non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp-1 (range 10)
                          [55 66 77])
      [0 1 2]))

  (testing "nested non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp-1 (repeat 10 [11 22 33])
                          [[44 55 66] [77 88 99] [111 222 333]])
      [[11 22 33] [11 22 33] [11 22 33]]

      (expand-and-clamp-1 [11 (repeat 5 (range 10)) 22]
                          [33 [[44 55] [66] [88 99 111]] 222])
      [11 [[0 1] [0] [0 1 2]] 22])))


(deftest expand-and-clamp-tests
  (testing "zero non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp [] [])
      [[] []]

      (expand-and-clamp [11] [22])
      [[11] [22]]

      (expand-and-clamp [11 22 33] [44 55 66])
      [[11 22 33] [44 55 66]]

      (expand-and-clamp [11 [22 [33]]] [[[44] 55] 66])
      [[11 [22 [33]]] [[[44] 55] 66]]

      (expand-and-clamp {:a 11 :b 22} {:c 33 :d 44})
      [{:a 11, :b 22} {:c 33, :d 44}]

      (expand-and-clamp '(11 22) '(33 44))
      ['(11 22) '(33 44)]

      (expand-and-clamp #{11 22} #{33 44})
      [#{22 11} #{33 44}]))

  (testing "simple non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp (take 99 (cycle [11 22 33])) [44 55 66])
      [[11 22 33] [44 55 66]]

      (expand-and-clamp [11 22 33] (take 99 (iterate #(+ 11 %) 44)))
      [[11 22 33] [44 55 66]]

      (expand-and-clamp (lazy-seq [11 22 33 44 55]) '(44 55 66))
      [[11 22 33] '(44 55 66)]

      (expand-and-clamp [11 22 33] (take 99 (range)))
      [[11 22 33] [0 1 2]]

      (expand-and-clamp (range 0 99) [11 22 33])
      [[0 1 2] [11 22 33]]

      (expand-and-clamp [11 22 33] (take 99 (repeat 99)))
      [[11 22 33] [99 99 99]]))

  (testing "one-level deep nesting non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp [11 (take 99 (cycle [22 33 44])) 55]
                        [66 [77 88 99] 111])
      [[11 [22 33 44] 55] [66 [77 88 99] 111]]

      (expand-and-clamp [11 [22 33 44] 55]
                        [66 (take 99 (iterate #(+ 111 %) 111)) 77])
      [[11 [22 33 44] 55] [66 [111 222 333] 77]]

      (expand-and-clamp {:a 11 :b (lazy-seq [22 33 44 55 66]) :c 55}
                        {:a 66 :b [77 88 99] :c 111})
      [{:a 11, :b [22 33 44], :c 55} {:a 66, :b [77 88 99], :c 111}]

      (expand-and-clamp '(11 (22 33 44) 55)
                        (list 66 (take 99 (range)) 111))
      ['(11 (22 33 44) 55) '(66 [0 1 2] 111)]

      (expand-and-clamp [11 (range 99) 22] [33 [44 55 66] 77])
      [[11 [0 1 2] 22] [33 [44 55 66] 77]]

      (expand-and-clamp [11 [22 33 44] 55] [66 (take 99 (repeat 99)) 77])
      [[11 [22 33 44] 55] [66 [99 99 99] 77]]))

  (testing "non-terminating in both (but not at the same path)"
    (are [x y] (= x y)
      (expand-and-clamp [[11 11 11] (take 99 (cycle [22 33 44])) 55]
                        [(take 99 (iterate inc 66)) [77 88 99 111] 222])
      [[[11 11 11] [22 33 44 22] 55]
       [[66 67 68] [77 88 99 111] 222]]

      (expand-and-clamp {:a (lazy-seq [11 22 33 44 55]) :b [44 55 66]}
                        {:a [77 88 99] :b (range 99)})
      [{:a [11 22 33], :b [44 55 66]}
       {:a [77 88 99], :b [0 1 2]}]

      (expand-and-clamp (list (take 99 (range)) [11 22 33])
                        (list [44 55 66] (take 99 (repeat 77))))
      ['([0 1 2] [11 22 33])
       '([44 55 66] [77 77 77])]))

  (testing ">1 level deep nesting"
    (are [x y] (= x y)
      (expand-and-clamp [[[(take 99 (cycle [11 22 33]))]]]
                        [[[[44 55 66]]]])
      [[[[[11 22 33]]]]
       [[[[44 55 66]]]]]

      (expand-and-clamp {:a {:b [11 {:c (take 99 (iterate inc 1))}]}}
                        {:a {:b [22 {:c [33 44 55]}]}})
      [{:a {:b [11 {:c [1 2 3]}]}}
       {:a {:b [22 {:c [33 44 55]}]}}]

      (expand-and-clamp (list 11 (list 22 (list 33 (range 99))))
                        (list 44 (list 55 (list 66 [77 88 99]))))
      ['(11 (22 (33 [0 1 2])))
       '(44 (55 (66 [77 88 99])))]))

  (testing "non-terminating sequences nesting in other non-terminating sequences"
    (are [x y] (= x y)
      (expand-and-clamp [11 (take 99 (cycle [(take 99 (repeat 11))
                                             (take 99 (repeat 22))
                                             (take 99 (repeat 33))])) 44]
                        [55 [[66 66 66]
                             [77 88 99]
                             [111 222 333]]
                         444])
      [[11 [[11 11 11] [22 22 22] [33 33 33]] 44]
       [55 [[66 66 66] [77 88 99] [111 222 333]] 444]]

      (expand-and-clamp [11 (take 9 (iterate (fn [[x y]] (vector (+ 1 x) (+ 10 y))) [11 22])) 33]
                        [22 [[33 44] [55 66] [77 88]] 99])
      [[11 [[11 22] [12 32] [13 42]] 33]
       [22 [[33 44] [55 66] [77 88]] 99]]

      (expand-and-clamp [[11] [22 33] [44 55 66] [77 88 99 111]]
                        (repeat 9 (range 0 9)))
      [[[11] [22 33] [44 55 66] [77 88 99 111]]
       [[0] [0 1] [0 1 2] [0 1 2 3]]])))


(deftest regex?-tests
  (are [x] (true? x)
    (regex? #"")
    (regex? #"foo")
    (regex? #"^A[bc].[123]+$")
    (not (regex? ""))))


(deftest validate-set-as-predicate-tests
  (are [x] (true? x)
    (empty? (filter #(not (:valid? %)) (validate-set-as-predicate data-1-set-predicate spec-1-set-predicate)))
    (not (:valid? (filter #(not (:valid? %)) (validate-set-as-predicate data-2-set-predicate spec-2-set-predicate))))))


(deftest apply-all-predicates-within-spec-set-tests
    (testing "empties"
      (are [x] (empty? x)
        (apply-all-predicates-within-spec-set #{} #{} [])
        (apply-all-predicates-within-spec-set #{1 2 3} #{} [])
        (apply-all-predicates-within-spec-set #{} #{int?} [])
        (apply-all-predicates-within-spec-set #{} #{int? string?} [])))

    (testing "all true"
      (are [x] (every? #(true? (:valid? %)) x)
        (apply-all-predicates-within-spec-set #{1 2 3} #{int?} [:b 2 :x 5])
        (apply-all-predicates-within-spec-set #{1 2 3} #{int? number?} [:x 3 :z 2])
        (apply-all-predicates-within-spec-set #{:a :b :c} #{keyword? simple-keyword?} [:v :q 4 3])))

    (testing "some false"
      (are [x] (some #(not (:valid? %)) x)
        (apply-all-predicates-within-spec-set #{1 2 3} #{int? string?} [])
        (apply-all-predicates-within-spec-set #{:a 42 "abc"} #{int? string? keyword?} []))))

  (deftest validate-set-elements-tests
    (testing "empties"
      (are [x] (empty? x)
        (validate-set-elements [] [])
        (validate-set-elements {} {})
        (validate-set-elements '() '())
        (validate-set-elements #{} #{})
        (validate-set-elements #{1 2 3} #{})
        (validate-set-elements #{} #{int?})))

    (testing "all true"
      (are [x] (every? #(true? (:valid? %)) x)
        (validate-set-elements #{1 2 3} #{int?})
        (validate-set-elements #{1 2 3} #{int? number? pos?})
        (validate-set-elements [42 #{1 2 3}] [int? #{number? int?}])
        (validate-set-elements {:a 42 :b #{"abc" "xyz" "qrs"}} {:a int? :b #{string? #(= 3 (count %))}})))

    (testing "some false"
      (are [x] (some #(not (:valid? %)) x)
        (validate-set-elements #{1 2 3 :foo} #{int? number?})
        (validate-set-elements #{1 2 3} #{string? keyword? boolean?}))))


(deftest validate-and-validate-set-tests
  (testing "empty colls and specs"
    (are [x] (true? x)
      (valid-scalar-spec? [] [])
      (valid-scalar-spec? '() '())
      (valid-scalar-spec? {} {})
      (valid-scalar-spec? #{} #{})))
  (testing "simple colls"
    (are [x] (true? x)
      (valid-scalar-spec? simple-coll simple-spec)))
  (testing "nested colls"
    (are [x] (true? x)
      (valid-scalar-spec? nested-coll nested-spec)
      (valid-scalar-spec? heterogeneous-coll heterogeneous-spec)))
  (testing "scalar specs on lists"
    (are [x] (true? x)
      (valid-scalar-spec? data-list-1 spec-list-1)
      (valid-scalar-spec? data-list-2 spec-list-2)
      (valid-scalar-spec? '() '())))
  (testing "colls with elements that are sets"
    (are [x] (true? x)
      (valid-scalar-spec? data-with-set-1 spec-with-set-1)
      (valid-scalar-spec? data-with-set-2 spec-with-set-2)
      (valid-scalar-spec? data-with-set-3 spec-with-set-3)))
  (testing "colls that are sets themselves"
    (are [x] (true? x)
      (valid-scalar-spec? #{33} #{int?})
      (valid-scalar-spec? #{11 22 33} #{int?})))
  (testing "non-terminating sequence as the top-level container"
    (are [x] (true? x)
      (valid-scalar-spec? (range 0 0) [])
      (valid-scalar-spec? [11 22 33] (take 0 (repeat int?)))
      (valid-scalar-spec? (range 99) [int? int? int?])
      (valid-scalar-spec? [11 22 33] (take 99 (repeat int?)))
      (valid-scalar-spec? [:foo "abc" true 11 22 33 44 55 66 77 88 99]
                          (lazy-cat [keyword? string? boolean?] (take 99 (repeat int?))))
      (valid-scalar-spec? [11 "abc" :foo
                           22 "xyz" :bar
                           33 "wqv" :baz]
                          (take 99 (cycle [int? string? keyword?])))))
  (testing "nested non-terminating sequences"
    (are [x] (true? x)
      (valid-scalar-spec? (take 99 (cycle [123 "abc" :foo \c])) [int? string? keyword? char?])
      (valid-scalar-spec? [123 "abc" :foo (range 0 9)] [int? string? keyword? [int? int? int?]])
      (valid-scalar-spec? {:a (range 0 99)} {:a [int? int?]})))
  (testing "sets as predicates"
    (are [x] (true? x)
      (valid-scalar-spec? data-1-set-predicate spec-1-set-predicate)
      (not (valid-scalar-spec? data-2-set-predicate spec-2-set-predicate))))
  (testing "regexes as predicates"
    (are [x] (true? x)
      (valid-scalar-spec? [42 :abc "foo"] [int? keyword? #"^foo$"])
      (valid-scalar-spec? {:a "baz" :b "qux"} {:a #"[abc]?.[xyz]+" :b #"[^Q].x"}))))


(deftest valid-dual-scalar-spec?-tests
  (testing "empty and nil data and specs"
    (are [x] (= [] x)
      (dual-validate-scalar-spec [] [] nil)
      (dual-validate-scalar-spec [] nil [])
      (dual-validate-scalar-spec [] nil nil)
      (dual-validate-scalar-spec [] [] [])))
  (testing "non-nested data and specs"
    (are [x] (true? x)

      (valid-dual-scalar-spec? [\x] [\y] [#(and (char? %1)
                                                (char? %2))])
      (valid-dual-scalar-spec? [11 22 33]
                               [44 55 66]
                               [#(and (int? %1)
                                      (int? %2))
                                #(and (int? %1)
                                      (int? %2))
                                #(and (int? %1)
                                      (int? %2))])

      (valid-dual-scalar-spec? [11 22 33]
                               [:foo :bar :baz]
                               (repeat 3 #(and (int? %1)
                                               (keyword? %2))))

      (valid-dual-scalar-spec? [11 22 33]
                               [12 :not-validated :not-validated]
                               [#(= (+ 1 %1) %2)])

      (valid-dual-scalar-spec? {:a true :c 55}
                               {:a false :c -55}
                               {:a #(every? true? [%1 (not %2)])
                                :c #(= (- %1) %2)})))
  (testing "nested data and specs"
    (is (valid-dual-scalar-spec? [[[99]]]
                                 [[[99]]]
                                 [[[#(= %1 %2)]]]))))


(deftest filter-validatation-tests
  (are [x y] (= x y)
    [] (filter-validation (validate-scalar-spec [11 "abc" :foo] [int? string? keyword?]) falsey)
    [] (filter-validation (validate-scalar-spec [11 "abc" :foo] [string? keyword? int?]) truthy)))


(deftest only-valid-tests
  (are [x y] (= x y)
    [] (only-valid (validate-scalar-spec [11 "abc" :foo] [string? keyword? int?]))

    (only-valid (validate-scalar-spec [11] [int?]))
    [{:path [0], :datum 11, :predicate int?, :valid? true}]

    (only-valid (validate-scalar-spec [11 "abc" true :foo \z] [int? string? char? keyword? boolean?]))
    [{:path [0], :datum 11, :predicate int?, :valid? true}
     {:path [1], :datum "abc", :predicate string?, :valid? true}
     {:path [3], :datum :foo, :predicate keyword?, :valid? true}]))


(deftest only-invalid-tests
  (testing "expected results"
    (are [x y] (= x y)
      [] (only-invalid (validate-scalar-spec [11 "abc" :foo] [int? string? keyword?]))

      (only-invalid (validate-scalar-spec [11] [keyword?]))
      [{:path [0], :datum 11, :predicate keyword?, :valid? false}]

      (only-invalid (validate-scalar-spec [11 "abc" true :foo \z] [int? string? char? keyword? boolean?]))
      [{:path [2], :datum true, :predicate char?, :valid? false}
       {:path [4], :datum \z, :predicate boolean?, :valid? false}]))
  (testing "nil results"
    (are [x] (nil? (:valid? x))
      (only-invalid (validate-scalar-spec [99 "HelloXWorld!" :foo] [int? #(re-matches #"Hello World!" %) keyword?])))))



(deftest valid-scalar-spec?-tests
  (are [x] (true? x)
    (valid-scalar-spec? [] nil)
    (valid-scalar-spec? [] [])
    (valid-scalar-spec? [11] [int?])
    (valid-scalar-spec? [11 "abc" :kw] [int? string? keyword?])
    (not (valid-scalar-spec? [11 "abc" :kw] [string? keyword? int?]))
    (valid-scalar-spec? [99 "HelloXWorld!" :foo] [int? #(re-matches #"Hello.World!" %) keyword?])))


(deftest only-colls-tests
  (are [x y] (= x y)
    [[]] (only-colls [[]])
    [[11]] (only-colls [[11]])
    [[11] [22]] (only-colls [[11] [22]])
    [[11] [33]] (only-colls [[11] 22 [33] 44])
    '(()) (only-colls '(()))
    '((11)) (only-colls '((11)))
    '((11) (22)) (only-colls '((11) (22)))
    '((11) (33)) (only-colls '((11) 22 (33) 44))
    [[22] '(44)] (only-colls [11 [22] 33 '(44) 55])
    '((22) [44]) (only-colls '(11 (22) 33 [44] 55))))


(deftest parent-literal-path-tests
  (are [x y] (= x y)
    [4 0]
    (parent-literal-path [4 0 0])

    [:e 0]
    (parent-literal-path [:e 0 0])

    [3 :c 2]
    (parent-literal-path [3 :c 2 :d])

    []
    (parent-literal-path [0])))


(deftest find-ordinal-tests
  (are [x y] (= x y)
    0 (find-ordinal [[]])
    0 (find-ordinal '(()))
    1 (find-ordinal [11 22 [33] 44 [55]])
    1 (find-ordinal '(11 22 [33] 44 (55)))
    3 (find-ordinal [[11] [22] [33] 44 55 [66]])
    3 (find-ordinal [{} '() #{} []])
    4 (find-ordinal [[11 22] [33 44] [nil] [55] [[[6]]]])))


(deftest sub-tests
  (testing "expected output"
    (are [x y] (= x y)
      [11 22 33]
      (sub [11 22 33 44 55 66] 0 3)

      [44 55 66]
      (sub [11 22 33 44 55 66] 3)

      [11 22 33 44 55 66]
      (sub [11 22 33 44 55 66] 0)

      [11 22 33]
      (sub '(11 22 33 44 55 66) 0 3)

      [44 55 66]
      (sub '(11 22 33 44 55 66) 3)

      [11 22 33 44 55 66]
      (sub '(11 22 33 44 55 66) 0)))
  (testing "comparing to clojure.core built-in (subvec)"
    (are [x y] (= x y)
      (sub    [11 22 33 44 55 66] 0 3)
      (subvec [11 22 33 44 55 66] 0 3)

      (sub    [11 22 33 44 55 66] 3)
      (subvec [11 22 33 44 55 66] 3)

      (sub    [11 22 33 44 55 66] 0)
      (subvec [11 22 33 44 55 66] 0))))

(comment
  (deftest wrap-form-tests
    (are [x y] (= x y)
      {:root []}
      (wrap-form [])

      {:root [11 22 33]}
      (wrap-form [11 22 33])

      {:root {:a 11 :b 22}}
      (wrap-form {:a 11 :b 22})))


  (deftest wrap-path-tests
    (are [x y] (= x y)
      [:root] (wrap-path [])
      [:root 11]  (wrap-path [11])
      [:root 11 22 33] (wrap-path [11 22 33])))
  )


(deftest only-colls-test
  (are [x y] (= x y)
    [] (only-colls [])
    [] (only-colls [11 22 33])
    [[11]] (only-colls [[11]])
    [[11] [33] [55]] (only-colls [[11] 22 [33] 44 [55]])
    [[11] [22] [33]] (only-colls [[11] [22] [33]])
    '() (only-colls '())
    '((11)) (only-colls '((11)))
    '() (only-colls '(11 22 33))
    '((11) (33) (55)) (only-colls '((11) 22 (33) 44 (55)))
    '((11) (22) (33) (44) (55)) (only-colls '((11) (22) (33) (44) (55)))))


(deftest ordinal-get-tests
  (testing "vectors"
    (are [x y] (= x y)
      nil
      (ordinal-get [] 0)

      [11]
      (ordinal-get [[11]] 0)

      nil
      (ordinal-get [11 22 33 44 55] 2)

      [55]
      (ordinal-get [[11] 22 [33] 44 [55]] 2)))

  (testing "lists"
    (are [x y] (= x y)
      nil
      (ordinal-get '() 0)

      '(11)
      (ordinal-get '((11)) 0)

      nil
      (ordinal-get '(11 22 33 44 55) 2)

      '(55)
      (ordinal-get '((11) 22 (33) 44 (55)) 2)))

  (testing "maps"
    (are [x y] (= x y)
      11
      (ordinal-get {:a 11} :a)

      11
      (ordinal-get {:a 11 :b 22} :a)))

  (testing "sets"
    (are [x y] (= x y)
      11
      (ordinal-get #{11} 11)

      22
      (ordinal-get #{11 22 33} 22)

      [22]
      (ordinal-get #{11 [22] 33} [22]))))


(deftest ordinal-get-in-tests
  (testing "vector example"
    (are [x y] (= x y)
      [22] (ordinal-get-in ord-vec-example [0 0])
      [44] (ordinal-get-in ord-vec-example [0 1])
      [55] (ordinal-get-in ord-vec-example [1 0])
      [77] (ordinal-get-in ord-vec-example [1 1])
      [99] (ordinal-get-in ord-vec-example [2 0 0])))
  (testing "list example"
    (are [x y] (= x y)
      '(11) (ordinal-get-in ord-list-example [0])
      '(44) (ordinal-get-in ord-list-example [1 0])
      '(55) (ordinal-get-in ord-list-example [1 1])
      '(66) (ordinal-get-in ord-list-example [1 2])
      '(77) (ordinal-get-in ord-list-example [2])))
  (testing "map example"
    (are [x y] (= x y)
      11 (ordinal-get-in ord-map-example [:a])
      22 (ordinal-get-in ord-map-example [:b :c])
      33 (ordinal-get-in ord-map-example [:b :d :e])))
  (testing "set example"
    (are [x y] (= x y)
      11 (ordinal-get-in ord-set-example [11])
      [22 33 [44]] (ordinal-get-in ord-set-example [[22 33 [44]]])
      [44] (ordinal-get-in ord-set-example [[22 33 [44]] 0])))
  (testing "heterogeneous example"
    (are [x y] (= x y)
      '(11) (ordinal-get-in ord-heterogeneous-example [0])
      [66] (ordinal-get-in ord-heterogeneous-example [1 1])
      88 (ordinal-get-in ord-heterogeneous-example [2 :a])
      [333] (ordinal-get-in ord-heterogeneous-example [2 :b 1]))))


(deftest head-of-path-tests
  (testing "homogeneous vector"
    (are [x y] (= x y)
      [] (head-of-path [4 3 1 0] 0)
      [4] (head-of-path [4 3 1 0] 1)
      [4 3] (head-of-path [4 3 1 0] 2)
      [4 3 1] (head-of-path [4 3 1 0] 3)
      [4 3 1 0] (head-of-path [4 3 1 0] 4)))
  (testing "heterogeneous vector"
    (are [x y] (= x y)
      [] (head-of-path [:a 11 "abc"] 0)
      [:a] (head-of-path [:a 11 "abc"] 1)
      [:a 11] (head-of-path [:a 11 "abc"] 2)
      [:a 11 "abc"] (head-of-path [:a 11 "abc"] 3)))
  (testing "composite valued indexes/keys"
    (are [x y] (= x y)
      [] (head-of-path [11 :a [22 33] #{44}] 0)
      [11] (head-of-path [11 :a [22 33] #{44}] 1)
      [11 :a] (head-of-path [11 :a [22 33] #{44}] 2)
      [11 :a [22 33]] (head-of-path [11 :a [22 33] #{44}] 3)
      [11 :a [22 33] #{44}] (head-of-path [11 :a [22 33] #{44}] 4))))


(deftest container-at-this-depth-tests
  (testing "form+path #1"
    (are [x y] (= x y)
      [11 [22 [33]]]
      (container-at-this-depth [11 [22 [33]]] [1 1 0] 0)

      [22 [33]]
      (container-at-this-depth [11 [22 [33]]] [1 1 0] 1)

      [33]
      (container-at-this-depth [11 [22 [33]]] [1 1 0] 2)

      33
      (container-at-this-depth [11 [22 [33]]] [1 1 0] 3)))
  (testing "form+path #2"
    (are [x y] (= x y)
      [11 {:a 11 :b [22 {:c 33}]}]
      (container-at-this-depth [11 {:a 11 :b [22 {:c 33}]}] [1 :b 1 :c] 0)

      {:a 11 :b [22 {:c 33}]}
      (container-at-this-depth [11 {:a 11 :b [22 {:c 33}]}] [1 :b 1 :c] 1)

      [22 {:c 33}]
      (container-at-this-depth [11 {:a 11 :b [22 {:c 33}]}] [1 :b 1 :c] 2)

      {:c 33}
      (container-at-this-depth [11 {:a 11 :b [22 {:c 33}]}] [1 :b 1 :c] 3)

      33
      (container-at-this-depth [11 {:a 11 :b [22 {:c 33}]}] [1 :b 1 :c] 4))))


(deftest chop-container-at-target-tests
  (are [x y] (= x y)
    [11]
    (chop-container-at-target [11 [22 33] [44] [[55 66]]] 0)

    [11 [22 33]]
    (chop-container-at-target [11 [22 33] [44] [[55 66]]] 1)

    [11 [22 33] [44]]
    (chop-container-at-target [11 [22 33] [44] [[55 66]]] 2)

    [11 [22 33] [44] [[55 66]]]
    (chop-container-at-target [11 [22 33] [44] [[55 66]]] 3)))


(deftest target-index-tests
  (are [x y] (= x y)
    1 (target-index [1 :b 3 :c] 0)
    3 (target-index [1 :b 3 :c] 2)
    :c (target-index [1 :b 3 :c] 3)))


(deftest ordinal-path-of-parent-tests
  (testing "homogeneous list test"
    (is (= [2 2] (ordinal-path-of-parent homogeneous-list-test homogeneous-list-path))))
  (testing "vector/list mix"
    (is (= [2 2] (ordinal-path-of-parent vector-list-mix vector-list-mix-path))))
  (testing "homogeneous map"
    (is (= [:c :e] (ordinal-path-of-parent homogeneous-map homogeneous-map-path))))
  (testing "heterogeneous vec/list/map"
    (is (= [2 2 0] (ordinal-path-of-parent heterogeneous-coll-1 heterogeneous-coll-1-path))))
  (testing "heterogeneous vec/list/map with intervening non-vec, non-list path elements"
    (is (= [2 :c 2] (ordinal-path-of-parent heterogeneous-coll-2 heterogeneous-coll-2-path))))
  (testing "target element not last"
    (is (= [1 :b 1 3 0] (ordinal-path-of-parent heterogeneous-coll-3 heterogeneous-coll-3-path))))
  (testing "maps with integer indexes"
    (is (= [33 55] (ordinal-path-of-parent map-with-integer-indexes map-with-integer-indexes-path))))
  (testing "maps with composite indexes (I'm getting cross-eyed)"
    (is (= [[55 66]] (ordinal-path-of-parent map-with-composite-indexes map-with-composite-indexes-path))))
  (testing "intervening sets, but none along path to the target element"
    (is (= [2 1 2] (ordinal-path-of-parent set-not-along-path set-not-along-path-path))))
  (testing "intervening sets along path to the target element"
    (is (= [1 2 [[99]] 0] (ordinal-path-of-parent set-along-the-way set-along-the-way-path))))
  (testing "(oridinal-path-to-parent) combined with (ordinal-get-in)"
    (is (= (get-in* combo-data-1 (butlast combo-path-1))
           (ordinal-get-in combo-data-1 (ordinal-path-of-parent combo-data-1 combo-path-1)))
        (= (get-in* combo-data-2 (butlast combo-path-2))
           (ordinal-get-in combo-data-2 (ordinal-path-of-parent combo-data-2 combo-path-2))))))


(deftest apply-one-coll-spec-tests
  (testing "singular collection specs"
    (are [x] (true? x)
      (:valid? (apply-one-coll-spec test-data-1 test-data-1-coll-spec [1]))
      (:valid? (apply-one-coll-spec test-data-1 test-data-1-coll-spec [0]))
      (:valid? (apply-one-coll-spec test-data-1 test-data-1-coll-spec [2 0]))

      (:valid? (apply-one-coll-spec test-data-2 test-data-2-coll-spec [0]))
      (:valid? (apply-one-coll-spec test-data-2 test-data-2-coll-spec [1]))
      (:valid? (apply-one-coll-spec test-data-2 test-data-2-coll-spec [6]))
      (:valid? (apply-one-coll-spec test-data-2 test-data-2-coll-spec [5 0]))
      (:valid? (apply-one-coll-spec test-data-2 test-data-2-coll-spec [5 1]))
      (:valid? (apply-one-coll-spec test-data-2 test-data-2-coll-spec [5 2]))

      (:valid? (apply-one-coll-spec test-data-3 test-data-3-coll-spec [1 :b 0]))
      (:valid? (apply-one-coll-spec test-data-3 test-data-3-coll-spec [1 :b 1]))
      (:valid? (apply-one-coll-spec test-data-3 test-data-3-coll-spec [2]))
      (:valid? (apply-one-coll-spec test-data-3 test-data-3-coll-spec [3 0]))
      (:valid? (apply-one-coll-spec test-data-3 test-data-3-coll-spec [3 1 0]))

      (:valid? (apply-one-coll-spec test-data-4 test-data-4-coll-spec [0]))
      (:valid? (apply-one-coll-spec test-data-4 test-data-4-coll-spec [1]))
      (:valid? (apply-one-coll-spec test-data-4 test-data-4-coll-spec [2]))

      (:valid? (apply-one-coll-spec test-data-5 test-data-5-coll-spec [:a 0]))
      (:valid? (apply-one-coll-spec test-data-5 test-data-5-coll-spec [:a 1]))
      (:valid? (apply-one-coll-spec test-data-5 test-data-5-coll-spec [:a 2]))
      (:valid? (apply-one-coll-spec test-data-5 test-data-5-coll-spec [:a 4 0]))
      (:valid? (apply-one-coll-spec test-data-5 test-data-5-coll-spec [:c 0]))
      (:valid? (apply-one-coll-spec test-data-5 test-data-5-coll-spec [:both-vecs?])))))


(deftest valid-collection-spec?-tests
  (testing "entire collection spec"
    (are [x] (true? x)
      (valid-collection-spec? test-data-6 nil)
      (valid-collection-spec? test-data-6 {})
      (valid-collection-spec? test-data-6 test-data-6-coll-spec)
      (valid-collection-spec? test-data-7 test-data-7-coll-spec)
      (valid-collection-spec? test-data-8 test-data-8-coll-spec)
      (valid-collection-spec? test-data-9 test-data-9-coll-spec)
      (valid-collection-spec? test-data-10 test-data-10-coll-spec)
      (valid-collection-spec? test-data-11 test-data-11-coll-spec)
      (valid-collection-spec? test-non-terminating-data-1 test-non-terminating-spec-1)
      (valid-collection-spec? test-non-terminating-data-2 test-non-terminating-spec-2)
      (valid-collection-spec? test-non-terminating-data-3 test-non-terminating-spec-3))))


(deftest valid?-combo-tests
  (are [x] (true? x)
    (valid? data-1 [] [])
    (valid? data-1 [] nil)
    (valid? data-1 nil [])
    (valid? data-1 nil nil)
    (valid? data-1 scalar-spec-1 coll-spec-1)
    (valid? data-2 scalar-spec-2 coll-spec-2)))


(deftest validate-dual-collection-spec-tests
  (testing "validate on empty"
    (are [x] (= [] x)
      (dual-validate-collection-spec [] [] [])
      (dual-validate-collection-spec [] nil [])
      (dual-validate-collection-spec [] [] nil)
      (dual-validate-collection-spec [] nil nil)))
  (testing "validate and valid?"
    (are [x] (true? x)
      (valid-dual-collection-spec? [11 22 33]
                                   [44 55 66]
                                   [#(= (count %1)
                                        (count %2))])

      (valid-dual-collection-spec? [11 [22] 33 [44] 55]
                                   [[66] 77 [88] 99 [111]]
                                   [#(< (count (filter vector? %1))
                                        (count (filter vector? %2)))
                                    #(> (count (filter int? %1))
                                        (count (filter int? %2)))])

      (valid-dual-collection-spec? [11 [22 [33 [44 55 66]]]]
                                   [11 [22 [33 [44 55 66]]]]
                                   [[[[#(= (count %1)
                                           (count %2))]]]])

      (valid-dual-collection-spec? {:a [11] :b [22 33 44]}
                                   {:a [55] :b [66 77 88]}
                                   {:a [#(= (count %1)
                                            (count %2))]
                                    :b [#(and (= 3 (count %1))
                                              (= 3 (count %2)))]
                                    :both-vecs? #(and (vector? (:b %1))
                                                      (vector? (:b %2)))}))))


(defmacro silly-macro [f & args] `(~f ~@args))
(def silly-macro-spec (list symbol? number? number?))

(defmacro and-2
  "Renamed version of clojure.core/and so I don't stomp over the real one."
  ([] true)
  ([x] x)
  ([x & next]
   `(let [and# ~x]
      (if and# (and-2 ~@next) and#))))

(def and-2-spec (list symbol? [symbol? boolean?] (list symbol? symbol? (list symbol? number?) symbol?)))


(comment
  ;; sanity checks
  (silly-macro + 1 2)
  (silly-macro + 1 10 100 1000)
  (macroexpand-1 `(silly-macro + 1 2 3 4))
  (validate-macro-with `(silly-macro + 1 2) silly-macro-spec)
  (valid-macro-spec? `(silly-macro + 1 2) silly-macro-spec)

  (and-2 true 99)
  (macroexpand-1 `(and-2 true 99))
  (validate-macro-with `(and-2 true 99) and-2-spec)
  (valid-macro-spec? `(and-2 true 99) and-2-spec)
  )


(deftest validate-and-valid-macro-spec-tests
  (are [x] (true? x)
    (valid-macro-spec? `(silly-macro + 1 2) silly-macro-spec)
    (valid-macro-spec? `(and-2 true 99) and-2-spec)))


(def test-data-12 [11 22 33 44 [55 66 [77 88] 99] 111 222])
(def test-spec-12 [{:paths [[1] [2]]
                    :predicate #(> %2 %1)}
                   {:paths [[3] [1] [6]]
                    :predicate #(vector %3 %1 %2)}
                   {:paths [[4 2 1] [1]]
                    :predicate #(= %1 (* 4 %2))}])

(def test-data-13 [1 2 3 [4 5 6] 7 8 [[9]]])
(def test-spec-13 [{:paths [[2] [3]]
                    :predicate #(= %1 (count %2))}
                   {:paths [[2]]
                    :predicate int?}
                   {:paths [[3] [6] [5] [6 0]]
                    :predicate #(and (vector? %1)
                                     (vector? %2)
                                     (= (inc %3)
                                        (get-in* %4 [0])))}])

(def test-data-14 {:a 22 :b [33 44 {:c 55 :d 66}] :e (list 77 88 :a 22)})
(def test-spec-14 [{:paths [[:a] [:b 2 :d]]
                    :predicate #(= (* 3 %1) %2)}
                   {:paths [[:e 2] [:e 3] []]
                    :predicate #(= %2 (get %3 %1))}])

(def test-data-15 (take 9 (repeat (vector 9 "abc" :foo))))
(def test-spec-15 [{:paths [[] [5 0]]
          :predicate #(= (count %1) %2)}])

(def test-data-16 [11 [5] 22 [2] 33])
(def test-spec-16 [{:paths [[] [1 0]]
                    :predicate #(= (count %1) %2)}
                   {:paths [[] [3 0]]
                    :predicate #(= (count (filter vector? %1)) %2)}])
(def invalid-test-spec-16 (-> test-spec-16
                              (assoc-in* [0 :paths 1 0] 3)
                              (assoc-in* [1 :paths 1 0] 1)))

(def test-data-17 (take 5 (repeat 5)))
(def test-spec-17 [{:paths [[4] [3] [2] [1] [0] []]
                    :predicate #(= %1 %2 %3 %4 %5 (count %6))}])


(deftest validate-with-path-spec-tests
  (are [x] (empty? x)
    (validate-with-path-spec [] [])
    (validate-with-path-spec [11 22 33] []))
  (are [x] (every? #(:valid? %) x)
    (validate-with-path-spec test-data-12 test-spec-12)
    (validate-with-path-spec test-data-13 test-spec-13)
    (validate-with-path-spec test-data-14 test-spec-14)
    (validate-with-path-spec test-data-15 test-spec-15)
    (validate-with-path-spec test-data-15 test-spec-15)
    (validate-with-path-spec test-data-16 test-spec-16)
    (validate-with-path-spec test-data-17 test-spec-17))
  (are [x] (every? #(not (:valid? %)) x)
    (validate-with-path-spec test-data-16 invalid-test-spec-16)))


(run-tests)