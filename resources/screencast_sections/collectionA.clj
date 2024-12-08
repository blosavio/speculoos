(require '[screedcast.core :refer [panel
                                   prettyfy-form-prettyfy-eval
                                   screencast-title
                                   whats-next-panel]])


(def collection-validation-basic-index 3)


[:body

 (panel
  (screencast-title collection-validation-basic-index "Collection Validation: Basics")
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

  [:h4   [:em "Any"] " predicates apply to their immediate parent collection."]

  (prettyfy-form-prettyfy-eval "(valid-collections? [42 \"abc\" 22/7] [coll? vector? sequential?])" 50 30)

  [:div.note
   [:p "All three predicates are applied to the vector, not only the first one. Scalar validation: 1-to-1 predicate-to-scalar. Collection validation: many-to-1 predicate-to-coll."]

   [:p "Applying predicates to their parent collection allows us to write specifications whose shape mimic the shape of the data."]

   [:p "3 Mottos + targeting parent collection are an emergent property of the collection validation algorithm. If we understand the algorithm, we can write clear, correct, and expressive collection specifications."]])


 (panel
  [:h3 "General pattern of discussion"]

  [:p "A. Manual algorithm"]
  [:p "B. Automatic algorithm"])


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
   [:p "Perform algorithm with one invocation. Two pairs of predicates+collections, two validation entries. `len-3?` not satisfied, `len-2?` was satisfied."]])


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
    [:pre [:code "[1 1]"]]]
   [:div.side-by-side
    [:div "path of predicates in specification"]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1 0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1 1 0])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(= [] (drop-last [0]))")
    (prettyfy-form-prettyfy-eval "(= [1] (drop-last [1 0]))")
    (prettyfy-form-prettyfy-eval "(= [1 1] (drop-last [1 1 0]))")]]

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
    [:td [:pre [:code "map?"]]]]

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
    [:td [:pre [:code "map?"]]]]

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

  [:p "To specify that nested map, we'd need…"]

  (prettyfy-form-prettyfy-eval "(drop-last [:y :baz])")

  [:p "…but…"]

  [:pre [:code "{:y map? :y {:baz set?}} ;; => java.lang.IllegalArgumentException; Duplicate key: :y"]]

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
    [:td [:pre [:code "map?"]]]]

   [:tr
    [:td [:div.vspace]]]

   [:tr
    [:td "specification"]
    [:td
     [:pre [:code "{:x 42     :y {:z \"abc\" }} ;; copy-paste data"]]
     [:pre [:code "{          :y {         }} ;; remove scalars"]]
     [:pre [:code "{:foo map? :y {:bar set?}} ;; insert predicates"]]]]])


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

  [:div.vspace]

  [:p "Predicates at " [:code "[:foo]"] " and " [:code "[:y :bar]"] " don't interfere."]

  [:div.note])


 (panel
  [:h3 "Using informative keywords in specifications"]

  [:pre
   [:code "{:foo       map? :y {:bar       set?}}"]
   [:br]
   [:code "{:is-a-map? map? :y {:is-a-set? set?}}"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42 :y {:z \"abc\"}} {:is-a-map? map? :y {:is-a-set? set?}})")

  [:div.note
   [:p "The keywords don't have any functional affect, so we might name them something informative to a human. But beware!"]])


 (panel
  [:h3 "Using mis-leading keywords in specifications"]

  [:pre
   [:code "{:is-a-map? map?   }"]
   [:br]
   [:code "{:is-a-map? " [:strong "vector?"] "}"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections {:x 42} {:is-a-map? vector?})" 45 45)

  [:div.note
   [:p "If the key purports to carry meaning to a human, it might be _mis_leading."]

   [:p "Speculoos makes zero effort to inspect the key. In fact, it drops the key on the floor and ignores it."]

   [:p "It's our responsibility to write a proper predicate."]])


 (whats-next-panel
  collection-validation-basic-index
  [:div.note [:p "We've just now covered the basics of collection validation. The next screencast will explore some of the nuances of how predicates apply to their parent containers, and why 'any'-and-all predicates apply."]])
 ]
