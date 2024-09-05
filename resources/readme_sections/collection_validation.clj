[:section#collection-validation
 [:h2 "Collection Validation"]

 [:p "You may have been uncomfortably shifting in your chair while reading through the examples above. Every example we've seen so far shows Speculoos validating individual scalars, such as integers, strings, booleans, etc."]

 [:pre (print-form-then-eval "(valid-scalars? [99 \"qwz\" -88] [int? string? neg-int?])" 40 80)]

 [:p "However, we might need to specify some property of a collection itself, such as a vector's length, the presence of a key in a map, relationships " [:em "between"] " datums, etc. That is " [:em "collection validation"] "."]

 [:p "One way to visualize the difference is this."]

 [:pre
  [:code " v----v-------v------v---- scalar validation targets these things"]
  [:br]
  [:code "[42   \\z {:x 'foo :y 22.7}]"]]

 [:p "In contrast…"]
 [:pre
  [:code "v -------v---------------v-v---- collection validation targets these things"]
  [:br]
  [:code "[42   \\z {:x 'foo :y 22.7} ]"]]

 [:p "One of Speculoos' main concepts is that scalars are specified and validated explicitly separately from collections. You perhaps noticed that the function name we have been using wasn't " [:code "validate"] " but instead " [:code "validate-scalars"] ". Speculoos provides a parallel group of functions to validate the properties of collections, independent of the scalar values they contain. Let's examine why and how they're separated."]

 [:p "Imagine we wanted to specify that our data vector was exactly three elements long. The paths of that data might look like this."]
 [:pre (print-form-then-eval "(all-paths [42 \"abc\" 22/7])")]

 [:p "Since we're now interested in specifying collections, we'll discard the " [:em "scalars"] " and focus only on the " [:em "collections"] ". In this case, there's only one collection, the vector at path " [:code "[]"] ", which signifies that it's the root collection."]

 [:p "We could try to write a specification with a bare predicate, like this."]
 [:pre
  [:code ";; a predicate that returns true if the collection has three elements"]
  [:br]
  (print-form-then-eval "(def len-3? #(= 3 (count %)))")]

 [:p "Then we could imagine some function might do this."]
 [:pre
  [:code ";; this fn doesn't actually exist"]
  [:br]
  [:code "(imaginary-validate-collection [42 'foo \\z] len-3?) ;; => true"]]

 [:p "Okay, that scenario maybe kinda could work. But what about this scenario: " [:em "A three-element vector nested within a two-element vector"] ". The paths would look like this."]
 [:pre (print-form-then-eval "(all-paths [11 [22 33 44]])")]

 [:p "Oh. Still ignoring the scalars, there are now two vectors which would be targets for our predicate, one at the root, and one at path " [:code "[1]"] ". We can't merely supply a pair of bare predicates to our " [:code "imaginary-validate-collection"] " function and have it magically know how to apply the predicates to the correct vector."]

 [:p "It quickly becomes apparent that we need to somehow arrange our collection predicates inside some kind of structure that will instruct the validation function where to apply the predicates. One of Speculoos' principles is " [:em "Make the specification shaped like the data"] ". Let me propose this structure."]

 [:pre [:code "[len-3? [len-3?]]"]]

 [:p "What do the paths of that thing look like?"]

 [:pre (print-form-then-eval "(all-paths [len-3? [len-3?]])")]

 [:p "Hmm. In the previous " [:a {:href "#scalar-validation"} "section"] ", when we were validating scalars, we followed the principle that validation only proceeds when a predicate in the specification shares the exact path as the scalar in the data. However, we can now see an issue if we try to apply that principle here. The nested vector of the data is located at path " [:code "[1]"] ". The nested predicate in the specification is located at path " [:code "[1 0]"] ", nearly same except for the trailing " [:code "0"] ". The root vector of the data is located at path " [:code "[]"] " while the predicate is located at path " [:code "[0]"] " of the specification, again, nearly the same except for the trailing zero. Clojure has a nice core function that performs that transformation."]
 [:pre
  (print-form-then-eval "(drop-last [1 0])")
  [:br]
  (print-form-then-eval "(drop-last [0])")]

 [:p "The slightly modified rule for validating collections is " [:em "Collection predicates in the specification are applied to the collection in the data that correspond to their parent."] " In other words, the predicate at path " [:code "pth"] " in the collection specification is applied to the collection at path " [:code "(drop-last pth)"] " in the data."]

 [:p "The modified algorithm for validating collections is as follows:"
  [:ol
   [:li "Run " [:code "all-paths"] " on the data, then the specification."]
   [:li "Remove " [:em "scalar"] " elements from the data, keeping only the collection elements."]
   [:li "Remove " [:em "non-predicate"] " elements from the collection specification."]
   [:li "Pair predicates at path " [:code "pth"] " in the specification with collections at path " [:code "(drop-last pth)"] " in the data. Discard all other un-paired collections and un-paired predicates."]
   [:li "For each remaining collection+predicate pair, apply the predicate to the collection."]]]

 [:p "Speculoos provides a function, " [:code "validate-collections"] ", that does that for us. Let's see."]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [validate-collections]])")
  [:br]
  [:br]
  (print-form-then-eval "(validate-collections [11 [22 33 44]] [len-3? [len-3?]])" 50 80)]

 [:p "Much of that looks familiar. " [:code "validate-collections"] " returns a validation entry for each to the two collections+predicate pairs. The " [:code ":datum"] " stuff represent the things being tested and the " [:code ":predicate"] "s report the predicate functions, and similarly, " [:code "valid?"] " reports whether that predicate was satisfied."]

 [:p "There are now three things that involve the concept of a path: the predicate was found at " [:code ":path-predicate"] " in the specification and the datum was found at " [:code ":ordinal-path-datum"] " in the data, which is also presented in a more friendly format as the literal path " [:code ":path-datum"] ". (We'll explain the terms embodied by these keywords as the discussion progresses.) In this example, the nested vector contains three elements, so its predicate was satisfied, while the root vector contains only two elements, and thus failed to satisfy its predicate."]

 [:p "Let's take a look at validating nested maps. Here are the paths of some example data."]

 [:pre (print-form-then-eval "(all-paths {:x 11 :y {:z 22}})")]

 [:p "Two scalars, which " [:code "validate-collections"] " ignores, and two collections. Let's apply our rule: the predicate in the specification applies to the collection in the data whose path is one element shorter. The two collections are located at paths " [:code "[]"] " and " [:code "[:y]"] ". To write a collection specification, we'd mimic the shape of the data, inserting predicates that apply to the parent. We can't simply write"]

 [:pre [:code "{map? {map?}}"]]

 [:p "because maps must contain an even number of forms. So we're going to need to add some keys in there. Technically, you could key your collection predicates however you want, but I strongly recommend choosing a key that doesn't appear in the data. This example shows why. We could put a predicate at key " [:code ":y"] " of the specification, and " [:code "validate-collections"] " will merrily chug along."]

 [:pre (print-form-then-eval "(validate-collections {:x 11 :y {:z 22}} {:y map?})" 45 80)]

 [:p "We can see that the singular " [:code "map?"] " predicate located at specification path " [:code "[:y]"] " was indeed applied to the root container at data path " [:code "(drop-last [:y])"] " which evaluates to path " [:code "[]"] ". But now we've consumed that key, and it cannot be used to target the nested map " [:code "{:z 22}"] " at " [:code "[:y]"] " in the data."]

 [:p "If we had instead invented a synthetic key, " [:code "drop-last"] " would trim it off the right end and the predicate would still be applied to the root container, while key " [:code ":y"] " remains available to target the nested map. In practice, I like to invent keys that are descriptive of the predicate so the validation results are easier to scan by eye."]

 [:pre (print-form-then-eval "(validate-collections {:x 11 :y {:z 22}} {:is-a-map? map? :y {:is-a-set? set?}})")]

 [:p "Notice that " [:code "validate-collections"] " completely ignored the scalars " [:code "11"] " and " [:code "22"] " at data keys " [:code ":x"] " and " [:code ":z"] ". It only applied predicate " [:code "map?"] " to the root of data and predicate " [:code "set?"] " to the nested map at key " [:code ":y"] ", which failed to satisfy." ]

 [:p "Let me emphasize: within a collection specification, the name of the predicate keys targeting a nested map have " [:em "absolutely no bearing on the operation of the validation"] "; they get truncated by the " [:code "drop-last"] " operation. We could have used something misleading like this."]

 [:pre
  [:code ";;             this keyword… ---v         v--- …gives the wrong impression about this predicate"]
  [:br]
  (print-form-then-eval "(validate-collections {:x 11} {:is-a-map? vector?})" 80 80)]
 [:p "Despite the key suggesting that we're testing for a map, the actual predicate tests for a vector, and returns " [:code "false"] "."]

 [:p "Here's something interesting."]

 [:pre (print-form-then-eval "(validate-collections [42] [vector? map?])" 40 80)]

 [:p "If we focus on the paths of the two predicates in the specification, we see that both " [:code "vector?"] " and " [:code "map?"] " target the root container because " [:code "(drop-last [0])"] " and " [:code "(drop-last [1])"] " both evaluate to the same path in the data. So we have another consideration: " [:em "Every"] " predicate in a specification's collection applies to the parent collection in the data. This means that we can apply an unlimited number of predicates to each collection."]

 [:pre (print-form-then-eval "(validate-collections [42] [vector? map? list? set? coll?])" 55 80)]

 [:p  "If " [:strong "any"] " number of predicates apply to the parent collection, there might be zero to infinity predicates before we encounter a nested collection in that sequence. How, then, does " [:code "validate-collections"] " determine where to apply the predicate inside a nested collection?"]
 
 [:p "The rule " [:code "validate-collections"] " follows is " [:em "Apply nested collection predicates in the order which they appear, ignoring scalars."] " Let's see that in action. First, we'll make some example data composed of a parent vector, containing a nested map, list, and set, with a couple of interleaved integers."]

 [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

 [:p "Now we need to compose a collection specification. Mantra #2 reminds us to make the specification mimic the shape of the data. I'm going to copy-paste the data and mash the delete key to remove the scalar datums."]

 [:pre [:code "[{     }    (       )    #{  }]"]]

 [:p "Just to emphasize how they align, here are the data (top) and the collection specification (bottom) with some spaced formatting."]
 [:pre
  [:code "[{:a 11} 22 (list 33) 44 #{55}] ;; <--- data"]
  [:br]
  [:code "[{     }    (       )    #{  }] ;; <--- collection specification"]
  [:br]
  [:code " ^--- 1st   ^--- 2nd     ^--- 3rd collection"]]

 [:p "The first thing to note is that our collection specification looks a lot like our data with all the scalars removed. The second thing to notice is that even though it contains zero predicates, that's a legitimate collection specification which " [:code "validate-collections"] " can consume. Check this out."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{}()#{}])")]

 [:p "Validation ignores collections in the data that are not paired with a predicate in the specification."]

 [:p "Okay, let's add a predicate. Let's specify that the second nested collection is a list. Predicates apply to their container, so we'll insert " [:code "list?"] " into the corresponding collection."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} (list list?) #{}])" 55 80)]

 [:p "One predicate in the specification pairs with one collection in the data, so we receive one validation result. The " [:code "list?"] " predicate at path " [:code "[1 0]"] " in the specification was applied to the collection located at path " [:code "[2]"] " in the data. That nested collection is indeed a list, so " [:code ":valid?"] " is " [:code "true"] "." ]

 [:p " Notice how " [:code "validate-collections"] " did some tedious and boring calculations to achieve the general effect of " [:em "The predicate in the second nested collection of the specification applies to the second nested collection of the data."] " It kinda skipped over that " [:code "22"] " because it ignores scalars, and we're validating collections."]

 [:p "Let's clear the slate and specify that nested set at the end."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{set?}])" 55 80)]

 [:p "One predicate applied to one collection, one validation result. And again, collection validation skipped right over the intervening scalars " [:code "22"] " and " [:code "44"] " in the data. " [:code "validate-collections"] " applied the predicate in the specification's third nested collection to the data's third nested collection."]

 [:p "We might as well specify that nested map now. Recall that collection predicates targeting a map require a sham key. Removing the " [:code "set?"] " predicate from the previous example, we'll insert a " [:code "map?"] " predicate at a key in the specification that doesn't appear in the data's nested map."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{:is-map? map?}()#{}])" 55 80)]

 [:p "Unlike the previous two validations, " [:code "validate-collections"] " didn't have to skip over any scalars. It merely applied the predicate in the specification's first nested collection to the data's first nested collection, which is indeed a map."]

 [:p "We've now seen how to specify and validate each of those three nested collections, so for completeness' sake, let's specify the root. Predicates apply to their container, so for clarity, we'll insert it at the beginning."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])" 55 80)]

 [:p "Technically, we could put that particular predicate anywhere in the top-level vector as long " [:code "(drop-last path)"] " evaluates to " [:code "[]"] ". All the following yield substantially the same results."]

 [:pre
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])"]
  [:br]
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} vector? () #{}])"]
  [:br]
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () vector? #{}])"]
  [:br]
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{} vector?])"]]

 [:p "In practice, I find it visually clearer to insert the predicates at the front."]

 [:p "Let's do one final, all-up demonstration."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {:is-map? map?} sequential? (list list?) coll? #{set?} any?])" 95 100)]

 [:p [:code "validate-collections"] " applied to the data's root four predicates — " [:code "vector?"] ", "  [:code "sequential?"] ", "  [:code "coll?"] ", and "  [:code "any?"] " — which we interleaved among the nested collections. In addition, it validated each of the three nested collections, skipping over the intervening scalars."]

 [:p "Collections nested within a map do not involve that kind of skipping because they're not sequential. Let's make this our example data."]

 [:pre [:code "{:a [99] :b (list 77)}"]]

 [:p "Now, we copy-paste the data, then delete the scalars."]

 [:pre [:code "{:a [  ] :b (list   )}"]]

 [:p "That becomes the template for our collection specification. Let's pretend we want to specify something about those two nested collections at keys " [:code ":a"]  " and " [:code ":b"] ". We stuff the predicates " [:em "directly inside those collections"] "."]

 [:pre [:code "{:a [vector?] :b (list list?)}"]]

 [:p "This becomes our collection specification. Let's see what happens."]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?)})" 55 80)]

 [:p "Checklist time."
  [:ul
  [:li "Specification shape mimics data? " [:em "Check."]]
  [:li "Validating collections, ignoring scalars? " [:em "Check."]]
  [:li "Two paired predicates, two validations? " [:em "Check."]]]]

 [:p "There's a subtlety to pay attention to: the " [:code "vector?"] " and " [:code "list?"] " predicates are contained within a vector and list, respectively. Those two predicates apply to their " [:em "immediate"] " parent container. " [:code "validate-collections"] " needs those " [:code ":a"] " and " [:code ":b"] " keys to find that vector and that list. You only use a sham key when validating the map immediately above your head."]

 [:p "Let's re-use that validation and tack on a sham key with a predicate aimed at the root map."]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?) :howdy map?})" 75 90)]

 [:p "We've got the vector and list validations as before, and then, at the end, we see that " [:code "map?"] " at the sham " [:code ":howdy"] " key was applied to the root."]

 [:p "One more example to illustrate this point. Again, here's our data."]

 [:pre [:code "{:a [99] :b (list 77)}"]]

 [:p "And again, we'll copy-paste the data, then delete the scalars. That'll be our template for our collection specification."]

 [:pre [:code "{:a [  ] :b (list   )}"]]

 [:p "Now, we'll go even further and delete the " [:code ":b"] "  key and its value, the nested list."]

 [:pre
  [:code "{:a [  ]             }"]
  [:br]
  [:br]
  [:code ";; without :b, won't be able to validate the list"]]

 [:p "Insert old reliable " [:code "vector?"] ". That predicate is paired with its immediate parent vector, so we need to keep the " [:code ":a"] " key."]

 [:pre [:code "{:a [vector?]        }"]]

 [:p "Finally, we'll add in a wholly different key, with a " [:code "coll?"] " predicate nested in a collection at the new key."]

 [:pre [:code "{:a [vector?] :flamingo [coll?]}"]]

 [:p "Test yourself: How many validations will occur?"]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :flamingo [coll?]})" 60 90)]

 [:p "In this example, there is only one predicate+collection pair. " [:code "vector?"] " applies to the vector at " [:code ":a"] ". We might have expected " [:code "coll?"] " to be applied somewhere because " [:code ":flamingo"] " doesn't appear in the map, but notice that " [:code "coll?"] " is " [:em "contained"] " in a vector. It would only ever apply to the thing that contained it. Since the data's root doesn't contain a collection at that key, the predicate is unpaired, and thus ignored. If we wanted to apply " [:code "coll?"] " to the root, we peel off its immediate container."]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :emu coll?})" 65 80)]

 [:p [:em "Now, "] [:code "coll?"] "'s immediate container is the root. Since it is now properly paired with a collection, it participates in validation. "]]