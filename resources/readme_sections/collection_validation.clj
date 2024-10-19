[:section#collection-validation
 [:h2 "Collection Validation"]

 [:p "You may have been uncomfortably shifting in your chair while reading through the examples above. Every example we've seen so far shows Speculoos validating individual scalars, such as integers, strings, booleans, etc."]

 [:pre (print-form-then-eval "(valid-scalars? [99 \"qwz\" -88] [int? string? neg-int?])" 40 80)]

 [:p "However, we might need to specify some property of a collection itself, such as a vector's length, the presence of a key in a map, relationships " [:em "between"] " datums, etc. That is " [:em "collection validation"] "."]

 [:p "One way to visualize the difference is this. Scalar validation targets…"]

 [:pre
  [:code " v----v-------v------v---- scalar validation targets these things"]
  [:br]
  [:code "[42   \\z {:x 'foo :y 9.87} ]"]]

 [:p "…integers, characters, symbols, etc."]

 [:p "In contrast, collection validation targets…"]
 [:pre
  [:code "v--------v---------------v-v---- collection validation targets these things"]
  [:br]
  [:code "[42   \\z {:x 'foo :y 9.87} ]"]]

 [:p "…vectors, maps, sequences, lists, and sets."]

 [:p "One of Speculoos' main concepts is that scalars are specified and validated explicitly separately from collections. You perhaps noticed that the function name we have been using wasn't " [:code "validate"] " but instead " [:code "validate-scalars"] ". Speculoos provides a distinct group of functions to validate the properties of collections, independent of the scalar values contained within the collection. The collection validation functions are distinguished by a " [:code "-collections"] " suffix. Let's examine why and how they're distinct."]

 [:p "Imagine we wanted to specify that our data vector was exactly three elements long. We might reasonably write this predicate, whose argument is a collection."]

 [:pre
  [:code ";; a predicate that returns `true` if the collection has three elements"]
  [:br]
  [:br]
  (print-form-then-eval "(defn len-3? [c] (= 3 (count c)))")]

 [:p "Notice that this predicate tests a property of the collection itself: the number of elements it contains. " [:code "validate-scalars"] " has no way to do this kind of test because it deliberately only considers scalars."]

 [:p "Now, we invent some example data."]

 [:pre [:code "[42 \"abc\" 22/7]"]]

 [:p "The paths of that data look like this."]

 [:pre (print-form-then-eval "(all-paths [42 \"abc\" 22/7])")]

 [:p "We're validating collections (Motto #1), so we're only interested in the root collection at path " [:code "[]"] " in the data. Let's apply Motto #2 and shape our specification to mimic the shape of the data. We'll copy-paste the data…"]

 [:pre [:code "[42 \"abc\" 22/7]"]]

 [:p "…delete the contents…"]

 [:pre [:code "[             ]"]]

 [:p "…and replace the contents with our " [:code "len-3?"] " predicate."]

 [:pre [:code "[len-3?       ]"]]

 [:p "That will be our specification. Notice: during collection validation, we insert predicates " [:em "inside"] " the collection that they target."]

 [:p "Validating collections uses a " [:em "slightly"] " adjusted version of the " [:a {:href "#scalar-algorithm"} "scalar validation algorithm"] ". (If you are curious " [:em "why"] " the collection algorithm is different, see " [:a {:href "#collection-predicate-paths"} "this later subsection"] ".) The algorithm for validating collections is as follows:"]

 [:ol.collection-algorithm
  [:li "Run " [:code "all-paths"] " on the data, then the specification."]
  [:li "Remove " [:em "scalar"] " elements from the data, keeping only the collection elements."]
  [:li "Remove " [:em "non-predicate"] " elements from the collection specification."]
  [:li "Pair predicates at path " [:code "pth"] " in the specification with collections at path " [:code "(drop-last pth)"] " in the data. Discard all other un-paired collections and un-paired predicates."]
  [:li "For each remaining collection+predicate pair, apply the predicate to the collection."]]

 [:p "Let's perform that algorithm manually. We run " [:code "all-paths"] " on both the data…"]

 [:pre (print-form-then-eval "(all-paths [42 \"abc\" 22/7])")]

 [:p "…and " [:code "all-paths"] " on our collection specification."]

 [:pre (print-form-then-eval "(all-paths [len-3?])")]

 [:p "We discard all scalar elements of the data, keeping only the collection elements."]

 [:pre [:code "[{:path [], :value [42 \"abc\" 22/7]}]"]]

 [:p "And we keep only the predicate elements of the specification."]

 [:pre [:code "[{:path [0], :value len-3?}]"]]

 [:p "The next step, pairing predicates to a target collection, is where it gets interesting. During scalar validation, we paired a predicate with a scalar when they shared the exact same path. That " [:a {:href "#collection-predicate-paths"} "doesn't work"] " for collection validation. Instead, we pair a collection and a predicate when the collection's path in the data is equivalent to " [:code "(drop-last pth)"] ", where " [:code "pth"] " is the predicate's path in the specification."]

 [:p "Looking at the previous two results, we see the root collection is path " [:code "[]"] ", while the " [:code "len-3?"] " predicate's path is " [:code "[0]"] ". " [:code "(drop-last [0])"] " evaluates to " [:code "()"] ", which is equivalent to the root path. So the predicate and the collection are paired. We then apply the predicate."]

 [:pre (print-form-then-eval "(len-3? [42 \"abc\" 22/7])")]

 [:p "The root collection " [:code "[42 \"abc\" 22/7]"] " satisfies the " [:code "len-3?"] " predicate because it contains three elements, so the validation returns " [:code "true"] "."]

 [:p "Speculoos provides a function, " [:code "validate-collections"] ", that does all that for us. The function signature is similar to what we saw earlier while validating scalars: data on the upper row, and the specification mimicking the shape of the data on the lower row."]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [validate-collections]])")
  [:br]
  [:br]
  (print-form-then-eval "(validate-collections [42 \"abc\" 22/7] [len-3?])" 45 80)]

 [:p "Much of that looks familiar. " [:code "validate-collections"] " returns a validation entry for every collection+predicate pair. In this case, the data's root vector was paired with the single " [:code "len-3?"] "predicate. The " [:code ":datum"] " represents the thing being tested, the " [:code ":predicate"] "s indicate the predicate functions, and " [:code "valid?"] " reports whether that predicate was satisfied. The root vector contains three elements, so " [:code "len-3?"] " was satisfied."]

 [:p "There are now three things that involve some notion of a path. The predicate was found at " [:code ":path-predicate"] " in the specification. The datum was found at " [:code ":ordinal-path-datum"] " in the data, which is also presented in a more friendly format as the literal path " [:code ":path-datum"] ". (We'll explain the terms embodied by these keywords as the discussion progresses.) Notice that the path of the root vector " [:code "[]"] " is equivalent to running " [:code "drop-last"] " on the path of the " [:code "len-3?"] " predicate: " [:code "(drop-last [0])"] " evaluates to " [:code "()"] "."]

 [:p "Let's explore validating a two-element vector nested within a two-element vector. To test whether each of those two vectors contain two elements, we could write this collection predicate."]

 [:pre (print-form-then-eval "(defn len-2? [c] (= 2 (count c)))")]

 [:p "Remember Motto #1: This predicate accepts a collection, " [:code "c"] ", not a scalar."]

 [:p "We'll invent some data, a two-element vector nested within a two-element vector by wrapping the final two elements inside an additional pair of brackets."]

 [:pre [:code "[42 [\"abc\" 22/7]]"]]

 [:p "Note that the outer root vector contains exactly two elements: one scalar " [:code "42"] " and one descendant collection, the nested vector " [:code "[\"abc\" 22/7]"] "."]

 [:p "Following Motto #2, we'll compose a collection specification whose shape mimics the shape of the data. We copy-paste the data, delete the scalars, and insert our predicates."]

 [:pre
  [:code "[42     [\"abc\" 22/7]] ;; copy-paste data"] [:br]
  [:code "[       [          ]] ;; delete scalars"] [:br]
  [:code "[len-3? [len-2?    ]] ;; insert predicates"]]

 [:p "(I've re-used the " [:code "len-3?"] " predicate so that in the following examples, it'll be easier to keep track of which predicate goes where when we have multiple predicates.)"]

 [:p "Let's take a look at the data's paths."]

 [:pre (print-form-then-eval "(all-paths [42 [\"abc\" 22/7]])")]

 [:p "Five elements: three scalars, which we ignore during collection validation, and two collections, the root collection and the nested vector."]

 [:p "Here are the specification's paths."]

 [:pre (print-form-then-eval "(all-paths [len-3? [len-2?]])")]

 [:p "Four total elements: two collections, the root vector at path " [:code "[]"] " and a nested vector at path " [:code "[1]"] ", and two functions, predicate " [:code "len-3?"] " in the top-level at path " [:code "[0]"] " and predicate " [:code "len-2?"] " in the lower-level at path " [:code "[1 0]"] "."]

 [:p "Next, we remove all scalar elements from the data, keeping only the elements that are collections."]

 [:pre
  [:code ";; non-scalar elements of data"]
  [:br]
  [:br]
  [:code "[{:path [], :value [42 [\"abc\" 22/7]]}\n {:path [1], :value [\"abc\" 22/7]}]"]]


 [:p "We kept two such collections: the root collection at path " [:code "[]"] " and the nested vector at path " [:code "[1]"] "."]

 [:p "Next, we remove all non-predicate elements from the specification, keeping only the predicates."]

 [:pre
  [:code ";; predicate elements of specification"]
  [:br]
  [:br]
  [:code "[{:path [0], :value len-3?}\n {:path [1 0], :value len-2?}]"]]

 [:p "There are two such collection predicates: " [:code "len-3?"] " at path " [:code "[0]"] " and " [:code "len-2?"] " at path " [:code "[1 0]"] ". Let's notice that if we apply " [:code "drop-last"] " to those paths, we get the paths of the two vectors in the data: "]

 [:ul
  [:li [:code "(drop-last [0])"] " yields " [:code "()"] ", which pairs with the data's root collection " [:code "[42 [\"abc\" 22/7]]"] " at path " [:code "[]"] "."]
  [:li [:code "(drop-last [1 0])"] " yields " [:code "(1)"] ", which pairs with the nested vector " [:code "[\"abc\" 22/7]"] " at path " [:code "[1]"] " of the data."]]

 [:p "In the previous " [:a {:href "#scalar-validation"} "section"] " when we were validating scalars, we followed the principle that validation only proceeds when a predicate in the specification shares the " [:em "exact"] " path as the scalar in the data. However, we can now see an issue if we try to apply that principle here. The nested vector of the data is located at path " [:code "[1]"] ". The nested " [:code "len-2?"] " predicate in the specification is located at path " [:code "[1 0]"] ", nearly same except for the trailing " [:code "0"] ". The root vector of the data is located at path " [:code "[]"] " while the " [:code "len-3?"] " predicate is located at path " [:code "[0]"] " of the specification, again, nearly the same except for the trailing " [:code "0"] ". Clojure has a nice core function that performs that transformation."]

 [:p "The slightly modified rule for validating collections is " [:em "Collection predicates in the specification are applied to the collection in the data that correspond to their parent."] " In other words, the predicate at path " [:code "pth"] " in the collection specification is applied to the collection at path " [:code "(drop-last pth)"] " in the data. So we pair predicate " [:code "len-3?"] " with the root collection " [:code "[42 [\"abc\" 22/7]]"] " and we pair predicate " [:code "len-2?"] " with the nested vector " [:code "[\"abc\" 22/7]"] "."]

 [:p "We can now perform the validation by hand. There are two vectors to validate, each with its own predicate."]

 [:pre
  (print-form-then-eval "(len-3? [42 [\"abc\" 22/7]])")
  [:br]
  (print-form-then-eval "(len-2? [\"abc\" 22/7])")]

 [:p  "The root vector " [:code "[42 [\"abc\" 22/7]]"] " does not satisfy the " [:code "len-3?"] " predicate it was paired with because it only contains two elements (one integer plus one nested vector). The nested vector "  [:code "[\"abc\" 22/7]"] " contains two elements, so it satisfies the " [:code "len-2?"] " collection predicate that it was paired with."]

 [:p [:code "validate-collections"] " does that entire algorithm for us with one invocation. Data on the upper row, collection specification on the lower row."]

 [:pre (print-form-then-eval "(validate-collections [42 [\"abc\" 22/7]] [len-3? [len-2?]])" 45 45)]

 [:p "One invocation performs the entire algorithm, which found two pairs of predicates+collections. Predicate " [:code "len-3?"] " at path " [:code "[0]"] " in the specification was paired with root collection at path " [:code "[]"] " in the data. The root collection contains only two elements, so " [:code "len-3?"] " returns " [:code "false"] ". Predicate " [:code "len-2?"] " at path " [:code "[1 0]"] " in the specification was paired with the nested vector at path " [:code "[1]"] " in the data. The nested vector contains two elements, so " [:code "len-2?"] " returns " [:code "true"] "."]

 [:p "To solidify our knowledge, let's do one more example with an additional nested vector and a third predicate. I'll be terse because this is just a review of the concepts from before."]

 [:p "The nested data, similar to the previous data, but with an additional vector wrapping " [:code "22/7"] "."]

 [:pre [:code "[42 [\"abc\" [22/7]]]"]]

 [:p "A new predicate testing for a length of one."]

 [:pre (print-form-then-eval "(defn len-1? [c] (= 1 (count c)))")]

 [:p "Motto #2: Shape the specification to mimic the data. Copy-paste the data, then delete the scalars."]

 [:pre [:code "[   [      [    ]]]"]]

 [:p "Insert collection predicates."]

 [:pre [:code "[len-3? [len-2? [len-1?]]]"]]

 [:p "Now that we have the data and specification in hand, we perform the collection validation algorithm."]

 [:ol
  [:li
   [:p "Run " [:code "all-paths"] " on the data."]

   [:pre (print-form-then-eval "(all-paths [42 [\"abc\" [22/7]]])")]

   [:p "Six elements: three collections, three scalars (will be ignored)."]

   [:p "Run " [:code "all-paths"] " on the specification."]

   [:pre (print-form-then-eval "(all-paths [len-3? [len-2? [len-1?]]])" 55 55)]

   [:p "Six elements: three collections, three predicates."]]

  [:li
   [:p "Remove scalar elements from the data, keeping only the collection elements."]

   [:pre [:code "[{:path [], :value [42 [\"abc\" [22/7]]]}\n {:path [1], :value [\"abc\" [22/7]]}\n {:path [1 1], :value [22/7]}]"]]

   [:p "Remove non-predicate elements from the specification."]

   [:pre [:code "[{:path [0], :value len-3?}\n {:path [1 0], :value len-2?}\n {:path [1 1 0], :value len-1?}]"]]]

  [:li
   [:p "Pair predicates at path " [:code "pth"] " in the specification with collections at path " [:code "(drop-last pth)"] " in the data."]

   [:pre
    [:code ";; paths of predicates  => paths of collections in data"]
    [:br]
    [:br]
    (print-form-then-eval "(drop-last [0])") [:br]
    (print-form-then-eval "(drop-last [1 0])") [:br]
    (print-form-then-eval "(drop-last [1 1 0])")]

   [:p [:code "()"] " is equivalent to " [:code "[]"] ", " [:code "(1)"] " is equivalent to " [:code "[1]"] ", etc. Therefore,"]

   [:ul
    [:li [:code "len-3?"] " pairs with " [:code "[42 [\"abc\" [22/7]]]"]]
    [:li [:code "len-2?"] " pairs with " [:code "[\"abc\" [22/7]]"]]
    [:li [:code "len-1?"] " pairs with " [:code "[22/2]"]]]

   [:p "All predicates pair with a collection, and all collections pair with a predicate. There are zero un-paired predicates, and zero un-paired collections."]]

  [:li "For each collection+predicate pair, apply the predicate."

   [:pre
    (print-form-then-eval "(len-3? [42 [\"abc\" [22/7]]])") [:br]
    (print-form-then-eval "(len-2? [\"abc\" [22/7]])") [:br]
    (print-form-then-eval "(len-1? [22/7])")]]

  [:p "The root collection fails to satisfy its predicate, but the two nested vectors do satisfy their respective predicates."]]

 [:p "Now, we lean on " [:code "validate-collections"] " to do all four steps of that algorithm with one invocation. Data on the upper row, specification on the lower row."]

 [:pre (print-form-then-eval "(validate-collections [42 [\"abc\" [22/7]]] [len-3? [len-2? [len-1?]]])" 50 45)]

 [:p [:code "validate-collections"] " discovered the same three collection+predicate pairs and helpfully reports their paths, alongside the results of applying each of the three predicates to their respective collections. As we saw when we ran the manual validation, the root collection failed to satisfy its " [:code "len-3?"] " predicate, but the two nested vectors did satisfy their predicates, " [:code "len-2?"] " and " [:code "len-1?"] ", respectively."]

 [:p "Next we'll tackle validating the collection properties of maps. The same principle governs: predicates apply to their parent container. Let's assume this data."]

 [:pre [:code "{:x 42}"]]

 [:p "A hash-map containing one key-value. Here are the paths of that example data."]

 [:pre
  [:code "(all-paths {:x 42})"]
  [:br]
  [:code ";; => [{:path [], :value {:x 42}}\n       {:path [:x], :value 42}]"]]

 [:p "One scalar, which " [:code "validate-collections"] " ignores, and one collection. Let's apply our rule: the predicate in the specification applies to the collection in the data whose path is one element shorter. The root collection is located at path " [:code "[]"] ". To write a collection specification, we'd mimic the shape of the data, inserting predicates that apply to the parent. We can't simply write…"]

 [:pre [:code "{map?} ;; => java.lang.RuntimeException..."]]

 [:p "…because maps must contain an even number of forms. So we're going to need to add a key in there. Let me propose this as a specification."]

 [:pre [:code "{:foo map?}"]]

 [:p [:code ":foo"] " doesn't have any particular meaning, and it won't affect the validation. Let's examine the paths of that proposed specification and apply the Mottos."]

 [:pre (print-form-then-eval "(all-paths {:foo map?})")]

 [:p "Two elements: the root collection at path " [:code "[]"] " and a predicate at path " [:code "[:foo]"] ". Since this will be the collection validation, Speculoos only considers the elements of the specification which are predicates, so non-predicate elements of the specification (i.e., the root collection) will be ignored, and only the " [:code "map?"] " predicate will participate, if it can be paired with a collection in the data."]

 [:p "Let's explore the " [:code "drop-last"] " business. There's only one element in the collection specification that's a predicate. Predicate " [:code "map?"] " is located at path " [:code "[:foo]"] "."]

 [:pre (print-form-then-eval "(drop-last [:foo])")]

 [:p "Fortunately, that evaluates to a path, " [:code "()"] ", which in the data, corresponds to a collection. Because the " [:code "(drop-last [:foo])"] " path of the predicate in the specification corresponds to the path of a collection in the data, we can form a validation pair."]

 [:pre (print-form-then-eval "(map? {:x 42})")]

 [:p "The root collection satisfies the " [:code "map?"] " predicate it is paired with."]

 [:p "Let's do that sequence automatically with " [:code "validate-collections"] ", data on the upper row, specification on the lower row."]

 [:pre (print-form-then-eval "(validate-collections {:x 42} {:foo map?})" 40 45)]

 [:p [:code "validate-collections"] " was, in fact, able to pair one predicate to one collection. Predicate " [:code "map?"] " at path " [:code "[:foo]"] " in the specification was paired with the root collection at path " [:code "[]"] ". Unlike scalar validation which pairs predicates to scalars with their " [:em "exact"] " paths, collection validation pairs are formed when the target path is equivalent to the predicate's path right-trimmed. In this example, predicate " [:code "map?"] "'s path is " [:code "[:foo]"] ". " [:code "(drop-last [:foo])"] " evaluates to " [:code "()"] ". A path " [:code "()"] " corresponds to the root collection, so the predicate " [:code "map?"] " was applied to the root collection. " [:code "{:x 42}"] " satisfies the predicate."]

 [:p "Because of the " [:code "drop-last"] " behavior, it mostly doesn't matter what key we associate our collection predicate. The key will merely get trimmed when searching for a target. In the example above, " [:code ":foo"] " was trimmed, but the key could be anything. Observe."]

 [:pre
  (print-form-then-eval "(drop-last [:foo])") [:br]
  (print-form-then-eval "(drop-last [:bar])") [:br]
  (print-form-then-eval "(drop-last [:baz])")]

 [:p [:em "Any"] " single key would get trimmed off, resulting in a path of " [:code "[]"] ", which would always point to the root collection."]

 [:p "Technically, we could key our collection predicates however we want, but I strongly recommend choosing a key that doesn't appear in the data. This next example shows why."]

 [:p "Let's explore a map nested within a map. This will be our example data."]

 [:pre [:code "{:x 42 :y {:z \"abc\"}"]]

 [:p "Let's put a collection predicate at key " [:code ":y"] " of the specification."]

 [:pre [:code "{:y map?}"]]

 [:p "Notice that " [:code ":y"] " also appears in the data."]

 [:p "Now we run " [:code "all-paths"] " on both the data…"]

 [:pre (print-form-then-eval "(all-paths {:x 42 :y {:z \"abc\"}})" 50 50)]

 [:p "…and " [:code "all-paths"] " on the specification."]

 [:pre (print-form-then-eval "(all-paths {:y map?})")]

 [:p "Discard all non-collection elements in the data…"]

 [:pre [:code "[{:path [],:value {:x 42, :y {:z \"abc\"}}}\n {:path [:y], :value {:z \"abc\"}}]"]]

 [:p "…and discard all non-predicate elements in the specification."]

 [:pre [:code "[{:path [:y], :value map?}]"]]

 [:p "This is something we see for the first time while discussing collection validation: Fewer predicates than collections. Since there is only one predicate, at least one collection will be un-paired, and ignored (Motto #3). In this example, the predicate's path is " [:code "[:y]"] ". We trim it with " [:code "drop-last"] "."]

 [:pre (print-form-then-eval "(drop-last [:y])")]

 [:p "The resulting " [:code "()"] " corresponds to path " [:code "[]"] " in the data. So we can now apply the collection predicate to the collection in the data."]

 [:pre (print-form-then-eval "(map? {:x 42 :y {:z \"abc\"}})")]

 [:p "Let's confirm that we produced the same answer as " [:code "validate-collections"] " would give us."]

 [:pre (print-form-then-eval "(validate-collections {:x 42 :y {:z \"abc\"}} {:y map?})" 45 80)]

 [:p "We see a " [:code "map?"]" predicate at key " [:code ":y"] " of the specification, and " [:code "validate-collections"] " merrily chugged along without a peep about masking the nested map " [:code "{:z \"abc\"}"] "."]

 [:p "We can see that the singular " [:code "map?"] " predicate located at specification path " [:code "[:y]"] " was indeed applied to the root container at data path " [:code "(drop-last [:y])"] " which evaluates to path " [:code "[]"] ". But now we've consumed that key, and it cannot be used to target the nested map " [:code "{:z \"abc\"}"] " at path " [:code "[:y]"] " in the data. We would not be able to validate any aspect of the nested collection " [:code "{:z \"abc\"}"] "."]

 [:p "Instead, if we had invented a wholly fictitious key, " [:code "drop-last"] " would trim that sham key off the right end of the path and the predicate would still be applied to the root container, while key " [:code ":y"] " remains available to target the nested map. " [:code ":foo/:bar/:baz"] "-style keywords are nice because humans understand that they don't carry any particular meaning. In practice, I like to invent keys that are descriptive of the predicate so the validation results are easier to scan by eye."]

 [:p "For instance, if we're validating that a collection's type is a map, we could use sham key " [:code ":is-a-map?"] ". We could also verify that the nested map is not a set by associating predicate " [:code "set?"] " to " [:code ":is-a-set?"] "."]

 [:pre (print-form-then-eval "(validate-collections {:x 42 :y {:z \"abc\"}} {:is-a-map? map? :y {:is-a-set? set?}})")]

 [:p "Notice that " [:code "validate-collections"] " completely ignored the scalars " [:code "42"] " and " [:code "\"abc\""] " at data keys " [:code ":x"] " and " [:code ":z"] ". It only applied predicate " [:code "map?"] " to the root of data and predicate " [:code "set?"] " to the nested map at key " [:code ":y"] ", which failed to satisfy. Any possible meaning suggested by keys " [:code ":is-a-map?"] " and " [:code ":is-a-set?"] " did not affect the actual validation; they are merely convenient markers that we chose to make the results easier to read."]

 [:p "Let me emphasize: when we're talking about a nested map's collection specification, the predicate's key has " [:em "absolutely no bearing on the operation of the validation"] ". The key, at the tail position of the path, gets trimmed by the " [:code "drop-last"] " operation. That's why " [:code ":foo"] " in the earlier examples doesn't need to convey any meaning. We could have made the key misleading like this."]

 [:pre
  [:code ";;             this keyword… ---v         v--- …gives the wrong impression about this predicate"]
  [:br]
  (print-form-then-eval "(validate-collections {:x 11} {:is-a-map? vector?})" 80 80)]

 [:p "Despite the " [:code ":is-a-map?"] " key suggesting that we're testing for a map, the predicate itself determines the outcome of the validation. The " [:code "vector?"] " predicate tests for a vector, and returns " [:code "false"] "."]

 [:p "It's our job to make sure we write the predicates correctly."]

 [:p "Here's something interesting."]

 [:pre (print-form-then-eval "(validate-collections [42] [vector? map?])" 40 80)]

 [:p "If we focus on the paths of the two predicates in the specification, we see that both " [:code "vector?"] " at path " [:code "[0]"] " and " [:code "map?"] " at path " [:code "[1]"] " target the root container because…"]

 [:pre (print-form-then-eval "(drop-last [0])")]

 [:p  "…and…"]

 [:pre (print-form-then-eval "(drop-last [1])")]

 [:p "…and in fact…"]

 [:pre (print-form-then-eval "(drop-last [99999])") ]

 "…all evaluate to the same equivalent path " [:code "[]"] " in the data. So we have another consideration: " [:em "Every"] " predicate in a specification's collection applies to the parent collection in the data. This means that we can apply an unlimited number of predicates to each collection."

 [:pre (print-form-then-eval "(validate-collections [42] [vector? map? list? set? coll?])" 55 80)]

 [:p "All five collection predicates were located at a single-element path, so for each of those five cases, " [:code "(drop-last [" [:em "0 through 4"] "])"]  " evaluated to " [:code "()"] ", which is the path to the data's root collection. " [:code "validate-collections"] " was therefore able to make five pairs, and we see five validation results."]

 [:p "That feature can be useful, but it raises an issue. How would we specify the collections of this data?"]

 [:pre [:code "[42 {:y \"abc\"}]"]]

 [:p "A map nested within a vector. And its paths."]

 [:pre (print-form-then-eval "(all-paths [42 {:y \"abc\"}])")]

 [:p "We may want to specify two facets of the root collection, that it's both a collection and a vector (that's redundant, I know). Furthermore, we want to specify that the data's second element is a map. That collection specification might look something like this."]

 [:pre [:code "[coll? vector? {:foo map?}]"]]

 [:p "And its paths."]

 [:pre (print-form-then-eval "(all-paths [coll? vector? {:foo map?}])")]

 [:p "Two predicates, " [:code "coll?"] " and " [:code "vector?"] ", apply to the root collection, because " [:code "(drop-last [0])"] " and " [:code "(drop-last [1])"] " both resolve the root collection's path. But somehow, we have to tell " [:code "validate-collections"] " how to target that " [:code "map?"] " predicate towards the nested map. We can see that " [:code "map?"] " is located at path " [:code "[2 :foo]"] ", and " [:code "(drop-last [2 :foo])"] " evaluates to " [:code "[2]"] ". The data's nested map " [:code "{:y \"abc\"}"] " is located at path " [:code "[1]"] ", which doesn't 'match'."]

 [:p  "If " [:strong "any"] " number of predicates apply to the parent collection, there might be zero to infinity predicates before we encounter a nested collection in that sequence. How, then, does " [:code "validate-collections"] " determine where to apply the predicate inside a nested collection?"]

 [:p "The rule " [:code "validate-collections"] " follows is " [:em "Within a sequential collection, apply nested collection predicates in the order which they appear, ignoring scalars."] " Let's see that in action. Here is the data, with the scalars removed from the root level."]

 [:pre [:code "[{:y \"abc\"}]"]]

 [:p "Here is the specification with the scalar (i.e., functions) removed from its root level."]

 [:pre [:code "[{:foo map?}]"]]

 [:p "Now we generate the paths for both of those."]

 [:pre
  [:code ";; pruned data"] [:br] [:br]
  (print-form-then-eval "(all-paths [{:y \"abc\"}])") [:br]
  [:br]
  [:br]
  [:code ";; pruned specification"] [:br] [:br]
  (print-form-then-eval "(all-paths [{:foo map?}])")]

 [:p "Next remove all non-collection elements from the data."]

 [:pre [:code "[{:path [], :value [{:y \"abc\"}]}\n {:path [0], :value {:y \"abc\"}}]"]]

 [:p "And remove all non-predicate elements of the specification."]

 [:pre [:code "[{:path [0 :foo], :value map?}]"]]

 [:p "There are two remaining collections in the data, but only one predicate. Motto #3 reminds us that at least one of the collections will be ignored. Can we make at least one collection+predicate pair? Let's perform the " [:code "drop-last"] " maneuver on the predicate's path."]

 [:pre (print-form-then-eval "(drop-last [0 :foo])")]

 [:p "Well, how about that? That resolves to " [:code "(0)"] ", which is equivalent to the path of the nested map " [:code "{:y \"abc\"}"] " in the pruned data. We can apply that predicate to the collection we paired it with."]

 [:pre (print-form-then-eval "(map? {:y \"abc\"})")]

 [:p "So the nested map is indeed a map. Let's see what " [:code "validate-collections"] " has to say."]

 [:pre (print-form-then-eval "(validate-collections [42 {:y \"abc\"}] [coll? vector? {:foo map?}])" 50 50)]

 [:p [:code "validate-collections"] " found three predicates in the specification on the lower row that it could pair with a collection in the data in the upper row. Both " [:code "coll?"] " and " [:code "vector?"] " predicates pair with the root collection because their paths, when right-trimmed with " [:code "drop-last"] " correspond to " [:code "[]"] ", which targets the root collection. Predicate " [:code "map?"] " was paired with the nested map " [:code "{:y \"abc\"}"] " in the data because " [:code "map?"] " was located in the first nested collection of the specification, and " [:code "{:y \"abc\"}"] " is the first (and only) nested collection in the data. We can see how " [:code "validate-collections"] " calculated the nested map's path because " [:code ":ordinal-path-datum"] " is " [:code "[0]"] ". The ordinal path reports the path into the 'pruned' collections, as if the the sequentials in the data and the sequentials in the specification contained zero scalars."]

 [:p "Let's do another example that really exercises this principle. First, we'll make some example data composed of a parent vector, containing a nested map, a nested list, and a nested set, with a couple of interleaved integers."]

 [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

 [:p "Let's examine the paths of that data."]

 [:pre (print-form-then-eval "(all-paths [{:a 11} 22 (list 33) 44 #{55}])" 55 47)]

 [:p "We got path elements for five scalars, and path elements for four collections: the root collection (a vector), and three nested collections (one each of map, list, and set)."]

 [:p "We're in collection validation mindset (Motto #1), so we ought to be considering the order of the nested collections. Let's eliminate the five scalars and enumerate the paths of the pruned data."]

 [:pre (print-form-then-eval "(all-paths [{}  (list) #{}])")]

 [:p "Let's make note of a few facts. The nested map, nested list, and nested set remain in the same relative order as in the full data. The root collection is, as always, at path " [:code "[]"] ". The nested collections are zero-indexed: the nested map is located at index " [:code "0"] ", the nested list is at index " [:code "1"] ", and the nested set is at index " [:code "2"] ". These indexes are what " [:code "validate-collections"] " reports as " [:code ":ordinal-path-datum"] ", the prefix " [:em "ordinal"] " indicating a position within a sequence, 'first', 'second', 'third', etc."]

 [:p "Now we need to compose a collection specification. Motto #2 reminds us to make the specification mimic the shape of the data. I'm going to copy-paste the data and mash the delete key to remove the scalar datums."]

 [:pre [:code "[{     }    (       )    #{  }]"]]

 [:p "Just to emphasize how they align, here are the data (upper row) and the collection specification (lower row) with some space for visual formatting."]
 [:pre
  [:code "[{:a 11} 22 (list 33) 44 #{55}] ;; <--- data"]
  [:br]
  [:code "[{     }    (       )    #{  }] ;; <--- collection specification"]
  [:br]
  [:code " ^--- 1st   ^--- 2nd     ^--- 3rd nested collection"]]

 [:p "The first thing to note is that our collection specification looks a lot like our data with all the scalars removed. The second thing to notice is that even though it contains zero predicates, that empty structure in the lower row is a legitimate collection specification which " [:code "validate-collections"] " can consume. Check this out."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{}()#{}])" 55 55)]

 [:p "Motto #3: Validation ignores collections in the data that are not paired with a predicate in the specification. Zero predicates, zero pairs."]

 [:p "Okay, let's add one predicate. Let's specify that the second nested collection is a list. Predicates apply to their parent container, so we'll insert " [:code "list?"] " into the list of the specification (lower row)."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} (list list?) #{}])" 55 80)]

 [:p "One predicate in the specification pairs with one collection in the data, so we receive one validation result. That nested collection is indeed a list, so " [:code ":valid?"] " is " [:code "true"] ". The " [:code "list?"] " predicate at path " [:code "[1 0]"] " in the specification was applied to the collection located at path " [:code "[2]"] " in the data."]

 [:p "Notice how " [:code "validate-collections"] " did some tedious and boring calculations to achieve the general effect of " [:em "The predicate in the second nested collection of the specification applies to the second nested collection of the data."] " It kinda skipped over that " [:code "22"] " because it ignores scalars, and we're validating collections. Basically, " [:code "validate-collections"] " performed that 'skip' by pruning the scalars from the data…"]

 [:pre [:code "[{} (list) #{}]"]]

 [:p "…and pruning all non-collections from the parent level above the predicate. In other words, " [:code "validate-collections"] " pruned from the specification any scalars with a path length exactly one element shorter than the path of the predicate."]

 [:pre [:code "[{} (list list?) #{}] ;; no pruning because zero scalars within the parent's level"]]

 [:p "Then, enumerating the paths for both pruned data and pruned specification."]

 [:pre
  [:code ";; data, all scalars pruned"]
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [{}  (list) #{}])")
  [:br]
  [:br]
  [:br]
  [:code ";; specification, parent-level pruned of non-collections"]
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [{} (list list?) #{}])")]

 [:p "There's only one predicate, " [:code "list?"] ", which is located at path " [:code "[1 0]"] " in the pruned specification. Right-trimming the predicate's path give us this."]

 [:pre (print-form-then-eval "(drop-last [1 0])")]

 [:p "That right-trimmed result is equivalent to ordinal path " [:code "[1]"] " which is the second element of the pruned data. There is indeed a collection at ordinal path " [:code "[1]"] " of the pruned data, so we have successfully formed a pair. We can apply the predicate to the thing at that path."]

 [:pre (print-form-then-eval "(list? (list 33))")]

 [:p "The list in the data indeed satisfies the " [:code "list?"] " predicate. " [:code "validate-collections"] " does all that with one invocation: pairs up predicates in the specification with nested collections in the data, and applies all predicates to their paired targets."]

 [:p "Let's see, again, how " [:code "validate-collections"] " handles this validation."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} (list list?) #{}])" 55 80)]

 [:p "We inserted only a single " [:code "list?"] " predicate into the specification, so, at most, we could receive only one collection+predicate pair. The data's nested list, " [:code "(list 33)"] " is the second nested collection within the sequential, so its ordinal path is " [:code "[1]"] ". The " [:code "list?"] " predicate is contained in the specification's second nested collection, so its ordinal path is also " [:code "[1]"] ". Since the " [:code "list?"] " predicate's container and the thing in the data share an ordinal path, " [:code "validate-collection"] " formed a collection+predicate pair. The " [:code "list?"] " predicate was satisfied because " [:code "(list 33)"] " is indeed a list."]

 [:p "Let's clear the slate and specify that nested set at the end. We start with the full data…"]

 [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

 [:p "…and prune all non-scalars from data to serve as a template for the specification…"]

 [:pre [:code "[{     }    (list   )    #{  }]"]]

 [:p "…and insert a " [:code "set?"] " predicate for the set. Collection predicates apply to their parent containers, so we'll insert it " [:em "inside"] " the set we want to validate."]

 [:pre [:code "[{} (list) #{set?}]"]]

 [:p " Usually, we wouldn't include non-predicates into the specification, but for demonstration purposes, I'm going to insert a couple of scalars, keywords " [:code ":skip-1"] " and " [:code ":skip-2"] ", that will ultimately get skipped because validation ignores non-predicates in the specification."]

 [:pre [:code "[{} :skip-1 (list) :skip-2 #{set?}]"]]

 [:p "First, we prune the non-collections from the data…"]

 [:pre [:code "[{} (list) #{}]"]]

 [:p "…then prune from the specification the non-predicates from the parent-level."]

 [:pre [:code "[{} (list) #{set?}]"]]

 [:p "That rids us of " [:code ":skip-1"] " and " [:code ":skip-2"] ", so now the nested collections in the specification align with the nested collections in the data."]

 [:p "We enumerate the paths of the pruned data…"]

 [:pre (print-form-then-eval "(all-paths [{} (list) #{}])")]

 [:p "…and enumerate the paths of the pruned specification."]

 [:pre (print-form-then-eval "(all-paths [{} (list) #{set?}])")]

 [:p "There is only one predicate, specification element " [:code "{:path [2 set?], :value set?}"] ". When we right-trim that path, which we calculated with respect to the pruned specification, we get…"]

 [:pre (print-form-then-eval "(drop-last [2 :set?])")]

 [:p "…which is equivalent to ordinal path " [:code "[2]"] " with respect to the pruned data. The element at that path in the data is indeed a collection, so we successfully paired the predicate with a collection. Validation proceeds by applying the predicate to the element."]

 [:pre (print-form-then-eval "(set? #{55})")]

 [:p "The element is indeed a set, so the predicate is satisfied."]

 [:p "Here's how we validate that nested set using " [:code "validate-collections"] ", data upper row, specification lower row."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} :skip-1 () :skip-2 #{set?}])" 55 80)]

 [:p "One predicate applied to one collection, one validation result. And again, collection validation skipped right over the intervening scalars, " [:code "22"] " and " [:code "44"] ", in the data, and over the intervening non-predicates, " [:code ":skip-1"] " and " [:code ":skip-2"] ", in the specification. " [:code "validate-collections"] " applied the " [:code "set?"] " predicate in the specification's third nested collection to the data's third nested collection " [:code "#{55}"] ", both at ordinal path " [:code "[2]"] " (i.e., the third non-scalar elements)."]

 [:p "We might as well specify and validate that nested map now. Here's our data again."]

 [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

 [:p "We remove all non-scalars to create a template for the specification."]

 [:pre [:code "[{     }    (list   )    #{  }]"]]

 [:p "Recall that collection predicates targeting a map require a sham key. We'll insert into the specification a " [:code "map?"] " predicate associated to a sham key," [:code ":is-map?"] ", that doesn't appear in the data's corresponding nested map."]

 [:pre [:code "[{:is-map? map?}    (list   )    #{  }]"]]

 [:p "And again, just to demonstrate how the skipping works, I'll insert a couple of non-predicates in front of the nested map."]

 [:pre [:code "[:skip-3 :skip-4 {:is-map? map?}    (list   )    #{  }]"]]

 [:p "Note that the data's nested map is located at path " [:code "[0]"] ", the first element, while, because of those to non-predicates, the specification's corresponding nested map is located at path " [:code "[2]"] ", the third element. In a moment, matching the ordinal paths of each (by 'pruning') will cause them to be paired."]

 [:p "Now, we prune the non-scalars from the data…"]

 [:pre [:code "[{} () #{}]"]]

 [:p "…and prune the non-predicates from the parent-level."]

 [:pre [:code "[{:is-map? map?} () #{}]"]]

 [:p "We enumerate the paths of the pruned data…"]

 [:pre (print-form-then-eval "(all-paths [{} () #{}])")]

 [:p "…and enumerate the paths of the pruned specification."]

 [:pre (print-form-then-eval "(all-paths [{:is-map? map?} () #{}])" 50 47)]

 [:p "There is only the one predicate, " [:code "map?"] ", which is located at path " [:code "[0 :is-map?]"] " in the pruned specification. We right-trim that path."]

 [:pre (print-form-then-eval "(drop-last [0 :is-map?])")]

 [:p "It turns out that there is, in fact, a collection at that ordinal path of the pruned data, so we've made a collection+predicate pairing. We apply the predicate to that collection element."]

 [:pre (print-form-then-eval "(map? {:a 11})")]

 [:p "The nested collection at ordinal path " [:code "[0]"] ", the first nested collection, in the pruned data satisfies the predicate " [:code "map?"] " located at ordinal path " [:code "[0]"] " in the pruned specification."]

 [:p [:code "validate-collections"] " does all that work for us. Upper row, data; lower row, specification."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [:skip-3 :skip-4 {:is-map? map?} () #{}])" 65 80)]

 [:p "Unlike the previous two validations, " [:code "validate-collections"] " didn't have to skip over any scalars in the data because the nested map is the first element. It did, however, have to skip over two non-predicates, " [:code ":skip-3"] " and " [:code ":skip-4"] ", in the specification. It applied the predicate in the specification's first nested collection to the data's first nested collection (both at ordinal path " [:code "[0]"] ", i.e., the first non-scalar element), which is indeed a map."]

 [:p "We've now seen how to specify and validate each of those three nested collections, so for completeness' sake, let's specify the root. Predicates apply to their container, so for clarity, we'll insert it at the beginning."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])" 55 80)]

 [:p "Technically, we could put that particular predicate anywhere in the top-level vector as long " [:code "(drop-last " [:em "path"] ")"] " evaluates to " [:code "[]"] ". All the following yield substantially the same results."]

 [:pre
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])"]
  [:br]
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} vector? () #{}])"]
  [:br]
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () vector? #{}])"]
  [:br]
  [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{} vector?])"]]

 [:p "In practice, I find it visually clearer to insert the predicates at the head of a sequential."]

 [:p "Let's do one final, all-up demonstration where we validate all four collections, the root collection containing three nested collections. Once again, here's the data."]

 [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

 [:p "We copy-paste the data and delete all scalars to create a template for the specification."]

 [:pre [:code "[{     }    (list   )    #{  }]"]]

 [:p "Now we insert the predicates. The rule is " [:em "Predicates apply to the collection that contains the predicate."] " So we insert a " [:code "set?"] " predicate into the set…"]

 [:pre [:code "[{     }    (list   )    #{set}]"]]

 [:p "…insert a " [:code "list?"] " predicate into the list…"]

 [:pre [:code "[{     }    (list list?)    #{set}]"]]

 [:p "…insert a " [:code "map?"] " predicate into the map, associated to sham key " [:code ":foo"] "…"]

 [:pre [:code "[{:foo map?}    (list list?)    #{set}]"]]

 [:p "…and insert a " [:code "vector?"] " predicate, a " [:code "sequential?"] " predicate, a " [:code "sequential?"] " predicate, and an " [:code "any?"] " predicate into the vector's top level."]

 [:pre [:code "[vector? {:foo map?} sequential? (list list?) coll? #{set} any?]"]]

 [:p "There will be two 'phases', each phase pruning a different level. The first phase validates the root collection with the top-level predicates. To start, we enumerate the paths of the data…"]

 [:pre (print-form-then-eval "(all-paths [{:a 11} 22 (list 33) 44 #{55}])" 55 47)]

 [:p "…and enumerate the paths of our specification."]

 [:pre [:code "(all-paths [vector? {:foo map?} sequential? (list list?) coll? #{set} any?])"]
  [:br]
  [:code";; => [{:path [], :value [vector? {:foo map?} sequential? (list?)  coll? #{set} any?]}
;;     {:path [0], :value vector?}
;;     {:path [1], :value {:foo map?}}
;;     {:path [1 :foo], :value map?}
;;     {:path [2], :value sequential?}
;;     {:path [3], :value (list?)}
;;     {:path [3 0], :value list?}
;;     {:path [4], :value coll?}
;;     {:path [5], :value #{set?}}
;;     {:path [5 set?], :value set?}
;;     {:path [6], :value any?]"]]

 [:p "Then, we keep only elements that a) are predicates and b) have a single-element path."]

 [:pre [:code "[{:path [0], :value vector?}
 {:path [2], :value sequential?}
 {:path [4], :value coll?}
 {:path [6], :value any?}]"]]

 [:p "In this first phase, we're focusing on predicates located at single-element paths, because " [:code "(drop-last [" [:em "i"] "])"] " will, for every " [:code "i"]", resolve to " [:code "[]"] ", which targets the root collection. We see from that last step, predicates " [:code "vector?"] ", " [:code "sequential?"] ", " [:code "coll?"] ", and " [:code "any?"] " all have single-element paths, so they will target the root collection. The conceptual linkage between a predicate's right-trimmed path and its target has the practical result that " [:em "predicates apply to their parent containers"] ". So we right-trim those paths."]

 [:pre
  (print-form-then-eval "(drop-last [0])") [:br]
  (print-form-then-eval "(drop-last [2])") [:br]
  (print-form-then-eval "(drop-last [4])") [:br]
  (print-form-then-eval "(drop-last [6])")]

 [:p "They all evaluate to " [:code "()"] ", which is equivalent to " [:code "[]"] ", the path to the root collection. So we may now apply all four predicates to the root collection."]

 [:pre
  (print-form-then-eval "(vector? [{:a 11} 22 (list 33) 44 #{55}])") [:br]
  (print-form-then-eval "(sequential? [{:a 11} 22 (list 33) 44 #{55}])") [:br]
  (print-form-then-eval "(coll? [{:a 11} 22 (list 33) 44 #{55}])") [:br]
  (print-form-then-eval "(any? [{:a 11} 22 (list 33) 44 #{55}])")]

 [:p "Now that we've applied all predicates in the top level to the root collection, the first phase is complete. The second phase involves validating the nested collections. We start the second phase with the original data…"]

 [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

 [:p "…and the original specification."]

 [:pre [:code "[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]"]]

 [:p "We remove the scalars from the data…"]

 [:pre [:code "[{     }    (       )    #{  }]"]]

 [:p "…and from the specification, we keep only the second-level predicates, i.e., the predicates contained in the nested collections."]

 [:pre [:code "[{:foo map?} (list list?) #{set}]"]]

 [:p "Next, we enumerate the paths of the pruned data…"]

 [:pre (print-form-then-eval "(all-paths [{} () #{}])")]

 [:p "…and enumerate the paths of the pruned specification."]

 [:pre (print-form-then-eval "(all-paths [{:foo map?} (list list?) #{set?}])" 55 50)]

 [:p "Next, we retain the path elements of the data's second-level collections only…"]

 [:pre [:code "[{:path [0], :value {}}\n {:path [1], :value ()}\n {:path [2], :value #{}}]"]]

 [:p "…and retain only the predicates of the pruned specification, which in this phase are only in the nested collections."]

 [:pre [:code "[{:path [0 :foo], :value map?}\n {:path [1 0], :value list?}\n {:path [2 set?], :value set?}]"]]

 [:p "Now we run the trim-right operation on the predicate paths."]

 [:pre
  (print-form-then-eval "(drop-last [0 :foo])") [:br]
  (print-form-then-eval "(drop-last [1 0])") [:br]
  (print-form-then-eval "(drop-last [2 set?])")]

 [:p "Then we try to form predicate+collection pairs. From top to bottom:"]

 [:ul
  [:li "Predicate " [:code "map?"] " at " [:code "[0 :foo]"] " pairs with the data element at path " [:pre (print-form-then-eval "(drop-last [0 :foo])")] "which resolves to the nested map " [:code "{:a 11}"] "."]
  [:li  "Predicate " [:code "list?"] " at " [:code "[1 0]"] " pairs with the data element at path " [:pre (print-form-then-eval "(drop-last [1 0])")] "which resolves to the nested list " [:code "(list 33)"] "."]
  [:li  "Predicate " [:code "set?"] " at " [:code "[2 set?]"] " pairs with the data element at path " [:pre (print-form-then-eval "(drop-last [2 set?])")] "which resolves to the nested set " [:code "#{55}"] "."]]

 [:p "We can finally apply each of those three predicates towards their respective target collections."]

 [:pre
  (print-form-then-eval "(map? {:a 11})") [:br]
  (print-form-then-eval "(list? (list? 33))") [:br]
  (print-form-then-eval "(set? #{55})")]

 [:p "Combining the two phases, we have seven total predicate+collections pairs, four in the top level, one in each of the three nested collections. All predicates were satisfied."]

 [:p "Now that we've manually done that collection validation, let's see how " [:code "validate-collections"] " compares."]

 [:pre (print-form-then-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {:foo? map?} sequential? (list list?) coll? #{set?} any?])" 95 100)]

 [:p "We inserted four predicates — " [:code "vector?"] ", "  [:code "sequential?"] ", "  [:code "coll?"] ", and "  [:code "any?"] " — directly into the specification's top level, interleaved among the nested map, list, and set. Because they're in the top level, those predicates apply to the collection that contains them, the root collection. The outer, parent vector satisfies all four predicates because it is indeed a vector, is sequential, is a collection, and it trivially satisfies " [:code "any?"] "."]

 [:p "In addition, " [:code "validate-collections"] " validated the data's three nested collections, each with the particular predicate they contained. Map " [:code "{:a 11}"] " is the first nested collection, so its " [:code "map?"] " predicate is found at ordinal path " [:code "[0]"] ". List " [:code "(list 33)"] "is the second nested collection, so its " [:code "list?"] " predicate is found at ordinal path " [:code "[1]"]  ", skipping over the intervening scalar " [:code "22"] ". Set " [:code "#{55}"] " is the third nested collection, paired with the " [:code "set?"] " predicate at ordinal path " [:code "[2]"] ", skipping over the intervening scalars " [:code "22"] " and " [:code "44"] ". All three nested collections satisfied their respective predicates."]

 [:p "Collections nested within a map do not involve that kind of skipping because they're not sequential. To demonstrate that, let's make this our example data."]

 [:pre [:code "{:a [99] :b (list 77)}"]]

 [:p "Now, we copy-paste the data, then delete the scalars."]

 [:pre [:code "{:a [  ] :b (list   )}"]]

 [:p "That becomes the template for our collection specification. Let's pretend we want to specify something about those two nested collections at keys " [:code ":a"]  " and " [:code ":b"] ". We stuff the predicates " [:em "directly inside those collections"] ". During a collection validation, predicates apply to the collection that contains them."]

 [:pre [:code "{:a [vector?] :b (list list?)}"]]

 [:p "This becomes our collection specification. For now, we've only specified one property for each of the two nested collections. We haven't stated any requirement of the root collection, the outer map." ]

 [:p "Let's validate with "  [:code "validate-collections"] "."]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?)})" 55 80)]

 [:p "Checklist time."
  [:ul
   [:li "Specification shape mimics data? " [:em "Check."]]
   [:li "Validating collections, ignoring scalars? " [:em "Check."]]
   [:li "Two paired predicates, two validations? " [:em "Check."]]]]

 [:p "There's a subtlety to pay attention to: the " [:code "vector?"] " and " [:code "list?"] " predicates are contained within a vector and list, respectively. Those two predicates apply to their " [:em "immediate"] " parent container. " [:code "validate-collections"] " needs those " [:code ":a"] " and " [:code ":b"] " keys to find that vector and that list. We only use a sham key when validating a map immediately above our heads. Let's demonstrate how a sham key works in this instance."]

 [:p "Let's re-use that specification and tack on a sham " [:code ":howdy"] " key with a " [:code "map?"] " predicate aimed at the root map."]

 [:pre [:code "{:a [vector?] :b (list list?) :howdy map?}"]]

 [:p "Now we validate with the new specification with three predicates: one predicate each for the root collection and the two nested collections."]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?) :howdy map?})" 75 90)]

 [:p "We've got the vector and list validations as before, and then, at the end, we see that " [:code "map?"] " at the sham " [:code ":howdy"] " key was applied to the root. Because the parent collection is " [:code "not"] " sequential (i.e., a map), " [:code "validate-collections"] " did not have to skip over any intervening non-collections. There is no concept of order; elements are addressed by non-sequential keys. For example, predicate " [:code "vector?"] " is located at path " [:code "[:a 0]"] " within the specification. Right-trimming that path…"]

 [:pre (print-form-then-eval "(drop-last [:a 0])")]

 [:p "…resolves to directly to the path of the collection nested at path " [:code "[:a]"] " in the data. It made no difference that predicate " [:code "map?"] " was floating around there at path " [:code "[:howdy]"] " in the parent level. Likewise, predicate " [:code "list?"] " was applied to the list nested at path " [:code "[:b 0]"] " in the data because its right-trimmed path…"]

 [:pre (print-form-then-eval "(drop-last [:b 0])")]

 [:p "…doesn't involve " [:code ":howdy"] " at any point."]

 [:p "One more example to illustrate how collection validation ignores un-paired elements. Again, here's our data."]

 [:pre [:code "{:a [99] :b (list 77)}"]]

 [:p "And again, we'll copy-paste the data, then delete the scalars. That'll be our template for our collection specification."]

 [:pre [:code "{:a [  ] :b (list   )}"]]

 [:p "Now, we'll go even further and delete the " [:code ":b"] "  key and its associated value, the nested list."]

 [:pre
  [:code "{:a [  ]             }"]
  [:br]
  [:br]
  [:code ";; without :b, impossible to validate the list associated to :b"]]

 [:p "Insert old reliable " [:code "vector?"] ". That predicate is paired with its immediate parent vector, so we need to keep the " [:code ":a"] " key."]

 [:pre [:code "{:a [vector?]        }"]]

 [:p "Finally, we'll add in a wholly different key that doesn't appear in the data, " [:code ":flamingo"]", with a " [:code "coll?"] " predicate nested in a vector associated to that new key."]

 [:pre [:code "{:a [vector?] :flamingo [coll?]}"]]

 [:p "Test yourself: How many validations will occur?"]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :flamingo [coll?]})" 60 90)]

 [:p "Answer: " [:em "one"] "."]

 [:p "In this example, there is only one predicate+collection pair. " [:code "vector?"] " applies to the vector at " [:code ":a"] ". We might have expected " [:code "coll?"] " to be applied to the root collection because " [:code ":flamingo"] " doesn't appear in the map, but notice that " [:code "coll?"] " is " [:em "contained"] " in a vector. It would only ever apply to the thing that contained it. Since the data's root doesn't contain a collection at key " [:code "flamingo"] ", the predicate is unpaired, and thus ignored."]

 [:p "If we did want to apply " [:code "coll?"] " to the root, it needs to be contained directly in the root. We'll associate " [:code "coll?"] " to key " [:code ":emu"] "."]

 [:pre (print-form-then-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :emu coll?})" 65 80)]

 [:p "Now, " [:code "coll?"] "'s immediate container is the root. Since it is now properly paired with a collection, it participates in validation."]

 [:p "We've churned through a ton of examples to reinforce the underlying mechanics of collection validation. But don't get overwhelmed by the drudgery. The vast majority of the time, we will be well-served to remember just these ideas while validating collections."]

 [:ol
  [:li "Shape the specification to mimic the data (Motto #2)."]
  [:li "Predicates apply to the collections that contain them."]
  [:li "To validate a map, associate the predicate to a key that doesn't appear in the data."]
  [:li "When collections are nested in a sequential collection, the predicates are applied to their immediate parent, in the order as if there were no intervening scalars in the ancestor."]
  [:li "Collections nested in maps are not affected by order."]]

 [:p "All the detailed mechanics we've discussed in this section have been to support those five ideas."]

 [:p "Two more additional notes."]

 [:ul
  [:li "When we worked through the collection validation algorithm by hand, we discussed it in terms of 'steps' and 'phases', etc., that have a strong imperative flavor. However, the implementation is purely functional. The 'steps' and 'phases' are merely one way to understand the consequences of the way Speculoos handles pairing predicates and their targets."]

  [:li "Our examples showed validating collections nested at most one level deep, e.g., a map nested in a vector. However, the algorithm is fully general. We can validate any element of any arbitrary depth, of any mixture of Clojure collection types. Just to show off:"]

  [:pre (print-form-then-eval "(validate-collections [99 88 77 {:x (list 66 55 {:y [44 33 22 11 #{42}]})}] [{:x (list {:y [#{set?}]})}])")]

  [:p "From the outset, I intended Speculoos to be capable of validating any " [:a {:href "#HANDS"} "heterogeneous, arbitrarily nested data structure"] "."]]

 [:section#when-collection-validate
  [:h3 "When to validate collections versus validating scalars"]

  [:p "So " [:em "when"] " do we use collection validation instead of scalar validation? Basically, any time we want to verify a property that's beyond a single scalar."]

  [:ul
   [:li
    [:p [:strong "Validate a property of the collection itself."] " In this section, we've often been validating the type of the collection."]

    [:pre
     (print-form-then-eval "(vector? [])")
     [:br]
     (print-form-then-eval "(vector? {})")]

    [:p "The type predicates are short, mnemonic, and built-in, but knowing the mere type of a collection perhaps isn't broadly useful. But maybe we'd like to know how many items a vector contains…"]

    [:pre (print-form-then-eval "(>= 3 (count [1 2 3]))")]

    [:p "…or if it contains an even number of elements…"]

    [:pre (print-form-then-eval "(even? (count [1 2 3]))")]

    [:p "…or if a map contains a particular key…"]

    [:pre (print-form-then-eval "(contains? {:x 42} :y)")]

    [:p "…or maybe if a set contains anything at all."]

    [:pre (print-form-then-eval "(empty? #{})")]

    [:p "None of those tests are available without access to the whole collection."]]

   [:li
    [:p [:strong "Validate a relationship between multiple scalars."] " Here's where the lines get blurry. If we'd like to know the whether the second element of a vector is greater than the first…"]

    [:pre (print-form-then-eval "(< (get [42 43] 0) (get [42 43] 1))" 20 20)]

    [:p "…or whether each successive value is double the previous value…"]

    [:pre
     (print-form-then-eval "(def doubles [2 4 8 16 32 64 128 256 512])")

     [:br]
     [:br]

     (print-form-then-eval "(every? #(= 2 %) (map #(/ %2 %1) doubles (next doubles)))")]

    [:p "It certainly looks at first glance that we're only interested in the values of the scalars. Where does the concept of a collection come into play? When validating the relationships " [:em "between"] " scalars, I imagine a double-ended arrow connecting the two scalars with a question floating above the arrow."]

    [:pre
     [:code ";;    greater-than?"]
     [:br]
     [:code "[42 <---------------> 43]"]]

    [:p "Validating a relationship is validating the concept that arrow represents. The relationship arrow is not a fundamental property of a single, underlying scalar, so a scalar validation won't work. The relationship arrow 'lives' in the collection, so validating the relationship arrow requires a collection validation."]]]]

 [:section#collection-predicate-paths
  [:h3 "Why the collection validation algorithm is different from the scalar validation algorithm"]

  [:p "The algorithm implemented by " [:code "validate-collections"] " is " [:em "slightly"] " different from " [:code "validate-scalars"] ". It has to do with the fact that a scalar in the data can occupy the exact same path as a predicate in the specification. A function, after all, is also a scalar. To be fully general (i.e., handle any pattern and depth of nesting), a collection in the data can not share a path with a predicate in the specification."]

  [:p "To begin, we'll intentionally take a wrong turn to show why the collection validation algorithm is a little bit different from the scalar validation algorithm. As before, we want to specify that our data vector is exactly " [:code "n"] " elements long. Recall these predicates."]

  [:pre
   (print-form-then-eval "(defn len-3? [c] (= 3 (count c)))") [:br]
   (print-form-then-eval "(defn len-2? [c] (= 2 (count c)))") [:br]
   (print-form-then-eval "(defn len-1? [c] (= 1 (count c)))")]

  [:p "We're interested in validating the root collection, at path " [:code "[]"] " in the data, so at first, we'll naively try to put our " [:code "len-3?"] " predicate at path " [:code "[]"] " in the specification."]

  [:p "We could then invoke some imaginary collection validation function that treats bare, free-floating predicates as being located at path " [:code "[]"] "."]

  [:pre
   [:code ";; this fn doesn't actually exist"]
   [:br]
   [:br]
   [:code "(imaginary-validate-collection [42 \"abc\" 22/7]\n                               len-3?)\n;; => true"]]

  [:p "Okay, that scenario maybe kinda sorta could work. By policy, " [:code "imaginary-validate-collection"] " could consider a bare predicate as being located at path " [:code "[]"] " in the specification, and therefore would apply to the root collection at path " [:code "[]"] " in the data."]

  [:p "But consider this scenario: " [:em "A two-element vector nested within a two-element vector"] ". One example of that data looks like this."]

  [:pre [:code "[42 [\"abc\" 22/7]]"]]

  [:p "Let's take a look at the paths."]

  [:pre (print-form-then-eval "(all-paths [42 [\"abc\" 22/7]])")]

  [:p "We're validating collections, so we're only interested in the root collection at path " [:code "[]"] " and the nested vector at path " [:code "[1]"] "."]

  [:pre [:code "[{:path [], :value [42 [\"abc\" 22/7]]}\n {:path [1], :value [\"abc\" 22/7]}]"]]

  [:p "And now we run into an problem: How do we compose a specification with two predicates, one at " [:code "[]"] " and one at " [:code "[1]"] "? The predicate aimed at the root collection has already absorbed, by policy, the root path, so there's nowhere to 'put' the second predicate."]

  [:pre
   [:code ";; this fn doesn't actually exist"]
   [:br]
   [:br]
   [:code "(imaginary-validate-collection [42 [\"abc\" 22/7]]\n                               len-3?\n                               len-2?)\n;; => true"]]

  [:p "Because the " [:code "len-3?"] " predicate absorbs the " [:code "[]"] " path to root, and because predicates are not themselves collections and cannot 'contain' something else, the second predicate, " [:code "len-2?"] ", needs to also be free-floating at the tail of the argument list. Our " [:code "imaginary-validate-collections"] " would have to somehow figure out that predicate " [:code "len-3?"] " ought to be paired with the root collection, " [:code "[42 [\"abc\" 22/7]"] " and predicate " [:code "len-2?"] " ought to be paired with the nested vector " [:code "[\"abc\" 22/7]"] "."]

  [:p "It gets even worse if we have another level of nesting. How about three vectors, each nested within another?"]

  [:pre [:code "[42 [ \"abc\" [22/7]]]"]]

  [:p "The paths for that."]

  [:pre (print-form-then-eval "(all-paths [42 [\"abc\" [22/7]]])")]

  [:p "Regarding only the data's collections, we see three elements to validate, at paths " [:code "[]"] ", " [:code "[1]"] ", and " [:code "[1 1]"] ". "]

  [:pre [:code "[{:path [], :value [42 [\"abc\" [22/7]]]}\n {:path [1], :value [\"abc\" [22/7]]}\n {:path [1 1], :value [22/7]}]"]]

  [:p "Invoking the imaginary collection validator would have to look something like this."]

  [:pre
   [:code ";; this fn doesn't actually exist"]
   [:br]
   [:br]
   [:code "(imaginary-validate-collection [42 [\"abc\" [22/7]]]\n                               len-3?\n                               len-2?\n                               len-1?)\n;; => true"]]

  [:p "Three free-floating predicates, with no indication of where they ought to be applied. The imaginary validator would truly need to read our minds to know which predicate pairs with which nested collection, if any."]

  [:p "Someone might propose that we include some paths immediately following each predicate to inform the imaginary validator where to apply those predicates."]

  [:pre
   [:code ";; this fn doesn't actually exist"]
   [:br]
   [:br]
   [:code "(imaginary-validate-collection-2 [42 [\"abc\" [22/7]]]\n                                 len-3? [0]\n                                 len-2? [1 0]\n                                 len-1? [1 1 0])\n;; => true"]]

  [:p "That certainly works, but at that point, we've manually serialized a nested data structure. I wouldn't want to have to write out the explicit paths of more than a few predicates. Furthermore, writing separate, explicit paths could be error-prone, and not terribly re-usable, nor compact. One of Speculoos' goals is to make composing specifications intuitive. I find writing specifications with data structure literals expressive and straightforward to manipulate."]

  [:p "Here's that same specification, written as a literal data structure."]

  [:pre [:code "[len-3? [len-2? [len-1?]]"]]

  [:p "Visually, that specification looks a lot like the data. If we know the rule about predicates applying to their immediate parent containers during collection validation, that specification carries meaning. And, we can slice and dice it any way we'd like with " [:code "assoc-in"] ", or any other standard tool."]

  [:p "Here is the collection validation, Speculoos-style, with data in the upper row, specification literal in the lower row."]

  [:pre (print-form-then-eval "(validate-collections [42 [\"abc\" [22/7]]] [len-3? [len-2? [len-1?]]])" 50 50)]

  [:p  "Speculoos' " [:a {:href "#mottos"} "Motto #2"] " is " [:em "Shape the specification to mimic the data"] ". The arrangement of our collection predicates inside a structure literal will instruct " [:code "validate-collections"] " where to apply the predicates. The advantage Speculoos offers is the fact that literals are easy for humans to inspect, understand, and manipulate."]
  ]

 #_[:div
    [:p [:strong "Kinda-strong opinion, loosely held, poorly worded."] " Someone might argue that separately validating scalars and collections is a bunch of ceremony and extra work for not much benefit. Consider these two validation patterns."]

    [:ul
     [:li [:em "Does this thing exist in the third slot of a vector, and is it an integer?"] " This question could be re-phrased as " [:em "Does this vector have at least three elements? If so, is the third element an integer?"] " When validation involves determining the existence of a scalar, it must know " [:em "where"] " the scalar exists. Even though the literal predicate doesn't reflect this, virtually, the validation tests something like this."

      [:pre [:code "(fn [v] (and (<= 3 (count v))\n             (int? (get v 2))))"]]

      [:p "Now we can see the problem. That " [:code "and"] " is combining two questions: one question concerning the count of elements contained by the collection, one question concerning the type of the scalar."]]

     [:li [:p [:em "If this thing exists in the third slot of a vector, is it an integer?"] " This is essentially the question answered by a Speculoos 'scalar validation'. It is focused and correctly isiolates the task of verifying some property of a datum. Speculoos scalar validation extracts scalars and predicates from heterogeneous, arbitrarily-nested Clojure data structures, and thus it already knows the " [:em "where"] ". If " [:code "validate-scalars"] " can " [:code "get-in"] " the scalar, then it knows the scalar exists, and scalar validation doesn't involve existence. Scalar validation thus is concerned only with the properties of the scalar itself, not its container."]]]

    [:p "I contend that the latter option is tecnhically correct, and therefore has benefits of clarity, precision, and flexiblity, only at the cost of a little bit of extra keyboarding to write separate scalar specifications and collection specifications. Speculoos is an experiment to test that contention. " [:a {:href "https://github.com/blosavio"} "Convince"] " me otherwise."]]

 ]