[:section#valid-thorough
 [:h2 "Validation Summaries and Thorough Validations"]

 [:p "Up until now, we've been using " [:code "validate-scalars"] " and " [:code "validate-collections"] ", because they're verbose. For teaching and learning purposes (and for " [:a {:href "#troubleshooting"} "diagnosing problems"] "), it's useful to see all the information considered by the validators. However, in many situations, once we've got our specification shape nailed down, we'll want a cleaner " [:em "yes"] " or " [:em "no"] " answer on whether the data satisfied the specification. We could certainly pull out the non-truthy, invalid results ourselves…"]

 [:pre (print-form-then-eval "(filter #(not (:valid? %)) (validate-scalars [42 \"abc\" 22/7] [int? symbol? ratio?]))" 50 40)]

 [:p "…and then check for invalids ourselves…"]

 [:pre [:code "(empty? *1) ;; => false"]]

 [:p "…but Speculoos provides a function that does exactly that, both for scalars…"]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [valid-scalars? valid-collections?]])")
  [:br]
  [:br]
  (print-form-then-eval "(valid-scalars? [42 \"abc\" 22/7] [int? symbol? ratio?])" 40 80)]

 [:p "…and for collections."]

 [:pre (print-form-then-eval "(valid-collections? [42 [\"abc\"]] [vector? [vector?]])"40 80)]

 [:p "Whereas the " [:code "validate-…"] " functions return a detailed validation report of every predicate+datum pair they see, the " [:code "valid-…?"] " variants provide a plain " [:code "true/false"] "."]

 [:p "Beware: Validation only considers paired predicates+datums (Motto #3). If our datum doesn't have a paired predicate, then it won't be validated. Observe."]

 [:pre
  (print-form-then-eval "(valid-scalars? {:a 42} {:b string?})" 30 80)
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars {:a 42} {:b string?})" 35 80)]

 [:p [:code "42"] " does not share a path with " [:code "string?"] ", the lone predicate in the specification. Since there are zero invalid results, " [:code "valid-scalars?"] " returns " [:code "true"] "."]

 [:p [:strong "» Within the Speculoos library, " [:code "valid?"] " means " [:em " zero invalids. «"]]]

 [:h3#thorough "Thorough validation"]

 [:p "Motto #3 reminds us that data elements not paired with a predicate are ignored. For some tasks, we may want to ensure that all elements in the data are subjected to at least one predicate. Plain " [:code "valid?"] "  only reports if all datum+predicate pairs are " [:code "true"] "."]

 [:pre (print-form-then-eval "(valid-scalars? [42 \"abc\" 22/7] [int?])" 35 45)]

 [:p "In this example, only " [:code "42"] " and " [:code "int?"] " form a pair that is validated. " [:code "\"abc\""] " and " [:code "22/7"] " are not paired with predicates, and therefore ignored. " [:code "valid-scalars"] " returns " [:code "true"] " regardless of the ignored scalars."]

 [:p "The " [:em "thorough"] " function " [:a {:href "#fn-terminology"} "variants"] " require that all data elements be specified, otherwise, they return " [:code "false"] ". Thoroughly validating that same data with that same specification shows the difference."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [thoroughly-valid-scalars?]])"  90 90)
  [:br]
  [:br]
  (print-form-then-eval "(thoroughly-valid-scalars? [42 \"abc\" 22/7] [int?])" 45 45) ]

 [:p "Whereas " [:code "valid-scalars?"] " ignored the un-paired " [:code "\"abc\""] " and " [:code "22/7"] ", " [:code "thoroughly-valid-scalars?"] " notices that neither have a predicate. Even though " [:code "42"] " satisfied " [:code "int?"] ", the un-paired scalars mean that this validation is not thorough, and thus " [:code "thoroughly-valid-scalars?"] " returns " [:code "false"] "."]

 [:p "The " [:code "utility"] " " [:a {:href "#utilities"} "namespace"] " provides a thorough variant for collections, as well as a variant for " [:a {:href "#combo"} "combo"] " validations. "[:code "thoroughly-valid-collections?"] " works analogously to what we've just seen."]

 [:p "Let's look at a combo example. First, the 'plain', non-thorough version. The data occupies the top row (i.e., first argument), the scalar specification occupies the middle row, and the collection specification occupies the lower row."]

 [:pre (print-form-then-eval "(valid? [42 \"abc\" 22/7] [int?] [vector?])" 35 45)]

 [:p "We validated the single vector, and only one out of the three scalars. " [:code "valid?"] " only considers paired elements+predicates, so it only validated " [:code "42"] ", a scalar, and the root vector, a collection. " [:code "valid?"] " ignored scalars " [:code "\"abc\""] " and " [:code "22/7"] "."]

 [:p "The thorough variant, " [:code "thoroughly-valid?"] ", however, does not ignore un-paired data elements. The function signatures is identical: data on the top row, scalar specification on the middle row, and the collection specification on the lower row."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [thoroughly-valid?]])")
  [:br]
  [:br]
  (print-form-then-eval "(thoroughly-valid? [42 \"abc\" 22/7] [int?] [vector?])" 35 45)]

 [:p "Even though both predicates, " [:code "int?"] " and " [:code "vector?"] ", were satisfied, " [:code "thoroughly-valid?"] " requires that all data elements be validated. Since " [:code "42"] " and " [:code "22/7"] " are un-paired, the entire validation returns " [:code "false"] "."]

 [:p "Note: Thoroughly validating does not ensure any measure of correctness nor rigor. 'Thorough' merely indicates that each element was exposed to " [:em "some"] " kind of predicate. That predicate could actually be trivially permissive. In the next example, " [:code "any?"] " returns " [:code "true"] " for all values."]

 [:pre (print-form-then-eval "(thoroughly-valid? [42 \"abc\" 22/7] [any? any? any?] [any?])" 35 45)]

 [:p "The only thing " [:code "thoroughly-valid?"]" tells us in this example is that the one vector and all three scalars were paired with a predicate, and that all four data elements satisfied a guaranteed-to-be-satisfied predicate."]

 [:p "Validation is only as good as the predicate. It's our responsibility to write a proper predicate."]

 [:h3#combo "Combo validation"]
 [:p "Validating scalars separately from validating collections is a core principle embodied by the Speculoos library. I believe that separating the two into distinct processes carries solid advantages because the specifications are more straightforward, the mental model is clearer, the implementation code is simpler, and it makes validation " [:em "à la carte"] ". Much of the time, we can probably get away with just a scalar specification."]

 [:p "All that said, it is not possible to specify and validate every aspect of our data with only scalar validation or only collection validation. When we really need to be strict and validate both scalars and collections, we could manually combine the two validations like this."]

 [:pre (print-form-then-eval "(and (valid-scalars? [42] [int?]) (valid-collections? [42] [vector?]))" 45 80)]

 [:p "Speculoos provides a pre-made utility that does exactly that. We supply some data, then a scalar specification, then a collection specification."]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [valid? validate]])")
  [:br]
  [:br]
  (print-form-then-eval "(valid? [42] [int?] [vector?])" 20 80)]

 [:p "Let me clarify what " [:code "valid?"] " is doing here, because it is " [:em "not"] " violating the first Motto about separately validating scalars and collections. First, " [:code "valid?"] " performs a scalar validation on the data, and puts that result on the shelf. Then, in a completely distinct operation, it performs a collection validation. " [:code "valid?"] " then pulls the scalar validation results off the shelf and combines it with the collection validation results, and returns a singular " [:code "true/false"] ".  (Look back at the first example of this sub-section to see the separation.)"]

 [:p "As an affirmation to how much I believe this, I reserved the shortest, most mnemonic function name, " [:code "valid?"] " to encourage Speculoos users to validate both scalars and collections, but separately."]

 [:p "Speculoos also provides a variant that returns detailed validation results after performing distinct scalar validation and collection validation."]

 [:pre (print-form-then-eval "(validate [42 \"abc\" 22/7] [int? symbol? ratio?] [vector?])" 50 40)]

 [:p [:code "validate"] " gives us the exact results as if we had run " [:code "validate-scalars"] " and then immediately thereafter " [:code "validate-collections"] ". " [:code "validate"] " merely provides us the convenience of quickly running both in succession without having to re-type the data. With one invocation, we can validate " [:em "all"] " aspects of our data, both scalars and collections, and we never violated Motto #1."]

 [:h3#fn-terminology "Function Naming Conventions"]

 [:p "Here are the general patterns regarding the function names."
  [:ul
   [:li [:strong [:code "validate-…"]] " functions return a detailed report for every datum+predicate pair."]
   [:li [:strong [:code "valid-…?"]] " functions return " [:code "true"] " if the predicate+datum pairs produce zero falsey results, " [:code "false"] " otherwise."
    ]
   [:li [:strong [:code "…-scalars"]] " functions consider only non-collection datums."]
   [:li [:strong [:code "…-collections"]] " functions consider only non-scalar datums."]
   [:li [:strong [:code "thoroughly-…"]] " functions return " [:code "true"] " only if every element (scalar or collection, as the case may be) is paired with a predicate, and every element satisfies its predicate."]]

  "'Plain' functions (i.e., " [:code "validate"] ", " [:code "valid?"] ", and " [:code "thoroughly-valid?"] ") perform a scalar validation, followed by performing a distinct collection validation, and returns a single comprehensive response that merges the results of both."]

 [:p "Here's how those terms are put together, and what they do."]

 [:table
  [:tr
   [:th "function"]
   [:th "checks…"]
   [:th "returns…"]
   [:th "note"]]

  [:tr
   [:td [:code "validate-scalars"]]
   [:td "scalars only"]
   [:td "detailed validation report"]
   [:td ""]]

  [:tr
   [:td [:code "valid-scalars?"]]
   [:td "scalars only"]
   [:td [:code "true/false"]]
   [:td ""]]

  [:tr
   [:td [:code "thoroughly-valid-scalars?"]]
   [:td "scalars only"]
   [:td [:code "true/false"]]
   [:td "only " [:code "true"] " if all scalars paired with a predicate"]]

  [:tr
   [:td [:code "validate-collections"]]
   [:td "collections only"]
   [:td "detailed validation report"]
   [:td ""]]

  [:tr
   [:td [:code "valid-collections?"]]
   [:td "collections only"]
   [:td [:code "true/false"]]
   [:td ""]]

  [:tr
   [:td [:code "thoroughly-valid-collections?"]]
   [:td "collections only"]
   [:td [:code "true/false"]]
   [:td "only " [:code "true"] " if all collections paired with a predicate"]]

  [:tr
   [:td [:code "validate"]]
   [:td "scalars, then collections, separately"]
   [:td "detailed validation report"]
   [:td ""]]

  [:tr
   [:td [:code "valid?"]]
   [:td "scalars, then collections, separately"]
   [:td [:code "true/false"]]
   [:td ""]]

  [:tr
   [:td [:code "thoroughly-valid?"]]
   [:td "scalars, then collections separately"]
   [:td [:code "true/false"]]
   [:td "only " [:code "true"] " if all datums paired with a predicate"]]]

 ]