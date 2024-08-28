[:section#exercising
 [:h2 "Generating Random Samples and Exercising"]

 [:pre (print-form-then-eval "(require '[speculoos.function-specs :refer [exercise-fn]])")]
 [:p "Another tool Speculoos offers in this category: exercising specified functions. If you have injected an argument scalar specification into your function, Speculoos can generate a series of specification-satisfying arguments and repeatedly invoke your function. Let's take advantage of the nice feature of " [:code "defn"] " that adds metadata during function definition."]
 
 [:pre
  (print-form-then-eval "(defn bottles {:speculoos/arg-scalar-spec [pos-int? string?]} [n liquid] (str n \" bottles of \" liquid \" on the wall, \" n \" bottles of \" liquid \"...\"))")
  [:br]
  [:br]
  (print-form-then-eval "(bottles 99 \"espresso\")")]
 
 [:p "Because we included a scalar specification where it could be found, Speculoos can exercise our function using random sample generators synthesized using that specification as a blueprint."]
 
 [:pre (print-form-then-eval "(exercise-fn bottles 5)" 50 150)]
 
 [:p "Not exactly thirst-quenching."]

 ]