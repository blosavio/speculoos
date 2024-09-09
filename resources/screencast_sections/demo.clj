[:body
 (panel
  [:h1 "Speculoos Title"]
  [:h3 [:em "An experiment with Clojure specification literals"]]
  [:p.note  "Imagine you'd like to know if " [:em "My Clojure vector contains an integer, then a string, and finally a ratio"] ". One example of that data vector might look like this."]
  [:a {:href "https://example.com"} "Test alert. Click Me."])

 (panel
  [:h1 "Speculoos TITLE"]
  [:p [:em "An experiment with Clojure specification literarals"]]
  [:p "Imagine..."])


 (panel
  [:h2 "Speculoos Info Slide"]
  [:pre [:code "[42 \"abc\" 22/7]"]]
  [:pre [:code "[int? string? ratio?]"]]
  [:p.note "It would be nice if we could write a specification that is shaped like that data. Speculoos can validate our data vector with that specification vector."])

 (panel
  [:h2 "Speculoos Plain Slide"]
  [:p "Three Mantras..."]
  [:div.notes "Remember to mention..."])

 (panel
  [:h2 "Speculoos auto-generated eval slide"]
  (prettyfy-form-prettyfy-eval "(speculoos.core/validate-scalars [42 \"abc\" 22/7] [int? string? ratio?])")
  [:p "Here's what we've done..."]
  [:div.notes "Remember to mention..."])

 (panel
  [:h2 "Speculoos Eval Slide"]
  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [valid-scalars?]])")
  (prettyfy-form-prettyfy-eval "(speculoos.core/validate-scalars [42 \"abc\" 22/7] [int? string? ratio?])" 55 45)
  [:p.note (random-sentence)])

 (panel
  [:h2 "Speculoos Info (i.e., non-eval) Slide"]
  [:pre [:code "{:x 42 :y 22/7}"]]
  [:pre [:code "{:x int? :y ratio?}"]]
  [:p.note "Now imagine we'd like ensure we have " [:em "A Clojure hash-map with an integer at key " [:code ":x"] " and a ratio at key " [:code ":y"]] ". Something like this. We could write a specification map that's shaped like that data map."])

 (panel
  [:h2 "Speculoos Eval Slide #2"]
  (prettyfy-form-prettyfy-eval "(valid-scalars? {:x 42 :y 22/7} {:x int? :y ratio?})" 40 80)
  [:p "Speculoos can validate our data map with that specification map. Notice how in both cases, the specifications mimic the shape of the data. The vector's specification is itself a vector. The map's specification is itself a map."])

 (panel
  [:h2 "Speculoos Side-by-Side Eval Slide #3"]
  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 40 80)]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(valid-scalars? {:x 42 :y 22/7} {:x int? :y ratio?})" 40 80)]]
  [:p "Speculoos can validate any heterogeneous, arbitrarily-nested data collection using specifications composed of plain Clojure collections and functions. In short, Speculoos is an experimental library that aims to perform the same tasks as " [:a {:href "https://clojure.org/about/spec"} [:code "clojure.spec.alpha"]] " with an intuitive interface that employs flexible and powerful specification literals."])
 ]