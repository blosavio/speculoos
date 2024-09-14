(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def set-index 10)


[:body
 (panel
  (screencast-title set-index "Sets")

  (prettyfy-form-prettyfy-eval "([11 22 33] 2)")

  (prettyfy-form-prettyfy-eval "({:howdy \"bonjour\" :hey \"salut\"} :howdy)")

  (prettyfy-form-prettyfy-eval "(#{:thumb :index :middle :ring :pinky} :thumb)")

  (prettyfy-form-prettyfy-eval "(#{:thumb :index :middle :ring :pinky} :bird)")

  [:div.note "Sets are…a handful. They enable some nice features, but they present some unique challenges compared to the other Clojure collections. " [:em "The elements in a set are addressed by their identities."] " What does that even mean? Let's compare to Clojure's other collections to get some context."

   [:p"The elements of a sequence are addressed by monotonically increasing integer indexes. Give a vector index " [:code "2"] " and it'll give you back the third element, if it exists."]

   [:p "The elements of a map are addressed by its keys. Give a map a key " [:code ":howdy"] " and it'll give you back the value at that key, if it exists."]

   [:p "Give a set some value, and it will give you back that value…"]

   [:p "…but only if that element exists in the set."]])


 (panel
  [:h3 "All-paths to members of a set"]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths #{:foo 42 \"abc\"})")]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths #{11 {:a [22 #{33}]}})" 45 55)]]

  [:div.note "So the " [:a {:href "#path"} "paths"] " to elements of vectors, lists, and maps are composed of indexes or keys. The paths to members of a set are the thing themselves. Let's take a look at a couple of examples."

   [:p "In the first example, the root element, a set, has a path " [:code "[]"] ". The remaining three elements, direct descendants of the root set have paths that consist of themselves. We find " [:code "42"] " at path " [:code "[42]"] " and so on. The second example applies the principle further."]

   [:p "How would we navigate to that " [:code "33"] "? Again the root element set has a path " [:code "[]"] ". There are two direct descendants of the root set: " [:code "11"] " and a map. We've already seen that the integer's path is the value of the integer. The path to the map is the map itself, which appears as the first element of its path. That path may look unusual, but Speculoos handles it without skipping a beat."]])


 (panel
  [:h3 "Navigating to elements nested within a set"]

  (prettyfy-form-prettyfy-eval "(require '[fn-in.core :refer [get-in*]])")

  (prettyfy-form-prettyfy-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]}])" 45 45)

  (prettyfy-form-prettyfy-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a])" 45 45)

  (prettyfy-form-prettyfy-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a 1])" 45 45)

  (prettyfy-form-prettyfy-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a 1 33])" 45 45)

  [:div.note "Let's borrow a function from the " [:a {:href "https://github.com/blosavio/fn-in"} "fn-in project"] " to zoom in on what's going on. The first argument is our example set. The second argument is a path. We'll build up the path to " [:code "33"] " piece by piece."

   [:p  "The map has one " [:code "MapEntry"] ", key " [:code ":a"] ", with an associated value, a two-element vector " [:code "[22 #{33}]"] ". A map value is addressed by its key, so the vector's path contains that key. Its path is that of its parent, with its key appended."]

   [:p "Paths into a vector are old hat by now. Our " [:code "33"] " is in a set at the second position, index " [:code "1"] " in zero-based land, which we append to the path."]

   [:p "We've now arrived at the little nested set which holds our " [:code "33"] ". Items in a set are addressed by their identity, and the identity of " [:code "33"] " is " [:code "33"] ". So we append that to the path so far."]

   [:p "And now we've finally fished out our " [:code "33"] ". Following this algorithm, we can get, change, and delete any element of any heterogeneous, arbitrarily-nested data structure, and that includes sets at any level of nesting. We could even make a path to a set, nested within a set, nested within a set."]])


 (panel
  [:h3 "Three ways Speculoos uses sets"]

  [:pre [:code (speculoos-hiccup/prettyfy "(validate-scalars [42 :red] [int? #{:red :green :blue}])" 55 45)]]

  [:pre [:code (speculoos-hiccup/prettyfy "(validate-scalars [42 #{:chocolate :vanilla :strawberry}] [int? #{keyword?}])" 60 45)]]

  [:pre [:code (speculoos-hiccup/prettyfy "(validate-collections [42 #{:puppy :kitten :goldfish}] [vector? #{set?}])" 55 45)]]

  [:div.note "When using Speculoos, we encounter sets in three scenarios. We'll briefly sketch the three scenarios, then later go into the details."
   [:ol
    [:li [:em "Scalar validation, scalar in data, set in specification."]

     [:p "In this scenario, we're validating scalars, so we're using a function with " [:code "scalar"] " in its name. We'll be testing properties of a scalar, in this example, the second element of a vector the keyword " [:code ":red"] ". The set in the specification is a predicate-like thing that tests membership."]]

    [:li [:em "Scalar validation, set in data, set in specification."]

     [:p "In this scenario, we're validating scalars, so we're using a scalar validation function, again " [:code "validate-scalars"] ". But this time, we're validating scalars " [:em "contained within a set"] " in the data, with scalar predicates contained within a set in the specification."]
     ]

    [:li [:em "Collection validation, set in data, set in specification."]

     [:p "In this scenario, we're validating some property of a collection, so we're using " [:code "validate-collections"] ". Collection predicates — targeting the nested set in the data — are themselves contained in a set nested in the collection specification."]]]])


 (panel
  [:h3 "Set as Scalar Predicate"]

  (prettyfy-form-prettyfy-eval "(def color? #{:red :green :blue})")

  (prettyfy-form-prettyfy-eval "(ifn? color?)")

  (prettyfy-form-prettyfy-eval "(color? :red)")

  (prettyfy-form-prettyfy-eval "(color? :plaid)")

  [:div.note "Let's remember back to the beginning of this section where we saw that Clojure sets can serve as membership tests. Speculoos can therefore use sets as a nice shorthand for a membership predicate."

   [:p [:code "color?"] " implements " [:code "IFn"] " and thus behaves like a predicate when invoked as a function. " [:code ":red"] " satisfies our " [:code "color?"]  " predicate and returns a truthy value, whereas " [:code ":plaid"] " does not and returns a falsey value."]])


 (panel
  [:h3 "Validating with sets as a scalar predicate (in a sequential)"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [42 :red])")

    (prettyfy-form-prettyfy-eval "(all-paths [int? #{:red :green :blue}])" 45 55)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(validate-scalars [42 :red] [int? #{:red :green :blue}])" 55 45)]]

  [:div.note "During scalar validation, when a scalar in our data shares a path with a set in the specification, Speculoos enters " [:em "set-as-a-predicate"] " mode. (" [:em "Mode"]" only in the casual sense.  There are no modes nor states. The algorithm merely branches to treat the set differently depending on the scenario.) We'll make our specification mimic the shape of our data, but instead of two predicate functions, we'll insert one scalar predicate function, followed by a set, which behaves like a membership predicate."

   [:p "Our example data contains two scalar datums: " [:code "42"] " in the first spot and "  [:code ":red"] "  in the second. Each of those datums shares a path with a predicate in the scalar specification. The " [:code "42"] " is paired with the " [:code "int?"] " scalar predicate because they both share the path " [:code "[0]"] ". Both " [:code ":red"] " and " [:code "#{:red :green :blue}"] " share a path " [:code "[1]"] ", Speculoos regards it as a " [:em "set-as-a-scalar-predicate"] ". Let's run that validation now."]

   [:p "When Speculoos validates scalars, it treats the set in the specification as a predicate because the corresponding element in the data is a scalar, not a set. In this example, "[:code ":red"] " is a member of the " [:code "#{:red :green :blue}"] " set-predicate."]])


 (panel
  [:h3 "Validatind with sets as a scalar predicate (in a map)"]

  (prettyfy-form-prettyfy-eval " (validate-scalars {:x 42 :y :red} {:x int? :y #{:red :green :blue}})" 55 45)

  [:div.note "The same principles hold when validating elements of a map with a set-predicate. When a set in the specification contains a set that shares a path with a scalar in the data, that set is treated as a membership predicate."

   [:p "Scalar "[:code "42"] " pairs with predicate " [:code "int?"] " at path " [:code "[:x]"] " and scalar " [:code ":red"] " pairs with set-predicate " [:code "#{:red :green :blue}"] " at path " [:code "[:y]"] "."]])


 (panel
  [:h3 "Specifying Scalars within Set"]

  (prettyfy-form-prettyfy-eval "(all-paths [42 #{:chocolate :vanilla :strawberry}])" 55 65)

  (prettyfy-form-prettyfy-eval "(all-paths [int? #{keyword?}])")

  [:div.note "Sometimes the scalars in our data are contained in a set. Speculoos can validate scalars within a set during a scalar validation operation. Validating a set's scalar members follows all the same principles as validating a vector's scalar members, except for one wrinkle: Since elements of a set have no inherent location, i.e., sets are unordered, sets in our data are validated against " [:em "all"] " predicates contained in the corresponding set at the same path in the specification. An example shows this better than words."

   [:p "Let's apply the Mantras. We intend to validate scalars, so we'll use " [:code "validate-scalars"] ", which only applies predicates to scalars. Next, we'll make our our specification mimic the shape of the data. In this example, both the data and the specification are a vector, with something in the first spot, and a set in the second spot. Finally, we'll make sure that all predicates are paired with a scalar."]])


 (panel
  [:h3 "Validating scalars within a set"]

  (prettyfy-form-prettyfy-eval "(validate-scalars [42 #{:chocolate :vanilla :strawberry}] [int? #{keyword?}])" 55 45)

  [:div.note "First, notice how the scalar specification looks a lot like the data. Because the shapes are similar, " [:code "validate-scalars"] " is able to systematically apply predicates from the specification to scalars in the data. Speculoos validates " [:code "42"] " against predicate " [:code "int?"] " because they share paths in their respective vectors. At vector index " [:code "1"] " our data and specification both hold sets, so Speculoos enters " [:em "validate-scalars-within-a-set-mode"] ". Every predicate contained in the specification set is applied to every datum in the data's set. In this example, " [:code "keyword?"] " is individually applied to " [:code ":chocolate"] ", " [:code ":vanilla"] ", and " [:code ":strawberry"] ", and since each satisfy the predicate, the validation returns " [:code "true"] "."])


 (panel
  [:h3 "Validating scalars within a set, multiple scalar predicates, one scalar"]

  (prettyfy-form-prettyfy-eval "(validate-scalars #{:chocolate} #{keyword? qualified-keyword?})" 55 55)

  [:div.note "One of the defining features of Clojure sets is that they're amorphous bags of items, without any inherent ordering. Within the context of a set, it doesn't make sense to target one scalar predicate towards one particular scalar datum. Therefore, Speculoos validates scalars contained within a set more broadly. If our specification set contains more than one predicate, each of the predicates is applied to " [:em "all"] " the scalars in the data's set. In the next example, the specification set contains two predicates."

   [:p "Two scalar predicates in the specification applied to the one scalar datum. "[:code ":chocolate"] " is a keyword, but not a qualified keyword. Next, we'll see how to validate multiple scalars with multiple scalar predicates."]])


 (panel
  [:h3 "Validating scalars within a set, multiple scapar predicates, multiple scalar datums"]

  (prettyfy-form-prettyfy-eval "(validate-scalars #{:chocolate :vanilla :strawberry} #{keyword? qualified-keyword?})" 55 55)

  [:div.note "Validation applies " [:code "keyword?"] " and " [:code "simple-keyword?"] ", in turn, to every scalar member of the data set. Speculoos tells us that all the scalars in the data are indeed keywords, but at least one of the data's scalars is not a qualified keyword. Notice how Speculoos condenses the validation results. Instead of a validation entry for each individual scalar in the data set, Speculoos combines all the results for all the scalars. Two scalar predicates, two validation results."])


 (panel
  [:h3 "Validating scalars within a set, nested in a map"]

  (prettyfy-form-prettyfy-eval "(validate-scalars {:x 42 :y #{\"a\" \"b\" \"c\"}} {:x int? :y #{string?}})" 65 45)

  [:div.note "Again, the same principles apply for validating sets contained in a map."

   [:p [:code "int?"] " at " [:code ":x"] " applies to " [:code "42"] " also at " [:code ":x"] ". Then, " [:code "string?"] " at " [:code ":y"] " is applied to scalars " [:code "\"a\""] ", " [:code "\"b\""] ", and " [:code "\"c\""] " at " [:code ":y"] "."]])


 (panel
  [:h3 "Two passes: set-as-a-predicate & validate-scalars-within-a-set"]

  (prettyfy-form-prettyfy-eval "(validate-scalars [42 #{:foo :bar :baz}] [#{40 41 42} #{keyword?}])" 45 55)

  [:div.note "Speculoos performs the two modes in separate passes, so we may even use both " [:em "set-as-a-predicate-mode"] " and " [:em "validate-scalars-within-a-set-mode"] " during the same validation, as long as the predicates stay on their own side of the fence."

   [:p "In this example, the predicate at index " [:code "0"] " of the specification is a set while the datum at same index of the data is " [:code "42"] ", a scalar. Speculoos uses the set-as-a-predicate mode. Since " [:code "42"] " is a member of " [:code "#{40 41 42}"] ", that datum validates as truthy. Because the data at index " [:code "1"] " is itself a set, Speculoos performs set-scalar-validation. The " [:code "keyword?"] " predicate is applied to each element of " [:code "#{:foo :bar :baz}"] " at index " [:code "1"] " and they all validate " [:code "true"] "."]])


 (panel
  [:h3 "Validating a Set as a Collection: examine all-paths"]

  (prettyfy-form-prettyfy-eval "(all-paths [42 #{:puppy :kitten :goldfish}])" 55 65)

  [:div.note "Let's discuss how collection validation works when a set is involved. During a collection validation operation, Speculoos will ignore all scalars in the data. It will only apply predicates to collections. The rules are identical to how the other collections are validated: predicates from the specification are applied to the parent container in the data. But let's not get bogged down in a textual description; let's look at some examples."

   [:p "First, we'll start with some data that consists of a vector containing an integer, followed by a three element set. Let's generate all the paths."]])


 (panel
  [:h3 "Constructing a collection specification for a set"]

  [:pre [:code "[42 #{:puppy :kitten :goldfish}])"]]

  [:pre [:code "[   #{                        }]"]]

  [:pre [:code "[vector? #{set?}               ]"]]

  [:div.note "Mantra #1: Collection validation ignores scalars, so out of all those elements, validation will only consider the root at path " [:code "[]"] " and the nested set at path " [:code "[1]"] "."

   [:p "A good strategy for creating a collection specification is to copy-paste the data and delete all the scalars…"]

   [:p "…and insert some collection predicates near the opening bracket."]])


 (panel
  [:h3 "Examine all-paths of our set's collection specification"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [42 #{:puppy :kitten :goldfish}])" 55 65)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [vector? #{set?}])")]]

  [:div.note "Let's generate the paths for that collection specification."

   [:p "Notice the paths to the two predicates. Now, let's run a collection validation."]])


 (panel
  [:h3 "Validate set against our collection specification"]

  (prettyfy-form-prettyfy-eval "(validate-collections [42 #{:puppy :kitten :goldfish}] [vector? #{set?}])" 55 45)

  [:div.note [:code "validate-collections"] " was able to pair two collections in the data with two predicates in the specification, and we received two validation results. Collection predicate " [:code "vector?"] " at path " [:code "[0]"] " in the specification was applied to whatever is at path " [:code "(drop-last [0])"] " in the data, which happens to be the root collection. Collection predicate " [:code "set?"] " at path " [:code "[1 set?]"] " in the specification was applied to path " [:code "(drop-last [1 set?])"] " in the data, which happens to be our nested set containing pet keywords."

   [:p "Remember: Scalar predicates apply to the scalar at their exact location. Collection predicates apply to the collection directly above their head."]])


 (whats-next-panel
  set-index
  [:div.note "What's-next presenter notes for 'Sets' screencast..."])
 ]