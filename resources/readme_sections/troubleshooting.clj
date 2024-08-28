[:section#troubleshooting
 [:h2 "Troubleshooting"]
 [:p "If you see surprising results, try these ideas."]
 [:ul
  [:li
   [:p "Remember the Mantras, and follow them."
    [:ol
     [:li [:strong "Validate scalars separately from validating collections."]

      [:p "You should never have a collection predicate like " [:code "vector?"] " in a scalar specification. Similarly, scalar predicates like " [:code "int?"] " should only appear in a collection specification in the context of testing a collection, like…"]

      [:pre [:code "(defn all-ints? [v] (every? #(int? %) v))"]]

      [:p "…or when validating some relationship " [:em "between"] " datums, like this."]

      [:pre [:code "(defn b-greater-than-a? [m] (< (m :a) (m :b)))"]]

      [:p "The function names " [:code "validate-scalars"] ", " [:code "validate-collections"] ", et. al., are strong beacons to remind you that you're either validating scalars, or validating collections."]]

     [:li [:strong "Make the specification mimic the shape of the data."]

      [:p "The speculoos functions don't enforce any requirements on the data and specification. If you feed it data that's a map and a specification that's a vector, it will dutifully try to validate what it has."]

      [:pre
       (print-form-then-eval "(validate-scalars {:a 99} [int?])" 30 80)
       [:br]
       [:br]
       [:code ";; No error nor exception with map data and vector specification"]]

      [:p "One word of warning: Because sequential things are indexed by integers, and map elements may also be indexed by integers, you could certainly abuse that flexibility like this."]

      [:pre
       [:code ";; data is a vector, specification is a map keyed with integers"]
       [:br]
       [:br]
       (print-form-then-eval "(validate-scalars [42 \"abc\" 22/7] {0 int? 1 string? 2 ratio?})" 50 40)]

      [:p "Speculoos merely knows that it could successfully locate " [:code "42"] " and " [:code "int?"] " at " [:code "0"] ", etc. It 'worked' in this instance, but surprise lurks if you try to get to clever."]]

     [:li [:strong "Validation ignores un-paired predicates and un-paired datums."]

      [:p "A decent number of surprsing validations result from predicates pairing to unexpected datums or not being paired at all."]

      [:pre
       [:code ";; Oops! specification contains un-paired key :c; string \"abc\" isn't validated"]
       [:br]
       (print-form-then-eval "(valid-scalars? {:a 42 :b \"abc\"} {:a int? :c symbol?})" 40 80)
       [:br]
       [:br]
       [:br ";; Oops! specification uses an extra level of nesting; [33] wasn't validated"]
       (print-form-then-eval "(validate-collections [11 [22 [33]]] [[[[list?]]]])")]

      [:p "Corollary: " [:strong [:code "valid?"] " being " [:code "true"] " means there were zero non-true results."] " If the validation did not find any predicate+datum pairs, there would be zero invalid results, and thus return valid. Use the " [:code "thorough-…"] " function variants to require all datums to be validated."]

      [:p "See below for strategies and tools for diagnosing mis-pairing."]]]]]

  [:li
   [:p "Speculoos specifications are regular old data structures containing regular old functions. (I assume your data is, too.) If you're wrangling with something deep down in some nested mess, use your Clojure powers to dive in and pull out the relevant pieces."]

   [:pre (print-form-then-eval "(let [data (get-in {:a {:b {:c [22/7]}}} [:a :b :c])
                                        spec (get-in {:a {:b {:c [int?]}}} [:a :b :c])]
                                    (validate-scalars data spec))" 75 40)]]

  [:li [:p "Use the verbose functions. If you're using the high-level " [:code "valid-…?"] " function variants, you'll only see " [:code "true/false"] ", which isn't helpful when troubleshooting. The " [:code "validate-…"] " variants are chatty and will display everything it considered during validation."]]

  [:li [:p "The " [:a {:href "https://blosavio.github.io/speculoos/speculoos.utility.html"} [:code "speculoos.utility"]] " namespace provides many functions for creating, viewing, analyzing, and modifying both scalar and collection specifications."]]

  [:li[:p "When the going really gets tough, break out " [:code "speculoos.core/all-paths"] " and apply it to your data, then to your specification, and then step through the validtion with your eyes."]

   [:pre
    (print-form-then-eval "(all-paths {:a [99]})")
    [:br]
    [:br]
    (print-form-then-eval "(all-paths {:a 'int?})" 40 50)
    [:br]
    [:br]
    [:code ";; Aha! The predicate int? at path [:a] and the integer 99 at path [:a 0] do not share a path!"]]]

  ]

 [:p "Finally, if you hit a wall, file a " [:a {:href "https://github.com/blosavio/speculoos/issues"} "bug report"] " or " [:a {:href "https://github.com/blosavio"} " email me"] "."]]