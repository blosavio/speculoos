[:section#mantras
 [:h2 "Mantras"]
 [:p "When using Speculoos, remember these three Mantras:"
  [:ol
   [:li "Validate scalars separately from validating collections."]
   [:li "Shape the specification to mimic the data."]
   [:li "Ignore un-paired predicates and un-paired datums."]]]
 
 [:p "Speculoos provides functions for validating scalars contained within a heterogeneous, arbitrarily-nested data structure, and another, distinct set of functions for validating properties of those nested collections. Validating scalars separately from validating collections carries several advantages. First, Speculoos can consume specifications composed of regular Clojure data structures. Inspect and manipulate your specification with any Clojure collection-handling functions you prefer. Second, separating the two offers mental clarity about what's going on. Your predicates will only ever apply to a scalar, or to a collection, nevern both. Third, you only need to specify as much, or little, as necessary. If you only want to validate a few scalars, you won't be forced to specify anything converning a collection."]
 
 [:p "Speculoos aims to make composing specifications straightforward, and inspecting them transparent. A Speculoos specification is merely an arrangement of nested vectors, lists, maps, sequences, and sets that contain predicates. Those predicates are arranged in a pattern that instruct the validation functions where to apply the predicates. The specification for a vector is a vector. The specification for a map, is itself a map. There's a nearly one-to-one correspondence between the shape of the data and the shape of the specification. Speculoos specifications aim to be intuitive to peek at by eye, but also amenable to alteration. You can use your favorite Clojure data wrangling functions to tighten, relax, or remove portions of a Speculoos specification."]
 
 [:p "Speculoos provides flexibility, power, and reusability of specifications by ignoring datums that do not have a corresponding predicate in the specification and ignoring predicates that do not have a corresponding datum in the data. Maybe in your role in an assembly line, you only care about some slice of a large chunk of data. Supplying predicates for only a subset of datums allows you to only validate those specified datums while being agnostic towards the other datums. Going in the other direction, maybe somebody shared a giant specification that describes data about a person, their postal address, their contact info, etc. Because a Speculoos specification is just a data structure with regular predicates, you can, on-the-fly, pull out the portion relevent to postal addresses and apply that to your instances of address data. Speculoos lets you specify exactly what elements you'd like to validate. No more, no less."]]