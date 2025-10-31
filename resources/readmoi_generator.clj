(ns readmoi-generator
  "Script to load functions and generate ReadMe html and markdown docs.

  CIDER eval buffer C-c C-k generates an html page and a markdown chunk."
  {:no-doc true}
  (:require
   [hiccup2.core :refer [raw]]
   [readmoi.core :refer [*project-group*
                         *project-name*
                         *project-version*
                         -main
                         prettyfy
                         print-form-then-eval]]
   [speculoos.core :refer [all-paths
                           expand-and-clamp-1
                           only-invalid
                           valid-collections?
                           valid-scalars?
                           valid?
                           validate
                           validate-collections
                           validate-scalars]]
   [speculoos.utility :refer [*such-that-max-tries*
                              basic-collection-spec-from-data
                              clamp-in*
                              collections-without-predicates
                              data-from-spec
                              defpred
                              exercise
                              inspect-fn
                              in?
                              predicates-without-collections
                              predicates-without-scalars
                              scalars-without-predicates
                              sore-thumb
                              spec-from-data
                              thoroughly-valid?
                              unfindable-generators
                              validate-predicate->generator
                              thoroughly-valid-scalars?]]
   [speculoos.function-specs :refer [exercise-fn
                                     inject-specs!
                                     instrument unstrument
                                     unject-specs!
                                     validate-fn
                                     validate-fn-with
                                     validate-higher-order-fn]]))


(alter-var-root #'speculoos.utility/*such-that-max-tries* (constantly 100))


(-main)

