[:section#scalar-validation
 [:h2 "Scalar Validation"]
 [:p "Let's return to the English-language specification we saw in the introduction: " [:em "A vector containing an integer, then a string, then a ratio"] ". Consider the paths of this vector…"]
 [:pre (print-form-then-eval "(all-paths [42 \"abc\" 22/7])")]

 [:p "…and the paths of this vector…"]
 [:pre (print-form-then-eval "(all-paths [int? string? ratio?])" 80 45)]

 [:p "We see that elements of both share paths. If we keep only the paths to scalars, i.e., we discard the root collections at path " [:code "[]"] ", each has three elements remaining."
  [:ul
   [:li [:code "42"] " and " [:code "int?"] " both at path " [:code "[0]"] ", in their respective vectors,"]
   [:li [:code "\"abc\""] " and " [:code "string?"] " both at path " [:code "[1]"] ", and"]
   [:li [:code "22/7"] " and " [:code "ratio?"] " both at path " [:code "[2]"] "."]]]
 [:p "Those pairs of scalars and predicates line up nicely, and we could evaluate each pair, in turn."]
 [:pre
  (print-form-then-eval "(int? 42)")
  [:br]
  (print-form-then-eval "(string? \"abc\")")
  [:br]
  (print-form-then-eval "(ratio? 22/7)")]

 [:p "All three scalars satisfy their respective predicates that they're paired with. Speculoos provides a function, " [:code "validate-scalars"] " that substantially does all that work for us. Given data and a specification that share the data's shape (Motto #2), " [:code "validate-scalars"] ":"]
 [:ol
  [:li "Runs " [:code "all-paths"] " on the data, then the specification."]
  [:li "Removes the collection elements from each, keeping only the scalars in each."]
  [:li "Removes the scalars in data that lack a predicate at the same path in the specification, and removes the predicates in the specification that lack datums at the same path in the data."]
  [:li "For each remaining pair of scalar+predicate, applies the predicate to the scalar."]]

 [:p "Let's see that in action. We invoke " [:code "validate-scalars"] " with the data vector as the first argument and the specification vector as the second argument."]
 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [validate-scalars]])")
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [42 \"abc\" 22/7] [int? string? ratio?])" 50 40)]

 [:p "Let's apply the Mottos to what we just did. Motto #1: At the moment, we're validating scalars, as the "[:em "-scalars"] " suffix of the function name reminds us. The validation yielded only predicates applied to scalars; scalar validation ignored the collections. Motto #2: The shape of our specification mimics the data. Because both are vectors, " [:code "validate-scalars"] " was able to properly apply each predicate its respective datum. Motto #3: Every predicate was paired with a datum and "[:em "vice versa"] ", so validation did not ignore anything."]

 [:p  [:code "validate-scalars"] " returns a sequence of all the scalars in data that share a path with a predicate in the specification. For each of those pairs, we receive a map containing the " [:code ":datum"] " scalar element of the data, the " [:code ":predicate"] " test function element of the specification, the " [:code ":path"] " addressing each in their respective structures, and the " [:code "valid?"] " result of applying the predicate function to the datum."]

 [:p "What if there's a length mis-match between the data and the specification? Motto #3 tells us that validation ignores un-paired datums. Let's look at the " [:code "all-paths"] " for that situation."]
 [:pre
  [:code ";; data vector containing an integer, a symbol, and a character"]
  [:br]
  (print-form-then-eval "(all-paths [42 \"abc\" 22/7])")
  [:br]
  [:br]
  [:code ";; specification vector containing one predicate"]
  [:br]
  (print-form-then-eval "(all-paths [int?])" 20 30)]

 [:p "After discarding the root collections at path " [:code "[]"] " we find the only scalar+predicate pair at path " [:code "[0]"] ", and that's the only pair that " [:code "validate-scalars"] " looks at."]

 [:pre (print-form-then-eval "(validate-scalars [42 \"abc\" 22/7] [int?])" 40 40)]

 [:p "Only scalar " [:code "42"] " in the data vector has a corresponding predicate " [:code "int?"] " in the specification vector, so the validation report contains only one entry. The second and third scalars, " [:code "\"abc\""] " and " [:code "22/7"] ", are ignored."]

 [:p "What about the other way around? More predicates in the specification than scalars in the data?"]
 [:pre
  [:code ";; data vector containing one scalar, an integer"]
  [:br]
  (print-form-then-eval "(all-paths [42])" 20 80)
  [:br]
  [:br]
  [:code ";; specification vector containing three predicates"]
  [:br]
  (print-form-then-eval "(all-paths [int? string? ratio?])" 60 50)]

 [:p "Motto #3 reminds us that validation ignores un-paired predicates. Only the predicate " [:code "int?"] " at path " [:code "[0]"] " in the specification vector shares its path with a scalar in the data vector, so that's the only scalar+predicate pair that " [:code "validate-scalars"] " processes."]

 [:pre (print-form-then-eval "(validate-scalars [42] [int? string? ratio?])" 40 40)]
 [:p [:code "validate-scalars"] " ignores both " [:code "string?"] " and " [:code "ratio?"] " within the specification vector because the data vector does not contain scalars at their respective paths."]

 [:p "Validating scalars contained within a map proceeds similarly. Let's send this map, our data, to " [:code "all-paths"] "."]
 [:pre (print-form-then-eval "(all-paths {:x 42 :y \"abc\" :z 22/7})" 40 50)]

 [:p "Four elements: the root collection (a map), and three scalars. Then we'll do the same for this map, our specification, which mimics the shape of the data (Motto #2), by also being a map with the same keys."]
 [:pre (print-form-then-eval "(all-paths {:x int? :y string? :z ratio?})" 45 52)]

 [:p "Again four elements: the root collection (a map), and three predicates. Note that each predicate shares a path with one of the scalars in the data map. Invoking " [:code "validate-scalars"] " with the data map followed by the specification map…"]

 [:pre (print-form-then-eval "(validate-scalars {:x 42 :y \"abc\" :z 22/7} {:x int? :y string? :z ratio?})" 55 40)]

 [:p "…we can see that "
  [:ul
   [:li [:code "42"] " at path " [:code "[:x]"] " in the data satisfies " [:code "int?"] " at path " [:code "[:x]"] " in the specification, "]
   [:li [:code "\"abc\""] " at path " [:code "[:y]"] " in the data satisfies " [:code "string?"] " at path " [:code "[:y]"] " in the specification, and"]
   [:li [:code "22/7"] " at path " [:code "[:z]"] " in the data satisfies " [:code "ratio?"] " at path " [:code "[:z]"] " in the specification. "]]

  "Because the specification mimics the shape of the data (i.e., the specification is a map with the same keys), " [:code "validate-scalars"] " is able to infer how to apply each predicate to the intended datum."]

 [:p [:code "validate-scalars"] " can only operate with complete scalar+predicate pairs. It ignores un-paired scalars and un-paired predicates. Since maps are not sequential, we can illustrate both scenarios with one example."]
 [:pre
  [:code ";; data with keys :x and :q"]
  [:br]
  (print-form-then-eval "(all-paths {:x 42 :q \"foo\"})")
  [:br]
  [:br]
  [:code ";; specification with keys :x and :s"]
  [:br]
  (print-form-then-eval "(all-paths {:x int? :s decimal?})" 40 50)]

 [:p "Notice that the two maps contain only a single scalar/predicate that share a path, " [:code "[:x]"] ". The other two elements, scalar " [:code "\"foo\""] " at path " [:code "[:q]"] " in the data map and predicate " [:code "decimal?"] " at path " [:code "[:s]"] " in the specification map, do not share a path with an element of the other. Those later two will be ignored."]

 [:pre (print-form-then-eval "(validate-scalars {:x 42 :q \"foo\"} {:x int? :s decimal?})" 45 40)]
 [:p [:code "validate-scalars"] " found only a single complete scalar+predicate pair located at path " [:code "[:x]"] ", so it applied " [:code "int?"] " to " [:code "42"] ", which returns satisfied."]

 [:hr]
 [:p "I am curious to know whether the features to this point are sufficient for Clojure programmers to get 40% of their specification and validation work done. 50%? "]
 [:p "Onward…"]
 [:hr]

 [:p "Scalars contained in nested collections are treated accordingly: predicates from the specification are only applied to scalars in the data which share their path. Non-scalars are ignored. Here are the paths for a simple nested data vector with some scalars."]
 [:pre (print-form-then-eval "(all-paths [42 [\"abc\" [22/7]]])")]

 [:p "Six total elements: three vectors, which " [:code "validate-scalars"] " will ignore, and three scalars. And here are the paths for a similarly-shaped nested specification."]
 [:pre
  [:code ";;                         v --- char? predicate will be notable during validation in a moment"]
  [:br]
  (print-form-then-eval "(all-paths [int? [string? [char?]]])" 45 55)]

 [:p "Again, six total elements: three vectors that will be ignored, plus three predicates. When we validate…"]

 [:pre (print-form-then-eval "(validate-scalars [42 [\"abc\" [22/7]]] [int? [string? [char?]]])" 55 40)]

 [:p "Three complete pairs of scalars and predicates."
  [:ul
   [:li [:code "42"] " at path " [:code "[0]"] " in the data satisfies predicate " [:code "int?"] " at path " [:code "[0]"] " in the specification,"]
   [:li [:code "\"abc\""] " at path " [:code "[1 0]"] " in the data satisfies predicate " [:code "string?"] " at path " [:code "[1 0]"] " in the specification,"]
   [:li [:code "22/7"] " at path " [:code "[1 1 0]"] " in the data " [:strong "does not satisfy"] " predicate " [:code "char?"] " at path " [:code "[1 1 0]"] " in the specification."]]
  [:a {:href "#valid-thorough"} "Later"]  ", we'll see that the lone, unsatisfied " [:code "char?"] " predicate would cause an entire " [:code "valid?"] " operation to return " [:code "false"] "."]

 [:p "When the data contains scalars that are not paired with predicates in the specification, they are not validated."]
 [:pre (print-form-then-eval "(validate-scalars [42 [\"abc\" [22/7]]] [int? [string?]])" 50 40)]

 [:p "Only the " [:code "42"] " and " [:code "\"abc\""] " are paired with predicates, so " [:code "validate-scalars"] " only validated those two scalars. " [:code "22/7"] " is unpaired, and therefore ignored. Likewise…"]

 [:pre (print-form-then-eval "(validate-scalars [42] [int? [string? [char?]]])" 45 40)]

 [:p "…" [:code "string?"] " and " [:code "char?"] " are not paired, and therefore ignored. When the data contains only one scalar, but the specification contains more predicates, " [:code "validate-scalars"] " only validates the complete scalar+predicate pairs."]

 [:p "Mis-matched, nested maps sing the same song. Here are the paths for all elements in a nested data map and a nested specification map."]
 [:pre
  [:code ";; data"]
  [:br]
  (print-form-then-eval "(all-paths {:x 42 :y {:z 22/7}})" 50 50)
  [:br]
  [:br]
  [:code ";; specification"]
  [:br]
  (print-form-then-eval "(all-paths {:x int? :y {:q string?}})" 50 50)]

 [:p "Notice that only the scalar " [:code "42"] " in the data and the predicate " [:code "int?"] "  in the specification share a path " [:code "[:x]"] ". " [:code "22/7"] " in the data and " [:code "string?"] " in the specification are un-paired."]

 [:pre (print-form-then-eval "(validate-scalars {:x 42 :y {:z 22/7}} {:x int? :y {:q string?}})" 55 40)]
 [:p [:code "validate-scalars"] " dutifully applies the only scalar+predicate pair, and tells us that " [:code "42"] " is indeed an integer."]

 [:p "One final illustration: what happens if there are zero scalar+predicate pairs."]
 [:pred (print-form-then-eval "(validate-scalars {:x 42} {:y int?})")]

 [:p "The only scalar, at the path " [:code "[:x]"] " in the data, does not share a path with the only predicate, at path " [:code "[:y]"] " in the specification. No validations were performed."]

 [:p "A Speculoos scalar specification says " [:em "This data element may or may not exist, but if it does, it must satisfy this predicate."] " See " [:a {:href "#valid-thorough"} " this later section"] " for functions that return high-level " [:code "true/false"] " validation summaries and for functions that ensure validation of " [:em "every"] " scalar element."]]