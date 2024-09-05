(require '[speculoos.function-specs :refer [inject-specs!]]
         '[speculoos.utility :refer [defpred]])

[:section#exercising
 [:h2 "Generating Random Samples and Exercising"]

 [:p "Before we have some fun with random samples, we must create random sample generators and put them in particular spots. Random sample generators are closely related to predicates. A predicate is a thing that can answer " [:em "Is the value you put in my hand an even, positive integer between ninety and one-hundred?"] " A random sample generator is a thing that says " [:em "I'm putting in your hand an even, positive integer between ninety and one-hundred"] "."]

 [:p "Starting with a quick demonstration, Speculoos can generate valid data when given a scalar specification."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [data-from-spec]])")
  [:br]
  [:br]
  (print-form-then-eval "(data-from-spec [int? string? keyword?] :random)")]

 [:p "When dealing with the basic " [:code "clojure.core"] " predicates, such as " [:code "int?"] ", " [:code "string?"] ", " [:code "keyword?"]  ", etc., Speculoos provides pre-made random sample generators that satisfy those predicates. (There are a few exceptions, due to the fact that there is not a one-to-one-to-one correspondence between scalar data types, "  [:code "clojure.core"] " predicates, and " [:code "clojure.test.check"] " generators.)"]

 [:p "Speculoos can also generate random scalar samples from predicate-like things, such as regular expressions and sets."]

 [:pre
  [:code                "      built-in               v--- regex        v--- set-as-a-predicate"] [:br]
  [:code                "      predicate ----v"] [:br]
  (print-form-then-eval "(data-from-spec {:x int? :y #\"fo{3,6}bar\" :z #{:red :green :blue}} :random)")]

 [:p "When we use either a 'basic' scalar predicate, such as " [:code "int?"] ", a regex, or a set-as-a-predicate, Speculoos should know how to generate a valid random sample that satisfies that predicate-like thing. Within the context of generating samples or exercising, basic predicate " [:code "int?"] " elicits an integer, regular expression " [:code "#fo{3,6}"] " generates a valid string, and set-as-a-predicate " [:code "#{:red :green :blue}"] " emits a sample randomly drawn from that set."]

 [:h3#create-gen "Creating Sample Generators"]

 [:p "This document often uses 'basic' predicates like " [:code "int?"] " and " [:code "string?"] " because they're short to type and straightforward to understand. In real life, we'll want to specify our data with more precision. Instead of merely " [:em "An integer"] ", we'll often want to express " [:em "An even positive integer between ninety and one-hundred."] " To do that, we need to create custom generators."]

 [:p [:code "clojure.test.check"] " provides a group of powerful, flexible, generators."]

 [:pre
  (print-form-then-eval "(require '[clojure.test.check.generators :as gen])")
  [:br]
  [:br]
  (print-form-then-eval "(gen/generate (gen/large-integer* {:min 700 :max 999}))")
  [:br]
  [:br]
  (print-form-then-eval "(gen/generate gen/keyword)")
  [:br]
  [:br]
  (print-form-then-eval "(gen/generate gen/string-alphanumeric)")]

 [:h3#access-gen "Storing and Accessing Sample Generators"]

 [:p "The custom generators we discussed in the previous subsection are merely floating around in the ether. To use them for exercising, we need to put those generators in a spot that Speculoos knows: the predicate's metadata."]

 [:p "Let's imagine a scenario. We want a predicate that specifies an integer between ninety (inclusive) and one-hundred (exclusive) and a corresponding random sample generator. First, we write the predicate, something like this."]

 [:pre [:code "(fn [n] (and (int? n) (<= 90 n 99)))"]]

 [:p "Second, we write our generator."]

 [:pre
  [:code ";; produce ten samples"]
  (print-form-then-eval "(gen/sample (gen/large-integer* {:min 90 :max 99}))")
  [:br]
  [:br]
  [:code ";; produce one sample"]
  [:br]
  (print-form-then-eval "(gen/generate (gen/large-integer* {:min 90 :max 99}))")]

 [:p "To make the generator invocable, we'll wrap it in a function."]

 [:pre
  (print-form-then-eval "(defn generate-nineties [] (gen/generate (gen/large-integer* {:min 90 :max 99})))")
  [:br]
  [:br]
  [:code ";; invoke the generator"]
  [:br]
  (print-form-then-eval "(generate-nineties)")]

 [:p "Third, we need to associate that generator into the predicate's metadata. We have a couple of options. The manual option uses " [:code "with-meta"] " during binding a name to the function body. We'll associate " [:code "generate-nineties"] " to the predicate's " [:a {:href "#recognized-metadata-keys"} "metadata key"] " " [:code ":speculoos/predicate->generator"] "."]

 [:pre
  (print-form-then-eval "(def nineties? (with-meta (fn [n] (and (int? n) (<= 90 n 99))) {:speculoos/predicate->generator generate-nineties}))")
  [:br]
  [:br]
  (print-form-then-eval "(nineties? 92)")
  [:br]
  [:br]
  (print-form-then-eval "(meta nineties?)" 20 75)]

 [:p "That gets the job done, but the manual option is kinda cluttered. The other option involves a Speculoos utility, " [:code "defpred"] ", that " [:strong "def"] "ines a " [:strong "pred"] "icate much the same as " [:code "defn"] ", but associates the generator with less keyboarding than the " [:code "with-meta"] " option. Supply a symbol, a predicate function body, and a random sample generator."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [defpred]])")
  [:br]
  [:br]
  (print-form-then-eval "(defpred NINEties? (fn [n] (and (int? n) (<= 90 n 99))) generate-nineties)")
  [:br]
  [:br]
  (print-form-then-eval "(NINEties? 97)")
  [:br]
  [:br]
  (print-form-then-eval "(meta NINEties?)" 45 75)]

 [:p [:code "defpred"] " automatically puts " [:code "generate-nineties"] " into the predicate " [:code "NINEties?"] " metadata. " [:a {:href "#auto-sample"} "Soon"] ", we'll discuss another couple of benefits to using " [:code "defpred"] ". Whichever way we accomplished getting the generator into the metadata at " [:code ":speculoos/predicate->generator"] ", Speculoos can now find it."]

 [:p "Speculoos uses function metadata for two purposes, and it's important to keep clear in our minds which is which."

  [:ul
   [:li [:p "Store " [:em "function specifications"] " in the metadata for that function. For example, if we have a " [:code "reverse"] " function, we put the specification to test " [:code "equal-lengths?"] " in the metadata at " [:code ":speculoos/argument-return-relationships"] "."]]

   [:li [:p "Store " [:em "random sample generators"] " in the metadata for that predicate. If we have a " [:code "nineties?"] " predicate, we put the random sample generator " [:code "generate-nineties"] " in the metadata at " [:code ":speculoos/predicate->generator"] "."]]]]

 [:h3#auto-sample "Creating Sample Generators Automatically"]

 [:p [:code "defpred"] " does indeed relieve us of some tedious keyboarding, but it offers another benefit. If we arrange the predicate definition according to " [:code "defpred"] "'s expectations, it can automatically create a random sample generator for that predicate. Let's see it in action and then we'll examine the details."]

 [:pre
  (print-form-then-eval "(defpred auto-nineties? (fn [n] (and (int? n) (<= 90 n 99))))")
  [:br]
  [:br]
  [:code "(meta auto-nineties?)\n;; => #:speculoos{:canonical-sample :auto-nineties?-canonical-sample,\n                  :predicate->generator #fn--88795}"]]

 [:p "Well, there's certainly " [:em "something"] " at " [:code ":speculoos/predicate->generator"] ", but is it anything useful?"]

 [:pre (print-form-then-eval "(binding [speculoos.utility/*such-that-max-tries* 1000]
                                 (let [possible-gen-90 (:speculoos/predicate->generator (meta auto-nineties?))]
                                   (possible-gen-90)))")]

 [:p "Yup! Since it is not-so-likely that a random integer generator would produce a value in the nineties, we bound the " [:code "max-tries"] " to a high count to give the generator lots of attempts. We then pulled out the generator from predicate " [:code "auto-nineties?"] "'s metadata and bound it to " [:code "possible-gen-90"] ". Then we invoked " [:code "possible-gen-90"] " and, in fact, it generated an integer in the nineties that satisfies the original predicate we defined as " [:code "auto-nineties"] "." [:code "defpred"] " automatically created a random sample generator whose output satisfies the predicate."]

 [:p "For " [:code "defpred"] " to do its magic, the predicate definition must follow a few patterns."]

 [:ul
  [:li "We must provide the textual representation of the definition. We can't merely assign another already-defined function."]
  [:li "The first symbol must be " [:code "and"] ", " [:code "or"] ", or a basic predicate for a Clojure built-in scalar, such as " [:code "int?"] ", that is registered at " [:code "speculoos.utility/predicate->generator"] "."
   [:pre
    [:code "(and (...)) ;; okay"] [:br]
    [:code "(or (...)) ;; okay"] [:br]
    [:code "(int? ...) ;; okay"] [:br]
    [:code "(let ...) ;; not okay"] [:br]]]
  [:li "The first clause after " [:code "and"] " and all immediate descendants of " [:code "or"] " must start with a basic predicate described above."]]

 [:p "Subsequent clauses of " [:code "and"] " will be used to create " [:code "test.check.generators/such-that"] " modifiers. Direct descendants of a top-level " [:code "or"] " will produce" [:code "n"] " separate random sample generators, each with " [:code "1/n"] " probability."]

 [:p "Speculoos exposes the internal tool " [:code "defpred"] " uses to create a generator, so we can inspect how it works. (I've lightly edited the output for clarity.)"]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [inspect-fn]])")
  [:br]
  [:br]
  [:code "(inspect-fn '(fn [i] (int? i)))"]
  [:br]
  [:code ";; => gen/small-integer"]]

 [:p "We learn that " [:code "inspect-fn"] " examines the textual representation of the predicate definition, extracts " [:code "int?"] " and infers that the base generator ought to be " [:code "gen/small-integer"] ". Next, we'll add a couple of modifiers with " [:code "and"] ". " [:code "int?"] " is in the first clause. (Again, lightly edited.)"]

 [:pre
  [:code "(inspect-fn '(fn [i] (and (int? i) (even? i) (pos? i))))"]
  [:br]
  [:code ";; => (gen/such-that (fn [i] (and (even? i) (pos? i)))\n;;       gen/small-integer {:max-tries speculoos.utility/*such-that-max-tries*}) "]]

 [:p [:code "int?"] " elicits a small-integer generator. " [:code "inspect-fn"] " then uses the subsequent clauses of the " [:code "and"] " expression to create a " [:code "such-that"] " modifier that generates only positive, even numbers."]

 [:p "Let's see what happens with an " [:code "or"] "."]

 [:pre
  [:code "(inspect-fn '(fn [x] (or (int? x) (string? x))))"]
  [:br]
  [:code ";; => (gen/one-of [gen/small-integer gen/string-alphanumeric])"]]

 [:p "Our predicate definition is satisfied with either an integer or a string. " [:code "inspect-fn"] " therefore creates a generator that will produce either an integer or a string with equal probability."]

 [:p "When automatically creating random sample generators, " [:code "defpred"] " handles nesting up to two levels deep. Let's see how we might combine both " [:code "or"] " and " [:code "and"] ". We'll define a predicate that tests for either an odd integer, a string of at least three characters, or a ratio greater than one-ninth."]

 [:pre
  (print-form-then-eval "(defpred combined-pred #(or (and (int? %) (odd? %)) (and (string? %) (<= 3 (count %))) (and (ratio? %) (< 1/9 %))))")
  [:br]
  [:br]
  (print-form-then-eval "(data-from-spec {:a combined-pred :b combined-pred :c combined-pred :d combined-pred :e combined-pred :f combined-pred :g combined-pred :h combined-pred :i combined-pred} :random)" 45 55)]

 [:p "We're kinda abusing " [:code "data-from-spec"] " here to generate nine samples. Inferring from " [:code "combined-pred"] "'s predicate structure, " [:code "defpred"] "'s automatically-created random sample generator emits one of three elements with equal probability: an odd integer, a string of at least three characters, or a ratio greater than one-ninth. All we had to do was write the predicate; "  [:code "defpred"] " wrote all three random sample generators."]

 [:h3#test-gen "Testing Sample Generators Residing in Metadata"]

 [:p "Some scenarios block us from using " [:code "defpred"] "'s automatic generators. We may not have access to the textual representation of the predicate definition. Or, sometimes we must hand-write a generator because a naive generator would be unlikely to find a satisfying value (e.g., a random number that must fall within a narrow range)."]

 [:p "The Write-generator-then-Apply-to-metadata-then-Test loop can be tedious, so the " [:code "utility"] " namespace provides a tool to help. " [:code "validate-predicate->generator"] " accepts a predicate function we supply, extracts the random sample generator residing in its metadata, generates a sample, and then feeds that sample back into the predicate to see if it satisfies."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [validate-predicate->generator]])")
  [:br]
  [:br]
  (print-form-then-eval "(defpred pred-with-incorrect-generator (fn [i] (int? i)) #(gen/generate gen/ratio))")
  [:br]
  [:br]
  (print-form-then-eval "(validate-predicate->generator pred-with-incorrect-generator)")]

 [:p "We defined scalar predicate " [:code "pred-with-incorrect-generator"] " to require an integer, but, using " [:code "defpred"] ", we manually created a generator that emits ratio values. Each of the generated samples fails to satisfy the " [:code "int?"] " predicate."]

 [:p "With help from " [:code "validate-predicate->generator"] ", we can hop back and forth to adjust the hand-made generator."]

 [:pre
  (print-form-then-eval "(defpred pred-with-good-generator (fn [i] (int? i)) #(gen/generate gen/small-integer))")
  [:br]
  [:br]
  (print-form-then-eval "(validate-predicate->generator pred-with-good-generator)")]

 [:p "In this particular case, we could have relied on " [:code "defpred"] " to " [:a {:href "#auto-sample"} "create a sample generator"] " for us."]

 [:p "Pretend somebody hands us a specification. It might be useful to know if we need to write a random sample generator for any of the predicates it contains, or if Speculoos can find a generator for all of them, either in the collection of known predicates-to-generators associations, or in the predicates' metadata. " [:code "unfindable-generators"] " tells us this information."]

 [:p "Let's compose a scalar specification containing " [:code "int?"] ", a set-as-a-predicate " [:code "#{:red :green :blue}"] ", and a regular expression " [:code "#\"fo{2,5}\""] "."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [unfindable-generators]])")
  [:br]
  [:br]
  (print-form-then-eval "(unfindable-generators [int? #{:red :green :blue} #\"fo{2,5}\"])")]

 [:p "Speculoos knows how to create random samples from all three of those predicate-like things, so " [:code "unfindable-generators"] " returns an empty vector, " [:em "nothing unfindable"] ". Now, let's make a scalar specification with three predicates that intentionally lack generators."]

 [:pre
  (print-form-then-eval "(def a? (fn [] 'a))")
  [:br]
  (print-form-then-eval "(def b? (fn [] 'b))")
  [:br]
  (print-form-then-eval "(def c? (fn [] 'c))")
  [:br]
  [:br]
  (print-form-then-eval "(unfindable-generators [a? b? c?])")]

 [:p [:code "unfindable-generators"] " informs us that if we had tried to do a task that " [:a {:href "#using-gen"} "uses a sample generator"] ", we'd have failed. With this knowledge, we could go back and add random sample generators to " [:code "a?"] ", " [:code "b?"] ", and " [:code "c?"] "."]

 [:h3#using-gen "Using Sample Generators"]

 [:p "Speculoos can do three things with random sample generators."
  [:ul
   [:li "Create a heterogeneous, arbitrarily-nested data structure when given a scalar specification."]
   [:li "Exercise a scalar specification."]
   [:li "Exercise a function with a scalar specification."]]]

 [:p "The first, creating a valid set of data from a given scalar specification, provides the foundation of the later two exercising functions, so we'll begin with " [:code "data-from-spec"] "."]

 [:p "Imagine we'd like to specify the scalars contained within a vector to be an integer, followed by a ratio, followed by a double-precision floating-point number. We've seen " [:a {:href "#scalar-validation"} "how to compose that scalar specification"] ". Let's give that scalar specification to " [:code "data-from-spec"] "."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [data-from-spec]])")
  [:br]
  [:br]
  (print-form-then-eval "(data-from-spec [int? ratio? double?] :random)")]

 [:p "That scalar specification contains three predicates, and each of those predicates targets a basic Clojure numeric type, so Speculoos automatically refers to " [:code "test.check"] "'s generators to produce a random sample."]

 [:p "Let's try another example. The scalar specification will be a map with three keys associated with predicates for a character, a set-as-a-predicate, and a regex-predicate."]

 [:pre (print-form-then-eval "(data-from-spec {:x char? :y #{:red :green :blue} :z #\"fo{3,5}bar\"} :random)" 45 25)]

 [:p "Again, without any further assistance, " [:code "data-from-spec"] " knew how to find or create a random sample generator for each predicate in the scalar specification. " [:code "char?"] " targets a basic Clojure type, so it generated a random character. Sets in a scalar specification, in this context, are considered a membership predicate. The random sample generator is merely a random selection of one of the members. Finally, Speculoos regards a regular expression as a predicate for validating strings. " [:code "data-from-spec"] " consults the " [:a {:href "https://github.com/weavejester/re-rand"} [:code "re-rand"]] " library to generate a random string from the regular expression."]

 [:p "If our scalar specification contains custom predicates, we'll have to provide a little more information. We'll make another scalar specification containing a positive, even integer…"]

 [:pre (print-form-then-eval "(defpred pos-even-int? (fn [i] (and (int? i) (pos? i) (even? i))))")]

 [:p "…relying on " [:code "defpred"] "'s predicate inspection machinery to infer a generator. After making our " [:code "pos-even-int?"] " predicate, we'll make a predicate satisfied by a three-character string, " [:code "(fn [s] (and (string? s) (= 3 (count s))))"] ". The generator which " [:code "defpred"] " would create for that predicate is kinda naive."]

 [:pre
  [:code "(inspect-fn '(fn [s] (and (string? s) (= 3 (count s)))))"]
  [:br]
  [:code ";; => (gen/such-that (fn [s] (and (= 3 (count s)))) gen/string-alphanumeric)"]
  [:br]
  [:code ";; …output elided…"]]

 [:p "That naive generator would produce random strings of random lengths until it found one exactly three characters long. It's possible it would fail to produce a valid value before hitting the " [:code "max-tries"] " limit. However, we can explicitly write a generator and attach it with " [:code "defpred"] "."]

 [:pre (print-form-then-eval "(defpred three-char-string? (fn [s] (and (string? s) (= 3 (count s)))) #(clojure.string/join (gen/sample gen/char-alphanumeric 3)))")]

 [:p "Now that we have two scalar predicates with custom sample generators — one created by " [:code "defpred"] ", one created by us — we'll bring them together into a single scalar specification and invoke " [:code "data-from-spec"] "."]

 [:pre (print-form-then-eval "(data-from-spec [pos-even-int? three-char-string?] :random)")]

 [:p [:code "data-from-spec"] " generates a valid data set whose randomly-generated scalars satisfy the scalar specification. In fact, we can feed the generated data back into the specification and it ought to validate " [:code "true"] "."]

 [:pre (print-form-then-eval "(speculoos.core/valid-scalars? (data-from-spec [int? ratio? double?]) [int? ratio? double?])")]

 [:p "Perhaps it would be nice to do that multiple times in a row: generate some random data from a specification and feed it back into the specification to see if it validates. Don't go off and write your own utility. Speculoos can "  [:em "exercise"] " a scalar specification."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [exercise]])")
  [:br]
  [:br]
  (print-form-then-eval "(exercise [int? ratio? double?])" 55 75)]

 [:p "Ten times, " [:code "exercise"] " generated a vector containing an integer, ratio, and double-precision numbers, then performed a scalar validation using those random samples as the data and the original scalar specification. In each of those ten runs, we see that " [:code "exercise"] " generated valid, " [:code "true"] " data."]

 [:p "So now we've seen that Speculoos can repeatedly generate random valid data from a scalar specification and run a validation of that random data. If we have injected an argument scalar specification into a function's metadata, Speculoos can repeatedly generate specification-satisfying arguments and repeatedly invoke that function."]

 [:p "We revisit our friend, " [:code "sum-three"] ", a function which accepts three numbers and sums them. That scalar specification we've been using mimics the shape of the argument sequence, so let's inject it into " [:code "sum-three"] "'s metadata."]

 [:pre
  (print-form-then-eval "(defn sum-three [x y z] (+ x y z))")
  [:br]
  [:br]
  (print-form-then-eval "(inject-specs! sum-three {:speculoos/arg-scalar-spec [int? ratio? double?]})")]

 [:p [:code "sum-three"] " is certainly capable of summing any three numbers we feed it, but just for fun, we specify that the arguments ought to be an integer, a ratio, and a double-precision number. Now that we've defined our function and added an argument scalar specification, let's exercise " [:code "sum-three"] "."]

 [:pre
  (print-form-then-eval "(require '[speculoos.function-specs :refer [exercise-fn]])")
  [:br]
  [:br]
  (print-form-then-eval "(exercise-fn sum-three)" 55 75)]

 [:p [:code "int?"] ", " [:code "ratio?"] ", and " [:code "double?"] " all have built-in generators, so we didn't have to create any custom generators. " [:code "exercise-fn"] " extracted " [:code "sum-three"] "'s argument scalar specification, then, ten times, generated a data set from random sample generators, then invoked the function with those arguments."]

 [:h3#canonical "Canonical Samples"]

 [:p "Sometimes it might be useful that a generated value be predictable. Perhaps we're writing documentation, or making a presentation, and we'd like the values to be aesthetically pleasing. Or, sometimes during development, it's nice to be able to quickly eyeball a known value."]

 [:p "Speculoos provides a canonical sample for many of Clojure's fundamental scalars when the relevant functions are invoked with the " [:code ":canonical"] " option. Here we use " [:code "data-from-spec"] " to illustrate the built-in canonical values of six of the basic scalars."]

 [:pre (print-form-then-eval "(data-from-spec {:x int? :y char? :z string? :w double? :q ratio? :v keyword?} :canonical)" 45 45)]

 [:p "The two exercising functions, " [:code "exercise"] " and " [:code "exercise-fn"] " both accept the " [:code ":canonical"] " option, as well."]

 [:pre
  (print-form-then-eval "(exercise [int? ratio? double?] :canonical)")
  [:br]
  [:br]
  (print-form-then-eval "(exercise-fn sum-three :canonical)")]

 [:p "Since the canonical values don't vary, it doesn't make much sense to exercise more than once."]

 [:p "Beyond the built-in canonical values, we can supply canonical values of our own choosing when we define a predicate. We can manually add the canonical values via " [:code "with-meta"] " or we can add a canonical value using " [:code "defpred"] " as an argument following a custom generator."]

 [:pre
  (print-form-then-eval "(defpred neg-odd-int? (fn [i] (and (int? i) (neg? i) (odd? i))) (constantly :ignored) -33)" 55 90)
  [:br]
  [:br]
  (print-form-then-eval "(defpred happy-string? (fn [s] (string? s)) (constantly :ignored) \"Hello Clojure!\")" 55 90)
  [:br]
  [:br]
  (print-form-then-eval "(defpred pretty-number? (fn [n] (number? n)) (constantly :ignored) 123.456)" 35 90)
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(data-from-spec [neg-odd-int? happy-string? pretty-number?] :canonical)")]

 [:p "We see that " [:code "data-from-spec"] " found the custom canonical values for each of the three predicates: " [:code "-33"] " for " [:code "neg-odd-int?"] ", " [:code "\"Hello Clojure!\""] " for " [:code "happy-string?"] ", and " [:code "123.456"] " for " [:code "pretty-number?"] ". Notice that " [:em "exercising"] " a function does not validate the arguments or returns. Function argument and return validation only occurs when we explicitly invoke " [:code "validate-fn-with"] ", " [:code "validate-fn"] ", or we intentionally instrument it."]
 ]