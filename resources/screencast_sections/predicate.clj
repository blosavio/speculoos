(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def predicate-index 10)


[:body

 (panel
  (screencast-title predicate-index "Predicates")

  (prettyfy-form-prettyfy-eval "(#(<= 5 % ) 3)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(#(= 3 (count %)) [1 2 3])")

  [:div.note
   [:p "A predicate function returns a truthy or falsey value."]])


 (panel
  [:h3 "Sets, membership predicates"]

  (prettyfy-form-prettyfy-eval "(#{:red :orange :yellow :green :blue :purple} :green)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(#{:red :orange :yellow :green :blue :purple} :swim)")

  [:div.note
   [:p "Non-boolean returns work, too. For example, " [:a {:href "#sets"} "sets"] " make wonderful membership tests. If a set is used as a membership predicate and the item is a member, it returns the item itself. :green is a member of this set."]

   [:p "If an item is not a member of a set, `nil` is returned. :swim is not a member of this set."]])


 (panel
  [:h3 "Regular expression, string-validating predicates"]

  (prettyfy-form-prettyfy-eval "(re-find #\"^Four\" \"Four score and seven years ago...\")")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(re-find #\"^Four\" \"When in the course of human events...\")")

  [:div.note
   [:p "Regular expressions come in handy for validating string contents. This regular expression tests if a string begins with 'Four'. This first string does, so `re-find` returns the match. This string does not, so `re-find` returns `nil`."]])


 (panel
  [:h3 "Validation: " [:em "Does a datum satisfy its predicate?"]]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(int? 42)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-scalars [42] [int?])" 25 40)]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(re-find #\"^Four\" \"Four score and...\")")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(validate-scalars [\"Four\"] [#\"^Four\"])" 30 40)]]

  [:div.note
   [:p "Invoking a predicate when supplied with a datum — scalar or collection — is the core action of Speculoos' validation."]

   [:p "Speculoos is fairly ambivalent about the predicate return value. The " [:code "validate…"] " " [:a {:href "#fn-terminology"} "family of functions"] " mindlessly churns through its sequence of predicate-datum pairs, evaluates them, and stuffs the results into " [:code ":valid?"] " keys. The " [:code "valid…?"] " family of functions rips through " [:em "that"] " sequence, and if none of the results are falsey, returns " [:code "true"] ", otherwise it returns " [:code "false"] "."]])


 (panel
  [:h3 "Increasing specificity of predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:pre [:code "#(and (int? %) (pos? %) (even? %))"]]

    [:div.vspace]

    [:pre [:code "#(or (string? %) (char? %))"]]]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "((every-pred number? pos? even?) 100)")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "((some-fn number? string? boolean?) \\z)")]]

  [:div.note
   [:p "For the bulk of our screencast discussions, we've been using the built-in predicates offered by " [:code "clojure.core"] " such as " [:code "int?"] " and " [:code "vector?"] " because they're short, understandable, and they render clearly. But in practice, it's not terribly useful to validate an element with a mere " [:em "Is this scalar an integer?"] " or " [:em "Is this collection a vector?"] " Often, we'll want to combine multiple predicates to make the validation more specific. We could certainly use " [:code "clojure.core/and"] "…"]

   [:p "…which have the benefit of being universally understood. But Clojure also provides a pair of nice functions that streamline the expression and convey your intention. " [:code "every-pred"] " composes an arbitrary number of predicates with " [:code "and"] " semantics."]

   [:p "Similarly, " [:code "some-fn"] " composes predicates with " [:code "or"] " semantics."]])


 (panel
  [:h3 "Maintaining pairing between scalars in a sequence and predicates in a sequence"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:pre [:code "[42 \"abc\" 22/7]"]]

    [:pre [:code "[…   …  ratio?]"]]]

   [:div.side-by-side
    [:pre [:code "#(identity true)"]]

    [:pre [:code  "(fn [] true)"]]]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [(constantly true) (constantly true) ratio?])" 65 50)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [any? any? char?])" 45 40)

  [:div.note
   [:p "When Speculoos validates the scalars of a sequence, it consumes each element in turn. If we care only about validating some of the elements, we must include placeholders in the specification to maintain the sequence of predicates."]

   [:p "For example, suppose we only want to validate "
    ", the third element of "
    ". The first two elements are irrelevant to us. We have a few options. We could write our own little always-true predicate. "
    [:code "#(true)"] " won't work because " [:code "true"] " is not invocable. " [:code "#(identity true)"] " loses the conciseness. This works…"]

   [:p [:code "constantly"] " is nice because it accepts any number of args. But for my money, nothing tops " [:code "any?"] "."]

   [:p [:code "any?"] " is four characters, doesn't require typing parentheses, and the everyday usage of " [:em "any"] " aligns well with its technical purpose."]])


 (panel
  [:h3 "Warning: " [:code "clojure.core/contains?"]]

  (prettyfy-form-prettyfy-eval "(contains? [97 98 99] 1)")

  [:div.vspace]

  [:pre [:code "(defn in? [coll item] (some #(= item %) coll))"]]

  [:div.no-display (require '[speculoos.utility :refer [in?]])]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(in? [97 98 99] 98)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(in? [97 98 99] 1)")

  [:div.note
   [:p "Watch out for " [:code "clojure.core/contains?"] ". It might seem natural to use " [:code "contains?"] " to check if a collection contains an item, but it doesn't do what its name suggests. Observe."]

   [:p [:code "contains?"] " actually tells you whether a collection contains a key. For a vector, it tests for an index. If you'd like to check whether a value is contained in a collection, you can use this pattern."]

   [:p "(Check out " [:code "speculoos.utility/in?"] ".)"]])


 (panel
  [:h3 "Helpful names and function object rendering"]

  [:pre [:code "[{:path [0],\n  :datum 42,\n  :predicate #function[documentation/eval94717/fn--94718],\n  :valid? false}]"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def greater-than-50? #(< 50 %))")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-scalars [42] [greater-than-50?])" 40 40)

  [:div.note
   [:p "We've been using the " [:code "#(…)"] " form because it's compact, but it does have a drawback when Speculoos renders the function in a validation report."]

   [:p "The function rendering is not terribly informative when the validation displays the predicate. Same problem with " [:code "(fn [v] (…))"] "."]

   [:p "One solution to this issue is to define your predicates with an informative name."]

   [:p "Now, the predicate entry carries a bit more meaning."]])


 (panel
  [:h3 "Regex niceties"]

  (prettyfy-form-prettyfy-eval "(def re #\"F\\dQ\\d\")")

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(defn re-pred [s] (re-matches re s))")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(validate-scalars [\"F1Q5\" \"F2QQ\"] [re-pred re-pred])" 40 30)]

   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? [\"A1B2\" \"CDEF\"] [#\"(\\w\\d){2}\" #\"\\w{4}\"])" 40 80)
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(validate-scalars {:a \"foo\" :b \"bar\"} {:a #\"f.\\w\" :b #\"^[abr]{0,3}$\"})" 55 50)]]

  [:div.note
   [:p "Regular expressions check the content of strings."]

   [:p "Speculoos considers free-floating regexes in a scalar specification as predicates, so you can simply jam them in there."]

   [:p "Using bare regexes in your scalar specification has a nice side benefit in that the " [:code "data-from-spec"] ", " [:code "exercise"] ", and " [:code "exercise-fn"] " utilities can generate valid strings."]])


 (panel
  [:h3 "Utilities described in other screencasts"]

  [:ul
   [:li "exercise"]
   [:li "defpred"]
   [:li "thoroughly-valid?"]])


(whats-next-panel
 predicate-index
 [:div.note "During the next screencast, we'll explore how Speculoos handles validating non-terminating sequences, and some really cool capabilities they give us to write powerful specifications."])
 ]