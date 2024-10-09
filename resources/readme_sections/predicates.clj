[:section#predicates
 [:h2 "Predicates"]

 [:p "A predicate function returns a truthy or falsey value."]

 [:pre
  (print-form-then-eval "(#(<= 5 % ) 3)")
  [:br]
  [:br]
  (print-form-then-eval "(#(= 3 (count %)) [1 2 3])")]

 [:p "Non-boolean returns work, too. For example, " [:a {:href "#sets"} "sets"] " make wonderful membership tests."]

 [:pre
  [:code ";; truthy"]
  [:br]
  (print-form-then-eval "(#{:red :orange :yellow :green :blue :purple} :green)")
  [:br]
  [:br]
  [:code ";; falsey"]
  [:br]
  (print-form-then-eval "(#{:red :orange :yellow :green :blue :purple} :swim)")]

 [:p "Regular expressions come in handy for validating string contents."]

 [:pre
  [:code ";; truthy"]
  [:br]
  (print-form-then-eval "(re-find #\"^Four\" \"Four score and seven years ago...\")")
  [:br]
  [:br]
  [:code ";; falsey"]
  [:br]
  (print-form-then-eval "(re-find #\"^Four\" \"When in the course of human events...\")")]

 [:p "Invoking a predicate when supplied with a datum — scalar or collection — is the core action of Speculoos' validation."]

 [:pre
  (print-form-then-eval "(int? 42)")
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [42] [int?])" 25 40)]

 [:p "Speculoos is fairly ambivalent about the predicate return value. The " [:code "validate…"] " " [:a {:href "#fn-terminology"} "family of functions"] " mindlessly churns through its sequence of predicate-datum pairs, evaluates them, and stuffs the results into " [:code ":valid?"] " keys. The " [:code "valid…?"] " family of functions rips through " [:em "that"] " sequence, and if none of the results are falsey, returns " [:code "true"] ", otherwise it returns " [:code "false"] "."]

 [:p "For most of this document, we've been using the built-in predicates offered by " [:code "clojure.core"] " such as " [:code "int?"] " and " [:code "vector?"] " because they're short, understandable, and they render clearly. But in practice, it's not terribly useful to validate an element with a mere " [:em "Is this scalar an integer?"] " or " [:em "Is this collection a vector?"] " Often, we'll want to combine multiple predicates to make the validation more specific. We could certainly use " [:code "clojure.core/and"] "…"]

 [:pre [:code "#(and (int? %) (pos? %) (even? %))"]]

 [:p "…and " [:code "clojure.core/or"] "…"]

 [:pre [:code "#(or (string? %) (char? %))"]]

 [:p "…which have the benefit of being universally understood. But Clojure also provides a pair of nice functions that streamline the expression and convey our intention. " [:code "every-pred"] " composes an arbitrary number of predicates with " [:code "and"] " semantics."]

 [:pre (print-form-then-eval "((every-pred number? pos? even?) 100)")]

 [:p "Similarly, " [:code "some-fn"] " composes predicates with " [:code "or"] " semantics."]

 [:pre (print-form-then-eval "((some-fn number? string? boolean?) \\z)")]

 [:p "When Speculoos validates the scalars of a sequence, it consumes each element in turn. If we care only about validating some of the elements, we must include placeholders in the specification to maintain the sequence of predicates."]

 [:p "For example, suppose we only want to validate "
  [:code "\\z"]
  ", the third element of "
  [:code "[42 :foo \\z]"]
  ". The first two elements are irrelevant to us. We have a few options. We could write our own little always-true predicate. "
  [:code "#(true)"] " won't work because " [:code "true"] " is not invocable. " [:code "#(identity true)"] " loses the conciseness. This works…"]

 [:pre [:code  "(fn [] true)"]]

 [:p "…but Clojure already includes a couple of nice options." ]

 [:pre (print-form-then-eval "(valid-scalars? [42 :foo \\z] [(constantly true) (constantly true) char?])" 65 50)]

 [:p [:code "constantly"] " is nice because it accepts any number of args. But for my money, nothing tops " [:code "any?"] "."]

 [:pre (print-form-then-eval "(valid-scalars? [42 :foo \\z] [any? any? char?])" 45 40)]

 [:p [:code "any?"] " is four characters, doesn't require typing parentheses, and the everyday usage of " [:em "any"] " aligns well with its technical purpose."]

 [:p "A word of warning about " [:code "clojure.core/contains?"] ". It might seem natural to use " [:code "contains?"] " to check if a collection contains an item, but it doesn't do what its name suggests. Observe."]

 [:pre (print-form-then-eval "(contains? [97 98 99] 1)")]

 [:p [:code "contains?"] " actually tells es whether a collection contains a key. For a vector, it tests for an index. If we'd like to check whether a value is contained in a collection, we can use this pattern."]

 [:pre
  [:code "(defn in? [coll item] (some #(= item %) coll))"]
  [:br]
  [:br]
  [:code ";; integer 98 is a value found in the vector"]
  [:br]
  (print-form-then-eval "(in? [97 98 99] 98)")
  [:br]
  [:br]
  [:code ";; integer 1 is not a value found in the vector"]
  [:br]
  (print-form-then-eval "(in? [97 98 99] 1)")]

 [:p "(Check out " [:code "speculoos.utility/in?"] ".)"]

 [:p "I've been using the " [:code "#(…)"] " form because it's compact, but it does have a drawback when Speculoos renders the function in a validation report."]

 [:pre [:code "[{:path [0],\n  :datum 42,\n  :predicate #function[documentation/eval94717/fn--94718],\n  :valid? false}]"]]

 [:p "The function rendering is not terribly informative when the validation displays the predicate. Same problem with " [:code "(fn [v] (…))"] "."]

 [:p "One solution to this issue is to define our predicates with an informative name."]

 [:pre
  (print-form-then-eval "(def greater-than-50? #(< 50 %))")
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [42] [greater-than-50?])" 40 40)]

 [:p "Now, the predicate entry carries a bit more meaning."]

 [:p "Regular expressions check the content of strings."]

 [:pre
  (print-form-then-eval "(def re #\"F\\dQ\\d\")")
  [:br]
  [:br]
  (print-form-then-eval "(defn re-pred [s] (re-matches re s))")
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [\"F1Q5\" \"F2QQ\"] [re-pred re-pred])" 40 80)]

 [:p "Speculoos considers free-floating regexes in a scalar specification as predicates, so we can simply jam them in there."]

 [:pre
  (print-form-then-eval "(valid-scalars? [\"A1B2\" \"CDEF\"] [#\"(\\w\\d){2}\" #\"\\w{4}\"])" 40 80)
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars {:a \"foo\" :b \"bar\"} {:a #\"f.\\w\" :b #\"^[abr]{0,3}$\"})" 55 50)]

 [:p "Using bare regexes in our scalar specification has a nice side benefit in that the " [:code "data-from-spec"] ", " [:code "exercise"] ", and " [:code "exercise-fn"] " utilities can generate valid strings."]

 [:p "Beyond their critical role they play in validating data, predicate functions can also carry metadata that describes how to " [:a {:href "#exercising"} "generate valid, random samples"] ". To help with that task, the " [:a {:href "#utilities"} "utility namespace"] " provides " [:code "defpred"] ", a helper macro that streamlines " [:strong "def"] "ing " [:strong "pred"] "icates and associating random sample generators."]

 [:p "Instead of storing specifications in a dedicated " [:a {:href "https://clojure.org/guides/spec#_registry"} "registry"] ",  Speculoos takes a " [:em "laissez-faire"] " approach: specifications may live directly in whatever namespace we please. If we feel that some sort of registry would be useful, we could make our own " [:a {:href "https://github.com/clojure/spec.alpha/blob/c630a0b8f1f47275e1a476dcdf77507316bad5bc/src/main/clojure/clojure/spec/alpha.clj#L52"} "modeled after"] " " [:code "spec.alpha"] "'s."]

 [:p "Finally, when checking function correctness, " [:a {:href "#fn-correctness"} "validating the relationship"] " between the function's arguments and the function's return value uses a function that kinda looks like a predicate. In contrast to a typical predicate that accepts one argument, that relationship-checking function accepts exactly two elements: the function's argument sequence and the function's return value."]]