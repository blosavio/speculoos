(panel
 [:h3 "★ Three Mottos"]

 [:ol.side-by-side-by-side-container

  [:div.side-by-side-by-side
   [:li "Validate scalars separately from validating collections."]
   [:div.vspace]
   [:p "scalars:"
    [:br]
    [:code "&emsp;&emsp;42, \"abc\", \\c, 22/7, :kw, 'foo, true, nil"]]
   [:div.vspace]
   [:p "collections:"
    [:br]
    [:code "&emsp;&emsp;[…], {…}, (…), #{…}"]]

   [:div.note
    [:p "Distinct functions for validating scalars and collections in HANDS. Advantages:"
     [:ol
      [:li "Simpler. No mini-language that mixes identities and quantities. Specs Cloj data structures w/functions. Manipulate specs w/anything, e.g. "[:code "assoc-in"] " No macros."]
      [:li "Mental clarity. Validation only applies to scalar, or to collection, never both. Predicate doesn't have to know anything about the quantity or location of the element."]
      [:li "Only specify as much, or as little, as necessary. If only a few scalars, won't be forced to specify a property concerning a collection."]]]
]]

  [:div.side-by-side-by-side
   [:li "Shape the specification to mimic the data."]
   [:div "data"]
   [:div "↓"]
   [:pre
    [:code "{:x 42   :y \"abc\"  }"]
    [:br]
    [:code "{:x int? :y string?}"]]
   [:div "↑"]
   [:div "specification"]

   [:div.note
    [:p "Composing specs straightforward; mimic shape of data. Arrangement of nested vectors, lists, maps, sequences, and sets that contain predicates. Pattern instruct the validation functions where to apply the predicates. Spec for a vector is a vector. Spec for a map, is a map. ~1-to-1 correspondence b/t shape of data and shape of specific. Strategy: copy-paste data, delete contents,  use as a template, replace elements with predicates. Peek at by eye — merely eval them at " [:span.small-caps "repl"] " — easy alteration: any Clojure data wrangling functions to tighten, relax, or remove portions of spec. " [:code "assoc-in"] ", " [:code "update-in"] ", & " [:code "dissoc"] "."]
]]

  [:div.side-by-side-by-side
   [:li "Ignore un-paired predicates and un-paired datums."]
   [:div "data"]
   [:div "↓"]
   [:pre
    [:code
     "{:x 42   "
     [:s ":y \"abc\""]
     "}"]
    [:br]
    [:code
     "{:x int? "
     [:s ":q double?"]
     "}"]]
   [:div "↑"]
   [:div "specification"]

   [:div.note
    [:p "Ignoring -> Flexibility, power, optionality, and re-usability. Ex #1: pipeline. Supplying predicates for subset of datums only validates those specified datums while being agnostic towards the other datums. Ex #2 Sprawling specification that describes a myriad of data about a person, postal address,  contact info, etc. B/c a spec just  data structure with regular predicates, can, on-the-fly, " [:code "get-in"] " portion relevant to postal addresses and apply that to our particular instances of address data. Specify exactly what elements we'd like to validate. No more, no less."]]]]

 [:div.note
  [:p "Three Mottos speaker notes"]])