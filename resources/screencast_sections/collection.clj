(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def collection-validation-index 3)


[:body

 (panel
  (screencast-title collection-validation-index "Collection Validation")
  [:p "So far…"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 40 80)

  [:div.note
   [:p "You may have been bothered about something while going through the 'scalar validation' screencast. Every example we've seen so far shows Speculoos validating individual scalars, such as integers, strings, booleans, etc."]

   [:p "However, we might need to specify some property of a collection itself, such as a vector's length, the presence of a key in a map, relationships " [:em "between"] " datums, etc. That is " [:em "collection validation"] "."]])


 (panel
  [:h3 "Scalars versus Collections"]

  [:table
   [:tr
    [:td "scalars"]
    [:td [:code
          [:span.de-highlight "["]
          [:span.highlight    "42"]
          [:span.de-highlight " {:x "]
          [:span.highlight    "\"abc\""]
          [:span.de-highlight " :y "]
          [:span.highlight    "22/7"]
          [:span.de-highlight "}]"]
          ]]]

   [:tr
    [:td "collections"]
    [:td [:code
          [:span.highlight "["]
          [:span.de-highlight "42"]
          [:span.highlight " {:x "]
          [:span.de-highlight "\"abc\""]
          [:span.highlight " :y "]
          [:span.de-highlight "22/7"]
          [:span.highlight "}]"]]]]]

  [:div.note
   [:p "This is one way to visualize the difference. Scalar validation targets only the scalars: numbers, strings, characters, etc.. Collection validation only validates the collections themselves: vectors, maps, sequences, lists, sets. We could kinda think about it as validating the brackets, parens, braces, etc."]])


 (panel
  [:h3 [:em "Validating"] " scalars versus " [:em "validating"] " collections"]

  [:code "speculoos.core/validate-scalars"]

  [:div.vspace]

  [:code "speculoos.core/validate-collections"]

  [:div.note
   [:p "One of Speculoos' main concepts is that scalars are specified and validated explicitly separately from collections. You perhaps noticed that the function name we have been using wasn't " [:code "validate"] " but instead " [:code "validate-scalars"] ". Speculoos provides a parallel group of functions to validate the properties of collections, independent of the scalar values they contain. Let's examine why and how they're separated."]])


 (panel
  [:h3 "When to validate collections versus validating scalars #1"]

  [:h4 "Validate property of the collection"]

  [:p "Examples"]

  [:ul
   [:li "Size of a collection: " [:code "#(< 10 (count %))"]]
   [:li "Existence of an element: " [:code "#(contains? % :email)"]]]

  [:div.note])

  (panel
  [:h3 "When to validate collections versus validating scalars #2"]

  [:h4 "Validation relationship between scalars"]

  [:p "Examples"]

  [:ul
   [:li "Second element equal to first: " [:code "#(= (get % 0) (get % 1))"]]
   [:li "All elements ascending: " [:code "#(apply < %)"]]]

  [:div.note
   [:p "Don't have to pick _just_ scalar or collection. We can do both and use the advantages of each."]])


 (load-file "resources/screencast_sections/mottos.clj")


 (panel
  [:h3 "Motivational quick example #1: validate vector length"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-collections?]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(defn length-3? [v] (= 3 (count v)))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? [42 \"abc\" 22/7] [length-3?])" 45 45)

  [:div.note
   [:p "3 Mottos: Collection validation, see fn name. Specification mimics shape of data. Everything was paired; nothing ignored."]

   [:p "Note: Predicates apply to their immediate parent collections. `length-3?` targets the vector, not scalar `42`."]])


 (panel
  [:h3 "Motivational quick example #2: validate key in map"]

  (prettyfy-form-prettyfy-eval "(defn map-contains-keyword-y? [m] (contains? m :y))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? {:x 42} {:foo map-contains-keyword-y?})" 55 45)

  [:div.note

   [:p "Ignore `:foo` for the moment. Notice: specification shape mimics shape of data (but not exact copy)."]])


 (panel
  [:h3 "Motivational quick example #3: validate two arguments"]

  (prettyfy-form-prettyfy-eval "(defn even-args-fn-call? [f] (even? (count (rest f))))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? (list < 1 2) (list even-args-fn-call?))" 55 45)

  [:div.note
   [:p "Validating aspects of the number of elements in a collection is only possible with the whole collection, not merely the scalars."]])


 (panel
  [:h3 "Motivational quick example #4: all set members even number"]

  (prettyfy-form-prettyfy-eval "(defn all-odd? [s] (every? odd? s))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? #{1 2 3} #{all-odd?})" 35 45)

  [:div.note
   [:p "All these examples require access to the entire collection. None could be accomplished with a scalar validation."]])


 (panel
  [:h3 "Where collection predicates apply #1"]

  [:h4 "Predicates apply to their " [:em "parent"] " collection."]
  
  (prettyfy-form-prettyfy-eval "(valid-collections? [42 \"abc\" 22/7] [vector?])" 40 30)

  [:div.note
   [:p "Predicate `vector?` is applied to the parent container, not the integer `42`."]])


  (panel
  [:h3 "Where collection predicates apply #2"]

  [:h4 "Predicates apply to their " [:em "immediate"] " parent collection."]
  
  (prettyfy-form-prettyfy-eval "(valid-collections? [[42 \"abc\" 22/7]] [[length-3?]]))" 40 30)


  [:div.note
   [:p "Predicate `length-3?` is applied to the immediate parent vector, not the grand-parent. The immediate parent has three elements, so the validation returns `true`."]

   [:p "The outer/root collection wasn't paired, so it was ignored (Motto #3)."]])


  (panel
  [:h3 "Where collection predicates apply #3"]

  [:h4   [:em "Any"] " predicates apply to their immedicate parent collection."]
  
  (prettyfy-form-prettyfy-eval "(valid-collections? [42 \"abc\" 22/7] [coll? vector? sequential?])" 50 30)

  [:div.note
   [:p "All three predicates are applied to the vector, not only the first one. Scalar validation: 1-to-1 predicate-to-scalar. Collection validation: many-to-1 predicate-to-coll."]

   [:p "Applying predicates to their parent collection allows us to write specifications whose shape mimic the shape of the data."]

   [:p "3 Mottos + targeting parent collection are an emergent property of the collection validation algorithm. If we understand the algorithm, we can write clear, correct, and expressive collection specifications."]])


 (panel
  [:h3 "How collection validation works"]

  [:h4 "Collection predicate"]

  (prettyfy-form-prettyfy-eval "(defn len-3? [c] (= 3 (count c)))")

  [:div.note
   [:p "Imagine we wanted to specify that our data vector was exactly three elements long. We might reasonably write this predicate, whose argument is a collection."]

   [:p "`len-3?` is a predicate that returns `true` if the collection has three elements"]

   [:p "Notice that this predicate tests a property of the collection itself: the number of elements it contains. " [:code "validate-scalars"] " has no way to do this kind of test because it deliberately only considers scalars."]])

 
 (panel
  [:h3 "All-paths of a Vector."]

  [:p [:em "A vector, containing exactly three elements."]]

  (prettyfy-form-prettyfy-eval "(all-paths [42 \"abc\" 22/7])")

  [:div.note
   [:p "Imagine we want to specify that our data vector was exactly three elements long. The paths of that data might look like this."]

   [:p "Since we're now interested in specifying collections, we'll discard the " [:em "scalars"] " and focus only on the " [:em "collections"] ". In this case, there's only one collection, the vector at path " [:code "[]"] ", which signifies that it's the root collection."]])


 (panel
  [:h3 "Collection specification: construction strategy"]

  [:pre [:code "[42 \"abc\" 22/7] ;; copy-paste data"]]

  [:div.vspace]

  [:pre [:code "[             ] ;; delete scalars"]]

  [:div.vspace]

  [:pre [:code "[len-3?       ] ;; insert predicate"]]

  [:div.note
   [:p "We're validating collections (Motto #1), so we're only interested in the root collection at path " [:code "[]"] " in the data. Let's apply Motto #2 and shape our specification to mimic the shape of the data. We'll copy-paste the data…"]
   
   [:p "That will be our specification. Notice: during collection validation, we insert predicates " [:em "inside"] " the collection that they target."]])


 (panel
  [:h3 "Collection validation algorithm"]

  [:ol.collection-algorithm
   [:li [:code "All-paths"] " data, then specification"]
   [:li [:s "scalar"] " elements from the data"]
   [:li [:s "non-predicate"] " elements from the specification"]
   [:li "Pair via paths: " [:em "path"] " to " [:code "(drop-last " [:em "path"] ")"] "; discard un-paired"]
   [:li "Apply predicates"]]

  [:div.note
   [:p "Validating collections uses a " [:em "slightly"] " adjusted version of the " [:a {:href "#scalar-algorithm"} "scalar validation algorithm"] ". (If you are curious " [:em "why"] " the collection algorithm is different, see " [:a {:href "#collection-predicate-paths"} "this later subsection"] ".) The algorithm for validating collections is this."]

   [:p "There are two main differences: We keep only the _collections_ from the data instead of the scalars (Motto #1). Predicates and their target do not share the exact same path. Instead, predicate at path pairs with collection at `(drop-last path)`."]])


 (panel
  [:h3 "Manual collection validation #1: Enumerate paths"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "data"]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [42 \"abc\" 22/7])")]
   [:div.side-by-side
    [:div  "specification"]
    [:div.vspace]
    [:div.vspace]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [len-3?])")]]

  [:div.note
   [:p "Let's perform that algorithm manually. We run " [:code "all-paths"] " on both the data…"  "…and " [:code "all-paths"] " on our collection specification."]])


 (panel
  [:h3 "Manual collection validation #2: Keep only…"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "…collections from data"]
    [:pre [:code "[{:path [], :value [42 \"abc\" 22/7]}]"]]]
   [:div.side-by-side
    [:div "…predicates from specification"]
    [:pre [:code "[{:path [0], :value len-3?}]"]]]]

  [:div.note
   [:p "We discard all scalar elements of the data, keeping only the collection elements. And we keep only the predicate elements of the specification."]])



 (panel
  [:h3 "Manual collection validation #3: Pair collections with predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "path of coll in data"]
    [:pre [:code "[]"]]]
   [:div.side-by-side
    [:div "path of predicate in specification"]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(= [] (drop-last [0]))")]]

  [:div.note
   [:p "The root collection's is path " [:code "[]"] "; " [:code "len-3?"] " predicate's path is " [:code "[0]"] ". " [:code "(drop-last [0])"] " evaluates to " [:code "()"] ", which is equivalent. So the predicate and the collection are paired."]])


 (panel
  [:h3 "Manual collection validation #4: Apply predicates"]

  (prettyfy-form-prettyfy-eval "(len-3? [42 \"abc\" 22/7])")

  [:div.note
   [:p " We then apply the predicate. The data vector is indeed three elements long, so predicate `len-3?` is satisfied."]])


 (panel
  [:h3 "Collection validation: flat vector"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [validate-collections]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections [42 \"abc\" 22/7] [len-3?])" 45 80)

  [:div.note
   [:p "That's familiar. " [:code "validate-collections"] " shows every collection+predicate pair. In this case, the data's root vector was paired with the single " [:code "len-3?"] "predicate. The " [:code ":datum"] " represents the thing being tested, the " [:code ":predicate"] "s indicate the predicate functions, and " [:code "valid?"] " reports whether that predicate was satisfied. The root vector contains three elements, so " [:code "len-3?"] " was satisfied."]

   [:p "There are now three things that involve some notion of a path. The predicate was found at " [:code ":path-predicate"] " in the specification. The datum was found at " [:code ":ordinal-path-datum"] " in the data, which is also presented in a more friendly format as the literal path " [:code ":path-datum"] ". (We'll explain the terms embodied by these keywords as the discussion progresses.) Notice that the path of the root vector " [:code "[]"] " is equivalent to running " [:code "drop-last"] " on the path of the " [:code "len-3?"] " predicate: " [:code "(drop-last [0])"] " evaluates to " [:code "()"] "."]])

 (panel
  [:h3 "Nested vector: data, predicates, & specification"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "[42 [\"abc\" 22/7]]"]]]
    [:div.vspace]]

   [:tr
    [:td "predicates"]
    [:td
     (prettyfy-form-prettyfy-eval "(defn len-2? [c] (= 2 (count c)))")
     (prettyfy-form-prettyfy-eval "(defn len-3? [c] (= 3 (count c)))")
     [:div.vspace]]]

   [:tr
    [:td"specification"]
    [:td [:pre
          [:code "[42     [\"abc\" 22/7]] ;; copy-paste data"] [:br]
          [:code "[       [          ]] ;; delete scalars"] [:br]
          [:code "[len-3? [len-2?    ]] ;; insert predicates"]]]]]

  [:div.note
   [:p "Let's explore validating a two-element vector nested within a two-element vector. To test whether each of those two vectors contain two elements, we could write this collection predicate."]

   [:p "Remember Motto #1: This predicate accepts a collection, " [:code "c"] ", not a scalar."]

   [:p "We'll invent some data, a two-element vector nested within a two-element vector by wrapping the final two elements inside an additional pair of brackets."]

   [:p "Note that the outer root vector contains exactly two elements: one scalar " [:code "42"] " and one descendant collection, the nested vector " [:code "[\"abc\" 22/7]"] "."]

   [:p "Following Motto #2, we'll compose a collection specification whose shape mimics the shape of the data. We copy-paste the data, delete the scalars, and insert our predicates."]

   [:p "(I've re-used the " [:code "len-3?"] " predicate so that in the following examples, it'll be easier to keep track of which predicate goes where when we have multiple predicates.)"]])

 (panel
  [:h3 "Nested vector #1: Enumerate paths"]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths [42 [\"abc\" 22/7]])")]
   [:div.side-by-side
    [:div.vspace]
    [:div.vspace]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [len-3? [len-2?]])")]])

 (panel
  [:h3 "Nested vector #2: Keep only…"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "…collections from data"]
    [:pre [:code "[{:path [], :value [42 [\"abc\" 22/7]]}\n {:path [1], :value [\"abc\" 22/7]}]"]]]
   [:div.side-by-side
    [:div "…predicates from specification"]
    [:pre [:code "[{:path [0], :value len-3?}\n {:path [1 0], :value len-2?}]"]]]])


 (panel
  [:h3 "Nested vector #3: Pair collections with predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "paths of collections in data"]
    [:pre [:code "[]"]]
    [:pre [:code "[1]"]]]
   [:div.side-by-side
    [:div "path of predicates in specification"]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    (prettyfy-form-prettyfy-eval "(= [] (drop-last [0]))")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [1 0])")
    (prettyfy-form-prettyfy-eval "(= [1] (drop-last [1 0]))")]]

  [:div.note
   [:p "Scalar validation: share exact paths, but collection validation: collection at `(drop-last path)`, predicate at `path`. The practical effect: the predicate applies to the immediate parent."]])


 (panel
  [:h3 "Nested vector #4: Apply predicates"]

  (prettyfy-form-prettyfy-eval "(len-3? [42 [\"abc\" 22/7]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(len-2? [\"abc\" 22/7])")

  [:div.note
   [:p "Nested vector satisfies predicate, but root doesn't; it only has two elements: the scalar `42` and the nested vector."]])


 (panel
  [:h3 "Collection validation: nested vector"]

  (prettyfy-form-prettyfy-eval "(validate-collections [42 [\"abc\" 22/7]] [len-3? [len-2?]])" 45 80)

  [:div.note
   [:p "Perform algorithm with one invovacation. Two pairs of predicates+collections, two validation entries. `len-3?` not satisfied, `len-2?` was satisfied."]])


 (panel
  [:h3 "Nested nested vector: data, predicates, & specification"]

  [:table
   [:tr
    [:td "data"]
    [:td
     [:pre [:code "[42 [\"abc\" [22/7]]]"]]
     [:div.vspace]]]

   [:tr
    [:td "predicates"]
    [:td
     (prettyfy-form-prettyfy-eval "(defn len-1? [c] (= 1 (count c)))")
     (prettyfy-form-prettyfy-eval "(defn len-2? [c] (= 2 (count c)))")
     (prettyfy-form-prettyfy-eval "(defn len-3? [c] (= 3 (count c)))")
     [:div.vspace]]]

   [:tr
    [:td "specification"]
    [:td
     [:pre [:code "[42     [\"abc\"  [22/7  ]]] ;; copy-paste data"]]
     [:pre [:code "[       [       [      ]]] ;; remove scalars"]]
     [:pre [:code "[len-3? [len-2? [len-1?]]] ;; insert predicates"]]]]]

  [:div.note
   [:p "One additional nested vector, one additional nested predicate. Three predicates, three collections. Specification properly mimics shape of data, so all three will be paired."]])

 (panel
  [:h3 "Nested nested vector #1: Enumerate paths"]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths [42 [\"abc\" [22/7]]])")]
   [:div.side-by-side
    [:div.vspace]
    [:div.vspace]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [len-3? [len-2? [len-1?]]])" 55 55)]]

  [:div.note
   [:p "Collection validation, so we're only interested in three collection elements of data, and predicate elements of specification."]])

  (panel
  [:h3 "Nested nested vector #2: Keep only…"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "…collections from the data"]
    [:pre [:code "[{:path [], :value [42 [\"abc\" [22/7]]]}\n {:path [1], :value [\"abc\" [22/7]]}\n {:path [1 1], :value [22/7]}]"]]]
   [:div.side-by-side
    [:div "…predicates from the specification"]
    [:pre [:code "[{:path [0], :value len-3?}\n {:path [1 0], :value len-2?}\n {:path [1 1 0], :value len-1?}]"]]]]

  [:div.note
   [:p "Keep only collections and predicates."]])


 (panel
  [:h3 "Nested nested vector #3: Pair collections with predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "paths of collections in data"]
    [:pre [:code "[]"]]
    [:pre [:code "[1]"]]
    [:pre [:code "[1 1"]]]
   [:div.side-by-side
    [:div "path of predicates in specification"]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1 0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1 1 0])")
    [:div.vspace]    
    (prettyfy-form-prettyfy-eval "(= [] (drop-last [0]))")
    (prettyfy-form-prettyfy-eval "(= [1] (drop-last [1 0]))")
    (prettyfy-form-prettyfy-eval "(= [1] (drop-last [1 1 0]))")]]

  [:div.note
   [:p "The `drop-last` procedure has the practical result that the predicates will apply to their immediate parent. Each of the three forms a pair."]])

  (panel
  [:h3 "Nested nested vector #4: Apply predicates"]
  
  (prettyfy-form-prettyfy-eval "(len-3? [42 [\"abc\" [22/7]]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(len-2? [\"abc\" [22/7]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(len-1? [22/7])"))

 
 (panel
  [:h3 "Collection validation: nested nested vector"]
  
  (prettyfy-form-prettyfy-eval "(validate-collections [42 [\"abc\" [22/7]]] [len-3? [len-2? [len-1?]]])" 50 80)

  [:div.note
   [:p "Data, upper row; specification, lower row. Same result as manual algorithm. Three predicate+collection pairs, three validation results. `len-3?` again wasn't satisfied. Other two were."]])


 (panel
  [:h3 "Flat map: data, predicate, & specification"]

  [:table
   [:tr
    [:td "data"]
    [:td
     [:pre [:code "{:x 42}"]]]]

   [:tr
    [:td "predicate"]
    [:td [:pred [:code "map?"]]]]

   [:tr
    [:td [:div.vspace]]]

   [:tr
    [:td "specification"]
    [:td
     [:pre [:code "{:x 42    } ;; copy-paste data"]]
     [:pre [:code "{         } ;; remove scalars"]]
     [:pre [:code "{map?     } ;; => java.lang.RuntimeException..."]]
     [:pre [:code "{:foo map?} ;; insert predicate"]]]]]
  
  [:div.note])


 (panel
  [:h3 "Flat map #1: Enumerate paths"]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths {:x 42})")]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths {:foo map?})")]]

  [:div.note])


 (panel
  [:h3 "Flat map #2: Keep only…"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "…collections from the data"]
    [:pre [:code "[{:path [], :value {:x 42}}]"]]]
   [:div.side-by-side
    [:div "…predicates from the specification"]
    [:pre [:code "[{:path [:foo], :value map?}]"]]]]

  [:div.note])


 (panel
  [:h3 "Flat map #3: Pair collections with predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "paths of collections in data"]
    [:pre [:code "[]"]]]
   [:div.side-by-side
    [:div "path of predicate in specification"]
    (prettyfy-form-prettyfy-eval "(drop-last [:foo])")
    [:div.vspace]    
    (prettyfy-form-prettyfy-eval "(= [] (drop-last [:foo]))")]]
  
  [:div.note])


 (panel
  [:h3 "Flat map #4: Apply predicates"]

  (prettyfy-form-prettyfy-eval "(map? {:x 42})")
  
  [:div.note])


 (panel
  [:h3 "Collection validation: flat map"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42} {:foo map?})" 40 80)

  [:div.note])


 (panel
  [:h3 "Predicate's key itself is irrelevant (mostly)"]

  [:pre
   [:code "(drop-last [:foo]) ;; => ()"]
   [:br]
   [:code "(drop-last [:bar]) ;; => ()"]
   [:br]
   [:code "(drop-last [:baz]) ;; => ()"]]

  [:div.vspace]
  
  [:p "But, don't use a key that appears in the data!"]

  [:div.note

   [:p "The next example shows why not."]])


 (panel
  [:h3 "Nested map: data, predicate, & specification (non-ideal specification)"]

  [:table
   [:tr
    [:td "data"]
    [:td
     [:pre [:code "{:x 42 :y {:z \"abc\"}}"]]]]

   [:tr
    [:td "predicate"]
    [:td [:pred [:code "map?"]]]]

   [:tr
    [:td [:div.vspace]]]

   [:tr
    [:td "specification"]
    [:td
     [:pre [:code "{:x 42 :y {:z \"abc\"}} ;; copy-paste data"]]
     [:pre [:code "{      :y           } ;; remove scalars"]]
     [:pre [:code "{      :y map?      } ;; insert predicate"]]]]]
  
  [:div.note])

 (panel
  [:h3 "Nested map #1: Enumerate paths (non-ideal specification)"]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths {:x 42 :y {:z \"abc\"}})" 55 55)]
   [:div.side-by-side
    [:div.vspace]
    [:div.vspace]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths {:y map?})")]]
  
  [:div.note])

 (panel
  [:h3 "Nested map #2: Keep only… (non-ideal specification)"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "…collections from the data"]
    [:pre [:code "[{:path [], :value {:x 42, :y {:z \"abc\"}}}\n {:path [:y], :value {:z \"abc\"}}]"]]]
   [:div.side-by-side
    [:div.vspace]
    [:div.vspace]
    [:div.vspace]
    [:div "…predicates from the specification"]
    [:pre [:code "[{:path [:y], :value map?}]"]]]]

  [:div.note])

 (panel
  [:h3 "Nested map #3: Pair collections with predicates (non-ideal specification)"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "paths of collections in data"]
    [:pre [:code "[]"]]
    [:pre [:code "[:y]"]]]
   [:div.side-by-side
    [:div "path of predicate in specification"]
    (prettyfy-form-prettyfy-eval "(drop-last [:y])")
    [:div.vspace]    
    (prettyfy-form-prettyfy-eval "(= [] (drop-last [:y]))")]]

  [:div.vspace]
  
  [:p "Motto #3: Ignore un-paired collections and un-paired predicates."]
  
  [:div.note])


 (panel
  [:h3 "Nested map #4: Apply predicates (non-ideal specification)"]

  (prettyfy-form-prettyfy-eval "(map? {:x 42 :y {:z \"abc\"}})")

  [:div.note])


 (panel
  [:h3 "Collection validation: nested map (non-ideal specification)"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42 :y {:z \"abc\"}} {:y map?})" 45 80)

  [:div.vspace]
  
  [:p "What if we'd wanted to validate " [:code "{:z \"abc\"}"] "?"]
  
  [:div.note])


 (panel
  [:h3 "Nested map, better specification"]

  [:table
   [:tr
    [:td "data"]
    [:td
     [:pre [:code "{:x 42 :y {:z \"abc\"}}"]]]]

   [:tr
    [:td "predicate"]
    [:td [:pred [:code "map?"]]]]

   [:tr
    [:td [:div.vspace]]]

   [:tr
    [:td "specification"]
    [:td
     [:pre [:code "{:x 42 :y     {:z \"abc\" }} ;; copy-paste data"]]
     [:pre [:code "{             {         }} ;; remove scalars"]]
     [:pre [:code "{:foo map? :y {:bar set?}} ;; insert predicate"]]]]

   ]
  
  )


 (panel
  [:h3 "Nested map, better specification"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:div "Paths of collections"]
    [:pre
     [:code "[]"]
     [:br]
     [:code "[:y]"]]]

   [:div.side-by-side
    [:div "Paths of predicates"]
    [:pre [:code "[:foo]"]]
    (prettyfy-form-prettyfy-eval "(drop-last [:foo])")

    [:div.vspace]
    
    [:pre [:code "[:y :bar]"]]
    (prettyfy-form-prettyfy-eval "(drop-last [:y :bar])")]]

  [:pre
   (prettyfy-form-prettyfy-eval "(map? {:x 42 :y {:z \"abc\"}})")
   [:br]
   (prettyfy-form-prettyfy-eval "(set? {:z \"abc\"})")]

  [:div.note])


 (panel
  [:h3 "Validating nested map, better specification"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42 :y {:z \"abc\"}} {:foo map? :y {:bar set?}})" 55 55)

  [:div.note])


 (panel
  [:h3 "Using informative keywords in specifications"]

  [:pre
   [:code "{:foo       map? :y {:bar       set?}}"]
   [:br]
   [:code "{:is-a-map? map? :y {:is-a-set? set?}}"]]

  [:div.vspace]
  
  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42 :y {:z \"abc\"}} {:is-a-map? map? :y {:is-a-set? set?}})"))


 (panel
  [:h3 "Using mis-leading keywords in specifications"]
  
  [:pre
   [:code "{:is-a-map? map?   }"]
   [:br]
   [:code "{:is-a-map? " [:strong "vector?"] "}"]]

  [:div.vspace]
  
  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42} {:is-a-map? vector?})" 45 45)

  [:div.note])


 
 




 (panel [:hr] [:hr] [:hr])

 (panel
  [:h3 "Collection validation, first attempt."]

  (prettyfy-form-prettyfy-eval "(def len-3? #(= 3 (count %)))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(len-3? [42 \"abc\" 22/7])")

  [:div.note
   [:p "We could try to write a specification like this: our `len-3?` predicate returns `true` if the collection has exactly three elements. Validation merely applies the predicate to the whole collection."]

   [:p "Okay, that scenario maybe kinda could work. But what about this scenario?"]])


 (panel
  [:h3 "Collection Validation, first attempt."]

  [:p [:em "This fn doesn't actually exist"]]

  [:pre [:code "(imaginary-validate-collection [42 'foo \\z]\n                               len-3?)\n;; => okay"]]

  [:div.note
   [:p "Then we could imagine some function might do this."]])


 (panel
  [:h3 "Collection validation, first attempt problem."]

  (prettyfy-form-prettyfy-eval "(all-paths [11 [22 33 44]])")

  [:div.vspace]

  [:pre [:code "(imaginary-validate-collection [42 'foo \\z]\n                               ???)"]]

  [:div.note
   [:p [:em "A three-element vector nested within a two-element vector"] ". The paths would look like this."]

   [:p "Oh. Still ignoring the scalars, there are now two vectors which would be targets for our predicate, one at the root, and one at path " [:code "[1]"] ". We can't merely supply a pair of bare predicates to our " [:code "imaginary-validate-collection"] " function and have it magically know how to apply the predicates to the correct vector."]])


 (panel
  [:h3 "Collection Validation, solving the problem with the Mottos."]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "[11 [22 33 44]]"]]]]

   [:tr
    [:td "template"]
    [:td [:pre [:code "[   [        ]]"]]]]

   [:tr
    [:td "specification"]
    [:td [:pre [:code "[len-3? [len-3?]]"]]]]]

  [:div.note
   [:p "It quickly becomes apparent that we need to somehow arrange our collection predicates inside some kind of structure that will instruct the validation function where to apply the predicates. One of Speculoos' principles is " [:em "Make the specification shaped like the data"] ". Let me propose this structure."]])


 (panel
  [:h3 "All-paths of the collection specification."]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [11 [22 33 44]])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [len-3? [len-3?]])")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(drop-last [1 0])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")]]

  [:div.note
   [:p "What do the paths of that thing look like?" "Hmm. In the previous scalar-validation screencast, when we were validating scalars, we followed the principle that validation only proceeds when a predicate in the specification shares the exact path as the scalar in the data. However, we can now see an issue if we try to apply that principle here. The nested vector of the data is located at path " [:code "[1]"] ". The nested predicate in the specification is located at path " [:code "[1 0]"] ", nearly same except for the trailing " [:code "0"] ". The root vector of the data is located at path " [:code "[]"] " while the predicate is located at path " [:code "[0]"] " of the specification, again, nearly the same except for the trailing zero. Clojure has a nice core function, `drop-last`, that performs that transformation."]])


 (panel
  [:h3 "Algorithm for validating collections."]

  [:ol
   [:li "Run " [:code "all-paths"] " on the data, then the specification."]
   [:li "Remove " [:em "scalar"] " elements from the data, keeping only the collection elements."]
   [:li "Remove " [:em "non-predicate"] " elements from the collection specification."]
   [:li "Pair predicates at path " [:code "pth"] " in the specification with collections at path " [:code "(drop-last pth)"] " in the data. Discard all other un-paired collections and un-paired predicates."]
   [:li "For each remaining collection+predicate pair, apply the predicate to the collection."]]

  [:div.note
   [:p "The slightly modified rule for validating collections is " [:em "Collection predicates in the specification are applied to the collection in the data that correspond to their parent."] " In other words, the predicate at path " [:code "pth"] " in the collection specification is applied to the collection at path " [:code "(drop-last pth)"] " in the data."]])


 (panel
  [:h3 "Manually validating nested collections."]

  [:div.side-by-side-container
   [:div.side-by-side
    "from data: only collections"
    [:pre [:code "[{:path [], :value [11 [22 33 44]]}\n {:path [1], :value [22 33 44]}]"]]]
   [:div.side-by-side
    "from specification: only predicates"
    [:pre [:code "[{:path [0], :value len-3?}\n {:path [1 0], :value len-3?}]"]]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1 0])")]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(len-3? [11 [22 33 44]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(len-3? [22 33 44])")

  [:div.note
   [:p "We've already run `all-paths` on the data (left) and the collection specification (right), now we filter each to keep only the collections and predicats, respectively. We form pairs by doing a `drop-last` on each of the predicates' paths."]

   [:p "The root vector `[11 [22 33 44]]` does not satisfy `len-3?`, but the nested vector `[22 33 44]` does."]])


 (panel
  [:h3 "Validate collections: simple vector"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [validate-collections]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections [11 22 33] [len-3?])" 50 80)

  [:div.note
   [:p "Speculoos provides a function, " [:code "validate-collections"] ", that does that for us. Let's see." "Much of that looks familiar. " [:code "validate-collections"] " returns a validation entry for each to the two collections+predicate pairs. The " [:code ":datum"] " stuff represent the things being tested and the " [:code ":predicate"] "s report the predicate functions, and similarly, " [:code "valid?"] " reports whether that predicate was satisfied."]

   [:p "There are now three things that involve the concept of a path: the `len-3?` collection predicate was found at " [:code ":path-predicate"] " in the specification and the datum was found at " [:code ":path-datum"] " in the data. Notice that the value associated to :path-datum is the same as the `drop-last` of :path-predicate. That means that `validate-collections` paired the root collection to the `len-3?` predicate. (We'll explain the term :ordinal-path-datum keyword as the discussion progresses.) In this example, the the root vector contains only three elements, and thus satisfies its collection predicate `len-3?`."]

   [:p ""]])


 (panel
  [:h3 "Validate collections: nested vector"]

  (prettyfy-form-prettyfy-eval "(validate-collections [11 [22 33 44]] [len-3? [len-3?]])" 50 80)

  [:div.note
   [:p "In this example we've nested a vector within the outer root vector. Two collections, two predicates. The nested vector [22 33] contains three elements, so its predicate was satisfied, while the root vector contains only two elements, and thus failed to satisfy its predicate."]])


 (panel
  [:h3 "Validating collections: maps."]

  (prettyfy-form-prettyfy-eval "(all-paths {:x 11 :y {:z 22}})")

  [:div.note
   [:p "Let's take a look at validating nested maps. Here are the paths of some example data, a map nested in another map, along with a couple of scalars."]

   [:p "Two scalars, which " [:code "validate-collections"] " ignores, and two collections. Let's apply our rule: the predicate in the specification applies to the collection in the data whose path is one element shorter. The two collections are located at paths " [:code "[]"] " and " [:code "[:y]"] "."]])


 (panel
  [:h3 "Validating maps: naive collection specification."]

  [:code "{map? {map?}}"]

  [:div.note
   [:p "To write a collection specification, we'd mimic the shape of the data, inserting predicates that apply to the parent. We can't simply write {map?} because maps must contain an even number of forms. So we're going to need to add some keys in there. Technically, you could key your collection predicates however you want, but I strongly recommend choosing a key that doesn't appear in the data. This example shows why. We could put a predicate at key " [:code ":y"] " of the specification, and " [:code "validate-collections"] " will merrily chug along."]])


 (panel
  [:h3 "Validating maps: somewhat better collection specification."]

  [:code "{:y map?}"]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 11 :y {:z 22}} {:y map?})" 45 80)

  [:div.note
   [:p "We can see that the singular " [:code "map?"] " predicate located at specification path " [:code "[:y]"] " was indeed applied to the root container at data path " [:code "(drop-last [:y])"] " which evaluates to path " [:code "[]"] ". But now we've consumed that key, and it cannot be used to target the nested map " [:code "{:z 22}"] " at " [:code "[:y]"] " in the data."]])


 (panel
  [:h3 "Validating maps: even better collection specification with a sham key."]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 11 :y {:z 22}} {:is-a-map? map? :y {:is-a-set? set?}})")

  [:div.note
   [:p "If we had instead invented a synthetic key, " [:code "drop-last"] " would trim it off the right end and the predicate would still be applied to the root container, while key " [:code ":y"] " remains available to target the nested map. In practice, I like to invent keys that are descriptive of the predicate so the validation results are easier to scan by eye."]

   [:p "Notice that " [:code "validate-collections"] " completely ignored the scalars " [:code "11"] " and " [:code "22"] " at data keys " [:code ":x"] " and " [:code ":z"] ". It only applied predicate " [:code "map?"] " to the root of data and predicate " [:code "set?"] " to the nested map at key " [:code ":y"] ", which failed to satisfy."]])


 (panel
  [:h3 "Validating maps: irrelevance of " [:strong "collection predicate's"] " sham key."]

  [:p [:code ":is-a-map?"] " keyword gives the wrong impression"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 11} {:is-a-map? vector?})" 50 80)

  [:div.note
   [:p "Let me emphasize: within a collection specification, the name of the predicate keys targeting a nested map have " [:em "absolutely no bearing on the operation of the validation"] ". They get truncated by the " [:code "drop-last"] " operation. We could have used something misleading like this. Despite the key suggesting that we're testing for a map, the actual predicate tests for a vector, and returns " [:code "false"] "."]])


 (panel
  [:h3 "Multiple collection predicates for a sequential."]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(validate-collections [42] [vector? map?])" 40 80)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [1])")]]

  [:div.note
   [:p "Here's something curious. If we focus on the paths of the two predicates in the specification, we see that both " [:code "vector?"] " and " [:code "map?"] " target the root container because " [:code "(drop-last [0])"] " and " [:code "(drop-last [1])"] " both evaluate to the same path in the data. So we have another consideration: " [:em "Every"] " predicate in a specification's collection applies to the parent collection in the data. This means that we can apply an unlimited number of predicates to each collection."]])


 (panel
  [:h3 "Many collection predicates for a sequential."]

  [:div.note
   [:p "Any number of collection predicates may apply to the parent collection, because their paths all `drop-last` evaluate to the parent's path. Here, we see all five collection predicates apply to the parent vector."]

   [:p "If " [:strong "any"] " number of predicates apply to the parent collection, there might be zero to infinity predicates before we encounter a nested collection in that sequence. How, then, does " [:code "validate-collections"] " determine where to apply the predicate inside a nested collection?"]]

  (prettyfy-form-prettyfy-eval "(validate-collections [42] [vector? map? list? set? coll?])" 55 80))


 (panel
  [:h3 "Collection predicates to nested collections."]

  [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

  [:pre [:code "[{     }    (       )    #{  }]"]]

  [:div.note
   [:p "The rule " [:code "validate-collections"] " follows is " [:em "Apply nested collection predicates in the order which they appear, ignoring scalars."] " Let's see that in action. First, we'll make some example data composed of a parent vector, containing a nested map, list, and set, with a couple of interleaved integers."]

   [:p "Now we need to compose a collection specification. Motto #2 reminds us to make the specification mimic the shape of the data. I'm going to copy-paste the data and mash the delete key to remove the scalar datums. Just to emphasize how they align, here are the data (top) and the collection specification (bottom) with some spaced formatting."]])


 (panel
  [:h3 "A minimally-competent collection specification."]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{}()#{}])" 60 60)

  [:div.note
   [:p "The first thing to note is that our collection specification looks a lot like our data with all the scalars removed. The second thing to notice is that even though it contains zero predicates, that's a legitimate collection specification which " [:code "validate-collections"] " can consume. Check this out. Validation ignores collections in the data that are not paired with a predicate in the specification."]

   [:p "Remember: A specification says 'Thing may, or may not exist, but if it does, it must satisfy this predicate.' `valid` means 'zero un-satisfied predicates'. This validation recognizes zero collection+predicate pairs, so there aren't any un-satisfied predicates."]])


 (panel
  [:h3 "Validating nested collections #1"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} (list list?) #{}])" 55 80)

  [:div.note
   [:p "Okay, let's add a predicate. Let's specify that the second nested collection is a list. Predicates apply to their container, so we'll insert " [:code "list?"] " into the corresponding collection."]

   [:p "One predicate in the specification pairs with one collection in the data, so we receive one validation result. The " [:code "list?"] " predicate at path " [:code "[1 0]"] " in the specification was applied to the collection located at path " [:code "[2]"] " in the data. That nested collection is indeed a list, so " [:code ":valid?"] " is " [:code "true"] "." ]

   [:p "Notice how " [:code "validate-collections"] " did some tedious and boring calculations to achieve the general effect of " [:em "The predicate in the second nested collection of the specification applies to the second nested collection of the data."] " It kinda skipped over that " [:code "22"] " because it ignores scalars, and we're validating collections."]])


 (panel
  [:h3 "Validating nested collections #2"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{set?}])" 55 80)

  [:div.note
   [:p "Let's clear the slate and specify that nested set at the third position. One predicate applied to one collection, one validation result. And again, collection validation skipped right over the intervening scalars " [:code "22"] " and " [:code "44"] " in the data. " [:code "validate-collections"] " applied the predicate in the specification's third nested collection to the data's third nested collection."]])


 (panel
  [:h3 "Validating nested collections #3"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{:is-map? map?}()#{}])" 55 80)

  [:div.note
   [:p "We might as well specify that nested map in the first position. Recall that collection predicates targeting a map require a sham key. Removing the " [:code "set?"] " predicate from the previous example, we'll insert a " [:code "map?"] " predicate at a key in the specification that doesn't appear in the data's nested map. Unlike the previous two validations, " [:code "validate-collections"] " didn't have to skip over any scalars. It merely applied the predicate in the specification's first nested collection to the data's first nested collection, which is indeed a map."]])


 (panel
  [:h3 "Validating nested collections #4"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])" 55 80)

  [:div.note
   [:p "We've now seen how to specify and validate each of those three nested collections, so for completeness' sake, let's specify the root. Predicates apply to their container, so for clarity, we'll insert it at the beginning, but we could interleave it anywhere at the first level of depth in the specification."]])


 (panel
  [:h3 "Arranging collection predicates in a sequential specification."]

  [:pre
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])"]
   [:br]
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} vector? () #{}])"]
   [:br]
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () vector? #{}])"]
   [:br]
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{} vector?])"]]

  [:div.note
   [:p "Technically, we could put that particular predicate anywhere in the top-level vector as long " [:code "(drop-last path)"] " evaluates to " [:code "[]"] ". All the following yield substantially the same results. In practice, I find it visually clearer to insert the predicates at the front."]])


 (panel
  [:h3 "All-up nested collection validation"]
  [:div.note
   [:p [:code "validate-collections"] " applied to the data's root four predicates — " [:code "vector?"] ", "  [:code "sequential?"] ", "  [:code "coll?"] ", and "  [:code "any?"] " — which we interleaved among the nested collections. In addition, it validated each of the three nested collections, skipping over the intervening scalars."]]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {:is-map? map?} sequential? (list list?) coll? #{set?} any?])" 95 100))


 (panel
  [:h3 "Specifying collections nested in a parent map"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a [99] :b (list 77)}"]]]]

   [:tr
    [:td "template"]
    [:td [:pre [:code "{:a [  ] :b (list   )}"]]]]

   [:tr
    [:td "specification"]
    [:td [:pre [:code "{:a [vector?] :b (list list?)}"]]]]]

  [:div.note
   [:p "Collections nested within a map do not involve that kind of skipping because they're not sequential. Let's make this top row our example data."]

   [:p "Now, we copy-paste the data into the middle row, then delete the scalars. That becomes the template for our collection specification."]

   [:p "Let's pretend we want to specify something about those two nested collections at keys " [:code ":a"]  " and " [:code ":b"] ". We stuff the predicates " [:em "directly inside those collections"] ". This becomes our collection specification. Let's see what happens."]])


 (panel
  [:h3 "Validating collections nested in a parent map"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?)})" 55 80)

  [:div.note
   [:p "Checklist time."]
   [:ul
    [:li "Specification shape mimics data? " [:em "Check."]]
    [:li "Validating collections, ignoring scalars? " [:em "Check."]]
    [:li "Two paired predicates, two validations? " [:em "Check."]]]

   [:p "There's a subtlety to pay attention to: the " [:code "vector?"] " and " [:code "list?"] " predicates are contained within a vector and list, respectively. Those two predicates apply to their " [:em "immediate"] " parent container. " [:code "validate-collections"] " needs those " [:code ":a"] " and " [:code ":b"] " keys to find that vector and that list. You only use a sham key when validating the map immediately above your head."]])


 (panel
  [:h3 "Validating nested maps: When to use a sham key #1"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?) :howdy map?})" 75 90)

  [:div.note
   [:p "Let's re-use that validation and tack on a sham key with a predicate aimed at the root map. We've got the vector and list validations as before, and then, at the end, we see that " [:code "map?"] " at the sham " [:code ":howdy"] " key was applied to the root."]])


 (panel
  [:h3 "Validating nested maps: When to use a sham key #2"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a [99] :b (list 77)}"]]]]

   [:tr
    [:td "template"]
    [:td [:pre [:code "{:a [  ] :b (list   )}"]]]]

   [:tr
    [:td "remove stuff"]
    [:td [:pre [:code "{:a [  ]             }"]]]]

   [:tr
    [:td "add predicate"]
    [:td [:pre [:code "{:a [vector?]        }"]]]]

   [:tr
    [:td "specification"]
    [:td [:pre [:code "{:a [vector?] :flamingo [coll?]}"]]]]]

  [:div.note
   [:p "One more example to illustrate this point. Again, here's our data in the top row. And again, we'll copy-paste the data, then delete the scalars. That'll be our template for our collection specification."]

   [:p "Now, we'll go even further and delete the " [:code ":b"] " key and its value, the nested list. Without :b, we won't be able to validate the list."]

   [:p "Next, we'll insert predicate " [:code "vector?"] ". That predicate is paired with its immediate parent vector, so we need to keep the " [:code ":a"] " key."]

   [:p "Finally, we'll add in a wholly different key, with a " [:code "coll?"] " predicate nested in a collection at the new key."]

   [:p "Test yourself: How many validations will occur? Keep in mind, we have two collection predicates."]])


 (panel
  [:h3 "Validating nested maps: When to use a sham key #3"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :flamingo [coll?]})" 60 90)

  [:div.note
   [:p "I asked a misleading question. In this example, there is only one predicate+collection pair. " [:code "vector?"] " applies to the vector at " [:code ":a"] "."]

   [:p "We might have expected " [:code "coll?"] " to be applied somewhere because " [:code ":flamingo"] " doesn't appear in the map, but notice that " [:code "coll?"] " is " [:em "contained"] " in a vector. It would only ever apply to the thing that contained it. Since the data's root doesn't contain a collection at that key, the predicate is unpaired, and thus ignored."]])


 (panel
  [:h3 "Validating nested maps: When to use a sham key #4"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :emu coll?})" 65 80)

  [:div.note
   [:p "If we wanted to apply " [:code "coll?"] " to the root, we peel off its immediate container. " [:em "Now, "] [:code "coll?"] "'s immediate container is the root. Since it is now properly paired with a collection, it participates in validation."]])


 (whats-next-panel
  collection-validation-index
  [:div.note "There will be more utilities for validating collections in upcoming screencasts..."])
  ]