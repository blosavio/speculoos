[:section#intro
 [:h2 "Introduction"]

 [:p "Imagine we'd like to know if our " [:em "Clojure vector contains an integer, then a string, and finally a ratio"] ". One example of that data vector might look like this."]

 [:pre [:code  "[42 \"abc\" 22/7]"]]

 [:p "It would be nice if we could write a specification that is shaped like that data."]

 [:pre [:code "[int? string? ratio?]"]]

 [:p "Speculoos can validate our data vector with that specification vector."]

 [:pre (print-form-then-eval "(valid-scalars? [42 \"abc\" 22/7] [int? string? ratio?])" 40 80)]

 [:p "Notice how the specification's predicate functions in the the lower row line up with the data's values in the upper row. Integer " [:code "42"] " pairs with predicate " [:code "int?"] ", string " [:code "\"abc\""] " pairs with predicate " [:code "string?"] ", and ratio " [:code "22/7"] " pairs with predicate " [:code "ratio?"] ". All three scalar values satisfy their respective predicates, so the validation returns " [:code "true"] "."]

 [:p "Now imagine we'd like ensure our " [:em "Clojure hash-map contains an integer at key " [:code ":x"] " and a ratio at key " [:code ":y"]] ". Something like this."]

 [:pre [:code "{:x 42 :y 22/7}"]]

 [:p "We could write a specification map that's shaped like that data map."]

 [:pre [:code "{:x int? :y ratio?}"]]

 [:p "Speculoos can validate our data map with that specification map."]

 [:pre (print-form-then-eval "(valid-scalars? {:x 42 :y 22/7} {:x int? :y ratio?})" 40 80)]

 [:p "Again, the specification's predicate functions in the lower row correspond to the data's values in the upper row. Integer " [:code "42"] " at key " [:code ":x"] " pairs with predicate " [:code "int?"] " also at key " [:code ":x"] ", while ratio " [:code "22/7"] " at key " [:code ":y"] " pairs with predicate " [:code "ratio?"] " also at key " [:code ":y"] ". Both scalar values satisfy their respective predicates, so the validation returns " [:code "true"] "."]

 [:p "Notice how in both cases, the specifications mimic the shape of the data. The vector's specification is itself a vector. The map's specification is itself a map."]

 [:p "Speculoos can validate any heterogeneous, arbitrarily-nested data collection using specifications composed of plain Clojure collections and functions. In short, Speculoos is an experimental library that aims to perform the same tasks as " [:a {:href "https://clojure.org/about/spec"} [:code "clojure.spec.alpha"]] " with an intuitive interface that employs flexible and powerful specification literals."]]