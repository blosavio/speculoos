(ns api
  (:require
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [speculoos-hiccup :refer :all]))


(def api-UUID #uuid "6e34abff-9d62-4a6d-b73c-a74d247a59fb")


(spit "doc/api.html"
      (page-template
       "Speculoos — API"
       api-UUID
       [:body
        (nav-bar "api")
        [:article
         [:h1 "Speculoos " [:code "API"]]
         [:section
          [:p "Vulputate odio ut enim blandit volutpat maecenas volutpat blandit aliquam. Pharetra magna ac placerat vestibulum. Pharetra vel turpis nunc eget. Tincidunt lobortis feugiat vivamus at augue eget. Purus non enim praesent elementum. Mus mauris vitae ultricies leo integer malesuada. Pretium aenean pharetra magna ac placerat. Accumsan tortor posuere ac ut consequat semper viverra. Tincidunt id aliquet risus feugiat in ante metus dictum. Viverra tellus in hac habitasse platea dictumst vestibulum."]]]]))