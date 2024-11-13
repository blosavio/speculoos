(ns changelog-validations
  "Functions to validate changelogs.

  NOTE: Ought to be split out into its own library."
  {:no-doc true}
  (:require
   [changelog-specifications :refer :all]
   [fn-in.core :refer [get-in*]]
   [speculoos.core :refer [only-invalid
                           valid-collections?
                           valid-scalars?
                           valid?
                           validate
                           validate-collections
                           validate-scalars]]
   [speculoos.utility :refer [collections-without-predicates
                              scalars-without-predicates
                              thoroughly-valid?
                              thoroughly-valid-scalars?]]))


(def full-changelog? true)

(set! *print-length* 7)

(def full-changelog-filename "changelog.edn")
(def streamlined-changelog-filename "resources/case_study/edited_changelog.edn")


(def changelog-data (load-file (if full-changelog? full-changelog-filename streamlined-changelog-filename)))



;;;; scalar validations


(comment
  (def example-change (get-in* changelog-data [2 :changes 2]))

  (only-invalid (validate-scalars example-change
                                  change-scalar-spec))

  (validate-scalars example-change
                    change-scalar-spec)

  (scalars-without-predicates example-change
                              change-scalar-spec)
  )


(comment
  (def example-version (get-in* changelog-data [2]))

  (only-invalid (validate-scalars example-version
                                  version-scalar-spec))
)


(comment

  (def changelog-scalar-spec-partial-1 [version-scalar-spec])

  (def changelog-scalar-spec-partial-2 [version-scalar-spec
                                        version-scalar-spec])

  (def changelog-scalar-spec-partial-3 [(assoc version-scalar-spec :changes [change-scalar-spec])])

  (def changelog-scalar-spec-partial-4 [version-scalar-spec
                                        version-scalar-spec
                                        (assoc version-scalar-spec :changes [change-scalar-spec
                                                                             change-scalar-spec
                                                                             change-scalar-spec])])

  (only-invalid (validate-scalars changelog-data
                                  changelog-scalar-spec-partial-1))

  (only-invalid (validate-scalars changelog-data
                                  changelog-scalar-spec-partial-2))

  (only-invalid (validate-scalars changelog-data
                                  changelog-scalar-spec-partial-3))

  (only-invalid (validate-scalars changelog-data
                                  changelog-scalar-spec-partial-4))

 )


(defn validate-changelog-scalars
  "Given changelog `c` and scalar specification `s`, returns invalid scalar
  validation results."
  {:UUIDv4 #uuid "cb3082d7-0f7e-4a47-ad43-5555e66d7bf6"}
  [c s]
  (only-invalid (validate-scalars c s)))


(defn elide-datums
  "Replace `:datum` validation entries `v` with '...' so that the viewing the
  results are clearer."
  {:UUIDv4 #uuid "e9ffa7a4-c375-434b-a7f1-2b6d9685e2d0"}
  [v]
  (vec (map #(assoc % :datum '...) v)))


(comment
  (elide-datums (validate-changelog-scalars changelog-data changelog-scalar-spec))

  ;; Note: This evaluation may take a 10s of seconds with CIDER/nREPL
  (validate-changelog-scalars changelog-data changelog-scalar-spec)

  )


(defn all-changelog-scalars-have-predicates?
  "Given changelog `c` and scalar specification `s`, returns `true` if all
  scalars in the changelog data are paired with a predicate."
  {:UUIDv4 #uuid "8dc5c146-1cc9-4dce-9f42-2843b85af6bc"}
  [c s]
  (empty? (predicates-without-scalars c s)))


(comment
  (all-changelog-scalars-have-predicates? changelog-data changelog-scalar-spec)
  )



;;;; collection validations


;; Collection valdidation component #1: Ensuring required keys

(comment

  (def example-change-2 (get-in* changelog-data [1 :changes 2]))

  (only-invalid (validate-collections example-change-2
                                      (first (:changes version-coll-spec))))

  )


(comment
  (def changelog-coll-spec-partial-1 [version-coll-spec])
  (def changelog-coll-spec-partial-2 [version-coll-spec
                                      version-coll-spec])
  (def changelog-coll-spec-partial-3 [version-coll-spec
                                      version-coll-spec
                                      version-coll-spec])


  (only-invalid (validate-collections changelog-data
                                      changelog-coll-spec-partial-1))

  (only-invalid (validate-collections changelog-data
                                      changelog-coll-spec-partial-2))

  (only-invalid (validate-collections changelog-data
                                      changelog-coll-spec-partial-3))
 )


(defn all-changelog-collections-have-predicates?
  "Given changelog `c` and collection specification `s`, returns `true` if all
  collections in the changelog data are paired with a predicate."
  {:UUIDv4 #uuid "8f42d121-6cde-4b35-a0c0-f67b2d014669"}
  [c s]
  (empty? (collections-without-predicates c s)))


(comment
  (all-changelog-collections-have-predicates? changelog-data changelog-coll-spec)
  )


(defn validate-changelog-collections
  "Given changelog `c` and collection specification `s`, returns invalid
  collection validation results."
  {:UUIDv4 #uuid "d559f4f5-a292-490a-ae1c-bf5dd96f953b"}
  [c s]
  (only-invalid (validate-collections c s)))


(comment
  (def changelog-coll-spec-only-req-keys (repeat version-coll-spec))

  (validate-changelog-collections changelog-data
                                  changelog-coll-spec-only-req-keys)
  )




;; Collection validation component #2: Validating proper version incrementing


(comment
  (only-invalid (validate-collections changelog-data
                                      [properly-incrementing-versions?]))
  )


(comment
  (validate-changelog-collections changelog-data
                                  changelog-coll-spec)
  )


;;;; Combo validations

(only-invalid (validate changelog-data
                        changelog-scalar-spec
                        changelog-coll-spec))

(valid? changelog-data
        changelog-scalar-spec
        changelog-coll-spec)