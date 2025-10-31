(require '[screedcast.core :refer [panel
                                   prettyfy-form-prettyfy-eval
                                   screencast-title
                                   whats-next-panel]])


(def intro-index 0)


[:body
 (panel
  (screencast-title intro-index "Introduction")
  [:h4.subtitle [:em "An experiment with Clojure specification literals"]]

  [:div.side-by-side-container
   [:div.side-by-side [:pre [:code "[int? string? ratio?]"]]]
   [:div.side-by-side [:pre [:code "{:id int? :food string? :dept keyword?}"]]]]

  [:div.note
   [:p "Speculoos is an experimental Clojure library that tries to do the same tasks as spec.alpha. Basically, it's a utility that answers the question " [:em "Does our data look the way we expect it to?"]]


   [:p "Imagine we'd like to know " [:em "Does our vector contains an integer, then a string, and finally a ratio?"] " The way to express that question is this. There's nothing new there. No new object type, or anything like that. It's a bog-standard Clojure vector containing three standard predicate functions, `int?`, `string?`, and `ratio?`. That's a Speculoos specification!"]

   [:p "This map on the right is also a Speculoos specification. It asks the question " [:em "Is the `:ID` an integer, is `:food` a string, and is the value associated to `:dept` a keyword."]]

   [:p "Again, nothing is new. Merely a Clojure map containing predicate functions."]])


 (panel
  [:h3 "Specifications are shaped like the data"]

  [:pre [:code "[42   \"abc\"   22/7  ]"]]
  [:pre [:code "[int? string? ratio?]"]]

  [:div.note
   [:p "One of Speculoos' core principles is 'specifications are shaped like the data.' Here, I've lined up that previous vector underneath another vector that contains an integer 42, a string 'abc', and a ratio 22/7. The specification on the lower line is *shaped* like that data: Each element lines up with a predicate."]])


 (panel
  [:h3 "Speculoos can validate with that specification vector."]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-scalars?]])")
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 45 45)

  [:div.note
   [:p "Speculoos can validate our data vector, on the upper line, with that specification vector, on the lower line. Validation systematically applies each predicate to its corresponding scalar. 42 is an integer, 'abc' is a string, and 22/7 is a ratio, so the validation returns 'true'. What about maps?"]])



 (panel
  [:h3 "Map specification are shaped like the data."]

  [:pre [:code "{:id 7    :food \"ice cream\" :dept :dairy  }"]]
  [:pre [:code "{:id int? :food string?     :dept keyword?}"]]

  [:div.note
   [:p "The upper map is our data containing scalars, while the lower map, which we saw on an earlier screen, is our specification containing predicates. Each element in the upper map *can* be paired with a predicate in the lower map. 7 with `int?`, 'ice cream' with `string?`, and :dairy with `keyword?`."]

   [:p "The 'shape' is similar, in that they're the same kind of collection and the maps contain the same keys: :id, :food, and :dept."]])


 (panel
  [:h3 "Speculoos validates maps, too."]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:id 7 :food \"ice cream\" :dept :dairy} {:id int? :food string? :dept keyword?})" 65 80)

  [:div.note
   [:p "Speculoos validates our data map, on the upper line, with our specification map, on the lower line, by systematically applying each predicate in the specification to its corresponding scalar in the data."]

   [:p "7 is an integer, 'ice cream' is a string, and :dairy is indeed a keyword. All the corresponding predicates are satisfied, so the validation returns 'true'."]])


 (panel
  [:h3 "Any Clojure collection"]

  [:table
   [:tr
    [:td "Vectors"]
    [:td [:code "[int? string? ratio?]"]]]

   [:tr
    [:td "Maps"]
    [:td [:code "{:id int? :food string? :dept keyword?}"]]]

   [:tr
    [:td "Sequences"]
    [:td [:code "(repeat int?)"]]]

   [:tr
    [:td "Lists"]
    [:td [:code "(list symbol? int? string?)"]]]

   [:tr
    [:td "Sets"]
    [:td [:code "#{keyword?}"]]]]

  [:div.note
   [:p "Speculoos can handle *any* Clojure collection type, including sequences, lists, and sets. A specification for a vector is a vector. A specification for a map is a map. A specification for a list is a list. Etc."]])


 (panel
  [:h3 "Speculoos can validate any heterogeneous, arbitrarily-nested data structure."]

  [:pre [:code "{:first-name string?\n :last-name string?\n :phone int?\n :email string?\n :address {:street-name string?\n           :street-number [int? char?]\n           :zip-code int?\n           :city string?\n           :state keyword?}}"]]

  [:div.note
   [:p "And Speculoos is not limited to flat, one-level-deep collections. It can handle any depth of nesting, of any mixture of collection types. Here, we see a specification that includes a vector, nested in a map, nested in a map. But Speculoos can validate any heterogeneous, arbitrarily-nested data structure."]

   [:p "Because Speculoos specifications are plain Clojure collections, we gain lots of flexibility and control."]])


 (panel
  [:h3 "Flexible"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:p "Compose"]
    (prettyfy-form-prettyfy-eval "(concat [int? string?] [ratio?])" 25 45)]

   [:div.side-by-side
    [:p "Alter"]
    (prettyfy-form-prettyfy-eval "(assoc {:id int? :food string? :dept keyword?} :id number?)" 45 45)]]

  [:div.note
   [:p "Because they're plain Clojure collections, the specifications may be manipulated by any Clojure functions. At any time, we can put two specifications together with 'concat', or we could relax the 'id' requirement to accept any number type instead of a more restrictive integer. No macros required."]

   [:p "We can manipulate the specifications at any time, suited to that particular context. We could have two component specifications from two different sources that we put together. Or, maybe within a pipeline, for just that step in a pipeline, we'd like to relax our requirement..."]])


 (panel
  [:h3 "Permissive: validate only datums that are present"]

  [:pre [:code.form "(valid-scalars? [42 \"abc\" 22/7]\n                [int? string? ratio?])"] [:code.eval " ;; => true"]]
  [:div.vspace]
  [:pre [:code.form "(valid-scalars? [42 \"abc\"]\n                [int? string? ratio?])"] [:code.eval " ;; => true"]]
  [:div.vspace]
  [:pre [:code.form "(valid-scalars? [42]\n                [int? string? ratio?])"] [:code.eval " ;; => true"]]
  [:div.vspace]
  [:pre [:code.form "(valid-scalars? []\n                [int? string? ratio?])"] [:code.eval " ;; => true"]]

  [:div.note
   [:p "Speculoos seamlessly consumes partial data. If we have a larger specification, it'll validate only what's present. This feature allows us write a single specification and apply it accreting data."]
   [:p "We could compose one, single specification vector to validate what will eventually be three elements, but starts empty. As our pipeline adds elements, we can use that one specification at each step."]])


 (panel
  [:h3 "Permissive: validate with only predicates that are present"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:id 7 :food \"ice cream\" :dept :dairy} {:id int? :food string? :dept keyword?})" 65 80)
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(valid-scalars? {:id 7 :food \"ice cream\" :dept :dairy} {:id int? :food string?})" 65 80)
  [:div.vspace]
  (prettyfy-form-prettyfy-eval "(valid-scalars? {:id 7 :food \"ice cream\" :dept :dairy} {:id int?})" 65 80)

  [:div.note
   [:p "In the other direction, Speculoos does not require us to validate all the data we have. We could have 'extra' data that we're merely passing through. It will happily validate using only the predicates we give it."]

   [:p "If we specify three key-values, it'll validate all three. If we specify only two, or only one, only those scalars are validated."]

   [:p "You might think this is weird, but there's a lot utility comes from this behavior. Particularly, it allows us pass through datums we'd prefer to ignore. Maybe at the moment, we only care that the :id is an integer, and we merely don't care about :food or :dept."]

   [:p "And Speculoos has better ways of ensuring presence of datums."]])


 (panel
  [:h3 "Validate collections"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-collections?]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? [42 \"abc\" 22/7 :foo] [#(= 3 (count %))])" 45 45)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? {:id 7 :food \"ice cream\" :dept :dairy} {:baz #(contains? % :id)})" 50 45)

  [:div.note
   [:p "The first core principle was 'specifications are shaped like the data'."]

   [:p "Another core principle is that Speculoos strictly distinguishes validating scalars (like numbers and strings) versus validating the properties of the collections themselves, such as their length (oh, no, our vector isn't three long) or whether they contain an element (such as, our ice-cream map does not contain something about a violin). Because of this separation, the predicates are simpler, the mental concept is cleaner, and the validations are correctly compartmentalized: validating the properties of a string datum is a completely separate concern from whether that string exists in the data."]

   [:p "The collection specification pattern is a little bit different than the scalar, but not too much. More details in a later screencast on the subject."]])


 (panel
  [:h3 "Niceties"]

  [:p "Predicate-like things"]

  (prettyfy-form-prettyfy-eval "(re-find #\"a.[cde]\" \"abc\")")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(#{1 5 7} 7)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [int? #\"a.[cde]\" #{1 5 7}])" 55 55)

  [:div.note
   [:p "I've tried to make Speculoos pleasant to use. I hope you appreciate composing and manipulating specifications with plain Clojure data structures."]

   [:p "I've also tried to make specifications expressive. Speculoos can seamlessly consume predicate-like things, such as regular expressions (to validate string properties) and sets (to validate membership in a set). If we stuff those into a scalar specification, validation works just as we'd expect. 42 is an integer, 'abc' satisfies a-something-C-or-D-or-E, and 7 is a member of this set."]

   [:p "Speculoos also has utilities to write and inspect specifications, generate samples, and exercise functions..."]])


 (panel
  [:h3 "Validate functions: arguments, return values, and argument-return relationships"]

  (prettyfy-form-prettyfy-eval "(inc 99)")
  [:pre
   [:code.form "(inc \"abc\")"]
   [:br]
   [:code.eval ";; => Unhandled java.lang.ClassCastException: class java.lang.String cannot be cast…"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.function-specs :refer [validate-fn-with]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-fn-with inc {:speculoos/arg-scalar-spec [int?]} \"abc\")")

  [:div.note
   [:p "One of spec.alpha's tent-pole features is specifying and validating function arguments and return values. Speculoos imitates those facilities."]

   [:p "Here we see `inc` bumps 99 to 100, but it chokes on a string. But if we pull in one of several function validators and specify our expectations for the argument, we can see why exactly."]

   [:p "Don't get bogged down in the weeds. Just know that Speculoos has those facilities."]])


 (whats-next-panel
  intro-index
  [:div.note
   [:p "I've created about a dozen-and-a-half screencasts as a follow-along companion the text documentation, which is the ReadMe. Next-Up is a discussion of Speculoos' mechanics: Knowing how Speculoos validates a HANDS greatly helps understanding how to use it."]])
 ]