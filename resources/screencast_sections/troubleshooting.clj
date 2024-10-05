(require '[speculoos-hiccup :refer [prettyfy-form-prettyfy-eval print-form-then-eval panel screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def troubleshooting-index 11)


[:body
 (panel
  (screencast-title troubleshooting-index "Troubleshooting")
  [:p "Remember the Mottos, and follow them!"]
  [:div.note "If you see surprising results, try these ideas."])


 (load-file "resources/screencast_sections/mottos.clj")


 (panel
  [:h3 "Validate scalars separately from validating collections."]

  [:ul
   [:li [:code "vector?"] " never in a scalar specification."]

   [:li
    [:code "int?"] " only in collection specification like this…"
    [:pre [:code "(defn all-ints? [v] (every? #(int? %) v))"]]
    ]

   [:li
    [:code "<"] " only in collection specification like this…"
    [:pre [:code "(defn b-greater-than-a? [m] (< (m :a) (m :b)))"]]
    ]]

  [:div.note
   [:p "We should never have a collection predicate like " [:code "vector?"] " in a scalar specification. Similarly, scalar predicates like " [:code "int?"] " should only appear in a collection specification in the context of testing a collection, like this second example, or when validating some relationship " [:em "between"] " datums, like this. The function names " [:code "validate-scalars"] ", " [:code "validate-collections"] ", et. al., are strong beacons to remind us that we're either validating scalars, or validating collections."]])


 (panel
  [:h3 "Make the specification mimic the shape of the data."]

  [:div.side-by-side-container
   [:div.side-by-side
    [:p [:em "Whoa!"]]

    (prettyfy-form-prettyfy-eval "(validate-scalars {:a 99} [int?])" 30 80)]

   [:div.side-by-side
    [:p [:em "Double Whoa!"]]

    (prettyfy-form-prettyfy-eval "(validate-scalars [42 \"abc\" 22/7] {0 int? 1 string? 2 ratio?})" 50 40)]]

  [:div.note
   [:p  "Speculoos with signal no error nor exception with map data and vector specification. The Speculoos functions don't enforce any requirements on the data and specification. If we feed it data that's a map and a specification that's a vector, it will dutifully try to validate what it has."]

   [:p "One word of warning: Because sequential things are indexed by integers, and map elements may also be indexed by integers, we could certainly abuse that flexibility like this. The data is a vector, specification is a map keyed with integers. Speculoos merely knows that it could successfully locate " [:code "42"] " and " [:code "int?"] " at " [:code "0"] ", etc. It 'worked' in this instance, but surprise lurks if we try to get to overly clever."]])


 (panel
  [:h3 "Validation ignores un-paired predicates and un-paired datums."]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:a 42 :b \"abc\"} {:a int? :c symbol?})" 40 80)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections [11 [22 [33]]] [[[[list?]]]])")

  [:div.note
   [:p  "A decent number of surprising validations result from predicates pairing to unexpected datums or not being paired at all."]

   [:p "On the top, the specification contains un-paired key :c; string \"abc\" isn't validated"]

   [:p "On the bottom, the specification uses an extra level of nesting; [33] wasn't validated"]

   [:p "Use 'thorough' variants, or use `...-without-specs` utilities."]])


 (panel
  [:h3 [:code ":valid? true"]]

  [:h4 [:code "valid?"] " returns " [:code "true"] " if zero un-satisfied predicates."]

  [:p "Strategies and tools for diagnosing mis-pairing…"]

  [:div.note
   [:p  "Corollary: " [:strong [:code "valid?"] " being " [:code "true"] " means there were zero non-true results."] " If the validation did not find any predicate+datum pairs, there would be zero invalid results, and thus return valid. Use the " [:code "thorough-…"] " function variants to require all datums to be validated."]])


 (panel
  [:h3 "Use your Clojure powers."]

  (prettyfy-form-prettyfy-eval "(let [data (get-in {:a {:b {:c [22/7]}}} [:a :b :c])
                                        spec (get-in {:a {:b {:c [int?]}}} [:a :b :c])]
                                    (validate-scalars data spec))" 75 40)

  [:div.note
   [:p "Speculoos specifications are regular old data structures containing regular old functions. (I assume your data is, too.) If we're wrangling with something deep down in some nested mess, use our Clojure powers to dive in and pull out the relevant pieces."]])


 (panel
  [:h3 "Use the verbose functions."]

  [:code "valid-scalars?"] " → " [:code "validate-scalars"]

  [:div.vspace]

  [:code "valid-collections?"] " → " [:code "validate-collections"]

  [:div.note
   [:p "If we're using the high-level " [:code "valid-…?"] " function variants, we'll only see " [:code "true/false"] ", which isn't helpful when troubleshooting. The " [:code "validate-…"] " variants are chatty and will display everything it considered during validation."]])


 (panel
  [:h3 "Check out " [:code "speculoos.utility"] "."]

  [:p "Functions to assist creating, viewing, analyzing, and modifying both scalar and collection specifications."]

  [:div.vspace]

  [:p "Remember: Speculoos specifications are plain, old Clojure data structures."]

  [:div.note
   [:p "The " [:a {:href "https://blosavio.github.io/speculoos/speculoos.utility.html"} [:code "speculoos.utility"]] " namespace provides many functions for creating, viewing, analyzing, and modifying both scalar and collection specifications."]])


 (panel
  [:h3 "Resort to " [:code "all-paths"] "."]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? {:a [99]} {:a string?})")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths {:a [99]})")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(all-paths {:a string?})" 40 50)]]

  [:p "Aha! The predicate " [:code "string?"] " at path " [:code "[:a]"] " and the integer " [:code "99"] " at path " [:code "[:a 0]"] " do not share a path!"]

  [:div.note
   [:p "When the going really gets tough, break out " [:code "speculoos.core/all-paths"] " and apply it to our data, then to our specification, and then step through the validation with our eyes."]])


 (panel
  [:h3 "Remember: Function arguments are contained in an implicit sequence. #1"]

  (prettyfy-form-prettyfy-eval "(defn arg-passthrough [& args] args)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(arg-passthrough [1 2 3])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(arg-passthrough [1 2 3] [4 5 6])")

  [:div.note
   [:p "When validating a function's arguments, remember that arguments are contained in an implicit sequence."]])


 (panel
  [:h3 "Remember: Function arguments are contained in an implicit sequence. #2"]

  [:p "This looks like a single vector in, single integer out…"]

  (prettyfy-form-prettyfy-eval "(first [1 2 3])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-fn-with first {:speculoos/arg-scalar-spec [string?]} [1 2 3])")

  [:div.vspace]

  [:p "Shouldn't integer " [:code "1"] " fail to satisfy predicate " [:code "string?"]]

  [:div.note
   [:p "If we're passing only a single value, it's easy to forget that the single value is contained in the argument sequence. Validating a function's arguments validates the " [:em "argument sequence"] ", not just the first lonely element that happens to also be a sequence."]])


 (panel
  [:h3 "Remember: Function arguments are contained in an implicit sequence. #3"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [[1 2 3]])")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [string?])")]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-scalars [[1 2 3]] [string?])")

  [:div.note
   [:p [:code "validate-fn-with"] " passes through the value returned by " [:code "first"] " because " [:code "validate-fn-with"] " did not find any invalid results. Why not? In this example, " [:code "1"] " and " [:code "string?"] " do not share a path, and therefore " [:code "validate-fn-with"] " preformed zero validations. Let's take a look."]

   [:p "We  find " [:code "1"] " at path " [:code "[0 0]"] " in the " [:em "argument sequence"] ", while scalar predicate " [:code "string?"] " is located at path " [:code "[0]"] " in the scalar specification. The two do not share paths are not paired, thus no validation."]])


 (panel
  [:h3 "Remember: Function arguments are contained in an implicit sequence. #4"]

  (prettyfy-form-prettyfy-eval "(validate-scalars [[1 2 3]] [[string?]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-fn-with first {:speculoos/arg-scalar-spec [[string?]]} [1 2 3])")

  [:div.note
   [:p "The fix is to make the specification mimic the shape of the data, the 'data' in this case being the " [:em "argument sequence"] ". Now that argument scalar specification mimics the shape of the " [:em "argument sequence"] ", scalar " [:code "1"] " and scalar predicate " [:code "string?"] " share a path " [:code "[0 0]"] ", and " [:code "validate-fn-with"] " performs a scalar validation. " [:code "1"] " fails to satisfy " [:code "string?"] "."]

   [:p "This also applies to validating arguments that are collections."]])


 (panel
  [:h3 "Contact me."]
  [:p "File an issue."
   [:br]
   "&emsp;"
   [:code "https://github.com/blosavio/speculoos/issues"]]

  [:p "Email me."
   [:br]
   "&emsp;"
   [:code "https://github.com/blosavio"]]

  [:div.note
   [:p "Finally, if you hit a wall, file a " [:a {:href "https://github.com/blosavio/speculoos/issues"} "bug report"] " or " [:a {:href "https://github.com/blosavio"} " email me"] "."]])
 ]