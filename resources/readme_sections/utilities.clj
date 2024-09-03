[:section#utilities
 [:h2 "Utility Functions"]
 
 [:p "You won't miss any crucial piece of Speculoos' functionality if you don't use this namespace, but perhaps something here might make your day a little nicer. Nearly every function takes advantage of " [:code "speculoos.core/all-paths"] ", which decomposes a heterogeneous, arbitrarily-nested data structure into a sequence of paths and datums. With that in hand, these not-clever functions churn through the entries and give you back something useful."]
 
 [:pre (print-form-then-eval "(require '[speculoos.utility :refer [scalars-without-predicates predicates-without-scalars collections-without-predicates predicates-without-collections thoroughly-valid? sore-thumb spec-from-data data-from-spec basic-collection-spec-from-data  exercise]])")]
 
 [:p "Recall that Speculoos only validates scalars and predicate located at identical paths in the. This pair of utilities tells us where we have unmatched scalars or unmatched predicates."]
 
 [:pre (print-form-then-eval "(scalars-without-predicates [42 [\"abc\" 22/7]] [int?])" 50 40)]
 
 [:p "With this information, we can see if the specification was ignoring scalars that we were expecting to validate, and adjust our specification for better coverage. (The "
 [:code "thoroughly-…"] " group of functions would strictly enforce all datums be paired with predicates.)" ]

 [:p "This next utility can help diagnose surprising results. Just because you put a predicate into the scalar specification doesn't force validation of a scalar that doesn't exist."]

 [:pre (print-form-then-eval "(predicates-without-scalars [42 :foo] [int? [keyword? string?]])" 55 40)]

 [:p "Now we can see two un-paired predicates. " [:code "string?"] " simply doesn't have a scalar, and " [:code "keyword"] " doesn't share a path with " [:code ":foo"] " so it wasn't used during validation."]
 
 [:p "It's not difficult to neglect a predicate for a nested element within a collection specification, so Speculoos offers analogous utilities to highlight those possible issues."]
 
 [:pre (print-form-then-eval "(collections-without-predicates [11 [22 {:a 33}]] [vector? [{:is-a-map? map?}]])" 65 40)]
 
 [:p "Yup, we didn't specify that inner vector whose first element is " [:code "22"] ". That's okay, though. Maybe we don't care to specify it. But now we're aware."]

 [:p "Maybe we put a predicate into a collection specification that clearly ought to be unsatisfied, but for some reason, " [:code "validate-collections"] " isn't picking it up."]

 [:pre (print-form-then-eval "(predicates-without-collections {:a 42} {:is-map? map? :b [set?]})" 65 40)]

 [:p "Aha. " [:code "set?"] " in the collection specification isn't paired with an element in the data, so it is unused during validation."]
 
 [:p "Taking that idea to its logical conclusion, " [:code "thoroughly-valid?"] " returns " [:code "true"] " only if every scalar and every collection in data have a corresponding predicate in the scalar specification and the collection specification, respectively, and all those predicates are satisfied."]
 
 [:pre
  [:code ";; all scalars and the vector have predicates; all predicates satisfied"]
  [:br]
  (print-form-then-eval "(thoroughly-valid? [42 :foo 22/7] [int? keyword? ratio?] [vector?])" 55 40)
  [:br]
  [:br]
  [:code ";; all scalars and the vector have predicates, but the 22/7 fails the scalar predicate"]
  [:br]
  (print-form-then-eval "(thoroughly-valid? [42 :foo 22/7] [int? keyword? string?] [vector?])" 55 40)
  [:br]
  [:br]
  [:code ";; all scalars and the vector have predicates, but the vector fails the collection predicate"]
  [:br]
  (print-form-then-eval "(thoroughly-valid? [42 :foo 22/7] [int? keyword? ratio?] [list?])" 55 40)
  [:br]
  [:br]
  [:code ";; all predicates are satisfied, but the 22/7 scalar is missing a predicate"]
  [:br]
  (print-form-then-eval "(thoroughly-valid? [42 :foo 22/7] [int? keyword?] [vector?])" 55 40)
  [:br]
  [:br]
  [:code ";; all predicates are satisfied, but the vector is missing a predicate"]
  [:br]
  (print-form-then-eval "(thoroughly-valid? [42 :foo 22/7] [int? keyword? ratio?] [])" 55 40)]

 [:p "In the first example, we learned that all our predicates are valid predicates, but in the second example, we see where our specification contains two non-predicates."]
 
 [:p "I envision that you'd be using these utility functions mainly during dev time, but I won't protest if you find them useful in production. This next utility, however, is probably only useful at the keyboard. Given data and a scalar specification, it prints back both, but with only the invalid scalars and predicates showing."]
 
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
 
 [:p#custom-generators "The " [:code "utility"] " namespace contains a trio of functions to assist writing, checking, and locating compound predicates that can be used by " [:code "data-from-spec"] " and the function validation functions to generate valid random sample data. A compound predicate such as " [:code "#(and (int? %) (< % 100))"] " does not have built-in generator provided by " [:code "clojure.test.check.generators"] ". However, " [:code "data-from-spec"] " can extract a generator residing in the predicate's metadata. Here's an example of how to add a hand-written generator to a scalar predicate. We'll write a scalar specification requiring a vector composed of a keyword followed by a number less than one-hundred. "]
 
 [:pre (print-form-then-eval "(data-from-spec [keyword? (with-meta #(and (number? %) (< % 100)) {:speculoos/predicate->generator #(rand 101)})] :random)" 140 60)]

 [:p [:code "data-from-spec"] " was able to locate two generators. First, because it's a predicate included in the core Clojure distribution, " [:code "keyword?"] " owns a built-in random sample generator. Second, the compound predicate for " [:em "number greater than one hundred"] " doesn't have a built-in generator, but " [:code "data-from-spec"] " found the generator stored in the predicate's metadata."]
 
 [:p "The custom generator must be provided at the metadata key " [:code ":speculoos/predicate->generator"] ". If we conform to their expectations, Speculoos' data-generating functions can automatically create generators from its specifications. But if for some reason we were handed a specification that did not, we must write the generators by hand. It can be tricky to make sure the generator produces values that precisely satisfy the predicate, so Speculoos provides a utility to check one against the other. What if we don't quite have the generator written correctly?"]
 
 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [validate-predicate->generator unfindable-generators defpred]] '[clojure.test.check.generators :as gen])" 160 40)
  [:br]
  [:br]
  [:code ";; warm up the generator for better-looking results"]
  [:br]
  (print-form-then-eval "(validate-predicate->generator (with-meta #(and (int? %) (even? %) (<= 50 500)) {:speculoos/predicate->generator #(last (gen/sample (gen/such-that odd? (gen/large-integer* {:min 50 :max 500})) 22))}) 5)")]
 
 [:p "Oops. We paired " [:code "odd?"] " with " [:code "such-that"] " when we should have used " [:code "even?"] ". Let's fix it using another helper, " [:code "defpred"] ", that relieves us of a bit of that keyboarding."]
 
 [:pre
  (print-form-then-eval "(defpred fixed #(and (int? %) (even? %) (<= 50 500)) #(last (gen/sample (gen/such-that even? (gen/large-integer* {:min 50 :max 500})) 22)))")
  [:br]
  [:br]
  (print-form-then-eval "(validate-predicate->generator fixed 5)")]
 
 [:p "We can see that the generator now yields values that satisfy its predicate."]
 
 [:p "If you don't manually supply a generator, " [:code "defpred"] " will create one automatically if your predicate's structure fulfills some assumptions. A top-level " [:code "or"] " indicates a set of possible Clojure scalar types. Let's define a predicate that tests for either an integer, keyword, or ratio and then check how it works."]
 
 [:pre
  (print-form-then-eval "(defpred int-kw-ratio? #(or (int? %) (keyword? %) (ratio? %)))" 40 40)
  [:br]
  [:br]
  (print-form-then-eval "(validate-predicate->generator int-kw-ratio? 9)")]

 [:p "Here's what just happened. " [:code "defpred"] " binds the symbol " [:code "int-kw-ratio?"] " to the anonymous function " [:code "#(or (int? %) (keyword? %) (ratio? %))"] " in the same manner as if we had used " [:code "def"] ". Furthermore, because we used a top-level " [:code "or"] " and recognized predicates, " [:code "defpred"] " attached to its metadata a random sample generator that produces an integer, keyword, or a ratio scalar with equal probabilities. Then, " [:code "validate-predicate->generator"] " exercised that generator nine times, and we can see that each of the nine samples satisfied the predicate."]
  
 [:p [:code "and"] " signals a set of modifiers as long as the first clause tests for a basic Clojure scalar type. Let's define a predicate that tests for an integer that is even and greater-or-equal to two."]
 
 [:pre
  (print-form-then-eval "(defpred even-int-more-5 (fn [i] (and (int? i) (even? i) (<= 2 i))))")
  [:br]
  [:br]
  (print-form-then-eval "(validate-predicate->generator even-int-more-5 5)")]
 
 [:p "Note how the first " [:em "s-expression"] " immediately following " [:code "and"] ", " [:code "(int? i)"]  ", selects the basic Clojure scalar type. All subsequent " [:em "s-expressions"] " refine the integer sample generator."]
 
 [:p "Now, let's see how we might combine both " [:code "or"] " and " [:code "and"] ". We'll define a predicate that tests for either an odd integer, a string of at least three characters, or a ratio greater than one-ninth."]
 
 [:pre
  (print-form-then-eval "(defpred combined-pred #(or (and (int? %) (odd? %))
                                                              (and (string? %) (<= 3 (count %)))
                                                              (and (ratio? %) (< 1/9 %))))")
  [:br]
  (print-form-then-eval "(validate-predicate->generator combined-pred 7)")]
 
 [:p "Perhaps we've got a specification in hand, and we'd like to know if all of the predicates have a random sample generator. Let's write a couple of specifications and deliberately neglect to give them random sample generators."]
 
 [:pre
  (print-form-then-eval "(def not-int? #(not (int? %)))")
  [:br]
  [:br]
  (print-form-then-eval "(def needless-str? #(string? %))")
  [:br]
  [:br]
  (print-form-then-eval "(unfindable-generators [int? {:a string? :b [not-int? needless-str?]}])")]
 
 [:p [:code "int?"] " and " [:code "string?"] " are included in the core Clojure distribution and have a sibling in " [:code "test.check"] ", so they " [:em "do"] " have a findable generator. However, we did not supply generators for " [:code "not-int?"] " and " [:code "needless-str?"] " within the metadata, so " [:code "unfindable-generators"] " reports the paths to those two predicates."]

 [:p "One final creative utility: " [:code "exercise"] ", which consumes a scalar specification, and generates a series of random data, then validates them."]
 
 [:pre (print-form-then-eval "(exercise [int? symbol? {:x boolean? :y ratio?}] 5)" 55 55)]
 
 [:p "Five times, " [:code "exercise"] " generated a random sample of an integer, symbol, boolean, and a ratio, arranged in exactly the shape of the specification. Thankfully, they all validated as " [:code "true"] "."]
 ]
