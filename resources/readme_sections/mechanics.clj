[:section#mechanics
 [:h2 "Mechanics"]
 [:p "Knowing a little bit about how Speculoos does its job will greatly help you understand how to use it. First, we need to know on how to address elements contained within a heterogeneous, arbitrarily-nested data structure. Speculoos follows the conventions set by " [:code "clojure.core/get-in"] ", and extends those conventions where necessary."]

 [:p "Vectors are addressed by zero-indexed integers."]
 [:pre
  [:code "           [100 101 102 103]"]
  [:br]
  [:code "indexes --> 0   1   2   3"]]

 [:p "Same for lists…"]
 [:pre
  [:code "          '(97 98 99 100)"]
  [:br]
  [:code "indexes --> 0  1  2  3"]]

 [:p "…and same for other sequences, like " [:code "range"] "."]
 [:pre
  (print-form-then-eval "(range 29 33)")
  [:br]
  [:code "indexes -----------> 0  1  2  3"]]

 [:p "Maps are addressed by their keys, which are often keywords, like this."]
 [:pre
  [:code "        {:a 1 :foo \"bar\" :hello 'world}"]
  [:br]
  [:code "keys --> :a   :foo       :hello"]]

 [:p "But maps may be keyed by " [:em "any"] " value, including integers…"]
 [:pre
  [:code "        {0 \"zero\" 1 \"one\" 99 \"ninety-nine\"}"]
  [:br]
  [:code "keys --> 0        1       99"]]

 [:p "…or some other scalars…"]
 [:pre
  [:code "        {\"a\" :value-at-str-key-a 'b :value-at-sym-key-b \\c :value-at-char-key-c}"]
  [:br]
  [:code "keys --> \"a\"                     'b                     \\c"]]

 [:p "…even composite values."]
 [:pre
  [:code "        {[0] :val-at-vec-0 [1 2 3] :val-at-vec-1-2-3 {} :val-at-empty-map}"]
  [:br]
  [:code "keys --> [0]               [1 2 3]                   {}"]]

 [:p "Set elements are addressed by their identities, so they are located at themselves."]
 [:pre
  [:code "             #{42 :foo true 22/7}"]
  [:br]
  [:code "identities --> 42 :foo true 22/7"]]
 
 [:p "A " [:em "path"] " is a sequence of indexes, keys, or identities that allow us refer to a single element buried within a nested data structure. For each level of nesting, we add an element to the path sequence. " [:code "clojure.core/get-in"] " illustrates how this works."]
 [:pre (print-form-then-eval "(get-in [100 101 102 103] [2])")]
 [:p "For a vector containing only integers, each element is addressed by a path of length one. To locate integer " [:code "102"] ", the path is " [:code "[2]"] ". If we consider a vector nested within a vector…"]
 [:pre (print-form-then-eval "(get-in [100 101 [102 103]] [2])")]
 [:p "…that same path " [:code "[2]"] " now locates the nested vector. To navigate to an integer contained within the nested vector…"]
 [:pre (print-form-then-eval "(get-in [100 101 [102 103]] [2 0])")]
 [:p "…requires a path of length two: " [:code "[2 0]"] " where the " [:code "2"] " addresses the nested vector " [:code "[102 103]"] " and the " [:code "0"] " addresses the " [:code "102"] " within the nested vector. If we have an integer contained within a vector, contained within a vector, contained within a vector, we'd use a path of length three to get that integer."]
 [:pre
  (print-form-then-eval "(get-in [100 [101 [102]]] [1])")
  [:br]
  (print-form-then-eval "(get-in [100 [101 [102]]] [1 1])")
  [:br]
  (print-form-then-eval "(get-in [100 [101 [102]]] [1 1 0])")]
 [:p "The " [:code "102"] " is buried three levels deep, so we use a path with that many entries."]
 
 [:p "This system works similary for maps. Elements contained in un-nested collections are located with a path of length one."]
 [:pre (print-form-then-eval "(get-in {:x 100 :y 101 :z 102} [:z])")]
 [:p "In this example, " [:code "102"] " is located with a path composed of a single key, keyword " [:code ":z"] ". If we now consider a map nested within another map…"]
 [:pre (print-form-then-eval "(get-in {:x 100 :y 101 :z {:w 102}} [:z :w])")]
 [:p "…we need a path with two elements: key " [:code ":z"] " navigates us to the nested " [:code "{:w 102}"] " map, and then key " [:code ":w"] " navigates us to the " [:code "102"] " within that nested map."]

 [:p "There's no restriction on what may be nested in what, so we can nest a map within a vector…"]
 [:pre (print-form-then-eval "(get-in [100 101 {:x 102}] [2 :x])")]
 [:p "…or nest a vector within a map…"]
 [:pre (print-form-then-eval "(get-in {:x 100 :y {:z [101 102]}} [:y :z 1])")]
 [:p "…or, if we use a " [:a {:href "https://github.com/blosavio/fn-in"} "modified version"] " of " [:code "clojure.core/get-in"] ", nest a vector within a map within a list."]
 [:pre
  (print-form-then-eval "(require '[fn-in.core :refer [get-in*]])")
  [:br]
  [:br]
  (print-form-then-eval "(get-in* '(100 101 {:x [102]}) [2 :x 0])")]
 [:p [:code "102"] " is contained in three levels of nesting, so its path is comprised of three pieces."]

 [:p "Speculoos provides a little machine to wrangle paths for you. When supplied with a heterogeneous, arbitrarily-nested data structure, " [:code "speculoos.core/all-paths"] " returns a sequence of " [:code "{:path _ :value _ }"] " for every element, both scalars and collections."]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [all-paths]])")
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [100 101 102])")]
 [:p "Note: we receive paths for four items, three integers, plus a path to the outer container itself. The root collection always has a path " [:code "[]"] ". The integer elements each have a path of a single, zero-indexed integer that locates them within the parent vector. Here's how it works with a map."]
 [:pre (print-form-then-eval "(all-paths {:x 100 :y 101 :z 102})" 80 45)]
 [:p "Each of the three integers has a path with a key that locates them within the parent map, and the parent map has a path of " [:code "[]"] " because it's the root collection."]

 [:p "If we supply a nested data structure, the paths reflect that nesting."]
 [:pre (print-form-then-eval "(all-paths [100 101 [102 103]])")]
 [:p "Now, we have six elements to consider: each of the four integers have a path, and both of the collections have a path. The outer parent vector has path " [:code "[]"] " because it's the root, and the nested collection is located at path " [:code "[2]"] ", the third element of the root vector. Let's look at all the paths of nested maps."]
 [:pre (print-form-then-eval "(all-paths {:x 100 :y 101 :z {:w 102}})" 80 49)]
 [:p "Again, each of the integers has a path, and each of the maps has a path, for a total of five paths."]
 
 [:p "There is nothing special about integers. " [:code "all-paths"] " will treat any element, scalar or collection, the same way. " [:em "Every element has a path."] " We could replace those integers with functions, un-nested in a vector…"]
 
 [:pre (print-form-then-eval "(all-paths [int? string? ratio?])" 50 45)]
 
 [:p "…or nested in a map…"]
 
 [:pre (print-form-then-eval "(all-paths {:x int? :y string? :z {:w ratio?}})" 80 40)]
 
 [:p "The important principle to remember is this: Every element, scalar and collection, of a heterogeneous, arbitrarily-nested data structure, can be assigned an unambiguous path, regardless of its container type."]

 [:p "If you ever find yourself with a nested list on your hands, " [:code "all-paths"] " has got you covered."]

 [:pre (print-form-then-eval "(all-paths [42 (list 'foo 'bar 'baz)])")]

 [:p "Likewise, sets are indispensible in some situations, so " [:code "all-paths"] " can handle it."]

 [:pre (print-form-then-eval "(all-paths {:a 42 :b #{:chocolate :vanilla :strawberry}})" 60 70)]

 [:p "Admittedly, addressing elements in a set can be a little like herding cats, but it's still useful to have the capability. Wrangling sets merits its own " [:a {:href "#sets"} "dedicated section"] "."]
 ]