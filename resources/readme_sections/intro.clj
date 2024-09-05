[:section#intro
 [:h2 "Introduction"]

 [:p "Imagine you'd like to know if " [:em "My Clojure vector contains an integer, then a string, and finally a ratio"] ". One example of that data vector might look like this."]

 [:pre [:code  "[42 \"abc\" 22/7]"]]

 [:p "It would be nice if we could write a specification that is shaped like that data."]

 [:pre [:code "[int? string? ratio?]"]]

 [:p "Speculoos can validate our data vector with that specification vector."]

 [:pre (print-form-then-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 40 80)]

 [:p "Now imagine we'd like ensure we have " [:em "A Clojure hash-map with an integer at key " [:code ":x"] " and a ratio at key " [:code ":y"]] ". Something like this."]

 [:pre [:code "{:x 42 :y 22/7}"]]

 [:p "We could write a specification map that's shaped like that data map."]

 [:pre [:code "{:x int? :y ratio?}"]]

 [:p "Speculoos can validate our data map with that specification map."]

 [:pre (print-form-then-eval "(valid-scalars? {:x 42 :y 22/7} {:x int? :y ratio?})" 40 80)]

 [:p "Notice how in both cases, the specifications mimic the shape of the data. The vector's specification is itself a vector. The map's specification is itself a map."]

 [:p "Speculoos can validate any heterogeneous, arbitrarily-nested data collection using specifications composed of plain Clojure collections and functions. In short, Speculoos is an experimental library that aims to perform the same tasks as " [:a {:href "https://clojure.org/about/spec"} [:code "clojure.spec.alpha"]] " with an intuitive interface that employs flexible and powerful specification literals."]]