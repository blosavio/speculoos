(ns pros-cons
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def pros-UUID #uuid "5c606f2e-4b44-4ecb-b054-3358306cc2c5")


(spit "doc/pros_cons.html"
      (page-template
       "Speculoos — Pros, Cons, & Alts"
       pros-UUID
       [:body
        (nav-bar "Pros, Cons, & Alts")
        [:article
         [:h1 "Speculoos Pros, Cons, & Alternatives"]
         [:section
          [:h2 "Pros"]
          [:ul
           [:li "I think writing specifications with Clojure data literals is nice."]

           [:li "I find it conceptually simpler to specify collections independently from scalars."]

           [:li "The way Speculoos decomposes a heterogeneous, arbitrarily-nested data structure makes it straightforward to write utility functions, especially with the starred functions. Many of the functions in " [:code "speculoos.utility"] " are just a few lines. " [:code "speculoos.core/all-paths"] " exposes this deconstructed version of both the data and the specification, so that you can write your own utilities."]]]

         [:section
          [:h2 "Cons"]
          [:ul
           [:li [:p [:code "clojure.spec.alpha"] " is designed and written by a " [:a {:href "https://www.infoq.com/presentations/Value-Identity-State-Rich-Hickey/"} "genuine"] " " [:a {:href "https://github.com/tallesl/Rich-Hickey-fanclub"} "wizard"] ". I wouldn't be surprised if Rich already had these ideas and then discarded them."]]

           [:li [:p "On the other hand, Speculoos was haphazardly written by a " [:a {:href "contact.html"} "person"] " who'll put milk in the pantry. My ideas may be absurd."]]

           [:li [:p "There seem to be a decent number of Clojure " [:a {:href "pros_cons.html#alts"} "projects"] " that do roughly the same validation tasks with a similar look and feel. Many appear to be mature and/or stable. Why not use one of those? For that matter, why not simply use " [:code "clojure.spec.alpha"] "?"]]
           
           [:li [:p "Cardinal sin of software development: I have written relatively few Clojure specs, and yet I wrote a data specification lib. Speculoos "
                 [:em "skates to where the puck used to be"]
                 ". Roughly speaking, Speculoos aims to replicate an acceptable subset of "
                 [:code "spec.alpha"]
                 " functionality. More specifically, what is contained in the "
                 [:a {:href "https://clojure.org/guides/spec"} [:em "Spec Guide"]]
                 ". If there happen to be any use cases or features not covered there and not apparent from the "
                 [:span.small-caps "api"]
                 ", Speculoos won't have them."
                 (label "exclude")
                 (side-note "exclude" "Other than features I have explicitly decided not to include, like returning conformed values.")
                 " Likewise, Speculoos copies any not-so-great ideas."]]

           [:li [:p [:strong "Speculoos is in a very " [:em "pre-alpha"] "."]
                 " I paid no particular attention to performance, and have done exactly zero performance benchmarking."
                 (label "laborious")
                 (side-note "laborious"
                            "In fact, some of Speculoos' bottom-level functions laboriously and inefficiently step through collections. I deliberately wrote this way for clarity to my inexperienced brain, with complete disregard for good performance.")
                 " Also, function instrumentation is ever-so-barely "
                 [:em "not broken"]
                 " because I don't know how to wrangle vars."]
            
            [:p "Implementation issues aside, some decisions are still up in the air. For one example, if a specification+data is invalid, should it return a fully-qualified invalid key such as "
             [:code ":speculoos/invalid"]
             ", or should it pass the data along and return the invalid announcement out-of-band?"]]

           [:li
            [:p "The un-feature that might sink the entire Speculoos project: Because Speculoos predicates are regular functions, there is no completely general way to automatically deconstruct them to create a generator."]

            [:p [:code "spec.alpha"]
             " dedicates much effort to automatically producing generators to test and exercise functions. I would not be surprised to learn that "
             [:code "spec.alpha"]
             "'s design involves "
             [:code "spec/and"]
             " and "
             [:code "spec/or"]
             " instead of bare predicates precisely to enable this capability."]

            [:p "On the other hand, Speculoos can only generate test values when the predicate is transparent, like "
             [:code "int?"]
             ". The moment a predicate gets wrapped like this "
             [:code "#(int? %)"]
             ", you must either arrange the predicate definition in a proscribed pattern, or you must manually supply the generator. And either of those options requires access to the source code."]]

           [:li
            [:p "Speculoos specifications become unwieldy when an element could be either a scalar or a collection. In that scenario, strictly separating collection validation from scalar specification works against you. For example, suppose you wanted to specify a " [:code "defn"] " S-expression. Sometimes you'll have a docstring…"]
            [:code "(defn foo \"docstring\" [arg1 arg2 …] (body))"]
            [:p "…and sometimes you won't."]
            [:code "(defn baz [arg1 arg2 …] (body))"]
            [:p "Depending on the presence of a docstring, the argument vector could be in the third or fourth position. A Speculoos collection specification is general enough to describe that pattern, but it's not quite as clean as it would be with a "
             [:code "spec.alpha"] " regex sequence spec."
             (label "other-hand")
             (side-note "other-hand" (h2/html "On the other hand, a semi-random sampling of a few dozen " [:code "clojure.core"] " functions turns up very few argument lists which would require that particular kind of optionality. Perhaps " [:code "defn"] " is an outlier."))]]]]

         [:section#alts
          [:h2 "Alternatives"]
          [:ul
           [:li "Staples SparX " [:a {:href "https://github.com/staples-sparx/clj-schema"} "clj-schema"]
            [:p  "Schemas for Clojure data structures and values. Delineates operations on maps, seqs, and sets. Contributors: Alex Baranosky, Laurent Petit, Punit Rathore"]]

           [:li "Steve Miner's " [:a {:href "https://github.com/miner/herbert"} "Herbert"]
            [:p [:em "A schema language for Clojure data"] " for documenting and validating. Schemas themselves are " [:code "edn"] " values. Yay! The " [:em "ReadMe"] " announces that Herbert is obsolete with the introduction of "  [:code "spec.alpha"] " in Clojure release 1.9"]]

           [:li "John Newman's " [:a {:href "https://github.com/johnmn3/injest"} "injest"]
            [:p "Path thread macros for navigating into and transforming data. An alternative to Speculoos' " [:code "fn-in*"] " facilities."]]

           [:li "Metosin " [:a {:href "https://github.com/metosin/malli"} "Malli"]
            [:p [:em "Data-driven schemas"] " incorporating the best parts of existing libs, mixed with their own tools. Lots of features, multiple choices for syntax. Active development as of 2024 May."]]
           
           [:li "Plumatic " [:a {:href "https://github.com/plumatic/schema"} "Schema"]
            [:p "Widely-recommended, widely-used, mature, and pretty to my eyes. If Speculoos has any appeal to you, check it out."]]

           [:li "Christophe Grand's " [:a {:href "https://github.com/cgrand/seqexp"} "seqexp"]
            [:p  [:em "Regular expressions for sequences (and other sequables)!"] " A nice compact library, with an implementation that focuses on performance."
             (label "note-to-self")
             (side-note "note-to-self" "Note to self: Study the implementation.")]]

           [:li "Jonathan Claggett's " [:a {:href "https://github.com/jclaggett/seqex"} "seqex"]
            [:p [:em "Sequence Expressions, similar to regular expressions but able to describe arbitrary sequences of values (not just characters)."] " Tightly-focused, clean and memorable interface. Clever re-application of regular expressions."]]

           [:li "Clojure's " [:a {:href "https://github.com/clojure/spec.alpha"} [:code "spec.alpha"]]
            [:p 
             "Developed by the Clojure team, so there's a ton of skill and experience, plus probably the most battle-hardened alternative. Looks to have been in " [:em "alpha"] " for seven years, so maybe the team is still mulling how to do Clojure specifications. Maybe there's a sliver of real estate for Speculoos' ideas to occupy."]]

           [:li "Clojure's " [:a {:href "https://github.com/clojure/spec-alpha2"} [:code "spec-alpha2"]]
            [:p "The " [:em "ReadMe"]
             (label "alpha-alpha")
             (side-note "alpha-alpha" (h2/html "This page refers to the library as " [:code "alpha.spec"] " but the GitHub project name is " [:code "spec-alpha2"] "."))
             " describes the project as " [:em "an evolution from spec.alpha as well as work towards several new features"] " and the "
             [:a {:href "https://github.com/clojure/spec-alpha2/wiki/Differences-from-spec.alpha"} "differences"] " page calls it " [:em "a work in progress"] "."
             (label "spec-alpha-2-status")
             (side-note "spec-alpha-2-status" (h2/html "As of 2020 June 20, " [:a {:href "https://ask.clojure.org/index.php/9397/clarify-usage-on-the-spec-alpha2-github-page?show=9398#a9398"} "Alex Miller states"] " " [:em "We do not believe this lib is ready to use yet (it is incomplete and has a number of known bugs)."]))
             " Consumes " [:a {:href "https://github.com/clojure/spec-alpha2/wiki/Schema-and-select"} "literal schemas"]
             ", so it seems like we're grasping for the same elusive ideas. There is some discussion on strictly separating symbolic specs and spec objects, but as far as I can tell, this is not separating scalar specification and collection specification the way Speculoos does."
             
             ]]

           [:li "Jamie Brandon's " [:a {:href "https://github.com/jamii/strucjure"} "Strucjure"]
            [:p "A " [:a {:href "https://www.scattered-thoughts.net/writing/strucjure-motivation/"} "well-reasoned"] ", elegant design. Repository was archived on 2019 March 16, but I still often click over to this project."]]

           [:li "Brian Marick's " [:a {:href "https://github.com/marick/structural-typing"} "structural-typing"]
            [:p "Focused on " [:a {:href "https://en.wikipedia.org/wiki/Structural_type_system"} "structural typing"] " in Clojure. Not precisely the same goals, but the " [:em "flavor"] " of this project is similar to Speculoos."]]

           [:li "Peter Taoussanis' " [:a {:href "https://github.com/taoensso/truss"} "Truss"]
            [:p [:em "A tiny library that provides fast and flexible runtime assertions with terrific error messages."] " Its use of pure predicates works the way my brain works."]]
]]]]))