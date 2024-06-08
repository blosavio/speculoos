(ns speculoos-hiccup-test
  (:require
   [clojure.test :refer [deftest is are testing run-tests]]
   [speculoos-hiccup :refer :all]))


(deftest comment-newlines-tests
  (are [x y] (= x y)
    (comment-newlines "" "-->" ";;")
    ";;-->"

    (comment-newlines "abcde" " => " ";;")
    ";; => abcde"

    (comment-newlines "abcde\nfghij\nklmno" " --> " ";;")
    ";; --> abcde\n;;     fghij\n;;     klmno"))


(deftest prettyfy-tests
  (are [x y] (= x y)
    (prettyfy (str (eval (read-string "[11 22 33]"))))
    "[11 22 33]"

    (prettyfy (str (eval (read-string "(repeat 3 (repeat 3 {:a 11 :b 22 :c 33}))"))))
    "(({:a 11, :b 22, :c 33}\n  {:a 11, :b 22, :c 33}\n  {:a 11, :b 22, :c 33})\n  ({:a 11, :b 22, :c 33}\n   {:a 11, :b 22, :c 33}\n   {:a 11, :b 22, :c 33})\n  ({:a 11, :b 22, :c 33}\n   {:a 11, :b 22, :c 33}\n   {:a 11, :b 22, :c 33}))"

    (prettyfy (str (eval (read-string "(repeat 2 (repeat 2 {:a 11 :b 22}))"))))
    "(({:a 11, :b 22} {:a 11, :b 22})\n  ({:a 11, :b 22} {:a 11, :b 22}))"))


(deftest print-form-then-eval-tests
  (are [x y] (=  x y)
    (print-form-then-eval "()")
    [:code "() ;; => ()"]

    (print-form-then-eval "(+)")
    [:code "(+) ;; => 0"]

    (print-form-then-eval "(+ 1 2 3)")
    [:code "(+ 1 2 3) ;; => 6"]

    (print-form-then-eval "(map inc [11 22 33])")
    [:code "(map inc [11 22 33]) ;; => (12 23 34)"]

    (print-form-then-eval "[11 22 33]")
    [:code "[11 22 33] ;; => [11 22 33]"]

    (print-form-then-eval "(def test-def 99)")
    [:code "(def test-def 99)"]

    (print-form-then-eval "(defn test-defn [x] (* 3 x))")
    [:code "(defn test-defn [x] (* 3 x))"]

    (print-form-then-eval "(defmacro Violets-awesome-macro [x] `(+ ~x))")
    [:code "(defmacro Violets-awesome-macro [x] `(+ ~x))"]

    ;; See issue with macroexpansion and lein test running: https://github.com/technomancy/leiningen/issues/912
    #_ (require '[speculoos.utility :refer [defpred]])
    #_ (print-form-then-eval "(defpred :awesome-predicate int? #(rand 99))")
    #_ [:code "(defpred :awesome-predicate int? #(rand 99))"]

    (print-form-then-eval "(require '[speculoos.core :as loos])")
    [:code "(require '[speculoos.core :as loos])"]

    (print-form-then-eval "(#(< % 5) 4)")
    [:code "(#(< % 5) 4) ;; => true"]

    (print-form-then-eval "(* 1 2 3)" " --->>> ")
    [:code "(* 1 2 3) ;; --->>> 6"]

    (print-form-then-eval "(map inc (range 0 23))")
    [:code "(map inc (range 0 23))\n;; => (1\n;;     2\n;;     3\n;;     4\n;;     5\n;;     6\n;;     7\n;;     8\n;;     9\n;;     10\n;;     11\n;;     12\n;;     13\n;;     14\n;;     15\n;;     16\n;;     17\n;;     18\n;;     19\n;;     20\n;;     21\n;;     22\n;;     23)"]

    (print-form-then-eval "(filter #(odd? %) (range 42))")
    [:code "(filter #(odd? %) (range 42))\n;; => (1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 35 37 39 41)"]

    (print-form-then-eval "(get-in {:a {:x 11 :y 22 :z 33} :b {:x 11 :y 22 :z 33} :c {:x 11 :y 22 :z 33}} [:b :z])")
    [:code "(get-in {:a {:x 11, :y 22, :z 33},\n         :b {:x 11, :y 22, :z 33},\n         :c {:x 11, :y 22, :z 33}}\n        [:b :z])\n;; => 33"]))


(deftest label-tests
  (are [x y] (= (str (label x)) y)
    ""
    "<label class=\"margin-toggle sidenote-number\" for=\"\"></label>"

    "foo"
    "<label class=\"margin-toggle sidenote-number\" for=\"foo\"></label>"))


(deftest side-note-tests
  (are [x y] (= x y)
    (str (side-note "" ""))
    "<input class=\"margin-toggle\" id=\"\" type=\"checkbox\"><span class=\"sidenote\"></span></input>"

    (str (side-note "foo" "bar"))
    "<input class=\"margin-toggle\" id=\"foo\" type=\"checkbox\"><span class=\"sidenote\">bar</span></input>"))


(deftest inline-img-tests
  (are [x y] (= x y)
    (str (inline-img "" "" "" ""))
    "<figure><label class=\"margin-toggle\" for=\"\">&amp;#8853;</label><input class=\"margin-toggle\" id=\"\" name=\"\" type=\"checkbox\" value=\"true\" /><span class=\"marginnote\"></span><img alt=\"\" src=\"\" /></figure>"

    (str (inline-img "foo" "bar" "https://example.com" "alt text"))
    "<figure><label class=\"margin-toggle\" for=\"foo\">&amp;#8853;</label><input class=\"margin-toggle\" id=\"foo\" name=\"foo\" type=\"checkbox\" value=\"true\" /><span class=\"marginnote\">bar</span><img alt=\"alt text\" src=\"https://example.com\" /></figure>"))


(deftest margin-img-tests
  (are [x y] (= x y)
    (str (margin-img "" "" "" ""))
    "<label class=\"margin-toggle\" for=\"\">&amp;#8853;</label><input class=\"margin-toggle\" id=\"\" name=\"\" type=\"checkbox\" value=\"true\" /><span class=\"marginnote\"><img alt=\"\" src=\"\" /></span>"

    (str (margin-img "foo" "bar" "https://example.com" "alt text"))
    "<label class=\"margin-toggle\" for=\"foo\">&amp;#8853;</label><input class=\"margin-toggle\" id=\"foo\" name=\"foo\" type=\"checkbox\" value=\"true\" /><span class=\"marginnote\"><img alt=\"alt text\" src=\"https://example.com\" />bar</span>"))


(deftest long-date-tests
  (are [x] x
    (string? (long-date))
    (some? (re-find #"^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$" (long-date)))))


(deftest short-date-tests
  (are [x] x
    (string? (short-date))
    (some? (re-find #"^\d{4} \S+ \d{2}$" (short-date)))))


(deftest copyright-tests
  (clojure.string/starts-with? (copyright) "Copyright © ")
  (clojure.string/ends-with? (copyright) " Brad Losavio.")
  (some? (re-find #"^Copyright © 20\d{2} Brad Losavio.$" (copyright))))


(run-tests)
