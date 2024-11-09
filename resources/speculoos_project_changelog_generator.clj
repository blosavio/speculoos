(ns speculoos-project-changelog-generator
  "CIDER eval buffer C-c C-k generates a 'changelog.md' in the project's
  level directory.

  NOTE: changelog aggregation, specification, validation, and webpage generation
  ought to be split out into its own library."
  {:no-doc true}
  (:require
   [fn-in.core :refer [get-in*]]
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]
   [speculoos.core :refer [#_all-paths
                           #_expand-and-clamp-1
                           only-invalid
                           valid-collections?
                           valid-scalars?
                           valid?
                           validate
                           validate-collections
                           validate-scalars]]
   [speculoos.utility :refer [#_*such-that-max-tries*
                              basic-collection-spec-from-data
                              #_clamp-in*
                              collections-without-predicates
                              data-from-spec
                              defpred
                              #_exercise
                              #_inspect-fn
                              in?
                              predicates-without-collections
                              predicates-without-scalars
                              scalars-without-predicates
                              sore-thumb
                              spec-from-data
                              thoroughly-valid?
                              #_unfindable-generators
                              validate-predicate->generator
                              thoroughly-valid-scalars?]]))


;; (def changelog-data (load-file "resources/changelog_entries/changelog.edn"))

(require '[fn-in.core :refer [update-in*]])

;; Make a temporarily trimmed changelog so it's easeier to handle for dev
(ns-unmap *ns* 'changelog-data)
(def changelog-data (into [] (map (fn [m] (update-in* m [:changes] #(into [] (subvec % 0 (min 3 (count %)))))) (load-file "resources/changelog_entries/changelog.edn"))))

"Strategy: Build up from small pieces, testing along the way. Start with scalars. Put aside. Then do collections. Then, at end, put two together into a combo validation."


;; specifications

;;; "Motto #1: Keep scalar a collection specifications separate"
;;;; "Scalar specification and testing"

(defn year-predicate [n] (and (int? n) (<= 2000 n)))

(def month-predicate #{"January"
                       "February"
                       "March"
                       "April"
                       "May"
                       "June"
                       "July"
                       "August"
                       "September"
                       "October"
                       "November"
                       "December"})

(defn day-predicate [n] (and (int? n) (<= 1 31)))

(def date-spec {:year year-predicate
                :month month-predicate
                :day day-predicate})

(defn ticket-predicate [t] (or (string? t) (uuid? t)))

(def reference-spec {:source string?
                     :url #"^https:\/{2}[\w\/\.]*"
                     :ticket ticket-predicate})

(def change-kinds #{:initial-release

                    :security

                    :performance-improvement
                    :preformance-regression

                    :memory-improvement
                    :memory-regression

                    :network-resource-improvement
                    :network-resource-regression

                    :added-dependency
                    :removed-dependency

                    :added-functions
                    :renamed-functions
                    :moved-functions
                    :removed-functions
                    :altered-functions

                    :function-arguments
                    :relaxed-input-requirements
                    :stricter-input-requirements

                    :increased-return
                    :decreased-return
                    :altered-return

                    :defaults

                    :implementation
                    :source-formatting
                    :error-messsage

                    :tests
                    :bug-fix
                    :deprecated-something

                    :policy
                    :meta-data
                    :documentation
                    :website
                    :release-note})


;; see Metosin's 'Project Status Model'
;; https://github.com/metosin/open-source/blob/main/project-status.md
;;   * experimental: Not recommended for production use.  No support nor maintenance. Testing and feedback welcome.
;;   * active: Actively developed. Recommended for use.
;;   * stable: Maintained and recommended for use. No major new features, but PRs are welcome.
;;   * inactive: Okay for production use. Will receive security fixes, but no new developments. Not recommended for new projects.
;;   * deprecated: Not recommended for any use.

(def status-predicate #{:experimental
                        :active
                        :stable
                        :inactive
                        :deprecated})

(defn breaking-predicate [b] (or (nil? b) (boolean? b)))

(def renamed-function-spec {:old-function-name symbol?
                            :new-function-name symbol?})

(def change-scalar-spec {:date date-spec
                         :description string?
                         :reference reference-spec
                         :change-type change-kinds
                         :breaking? breaking-predicate
                         :added-functions (repeat symbol?)
                         :renamed-functions (repeat renamed-function-spec)
                         :moved-functions (repeat symbol?)
                         :altered-functions (repeat symbol?)
                         :removed-functions (repeat symbol?)})


(set! *print-length* 99)


(comment
  (get-in* changelog-data [1 :changes 2])

  (only-invalid (validate-scalars (get-in* changelog-data [1 :changes 2])
                                  change-scalar-spec))

  (validate-scalars (get-in* changelog-data [1 :changes 2])
                    change-scalar-spec)

  )


(def person-spec {:name string?
                  :email #"^[\w\.]+@[\w\.]+"})

(defn version-predicate [i] (and (int? i) (<= 0 i)))

(def version-scalar-spec {:date date-spec
                          :responsible person-spec
                          :version version-predicate
                          :comment string?
                          :project-status status-predicate
                          :stable boolean?
                          :urgency #{:low :medium :high}
                          :breaking? boolean?})


(comment
  (validate-scalars (get-in* changelog-data [2])
                    version-scalar-spec)
  ;; [{:path [:date :month], :datum nil, :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? nil}
  ;; {:path [:project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
  ;; {:path [:urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
  ;; {:path [:date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
  ;; {:path [:date :day], :datum nil, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? false}
  ;; {:path [:breaking?], :datum true, :predicate #function[clojure.core/boolean?], :valid? true}
  ;; {:path [:stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
  ;; {:path [:responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
  ;; {:path [:responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
  ;; {:path [:comment], :datum "work on addressing comments", :predicate #function[clojure.core/string?--5475], :valid? true}
  ;; {:path [:version], :datum 2, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}]
  )


"Composition: assembling the _version_ and _change_ specifications."

"There's no rule that says we've got to do everything at once. Let's start small."

(def changelog-scalar-spec [version-scalar-spec])

(def changelog-scalar-spec [version-scalar-spec
                            version-scalar-spec])

(def changelog-scalar-spec [(assoc version-scalar-spec :changes [change-scalar-spec])])

(def changelog-scalar-spec [version-scalar-spec
                            version-scalar-spec
                            (assoc version-scalar-spec :changes [change-scalar-spec
                                                                 change-scalar-spec
                                                                 change-scalar-spec])])

(comment
  (validate-scalars changelog-data
                    changelog-scalar-spec)
;; [{:path [0 :date :month], :datum "June", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "June"}
;;  {:path [0 :project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
;;  {:path [0 :urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
;;  {:path [1 :date :month], :datum "July", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "July"}
;;  {:path [1 :project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
;;  {:path [1 :urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
;;  {:path [2 :date :month], :datum nil, :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? nil}
;;  {:path [2 :project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
;;  {:path [2 :urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
;;  {:path [2 :changes 0 :date :month], :datum "August", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "August"}
;;  {:path [2 :changes 0 :change-type], :datum :added-function, :predicate #{:changed-return :memory-regression :preformance-regression :added-dependency :decreased-return :source-formatting :added-function :network-resource-regression :tests :security :release-note :bug-fix :error-messsage :relaxed-input-requirements :meta-data :deprecated-something :network-resource-improvement :defaults :policy :documentation :function-argument :memory-improvement :increased-return :website :renamed-function :removed-dependency :performance-improvement :stricter-input-requirements :initial-release :implementation :removed-function :moved-function}, :valid? :added-function}
;;  {:path [2 :changes 1 :date :month], :datum "August", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "August"}
;;  {:path [2 :changes 1 :change-type], :datum :implementation, :predicate #{:changed-return :memory-regression :preformance-regression :added-dependency :decreased-return :source-formatting :added-function :network-resource-regression :tests :security :release-note :bug-fix :error-messsage :relaxed-input-requirements :meta-data :deprecated-something :network-resource-improvement :defaults :policy :documentation :function-argument :memory-improvement :increased-return :website :renamed-function :removed-dependency :performance-improvement :stricter-input-requirements :initial-release :implementation :removed-function :moved-function}, :valid? :implementation}
;;  {:path [2 :changes 2 :date :month], :datum "August", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "August"}
;;  {:path [2 :changes 2 :change-type], :datum :changed-return, :predicate #{:changed-return :memory-regression :preformance-regression :added-dependency :decreased-return :source-formatting :added-function :network-resource-regression :tests :security :release-note :bug-fix :error-messsage :relaxed-input-requirements :meta-data :deprecated-something :network-resource-improvement :defaults :policy :documentation :function-argument :memory-improvement :increased-return :website :renamed-function :removed-dependency :performance-improvement :stricter-input-requirements :initial-release :implementation :removed-function :moved-function}, :valid? :changed-return}
;;  {:path [0 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;;  {:path [0 :date :day], :datum 6, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;;  {:path [0 :breaking?], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;;  {:path [0 :stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;;  {:path [0 :responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [0 :responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
;;  {:path [0 :comment], :datum "initial public release", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [0 :version], :datum 0, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}
;;  {:path [1 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;;  {:path [1 :date :day], :datum 26, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;;  {:path [1 :breaking?], :datum true, :predicate #function[clojure.core/boolean?], :valid? true}
;;  {:path [1 :stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;;  {:path [1 :responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [1 :responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
;;  {:path [1 :comment], :datum "request for comments", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [1 :version], :datum 1, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}
;;  {:path [2 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;;  {:path [2 :date :day], :datum nil, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? false}
;;  {:path [2 :breaking?], :datum true, :predicate #function[clojure.core/boolean?], :valid? true}
;;  {:path [2 :stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;;  {:path [2 :responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [2 :responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
;;  {:path [2 :comment], :datum "work on addressing comments", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [2 :changes 0 :description], :datum "Added `thoroughly-valid-scalars?` and `thoroughly-valid-collections?` utilities.", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [2 :changes 0 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;;  {:path [2 :changes 0 :date :day], :datum 1, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;;  {:path [2 :changes 0 :added-functions 0], :datum thoroughly-valid-scalars?, :predicate #function[clojure.core/symbol?], :valid? true}
;;  {:path [2 :changes 0 :added-functions 1], :datum thoroughly-valid-collections?, :predicate #function[clojure.core/symbol?], :valid? true}
;;  {:path [2 :changes 0 :breaking?], :datum false, :predicate #function[speculoos-project-changelog-generator/breaking-predicate], :valid? true}
;;  {:path [2 :changes 1 :description], :datum "Adjusted `thoroughly-valid?` to internally use new functions.", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [2 :changes 1 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;;  {:path [2 :changes 1 :date :day], :datum 1, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;;  {:path [2 :changes 1 :altered-functions 0], :datum thoroughly-valid?, :predicate #function[clojure.core/symbol?], :valid? true}
;;  {:path [2 :changes 1 :breaking?], :datum false, :predicate #function[speculoos-project-changelog-generator/breaking-predicate], :valid? true}
;;  {:path [2 :changes 2 :description], :datum "Adjusted `scalars-without-predicates` to return a set of all-paths elements, instead of a sequence, to be consistent with `collections-without-predicates`.", :predicate #function[clojure.core/string?--5475], :valid? true}
;;  {:path [2 :changes 2 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;;  {:path [2 :changes 2 :date :day], :datum 1, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;;  {:path [2 :changes 2 :altered-functions 0], :datum scalars-without-predicates, :predicate #function[clojure.core/symbol?], :valid? true}
;;  {:path [2 :changes 2 :breaking?], :datum true, :predicate #function[speculoos-project-changelog-generator/breaking-predicate], :valid? true}
;;  {:path [2 :version], :datum 2, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}]


;; [{:path [0 :date :month], :datum "June", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "June"}
;; {:path [0 :project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
;; {:path [0 :urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
;; {:path [1 :date :month], :datum "July", :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? "July"}
;; {:path [1 :project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
;; {:path [1 :urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
;; {:path [2 :date :month], :datum nil, :predicate #{"August" "May" "April" "July" "March" "June" "October" "January" "September" "November" "December" "February"}, :valid? nil}
;; {:path [2 :project-status], :datum :experimental, :predicate #{:inactive :stable :experimental :active :deprecated}, :valid? :experimental}
;; {:path [2 :urgency], :datum :low, :predicate #{:medium :high :low}, :valid? :low}
;; {:path [0 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;; {:path [0 :date :day], :datum 6, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;; {:path [0 :breaking?], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [0 :stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [0 :responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [0 :responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
;; {:path [0 :comment], :datum "initial public release", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [0 :version], :datum 0, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}
;; {:path [1 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;; {:path [1 :date :day], :datum 26, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? true}
;; {:path [1 :breaking?], :datum true, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [1 :stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [1 :responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [1 :responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
;; {:path [1 :comment], :datum "request for comments", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [1 :version], :datum 1, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}
;; {:path [2 :date :year], :datum 2024, :predicate #function[speculoos-project-changelog-generator/year-predicate], :valid? true}
;; {:path [2 :date :day], :datum nil, :predicate #function[speculoos-project-changelog-generator/day-predicate], :valid? false}
;; {:path [2 :breaking?], :datum true, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [2 :stable], :datum false, :predicate #function[clojure.core/boolean?], :valid? true}
;; {:path [2 :responsible :name], :datum "Brad Losavio", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [2 :responsible :email], :datum "blosavio@sagevisuals.com", :predicate #"^[\w\.]+@[\w\.]+", :valid? "blosavio@sagevisuals.com"}
;; {:path [2 :comment], :datum "work on addressing comments", :predicate #function[clojure.core/string?--5475], :valid? true}
;; {:path [2 :version], :datum 2, :predicate #function[speculoos-project-changelog-generator/version-predicate], :valid? true}]

  )


(def changelog-scalar-spec (repeat (assoc version-scalar-spec :changes (repeat change-scalar-spec))))

"Fun! A `clojure.lang.repeat` nested in a `clojure.lang.repeat`. Speculoos can handle that without a sweating. As long as there's not a repeat at the same path in the data. And there isn't. The changelog is hand-written, with each entry unique."

(validate-scalars changelog-data
                  changelog-scalar-spec)

(only-invalid (validate-scalars changelog-data
                                changelog-scalar-spec))

"Not in the least performant. But 1) Speculoos is experimental, exploring the concepts. No attention to performance, yet. 2) This style of 'off-line' validation (i.e., at the REPL, not in the middle of a high-throughput pipeline)...it's good enough."




;;;; "Collection specification and testing"

"No need to validate everything in the universe. Let's just do two. 1. Make sure we have required keys. 2. Verify relationship between version numbers."


"1. Ensuring required keys"

(def version-required-keys #{:date
                             :responsible
                             :version
                             :comment
                             :project-status
                             :stable
                             :urgency
                             :breaking?
                             :changes})

(def changes-required-keys #{:description
                             :date
                             :change-type
                             :breaking?})


(defn contains-required-keys?
  "Returns a predicate that tests whether a map passed as the first argument
  contains all keys enumerated in set `req-keys`."
  {:UUIDv4 #uuid "71880b60-6ce7-4477-84f0-f8716b047692"}
  [req-keys]
  #(empty? (clojure.set/difference req-keys (set (keys %)))))


(def version-coll-spec {:req-ver-keys? (contains-required-keys? version-required-keys)
                        :changes (vec (repeat 99 {:req-chng-keys? (contains-required-keys? changes-required-keys)}))})


(comment
  (let [c1 (get-in* changelog-data [1 :changes 2])
        c2 (dissoc c1 :description)]
    (validate-collections c2
                          {:req-chng-keys? (contains-required-keys? changes-required-keys)}))


  (validate-collections (get-in* changelog-data [2])
                        version-coll-spec)
  ;; ({:datum ...
  ;;, :valid? true,
  ;; :path-predicate [:req-ver-keys?],
  ;; :predicate #function[speculoos-project-changelog-generator/contains-required-keys?/fn--369860],
  ;; :ordinal-path-datum [], :path-datum []}
  ;; {:datum ...
  ;; :valid? true,
  ;;  :path-predicate [:changes 0 :req-chng-keys?],
  ;;  :predicate #function[speculoos-project-changelog-generator/contains-required-keys?/fn--369860],
  ;;  :ordinal-path-datum [:changes 0], :path-datum [:changes 0]}
  ;; {:datum ...
  ;;  :valid? true,
  ;;  :path-predicate [:changes 1 :req-chng-keys?],
  ;;  :predicate #function[speculoos-project-changelog-generator/contains-required-keys?/fn--369860],
  ;;  :ordinal-path-datum [:changes 1], :path-datum [:changes 1]}
  ;; {:datum ...
  ;;  :valid? true,
  ;;  :path-predicate [:changes 2 :req-chng-keys?],
  ;;  :predicate #function[speculoos-project-changelog-generator/contains-required-keys?/fn--369860],
  ;;  :ordinal-path-datum [:changes 2],
  ;;  :path-datum [:changes 2]})
  )

(def changelog-coll-spec [version-coll-spec])

(comment
  (validate-collections changelog-data
                        changelog-coll-spec)
  )

(def changelog-coll-spec [version-coll-spec
                          version-coll-spec])

(comment
  (validate-collections changelog-data
                        changelog-coll-spec)
  )


(def changelog-coll-spec (repeat version-coll-spec))

(comment
  (only-invalid (validate-collections changelog-data
                                      changelog-coll-spec))
  )




"2. Validating proper version incrementing"

"Someone might reasonably point out that that manually declaring the version number _inside_ a sequential collection is redundant and error-prone. But, I may change my mind in the future and swith to dotted version numbers, version letters, or some other format. Plus, the changelog is intended to be manchine- _and_ human-readable (with priority on the latter), and the subsections are split between differnt files. So it's more ergonomic to put in an explicit version number."



(defn properly-incrementing-versions?
  "Returns `true` if each successive version is exactly one more than previous."
  {:UUIDv4 #uuid "c937abfd-d230-4cd7-81c5-0f1a67ab911a"}
  [c-log]
  (every? #{1} (map #(- (:version %2) (:version %1)) c-log (next c-log))))


(comment
  (def test-c-log-1 [{:version 0}
                     {:version 1}
                     {:version 2}
                     {:version 3}
                     {:version 4}])

  (def test-c-log-2 [{:version 0}
                     {:version 1}
                     {:version 99}])

  (properly-incrementing-versions? test-c-log-1)
  (properly-incrementing-versions? test-c-log-2)

  (every? #{1} (map #(- (:version %2) (:version %1)) test-c-log-1 (next test-c-log-1)))

  (validate-collections changelog-data
                        [properly-incrementing-versions?])
  )

"The changelog collection specification is a vector --- mimicing the shape of the changelog data --- containing the `properly-incrementing-versions?` predicate followed by an infinite number of version collection specificaitons."

(def changelog-coll-spec (conj [properly-incrementing-versions?] (repeat version-coll-spec)))

(only-invalid (validate-collections changelog-data
                                    changelog-coll-spec))


"Finally, we can do a combo so that both scalars and collections are validated with a single function invocation."

(only-invalid (validate changelog-data
                        changelog-scalar-spec
                        changelog-coll-spec))

(valid? changelog-data
        changelog-scalar-spec
        changelog-coll-spec)


;; generate html/markdown panel for changelog.md file on GitHub

(def changelog-webpage-UUID #uuid "d571f801-3b49-4fd9-a5f3-620e034d0a8d")


(defn renamed-fns
  "Given a sequence `o2n` of 'old-fn-name'-to-'new-fn-name' maps, generate a
  hiccup/html unordered list of old to new."
  {:UUIDv4 #uuid "6f04837d-8314-4ef2-b729-14a1cb31b990"}
  [o2n]
  (let [sorted-oldnames (sort-by :old-function-name o2n)]
    (reduce #(conj %1 [:li [:code (name (:old-function-name %2))] " → " [:code (name (:new-function-name %2))]]) [:ul] sorted-oldnames)))


(comment
  (def test-rename-1 (get-in changelog-data [1 :changes 0 :renamed-functions]))

  (renamed-fns test-rename-1)


  (def test-rename-2 (get-in changelog-data [0]))

  (renamed-fns test-rename-2)


  (def test-rename-3 (get-in changelog-data [1 :changes 4 :renamed-functions]))

  (renamed-fns test-rename-3)
  )


(defn something-ed-fns
  "Given a sequence `changes` of changelog change maps, aggregate functions that
  have `change-type`, one of
  * `:added-functions`,
  * `:renamed-functions`,
  * `:moved-functions`,
  * `:removed-functions`, or
  * `:function-arguments`."
  {:UUIDv4 #uuid "d9a2782a-c903-4338-93f2-78871d352cdd"}
  [changes change-type]
  (let [aggregation (reduce #(clojure.set/union %1 (set (change-type %2))) #{} changes)]
    (if (= change-type :renamed-functions)
      [(renamed-fns aggregation)]
      (->> aggregation
           vec
           sort
           (map #(vector :code (str %)))
           (interpose ", ")))))


(comment
  (def test-changes-1 (get-in changelog-data [1 :changes]))

  (get-in test-changes-1 [0])

  (something-ed-fns test-changes-1 :added-functions)
  (something-ed-fns test-changes-1 :renamed-functions)
  (something-ed-fns test-changes-1 :moved-functions)
  (something-ed-fns test-changes-1 :removed-functions)
  (something-ed-fns test-changes-1 :altered-functions)
  ;; ([:code "data-with-specs"] ", " [:code "data-without-specs"] ", " [:code "defpred"] ", " [:code "specs-without-data"])

  (sort ["bc" "cdefg" "d""abc"])
)


(defn markdown-ize
  "Given a string `s`, insert hiccup/html for
  * [:code ...] for `...`
  * [:strong ...] for *...*
  * [:em ...] for _..._"
  {:UUIDv4 #uuid "47071a59-e140-4095-92ba-10db50fb6c4c"}
  [s]
  (let [replace-regexes {#"\`([\w\-]*)\`" :code
                         #"\*([\w\-]*)\*" :strong
                         #"\_([\w\-]*)\_" :em}]
    (reduce (fn [stg [re rp]] (clojure.string/replace stg re #(str [rp (% 1)]))) s replace-regexes)))


(comment
  (def test-string-1 "Re-named functions in collection-functions namespace from `blah-2` to `blah` to emphasize they're not merely a new version, but that they _operate_ on *any* Clojure collection type, and to be consisten with fn-in namespace members.")

  (markdown-ize test-string-1)

  (str [:code "blah"])


  (clojure.string/split test-string-1 #"\`([\w\-]*)\`")

  (def matcher (re-matcher #"\`([\w\-]*)\`" test-string-1))
  (re-find matcher)

  (re-groups matcher)

  (re-seq #"\`([\w\-]*)\`" test-string-1)

  (map #(vector :code (% 1)) (re-seq #"\`([\w\-]*)\`" test-string-1))

  (interleave (clojure.string/split test-string-1 #"\`([\w\-]*)\`")
              (map #(vector :code (% 1)) (re-seq #"\`([\w\-]*)\`" test-string-1)))

  (clojure.string/split test-string-1 #"\*([\w\-]*)\*")
  (clojure.string/split test-string-1 #"\_([\w\-]*)\_")
  )


(defn change-details
  "Given a sequence of `changes`, return a hiccup/html unordered list that lists
  the changes."
  {:UUIDv4 #uuid "5ed2bc16-9d57-4a88-acb4-ee5dae218110"}
  [changes]
  (let [grouped-changes (group-by #(:breaking? %) changes)
        breaking-changes (grouped-changes true)
        non-breaking-changes (concat (grouped-changes false)
                                     (grouped-changes nil))
        issue-reference #(if (:reference %) [:a {:href (:url (:reference %))} (:source (:reference %))] nil)
        issue-reference-seperator #(if (:reference %) ": " nil)]
    [:div
     [:h4 "Breaking changes"]
     (into [:ul] (map (fn [v] [:li [:div (issue-reference v) (issue-reference-seperator v) (str (:description v))]])) breaking-changes)
     [:h4 "Non-breaking changes"]
     (into [:ul] (map (fn [v] [:li [:div (issue-reference v) (issue-reference-seperator v) (str (:description v))]])) non-breaking-changes)]))


(comment
  (def test-changes-2 (get-in changelog-data [1 :changes]))

  (change-details test-changes-2)

  ({nil "a"} nil)

  )


(defn generate-version-section
  "Given a map `m` that contains data on a single changelog version, generate
  hiccup/html for a section that displays that info."
  {:UUIDv4 #uuid "6d232a01-4cc1-4b91-8b63-7a5da9a96cb3"}
  [m]
  (let [changed-function-div (fn [label change-type] (let [something-ized-fn (something-ed-fns (m :changes) change-type)]
                                                       (if (empty? something-ized-fn)
                                                         nil
                                                         (into [:div [:em (str label " functions: ")]] something-ized-fn))))]
    [:section
     [:h3 (str "version " (:version m))]
     [:p
      (str (:year (:date m)) " "
           (:month (:date m)) " "
           (:day (:date m))) [:br]
      (str (:name (:responsible m)) " (" (:email (:responsible m)) ")") [:br]
      [:em "Description: "] (str (:comment m)) [:br]
      [:em "Project status: "] [:a {:href "https://github.com/metosin/open-source/blob/main/project-status.md"} (name (:project-status m))] [:br]
      [:em "Urgency: "] (name (:urgency m)) [:br]
      [:em "Breaking: "] (if (:breaking? m) "yes" "no")]
     [:p
      (changed-function-div "added" :added-functions)
      (let [possible-renames (something-ed-fns (m :changes) :renamed-functions)
            _ (println "\npossible-renames:" possible-renames)]
        (if (= [[:ul]] possible-renames)
          nil
          (into [:div [:em "renamed functions: "]] possible-renames)))
      (changed-function-div "moved" :moved-functions)
      (changed-function-div "removed" :removed-functions)
      (changed-function-div "altered" :altered-functions)]
     (change-details (m :changes))
     [:hr]]))


(comment
  (get-in* changelog-data [1])
  (get-in* changelog-data [1 :date])
  (get-in* changelog-data [1 :date :day])

  (str 2024 "--" "today" "---" 999)
  (generate-version-section (get changelog-data 0))
  ;; [:section
  ;; [:h3 "version 0"]
  ;; [:p "2024 June 6" [:br] "Brad Losavio (blosavio@sagevisuals.com)" [:br]
  ;; [:em "Notes: "] "initial public release" [:br]
  ;; [:em "Project status: "] [:a {:href "https://github.com/metosin/open-source/blob/main/project-status.md"} "experimental"] [:br]
  ;; [:em "Urgency: "] "low" [:br] [:em "Breaking?: "] "no"]
  ;; [:h4 "Changes"]
  ;; [:p [:div [:em "added functions: "]] [:br]
  ;; [:div [:em "renamed functions: "] []] [:br]
  ;; [:div [:em "moved functions: "]] [:br]
  ;; [:div [:em "removed functions: "]] [:br]
  ;; [:div [:em "altered functions: "]]]]
  )


(def changelog-info
  [:section
   [:h4 "Changelog info"]
   [:p#info "A human- and machine-readable " [:code "changelog.edn"] " will accompany each version at the project's root directory. " [:code "changelog.edn"] " is tail-appended file constructed from all previous releases, possibly automatically-composed of per-version " [:code "changelog-v" [:em "N"] ".edn"] " files in a sub-directory."]
   [:p "A " [:code "changelog.md"] " file, intended for display on the web, is generated by a script. This script also contains specifications describing the changelog data."]
   [:p "Tentative policy: Bug fixes are non-breaking changes."]])


(def changelog-md-footer [:p#page-footer
                          (copyright)
                          [:br]
                          (str "Compiled " (short-date) ".")
                          [:span#uuid [:br] changelog-webpage-UUID]])


(spit "changelog.md"
      (h2/html
       (vec (-> [:body
                 [:h1 "Speculoos library changelog"]
                 [:a {:href "#info"} "changelog meta"]]
                #_(map #(vector :section [:h3 (str "Version " %)]) [0 1 2])
                #_(generate-version-section (get changelog-data 1))
                (into (map #(generate-version-section %) (reverse changelog-data)))
                (conj changelog-info)
                (conj changelog-md-footer)))))


(comment
  [:article
   [:h1 "Speculoos library changelog"]
   [:section
    [:h3 "version 2"]
    [:p "Foo bar baz."]]
   [:section
    [:h3 "version 1"]
    [:p "Zab rab oof."]]
   [:section
    [:h3 "version 0"]
    [:p "Initial public relase."]]]
  )


;; stub for case study webpage

(def case-study-UUID #uuid "e3856cb2-b1d6-40cb-8659-8f2e7e56fcca")


(spit "doc/case_study.html"
      (page-template
       "Case study: Specifying and validating a library changelog"
       case-study-UUID
       [:body
        [:article
         [:h1 "Case study: Specifying and validating a library changelog"]
         [:p "Foo bar baz."]
         [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]]]))








(comment
  ;; explore subvec

  (def test-vec-1 (subvec [0 1 2 3 4 5] 0 6))

  (type test-vec-1)
  ;; clojure.lang.APersistentVector$SubVector

  (get test-vec-1 0) ;; 0
  (get-in test-vec-1 [3]) ;; 3

  (derive clojure.lang.APersistentVector$SubVector :fn-in.core/:vector)

  (get* test-vec-1 0)
  (get-in* test-vec-1 [0])
  )