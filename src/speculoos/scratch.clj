(ns speculoos.scratch
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [speculoos.collection-functions :refer :all]
            [speculoos.fn-in :refer :all]
            [speculoos.core :refer :all]
            [speculoos.function-specs :refer :all]))


(load-file "src/speculoos/collection_hierarchy.clj")


(defn seq-regex
  "Returns true if sequence s fully satisfies pred-regex, pairs of
   predicates and regex-like operators. Predicates partition the sequence, regex
   operators check the quantity, according to the following.
   ? zero-or-one
   . exactly-one
   + one-or-more
   * zero-or-more
   [m n] between m and n (itegers), inclusive
   i exactly i (integer)

   example:
   (seq-regex [1 2 3 :a :b 'foo] int? '* keyword? '+ symbol? '.) ;; => true
   tests
   'zero-or-more integers, followed by one-or-more keywords,
   followed by exactly-one symbol'

   You must supply predicate and regexes to exhaust the sequence, otherwise
   returns false. If you don't care about entities beyond a particular index,
   use any? '*

   example:
   (seq-regex [1 2 :a :b true false] int? 2 keyword? [1 3] any? '*) ;; => true
   tests
   'exactly two integers, followed by one to three keywords, followed by
   any number of anything.'

   failing example:
   (seq-regex [1 2 :a :b] int? 2) ;; => false 
   'exactly two integers' fails because trailing keywords are not matched.

   Any unused pred-regex pairs are ignored.
   example:
   (seq-regex [1 2 3] int? 3 keyword? 3) ;; => true

   If your first regex is zero-or-more '*, then it will match an empty sequence,
   regardless of any trailing pred-regex pairs.
   example:
   (seq-regex [] int? '* keyword '+) ;; => true

   Do not stack the one-or-more regex in an attempt to get an integer >1.
   example:
   (seq-regex [1 2 3] int? '. int? '. int? '.) ;; => false

   Instead, use the exactly-integer or the range ops.
   example:
   (seq-regex [1 2 3] int? 3) ;; => true

   Predicates must be specific enough so that they aren't consumed further than
   you intend. This is trecherous when, e.g., converting to string.
   possibly suprising failing example:
   (seq-regex [:a :b :c 'fo 'br 'bz] #(= 2 (count (str %))) 3 symbol? 3)
   ;; => false

   'fo, 'br, and 'bz all satisfy length=2 when converted to string, leaving
   symbol? no values to test. Instead, insert a guarding predicate.
   example:
   (seq-regex [:a :b :c 'fo 'br 'bz] #(and (keyword? %) (= 2 (count (str %)))) 3 symbol? 3)
   ;; => true"
  {:UUIDv4 #uuid "9a47dc36-4c8f-4adc-acc1-ca5e1c197384"
   :no-doc true}
  [s & pred-regexes]
  (if (odd? (count pred-regexes))
    (throw (AssertionError. "seq-regex requires pairs of predicate/regex-op. Odd number supplied."))
    (let [pred (or (first pred-regexes) (constantly false))
          regex-op (fnext pred-regexes)
          front (take-while pred s)
          rear (drop-while pred s)
          remainder-pred-regexes (nnext pred-regexes)
          qty (cond
                (int? regex-op) [regex-op regex-op]
                (vector? regex-op) regex-op
                (symbol? regex-op) ({'. [1 1]
                                     '? [0 1]
                                     '+ [1 ##Inf]
                                     '* [0 ##Inf]} regex-op)
                (nil? regex-op) [##Inf ##Inf])
          expected-count? ((fn [[mn mx]] #(<= mn (count %) mx)) qty)]
      (and (expected-count? front)
           (or (empty? rear)
               (apply seq-regex rear remainder-pred-regexes))))))


(deftest seq-regex-tests
  {:no-doc true}
  (testing "non-even regex args"
    (is (thrown? AssertionError (seq-regex [1 2 3] int?))))

  (testing "empty seq"
   (are [x y] (= x y)
     false (seq-regex [])
     true (seq-regex [] nil? '*)))

  (testing "empty regex args, and un-specified trailing values"
   (are [x] (false? x)
     (seq-regex [1 2 3])
     (seq-regex [1 2 3 :a :b :c] int? 3)
     (seq-regex [1 2 3 :a :b :c 'foo] int? '* keyword? 3)))

  (testing "exactly-one regex '."
   (are [x y] (= x y)
     false (seq-regex [1 2] int? '.)
     true (seq-regex [1] int? '.)
     true (seq-regex [:a] keyword? '.)))

  (testing "zero-or-one regex '?"
   (are [x y] (= x y)
     false (seq-regex [1 2 3] int? '?)
     true (seq-regex [1] int? '?)
     true (seq-regex [] int? '?)))

  (testing "one-or-more regex '+"
   (are [x y] (= x y)
     false (seq-regex [] int? '+)
     true (seq-regex [1] int? '+)
     true (seq-regex [1 2] int? '+)))

  (testing "zero-or-more regex '*"
   (are [x y] (= x y)
     true (seq-regex [] int? '*)
     true (seq-regex [1] int? '*)
     true (seq-regex [1 2 3] int? '*)
     false (seq-regex [1 2 3] keyword? '*)))

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
     (seq-regex [] int? '* string? '. symbol? '+ boolean? '?)
     (seq-regex [1 2 3 :a :b :c 'foo 'bar 'baz] int? 3 keyword? [0 5] symbol? '+)
     (seq-regex [1 2 3 :a 'foo] int? '* keyword? '. symbol? '+)
     (seq-regex [1 2 3 :a :b :c 4 5 6] int? 3 keyword? [0 5] int? '*)
     (seq-regex [1 2 3 :a :b :c 'foo 'bar 'baz]
                #(and (int? %) (pos? %)) 3
                #(and (simple-keyword? %) (= 2 (count (str %)))) 3
                #(= 3 (count (str %))) 3)
     (seq-regex [1 2 3 :a :b :c 4 5 6] (complement keyword?) 3 keyword? '+ int? '*)))
  )

(run-tests)
