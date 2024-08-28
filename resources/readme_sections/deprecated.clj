;; This text refers to arg-vs-ret feature no longer provided by `validate-fn-with`
;; keeping this just in case there's an example or comment I'd like to retrieve

[:p "Perhaps by this point, it won't surprise you to learn that " [:code "validate-fn-with"] " handles two different " [:em "argument versus return"] " specifications: scalar and collection. They hold to exactly the same concepts as scalar specifications and collection specifications: scalars are always validated separately from collection validation."]

 [:p "There's a twist, though. A " [:em "versus"] " specification, both scalar or collection, contain 'predicates' that accept exactly two arguments: an element from the argument, and an element from the return value. In a scalar-versus-predicate, the function accepts two scalars that share paths. In a collection-versus-predicate, the function accepts two collections that share paths. (I'd like to avoid overloading 'predicate', but " [:em "comparator"] " is already used elsewhere in Clojure.)"]


 [:p "Let's build a little toy version. Pretend we've got a little machine that consumes three numbers and gives back each of those numbers incremented by one. That general process looks like this."]

 [:pre
  [:code ";; starting numbers"]
  [:br]
  [:code "[97 98 99]"]
  [:br]
  [:br]
  [:code ";; ... little machine does some work..."]
  [:br]
  [:br]
  [:code ";; final numbers"]
  [:br]
  [:code "[98 99 100]"]]

 [:p "To check the correctness of our little machine, we might want to express " [:em "Each of the final numbers is greater than its corresponding starting number."] " Clojure has a function that can perform that test on a single pair of numbers."]

 [:pre
  (print-form-then-eval "(< 97 98)")
  [:br]
  (print-form-then-eval "(< 98 99)")
  [:br]
  (print-form-then-eval "(< 99 100)")]

 [:p "All three tests verify that our little machine produced three numbers greater than the three numbers we started with. Let's make it a little more structured."]

 [:pre
  [:code ";; starting `99` and final `100` are both located at get-in `path [2]`"]
  [:br]
  (print-form-then-eval "(let [path [2]
                                start-datum (get-in [97 98 99] path)
                                final-datum (get-in [98 99 100] path)]
                            (< start-datum final-datum))")]

 [:p "We assumed a " [:code "get-in"] " path " [:code "[2]"] ", which grabs the " [:code "99"] " from the starting numbers and " [:code "100"] " from the final numbers. " [:code "100"] " is greater than " [:code "99"] ", so the " [:code "let"] " expression evaluates to " [:code "true"] ". We could edit the " [:code "path"] " binding to change which pairs of numbers are grabbed from the starting and final numbers."]

 [:p "It's tedious to do all three tests manually, so let's wrap that in a function that we can easily call and make the path changeable."]

 [:pre (print-form-then-eval "(defn greater?
                                 [start final path]
                                 (let [start-datum (get-in start path)
                                       final-datum (get-in final path)]
                                   (< start-datum final-datum)))")
  [:br]
  [:br]
  (print-form-then-eval "(greater? [97 98 99] [98 99 100] [2])")]

 [:p [:code "greater?"] " gives us is a predicate-like thing that accepts two collections, a path, returns a " [:code "true/false"] " if the final element is greater than the starting element. Let's examine the paths of our two sequences."]

 [:pre
  [:code ";; starting numbers"]
  [:br]
  (print-form-then-eval "(all-paths [97 98 99])")
  [:br]
  [:br]
  [:code ";; ending numbers"]
  [:br]
  (print-form-then-eval "(all-paths [98 99 100])")]

 [:p "Recalling Mantra #1, we're validating scalars, so we'll ignore the root vectors in both. Both vectors have three scalars, an integer located at paths " [:code "[0]"] ", " [:code "[1]"] ", and " [:code "[2]"] ". We could apply " [:code "greater?"] " manually to each of those three pairs."]

 [:pre
  (print-form-then-eval "(greater? [97 98 99] [98 99 100] [0])")
  [:br]
  (print-form-then-eval "(greater? [97 98 99] [98 99 100] [1])")
  [:br]
  (print-form-then-eval "(greater? [97 98 99] [98 99 100] [2])")]

 [:p "In each case, the integer in the final vector is greater than the corresponding integer in the start vector. Coming back to our original goal, let's remember that the 'start' vector is the sequence of arguments, and the 'final' vector is the output value of some little machine that increments the arguments. So " [:code "greater?"] " is vaguely a thing that could check whether that little machine works. Let's make that little machine."]

 [:pre
  (print-form-then-eval "(defn little-machine-1 [& args] (mapv inc args))")
  [:br]
  [:br]
  (print-form-then-eval "(little-machine-1 97 98 99)")]

 [:p "To our eyes, " [:code "little-machine-1"] " " [:em "appears"] " to work. Let's manually run some checks with " [:code "greater?"] " upon each of the three pairs."]

 [:pre
  (print-form-then-eval "(let [v [97 98 99]] (greater? v (apply little-machine-1 v) [0]))" 55 45)
  [:br]
  [:br]
  (print-form-then-eval "(let [v [97 98 99]] (greater? v (apply little-machine-1 v) [1]))" 55 45)
  [:br]
  [:br]
  (print-form-then-eval "(let [v [97 98 99]] (greater? v (apply little-machine-1 v) [2]))" 55 45)]

 [:p "Each of the " [:code "let"] " expressions above uses a predicate-like thing, " [:code "greater?"]  ", to validate a " [:em "relationship"] " between an argument scalar passed to " [:code "little-machine-1"] " and a return scalar produced by " [:code "little-machine-1"] "."]

 [:p "Wouldn't it be nice if there was some way to automate all that typing? That's exactly what a " [:em "argument versus return scalar specification"] " describes. Here's a summary of what " [:code "validate-fn-with"] " is going to do."]

 [:pre
  [:code "[97 98 99] ;; argument sequence passed to `little-machine-1`"]
  [:br]
  [:br]
  [:code "[98 99 100] ;; return sequence produced by `little-machine-1`"]
  [:br]
  [:br]
  [:code "[>  <  >] ;; arg vs ret scalar specification that validates the relationships between args/rets"]
  [:br]
  [:br]
  [:code "[false true false] ;; validation results of applying arg vs ret spec against args and rets"]]

 [:p "Because " [:code "validate-fn-with"] " yields the functions return if all predicates are satisfied, we'll intentionally mess up the specification so that the first and third elements fail. Here it is with zero relationships, so " [:code "little-machine-1"] " is invoked, but not validated."]

 [:pre
  (print-form-then-eval "(validate-fn-with little-machine-1 {} 97 98 99)")
  [:br]
  [:br]
  [:code ";; same as"]
  [:br]
  (print-form-then-eval "(little-machine-1 97 98 99)")]

 [:p "Since we gave it zero relationships to test, " [:code "little-machine-1"] "'s results merely passed through."]

 [:p " Now, we'll add the " [:em "argument versus return scalar specification"] " associated where " [:code "validate-fn-with"] " knows to find it."]

 [:pre (print-form-then-eval "(validate-fn-with little-machine-1 {:speculoos/arg-vs-ret-scalar-spec [> < >]} 97 98 99)")]

 [:p "Much like how " [:code "validate-scalars"] " operates, " [:code "validate-fn-with"] " systematically applies the relationship tests to argument+scalar pairs."]

 [:ul
  [:li "At shared path " [:code "[0]"] ", " [:code "validate-fn-with"] " evaluated " (print-form-then-eval "(> 97 98)")]
  [:li "At shared path " [:code "[1]"] ", " [:code "validate-fn-with"] " evaluated " (print-form-then-eval "(< 98 99)")]
  [:li "At shared path " [:code "[2]"] ", " [:code "validate-fn-with"] " evaluated " (print-form-then-eval "(> 99 100)")]]

 [:p "We receive two pairs of scalars that did not satisfy their specified relationship because we intentionally flipped the " [:code "<"] " relationship test to " [:code ">"] ". " [:code "validate-fn-with"] " returns only invalid relationships, so we don't see a result for " [:code "(< 98 99)"] "."]

 [:p "Let's do another example where each of the arguments has a different relationship to it return value. " [:em "A string truncated to three characters, an number multiplied by ten, and a symbol converted into a string"] ". Sometimes, it's helpful to write the relationships first."]

 [:pre
  (print-form-then-eval "(defn str-length-3? [s1 s2] (= s2 (subs s1 0 3)))")
  [:br]
  [:br]
  (print-form-then-eval "(str-length-3? \"abcdef\" \"abc\")")
  [:br]
  (print-form-then-eval "(str-length-3? \"abcdef\" \"abcdef\")")
  [:br]
  [:br]
  (print-form-then-eval "(defn ten-times? [n1 n2] (= n2 (* 10 n1)))")
  [:br]
  [:br]
  (print-form-then-eval "(ten-times? 7 70)")
  [:br]
  (print-form-then-eval "(ten-times? 7 7)")
  [:br]
  [:br]
  (print-form-then-eval "(defn stringified-sym? [sym str?] (= str? (str sym)))")
  [:br]
  [:br]
  (print-form-then-eval "(stringified-sym? 'foo \"foo\")")
  [:br]
  (print-form-then-eval "(stringified-sym? 'foo 'foo)")]

 [:p "Now that we've established the three relationships, let's plan the tasks. The sketch looks like this."]

 [:pre
  [:code "[\"foobar\" 7  'baz] ;; argument sequence"]
  [:br]
  [:br]
  [:code "[\"foo\"    70 \"baz\"] ;; expected return sequence"]
  [:br]
  [:br]
  [:code "[str-length-3? ten-times? stringified-sym?] ;; scalar relationships"]
  [:br]
  [:br]
  [:code "[true true true] ;; all relationships valid"]]

 [:p "Now we'll write our new little machine. Maybe we wrote it before we had our morning coffee. It's got a couple of bugs."]

 [:pre
  (print-form-then-eval "(defn little-machine-2 [& args] (vector (identity (nth args 0))
                                                                 (* 10 (nth args 1))
                                                                 (identity (nth args 2))))" 45 45)
  [:br]
  [:br]
  (print-form-then-eval "(little-machine-2 \"foobar\" 7 'baz)" 45 45)]

 [:p "The first string is not truncated to three characters, and the third symbol is not stringified. " [:code "validate-fn-with"] " ought to interrupt with a couple of comments. We'll need to associate the scalar relationships at the proper key."]

 [:pre (print-form-then-eval "(validate-fn-with little-machine-2 {:speculoos/arg-vs-ret-scalar-spec [str-length-3? ten-times? stringified-sym?]} \"foobar\" 7 'baz)" 115 85)]

 [:p "Indeed, " [:code "validate-fn-with"] " reports two invalid relationships. Scalar relationship " [:code "str-length-3?"] " was not satisfied and scalar relationship " [:code "stringified-sym?"] " was also not satified."]

 [:p "Now that we've drunk a nice cup of coffee, we fix our little machine."]

 [:pre
  (print-form-then-eval "(defn little-machine-3 [& args] (vector (subs (nth args 0) 0 3)
                                                                 (* 10 (nth args 1))
                                                                 (str (nth args 2))))" 45 45)
  [:br]
  [:br]
  (print-form-then-eval "(little-machine-3 \"foobar\" 7 'baz)" 45 45)]

 [:p "And validate " [:code "little-machine-3"] "'s scalar relationships."]

 [:pre (print-form-then-eval "(validate-fn-with little-machine-3 {:speculoos/arg-vs-ret-scalar-spec [str-length-3? ten-times? stringified-sym?]} \"foobar\" 7 'baz)" 115 85)]

 [:p "Yup. The three scalars returned by " [:code "little-machine-3"] " fulfill all three specified relationships to their corresponding arguments, so the return values merely pass through."]

 [:p "The " [:em "argument versus return scalar relationship"] " validation is perhaps limited in usefulness because to participate in relationship validation, the scalars in the return must share paths with scalars in the arguments. This requires a high degree of homology between the return collection, and the arguments " [:em "considered as a sequence."] " We contorted " [:code "little-machine-1/2/3"] " to fulfill that homology, but that arrangement may not often happen in our normal everyday development."]

 [:p "For more flexibility, we can specify and validate the relationships between argument collections and return collections."]
 
 [:p "Let's try an example. We'd like to see if our yet-to-be-written reversing function works correctly. A reverse function produces an output sequence, containing exactly the same input elements, in reversed order. We jot down a few collection relationship functions that we could test at the REPL."]

 [:pre
  (print-form-then-eval "(defn equal-count? [s1 s2] (= (count s1) (count s2)))")
  [:br]
  [:br]
  (print-form-then-eval "(equal-count? [11 22 33] [33 22 11])")
  [:br]
  (print-form-then-eval "(equal-count? [11 22 33] [33])")
  [:br]
  [:br]
  (print-form-then-eval "(defn same-elements? [s1 s2] (empty? (clojure.set/difference (set s1) (set s2))))")
  [:br]
  [:br]
  (print-form-then-eval "(same-elements? [11 22 33] [33 22 11])")
  [:br]
  (print-form-then-eval "(same-elements? [11 22 33] [55 66 77])")
  [:br]
  [:br]
  (print-form-then-eval "(defn reversed? [s1 s2] (= (reverse s1) s2))")
  [:br]
  [:br]
  (print-form-then-eval "(reversed? [11 22 33] [33 22 11])")
  [:br]
  (print-form-then-eval "(reversed? [11 22 33] [11 22 33])")]

 [:p "These three relationship functions each consume two collections and return a " [:code "true/false"] " based on whether the relationship is satisfied. Ultimately, the argument sequence will serve as " [:code "s1"] " and the function's return collection will serve as " [:code "s2"] "."]

 [:p "Let's sketch out by hand how we'd validate the collection relationships."]

 [:pre
  [:code "[96 97 98 99 100] ;; collection passed as the argument"]
  [:br]
  [:br]
  [:code "[100 99 98 97 96] ;; collection returned from a properly-working reversing function"]
  [:br]
  [:br]
  [:code "[equal-count? same-elements? reversed?] ;; collection relationships"]
  [:br]
  [:br]
  [:code "[true true true] ;; three relationships all satisfied"]]

 [:p "Remember the lesson from " [:a {:href "collection-validation"} "validating collections"] ": collection predicates apply to their immediate parent container. That means " [:code "equal-count?"] ", " [:code "same-elements?"] ", and " [:code "reversed?"] " all consume the two collections, the argument sequence and the function's return sequence. Don't get distracted by the integers; they're merely contents. Collection validation targets only collections."]

 [:pre
  (print-form-then-eval "(defn equal-count? [[s1] s2] (= (count s1) (count s2)))")
  [:br]
  [:br]
  (print-form-then-eval "(defn same-elements? [[s1] s2] (empty? (clojure.set/difference (set s1) (set s2))))" 95 80)
  [:br]
  [:br]
  (print-form-then-eval "(defn reversed? [[s1] s2] (= (reverse s1) s2))")]

 [:p "Let's do a couple of relationship validations by hand. First, a completely by-hand."]

 [:pre
  [:code ";; extract a relationship-fn"]
  [:br]
  (print-form-then-eval "(get-in [equal-count? same-elements? reversed?] [0])" 80 80)
  [:br]
  [:br]
  [:code ";; extract args at parent's path"]
  [:br]
  (print-form-then-eval "(get-in [96 97 98 99 100] (drop-last [0]))" 55 55)
  [:br]
  [:br]
  [:code ";; apply extracted relationship-fn to extracted args and extracted return"]
  [:br]
  (print-form-then-eval "(let [arg [[96 97 98 99 100]]
                                ret [100 99 98 97 96]
                                relationship-fns [equal-count? same-elements? reversed?]
                                path [0]]
                            ((get-in relationship-fns path)
                             (get-in arg (drop-last path))
                             (get-in ret (drop-last path))))")]

 [:p "Second, just for fun, we can function-ize it to make it more general."]

 [:pre (print-form-then-eval "(defn satisfied-relationship?
                                 [argument return relationship-fns path]
                                 ((get-in relationship-fns path)
                                  (get-in argument (drop-last path))
                                  (get-in return (drop-last path))))")
  [:br]
  [:br]
  [:code ";; test `equal-count?` at path [0]"]
  [:br]
  (print-form-then-eval "(satisfied-relationship? [[96 97 98 99 100]] [100 99 98 97 96] [equal-count? same-elements? reversed?] [0])")
  [:br]
  [:br]
  [:code ";; test `same-elements?` at path [1]"]
  [:br]
  (print-form-then-eval "(satisfied-relationship? [[96 97 98 99 100]] [100 99 98 97 96] [equal-count? same-elements? reversed?] [1])")
  [:br]
  [:br]
  [:code ";; test `reversed?` at path [2]"]
  [:br]
  (print-form-then-eval "(satisfied-relationship? [[96 97 98 99 100]] [100 99 98 97 96] [equal-count? same-elements? reversed?] [2])")]

 [:p "At this point, we've created three collection relationship validation functions and tested them on synthetic argument collections and return collections. And we've done a few tedious manual relationship validations. It's due time to use " [:code "validate-fn-with"] ". Let's create a completely broken reversing function so we can see all three invalidations."]

 [:pre
  (print-form-then-eval "(defn broken-reverse [v] (conj v 42))")
  [:br]
  [:br]
  (print-form-then-eval "(broken-reverse [96 97 98 99 100])")]

 [:p [:code "broken-reverse"] " returns a longer vector, with different elements, and definitively " [:em "not"] " reversed. It will spectacularly fail all three collection relationship functions."]

 [:p "As before, we must associate the specification to the proper key recognized by " [:code "validate-fn-with"] "."]

 [:pre [:code "{:speculoos/arg-vs-ret-collection-spec [equal-count? same-elements? reversed?]}"]]

 [:p "With all that done, let's finally run the validation."]

 ;; NOTE! `same-elements?` from above using clojure.set/difference has a bug
 ;; the version at the head of the following :pre block is correct
 
 [:pre
  (print-form-then-eval "(defn same-elements? [[s1] s2] (= (sort s1) (sort s2)))")
  [:br]
  [:br]
  [:code ";; no relationship specifications, `broken-reverse` output passes through"]
  [:br]
  (print-form-then-eval "(validate-fn-with broken-reverse {} [96 97 98 99 100])")
  [:br]
  [:br]
  [:code ";; associated collection relationship specification, invalid reports"]
  [:br]
  (print-form-then-eval "(validate-fn-with broken-reverse {:speculoos/arg-vs-ret-collection-spec [equal-count? same-elements? reversed?]} [96 97 98 99 100])" 95 75)]

 (same-elements? [[11 22 33]] [33 22 11])
 (same-elements? [[11 22 22 33]] [11 22 33 33])