[:section#non-terminating-sequences
 [:h2 "Non-terminating sequences"]

 [:p "Speculoos absorbs lots of power from Clojure's infinite, lazy sequences. That power stems from the fact that Speculoos only validates complete pairs of datums and predicates. Datums without predicates are not validated, and predicates without datums are ignored. That policy provides optionality in our data. If a datum is present, it is validated against its corresponding predicate, but if that datum is non-existent, it is not required."]

 [:p "In the following examples, the first argument in the upper row is the data, the second argument in the lower row is the specification."]

 [:pre
  [:code ";; un-paired scalar predicates"]
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [42] [int? keyword? char?])" 40 40)
  [:br]
  [:br]
  [:br]
  [:code ";; un-paired scalar datums"]
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [42 :foo \\z] [int?])" 30 40)]

 [:p "We remember " [:a {:href "#mottos"} "Motto #3"] ": " [:em "Ignore un-paired predicates and un-paired datums. "] " In the first example, only the single integer " [:code "42"] " is validated because it was paired with predicate " [:code "int?"] "; the remaining two predicates, " [:code "keyword?"] " and " [:code "char?"] ", are un-paired, and therefore ignored. In the second example, only  " [:code "int?"] " participated in validation because it was the only predicate that pairs with a scalar. Scalars " [:code ":foo"] " and " [:code "\\z"] " were not paired with a predicate, and were therefore ignored. The fact that the specification vector is shorter than the data implies that any trailing, un-paired data elements are un-specified. We can take advantage of this fact by intentionally making either the data or the specification " [:em "run off the end"] "."]

 [:p "First, if we'd like to validate a non-terminating sequence, specify as many datums as necessary to capture the pattern. " [:code "repeat"] " produces multiple instances of a single value, so we only need to specify one datum."]

 [:pre (print-form-then-eval "(validate-scalars (repeat 3) [int?])" 30 40)]

 [:p "Despite " [:code "(repeat 3)"] " producing a non-terminating sequence of integers, only the first integer was validated because that's the only predicate supplied by the specification."]

 [:p [:code "cycle"] " can produce different values, so we ought to test for as many as appear in the definition."]

 [:pre (print-form-then-eval "(validate-scalars (cycle [42 :foo 22/7]) [int? keyword? ratio?])" 45 45)]

 [:p "Three unique datums. Only three predicates needed."]

 [:p "On the other side of the coin, non-terminating sequences serve a critical role in composing Speculoos specifications. They express " [:em "I don't know how many items there are in this sequence, but they all must satisfy these predicates"] "."]

 [:pre
  (print-form-then-eval "(valid-scalars? [1] (repeat int?))")
  [:br]
  (print-form-then-eval "(valid-scalars? [1 2] (repeat int?))")
  [:br]
  (print-form-then-eval "(valid-scalars? [1 2 3] (repeat int?))")
  [:br]
  (print-form-then-eval "(valid-scalars? [1 2 3 4] (repeat int?))")
  [:br]
  (print-form-then-eval "(valid-scalars? [1 2 3 4 5] (repeat int?))")]

 [:p "Basically, this idiom serves the role of a regular expression " [:code "zero-or-more"] ". Let's pretend we'd like to validate an integer, then a string, followed by any number of characters. We compose our specification like this."]

 [:pre
  [:code ";; use `concat` to append an infinite sequence of `char?`"]
  [:br]
  [:br]
  (print-form-then-eval "(validate-scalars [99 \"abc\" \\x \\y \\z] (concat [int? string?] (repeat char?)))" 65 40)
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(require '[speculoos.core :refer [only-invalid]])")
  [:br]
  [:br]
  [:br]
  [:code ";; string \"y\" will not satisfy scalar predicate `char?`; use `only-valid` to highlight invalid element"]
  [:br]
  [:br]
  (print-form-then-eval "(only-invalid (validate-scalars [99 \"abc\" \\x \"y\" \\z] (concat [int? string?] (repeat char?))))" 75 40)]

 [:p "Or perhaps we'd like to validate a function's argument list composed of a ratio followed by " [:code "&-args"] " consisting of any number of alternating keyword-string pairs."]

 [:pre
  [:code ";; zero &-args"]
  [:br]
  [:br]
  (print-form-then-eval "(valid-scalars? [2/3] (concat [ratio?] (cycle [keyword string?])))" 65 40)
  [:br]
  [:br]
  [:br]
  [:code ";; two pairs of keyword+string optional args"]
  [:br]
  [:br]
  (print-form-then-eval "(valid-scalars? [2/3 :opt1 \"abc\" :opt2 \"xyz\"] (concat [ratio?] (cycle [keyword string?])))")
  [:br]
  [:br]
  [:br]
  [:code ";; one pair of optional args; 'foo does not satisfy `string?` scalar predicate"]
  [:br]
  [:br]
  (print-form-then-eval "(only-invalid (validate-scalars [2/3 :opt1 'foo] (concat [ratio?] (cycle [keyword string?]))))")]

 [:p "Using non-terminating sequences this way sorta replicates " [:code "spec.alpha"] "'s sequence regexes. I think of it as Speculoos' super-power."]

 [:p "Also, Speculoos can handle nested, non-terminating sequences."]

 [:pre (print-form-then-eval "(valid-scalars? [[1] [2 \"2\"] [3 \"3\" :3]] (repeat (cycle [int? string? keyword?])))")]

 [:p "This specification is satisfied with a " [:em "Possibly infinite sequence of arbitrary-length vectors, each vector containing a pattern of an integer, then a string, followed by a keyword"] "."]

 [:p "One detail that affects usage: A non-terminating sequence must not appear at the same path within both the data and specification. I am not aware of any method to inspect a sequence to determine if it is infinite, so Speculoos will refuse to validate a non-terminating data sequence at the same path as a non-terminating predicate sequence, and " [:em "vice versa"] ". However, feel free to use them in either data or in the specification, as long as they live at different paths."]

 [:pre
  [:code ";; data's infinite sequence at :a, specification's infinite sequence at :b"]
  [:br]
  [:br]
  (print-form-then-eval "(valid-scalars? {:a (repeat 42) :b [22/7 true]} {:a [int?] :b (cycle [ratio? boolean?])})")
  [:br]
  [:br]
  [:br]
  [:code ";; demo of some invalid scalars"]
  [:br]
  [:br]
  (print-form-then-eval "(only-invalid (validate-scalars {:a (repeat 42) :b [22/7 true]} {:a [int? int? string?] :b (repeat ratio?)}))")]

 [:p "In both cases above, the data contains a non-terminating sequence at key " [:code ":a"] ", while the specification contains a non-terminating sequence at key " [:code ":b"] ". Since in both cases, the two infinite sequences do not share a path, validation can proceed to completion."]

 [:p "So what's going on? Internally, Speculoos finds all the potentially non-terminating sequences in both the data and the specification. For each of those hits, Speculoos looks into the other nested structure to determine how long the counterpart sequence is. Speculoos then " [:em "clamps"] " the non-terminating sequence to that length. Validation proceeds with the clamped sequences. Let's see the clamping in action."]

 [:pre
  (print-form-then-eval "(require '[speculoos.core :refer [expand-and-clamp-1]])")
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(expand-and-clamp-1 (range) [int? int? int?])")]

 [:p [:code "range"] " would have continued merrily on forever, but the clamp truncated it at three elements, the length of the second argument vector. That's why two non-terminating sequences at the same path are not permitted. Speculoos has no way of knowing how short or long the sequences ought to be, so instead of making a bad guess, it throws the issue back to us. The way " [:em "we"] " indicate how long it should be is by making the counterpart sequence a specific length. Where should Speculoos clamp that " [:code "(range)"] " in the above example? The answer is the length of the other sequential thing, " [:code "[int? int? int?]"] ", or three elements."]

 [:p "Speculoos' " [:a {:href "#utilities"} "utility"] " namespace provides a " [:code "clamp-in*"] " tool for us to clamp any sequence within a homogeneous, arbitrarily-nested data structure. We invoke it with a pattern of arguments similar to " [:code "clojure.core/assoc-in"] "."]

 [:pre
  (print-form-then-eval "(require '[speculoos.utility :refer [clamp-in*]])")
  [:br]
  [:br]
  [:br]
  (print-form-then-eval "(clamp-in* {:a 42 :b ['foo 22/7 {:c (cycle [3 2 1])}]} [:b 2 :c] 5)" 55 55)]

 [:p [:code "clamp-in*"] " used the path " [:code "[:b 2 :c]"] " to locate the non-terminating " [:code "cycle"] " sequence, clamped it to " [:code "5"] " elements, and returned the new data structure with that terminating sequence, converted to a vector. This way, if Speculoos squawks at us for having two non-terminating sequences at the same path, we have a way to clamp the data, specification, or both at any path, and validation can proceed."]

 [:p "Be sure to set your development environment's printing length"]

 [:pre (print-form-then-eval "(set! *print-length* 99)")]

 [:p "or you may jam up your session."]]
