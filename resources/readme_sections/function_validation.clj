[:section#function-validation
 [:h2 "Specifying and Validating Functions"]

 [:p "Being able to validate Clojure data enables us to check the usage and behavior of functions."]

 [:ol
  [:li [:strong "Validating arguments"] " Speculoos can validate any property of the arguments passed to a function when it is invoked. We can ask questions like " [:em "Is the argument passed to the function a number?"] ", a scalar validation, and " [:em "Are there an even number of arguments?"] ", a collection validation."]

  [:li [:strong "Validating return values"] " Speculoos can validate any property of the value returned by a function. We can ask questions like " [:em "Does the function return a four-character string?"] ", a scalar validation, and " [:em "Does the function return a map containing keys " [:code ":x"] " and " [:code ":y"]] ", a collection validation."]

  [:li [:strong "Validating function correctness"] " Speculoos can validate the correctness of a function in two ways."

   [:ul
    [:li "Speculoos can validate the " [:em "relationships"] " between the arguments and the function's return value. We can ask questions like " [:em "Is each of the three integers in the return value larger than the three integers in the arguments?"] ", a scalar validation, and " [:em "Is the return sequence the same length as the argument sequence, and are all the elements in reverse order?"] ", a collection validation."]

    [:li "Speculoos can " [:em "exercise"] " a function. This allows us to check " [:em "If we give this function one thousand randomly-generated valid inputs, does the function always produce a valid return value?"] " Exercising functions with randomly-generated samples is described in the "] [:a {:href "#exercising"} "next section"] "."]]]

 [:p "None of those six checks are strictly required. Speculoos will happily validate using only the specifications we provide."]

 [:h3#fn-args "1. Validating Function Arguments"]

 [:p "When we invoke a function with a series of arguments, that series of values forms a sequence, which Speculoos can validate like any other heterogeneous, arbitrarily-nested data structure. Speculoos offers " [:a {:href "#explicit"} "a trio"] " of function-validating functions with differing levels of explicitness. We'll be primarily using " [:code "validate-fn-with"] " because it is the most explicit of the trio, and we can most easily observe what's going on."]

 [:p "Let's pretend we want to validate the arguments to a function " [:code "sum-three"] " that expects three integers and returns their sum."]

 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-with]])")
  [:br]
  [:br]
  (print-form-then-eval "(defn sum-three [x y z] (+ x y z))")
  [:br]
  [:br]
  (print-form-then-eval "(sum-three 1 20 300)")]

 [:p "The argument list is a " [:em "sequence"] " of values, in this example, a sequential thing of three integers. We can imagine a " [:a {:href "#scalar-validation"} "scalar specification"] " for just such a sequence."]

 [:pre [:code "[int? int? int?]"]]

 [:p "When using " [:code "validate-fn-with"] ", we supply the function name, a map containing zero or more specifications, and some trailing " [:code "&-args"] " as if they had been supplied directly to the function. Here's the function signature, formatted the way we'll be seeing in the upcoming discussion. The function name will appear in the top row (i.e., first argument), a specification organizing map in the second row, followed by zero or more arguments to be supplied to the function being validated."]

 [:pre
  [:code "(validate-fn-with "]
  [:code [:em "function-name"]] [:br]
  [:code [:em "                  specification-organizing-map"]] [:br]
  [:code [:em "                  argument-1"]] [:br]
  [:code [:em "                  argument-2"]] [:br]
  [:code "                  ⋮"] [:br]
  [:code [:em "                  argument-n"]]
  [:code ")"]]

 [:p "Speculoos can validate five aspects of a function using up to five specifications, each specification associated in that map to a particular key. We'll cover each of those five aspects in turn. To start, we want to specify the " [:em "argument scalars"] "."]

 [:p "Instead of individually passing each of those five specifications to " [:code "validate-fn-with"] " and putting " [:code "nil"] " placeholders where we don't wish to supply a specification, we organize the specifications. To do so, we associate the arguments' scalar specification to the qualified key " [:code ":speculoos/arg-scalar-spec"] "."]

 [:pre [:code "{:speculoos/arg-scalar-spec [int? int? int?]}"]]

 [:p "Then, we validate the arguments to " [:code "sum-three"] " like this."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?]} 1 20 300)" 75 45)]

 [:p "The arguments conformed to the scalar specification, so " [:code "validate-fn-with"] " returns the value produced by " [:code "sum-three"] ". Let's intentionally invoke " [:code "sum-three"] " with one invalid argument by swapping integer " [:code "1"] " with a floating-point " [:code "1.0"] "."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?]} 1.0 20 300)" 75 45)]

 [:p "Hey, that kinda " [:a {:href "#scalar-validation"} "looks familiar"] ". It looks a lot like something " [:code "validate-scalars"] " would emit if we filtered to keep only the invalids. We see that " [:code "1.0"] " at path " [:code "[0]"] " failed to satisfy its " [:code "int?"] " scalar predicate. We can also see that the function specification type is " [:code ":speculoos/argument"] ". Since Speculoos can validate scalars and collections of both arguments and return values, that key-val is a little signpost to help us pinpoint exactly what and where. Let's invoke " [:code "sum-three"] " with a second invalid argument, a ratio " [:code "22/7"] " instead of integer " [:code "300"] "."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?]} 1.0 20 22/7)" 75 45)]

 [:p "In addition to the invalid " [:code "1.0"] " at path " [:code "[0]"] ", we see that " [:code "22/7"] " at path " [:code "[2]"] " also fails to satisfy its " [:code "int?"] " scalar predicate. The scalar predicate's path in the scalar specification is the same as the path of the " [:code "22/7"] " in the " [:code "[1.0 20 22/7]"] " sequence of arguments. Roughly, " [:code "validate-fn-with"] " is doing something like this…"]

 [:pre (print-form-then-eval "(speculoos.core/only-invalid (validate-scalars [1.0 20 22/7] [int? int? int?]))" 45 45)]

 [:p "…validating scalars with " [:code "validate-scalars"] " and keeping only the invalids."]

 [:p "Okay, we see that term " [:em "scalar"] " buzzing around, so there must be something else about validating collections. Yup. We can also " [:a {:href "#collection-validation"} "validate collection"] " properties of the argument sequence. Let's specify that the argument sequence must contain three elements, using a custom collection predicate, " [:code "count-3?"] "."]

 [:pre (print-form-then-eval "(defn count-3? [v] (= 3 (count v)))")]

 [:p "Let's simulate the collection validation first. Remember, collection predicates are applied to their parent containers, so " [:code "count-3?"] " must appear within a collection so that it'll be paired with the data's containing collection."]

 [:pre (print-form-then-eval "(validate-collections [1 20 30] [count-3?])" 40 85)]

 [:p "That result fits with " [:a {:href "#collection-validation"} "our discussion"] " about validating collections."]

 [:p "Next, we'll associate that collection specification into our function specification map at " [:code ":speculoos/arg-collection-spec"] " and invoke " [:code "validate-fn-with"] " with three valid arguments."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-collection-spec [count-3?]} 1 20 300)" 75 85)]

 [:p "The argument sequence satisfies our collection specification, so " [:code "sum-three"] " returns the expected value. Now let's repeat, but with an additional argument, " [:code "4000"] ", that causes the argument sequence to violate its collection predicate."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-collection-spec [count-3?]} 1 20 300 4000)" 75 85)]

 [:p "This four-element argument sequence, " [:code "[1 20 300 4000]"] ", failed to satisfy our " [:code "count-3?"] " collection predicate, so " [:code "validate-fn-with"] " emitted a validation report."]

 [:p "Note #1: Invoking " [:code "sum-three"] " with four arguments would normally trigger an arity exception. " [:code "validate-fn-with"] " catches the exception and validates as much as it can."]

 [:p "Note #2: Don't specify and validate the " [:em "type"] " of the arguments container, i.e., " [:code "vector?"] ". That's an implementation detail and not guaranteed."]

 [:p "Let's get fancy and combine an argument scalar specification and an argument collection specification. Outside of the context of checking a function, that " [:a {:href "#combo"} "combo validation"] " would look like this: data is the first argument to " [:code "validate"] ", then the scalar specification on the next row, then the collection specification on the lower row."]

 [:pre (print-form-then-eval "(speculoos.core/only-invalid (validate [1.0 20 22/7 4000] [int? int? int?] [count-3?]))" 45 55)]

 [:p  "Let's remember: scalars and collections are " [:em "always"] " validated separately. " [:code "validate"] " is merely a convenience function that does both a scalar validation, then a collection validation, in discrete processes, with a single function invocation. Each of the first three scalars that paired with a scalar predicate were validated as scalars. The first and third scalars, " [:code "1.0"] " and " [:code "22/7"] ", failed to satisfy their respective predicates. The fourth argument, " [:code "4000"] ", was not paired with a scalar predicate and was therefore ignored (" [:a {:href "#mottos"} "Motto #3"] "). Then, the argument sequence as a whole was validated against the collection predicate " [:code "count-3?"] "."]

 [:p [:code "validate-fn-with"] " performs substantially that combo validation. We'll associate the " [:strong "arg"] "ument " [:strong "scalar"] " " [:strong "spec"] "ification with " [:code ":speculoos/arg-scalar-spec"] " and the " [:strong "arg"] "ument " [:strong "collection"] " " [:strong "spec"] "fication with " [:code ":speculoos/arg-collection-spec"] " and pass the invalid, four-element argument sequence."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?] :speculoos/arg-collection-spec [count-3?]} 1.0 20 22/7 4000)" 95 75)]

 [:p "Just as in the " [:code "validate"] " simulation, we see three items fail to satisfy their predicates. Scalars " [:code "1.0"] " and " [:code "22/7"] " are not integers, and the argument sequence as a whole, " [:code "[1.0 20 22/7 4000]"] ", does not contain exactly three elements, as required by its collection predicate, " [:code "count-3?"] "."]


 [:h3#fn-ret "2. Validating Function Return Values"]

 [:p "Speculoos can also validate values returned by a function. Reusing our " [:code "sum-three"] " function, and going back to valid inputs, we can associate a " [:strong "ret"] "urn " [:strong "scalar"] " " [:strong "spec"] "ification into " [:code "validate-fn-with"] "'s specification map to key " [:code ":speculoos/ret-scalar-spec"] ". Let's stipulate that the function returns an integer. Here's how we pass that specification to " [:code "validate-fn-with"] "."]

 [:pre [:code "{:speculoos/ret-scalar-spec int?}"]]

 [:p  "And now, the function return validation."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/ret-scalar-spec int?} 1 20 300)" 55 55)]

 [:p "The return value " [:code "321"] " satisfies " [:code "int?"] ", so " [:code "validate-fn-with"] " returns the computed sum."]

 [:p "What happens when the return value is invalid? Instead of messing up " [:code "sum-three"] "'s definition, we'll merely alter the scalar predicate. Instead of an integer, we'll stipulate that " [:code "sum-three"] " returns a string with scalar predicate " [:code "string?"] "."]

 [:pre#nil (print-form-then-eval "(validate-fn-with sum-three {:speculoos/ret-scalar-spec string?} 1 20 300)" 55 55)]

 [:p "Very nice. " [:code "sum-three"] " computed, quite correctly, the sum of the three arguments. But we gave it a bogus return scalar specification that claimed it ought to be a string, which integer " [:code "321"] " fails to satisfy."]

 [:p "Did you happen to notice the " [:code "path"] "? We haven't yet encountered a case where a path is " [:code "nil"] ". In this situation, the function returns a 'bare' scalar, not contained in a collection. Speculoos can validate a bare scalar when that bare scalar is a function's return value."]

 [:p "Let's see how to validate a function when the return value is a collection of scalars. We'll write a new function, " [:code "enhanced-sum-three"] ", that returns four scalars: the three arguments and their sum, all contained in a vector."]

 [:pre
  (print-form-then-eval "(defn enhanced-sum-three [x y z] [x y z (+ x y z)])" 25 25)
  [:br]
  [:br]
  (print-form-then-eval "(enhanced-sum-three 1 20 300)")]

 [:p "Our enhanced function now returns a vector of four elements. Let's remind ourselves how we'd manually validate that return value. If we decide we want " [:code "enhanced-sum-three"] " to return four integers, the scalar specification would look like this."]

 [:pre [:code "[int? int? int? int?]"]]

 [:p "The scalar specification is shaped like our data (Motto #2)."]

 [:p "And the manual validation would look like this, with the data on the upper row, the scalar specification on the lower row."]

 [:pre (print-form-then-eval "(validate-scalars [1 20 300 321] [int? int? int? int?])" 45 45)]

 [:p "Four paired scalars and scalar predicates yield four validation results. Let's see what happens when we validate the function return scalars."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-scalar-spec [int? int? int? int?]} 1 20 300)")]

 [:p "Since we fed " [:code "validate-fn-with"] " a specification that happens to agree with those arguments, " [:code "enhanced-sum-three"] " returns its computed value, " [:code "[1 20 300 321]"] "."]

 [:p "Let's stir things up. We'll change the return scalar specification to something we know will fail: The first scalar a character, the final scalar a boolean."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-scalar-spec [char? int? int? boolean?]} 1 20 300)")]

 [:p [:code "enhanced-sum-three"] "'s function body remained the same, and we fed it the same integers as before, but we fiddled with the return scalar specification so that the returned vector contained two scalars that failed to satisfy their respective predicates. " [:code "1"] " at path " [:code "[0]"] " does not satisfy its wonky scalar predicate " [:code "char?"] " at the same path. And " [:code "321"] " at path " [:code "[3]"] " does not satisfy fraudulent scalar predicate " [:code "boolean?"] " that shares its path."]

 [:p "Let's set aside validating scalars for a moment and validate a facet of " [:code "enhanced-sum-three"] "'s return collection. First, we'll do a manual demonstration with " [:code "validate-collections"]". Remember: Collection predicates apply to their immediate parent container. We wrote " [:code "enhanced-sum-three"] " to return a vector, but to make the validation produce something interesting to look at, we'll pretend we're expecting a list."]

 [:pre (print-form-then-eval "(validate-collections [1 20 300 321] [list?])" 40 45)]

 [:p "That collection validation aligns with our understanding. " [:code "[1 20 300 321]"] " is not a list. The " [:code "list?"] " collection predicate at path " [:code "[0]"] " in the specification was paired with the thing found at path " [:code "(drop-last [0])"] " in the data, which in this example is the root collection. We designed " [:code "enhanced-sum-three"] " to yield a vector, which failed to satisfy predicate " [:code "list?"] "."]

 [:p "Let's toss that collection specification at " [:code "validate-with-fn"] " and have it apply to " [:code "enhanced-sum-three"] "'s return value, which won't satisfy. We pass the " [:strong "ret"] "urn " [:strong "collection spec"] "ification by associating it to the key " [:code ":speculoos/ret-collection-spec"] "."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-collection-spec [list?]} 1 20 300)")]

 [:p "Similarly to the manual collection validation we previously performed with " [:code "validate-collections"] ", we see that " [:code "enhanced-sum-three"] "'s return vector " [:code "[1 20 300 321]"] " fails to satisfy its " [:code "list?"] " collection predicate."]

 [:p "A scalar validation followed by an independent collection validation allows us to check every possible aspect that we could want. Now that we've seen how to individually validate " [:code "enhance-sum-three"] "'s return scalars and return collections, we know how to do both with one invocation."]

 [:p "Remember Motto #1: Validate scalars separately from validating collections. Speculoos will only ever do one or the other, but " [:code "validate"] " is a " [:a {:href "#combo"} "convenience function"] " that performs a scalar validation immediately followed by a collection validation. We'll re-use the scalar specification and collection specification from the previous examples."]

 [:pre (print-form-then-eval "(speculoos.core/only-invalid (validate [1 20 300 321] [char? int? int? boolean?] [list?]))" 45 45)]

 [:p [:code "only-invalid"] " discards the validations where the predicates are satisfied, leaving only the invalids. Two scalars failed to satisfy their scalar predicates. Integer " [:code "1"] " at path " [:code "[0]"] " in the data fails to satisfy scalar predicate " [:code "char?"] " at path " [:code "[0]"] " in the scalar specification. Integer " [:code "321"] " fails to satisfy scalar predicate " [:code "boolean?"] " at path " [:code "[3]"] " in the scalar specification. Finally, our root vector " [:code "[1 20 300 321]"] " located at path " [:code "[]"] " fails to satisfy the collection predicate " [:code "list?"] " at path " [:code "[0]"] "."]

 [:p "Now that we've seen the combo validation done manually, let's validate " [:code "enhanced-sum-three"] "'s return in the same way. Here we see the importance of organizing the specifications in a container instead of passing them as individual arguments: it keeps our invocation neater."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-scalar-spec [char? int? int? boolean?] :speculoos/ret-collection-spec [list?]} 1 20 300)")]

 [:p [:code "validate-fn-with"] "'s validation is substantially the same as the one " [:code "validate"] " produced in the previous example, except now, the data comes from invoking " [:code "enhanced-sum-three"] ". Two scalar invalids and one collection invalid. Integer " [:code "1"] " fails to satisfy scalar predicate " [:code "char?"] ", integer " [:code "321"] " fails to satisfy scalar predicate " [:code "boolean?"] ", and the entire return vector " [:code "[1 20 300 321]"] " fails to satisfy collection predicate " [:code "list?"] "."]

 [:p "Okay. I think we're ready to put together all four different function validations we've so far seen. We've seen…"]

 [:ul
  [:li "a function argument scalar validation,"]
  [:li "a function argument collection validation,"]
  [:li "a function return scalar validation, and"]
  [:li "a function return collection validation."]]

 [:p "And we've seen how to combine both function argument validations, and how to combine both function return validations. Now we'll combine all four validations into one " [:code "validate-fn-with"] " invocation."]

 [:p "Let's review our ingredients. Here's our " [:code "enhanced-sum-three"] " function."]

 [:pre (print-form-then-eval "(defn enhanced-sum-three [x y z] [x y z (+ x y z)])" 25 25)]

 [:p [:code "enhanced-sum-three"] " accepts three number arguments and returns a vector of those three numbers with their sum appended to the end of the vector. Technically, Clojure would accept any numeric thingy for " [:code "x"] ", " [:code "y"] ", and " [:code "z"] ", but for illustration purposes, we'll make our scalar predicates something non-numeric so we can see something interesting in the validation reports."]

 [:p "With that in mind, we pretend that we want to validate the function's argument sequence as a string, followed by an integer, followed by a symbol. The function scalar specification will be…"]

 [:pre [:code "[string? int? symbol?]"]]

 [:p "To allow " [:code "enhanced-sum-three"] " to calculate a result, we'll supply three numeric values, two of which will not satisfy that argument scalar specification."]

 [:p "So that it produces something interesting, we'll make our function argument collection specification also complain. First, we'll write a collection predicate."]

 [:pre (print-form-then-eval "(defn length-2? [v] (= 2 (count v)))")]

 [:p "We know for sure that the argument sequence will contain three values, so predicate " [:code "length-2?"] " will produce something interesting to see."]

 [:p "During collection validation, predicates apply to the collection in the data that corresponds to the collection that contains the predicate. We want our predicate, " [:code "length-2?"] " to apply to the argument sequence, so we'll insert it into a vector. Our argument collection specification will look like this."]

 [:pre [:code "[length-2?]"]]

 [:p "Jumping to " [:code "enhanced-sum-three"] "'s output side, we expect a vector of four numbers. Again, we'll craft our function return scalar specification to contain two predicates that we know won't be satisfied because those scalar predicates are looking for something non-numeric."]

 [:pre [:code "[char? int? int? boolean?]"]]

 [:p "We know " [:code "enhanced-sum-three"] " will return a vector containing four integers, but the " [:code "char?"] " and " [:code "boolean?"] " will give us something to look at."]

 [:p "Finally, since we defined " [:code "enhanced-sum-three"] " to return a vector, we'll make the function return collection specification look for a list."]

 [:pre [:code "[list?]"]]

 [:p "Altogether, those four specification are organized like this."]

 [:pre [:code "{:speculoos/arg-scalar-spec     [string? int? symbol?]\n :speculoos/arg-collection-spec [#(= 2 (count %))]\n :speculoos/ret-scalar-spec     [char? int? int? boolean?]\n :speculoos/ret-collection-spec [list?]}"]]

 [:p "It's time to see what we've assembled."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/arg-scalar-spec[string? int? symbol?] :speculoos/arg-collection-spec [length-2?] :speculoos/ret-scalar-spec [char? int? int? boolean?] :speculoos/ret-collection-spec [list?]} 1 20 300)" 80 80)]

 [:p "We've certainly made a mess of things. But it'll be understandable if we examine the invalidation report piece by piece. The first thing to know is that we have already seen each of those validations before in the previous examples, so we could always scroll back to those examples above and see the validations in isolation."]

 [:p "We see six non-satisfied predicates:"
  [:ul
   [:li "Scalar " [:code "1"] " in the arguments sequence fails to satisfy scalar predicate "[:code "string?"] " in the argument scalar specification." ]
   [:li "Scalar " [:code "300"] " in the arguments sequence fails to satisfy scalar predicate "[:code "symbol?"] " in the argument scalar specification."]
   [:li "The argument sequence " [:code "[1 20 300]"] " fails to satisfy collection predicate " [:code "length-2?"] " in the argument collection specification." ]
   [:li "Scalar " [:code "1"] " in the return vector fails to satisfy scalar predicate " [:code "char?"] " in the return scalar specification."]
   [:li "Scalar " [:code "321"] " in the return vector fails to satisfy scalar predicate " [:code "boolean?"] " in the return scalar specification."]
   [:li "The return vector " [:code "[1 20 300 321]"] " fails to satisfy collection predicate " [:code "list?"] " in the return collection specification."]]]

 [:p "Also note that the validation entries have a " [:code ":fn-spec-type"] " entry associated to either " [:code ":speculoos/return"] " or " [:code ":speculoos/argument"] ", which tells us where a particular invalid was located. There may be a situation where indistinguishable invalid datums appear in both the arguments and returns. In this case, integer " [:code "1"] " was an invalid datum at path " [:code "[0]"] " for both the argument sequence and the return vector. Keyword " [:code ":fn-spec-type"] " helps resolve the ambiguity."]


 [:h3#fn-correctness "3. Validating Function Correctness"]

 [:p "So far, we've seen how to validate function argument sequences and function return values, both their scalars, and their collections. Validating function argument sequences allows us to check if the function was invoked properly. Validating function return values gives a limited ability to check the internal operation of the function."]

 [:p " If we want another level of thoroughness for checking correctness, we can specify and validate the " [:strong "relationships"] " between the functions arguments and return values. Perhaps we'd like to be able to express " [:em "The return value is a collection, with all the same elements as the input sequence."] " Or " [:em "The return value is a concatenation of the even indexed elements of the input sequence."] " Speculoos' term for this action is " [:em "validating function argument and return value relationship"] "."]

 [:p "Let's pretend I wrote a reversing function, which accepts a sequential collection of elements and returns those elements in reversed order. If we give it…"]

 [:pre [:code "[11 22 33 44 55]"]]

 [:p "…my reversing function ought to return…"]

 [:pre [:code "[55 44 33 22 11]"]]

 [:p "Here are some critical features of that process that relate the reversing function's arguments to its return value."]

 [:ul
  [:li "The return collection is the same length as the input collection."]
  [:li "The return collection contains all the same elements as the input collection."]
  [:li "The elements of the return collection appear in reverse order from their positions in the input collection."]]

 [:p "Oops. I must've written it before I had my morning coffee."]

 [:pre
  (print-form-then-eval "(defn broken-reverse [v] (conj v 9999))")
  [:br]
  [:br]
  (print-form-then-eval "(broken-reverse [11 22 33 44 55])" 45 35)]

 [:p "Pitiful. We can see by eye that " [:code "broken-reverse"] " fulfilled none of the three relationships. The return collection is not the same length, contains additional elements, and is not reversed. Let's codify that pitifulness."]

 [:p "First, we'll write three " [:a {:href "#relationship"} "relationship functions"] ". Relationship functions are a lot like predicates. They return a truthy or falsey value, but instead consume two things instead of one. The function's argument sequence is passed as the first thing and the function's return value is passed as the second thing."]

 [:p "The first predicate tests " [:em "Do two collections contain the same number of elements?"] ""]

 [:pre
  (print-form-then-eval "(defn same-length? [v1 v2] (= (count v1) (count v2)))")
  [:br]
  [:br]
  (print-form-then-eval "(same-length? [11 22 33 44 55] [11 22 33 44 55])" 45 80)
  [:br]
  [:br]
  (print-form-then-eval "(same-length? [11 22] [11 22 33 44 55])" 35 80)]

 [:p "When supplied with two collections whose counts are the same, predicate " [:code "same-length?"] " returns " [:code "true"] "."]

 [:p "The second predicate tests " [:em "Do two collections contain the same elements?"]]

 [:pre
  (print-form-then-eval "(defn same-elements? [v1 v2] (= (sort v1) (sort v2)))")
  [:br]
  [:br]
  (print-form-then-eval "(same-elements? [11 22 33 44 55] [55 44 33 22 11])" 45 80)
  [:br]
  [:br]
  (print-form-then-eval "(same-elements? [11 22 33 44 55] [55 44 33 22 9999])" 45 80)]

 [:p "When supplied with two collections which contain the same elements, predicate " [:code "same-elements?"] " returns " [:code "true"] "."]

 [:p "The third predicate tests " [:em "Do the elements of one collection appear in reversed order when compared to another collection?"]]

 [:pre
  (print-form-then-eval "(defn reversed? [v1 v2] (= v1 (reverse v2)))")
  [:br]
  [:br]
  (print-form-then-eval "(reversed? [11 22 33 44 55] [55 44 33 22 11])" 40 80)
  [:br]
  [:br]
  (print-form-then-eval "(reversed? [11 22 33 44 55] [11 22 33 44 55])" 40 80)]

 [:p "When supplied with two collections, predicate " [:code "reversed?"] " returns " [:code "true"] " if the elements of the first collection appear in the reverse order relative to the elements of the second collection."]

 [:p [:code "same-length?"] ", " [:code "same-element?"] ", " [:code "reversed?"] " all consume two sequential things and test a relationship between the two. If their relationship is satisfied, they signal " [:code "true"] ", if not, then they signal " [:code "false"] ". They are all three gonna have something unkind to say about " [:code "broken-reverse"] "."]

 [:p "Now that we've established a few relationships, we need to establish " [:em "where"] " to apply those relationship tests. Checking " [:code "broken-reverse"] "'s argument/return relationships with " [:code "same-length?"] ", " [:code "same-elements?"] ", and " [:code "reversed?"] " will be fairly straightforward: For each predicate, we'll pass the first argument (itself a collection), and the return value (also a collection). But some day, we might want to check a more sophisticated relationship that needs to extract some slice of the argument and/or slice of the return value. Therefore, we must declare a path to the slices we want to check. Of the return value, we'd like to check the root collection, so the return value's path is merely " [:code "[]"] "."]

 [:p "When we consider how to extract the arguments, there's one tricky detail we must accommodate. The " [:code "[11 22 33 44 55]"] " vector we're going to pass to " [:code "broken-reverse"] " is itself contained in the argument sequence. Take a look."]

 [:pre
  (print-form-then-eval "(defn arg-passthrough [& args] args)")
  [:br]
  [:br]
  (print-form-then-eval "(arg-passthrough [11 22 33 44 55])")]

 [:p "To extract " [:code "[11 22 33 44 55]"] ", the path will need to be " [:code "[0]"] "."]

 [:pre (print-form-then-eval "(nth (arg-passthrough [11 22 33 44 55]) 0)" 45 45)]

 [:p "When invoked with paths " [:code "[0]"] " and " [:code "[]"] ", respectively for the arguments and returns, " [:em "validate argument-return relationship"] " does something like this."]

 [:pre (print-form-then-eval "(same-length? (get-in [[11 22 33 44 55]] [0]) (get-in [11 22 33 44 55 9999] []))" 55 55)]

 [:p "So here are the components to a single argument/return relationship validation."]

 [:ul
  [:li "A path to the interesting slice of the arguments. Example: " [:code "[0]"]]
  [:li "A path to the interesting slice of the return value. Example: " [:code "[]"]]
  [:li "A relationship function. Example: " [:code "same-length?"]]]

 [:p "We stuff all three of those items into a map, which will be used for a single relationship validation."]

 [:pre [:code "{:path-argument [0]\n :path-return []\n :relationship-fn same-length?}"]]

 [:p  "Within that map, both " [:code ":path-…"] " entries govern what slices of the argument and return are given to the relationship function. In this example, we want to extract the first item, at path " [:code "[0]"] ", of the argument sequence and the entire return value, at path " [:code "[]"] "."]

 [:p "We've written three argument/function relationships to test " [:code "broken-reverse"] ", so we'll need to somehow feed them to " [:code "validate-fn-with"] ". We do that by associating them into the organizing map with keyword " [:code ":speculoos/argument-return-relationships"] ". Notice the plural " [:em "s"] ". Since there may be more than one relationship, we collect them into a vector. For the moment, let's insert only the " [:code "same-length?"] " relationship."]

 [:pre [:code
"{:speculoos/argument-return-relationships [{:path-argument [0]
                                            :path-return []
                                            :relationship-fn same-length?}]}"]]

 [:p "Eventually, we'll test all three relationships, but for now, we'll focus on " [:code "same-length?"] "."]

 [:p "We're ready to validate."]

 [:pre (print-form-then-eval "(validate-fn-with broken-reverse {:speculoos/argument-return-relationships [{:path-argument [0] :path-return [] :relationship-fn same-length?}]} [11 22 33 44 55])" 45 70)]

 [:p "We supplied " [:code "broken-reverse"] " with a five-element vector, and it returned a six-element vector, failing to satisfy the specified " [:code "same-length?"] " relationship."]

 [:p "We wrote two other relationship functions, but " [:code "same-elements?"] " and " [:code "reversed?"] " are merely floating around in the current namespace. We did not send them to "  [:code "validate-fn-with"] ", so it checked only " [:code "same-length?"] ", which we explicitly supplied. Remember Motto #3: Un-paired predicates (or, relationships in this instance) are ignored."]

 [:p "Let's check all three relationships now. We explicitly supply the additional two relationship predicates, all with the same paths."]

 [:pre (print-form-then-eval "(validate-fn-with broken-reverse {:speculoos/argument-return-relationships [{:path-argument [0] :path-return [] :relationship-fn same-length?} {:path-argument [0] :path-return [] :relationship-fn same-elements?} {:path-argument [0] :path-return [] :relationship-fn reversed?}]} [11 22 33 44 55])" 45 70)]

 [:p [:code "broken-reverse"] " is truly broken. The " [:code "same-length?"] " result appears again, and then we see the two additional unsatisfied relationships because we added " [:code "same-elements?"] " and " [:code "reversed?"] ". " [:code "broken-reverse"] " returns a vector with more and different elements, and the order is not reversed."]

 [:p "Just for amusement, let's see what happens when we validate " [:code "clojure.core/reverse"] " with the exact same relationship specifications."]

 [:pre
  (print-form-then-eval "(reverse [11 22 33 44 55])")
  [:br]
  [:br]
  (print-form-then-eval "(validate-fn-with reverse {:speculoos/argument-return-relationships [{:path-argument [0] :path-return [] :relationship-fn same-length?} {:path-argument [0] :path-return [] :relationship-fn same-elements?} {:path-argument [0] :path-return [] :relationship-fn reversed?}]} [11 22 33 44 55])" 45 70)]

 [:p [:code "clojure.core/reverse "] " satisfies all three argument/return relationships, so " [:code "validate-fn-with"] " passes through the correctly-reversed output."]

 [:p "Not every function consumes a collection. Some functions consume a scalar value. Some functions return a scalar. And some functions have the audacity to do both. " [:code "validate-fn-with"] " can validate that kind of argument/return relationship. "]

 [:p "I'll warn you now, I'm planning on writing a buggy increment function. We could express two ideas about the argument/return relationship. First, a correctly-working increment function, when supplied with a number, " [:code "n"] ", ought to return a number that is larger than " [:code "n"] ". Second, a correctly-working return value ought to be " [:code "n"] " plus one. Let's specify those relationships."]

 [:p "The first predicate tests " [:em "Is the second number larger than the first number?"] " We don't need to write a special predicate for this job; " [:code "clojure.core"] " provides one that does everything we need."]

 [:pre
  #_ (print-form-then-eval "(defn larger-than? [n1 n2] (< n1 n2))")
  #_ [:br]
  #_ [:br]
  (print-form-then-eval "(< 99 100)")
  [:br]
  (print-form-then-eval "(< 99 -99)")]

 [:p "When supplied with two (or more) numbers, predicate " [:code "<"] " returns " [:code "true"] " if the second number is larger than the first number."]

 [:p "The second predicate tests " [:em "Is the second number equal to the first number plus one?"]]

 [:pre
  (print-form-then-eval "(defn plus-one? [n1 n2] (= (+ n1 1) n2))")
  [:br]
  [:br]
  (print-form-then-eval "(plus-one? 99 100)")
  [:br]
  (print-form-then-eval "(plus-one? 99 -99)")]

 [:p "When supplied with two numbers, predicate " [:code "plus-one?"] " returns " [:code "true"] " if the second number equals the sum of the first number and one."]

 [:p "Validating argument/return relationships requires us to declare which parts of the argument sequence and which parts of the return value to send to the relationship function. When we invoke the increment function with a single number, the number lives in the first spot of the argument sequence, so it will have a path of " [:code "[0]"] ". The increment function will return a 'bare' number, so a path is not really an applicable concept. We " [:a {:href "#nil"} "previously saw"] " how a " [:code "nil"] " path indicates a bare scalar, so now we can assemble the two relationship maps, one each for " [:code "<"] " and " [:code "plus-one?"] "."]

 [:pre
  [:code "{:path-argument [0]\n :path-return nil\n :relationship-fn <}"]
  [:br]
  [:br]
  [:code "{:path-argument [0]\n :path-return nil\n :relationship-fn plus-one?}"]]

 [:p "Now is a good time to write the buggy incrementing function."]

 [:pre
  (print-form-then-eval "(defn buggy-inc [n] (- n))")
  [:br]
  [:br]
  (print-form-then-eval "(buggy-inc 99)")]

 [:p "Looks plenty wrong. Let's see exactly how wrong."]

 [:pre (print-form-then-eval "(validate-fn-with buggy-inc {:speculoos/argument-return-relationships [{:path-argument [0] :path-return nil :relationship-fn <} {:path-argument [0] :path-return nil :relationship-fn plus-one?}]} 99)" 55 65)]

 [:p [:code "buggy-inc"] "'s return value failed to satisfy both relationships with its argument. " [:code "-99"] " is not larger than " [:code "99"] ", nor is it what we'd get by adding one to " [:code "99"] "."]

 [:p "Just to verify that our relationships are doing what we think they're doing, let's run the same thing on " [:code "clojure.core/inc"] "."]

 [:pre (print-form-then-eval "(validate-fn-with inc {:speculoos/argument-return-relationships [{:path-argument [0] :path-return nil :relationship-fn <} {:path-argument [0] :path-return nil :relationship-fn plus-one?}]} 99)" 55 45)]

 [:p [:code "inc"] " correctly returns " [:code "100"] " when invoked with " [:code "99"] ", so both " [:code "<"] " and " [:code "plus-one?"] " relationships are satisfied. Since all relationships were satisfied, the return value " [:code "100"] " passes through."]

 [:p "So far, the " [:code ":path-argument"] "s and the " [:code ":path-return"] "s have been similar between relationship specifications, but they don't need to be. I'm going to invent a really contrived example. " [:code "pull-n-put"] " and " [:code "pull-n-whoops"] " are both intended to pull out emails and phone numbers and stuff them into some output vectors. Here's our raw person data: emails and phone numbers."]

 [:pre
    (print-form-then-eval "(def person-1 {:email \"aragorn@sonofarath.org\" :phone \"867-5309\"})" 55 55)
  [:br]
  (print-form-then-eval "(def person-2 {:email \"vita@meatavegam.info\" :phone \"123-4567\"})" 55 55)
  [:br]
  (print-form-then-eval "(def person-3 {:email \"jolene@justbecauseyou.com\" :phone \"555-FILK\"})" 55 55)]

 [:p [:code "pull-n-put"] " is the correct implementation, producing correct results."]

 [:pre
  [:code ";; correct implementation"]
  [:br]
  [:br]
  (print-form-then-eval "(defn pull-n-put
                            [p1 p2 p3]
                            {:email-addresses [(p1 :email) (p2 :email) (p3 :email)]
                             :phone-numbers [(p1 :phone) (p2 :phone) (p3 :phone)]})" 35 55)
  [:br]
  [:br]
  [:br]
  [:code ";; intended results"]
  [:br]
  [:br]
  (print-form-then-eval "(pull-n-put person-1 person-2 person-3)" 55 55)]

 [:p [:code "pull-n-put"] " pulls out the email addresses and phone numbers and puts each at the proper place. However, " [:code "pull-n-whoops"] "…"]

 [:pre
  [:code ";; incorrect implementation"]
  [:br]
  [:br]
  (print-form-then-eval "(defn pull-n-whoops
                            [p1 p2 p3]
                            {:email-addresses [(p1 :phone) (p2 :phone) (p3 :phone)]
                             :phone-numbers [:apple :banana :mango]})")
  [:br]
  [:br]
  [:br]
  [:code ";; wrong results"]
  [:br]
  [:br]
  (print-form-then-eval "(pull-n-whoops person-1 person-2 person-3)" 55 55)]

 [:p "…does neither. " [:code "pull-n-whoops"] " puts the phone numbers where the email addresses ought to be and inserts completely bogus phone numbers."]

 [:p "We can specify a couple of relationships to show that " [:code "pull-n-whoops"] " produces a return value that does not validate. In a correctly-working implementation, the scalars aren't transformed, " [:em "per se"] ", merely moved to another location. So our relationship function will merely be equality, and the paths will do all the work."]

 [:p "Phone number " [:code "555-FILK"] " at argument path " [:code "[2 :phone]"] " ought to appear at return path " [:code "[:phone-numbers 2]"] ". That relationship specification looks like this."]

 [:pre [:code "{:path-argument [2 :phone]\n :path-return [:phone-numbers 2]\n :relationship-fn =}"]]

 [:p "Similarly, email address " [:code "aragorn@sonofarath.org"] " at argument path " [:code "[0 :email]"] " ought to appear at return path " [:code "[:email-addresses 0]"] ". That relationship specification looks like this."]

 [:pre [:code "{:path-argument [0 :email]\n :path-return [:phone-numbers 0]\n :relationship-fn =}"]]

 [:p "Now, we insert those two specifications into a vector and associate that vector into the organizing map."]

 [:pre [:code
"{:speculoos/argument-return-relationships [{:path-argument [2 :phone]
                                             :path-return [:phone-numbers 2]
                                             :relationship-fn =}]}
                                            {:path-argument [0 :email]
                                             :path-return [:email-addresses 0]
                                             :relationship-fn =}"]]

 [:p "All that remains is to consult " [:code "validate-fn-with"] " to see if the relationships are satisfied. First, we'll do " [:code "pull-n-put"] ", which should yield the intended results."]

 [:pre (print-form-then-eval "(validate-fn-with pull-n-put {:speculoos/argument-return-relationships [{:path-argument [2 :phone] :path-return [:phone-numbers 2] :relationship-fn =} {:path-argument [0 :email] :path-return [:email-addresses 0] :relationship-fn =}]} person-1 person-2 person-3)")]

 [:p "Yup. " [:code "pull-n-put"] "'s return value satisfied both equality relationships with the arguments we supplied, so " [:code "validate-fn-with"] " passed on that correct return value."]

 [:p "Now we'll validate " [:code "pull-n-whoops"] ", which does not produce correct results."]

 [:pre (print-form-then-eval "(validate-fn-with pull-n-whoops {:speculoos/argument-return-relationships [{:path-argument [2 :phone] :path-return [:phone-numbers 2] :relationship-fn =} {:path-argument [0 :email] :path-return [:email-addresses 0] :relationship-fn =}]} person-1 person-2 person-3)")]

 [:p [:code "validate-fn-with"] " tells us that " [:code "pull-n-whoops"] "'s output satisfies neither argument/return relationship. Where we expected phone number " [:code "555-FILK"] ", we see " [:code ":mango"] ", and where we expected email " [:code "aragorn@sonofarath.org"] ", we see phone number " [:code "867-5309"] "."]

 [:p "The idea to grasp from validating " [:code "pull-n-put"] " and " [:code "pull-n-whoops"] " is that even though the relationship function was a basic equality " [:code "="] ", the relationship validation is precise, flexible, and powerful because we used paths to focus on exactly the relationship we're interested in. On the other hand, whatever function we put at " [:code ":relationship-fn"] " is completely open-ended, and can be similarly sophisticated."]

 [:p "Before we finish this subsection, I'd like to demonstrate how to combine all five types of validation: argument scalars, argument collections, return scalars, return collections, and argument/return relationship. We'll rely on our old friend " [:code "broken-reverse"] ". Let's remember what " [:code "broken-reverse"] " actually does."]

 [:pre (print-form-then-eval "(broken-reverse [11 22 33 44 55])")]

 [:p "Instead of properly reversing the argument collection, it merely appends a spurious " [:code "9999"] "."]

 [:p "We'll pass a vector as the first and only argument. Within that vector, we pretend to not care about the first two elements, so we'll use " [:code "any?"] " predicates as placeholders.  We'll specify the third element of that vector to be a decimal with a " [:code "decimal?"] " scalar predicate. The entire "  [:em "argument sequence"] " is validated, so we must make sure the shape of the scalar specification mimics the shape of the data."]

 [:pre [:code ":speculoos/arg-scalar-spec [[any? any? decimal?]]"]]

 [:p "Just so we see an invalid result, we'll make the argument collection specification expect a list, even though we know we'll be passing a vector. And again, we must make the collection specification's shape mimic the data, so to mimic the argument sequence, it looks like this."]

 [:pre [:code ":speculoos/arg-collection-spec [[list?]]"]]

 [:p "We know that " [:code "broken-reverse"] " returns the input collection with " [:code "9999"] " conjoined. We'll write the return scalar specification to expect a string in the fourth slot, merely so that we'll see integer " [:code "44"] " fail to satisfy."]

 [:pre [:code ":speculoos/ret-scalar-spec [any? any? any? string?]"]]

 [:p "And since we're expecting " [:code "broken-reverse"] " to return a vector, we'll write the return collection specification to expect a set."]

 [:pre [:code ":speculoos/ret-collection-spec [set?]"]]

 [:p "Finally, we've previously demonstrated that " [:code "broken-reverse"] " fails to satisfy the " [:code "reversed?"] " argument/return relationship specification. We'll pass " [:code "reversed?"] " the first argument and the entire return."]

 [:pre [:code ":speculoos/argument-return-relationships [{:path-argument [0]\n                                           :path-return []\n                                           :relationship-fn reversed?}]"]]

 [:p#messy "We assemble all five of those specifications into an organizing map…"]

 [:pre (print-form-then-eval "(def organizing-map {:speculoos/arg-scalar-spec [[any? any? decimal?]]
                                                                 :speculoos/arg-collection-spec [[list?]]
                                                                 :speculoos/ret-scalar-spec [any? any? any? string?]
                                                                 :speculoos/ret-collection-spec [set?]
                                                                 :speculoos/argument-return-relationships [{:path-argument [0]
                                                                                                            :path-return []
                                                                                                            :relationship-fn reversed?}]})")]

 [:p "…and invoke " [:code "validate-fn-with"] "."]

 [:pre (print-form-then-eval "(validate-fn-with broken-reverse organizing-map [11 22 33 44 55])" 55 65)]

 [:p "We supplied five specifications, five datums failed to satisfy those specifications, and we receive five invalidation entries."

  [:ul
   [:li "Argument scalar " [:code "33"] " did not satisfy " [:code "decimal?"] "."]
   [:li "Argument collection " [:code "[11 22 33 44 55]"] " did not satisfy " [:code "list?"] "."]
   [:li "Return scalar " [:code "44"] " did not satisfy " [:code "string?"] "."]
   [:li "Return collection " [:code "[11 22 33 4  55 9999]"] " did not satisfy " [:code "set?"] "."]
   [:li "Argument " [:code "[11 22 33 44 55]"] " and return " [:code "[11 22 33 44 55 9999]"] " did not satisfy relationship " [:code "reversed?"] "."]]]


 [:h3#recognized-metadata-keys "Recognized metadata specification keys"]

 [:p "Speculoos consults the following defined group of keys in a specification map when it validates."]

 [:pre (print-form-then-eval "speculoos.function-specs/recognized-spec-keys")]


 [:h3#explicit "Function Metadata Specifications"]

 [:p "Speculoos offers three patterns of function validation."
  [:ol
   [:li [:code "validate-fn-with"] " performs " [:strong "explicit"] " validation with a specification supplied in a separate map. The function var is not altered."]
   [:li [:code "validate-fn"] " performs " [:strong "explicit"] " validation with specifications contained in the function's metadata."]
   [:li [:code "instrument"] " provides " [:strong "implicit"] " validation with specifications contained in the function's metadata."]]]

 [:p "Up until this point, we've been using the most explicit variant, " [:code "validate-fn-with"] " because its behavior is the most readily apparent. " [:code "validate-fn-with"]" is nice when we want to quickly validate a function " [:em "on-the-fly"] " without messing with the function's metadata. We merely supply the function's name, a map of specifications, and a sequence of arguments as if we were directly invoking the function."]

 [:p "Speculoos function specifications " [:a {:href "https://clojure.org/about/spec#_dont_further_add_tooverload_the_reified_namespaces_of_clojure"} "differ"] " from " [:code "spec.alpha"] " in that they are stored and retrieved directly from the function's metadata. Speculoos is an experimental library, and I wanted to test whether it was a good idea to store a function's specifications in its own metadata. I thought it would be nice if I could hand you one single thing and say "]

 [:blockquote [:p  [:em "Here's a Clojure function you can use. Its name suggests what it does, its docstring tells you how to use it, and human- and machine-readable specifications check the validity of the inputs, and tests that it's working properly. All in one neat, tidy "] "S-expression."]]

 [:p "To validate a function with metadata specifications, we use " [:code "validate-fn"] " (or as we'll " [:a {:href "#instrument"} "discuss later"] ", " [:code "instrument"] "). Speculoos offers a pair convenience functions to add and remove specifications from a function's metadata. To add, use " [:code "inject-specs!"] ". Let's inject a couple of function specifications to " [:code "sum-three"] " which we " [:a {:href "#fn-args"} "saw earlier"] "."]

 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn inject-specs! unject-specs!]])" 90 45)
  [:br]
  [:br]
  (print-form-then-eval "(inject-specs! sum-three {:speculoos/arg-scalar-spec [int? int? int?] :speculoos/ret-scalar-spec int?})")]

 [:p "We can observe that the specifications indeed live in the function's metadata with " [:code "clojure.core/meta"] ". There's a lot of metadata, so we'll use " [:code "select-keys"] " to extract only the key-values associated by " [:code "inject-specs!"] "."]

 [:pre (print-form-then-eval "(select-keys (meta #'sum-three) speculoos.function-specs/recognized-spec-keys)" 80 55)]

 [:p "We see that " [:code "inject-specs!"] " injected both an argument scalar specification and a return scalar specification."]

 [:p "If we later decided to undo that, " [:code "unject-specs!"] " removes all recognized Speculoos specification entries, regardless of how they got there (maybe some combination of " [:code "inject-specs!"] " and " [:code "with-meta"] "). For the upcoming demonstrations, though, we'll keep those specifications in " [:code "sum-three"] "'s metadata."]

 [:p "Now that " [:code "sum-three"] " holds the specifications in its metadata, we can try the second pattern of explicit validation pattern, using " [:code "validate-fn"] ". It's similar to " [:code "validate-fn-with"] ", except we don't have to supply the specification map; it's already waiting in the metadata. Invoked with valid arguments, " [:code "sum-three"] " returns a valid value."]

 [:pre (print-form-then-eval "(validate-fn sum-three 1 20 300)")]

 [:p "Invoking " [:code "sum-three"] " with an invalid floating-point number, Speculoos interrupts with a validation report."]

 [:pre (print-form-then-eval "(validate-fn sum-three 1 20 300.0)")]

 [:p "Scalar argument " [:code "300.0"] " failed to satisfy its paired scalar predicate " [:code "int?"] ". Also, scalar return " [:code "321.0"] " failed to satisfy its paired scalar predicate " [:code "int?"] "."]

 [:p "The metadata specifications are passive and have no effect during normal invocation."]

 [:pre (print-form-then-eval "(sum-three 1 20 300.0)")]

 [:p "Even though " [:code "sum-three"] " currently holds a pair of scalar specifications within its metadata, directly invoking " [:code "sum-three"] " does not initiate any validation."]

 [:p [:code "validate-fn"] " only interrupts when a predicate paired with a datum is not satisfied. If we remove all the specifications, there won't be any predicates. Let's remove " [:code "sum-three"] "'s metadata specifications with " [:code "unject-specs!"] "."]

 [:pre
  (print-form-then-eval "(unject-specs! sum-three)")
  [:br]
  [:br]
  [:br]
  [:code ";; all recognized keys are removed"]
  [:br]
  [:br]
  (print-form-then-eval "(select-keys (meta #'sum-three) speculoos.function-specs/recognized-spec-keys)" 80 55)]

 [:p "Now that " [:code "sum-three"] "'s metadata no longer contains specifications, " [:code "validate-fn"] " will not perform any validations."]

 [:pre (print-form-then-eval "(validate-fn sum-three 1 20 300.0)")]

 [:p "The return value " [:code "321.0"] " merely passes through because there were zero predicates."]

 [:p "We can try a more involved example. Let's inject that " [:a {:href "#messy"} "messy ball"] " of metadata specifications into " [:code "broken-reverse"] "."]

 [:pre (print-form-then-eval "(inject-specs! broken-reverse {:speculoos/arg-scalar-spec [[any? any? decimal?]], :speculoos/arg-collection-spec [[list?]], :speculoos/ret-scalar-spec [any? any? any? string?], :speculoos/ret-collection-spec [set?], :speculoos/argument-return-relationships [{:path-argument [0], :path-return [], :relationship-fn reversed?}]})")]

 [:p "Now we double-check the success of injecting the metadata."]

 [:pre (print-form-then-eval "(select-keys (meta #'broken-reverse) speculoos.function-specs/recognized-spec-keys)" 80 75)]

 [:p "We confirm that all five function specifications in " [:code "broken-reverse"] "'s metadata. " [:code "validate-fn"] " can now find those specifications."]

 [:p "Finally, we validate " [:code "broken-reverse"] "."]

 [:pre (print-form-then-eval "(validate-fn broken-reverse [11 22 33 44 55])")]

 [:p "Notice, this is the exact same validation " [:a {:href "#messy"} "as before"] ", but because all the messy specifications were already tucked away in the metadata, the validation invocation was a much cleaner one-liner, " [:br] [:code "(validate-fn broken-reverse [11 22 33 44 55])"] "."]

 [:p "Again, metadata specification have no effect when the function is directly invoked."]

 [:pre (print-form-then-eval "(broken-reverse [11 22 33 44 55])")]

 [:p "We never " [:em "unjected"] " the specifications from " [:code "broken-reverse"] "'s metadata, but they have absolutely no influence outside of Speculoos' function validation."]


 [:h3#instrument "Instrumenting Functions"]

 [:p [:strong "Beware: "] [:code "instrument"] "-style function validation is very much a work in progress. The current implementation is sensitive to invocation order and can choke on multiple calls."]

 [:p "Until this point in our discussion, Speculoos has only performed function validation when we explicitly called either " [:code "validate-fn-with"] " or " [:code " validate-fn"] ". With those two utilities, the specifications in the metadata are passive and produce no effect, even when invoking with arguments that would otherwise fail to satisfy the specification's predicates."]

 [:p "Speculoos' third pattern of function validation " [:em "instruments"] " the function using the metadata specifications. Every direct invocation of the function itself automatically validates arguments and returns using any specification in the metadata. Let's explore function instrumentation using " [:code "sum-three"] " " [:a {:href "#fn-args"} "from earlier"] ". " [:code "instrument"] " will only validate with metadata specifications. First, we need to inject our specifications."]

 [:pre (print-form-then-eval "(inject-specs! sum-three {:speculoos/arg-scalar-spec [int? int? int?] :speculoos/ret-scalar-spec int?})")]

 [:p [:code "sum-three"] " now holds two scalar specifications within its metadata, but those specifications are merely sitting there, completely passive."]

 [:pre
  [:code ";; valid args and return value"]
  [:br]
  [:br]
  (print-form-then-eval "(sum-three 1 20 300)")
  [:br]
  [:br]
  [:br]
  [:code ";; invalid arg 300.0 and invalid return value 321.0"]
  [:br]
  [:br]
  (print-form-then-eval "(sum-three 1 20 300.0)")]

 [:p "That second invocation above supplied an invalid argument and produced an invalid return value, according to the metadata specifications. But we didn't explicitly validate with " [:code "validate-fn"] ", and " [:code "sum-three"] " is not yet instrumented, so " [:code "sum-three"] " returns the computed value " [:code "321.0"] " without interruption."]

 [:p "Let's instrument " [:code "sum-three"] " and see what happens."]

 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [instrument unstrument]])")
  [:br]
  [:br]
  [:code "(instrument sum-three)"]]

 [:p "Not much. We've only added the specifications to the metadata and instrumented " [:code "sum-three"] ". An instrumented function is only validated when it is invoked.
"]

 [:pre [:code "(sum-three 1 20 300) ;; => 321"]]

 [:p "We just invoked " [:code "sum-three"] ", but all three integer arguments and the bare scalar return value satisfied all their predicates, so " [:code "321"] " passes through. Let's invoke with two integer arguments and one non-integer argument."]

 [:pre
  [:code ";; arg 300.0 does not satisfy its paired predicate in the argument scalar specification,"]
  [:br]
  [:code ";; but `sum-three` is capable of computing a return with those given inputs"]
  [:br]
  [:br]
  [:code "(sum-three 1 20 300.0) ;; => 321.0"]]

 [:p "That's interesting. In contrast to " [:code "validate-fn-with"] " and " [:code "validate-fn"] ", an instrumented function is not interrupted with an invalidation report when predicates are not satisfied. The invalidation report is instead written to " [:code "*out*"] "."]

 [:pre
  [:code ";; validation report is written to *out*"]
  [:br]
  [:br]
  [:code "(with-out-str (sum-three 1 20 300.0))"]
  [:br]
  [:code ";; => ({:path [2], :datum 300.0, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}\n"]
  [:code ";;     {:path nil, :datum 321.0, :predicate int?, :valid? false, :fn-spec-type :speculoos/return})"]]

 [:p "Speculoos will implicitly validate any instrumented function with any permutation of " [:a {:href "#recognized-metadata-keys"} "specifications within its metadata"] "."]

 [:p "When we want to revert " [:code "sum-three"] " back to normal, we " [:code "unstrument"] " it."]

 [:pre [:code "(unstrument sum-three)"]]

 [:p "Now that it's no longer instrumented, " [:code "sum-three"] " will yield values as normal, even if the arguments and return value do not satisfy the metadata specifications."]

 [:pre
  [:code ";; valid arguments and return value"]
  [:br]
  [:code "(sum-three 1 20 300) ;; => 321"]
  [:br]
  [:br]
  [:code ";; one invalid argument, invalid return value"]
  [:br]
  [:code "(sum-three 1 20 300.0) ;; => 321.0"]
  [:br]
  [:br]
  [:code ";; nothing written to *out*"]
  [:br]
  [:code "(with-out-str (sum-three 1 20 300.0)) ;; => \"\""]]

 (comment
   (ns-unmap *ns* 'sum-three)
   (defn sum-three [x y z] (+ x y z))
   (inject-specs! sum-three {:speculoos/arg-scalar-spec [int? int? int?] :speculoos/ret-scalar-spec int?})
   (meta #'sum-three)
   (intrument sum-three)
   (sum-three 1 20 300)
   (sum-three 1 20 300.0)

   ;; valiation report is written to *out*
   (with-out-str (sum-three 1 20 300.0)) ;;
   "({:path [2], :datum 300.0, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}\n"
   " {:path nil, :datum 321.0, :predicate int?, :valid? false, :fn-spec-type :speculoos/return})"

   (unstrument sum-three)
   (sum-three 1 20 300)
   (sum-three 1 20 300.0)
   )


 [:h3#hof "Validating Higher-Order Functions"]

 [:p "Speculoos has a story about validating higher-order functions, too. It uses very similar patterns to first-order function validation: Put some specifications in the function's metadata with the " [:a {:href "#recognized-metadata-keys"} "proper, qualified keys"] ", then invoke the function with some sample arguments, then Speculoos will validate the results."]

 [:p "The classic " [:span.small-caps "hof"] " is something like " [:code "(defn adder [x] #(+ x %))"] ". To make things a tad more interesting, we'll add a little flourish."]

 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")
  [:br]
  [:br]
  (print-form-then-eval "(defn addder [x] (fn [y] (fn [z] (+ x (+ y z)))))")
  [:br]
  [:br]
  (print-form-then-eval "(((addder 7) 80) 900)")]

 [:p [:code "addder"] " returns a function upon each of its first two invocations, and only on its third invocation does " [:code "addder"] " return a scalar. Specifying and validating a function object does not convey much meaning: it would merely satisfy " [:code "fn?"] " which isn't very interesting. So to validate a " [:span.small-caps "hof"] ", Speculoos requires it to be invoked until it produces a value. So we'll supply the validator with a series of argument sequences that, when fed in order to the " [:span.small-caps "hof"] ", will produce a result. For the example above, it will look like " [:code "[7] [80] [900]"]  "."]

 [:p "The last task we must do is create the specification. " [:span.small-caps "hof"] " specifications live in the function's metadata at key " [:code ":speculoos/hof-specs"] ", which is a series of nested specification maps, one nesting for each returned function. For this example, we might create this " [:span.small-caps "hof"] " specification."]

 [:pre (print-form-then-eval "(def addder-spec {:speculoos/arg-scalar-spec [string?] :speculoos/hof-specs {:speculoos/arg-scalar-spec [boolean?] :speculoos/hof-specs {:speculoos/arg-scalar-spec [char?] :speculoos/ret-scalar-spec keyword?}}})" 100 90)]

 [:p "Once again, for illustration purposes, we've crafted a specification composed of predicates that we know will invalidate, but will permit the function stack to evaluate to completion. (Otherwise, validation halts on exceptions.)"]

 [:p [:span.small-caps "hof"] " validation requires that the function's metadata hold the specifications. So we inject them."]

 [:pre (print-form-then-eval "(inject-specs! addder addder-spec)")]

 [:p "And finally, we execute the validation with " [:code "validate-higher-order-fn"]]

 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")
  [:br]
  [:br]
  (print-form-then-eval "(validate-higher-order-fn addder [7] [80] [900])")]

 [:p "Let's step through the validation results. Speculoos validates " [:code "7"] " against scalar predicate " [:code "string?"] " and then invokes "[:code "addder"] " with argument " [:code "7"] ". It then validates " [:code "80"] " against scalar predicate " [:code "boolean?"] " and then invokes the returned function with argument " [:code "80"] ". It then validates " [:code "900"] " against scalar predicate " [:code "char?"] " and invokes the previously returned function with argument " [:code "900"] ". Finally, Speculoos validates the ultimate return value " [:code "987"] " against scalar predicate " [:code "keyword?"] ". If all the predicates were satisfied, " [:code "validate-higher-order-fn"] " would yield the return value of the function call. In this case, all three arguments and the return value are invalid, and Speculoos yields a validation report."]
  ]
