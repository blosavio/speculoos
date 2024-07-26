(ns four-oh-four
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def four-oh-four-UUID #uuid "9bceec92-e40d-488f-b70a-604b11e8499d")


(spit "doc/404.html"
      (page-template
       "Speculoos — Page not found"
       four-oh-four-UUID
       [:body
        nil ;;(nav-bar "_insert_menu_entry_")
        [:article
         [:h1 "Speculoos: Your requested page was not found."]
         [:section
          [:p "Here are the Speculoos Project pages."]
          [:dl
           [:dt [:a.four-oh-four-link {:href "home.html"} "Home"]] [:dd "Brief introduction of the Speculoos experiment."]
           [:dt [:a.four-oh-four-link {:href "ideas.html"} "Ideas"]] [:dd "A more in-depth discussion of the ideas the Speculoos experiment tests."]
           [:dt [:a.four-oh-four-link {:href "documentation.html"} "Documentation"]] [:dd "Discussion of why and how to use Speculoos, including implementation details and a glossary of terms."]
           [:dt [:a.four-oh-four-link {:href "recipes.html"} "Recipes"]] [:dd "Examples showing how to use various features of the Speculoos library."]
           [:dt [:a.four-oh-four-link {:href "diff.html"} [:code "(diff spec.alpha speculoos)"]]] [:dd "Side-by-side comparison of " [:code "spec.alpha"] " and Speculoos."]
           [:dt [:a.four-oh-four-link {:href "pros_cons.html"} "Pros, Cons, & Alts"]] [:dd "Good and bad parts of Speculoos, plus alternative libraries."]
           [:dt [:a.four-oh-four-link {:href "index.html"} [:span.small-caps "api"]]] [:dd "Application Programming Interface, exhaustive listing of Speculoos namespaces, containing their public functions and their  invocation."]
           [:dt [:a.four-oh-four-link {:href "source.html"} "Source"]] [:dd "Source code repository, dependency, compatibility, installation, and release information."]
           [:dt [:a.four-oh-four-link {:href "contact.html"} "Contact"]] [:dd "Speculoos project contact information, and author's motivation."]
           ]]]]))