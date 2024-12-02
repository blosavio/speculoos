(ns screedcast-generator
  "CIDER eval buffer C-c C-k generates a screencast page and a
  screencast+comments page."
  {:no-doc true}
  (:require
   [fn-in.core :refer [get-in*]]
   [hiccup2.core :as h2]
   [readmoi.core :refer :all]
   [screedcast.core :refer :all]
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


(def screedcast-options (load-file "resources/screedcast_options.edn"))


(generate-all-screencasts screedcast-options)