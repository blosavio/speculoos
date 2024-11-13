(ns changelog-specifications-test
  "Tests for the changelog predicates."
  {:no-doc true}
  (:require
   [changelog-specifications :refer :all]
   [clojure.test :refer [are
                         deftest
                         is
                         run-tests
                         testing]]
   [speculoos.core :refer [valid-collections?
                           valid-scalars?]]))


(deftest year-predicate-tests
  (are [x] (true? x)
    (year-predicate 2000)
    (year-predicate 3000))
  (are [x] (false? x)
    (year-predicate -1)
    (year-predicate "abc")))


(deftest day-predicate-tests
  (are [x] (true? x)
    (day-predicate 1)
    (day-predicate 31))
  (are [x] (false? x)
    (day-predicate 0)
    (day-predicate 32)))


(deftest ticket-predicate-tests
  (are [x] (true? x)
    (ticket-predicate "")
    (ticket-predicate "abc")
    (ticket-predicate #uuid "dd60a7cf-146b-4ee0-bc32-311442b5a278"))
  (are [x] (false? x)
    (ticket-predicate 'foo)
    (ticket-predicate :foo)))


(defn url? [s] (and (string? s) (boolean (re-find (get reference-spec :url) s))))

(deftest ref-url-tests
  (are [x] (true? x)
    (url? "https://foo")
    (url? "https://example.com"))
  (are [x] (false? x)
    (url? "")
    (url? "http:")
    (url? "https://")))


(deftest breaking-predicate-tests
  (are [x] (true? x)
    (breaking-predicate true)
    (breaking-predicate false)
    (breaking-predicate nil))
  (are [x] (false? x)
    (breaking-predicate :true)
    (breaking-predicate "true")))


(defn email? [s] (and (string? s) (boolean (re-find (get person-spec :email) s))))

(deftest person-email-tests
  (are [x] (true? x)
    (email? "foo@example.com")
    (email? "a@b"))
  (are [x] (false? x)
    (email? :foo)
    (email? "foo_at_example.com")
    (email? "@example.com")
    (email? "foo@")))


(deftest version-predicate-tests
  (are [x] (true? x)
    (version-predicate 0)
    (version-predicate 1)
    (version-predicate 99))
  (are [x] (false? x)
    (version-predicate "0")
    (version-predicate :0)
    (version-predicate 1.0)))


(def req-keys-1? (contains-required-keys? #{:a :b :c}))

(deftest contains-required-keys?-tests
  (are [x] (true? x)
    (req-keys-1? {:a 1 :b 2 :c 3})
    (req-keys-1? {:a 1 :b 2 :c 3 :d 4}))
  (are [x] (false? x)
    (req-keys-1? {})
    (req-keys-1? {:a 1 :b 2})))


(deftest properly-incrementing-versions?-tests
  (are [x] (true? x)
    (properly-incrementing-versions? [])
    (properly-incrementing-versions? [{:version 0}])
    (properly-incrementing-versions? [{:version 0}
                                      {:version 1}
                                      {:version 2}]))
  (are [x] (false? x)
    (properly-incrementing-versions? [{:version 0}
                                      {:version 0}])
    (properly-incrementing-versions? [{:version 0}
                                      {:version 1}
                                      {:version 99}])))


(run-tests)