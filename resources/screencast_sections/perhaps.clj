(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def perhaps-index 15)


[:body
 (panel
  (screencast-title perhaps-index "Perhaps So")
  [:h3 [:em "How Speculoos addresses issues raised in '" [:a {:href "https://www.youtube.com/watch?v=YR5WdGrpoug"} "Maybe Not"] "'"]]

  [:div.note
   [:p "Rich Hickey presented " [:a {:href "https://2018.clojure-conj.org/rich-hickey/"} "Maybe Not"]  " at the 2018 " [:em "Clojure conj"] ". If I correctly understand his presentation, he identified some flaws in " [:code "clojure.spec.alpha"] ", the Clojure distribution's built-in library for specifying and validating data. Mr Hickey highlighted three issues."]])


 (panel
  [:h3 "Problems raised by " [:em "Maybe Not"]]

  [:ol
   [:li [:strong "Representing"] " partial information in an aggregate data structure."]
   [:li [:strong "Specifying"] " partial information in an aggregate data structure."]
   [:li [:strong "Validating"] " partial information in an aggregate data structure."]]

  [:div.note
   [:p "He was apparently not satisfied with the way " [:code "spec.alpha"] " handles these three issues."]

   [:p "The " [:a {:href "https://github.com/blosavio/speculoos"} "Speculoos library"]
    " is an experiment to see if it is possible to perform the same tasks as "
    [:code "spec.alpha"]
    " using literal specifications. Due to some of the implementation details and policies I chose, the library has some emergent properties that end up neatly handling those three issues."]])


 (let [motto-notes
       [:div
        [:p "Efficiently using Speculoos requires remembering three mottos."]

        [:ol
         [:li "Validate scalars separately from validating collections."]
         [:li "Shape the specification to mimic the data."]
         [:li "Ignore un-paired predicates and un-paired datums."]]

        [:p "If we follow those three mottos, handling partial information is straightforward. Briefly, Speculoos specifies and validates scalars separately from specifying and validating collections. A scalar specification describes the properties of the datums themselves. The presence or absence of a scalar is a completely separate concern and falls under the jurisdiction of specifying and validating the collection. By separating the two concerns, Speculoos seamlessly handles partial information while avoiding the issues that befall " [:code "spec.alpha"] "."]

        [:p "Let's examine each issue in more detail."]]]

   (-> (load-file "resources/screencast_sections/mottos.clj")
       (update 4 #(conj % motto-notes))))


 (panel
  [:h3 "Representing partial information"]

  [:div.side-by-side-by-side-container
   [:div.side-by-side-by-side [:pre [:code "{:first-name \"Albert\"\n :last-name \"Einstein\"\n :age 76}"]]]
   [:div.side-by-side-by-side [:pre [:code "{:first-name \"Isaac\"\n :last-name \"Newton\"\n :age nil}"]]]
   [:div.side-by-side-by-side [:pre [:code "{:first-name \"Maria\"\n :last-name \"Göppert-Mayer\"}"]]]]

  [:div.note
   [:p "Representing partial data is not specifically the purview of the Speculoos library, but of Clojure itself. We'll discuss partial information only in order to supply us with examples for later."]

   [:p "Mr Hickey highlights the fact that idiomatic Clojure merely excludes missing information instead of 'holding a slot' with a " [:code "nil"] ". Imagine data about a person that could include their first name, last name, and and their age. Here's an example of 'complete' data."]

   [:p "The following example of partial data, with " [:code "nil"] " associated to 'missing' " [:code ":age"] " information, is atypical."]

   [:p "The more idiomatic way to represent partial information about a person involves merely leaving off the person's age."]])


 (panel
  [:h3 "Specifying information"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:p "data"]
    [:pre [:code "{:first-name \"Albert\"\n :last-name \"Einstein\"\n :age 76}"]]]
   [:div.side-by-side
    [:p "specification"]
    [:pre [:code "{:first-name string?\n :last-name string?\n :age int?}"]]]]

  [:div.vspace]

  [:p "signature for validating"]
  [:pre [:code "(valid-scalars? data\n                specification)"]]

  [:div.note
   [:p "A Speculoos specification is a plain Clojure data structure containing predicates. The specification's shape mimics the shape of the data (Motto #2). Professor Einstein's data is a map with keys " [:code ":first-name"] ", " [:code ":last-name"] ", and " [:code ":age"] ". The Speculoos specification for that data might look like this."]

   [:p "The specification is likewise a map with those same keys, i.e., the same 'shape', with predicates " [:code "string?"] " and " [:code "int?"] " replacing datums. Speculoos assembles pairs of datums and predicates and reports if the datums satisfy their corresponding predicates and returns " [:code "true/false"] "."]

   [:p "Signature for validating"]

   [:p "I'll be printing the specification on the line directly below the data to visually emphasize how the shape of the specification mimics the shape of the data (Motto #2)."]

   [:p  "(Speculoos offers a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#valid-thorough"} "verbose variant"] " if we need to see details of the validation.)"]])


 (panel
  [:h3 "Validating complete data"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-scalars?]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\" :age 76} {:first-name string? :last-name string? :age int?})")

  [:div.note
   [:p [:code "valid-scalars?"] " systematically walks through both the data and specification, and where it finds a datum paired with a predicate, it validates. "]

   [:ul
    [:li [:code "\"Albert\""] " at " [:code ":first-name"] " in the data satisfies " [:code "string?"] " at " [:code ":first-name"] " in the specification, "]
    [:li [:code "\"Einstein\""] " at " [:code ":last-name"] " in the data satisfies " [:code "string?"] " at " [:code ":last-name"] " in the specification, and"]
    [:li [:code "76"] " at " [:code ":age"] " in the data satisfies " [:code "int?"] " at " [:code ":age"] " in the specification."]]

   [:p "Three datums paired with three predicates. All predicates were satisfied. So " [:code "valid-scalars?"] " returned " [:code "true"] "."]])


 (panel
  [:h3 "Validating partial data"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\"} {:first-name string? :last-name string? :age int?})")

  [:div.note
   [:p "Let's see what happens if we remove Professor Einstein's age from the data but leave the corresponding predicate in the specification."]

   [:p "That result may be surprising. Why doesn't the missing age datum cause a " [:code "false"] " result? We need to consider Motto #3: Un-paired predicates are ignored. " [:code "valid-scalars?"] " was able to find two datum+predicate pairs."]

   [:ul
    [:li [:code "Albert"] " at " [:code ":first-name"] " in the data satisfies predicate " [:code "string?"] " at " [:code "first-name"] " in the specification."]
    [:li [:code "Einstein"] " at " [:code ":last-name"] " in the data satisfies predicate " [:code "string?"] " at " [:code ":last-name"] " in the specification."]
    [:li "Predicate " [:code "int?"] " at " [:code ":age"] " in the specification was not paired with an element in the data and was therefore ignored, as Motto #3 informs us."]]

   [:p "That may seem kinda broken, but it opens up some powerful capabilities we're about to explore. Later, we'll see how to verify that the age datum actually exists in the data."]])


 (panel
  [:h3 "Validating complete data, partial specification"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\" :email \"al@princeton.edu\"} {:first-name string? :last-name string?})")

  [:div.note
   [:p "What about the other way around? What if our data contains a key-value that does not appear in the specification? Let's add an email entry."]

   [:p "Again, " [:code "valid-scalars?"] " found two datum+predicate pairs. Both first-name and last-name datums satisfied their corresponding predicates, so the validation returned " [:code "true"] ". The email datum did not have a corresponding predicate, so, according to Motto #3, it was ignored."]

   [:p "The general idea behind Motto #3 is " [:em "This element may or may not exist, but if it does exist, it must satisfy this predicate."] " Taken to its logical conclusion, " [:code "valid?"] ", within the Speculoos library, conveys the notion " [:em "Zero invalid results"] "."]])


 (panel
  [:h3 "Validating complete data, empty specification"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\" :age 76} {})" 66 33)

  [:div.note
   [:p "We might imagine a scenario where we absolutely do not care about any facet of our data. In that case, our specification would contain exactly zero predicates."]

   [:p [:code "valid-scalars?"] " found zero pairs of datums and predicates. Since there were zero invalids, " [:code "valid-scalars?"] " returns " [:code "true"] "."]])


 (panel
  [:h3 "Validating empty data"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {} {:first-name string? :last-name string? :age int? :address {:street-name string? :street-number int? :zip-code int? :city string? :state keyword?} :email #\"\\w@\\w\"})")

  [:div.note
   [:p "Perhaps over the last few months, our team has been assembling a comprehensive specification for a person's data that includes predicates for a whole slew of possible datums. We need to be able to handle partial data. I.e., not every instance of data we encounter will be complete. The edge case would be zero datums."]

   [:p "Not a single one of those predicates was paired with a datum, so there were zero invalid results. Thus, " [:code "valid-scalars?"] " returns " [:code "true"] "."]])


 (panel
  [:h3 "Validating complete data, one invalid datum"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\" :age \"not an integer!\"} {:first-name string? :last-name string? :age int?})")

  [:div.note
   [:p "In every example we've seen so far, all the datums satisfy the predicate they were paired with. Here's what happens if at least one datum does not satisfy its predicate."]

   [:p "String datum " [:code "not an integer!"] " failed to satisfy the " [:code "int?"] " predicate located at " [:code ":age"] " in the specification. Therefore, " [:code "valid-scalars?"] " returned " [:code "false"] ". Speculoos provides other functions that give more detail about invalid elements, but for simplicity, we'll stick with the " [:code "true/false"] " results."]])


 (panel
  [:h3 "Validating presence of a datum: datum not there"]

  (prettyfy-form-prettyfy-eval "(defn has-age? [m] (contains? m :age))")

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-collections?]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-collections? {:first-name \"Albert\" :last-name \"Einstein\"} {:foo has-age?})" 45 45)

  [:div.note "If we wanted to ensure that the data contains a particular key-value, we need to validate the collection itself (Motto #1). Presence of absence of a datum is a property of the collection. First, we'll write a " [:code "has-age?"] " collection predicate that tests whether the map contains an " [:code ":age"] " key."

   [:p "Then, we insert " [:code "has-age?"] " into the collection specification. Collection validation operates a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#collection-validation"} "little differently"] " from scalar validation, but take my word that this is how to assemble the collection specification for this situation."]

   [:p  "Given this collection specification, " [:code "has-age?"] " will be paired with the root collection of the data."]

   [:p "Finally, we invoke a completely different function to validate (Motto #1)."]

   [:p [:code "valid-collections?"] " informs us that the map fails to satisfy the " [:code "has-age?"] " collection predicate. It fails because the map does not contain " [:code ":age"] " as the collection specification requires."]])


 (panel
  [:h3 "Validating presence of a datum: datum is there"]

  (prettyfy-form-prettyfy-eval "(valid-collections? {:first-name \"Albert\" :last-name \"Einstein\" :age 76} {:foo has-age?})" 45 45)

  [:div.note
   [:p "Now our map *does* contain :age. " [:code "valid-collections?"] " informs us that this map to satisfies the " [:code "has-age?"] " collection predicate."]])


 (panel
  [:h3 "Combo validation: scalars, then collections"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid?]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid? {:first-name \"Albert\" :last-name \"Einstein\"} {:first-name string? :last-name string? :age int?} {:foo has-age?})")

  [:div.note
   [:p "We'll often want to validate some aspects of both the scalars and the collections, so Speculoos provides a combo function that does a scalar validation, immediately followed by a collection validation, and returns the overall result."]

   [:p "The " [:code "string?"] " scalar predicates at " [:code ":first-name"] " and " [:code ":last-name"] " were both satisfied. Scalar predicate " [:code "int?"] " at " [:code ":age"] " was ignored. However, collection predicate " [:code "has-age?"] " at " [:code ":foo"] " was not satisfied, so " [:code "valid?"] " returns " [:code "false"] "."]

   [:p "Specifying and validating scalar datums separately from specifying and validating collections completely isolates two concerns. The scalar predicates are entirely devoted to testing the properties of the scalars themselves. The collection predicates are devoted to properties of the collections, including the size of the collections, the presence/absence of elements, and relationships " [:em "between"] " elements. Using both, Speculoos can validate any facet of a heterogeneous, arbitrarily-nested data structure."]])


 (panel
  [:h3 "When?!"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\" :age 76.0} {:first-name string? :last-name string? :age int?})")

  [:div.note
   [:p "A portion of " [:em "Maybe Not"] " discusses " [:code "spec.alpha"] "'s issues with optionality. Mr Hickey contends it is a mistake to put optionality into aggregate specifications because doing so destroys a specification's re-usability. An entity that is optional in one context might not be optional in another context."]

   [:p "Speculoos does not suffer from this problem. Because of Motto #3, any predicate that is not paired with a datum is ignored. Any datum that is not paired with predicate is also ignored. Only when a datum is paired with a predicate is the pair considered in the validation result. Separately, if a particular context requires the presence of a datum, we can validate that with a collection validation."]

   [:p "Mr Hickey points out that validating arguments and return values of a function provide a built-in context: the context of the function itself. Speculoos validations themselves carry an inherent context: The context is the validation function with the specification at the moment of invocation, such as we've seen with " [:code "valid-scalars?"] "."]

   [:p#flexible "In addition to being straightforward to compose, Speculoos specifications are extremely flexible because they are plain Clojure data structures. Speculoos specifications can be manipulated using any Clojure tools, including the entire core library or any third-party library. Quite a lot of this flexibility can be demonstrated with merely " [:code "get"] ", " [:code "assoc"] ", " [:code "update"] ", and " [:code "dissoc"] "."]

   [:p "Let's pretend that a specification someone handed us requires the age datum to be an integer. If Professor Einstein's age is instead a floating-point double, the validation would fail."]

   [:p [:code "76.0"] " is not an integer."]])


 (panel
  [:h3 "At that moment: Relax the requirement"]

  (prettyfy-form-prettyfy-eval "(assoc {:first-name string? :last-name string? :age int?} :age number?)" 75 75)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Albert\" :last-name \"Einstein\" :age 76.0} (assoc {:first-name string? :last-name string? :age int?} :age number?))" 95 95)

  [:div.note
   [:p "We can easily relax our specification to accept that the age be any kind of number."]

   [:p "With that relaxed specification in hand, that data is now valid."]

   [:p [:code "76.0"] " satisfies scalar predicate " [:code "number?"] " which we " [:code "assoc"] "-ed into the specification on-the-fly."]

   [:p "The original specification is immutable, as it always was. During the validation, we associated a more permissive scalar predicate so that, in this context (while invoking " [:code "valid-scalars?"] "), the data is valid."]])


 (panel
  [:h3 "At that moment: Remove the requirement"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-collections? {:first-name \"Albert\" :last-name \"Einstein\"} {:foo has-age?})" 35 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(dissoc {:foo has-age?} :foo)")

    (prettyfy-form-prettyfy-eval "(valid-collections? {:first-name \"Albert\" :last-name \"Einstein\"} (dissoc {:foo has-age?} :foo))" 45 45)]]

  [:div.note
   [:p "Now, let's pretend someone handed us a collection specification that requires the presence of the age key-value, but in our little part of the world, our data doesn't have it, and our little machine doesn't need it. Without intervention, collection validation will fail."]

   [:p "The data does not contain a key " [:code ":age"] " so the data is invalid, according to the specification we were handed."]

   [:p "But, our little data processing machine doesn't need Professor Einstein's age, so we can straightforwardly remove that requirement in our context. Here's the altered collection specification."]

   [:p "If we now use that altered collection specification in the context of our little machine, the data is valid."]

   [:p "The collection specification no longer contains the " [:code "has-age?"] " predicate, so the data is declared valid in our context."]

   [:p "Specifications made of plain Clojure data structures absorb every drop of generality, composability, and re-usability of the underlying data structures. They may be passed over the wire, stored in the file system, version controlled, annotated with metadata, and manipulated at will to suit any context."]])


 (panel
  [:h3 "Replicating specific scenarios from " [:em "Maybe Not"]]

   [:div.note
    [:p "In this section, we'll explore how Speculoos handles the specific, problematic scenarios presented by Mr Hickey."]])


 (panel
  [:h3 "Predicates use proper " [:code "or"]]

  [:table
   [:tr
    [:td "Commutative:"]
    [:td
     (prettyfy-form-prettyfy-eval "(#(or (int? %) (string? %)) 42)")
     (prettyfy-form-prettyfy-eval "(#(or (string? %) (int? %)) 42)")]]

   [:tr
    [:td "Associative:"]
    [:td
     (prettyfy-form-prettyfy-eval "(#(or (int? %) (or (string? %) (char? %))) 42)")
     (prettyfy-form-prettyfy-eval "(#(or (or (int? %) (string? %)) (char? %)) 42)")]]

   [:tr
    [:td "Distributive:"]
    [:td (prettyfy-form-prettyfy-eval "(#(or (and (int? %) (even? %)) (string? %)) 42)")]]

   [:tr [:td "Etc."]]]

  [:div.note
   [:p "Speculoos predicates are plain old Clojure functions. When we need to validate an element that may be one of multiple types, the predicates use " [:code "clojure.core/or"] ", which will inherit all the proper semantics."]])


 (panel
  [:h3 [:code "nil"] "-able"]

  [:p "Don't."]

  [:div.note
   [:p "This one's easy: just write Speculoos predicates without " [:code "nilable"] "."]])


 (panel
  [:h3 "Namespaced specifications"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 45 45)]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def specification-1 [int? string? ratio?])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] specification-1)" 45 45)]]

  [:div.note
   [:p "One of " [:code "spec.alpha"] "'s propositions is that specs are " [:a {:href "https://clojure.org/about/spec#_global_namespaced_names_are_more_important"} "required to be namespace-qualified"] ". Speculoos takes a hands-off approach. Speculoos specifications are plain Clojure data structures that are referenced however we want. Specifications may be a literal, like " [:code "[int? string? ratio?]"] "."]

   [:p "Or, we may bind them to a symbol in our current namespace."]

   [:p "(We could also bind them to a symbol in a different namespace; not shown.)"]])


 (panel
  [:h3 "Specification registry"]

  [:div.no-display (defonce speculoos-registry (atom {:speculoos/specification-2 [int? string? ratio?] :speculoos/specification-3 {:first-name string? :last-name string? :age int}}))]

  [:pre [:code "(defonce speculoos-registry (atom {:speculoos/specification-2 [int? string? ratio?]\n                                   :speculoos/specification-3 {:first-name string?\n                                                               :last-name  string?\n                                                               :age        int?}})"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] (@speculoos-registry :speculoos/specification-2))" 65 45)

  [:div.note
   [:p "Or, we may gather them into our own bespoke registry."]])


 (panel
  [:h3 "Car specification"]

  (prettyfy-form-prettyfy-eval "(def car-scalar-specification {:make string? :model string? :year #(and (int? %) (>= % 1885))})" 45 45)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def car-collection-specification {:foo #(contains? % :make)})")

  [:div.note
   [:p "To follow along precisely, we could split out the " [:em "make"] ", " [:em "model"] ", and " [:em "year"] " concepts into their own named predicate functions, but for brevity, I'll stuff them directly into our " [:code "car"] " scalar specification."]

   [:p "At this point, we're not stating anything definitive about presence or absence of an element. A scalar specification says, for each scalar predicate, " [:em "This element may or may not exist, but if it does, the element must satisfy this predicate"] ". Declaring that " [:code ":make"] " is a string is completely separate from declaring that our car data must contain a " [:code ":make"] " value."]

   [:p "If we want to require that our car data contains a " [:code ":make"] " entry, we declare that requirement in a collection specification."]

   [:p "Unless explicitly required in a collection specification, Speculoos treats all scalars as optional. So we don't have to say anything about " [:code ":model"] " or " [:code ":year"] "."]])


 (panel
  [:h3 "Car validation: complete data"]

  (prettyfy-form-prettyfy-eval "(valid? {:make \"Acme Motor Cars\" :model \"Type 1\" :year 1905} car-scalar-specification car-collection-specification)")

  [:div.note
   [:p "Let's validate with all the specified values."]

   [:p "The values we supplied for " [:code ":make"] ", " [:code ":model"] ", and " [:code "year"] " all satisfied their respective scalar predicates. Furthermore, the " [:cde "car"] " map itself satisfied the collection specification's requirement that the map contain a key " [:code ":make"] "."]])


 (panel
  [:h3 "Car validation: partial data"]

  (prettyfy-form-prettyfy-eval "(valid? {:make \"Acme Motor Cars\"} car-scalar-specification car-collection-specification)")

  [:div.note
   [:p "Now, let's validate some car data with partial information: " [:code ":model"] " and " [:code ":year"] " values are missing."]

   [:p [:code "{:make \"Acme Motor Cars\"}"] " is valid car data because " [:code ":make"] " satisfies its scalar predicate and the map itself contains the only key we required in the collection specification. " [:code ":model"] " and " [:code ":year"] " are implicitly optional because we did not require their existence in the collection specification."]])


 (panel
  [:h3 "Car validation: extra data"]

  (prettyfy-form-prettyfy-eval "(valid? {:make \"Acme Motor Cars\" :year 1905 :cpu \"Intel Pentium\"} car-scalar-specification car-collection-specification)")

  [:div.note
   [:p "What if we have extra information? Let's validate data about an early 1900s car with a completely anachronistic computer chip."]

   [:p "Again, this car data is valid, because we did not specify any property relating to " [:code ":cpu"] ", so the validation ignored that datum. All the other existing datums satisfied their corresponding predicates."]])


 (panel
  [:h3 "Car validation: missing required data"]

  (prettyfy-form-prettyfy-eval "(valid? {:model \"Type 1\" :year 1905} car-scalar-specification car-collection-specification)")

  [:div.note
   [:p "What if we neglect to include the " [:code ":make"] " element?"]

   [:p "Finally, we run afoul of our collection specification: Our car data lacks a " [:code ":make"] " element, which our collection specification explicitly requires."]])


 (panel
  [:h3 "Car validation: different context, increasing requirements"]

  [:pre
   [:code.form "(assoc car-collection-specification :b #(contains? % :year))"]
   [:code.eval "\n;; => {:foo #(contains? % :make), :b #(contains? % :year)}"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid? {:make \"Acme Motor Cars\" :model \"Type 1\"} car-scalar-specification (assoc car-collection-specification :b #(contains? % :year)))")

  [:div.note
   [:p "What if we're in a different context, and suddenly we absolutely must have a " [:code ":year"] " element, too? Right then and there, we can augment the collection specification, because it's a plain Clojure data structure. And we know how to associate items on-the-fly into a map."]

   [:p "So in this new context, we use that new collection specification with tighter requirements."]

   [:p "Now, the absence of " [:code ":year"] " element of our car data is no longer ignored. This car data fails to satisfy one of its collection predicates that require the presence of a " [:code ":year"] " entry."]

   [:p "Note that specifying the values of the scalars themselves (in a Speculoos scalar specification), is completely orthogonal to requiring their presence (which we declare in a Speculoos collection specification). By splitting the two concerns, the specifications become straightforwardly re-usable. We are free to specify any number of properties of the car data that may or may not exist. Then, in a particular context, we adjust our collection specification to require the particular group of elements that we need for that particular context."]

   [:p "Speculoos specifications shouldn't proliferate uncontrollably because, as plain Clojure data structures, they're readily manipulable, on-the-fly, with " [:code "assoc"] " and friends."]])


 (panel
  [:h3 "Symmetric request/response schemas"]

  [:p [:em "Give me a partially filled-in form, and I will give you back a more filled-in form"]]

  (prettyfy-form-prettyfy-eval "(def one-spec {:ID int? :name string? :phone int?})")

  [:div.vspace]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(valid-scalars? {:ID 99} one-spec)" 25 45)]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(valid-scalars? {:ID 99 :name \"Sherlock Holmes\" :phone 12345678} one-spec)" 75 45)]]

  [:div.note
   [:p "No problem for Speculoos to validate that scenario with a single scalar specification. Because un-paired predicates are ignored, we can simply use the same scalar specification to validate both the " [:em "before"] " data and the " [:em "after"] " data."]

   [:p "Let's pretend we query a service with an ID, and the service returns that ID and the associated name and phone number. That would be a straightforward scalar specification."]

   [:p "Before we submit our request to the service, let's validate the partially filled-in form."]

   [:p [:code "valid-scalars?"] " only considers pairs of datum+predicates. Our partially filled-in request only has one datum that is paired with a predicate: " [:code "99"] " at " [:code ":ID"] " is paired with " [:code "int?"] " at " [:code ":ID"] " in the scalar specification. " [:code "99"] " satisfies its paired scalar predicate " [:code "int?"] ", so " [:code "valid-scalars?"] " returns " [:code "true"] "."]

   [:p "Now that we've validated our request, we send it off to the service, which returns " [:code "{:ID 99 :name \"Sherlock Holmes\" :phone 123456789}"] ". We can validate the response with the exact same scalar specification."]

   [:p "During this invocation, " [:code "valid-scalars?"] " encountered three datum+predicate pairs, and each datum satisfied its corresponding scalar predicate, so the validation returns " [:code "true"] "."]])


 (panel
  [:h3 "Symmetric request/response: invalid data"]

  (prettyfy-form-prettyfy-eval "(def one-spec {:ID int? :name string? :phone int?})")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:ID 99 :name \\z :phone \\q} one-spec)" 45 25)

  [:div.note
   [:p "What if we submit a query that causes the service to emit garbage data like " [:code "{:ID 0 :name \\z :phone \\q}"] "? Let's validate that."]

   [:p "Same scalar specification, but since the datums do not satisfy their scalar predicates, the service's response does not satisfy the specification."]

   [:p "One specification is sufficient to validate the data at each step. The specification is re-used, and there's one central location of what an " [:em "ID/name/phone"] " aggregate looks like."]])


 (panel
  [:h3 "Information-building pipelines"]

  [:p "Cupcakes!"]

  [:pre
   [:code.form
    [:p
     [:strong "flour"] " → 150 grams" [:br]
     [:strong "eggs"] " → 2" [:br]
     [:strong "sugar"] " → 130 grams" [:br]
     [:strong "butter"] " → 60 grams" [:br]
     [:strong "milk"] " → 1/8 liter"]]
   [:code.eval
    [:p
     "{}" [:br]
     "{:flour 150.0}" [:br]
     "{:flour 150.0 :eggs 2}" [:br]
     "{:flour 150.0 :eggs 2 :sugar 130.0}" [:br]
     "{:flour 150.0 :eggs 2 :sugar 130.0 :butter 60.0}" [:br]
     "{:flour 150.0 :eggs 2 :sugar 130.0 :butter 60.0 :milk 1/8}"]]]

  [:div.note
   [:p "An information-building pipeline is merely repeated application of the principles embodied in the  " [:em "request/response"] " pattern we discussed earlier. A singular scalar specification can describe all the steps of a serially aggregating data structure."]

   [:p "Let's pretend our cupcake processing pipeline accepts an accumulating map, and adds a new quantity based on an ingredient we pass alongside. It might look something like this."]

   [:p "Our pipeline constructs the accumulating map in six steps like this."]])


 (panel
  [:h3 "One specification for entire pipeline"]

  [:pre [:code "(def cupcake-spec {:flour double? :eggs int? :sugar double? :butter double? :milk ratio?})"]]

  [:div.vspace]

  [:pre
   [:code "(valid-scalars? {}                                                         cupcake-spec) ;; => true"] [:br]
   [:code "(valid-scalars? {:flour 150.0}                                             cupcake-spec) ;; => true"] [:br]
   [:code "(valid-scalars? {:flour 150.0 :eggs 2}                                     cupcake-spec) ;; => true"] [:br]
   [:code "(valid-scalars? {:flour 150.0 :eggs 2 :sugar 130.0}                        cupcake-spec) ;; => true"] [:br]
   [:code "(valid-scalars? {:flour 150.0 :eggs 2 :sugar 130.0 :butter 60.0}           cupcake-spec) ;; => true"] [:br]
   [:code "(valid-scalars? {:flour 150.0 :eggs 2 :sugar 130.0 :butter 60.0 :milk 1/8} cupcake-spec) ;; => true"]]

  [:div.note
   [:p "We can write one single scalar specification that validates each of those six steps."]

   [:p "Now, we can validate the data at each step."]

   [:p "Notice: " [:code "cupcake-spec"] " remained the same for each of the six validations as the pipeline added more and more elements. Speculoos' policy of ignoring un-paired predicates offers us the ability to specify the final shape at the outset, and the validation only considers the elements present at the moment of invocation."]])


 (panel
  [:h3 "Nested schemas"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a 42 :b \"abc\" :c [\\x \\y \\z] :d ['foo 'bar 'baz]}"]]]]

   [:tr
    [:td "remove scalars"]
    [:td [:pre [:code "{:a    :b     :c [       ] :d [        ]}"]]]]

   [:tr
    [:td "specification"]
    [:td [:pre [:code "{:a int? :b string? :c [char? char? char?] :d [symbol? symbol? symbol?]}"]]]]

   [:tr
    [:td "validate"]
    [:td (prettyfy-form-prettyfy-eval "(valid-scalars? {:a 42 :b \"abc\" :c [\\x \\y \\z] :d ['foo 'bar 'baz]} {:a int? :b string? :c [char? char? char?] :d [symbol? symbol? symbol?]})")]]]

  [:div.note
   [:p "Speculoos was designed from the outset to validate any heterogeneous, arbitrarily-nested data structure. Mr Hickey imagines a data structure something like this."]

   [:p "We can immediately compose a specification for that data. One trick is to take advantage of the fact that Speculoos specifications mimic the shape of the data. So first, we copy-paste the data, and delete the scalars."]

   [:p "Then, we insert our predicates, one predicate for each scalar."]

   [:p "Finally, we validate."]

   [:p [:code "valid-scalars?"] " systematically marches through the data and specification, searching for pairs of scalars and predicates. In this case, it finds pairs at keys " [:code ":a"] " and " [:code ":b"] ", and dives down into the nested vectors at keys " [:code ":c"] " and " [:code ":d"] ". So scalar "  [:code "\\x"] " is paired with predicate " [:code "char?"] ", scalar " [:code "'foo"] " is paired with predicate " [:code "symbol?"] ", etc. All the scalars satisfy their corresponding predicates, so the validation returns " [:code "true"] "."]])



 (panel
  [:h3 "Nested schemas: composing with named sub-components"]

  (prettyfy-form-prettyfy-eval "(def three-chars? [char? char? char?])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def three-syms? [symbol? symbol? symbol?])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:a 42 :b \"abc\" :c [\\x \\y \\z] :d ['foo 'bar 'baz]} {:a int? :b string? :c three-chars? :d three-syms?})")

  [:div.note
   [:p "We could also compose an equivalent scalar specification from pre-defined sub-components. Consider this."]

   [:p "Regular old Clojure composition in action. The scalar specification refers to " [:code "three-chars?"] " at its key " [:code ":c"] " and refers to " [:code "three-syms?"] " at its key " [:code ":d"] ". We can thus mix and match with impunity to compose our specifications."]])


 (panel
  [:h3 "Nested schemas: extracting slices"]

  (prettyfy-form-prettyfy-eval "(get {:a 42 :b \"abc\" :c [\\x \\y \\z] :d ['foo 'bar 'baz]} :c)")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? (get {:a 42 :b \"abc\" :c [\\x \\y \\z] :d ['foo 'bar 'baz]} :c) three-chars?)")

  [:div.note
   [:p "A riff on that tune is to extract some selection of our data and validate it against a smaller specification. Pretend we only care about validating the three-element vector at " [:code ":c"] ". We've got tools that can pull that vector out."]

   [:p "And we've already written a specification for that extracted vector, " [:code "three-chars?"] ", so we can immediately validate."]

   [:p "This invocation used " [:code "get"] " to extract the vector at " [:code ":c"] " and validated it against predicate " [:code "three-chars?"] "."]])


 (panel
  [:h3 "Nested schemas: validating slices in place"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:a 42 :b \"abc\" :c [\\x \\y \\z] :d ['foo 'bar 'baz]} {:c three-chars?})")

  [:div.note
   [:p "Alternatively, we could leverage the fact that un-paired datums are ignored, and specify only that nested vector."]

   [:p "We performed a validation on only a selection because " [:code "valid-scalars?"] " applied only the three " [:code "char?"] " scalar predicates to the contents of the nested vector at " [:code ":c"] "."]])


 (panel
  [:h4 "Movie times & placing orders: assembling specifications"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def street string?)")
    (prettyfy-form-prettyfy-eval "(def city string?)")
    (prettyfy-form-prettyfy-eval "(def state keyword?)")
    (prettyfy-form-prettyfy-eval "(def zip int?)")

    (prettyfy-form-prettyfy-eval "(def address {:street street :city city :state state :zip zip})")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(def id int?)")
    (prettyfy-form-prettyfy-eval "(def first-name string?)")
    (prettyfy-form-prettyfy-eval "(def last-name string?)")]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def user {:id id :first-name first-name :last-name last-name :address address})")

  [:div.note
   [:p "Mr Hickey's next example extends the discussion of validating Professor Einstein's data from earlier. First, we'll write some bottom-level specifications."]

   [:p "Then, we aggregate them into a specification for an address."]

   [:p "Next, we write some more bottom-level specifications."]

   [:p "Finally, we aggregate those specifications into a user specification, including the " [:code "address"] " aggregate from before."]])


 (panel
  [:h3 "Validating in " [:code "get-movie-times"] " context"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:id 101 :address {:zip 90210}} {:id id :first-name first-name :last-name last-name :address address})" 75 45)

  [:div.note
   [:p "Speculoos provides several utilities for " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#function-validation"} "validating function arguments and return values"] ", but to avoid introducing a new utility, we'll stick with " [:code "valid-scalars?"] " and I'll ask that you trust me that the function validation operates substantially the same way."]

   [:p "If we imagine that a function " [:code "get-movie-times"] " expects an ID and a zip, we could validate that slice of data."]])


 (panel
  [:h3 "Validating in " [:code "place-order"] " context"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:first-name \"Helen\" :last-name \"tis Troías\" :address {:street \"Equine Avenue\" :city \"Sparta\" :state :LCNIA :zip 54321}} {:id id :first-name first-name :last-name last-name :address address})" 55 45)

  [:div.note
   [:p "In the context of a different function, " [:code "place-order"] ", we might want to validate a first name, last name, and the full address. Validating that slice of data would look like this."]

   [:p "Exact same specification in both cases, " [:code "user"] ", but this time, a different slice of data was compared to specification. Because Speculoos only validates using predicates that are paired with scalars, the extra, un-paired predicates (in this example, " [:code ":id"] ") in scalar specification " [:code "user"] " have no effect."]

   [:p "Also note that the data is a heterogeneous, nested data structure (Mr Hickey calls it a 'tree'), and because Speculoos specifications mimic the shape of the data, the " [:code "user"] " scalar specification is also a tree. Speculoos can handle any depth of nesting, with any of Clojure's data structures (vectors, lists, sequences, maps, and sets)."]])


 (panel
  [:h3 "No requirements"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:sheep #{\"Fred\" \"Ethel\"} :helicopters 1} {})" 60 45)

  [:div.note
   [:p "Speculoos will happily validate data with an empty specification."]

   [:p "Validating with an empty specification will always return " [:code "true"] ". That behavior is governed by ignoring un-paired scalars (i.e., there are no predicates to pair with), and zero un-satisfied predicates is considered " [:em "valid"] ". Speculoos is 'open' in the sense that Mr Hickey discusses: Extra information is okay. Speculoos merely ignores it if it isn't paired with a predicate."]])


 (panel
  [:h3 "Generating random samples"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.utility :refer [exercise]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(exercise {:sheep #{\"Fred\" \"Ethel\" \"Lucy\" \"Ricky\" \"Little Ricky\"} :helicopters pos-int?} 5)" 75 55)

  [:div.note
   [:p "Speculoos can generate valid test data if we supply it with a scalar specification."]])


 (panel
  [:h3 "Programmatically manipulating specifications: extracting the address"]

  (prettyfy-form-prettyfy-eval "(get {:id int? :first-name string? :last-name string? :address {:street string? :city string? :zip int? :state keyword?}} :address)" 45 45)

  [:div.note
   [:p "We " [:a {:href "#flexible"} "discussed"] " this earlier. Speculoos specifications are plain Clojure data structures containing plain predicate functions. Slice and dice them however we want."]

   [:p "Extract a slice of a specification, perhaps just the address."]

   [:p "You can quickly make your own *ad hoc* specification registry."]])


 (panel
  [:h3 "Programmatically manipulating specifications: tightening ID requirements"]

  (prettyfy-form-prettyfy-eval "(assoc {:id int? :first-name string? :last-name string? :address {:street string? :city string? :zip int? :state keyword?}} :id even?)" 45 45)

  [:div.note
   [:p "Alter a portion of a specification, perhaps by tightening the requirements of the ID."]

   [:p "We could, on-the-fly, require " [:code ":id"] " to be a positive integer by invoking " [:code "valid-scalars?"] " with that " [:code "assoc"] "-ed specification. The original specification is immutable as always, and remains unchanged. But at that moment of validation, the requirements were tightened."]

   [:p "No need to write a macro. Just manipulate Clojure data with our favorite utilities."]])


 (panel
  [:h3 "Function validation, nailing down everything, & final thoughts"]

  [:div.note
   [:p "Speculoos has an " [:a {:href "https://blosavio.github.io/speculoos/speculoos.function-specs.html"} "entire namespace"] " dedicated to validating function arguments and return values. Function validation follows all the same principles we've been discussing about validating data. It's a lengthy topic, so I'll refer to the " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#function-validation"} "documentation on the subject"] "."]

   [:p "Nail down everything!"]

   [:p "I hope at this point I've made a convincing case that Speculoos is open and permissive: Only specify what we're interested in, and Speculoos will ignore the rest. There is no requirement that we describe every possible facet of our data. Whatever small amount we " [:em "do"] " specify, can be used to validate our data, and generate test samples, etc."]

   [:p "It's fortunate that Speculoos' implementation details combined with a few policy decisions resulted in being able to address most all of " [:em "Maybe Not"] "'s concerns. I don't claim that Speculoos is the only solution to these issues, but that the principles under which Speculoos operates provides one possible solution."]

   [:p [:a {:href "https://github.com/blosavio"} "Let me know"] " what you think."]])


 (whats-next-panel
  perhaps-index
  [:div.note "What's next speaker note within 'Perhaps So' screencast..."])
 ]