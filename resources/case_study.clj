(ns case-study
  "Generate an hiccup/html webpage describing a case study concerning specifying
  and validating the Speculoos project changelog.edn"
  {:no-doc true}
  (:require
   [fn-in.core :refer [get-in*]]
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [readmoi.core :refer [page-template
                         prettyfy
                         print-form-then-eval
                         tidy-html-document]]
   [speculoos.core :refer [only-invalid
                           valid-collections?
                           valid-scalars?
                           valid?
                           validate
                           validate-collections
                           validate-scalars]]
   [speculoos.utility :refer [basic-collection-spec-from-data
                              collections-without-predicates
                              data-from-spec
                              defpred
                              in?
                              predicates-without-collections
                              predicates-without-scalars
                              scalars-without-predicates
                              sore-thumb
                              spec-from-data
                              thoroughly-valid?
                              validate-predicate->generator
                              thoroughly-valid-scalars?]]))


(def changelog-data (load-file "resources/case_study/edited_changelog.edn"))


(def page-body
  [:body
   [:article
    [:h1 "Case study: Specifying and validating the Speculoos library changelog"]

    [:section#intro
     [:p "So what's it like to use " [:a {:href "https://github.com/blosavio/speculoos"} "Speculoos"] " on a task that's not merely a demonstration? Let's specify and validate the Speculoos library " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/changelog_entries/changelog.edn"} [:code "changelog.edn"]] ". To begin, a few words about the changelog itself."]

     [:p "Speculoos is an experimental library. Among the ideas I wanted to explore is a changelog published in Clojure " [:strong "e"] "xtensible " [:strong "d"] "ata " [:strong "n"] "otation (" [:a {:href "https://github.com/edn-format/edn"} "edn"] "). The goal is to have a single, canonical, human- and machine-readable document that describes the project's changes from one version to the next. That way, it would be straightforward to automatically generate a nicely-formatted changelog webpage and query the changelog data so that people can make informed decisions about changing versions."]

     [:p [:em "Note: Since publishing this case study, I've released a " [:a {:href "https://github.com/blosavio/chlog"} "separate library"] " that explores these principles."]]
     
     [:p "Here's the info that I think would be useful for a changelog entry."]

     [:ul
      [:li "Version number"]
      [:li "Date"]
      [:li "Person responsible, with contact info"]
      [:li "Status of the project (i.e., stable, deprecated, etc.)"]
      [:li "Urgency (i.e., high for a security fix, etc.)"]
      [:li "Flag if changes are breaking from previous version"]
      [:li "Free-form comments"]]

     [:p "We can quickly assemble an example."]

     [:pre [:code "{:version 99\n :date {:year 2025\n        :month \"November\"\n        :day 12}\n :responsible {:name \"Kermit Frog\"\n               :email \"its.not.easy@being.gre.en\"}\n :project-status :stable\n :urgency :low\n :breaking? false\n :comment \"Improved arithmetic capabilities.\"\n :changes [«see upcoming discussion»]}"]]

     [:p "Furthermore, for each of those changelog entries, I think it would be nice to tell people more details about the individual changes so they can make technically supported decisions about changing versions. A single, published version could consist of multiple changes, associated to a key " [:code ":changes"] ", with each change detailed with this info."]

     [:ul
      [:li "A free-form, textual description of the change."]
      [:li "A reference, (e.g., GitHub issue number, JIRA ticket number, etc.)"]
      [:li "Kind of change (i.e., bug fix, renamed function, removed function, improved performance, etc.)"]
      [:li "Flag indicating if this particular change is breaking."]
      [:li "Added, renamed, moved, altered, or deleted functions."]]

     [:p "Here's an example of one change included in a published version."]

     [:pre [:code "{:description \"Addition function `+` now handles floating point decimal number types.\"\n :reference {:source \"Issue #78\"\n             :url \"https://example.com/issue/87\"}\n :change-type :relaxed-input-requirements\n :breaking? false\n :altered-functions ['+]\n :date {:year 2025\n        :month \"November\"\n        :day 8}\n :responsible {:name \"Fozzie Bear\"\n               :email \"fozzie@wocka-industries.com\"}}"]]

     [:p "The date and person responsible for an individual change need not be the same as the date and person responsible for the version that contains it. So while Kermit was responsible for publishing the overall version on 2025 November 12, Fozzie was responsible for creating the individual change to the plus function on 2025 November 08."]

     [:p "With the expected shape of our changelog data established, we can now compose the specifications that will allow us to validate the data. We must keep in mind Speculoos ' "[:a {:href "https://github.com/blosavio/speculoos/tree/main?tab=readme-ov-file#-three-mottos"} "Three Mottos" ] ". Motto #1 reminds us to keep scalar a collection specifications separate. The validation functions themselves enforce this principle, but adhering to Motto #1 helps minimize confusion."]

     [:p " Motto #2 reminds us to shape the specification so that it mimics the data. This motto reveals a convenient tactic: copy-paste the data, delete the scalars, and insert predicates."]

     [:p "Motto #3 reminds us to ignore un-paired predicates and un-paired datums. In practice, the consequence of this principle is that we may provide more data than we specify, and the un-specified data merely flows through, un-validated. On the other hand, we may specify more elements than actually exist in a particular piece of data. That's okay, too. Those un-paired predicates will be ignored."]

     [:p "Our overall strategy is this: Build up specifications from small pieces, testing those small pieces along the way. Then, after we we're confident in the small pieces, we can assemble them at the end. We'll start with specifying and validating the scalars. Once we've done that, we'll put them aside. Then, we'll specify and validate the collections, testing them until we're confident we've got the correct specifications. At the end, we'll bring together both scalar validation and collection validation into a combo validation."]

     [:p "The structure of this case study document should reinforce those principles."]

     [:a {:href "#scalars"} "Specifying & validating scalars"] [:br]
     [:a {:href "#collections"} "Specifying & validating collections"] [:br]
     [:a {:href "#combo"} "Combo validations"] [:br]
     [:a {:href "#conclusion"} "Observations & conclusion"]

     [:p "Scalars and collections are separate concepts, so we handle them in different steps. At the end, merely for convenience, we can use a combo validation that separately validates the scalars and the collections with a single invocation."]

     [:p "Let's set up our environment with the tools we'll need."]

     [:pre
      [:code "(require '[speculoos.core :refer [valid-scalars? valid-collections? valid?]]\n         '[fn-in.core :refer [get-in*]])"]
      [:br]
      [:br]
      (print-form-then-eval "(set! *print-length* 99)")]]

    [:p "Code for this case study may be found at the following links."]

    [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/case_study/changelog_specifications.clj"} "predicates & specifications"]
    [:br]
    [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/case_study/edited_changelog.edn"} "fictitious changelog data"]


    [:section#scalars
     [:h3 "Specifying & validating scalars"]

     [:p "We'll start simple. Let's compose a " [:em "date"] " specification. Informally, a date is a year, a month, and a day. Let's stipulate that a valid year is " [:em "An integer greater-than-or-equal-to two-thousand"] ". Here's a predicate for that concept."]

     [:pre (print-form-then-eval "(defn year-predicate [n] (and (int? n) (<= 2000 n)))")]

     [:p "Speculoos predicates are merely Clojure functions. Let's try it."]

     [:pre
      (print-form-then-eval "(year-predicate 2025)")
      [:br]
      [:br]
      (print-form-then-eval "(year-predicate \"2077\")")]

     [:p "That looks good. Integer " [:code "2025"] " is greater than two-thousand, while string " [:code "\"2077\""] " is not an integer."]

     [:p "Checking day of the month is similar."]

     [:pre (print-form-then-eval "(defn day-predicate [n] (and (int? n) (<= 1 n 31)))")]

     [:p [:code "day-predicate"] " is satisfied only by an integer between one and thirty-one, inclusive."]

     [:p "Speculoos can validate a scalar by testing if it's a member of a set. A valid month may only be one of twelve elements. Let's enumerate the months of the year, months represented as strings."]

     [:pre (print-form-then-eval "(def month-predicate #{\"January\"
                                                          \"February\"
                                                          \"March\"
                                                          \"April\"
                                                          \"May\"
                                                          \"June\"
                                                          \"July\"
                                                          \"August\"
                                                          \"September\"
                                                          \"October\"
                                                          \"November\"
                                                          \"December\"})")]

     [:p "Let's see how that works."]

     [:pre (print-form-then-eval "(month-predicate \"August\")")]

     [:p [:code "month-predicate"] " is satisfied (i.e., returns a truthy value) because string " [:code "\"August\""] " is a member of the set."]

     [:pre (print-form-then-eval "(month-predicate :November)")]

     [:p "Keyword " [:code ":November"] " does not satisfy " [:code "month-predicate"] " because it is not a member of the set. " [:code "month-predicate"] " returns a falsey value, " [:code "nil"] "."]

     [:p "We've now got predicates to check a year, a month, and day. The notion of " [:em "date"] " includes a year, month, and a day traveling around together. We can collect them into one group using a Clojure collection. A hash-map works well in this scenario."]

     [:pre [:code "{:year 2020\n :month \"January\"\n :day 1}"]]

     [:p "Speculoos specifications are plain old regular Clojure data collections. " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#-three-mottos"} "Motto #2"] " reminds us to shape the specification to mimic the data. To create a scalar specification, we could copy-paste the data, and delete the scalars…"]

     [:pre [:code "{:year ____\n :month ___ \n :day __}"]]

     [:p "…and insert our predicates."]

     [:pre (print-form-then-eval "(def date-spec {:year year-predicate :month month-predicate :day day-predicate})")]

     [:p "Let's check our progress against some valid data. We're validating scalars (Motto #1), so we'll use a function with a " [:code "-scalars"] " suffix. The data is the first argument on the upper row, the specification is the second argument on the lower row."]

     [:pre (print-form-then-eval "(valid-scalars? {:year 2024 :month \"January\" :day 1} {:year year-predicate :month month-predicate :day day-predicate})" 85 75)]

     [:p "Each of the three scalars satisfies their respective predicates (Motto #3), so " [:code "valid-scalars?"] " returns " [:code "true"] "."]

     [:p "Now let's feed in some invalid data."]

     [:pre (print-form-then-eval "(valid-scalars? {:year 2024 :month \"Wednesday\" :day 1} {:year year-predicate :month month-predicate :day day-predicate})" 85 85)]

     [:p "While " [:code "\"Wednesday\""] " is indeed a string, it is not a member of the " [:code "month-predicate"] " set, so " [:code "valid-scalars?"] " returns " [:code "false"] "."]

     [:div.no-display
      [:p "Perhaps we could have used an " [:code "instant"] " literal like this."]

      [:pre (print-form-then-eval "(java.util.Date.)")]

      [:p "But I wanted to demonstrate how Speculoos can specify and validate hand-made date data."]]

     [:p "Now that we can validate the date component of the changelog, we'll need to specify and validate the information about the person responsible for that publication. The changelog information about a person gathers their name, a free-form string, and an email address, also a string. In addition to being a string, a valid email address:"]

     [:ul
      [:li "Starts with one or more alphanumeric characters or periods,"]
      [:li "Followed by exactly one " [:code "@"] " character,"]
      [:li "Followed by one or more alphanumeric characters or periods."]]

     [:p "Regular expressions are powerful tools for testing those kind of string properties, and Speculoos scalar validation supports them. A regular expression appearing in a scalar specification is considered a predicate. Let's make the following a specification about a changelog person."]

     [:pre (print-form-then-eval "(def person-spec {:name string? :email #\"^[\\w\\.]+@[\\w\\.]+\"})")]

     [:p "Let's give that specification a whirl. First, we validate some valid person data (data in upper row, specification in lower row)."]

     [:pre (print-form-then-eval "(valid-scalars? {:name \"Abraham Lincoln\" :email \"four.score.seven.years@gettysburg.org\"} {:name string? :email #\"^[\\w\\.]+@[\\w\\.]+\"})" 95 95)]

     [:p "Both name and email scalars satisfied their paired predicates. Now, let's see what happens when we validate some data that is invalid."]

     [:pre (print-form-then-eval "(valid-scalars? {:name \"George Washington\" :email \"crossing_at_potomac\"} {:name string? :email #\"^[\\w\\.]+@[\\w\\.]+\"})")]

     [:p "Oops. That email address does not satisfy the regular expression because it does not contain an " [:code "@"] " character, so the person data is invalid."]

     [:p "Perhaps the most pivotal single datum in a changelog entry is the version number. For our discussion, let's stipulate that a version is an integer greater-than-or-equal-to zero. Here's a predicate for that."]

     [:pre (print-form-then-eval "(defn version-predicate [i] (and (int? i) (<= 0 i)))")]

     [:p "And a pair of quick demos."]

     [:pre
      (print-form-then-eval "(version-predicate 99)")
      [:br]
      [:br]
      (print-form-then-eval "(version-predicate -1)")]

     [:p "At this point, let's assemble what we have. Speculoos specifications are merely Clojure collections that mimic the shape of the data. So let's collect those predicates into a map."]

     [:pre [:code "{:version version-predicate\n :date date-spec\n :person person-spec}"]]

     [:p "Notice, " [:code "date-spec"] " and " [:code "person-spec"] " are each themselves specifications. We compose a Speculoos specification using standard Clojure composition."]

     [:p "The partial changelog entry might look something like this."]

     [:pre [:code
            "{:version 99\n :date {:year 2025\n        :month \"August\"\n        :day 1}\n :person {:name \"Abraham Lincoln\"\n          :email \"four.score.seven.years@gettysburg.org\"}}"]]

     [:p "Let's check our work so far. First, we'll validate some data we know is valid."]

     [:pre (print-form-then-eval "(valid-scalars? {:version 99 :date {:year 2025 :month \"August\" :day 1} :person {:name \"Abraham Lincoln\" :email \"four.score.seven.years@gettysburg.org\"}} {:version version-predicate :date date-spec :person person-spec})")]

     [:p "Dandy."]

     [:p "Second, we'll feed in some data we suspect is invalid."]

     [:pre (print-form-then-eval "(valid-scalars? {:version 1234 :date {:year 2055 :month \"Octoberfest\" :day 1} :person {:name \"Paul Bunyan\" :email \"babe@blue.ox\"}} {:version version-predicate :date date-spec :person person-spec})")]

     [:p "Hmm. " [:em "Something"] " doesn't satisfy their predicate, but my eyesight isn't great and I can't immediately spot the problem. Let's use a more verbose function, " [:code "validate-scalars"] ", which returns detailed results."]

     [:pre (print-form-then-eval "(validate-scalars {:version 1234 :date {:year 2055 :month \"Octoberfest\" :day 1} :person {:name \"Paul Bunyan\" :email \"babe@blue.ox\"}} {:version version-predicate :date date-spec :person person-spec})")]


     [:p "Ugh, too verbose. Let's pull in a utility that filters the validation results so only the invalid results are displayed."]

     [:pre (print-form-then-eval "(require '[speculoos.core :refer [only-invalid]])")]

     [:p "Now we can focus."]

     [:pre (print-form-then-eval "(only-invalid (validate-scalars {:version 1234 :date {:year 2055 :month \"Octoberfest\" :day 1} :person {:name \"Paul Bunyan\" :email \"babe@blue.ox\"}} {:version version-predicate :date date-spec :person person-spec}))")]

     [:p "Aha. One scalar datum failed to satisfy the predicate it was paired with. " [:code "\"Octoberfest\""] " is not a month enumerated by our month predicate."]

     [:p "So far, our changelog entry has a version number, a date, and a person. In the introduction, we outlined that a changelog entry would contain more info than that. So let's expand it."]

     [:p "It would be nice to tell people whether that release was breaking relative to the previous version. The initial release doesn't have a previous version, so it's breakage will be " [:code "nil"] ". For all subsequent versions, breakage will carry a " [:code "true"] " or " [:code "false"] " notion, so we'll require that datum be a boolean or " [:code "nil"] "."]

     [:pre (print-form-then-eval "(defn breaking-predicate [b] (or (nil? b) (boolean? b)))")]

     [:p "Also, it would be nice if we indicate the status of the project upon that release. A " [:a {:href "https://github.com/metosin/open-source/blob/main/project-status.md"} "reasonable enumeration of a project's status"] " might be " [:em "experimental"] ", "  [:em "active"] ", "  [:em "stable"] ", "  [:em "inactive"] ", or "  [:em "deprecated"] ". Since a valid status may only be one of a handful of values, a set makes a good membership predicate."]

     [:pre (print-form-then-eval "(def status-predicate #{:experimental :active :stable :inactive :deprecated})")]

     [:p "Let's assemble the version predicate, the breaking predicate, and the status predicate into another partial, temporary specification."]

     [:pre [:code "{:version version-predicate\n :breaking? breaking-predicate\n :project-status status-predicate}"]]

     [:p "Now that we have another temporary, partial specification, let's use it to validate (data in the upper row, specification in the lower row)."]

     [:pre (print-form-then-eval "(valid-scalars? {:version 99 :breaking? false :project-status :stable} {:version version-predicate :breaking? breaking-predicate :project-status status-predicate})" 115 25)]

     [:p "Now, let's validate some invalid data."]

     [:pre (print-form-then-eval "(valid-scalars? {:version 123 :breaking? true :project-status \"finished!\"} {:version version-predicate :breaking? breaking-predicate :project-status status-predicate})" 115 25)]

     [:p "Perhaps we're curious about exactly which datum failed to satisfy its predicate. So we switch to " [:code "validate-scalars"] " and filter with " [:code "only-invalid"] "."]

     [:pre (print-form-then-eval "(only-invalid (validate-scalars {:version 123 :breaking? true :project-status :finished!} {:version version-predicate :breaking? breaking-predicate :project-status status-predicate}))" 115 75)]

     [:p "Yup. Scalar " [:code ":finished!"] " is not enumerated by " [:code "status-predicate"] "."]

     [:p "A comment concerning a version is a free-form string, so we can use a bare " [:code "string?"] " predicate. Upgrade urgency could be represented by three discrete levels, so a set " [:code "#{:low :medium :high}"] " makes a fine predicate."]

     [:p "Now that we've got all the individual components for validating the version number, date (with year, month, day), person responsible (with name and email), project status, breakage, urgency, and a comment, we can assemble the specification for one changelog entry."]

     [:pre [:code "{:version version-predicate\n :date date-spec\n :responsible person-spec\n :project-status status-predicate\n :breaking? breaking-predicate\n :urgency #{:low :medium :high}\n :comment string?}"]]

     [:p "Let's use that specification to validate some data. Here's a peek behind the curtain: At this very moment, I don't have sample data to show you. I need to write some. I'm going to take advantage of the fact that a Speculoos specification is a regular Clojure data structure whose shape mimics the data. I already have the specification in hand. I'm going to copy-paste the specification, delete the predicates, and then insert some scalars."]

     [:p "Here's the specification with the predicates deleted."]

     [:pre [:code "{:version ___\n :date {:year ___\n        :month ___\n        :day ___}\n :responsible {:name ___\n               :email___}\n :project-status ___\n :breaking? ___\n :urgency ___\n :comment ___}"]]

     [:p "That will serve as a template. Then I'll insert some scalars."]

     [:pre [:code "{:version 55\n :date {:year 2025\n        :month \"December\"\n        :day 31}\n :responsible {:name \"Rowlf\"\n               :email \"piano@example.org\"}\n :project-status :active\n :breaking? false\n :urgency :medium\n :comment \"Performance improvements and bug fixes.\"}"]]

     [:p "Let's run a validation with that data and specification."]

     [:pre (print-form-then-eval "(valid-scalars?
                                  {:version 55
                                   :date {:year 2025
                                          :month \"December\"
                                          :day 31}
                                   :responsible {:name \"Rowlf Dog\"
                                                 :email \"piano@example.org\"}
                                   :project-status :active
                                   :breaking? false
                                   :urgency :medium
                                   :comment \"Performance improvements and bug fixes.\"}

                                  {:version version-predicate
                                   :date date-spec
                                   :responsible person-spec
                                   :project-status status-predicate
                                   :breaking? breaking-predicate
                                   :urgency #{:low :medium :high}
                                   :comment string?})")]

     [:p "Since I wrote the data based on the specification, it's a good thing the data is valid."]

     [:p "Let me change the version to a string, validate with the verbose " [:code "validate-scalars"] " and filter the output with " [:code "only-invalid"] " to keep only the invalid scalar+predicate pairs."]

     [:pre (print-form-then-eval "(only-invalid (validate-scalars
                                  {:version \"foo-bar-baz\"
                                   :date {:year 2025
                                          :month \"December\"
                                          :day 31}
                                   :responsible {:name \"Rowlf Dog\"
                                                 :email \"piano@example.org\"}
                                   :project-status :active
                                   :breaking? false
                                   :urgency :medium
                                   :comment \"Performance improvements and bug fixes.\"
                                   :changes []}

                                  {:version version-predicate
                                   :date date-spec
                                   :responsible person-spec
                                   :project-status status-predicate
                                   :breaking? breaking-predicate
                                   :urgency #{:low :medium :high}
                                   :comment string?}))")]

     [:p "Yup. String " [:code "\"foo-bar-baz\""] " is not a valid version number according to " [:code "version-predicate"] ". If I had made a typo while writing that changelog entry, before it got any further, validation would have informed me that I needed to correct that version number."]

     [:p "In the introduction, we mentioned that each version entry could contain a sequence of maps detailing the specific changes. That sequence is associated to " [:code ":changes"] ". Maybe you noticed I snuck that into the data in the last example. We haven't yet written any predicates for that key-val, so " [:code "validate-scalars"] " ignored it (Motto #3). We won't ignore it any longer."]

     [:p "The nesting depth is going to get messy, so let's put aside the version entry and zoom in on what a change entry might look like. Way back at the beginning, of this case study, we introduced this example."]

     [:pre [:code "{:description \"Addition function `+` now handles floating point decimal number types.\"\n :reference {:source \"Issue #78\"\n             :url \"https://example.com/issue/87\"}\n :change-type :relaxed-input-requirements\n :breaking? false\n :altered-functions ['+]\n :date {:year 2025\n        :month \"November\"\n        :day 8}\n :responsible {:name \"Fozzie Bear\"\n               :email \"fozzie@wocka-industries.com\"}}"]]

     [:p "This 'change' entry provides details about who changed what, when, and a reference to an issue-tracker. A single version may bundle multiple of these change entries."]

     [:p "I'll copy-paste the sample and delete the scalars."]

     [:pre [:code "{:description ___\n :reference {:source ___\n             :url ___}\n :change-type ___\n :breaking? ___\n :altered-functions []\n :date {:year ___\n        :month ___\n        :day ___}\n :responsible {:name ___\n               :email ___}}"]]

     [:p "That'll be a good template for a change entry specification."]

     [:p "We can start filling in the blanks because we already have specifications for " [:em "date"] ", " [:em "person"] ", and " [:em "breaking"] ". Similarly, a description is merely free-form text which can be validated with a simple " [:code "string?"] " predicate."]

     [:pre [:code "{:description string?\n :reference {:source ___\n             :url ___}\n :change-type ___\n :breaking? breaking-predicate\n :altered-functions []\n :date date-spec\n :responsible person-spec}"]]

     [:p "Now we can tackle the remaining blanks. The " [:em "reference"] " associates this change to a issue-tracker. The " [:code ":source"] " is a free-form string (i.e., \"GitHub Issue #27\", etc.), while " [:code ":url"] " points to a web-accessible resource. Let's require that a valid entry be a string that starts with \"https://\". We can demonstrate that regex."]

     [:pre
      (print-form-then-eval "(re-find #\"^https:\\/{2}[\\w\\/\\.]*\" \"https://example.com\")" 50 50)
      [:br]
      [:br]
      [:br]
      (print-form-then-eval "(re-find #\"^https:\\/{2}[\\w\\/\\.]*\" \"ht://example.com\")" 50 55)]

     [:p "The first example returns a match (truthy), while the second example is a malformed url and fails to find a match (falsey)."]

     [:p "Different issue trackers have different ways of referring to issues, so to accommodate that, we can include an optional " [:code ":ticket"] " entry that can be a free-form string or a " [:span.small-caps "uuid"] "."]

     [:pre (print-form-then-eval "(defn ticket-predicate [t] (or (string? t) (uuid? t)))")]

     [:p "Let's assemble those predicates to define this sub-component."]

     [:pre (print-form-then-eval "(def reference-spec {:source string?
                                                        :url #\"^https:\\/{2}[\\w\\/\\.]*\"
                                                        :ticket ticket-predicate})")]


     [:p "Slowly and steadily filling in the blanks, our change specification currently looks like this."]

     [:pre [:code "{:description string?\n :reference reference-spec\n :change-type ___\n :breaking? breaking-predicate\n :altered-functions []\n :date date-spec\n :responsible person-spec}"]]

     [:p "Let's take a look at the first remaining blank. "[:em "Change type"] " may be one of an enumerated set of values. That term " [:em "set"] " is a clue to writing the predicate. We ought to use a set as a membership predicate if we can enumerate all possible valid values. I've jotted down the common cases I can think of."]

     [:div.no-display
      ;; Need to deine a `change-kinds` set, but make an ordered version for readability.

      (def change-kinds-ordered [:initial-release

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

                                 :other])

      ;; bind the operational symbol to a proper set; this value will be used during evaluation
      (def change-kinds (set change-kinds-ordered))

      ;; create a readable string that appears to be set with the desired ordering
      (def change-kinds-str (-> (str "(def change-kinds " change-kinds-ordered ")")
                                (clojure.string/replace #"\[" "#{")
                                (clojure.string/replace #"\]" "}")))]

     [:pre (prettyfy change-kinds-str 12)]

     [:p "Maybe this a good idea for validating changelog data, maybe it's not. But it's an experiment either way."]

     [:p "On to that second blank. An " [:em "altered function"] " is a collection of symbols that inform the reader of the changelog the precise names of functions that were altered during that particular change. There may be zero or more, so a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#non-terminating-sequences"} "non-terminating repeat"] " of predicates is an elegant tool to specify that concept."]

     [:pre
      [:code ";; data           scalar specification"] [:br]
      [:code "['foo          ] [symbol?                ]"] [:br]
      [:code "['foo 'bar     ] [symbol? symbol?        ]"] [:br]
      [:code "['foo 'bar 'baz] [symbol? symbol? symbol?]"] [:br]
      [:br]
      [:code "     ⋮              ⋮"] [:br]
      [:br]
      [:code "['foo 'bar 'baz 'zab 'oof…] (repeat symbol?)"]]

     [:p "Because Speculoos ignores un-paired predicates, the non-terminating sequence of " [:code "symbol?"] " predicates conveys the notion of " [:em "zero or more symbols"] "."]

     [:p "Now we've created all the predicates for the parts of a change entry. When assembled into a scalar specification, it looks like this."]

     [:pre (print-form-then-eval "(def change-scalar-spec {:date date-spec
                                                            :description string?
                                                            :reference reference-spec
                                                            :change-type change-kinds
                                                            :breaking? breaking-predicate
                                                            :altered-functions (repeat symbol?)})")]

     [:p "Remember, any single changelog version may contain zero or more of that shape of changelog data. To remind ourselves what that looks like, let's bind that version specification from before to a name." ]

     [:pre (print-form-then-eval "(def version-scalar-spec {:date date-spec
                                                             :responsible person-spec
                                                             :version version-predicate
                                                             :comment string?
                                                             :project-status status-predicate
                                                             :stable boolean?
                                                             :urgency #{:low :medium :high}
                                                             :breaking? boolean?
                                                             :changes []})")]

     [:p "Let's stuff an infinite number of " [:code "change-scalar-spec"] "s into the " [:code ":changes"] " slot of " [:code "version-scalar-spec"] "."]

     [:pre (print-form-then-eval "(def version-scalar-spec {:date date-spec
                                                             :responsible person-spec
                                                             :version version-predicate
                                                             :comment string?
                                                             :project-status status-predicate
                                                             :stable boolean?
                                                             :urgency #{:low :medium :high}
                                                             :breaking? boolean?
                                                             :changes (repeat change-scalar-spec)})")]

     [:p "Now, this one, single " [:code "version-scalar-spec"] " could potentially validate an arbitrary number of changes. Each of those changes can announce alterations to an arbitrary number of functions."]

     [:p "If we recall from the beginning, a changelog is an ever-growing sequence of versions. Upon the initial release, we have one version, which we could validate with this specification."]

     [:pre [:code "[version-scalar-spec]"]]

     [:p "After a while, we make some upgrades, and release a second version. The changelog has a version entry appended the sequence. The two-element changelog can be validated with this specification."]

     [:pre [:code "[version-scalar-spec\n version-scalar-spec]"]]

     [:p "Oops. We found a bug, and need to make a third version. The changelog describing the new version now has three entries, validated with this specification."]

     [:pre [:code "[version-scalar-spec\n version-scalar-spec\n version-scalar-spec]"]]

     [:p "Hmm. We can't know ahead of time how many versions we'll have, and it would be nice if we didn't have to keep manually updating the sequence each time we need to add to the changelog. Speculoos specifications are merely standard Clojure collections. " [:code "clojure.core/repeat"] " provides a convenient way to express " [:em "an infinite number of things"] "."]

     [:pre (print-form-then-eval "(def changelog-scalar-spec (repeat version-scalar-spec))")]

     [:p "Fun! A " [:code "clojure.lang/repeat"] " nested in a " [:code "clojure.lang/repeat"] ". Speculoos can handle that without a sweating. As long as there's not a repeat at the same path in the data. And there isn't. The changelog is hand-written, with each entry unique."]

     [:p "So, I don't see any reason we shouldn't validate a changelog. This is Speculoos' actual " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/changelog_entries/changelog.edn"} "operational changelog"] ". While writing the first draft of this case study, I validated it and corrected the errors (see the case study " [:a {:href "#conclusion"} "conclusion"] "). Therefore, validating the real changelog doesn't have any interesting errors to look at."]

     [:p "For our walk-through, I've cooked up a somewhat " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/case_study/edited_changelog.edn"}  "fictitious changelog"] " to try out our scalar specification. I trimmed the Speculoos library changelog and added a few deliberate invalid scalars. We'll invoke " [:code "validate-scalars"] " with the changelog data in the upper row, and the scalar specification in the lower row."]

     [:div.no-display (def changelog-data (load-file "resources/case_study/edited_changelog.edn"))]

     [:pre (print-form-then-eval "(only-invalid (validate-scalars changelog-data changelog-scalar-spec))" 65 55)]

     [:p [:code "validate-scalars"] " returns a sequence of validation results, and " [:code "only-invalid"] " filters the sequence to keep only the results where the scalar did not satisfy the predicate it was paired with. We can see that there are six invalid scalars, each with its own map that details the problem."]

     [:ul
      [:li "String " [:code "\"okay?\""] " is not a valid " [:em "project status"] " because it is not a member of the set " [:code "#{:active :deprecated :experimental :inactive :stable}"] "."]
      [:li [:code "nil"] " is not a valid month because it is not a member of the enumerated months."]
      [:li [:code ":removed-function"] " (note the lack of a trailing 's') is not a valid " [:em "change type"] " because it is not a member of the enumerated possibilities."]
      [:li [:code "me_at_example.com"] " is not a valid " [:em "email"] " because it does not satisfy the regular expression predicate."]
      [:li [:code "32"] " is an invalid " [:em "day"] " because it is greater than " [:code "31"] " and therefore fails to satisfy " [:code "day-predicate"] "."]
      [:li [:code ":smash-data"] " is not a valid " [:em "removed function"] " datum because the specification requires it to be a symbol."]]

     [:p "While this demonstration used slightly fictitious data, it is representative of the actual problems I discovered when I validated the real changelog."]
     ] ;; end of scalar section


    [:section#collections
     [:h3 "Specifying & validating collections"]

     [:p [:a {:href "https://github.com/blosavio/speculoos#mottos"} "Motto #1"] " for using Speculoos is to separate scalar validation from collection validation. Scalar validation concerns the properties of individual datums, such as " [:em "Is the day thirty-one or less?"] " or " [:em "Is the email a string with an @ symbol?"]]

     [:p "Collection validation concerns itself with properties of the collections themselves, such as " [:em "Does this map contain the required keys?"] ", as well as " [:em "relationships"] " between scalars, such as " [:em "Is the second integer one greater than the first integer?"]]

     [:p "Collection validation is powerful, but writing collection specifications can by a tad tricky. So judgment is called for. There's no need to validate everything in the universe. Let's just validate two properties of interest."]

     [:ol
      [:li "Make sure the changelog contains our required keys."]
      [:li "Verify the relationship between version numbers."]]

     [:h4 "Ensuring required keys"]

     [:p "Earlier when we were validating the scalars, we were concerned with whether the date was an integer or whether the email was a string. But scalar validation does not concern itself with the " [:em "existence"] " of a particular datum. If a datum exists and it can be paired with a predicate, the datum is validated. If there's no datum to pair with a predicate, the predicate is ignored. When we want to ensure the existence of a datum, we use a collection predicate."]

     [:p "It seems reasonable that a changelog entry for a version must have a version number, a date, a person responsible, a comment, the project's status, the urgency of switching to that version, whether that version is breaking with respect to the previous version, and a listing of the actual changes. Let's gather those required keys into a set."]

     [:pre (print-form-then-eval "(def version-required-keys #{:date
                                                              :responsible
                                                              :version
                                                              :comment
                                                              :project-status
                                                              :urgency
                                                              :breaking?
                                                              :changes})")]

     [:p "The scalar specification was concerned with the properties of those concepts, " [:em "if they exist in the data"] ". This collection predicate tests whether or not they are present."]

     [:p "Furthermore, we'd like to require that each of those change listings contains a description, a date, a change type, and whether it is a breaking change. Here are those required keys."]

     [:pre (print-form-then-eval "(def changes-required-keys #{:description
                                                                :date
                                                                :change-type
                                                                :breaking?})")]

     [:p "Collection validation doesn't regard a set as a predicate they way scalar validation does, so we need to write a predicate function that will accept a collection and a list of required keys and returns a boolean reporting whether that collection contains those keys. However, we have two situations where we want to do mostly the same things: keys required in a " [:em "version"] " map, and keys required in a " [:em "change"] " map. We don't want to repeat code. So we write a higher order function that returns a predicate."]

     [:pre (print-form-then-eval "(defn contains-required-keys?
                                     \"Returns a predicate that tests whether a map passed as the first argument contains all keys enumerated in set `req-keys`.\"
                                     [req-keys]
                                     #(empty? (clojure.set/difference req-keys (set (keys %)))))")]

     [:p "Let's give that a spin."]

     [:pre
      (print-form-then-eval "((contains-required-keys? #{:a :b :c}) {:a 1 :b 2 :c 3})")
      [:br]
      [:br]
      (print-form-then-eval "((contains-required-keys? #{:a :b :c}) {:a 1 :b 2 :c 3 :d 4})")
      [:br]
      [:br]
      (print-form-then-eval "((contains-required-keys? #{:a :b :c}) {:a 1})")]

     [:p "The first two examples evaluate to " [:code "true"] " because the maps do indeed contain all three required keys. The second example contains an extra " [:code ":d"] " key, but the predicate doesn't mind. The third example returns " [:code "false"] " because the map is missing keys " [:code ":b"] " and " [:code ":c"] "."]

     [:p "The following creates a predicate that tests whether a version map contains the required keys."]

     [:pre [:code "(contains-required-keys? version-required-keys)"]]

     [:p "And this predicate tests whether a change map contains the required keys."]

     [:pre [:code "(contains-required-keys? changes-required-keys)"]]

     [:p "One of the principles of composing a collection specification is " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#where-collection-predicates-apply"} [:em "Predicates apply to their immediate parent collection"]] ". The practical consequence of that is we insert the predicate into a collection of the same kind that we want to validate. We define a collection specification for a version map like this."]

     [:pre (print-form-then-eval "(def version-coll-spec {:req-ver-keys? (contains-required-keys? version-required-keys)
                                                     :changes (vec (repeat 99 {:req-chng-keys? (contains-required-keys? changes-required-keys)}))})")]

     [:p "There is one required-keys predicate aimed at the top-level version map. There is a second required-keys predicate aimed at the changes sequence. (Because of the current implementation, it is not possible to use an infinite " [:code "repeat"] " to validate zero or more collections. I therefore had to make a defined number of them — 99 because that seems plenty for this situation — and convert it to a vector. I very much want to revisit this implementation to see if this restriction can be removed, for generality, and so that writing the specification is more elegant.)"]

     [:p "We can run a quick test on version 1 of the trimmed changelog version."]

     [:pre
      [:code "(only-invalid (validate-collections (get-in* changelog-data [1])
                                             version-coll-spec))"]
      [:br]
      [:code
       ";; => ({:datum {:date {:year 2024,
;;                     :month \"July\",
;;                     :day 26},
;;              :breaking? true,
;;              :project-status \"okay!\",
;;              :stable false,
;;              :responsible {:name \"Brad Losavio\",
;;                            :email \"me@example.com\"},
;;              :comment \"Request for comments.\",
;;              :changes [«listing elided»],
;;              :version 1},
;;      :valid? false,
;;      :path-predicate [:req-ver-keys?],
;;      :predicate #function[case-study/contains-required-keys?/fn--30138],
;;      :ordinal-path-datum [],
;;      :path-datum []})"]]

     [:p "We can see that one predicate was not satisfied: the anonymous predicate produced by the " [:code "contains-required-keys"] " higher-order function. It tells us that this map doesn't contain at least one required key, in this case, " [:code ":urgency"] "."]

     [:p "In that example, we used " [:code "get-in*"] " to extract a single changelog entry describing a single version. But ultimately, we want to validate zero or more version entries as the project develops over time, so we use our " [:code "repeat"] " trick."]

     [:pre (print-form-then-eval "(def changelog-coll-spec (vec (repeat 99 version-coll-spec)))")]

     [:p "Now, we can validate an ever-growing changelog with that one collection specification."]

     [:p "That takes care of testing for the presence of all the required keys."]

     [:h4 "Validating proper version incrementing"]

     [:p "Someone might reasonably point out that manually declaring the version number inside a sequential collection is redundant and error-prone. It is. But, I may change my mind in the future and switch to dotted version numbers, or version letters, or some other format. Plus, the changelog is intended to be machine- and human-readable (with priority on the latter), and for organizing purposes, the subsections are split between different files. So it's more ergonomic to include an explicit version number. In that case, we can validate the version number sequence as a kind of 'spell-check' to alert me when I've made an error writing a changelog entry."]

     [:p "Here's a predicate that will extract the version number from each changelog entry and compare it to the previous."]

     [:pre (print-form-then-eval "(defn properly-incrementing-versions?
                                     \"Returns `true` if each successive version is exactly one more than previous.\"
                                     [c-log]
                                     (every? #{1} (map #(- (:version %2) (:version %1)) c-log (next c-log))))")]

     [:p "Let's give it a spin. Collection predicates apply to their immediate parent collection, so we insert the predicate into the root of the specification."]

     [:pre
      [:code "(validate-collections changelog-data
                      [properly-incrementing-versions?])"]
      [:br]
      [:code
       ";; => ({:datum [«data elided»],
;;      :valid? false,
;;      :path-predicate [0],
;;      :predicate properly-incrementing-versions?,
;;      :ordinal-path-datum [],
;;      :path-datum []})"]]

     [:p "Our " [:em "ad hoc"]" specification contained only a single predicate, " [:code "properly-incrementing-versions?"] ", and it was not satisfied with the datum it was paired with. Unfortunately, we only have the identity of the unsatisfied predicate, and the value of the datum, which is the entire changelog in this case. So we don't have any details on " [:em "where"] " exactly the version numbers are wrong. We need to use our Clojure powers for more insight. Fortunately, it's a one-liner to pull out the version datums."]

     [:pre (print-form-then-eval "(map #(:version %) changelog-data)")]

     [:p "Oops. " [:code "99"] " does not properly follow " [:code "1"] ". Gotta go edit the third changelog entry."]

     [:p "Notice that, while on a basic level, we are inspecting scalars, we couldn't use scalar validation for this task. We are validating the " [:em "relationships"] " between multiple scalars. Handling multiple scalars necessarily requires a collection validation."]

     [:h4 "Assembling the collection specification"]

     [:p "We've now created and demonstrated collection specifications for both the required keys and for properly-incrementing version numbers. Let's put them together into a single specification. Speculoos specifications are standard Clojure collections, so we can use regular composition. The changelog collection specification is a vector — mimicking the shape of the changelog data — containing the " [:code "properly-incrementing-versions?"] " predicate followed by an infinite number of version collection specifications."]

     [:pre (print-form-then-eval "(def changelog-coll-spec (concat [properly-incrementing-versions?] (vec (repeat 99 version-coll-spec)))))")]

     [:p "As a sanity check, let's re-run the validation with the composed collection specification."]

     [:pre
      [:code "(only-invalid (validate-collections changelog-data
                                    changelog-coll-spec))"]
      [:br]
      [:code
       ";; => ({:datum [«data elided»],
;;      :valid? false,
;;      :path-predicate [0],
;;      :predicate properly-incrementing-versions?,
;;      :ordinal-path-datum [],
;;      :path-datum []}
;;     {:datum {«data elided»},
;;      :valid? false,
;;      :path-predicate [2 :req-ver-keys?],
;;      :predicate #function[case-study/contains-required-keys?/fn--32159],
;;      :ordinal-path-datum [1],
;;      :path-datum [1]})"]]

     [:p "Exactly the same two invalid results we saw before. There is a problem with the version number intervals. And one of the changelog version entries is missing a required key. The only difference is that we used one comprehensive collection specification, " [:code "changelog-collection-spec"] "."]

     ] ;; end of collection section

    [:section#combo
     [:h3 "Combo validation"]

     [:p "If we find it convenient, we could do a combo so that both scalars and collections are validated with a single function invocation."]

     [:pre [:code "(only-invalid (validate changelog-data\n                        changelog-scalar-spec\n                        changelog-coll-spec))"]]

     [:p "I won't evaluate the expression because we've already seen the results."]

     [:p "I should also mention that " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#combo-validation"} "'combo' validation"] " with " [:code "validate"] " and friends does not violate " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "Motto #1"] ". It performs a scalar validation, then a wholly distinct collection validation, then merges the results. The two tasks are, as always, distinct. " [:code "validate"] " merely provides us with a convenient way to perform both with one function evaluation."]

     ] ;; end of combo validaton section

    [:section#conclusion
     [:h3 "Observations & conclusion"]

     [:p "Specifying and validating Speculoos' changelog was a valuable exercise. I certainly could have written a dozen or so bespoke validation functions, but I probably wrote the predicates and specifications faster. (A proper scientific test would have been to do both while measuring the time for each, but I didn't.) I contend that the " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/case_study/changelog_specifications.clj"}"predicates and specifications"] " are more understandable and maintainable than a bag of loose, one-off validation functions."]

     [:p "While writing this case study, I realized something I hadn't noticed while writing simplified examples for documentation. Real-world predicate functions ought to be formally unit-tested. After an edge-case bug and fumbling a set operation, I created a dedicated namespace and wrote up a bunch of " [:code "clojure.test.check"] " tests. Specifications, and by extension, validation, are only as good as the predicates. If the predicate functions are crummy, the validation results will be, too. Unit-testing predicates and carefully composing specifications, while systematically testing against exemplar data, does take a little time and effort. But like unit-testing, the time and effort is worth the investment."]

     [:p "Numbers-wise, validating the changelog revealed eleven errors spread across multiple files, including two errant " [:code "nil"] "s that ought to have been replaced, and numerous mis-spelled keywords. So the case study was valuable in correcting real-world data. And from now on, I can validate each changelog entry with the exact same specifications we've already written here. That seems like a very useful instance for validating data: checking the changelog's correctness the moment I'm typing it in, instead of finding out sometime later that I can't generate the html because I mis-spelled a keyword."]

     [:p "Also, those errors found by the validation suggested procedural changes that will improve how I handle the changelog experiment. Seeing so many " [:span.intentional-misspelling "keybroading"] " mistakes was eye-opening, and my immediate response was to create a template based upon the specification, so that each new entry will have spellings that conform to specification. Long-term, I may write a command-line tool that generates a correct version entry and appends it to the changelog. So even within this minimal case study, validation could improve the way a project is managed. If we analyze the errors in our data, it might suggest improvements elsewhere."]

     [:p "Doing the case study was also useful to me to see what it's like to use Speculoos beyond intentionally simplified examples. The performance is not great, but the Speculoos library is squarely in the " [:em "experimental"] " stage. And for this style of interactive development (i.e., at the " [:span.small-caps "repl"] ", not in the middle of a high-throughput pipeline), the performance tolerable. Also, the validation report can get unwieldy when the datum is a deeply-nested collection. I manually glossed over that issue with " [:code "«data elided»"] " for this case study. When processing it with machines, it doesn't matter much. But for the sakes of our eyes, it's an issue that I'm going to think about."]

     [:p "Some people advocate writing unit tests first, before writing the actual functions. While unit testing is indispensable to me, I'm not in that camp. But I did come to a similar realization: Writing specifications for some data (or a function's arguments/returns) before you have the data is a legit tactic. It forces clarified thinking about how the data ought to arranged, and documents it in human- and machine-readable form. If we write this specification for a date…"]

     [:pre [:code "{:year int?\n :month string?\n :day int?}"]]

     [:p "…without having any concrete substantiation of data, you and I can already discuss the merits of those choices. What restrictions should we put on values of " [:em "year"] "? Should " [:em "month"] " values be strings, or keywords? Should the key for " [:em "day of the month"] " be " [:code ":day"] " or " [:code ":date"] "? That little map is not pseudo-code. We could send it, un-modified, to " [:code "validate-scalars"] " and get a feel for how it would work with real data."]

     [:p [:a {:href "https://github.com/blosavio"} "Let me know"] " what you think."]

     ]
    ] ;; end of [:article]
   ] ;; end of [:body]
  )


#_(def page-body
    [:body
     [:article
      [:h1 "Case study: Specifying and validating a library changelog"]
      [:p "Foo bar baz."]
      [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]]])


(def case-study-UUID #uuid "e3856cb2-b1d6-40cb-8659-8f2e7e56fcca")


(do
  (spit "doc/case_study.html"
        (page-template
         "Case study: Specifying and validating a library changelog"
         case-study-UUID
         page-body
         "Brad Losavio"))

  (tidy-html-document "doc/case_study.html"))


(defn -main
  [& args]
  {:UUIDv4 #uuid "6de5cc17-c3ea-4609-bc03-bb23880b378e"}
  (println "generated Speculoos case study\nWarning! This does not properly re-insert function objects!"))