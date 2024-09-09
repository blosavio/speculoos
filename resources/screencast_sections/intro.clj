[:body
 (panel
  [:h1 "Speculoos — Part 1"]
  [:h3 [:em "An experiment with Clojure specification literals"]]
  [:pre [:code "[42 \"abc\" 22/7]"]]
  [:p "https://github.com/blosavio/speculoos"]
  [:div.note  "Imagine you'd like to know if " [:em "My Clojure vector contains an integer, then a string, and finally a ratio"] ". One example of that data vector might look like this."])

 (panel
  [:h3 "Specifications shaped like the data?"]
  [:pre [:code "[42 \"abc\" 22/7]"]]
  [:pre [:code "[int? string? ratio?]"]]
  [:p.note "It would be nice if we could write a specification that is shaped like that data. Speculoos can validate our data vector with that specification vector."])

 (panel
  [:h3 "Speculoos can do that."]
  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-scalars?]])")
  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 45 45)
  [:div.note ""])

(panel
  [:h3 "In valid data."]
  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 :not-a-string 22/7] [int? string? ratio?])" 55 45)
  [:div.note [:code ":not-a-string"] " fails to satisfy the " [:code "string?"] " predicate it lines up with."])

(panel
  [:h3 "Detailed validation report."]
  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [validate-scalars]])")
  (prettyfy-form-prettyfy-eval "(validate-scalars [42 :not-a-string 22/7] [int? string? ratio?])" 55 45)
  [:div.note "Report is " [:code "get-in"] "-able. We'll discuss all this info in due time."])

 (panel
  [:h3 "Speculoos' Three Mantras"]
  [:ol
   [:li "Validate scalars separately from validating collections."]
   [:li "Shape the specification to mimic the data."]
   [:li "Ignore un-paired predicates and un-paired datums."]]
  [:div.note "Remember to mention..."])

 (panel
  [:h3 "What about maps?"]
  [:pre [:code "{:x 42 :y 22/7}"]]
  [:pre [:code "{:x int? :y ratio?}"]]
  [:div.note "Now imagine we'd like ensure we have " [:em "A Clojure hash-map with an integer at key " [:code ":x"] " and a ratio at key " [:code ":y"]] ". Something like this. We could write a specification map that's shaped like that data map."])

 (panel
  [:h3 "Speculoos can validate maps, too."]
  (prettyfy-form-prettyfy-eval "(valid-scalars? {:x 42 :y 22/7} {:x int? :y ratio?})" 40 80)
  [:div.note "Speculoos can validate our data map with that specification map. Notice how in both cases, the specifications mimic the shape of the data. The vector's specification is itself a vector. The map's specification is itself a map."])

 (panel
  [:h3 "Speculoos can validate any heterogeneous, arbitrarily-nested data structure."]

  [:pre [:code "[42 {:x \"abc\" :y 22/7}]"]]
  [:pre [:code "{:z 99 :q ['foo]}"]]
  [:pre [:code "(list 42 [\"abc\"] {:w 22/7})"]]
  [:pre [:code "[99 #{:chocolate :vanilla :strawberry}]"]]

  [:div.note
   [:ul
    [:li "Speculoos can validate any heterogeneous, arbitrarily-nested data collection using specifications composed of plain Clojure collections and functions. In short, Speculoos is an experimental library that aims to perform the same tasks as " [:a {:href "https://clojure.org/about/spec"} [:code "clojure.spec.alpha"]] " with an intuitive interface that employs flexible and powerful specification literals."]
    [:li "Don't worry about how to do all that. We only have to apply the three Matras. Everything else falls into place."]]])

 (panel
  [:h3 "Validate collections"]
  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-collections?]])")
  (prettyfy-form-prettyfy-eval "(defn length-3? [v] (= 3 (count v)))")
  (prettyfy-form-prettyfy-eval "(valid-collections? [42 \"abc\" 22/7] [length-3?])")
  (prettyfy-form-prettyfy-eval "(valid-collections? [42] [length-3?])")
  [:div.note "Validate scalars vs. validate collections. Specification pattern is a little bit different. More details later."])

 (panel
  [:h3 "Validate functions: arguments, return values, and argument-return relationships"]
  (prettyfy-form-prettyfy-eval "(require '[speculoos.function-specs :refer [validate-fn-with]])")
  (prettyfy-form-prettyfy-eval "(inc 99)")
  (prettyfy-form-prettyfy-eval "(validate-fn-with inc {:speculoos/arg-scalar-spec [int?]} 99)")
  (prettyfy-form-prettyfy-eval "(validate-fn-with inc {:speculoos/arg-scalar-spec [int?]} \"abc\")")
  [:div.note "Don't get bogged down in the weeds. Just know that Speculoos has some facilities."])

 (panel
  [:h3 "What's next."]
  [:code.eval "Last page code.eval..."]
  [:div.note "Last page's notes div..."])
 ]