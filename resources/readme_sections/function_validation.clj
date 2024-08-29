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

 [:p "The argument list is a " [:em "sequence"] " of values, in this example, a sequential thing of three integers. We can imagine a scalar specification for just such as sequence."]

 [:pre [:code "[int? int? int?]"]]

 [:p "When using " [:code "validate-fn-with"] ", we supply the function name, a map containing zero or more specifications, and some trailing " [:code "&-args"] " as if they had been supplied directly to the function. Speculoos can validate five aspects of a function using up to five specifications, each specification associated in that map to a particular key. We'll cover each of those five aspects in turn. To start, we want to specify the " [:em "argument scalars"] "."]

 [:p "Instead of individually passing each of those five specifications to " [:code "validate-fn-with"] " and putting " [:code "nil"] " placeholders where don't wish to supply a specification, we organize the specifications. To do so, we associate the arguments' scalar specification to the qualified key " [:code ":speculoos/arg-scalar-spec"] "."]
 
 [:pre [:code "{:speculoos/arg-scalar-spec [int? int? int?]}"]]

 [:p "Then, we validate the arguments to " [:code "sum-three"] " like this."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?]} 1 20 300)" 75 45)]

 [:p "The arguments conformed to the scalar specification, so " [:code "validate-fn-with"] " returns the value produced by " [:code "sum-three"] ". Let's intentionally invoke " [:code "sum-three"] " with one invalid argument by swapping integer " [:code "1"] " with a floating-point " [:code "1.0"] "."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?]} 1.0 20 300)" 75 45)]

 [:p "Hey, that kinda looks familiar. It looks a lot like something " [:code "validate-scalars"] " would emit if we filtered to keep only the invalids. We see that " [:code "1.0"] " at path " [:code "[0]"] " failed to satisfy its " [:code "int?"] " scalar predicate. We can also see that the function specification type is " [:code ":speculoos/argument"] ". Since Speculoos can validate scalars and collections of both arguments and collections, that key-val is a little signpost to help us pinpoint exactly what and where. Let's invoke " [:code "sum-three"] " with a second invalid argument, a ratio " [:code "22/7"] " instead of integer " [:code "300"] "."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?]} 1.0 20 22/7)" 75 45)]

 [:p "In addition to the invalid " [:code "1.0"] " at path " [:code "[0]"] ", we see that " [:code "22/7"] " at path " [:code "[2]"] " also fails to satisfy its " [:code "int?"] " scalar predicate. The scalar predicate's path in the scalar specification is the same as the path of the " [:code "22/7"] " in the " [:code "[1.0 20 22/7]"] " sequence of arguments. Roughly, " [:code "validate-fn-with"] " is doing something like this…"]

 [:pre (print-form-then-eval "(speculoos.core/only-invalid (validate-scalars [1.0 20 22/7] [int? int? int?]))" 45 45)]

 [:p "…validating scalars with " [:code "validate-scalars"] " and keeping only the invalids."]

 [:p "Okay, we see that term " [:em "scalar"] " buzzing around, so there must be something else about validating collections. Yup. We can also validate collection properties of the argument sequence. Let's specify that the argument sequence must contain three elements with a custom collection predicate."]

 [:pre (print-form-then-eval "(defn count-3? [v] (= 3 (count v)))")]

 [:p "Let's simulate the collection validation first. Remember, collection predicates are applied to their parent containers, so " [:code "count-3?"] " must appear within a collection so that it'll be paired with the data's containing collection."]

 [:pre (print-form-then-eval "(validate-collections [1 20 30] [count-3?])" 40 85)]

 [:p "That result fits with " [:a {:href "#validating-collections"} "our discussion"] " about validating collections."]

 [:p "Next, we'll associate that collection specification into our function specification map at " [:code ":speculoos/arg-collection-spec"] " and invoke " [:code "validate-fn-with"] " with three valid arguments."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-collection-spec [count-3?]} 1 20 300)" 75 85)]

 [:p "The argument sequence satisfies our collection specification, so " [:code "sum-three"] " returns the expected value. Now let's repeat, but with an additional argument that causes the argument list to violate its collection predicate."]
 
 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-collection-spec [count-3?]} 1 20 300 4000)" 75 85)]

 [:p "This latest argument list " [:code "[1 20 300 4000]"] " failed to satisfy our " [:code "count-3?"] " collection predicate, so " [:code "validate-fn-with"] " emitted a validation report."]

 [:p "Note: Don't specify and validate the " [:em "type"] " of the argument container, i.e., " [:code "vector?"] ". That's an implementation detail and not guaranteed."]

 [:p "Let's get fancy and combine an argument scalar specification and an argument collection specification. Outside of the context of checking a function, that " [:a {:href "#valid-thorough"} "combo validation"] " would look like this."]

 [:pre (print-form-then-eval "(speculoos.core/only-invalid (validate [1.0 20 22/7 4000] [int? int? int?] [count-3?]))" 45 55)]

 [:p  "Let's remember: scalars and collections are " [:em "always"] " validated separately. " [:code "validate"] " is merely a convenience function that does both a scalar validation, then a collection validation, in discrete processes, with a single function invocation. Each of the first three scalars that paired with a scalar predicate were validated as scalars. The first and third scalars failed to satisfy their respective predicates. The fourth argument, " [:code "4000"] ", was not paired with a scalar predicate and was therefore ignored. Then, the argument sequence as a whole was validated against the collection predicate " [:code "count-3?"] "."]

 [:p [:code "validate-fn-with"] " performs substantially that combo validation. We'll associate the " [:strong "arg"] "ument " [:strong "scalar"] " " [:strong "spec"] "ification with " [:code ":speculoos/arg-scalar-spec"] " and the " [:strong "arg"] "ument " [:strong "collection"] " " [:strong "spec"] "fication with " [:code ":speculoos/arg-collection-spec"] " and pass the invalid argument sequence."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/arg-scalar-spec [int? int? int?] :speculoos/arg-collection-spec [count-3?]} 1.0 20 22/7 4000)" 95 75)]

 [:p "Just as in the " [:code "validate"] " simulation, we see three items fail to satisfy their predicates. Scalars " [:code "1.0"] " and " [:code "22/7"] " are not integers, and the argument sequence as a whole, " [:code "[1.0 20 22/7 4000]"] ", does not contain exactly three elements as its collection predicate requires."]
 
 
 [:h3#fn-ret "2. Validating Function Return Values"]

 [:p "Speculoos can also validate values returned by a function. Reusing our " [:code "sum-three"] " function, and going back to valid inputs, we can associate a " [:strong "ret"] "urn " [:strong "scalar"] " " [:strong "spec"] "ification into " [:code "validate-fn-with"] "'s specification map to key " [:code ":speculoos/ret-scalar-spec"] ". Let's stipulate that the function returns an integer. Here's how we pass that specification to " [:code "validate-fn-with"] "."]

 [:pre [:coce "{:speculoos/ret-scalar-spec int?}"]]

 [:p  "And now, the function validation."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/ret-scalar-spec int?} 1 20 300)" 55 55)]

 [:p "The return value " [:code "321"] " satisfies " [:code "int?"] ", so " [:code "validate-fn-with"] " returns the computed sum."]

 [:p "What happens when the return value is invalid? Instead of messing up " [:code "sum-three"] "'s defninition, we'll merely alter the scalar predicate. Instead of an integer, we'll stipulate that " [:code "sum-three"] " returns a string with scalar predicate " [:code "string?"] "."]

 [:pre (print-form-then-eval "(validate-fn-with sum-three {:speculoos/ret-scalar-spec string?} 1 20 300)" 55 55)]

 [:p "Very nice. " [:code "sum-three"] " computed, quite correctly, the sum of the three arguments. But we gave it a bogus return scalar specification that claimed it ought to be a string, which integer " [:code "321"] " fails to satify."]

 [:p "Did you happen to notice the " [:code "path"] "? We haven't yet encountered a case where a path is " [:code "nil"] ". In this situation, the function returns a 'bare' scalar, not contained in a collection. Speculoos can validate a bare scalar when that bare scalar is a function's return value."]

 [:p "Let's see how to validate a function when the return value is a collection of scalars. We'll write a new function that returns four scalars: the three arguments and their sum."]

 [:pre
  (print-form-then-eval "(defn enhanced-sum-three [x y z] [x y z (+ x y z)])" 25 25)
  [:br]
  [:br]
  (print-form-then-eval "(enhanced-sum-three 1 20 300)")]

 [:p "Our enhanced function now returns a vector of four elements. Let's remind ourselves how we'd manually validate that return value. If we decide we want " [:code "enhanced-sum-three"] " to return four integers, the scalar specification would look like this."]

 [:pre [:code "[int? int? int? int?]"]]

 [:p "And the manual validation would look like this."]
 
 [:pre (print-form-then-eval "(validate-scalars [1 20 300 321] [int? int? int? int?])" 45 45)]

 [:p "Four paired scalars and scalar predicates yield four validaton results. Let's see what happens when we validate the function return scalars."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-scalar-spec [int? int? int? int?]} 1 20 300)")]

 [:p "Since we fed " [:code "validate-fn-with"] " a specification that happens to agree with those arguments, " [:code "enhanced-sum-three"] " returns its computed value, " [:code "[1 20 300 321]"] "."]

 [:p "Let's stir things up. We'll change the return scalar specification to something we know will fail: The first scalar a character, the final scalar a boolean."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-scalar-spec [char? int? int? boolean?]} 1 20 300)")]

 [:p [:code "enhanced-sum-three"] "'s function body remained the same, and we fed it the same integers as before, but we fiddled with the return scalar specification so that we got two invalid scalars. " [:code "1"] " at path " [:code "[0]"] " does not satisfy its wonky scalar predicate " [:code "char?"] " at the same path. And " [:code "321"] " at path " [:code "[3]"] " does not satisfy fraudulent scalar predicate " [:code "boolean?"] " that shares its path."]

 [:p "Let's set aside validating scalars for a moment and validate a facet of " [:code "enhanced-sum-three"] "'s return collection. First, we'll do a manual demonstration with " [:code "validate-collections"]". Let's remember: collection predicates apply to their immediate parent container. We wrote " [:code "enhanced-sum-three"] " to return a vector, but to make the validation produce something interesting to look at, we'll pretend we're expecting a list."]

 [:pre (print-form-then-eval "(validate-collections [1 20 300 321] [list?])" 40 45)]

 [:p "That collection validation aligns with our understanding. " [:code "[1 20 300 321]"] " is not a list. The " [:code "list?"] " collection predicate at path " [:code "[0]"] " in the specification was paired with the thing found at path " [:code "(drop-last [0])"] " in the data, which in this example is the root collection. We designed " [:code "enhanced-sum-three"] " to yield a vector."]

 [:p "Let's toss that collection specification at " [:code "validate-with-fn"] " and have it apply to " [:code "enhanced-sum-three"] "'s return value, which won't satisfy. We pass the " [:strong "ret"] "urn " [:strong "collection spec"] "ification by associating it to the key " [:code ":speculoos/ret-collection-spec"] "."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-collection-spec [list?]} 1 20 300)")]

 [:p "Similarly to the manual collection validation we previously performed with " [:code "validate-collections"] ", we see that " [:code "enhanced-sum-three"] "'s return vector " [:code "[1 20 300 321]"] " fails to satisfy its " [:code "list?"] " collection predicate."]

 [:p "A scalar validation followed by an independent collection validation allows us to check every possible aspect that we could want. Now we that we've seen how to individually validate " [:code "enhance-sum-three"] "'s return scalars and return collections, we know how to do both with one invocation."]

 [:p "Remember Mantra #1: Validate scalars separately from validating collections. Speculoos will only ever do one or the other, but " [:code "validate"] " is a convenience function that performs a scalar validation immediately followed by a collection validation. We'll re-use the scalar specification and collection specification from the previous examples."]

 [:pre (print-form-then-eval "(speculoos.core/only-invalid (validate [1 20 300 321] [char? int? int? boolean?] [list?]))" 45 45)]

 [:p [:code "only-invalid"] " discards the validation where the predicates are satisfied, leaving only the invalids. Two scalars failed to satisfy their scalar predicates. Integer " [:code "1"] " at path " [:code "[0]"] " in the data fails to satisfy scalar predicate " [:code "char?"] " at path " [:code "[0]"] " in the scalar specification. Integer " [:code "321"] " fails to satisfy scalar predicate " [:code "boolean?"] " at path " [:code "[3]"] " in the scalar specification. Finally, our root vector " [:code "[1 20 300 321]"] " located at path " [:code "[]"] " fails to satisfy the collection predicate " [:code "list?"] " at path " [:code "[0]"] "."]

 [:p "Now that we've seen the combo validation done manually, let's validate " [:code "enhanced-sum-three"] "'s return in the same way. Here's where we see why to organize the specifications in a container instead of passing them as individual arguments: it keeps our invocation neater."]

 [:pre (print-form-then-eval "(validate-fn-with enhanced-sum-three {:speculoos/ret-scalar-spec [char? int? int? boolean?] :speculoos/ret-collection-spec [list?]} 1 20 300)")]

 [:p [:code "valiate-fn-with"] "'s validation is substantially the same as the one " [:code "validate"] " produced in the previous example, except, now, the data comes from invoking " [:code "enhanced-sum-three"] ". Two scalar invalids and one collectoin invalid. Integer " [:code "1"] " fails to satisfy scalar predicate " [:code "char?"] ", integer " [:code "321"] " fails to satisfy scalar predicate " [:code "boolean?"] ", and the entire return vector " [:code "[1 20 300 321]"] " fails to satisfy collection predicate " [:code "list?"] "."]

 [:p "Okay. I think we're ready to put together all four different function validations we've so far seen. We've seen…"]

 [:ul
  [:li "a function argument scalar validation,"]
  [:li "a function argument collection validation,"]
  [:li "a function return scalar validation, and"]
  [:li "a function return collection validation."]]

 [:p "And we've seen how to combine both function argument validations, and how to combine both function return validations. Now we'll combine all four validations into one " [:code "validate-fn-with"] " invocation."]

 [:p "Let's review our ingredients. Here's our " [:code "enhanced-sum-three"] " function."]

 [:pre (print-form-then-eval "(defn enhanced-sum-three [x y z] [x y z (+ x y z)])" 25 25)]

 [:p [:code "enhanced-sum-three"] " accepts three number arguments and returns a vector of those three numbers with their sum appended to the end of the vector. Technically, Clojure would accept any numberic thingy for " [:code "x"] ", " [:code "y"] ", and " [:code "z"] ", but for illustration purposes, we'll make our scalar predicates something non-numeric so we can see something interesting in the validation reports."]

 [:p "With that in mind, we pretend that we want to validate the function's argument sequence as a string, followed by an integer, followed by a symbol. The function scalar specification will be…"]

 [:pre [:code "[string? int? symbol?]"]]

 [:p "To allow " [:code "enhanced-sum-three"] " to calculate a result, we'll supply three numeric values, two of which will not satisfy that argument scalar specification. So that it produces something interesting, we'll make our function argument collection specification also complain."]

 [:pre
  [:code (print-form-then-eval "(defn length-2? [v] (= 2 (count v)))")]
  [:br]
  [:br]
  [:code ";; collection predicates apply to the path of the parent container"]
  [:br]
  [:code "[length-2?]"]]

 [:p "We know for sure that the argument sequence will contain three values, so that particular argument collection predicate will produce something interesting."]

 [:p "Jumping to " [:code "enhanced-sum-three"] "'s output side, we expect a vector of four numbers. Again, we'll craft our function return scalar specification to contain two predicates that we know won't be satisfied because those scalar predicates are looking for something non-numeric."]

 [:pre [:code "[char? int? int? boolean?]"]]

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

 [:p " If we want another level of thoroughness checking the correctness, we can specify and validate the relationships between the functions arguments and return values. Perhaps you'd like to be able to express "  [:em "The return value is a collection, with all the same elements as the input sequence."] " Or " [:em "The return value is a concatenation of the even indexed elements of the input sequence."] " Speculoos' term for this action is " [:em "validating function argument and return value relationship"] "."]

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
  (print-form-then-eval "(defn broken-reverse [v] (conj v 42))")
  [:br]
  [:br]
  (print-form-then-eval "(broken-reverse [11 22 33 44 55])" 45 35)]

 [:p "Pitiful." [:code "broken-reverse"] " fulfilled none of the three relationships. The return collection is not the same length, contains additonal elements, and is not reversed. Let's codify that pitifulness."]

 [:p "First, we'll write three " [:a {:href "#relationships"} "relationship functions"] ". Relationship funcstions are a lot like predicate. They return a truthy or falsey value, but instead consume two things instead of one. The function's argument sequence is passed as the first thing and the function's return value is passed as the second thing."]

 [:pre
  (print-form-then-eval "(defn same-length? [v1 v2] (= (count v1) (count v2)))")
  [:br]
  [:br]
  (print-form-then-eval "(same-length? [11 22 33 44 55] [11 22 33 44 55])" 45 80)
  [:br]
  [:br]
  (print-form-then-eval "(same-length? [11 22] [11 22 33 44 55])" 35 80)
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(defn same-elements? [v1 v2] (= (sort v1) (sort v2)))")
  [:br]
  [:br]
  (print-form-then-eval "(same-elements? [11 22 33 44 55] [55 44 33 22 11])" 45 80)
  [:br]
  [:br]
  (print-form-then-eval "(same-elements? [11 22 33 44 55] [55 44 33 22 9999])" 45 80)
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(defn reversed? [v1 v2] (= v1 (reverse v2)))")
  [:br]
  [:br]
  (print-form-then-eval "(reversed? [11 22 33 44 55] [55 44 33 22 11])" 40 80)
  [:br]
  [:br]
  (print-form-then-eval "(reversed? [11 22 33 44 55] [11 22 33 44 55])" 40 80)]

 [:p [:code "same-length?"] ", " [:code "same-element?"] ", " [:code "reversed?"] " all consume two sequential things and test a relationship between the two. If their relationship is satisfied, they signal " [:code "true"] ", if not, then they signal " [:code "false"] ". They are all three gonna have something unkind to say about " [:code "broken-reverse"] "."]

 [:p "In our example, checking " [:code "broken-reverse"] "'s argument/return relationship will be fairly straightforward: There's a single argument collection of elements, and a single return collection of elements. But we might someday want to check a more sophisticated relationship that needs to extract some slice of the argument or return value. Therefore, we must declare a path to the slice we want to check. Of the return value, we'd like to check the root collection, so that path is merely " [:code "[]"] "."]
 
 [:p "To extract argument, there's one tricky detail we must accommodate. The vector we're going to pass as an argument to " [:code "broken-reverse"] " is itself contained in the argument sequence. Take a look."]

 [:pre
  (print-form-then-eval "(defn arg-passthrough [& args] args)")
  [:br]
  [:br]
  (print-form-then-eval "(arg-passthrough [11 22 33 44 55])")]

 [:p "To extract the first argument passed to " [:code "broken-reverse"] ", the path needs to be " [:code "[0]"] "."]

 [:pre (print-form-then-eval "(nth (arg-passthrough [11 22 33 44 55]) 0)" 45 45)]

 [:p "Now that we know how to extract the interesting pieces, we load each into a map that looks like this."]

 [:pre [:code "{:path-argument [0]\n :path-return []\n :relationship-fn same-length?}"]]
 
 [:p "We've written three argument/function relationships to test " [:code "broken-reverse"] ", so we'll need to somehow feed them to " [:code "validate-fn-with"] ". We do that by associating them into the organizing map with keyword " [:code ":speculoos/argument-return-relationships"] ". Notice the plural " [:em "s"] ". Since there may be more than one relationship, we collect them into a vector. For the moment, let's just insert the " [:code "same-length?"] " relationship."]

 [:pre [:code
"{:speculoos/argument-return-relationships [{:path-argument [0]
                                            :path-return []
                                            :relationship-fn same-length?}]}"]]

 [:p "We're ready to validate."]

 [:pre (print-form-then-eval "(validate-fn-with broken-reverse {:speculoos/argument-return-relationships [{:path-argument [0] :path-return [] :relationship-fn same-length?}]} [11 22 33 44 55])" 45 70)]

 [:p "We supplied " [:code "broken-reverse"] " with a five-element vector, and it returned a six-element vector, failing to satisfy the specified " [:code "same-length?"] "relationship. We wrote two other relationship functions, but we did not send them to " [:code "validate-fn-with"] ", so it checked only what we explicitly supplied. Remember Mantra #3: Un-paired predicates (or, relationships in this instance) are ignored."]

 [:p "Let's check all three relationships now."]

 [:pre (print-form-then-eval "(validate-fn-with broken-reverse {:speculoos/argument-return-relationships [{:path-argument [0] :path-return [] :relationship-fn same-length?} {:path-argument [0] :path-return [] :relationship-fn same-elements?} {:path-argument [0] :path-return [] :relationship-fn reversed?}]} [11 22 33 44 55])" 45 70)]

 
 

 [:h3#recognized-metadata-keys "Recognized metadata specification keys"]

 [:p "Speculoos consults the following defined group of keys in a specification map when it validates."]
 
 [:pre (print-form-then-eval "speculoos.function-specs/recognized-spec-keys")]



 
 [:h3#explicit "Function Metadata Specifications"]


 [:p "Speculoos function specifications " [:a {:href "https://clojure.org/about/spec#_dont_further_add_tooverload_the_reified_namespaces_of_clojure"} "differ"] " from " [:code "spec.alpha"] " in that they are stored and retrieved directly from the function's metadata. Speculoos is an experiment, but I thought it would be nice if I could hand you one single thing and say "]
 
 [:blockquote [:p  [:em "Here's a Clojure function you can use. Its name suggests what it does, its docstring that tells you how to use it, and human- and machine-readable specifications check the validity of the inputs, and tests that it's working properly. All in one neat, tidy "] "S-expression."]]

 [:p "Speculoos offers three patterns of function validation."
  [:ol
   [:li [:code "validate-fn-with"] " performs explicit validation with a specification supplied in a separate map. The function var is not altered." ]
   [:li [:code "validate-fn"] " performs explicit validation with specifications contained in the function's metadata." ]
   [:li [:code "instrument/unstrument"] " provide implicit validation with specifications contained in the function's metadata." ]]]

 [:p "Up until this point, we've been using the most explicit variant, " [:code "validate-fn-with"] " because its behavior is the most readily apparent."]
 [:p "The first pattern is nice because you can quickly validate a function " [:em "on-the-fly"] " without messing with the function's metadata. Merely supply the function's name, a map of specifications (we'll discuss this soon), and a sequence of args as if you were directly invoking the function."]

 [:p "Speculoos offers a pair convenience functions to add and remove specifications from a function's metadata. To add, use " [:code "inject-specs!"] "."]
  
 [:pre
  (defn add-ten [x] (+ 10 x))
  (print-form-then-eval "(require '[speculoos.function-specs :refer [inject-specs! unject-specs! validate-fn]])")
  [:br]
  [:br]
  #_(print-form-then-eval "(inject-specs! add-ten {:speculoos/arg-scalar-spec [int?] :speculoos/ret-scalar-spec int?})")]
  
 [:p "We can observe that the specifications indeed live in the function's metadata. If we later decided to undo that, " [:code "unject-specs!"] " removes all recognized Speculoos specification entries, regardless of how they got there. For the upcoming demonstrations, though, we'll keep those specifications in " [:code "add-ten"] "'s metadata."]


 #_[:pre (print-form-then-eval "(select-keys (meta #'add-ten) speculoos.function-specs/recognized-spec-keys)")]
  
 [:p "Now that " [:code "add-ten"] " holds the specifications in its metadata, we can try the second pattern of explicit validation pattern. It's similar, except we don't have to supply the specification map; it's already waiting in the metadata. Invoked with a valid argument, " [:code "add-ten"] " returns a valid value."]
  
 [:pre (print-form-then-eval "(validate-fn add-ten 15)")]
  
 [:p "Invoking " [:code "add-ten"] " with an invalid float, Speculoos interrupts with a validation report."]
  
 #_[:pre (print-form-then-eval "(validate-fn add-ten 1.23)")]

 (defn silly [s] (identity s))
 #_[:pre
      [:code ";; a collection specification that requires the arg sequence to contain three items"]
      [:br]
      (print-form-then-eval "(defn length-3? [v] (= 3 (count v)))")
      [:br]
      [:br]
      (print-form-then-eval "(defn silly \"A contrived demo.\" [x s r] (vector (/ r 2) (inc x) (apply str (reverse (.toString s)))))")]
  
 [:p "Next, we inject our specifications into the function's metadata: a scalar specification and collection specification, each, for both the argument sequence and the return sequence. A grand total of four specifications."]
  
 #_[:pre (print-form-then-eval "(inject-specs! silly {:speculoos/arg-scalar-spec [int? string? ratio?] :speculoos/arg-collection-spec [length-3?] :speculoos/ret-scalar-spec [ratio? int? string?] :speculoos/ret-collection-spec [length-3?]})")]
  
 [:p "Valid inputs…"]
  
 #_[:pre (print-form-then-eval "(validate-fn silly 42 \"abc\" 1/3)")]
  
 [:p "…produce a valid return vector of three elements. The function halves the ratio, increments the integer, and reverses the string. But invalid arguments…"]
  
 #_[:pre (print-form-then-eval "(validate-fn silly 42 \\a 9)")]
  
 [:p "…yields an invalidation report: the character " [:code "\\a"] " does not satisfy its " [:code "string?"] " scalar predicate, and the integer " [:code "9"] " does not satisfy its " [:code "ratio?"] " scalar predicate."]


 [:p "Until this point in our discussion, Speculoos has only performed function validation when we explicitly call either " [:code "validate-fn-with"] " or " [:code " validate-fn"] ". The specifications in the metadata are passive and produce no effect, even with arguments that would otherwise fail to satisfy the specification's predicates."]
  
 [:pre
  (print-form-then-eval "(add-ten 15)")
  [:br]
  (print-form-then-eval "(add-ten 1.23)")]
  



  
 [:h3 "Instrumenting Functions"]
  
 [:p "Speculoos' third pattern of function validation " [:em "instruments"] " the function using the metadata specifications. (Beware: "
  [:code "instrument"]
  "-style function validation is very much a work in progress. The implementation is sensitive to invocation order and can choke on multiple calls. The var mutation is not robust.) Every invocation of the function itself automatically validates any specified arguments and return values."]

 [:pre (print-form-then-eval "(require '[speculoos.function-specs :refer [instrument unstrument]])")]
  
 #_ [:div.no-display (instrument silly)]
 #_[:pre
      [:code "(instrument silly)"]
      [:br]
      [:br]
      [:code ";; valid invocation, function returns as normal"]
      [:br]
      (print-form-then-eval "(silly 42 \"abc\" 1/3)")
      [:br]
      [:br]
      [:code ";; args do not satisfy their predicates, but function is capable of return with those given inputs"]
      [:br]
      (print-form-then-eval "(silly 42 \\a 9)")
      [:br]
      [:br]
      [:code ";; validation report is printed to *out*"]
      [:br]
      [:code "(with-out-str (silly 42 \\a 9))"
       [:br]
       *eval-separator*
       "({:path [1], :datum a, :predicate #function[clojure.core/string?--5475], :valid? false}\n"
       *eval-separator*
       " {:path [2], :datum 9, :predicate #function[clojure.core/ratio?], :valid? false})"]]
  
 [:p "The function returns if it doesn't throw an exception. Any non-satisfied predicates are reported to " [:code "*out*"] ". When we are done, we can " [:em "unstrument"] " the function, and Speculoos will no longer intervene."]
  
 #_ [:div.no-display (unstrument silly)]
  
 #_[:pre
      [:code "(unstrument silly)"]
      [:br]
      [:br]
      (print-form-then-eval "(silly 42 \\a 9)")]
  
 [:p "Even though character " [:code "\\a"] " and not-ratio " [:code "9"] " do not satisfy their respective predicates, " [:code "silly"] " is no longer instrumented, and Speculoos is not intercepting its invocation, so the function returns a value. (Frankly, I wrote " [:code "instrument/unstrument"] " to mimic the features " [:code "spec.alpha"] " offers. My implementation is squirrelly, and I'm unskilled with mutating vars. I lean much more towards the deterministic " [:code "validate-fn"] " and " [:code "validate-fn-with"] ".)"]
  
 [:p "Beyond validating a function's argument sequence and its return, Speculoos can perform a validation that checks the relationship between any aspect of the arguments and return values. When the arguments sequence and return sequence share a high degree of shape, an " [:em "argument versus return scalar specification"] " will work well. A good example of this is using " [:code "map"] " to transform a sequence of values. Each item in the return sequence has a corresponding item in the argument sequence."]
  
 #_[:pre
      [:code ";; let's validate the return of this function versus its args"]
      [:br]
      (print-form-then-eval "(defn mult-ten [& args] (map #(* 10 %) args))")
      [:br]
      [:br]
      (print-form-then-eval "(mult-ten 1 2 3)")]
  
 [:p "The predicates in a " [:em "versus"] " specification are a little bit unusual. Each predicate accepts " [:em "two"] " arguments: the first is the element from the argument sequence, the second is the corresponding element from the return sequence."]
  
 #_[:pre
      [:code ";; example scalar predicate"]
      [:br]
      (print-form-then-eval "(defn ten-times? [a r] (= (* 10 a) r))")]
  
 [:p "Let's make a bogus predicate that we know will fail, just so we can see what happens when a relationship is not satisfied."]
  
 #_[:pre
      [:code ";; intentionally broken scalar predicate"]
      [:br]
      (print-form-then-eval "(defn nine-times? [a r] (= (* 9 a) r))")]
  
 [:p "And stuff them into a map whose keys Speculoos recognizes."]
  
 #_[:pre
      [:code ";; an args versus return scalar specification"]
      [:br]
      (print-form-then-eval "(def mult-ten-vs-spec {:speculoos/arg-vs-ret-scalar-spec [ten-times? nine-times? ten-times?]})" 200 80)]
  
 [:p "Now that we've prepared our predicates and composed a versus specification, we can validate the relationship between the arguments and the returns. We'll invoke " [:code "mult-ten"] " with the same " [:code "1 2 3"] " sequence we saw above."]
  
 #_[:pre (print-form-then-eval "(validate-fn-with mult-ten mult-ten-vs-spec 1 2 3)")]
  
 [:p "We intentionally constructed our specification to fail at the middle element, and sure enough, the validation report tells us the argument and return scalars do not share the declared relationship. " [:code "20"] " from the return sequence is not nine times " [:code "2"] " from the arg sequence."]
  
 [:p "To complete the " [:em "scalars/collections/arguments/returns/versus"] " feature matrix, Speculoos can also validate function argument " [:em "collections"] " against return collections. All of the previous discussion holds, with the twist that the specification predicates apply against the argument collections and return collections. Examples show better than words."]
  
 #_[:pre
      (print-form-then-eval "(defn goofy-reverse [& args] (reverse args))")
      [:br]
      [:br]
      (print-form-then-eval "(goofy-reverse 1 2 3)")]
  
 [:p " Speculoos passes through the function's return if all predicates are satisfied, so we'll intentionally bungle one of the predicates to cause an invalidation report."]
  
 #_[:pre
      [:code ";; a collection specification that requires one vector to be longer than the other; "]
      [:br]
      (print-form-then-eval "(defn equal-lengths? \"Buggy!\" [v1 v2] (= (+ 1 (count v1)) (count v2)))")
      [:br]
      [:br]
      (print-form-then-eval "(defn mirror-elements-equal? [v1 v2] (= (first v1) (last v2)))")]
  
 [:p "Composing our args versus return collection specification, using the proper pseudo-qualified key. Remember: collection predicates apply to their immediate containing collections."]
  
 #_[:pre (print-form-then-eval "(def rev-coll-spec {:speculoos/arg-vs-ret-collection-spec [equal-lengths? mirror-elements-equal?]})")]
  
 [:p "Our goofy reverse function fails our buggy argument versus return validation."]
  
 #_[:pre (print-form-then-eval "(validate-fn-with goofy-reverse rev-coll-spec 1 2 3)")]
  
 [:p [:code "goofy-reverse"] " behaves exactly as it should, but for illustration purposes, we applied a buggy collection specification that we knew would fail. The validation report shows us the two things it compared, in this instance, the argument sequence and the returned, reversed sequence, and furthermore, that those two collections failed to satisfy the buggy predicate, " [:code "equal-lengths?"] ". The other predicate, " [:code "mirror-elements-equal?"] " was satisfied because the first element of the argument collection is equal to the last element of the return collection, and was therefore not included in the report."]

 [:h3#hof "Validating Higher-Order Functions"]
  
 [:p "Speculoos has a story about validating higher-order functions, too. It uses very similar patterns to first-order function validation: put some specifications in the function's metadata with the properly qualified keys, then invoke the function with some sample arguments, then Speculoos will validate the results. Here's how it works. We start with a flourish of the classic adder " [:span.small-caps "hof"] "."]
  
 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")
  [:br]
  [:br]
  (print-form-then-eval "(defn addder [x] (fn [y] (fn [z] (+ x (* y z)))))")
  [:br]
  [:br]
  (print-form-then-eval "(((addder 3) 2) 10)")]
  
 [:p [:code "addder"] " returns a function upon each of its first two invocations, and only on its third does it return a scalar. Specifying and validating a function value does not convey much meaning: it would merely satisfy " [:code "fn?"] " which isn't very interesting. So to validate a " [:span.small-caps "hof"] ", Speculoos requires it to be invoked until it produces a value. So we'll supply the validator with a series of arg sequences  that, when fed in order to the " [:span.small-caps "hof"] ", will produce a result. For the example above, it will look like " [:code "[3] [2] [10]"]  "."]
  
 [:p "The last task we must do is create the specification. " [:span.small-caps "hof"] " specifications live in the function's metadata at key " [:code ":speculoos/hof-specs"] " which is a series of nested specification maps, one nesting for each returned function. For this example, we might create this " [:span.small-caps "hof"] " specification."]
  
 [:pre (print-form-then-eval "(def addder-spec {:speculoos/arg-scalar-spec [even?]
                                                :speculoos/hof-specs {:speculoos/arg-scalar-spec [ratio?]
                                                                      :speculoos/hof-specs {:speculoos/arg-scalar-spec [float?]}}})")]
  
 [:p "Once again, for illustration purposes, we've crafted predicates that we know will invalidate, but will permit the function stack to evaluate to completion. (Otherwise, validation halts on exceptions."]
  
 [:p [:span.small-caps "hof"] " validation requires that the metadata hold the specifications. So we inject them."]
  
 [:pre (print-form-then-eval "(inject-specs! addder addder-spec)")]
  
 [:p "And finally, we execute the validation with " [:code "validate-higher-order-fn"]]
  
 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-higher-order-fn]])")
  [:br]
  [:br]
  (print-form-then-eval "(validate-higher-order-fn addder [3] [5] [10])")]
  
 [:p "Let's step through the validation results. Speculoos validates " [:code "3"] " against scalar predicate " [:code "even?"] " and then invokes "[:code "addder"] " with argument " [:code "3"] ". It then validates " [:code "5"] " against scalar predicate " [:code "ratio?"] " and then invokes the returned function with argument " [:code "5"] ". Finally, Speculoos validates " [:code "10"] " against scalar predicate " [:code "float?"] " and invokes the previously returned function with argument " [:code "10"] ". If all the predicates were satisfied, Speculoos would yield the return value of the function call. In this case, all three arguments are invalid, and Speculoos yields a validation report."]
  ]
