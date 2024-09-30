(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def sequence-index 9)


[:body
 (panel
  (screencast-title sequence-index "Non-terminating sequences")

  [:h4 "Creating arbitrarily-long sequences of pairs"]

  (prettyfy-form-prettyfy-eval "(validate-scalars [42] [int? keyword? char?])" 40 40)

  (prettyfy-form-prettyfy-eval "(validate-scalars [42 :foo \\z] [int?])" 30 40)

  [:div.note "Speculoos absorbs lots of power from Clojure's infinite, lazy sequences. That power stems from the fact that Speculoos only validates complete pairs of datums and predicates. Datums without predicates are not validated, and predicates without datums are ignored. That policy provides optionality in our data. If a datum is present, it is validated against its corresponding predicate, but if that datum is non-existent, it is not required."

   [:p "In the first example, only the single integer " [:code "42"] " is validated, the rest of the predicates are ignored. In the second example, only the " [:code "42"] "  was validated because the specification implies that any trailing elements are un-specified. We can take advantage of this fact by intentionally making either the data or the specification " [:em "run off the end"] "."]])


 (panel
  [:h3 "Validating a non-terminating, repeating sequence of scalars."]

  (prettyfy-form-prettyfy-eval "(validate-scalars (repeat 3) [int?])" 30 40)

  [:div.note "First, if we'd like to validate a non-terminating sequence, specify as many datums as necessary to capture the pattern. " [:code "repeat"] " produces multiple instances of a single value, so we only need to specify one datum."

   [:p "Despite " [:code "(repeat 3)"] " producing a non-terminating sequence of integers, only the first integer was validated because that's the only predicate supplied by the specification."]])


 (panel
  [:h3 "Validating a non-terminating, cycling sequence of scalars"]

  (prettyfy-form-prettyfy-eval "(validate-scalars (cycle [42 :foo 22/7]) [int? keyword? ratio?])" 45 45)

  [:div.note [:code "cycle"] " can produce different values, so we ought to test for as many as appear in the definition."

   [:p "Three unique datums. Only three predicates needed."]])


 (panel
  [:h3 "Creating specifications with non-terminating sequences of predicates"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [1] (repeat int?))")

  (prettyfy-form-prettyfy-eval "(valid-scalars? [1 2] (repeat int?))")

  (prettyfy-form-prettyfy-eval "(valid-scalars? [1 2 3] (repeat int?))")

  (prettyfy-form-prettyfy-eval "(valid-scalars? [1 2 3 4] (repeat int?))")

  (prettyfy-form-prettyfy-eval "(valid-scalars? [1 2 3 4 5] (repeat int?))")

  [:div.note "On the other side of the coin, non-terminating sequences serve a critical role in composing Speculoos specifications. They express " [:em "I don't know how many items there are in this sequence, but they all must satisfy these predicates"] "."

   [:p ]])


 (panel
  [:h3 "Specifying " [:em "Something, followed by any number of something else"]]

  (prettyfy-form-prettyfy-eval "(validate-scalars [99 \"abc\" \\x \\y \\z] (concat [int? string?] (repeat char?)))" 65 40)

  (prettyfy-form-prettyfy-eval "(speculoos.core/only-invalid (validate-scalars [99 \"abc\" \\x \"y\" \\z] (concat [int? string?] (repeat char?))))" 75 40)

  [:div.note
   "Basically, this idiom serves the role of a regular expression " [:code "zero-or-more"] ". Let's pretend we'd like to validate an integer, then a string, followed by any number of characters. We compose our specification like this."
   "use `concat` to append an infinite sequence of `char?`"
   "string \"y\" will not satisfy scalar predicate `char?`; use `only-valid` to highlight invalid element"])


 (panel
  [:h3 "Specifying " [:em "Something, followed by alternating pairs"]]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [2/3] (concat [ratio?] (cycle [keyword string?])))" 65 40)

  (prettyfy-form-prettyfy-eval "(valid-scalars? [2/3 :opt1 \"abc\" :opt2 \"xyz\"] (concat [ratio?] (cycle [keyword string?])))")

  (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars [2/3 :opt1 'foo] (concat [ratio?] (cycle [keyword string?]))))")

  [:div.note "Or perhaps we'd like to validate a function's argument list composed of a ratio followed by " [:code "&-args"] " consisting of any number of alternating keyword-string pairs."

   "zero &-args."
   "two pairs of keyword+string optional args."
   "one pair of optional args; 'foo does not satisfy `string?` scalar predicate"

   [:p "Using non-terminating sequences this way sorta replicates " [:code "spec.alpha"] "'s sequence regexes. I think of it as Speculoos' super-power."]])


 (panel
  [:h3 "Nested, non-terminating sequences"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? [[1] [2 \"2\"] [3 \"3\" :3]] (repeat (cycle [int? string? keyword?])))")

  [:div.note "Also, Speculoos can handle nested, non-terminating sequences."
   [:p "This specification is satisfied with a " [:em "Possibly infinite sequence of arbitrary-length vectors, each vector containing a pattern of an integer, then a string, followed by a keyword"] "."]])


 (panel
  [:h3 "Restriction: Non-terminating sequences can't share paths"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:a (repeat 42) :b [22/7 true]} {:a [int?] :b (cycle [ratio? boolean?])})")

  (prettyfy-form-prettyfy-eval "(speculoos.core/only-invalid (validate-scalars {:a (repeat 42) :b [22/7 true]} {:a [int? int? string?] :b (repeat ratio?)}))")

  [:div.note "One detail that affects usage: A non-terminating sequence must not appear at the same path within both the data and specification. I am not aware of any method to inspect a sequence to determine if it is infinite, so Speculoos will refuse to validate a non-terminating data sequence at the same path as a non-terminating predicate sequence, and " [:em "vice versa"] ". However, feel free to use them in either data or in the specification, as long as they live at different paths."

   "data's infinite sequence at :a, specification's infinite sequence at :b."
   "demo of some invalid scalars"

   [:p "In both cases above, the data contains a non-terminating sequence at key " [:code ":a"] ", while the specification contains a non-terminating sequence at key " [:code ":b"] ". Since in both cases, the two infinite sequences do not share a path, validation can proceed to completion."]])


 (panel
  [:h3 "How Speculoos' validators handle non-terminating sequences"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [expand-and-clamp-1]])")

  (prettyfy-form-prettyfy-eval "(expand-and-clamp-1 (range) [int? int? int?])")

  [:div.note "So what's going on? Internally, Speculoos finds all the potentially non-terminating sequences in both the data and the specification. For each of those hits, Speculoos looks into the other nested structure to determine how long the counterpart sequence is. Speculoos then " [:em "clamps"] " the non-terminating sequence to that length. Validation proceeds with the clamped sequences. Let's see the clamping in action."

   [:p [:code "range"] " would have continued merrily on forever, but the clamp truncated it at three elements, the length of the second argument vector. That's why two non-terminating sequences at the same path are not permitted. Speculoos has no way of knowing how short or long the sequences ought to be, so instead of making a bad guess, it throws the issue back to us. The way " [:em "we"] " indicate how long it should be is by making the counterpart sequence a specific length. Where should Speculoos clamp that " [:code "(range)"] " in the above example? The answer is the length of the other sequential thing, " [:code "[int? int? int?]"] ", or three elements."]])


 (panel
  [:h3 "Explicitly clamping non-terminating sequences to a finite length"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.utility :refer [clamp-in*]])")

  (prettyfy-form-prettyfy-eval "(clamp-in* {:a 42 :b ['foo 22/7 {:c (cycle [3 2 1])}]} [:b 2 :c] 5)" 55 55)

  [:div.note "Speculoos' " [:a {:href "#utilities"} "utility"] " namespace provides a " [:code "clamp-in*"] " tool for us to clamp any sequence within a homogeneous, arbitrarily-nested data structure. We invoke it with a pattern of arguments similar to " [:code "clojure.core/assoc-in"] "."

   [:p [:code "clamp-in*"] " used the path " [:code "[:b 2 :c]"] " to locate the non-terminating " [:code "cycle"] " sequence, clamped it to " [:code "5"] " elements, and returned the new data structure with that terminating sequence. This way, if Speculoos squawks at us for having two non-terminating sequences at the same path, we have a way to clamp the data, specification, or both at any path, and validation can proceed."]])


 (panel
  [:h3 "Keep your session from jamming"]

  (prettyfy-form-prettyfy-eval "(set! *print-length* 99)")

  [:div.note "Be sure to set your development environment's printing length"])


 (whats-next-panel
  sequence-index
  [:div.note "Non-terminating sequences hidden presenter notes..."])
 ]
