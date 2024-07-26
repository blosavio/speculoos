(ns source
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def source-UUID #uuid "31a14e69-fdbf-4507-bec8-b9644ee95444")


(spit "doc/source.html"
      (page-template
       "Speculoos — Source"
       source-UUID
       [:body
        (nav-bar "Source")
        [:article
         [:h1 "Speculoos Source Code"]
         [:section
          [:p "Current version: " [:code "1"] " (experimental)"]
          [:p "Add this dependency to your " [:a {:href "https://clojure.org/reference/deps_edn"} [:code "deps.edn"]] " file:"]
          [:pre [:code "com.sagevisuals/speculoos {:mvn/version \"version 1\"}"]]
          [:p "Or this to your " [:a {:href "https://leiningen.org/"} "Leiningen"] " " [:code "project.clj"] " file:"]
          [:pre [:code "com.sagevisuals/speculoos \"version 1\""]]
          [:p "Find the source code on " [:a {:href "https://github.com/blosavio/speculoos"} "Speculoos' repository page."]]
          [:p "Released under the " [:a {:href "https://opensource.org/license/mit"} "MIT License"] "."]
          [:p [:code "gpg"] " key information:"
           [:ul
            [:li "ID: " [:code "070D3656DB567BA7"]]
            [:li "Fingerprint: " [:code "AFB0 6AE3 A5B4 85EB 1A02 EB95 070D 3656 DB56 7BA7"]]
            [:li "Description: " [:code "Bradley Losavio <"
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
                                  ">"]]]]
          [:p "Check at the " [:a {:href "https://keyserver.ubuntu.com/"} " OpenPGP keyserver"] "."]
          ]]]))