[:section#sets
 [:h2 "Sets"]

 [:p "Sets are…a handful. They perform certain tasks with elegance that ought not be dismissed, but using sets present some unique challenges compared to the other Clojure collections. " [:em "The elements in a set are addressed by their identities."] " What does that even mean? Let's compare to Clojure's other collections to get some context."]

 [:p"The elements of a sequence are addressed by monotonically increasing integer indexes. Give a vector index " [:code "2"] " and it'll give us back the third element, if it exists."]

 [:pre (print-form-then-eval "([11 22 33] 2)")]

 [:p "The elements of a map are addressed by its keys. Give a map a key " [:code ":howdy"] " and it'll give us back the value at that key, if it exists."]

 [:pre (print-form-then-eval "({:howdy \"bonjour\" :hey \"salut\"} :howdy)")]

 [:p "Give a set some value, and it will give us back that value…"]

 [:pre (print-form-then-eval "(#{:thumb :index :middle :ring :pinky} :thumb)")]

 [:p "…but only if that element exists in the set."]

 [:pre (print-form-then-eval "(#{:thumb :index :middle :ring :pinky} :bird)")]

 [:p "So the " [:a {:href "#path"} "paths"] " to elements of vectors, lists, and maps are composed of indexes or keys. The paths to members of a set are the thing themselves. Let's take a look at a couple of examples."]

 [:p "We use " [:code "all-paths"] " to " [:a {:href "#mechanics"} " enumerate the paths"] " of elements contained in a Clojure data collection."]

 [:pre (print-form-then-eval "(all-paths #{:foo 42 \"abc\"})")]

 [:p "In this first example, the root element, a set, has a path " [:code "[]"] ". The remaining three elements, direct descendants of the root set have paths that consist of themselves. We find " [:code "42"] " at path " [:code "[42]"] " and so on."]

 [:p "The second example applies the principle further. This set contains one integer and one set-nested-in-a-vector-nested-in-a-map."]

 [:pre (print-form-then-eval "(all-paths #{11 {:a [22 #{33}]}})" 45 55)]

 [:p "As an exercise, we'll walk through how we'd navigate to that " [:code "33"] ". Let's borrow a function from the " [:a {:href "https://github.com/blosavio/fn-in"} "fn-in project"] " to zoom in on what's going on. The first argument (upper row) is our example set. The second argument (lower row) is a path. We'll build up the path to " [:code "33"] " piece by piece."]

 [:p "To start, we'll get the root."]

 [:pre
  (print-form-then-eval "(require '[fn-in.core :refer [get-in*]])")
  [:br]
  [:br]
  [:br]
  [:code "(get-in* #{11 {:a [22 #{33}]}}\n         [])"]
  [:br]
  [:code ";; => #{{:a [22 #{33}]} 11} "]]

 [:p "As with any collection type, the root element has a path " [:code "[]"] ". Supplying " [:code "get-in*"] " with a path " [:code "[]"] " retrieves the entire collection."]

 [:p  "There are two direct descendants of the root set: scalar " [:code "11"] " and a map. We've already seen that the integer's path is the value of the integer."]

 [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [11])" 45 45)]

 [:p "The path to the map is the map itself, which appears as the first element of its path. Combining the root path " [:code "[]"] " with the value of the map " [:code "{:a [22 #{33}]}"] " results in this path to the map."]

 [:pre [:code "[{:a [22 #{33}]}]]"]]

 [:p "Since we're often dealing with maps and sequentials, indexed by keywords and integers, that path may look unusual. But Speculoos handles goofy paths without skipping a beat."]

 [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]}])" 45 45)]

 [:p "When supplied with that path, " [:code "get-in*"] " extracts the map contained in the set."]

 [:p  "The map has one " [:code "MapEntry"] ", key " [:code ":a"] ", with an associated value, a two-element vector " [:code "[22 #{33}]"] ". A map value is addressed by its key, so the vector's path contains that key. Its path is that of its parent, with its " [:code ":a"] " key appended."]

 [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a])" 45 45)]

 [:p "Paths into a vector are old hat by now. Our " [:code "33"] " is contained in a set, located at the second position, index " [:code "1"] " in zero-based land, which we append to the accumulating path."]

 [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a 1])" 45 45)]

 [:p "We've now arrived at the little nested set which holds our " [:code "33"] ". Items in a set are addressed by their identity, and the identity of " [:code "33"] " is " [:code "33"] ". So we append that to the path so far."]

 [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a 1 33])" 45 45)]

 [:p "And now we've finally fished out our " [:code "33"] ". Following this algorithm, we can get, change, and delete any element of any heterogeneous, arbitrarily-nested data structure, and that includes sets at any level of nesting. We could even make a path to a set, nested within a set, nested within a set."]

 [:p "When using Speculoos, we encounter sets in three scenarios. We'll briefly sketch the three scenarios, then later go into the details."]

 [:ol
  [:li [:em "Scalar validation, scalar in data, set in specification."]

   [:p "In this scenario, we're validating scalars, so we're using a function with " [:code "scalar"] " in its name."]

   [:pre [:code (prettyfy "(validate-scalars [42 :red] [int? #{:red :green :blue}])" 55 45)]]

   [:p "In the example above, we're testing a property of a scalar, keyword " [:code ":red"] ", the second element of the data (first argument, upper row). The set " [:code "#{:red :green :blue}"] " in the specification (lower row) is a predicate-like thing that tests membership."]]

  [:li [:em "Scalar validation, set in data, set in specification."]

   [:p "In this scenario, we're validating scalars, so we're using a scalar validation function, again " [:code "validate-scalars"] "."]

   [:pre [:code (prettyfy "(validate-scalars [42 #{:chocolate :vanilla :strawberry}] [int? #{keyword?}])" 60 45)]]

   [:p "This time, we're validating scalars " [:em "contained within a set"] " in the data (upper row), with scalar predicates contained within a set in the specification (lower row)."]]

  [:li [:em "Collection validation, set in data, set in specification."]

   [:p "In this scenario, we're validating a property of a collection, so we're using " [:code "validate-collections"] "."]

   [:pre [:code (prettyfy "(validate-collections [42 #{:puppy :kitten :goldfish}] [vector? #{set?}])" 55 45)]]

   [:p "Collection predicates — targeting the nested set in the data (upper row) — are themselves contained in a set nested in the collection specification (lower row)."]]]

 [:h3 "1. Set as Scalar Predicate"]

 [:p "Let's remember back to the beginning of this section where we saw that Clojure sets can serve as membership tests. Speculoos can therefore use sets as a nice shorthand for a membership predicate."]

 [:pre
  (print-form-then-eval "(def color? #{:red :green :blue})")
  [:br]
  [:br]
  (print-form-then-eval "(ifn? color?)")
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(color? :red)")
  [:br]
  [:br]
  (print-form-then-eval "(color? :plaid)")]

 [:p [:code "color?"] " implements " [:code "IFn"] " and thus behaves like a predicate when invoked as a function. " [:code ":red"] " satisfies our " [:code "color?"]  " predicate and returns a truthy value, " [:code ":red"] ", whereas " [:code ":plaid"] " does not and returns a falsey value, " [:code "nil"] "."]

 [:p "During scalar validation, when a scalar in our data shares a path with a set in the specification, Speculoos enters " [:em "set-as-a-predicate"] " mode. I say 'mode' only in the casual sense. The implementation uses no modes nor state. The algorithm merely branches to treat the set differently depending on the scenario."]

 [:p "We'll make our specification mimic the shape of our data (Motto #2), but instead of two predicate functions pairing with two scalars (Motto #3), we'll insert one scalar predicate function, followed by a set, which behaves like a membership predicate."]

 [:pre
  [:code ";; data"]
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [42 :red])")
  [:br]
  [:br]
  [:br]
  [:code ";; scalar specification"]
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [int? #{:red :green :blue}])" 45 55)]

 [:p "Our example data contains two scalar datums: " [:code "42"] " in the first spot and "  [:code ":red"] "  in the second. Each of those datums shares a path with a predicate-like thing in the scalar specification. The " [:code "42"] " is paired with the " [:code "int?"] " scalar predicate because they both share the path " [:code "[0]"] ". Both " [:code ":red"] " and " [:code "#{:red :green :blue}"] " share a path " [:code "[1]"] ", so Speculoos regards it as a " [:em "set-as-a-scalar-predicate"] "."]

 [:p "Let's run that validation now. The data vector is the first argument in the upper row, the specification vector is the second argument in the lower row."]

 [:pre (print-form-then-eval "(validate-scalars [42 :red] [int? #{:red :green :blue}])" 55 45)]

 [:p "When Speculoos validates scalars, it treats the set in the specification as a predicate because the corresponding element in the data is a scalar, not a set. In this example, "[:code ":red"] " is a member of the " [:code "#{:red :green :blue}"] " set-predicate."]

 [:p "The same principles hold when validating elements of a map containing a set-predicate. When a set in the specification contains a set that shares a path with a scalar in the data, that set is treated as a membership predicate."]

 [:pre (print-form-then-eval " (validate-scalars {:x 42 :y :red} {:x int? :y #{:red :green :blue}})" 55 45)]

 [:p "Scalar "[:code "42"] " pairs with predicate " [:code "int?"] " at path " [:code "[:x]"] " and scalar " [:code ":red"] " pairs with set-predicate " [:code "#{:red :green :blue}"] " at path " [:code "[:y]"] ". Speculoos validates scalars in the data that share paths with predicates in the specification. Since " [:code "#{:red :green :blue}"] " is considered a predicate, scalar " [:code ":red"] " is validated."]

 [:h3 "2. Validate Scalars within Set"]

 [:p "Sometimes the scalars in our data are contained in a set. Speculoos can validate scalars within a set during a scalar validation operation. Validating a set's scalar members follows all the same principles as validating a vector's scalar members, except for one wrinkle: Since elements of a set have no inherent location, i.e., sets are unordered, sets in our data are validated against " [:em "all"] " predicates contained in the corresponding set at the same path in the specification. An example shows this better than words."]

 [:p "Let's enumerate the paths of some example data, with some scalars contained in a nested set, and then enumerate the paths of a specification, shaped like that data, with one predicate contained in a nested set."]

 [:pre
  [:code ";; data, some scalars are contained within a set"]
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [42 #{:chocolate :vanilla :strawberry}])" 55 65)
  [:br]
  [:br]
  [:br]
  [:code ";; scalar specification, one predicate contained within a set"]
  [:br]
  [:br]
  (print-form-then-eval "(all-paths [int? #{keyword?}])")]

 [:p "Let's apply the Mottos. We intend to validate scalars, so we'll use " [:code "validate-scalars"] ", which only applies predicates to scalars. Next, we'll make our our specification mimic the shape of the data. In this example, both the data and the specification are a vector, with something in the first spot, and a set in the second spot. Finally, we'll make sure that all predicates are paired with a scalar."]

 [:p "Now we validate the scalars."]

 [:pre (print-form-then-eval "(validate-scalars [42 #{:glass :rubber :paper}] [int? #{keyword?}])" 55 45)]

 [:p "First, notice how the scalar specification (lower row) looks a lot like the data (upper row). Because the shapes are similar, " [:code "validate-scalars"] " is able to systematically apply predicates from the specification to scalars in the data. Speculoos validates " [:code "42"] " against predicate " [:code "int?"] " because they share a path, " [:code "[0]"] ", in their respective vectors. Path " [:code "[1]"] " navigates to a set in both our data vector and our specification vector, so Speculoos enters " [:em "validate-scalars-within-a-set-mode"] "."]

 [:p "Every predicate contained in the specification set is applied to every datum in the data's set. In this example, " [:code "keyword?"] " is individually applied to " [:code ":glass"] ", " [:code ":rubber"] ", and " [:code ":paper"] ", and since each satisfies the predicate, the validation returns " [:code "true"] "."]

 [:p "One of the defining features of Clojure sets is that they're amorphous bags of items, without any inherent ordering. Within the context of a set, it doesn't make sense to target one scalar predicate towards one particular scalar datum. Therefore, Speculoos validates scalars contained within a set more broadly. If our specification set contains more than one predicate, each of the predicates is applied to " [:em "all"] " the scalars in the data's set."]

 [:p "In the next example, the specification set contains two predicates, " [:code "keyword?"] " an " [:code "qualified-keyword?"] "."]

 [:pre (print-form-then-eval "(validate-scalars #{:chocolate} #{keyword? qualified-keyword?})" 55 55)]

 [:p "Two scalar predicates in the specification applied to the one scalar datum. "[:code ":chocolate"] " is a keyword, but not a qualified keyword."]

 [:p "Next, we'll see how to validate multiple scalars with multiple scalar predicates. The set in the data contains three scalars. The set in the specification contains two predicates, same as the previous example."]

 [:pre (print-form-then-eval "(validate-scalars #{:chocolate :vanilla :strawberry} #{keyword? qualified-keyword?})" 55 55)]

 [:p "Validation applies " [:code "keyword?"] " and " [:code "simple-keyword?"] ", in turn, to every scalar member of the data set. Speculoos tells us that all the scalars in the data are indeed keywords, but at least one of the data's scalars is not a qualified keyword. Notice how Speculoos condenses the validation results. Instead of a validation entry for each individual scalar in the data set, Speculoos combines all the results for all the scalars, associated to key " [:code ":datums-set"] ". Two scalar predicates, two validation results."]

 [:p "Again, the same principles apply for validating sets contained in a map."]

 [:pre (print-form-then-eval "(validate-scalars {:x 42 :y #{\"a\" \"b\" \"c\"}} {:x int? :y #{string?}})" 65 45)]

 [:p [:code "int?"] " at " [:code ":x"] " applies to " [:code "42"] " also at " [:code ":x"] ". Then, " [:code "string?"] " at " [:code ":y"] " is applied to scalars " [:code "\"a\""] ", " [:code "\"b\""] ", and " [:code "\"c\""] " at " [:code ":y"] "."]


 [:p "Speculoos performs the two modes in separate passes, so we may even use both " [:em "set-as-a-predicate-mode"] " and " [:em "validate-scalars-within-a-set-mode"] " during the same validation, as long as the predicates stay on their own side of the fence."]

 [:pre (print-form-then-eval "(validate-scalars [42 #{:foo :bar :baz}] [#{40 41 42} #{keyword?}])" 45 55)]

 [:p "In this example, the predicate " [:code "#{40 41 42}"] " at index " [:code "0"] " of the specification is a set while the datum at same index of the data is " [:code "42"] ", a scalar. Speculoos uses the set-as-a-predicate mode. Since " [:code "42"] " is a member of " [:code "#{40 41 42}"] ", that datum validates as truthy. Because the data at index " [:code "1"] " is itself a set, Speculoos performs set-scalar-validation. The " [:code "keyword?"] " predicate is applied to each element of " [:code "#{:foo :bar :baz}"] " at index " [:code "1"] " and they all validate " [:code "true"] "."]

 [:h3 "3. Validate Set as a Collection"]

 [:p "Let's discuss how collection validation works when a set is involved. During a collection validation operation, Speculoos will ignore all scalars in the data. It will only apply predicates to collections. The rules are identical to how the other collections are validated: predicates from the specification are applied to the corresponding parent container in the data. But let's not get bogged down in a textual description; let's look at some examples."]

 [:p "First, we'll start with some data that consists of a vector containing an integer, followed by a three element set. Let's generate all the paths."]

 [:pre (print-form-then-eval "(all-paths [42 #{:puppy :kitten :goldfish}])" 55 65)]

 [:p "Motto #1: Collection validation ignores scalars, so out of all those elements, validation will only consider the root at path " [:code "[]"] " and the nested set at path " [:code "[1]"] "."]

 [:p "A good strategy for creating a collection specification is to copy-paste the data and delete all the scalars…"]

 [:pre [:code "[        #{    }]"]]

 [:p "…and insert some collection predicates near the opening bracket."]

 [:pre [:code "[vector? #{set?}]"]]

 [:p "Let's generate the paths for that collection specification."]

 [:pre (print-form-then-eval "(all-paths [vector? #{set?}])")]

 [:p "Notice the paths to the two predicates. Predicate " [:code "vector"] " is located at path " [:code "[0]"] ", while predicate " [:code "set?"] " is located at path " [:code "[1 set?]"] ". When validating collections, Speculoos only considers predicates within a specification."]

 [:p "Now, let's run a collection validation."]

 [:pre (print-form-then-eval "(validate-collections [42 #{:puppy :kitten :goldfish}] [vector? #{set?}])" 55 45)]

 [:p [:code "validate-collections"] " was able to pair two collections in the data with two predicates in the specification, and we received two validation results. Collection predicate " [:code "vector?"] " at path " [:code "[0]"] " in the specification was applied to whatever is at path " [:code "(drop-last [0])"] " in the data, which happens to be the root collection. Collection predicate " [:code "set?"] " at path " [:code "[1 set?]"] " in the specification was applied to path " [:code "(drop-last [1 set?])"] " in the data, which happens to be our nested set containing pet keywords. Both predicates were satisfied."]

  [:p "Remember: Scalar predicates apply to the scalar at their exact location. Collection predicates apply to the collection directly above their head."]
 ]