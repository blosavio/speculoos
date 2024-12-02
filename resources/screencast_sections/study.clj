(require '[screedcast.core :refer [panel
                                   prettyfy-form-prettyfy-eval
                                   screencast-title
                                   whats-next-panel]])


(def study-index 16)


[:body
 (panel
  (screencast-title study-index "Case study")
  [:h3 [:em "Specifying and validating Speculoos' changelog"]]

  [:div.note
   [:p "What it's like to use " [:a {:href "https://github.com/blosavio/speculoos"} "Speculoos"] " on a task that's not merely a demonstration? Specify and validate the Speculoos library " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/changelog_entries/changelog.edn"} [:code "changelog.edn"]] ". To begin, a few words about the changelog itself."]])


 (panel
  [:h3 "An " [:em "experimental"] " changelog"]

  [:ul
   [:li "Human- and machine-readable"]
   [:li "Query-able"]
   [:li "Generate formatted representations"]]

  [:div.note
   [:p "Speculoos is an experimental library. Among the ideas I wanted to explore is a changelog published in Clojure " [:strong "e"] "xtensible " [:strong "d"] "ata " [:strong "n"] "otation (" [:a {:href "https://github.com/edn-format/edn"} "edn"] "). The goal is to have a single, canonical, human- and machine-readable document that describes the project's changes from one version to the next. That way, it would be straightforward to automatically generate a nicely-formatted changelog webpage and query the changelog data so that people can make informed decisions about changing versions."]])


 (panel
  [:h3 "Concept: " [:em "Version"]]

  [:ul
   [:li "Version number"]
   [:li "Date"]
   [:li "Person responsible, with contact info"]
   [:li "Status of the project (i.e., stable, deprecated, etc.)"]
   [:li "Urgency (i.e., high for a security fix, etc.)"]
   [:li "Flag if changes are breaking from previous version"]
   [:li "Free-form comments"]]

  [:div.note
   [:p "Here's the info that I think would be useful for a changelog entry."]])


 (panel
  [:h3 "Example " [:em "version"] " entry"]


  [:pre [:code "{:version 99\n :date {:year 2025\n        :month \"November\"\n        :day 12}\n :responsible {:name \"Kermit Frog\"\n               :email \"its.not.easy@being.gre.en\"}\n :project-status :stable\n :urgency :low\n :breaking? false\n :comment \"Improved arithmetic capabilities.\"\n :changes [«see upcoming discussion»]}"]]

  [:p "Each version may have multiple " [:em "changes"] "."]

  [:div.note
   [:p "We can quickly assemble an example."]])


 (panel
  [:h3 "Concept: " [:em "Change"]]

  [:ul
   [:li "A free-form, textual description of the change."]
   [:li "A reference, (e.g., GitHub issue number, JIRA ticket number, etc.)"]
   [:li "Kind of change (i.e., bug fix, renamed function, removed function, improved performance, etc.)"]
   [:li "Flag indicating if this particular change is breaking."]
   [:li "Added, renamed, moved, altered, or deleted functions."]]

  [:div.note
   [:p "Furthermore, for each of those changelog entries, I think it would be nice to tell people more details about the individual changes so they can make technically supported decisions about changing versions. A single, published version could consist of multiple changes, associated to a key " [:code ":changes"] ", with each change detailed with this info."]])


 (panel
  [:h3 "Example " [:em "change"] " entry"]

  [:pre [:code "{:description \"Addition function `+` now handles floating point decimal number types.\"\n :reference {:source \"Issue #78\"\n             :url \"https://example.com/issue/87\"}\n :change-type :relaxed-input-requirements\n :breaking? false\n :altered-functions ['+]\n :date {:year 2025\n        :month \"November\"\n        :day 8}\n :responsible {:name \"Fozzie Bear\"\n               :email \"fozzie@wocka-industries.com\"}}"]]

  [:div.note
   [:p "Here's an example of one change included in a published version."]

   [:p "The date and person responsible for an individual change need not be the same as the date and person responsible for the version that contains it. So while Kermit was responsible for publishing the overall version on 2025 November 12, Fozzie was responsible for creating the individual change to the plus function on 2025 November 08."]])


 (let [motto-notes
       [:div
        [:p "Efficiently using Speculoos requires remembering three mottos."]

        [:ol
         [:li "Validate scalars separately from validating collections."]
         [:li "Shape the specification to mimic the data."]
         [:li "Ignore un-paired predicates and un-paired datums."]]

        [:p " Motto #2 reminds us to shape the specification so that it mimics the data. This motto reveals a convenient tactic: copy-paste the data, delete the scalars, and insert predicates."]

        [:p "Motto #3 reminds us to ignore un-paired predicates and un-paired datums. In practice, the consequence of this principle is that we may provide more data than we specify, and the un-specified data merely flows through, un-validated. On the other hand, we may specify more elements than actually exist in a particular piece of data. That's okay, too. Those un-paired predicates will be ignored."]]]

   (-> (load-file "resources/screencast_sections/mottos.clj")
       (update 4 #(conj % motto-notes))))


 (panel
  [:h3 "Strategy: Build from small pieces"]

  [:ol
   [:li "Specify & validate scalars"]
   [:li "Specify & validate collections"]
   [:li "'Combo' validate"]]

  [:div.note
   [:p "Our overall strategy is this: Build up specifications from small pieces, testing those small pieces along the way. Then, after we we're confident in the small pieces, we can assemble them at the end. We'll start with specifying and validating the scalars. Once we've done that, we'll put them aside. Then, we'll specify and validate the collections, testing them until we're confident we've got the correct specifications. At the end, we'll bring together both scalar validation and collection validation into a combo validation."]

   [:p "Scalars and collections are separate concepts, so we handle them in different steps. At the end, merely for convenience, we can use a combo validation that separately validates the scalars and the collections with a single invocation."]])


 (panel
  [:h3 "Develpoment environment setup"]

  [:pre [:code "(require '[speculoos.core :refer [valid-scalars? valid-collections? valid?]]\n         '[fn-in.core :refer [get-in*]])"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(set! *print-length* 99)")

  [:p "Code and fictitious data at " [:a {:href ""} "https://github.com/blosavio/speculoos"]]

  [:div.note
   [:p "Let's set up our environment with the tools we'll need."]])


 (panel
  [:h3 "Specifying scalars: Year"]

  (prettyfy-form-prettyfy-eval "(defn year-predicate [n] (and (int? n) (<= 2000 n)))")

  [:div.vspace]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(year-predicate 2025)")]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(year-predicate \"2077\")")]]

  [:div.note
   [:p "We'll start simple. Let's compose a " [:em "date"] " specification. Informally, a date is a year, a month, and a day. Let's stipulate that a valid year is " [:em "An integer greater-than-or-equal-to two-thousand"] ". Here's a predicate for that concept."]

   [:p "Speculoos predicates are merely Clojure functions. Let's try it."]

   [:p "That looks good. Integer " [:code "2025"] " is greater than two-thousand, while string " [:code "\"2077\""] " is not an integer."]])


 (panel
  [:h3 "Specifying scalars: Day"]

  (prettyfy-form-prettyfy-eval "(defn day-predicate [n] (and (int? n) (<= 1 n 31)))")

  [:div.note
   [:p "Checking day of the month is similar."]
   [:p [:code "day-predicate"] " is satisfied only by an integer between one and thirty-one, inclusive."]])


 (panel
  [:h3 "Specifying scalars: Month"]

  [:div.no-display (def month-predicate #{"January"
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
                                          "December"})]

  [:div.side-by-side-container
   [:div.side-by-side
    [:pre [:code
           "(def month-predicate #{\"January\"
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
                       \"December\"})"]]]
   [:div.side-by-side
    (prettyfy-form-prettyfy-eval "(month-predicate \"August\")")

    [:div.vspace]

    (prettyfy-form-prettyfy-eval "(month-predicate :November)")]]

  [:div.note
   [:p "Speculoos can validate a scalar by testing if it's a member of a set. A valid month may only be one of twelve elements. Let's enumerate the months of the year, months represented as strings."]

   [:p "Let's see how that works."]

   [:p [:code "month-predicate"] " is satisfied (i.e., returns a truthy value) because string " [:code "\"August\""] " is a member of the set."]

   [:p "Keyword " [:code ":November"] " does not satisfy " [:code "month-predicate"] " because it is not a member of the set. " [:code "month-predicate"] " returns a falsey value, " [:code "nil"] "."]])


 (panel
  [:h3 "Specifying scalars: Composing a " [:em "date"] " specification"]

  [:pre [:code "{:year 2020 :month \"January\" :day 1}"]]

  [:div.vspace]

  [:pre [:code "{:year ____ :month _________ :day __}"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def date-spec {:year year-predicate :month month-predicate :day day-predicate})")

  [:div.note
   [:p "We've now got predicates to check a year, a month, and day. The notion of " [:em "date"] " includes a year, month, and a day traveling around together. We can collect them into one group using a Clojure collection. A hash-map works well in this scenario."]

   [:p "Speculoos specifications are plain old regular Clojure data collections. " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#-three-mottos"} "Motto #2"] " reminds us to shape the specification to mimic the data. To create a scalar specification, we could copy-paste the data, and delete the scalars…and insert our predicates."]])


 (panel
  [:h3 "Check " [:em "date"] " scalar specification: Correct data"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:year 2024 :month \"January\" :day 1} {:year year-predicate :month month-predicate :day day-predicate})" 85 75)

  [:div.note
   [:p "Let's check our progress against some valid data. We're validating scalars (Motto #1), so we'll use a function with a " [:code "-scalars"] " suffix. The data is the first argument on the upper row, the specification is the second argument on the lower row."]

   [:p "Each of the three scalars satisfies their respective predicates (Motto #3), so " [:code "valid-scalars?"] " returns " [:code "true"] "."]])


 (panel
  [:h3 "Check" [:em "date"] " scalar specification: Incorrect data"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:year 2024 :month \"Wednesday\" :day 1} {:year year-predicate :month month-predicate :day day-predicate})" 85 85)    

  [:div.note
   [:p "Now let's feed in some invalid data."]

   [:p "While " [:code "\"Wednesday\""] " is indeed a string, it is not a member of the " [:code "month-predicate"] " set, so " [:code "valid-scalars?"] " returns " [:code "false"] "."]])


 (panel
  [:h3 "Specifying scalars: A " [:em "person"]]

  [:ul
   [:li "name"]
   [:li "email"]]

  [:div.note
   [:p "Now that we can validate the date component of the changelog, we'll need to specify and validate the information about the person responsible for that publication. The changelog information about a person gathers their name, a free-form string, and an email address, also a string. In addition to being a string, a valid email address:"]

   [:ul
    [:li "Starts with one or more alphanumeric characters or periods,"]
    [:li "Followed by exactly one " [:code "@"] " character,"]
    [:li "Followed by one or more alphanumeric characters or periods."]]])


 (panel
  [:h3 "Specifying scalars: A person's email"]

  (prettyfy-form-prettyfy-eval "(def person-spec {:name string? :email #\"^[\\w\\.]+@[\\w\\.]+\"})")

  [:div.note
   [:p "Regular expressions are powerful tools for testing those kind of string properties, and Speculoos scalar validation supports them. A regular expression appearing in a scalar specification is considered a predicate. Let's make the following a specification about a changelog person."]])


 (panel
  [:h3 "Testing " [:em "person"] " scalar specification #1"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:name \"Abraham Lincoln\" :email \"four.score.seven.years@gettysburg.org\"} {:name string? :email #\"^[\\w\\.]+@[\\w\\.]+\"})" 95 95)

  [:div.note
   [:p "Let's give that specification a whirl. First, we validate some valid person data (data in upper row, specification in lower row)."]

   [:p "Both name and email scalars satisfied their paired predicates. "]])


 (panel
  [:h3 "Testing " [:em "person"] "scalar specification #2"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:name \"George Washington\" :email \"crossing_at_potomac\"} {:name string? :email #\"^[\\w\\.]+@[\\w\\.]+\"})")

  [:div.note
   [:p "Now, let's see what happens when we validate some data that is invalid."]

   [:p "Oops. That email address does not satisfy the regular expression because it does not contain an " [:code "@"] " character, so the person data is invalid."]])


 (panel
  [:h3 "Specifying scalars: version number"]

  (prettyfy-form-prettyfy-eval "(defn version-predicate [i] (and (int? i) (<= 0 i)))")

  [:div.vspace]

  [:div.side-by-side-container
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(version-predicate 99)")]
   [:div.side-by-side (prettyfy-form-prettyfy-eval "(version-predicate -1)")]]

  [:div.note
   [:p "Perhaps the most pivotal single datum in a changelog entry is the version number. For our discussion, let's stipulate that a version is an integer greater-than-or-equal-to zero. Here's a predicate for that."]

   [:p "And a pair of quick demos."]])


 (panel
  [:h3 "Specifying scalars: Assemble existing parts"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:pre [:code "{:version version-predicate\n :date date-spec\n :person person-spec}"]]]

   [:div.side-by-side
    [:pre [:code
           "{:version 99\n :date {:year 2025\n        :month \"August\"\n        :day 1}\n :person {:name \"Abraham Lincoln\"\n          :email \"four.score.seven.years@gettysburg.org\"}}"]]]]

  [:div.note
   [:p "At this point, let's assemble what we have. Speculoos specifications are merely Clojure collections that mimic the shape of the data. So let's collect those predicates into a map."]

   [:p "Notice, " [:code "date-spec"] " and " [:code "person-spec"] " are each themselves specifications. We compose a Speculoos specification using standard Clojure composition."]

   [:p "The partial changelog entry might look something like this."]])


 (panel
  [:h3 "Quick check: scalar validation #1"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:version 99 :date {:year 2025 :month \"August\" :day 1} :person {:name \"Abraham Lincoln\" :email \"four.score.seven.years@gettysburg.org\"}} {:version version-predicate :date date-spec :person person-spec})")

  [:div.note
   [:p "Let's check our work so far. First, we'll validate some data we know is valid."]])


 (panel
  [:h3 "Quick check: scalar validation #2"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:version 1234 :date {:year 2055 :month \"Octoberfest\" :day 1} :person {:name \"Paul Bunyan\" :email \"babe@blue.ox\"}} {:version version-predicate :date date-spec :person person-spec})")

  [:div.note
   [:p "Second, we'll feed in some data we suspect is invalid."]
   [:p "Hmm. " [:em "Something"] " doesn't satisfy their predicate, but my eyesight isn't great and I can't immediately spot the problem. Let's use a more verbose function, " [:code "validate-scalars"] ", which returns detailed results."]])


 (panel
  [:h3 "Quick check: scalar validation #3, verbose"]

  (prettyfy-form-prettyfy-eval "(validate-scalars {:version 1234 :date {:year 2055 :month \"Octoberfest\" :day 1} :person {:name \"Paul Bunyan\" :email \"babe@blue.ox\"}} {:version version-predicate :date date-spec :person person-spec})")

  [:div.note
   [:p "Ugh, too verbose. Let's pull in a utility that filters the validation results so only the invalid results are displayed."]])


 (panel
  [:h3 "Quick check: scalar validation #4, just right verbosity"]

  (prettyfy-form-prettyfy-eval "(require '[speculoos.core :refer [only-invalid]])")

  (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars {:version 1234 :date {:year 2055 :month \"Octoberfest\" :day 1} :person {:name \"Paul Bunyan\" :email \"babe@blue.ox\"}} {:version version-predicate :date date-spec :person person-spec}))")

  [:div.note
   [:p "Now we can focus."]

   [:p "Aha. One scalar datum failed to satisfy the predicate it was paired with. " [:code "\"Octoberfest\""] " is not a month enumerated by our month predicate."]])


 (panel
  [:h3 "Specifying scalars: breakage"]

  (prettyfy-form-prettyfy-eval "(defn breaking-predicate [b] (or (nil? b) (boolean? b)))")

  [:div.note
   [:p "So far, our changelog entry has a version number, a date, and a person. In the introduction, we outlined that a changelog entry would contain more info than that. So let's expand it."]

   [:p "It would be nice to tell people whether that release was breaking relative to the previous version. The initial release doesn't have a previous version, so it's breakage will be " [:code "nil"] ". For all subsequent versions, breakage will carry a " [:code "true"] " or " [:code "false"] " notion, so we'll require that datum be a boolean or " [:code "nil"] "."]])


 (panel
  [:h3 "Specifying scalars: project status"]

  (prettyfy-form-prettyfy-eval "(def status-predicate #{:experimental :active :stable :inactive :deprecated})")

  [:div.note
   [:p "Also, it would be nice if we indicate the status of the project upon that release. A " [:a {:href "https://github.com/metosin/open-source/blob/main/project-status.md"} "reasonable enumeration of a project's status"] " might be " [:em "experimental"] ", "  [:em "active"] ", "  [:em "stable"] ", "  [:em "inactive"] ", or "  [:em "deprecated"] ". Since a valid status may only be one of a handful of values, a set makes a good membership predicate."]])


 (panel
  [:h3 "Quick check: scalar validation #5"]

  [:pre [:code "{:version version-predicate\n :breaking? breaking-predicate\n :project-status status-predicate}"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:version 99 :breaking? false :project-status :stable} {:version version-predicate :breaking? breaking-predicate :project-status status-predicate})" 115 25)

  [:div.note
   [:p "Let's assemble the version predicate, the breaking predicate, and the status predicate into another partial, temporary specification."]

   [:p "Now that we have another temporary, partial specification, let's use it to validate (data in the upper row, specification in the lower row)."]])


 (panel
  [:h3 "Quick check: scalar validation #6"]

  (prettyfy-form-prettyfy-eval "(valid-scalars? {:version 123 :breaking? true :project-status \"finished!\"} {:version version-predicate :breaking? breaking-predicate :project-status status-predicate})" 115 25)

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars {:version 123 :breaking? true :project-status :finished!} {:version version-predicate :breaking? breaking-predicate :project-status status-predicate}))" 115 75)

  [:div.note
   [:p "Now, let's validate some invalid data."]

   [:p "Perhaps we're curious about exactly which datum failed to satisfy its predicate. So we switch to " [:code "validate-scalars"] " and filter with " [:code "only-invalid"] "."]

   [:p "Yup. Scalar " [:code ":finished!"] " is not enumerated by " [:code "status-predicate"] "."]])


 (panel
  [:h3 "Scalar specification: upgrade urgency & comments"]

  [:pre [:code "{:version version-predicate\n :date date-spec\n :responsible person-spec\n :project-status status-predicate\n :breaking? breaking-predicate\n :urgency #{:low :medium :high}\n :comment string?}"]]

  [:div.note
   [:p "A comment concerning a version is a free-form string, so we can use a bare " [:code "string?"] " predicate. Upgrade urgency could be represented by three discrete levels, so a set " [:code "#{:low :medium :high}"] " makes a fine predicate."]

   [:p "Now that we've got all the individual components for validating the version number, date (with year, month, day), person responsible (with name and email), project status, breakage, urgency, and a comment, we can assemble the specification for one changelog entry."]])


 (panel
  [:h3 "Example " [:em "version"] " data"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:pre [:code "{:version ___\n :date {:year ___\n        :month ___\n        :day ___}\n :responsible {:name ___\n               :email___}\n :project-status ___\n :breaking? ___\n :urgency ___\n :comment ___}"]]]

   [:div.side-by-side
    [:pre [:code "{:version 55\n :date {:year 2025\n        :month \"December\"\n        :day 31}\n :responsible {:name \"Rowlf\"\n               :email \"piano@example.org\"}\n :project-status :active\n :breaking? false\n :urgency :medium\n :comment \"Performance improvements and bug fixes.\"}"]]]]

  [:div.note
   [:p "Let's use that specification to validate some data. Here's a peek behind the curtain: At this very moment, I don't have sample data to show you. I need to write some. I'm going to take advantage of the fact that a Speculoos specification is a regular Clojure data structure whose shape mimics the data. I already have the specification in hand. I'm going to copy-paste the specification, delete the predicates, and then insert some scalars."]

   [:p "Here's the specification with the predicates deleted."]

   [:p "That will serve as a template. Then I'll insert some scalars."]])


 (panel
  [:h3 "Validating scalars of valid " [:em "version"] " data"]

  [:pre (prettyfy-form-prettyfy-eval "(valid-scalars?
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

  [:div.note
   [:p "Let's run a validation with that data and specification."]

   [:p "Since I wrote the data based on the specification, it's a good thing the data is valid."]])


 (panel
  [:h3 "Validating scalars of " [:strong "invalid"] " version data"]

  [:pre (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars
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

  [:div.note
   [:p "Let me change the version to a string, validate with the verbose " [:code "validate-scalars"] " and filter the output with " [:code "only-invalid"] " to keep only the invalid scalar+predicate pairs."]

   [:p "Yup. String " [:code "\"foo-bar-baz\""] " is not a valid version number according to " [:code "version-predicate"] ". If I had made a typo while writing that changelog entry, before it got any further, validation would have informed me that I needed to correct that version number."]

   [:p "In the introduction, we mentioned that each version entry could contain a sequence of maps detailing the specific changes. That sequence is associated to " [:code ":changes"] ". Maybe you noticed I snuck that into the data in the last example. We haven't yet written any predicates for that key-val, so " [:code "validate-scalars"] " ignored it (Motto #3). We won't ignore it any longer."]])


 (panel
  [:h3 "Reviewing the " [:em "changes"] " concept"]

  [:pre [:code "{:description \"Addition function `+` now handles floating point decimal number types.\"\n :reference {:source \"Issue #78\"\n             :url \"https://example.com/issue/87\"}\n :change-type :relaxed-input-requirements\n :breaking? false\n :altered-functions ['+]\n :date {:year 2025\n        :month \"November\"\n        :day 8}\n :responsible {:name \"Fozzie Bear\"\n               :email \"fozzie@wocka-industries.com\"}}"]]

  [:div.note
   [:p "The nesting depth is going to get messy, so let's put aside the version entry and zoom in on what a change entry might look like. Way back at the beginning, of this case study, we introduced this example."]

   [:p "This 'change' entry provides details about who changed what, when, and a reference to an issue-tracker. A single version may bundle multiple of these change entries."]])


 (panel
  [:h3 "Specifying scalars of a " [:em "change"]]

  [:pre [:code "{:description ___\n :reference {:source ___\n             :url ___}\n :change-type ___\n :breaking? ___\n :altered-functions []\n :date {:year ___\n        :month ___\n        :day ___}\n :responsible {:name ___\n               :email ___}}"]]

  [:div.note
   [:p "I'll copy-paste the sample and delete the scalars."]

   [:p "That'll be a good template for a change entry specification."]])


 (panel
  [:h3 "Specifying scalars of a " [:em "change"] ": re-using predicates"]

  [:pre [:code "{:description string?\n :reference {:source ___\n             :url ___}\n :change-type ___\n :breaking? breaking-predicate\n :altered-functions []\n :date date-spec\n :responsible person-spec}"]]

  [:div.note
   [:p "We can start filling in the blanks because we already have specifications for " [:em "date"] ", " [:em "person"] ", and " [:em "breaking"] ". Similarly, a description is merely free-form text which can be validated with a simple " [:code "string?"] " predicate."]])


 (panel
  [:h3 "Specifing scalars of a change's " [:em "reference"]]

  [:table
   [:tr
    [:td
     "source"
     [:div.vspace]]
    [:td
     [:code "string?"]
     [:div.vspace]]]

   [:tr
    [:td "url"]
    [:td
     (prettyfy-form-prettyfy-eval "(re-find #\"^https:\\/{2}[\\w\\/\\.]*\" \"https://example.com\")" 50 50)

     [:div.vspace]

     (prettyfy-form-prettyfy-eval "(re-find #\"^https:\\/{2}[\\w\\/\\.]*\" \"ht://example.com\")" 50 55)

     [:div.vspace]]]
   [:tr
    [:td "ticket"]
    [:td (prettyfy-form-prettyfy-eval "(defn ticket-predicate [t] (or (string? t) (uuid? t)))")]]]

  [:div.note
   [:p "Now we can tackle the remaining blanks. The " [:em "reference"] " associates this change to a issue-tracker. The " [:code ":source"] " is a free-form string (i.e., \"GitHub Issue #27\", etc.), while " [:code ":url"] " points to a web-accessible resource. Let's require that a valid entry be a string that starts with \"https://\". We can demonstrate that regex."]

   [:p "The first example returns a match (truthy), while the second example is a malformed url and fails to find a match (falsey)."]

   [:p "Different issue trackers have different ways of referring to issues, so to accommodate that, we can include an optional " [:code ":ticket"] " entry that can be a free-form string or a " [:span.small-caps "uuid"] "."]])


 (panel
  [:h3 "Assembling the scalar specification for a " [:em "reference"] " sub-component"]

  [:pre (prettyfy-form-prettyfy-eval "(def reference-spec {:source string? :url #\"^https:\\/{2}[\\w\\/\\.]*\" :ticket ticket-predicate})")]

  [:div.note
   [:p "Let's assemble those predicates to define this sub-component."]])


 (panel
  [:h3 "Assembling the scalar specification for a " [:em "change"]]

  [:pre [:code "{:description string?\n :reference reference-spec\n :change-type ___\n :breaking? breaking-predicate\n :altered-functions []\n :date date-spec\n :responsible person-spec}"]]

  [:div.note
   [:p "Slowly and steadily filling in the blanks, our change specification currently looks like this."]

   [:p "Let's take a look at the first remaining blank. "[:em "Change type"] " may be one of an enumerated set of values. That term " [:em "set"] " is a clue to writing the predicate. "]])


 (panel
  [:h3 "Specifying " [:em "change types"]]

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


  [:div.note
   [:p "We ought to use a set as a membership predicate if we can enumerate all possible valid values. I've jotted down the common cases I can think of."]

   [:p "Maybe this a good idea for validating changelog data, maybe it's not. But it's an experiment either way."]])


 (panel)


 (panel
  [:h3 "Specifying scalars: zero-or-more symbols"]

  [:div.side-by-side-container
   [:div.side-by-side
    [:p [:em "change"] " specification, so far…"]
    [:pre [:code "{:description string?\n :reference reference-spec\n :change-type change-kinds\n :breaking? breaking-predicate\n " [:strong ":altered-functions [___]"] "\n :date date-spec\n :responsible person-spec}"]]]

   [:div.side-by-side
    [:p "specifying an unknown quantity of scalars"]
    [:pre
     [:code ";; data           scalar specification"] [:br]
     [:code "['foo          ] [symbol?                ]"] [:br]
     [:code "['foo 'bar     ] [symbol? symbol?        ]"] [:br]
     [:code "['foo 'bar 'baz] [symbol? symbol? symbol?]"] [:br]
     [:br]
     [:code "     ⋮              ⋮"] [:br]
     [:br]
     [:code "['foo 'bar 'baz 'zab 'oof…] (repeat symbol?)"]]]]

  [:div.note
   [:p "On to that second blank. An " [:em "altered function"] " is a collection of symbols that inform the reader of the changelog the precise names of functions that were altered during that particular change. There may be zero or more, so a " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#non-terminating-sequences"} "non-terminating repeat"] " of predicates is an elegant tool to specify that concept."]

   [:p "Because Speculoos ignores un-paired predicates, the non-terminating sequence of " [:code "symbol?"] " predicates conveys the notion of " [:em "zero or more symbols"] "."]])


 (panel
  [:h3 "Completed " [:em "change"] " scalar specification"]

  (prettyfy-form-prettyfy-eval "(def change-scalar-spec {:date date-spec
                                                            :description string?
                                                            :reference reference-spec
                                                            :change-type change-kinds
                                                            :breaking? breaking-predicate
                                                            :altered-functions (repeat symbol?)})")
  [:div.note
   [:p "Now we've created all the predicates for the parts of a change entry. When assembled into a scalar specification, it looks like this."]

   [:p "Remember, any single changelog version may contain zero or more of that shape of changelog data. To remind ourselves what that looks like..." ]])

 (panel
  [:h3 "Naming the " [:em "version"] " scalar specification"]

  (prettyfy-form-prettyfy-eval "(def version-scalar-spec {:date date-spec
                                                             :responsible person-spec
                                                             :version version-predicate
                                                             :comment string?
                                                             :project-status status-predicate
                                                             :stable boolean?
                                                             :urgency #{:low :medium :high}
                                                             :breaking? boolean?
                                                             :changes (repeat change-scalar-spec)})")
  [:pre [:code ";;           ^--- zero-or-more 'change' specifications"]]

  [:div.note
   [:p "...let's bind that version specification from before to a name."]

   [:p "Let's stuff an infinite number of " [:code "change-scalar-spec"] "s into the " [:code ":changes"] " slot of " [:code "version-scalar-spec"] "."]

   [:p "Now, this one, single " [:code "version-scalar-spec"] " could potentially validate an arbitrary number of changes. Each of those changes can announce alterations to an arbitrary number of functions."]])


 (panel
  [:h3 "How many versions could a changelog contain?"]

  [:pre
   [:code "[version-scalar-spec]"]
   [:br]
   [:code "[version-scalar-spec version-scalar-spec]"]
   [:br]
   [:code "[version-scalar-spec version-scalar-spec version-scalar-spec]"]
   [:br]
   [:code "         ⋮                    ⋮                   ⋮"]]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "(def changelog-scalar-spec (repeat version-scalar-spec))")

  [:div.note
   [:p "If we recall from the beginning, a changelog is an ever-growing sequence of versions. Upon the initial release, we have one version, which we could validate with this specification."]

   [:p "After a while, we make some upgrades, and release a second version. The changelog has a version entry appended the sequence. The two-element changelog can be validated with this specification."]

   [:p "Oops. We found a bug, and need to make a third version. The changelog describing the new version now has three entries, validated with this specification."]

   [:p "Hmm. We can't know ahead of time how many versions we'll have, and it would be nice if we didn't have to keep manually updating the sequence each time we need to add to the changelog. Speculoos specifications are merely standard Clojure collections. " [:code "clojure.core/repeat"] " provides a convenient way to express " [:em "an infinite number of things"] "."]

   [:p "Fun! A " [:code "clojure.lang/repeat"] " nested in a " [:code "clojure.lang/repeat"] ". Speculoos can handle that without a sweating. As long as there's not a repeat at the same path in the data. And there isn't. The changelog is hand-written, with each entry unique."]])


 (panel
  [:h3 "Validating (fictitious) changelog data"]

  [:div.no-display (def changelog-data (load-file "resources/case_study/edited_changelog.edn"))]

  (prettyfy-form-prettyfy-eval "(only-invalid (validate-scalars changelog-data changelog-scalar-spec))" 65 55)

  [:div.note
   [:p "So, I don't see any reason we shouldn't validate a changelog. This is Speculoos' actual " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/changelog_entries/changelog.edn"} "operational changelog"] ". While writing the first draft of this case study, I validated it and corrected the errors (see the case study " [:a {:href "#conclusion"} "conclusion"] "). Therefore, validating the real changelog doesn't have any interesting errors to look at."]

   [:p "For our walk-through, I've cooked up a somewhat " [:a {:href "https://github.com/blosavio/speculoos/tree/main/resources/case_study/edited_changelog.edn"}  "fictitious changelog"] " to try out our scalar specification. I trimmed the Speculoos library changelog and added a few deliberate invalid scalars. We'll invoke " [:code "validate-scalars"] " with the changelog data in the upper row, and the scalar specification in the lower row."]

   [:p [:code "validate-scalars"] " returns a sequence of validation results, and " [:code "only-invalid"] " filters the sequence to keep only the results where the scalar did not satisfy the predicate it was paired with. We can see that there are six invalid scalars, each with its own map that details the problem."]

   [:ul
    [:li "String " [:code "\"okay?\""] " is not a valid " [:em "project status"] " because it is not a member of the set " [:code "#{:active :deprecated :experimental :inactive :stable}"] "."]
    [:li [:code "nil"] " is not a valid month because it is not a member of the enumerated months."]
    [:li [:code ":removed-function"] " (note the lack of a trailing 's') is not a valid " [:em "change type"] " because it is not a member of the enumerated possibilities."]
    [:li [:code "me_at_example.com"] " is not a valid " [:em "email"] " because it does not satisfy the regular expression predicate."]
    [:li [:code "32"] " is an invalid " [:em "day"] " because it is greater than " [:code "31"] " and therefore fails to satisfy " [:code "day-predicate"] "."]
    [:li [:code ":smash-data"] " is not a valid " [:em "removed function"] " datum because the specification requires it to be a symbol."]]

   [:p "While this demonstration used slightly fictitious data, it is representative of the actual problems I discovered when I validated the real changelog."]])


 (panel)


 (load-file "resources/screencast_sections/mottos.clj")


 (panel
  [:h3 "Specifying & validating collections"]

  [:ol
   [:li "Required keys."]
   [:li "Relationship between version numbers."]]

  [:div.note
   [:p [:a {:href "https://github.com/blosavio/speculoos#mottos"} "Motto #1"] " for using Speculoos is to separate scalar validation from collection validation. Scalar validation concerns the properties of individual datums, such as " [:em "Is the day thirty-one or less?"] " or " [:em "Is the email a string with an @ symbol?"]]

   [:p "Collection validation concerns itself with properties of the collections themselves, such as " [:em "Does this map contain the required keys?"] ", as well as " [:em "relationships"] " between scalars, such as " [:em "Is the second integer one greater than the first integer?"]]

   [:p "Collection validation is powerful, but writing collection specifications can by a tad tricky. So judgment is called for. There's no need to validate everything in the universe. Let's just validate two properties of interest."]])


 (panel
  [:h3 "Ensuring required keys"]

  [:div.no-display
   (def version-required-keys #{:date
                                :responsible
                                :version
                                :comment
                                :project-status
                                :urgency
                                :breaking?
                                :changes})

   (def changes-required-keys #{:description
                                :date
                                :change-type
                                :breaking?})]

  [:div.side-by-side-container
   [:div.side-by-side [:pre [:code (prettyfy (str "(def version-required-keys " version-required-keys ")") 15)]]]
   [:div.side-by-side [:pre [:code (prettyfy (str "(def changes-required-keys " changes-required-keys ")") 15)]]]]

  [:div.note
   [:p "Earlier when we were validating the scalars, we were concerned with whether the date was an integer or whether the email was a string. But scalar validation does not concern itself with the " [:em "existence"] " of a particular datum. If a datum exists and it can be paired with a predicate, the datum is validated. If there's no datum to pair with a predicate, the predicate is ignored. When we want to ensure the existence of a datum, we use a collection predicate."]

   [:p "It seems reasonable that a changelog entry for a version must have a version number, a date, a person responsible, a comment, the project's status, the urgency of switching to that version, whether that version is breaking with respect to the previous version, and a listing of the actual changes. Let's gather those required keys into a set."]

   [:p "The scalar specification was concerned with the properties of those concepts, " [:em "if they exist in the data"] ". This collection predicate tests whether or not they are present."]

   [:p "Furthermore, we'd like to require that each of those change listings contains a description, a date, a change type, and whether it is a breaking change. Here are those required keys."]])


 (panel
  [:h3 "Writing a predicate-returning function"]

  [:pre (prettyfy-form-prettyfy-eval "(defn contains-required-keys?
                                     \"Returns a predicate that tests whether a map passed as the first argument contains all keys enumerated in set `req-keys`.\"
                                     [req-keys]
                                     #(empty? (clojure.set/difference req-keys (set (keys %)))))")]

  [:div.vspace]

  (prettyfy-form-prettyfy-eval "((contains-required-keys? #{:a :b :c}) {:a 1 :b 2 :c 3})")

  (prettyfy-form-prettyfy-eval "((contains-required-keys? #{:a :b :c}) {:a 1 :b 2 :c 3 :d 4})")

  (prettyfy-form-prettyfy-eval "((contains-required-keys? #{:a :b :c}) {:a 1})")

  [:div.note
   [:p "Collection validation doesn't regard a set as a predicate they way scalar validation does, so we need to write a predicate function that will accept a collection and a list of required keys and returns a boolean reporting whether that collection contains those keys. However, we have two situations where we want to do mostly the same things: keys required in a " [:em "version"] " map, and keys required in a " [:em "change"] " map. We don't want to repeat code. So we write a higher order function that returns a predicate."]

   [:p "Let's give that a spin."]

   [:p "The first two examples evaluate to " [:code "true"] " because the maps do indeed contain all three required keys. The second example contains an extra " [:code ":d"] " key, but the predicate doesn't mind. The third example returns " [:code "false"] " because the map is missing keys " [:code ":b"] " and " [:code ":c"] "."]])


 (panel
  [:h3 "Required keys predicates"]

  [:pre [:code "(contains-required-keys? version-required-keys)"]]

  [:div.vspace]

  [:pre [:code "(contains-required-keys? changes-required-keys)"]]

  [:div.note
   [:p "The following creates a predicate that tests whether a version map contains the required keys."]

   [:p "And this predicate tests whether a change map contains the required keys."]])


 (panel
  [:h3 "Collection predicates apply to their parent container"]

  [:pre (prettyfy-form-prettyfy-eval "(def version-coll-spec {:req-ver-keys? (contains-required-keys? version-required-keys)
                                                     :changes (vec (repeat 99 {:req-chng-keys? (contains-required-keys? changes-required-keys)}))})")]

  [:div.note
   [:p "One of the principles of composing a collection specification is " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#where-collection-predicates-apply"} [:em "Predicates apply to their immediate parent collection"]] ". The practical consequence of that is we insert the predicate into a collection of the same kind that we want to validate. We define a collection specification for a version map like this."]

   [:p "There is one required-keys predicate aimed at the top-level version map. There is a second required-keys predicate aimed at the changes sequence. (Because of the current implementation, it is not possible to use an infinite " [:code "repeat"] " to validate zero or more collections. I therefore had to make a defined number of them — 99 because that seems plenty for this situation — and convert it to a vector. I very much want to revisit this implementation to see if this restriction can be removed, for generality, and so that writing the specification is more elegant.)"]

   [:p "Kinda complicated, but this Case Study isn't a tutorial."]])


 (panel
  [:h3 "Quick test: validating required keys"]

  [:pre
   [:code.form "(only-invalid (validate-collections (get-in* changelog-data [1])
                                             version-coll-spec))"]
   [:br]
   [:code.eval
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

  [:div.note
   [:p "We can run a quick test on version 1 of the trimmed changelog version."]

   [:p "We can see that one predicate was not satisfied: the anonymous predicate produced by the " [:code "contains-required-keys"] " higher-order function. It tells us that this map doesn't contain at least one required key, in this case, " [:code ":urgency"] "."]

   [:p "In that example, we used " [:code "get-in*"] " to extract a single changelog entry describing a single version. But ultimately, we want to validate zero or more version entries as the project develops over time, so we use our " [:code "repeat"] " trick."]])


 (panel
  [:h3 "Zero-or-more " [:em "version"] " collection specificaitons"]

  [:pre (prettyfy-form-prettyfy-eval "(def changelog-coll-spec (vec (repeat 99 version-coll-spec)))")]

  [:div.note
   [:p "Now, we can validate an ever-growing changelog with that one collection specification."]

   [:p "That takes care of testing for the presence of all the required keys."]])


 (panel
  [:h3 "Predicate to test proper version incrementing"]

  [:pre (prettyfy-form-prettyfy-eval "(defn properly-incrementing-versions?
                                     \"Returns `true` if each successive version is exactly one more than previous.\"
                                     [c-log]
                                     (every? #{1} (map #(- (:version %2) (:version %1)) c-log (next c-log))))")]



  [:div.note
   [:p "Someone might reasonably point out that manually declaring the version number inside a sequential collection is redundant and error-prone. It is. But, I may change my mind in the future and switch to dotted version numbers, or version letters, or some other format. Plus, the changelog is intended to be machine- and human-readable (with priority on the latter), and for organizing purposes, the subsections are split between different files. So it's more ergonomic to include an explicit version number. In that case, we can validate the version number sequence as a kind of 'spell-check' to alert me when I've made an error writing a changelog entry."]

   [:p "Here's a predicate that will extract the version number from each changelog entry and compare it to the previous."]])


 (panel
  [:h3 "Quick test: Validate version increments"]

  [:pre
   [:code.form "(validate-collections changelog-data
                      [properly-incrementing-versions?])"]
   [:br]
   [:code.eval
    ";; => ({:datum [«data elided»],
;;      :valid? false,
;;      :path-predicate [0],
;;      :predicate properly-incrementing-versions?,
;;      :ordinal-path-datum [],
;;      :path-datum []})"]]

  [:div.note
   [:p "Let's give it a spin. Collection predicates apply to their immediate parent collection, so we insert the predicate into the root of the specification."]])


 (panel
  [:h3 "Quick test: Finind offending values"]

  (prettyfy-form-prettyfy-eval "(map #(:version %) changelog-data)")

  [:div.note
   [:p "Our " [:em "ad hoc"]" specification contained only a single predicate, " [:code "properly-incrementing-versions?"] ", and it was not satisfied with the datum it was paired with. Unfortunately, we only have the identity of the unsatisfied predicate, and the value of the datum, which is the entire changelog in this case. So we don't have any details on " [:em "where"] " exactly the version numbers are wrong. We need to use our Clojure powers for more insight. Fortunately, it's a one-liner to pull out the version datums."]

   [:p "Oops. " [:code "99"] " does not properly follow " [:code "1"] ". Gotta go edit the third changelog entry."]

   [:p "Notice that, while on a basic level, we are inspecting scalars, we couldn't use scalar validation for this task. We are validating the " [:em "relationships"] " between multiple scalars. Handling multiple scalars necessarily requires a collection validation."]])


 (panel
    [:h3 "Assembling the collection specification"]

    (prettyfy-form-prettyfy-eval "(def changelog-coll-spec (concat [properly-incrementing-versions?] (vec (repeat 99 version-coll-spec)))))")

    [:div.note
     [:p "We've now created and demonstrated collection specifications for both the required keys and for properly-incrementing version numbers. Let's put them together into a single specification. Speculoos specifications are standard Clojure collections, so we can use regular composition. The changelog collection specification is a vector — mimicking the shape of the changelog data — containing the " [:code "properly-incrementing-versions?"] " predicate followed by an infinite number of version collection specifications."]])


 (panel
  [:h3 "Sanity check: validate with composed collection specification"]

  [:pre
   [:code.form "(only-invalid (validate-collections changelog-data
                                    changelog-coll-spec))"]
   [:br]
   [:code.eval
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

  [:div.note
   [:p "As a sanity check, let's re-run the validation with the composed collection specification."]

   [:p "Exactly the same two invalid results we saw before. There is a problem with the version number intervals. And one of the changelog version entries is missing a required key. The only difference is that we used one comprehensive collection specification, " [:code "changelog-collection-spec"] "."]])


 (panel
  [:h3 "'Combo' validation"]

  [:pre [:code "(only-invalid (validate changelog-data\n                        changelog-scalar-spec\n                        changelog-coll-spec))"]]

  [:div.note
   [:p "If we find it convenient, we could do a combo so that both scalars and collections are validated with a single function invocation."]

   [:p "I won't evaluate the expression because we've already seen the results."]

   [:p "I should also mention that " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#combo-validation"} "'combo' validation"] " with " [:code "validate"] " and friends does not violate " [:a {:href "https://github.com/blosavio/speculoos?tab=readme-ov-file#mottos"} "Motto #1"] ". It performs a scalar validation, then a wholly distinct collection validation, then merges the results. The two tasks are, as always, distinct. " [:code "validate"] " merely provides us with a convenient way to perform both with one function evaluation."]])

 (panel
  [:h3 "Observations & conclusion"]

  [:ul
   [:li "efficiency & expressiveness"]
   [:li "importance of unit-testing predicates"]
   [:li "real-world counts of changelog errors"]
   [:li "results suggest procedural changes"]
   [:li "experiences beyond simplified examples"]]

  [:div.vspace]

  [:pre [:code "{:year int?\n :month string?\n :day int?}"]]

  [:div.note
   [:p "Valuable exercise. Bespoke validation functions, but specifications faster. (proper scientific test...) Predicates+specifications more understandable & maintainable than one-off validation functions."]

   [:p "Realization: Real-world predicate functions ought to be formally unit-tested. 1. Edge-case bug and 2. fumbling a set operation, created dedicated namespace with  tests. Specifications, and by extension, validation, are only as good as the predicates. If predicates crummy,  validation crummy, too. Unit-testing does take a little time and effort. But like unit-testing, the time and effort is worth the investment."]

   [:p "Eleven errors spread across multiple files, including two errant " [:code "nil"] "s, and numerous mis-spelled keywords. From now on, can validate each changelog entry with the exact same specifications we've already written here. Checking the changelog's correctness the moment typing it in, instead of finding out sometime later."]

   [:p "Validation suggestsed procedural changes: keyboarding mistakes was eye-opening, create template based upon the specification, so that each new entry will have spellings that conform to specification. Long-term, a command-line tool?"]

   [:p "Beyond intentionally simplified examples. Performance. But" [:em "experimental"] ". And interactive development (i.e., at the " [:span.small-caps "repl"] ", not in the middle of a high-throughput pipeline), tolerable. Also, validation report unwieldy. Glossed with " [:code "«data elided»"] ". When processing it with machines, it doesn't matter much. Going to think about."]

   [:p "Maybe not test-driven dev, but similar realization: Writing specifications for some data (or a function's arguments/returns) before you have the data is a legit tactic. Forces clarified thinking about how the data ought to arranged, and documents it in human- and machine-readable form. Specification for a date…"]

   [:p "…without having any concrete substantiation of data, can discuss the merits of those choices. Restrictions on " [:em "year"] "? Should " [:em "month"] " values be strings, or keywords? Should  key for " [:em "day of the month"] " be " [:code ":day"] " or " [:code ":date"] "? That little map is not pseudo-code! We could send it, un-modified, to " [:code "validate-scalars"] " and get a feel for how it would work with real data."]

   [:p "Let me know" " what you think."]])


 (whats-next-panel
  study-index
  [:div.note "What's next speaker note within 'Case Study' screencast..."])
 ]