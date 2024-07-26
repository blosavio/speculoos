(ns contact
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def contact-UUID #uuid "c1f8107e-b14e-4f09-9bb7-5ff3302e4d48")


(spit "doc/contact.html"
      (page-template
       "Speculoos — Contact"
       contact-UUID
       [:body
        (nav-bar "Contact")
        [:article
         [:h1 "Speculoos Contact"]
         [:section
          [:h2 "Information"]
          [:p "Brad Losavio"
           [:br]

           [:span.totally-not-mail
            "blo"
            [:r "||cat-pictures||"]
            "sav"
            [:r "||cookie-recipes||"]
            "io@sag"
            [:r "||lullaby-songs||"]
            "evis"
            [:r "||washing-machine-repair-videos|"]
            "uals.com"]

           [:br]
           "he/him/his"]]

         [:section
          [:h2 "Motivation"]
          [:p "I wondered if someone could write a " [:code "spec.alpha"]
           "-like thing that used plain data structures and predicates. After a couples weeks of morning walks around my neighborhood, I stared to wonder if " [:em "I"] " could write a " [:code "spec.alpha"]"-like substance that uses plain data structures and predicates. My brain just wouldn't drop the idea and it seemed like a fun project to learn Clojure. Thus, Speculoos."]

          [:p "Clojure is the niftiest programming language I've encountered. It would be cool if Clojure hung around for " [:a {:href "https://paulgraham.com/hundred.html"} "100 years"] ". While much of Clojure is stable and mature, the data specification story seems to be unsettled."
           (label "unsettled")
           (side-note "unsettled"
                      (h2/html "The core team has "
                               [:a {:href "https://github.com/clojure/spec.alpha"} "two"]
                               " "
                               [:a {:href "https://github.com/clojure/spec-alpha2"} "projects"]
                               " in multiple years of alpha stage, and quite a few open source libraries have been "
                               [:a {:href "pros_cons.html#alts"} "published"]
                               "."))
           " For whatever reason, the evolution of a stable answer is taking longer than other parts of the ecosystem. Perhaps I can contribute to the Clojure community by nudging that evolution just a tiny bit. Or maybe there's room for multiple solutions with different approaches."]
          [:p "At this point, my loftiest aspiration is that the core team would notice and be motivated to release some new version of " [:code "spec"] "."]]
]]))