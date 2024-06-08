(ns ideas
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def ideas-UUID #uuid "27fd9200-3869-488b-9604-1f88ee84c053")


(spit "doc/ideas.html"
      (page-template
       "Speculoos --- 3 Ideas"
       ideas-UUID
       [:body
        (nav-bar "Ideas")
        [:article
         [:section
          [:h1 "Speculoos Ideas"]
          [:p "Speculoos aims to do what " [:code "clojure.spec.alpha"] " does, for the same " [:a {:href "https://clojure.org/about/spec"} "reasons"] ", but with its own twist."]
          [:p "So what does " [:a {:href "https://clojure.org/guides/spec"}[:code "spec.alpha"]] " do? Briefly, it provides a system to describe Clojure data, validate data, and thoroughly test function input and output."
           (label "side-by-side")
           (side-note "side-by-side" (h2/html "See " [:a {:href "diff.html"} [:code "diff"]] " for a side-by-side comparison."))
           " What does that look like? First, we'll start with some regular old Clojure data."]

          [:pre (print-form-then-eval "(def person-1 {:name \"Nathaniel Bumppo\"
                                        :phone \"314-1592\"
                                        :address {:street-number 1789
                                                  :street-name \"Fenimore Avenue\"}
                                        :occupation \"deer veterinarian\"
                                        :favorite-ice-cream-flavor :butter-pecan})")]

          [:p "Writing a Speculoos specification involves creating a regular old Clojure data structure that mimics the data: replace every datum with a predicate."]

          [:pre (print-form-then-eval "(def person-spec {:name string?
                                        :phone #(and string? (re-matches #\"\\d{3}-\\d{4}\" %))
                                        :address {:street-number int?
                                                  :street-name string?
                                                  :apt-number int?}
                                        :occupation string?
                                        :favorite-ice-cream-flavor keyword?})")]

          [:p "Now, we merely send both to Speculoos."]

          [:pre
           (print-form-then-eval "(require '[speculoos.core :refer [valid-scalar-spec? only-invalid validate-scalar-spec]])")
           [:br]
           (print-form-then-eval "(valid-scalar-spec? person-1 person-spec)")]

          [:p "Let's create some invalid data."]

          [:pre (print-form-then-eval "(def person-2 {:name \"Lucy Ricardo\"
                                     :phone \"Klondike5-6553\"
                                     :address {:street-number 623
                                               :street-name \"East 68th Street\"
                                               :apt-number 4}
                                     :occupation \"candy assembly line operator\"
                                     :favorite-ice-cream-flavor :vita-meata-vegemin})")

           (print-form-then-eval "(valid-scalar-spec? person-2 person-spec)")]

          [:p "And when we delve a little deeper"]

          [:pre (print-form-then-eval "(only-invalid (validate-scalar-spec person-2 person-spec))")]

          [:p "we can see that the phone number in " [:code "person-2"] " is not valid."
           (label "invalid-phone")
           (side-note "invalid-phone" "Perhaps in this case, though, our phone number specification doesn't properly cover all the possible formats in the wild.")]

          [:p "Speculoos checks sequences with the same concept."]

          [:pre (print-form-then-eval "(def vegetables [:bundle \"carrots\" :crown \"broccoli\" :stalks \"celery\"])")]

          [:p "A Speculoos specification is a plain Clojure data structure that mimics the data: predicates replace the datums."]

          [:pre (print-form-then-eval "(def vegetable-spec [keyword? string? keyword? string? keyword? string?])")]

          [:p "We check our veggies in the same way."]

          [:pre (print-form-then-eval "(valid-scalar-spec? vegetables vegetable-spec)")]

          [:p "And some invalid data."]

          [:pre (print-form-then-eval "(def rotten-vegetables [:basket :onions])")]

          [:p "Speculoos tells us what's wrong."]

          [:pre (print-form-then-eval "(only-invalid (validate-scalar-spec rotten-vegetables vegetable-spec))")]

          [:p "Speculoos has a similar story for testing functions. Speculoos function specifications live in the function's metadata."]

          [:pre (print-form-then-eval "(defn transmogrifier
                        {:speculoos/arg-scalar-spec [string? string?]
                         :speculoos/ret-scalar-spec string?}
                        [x y]
                        (str x \" is a \" y))")]

          [:p "Speculoos can instrument a function in the same way as " [:code "spec.alpha"] ", but I often prefer to do it like this."]

          [:pre
           (print-form-then-eval "(require '[speculoos.function-specs :refer [validate-fn-meta-spec exercise-fn]])")
           [:br]
           (print-form-then-eval "(validate-fn-meta-spec transmogrifier \"Hobbes\" \"tiger\")")
           [:br]
           (print-form-then-eval "(validate-fn-meta-spec transmogrifier \"Hobbes\" :stuffed-tiger)")]

          [:p "If we want to exercise our function, Speculoos can."]

          [:pre (print-form-then-eval "(exercise-fn transmogrifier 5)")]

          [:p "So while doing similar tasks as " [:code "spec.alpha"] ", we can see that Speculoos does things a bit differently."]

          [:p "Speculoos explores three ideas. First, creating specifications with standard Clojure data structures is intuitive and powerful without sacrificing any necessary features. Second, and related to the first, Clojure's extensive data structure manipulation functions, augmented with a handful of " [:code "get-in"] " style functions, will allow Clojurists to inspect, manipulate, and delete any data and/or specification to accomplish their task. Third, specifying data collections ought to be conceptually and practically distinct from specifying the scalar values they contain."]

          [:p "Let's examine each of those ideas in turn."]]

         [:section
          [:h2 "Literal specifications"]
          [:p "Speculoos validation functions consume run-of-the-mill Clojure data structures. To specify a vector, you write vector. To specify a map, you write a map. Same for lists, same for sets. Speculoos specifications can be composed and modified with any function in " [:code "clojure.core"] ", a " [:a {:href "https://github.com/plumatic/plumbing"} "pre"] "-" [:a {:href "https://github.com/redplanetlabs/specter"} "existing"] " " [:a {:href "https://github.com/weavejester/medley"} "library"] ", or any pet tool you created that makes sense to the way your brain thinks."]

          [:p "This carries enormous benefits. First, writing Speculoos specifications is natural and intuitive. Simply make your specification look like your data."]

          [:pre (print-form-then-eval "(valid-scalar-spec? [99 \"abc\" :foo \\c false 'sym 22/7]
                                                 [int? string? keyword? char? boolean? symbol? ratio?])")]

          [:p "Speculoos handles heterogenous, arbitrarily-nested data structures of all of Clojure's data collection types."]

          [:pre
           (print-form-then-eval "(def data-1 {:a 72 :b [\"xyz\" :baz] :c {:d (list \\z true)}})")
           [:br]
           (print-form-then-eval "(def spec-1 {:a int? :b [string? keyword?] :c {:d (list char? boolean?)}})")
           [:br]
           (print-form-then-eval "(valid-scalar-spec? data-1 spec-1)")]

          [:p "There is a very nearly one-to-one correspondence between your data and a Speculoos specification."]

          [:p "Since specifications are pure Clojure data structures, any function that operates on a Clojure data structure will work, such as old reliable " [:code "clojure.core/assoc"] "."]

          [:pre (print-form-then-eval "(valid-scalar-spec? {:x 33 :y 44}
                                                 (assoc {:y int?} :x int?))")]

          [:p "Composing a specification with "[:code "concat"] " is like a set of well-worn flannel pyjamas."]

          [:pre
           (print-form-then-eval "(def spec-from-ten-years-ago [#(> % 5) #(not= % 3)])")
           [:br]
           (print-form-then-eval "(def spec-from-the-wire [#(= % \"hello\") #(even? %)])")
           [:br]
           (print-form-then-eval "(valid-scalar-spec? [6 2 [\"hello\" 4]]
                                                 (concat spec-from-ten-years-ago
                                                         spec-from-the-wire))")]

          [:p "What if your little software machine works at the middle of a long pipeline, and the pipeline hands you some data and a specification, but your little machine's needs aren't as strict as the specification? Speculoos can relax the specification on-the-fly with standard " [:code "core"] " functions, because the specification is just a map."]

          [:pre (print-form-then-eval "(def overly-strict-spec {:a float? :b #(and (string? %)
                                                                    (< 10 (count %)))})")
           [:br]
           (print-form-then-eval "(valid-scalar-spec? {:a 5.5 :b \"baz\"}
                                                 (assoc overly-strict-spec :b string?))")]

          [:p "Your little machine does its job, and then sends off the processed data and the original specifciation."]

          [:p "I propose that any other way of composing data specifications could, at most, only " [:em "match"] " it in power, but never exceed it. And to merely match it would require replicating all of Clojure plus all external libraries, everywhere. It's almost cheating to have the entirity of Clojure and its ecosystem to write specifications. People won't have to learn a new grammar or " [:span.small-caps "dsl"] ". Clojurists are already adept at diving into a data structure, pulling it apart, manipulating values, and putting back together. Could there logically be a superior method than writing specifications with pure Clojure data literals?"]

          [:p "Speculoos' second core idea closely relates to the first, but I wanted to separate them so that if the second idea is a dud, it wouldn't sink both."]]


         [:section
          [:h2 "Augemented functions for manipulating nested data structures"]

          [:p "I mentioned earlier Clojure's extensive core library for handling data structures. " [:code "get-in"] ", " [:code "assoc-in"] ", " [:code "update-in"] ", " [:code "dissoc"] " are among my favorites. However, they have some limitations that block them from serving in all the scenarios I wanted. I envisioned a set of functions that presented a consistent interface to inspect, change, and remove elements in vectors, maps, lists, and sets, at any arbitrary level of nesting."]

          [:p "To that end, I wrote " [:em "starred"] " versions: " [:code "get-in*"] ", " [:code "assoc-in*"] ", " [:code "update-in*"] ", " [:code "dissoc-in*"] " which all operate similar to their " [:code "clojure.core"] " namesakes, but work on any heterogenous, arbitrarily-nested data structure. Their unified interface pivots on the concept of a "  [:em "path"] ", a vector of elements that unambiguously addresses a single datum in a heterogenous, arbitrarily-nested data structure. Elements in vectors and lists are referenced by zero-based integers, map elements are addressed by their keys"
           (label "tread-carefully")
           (side-note "tread-carefully" "Which are often keywords, but could be integers or any composite value, so tread carefully.")
           ", and set elements are addressed by the elements themselves. This family of functions makes a useful toolbox to compose and adjust Speculoos specification literals."]

          [:p "Let's take a look at what the starred functions can do. First, an example heterogenous, arbitrarily-nested data structure."]

          [:pre (print-form-then-eval "(def crazy-data [11 22 {:a 33 :b [44 55] :c [66 [77 {:d [88]}]]} 99 [[[111]] (list 222)] #{333 [444]}])")]

          [:p "Inspecting a nested value,"]

          [:pre
           (print-form-then-eval "(require '[speculoos.fn-in :refer [get-in* assoc-in* update-in* dissoc-in*]])")
           [:br]
           (print-form-then-eval "(get-in* crazy-data [2 :c 1 1 :d 0])")]

          [:p "Associating a nested value,"]

          [:pre (print-form-then-eval "(assoc-in* crazy-data [4 1 2] 999)")]

          [:p "Updating a nested value,"]

          [:pre (print-form-then-eval "(update-in* crazy-data [5 [444] 0] #(+ 444 %))")]

          [:p "Dissociating a nested value."]

          [:pre (print-form-then-eval "(dissoc-in* crazy-data [2 :b 0])")]

          [:p "Note how the starred functions are able to dive into any of the collection types to do their jobs. Such capabilities are invaluable to straightforwardly manipulating Speculoos specification literals, or adjusting invalid/non-conforming data. In fact, the Speculoos implementation makes liberal use of each of these starred functions to perform the mechanics of validation. Beyond that, they also enable many of the Speculoos utility functions, such as generating sample data from a specification and data repair."]

          [:p "One of " [:code "spec.alpha"] "'s headline features that Speculoos does not attempt to replicate is returning conformed values. This part of the Speculoos experiment tests if having the family of starred functions removes some of the need of conformed returns."]]


         [:section
          [:h2 "Separating scalar specification and collection specification"]

          [:p "You may have been uncomfortably shifting in your chair while reading through the Speculoos examples above. Every example we've seen so far shows Speculoos validating individual scalars, such as integers, strings, booleans, etc."]

          [:pre (print-form-then-eval "(valid-scalar-spec? [false \"qwz\" -88] [false? string? neg-int?])")]

          [:p "However, you might need to specify some property of a collection itself, such as a vector's length, the presence of a key in a map, relationships " [:em "between"] " datums, etc."]

          [:p "Speculoos' third idea is that specification of scalars and specification of collections should be explicitly separate. You perhaps noticed that the function name wasn't " [:code "valid?"] " but instead " [:code "valid-scalar-spec?"] ". Speculoos provides a parallel group of functions to validate the properties of collections, independent of the scalar values they contain."]

          [:pre
           (print-form-then-eval "(def scalars-not-important [33 {:Q 77 :W [:granite :marble :basalt]}])")
           [:br]
           (print-form-then-eval "(def spec-for-collections [#(= 2 (count %)) {:W [vector? #(every? (complement coll?) %)]
                                                                        :all-keywords-capitals? (fn [m] (every? #(re-matches #\"^[A-Z]$\" (name %)) (keys m)))}])")
           [:br]
           (print-form-then-eval "(require '[speculoos.core :refer [valid-collection-spec? valid?]])")
           [:br]
           (print-form-then-eval "(valid-collection-spec? scalars-not-important spec-for-collections)")]

          [:p "To show how strongly I believe this idea, I reserved a very precious resource — the shortest, most mnemonic function name, " [:code "valid?"] " — for simultanesouly, but separately, checking a scalar specification and a collection specification."]

          [:pre (print-form-then-eval "(valid? [11 \"abc\" :foo \\c] [int? string? keyword? char?] [vector? #(< (count %) 5)])")]

          [:p   "Having to write two specifications for each data structure does increase work, but I think the benefits are worth it. It is much simpler conceptually, requiring only a few memorable rules for either task. Merging scalar and collection specification requires a grammar that somehow describes the bottom-level values immediately adjacent to describing the containers that contain those values. Cramming all those characters into one spot makes for a visually noisy specification. At every moment, you have to mentally keep track of what kind of thing you are specifying, scalar or collection. And, I hope you can appreciate the downstream benefits that the implementation code is much simpler."]

          [:p "In practice, you don't have to write two specifications if you don't need both. Specify only as much as you want; Speculoos validates only what you provide. If you want to specify some subset of your scalar values, Speculoos is happy to check only those."]

          [:p "Here, we only care about the second and fourth scalars, and completely ignore saying anything about the collection."
           (label "implied")
           (side-note "implied" "Although this particular scalar specification appears to imply a vector of four elements, Speculoos' scalar validation does not enforce that. Collection validation could check the count if we weren't leaving it empty for the puproses of this example.")]

          [:pre (print-form-then-eval "(valid? [11 \"abc\" :foo \\c] [any? string? (constantly true) char?] [])")]

          [:p "If you don't care about the values themselves but only want to specify properties of the collection, Speculoos will check those properties of the collections and ignore the datums."]

          [:pre (print-form-then-eval "(valid? {:a [11 22] :b [33 44] :c [55 66]} {} {:b [vector? #(= 2 (count %))]})")]

          [:p "Speculoos is " [:em "à la carte"] " as much as possible."]]]]))