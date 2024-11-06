(require '[speculoos-hiccup :refer [panel prettyfy-form-prettyfy-eval screencast-title]]
         '[speculoos-project-screencast-generator :refer [whats-next-panel]])


(def collection-validation-advanced-index 4)


[:body
 (panel
  (screencast-title collection-validation-advanced-index "Collection Validation, Advanced")

  [:div.note
   [:p "In the previous screencast, we discussed the collection validation using some basic examples. Vectors --- both flat and nested, and maps --- both flat and nested. Each of the examples used one predicate targeting one collection."]

   [:p "Because of the way the algorithm works, Speculoos collection validation can optionally involve multiple predicates targeting one collection. This never violates any of the three mottos, but it may be occasionally useful. So to know when and how to use that pattern --- and to possibly diagnose a confusing result is we mis-construct a specification --- let's start by reviewing some principles."]])

 (panel
  [:h3 "Scalars versus Collections"]

  [:table
   [:tr
    [:td "scalars"]
    [:td [:code
          [:span.de-highlight "["]
          [:span.highlight    "42"]
          [:span.de-highlight " {:x "]
          [:span.highlight    "\"abc\""]
          [:span.de-highlight " :y "]
          [:span.highlight    "22/7"]
          [:span.de-highlight "}]"]
          ]]]

   [:tr
    [:td "collections"]
    [:td [:code
          [:span.highlight "["]
          [:span.de-highlight "42"]
          [:span.highlight " {:x "]
          [:span.de-highlight "\"abc\""]
          [:span.highlight " :y "]
          [:span.de-highlight "22/7"]
          [:span.highlight "}]"]]]]]

  [:div.note
   [:p "This is one way to visualize the difference. Scalar validation targets only the scalars: numbers, strings, characters, etc.. Collection validation only validates the collections themselves: vectors, maps, sequences, lists, sets. We could kinda think about it as validating the brackets, parens, braces, etc."]])


 (load-file "resources/screencast_sections/mottos.clj")


 (panel
  [:h3 "When to validate collections versus validating scalars"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:h4 "Validate properties of the collection"]

    [:ul
     [:li "Size of a collection"]
     [:li "Existence of an element"]]]

   [:div.side-by-side
    [:h4 "Validate " [:em "relationships between"] " scalars"]

    [:ul
     [:li "Second element equal to first"]
     [:li "All elements ascending"]]]]

  [:div.note
   [:p "Ex: count of a vector; map contains an `:email` key?"]
   [:p "Ex: 1st and 2nd both 42?; 2nd is bigger than 1st, 3rd bigger than 2nd, etc?"]])


 (panel
  [:h3 "Where collection predicates apply"]

  [:h4 "Any predicates apply to their immediate parent collection."]

  [:p "Important terms"]
  [:ul
   [:li "Parent"]
   [:li "Immediate"]
   [:li "Any"]]

  [:div.note
   [:p "Unlike scalar validation, wherein predicates apply to scalars that share their exact paths, collection predicates apply to the collection that corresponds to their parent container."]

   [:p "Importantly, it's the *immediate* parent only, it does not bubble up to a higher layer."]

   [:p "*Any* is the crux of this screencast: A Clojure collection may contain an arbitrary number of elements, including predicates. So a Speculoos specification may contain an arbitrary number of predicates."]

   [:p "3 Mottos + targeting parent collection are an emergent property of the collection validation algorithm. If we understand the algorithm, we can write clear, correct, and expressive collection specifications."]])


 (panel
  [:h3 "Quick review: Manually validate a flat vector"]

  [:table
   [:tr
    [:td "data"]
    [:td [:code "[42 \"abc\" 22/7]"]]]

   [:tr
    [:td "predicate"]
    [:td (prettyfy-form-prettyfy-eval "(defn len-3? [c] (= 3 (count c)))")]]

   [:tr
    [:td "specification"]
    [:td
     [:pre
      [:pre [:code "[42 \"abc\" 22/7] ;; copy-paste data"]]
      [:pre [:code "[             ] ;; delete scalars"]]
      [:pre [:code "[len-3?       ] ;; insert predicate"]]]]]

   [:tr
    [:td "enumerate paths"]
    [:td [:div.side-by-side-container
          [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths [42 \"abc\" 22/7])")]
          [:div.side-by-side (prettyfy-form-prettyfy-eval "(all-paths [len-3?])")]]]]
   [:tr
    [:td "keep only…"]
    [:td
     [:div.side-by-side-container
      [:div.side-by-side [:code "[{:path [], :value [42 \"abc\" 22/7]}]"]]
      [:div.side-by-side [:code "[{:path [0], :value len-3?}]"]]]]]

   [:tr
    [:td "apply predicate"]
    [:td (prettyfy-form-prettyfy-eval "(len-3? [42 \"abc\" 22/7])")]]]

  [:div.note])


 (panel
  [:h3 "Quick review: Validating flat vector with Speculoos"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [validate-collections]])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(validate-collections [42 \"abc\" 22/7] [len-3?])" 45 55)

  [:div.note])


 (panel
  [:h3 "Validating flat vector with two predicates"]

  (prettyfy-form-prettyfy-eval "(validate-collections [42] [vector? map?])" 40 55)

  [:div.note
   [:p "Only one collection, but TWO predicates and two validations?"]])


 (panel
  [:h3 "Flat vector, two predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [42])")]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(all-paths [vector? map?])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1])")
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [99999])")]]

  [:div.note
   [:p "Remember: `vector?` and `map?` are merely short stand-ins for more interesting/powerful predicates that actually validate something interesting."]

   [:p "The `drop-last` step of the algorithm means, functionally, that any and all predicates will be applied to the parent container. This is relevant to *all* collection types: vectors, maps, sets, lists, sequences, etc."]])


 (panel
  [:h3 "Flat vector, five predicates"]

  (prettyfy-form-prettyfy-eval "(validate-collections [42] [vector? map? list? set? coll?])" 55 55)

  [:div.note
   [:p "The `drop-last` paths of all five predicates all evaluated to root, so the validation made five predicate+collection pairs (Motto #3). Every predicate is paired with at most one collection, but any collection may be paired with any number of predicates (zero or more)."]])


 (panel
  [:h3 "Possible problem: nested collection following multiple predicates"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:pre [:code "[42 {:y \"abc\"}]"]]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [42 {:y \"abc\"}])")

    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [0])")
    (prettyfy-form-prettyfy-eval "(drop-last [1])")]

   [:div.side-by-side
    [:pre [:code "[coll? vector? {:foo map?}]"]]
    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(all-paths [coll? vector? {:foo map?}])" 55 50)

    [:div.vspace]
    (prettyfy-form-prettyfy-eval "(drop-last [2 :foo])")]]

  [:div.note
   [:p "How do we match path `[2 :foo]` to `[1]`? A simple `drop-last` won't work."]])


 (panel
  [:h3 "Additional rule for sequentials"]

  [:h4 "For collections nested within a sequential collection, apply nested collection predicates in the order which they appear, ignoring scalars."])



 (panel

  [:h3 "How: " [:em "Prune the intervening scalars"]]

  [:table
   [:tr
    [:td "originals"]
    [:td [:code "[42 {:y \"abc\"}]"]]
    [:td [:code "[coll? vector? {:foo map?}]"]]]

   [:tr
    [:td [:div.vspace]]]

   [:tr
    [:td "prune scalars"]
    [:td [:code "[{:y \"abc\"}]"]]
    [:td [:code "[{:foo map?}]"]]]

   [:tr
    [:td [:div.vspace]]]

   [:tr
    [:td "enumerate paths"]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{:y \"abc\"}])")]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{:foo map?}])")]]]

  (prettyfy-form-prettyfy-eval "(drop-last [0 :foo])")

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(map? {:y \"abc\"})")

  [:div.note
   [:p "With the intervening scalars pruned, the `drop-last` procedure pairs the predicate with the nested `map?` predicate."]])


 (panel
  [:h3 "Validating collections nested within a sequential"]

  (prettyfy-form-prettyfy-eval "(validate-collections [42 {:y \"abc\"}] [coll? vector? {:foo map?}])" 50 50)

  [:div.note
   [:p "`coll?` and `vector?` predicates apply to their immediate parent container. The map is nested within a sequential, and the those two predicates intervene, messing up the `drop-last` procedure. So we must appeal to the new rule: prune the predicates."]
   [:p "`validate-collections` does all the pruning for us. When it looked at the map in the sequential, it removed all the preceding scalars: 42 and `coll?`, and `vector?`. Now, `(drop-last [])` works. That's what the 'ordinal-path-datum' tells us: the paths as if the scalars were removed from both."]])


 (panel
  [:h3 "Stretching the principle: Exemplar sequential data"]

  [:pre
   [:code.form "[{:a 11} 22 (list 33) 44 #{55}]"]
   [:br]
   [:code.eval " 0       1  2         3  4     <--- indexes"]
   [:br]
   [:code.eval " 1st        2nd          3rd   <--- nested collections"]]

  [:div.note
   [:p "For the next few demonstrations, we'll be re-using this exemplar data. So let's get comfortable with it. All the double-digit scalars are merely there to get in our way. To validate the collections, we'll have to carefully apply the principles."]

   [:p "Four collections: one root, three nested (one each of map, list, set). Motto #1 reminds us to be in mindset."]

   [:p "Most critically, the root collection --- a vector --- is a sequential, so the order of its contents matter. Therefore, the ordering of its specification will also matter, because the shape mimics the data."]])


 (panel
  [:h3 "Stretching the principle: Prune the sequential data"]

  [:pre [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]

  [:pre [:code "[{     }    (       )    #{  }]"]])


 (panel
  [:h3 "Stretching the principle: Enumerate paths of pruned data"]

  [:pre (prettyfy-form-prettyfy-eval "(all-paths [{}  () #{}])")]

  [:div.vspace]

  [:p "original data: " [:code
                         "[{"
                         [:span.de-highlight ":a 11"]
                         "} "
                         [:span.de-highlight "22"]
                         " ("
                         [:span.de-highlight "list 33"]
                         ") "
                         [:span.de-highlight "44"]
                         " #{"
                         [:span.de-highlight "55"]
                         "}]"]]

  [:div.note
   [:p "Note: Nested map, nested list, and nested set remain in the same relative order. The root collection is, as always, at path " [:code "[]"] ". The nested collections are zero-indexed: nested map " [:code "0"] ",nested list at " [:code "1"] ",  nested set at " [:code "2"] ". These indexes are what " [:code "validate-collections"] " reports as " [:code ":ordinal-path-datum"] ", the prefix " [:em "ordinal"] " indicating a position within a sequence, 'first', 'second', 'third', etc."]])


 (panel
  [:h3 "Stretching the principle: Composing specification by deleting scalars"]

  [:pre
   [:code "[{:a 11} 22 (list 33) 44 #{55}] ;; <--- data"]
   [:br]
   [:code "[{     }    (       )    #{  }] ;; <--- collection specification"]
   [:br]
   [:code " ^--- 1st   ^--- 2nd     ^--- 3rd nested collection"]]

  [:div.note
   [:p "Note: collection specification looks a lot like our data with all the scalars removed."]

   [:p "The ordering is important: 1st nested collection, 2nd nested collection, etc."]

   [:p "Note: Even though it contains zero predicates, that empty structure in the lower row is a legitimate collection specification which " [:code "validate-collections"] " can consume."]])


 (panel
  [:h3 "Stretching the principle: Validating with zero predicates"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{}()#{}])" 55 55)

  [:div.note
   [:p "Motto #3: Validation ignores collections in the data that are not paired with a predicate in the specification. Zero predicates, zero pairs."]
   [:p "The idea is that if we don't add any predicates, we're declaring that we don't care. Speculoos proceeds without complaint."]])


 (panel
  [:h3 "Stretching the principle: Validating with one predicate targeting second nested collection"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} (list list?) #{}])" 55 80)

  [:div.note
   [:p "One predicate+collection pair, one validation result. Nested collection is indeed a list, so " [:code ":valid?"] " is " [:code "true"] ". The " [:code "list?"] " predicate at path " [:code "[1 0]"] " in the specification was applied to the collection located at path " [:code "[2]"] " in the data."]

   [:p "Notice how " [:code "validate-collections"] " did some tedious and boring calculations to achieve the general effect of " [:em "The predicate in the second nested collection of the specification applies to the second nested collection of the data."] " It kinda skipped over that " [:code "22"] " because it ignores scalars, and we're validating collections."]

   [:p "Basically, " [:code "validate-collections"] " performed that 'skip' by pruning the scalars from the data…"]])


  (panel
  [:h3 "Q: How did that happen? A: Pruning."]

[:table
 [:tr
  [:td "originals"]
  [:td [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]
  [:td [:code "[{} (list list?) #{}]"]]]

 [:tr
  [:td "prune"]
  [:td [:pre [:code "[{     }    (       )    #{  }]"]]]
  [:td [:code "[{} (list list?) #{}]"]]]

 [:tr
  [:td "paths"]
  [:td (prettyfy-form-prettyfy-eval "(all-paths [{} () #{}])")]
  [:td (prettyfy-form-prettyfy-eval  "(all-paths [{} (list list?) #{}])")]]

 [:tr
  [:td "keep only"]
  [:td "everything"]
  [:td "{:path [1 0], :value list?}"]]]

 (prettyfy-form-prettyfy-eval "(drop-last [1 0])")

 (prettyfy-form-prettyfy-eval "(list? (list 33))")

 [:div.note
  [:p "Notice that the specification mimics, but does not exactly copy, the shape of the data (Motto #2). May have to squint a bit, but the shape _is_ similar."]

  [:p "We prune all the integers away from the data, which has the effect of collapsing all the nested collections to the front of the sequential. Since the specification doesn't have any intervening predicates in its corresponding sequential, nothing changes."]

  [:p "We can create one pair: predicate `list?` has a 'drop-last' path that evaluates to the nested list. We can therefore apply the predicate to the one collection. Motto #3 tells us that the other collections are ignores."]])


 (panel
  [:h3 "Stretching principle: Re-visiting the validation"]

    (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} (list list?) #{}])" 55 80))


 (panel
  [:h3 "More colls nested in a sequential: specification"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "[{:a 11} 22 (list 33) 44 #{55 }]"]]]]

   [:tr
    [:td "delete all non-scalars"]
    [:td [:pre [:code "[{     }    (       )    #{   }]"]]]]

   [:tr
    [:td "insert predicate"]
    [:td [:pre [:code "[{     }    (       )    #{set?}]"]]]]

   [:tr
    [:td "insert markers"]
    [:td [:pre [:code "[{     } :skip-1 :skip-2 (       ) :skip-3 :skip-4 #{set?}]"]]]]]

  [:div.note
   [:p "Make the specification using familiar recipe."]
   [:p "Normally, wouldn't insert a non-predicate in a specification (Speculoos doesn't care, and won't stop us)."]])


 (panel
  [:h3 "More colls nested in a sequential: pruning intervening scalars"]

  [:table
   [:tr
    [:th]
    [:th "data"]
    [:th "specification"]]

   [:tr
    [:td "originals"]
    [:td [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]
    [:td [:code "[{} :skip-1 :skip-2 () :skip-3 :skip-4 #{set?}]"]]]

   [:tr
    [:td "prune"]
    [:td [:pre [:code "[{     }    (       )    #{  }]"]]]
    [:td [:pre [:code "[{}                 ()                 #{set?}]"]]]]

   [:tr
    [:td "paths"]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{} () #{}])")]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{} () #{set?}])")]]

   [:tr
    [:td "keep only"]
    [:td]
    [:td [:code "{:path [2 set?], :value set?}"]]]

   [:tr
    [:td "form pairs"]
    [:td]
    [:td (prettyfy-form-prettyfy-eval "(drop-last [2 set?])")]]

   [:tr
    [:td "apply predicate"]
    [:td (prettyfy-form-prettyfy-eval "(set? #{55})")]]])


 (panel
  [:h3 "More colls nested in a sequential: validating with intervening scalars"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} :skip-1 :skip-2 () :skip-3 :skip-4 #{set?}])" 70 80)

  [:div.note
   [:p "One predicate, one collection, one validation result. Validation skipped right over the intervening scalars, " [:code "22"] " and " [:code "44"] ", in the data, and over the intervening non-predicates, " [:code ":skip-1"] " and " [:code ":skip-2"] ", etc., in the specification. " [:code "validate-collections"] " applied " [:code "set?"] " in the third nested collection to the data's third nested collection " [:code "#{55}"] ", both at ordinal path " [:code "[2]"] " (i.e., the third non-scalar elements)."]])


 (panel
  [:h3 "Skipping markers: data & specification"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "[{:a 11    } 22 (list 33) 44 #{55}]"]]]]

   [:tr
    [:td "delete all non-scalars"]
    [:td [:pre [:code "[{         }    (        )   #{  }]"]]]]

   [:tr
    [:td "insert predicate"]
    [:td [:pre [:code "[{:foo map?}    (       )    #{  }]"]]]]

   [:tr
    [:td "insert markers"]
    [:td [:pre [:code "[:skip-5 :skip-6 {:foo map?}    (       )    #{  }]"]]]]])


 (panel
  [:h3 "Skipping markers: manual algorithm"]

  [:table
   [:tr
    [:th]
    [:th "data"]
    [:th "specification"]]

   [:tr
    [:td "originals"]
    [:td [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]
    [:td [:code "[:skip-5 :skip-6 {:foo map?} () #{}]"]]]

   [:tr
    [:td "prune"]
    [:td [:pre [:code "[{     }    (       )    #{  }]"]]]
    [:td [:pre [:code "[                {:foo map?} () #{}]"]]]]

   [:tr
    [:td "paths"]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{} () #{}])")]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{:foo map?} () #{}])")]]

   [:tr
    [:td "keep only"]
    [:td]
    [:td [:code "{:path [0 :foo], :value map?}"]]]

   [:tr
    [:td "form pairs"]
    [:td]
    [:td (prettyfy-form-prettyfy-eval "(drop-last [0 map?])")]]

   [:tr
    [:td "apply predicate"]
    [:td (prettyfy-form-prettyfy-eval "(map? {:a 11})")]]])


 (panel
  [:h3 "Skipping markers: validation"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [:skip-5 :skip-6 {:foo? map?} () #{}])" 65 80))


 (panel
  [:h3 "Validating the root collection: data & specification"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "[        {:a 11} 22 (list 33) 44 #{55}]"]]]]

   [:tr
    [:td "delete all non-scalars"]
    [:td [:pre [:code "[        {     }    (       )    #{  }]"]]]]

   [:tr
    [:td "insert predicate"]
    [:td [:pre [:code "[vector? {     }    (       )    #{  }]"]]]]])


(panel
  [:h3 "Validating the root collection: manual algorithm"]

  [:table
   [:tr
    [:th]
    [:th "data"]
    [:th "specification"]]

   [:tr
    [:td "originals"]
    [:td [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]
    [:td [:code "[vector? {} () #{}]"]]]

   [:tr
    [:td "prune"]
    [:td [:pre [:code "[{     }    (       )    #{  }]"]]]
    [:td [:pre [:code "[vector? {} () #{}]"]]]]

   [:tr
    [:td "paths"]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [{} () #{}])")]
    [:td (prettyfy-form-prettyfy-eval "(all-paths [vector? {} () #{}])")]]

   [:tr
    [:td "keep only"]
    [:td]
    [:td [:code "{:path [0], :value vector?}"]]]

   [:tr
    [:td "form pairs"]
    [:td]
    [:td (prettyfy-form-prettyfy-eval "(drop-last [0])")]]

   [:tr
    [:td "apply predicate"]
    [:td (prettyfy-form-prettyfy-eval "(vector? [{:a 11} 22 (list 33) 44 #{55}])")]]])


 (panel
  [:h3 "Validating the root collection"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])" 65 80))


 (panel
  [:h3 "Any predicate applies to its immediate parent"]

  [:pre
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [" [:strong "vector?"] " {} () #{}])"]
   [:br]
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} " [:strong "vector?"] " () #{}])"]
   [:br]
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () " [:strong "vector?"] " #{}])"]
   [:br]
   [:code "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{} " [:strong "vector?"] "])"]]

  [:div.vspace]

  [:pre
   [:code "(drop-last [0]) ;; => ()"] [:br]
   [:code "(drop-last [1]) ;; => ()"] [:br]
   [:code "(drop-last [2]) ;; => ()"] [:br]
   [:code "(drop-last [3]) ;; => ()"]]

  [:div.note
   [:p "Could put predicate anywhere within sequential. Substantially the same validation result."]
   [:p "Maybe you feel like me and find it better at the front."]])


 (panel
  [:h3 "Finally, validating all collections at once: data & specification"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "[        {:a 11    }  22         (list 33   ) 44   #{55  }]"]]]]

   [:tr
    [:td "delete all non-scalars"]
    [:td [:pre [:code "[        {         }             (          )      #{    }]"]]]]

   [:tr
    [:td "insert predicates"]
    [:td [:pre [:code "[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]"]]]]])


 (panel
  [:h3 "Validating two levels: two 'phases'"]

  [:ol
   [:li "root level"]
   [:li "nested level"]])



 (panel
  [:h3 "Phase 1: Validating the root level"]

  [:table
   [:tr
    [:th]
    [:th "data"]
    [:th "specification"]]

   [:tr
    [:td "originals"]
    [:td [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]
    [:td [:code "[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]"]]]

   [:tr
    [:td "prune"]
    [:td [:pre [:code "[{     }    (       )    #{  }]"]]]
    [:td [:pre [:code "[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]"]]]]

   [:tr
    [:td "paths"]
    [:td [:code "..."]]
    [:td [:code "..."]]]

   [:tr
    [:td "keep only"]
    [:td [:pre [:code "[{:path [], :value [{:a 11} 22 (33) 44 #{55}]}\n {:path [0], :value {:a 11}}\n {:path [2], :value (33)}\n {:path [4], :value #{55}} ]"]]]
    [:td [:pre [:code "[{:path [0], :value vector?}\n {:path [2], :value sequential?}\n {:path [4], :value coll?}\n {:path [6], :value any?}]"]]]]

   [:tr
    [:td "form pairs"]
    [:td]
    [:td
     [:pre
      [:code "(drop-last [0]) ;; => ()"] [:br]
      [:code "(drop-last [2]) ;; => ()"] [:br]
      [:code "(drop-last [4]) ;; => ()"] [:br]
      [:code "(drop-last [6]) ;; => ()"]]]]

   [:tr
    [:td "apply predicates"]
    [:td
     [:pre
      [:code "(vector? [{:a 11} 22 (list 33) 44 #{55}]) ;; => true"] [:br]
      [:code "(sequential? [{:a 11} 22 (list 33) 44 #{55}]) ;; => true"] [:br]
      [:code "(coll? [{:a 11} 22 (list 33) 44 #{55}]) ;; => true"] [:br]
      [:code "(any? [{:a 11} 22 (list 33) 44 #{55}]) ;; => true"]]]
    [:td]]])


 (panel
  [:h3 "Phase 2: Validating the nested level"]

  [:table
   [:tr
    [:th]
    [:th "data"]
    [:th "specification"]]

   [:tr
    [:td "originals"]
    [:td [:code "[{:a 11} 22 (list 33) 44 #{55}]"]]
    [:td [:code "[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]"]]]

   [:tr
    [:td "prune"]
    [:td [:pre [:code "[{     }    (       )    #{  }]"]]]
    [:td [:pre [:code "[{:foo map?} (list list?) #{set?}]"]]]]

   [:tr
    [:td "paths"]
    [:td [:code "..."]]
    [:td [:code "..."]]]

   [:tr
    [:td "keep only"]
    [:td [:pre [:code "[{:path [], :value [{:a 11} 22 (33) 44 #{55}]}\n {:path [0], :value {:a 11}}\n {:path [2], :value (33)}\n {:path [4], :value #{55}} ]"]]]
    [:td [:pre [:code "[{:path [0 :foo], :value map?}\n {:path [1 0], :value list?}\n {:path [2 set?], :value set?}}]"]]]]

   [:tr
    [:td "form pairs"]
    [:td]
    [:td
     [:pre
      [:code "(drop-last [0 :foo]) ;; => (0)"] [:br]
      [:code "(drop-last [1 0]) ;; => (1)"] [:br]
      [:code "(drop-last [2 set?]) ;; => (2)"]]]]

   [:tr
    [:td "apply predicates"]
    [:td
     [:pre
      [:code "(map? {:a 11}) ;; => true"] [:br]
      [:code "(list? (list 33)) ;; => true"] [:br]
      [:code "(set? #{55}) ;; => true"]]]
    [:td]]])


 (panel
  [:h3 "Validating both levels at once"]

  (prettyfy-form-prettyfy-eval "(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {:foo? map?} sequential? (list list?) coll? #{set?} any?])" 95 100))

 (panel)


 (panel
  [:h3 "Nesting within non-sequentials: no skipping nor pruning"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a [99] :b (list 77)}"]]]]
   [:tr
    [:td "delete scalars"]
    [:td [:pre [:code "{:a [  ] :b (list   )"]]]]
   [:tr
    [:td "insert predicates"]
    [:td [:pre [:code "{:a [vector?] :b (list list?)} ;; collection specification"]]]]]

  [:div.note
   [:p "Maps aren't sequential, so no skipping or pruning. Here, we create a specification based upon this data."]
   [:p "Predicates `vector?` and `list?` apply to their _immediate_ parents, i.e., the map at `:a` and the list at `:b`. For now, we haven't declared any requirement of the root map itself."]])


 (panel
  [:h3 "Validating collections nested within a non-sequential"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?)})" 55 80)

  [:div.vspace]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(drop-last [:b 0])")]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(drop-last [:a 0])")]]

  [:div.note
   [:p "Checklist...Mottos #1-3: _collection validation_; specification shape mimics the data (b/c we copy-pasted); un-paired datums ignored (we didn't specify the root collection)"]
   [:p "Predicate `vector?` at path [:a 0] targets the collection at (drop-last [:a 0]). That evals to [:a], the nested vector. `[99]` is not a vector, so the predicate is un-satisfied."]
   [:p "Predicate `list?` at path [:b 0] targets the collection at (drop-last [:b 0]), which evals to [:b], the nested list. `(77) is in fact a list, so the predicate is satisfied."]
   [:p "Since maps are *not* sequential, it doesn't matter what order the nested predicate appear, unlike the vector we studied earlier. Just like how it doesn't matter whether I eval these two `drop-last` expression 'out-of-order'."]])


 (panel
  [:h3 "Specifying collections nested within a non-sequential, plus the root"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a [99] :b (list 77)}"]]]]
   [:tr
    [:td "additional predicate"]
    [:td [:pre [:code "{:a [vector?] :b (list list?) :howdy map?} ;; collection specification"]]]]]

  [:div.note
   [:p "Let's re-use that specification an add a sham key, `:howdy`, associated with a `map?` predicate which targets the root."]

   [:p "Now we have three predicates. Let's see if we can make a pair with each."]])


 (panel
  [:h3 "Validating collections nested within a non-sequential, plus the root"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :b (list list?) :howdy map?})" 75 90)

  [:div.vspace]

  [:pre
   [:code "(drop-last [:a 0]) ;; => (:a)"] [:br]
   [:code "(drop-last [:b 0]) ;; => (:b)"] [:br]
   [:code "(drop-last [:howdy]) ;; => ()"]]

  [:div.note
   [:p "Same two results as before due to the same predicate+collection pairs (Motto #3). Plus an additional pairing."]
   [:p "The `map?' predicate at `:howdy` targets the root collection because `(drop-last [:howdy])` evals to [], which locates the root collection. It's a map, so the predicate is satisfied. Three predicates paired with three collections."]])


 (panel
  [:h3 "Specifying collections nested within a non-sequential, un-paired predicate"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a [99] :b (list 77)}"]]]]
   [:tr
    [:td "delete scalars"]
    [:td [:pre [:code "{:a [  ] :b (list   )}"]]]]
   [:tr
    [:td "delete one key-val"]
    [:td [:pre [:code "{:a [  ]             }"]]]]
   [:tr
    [:td "insert one paired, one un-paired predicate"]
    [:td [:pre [:code "{:a [vector?] :flamingo [coll?]} ;; collection specification"]]]]]

  [:div.vspace]

  [:p [:em "Fact:"] " Two predicates."]
  [:p [:em "Quiz:"] " How many validation pairs?"]
  [:p [:em "Hint:"] " Run " [:code "drop-last"] " on predicates' paths."])


 (panel
  [:h3 "Validating collections nested within a non-sequential, un-paired predicate"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :flamingo [coll?]})" 60 90)

  [:div.vspace]

  [:pre
   (prettyfy-form-prettyfy-eval "(drop-last [:a 0])")
   (prettyfy-form-prettyfy-eval "(drop-last [:flamingo 0])")]

  [:div.note
   [:p "Motto #3: Ignore un-paired data elements and un-paired predicates. Okay, then. How many pairs can we make?"]

   [:p "Two predicates. Predicate `vector?` has a `drop-last` path of [], which we've seen before targets its immediate parent. So that's one pair."]

   [:p "Predicate `coll?` is at path [:flamingo 0]. `(drop-last [:flamingo 0])` evals to path [:flamingo]. The data does not contain *any* element there, much less a collection. So `coll?` is ignored."]

   [:p "One paired collection+predicate. One validation result."]])


 (panel
  [:h3 "Specifying a nested collection in a non-sequential, plus the root"]

  [:table
   [:tr
    [:td "data"]
    [:td [:pre [:code "{:a [99] :b (list 77)}"]]]]
   [:tr
    [:td "delete scalars"]
    [:td [:pre [:code "{:a [  ] :b (list   )}"]]]]
   [:tr
    [:td "delete one key-val"]
    [:td [:pre [:code "{:a [  ]             }"]]]]
   [:tr
    [:td "insert two paired predicates"]
    [:td [:pre [:code "{:a [vector?] :emu coll?} ;; collection specification"]]]]]

  [:div.note
   [:p "Perhaps we intended to specify and validate the root map. How would we actually do that. Similar procedure. Cut-paste data, delete scalars."]

   [:p "Insert the `vector?` predicate so that it will validate its immediate parent. That's the critical point. We need to insert the predicate that will target the root map so that the root map is the immediate parent of that predicate. That's when we use a sham key. `:foo` works fine, but `:emu` is fun, too. And it doesn't matter what the key is as long as it doesn't block validating a nested collection. The data contains only `:a` and `:b`, so `:emu` doesn't interfere."]

   [:p "No judgment on flamingos vs emus. I merely thought they were fun."]])


 (panel
  [:h3 "Validating a nested collection in a non-sequential, plus the root"]

  (prettyfy-form-prettyfy-eval "(validate-collections {:a [99] :b (list 77)} {:a [vector?] :emu coll?})" 65 80)

  [:div.vspace]

  [:pre
   (prettyfy-form-prettyfy-eval "(drop-last [:a 0])")
   (prettyfy-form-prettyfy-eval "(drop-last [:emu])")]

  [:div.note
   [:p "*Now* we have two pairs of collections+predicates. Both predicates apply to their immediate parent container. `vector?` targets the nested [99], and `coll?` targets the root collection, the outer map."]

   [:p "Right-trimming the predicate's path with `drop-last` produces the empty path, which properly targets the root map as we intended."]])


 (panel
  [:h3 "Remember these principles when validating collections"]

  [:ol
   [:li "Specification shape mimics the data (Motto #2)."]
   [:li [:em "All"] " predicates apply to the " [:em "immediate"] " collections that " [:em "contain"] " them."]
   [:li "Maps: predicates at keys that not in  data."]
   [:li "Collections nested in sequentials: predicates apply to immediate parent, ignoring intervening scalars. "]
   [:li "Collections nested in maps not affected by order."]]

  [:div.note
   [:p "Don't get dazzled by the details. We just need to remember these operating principles."]])


 (panel
  [:h3 "More than one level of nesting"]

  [:p "Validate any heterogeneous, arbitrarily-nested data structure"]

  (prettyfy-form-prettyfy-eval "(validate-collections [99 88 77 {:x (list 66 55 {:y [44 33 22 11 #{42}]})}] [{:x (list {:y [#{set?}]})}])")

  [:div.note
   [:p "Even though all the examples we covered in this discussion were 'one level deep' --- e.g., a map nested in a vector --- Speculoos can validate any collection nested to any arbitrary depth, of any mixture of Clojure collection types."]

   [:p "For example, here we have a set, nested in a vector, nested in a list, nested in a map, nested in a vector. Five levels deep. No problem!"]

   [:p "Notice how the principles apply: The `set?` predicate applies to its immediate parent collection, the set `#{42}` in the data. The specification is shaped to mimic the data. And we only made one collection+predicate pair, so there is only one validation result."]])

 (whats-next-panel
  collection-validation-advanced-index
  [:div.note
   [:p "Now that we've discussed both basic and advanced collection validations, if you're interested, we have a third, short screencast discussing some odd-and-ends of collection validation."]

   [:p "Otherwise, feel free to continue with the screencast covering validation summaries and 'thorough' validations, utilities that augment Motto #3 concerning ignored data elements."]])
 ]
