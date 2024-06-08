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

           [:li "The way Speculoos decomposes a heterogenous, arbitrarily-nested data structure makes it straightforward to write utility functions, especially with the starred functions. Many of the functions in " [:code "speculoos.utility"] " are just a few lines. " [:code "speculoos.core/all-paths"] " exposes this deconstructed version of both the data and the specification, so that you can write your own utilities."]]]

         [:section
          [:h2 "Cons"]
          [:ul
           [:li [:p [:code "clojure.spec.alpha"] " is designed and written by a " [:a {:href "https://www.infoq.com/presentations/Value-Identity-State-Rich-Hickey/"} "genuine"] " " [:a {:href "https://github.com/tallesl/Rich-Hickey-fanclub"} "wizard"] ". I feel sheepish proposing ideas that Rich has possibly reasoned about, then discarded. The entire Speculoos project may be bulldozing " [:a {:href "https://en.wikipedia.org/wiki/G._K._Chesterton#Chesterton's_fence"} "Chesterton's fence"] "."]]

           [:li [:p "On the other hand, Speculoos was haphazardly written by a " [:a {:href "contact.html"} "person"] " who'll put milk in the pantry. My ideas may be absurd."]]
           
           [:li [:p "Cardinal sin of software development: I have written relatively few Clojure specs, and yet I wrote a data specification lib."]]

           [:li [:p "I paid no particular attention to performance, and have done exactly zero performace benchmarking. In fact, some of Speculoos' bottom-level functions laboriously and ineffiently step through collections. I deliberately wrote this way for clarity to my inexperienced brain, with complete disregard for good performance."]]

           [:li [:p "There seem to be a decent number of Clojure " [:a {:href "pros_cons.html#alts"} "projects"] " that do roughly the same validation tasks with a similar look. Many appear to be mature and/or stable. Why not use one of those? Why not simply use " [:code "clojure.spec.alpha"] "?"]]

           [:li [:p [:strong "Speculoos is in a very " [:em "pre-alpha"] "."] " For example, if a specification+data is invalid, should it fail fast by returning a fully-qualified invalid key, or for maximum resiliency, should it pass the data along and return the invalid result out-of-band? Or, how could Speculoos give the user that choice: an argument flag, or an entirely different function? Also, function instrumentation is ever so barely " [:em "not broken"] "."]]

           [:li [:p "Skating to where the puck " [:em "used to be"] ". Roughly speaking, Speculoos aims to replicate an acceptable subset of " [:code "spec.alpha"] " functionality, more specifically, what is contained in the " [:a {:href "https://clojure.org/guides/spec"} [:em "Spec Guide"]] ". If there happen to be any use cases or features not covered there and not apparent from the " [:span.small-caps "api"] ", Speculoos won't have them."
                 (label "exclude")
                 (side-note "exclude" "Other than features I have explicitly decided not to include, like returning conformed values.")]]

           [:li [:p "The un-feature that might sink the entire Speculoos project: Because Speculoos predicates are regular functions, there is no straightforward way to automatically deconstruct them to create a generator. "
                 [:code "spec.alpha"]
                 " dedicates much effort to automatically producing generators to test and exercise functions. I would not be surprised to learn that "
                 [:code "spec.alpha"]
                 "'s design involves "
                 [:code "spec/and"]
                 " and "
                 [:code "spec/or"]
                 " instead of bare predicates precisely to enable this capability. Speculoos on the other hand, can only generate test values when the predicate is transparent, like "
                 [:code "int?"]
                 ". The moment a predicate gets wrapped like this "
                 [:code "#(int? %)"]
                 ", Speculoos can only rely on a generator that you manually supply."]]

           [:li [:p "Speculoos struggles when a thing could be either a scalar or a collection. For example, suppose you want to specify a " [:code "defn"] "  S-expression with a symbol in the first positon, a symbol in the second positon, then zero or one doctrings, followed by an argument vector, etc. A Speculoos collection specification is general enough to describe that pattern, but it's not quite as clean as it would be with " [:code "spec.alpha"] " regex sequence spec."
                 (label "other-hand")
                 (side-note "other-hand" (h2/html "On the other hand, a semi-random sampling of a few dozen " [:code "clojure.core"] " functions turns up very few argument lists which would require that particular kind of optionality. Perhaps " [:code "defn"] " is an outlier."))]]]]

         [:section#alts
          [:h2 "Alternatives"]
          [:ul
           [:li "Staples SparX " [:a {:href "https://github.com/staples-sparx/clj-schema"} "clj-schema"]
            [:p  "Schemas for Clojure data structures and values. Delineates operations on maps, seqs, and sets. I am encouraged by the similarities to Speculoos. Contributors: Alex Baranosky, Laurent Petit, Punit Rathore"]]

           [:li "Steve Miner's " [:a {:href "https://github.com/miner/herbert"} "Herbert"]
            [:p [:em "A schema language for Clojure data"] " for documenting and validating. Very nice look and feel. Schemas themselves are " [:code "edn"] " values. Yay! The " [:em "ReadMe"] " announces that Herbert is obsolete with the introduction of "  [:code "spec.alpha"] " in Clojure release 1.9"]]

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
            [:p [:em "Sequence Expressions, similar to regular expressions but able to describe arbitrary sequences of values (not just characters)."] " Wonderfully-focused, clean and memorable interface. Clever re-application of regular expressions."]]

           [:li "Clojure's " [:a {:href "https://github.com/clojure/spec.alpha"} [:code "spec.alpha"]]
            [:p 
             "Developed by the Clojure team, so there's a ton of skill and experience. Looks to have been in " [:em "alpha"] " for seven years, so maybe the team is still mulling how to do Clojure specifications. Maybe there's a sliver of real estate for Speculoos' ideas to occupy."]]

           [:li "Clojure's " [:a {:href "https://github.com/clojure/spec-alpha2"} [:code "spec-alpha2"]]
            [:p "The " [:em "ReadMe"]
             (label "alpha-alpha")
             (side-note "alpha-alpha" (h2/html "This page refers to the library as " [:code "alpha.spec"] " but the GitHub project name is " [:code "spec-alpha2"] "."))
             " describes the project as " [:em "an evolution from spec.alpha as well as work towards several new features"] " and the "
             [:a {:href "https://github.com/clojure/spec-alpha2/wiki/Differences-from-spec.alpha"} "differences"] " page calls it " [:em "a work in progress"] ". Consumes " [:a {:href "https://github.com/clojure/spec-alpha2/wiki/Schema-and-select"} "literal schemas"]
             ", so that gives me encouragement that Speculoos might be on the right track. There is some discussion on strictly separating symbolic specs and spec objects, but as far as I can tell, this is not separating scalar specification and collection specification the way Speculoos does. Beyond the few GitHub pages for the project, I  been able to find any external tutorial-type materials that might help me understand it better."
             (label "spec-alpha-2-status")
             (side-note "spec-alpha-2-status" (h2/html "As of 2020 June 20, " [:a {:href "https://ask.clojure.org/index.php/9397/clarify-usage-on-the-spec-alpha2-github-page?show=9398#a9398"} "Alex Miller states"] " " [:em "We do not believe this lib is ready to use yet (it is incomplete and has a number of known bugs)."]))]]

           [:li "Jamie Brandon's " [:a {:href "https://github.com/jamii/strucjure"} "Strucjure"]
            [:p "A " [:a {:href "https://www.scattered-thoughts.net/writing/strucjure-motivation/"} "well-reasoned"] ", elegant design. Repository was archived on 2019 March 16, but I still often click over to this project."]]

           [:li "Brian Marick's " [:a {:href "https://github.com/marick/structural-typing"} "structural-typing"]
            [:p "Focused on " [:a {:href "https://en.wikipedia.org/wiki/Structural_type_system"} "structural typing"] " in Clojure. Not precisely the same goals, but the " [:em "flavor"] " of this project is similar to Speculoos."]]

           [:li "Peter Taoussanis' " [:a {:href "https://github.com/taoensso/truss"} "Truss"]
            [:p [:em "A tiny library that provides fast and flexible runtime assertions with terrific error messages."] " Its use of pure predicates works the way my brain works. I like the explicitness of the function argument assertions, but struggle with accepting the trade-off of gumming up the function body."]]
]]]]))