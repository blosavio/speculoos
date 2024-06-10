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
          [:p "I wondered if specifications composed of plain data structures and predicates could do the same tasks as " [:code "clojure.spec.alpha"]
           ". After a couples weeks of morning walks around my neighborhood, my brain just wouldn't drop the idea and it seemed like a fun project to learn Clojure. Thus, Speculoos."]

          [:p "I hope to not give the impression of somebody swooping in to solve a problem with ideas no one has thought of before. Far from it. I want to contribute back to a community of people who think carefully about problems, apply their skills and knowledge, and "
           [:a {:href "pros_cons.html#alts"} "share"] " their work."]

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

         [:section
          [:h2 "Confession"]
          [:p "Something funny: Now that I've published a data specification library, I am not sure where Speculoos' story fits. I can't recall a situation while writing Speculoos where it would have helped its own development. Maybe that's because the inputs to a validation library are necessarily open-ended and therefore resist anything but the most generic specification."
           (label "open-ended")
           (side-note "open-ended" (h2/html "How would you specify " [:em "All possible heterogenous, arbitrarily-nested collections"] "? The most you could say is that it was a collection, but not much beyond that."))
           " I simply took it as an article of faith that such a thing is useful. After all, Rich Hickey wrote two."]]
]]))