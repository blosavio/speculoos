(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def collection-validation-extras-index 5)


[:body
 (panel
  (screencast-title collection-validation-extras-index "Collection Validation: Extras")

  [:p "Why collection validation algorithm must be different"]

  [:div.note
   [:p "Originally was going to be a catch-all to discuss several topics, but elsewhere, I explained all the other issues. So there's only this one for now."]])


 (panel
  [:h3 "Differences between algorithms"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:strong "Scalar validation"]
    [:ol
     [:li [:code "all-paths"] " the data & specification."]
     [:li "Keep only scalars from  data."]
     [:li "Keep only predicates from specification."]
     [:li [:strong "Pair predicates to scalars that share exact paths."]]
     [:li "Evaluate paired predicates+scalars."]]
     ]
   [:div.side-by-side
    [:strong "Collection validation"]
    [:ol
     [:li [:code "all-paths"] " the data & specification."]
     [:li "Keep only collection elements from data."]
     [:li "Keep only predicates from specification."]
     [:li [:strong "Pair predicates at " [:code "path"] " to collections at " [:code "(drop-last path)"] "."]]
     [:li "Evaluate paired predicates+collections."]]]]

  [:div.note
   [:p "Discard all other un-paired collections and un-paired predicates."]])


 (panel
  [:h3 "Why can't collection validation use the scalar validation algorithm?"]

  [:p [:em "Answer: "] "Predicate = function = scalar"]

  [:p "This works."]
  [:pre
   [:code "[42   [\"abc\"   [22/7  ]]] ;; data"]
   [:br]
   [:code "[int? [string? [ratio?]]] ;; scalar specification"]]

  [:div.vspace]

  [:p "This does not work, generally."]
  [:pre
   [:code "[42   [\"abc\"  [22/7]]] ;; data"]
   [:br]
   [:code "[:foo vector?        ] ;; un-usable collection specification"]]

  [:div.note
   [:p "Speculoos predicates are Clojure functions. A function is a scalar. Therefore, a Speculoos predicate is a scalar."]

   [:p "A predicate, being a scalar, can replace any scalar datum in a heterogeneous, arbitrarily-nested data structure. Another way of thinking about that, is that a scalar contained in a heterogeneous, arbitrarily nested data structure can occupy the exact same path as a predicate in a specification that mimics the shape of that structure. Predicate `int?` is located at index 0, same as scalar 42. Predicate `string?` is located at path [1 0], same as scalar \"abc\". And predicate `ratio?` is located at path [1 1 0], exactly the same as scalar 22/7. The predicates and scalars can be straightforwardly paired. That's how Speculoos performs scalar validation."]

   [:p "There is no such symmetry between a predicate and a collection. A predicate and a collection can utterly not share paths in a completely general manner. The nested vector and the predicate `vector?` both share path [1]. But since a predicate can not contain anything, any element contained in the nested vector can not be addressed."]
   [:p "Someone might try to imagine a way to arrange the collection predicates within a structure so that the paths would align, but then the shape of the specification would no longer mimic the shape of the data. Speculoos values that mimicry. It's straightforward and intuitive to compose a specification."]
])

 (panel
  [:h3 "Why not explicit paths?"]

  [:pre
   [:code ";; this fn doesn't actually exist"]
   [:br]
   [:br]
   [:code "(imaginary-validate-collection-2 [42 [\"abc\" [22/7]]]\n                                 len-3? [0]\n                                 len-2? [1 0]\n                                 len-1? [1 1 0])\n;; => true"]]

  [:div.vspace]

  [:p "…vs. Speculoos' specification literal."]

   [:pre [:code "[len-3? [len-2? [len-1?]]"]]

  [:div.note
   [:p "Someone may also try to imagine a new data type that is both a function and a collection, but then we'd lose the substantial advantages that a Speculoos predicate is just a Clojure function."]

   [:p "This manually serializes a nested data structure. Doesn't scale well: error-prone, not re-usable. Also, not readily apparent. The literal specification beneath is much more understandable at a glance. Each of the three predicates apply to their immediate parent collection. Done."]

   [:p " And it's manipulable by `assoc-in`, composable with `concat`, etc. And it's re-usable and version controllable because it's all in once tidy collection, not a loose bag of predicates and paths."]])


 (whats-next-panel
  collection-validation-extras-index
  [:div.note "There will be more utilities for validating collections in upcoming screencasts..."])
 ]
