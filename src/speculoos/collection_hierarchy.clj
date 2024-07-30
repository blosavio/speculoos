(ns speculoos.collection-hierarchy
  "Referred by speculoos.core, speculoos.utility, and speculoos.fn-in.
   Used for multimethods, which should probably be converted to protocols.
   (But I think multimethods are prettier.)"
  {:no-doc true})


(defn create-collection-hierarchy
  {:UUIDv4 #uuid "6577d42e-527b-4283-bd10-58e10dffd55b"}
  []
  (do
    (derive clojure.lang.PersistentList ::list)
    (derive clojure.lang.PersistentList$EmptyList ::list)

    (derive clojure.lang.Cons ::list)

    (derive clojure.lang.PersistentVector ::vector)
    (derive clojure.lang.PersistentVector$ChunkedSeq ::vector)

    (derive clojure.lang.PersistentArrayMap ::map)
    (derive clojure.lang.PersistentHashMap ::map)
    (derive clojure.lang.PersistentTreeMap ::map)

    (derive clojure.lang.MapEntry ::map-entry)

    (derive clojure.lang.PersistentHashSet ::set)
    (derive clojure.lang.PersistentTreeSet ::set)

    (derive ::list ::non-map-entry-collection)
    (derive ::vector ::non-map-entry-collection)
    (derive ::map ::non-map-entry-collection)
    (derive ::set ::non-map-entry-collection)

    (derive ::list ::non-map)
    (derive ::vector ::non-map)
    (derive ::set ::non-map)

    (derive clojure.lang.Cycle ::non-terminating)
    (derive clojure.lang.Iterate ::non-terminating)
    (derive clojure.lang.LazySeq ::non-terminating)
    (derive clojure.lang.LongRange ::non-terminating)
    (derive clojure.lang.Range ::non-terminating)
    (derive clojure.lang.Repeat ::non-terminating)
    ))