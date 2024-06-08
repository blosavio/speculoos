(ns speculoos.collection-functions-test
  (:require [speculoos.collection-functions :refer :all]
            [clojure.test :refer :all]
            [clojure.string :as string]))

;; can run these tests from the CLI with
;; :: ..../speculoos/test/speculoos$ lein test
;; or
;; ..../speculoos/test/speculoos$ lein test :only speculoos.collection-functions-test

;; Another method: run M-x cider-load-file <filepath to ns> (C-c C-l), then cider-load-buffer (C-c C-k) to eval all forms in this ns

;; Note: I've tried to put the function eval forms at the tail of a line so that
;; a person may easily C-c C-e (cider-eval-last-sexp) and see the result.


(deftest reduce-2-test
  (testing "reduce-2 output"
    (are [x y] (= x y)
      0   (reduce-2 + [])
      []  (reduce-2 + [] [])
      [4] (reduce-2 + [4] [])
      15 (reduce-2 + [1 2 3 4 5])
      25 (reduce-2 + 10 [1 2 3 4 5])
      3 (reduce-2 + [3])
      [[:a :b] [:c :d] [:e :f]] (reduce-2 #(conj %1 %2) [[:a :b]] [[:c :d] [:e :f]])
      {:a 99, :b 2, :c 3} (reduce-2 #(assoc %1 (first %2) (second %2)) {:a 5} {:b 2 :c 3 :a 99})
      '(:baz :bar :foo) (reduce-2 #(conj %1 %2) '() '(:foo :bar :baz))
      #{:a :b :c} (reduce-2 #(conj %1 %2) #{} #{:a :b :c})
      '(44) (reduce-2 + (sequence [44]) (sequence []))
      44 (reduce-2 + (sequence [44]))
      165 (reduce-2 + 11 (sequence [22 33 44 55]))
      6 (reduce-2 + 0 #{1 2 3})
      6 (reduce-2 + (range 0 4)))
    (is (thrown? ClassCastException ; (+) is undefined for a nil argument
                 (reduce-2 + [] [4]))))

  (testing "comparing reduce-2 to built-in reduce"
    (testing "no init val"
      (are [x y] (= x
                    y)
        (reduce   + [])
        (reduce-2 + [])

        (reduce   + [3])
        (reduce-2 + [3])

        (reduce   + [1 2 3 4 5])
        (reduce-2 + [1 2 3 4 5])

        (reduce   + 3 [])
        (reduce-2 + 3 [])
        ))

    (testing "init val supplied"
      (are [x y] (= x
                    y)
        (reduce   + 5 [6])
        (reduce-2 + 5 [6])

        (reduce   + 10 [1 2 3 4 5])
        (reduce-2 + 10 [1 2 3 4 5])

        (reduce   #(conj %1 %2) [] [[:a :b] [:c :d]])
        (reduce-2 #(conj %1 %2) [] [[:a :b] [:c :d]])

        (reduce   #(assoc %1 (first %2) (second %2)) {:a 5} {:a 99 :b 2 :c 3})
        (reduce-2 #(assoc %1 (first %2) (second %2)) {:a 5} {:a 99 :b 2 :c 3})
        ))))


(deftest map-2-test
  (testing "map-2 output"
    (are [x y] (= x
                  y)
      (map-2 inc [1 2 3 4 5])
      '(2 3 4 5 6)

      (map-2 inc [])
      '()

      (map-2 #(vector (first %) (* 2 (second %))) {:a 1 :b 2 :c 3})
      '([:a 2] [:b 4] [:c 6])

      (map-2 (fn [[key value]] [key (* 2 value)]) {:a 1 :b 2 :c 3})
      '([:a 2] [:b 4] [:c 6])

      (map-2 :a [{:a 11 :b 0}
                 {:a 22 :b 0}
                 {:a 33 :b 1}
                 {:a 44 :b 0}])
      '(11 22 33 44)

      '() (map-2 inc (sequence []))
      '(2 3 4 5 6) (map-2 inc (sequence [1 2 3 4 5]))

      '(1 2 3 4) (map-2 inc (range 0 4))
      '(2 3 4) (sort (map-2 inc #{1 2 3}))
      '(6 4 5) (map #(% 5) (take 3 (cycle [inc dec identity])))))


  (testing "comparing map-2 output to built-in map"
    (are [x y] (= x
                  y)
      (map   inc [])
      (map-2 inc [])

      (map   inc [1 2 3 4 5])
      (map-2 inc [1 2 3 4 5])

      (map   #(vector (first %) (* 2 (second %))) {:a 1 :b 2 :c 3})
      (map-2 #(vector (first %) (* 2 (second %))) {:a 1 :b 2 :c 3})

      (map   (fn [[key value]] [key (* 2 value)]) {:a 1 :b 2 :c 3})
      (map-2 (fn [[key value]] [key (* 2 value)]) {:a 1 :b 2 :c 3})

      (map   :a [{:a 11 :b 0} {:a 22 :b 0} {:a 33 :b 1} {:a 44 :b 0}])
      (map-2 :a [{:a 11 :b 0} {:a 22 :b 0} {:a 33 :b 1} {:a 44 :b 0}])
      )))


(deftest map-indexed-2-test
  (testing "map-indexed-2 output"
    (are [x y] (= x
                  y)
      (map-indexed-2 #(vector %1 %2) [:a :b :c :d :e])
      '([0 :a] [1 :b] [2 :c] [3 :d] [4 :e])

      (map-indexed-2 vector "foobar")
      '([0 \f] [1 \o] [2 \o] [3 \b] [4 \a] [5 \r])

      (map-indexed-2 hash-map "foobar")
      '({0 \f} {1 \o} {2 \o} {3 \b} {4 \a} {5 \r})

      (map-indexed-2 list [:a :b :c])
      '((0 :a) (1 :b) (2 :c))

      (map-indexed-2 #(conj (sequence [%1 %2])) "foobar")
      '((0 \f) (1 \o) (2 \o) (3 \b) (4 \a) (5 \r))

      (map-indexed-2 #(vector %1 %2) (range 3))
      '([0 0] [1 1] [2 2])

      (map-indexed-2 #(vector %1 %2) #{:green :blue :purple})
      '([0 :green] [1 :blue] [2 :purple])
      ))

  (testing "comparing map-indexed-2 to built-in map-indexed"
    (are [x y] (= x
                  y)
      (map-indexed #(vector %1 %2) [:foo :bar :baz])
      (map-indexed-2 #(vector %1 %2) [:foo :bar :baz])

      (map-indexed   #(vector %1 %2) '(:foo :bar :baz))
      (map-indexed-2 #(vector %1 %2) '(:foo :bar :baz))

      (map-indexed   #(list %1 %2) '(:foo :bar :baz))
      (map-indexed-2 #(list %1 %2) '(:foo :bar :baz))

      (map-indexed   #(vector %1 %2) [:foo :bar :baz])
      (map-indexed-2 #(vector %1 %2) [:foo :bar :baz])

      (map-indexed   #(vector %1 %2) {:a "foo" :b "bar" :c "baz"})
      (map-indexed-2 #(vector %1 %2) {:a "foo" :b "bar" :c "baz"})

      (map-indexed   vector "foobar")
      (map-indexed-2 vector "foobar")

      (map-indexed   hash-map "foobar")
      (map-indexed-2 hash-map "foobar")

      (map-indexed   list [:a :b :c])
      (map-indexed-2 list [:a :b :c])

      (map-indexed   #(vector %1 %2) [:a :b :c :d :e])
      (map-indexed-2 #(vector %1 %2) [:a :b :c :d :e])

      (map-indexed   vector [])
      (map-indexed-2 vector [])

      (map-indexed #(sequence [%1 %2]) (sequence [:foo :bar :baz]))
      (map-indexed-2 #(sequence [%1 %2]) (sequence [:foo :bar :baz]))
      )))


(deftest reduce-indexed-test
  (testing "reduce-indexed output on all four collection types, plus seqs"
    (are [x y] (= x y)
      (reduce-indexed #(conj %2 (vector %1 %3))
                      [[99 :a]]
                      [:b :c :d])
      [[99 :a] [0 :b] [1 :c] [2 :d]]

      (reduce-indexed #(conj %2 (list %1 %3))
                      '((99 :a))
                      '(:b :c :d))
      '((2 :d) (1 :c) (0 :b) (99 :a))

      (reduce-indexed #(conj %2 (vector %1 %3))
                      #{:initial-contents}
                      #{:one :two :three})
      #{[1 :three] :initial-contents [0 :one] [2 :two]}

      (reduce-indexed #(conj %2 (hash-map %1 %3))
                      {:a 99}
                      {:b 2 :c 3 :d 4})
      {:a 99, 0 [:b 2], 1 [:c 3], 2 [:d 4]}

      (reduce-indexed #(conj %2 (sequence [%1 %3]))
                (sequence [99 :a])
                (sequence [:b :c :d]))
      '((2 :d) (1 :c) (0 :b) 99 :a)

      (reduce-indexed #(* %3 (+ %1 %2)) 0 (range 4))
      27

      (reduce-indexed #(conj %2 [%1 %3]) [] (take 5 (cycle [:a :b :c])))
      [[0 :a] [1 :b] [2 :c] [3 :a] [4 :b]]
      ))

  (testing "init val not supplied, f not applied"
    (are [x y] (= x y)
      0 (reduce-indexed + [])
      1 (reduce-indexed + [1])

      0 (reduce-indexed + '())
      1 (reduce-indexed + '(1))

      0 (reduce-indexed + {})
      [:a 1] (reduce-indexed + {:a 1})

      0 (reduce-indexed + #{})
      1 (reduce-indexed + #{1})

      0 (reduce-indexed + (sequence []))
      1 (reduce-indexed + (sequence [1]))
      ))

  (testing "init value supplied"
    (are [x y] (= x y)
      [] (reduce-indexed + [] [])
      {} (reduce-indexed + {} {})
      '() (reduce-indexed + '() '())
      #{} (reduce-indexed + #{} #{})
      '() (reduce-indexed + (sequence []) (sequence []))


      [5] (reduce-indexed + [5] [])
      {:a 1} (reduce-indexed + {:a 1} {})
      '(3) (reduce-indexed + '(3) '())
      #{4} (reduce-indexed + #{4} #{})
      '(99) (reduce-indexed + (sequence [99]) (sequence []))

      :no-items-in-coll (reduce-indexed + :no-items-in-coll [])
      ))

  (testing "exceptions"
    (is (thrown? ClassCastException ; (+) is undefined for a nil argument
                 (reduce-indexed + [] [5]))))

  (testing "map assembly tests"
    (are [x y] (= x y)
      {:init-val 99 :a 0 :b 1 :c 2}
      (reduce-indexed #(assoc %2 %3 %1) {:init-val 99} [:a :b :c])

      {0 {:idx 0 :val 11} 1 {:idx 1 :val 22} 2 {:idx 2 :val 33}}
      (reduce-indexed #(assoc %2 %1 {:idx %1 :val (second %3)}) {} {:a 11 :b 22 :c 33})
      ))

  (testing "vector conj tests"
    (are [x y] (= x y)
      (reduce-indexed #(conj %2 (vector %1 %3))
                      [:initial-contents]
                      [:item1 :item2 :item3])
      [:initial-contents [0 :item1] [1 :item2] [2 :item3]]))

  (testing "list creation"
    (are [x y] (= x y)
      '({:idx 2, :val 33} {:idx 1, :val 22} {:idx 0, :val 11} :init-element)
      (reduce-indexed #(conj %2 {:idx %1 :val %3}) '(:init-element) '(11 22 33))))

  (testing "set handling"
    (are [x y] (= x y)
      (reduce-indexed #(conj %2 %1 %3) #{:init-val} #{11 22 33})
      #{:init-val 0 11 1 22 2 33})))


(comment
;; zoom-in on what's going on internally
(defn print-plus
  [& args]
  (println "arg1:" (nth args 0)
           "arg2:" (nth args 1)
           "arg3:" (nth args 2))
  (apply + args))

(print-plus 11 22 33)

(reduce-indexed print-plus 100 [10 20 20 30 40 50])
)


(run-tests)
