(require
 '[readmoi.core :refer [print-form-then-eval]]
 '[screedcast.core :refer [panel
                           prettyfy-form-prettyfy-eval
                           screencast-title
                           whats-next-panel]])


(def mechanics-index 1)


[:body
 (panel
  (screencast-title mechanics-index "Mechanics")

  [:pre [:code "{:first-name string?\n :last-name string?\n :phone int?\n :email string?\n :address {:street-name string?\n           :street-number [int? char?]\n           :zip-code int?\n           :city string?\n           :state keyword?}}"]]

  [:div.note
   [:p "Knowing a *little* bit about how Speculoos does its job will greatly help us understand how to use it. First, we need to know on how to address elements contained within a heterogeneous, arbitrarily-nested data structure, like this person specification. Speculoos follows the conventions set by " [:code "clojure.core/get-in"] ", and extends those conventions where necessary."]])


 (panel
  [:h3 "Vectors are addressed by zero-indexed integers."]
  [:pre
   [:code "           [100 101 102 103]"]
   [:br]
   [:code "indexes --> 0   1   2   3"]])


 (panel
  [:h3 "Same for lists…"]
  [:pre
   [:code "          '(97 98 99 100)"]
   [:br]
   [:code "indexes --> 0  1  2  3"]])


 (panel
  [:h3 "…and same for sequences, like " [:code "range"] "."]
  [:pre
   (print-form-then-eval "(range 29 33)")
   [:br]
   [:code "indexes -----------> 0  1  2  3"]]

  [:div.note
   [:p "This also applies to clojure.lang.{cycle,iterate,lazy-seq,range,repeat}"]])


 (panel
  [:h3 "Maps are addressed by their keys."]
  [:pre
   [:code "        {:a 1 :foo \"bar\" :hello 'world}"]
   [:br]
   [:code "keys --> :a   :foo       :hello"]]

  [:div.note
   [:p "...which are often keywords, like this."]])


 (panel
  [:h3 "But maps may be keyed by " [:em "any"] " value, including integers…"]
  [:pre
   [:code "        {0 \"zero\" 1 \"one\" 99 \"ninety-nine\"}"]
   [:br]
   [:code "keys --> 0        1       99"]])


 (panel
  [:h3 "…or some other scalars…"]
  [:pre
   [:code "        {\"a\" :value-at-str-key-a 'b :value-at-sym-key-b \\c :value-at-char-key-c}"]
   [:br]
   [:code "keys --> \"a\"                     'b                     \\c"]]

  [:div.note
   [:p "Like a string, a symbol, a character, or..."]])


 (panel
  [:h3 "…even composite values."]
  [:pre
   [:code "        {[0] :val-at-vec-0 [1 2 3] :val-at-vec-1-2-3 {} :val-at-empty-map}"]
   [:br]
   [:code "keys --> [0]               [1 2 3]                   {}"]])


 (panel
  [:h3 "Set elements are addressed by their identities, so they are located at themselves."]
  [:pre
   [:code "             #{42 :foo true 22/7}"]
   [:br]
   [:code "identities --> 42 :foo true 22/7"]])


 (panel
  [:h3 "Paths"]
  [:p "Let's play " [:em "Get the " [:code "102"] "!"]]
  [:div.vspace]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 101 102 103] [2])" 25 45)]
   [:div.side-by-side
    [:pre [:code "(get-in* " [:em "coll\n         path"] ")"]]]]

  [:div.note
   [:p "First, we'll define 'path'. A " [:em "path"] " is a sequence of indexes, keys, or identities that allow us refer to a single element buried within a nested data structure. For each level of nesting, we add an element to the path sequence. " [:code "clojure.core/get-in"] " illustrates how this works."]

   [:p "The 102 is the third element of a vector, which we've seen is addressed by zero-indexed integers. So, zero-one-two... 102 is get-ed by a path of one element, [2]."]

   [:p "For a vector containing only integers, each element is addressed by a path of length one. To locate integer " [:code "102"] ", the path is " [:code "[2]"] ". If we consider a vector nested within a vector…"]])


 (panel
  [:h3 "Nesting"]
  [:p [:em "Get the " [:code "102"] "!"]]
  [:div.vspace]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 101 [102 103]] [2])" 27 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in [102 103] [0])" 20 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 101 [102 103]] [2 0])" 27 45)]]

  [:div.note
   [:p "…that same path " [:code "[2]"] " now locates the nested vector. Then, within that nested vector, 102 is at index 0."]

   [:p "So to navigate to the 102 contained within the nested vector requires a path of length two: " [:code "[2 0]"] " where the " [:code "2"] " addresses the nested vector " [:code "[102 103]"] " and the " [:code "0"] " addresses the " [:code "102"] "'s position within the nested vector."]])


 (panel
  [:h3 "Deeper nesting"]
  [:p [:em "Get the " [:code "102"] "!"]]
  [:div.vspace]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 [101 [102]]] [1])" 27 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in [101 [102]] [1])" 20 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in [102] [0])" 15 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 [101 [102]]] [1 1 0])" 30 34)] ]

  [:div.note
   [:p "Let's extend that idea: If our 102 is contained within a vector, contained within a vector, contained within a vector, we'd use a path of length three to get that integer."]

   [:p "Index 1 gets us this nested vector, index 1 gets us this nested vector, and index 0 gets us our 102. We assemble our three-element path 1-1-0, and that gets us the 102 buried within the original collection."]

   [:p "The " [:code "102"] " is buried three levels deep, so we use a path with three entries."]])


 (panel
  [:h3 "Paths to nested maps."]
  [:p [:em "Get the " [:code "102"] "!"]]
  [:div.vspace]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in {:x 100 :y 101 :z {:w 102}} [:z])" 30 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in {:w 102} [:w])" 20 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in {:x 100 :y 101 :z {:w 102}} [:z :w])" 25 45)]]

  [:div.note
   [:p "This pattern works similarly for maps. In this example, " [:code "102"] " is located with a path composed of a single key, keyword " [:code ":z"] ". If we now consider a map nested within another map, we need a path with two elements: key "  [:code ":z"] " navigates us to the nested " [:code "{:w 102}"] " map, and then key " [:code ":w"] " navigates us to the " [:code "102"] " within that nested map."]

   [:p "A two element path of :z :w gets us our 102."]])


 (panel
  [:h3 "Heterogeneous nesting #1"]
  [:p [:em "Get the " [:code "102"] "!"]]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 101 {:x 102}] [2])" 30 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in {:x 102} [:x])" 20 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in [100 101 {:x 102}] [2 :x])" 30 45)]]

  [:div.note
   [:p "There's no restriction on what may be nested in what. So we can nest a map within a vector. An index 2 gets us the nested map, then a key :x gets us the 102. Assembling the two-element path 2 :x gets us the 102. Also..."]])


 (panel
  [:h3 "Heterogeneous nesting #2"]
  [:p [:em "Get the " [:code "102"] "!"]]
  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in {:x 100 :y {:z [101 102]}} [:y])" 35 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in {:z [101 102]} [:z])" 25 45)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(get-in [101 102] [1])" 20 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(get-in {:x 100 :y {:z [101 102]}} [:y :z 1])" 35 45)]]

  [:div.note
   [:p "Nest a vector within a map. Key :y gets us this nested map, key :z get us the nested vector, and index 1 gets us our 102. Assembling the three-element path :y :z 1 gets our 102."]])


 (panel
  [:h3 "Special " [:code "get-in"] " for all Clojure data types"]
  [:p [:em "Get the " [:code "102"] "!"]]
  (prettyfy-form-prettyfy-eval "(clojure.core/get-in '(100 101 {:x [102]}) [2 :x 0])" 45 45)
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(require '[fn-in.core :refer [get-in*]])")
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(get-in* '(100 101 {:x [102]}) [2 :x 0])" 30 45)

  [:div.note
   [:p "Perhaps we stumble upon a vector nested within a map nested within a list. clojure.core struggles with that. `nil` isn't what we'd like."]

   [:p "I've made a supplementary library, " [:a {:href "https://github.com/blosavio/fn-in"} "modified version"] " of " [:code "clojure.core/get-in"] " that can seamlessly handle all Clojure collections." [:a {:href "https://github.com/blosavio/fn-in"} [:code "fn-in"]] " can inspect, update, exchange, and remove elements from any heterogeneous, arbitrarily-nested Clojure data structures."]

   [:p [:code "102"] " is contained in three levels of nesting, so its path is assembled of three pieces." ]])


 (panel
  [:h3 "Utility function that enumerates all the paths."]
  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [all-paths]])")
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(all-paths [100 101 102])")

  [:div.note
   [:p "Speculoos provides a little machine to wrangle paths for us. When supplied with a heterogeneous, arbitrarily-nested data structure, " [:code "speculoos.core/all-paths"] " returns a sequence of " [:code "{:path … :value …}"] " for every element, both scalars and collections."]

   [:p "Notice that for this three-element vector, we receive paths for four items: the three integers, plus a path to the outer container itself. The root collection always has a path " [:code "[]"] ". The integer elements each have a path of a single, zero-indexed integer that locates them within the parent vector. Here's how it works with a map."]])


 (panel
  [:h3 "All-paths of a map."]
  [:pre (prettyfy-form-prettyfy-eval "(all-paths {:x 100 :y 101 :z 102})" 80 45)]

  [:div.note
   [:p "Each of the three integers has a path with a key --- :x, :y, and :z --- that locates them within the parent map, and the parent map has a path of " [:code "[]"] " because it's the root collection."]])


 (panel
  [:h3 "All-paths of a nested data structure, example #1."]
  (prettyfy-form-prettyfy-eval "(all-paths [100 101 [102 103]])")

  [:div.note
   [:p "If we supply a nested data structure, the paths reflect that nesting."]

   [:p "Now, we have six elements to consider: each of the four integers --- 100 to 103 --- have a path, and both of the collections have a path. The outer parent vector has path " [:code "[]"] " because it's the root, and the nested collection is located at path " [:code "[2]"] ", the third element of the root vector."]

   [:p "Let's look at all the paths of nested maps."]])


 (panel
  [:h3 "All-paths of a nested data structure, example #2."]
  (prettyfy-form-prettyfy-eval "(all-paths {:x 100 :y 101 :z {:w 102}})" 80 49)

  [:div.note
   [:p "Again, each of the three integers --- 100, 101, and 102 --- has a path, and both of the maps have a path, for a total of five paths."]])


 (panel
  [:h3 "All-paths of a sequence."]
  [:pre
   [:code.form "(cycle [:foo :bar :baz])"]
   [:br]
   [:code.eval ";; => (:foo :bar :baz :foo :bar :baz :foo…)"]]
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(all-paths (vec (take 3 (cycle [:foo :bar :baz]))))")

  [:div.note
   [:p "Sequences are an important collection type. clojure.lang.cycle, here for example, generates a non-terminating pattern of :foo-:bar-:baz."]

   [:p "Elements of a sequence are located by integer indexes, so :foo :bar and :baz are at 0, 1, and 2, with the root collection at []."]

   [:p "Note that `all-paths` requires a terminating sequential thingy, so we must convert the clojure.lang.cycle to a vector so that `all-paths` can inspect it. Most of Speculoos' other functions will take care of that for us without having to do that conversion."]])


 (panel
  [:h3 "All-paths of a list."]
  (prettyfy-form-prettyfy-eval "(all-paths (list 'foo 'bar 'baz))")

  [:div.note
   [:p  "If we ever find ourselves with a nested list on our hands, " [:code "all-paths"] " has got you covered."]

   [:p "List elements are located by indexes, similar to vectors and sequences."]])


 (panel
  [:h3 "All-paths of a set."]
  (prettyfy-form-prettyfy-eval "(all-paths #{:chocolate :vanilla :strawberry})" 60 70)

  [:div.note
   [:p  "Sets are indispensable in some situations, so " [:code "all-paths"] " can handle it."]

   [:p "Admittedly, addressing elements in a set can be a little like herding cats, but it's still useful to have the capability. Wrangling sets merits its own " [:a {:href "#sets"} "dedicated screencast"] "."]

   [:p "Briefly, each element of the set is located at itself --- :chocolate, :vanilla, :strawberry --- while the root collection --- the set --- is located at path []."]

   [:p "Note: This ordering is an implementation detail. It's not guaranteed for a set, or any other collection type, for that matter."]])


 (panel
  [:h3 "All-paths when the elements are functions."]
  (prettyfy-form-prettyfy-eval "(all-paths [int? string? ratio?])" 45 45)

  [:div.note
   [:p "There is nothing special about integers. " [:code "all-paths"] " will treat any element, scalar or collection, the same way. " [:em "Every element has a path."] " We could replace those integers with functions, un-nested in a vector --- like this: four elements, four paths. Three scalars (`int?`, `string?`, `ratio?`, functions in this example, plus one collection. Or, we could have nested them in a map, and so on."]

   [:p "The important principle to remember is this: Every element --- scalar and collection --- of a heterogeneous, arbitrarily-nested data structure, can be assigned an unambiguous path, regardless of its container type."]])


 (whats-next-panel
  mechanics-index
  [:div.note
   [:p "So what does all this paths business have to do with validation? Speculoos inspects the path of a predicate within a specification in an attempt to pair it with an element in the data. If it " [:em "can"] " pair a predicate with a datum, it applies the predicate to that datum."]

   [:p "Now that we've got a system for referring to every element within a data structure, we can discuss how to validate scalars, the topic of the next screencast."]])
 ]