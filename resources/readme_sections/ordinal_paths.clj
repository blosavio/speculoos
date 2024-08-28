(comment ;; digression on ordinal paths

  [:pre
   [:code "                    v------------v--- where are list? and seq? applied?"]
   [:br]
   [:code "[vector? map? set? [list?] map? [seq?]"]]

  [:p "Now we return to that funky keyword " [:code ":ordinal-path-datum"] ". In normal usage, you won't have to do much of anything with it. It's useful for diagnostic purposes. " [:em "Path"] " and " [:em "datum"] " mean just that, a path to the data. " [:em "Ordinal"] " signifies that this variety of path involves some kind of order. In the context of predicates in a collection specification, it denotes the " [:em "0th"] ", " [:em "1st"] ", " [:em "2nd"] ", etc, rank, when considering only collections."]

  [:p [:code "get"] " will help illustrate. " [:code "get"] " says " [:em "If you give me a sequence and an integer " [:code "n"] ", I'll give you back the " [:strong "n"] "th item of the sequence."] " Speculoos provides a special version of "[:code "get"] " named " [:code "ordinal-get"] " that says " [:em "If you give me a sequence and an integer " [:code "n"] ", I'll give you back the " [:strong "nth collection"] " contained in the sequence."] " Let's take an example of a parent vector containing alternating integers and vectors."]

  [:pred [:code "[11 [22] 33 [44] 55 [66]]"]]

  [:p "We can simulate the ordinal counting operation by first removing the non-collections."]

  [:pre (print-form-then-eval "(as-> [11 [22] 33 [44] 55 [66]] v (filterv #(coll? %) v) (get v 2))")]

  [:p [:code "filterv"] " retains only the collections " [:code "[22]"] ", " [:code "[44]"] ", and " [:code "[66]"] " in its returned vector. Then " [:code "get"] " returns the element at zero-indexed integer " [:code "2"] ", which is " [:code "[66]"] ". " [:code "ordinal-get"] " does substantially that process."]

  [:pre
   (print-form-then-eval "(require '[speculoos.core :refer [ordinal-get]])")
   [:br]
   [:br]
   (print-form-then-eval "(ordinal-get [11 [22] 33 [44] 55 [66]] 2)")]

  [:p [:code "[66]"] " is located at ordinal index " [:code "2"] " because it is the third collection of the input sequence." [:code "validate-collections"] "' rule for applying predicates to collections nested in a sequence is that it applies them by ordinal. Let's take a look at some sequential data containing some nested collections."]
  [:pre (print-form-then-eval "(def nested-data [11 {:a 22} 33 (list 44) 55 #{66} 77 [88]])")]

  [:p "Collection validation ignores scalars, so those two-digit integers are just along for the ride. Let's make our collection specification shaped (mostly) like our data. We'll start with the shell. I'm just going to copy-paste and let my editor delete scalars."]

  [:pre [:code "[{:a } (list ) #{} []]"]]

  [:p "Collection validation ignores scalars, and that " [:code ":a"] " keyword only points to a scalar, so I'll delete that, too."]
  
  [:pre [:code "[{} (list ) #{} []]"]]

  [:p "Let's test the root container with a simple predicate. The predicate will be applied to the correspsonding collection in the data that contains it."]

  [:pre [:code "[vector? {} (list ) #{} []]"]]

  [:p "Next, we'd like to know that root container of the data contains exactly four non-scalars, so we'll write a predicate."]

  [:pre (print-form-then-eval "(def contains-four-colls? (fn [v] (= 4 (count (filter #(coll? %) v)))))")]

  [:p "But where shall we put it? Collection predicates apply to their containing collection, and every predicate will be applied because " [:code "(drop-last predicate-path)"] " for each of those predicates all evaluate to the same location. We could put it here…"]

  [:pre [:code "[vector? {} contains-four-colls? (list ) #{} []]"]]

  [:p "…or here…"]

  [:pre [:code "[vector? {} (list ) contains-four-colls? #{} []]"]]

  [:p "…or here at the end…"]

  [:pre [:code "[vector? {} (list ) #{} [] contains-four-colls?]"]]

  [:p "…but I recommend clumping them up at the beginning. I like having them all visually close together instead of spread out between the nested collections where they might escape human notice."]

  [:pre [:code "[vector? contains-four-colls? {} (list ) #{} []]"]]

  [:p "Now let's put some rudimentary collection predicates in each of the nested collections."]
  
  [:pre
   [:code "                                  v---- remember, predicates applied to a map require a sham key"]
   [:br]
   [:pre (print-form-then-eval "(def nested-coll-spec [vector? contains-four-colls? {:is-map? map?} (list list?) #{set?} [sequential?]])")]]

  [:p "Let's run a validation and see if we can understand the output."]

  [:pre (print-form-then-eval "(validate-collections nested-data nested-coll-spec)")]

  [:p "Phew. We can do this. Gotta go bit by bit. First thing to notice is that we put six predicates into the collection specification, and the validation returned six maps, so " [:code "validate-collections"] " was able to pair up each of predicates in the specification with a collection in the data. If there were any un-paired predicates, it would have ignored them. Taking the results in order:"
   [:ul
    [:li [:code "vector?"] " was applied to the root of the data, which it satisfies." ]
    [:li "Likewise, "[:code "contains-four-colls?"] " was also applied to the root collection of the data because when the last element of its own path was dropped, it too targeted the root."]
    [:li [:code "map?"] " is nested at path " [:code "[2 :is-map?]"] " in the collection specification. " [:code "(drop-last [2 :is-map?])"] " yields " [:code "[2]"] ". Since the parent of that collection is a sequence, there could have zero or infinity intervening predicates targeting the parent. " [:code "validate-collections"] " determined that this nested map is at the ordinal path " [:code "[0]"] " which means it's the first. That signifies that we want to apply that predicate to whatever is the first collection contianed within the data's root. In this example, that's " [:code "{:a 22}"] ", which indeed satisfies the predicate."]
    [:li ]
    [:li ]
    [:li ]]]  
  )