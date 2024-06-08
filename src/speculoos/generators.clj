(ns speculoos.generators
  "Exploring creating generators for 'opaque', composed predicates.
   test.check provides generators for atomic data types such as integer and
   string. But how might one create a generator for #(and (int? %) (< % 10))
   or #(or (int? %) (string? %))?
   This namespace explores those possibilities.

   Perhaps by combining different strategies.
   1. Some functions have their source code inspectable via a tool like clojure.repl/source.
      Because it works by consulting a file on the filesystem, it doesn't appear to work for
      functions at the REPL or REPL equivalent (i.e., nREPL in CIDER). At miniumun, we'd need
      to inspect the function to observe the 'and, 'int?, < and infer something from that.

   2. Use the docstring to make an educated guess based on
       a) example usage that it might contain, but
       b) because clojure.repl/doc constructs its return by aggregating info from
          metadata, just use the metadata, but
       c) some pertinent info might be contained in the :doc text itself and
          not programiatically exposed.
          
   3. Function metadata
       a) :arglist
       b) known metadata keys that contain example usage

   But! In the end, arglists and example usage just give you a basic type (num, str, etc), but don't show you additional restrictions, such as <= 10.

   https://stackoverflow.com/questions/9217911/can-you-get-the-code-as-data-of-a-loaded-function-in-clojure
   https://clojureverse.org/t/how-to-print-a-source-of-a-function-passed-as-a-parameter/6160

   And And! Predicates are not generic functions, their range is a bit more narrow, which may
   help us. They accept one arg, and return only true/false, so their search space is
   more tractable."
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [clojure.repl :refer [demunge source-fn]]
            [speculoos.collection-functions :refer :all]
            [speculoos.fn-in :refer :all]
            [speculoos.core :refer :all]
            [speculoos.function-specs :refer :all]))


(load-file "src/speculoos/collection_hierarchy.clj")

;; explore 'fuzzy' searching for predicate success

(defn plus
  "Test of metadata searching.
   Returns the sum of nums.
   (+) returns 0.
   Does not auto-promote longs, will throw on overflow.
   See also: +'"
  {:UUIDv4 "606ad88a-7f9b-4dc5-86cd-058b28ef8858"
   :speculoos/example-usage ['(+)
                             '(+ 1 2)
                             '(+ 1.1 2.2 3.3 4.4 5.5 6.6)]
   :speculoos/arg-scalar-spec (repeat 7 number?)
   :speculoos/ret-scalar-spec double?}
  [& args] (apply + args))


(defn meta-fn-example-usage
  "Returns the example usage metadata of function f."
  {:UUIDv4 #uuid "7dd478a3-c6f8-4175-a187-663adbc11a5e"}
  [f]
  (:speculoos/example-usage (fn-meta f)))


(meta-fn-example-usage plus)
(meta-fn-example-usage +)


(defn eval-example-usage-to-str
  "Returns a sequence of evaluated example usages of function f.
   eval-marker is a string that interposes the expression and the returned results.
   Note to self: Need to think through the security imliciations."
  {:UUIDv4 #uuid "f387184e-6655-4338-b456-70c7e3858264"}
  [f eval-marker]
  (clojure.string/join "\n" (map #(str % eval-marker (eval %)) (meta-fn-example-usage f))))


(eval-example-usage-to-str plus " ;; => ")
(eval-example-usage-to-str + " ;; => ")


(defn meta-fn-specs
  "Returns the speculoos specs in the metadata of function f."
  {:UUIDv4 #uuid "e9c4874a-08f0-46e1-9a1e-b17d27cc71f5"}
  [f]
  (select-keys (fn-meta f) speculoos.function-specs/recognized-spec-keys))


(meta-fn-specs plus)
(meta-fn-specs +)


(defn format-fn-specs
  "Make the function name in fstring nice to read."
  {:UUIDv4 #uuid "4867e723-7ec6-49b7-bcb9-7e1ee397b08d"}
  [fstring]
  (let [re #"\#function\[clojure\.core\/(\S*)\]"]
    (clojure.string/replace fstring re "$1")))

(format-fn-specs (meta-fn-specs plus))


(defn doc+
  "Enhanced docstring for function f, appending its original docstring
   with example usage and specs."
  {:UUIDv4 #uuid "5fa1fe9b-c5e6-4da8-9413-bc107be49852"}
  [f]
  (str
   (:doc (fn-meta f))
   "\nExample usage:\n"
   (eval-example-usage-to-str f " ;; => ")
   "\nSpecs:\n"
   (format-fn-specs (meta-fn-specs f))))

(println (doc+ plus))


(clojure.repl/source +)
(clojure.repl/source-fn '+)

(defn my-pred
  "docstring"
  {:metadata1 "val1"}
  [x] (and (int? x)
           (even? x)))

(my-pred 3)
(my-pred 4)
(my-pred 4.0)

(clojure.repl/source +)
(clojure.repl/source-fn '+)


(defn fn-source [f]
  (let [fn-name (-> f .getClass .getName)
        fn-name (demunge fn-name)
        fn-sym (symbol fn-name)]
    (source-fn fn-sym)))

(fn-source +)
(fn-source reduce)



(with-out-str (clojure.repl/source +))


(clojure.repl/source-fn (fn-var my-pred))


(fn-source +)
(fn-source speculoos.generators/my-pred)
(fn-source my-pred)

(fn-source speculoos.function-specs/fn-var)
(fn-source speculoos.generators/fn-source)

(fn-meta +)
(fn-meta plus)
(meta #'+)
(meta #'plus)
(fn-meta clojure.core/+)
(fn-meta speculoos.generators/plus)
(fn-meta my-pred)

(type #(+ % 5)) ;; speculoos.generators$eval12313$fn__12314

(def xyz #(+ % 5))
(type xyz) ;; speculoos.generators$xyz
(var xyz) ;; #'speculoos.generators/xyz
(fn-var xyz) ;; #'speculoos.generators/xyz
(fn-meta xyz) ;; {:line 141, :column 1, :file "/home/xel/Documents/computer/clojure/projects/speculoos/speculoos/src/speculoos/generators.clj", :name xyz, :ns #namespace[speculoos.generators]}
(fn-source xyz)


(with-out-str (clojure.repl/doc reduce))
(with-out-str (clojure.repl/doc +))
(meta #'+)

(clojure.repl/doc new)