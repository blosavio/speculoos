(ns changelog-specifications
  "Speculoos specifications for changelog entries."
  {:UUIDv4 #uuid "c2d0b1f4-3af9-481d-b34a-6e96e6989a00"
   :no-doc true})


;; scalar specifications


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


(defn day-predicate [n] (and (int? n) (<= 1 n 31)))


(def date-spec {:year year-predicate
                :month month-predicate
                :day day-predicate})


(defn ticket-predicate [t] (or (string? t) (uuid? t)))


(def reference-spec {:source string?
                     :url #"^https:\/{2}[\w\/\.]+"
                     :ticket ticket-predicate})


(def change-kinds #{:initial-release

                    :security

                    :performance-improvement
                    :performance-regression

                    :memory-improvement
                    :memory-regression

                    :network-resource-improvement
                    :network-resource-regression

                    :added-dependency
                    :removed-dependency
                    :dependency-version

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
                    :error-message

                    :tests
                    :bug-fix
                    :deprecated-something

                    :policy
                    :meta-data
                    :documentation
                    :website
                    :release-note

                    :other})


;; see Metosin's 'Project Status Model'
;; https://github.com/metosin/open-source/blob/main/project-status.md
;;
;;   * experimental: Not recommended for production use.  No support nor maintenance. Testing and feedback welcome.
;;   * active:       Actively developed. Recommended for use.
;;   * stable:       Maintained and recommended for use. No major new features, but PRs are welcome.
;;   * inactive:     Okay for production use. Will receive security fixes, but no new developments. Not recommended for new projects.
;;   * deprecated:   Not recommended for any use.


(def status-predicate #{:experimental
                        :active
                        :stable
                        :inactive
                        :deprecated})


(defn breaking-predicate [b] (or (nil? b) (boolean? b)))


;; Policy: ':breaking?' is `false` if that version can be installed and will
;; work in all known circumstances with *no* other changes (including changes in
;; deps); otherwise, the version is `breaking? true`.

;; The following lists are not chiseled in stone. Use judgment, and err on the
;; side of caution. Think of other people!
;;
;; Examples:
;;   breaking:
;;     all regressions (performance, memory, network)
;;     added or changed dependency (if outward-facing, not dev only; see note below)
;;     renamed/moved/removed functions
;;     stricter input requirements
;;     decreased return
;;     different default
;;   non-breaking:
;;     all improvements (performance, memory, network)
;;     removed dependencies
;;     added functions
;;     relaxed input requirements
;;     increased returns
;;     implementation
;;     source code
;;     docs
;;
;;  Open question: Is a bug-fix a breaking change? What if someone was depending
;;  on the incorrect behavior. If the issue truly is a bug, then a bug fix ought
;;  to be isolated to an implementation issue, and therefore a non-breaking
;;  change. Probably rare.
;;
;;  Also: Changes in dependencies that are only used during dev (i.e., zprint
;;  for generating documentation) are non-breaking changes.


(def renamed-function-spec {:old-function-name symbol?
                            :new-function-name symbol?})

(def person-spec {:name string?
                  :email #"^[\w\.]+@[\w\.]+"})


(def change-scalar-spec {:date date-spec
                         :responsible person-spec
                         :description string?
                         :reference reference-spec
                         :change-type change-kinds
                         :breaking? breaking-predicate
                         :added-functions (repeat symbol?)
                         :renamed-functions (repeat renamed-function-spec)
                         :moved-functions (repeat symbol?)
                         :altered-functions (repeat symbol?)
                         :removed-functions (repeat symbol?)})


(defn version-predicate [i] (and (int? i) (<= 0 i)))


(def version-scalar-spec {:date date-spec
                          :responsible person-spec
                          :version version-predicate
                          :comment string?
                          :project-status status-predicate
                          :stable boolean?
                          :urgency #{:low :medium :high}
                          :breaking? boolean?})


(def changelog-scalar-spec (repeat (assoc version-scalar-spec :changes (repeat change-scalar-spec))))


;; collection specifications

;;; required keys

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


(def changelog-coll-spec (vec (repeat 99 version-coll-spec)))


;;; proper version incrementing

"Someone might reasonably point out that that manually declaring the version number _inside_ a sequential collection is redundant and error-prone. But, I may change my mind in the future and switch to dotted version numbers, version letters, or some other format. Plus, the changelog is intended to be machine- _and_ human-readable (with priority on the latter), and the subsections are split between different files. So it's more ergonomic to put in an explicit version number."


(defn properly-incrementing-versions?
  "Returns `true` if each successive version is exactly one more than previous."
  {:UUIDv4 #uuid "c937abfd-d230-4cd7-81c5-0f1a67ab911a"}
  [c-log]
  (every? #{1} (map #(- (:version %2) (:version %1)) c-log (next c-log))))


(def changelog-coll-spec (concat [properly-incrementing-versions?] (vec (repeat 99 version-coll-spec))))