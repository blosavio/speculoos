(ns documentation
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as hutil]
   [speculoos-hiccup :refer :all]
   [zprint.core :as zp]))


(def documentation-UUID #uuid "a944b037-0fba-43d6-b27b-c701f468ee2b")
(declare predicate-1 predicate-2 predicate-3 predicate-4)
(def ^:dynamic *eval-separator* ";; => ")


(spit "doc/documentation.html"
      (page-template
       "Speculoos --- Documentation"
       documentation-UUID
       [:body
        (nav-bar "Documentation")
        (into
         [:article
          [:h1 "Speculoos Documentation"]]
         (section-nav

          [:section#description
           [:h2 "Description"]
           [:p  "The Speculoos library validates Clojure data. Building on that capability, Speculoos can test a function's arguments, its return values, and the relationships between them. Speculoos can also generate sample data to test functions. In short, Speculoos attempts to do what "
            [:code "clojure.spec.alpha"]
            " does, while exploring " [:a {:href "ideas.html"} "three ideas"] "."]
           [:p
            "Let's consider validation. "
            [:em "Does our data look the way we expect it?"]
            " Validation is the process of creating a specification, a human- and machine-readable declaration about properties of data, and then checking if an instance of data satisfies that specification."]
           [:p "Pretend Dominique's service accepts a string. Clojure's " [:code "clojure.core/string?"] " tests whether something is a string."]
           [:pre
            (print-form-then-eval "(string? \"big parade\")")
            (print-form-then-eval "(string? 76)")]
           [:p "We now know that we can send Dominique's service "
            [:code "\"big parade\""]
            " but not "
            [:code "76"] "."]
           [:p "Dominique updates her service, and now she requires a "
            [:em "vector which contains a string."]
            " We could write a bespoke function to validate this new requirement, but Dominique's service will probably evolve, and so we decide to pull in Speculoos as a dependency to handle more sophisticated validation."]
           [:pre (print-form-then-eval "(require '[speculoos.core :refer [valid-scalar-spec? validate-scalar-spec only-invalid]])")]
           [:p "So we stuff "
            [:code "string?"]
            " into a vector and ask Speculoos to apply it to our data."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [\"big parade\"] [string?])")]
           [:p "Speculoos' "
            [:code "valid…?"]
            " family of functions take data as the first argument and our specification as the second argument."]
           [:p "Dominique later tells us that she needs more information, and her requirement expands to "
            [:em "vector which contains a string, followed by a keyword and an integer"]
            ". We create a Speculoos specification by arranging predicates in the same pattern as we expect the elements to appear in the data."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [\"big parade\" :trombones 76] [string? keyword? int?])")]
           [:p "Speculoos marches along our vector, testing each element against the predicate in our specification. In this example, Speculoos tells us that " [:code "[\"big parade\" :trombones 67]"] " satisfies all our properties and returns "[:code "true"] ". What if we supply a non-sensical number of trombones?"]
           [:pre (print-form-then-eval "(valid-scalar-spec? [\"big parade\" :trombones \\z] [string? keyword? int?])")]
           [:p "Sure enough, the character " [:code "\\z"] " is not a valid number of trombones in a big parade. We've specified our data in a way that humans and machines can understand and validated some data."]
           [:p "Ever busy, Dominique tells us we now need to validate any number of suffixed keyword+integer pairs. So we "
            [:em "compose"]
            " a new specification. "
            [:code "concat"]
            " combines two specification sub-components while "
            [:code "cycle"]
            " gives us an infinite sequence of keyword/integer pairs."]
           [:pre (print-form-then-eval "(def our-composed-spec (concat [string?] (cycle [keyword? int?])))")]
           [:p "Let's see what happens when we toss Speculoos some larger data and that non-terminating specification."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [\"parade\" :trombones 76 :cornets 110 :bassoons 0]  our-composed-spec)")]
           [:p "Speculoos only validates elements that are contained both in the data and in the specification, so the fact that "
            [:code "cycle"]
            " never terminates makes it a powerful tool to specify an arbitrarily long sequence."]
           [:p "Our friend Ahmed's service also requires data of a particular shape, but in his case, a map that looks something like " [:code "{:species \"oak\" :type :deciduous}"] ". Speculoos validates maps by applying predicates found in the identical keys of the specification."]
           [:pre (print-form-then-eval "(valid-scalar-spec? {:species \"oak\" :type :deciduous} {:species string? :type keyword?})")]
           [:p "And if we supply data with the wrong type, for example, a string instead of a keyword"]
           [:pre (print-form-then-eval "(valid-scalar-spec? {:species \"oak\" :type \"deciduous\"} {:species string? :type keyword?})")]
           [:p "Speculoos reports that the data is not valid."]
           [:p "Ahmed tells us that the " [:code ":type"] " may only be either " [:code ":deciduous"] " or " [:code ":coniferous"] ". Clojure sets make wonderful predicates"]
           [:pre
            (print-form-then-eval "(#{:deciduous :coniferous} :deciduous)")
            (print-form-then-eval "(#{:deciduous :coniferous} :shrubbery)")]
           [:p " so we tighten our specification to test memebership in Ahmed's enumerated set."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? {:species \"oak\" :type :deciduous} {:species string? :type #{:deciduous :coniferous}})")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? {:species \"oak\" :type :confetti} {:species string? :type #{:deciduous :coniferous}})")]
           [:p "Speculoos tells us that " [:code ":confetti"] " is not one of the expected values."]
           [:p "Gertrude challenges us with a new task: validate some elements in a nested data structure. She supplies us with some data."]
           [:pre (print-form-then-eval "(def hobbit {:first-name \"Peregrin\"
                                :last-name \"Took\"
                                :nick-name \"Pippin\"
                                :home-town \"The Shire\"
                                :friends [\"Frodo\" \"Samwise\" \"Merry\" \"Aragorn\" \"Legolas\" \"Gmili\" :Gandalf]})")]
           [:p "We then write a specification for single hobbit."]
           [:pre (print-form-then-eval "(def hobbit-spec {:first-name string? :last-name string? :favorite-ice-cream keyword? :friends (repeat string?)})")]
           [:p "Remembering that Speculoos only validates elements in both the data and the specification, we take advantage of " [:code "repeat"] " to supply an arbitrarily long sequence of hobbit friend specifications."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [{:first-name \"Meriadoc\"} {:last-name :Brandybuck} {:favorite-pipe-weed \"Longbottom Leaf\"} hobbit] (repeat hobbit-spec))")]
           [:p "In this example, we can see the problems by eye: the hobbit's " [:code ":last-name"] " and Pippin's final friend, Gandalf, should both be strings, but in this instance both are invalid keywords. Speculoos' family of " [:code "valid…?"] " functions merely return " [:code "true/false"] ". Speculoos provides some helper functions that highlight the interesting, invalid elements."]
           [:pre (print-form-then-eval "(only-invalid (validate-scalar-spec [{:first-name \"Meriadoc\"} {:last-name :Brandybuck} {:favorite-pipe-weed \"Longbottom Leaf\"} hobbit] (repeat hobbit-spec)))")]
           [:p "Now we can tell Gertrude precisely where the problems are. The " [:em "path"] " elements of the report provide an unambiguous address to the invalid data elements. The path " [:code "[1 :last-name]"] " can be used to inspect the data like this."]
           [:pre
            (print-form-then-eval "(require '[speculoos.fn-in :refer [get-in* assoc-in* update-in* dissoc-in*]])")
            [:br]
            (print-form-then-eval "(get-in* [{:first-name \"Meriadoc\"} {:last-name :Brandybuck} {:favorite-pipe-weed \"Longbottom Leaf\"} hobbit] [1 :last-name])")]
           [:p [:code "get-in*"] " extracts the map at the outer vector's " [:code "1"] " index, then gets the nested map's value keyed by " [:code ":last-name"]  ", which in this case is " [:code ":Brandybuck"] "."]
           [:p "Speculoos provides analogous functions for altering or removing values in a nested structure. We could 'repair' the hobbity data to a valid state like this"]
           [:pre (print-form-then-eval "(def repaired-Pippin (assoc-in* hobbit [:friends 6] \"Gandalf\"))")]
           [:p "such that we can send Gertrude a proper hobbit."]
           [:pre (print-form-then-eval "(valid-scalar-spec? repaired-Pippin hobbit-spec)")]

           [:p "Viraj has made a machine that requires a vector which contains at least five elements. So far, we have been using Speculoos to validate " [:em "scalars"] ", indivisible elements such as strings, keywords, and integers. The vector's length is a property of the " [:em "collection"] ", and Speculoos validates collection specifications in a distinct process. First, we define a collection specification with a useful name."]
           [:pre (print-form-then-eval "(def length-six-or-greater? #(<= 6 (count %)))")]
           [:p "Then we can validate some sample data to see if it satisfies the collection specification."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [valid-collection-spec? validate-collection-spec]])")
            [:br]
            (print-form-then-eval "(valid-collection-spec? [:Dorothy :Toto :Scarecrow :Tin-Man :Cowardly-Lion :Wizard-of-Oz] [length-six-or-greater?])")]
           [:p "Let's see what happens if we don't supply Viraj with enough data."]
           [:pre (print-form-then-eval "(valid-collection-spec? [:water :coffee :tea] [length-six-or-greater?])")]
           [:p "Viraj will be disappointed. Speculoos can tell us more than "
            [:code "true/false"]
            " if we invoke a different function"]
           [:pre (print-form-then-eval "(validate-collection-spec [:water :coffee :tea] [length-six-or-greater?])" )]
           [:p "One of Speculoos' core ideas is that collections ought to be validated separately from the scalars they contain. However, Viraj wants to specify two concepts: his vector must contain at least six elements, and each of those elements must be a keyword or a string. We've already written his collection specification: the vector must contain at least six elements. We can write his scalar specification like this."]
           [:pre (print-form-then-eval "(def kw-or-string? #(or (keyword %) (string? %)))")]
           [:p "We give that scalar specification a couple of quick tests."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? [:Dorothy :Toto \"Scarecrow\" :Tin-Man \"Cowardly-Lion\" :Wizard-of-Oz] (repeat kw-or-string?))")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? [:Dorothy 42 :Toto \"Scarecrow\" :Tin-Man \"Cowardly-Lion\" :Wizard-of-Oz] (repeat kw-or-string?))")]
           [:p "Though scalars and collections are specified separately, for any one chunk of data, we'll often want to specify aspects of its scalars and aspects of their containing collections. Speculoos provides functions to validate both simultaneously, but distinctly. We invoke " [:code "valid?"] " with the data as the first argument, the scalar specification as the second argument, and the collection specification as the third argument."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [valid? validate]])")
            [:br]
            (print-form-then-eval "(valid?  [:Dorothy :Toto \"Scarecrow\" :Tin-Man \"Cowardly-Lion\" :Wizard-of-Oz] (repeat kw-or-string?) [length-six-or-greater?])")]
           [:p "Speculoos can inform Viraj with details on invalid properties of his data."]
           [:pre (print-form-then-eval "(only-invalid (validate [:Dorothy 42 :Toto \"Scarecrow\"] (repeat kw-or-string?) [length-six-or-greater?]))")]
           [:p "Yup, " [:code "42"] " is neither a keyword nor a string, and the vector itself is not long enough. We validated the scalars and collections separately, but with a single function invocation."]

           [:p "Now that we know how to use Speculoos to specify and validate Clojure data structures, we can explore how to specify and test the behavior of Clojure functions. A function's argument list is merely a sequence of values, and a function's return is also a value, possibly a heterogeneous, arbitrarily-nested data structure. Let's imagine our friend Himari has asked us to work on this function."]
           [:pre (print-form-then-eval "(defn dental-recommendation \"101% statistically-rigorous hygiene advice given integer n and flavor.\" {:speculoos/arg-scalar-spec [pos-int? string?] :speculoos/ret-scalar-spec string?} [n flavor] (str n \" out of \" (inc n) \" dentists recommend \" flavor \"-o-Dent toothpaste!\"))")
            [:br]
            (print-form-then-eval "(dental-recommendation 4 \"Mint\")")]
           [:p "Note the specifications are included in the function's metadata. Since Speculoos specifications are human- and machine-readable, they serve both as documentation,"
            (label "doc-enhancement")
            (side-note "doc-enhancement" (h2/html "Also note that the docstring misleadingly calls for an integer, while the argument specification more rigorously calls for a " [:em "positive"] " integer. Specifications, as in this instance, can potentially communicate more and better information to your library users."))
            " and as a basis to perform tests and validation of the function. Speculoos will validate any present scalar specification or collection specification on the argument list or the return values. In addition, Speculoos will perform an "
            [:em "argument-vs-return"] " validation, analagous to the one performed by " [:code "spec.alpha"] ". Merely adding the meta-data to the function does not cause any action."]
           [:pre (print-form-then-eval "(dental-recommendation 4.4 \"Taco\")")]
           [:p [:code "4.4"] " is not an integer, but Speculoos was not watching. We must explicitly request that Speculoos perform a validation."]
           [:pre
            (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-meta-spec validate-fn-with exercise-fn]])")
            [:br]
            (print-form-then-eval "(validate-fn-meta-spec dental-recommendation 4 :Jalapeno)")]
           [:p "Oops, " [:code ":Jalapeno"] " is not a string as required by the argument scalar specification, so Speculoos tosses a report. Speculoos can also validate a function on-the-fly. For demonstration purposes, we'll specify a character for the second argument, but supply a string, which we have previously considered valid."]
           [:pre (print-form-then-eval "(validate-fn-with dental-recommendation {:speculoos/arg-scalar-spec [pos-int? char?]} 88 \"Filet Mignon\")")]
           [:p "Sure enough, " [:code "\"Filet Mignon\""] " is not a character. Finally, Speculoos can peek at the argument specification and generate test data for exercising Himari's function."]
           [:pre (print-form-then-eval "(exercise-fn dental-recommendation 5)")]
           [:p "Speculoos generated five pairs of random postive integers and random strings and invoked Himari's function."]
           [:p "We've now taken a whirlwind tour of how Speculoos can help specify and validate Clojure data structures, and how that can be used to describe Clojure functions, check their arguments, and automatically test their operation. Now, let's talk about Speculoos' implementation."]]

          [:section#implementation
           [:h2 "Implementation"]
           [:p "Understanding a little bit about Speculoos' inner workings will help with knowing how to effectively use it. Speculoos is not clever. Nor is it efficient. It's first step in handling any Clojure data structure is to exhaustively enumerate the path to every element. Let's take a look."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [all-paths]])")
            [:br]
            (print-form-then-eval "(all-paths [42 \"abc\" :foo])")]
           [:p [:code "all-paths"]
            (label "all-paths")
            (side-note "all-paths" (h2/html [:code "all-paths"] " might be useful to write your own utility functions."))
            " recursively walks through any heterogeneous, arbitrarily-nested Clojure data structure and gathers a " [:code ":path"] " and " [:code ":value"] " for each element. The path for each element is a sequence of values that unambiguously addresses where in the structure to find that element. In this example, zero-based integers address elements contained in our vector. " [:code "42"] " resides at index zero, " [:code "\"abc\""] " at index one, etc. Note that the outer parent vector has a path of " [:code "[]"] "."]
           [:p "Similar story for maps."]
           [:pre (print-form-then-eval "(all-paths {:a 42 :b \"abc\" :c true})")]
           [:p "Map elements are addressed by their keys. Very often, the key is a Clojure keyword, and the element's path contains that keyword. However, any Clojure value can be a map key. Integers are valid map keys."]
           [:pre (print-form-then-eval "(all-paths {0 \"zero value\" 1 \"one value\" 99 \"ninety-nine value\"})")]
           [:p "The paths for those elements are therefore integers. Composite values are also valid map keys."]
           [:pre (print-form-then-eval "(all-paths {[1 2 3] \"one-two-three val\" {:a 1} \"a-1-map val\" [] \"empty vec value\"})")]
           [:p "In this case, the elements' paths are addressed by those composite keys. " [:code "\"a-1-map\""] " is addressed by the key " [:code "{:a 1}"] ". The path to " [:code "\"one-two-three val\""] " is " [:code "[1 2 3]"] "."]
           [:p "Perhaps you've noticed that I've used the term " [:em "heterogenous, arbitrarily-nested data structure"] ". This path principle extends to every element type, at any nesting level. Let's look at a few illustrations. First, paths for nested elements."]
           [:pre (print-form-then-eval "(all-paths [11 [22 33] 44 [[55]]])")]
           [:p "Elements in the top-level vector, in this example " [:code "11"] " and " [:code "44"] ", have single integer paths. " [:code "22"] " and " [:code "33"] " have paths composed of two integers. The first integer is the index to the two-element child vector, while the second integer is the index to their respective position in that child vector. Paths to elements nested in maps work similarly."]
           [:pre (print-form-then-eval "(all-paths {:a [11 22] :b [33 [44] {:c 55}]})")]
           [:p "Each element, scalar or collection, has a unique, unambiguous path that tells you how to navigate to that value. For this heterogenous, nested structure, some element's paths contain both integer indexes and keyword keys. Value " [:code "55"] " in this example is addressed by key " [:code ":b"] " at the top-level map, index " [:code "2"] " within that vector, and finally at key " [:code ":c"] " within that nested map."]
           [:p "There is no conceptual limit on how deep the nesting, nor on type of the containers. Speculoos handles each of Clojure's data collections: vectors, maps, lists, and sets."]
           [:pre (print-form-then-eval "(all-paths [11 {:a 22 :b (list 33 #{44})}])")]
           [:p "Notice how there is a single, unified path to every element, regardless of its container type. Vectors and list elements are addressed by an integer index. Map elements are addressed by their key. Map keys are often Clojure keywords, but we've seen that they can be any value such as integers, or even composite data types. Sets elements are addressed by their identities. So in the example above, " [:code "44"] " is located at path " [:code "[1 :b 1 44]"] ", where the tail value is not an index nor a key, but the element's identity. Admittedly, addressing elements in a set can be a little like herding cats, but it's still useful to have the capability. Wrangling sets merits its own " [:a {:href "#sets"} "dedicated section"] "."]
           [:p "With an element's path, you can precisely inspect its value. Let's pull out that " [:code "99"] "."]
           [:pre (print-form-then-eval "(get-in* {:x 11 :y (list 22 33 [44 {:z [55 66 77 [88 {:q 99}]]}])} [:y 2 1 :z 3 1 :q])")]
           [:p "Speculoos' family of starred functions can get, associate, update, and dissociate any value of any type in a heterogeneous, arbitrarily-nested data structure. Later, we'll " [:a {:href "#fn-in*"} "discuss a little more"] " about the starred functions. For now, know that much of Speculoos' functionality relies on navigating paths. Once we've gathered the paths for a piece of data, and a specification composed of predicates arranged in a data structure that mimics that data, validation is merely a process of systematically appying each predicate to its respective scalar value in the data."]
           [:pre
            (print-form-then-eval "(def data {:a 11 :b [22/7]})")
            [:br]
            (print-form-then-eval "(def scalar-spec {:a int? :b [decimal?]})")
            [:br]
            [:code ";; equivalent to (int? 11)"]
            (print-form-then-eval "((get-in* scalar-spec [:a]) (get-in* data [:a]))")
            [:br]
            [:code ";; equivalent to (decimal? 22/7)"]
            (print-form-then-eval "((get-in* scalar-spec [:b 0]) (get-in* data [:b 0]))")]
           [:p "Speculoos' " [:code "validate-scalar-spec"] " does exactly that."]
           [:pre (print-form-then-eval "(validate-scalar-spec data scalar-spec)")]
           [:p "Speculoos' "[:code "valid…?"] " series of functions are simply conveniences that return " [:code "true"] " if all the supplied predicates are satisfied."]
           [:p "Distinct from validating scalars, Speculoos can validate properties of a collection while ignoring the elements it contains. Recall that every element of a heterogeneous, arbitrarily-nested collection owns a path, not only the scalar elements. Consider this vector containing an empty map at index " [:code "0"] " and an empty list at index " [:code "1"] ", a total of three elements, each with its own path."]
           [:pre (print-form-then-eval "(all-paths [{} '()])")]
           [:p "Validating a collection specification is analagous to validating a scalar specification, with the difference being that the predicate is applied to the parent container. Mechanistically, the collection predicate is applied to the entity located at " [:code "(drop-last predicate-path)"] "."
            (label "more-details")
            (side-note "more-details" "Speculoos collection validation has a few more additional rules to make it completely generic, but let's consider only this bit for now.")]
           [:pre
            (print-form-then-eval "(def data [{:foo \"bar\"} '(:baz)])")
            (print-form-then-eval "(def count-is-two? #(= 2 (count %)))")
            (print-form-then-eval "(def collection-spec [count-is-two?])")           
            [:br]
            (print-form-then-eval "(def path-to-element [])")
            (print-form-then-eval "(def path-to-predicate [0])")
            [:br]
            (print-form-then-eval "(get-in* data path-to-element)")
            [:br]
            (print-form-then-eval "(get-in* collection-spec path-to-predicate)")]
           [:p "The sole collection predicate in the specification "
            [:code "count-is-two?"]
            " is located at path "
            [:code "[0]"]
            " whereas the collection to be tested is located at "
            [:code "(drop-last [0])"]
            "."]
           [:pre
            [:code ";; equivalent to (count-is-two? [{:foo \"bar\"} '(:baz)])"]
            (print-form-then-eval "((get-in* collection-spec path-to-predicate) (get-in* data path-to-element))")]
           [:p
            "We invoke the predicate, which tests if a collection has a count equal to two, with the vector as the sole argument. In this case, the predicate returns "
            [:code "true"]
            ". I've hand-waived too much, so now would be a good time be more explicit."]]

          [:section#principles
           [:h2 "Speculoos working principles"]
           [:p "So you've got some Clojure data, and you want to check it somehow. Awesome. Maybe Speculoos can help. Let's understand Speculoos' principles."]
           [:p "Speculoos explicitly distinguishes specifying and validating scalars (numbers, strings, booleans, etc.) and collections (vectors, maps, lists, sequences, and sets). I visualize the distinction like this."
            (label "color-blind")
            (side-note "color-blind" "Advance apologies to color-blind readers. (I myself am partially colorblind.) But text has only limited information channels after the character forms and color.")]
           [:div.side-by-side-container
            [:div.side-by-side.tight
             [:p "Scalar specification " [:span.highlight "targets"]]
             [:code
              [:span.de-highlight "["]
              [:span.highlight "42 \\z "]
              [:span.de-highlight "{"]
              [:span.highlight ":a true :b 22/7"]
              [:span.de-highlight "}]"]]]

            [:div.side-by-side.tight
             [:p "Collection specification " [:span.highlight "targets"]]
             [:code
              [:span.highlight "["]
              [:span.de-highlight "42 \\z "]
              [:span.highlight "{"]
              [:span.de-highlight ":a true :b 22/7"]
              [:span.highlight "}]"]]]]
           [:p "They compliment each other. Scalar specifications target the data elements. Collection specifications target the organizers. Employing both, you can specify nearly any aspect of your heterogeneous, arbitrarily-nested data structure."]

           [:p "Speculoos specifications mimic the shape of your data. Validating a vector's scalars involves a specification that's also a vector."]
           [:pre
            (print-form-then-eval "(def vector-data [\"water\" \"coffee\" \"tea\"])")
            [:br]
            (print-form-then-eval "(def vector-scalar-spec [predicate-1 predicate-2 predicate-3])")]
           [:p "A collection specification for a validating a map is itself a map."]
           [:pre
            (print-form-then-eval "(def map-data {:a \"water\" :b [\"coffee\" \"tea\"]})")
            [:br]
            (print-form-then-eval "(def map-collection-spec {:check-keys predicate-4 :b [predicate-1 predicate-2]})")]
           [:p "You could even copy-paste your data and instruct your text editor to replace the datums with your choice of predicates. But don't let me box you in. Speculoos specifications are merely Clojure data structures and Speculoos predicates are merely Clojure functions. Compose your Speculoos specification with whatever tools you know and love. To augment "
            [:code "clojure.core"]
            "'s offerings, I wrote a "
            [:a {:href "#fn-in*"} "handful"]
            " of my own that appeal to the particular way my brain works."]

           [:p "Speculoos only validates where and when you want. Speculoos will simply ignore any datum that does not have a corresponding predicate."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? {:a 42 :not-checked \"blah blah\"} {:a int?})")
            [:br]
            (print-form-then-eval "(def all-ints? #(every? int? %))")
            [:br]
            (print-form-then-eval "(valid-collection-spec? [[1 2] [:not-checked] [3 4]] [[all-ints?] [] [all-ints?]])")]
           [:p "If you want to temporarily relax a specification, merely " [:code "assoc"] " or " [:code "dissoc"] " that particular predicate."]
           [:pre
            (print-form-then-eval "(def list-data (list 'sym 22/7 true))")
            (print-form-then-eval "(def list-scalar-spec (list symbol? ratio? string?))")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? list-data list-scalar-spec)")
            [:br]
            (print-form-then-eval "(def relaxed-spec (dissoc-in* list-scalar-spec [2]))")
            (print-form-then-eval "(valid-scalar-spec? list-data relaxed-spec)")]
           [:p "Your data and specification haven't changed and you can get on with your day."]]

          [:section#core
           [:h2 "Core functions"]
           [:p "The "[:code "speculoos.core"] " namespace provides functions for validating Clojure data structures. Functions whose names start " [:em "validate-…"] " systematically apply predicates, arranged in a prescibed pattern in your specification, to elements of your data. The return value is an exhaustive sequence of all datum/predicate pairs, their path, and whether the predicate was satisfied. For example, we can validate with a scalar specification"]
           [:pre (print-form-then-eval "(validate-scalar-spec [\"mountains\" :beach 'forest] [string? keyword? string?])")]
           [:p "and obtain three evaluations of the three datums. We see that the first two datums are valid, while the third datum is invalid (i.e. " [:code ":valid?"] " is " [:code "false"] ") because " [:code "'forest"] " at path " [:code "[2]"] " is not a string. This namespace contains " [:code "validate-…"] " variants for scalars and collections." ]
           [:p "Sometimes, we just want a high-level " [:em "yes/no"] " whether the data structure as a whole satisfies all of the predicates. Functions named with the pattern " [:em "valid-…?"] " return a simple boolean, dependent on whether all the specified predicates are satisfied."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [\"mountains\" :beach 'forest] [string? keyword? string?])")]
           [:p "With the same data and specification as before, we see that our data is indeed invalid. Similarly to the validation functions, the namespace provides functions name with variations of " [:code "valid…?"] " for both scalars and collections."]
           [:p "Speculoos validates scalars contained in maps with the same principle: make your specification look like the data, replacing the scalar with your choice of predicate. Here's a quick example."]
           [:pre (print-form-then-eval "(valid-scalar-spec? {:first-name \"Sherlock\" :last-name \"Holmes\" :address {:street-name \"Baker Street\" :street-number [221 'B]}}
                                                                 {:first-name string? :last-name string? :address {:street-name string? :street-number [int?] :city string?}})")]
           [:p "Predicates in the specification share the same keyword as the value they validate. Any datum that does not have a corresponding key-vlaue pair in the predicate will not be validated. In this example, the second element of the " [:code ":street-number"] " is not validated becuase " [:code "'B"] " does not have a corresponding predicate in that path of the specification. Likewise, any predicate that does not have a corresponding datum will also not be validated. In this example, " [:code ":city"] "'s predicate, " [:code "string?"] ", will not be used for validation because the data does not have a corresponding datum at that path."]
           [:p "You're probably tired of me mentioning how Speculoos splits up scalar and collection validation, but here's a twist: Speculoos has a pair of convenience functions that do both. As a consolation to having to write two specifications for each data structure, I reserved the best, i.e., shortest, function names for doing both with one invocation. Supply data, then a scalar specification, followed by collection specification."]
           [:pre
            (print-form-then-eval "(defn length-four? [v] (= 4 (count v)))")
            [:br]
            (print-form-then-eval "(valid?   [42 :foo \\z] [int? keyword? char?] [length-four?])")]
           [:p "Plain " [:code "valid?"] " performs a scalar validation immediately followed by a collection validation and returns a single boolean. In this example, three scalars were validated followed by one collection validation. For a litle more detail,"]
           [:pre (print-form-then-eval "(validate [42 :foo \\z] [int? keyword? char?] [length-four?])")]
           [:p [:code "validate"] " generates an itemized sequence of all the scalar validations followed by all the collection validations. We can see that our data did not satisfy the collection specification: Our specification required the vector contain exactly four elements, whereas our vector contains only three."]
           [:p "It's tedious to manually scan validation results, so Speculoos supplies a helper function to show only the invalid results."]
           [:pre (print-form-then-eval "(only-invalid (validate [42 :foo \\z] [int? keyword? char?] [length-four?]))")]
           [:p "Speculoos validates sequences by applying predicates, located in the specification, to the sequence located at the path of its parent. In this instance, the predicate " [:code "length-four?"] " is found at path " [:code "[0]"] " in the specification, but is applied to the path " [:code "[]"] " of the data, which is the three-element vector."
            (label "examine-soon")
            (side-note "examine-soon" (h2/html "This invalid result contains a key-value pair that we haven't yet discussed, an " [:em "ordinal parent path"] ", which we will examine more closely in a few paragraphs."))]
           [:p "Let's try another tinted text illustration to compare the differences between applying predicates to scalars and collections."
            [:div.side-by-side-container
             [:div.side-by-side.tight
              [:p "Data scalars, top"]
              [:pre [:code
                     [:span.de-highlight "["]
                     [:span.highlight.c1 "42   "]
                     [:span.highlight.c2 ":foo    "]
                     [:span.highlight.c3 "\\z    "]
                     [:span.de-highlight "]"]]
               [:br]
               [:code
                [:span.de-highlight "["]
                [:span.highlight.c1 "int? " ]
                [:span.highlight.c2 "keyword? "]
                [:span.highlight.c3 "char?"]
                [:span.de-highlight "]"]]]
              [:p "Scalar specification predicates, bottom"]]

             [:div.side-by-side.tight
              [:p "Data sequence, top"]
              [:pre [:code
                     [:span.highlight.c4 "["]
                     [:span.de-highlight "42 "]
                     [:span.de-highlight ":foo "]
                     [:span.de-highlight "\\z    "]
                     [:span.highlight.c4 "]"]]
               [:br]
               [:code
                [:span.de-highlight "["]
                [:span.highlight.c4 "length-four?"]
                [:span.de-highlight "]"]]]
              [:p "Collection specification predicate, bottom"]]]]
           [:p "The difference is subtle. The scalar predicates ("
            [:span.c1 "red"]
            ", "
            [:span.c2 "blue"]
            ", and "
            [:span.c3 "green"]
            " in the left column) share the exact same paths as their target elements in the data vector. " [:code.c1 "int?"] " applies to " [:code.c1 "42"] ", " [:code.c2 "keyword?"] " applies to " [:code.c2 ":foo"] ", and " [:code.c3 "char?"] " applies to " [:code.c3 "\\z"] ". The predicate for the collection validation, "
            [:span.c4 "length-four?"]
            ", contained "
            [:em "within"] " the specification vector, applies the data vector itself. That's an ongoing theme: scalar predicates apply to the datum that shares their path, collection predicates apply to the parent container."]
           [:p "Let's set aside the scalar validation component and focus on collection validation. Pretend we want to validate a four-element vector with at least one element being a keyword."
            (label "reasonable")
            (side-note "reasonable" (h2/html "You might reasonably think that testing for a keyword is a scalar specification. But in this case, we are testing for a "
                                             [:em "keyword somewhere in a collection"] ", so it's a property of the collection."))
            " First, we'll set up our predicates."]
           [:pre
            (print-form-then-eval "(defn length-four? [v] (= 4 (count v)))")
            (print-form-then-eval "(defn at-least-one-kw? [v] (some keyword? v))")
            (print-form-then-eval "(defn first-greater-than-fourth? [v] (> (v 0) (v 3)))")]
           [:p "The first wrinkle is that " [:em "every"] " predicate in a collection specification, no matter how few nor how many, applies to the immediate parent container in the data. Let's validate the collection against three predicates using Speculoos' dedicated collection validation function."]
           [:pre (print-form-then-eval "(validate-collection-spec [42 \"abc\" 'foo 22/7] [length-four? at-least-one-kw? first-greater-than-fourth?])")]
           [:p "Speculoos validates all three predicates against the collection. " [:code "length-four?"] " is satisfied because " [:code "[42 \"abc\" 'foo 227]"] " contains four elements, " [:code "first-greater-than-fourth?"] " is satisfied because the first element is greater than the fourth element, but " [:code "at-least-one-kw?"] " is not satisfied because there is not at least one keyword. All three collection collection predicates were applied to the single container vector."]
           [:p "The second wrinkle involves how Speculoos determines where to find the parent collection sequence. Every predicate in the collection specification applies to its parent, "[:em "regardless of the presence of any intervening predicates or collections at that level of nesting"] ". So that the next example highlights what we're interested in, we'll bury our vector of interest into some nested mess where we don't care about any of the other stuff."]
           [:pre (print-form-then-eval "(def data-1 [[] [] []  [[] [] [42 \"abc\" 'foo 22/7]]])")]
           [:p "Okay, we've got to look sharp; our target vector is two levels deep. Speculoos' mantra is " [:em "Make your specification shaped like your data"] ". For a Speculoos scalar specification there is a nearly one-to-one correspondence between the data and the specification. The matra holds for Speculoos collection specification, but the correspondence is slightly weaker. We want our collection specification to mimic the data's square brackets, preserving the order, but Speculoos collection validation does not require a predicate for every collection. In this example, we don't care about all those empty vectors, so we simply leave their counterparts in the specification empty. We only put predicates into the collection that we want to validate."]
           [:pre
            (print-form-then-eval "(def spec-1 [[] [] [] [[] [] [length-four? at-least-one-kw? first-greater-than-fourth?]]])")
            [:br]
            (print-form-then-eval "(validate-collection-spec data-1 spec-1)")]
           [:p "As before, our vector satisfies our first and third collection predicates, while the second predicate is not satisfied because it does not contain at least one keyword. And now we can see a better demonstration of the ordinal parent paths. The predicates live at paths " [:code "[3 2 0]"] ", " [:code "[3 2 1]"] ", and " [:code "[3 2 2]"] ", respectively, in " [:code "spec-1"] ". But they all apply to the " [:em "vector"] " that lives at " [:code "[3 2]"] " in " [:code "data-1"] "."]
           [:p "Let's combine two principles: Speculoos applies collection predicates to the parent container, and all collection predicates apply to the parent. Speculoos does not require the predicates to live at any particular index within the specification. Validating sequences can therefore involve an interrupted stream of collection predicates."]
           [:pre
            (print-form-then-eval "(def data-2 [42 \"abc\" 'foo 22/7])")
            [:br]
            (print-form-then-eval "(def spec-2 [length-four? [] at-least-one-kw? [] first-greater-than-fourth?])")
            [:br]
            (print-form-then-eval "(validate-collection-spec data-2 spec-2)")]
           [:p "Validation skips over the empty vectors in the specification, in this example for two reasons. First, those nested vectors in the specification do not contain a predicate. But, second,  even if " [:code "spec-2"] " did contain those predicates, " [:code "data-2"] " possesses no corresponding nested vectors to test. Speculoos offers this flexibility in arranging your collection predicates so that the system is completely general. In practice, I recommend that you place all your collection predicates as close as possible to the head of the sequence where they apply. That convention will help future human readers of your specification."]
           [:p "So far, we've seen the " [:em "parent"] " and " [:em "path"] " parts, but let's do a final example that demonstrates " [:em "ordinal"] ". First, let's create some length-checking predicates with nice names that are easy to track."]
           [:pre
            (print-form-then-eval "(defn cero? [v] (= 0 (count v)))")
            (print-form-then-eval "(defn uno? [v] (= 1 (count v)))")
            (print-form-then-eval "(defn dos? [v] (= 2 (count v)))")
            (print-form-then-eval "(defn tres? [v] (= 3 (count v)))")]
           [:p "Our example data will be a series of different sized vectors nested within a parent vector."]
           [:pre (print-form-then-eval "(def data-3 ['_ [] '_ [:one] '_ [:two :three] '_ [ :four :five :six]])")]
           [:p "Our collection specification will test for the lengths of the nested vectors, interleaved with predicates that will test properties of the parent vector itself. Our length-checking predicates are contained within the collection to which they apply, in the order in which they appear in data."]
           [:pre (print-form-then-eval "(def spec-3 [[cero?] vector? [uno?] empty? [dos?] list? [tres?] map?])")]
           [:p "Speculoos validates collection predicates against their parent, so " [:code "vector?"] ", " [:code "empty?"] ", " [:code "list?"] ", and " [:code "map?"] " apply to the parent vector. The length-checking predicates apply to their nested child vectors, in order."]
           [:pre (print-form-then-eval "(validate-collection-spec data-3 spec-3)")]
           [:p "Okay. Eight predicates in the collection specification, eight validation results. That's a good sanity check. Let's inspect only the valid results."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [only-valid]])")
            [:br]
            (print-form-then-eval "(only-valid (validate-collection-spec data-3 spec-3))")]
           [:p "We see each of the nested children vectors satisfy their predicates, but more interestingly, we see how Speculoos located them. The " [:code ":path"] " key-value pair reports where Speculoos found the collection predicate within " [:code "spec-3"] ". The " [:code "ordinal-parent-path"] " indicates where Speculoos applied the predicates within " [:code "data-3"] ". For example, " [:code "[:four :five :six]"] " resides at ordinal parent path " [:code "[3]"] ", which means that it is the fourth nested collection"
            (label "zero-indexed")
            (side-note "zero-indexed" "Zero-indexed.")
            " within the parent sequence. Predicate " [:code "dos?"] " located at path " [:code "[4 0]"] " in " [:code "spec-3"] " was applied to " [:code "[:two :three]"] " the third collection of the parent sequence, and therefore an ordinal parent path " [:code "[2]"] "."]
           [:p "And now, let's inspect the invalid results."]
           [:pre (print-form-then-eval "(only-invalid (validate-collection-spec data-3 spec-3))")]
           [:p "These three predicates were interleaved among the nested child vectors, so they all apply to the parent sequence. Their ordinal parent paths are all identical, " [:code "[]"] " which points to " [:code "data-3"] "'s root. This illustrates how Speculoos applies all collection predicates to their parent collection, regardless of their position within the sequence."]
           [:p "You may find a situtation where it's most natural to specify a sequence with regular expression style, so Speculoos provides a toy utility for that job. " [:code "seq-regex"] " accepts a sequence followed by pairs of predicates and regex-like operators."]
           [:pre
            (print-form-then-eval "(require '[speculoos.utility :refer [seq-regex]])")
            [:br]
            (print-form-then-eval "(seq-regex [42 :foo :bar :baz 22/7] int? :. keyword? [1,3] ratio? :?)")]
           [:p "In this example, we asked if our sequence contains exactly one integer, followed by one to three keywords, followed by zero or one ratios. In a collection validation, " [:code "seq-regex"] " could be useful like this."]
           [:pre (print-form-then-eval "(valid-collection-spec? {:a [1 2 3 'foo 'bar \\c \\l \\o \\j \\u \\r \\e]} {:a [#(seq-regex % int? [0,3] symbol? :+ char? :*)]})")]
           [:p "The nested vector does indeed hold zero to three integers, followed by one or more symbols, followed by any number of characters."]
           [:p "Hey, maps are collections, too. Speculoos has got a story for maps that rhymes. A map specification looks mostly like the data, predicates apply to the containing parent, and any number of predicates may be applied. Let's go."]
           [:pre
            (print-form-then-eval "(def data-4 {:the-clown \"Bozo\"})")
            [:br]
            (print-form-then-eval "(def red-nose? #(contains? % :the-clown))")
            (print-form-then-eval "(def spec-4 {:foo red-nose? :whoa-nellie not-empty})")
            [:br]
            (print-form-then-eval "(validate-collection-spec data-4 spec-4)")]
           [:p "After bashing out a few scalar specifications for a map, creating a map's collection specification takes a little getting used to. During map validation, Speculoos searches for predicate located at keys " [:em "not"] " found in the data. Those predicates validate the containing map. If a map specification's key " [:em "is"] " in the data set, then Speculoos dives into the value associated with that key. In the context of a collection specification, that value must be a collection."]
           [:pre
            (print-form-then-eval "(def stop-and-smell {:yellow [\"dandelion\" \"buttercup\" \"marigold\"] :red [\"poppy\" \"rose\"] :blue [\"bluebell\"] :purple [\"violet\"]})")
            [:br]
            (print-form-then-eval "(def valid-color? #(every? #{:red :orange :yellow :green :blue :purple} (keys %)))")
            [:br]
            (print-form-then-eval "(def flowers-spec {:yellow [#(every? string? %)] :red [#(= 2 (count %))] :colors-spec valid-color?})")
            [:br]
            (print-form-then-eval "(validate-collection-spec stop-and-smell flowers-spec)")]
           [:p [:code "flowers-spec"] " contains a total of three predicates. The " [:code "valid-color?"] " predicate applies to the root " [:code "stop-and-smell"] " map because the key " [:code ":colors-spec"] " does not appear in the data. Two predicates, located at shared keys " [:code ":yellow"] " and " [:code "red"] " apply to nested vectors because those keys are shared between the data and specification, and the values at those keys are themselves collections."]
           [:p "Don't create a " [:code "does-not-contain-kw"] " or some other predicate that would restrict the keys a map " [:em "might"] " contain. It's better to gracefully ignore map elements that don't concern you. Speculoos does not have any code branches that would prohibit you from doing this, so be a good neighbor."]
           [:p "Now that we've seen an example of validating vectors nested in a map, let's compare that to validating maps nested in a vector. Let's test whether a nested map contains " [:code ":first-name"] " and " [:code ":last-name"] " keys."]
           [:pre (print-form-then-eval "(def person-spec #(and (contains? % :first-name) (contains? % :last-name)))")]
           [:p "Our data is a sequence containing two maps that describe two missions of the US Apollo moon project. We'll target the " [:code ":commander"] " entries of the two mission maps. Each valid commander map should contain both a " [:code ":first-name"] " and a " [:code ":last-name"] " entry, though as Speculoos will report in a moment, one of the entries does not."]
           [:pre (print-form-then-eval "(def apollo [{:mission 12
                                                 :CM \"Yankee Clipper\"
                                                 :LM \"Intrepid\"
                                                 :commander {:first-name \"Charles\" :last-name \"Conrad\" :nick-name \"Pete\"}
                                                 :cm-pilot {:first-name \"Richard\" :last-name \"Gordon\" :nick-name \"Dick\"}
                                                 :lm-pilot {:first-name \"Alan\" :last-name \"Bean\" :nick-name \"Al\"}}
                                                {:mission 17
                                                 :CM \"America\"
                                                 :LM \"Challenger\"
                                                 :commander {:nick-name \"Gene\" :last-name \"Cernan\"}
                                                 :cm-pilot {:nick-name \"Ron\" :last-name \"Evans\"}
                                                 :lm-pilot {:nick-name \"Harry\" :last-name \"Schmitt\"}}])")]
           [:p "The collection specification for validating the nested map entries is deceptively simple: Make the specification look " [:em "mostly"] " like the data, inserting predicates at points we want to test. So we start with a root vector to hold everything. Then, we insert two maps to correspond to the two mission maps."]
           [:pre (print-form-then-eval "(def apollo-spec [{:commander {:check-person person-spec}} {:commander {:check-person person-spec}}])")]
           [:p "We've got to get the keywords correct so that we validate what we want. First, within the mission map, we use the identical key" [:code ":commander"] " because Speculoos requires that to navigate to the correct commander map. Once Speculoos has got hold of the commander map to validate, we use a key that doesn't appear in the data, " [:code ":check-person"] " is an informative label, to indicate to Speculoos that we've arrived at the map we want to validate. In this example, " [:code "person-spec"] " will be applied to "]
           [:pre [:code "{:first-name \"Charles\" :last-name \"Conrad\" :nick-name \"Pete\"}"]]
           [:p  " and "]
           [:pre [:code "{:nick-name \"Gene\" :last-name \"Cernan\"}."]]
           [:p "And indeed, validating the data against our specification shows us that our second commander map is invalid."]
           [:pre (print-form-then-eval "(validate-collection-spec apollo apollo-spec)")]
           [:p "The second commander map does not contain a " [:code ":first-name"] " key. Note this validation would be impossible with a scalar specification because Speculoos considers presence or absence of a map's key to be a property of the collection, not of the scalar itself."]
           [:p "Let's wrap up this section on collection specification by making some cheat sheets. The guidelines for scalar specification fit on the back of a business card."]
           [:div.business-card-container
            [:div.business-card
             [:p.centered [:em "Speculoos scalar specifications"]
              [:ol
               [:li "Make the specification mimic the data's shape."]
               [:li "Put predicates in the corresponding datum spots to validate."]]]]]
           [:p "The guidelines for collection specification require a business card plus a sticky note."]
           [:div.business-card-container
            [:div.business-card
             [:p.centered [:em "Speculoos collection specifications"]]
             [:ol
              [:li "Make the specification mimic the data's shape."]
              [:li "Put predicates " [:em "inside"] " the collection to be validated."]]]]
           [:br]
           [:div.business-card-container
            [:div.business-card.sticky-note
             [:p.centered [:em "Speculoos collection" [:br] "specifications, cont."]]
             [:ol
              [:li [:em "Sequences:"] " Predicates in nested collections are applied in order of their parent collection, regardless of any intervening entities."]
              [:li [:em "Maps:"] " Predicates at keys that do not appear in the data are applied to the containing map."]
              ]]]]

          [:section#fn-in*
           [:h2 [:code "fn-in*"]]
           [:p "Speculoos' implementation and developer interface lean heavily on the concept of a " [:a {:href "#path"} "path"] ". Paths unambiguously point to datums and predicates, and thereby enable applying a predicate to that datum. Also, a path is an important component of the validation reports, giving the precise location of datums, and, when they differ in a collection specification, the predicate. Given the importance of this path concept, Speculoos provides an auxilliary set of tools to inspect, change, and remove elements in a heterogeneous, arbitrarily-nested data structure. These functions are modelled after " [:code "clojore.core/get-in"] ", " [:code "assoc-in"] ", " [:code "update-in"] ", and " [:code "dissoc"] "."
            (label "dissoc-in")
            (side-note "dissoc-in" (h2/html [:code "clojure.core"] " does " [:a {:href "https://ask.clojure.org/index.php/730/missing-dissoc-in"} "not provide"] " an equivalent " [:code "dissoc-in"] "."))
            " The starred versions of their " [:code "clojure.core"] " namesakes consume identical argument lists, but have the ability to seamlessly navigate any of the Clojure data structures (vectors, lists, maps, and sets) in any nesting pattern."]
           [:p "Here's how paths work. Vectors are indexed by zero-based integers."]
           [:pre
            [:code "           [100 101 102 103]"]
            [:code "indexes --> 0   1   2   3"]
            ]
           [:p "Same for lists."]
           [:pre
            [:code "          '(97 98 99 100)"]
            [:code "indexes --> 0  1  2  3"]]
           [:p "Maps are addressed by their keys, which are often keywords, like this"]
           [:pre
            [:code "        {:a 1 :foo \"bar\" :hello 'world}"]
            [:code "keys --> :a   :foo       :hello"]]
           [:p "but maps may be keyed by any value, including integers"]
           [:pre
            [:code "        {0 \"zero\" 1 \"one\" 99 \"ninety-nine\"}"]
            [:code "keys --> 0        1       99"]]
           [:p "or any other value"]
           [:pre
            [:code "        {\"a\" :value-at-str-key-a 'b :value-at-sym-key-b \\c :value-at-char-key-c}"]
            [:code "keys --> \"a\"                     'b                     \\c"]]
           [:p "even composite values."]
           [:pre
            [:code "        {[0] :val-at-vec-0 [1 2 3] :val-at-vec-1-2-3 {} :val-at-empty-map}"]
            [:code "keys --> [0]               [1 2 3]                   {}"]]
           [:p "Set elements are addressed by their identities, so they are located at themselves."]
           [:pre
            [:code "             #{42 :foo true 22/7}"]
            [:code "identities --> 42 :foo true 22/7"]]
           [:p "A " [:em "path"] " is a vector of indexes, keys, or identities that allow the starred functions to dive into a nested data structure, one item per level of nesting."]
           [:pre
            (print-form-then-eval "(require '[speculoos.fn-in :refer [get-in* assoc-in* update-in* dissoc-in*]])")
            [:br]
            (print-form-then-eval "(get-in* [1 2 3 [4 5 6 [7 8 9]]] [3 3 2])")]
           [:p "The operation doesn't necessarily bottom out on a scalar value,"]
           [:pre (print-form-then-eval "(get-in* [1 2 3 [4 5 6 [7 8 9]]] [3 3])")]
           [:p "but could point to a nested collection."]
           [:p "Within a group of nested vectors, all components of the path are zero-based integers. Each integer serves as an index into a single depth."]
           [:p "Nested lists are similar."]
           [:pre (print-form-then-eval "(get-in* '(1 2 (3 4 (5 6) 7) 8) [2 2 1])")]
           [:p "Within a group of nested maps, all components of the path are keys. In this example, all the keys are keywords."]
           [:pre (print-form-then-eval "(get-in* {:a 11 :b {:c 22 :d {:e 33 :f {:g 44}}}} [:b :d :f :g])")]
           [:p "Nested maps with integer keys."]
           [:pre (print-form-then-eval "(get-in* {99 \"ninety-nine\" 33 {44 \"fourty-four\" 111 {55 \"fifty-five\"}}} [33 111 55])")]
           [:p "Nested map with a path composed of a composite key " [:code "[3]"] ", a symbol " [:code "'yipee"] " key, and a boolean " [:code "true"] " key, yielding a keyword."]
           [:pre (print-form-then-eval "(get-in* {[3] {:a 44 'yipee {true :ice-cream}}} [[3] 'yipee true])")]
           [:p "Set elements are addressed by their identities."]
           [:pre (print-form-then-eval "(get-in* #{99 :foo \\z} [:foo])")]
           [:p "The empty vector addresses the top-level root collection of any collection type."]
           [:pre
            (print-form-then-eval "(get-in* [1 2 3] [])")
            (print-form-then-eval "(get-in* '(:foo \"bar\" 42) [])")
            (print-form-then-eval "(get-in* {:a 1 :b 2} [])")
            (print-form-then-eval "(get-in* #{:foo 42 \\z} [])")]

           [:p "We've just seen a truckload of examples of how we can get a value if we know its path. Also, with an element's path, we can associate a new value, swapping in an entirely new value."]
           [:pre (print-form-then-eval "(assoc-in* [42 \"abc\" :foo] [2] \\z)")]
           [:p "We can update the value, applying a function to the current value.."]
           [:pre (print-form-then-eval "(update-in* [42 \"abc\" :foo] [0] inc)")]
           [:p "We can remove a value entirely."]
           [:pre (print-form-then-eval "(dissoc-in* [42 \"abc\" :foo] [1])")]

           [:p "One of " [:code "spec.alpha"] "'s core features is returning " [:em "conformed"] " data, a version of the validated data that is annotated according to whichever optional predicate was satisfied. Speculoos does not attempt to replicate this feature. The " [:code "valid…?"] " function family returns simple " [:code "true/false"] " while the " [:code "validate…"] " function family returns reports which are strictly Clojure data."]
           [:pre (print-form-then-eval "(validate [42 \"abc\"] [int? char?] [vector?])")]
           [:p "The validation report consists of a sequence of maps, one for each predicate/datum pair that holds the datum, the predicate, the path(s) where they are located within the data and specification, and the results of the evaluation. Most importanly, the validation report can be processed with any Clojure tools. Pull out paths using regular tools. For example, we can filter the previous result to see only unsatisfied predicates."
            (label "only-invalid")
            (side-note "only-invalid" (h2/html "In fact, this is substantially how " [:code "speculoos.core/only-invalid"] " works."))]
           [:pre (print-form-then-eval "(filter #(not (:valid? %)) (validate [42 \"abc\"] [int? char?] [vector?]))")]
           [:p "We can then use " [:code "get-in*"] " to get the path."]
           [:pre (print-form-then-eval "(get-in* (filter #(not (:valid? %)) (validate [42 \"abc\"] [int? char?] [vector?])) [0 :path] )")]
           [:p "With the path to the invalid datum and predicate, we could investigate the offending data, amend the data so that it validates, alter the specification so that the predicate is satisfied, or even remove the predicate so that it is not tested. The " [:a {:href "#utility"} [:code "speculoos.utility"]] " namespace provides all those options."]]

          [:section#predicates
           [:h2 "Predicates"]
           [:p "A predicate function returns a truthy or falsey value."]
           [:pre
            (print-form-then-eval "(#(<= 5 % ) 3)")
            [:br]
            (print-form-then-eval "(#(= 3 (count %)) [1 2 3])")]
           [:p "Non-boolean returns work, too. For example, " [:a {:href "#sets"} "sets"] " make wonderful membership tests."]
           [:pre
            [:code ";; truthy"]
            (print-form-then-eval "(#{:red :orange :yellow :green :blue :purple} :green)")
            [:br]
            [:code ";; falsey"]
            (print-form-then-eval "(#{:red :orange :yellow :green :blue :purple} :swim)")]
           [:p "Regular expressions come in handy for validating string contents."]
           [:pre
            (print-form-then-eval "(re-find #\"^Four\" \"Four score and seven years ago...\")")
            [:br]
            (print-form-then-eval "(re-find #\"^Four\" \"When in the course of human events...\")")]
           [:p "Invoking a predicate when supplied with a datum — scalar or collection — is the core action of Speculoos' validation."]
           [:pre
            (print-form-then-eval "(int? 42)")
            [:br]
            (print-form-then-eval "(validate-scalar-spec [42] [int?])")]
           [:p "Speculoos is fairly ambivalent about the predicate return value. The " [:code "validate…"] " family of functions mindlessly churns through its sequence of predicate-datum pairs, evaluates them, and stuffs the results into " [:code ":valid?"] " keys. The " [:code "valid…?"] " family of functions rips through " [:em "that"] " sequence, and if none of the results are falsey, returns " [:code "true"] ", otherwise it returns " [:code "false"] "."]

           [:p "Often, we want to combine multiple predicates to make the validation more specific. We can certainly use " [:code "clojure.core/and"]]
           [:pre [:code "#(and (int? %) (pos? %) (even? %))"]]
           [:p " and " [:code "clojure.core/or"]]
           [:pre [:code "#(or (string? %) (char? %))"]]
           [:p "which have the benefit of being universally understood. But Clojure also provides a pair of nice functions that streamline the expression and convey your intention. " [:code "every-pred"] " composes an arbitrary number of predicates with " [:code "and"] " semantics."]
           [:pre (print-form-then-eval "((every-pred number? pos? even?) 100)")]
           [:p "Similarly, " [:code "some-fn"] " composes predicates with " [:code "or"] " semantics."]
           [:pre (print-form-then-eval "((some-fn number? string? boolean?) \\z)")]

           [:p "When Speculoos validates the scalars of a sequence, it consumes each element in turn. If we care only about validating some of the elements, we must include placeholders in the specification to maintain the sequence of predicates. For example, suppose we only want to validate the third element of " [:code "[42 :foo \\z]"] "; the first two elements are irrelevant to us. We have a few options. We could write our own little always-true predicate,"(label "no-%")
            (side-note "no-%" (h2/html [:code "#(true)"] " won't work because " [:code "true"] " is not invocable. " [:code "#(identity true)"] " loses the conciseness."))]
           [:pre [:code  "(fn [] true)"]]
           [:p "but Clojure already has a couple of nice options." ]
           [:pre (print-form-then-eval "(valid-scalar-spec? [42 :foo \\z] [(constantly true) (constantly true) char?])")]
           [:p [:code "constantly"] " is nice because it accepts any number of args. But for my money, nothing tops " [:code "any?"] "."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [42 :foo \\z] [any? any? char?])")]
           [:p "It's four characters, doesn't require typing parentheses, and the everyday usage of " [:em "any"] " aligns well with its techincal purpose."]

           [:p "A word of warning about " [:code "clojure.core/contains?"] ". It might seem natural to use " [:code "contains?"] " to check if a collection contains an item, but it doesn't do what its name suggests. Observe."]
           [:pre (print-form-then-eval "(contains? [97 98 99] 1)")]
           [:p [:code "contains?"] " actually tells you whether a collection contains a key. For a vector, it tests for an index. If you'd like to check whether a value is contained in a collection, you can use this pattern."
            (label "in?")
            (side-note "in?" (h2/html "Check out " [:code "speculoos.utility/in?"] "."))]
           [:pre
            (print-form-then-eval "(defn in? [coll item] (some #(= item %) coll))")
            [:br]
            (print-form-then-eval "(in? [97 98 99] 98)")
            [:br]
            (print-form-then-eval "(in? [97 98 99] 1)")]
           [:p "I've been using the " [:code "#(...)"] " form because it's compact, but it does have a drawback for use with Speculoos."]
           [:pre [:code "[{:path [0], :datum 42, :predicate #function[documentation/eval94717/fn--94718], :valid? false}]"]]
           [:p "The function rendering is not terribly informative when the validation displays the predicate. Same problem with " [:code "(fn [v] (...))"] "."]
           [:p "One solution to this issue is to define your predicates with an informative name."]
           [:pre
            (print-form-then-eval "(def greater-than-50? #(< 50 %))")
            [:br]
            (print-form-then-eval "(validate-scalar-spec [42] [greater-than-50?])")]
           [:p "Now, the predicate entry is a little nicer."]
           [:p "Regular expressions check the content of strings."]
           [:pre
            (print-form-then-eval "(def re #\"F\\dQ\\d\")")
            (print-form-then-eval "(defn re-pred [s] (re-matches re s))")
            [:br]
            (print-form-then-eval "(validate-scalar-spec [\"F1Q5\" \"F2QQ\"] [re-pred re-pred])")]
           [:p "Speculoos considers free-floating regexes in a scalar specifcation as predicates, so you can simply jam them in there."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? [\"A1B2\" \"CDEF\"] [#\"(\\w\\d){2}\" #\"\\w{4}\"])")
            [:br]
            (print-form-then-eval "(validate-scalar-spec {:a \"foo\" :b \"bar\"} {:a #\"f.\\w\" :b #\"^[abr]{0,3}$\"})")]
           [:p "Using bare regexes in your scalar specification has a nice side benefit in that the " [:code "data-from-spec"] " and " [:code "exercise-fn"] " utilities can generate valid strings."]

           [:p [:code "spec.alpha"] " makes a deliberate choice to store predicates in a dedicated " [:a {:href "https://clojure.org/guides/spec#_registry"} "registry"] ".  Speculoos takes a more " [:em "laissez-faire"] " approach: specifications may live in whatever namespace you please. If you feel that some sort of registry would be useful, you could make your own " [:a {:href "https://github.com/clojure/spec.alpha/blob/c630a0b8f1f47275e1a476dcdf77507316bad5bc/src/main/clojure/clojure/spec/alpha.clj#L52"} "modelled after "] [:code "spec.alpha"] "'s."]
           [:p "Finally, be aware that Speculoos presents a couple of situations where a predicate needs to accept more than one argument. First, when specifying a function's " [:a {:href "#fn-specs"} "arg-vs-ret"] ", the predicate must accept two inputs: the function's argument sequence and its return value. Second, when using the " [:a {:href "#path-spec"} [:code "validate-with-path-spec"]] " utility, the predicate's arguments must exactly match the number of supplied paths, which could be greater than one."]]

          [:section#utility
           [:h2 "Utility functions"]
           [:p "You won't miss any crucial piece of Speculoos' functionality if you don't use this namespace, but perhaps something here might make your day a little nicer. Nearly every function takes advantage of " [:code "speculoos.core/all-paths"] ", which decomposes a heterogeneous, arbitrarily-nested data structure into a sequence of paths and datums. With that in hand, these not-clever functions churn through the entries and give you back something useful."]
           [:pre (print-form-then-eval "(require '[speculoos.utility :refer [data-without-specs specs-without-data all-specs-okay non-predicates sore-thumb spec-from-data data-from-spec basic-collection-spec-from-data collections-without-specs exercise]])")]
           [:p "Recall that Speculoos only validates a datum and predicate at identical paths in data and specificaton, respectively. This pair of utilities tells us where we have unmatched datums or unmatched predicates."]
           [:pre
            (print-form-then-eval "(data-without-specs [42 [:foo \"abc\"]] [int?])")
            [:br]
            (print-form-then-eval "(specs-without-data [42] [int? [keyword? string?]])")]
           [:p "With this information, we can see if we were missing datums we were expecting,"
            (label "missing")
            (side-note "missing" "Or more formally, we could check for missing data by validating against a collection specification.")
            " or we could adjust our specification for better coverage."]
           [:p "It's not difficult to neglect a predicate for a nested element within a collection specification, so Speculoos offers an analogous utility to highlight that possible issue."]
           [:pre (print-form-then-eval "(collections-without-specs [11 [22 {:a 33}]] [vector? [{:is-a-map? map?}]])")]
           [:p "Yup, we didn't specify that inner vector whose first element is " [:code "22"] ". That's okay, though. Maybe we don't care to specify it. But now we're aware."
            (label "coll-preds-without")
            (side-note "coll-preds-without" (h2/html [:code "specs-without-data"] " does not have an analogous utility for collection predicates because every predicate in a specificaiton is contained within a collection and by policy always applies to its container. There can never be a collection predicate without a target collection."))]
           [:p "I can imagine passing around specifications as any other re-usable piece of data. Perhaps somewhere along the way, a wayward emacs key sequence damaged the specification. Here's a couple of functions to check the specification itself."]
           [:pre
            (print-form-then-eval "(all-specs-okay {:a int? :b [string? ] :d [[int?]]})")
            [:br]
            (print-form-then-eval "(non-predicates {:a int? :b [string? \"foo\"] :d [[99]]})")]
           [:p "In the first example, we learned that all our predicates are valid predicates, but in the second example, we see where our specification contains two non-predicates."]
           [:p "I envision that you'd be using these utility functions mainly during dev time, but I won't protest if you find them useful in production. This next utility, however, is probably only useful at the keyboard. Given data and a specification, it prints back both, but with only the invalid datums and predicates showing."]
           [:div.no-display
            (def sore-thumb-example "(sore-thumb [42 {:a true :b [22/7 :foo]} 1.23] [int? {:a boolean? :b [ratio? string?]} int?])")
            (def sore-thumb-example-eval (with-out-str (eval (read-string sore-thumb-example))))
            ;; Leave the following :pre block as is.
            ]
           [:pre
            [:code (prettyfy sore-thumb-example)]
            [:br]
            [:code ";; to *out*"]
            [:code (clojure.string/replace sore-thumb-example-eval "\"" "")]]
           [:p "I've found it handy for quickly pin-pointing the unsatisfied datum-predicate pairs in a large, deeply-nested data structure."]
           [:p "I think of the next few utilities as " [:em "creative"] ", making something that didn't previously exist. We'll start with a pair of functions which perform complimentary actions."]
           [:pre
            (print-form-then-eval "(spec-from-data [33 {:a :baz :b [1/3 false]} '(3.14 \\z)])")
            [:br]
            (print-form-then-eval "(data-from-spec {:x int? :y [ratio? boolean?] :z (list char? neg-int?)} :random)")]
           [:p "I hope their names give good indications of what they do. The generated specification contains only basic predicates,"
            (label "compound")
            (side-note "compound" (h2/html "A few " [:a {:href "#custom-generators"} "paragraphs down"] " we'll see some ways to create random sample generators for compound predicates."))
            " that is, merely " [:em "Is it an integer?"] ", not " [:em "Is it an even integer greater than 25, divisible by 3?"] ". But it's convenient raw material to start crafting a tighter specification. Oh, yeah…they both round-trip."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? (data-from-spec [int? keyword? string?]) [int? keyword? string?])")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? [42 :foo 'baz] (spec-from-data [42 :foo 'baz]))")]
           [:p "Speaking of raw material, Speculoos also has a collection specification generator."]
           [:pre (print-form-then-eval "(basic-collection-spec-from-data [55 {:q 33 :r ['foo 'bar]} '(22 44 66)])")]
           [:p "Which does not produce a specification that is immediately useful, but does provide a good starting template, because collection specifications can be tricky to get just right."]
           [:p#custom-generators "The " [:code "utility"] " namespace contains a trio of functions to assist writing compound predicates that can be used by " [:code "data-from-spec"] " to generate valid random sample data. A compound predicate such as " [:code "#(and (int? %) (< % 100))"] " does not have built-in generator provided by " [:code "clojure.test.check.generators"] ". However, " [:code "data-from-spec"] " can extract a custom generator residing in the predicate's metadata."]
           [:pre
            (print-form-then-eval "(data-from-spec [keyword? (with-meta #(and (number? %) (< % 100)) {:speculoos/predicate->generator #(rand 101)})] :random)")]
           [:p "The custom generator must be provided at the metadata key" [:code ":speculoos/predicate->generator"] ". " [:code "clojure.spec.alpha"] " automatically creates generators from its specs, but Speculoos isn't that slick. We must write them by hand." (label "ick") (side-note "ick" "Ugh.") " It can be tricky to make sure the generator produces values that precisely satisfy the predicate, so Speculoos provides a utility to check one against the other. What if we don't quite have the generator written correctly?"]
           [:pre
            (print-form-then-eval "(require '[speculoos.utility :refer [validate-predicate->generator unfindable-generators defpred]]
                                             '[clojure.test.check.generators :as gen])")
            [:br]
            [:code ";; warm up the generator for better-looking results"]
            (print-form-then-eval "(validate-predicate->generator (with-meta #(and (int? %) (even? %) (<= 50 500))
                                                                     {:speculoos/predicate->generator #(last (gen/sample (gen/such-that odd? (gen/large-integer* {:min 50 :max 500})) 22))}) 5)")]
           [:p "Oops. We paired " [:code "odd?"] " with " [:code "such-that"] " when we should have used " [:code "even?"] ". Let's fix it using another helper, " [:code "defpred"] ", that relieves us of a bit of that keyboarding."]
           [:pre
            (print-form-then-eval "(defpred fixed #(and (int? %) (even? %) (<= 50 500)) #(last (gen/sample (gen/such-that even? (gen/large-integer* {:min 50 :max 500})) 22)))")
            [:br]
            (print-form-then-eval "(validate-predicate->generator fixed 5)")]
           [:p "We can see that the generator now yields values that satisfy its predicate."]
           [:p "Perhaps we've got a specification in hand, and we'd like to know if all of the predicates have a random sample generator."]
           [:pre
            (print-form-then-eval "(def not-int? #(not (int? %)))")
            (print-form-then-eval "(def needless-str? #(string? %))")
            [:br]
            (print-form-then-eval "(unfindable-generators [int? {:a string? :b [not-int? needless-str?]}])")]
           [:p "We see the paths to two predicates that do not have a random sample generator known to Speculoos."]

           [:p "One final creative utility: " [:code "exercise"] ", which consumes a scalar specification, and generates a series of random data, then validates them."]
           [:pre (print-form-then-eval "(exercise [int? symbol? {:x boolean? :y ratio?}] 5)")]
           [:p "I'm not sure that's generally useful, but I include it to illustrate the benefits of the " [:code "all-paths"] " structure. All these utilities contain just a handful of lines because " [:code "all-paths"] " has already done the hard work, which makes me feel like Speculoos somehow aligns with Alan Perlis' " [:a {:href "https://web.archive.org/web/19990117034445/http://www-pu.informatik.uni-tuebingen.de/users/klaeren/epigrams.html"} "epigram"] " " [:a {:href "https://dl.acm.org/doi/10.1145/947955.1083808"} "#9"] "."]
           [:p "Now that we've finished the creative utilities, the next involve altering data structures and specifications. I do not know if any of these would be useful in the real world, but they were quick to write, and again illustrate the power of the " [:code "all-paths"] " core function."]
           [:p "Imagine a specification damaged by cosmic rays, and some data that we know for certain is valid. Speculoos can mangle the specification into working." (label "immutable") (side-note "immutable" (h2/html [:em "Mangle"] " only in the everyday sense. All the Clojure data structures — including Speculoos specifications — remain immutable.")) " If for some reason our specification contained an entry that was not a predicate, Speculoos could swap it out so that the specification can be fed into " [:code "validate"] "."]
           [:pre
            (print-form-then-eval "(require '[speculoos.utility :refer [swap-non-predicates nil-out bed-of-procrustes apathetic bazooka-swatting-flies smash-data]])")
            [:br]
            (print-form-then-eval "(swap-non-predicates [int? :foo string?])")]
           [:p "Similarly, if we just couldn't tolerate invalid datum-predicate pairs, we could replace each with " [:code "nil/nil?"] "."]
           [:pre (print-form-then-eval "(nil-out [42 'foo 22/7 true] [int? keyword? ratio? float?])")]
           [:p "Starting to get ridiculous, but if we wanted to completely remove invalid datum-predicate pairs, we have this…"]
           [:pre (print-form-then-eval "(bed-of-procrustes [42 {:a true :b 1.23} :foo] [int? {:a boolean? :b int?} symbol?])")]
           [:p "If you have some data you really like, but your specification, not so much, you could mangle the specification into compliance."]
           [:pre (print-form-then-eval "(apathetic [42 'foo {:x 22/7 :y \\z}] [int? keyword? {:x ratio? :y string?}])")]
           [:p "Along those same lines, if you have some good data, and you want to conform your specification to it with predicates that validate, Speculoos gives you this."]
           [:pre (print-form-then-eval "(bazooka-swatting-flies [42 'foo {:x 22/7 :y \\z}] [int? keyword? {:x ratio? :y string?}])")]
           [:p "On the other hand, if you have a specification that you like, and some data that only kinda works, Speculoos can force the square data peg into the round hole."]
           [:pre (print-form-then-eval "(smash-data [42 'foo {:x 22/7 :y \\z}] [int? keyword? {:x ratio? :y string?}])")]
           [:p "I don't advocate using any of these mangling utilities except as a means to temporarily get something done during development."]]

          [:section#fn-specs
           [:h2 "Functions specification and testing"]
           [:p "Being able to validate Clojure data enables us to inspect functions. Function arguments take the form of a sequence, which Speculoos can validate like any other heterogeneous, arbitrarily-nested data structure. Likewise, the function return is a value, which may be also validated. Furthermore, " [:code "spec.alpha"] " can verify a specified relationship between a function's arguments and its return value. Speculoos supports that idea, too.  Speculoos inspects functions at three sites: validating arguments, validating returns, and validating the relationships between arguments and returns. Within each of those three sites, Speculoos offers both scalar validation and collection validation, for a total of six possible validations. None are strictly required. Speculoos will happily validate only what you ask."]

           [:p "Speculoos function specifications " [:a {:href "https://clojure.org/about/spec#_dont_further_add_tooverload_the_reified_namespaces_of_clojure"} "differ"] " from " [:code "spec.alpha"] " in that they are stored and retrieved directly from the function's metadata. It's an experiment, but I thought it might be rather nice if I could hand you one single thing and say "]
           [:blockquote [:p  [:em "Here's a Clojure function you can use. Its name suggests what it does, its docstring that tells you how to use it, and human- and machine-readable specifications  check the validity of the inputs, and tests that it's working properly. All in one neat, tidy "] "S-expression" [:em"."]]]
           [:p "Speculoos offers three patterns of function validation."
            [:ol
             [:li "Explicit validation with a specification supplied in a separate map. The function var is not altered."]
             [:li "Explicit validation with specifications contained in the function's metadata."]
             [:li "Implicit validation with specifications contained in the function's metadata."]]]
           [:p "The first pattern is nice because you can quickly validate a function " [:em "on-the-fly"] " without messing with the function's metadata. Merely supply the function's symbol, a map of specifications, and a sequence of args."]
           [:pre
            (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-with validate-fn-meta-spec inject-specs! unject-specs! instrument unstrument exercise-fn recognized-spec-keys validate-higher-order-fn]])")
            [:br]
            (print-form-then-eval "(defn add-ten \"Returns ten more than integer i.\" [i] (+ 10 i))")
            [:br]
            (print-form-then-eval "(validate-fn-with add-ten {:speculoos/arg-scalar-spec [int?] :speculoos/ret-scalar-spec int?} 15)")]
           [:p "Since all specifications are satisfied, " [:code "add-ten"] " returns its evaluation. When one or more predicates are not satisfied, Speculoos returns a validation report."]
           [:pre (print-form-then-eval "(validate-fn-with add-ten {:speculoos/arg-scalar-spec [float?] :speculoos/ret-scalar-spec float?} 15)")]
           [:p "Speculoos steps in to tell us that neither the argument nor the return value are floating point numbers. Note that in this example, the return value is a bare scalar. When a function returns a non-collection, Speculoos handles it."]
           [:p "Speculoos consults this defined group of keys in a specfication map when it validates."]
           [:pre (print-form-then-eval "speculoos.function-specs/recognized-spec-keys")]
           [:p "Speculoos offers a pair convenience functions to add and remove specificatons from a function's metadata."]
           [:pre (print-form-then-eval "(inject-specs! add-ten {:speculoos/arg-scalar-spec [int?] :speculoos/ret-scalar-spec int?})")]
           [:p "We can observe that the specifications indeed live in the function's metadata. If we later decided to undo that, " [:code "unject-specs!"] " removes all recognized Speculoos specification entries, regardless of how they got there. For the upcoming demonstrations, though, we'll keep those specifications in " [:code "add-ten"] "'s metadata."]
           [:pre (print-form-then-eval "(select-keys (meta #'add-ten) recognized-spec-keys)")]
           [:p "Now that " [:code "add-ten"] " holds the specifications in its metadata, we can try the second pattern of explicit validation pattern. It's similar, except we don't have to supply the specification map; it's already waiting in the metadata. Invoked with a valid argument, " [:code "add-ten"] " returns a valid value."]
           [:pre (print-form-then-eval "(validate-fn-meta-spec add-ten 15)")]
           [:p "Invoking " [:code "add-ten"] " with an invalid float, Speculoos generates a report."]
           [:pre (print-form-then-eval "(validate-fn-meta-spec add-ten 1.23)")]
           [:p "Until this point in our discussion, Speculoos has only performed function validation when we explicitly call either " [:code "validate-fn-with"] " or " [:code " validate-fn-meta-spec"] ". The specifications in the metadata are passive and produce no effect, even with arguments that would otherwise fail to satisfy the specification's predicates."]
           [:pre
            (print-form-then-eval "(add-ten 15)")
            [:br]
            (print-form-then-eval "(add-ten 1.23)")]
           [:p "The arguments list to a function is exactly that: a sequence. And Speculoos can validate sequences, both its scalars and as a collection. Let's see a multi-argument function that returns a collection. We create a named collection specification, then define our silly function."]
           [:pre
            (print-form-then-eval "(defn length-3? [v] (= 3 (count v)))")
            (print-form-then-eval "(defn silly \"A contrived demo.\" [x s r] (vector (/ r 2) (inc x) (apply str (reverse (.toString s)))))")]
           [:p "Next, we inject our specifications into the function's metadata: a scalar specification and collection specication, each, for both the argument sequence and the return sequence. A grand total of four specifications."]
           [:pre (print-form-then-eval "(inject-specs! silly {:speculoos/arg-scalar-spec [int? string? ratio?] :speculoos/arg-collection-spec [length-3?] :speculoos/ret-scalar-spec [ratio? int? string?] :speculoos/ret-collection-spec [length-3?]})")]
           [:p "Valid inputs…"]
           [:pre (print-form-then-eval "(validate-fn-meta-spec silly 42 \"abc\" 1/3)")]
           [:p "…produce valid returns. But invalid arguments…"]
           [:pre (print-form-then-eval "(validate-fn-meta-spec silly 42 \\a 9)")]
           [:p "…yields an invalidation report: two of our arguments do not satisfy their scalar predicates."]
           [:p "Speculoos' third pattern of function validation " [:em "instruments"] " the function using the metadata specifications."
            (label "fagile")
            (side-note "fragile" (h2/html [:code "intrument"] "-style function validation is very much a work in progress. The implementation is sensitive to invocation order and can choke on multiple calls. The var mutation is not robust. Beware.")) " Every invocation of the function itself automatically validates any specified arguments and return values."]
           #_ [:div.no-display (instrument silly)]
           [:pre
            [:code "(instrument silly)"]
            [:br]
            (print-form-then-eval "(silly 42 \"abc\" 1/3)")
            [:br]
            (print-form-then-eval "(silly 42 \\a 9)")
            [:br]
            [:code "(with-out-str (silly 42 \\a 9))"
             [:br]
             *eval-separator*
             "({:path [1], :datum a, :predicate #function[clojure.core/string?--5475], :valid? false}\n"
             *eval-separator*
             " {:path [2], :datum 9, :predicate #function[clojure.core/ratio?], :valid? false})"]]
           [:p "The function returns if it doesn't throw an exception. Any non-satisfied predicates are reported to " [:code "*out*"] ". When we are done, we can " [:em "unstrument"] " the function, and Speculoos will no longer intervene."]
           #_ [:div.no-display (unstrument silly)]
           [:pre
            [:code "(unstrument silly)"]
            [:br]
            (print-form-then-eval "(silly 42 \\a 9)")]
           [:p "Even though character " [:code "\\a"] " and non-ratio " [:code "9"] " do not satisfy the specifications, " [:code "silly"] " is no longer instrumented, and Speculoos is not intercepting its invocation, so the function returns a value."
            (label "preference")
            (side-note "preference " (h2/html  "Frankly, I wrote " [:code "instrument/unstrument"] " to mimic the features " [:code "spec.alpha"] " offers. My implementation is squirrelly, and I don't like mutating vars. I lean much more towards the deterministic " [:code "validate-fn-meta-spec"] " and " [:code "validate-fn-with"] "."))]
           [:p "Beyond validating a function's argument sequence and its return, Speculoos can perform a validation that checks the relationship between any aspect of the arguments and return values. When the arguments sequence and return sequence share a high degree of shape, an " [:em "argument versus return scalar specification"] " will work well. A good example of this is using " [:code "map"] " to transform a sequence of values. Each item in the return sequence has a corresponsing item in the argument sequence."]
           [:pre
            (print-form-then-eval "(defn mult-ten [& args] (map #(* 10 %) args))")
            [:br]
            (print-form-then-eval "(mult-ten 1 2 3)")]
           [:p "The predicates in a " [:em "versus"] " specification are a little bit unusual. Each predicate accepts two arguments: the first is the element from the argument sequence, the second is the corresponding element from the return sequence."]
           [:pre (print-form-then-eval "(defn ten-times? [a r] (= (* 10 a) r))")]
           [:p "Let's make a bogus predicate that we know will fail, just so we can see what happens when a relationship is not satisfied."]
           [:pre (print-form-then-eval "(defn nine-times? [a r] (= (* 9 a) r))")]
           [:p "And stuff them into a map whose keys Speculoos recognizes."]
           [:pre (print-form-then-eval "(def mult-ten-vs-spec {:speculoos/arg-vs-ret-scalar-spec [ten-times? nine-times? ten-times?]})")]
           [:p "Now that we've prepared our predicates and composed a versus specification, we can validate the relationship between the arguments and the returns."]
           [:pre (print-form-then-eval "(validate-fn-with mult-ten mult-ten-vs-spec 1 2 3)")]
           [:p "We intentionally constructed our specification to fail at the middle element, and sure enough, the validation report tells us the argument and return scalars do not share the declared relationship."]
           [:p "To complete the " [:em "scalars/collections/arguments/returns/versus"] " feature matrix, Speculoos can also validate function argument collections against return collections. All of the previous discussion holds, with the twist that the specification predicates apply against the argument collections and return collections. Examples show better than words."]
           [:pre
            (print-form-then-eval "(defn goofy-reverse [& args] (reverse args))")
            [:br]
            (print-form-then-eval "(goofy-reverse 1 2 3)")]
           [:p " Speculoos passes through the function's return if all predicates are satisfied, so we'll intentionally bungle one of the predicates to cause an invalidation report."]
           [:pre
            (print-form-then-eval "(defn equal-lengths? \"Buggy!\" [v1 v2] (= (+ 1 (count v1)) (count v2)))")
            [:br]
            (print-form-then-eval "(defn mirror-elements-equal? [v1 v2] (= (first v1) (last v2)))")]
           [:p "Composing our args versus return collection specification, using the properly psuedo-qualified key."]
           [:pre (print-form-then-eval "(def rev-coll-spec {:speculoos/arg-vs-ret-collection-spec [equal-lengths? mirror-elements-equal?]})")]
           [:p "Our goofy reverse function fails our buggy argument versus return validation."]
           [:pre (print-form-then-eval "(validate-fn-with goofy-reverse rev-coll-spec 1 2 3)")]
           [:p [:code "goofy-reverse"] " behaves exactly as it should, but for illustration purposes, we applied a buggy collection specification that we knew would fail. The validation report shows us the two things it compared, in this instance, the argument sequence and the returned, reversed sequence, and furthermore, that those two collections failed to satisfy the buggy predicate, " [:code "equal-lengths?"] ". The other predicate, " [:code "mirror-elements-equal?"] " was satisfied because the first element of the argument collection is equal to the last element of the return collection, and was therefore not included in the report."
            [:p "Speculoos has a story about validating higher-order functions, too. It uses very similar patterns to regular function validation: put some specifications in the function's metadata with the properly qualified keys, then invoke the function with some sample arguments, then Speculoos will validate the results. Here's how it works. A flourish of the classic adder " [:span.small-caps "hof"] "."]]
           [:pre
            (print-form-then-eval "(defn addder [x] (fn [y] (fn [z] (+ x (* y z)))))")
            [:br]
            (print-form-then-eval "(((addder 3) 2) 10)")]
           [:p [:code "addder"] " returns a function upon it first two invocations, and only on its third does it return a scalar. Specifying and validating a function value does not convey much meaning,"
            (label "fn-value?")
            (side-note "fn-value?" (h2/html "It would merely satisfy " [:code "fn?"] " which isn't very interesting."))
            " so to validate a " [:span.small-caps "hof"] ", Speculoos requires it to be invoked until it produces a value. So we'll supply the validator a series of " [:code "&-args"] " that, when fed in order to the " [:span.small-caps "hof"] ", will produce a result. For the example above, it will look like " [:code "[3] [2] [10]"]  "."]
           [:p "The last task we must do is create the specification. " [:span.small-caps "hof"] " specifications live in the function's metadata at key " [:code ":speculoos/hof-specs"] " which is a series of nested specification maps, one nesting for each returned function. For this example, we might create this " [:span.small-caps "hof"] " specification."]
           [:pre (print-form-then-eval "(def addder-spec {:speculoos/arg-scalar-spec [even?]
                                                    :speculoos/hof-specs {:speculoos/arg-scalar-spec [ratio?]
                                                                          :speculoos/hof-specs {:speculoos/arg-scalar-spec [float?]}}})")]
           [:p "Once again, for illustration purposes, we've crafted predicates that we know will invalidate, but will permit the function stack to evaluate to completion."
            (label "halt")
            (side-note "halt" "Validation halts on exceptions.")]
           [:p [:span.small-caps "hof"] " validation requires that the metadata hold the specifications. So we inject them."]
           [:pre (print-form-then-eval "(inject-specs! addder addder-spec)")]
           [:p "And finally execute the validation."]
           [:pre (print-form-then-eval "(validate-higher-order-fn addder [3] [5] [10])")]
           [:p "Let's step through the validation results. Speculoos validates " [:code "3"] " against predicate " [:code "even?"] " and then invokes "[:code "addder"] " with argument " [:code "3"] ". It then validates " [:code "5"] " against predicate " [:code "ratio?"] " and then invokes the returned function with argument " [:code "5"] ". Finally, Speculoos validates " [:code "10"] " against predicate " [:code "float?"] " and invokes the previously returned function with argument " [:code "10"] ". If all the predicates were satisfied, Speculoos would yield the return value of the function call. In this case, all three arguments are invalid, and Speculoos yields a validation report."]
           [:p "Another tool Speculoos offers in this category: exercising specified functions. If you have injected argument scalar specifications into your function, Speculoos can generate a series of specification-satisfying arguments and invoke your function. Let's take advantage of the nice feature of " [:code "defn"] " that adds metadata during function definition."]
           [:pre
            (print-form-then-eval "(defn bottles {:speculoos/arg-scalar-spec [pos-int? string?]} [n liquid] (str n \" bottles of \" liquid \" on the wall, \" n \" bottles of \" liquid \"...\"))")
            [:br]
            (print-form-then-eval "(bottles 99 \"espresso\")")]
           [:p "Now, Speculoos can exercise our function."]
           [:pre (print-form-then-eval "(exercise-fn bottles 5)")]
           [:p "Not exactly thirst-quenching."]
           [:p "Finally, Speculoos can validate the macroexpansion of a macro against a scalar specification. Let's write a dinky example macro."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [validate-macro-with valid-macro-spec?]])")
            [:br]
            (print-form-then-eval "(defmacro dinky-macro [f x] `(~f ~@x))")
            [:br]
            (print-form-then-eval "(macroexpand-1 '(dinky-macro + [1 2 3]))")]
           [:p "We must remember that things that look like functions in a macroexpansion are actually symbols. Let's write a scalar specification, then validate the macroexpansion."]
           [:pre
            (print-form-then-eval "(def dinky-specification (list symbol? int? int? int?))")
            [:br]
            [:code
             "(validate-macro-with '(dinky-macro + [1 2 3]) dinky-specification)
;; => [{:path [0], :datum +, :predicate clojure.core/symbol?, :valid? true}
;;     {:path [1], :datum 1, :predicate clojure.core/int?, :valid? true}
;;     {:path [2], :datum 2, :predicate clojure.core/int?, :valid? true}
;;     {:path [3], :datum 3, :predicate clojure.core/int?, :valid? true}]"]]
           [:p "Or, more briefly."]
           [:pre (print-form-then-eval "(valid-macro-spec? '(dinky-macro + [1 2 3]) dinky-specification)")]
           [:div.no-display "Macro validation is included in the " [:code "core"] " namespace and not the " [:code "function-specs"] " namespace because Speculoos validates macroexpansion on a structural basis, not it arguments nor returns."]
           ]

          [:section#non-terminating
           [:h2 "Non-terminating sequences"]
           [:p "Speculoos absorbs lots of power from Clojure's infinite, lazy sequences."
            (label "inifinite")
            (side-note "infinite" (h2/html [:em "(possibly) non-terminating sequences"] " might be more accurate."))
            " That power stems from the fact that Speculoos only validates complete pairs of datums and predicates. Datums without predicates are not validated, and predicates without datums are ignored. That policy provides optionality in your data. If a datum is present, it is validated against its corresponding predicate, but if that datum is non-existent, it is not required."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? [42] [int? keyword? char?])")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? [42 :foo \\z] [int?])")]
           [:p "In the first example, only the single integer is validated, the rest of the predicates are ignored. In the second example, only the first integer was validated because the specification implies that any trailing elements are un-specified. We can take advantage of this fact by intentionally making either the data or the specification " [:em "run off the end"] "."]
           [:p "First, if you'd like to validate a non-terminating sequence, specify as many datums as necessary to capture the pattern. " [:code "repeat"] " produces mulitiple instances of a single value, so we only need to specify one datum."]
           [:pre (print-form-then-eval "(valid-scalar-spec? (repeat 3) [int?])")]
           [:p [:code "cycle"] " can produce different values, so we ought to test for as many as appear in the definition."]
           [:pre (print-form-then-eval "(valid-scalar-spec? (cycle [42 :foo 22/7]) [int? keyword? ratio?])")]
           [:p "Three unique datums. Only three predicates needed."]
           [:p "On the other side of the coin, non-terminating sequences serve a critical role in composing Speculoos spefications. They express " [:em "I don't know how many items there are in this sequence, but they all must satisfy these predicates"] "."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? [1] (repeat int?))")
            (print-form-then-eval "(valid-scalar-spec? [1 2] (repeat int?))")
            (print-form-then-eval "(valid-scalar-spec? [1 2 3] (repeat int?))")
            (print-form-then-eval "(valid-scalar-spec? [1 2 3 4] (repeat int?))")
            (print-form-then-eval "(valid-scalar-spec? [1 2 3 4 5] (repeat int?))")]
           [:p "Basically, it serves the role of a regular experssion " [:code "zero-or-more"] ". Let's pretend we'd like to validate an integer, then a string, followed by any number of characters. We compose our specification like this."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? [99 \"abc\" \\x \\y \\z] (concat [int? string?] (repeat char?)))")
            [:br]
            (print-form-then-eval "(only-invalid (validate-scalar-spec [99 \"abc\" \\x \"y\" \\z] (concat [int? string?] (repeat char?))))")]
           [:p "Or perhaps we'd like to validate a function's argument list composed of a ratio followed by " [:code "&-args"] " consisting of any number of alternating keyword-string pairs."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? [2/3] (concat [ratio?] (cycle [keyword string?])))")
            [:br]
            (print-form-then-eval "(valid-scalar-spec? [2/3 :opt1 \"abc\" :opt2 \"xyz\"] (concat [ratio?] (cycle [keyword string?])))")
            [:br]
            (print-form-then-eval "(only-invalid (validate-scalar-spec [2/3 :opt1 'foo] (concat [ratio?] (cycle [keyword string?]))))")]
           [:p "Using non-terminating sequences this way sorta replicates " [:code "spec.alpha"] "'s sequence regexes. I think of it as Speculoos' super-power."]
           [:p "Also, Speculoos can handle nested, non-terminating sequences."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [[1] [2 \"2\"] [3 \"3\" :3]] (repeat (cycle [int? string? keyword?])))")]
           [:p "One implementation detail: A non-terminating sequence must not appear at the same path within both the data and specification. I am not aware of any method to inspect a sequence to determine if it is inifinite, so Speculoos will refuse to validate a non-terminating data sequence at the same path as a non-terminating predicate sequence, and " [:em "vice versa"] ". However, feel free to use them in either data or in the specification, as long as they live at different paths."]
           [:pre
            (print-form-then-eval "(valid-scalar-spec? {:a (repeat 42) :b [22/7 true]} {:a [int?] :b (cycle [ratio? boolean?])})")
            [:br]
            (print-form-then-eval "(only-invalid (validate-scalar-spec {:a (repeat 42) :b [22/7 true]} {:a [int? int? string?] :b (repeat ratio?)}))")]
           [:p "Here, the data contains a non-terminating sequence at key " [:code ":a"] ", while the specification contains a non-terminating sequence at key " [:code ":b"] ". Since the two do not share a path, validation can proceed to completion."]
           [:p "So what's going on? Internally, Speculoos finds all the potentially non-terminating sequences in both the data and the specification. For each of those hits, Speculoos looks into the other nested structure to determine how long the counterpart sequence is. Speculoos then " [:em "clamps"] " the non-terminating sequence to that length. Validation proceeds with the clamped sequences. Let's see the clamping in action."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [expand-and-clamp-1]])")
            [:br]
            (print-form-then-eval "(expand-and-clamp-1 (range) [int? int? int?])")]
           [:p [:code "range"] " would have continued merrily on forever, but the clamp truncated it at three elements, the length of the second argument vector. That's why two non-terminating sequences at the same path are not permitted. Speculoos has no way of knowing how short or long the sequences ought to be, so instead of making a bad guess, it throws the issue back to you."]
           [:p "Speculoos' " [:a {:href "#utility"} [:code "utility"]] " namespace provides a " [:code "clamp-in*"] " tool for you to clamp any sequence within a homogeneous, arbitrarily-nested data structure. You invoke it with the same pattern of arguments as " [:code "assoc-in*"] " which, in turn, mimics " [:code "clojure.core/assoc-in"] "."]
           [:pre
            (print-form-then-eval "(require '[speculoos.utility :refer [clamp-in*]])")
            [:br]
            (print-form-then-eval "(clamp-in* {:a 42 :b ['foo 22/7 {:c (cycle [3 2 1])}]} [:b 2 :c] 5)")]
           [:p [:code "clamp-in*"] " used the path " [:code "[:b 2 :c]"] " to locate the non-terminating sequence, clamped it to five elements, and returned the new data structure with that terminating sequence. This way, if Speculoos squawks at you, you have a way to clamp the data, specification, or both at any path, and validation can proceed."]
           [:p "Be sure to set your development environment's printing length"]
           [:pre (print-form-then-eval "(set! *print-length* 99)")]
           [:p "or you may jam up your session."]]

          [:section#sets
           [:h2 "Sets"]
           [:p "Sets are…special. They enable some nice features, but they present some unique challenges compared to the other Clojure collections. " [:em "The elements in a set are addressed by their identities."] " What does that even mean? Let's compare to Clojure's other collections to get some context."]
           [:p"The elements of a sequence are addressed by monotonically increasing integer indexes. Give a vector index " [:code "2"] " and it'll give you back the third element, if it exists."]
           [:pre (print-form-then-eval "([11 22 33] 2)")]
           [:p "The elements of a map are addressed by its keys. Give a map a key " [:code ":howdy"] " and it'll give you back the value at that key, if it exists."]
           [:pre (print-form-then-eval "({:howdy \"bonjour\" :hey \"salut\"} :howdy)")]
           [:p "Give a set some value, and it will give you back that value…"]
           [:pre (print-form-then-eval "(#{:thumb :index :middle :ring :pinky} :thumb)")]
           [:p "…but only if that element exists in the set."]
           [:pre (print-form-then-eval "(#{:thumb :index :middle :ring :pinky} :bird)")]
           [:p "So the " [:a {:href "#path"} "paths"] " to elements of vectors, lists, and maps are composed of indexes or keys. The paths to members of a set are the thing themselves. Let's take a look at a couple of examples."]
           [:pre (print-form-then-eval "(all-paths #{:foo 42 \"abc\"})")]
           [:p "In the first example, the root element, a set, has a path " [:code "[]"] ". The remaining three elements, direct descendants of the root set have paths that consist of themselves. We find " [:code "42"] " at path " [:code "[42]"] " and so on. The second example applies the principle further."]
           [:pre (print-form-then-eval "(all-paths #{11 {:a [22 #{33}]}})")]
           [:p "How would we navidgate to that " [:code "33"] "? Again the root element set has a path " [:code "[]"] ". There are two direct descendants of the root set: " [:code "11"] " and a map. We've already seen that the integer's path is the value of the integer. The path to the map is the map itself, which appears as the first element of its path. That path may look unusual, but Speculoos " [:a {:href "#fn-in*"} "starred functions"] " take it without skipping a beat."]
           [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]}])")]
           [:p  "The map has one " [:code "MapEntry"] ", key " [:code ":a"] ", with an associated value, a two-element vector " [:code "[22 #{33}]"] ". A map value is addressed by its key, so the vector's path contains that key. Its path is that of its parent, with its key appended."]
           [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a])")]
           [:p "Paths into a vector are old hat by now. Our " [:code "33"] " is in a set at the second position, index " [:code "1"] " in zero-based land, which we append to the path."]
           [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a 1])")]
           [:p "We've now arrived at the little nested set which holds our " [:code "33"] ". Items in a set are addressed by their identity, and the identity of " [:code "33"] " is " [:code "33"] ". So we append that to the path so far."]
           [:pre (print-form-then-eval "(get-in* #{11 {:a [22 #{33}]}} [{:a [22 #{33}]} :a 1 33])")]
           [:p "And now we've got our goal. Following this algrorithm, we can get, change, and delete any element of any heterogeneous, arbitrarily-nested data structure, and that includes sets at any level of nesting. We could even make a path to a set, nested within a set, nested within a set, but it's not pretty. Best to let Speculoos handle it."]
           [:p "Speculoos can validate scalars within a set during a scalar validation operation. Validating a set's scalar members follows all the same principles as validating a vector's scalar members, except for one wrinkle: Since elements of a set have no inherent location" (label "unordered") (side-note "unordered" "I.e., sets are unordered.") " sets in our data are validated against all predicates contained in the corresponding set at the same path in the specification. An example shows this better than words."]
           [:pre (print-form-then-eval "(valid-scalar-spec? [42 #{:glass :rubber :paper}] [int? #{keyword?}])")]
           [:p "Speculoos validates " [:code "42"] " against predicate " [:code "int?"] " because they share paths in their respective vectors. At vector index " [:code "1"] " our data and specificaton both hold sets, so Speculoos enters " [:em "set-scalar-validation-mode"] ". Every predicate contained in the specification set is applied to every datum in the data's set. In this example, " [:code "keyword?"] " is individually applied to " [:code ":glass"] ", " [:code ":rubber"] ", and " [:code ":paper"] ", and since each satisfy the predicate, the validation returns " [:code "true"] "."]
           [:p "If our specification set contains more than one predicate, each of the predicates is applied to all the scalars in the data's set. In the next example, the specification set contains two predicates."]
           [:pre (print-form-then-eval "(validate-scalar-spec #{:chocolate :vanilla :strawberry} #{keyword? qualified-keyword?})")]
           [:p "Validaton applies " [:code "keyword?"] " and " [:code "qualified-keyword?"] ", in turn, to every scalar member of the data set. Speculoos tells us that all the scalars in the data are indeed keywords, but at least one of the data's scalars is not a qualified keyword. Notice how Speculoos condenses the validaton results. Instead of a validation entry for each individual scalar in the data set, Speculoos combines all the results for all the scalars. Two predicates, two validation results."]
           [:p  "Clojure sets can serve as membership tests."]
           [:pre
            (print-form-then-eval "(def self? #{:me :myself :I})")
            [:br]
            (print-form-then-eval "(ifn? self?)")
            [:br]
            (print-form-then-eval "(self? :me)")
            (print-form-then-eval "(self? :you)")]
           [:p [:code "self?"] " implements " [:code "IFn"] " and thus behaves like a predicate when invoked as a function. " [:code ":me"] " satisfies our " [:code "self?"]  " predicate, whereas " [:code ":you"] " does not."]
           [:p "When a scalar in our data set shares a path with a set in the specification, Speculoos enters " [:em "set-as-a-predicate"] " mode."]
           [:pre
            (print-form-then-eval "(def geography #{:mountain :plateau :valley})")
            [:br]
            (print-form-then-eval "(validate-scalar-spec [:valley {:a :mountain :b :river}] [geography {:a geography :b geography}])")]
           [:p "Our example data contains three datums: " [:code ":valley"] " in the root vector, " [:code ":mountain"] " and " [:code ":river"] "  in the nested map. Each of those datums shares a path with a set-as-a-predicate in the specification. When Speculoos validates, it treats each set in the specificaton as a predicate because the corresponding element in the data is a scalar, not a set. In this example, " [:code ":valley"] " and " [:code ":mountain"] " are members of the " [:code "geography"] " set-predicate, whereas " [:code ":river"] " is not."]
           [:p "Speculoos performs the two modes in separate passes, so we may even use both set-as-a-predicate-mode and set-scalar-validation-mode during the same validation, as long as the predicates stay on their own side of the fence."]
           [:pre (print-form-then-eval "(validate-scalar-spec [42 #{:foo :bar :baz}] [#{40 41 42} #{keyword?}])")]
           [:p "In this example, the predicate at index " [:code "0"] " of the specification is a set while the datum at same index of the data is " [:code "42"] ", a scalar. Speculoos uses the set-as-a-predicate mode. Since " [:code "42"] " is a member of " [:code "#{40 41 42}"] ", that datum validates as truthy. Because the data at index " [:code "1"] " is itself a set, Speculoos performs set-scalar-validation. The " [:code "keyword?"] " predicate is applied to each element of " [:code "#{:foo :bar :baz}"] " at index " [:code "1"] " and they all validate " [:code "true"] "."]
           [:p " Speculoos will also validate a set as whole during a collection validation operation. The rules are identical to how the other collections are validated: predicates from the specification are applied to the parent container in the data. Nested collections are validated by ordinal, regardless of intervening predicates in the parent collection. But let's not get bogged down in a textual description; let's look at some examples."]
           [:p "The simplest case involvles validating a set as a collection. In Speculoos's collection validation, the predicates apply to collections, not scalars."]
           [:pre
            (print-form-then-eval "(def count-3? #(= 3 (count %)))")
            [:br]
            (print-form-then-eval "(validate-collection-spec #{:foo :bar :baz} #{count-3?})")]
           [:p "Our data is a set containing a trio of keywords. Instead of a scalar specification applied to each of the scalars, our predicate " [:code "count-3?"] " applies to the containing collection " [:code "#{:foo :bar :baz}"] "."]
           [:p "Another principle of collection validation is that Speculoos will apply any number of collection predicates to its parent container. We'll illustrate this by creating three predicates that cannot by tested by validating scalars alone; the collection as a whole must be validated."]
           [:pre
            (print-form-then-eval "(def equal-kw-sym? #(= (count (filter symbol? %))
                                                          (count (filter keyword? %))))")
            [:br]
            (print-form-then-eval "(def more-kw? #(< (count (filter symbol? %))
                                                     (count (filter (complement symbol?) %))))")
            [:br]
            (print-form-then-eval "(def zero-int? #(empty? (filter int? %)))")
            [:br]
            (print-form-then-eval "(validate-collection-spec #{:foo :bar :baz 'foo 'bar} #{equal-kw-sym? more-kw? zero-int?})")]
           [:p "Speculoos applies all three collection predicates to the parent set. The set does indeed contain more keywords than non-keywords, the set does not contain any integers, but the set does not hold equal numbers of keywords and symbols."]
           [:p "Nested sets are validated according to Speculoos' conventions for all collections: predicates apply to their parents, and predicates may be interleaved among nested collections. Example time. Lots of collection predicates."]
           [:pre
            (print-form-then-eval "(def count-5? #(= 5 (count %)))")
            (print-form-then-eval "(def last-is-55? #(= 55 (last %)))")
            (print-form-then-eval "(def middle-is-33? #(= 33 (get % 2)))")
            (print-form-then-eval "(def all-kw? #(every? keyword? %))")
            (print-form-then-eval "(def all-qual-kw? #(every? qualified-keyword? %))")
            (print-form-then-eval "(def all-sym? #(every? symbol? %))")]
           [:p "Our parent container will be a vector with integers in the first, third, and fifth slots, with two sets nested at the second and fourth spots. The first nested set contains three keywords, the second nested set contains three symbols. Our specification is a vector with collection predicates at the first, second, and fifth spots and sets nested at the third and fourth spots."]
           [:pre (print-form-then-eval "(validate-collection-spec [42 #{:foo :bar :baz} 33 #{'foo 'bar 'baz} 55] [count-5? last-is-55? #{all-kw? all-qual-kw?} #{all-sym?} middle-is-33?])")]
           [:p "The three top-level predicates " [:code "count-5?"] ", " [:code "last-is-55?"] ", and " [:code "middle-is-33?"] " all apply to the root vector, regardless of their positions within the root vector. Two predicates contained in the first nested set, " [:code "all-kw?"] " and " [:code "all-qual-kw?"] ", apply to the first nested set " [:code "#{:foo :bar :baz}"] ", regardless of their index within the root vector. Likewise, the single predicate in the second set, " [:code "all-sym?"] ", applies to the second set " [:code "#{'foo 'bar 'baz}"] " in the data."]
           [:p "The order of collection predicates does not matter. Speculoos applies them all to the parent container. All the following produce functionally equivalent results."]
           [:pre
            [:code "["[:strong "count-5? last-is-55?"] " #{all-kw? all-qual-kw?}" [:strong " middle-is-33?"] " #{all-sym?}]"]
            [:code "[#{all-kw? all-qual-kw?} " [:strong "count-5? last-is-55? middle-is-33?"] " #{all-sym?}]"]
            [:code "[#{all-kw? all-qual-kw?} #{all-sym?} " [:strong "count-5? last-is-55? middle-is-33?"] "]"]]
           [:p "Just like elements contained in a Clojure set, the validation results are the same, merely the ordering is different."]]

          [:section#path-spec
           [:h2 "Un digestif"]
           [:p "Speculoos' performs scalar and collection validation by inferring the paths from the specification's structure: the predicate located at path " [:code "[2 :a 5]"] " in the specification will be validated against datum located at path" (label "or-coll") (side-note "or-coll" "Or, in a collection validation, its parent.") [:code " [2 :a 5]"] " within the data structure. Writing a specification involves composing a data structure that mimics the data so that the paths of the predicates correspond to the paths of the datums."]
           [:p "One alternative way to assign predicates to datums in a heterogeneous, arbitrarily-nested data structure would be to explicitly state the datum paths. In this scenario, we supply the validator an explicit path into the data for each predicate. Speculoos provides a utility to do this. It's somewhat tedious to use, but it is quite powerful. Let's pretend we'd like to specify the third item of a vector as an integer."]
           [:pre
            (print-form-then-eval "(require '[speculoos.core :refer [validate-with-path-spec]])")
            [:br]
            (print-form-then-eval "(validate-with-path-spec [11 22 33] [{:paths [[2]] :predicate int?}])")]
           [:p "Basically, we manually do the first half of Speculoos' validation. We feed the validator the path to the datum instead of asking it to infer it from the structure of the specification. In this example, the datum " [:code "33"] " is located at path " [:code "[2]"] ". " [:code "validate-with-path-spec"] " iterates through a sequence of maps, each map contains paths to datums, and predicates to apply. This utility offers great flexibility because in any validation, it will consume any number of datums and send it to a high-arity predicate. Let's see that in action."]
           [:pre
            (print-form-then-eval "(defn all-increasing? [& args] (apply < args))")
            [:br]
            (print-form-then-eval "(validate-with-path-spec [11 22 33] [{:paths [[0] [1] [2]] :predicate all-increasing?}])")]
           [:p "We establish a predicate that tests if a group of numbers is monontonically increasing. " [:code "validate-with-path-spec"] " extracts the three datums with the three paths contained at " [:code ":paths"] ". With those datums in hand, it invokes the predicate with a " [:code "(apply predicate args)"] " pattern. The datums are fed in the order of the " [:code ":paths"] " sequence. Let's feed them in reverse."]
           [:pre
            (print-form-then-eval "(defn all-decreasing? [& args] (apply > args))")
            [:br]
            (print-form-then-eval "(validate-with-path-spec [11 22 33] [{:paths [[2] [1] [0]] :predicate all-decreasing?}])")]
           [:p "And indeed, provided in reverse order, the integers are monotonically decreasing. Let's really push it with a five-arity predicate."
            (label "crazy")
            (side-note "crazy" (h2/html "Admittedly, " [:code "crazy-predicate"] " would never be useful in an actual validation, but it does highlight how to use " [:code "validate-with-path-spec"] ". "))]
           [:pre
            (print-form-then-eval "(defn crazy-predicate [w x y z coll] (and (= w (count coll))
                                                                             (= y (get coll x))
                                                                             (= z (last coll))))")
            [:br]
            (print-form-then-eval "(def crazy-data {:a 3 :b [55 77 99] :c [77 1 [99]]})")
            [:br]
            (print-form-then-eval "(get-in* crazy-data [:a])")
            (print-form-then-eval "(get-in* crazy-data [:c 1])")
            (print-form-then-eval "(get-in* crazy-data [:c 0])")
            (print-form-then-eval "(get-in* crazy-data [:c 2 0])")
            (print-form-then-eval "(get-in* crazy-data [:b])")
            [:br]
            (print-form-then-eval "(= 3 (count [55 77 99]))")
            (print-form-then-eval "(= 77 (get [55 77 99] 1))")
            (print-form-then-eval "(= 99 (last [55 77 99]))")
            [:br]
            (print-form-then-eval "(validate-with-path-spec crazy-data [{:paths [[:a] [:c 1] [:c 0] [:c 2 0] [:b]] :predicate crazy-predicate}])")]
           [:p  [:code "crazy-predicate"] " receives five arguments: four scalars, and one collection, and performs three comparisons on the collection involving its length and the values at couple of indexes. We've sprinkled throughout " [:code "carzy-data"] " the arguments which " [:code "validate-with-path-spec"] " will pass to " [:code "crazy-predicate"] ". Referring to " [:code "crazy-predicate"] "'s argument list, we find the vector length " [:code "w"] " at path " [:code "[:a]"] ", the vector's last value " [:code "z"] " at " [:code "[:c 2 0]"] ", etc. " [:code "crazy-predicate"] " calculates that " [:code "[55 77 99]"] " contains three elements, it finds " [:code "77"] " at index " [:code "1"] ", and " [:code "99"] " is indeed the last datum, so " [:code "valid?"] " is " [:code "true"] "."]
           [:p "To validate more, supply more specification entries."]
           [:pre
            (print-form-then-eval "(validate-with-path-spec [11 22 33] [{:paths [[1]] :predicate int?}
                                                                             {:paths [[2]] :predicate ratio?}
                                                                             {:paths [[]] :predicate vector?}])")]
           [:p [:code "validate-with-path-spec"] " dutifully steps through each " [:code ":paths/:predicate"] " map of the specification sequence and applies each predicate to the datums it pulls out."]
           [:p [:code "validate-with-path-spec"] " provides us with the flexibility of validating any number of scalar and/or collection datums with multi-arity predicates. Writing its specifications, however, is not compact nor expressive. I am still considering how we might improve that."]]


          [:section#glossary
           [:h2 "Glossary"]
           [:dl
            [:dt#datums "datums"]
            [:dd "Usually, the plural of "
             [:em "datum"]
             " would be "
             [:em "data"]
             ", but there are scenarios when " [:em "data"] " implies too much. A "
             [:code ":name"]
             ", an "
             [:code ":email"]
             ", and a "
             [:code ":favorite-ice-cream"]
             " make a coherent grouping of "
             [:em "data"]
             " about a person. On the other hand, if you're watching a pipeline, and pulling out a singular piece of information and sending it off somewhere else, there's no apparent relationship (from your perspective) about each datum. But over time, you are handling multiple, singluar pieces of information, " [:em "datums"] ". Similarly, a value passed to a function is a datum and a value returned from that function is also a datum, but together, they don't necessarily aggregate to " [:em "data"], " so I'd call the two " [:em "datums"] ". I use " [:em "datums"] " throughout the text of the Speculoos project because it makes sense to my weirdo brain."]

            [:dt#HANDS "heterogeneous, arbitrarily-nested data structure"]
            [:dd "Exactly one Clojure collection (vector, map, list, or set) with zero or more elements or nested collections, nested to any depth."]

            [:dt#non-term-seq "non-terminating sequence"]
            [:dd "One of " [:code "clojure.lang.{Cycle,Iterate,LazySeq,LongRange,Range,Repeat}"] " that may or may not be realized, and possibly infinite."
             (label "infinite?")
             (side-note "infinite?" "I am not aware of any way to determine if such a sequence is infinite, so Speculoos treats them as if they are.")]

            [:dt#path "path"]
            [:dd "A vector that unambiguously navigates to a single element (scalar or sub-collection) in a heterogeneous, arbitrarily-nested data structure. Almost identical to the second argument of "
             [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/get-in"} [:code "clojure.core/get-in"]]
             ", but with more generality. In the context of the Speculoos project, you supply a "
             [:em "path"]
             " as the second argument to the starred functions "
             [:code "get-in*"] ", "
             [:code "assoc-in*"] ", "
             [:code "update-in*"] ", and "
             [:code "dissoc-in*"]
             ". Vector elements and list elements are indexed by zero-based integers. Map values are addressed by their keys, which are often keywords, but can be any data type, including integers, or composite types."
             (label "keys")
             (side-note "keys" "You don't often need to key a map on a multi-element, nested structure, but when you need to, it's awesome.")
             " Set members are addressed by their identities."
             (label "nested-sets")
             (side-note "nested-sets" "Nested collections contained in a set can certainly be addressed; the path vector itself contains the collections.")
             " An empty vector " [:code "[]"] " addresses the outermost, containing collection. "
             [:em "Path"]
             "s are used extensively in the implementation of Speculoos' validation functions, but also are crucial in the reports of invalid datums."]

            [:dt "predicate"]
            [:dd "A function"
             (label "IFn")
             (side-note "IFn" (h2/html "Or something that implements " [:code "IFn"] ", like a set."))
             " that returns a truthy or falsey value.  In the vast majority of instances, a function of one argument, but in certain corners of Speculoos, such as in argument-vs-return specifications, the function consumes more than one argument."]

            [:dt "scalar"]
            [:dd "A single, non-divisible datum, such as an integer, string, boolean, etc. Essentially, a shorter term for " [:em "non-collection"] "."
             (label "atom")
             (side-note "atom" (h2/html "I'd prefer to have adopted " [:em "atom"] " in this role, but Clojure already uses it " [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/atom"} "elsewhere"] "."))]

            [:dt "validate"]
            [:dd "An action that returns an exhaustive listing of all datum+predicate pairs, their paths, and whether the datum satifies the predicate."]

            [:dt "valid?"]
            [:dd "Returns " [:code "true"] " if all datums satisfy their predicates, " [:code "false"] " otherwise."]]
           ]))]))