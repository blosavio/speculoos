[:section#valid-thorough
 [:h2 "Validation Summaries and Thorough Validations"]
 [:p "Up until now, we've been using " [:code "validate-scalars"] " and " [:code "validate-collections"] ", because they're verbose. For teaching and learning purposes (and for diagnosing problems), it's useful to see all the information considered by the validators. However, in production, once you've got your specification shape nailed down, you'll want a cleaner " [:em "yes"] " or " [:em "no"] " answer on whether the data satisfied the specification. You could certainly pull out the non-truthy, invalid results yourself…"]

 [:pre (print-form-then-eval "(filter #(not (:valid? %)) (validate-scalars [42 \"abc\" 22/7] [int? symbol? ratio?]))" 50 40)]

 [:p "…and then check for invalids yourself…"]

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

 [:p "Beware: Validation only considers paired predicates+datums (Mantra #3). If your datum doesn't have a paired predicate, then it won't be validated. Observe."]

 [:pre
  (print-form-then-eval "(valid-scalars? {:a 42} {:b string?})" 30 80)
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars {:a 42} {:b string?})" 35 80)]

 [:p [:code "42"] " does not share a path with " [:code "string?"] ", the lone predicate in the specification. Since there are zero invalid results, " [:code "valid-scalars?"] " returns " [:code "true"] "."]

 [:p [:strong "» Within the Speculoos library, " [:code "valid?"] " means " [:em " zero invalids. «"]]]

 [:h3 "Combo validation"]
 [:p "Validating scalars separately from validating collections is a core principle embodied by the Speculoos library. I believe that separating the two into distinct processes carries solid advantages because the specifications are more straightforward, the mental model is clearer, the implementation code is simpler, and it makes validation " [:em "à la carte"] ". Much of the time, you can probably get away with just a scalar spefication."]

 [:p "All that said, it is not possible to specify and validate every aspect of your data with only scalar validation or only collection validation. When you really need to be strict and validate both scalars and collections, you could manually combine like this."]

 [:pre (print-form-then-eval "(and (valid-scalars? [42] [int?]) (valid-collections? [42] [vector?]))" 45 80)]

 [:p "Speculoos provides a pre-made utility that does exactly that. You supply some data, then a scalar specification, then a collection specification."]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [valid? validate]])")
  [:br]
  [:br]
  (print-form-then-eval "(valid? [42] [int?] [vector?])" 20 80)]

 [:p "Let me emphasize what " [:code "valid?"] " is doing here, because it is " [:em "not"] " violating the first Mantra about separately validating scalars and collectios. First, " [:code "valid?"] " performs a scalar validation on the data, and puts that result on the shelf. Then, in a completely distinct operation, it performs a collection validation. " [:code "valid?"] " then pulls the scalar validation results off the shelf and combines it with the collection validation results, and returns a singular " [:code "true/false"] ".  (Look back at the first example of this sub-section to see the separation.)"]

 [:p "I reserved the shortest, most mnemonic function name, " [:code "valid?"] ", to signal how important it is to separate scalar and collection validation."]

 [:p "Speculoos also provides a variant that returns detailed validation results after performing distinct scalar validation and collection validation."]

 [:pre (print-form-then-eval "(validate [42 \"abc\" 22/7] [int? symbol? ratio?] [vector?])" 50 40)]

 [:p [:code "validate"] " gives you the exact results as if we had run " [:code "validate-scalars"] " and then immediately thereafter " [:code "validate-collections"] ". " [:code "validate"] " merely gives us the convenience of quickly running both in succession without having to re-type the data. With one invocation, we can validate " [:em "all"] " aspects of our data, both scalars and collections, and we never violated Matra #1."]

 [:h3 "Thorough validation"]

 [:p "Here are the general patterns regarding the function names."
  [:ul
   [:li [:strong [:code "validate-…"]] " functions return a detailed report for every datum+predicate pair."]
   [:li [:strong [:code "valid-…?"]] " functions return " [:code "true"] " if the predicate+datum pairs produce zero falsey results, " [:code "false"] " otherwise."
    ]
   [:li [:strong [:code "…-scalars"]] " functions consider only non-collection datums."]
   [:li [:strong [:code "…-collections"]] " functions consider only non-scalar datums."]
   [:li [:strong [:code "thoroughly-…"]] " functions return " [:code "true"] " only if every element (scalar or collection, as the case may be) is paired with a predicate, and every element satisfies its predicate."]]
  
  "'Plain' functions (i.e., " [:code "validate"] ", " [:code "valid?"] ", and " [:code "thoroughly-valid?"] ") perform a scalar validation, followed by performing a distict collection validation, and returns a single comprehensive response that merges the results of both."]

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