[:section#utilities
 [:h2 "Utility Functions"]

 [:p "You won't miss any crucial piece of Speculoos' functionality if you don't use this namespace, but perhaps something here might make your day a little nicer. Nearly every function takes advantage of " [:code "speculoos.core/all-paths"] ", which decomposes a heterogeneous, arbitrarily-nested data structure into a sequence of paths and datums. With that in hand, these not-clever functions churn through the entries and give you back something useful."]

 [:pre (print-form-then-eval "(require '[speculoos.utility :refer [scalars-without-predicates predicates-without-scalars collections-without-predicates predicates-without-collections

 thoroughly-valid?

 sore-thumb spec-from-data data-from-spec basic-collection-spec-from-data


  exercise

]])")]

 [:p "Recall that Speculoos only validates using elements in the data and predicates in the specification located at identical paths. This duo of utilities tells us where we have unmatched scalars or unmatched predicates. The first of the duo tells us about un-paired scalars."]

 [:pre (print-form-then-eval "(scalars-without-predicates [42 [\"abc\" 22/7]] [int?])" 50 40)]

 [:p "With this information, we can see if the specification was ignoring scalars that we were expecting to validate, and adjust our specification for better coverage. (The "
 [:code "thoroughly-…"] " " [:a {:href "#thorough"} " group of functions"] " would strictly enforce all datums be paired with predicates.)"]

 [:p "The second utility of that duo performs the complementary operation by telling us about un-paired predicates."]

 [:pre (print-form-then-eval "(predicates-without-scalars [42] [int? string? ratio?])")]

 [:p "It is especially helpful for " [:a {:href "#troubleshooting"} "diagnosing surprising results"] ". Just because we put a predicate into the scalar specification doesn't force validation of a scalar that doesn't exist."]

 [:pre (print-form-then-eval "(predicates-without-scalars [42 \"abc\"] [int? [string? ratio?]])" 55 40)]

 [:p "Now we can see two un-paired predicates. " [:code "ratio?"] " simply doesn't have a scalar to pair with, and " [:code "string?"] " doesn't share a path with " [:code "\"abc\""] " so it wasn't used during validation."]

 [:p "It's not difficult to neglect a predicate for a nested element within a collection specification, so Speculoos offers analogous utilities to highlight those possible issues."]

 [:pre (print-form-then-eval "(collections-without-predicates [11 [22 {:a 33}]] [vector? [{:is-a-map? map?}]])" 65 40)]

 [:p "Yup, we didn't specify that inner vector whose first element is " [:code "22"] ". That's okay, though. Maybe we don't care to specify it. But now we're aware."]

 [:p "Maybe we put a predicate into a collection specification that clearly ought to be unsatisfied, but for some reason, " [:code "validate-collections"] " isn't picking it up."]

 [:pre (print-form-then-eval "(predicates-without-collections {:a 42} {:is-map? map? :b [set?]})" 65 40)]

 [:p "Aha. " [:code "set?"] " in the collection specification isn't paired with an element in the data, so it is unused during validation."]

 [:p "Taking those ideas further, the " [:a {:href "#thorough"} [:em "thorough validation variants"]] " return " [:code "true"] " only if every scalar and every collection in data have a corresponding predicate in the scalar specification and the collection specification, respectively, and all those predicates are satisfied."]
 
 [:p "This next utility is probably only useful during development. Given data and a scalar specification, " [:code "sore-thumb"] " prints back both, but with only the invalid scalars and predicates showing."]

 [:div.no-display
  (def sore-thumb-example "(sore-thumb [42 {:a true :b [22/7 :foo]} 1.23] [int? {:a boolean? :b [ratio? string?]} int?])")
  (def sore-thumb-example-eval (with-out-str (eval (read-string sore-thumb-example))))
  ;; Leave the following :pre block as is.
  ]

 [:pre
  [:code (prettyfy sore-thumb-example)]
  [:br]
  [:br]
  [:code ";; to *out*"]
  [:br]
  [:code (clojure.string/replace sore-thumb-example-eval "\"" "")]]

 [:p "I've found it handy for quickly pin-pointing the unsatisfied scalar-predicate pairs in a large, deeply-nested data structure."]

 [:p "I think of the next few utilities as " [:em "creative"] ", making something that didn't previously exist. We'll start with a pair of functions which perform complimentary actions."]

 [:pre
  (print-form-then-eval "(spec-from-data [33 {:a :baz :b [1/3 false]} '(3.14 \\z)])" 65 25)
  [:br]
  [:br]
  (print-form-then-eval "(data-from-spec {:x int? :y [ratio? boolean?] :z (list char? neg-int?)} :random)" 65 25)]

 [:p "I hope their names give good indications of what they do. The generated specification contains only basic predicates, that is, merely " [:em "Is it an integer?"] ", not " [:em "Is it an even integer greater than 25, divisible by 3?"] ". But it's convenient raw material to start crafting a tighter specification. (Oh, yeah…they both round-trip.) A few " [:a {:href "#custom-generators"} "paragraphs down"] " we'll see some ways to create random sample generators for compound predicates."]

 [:p "Speaking of raw material, Speculoos also has a collection specification generator."]

 [:pre (print-form-then-eval "(basic-collection-spec-from-data [55 {:q 33 :r ['foo 'bar]} '(22 44 66)])" 75 95)]

 [:p "Which produces a specification that is perhaps not immediately useful, but does provide a good starting template, because collection specifications can be tricky to get just right."]

 [:p#custom-generators "The " [:code "utility"] " namespace contains a trio of functions to assist " [:a {:href "#exercising"} "writing, checking, and locating"] " compound predicates that can be used by " [:code "data-from-spec"] ", " [:code "validate-fn"] ", and " [:code "validate-fn-with"] " to generate valid random sample data. A compound predicate such as " [:code "#(and (int? %) (< % 100))"] " does not have built-in generator provided by " [:code "clojure.test.check.generators"] ". However, " [:code "data-from-spec"] " and friends can extract a generator residing in the predicate's metadata. The " [:code "defpred"] " utility " [:a {:href "#access-gen"} " streamlines"] " that task."]
 ]