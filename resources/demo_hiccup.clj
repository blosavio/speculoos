(ns demo-hiccup
  (:require [hiccup2.core :as h2]
            [hiccup.page :as page]
            [hiccup.element :as element]
            [hiccup.form :as form]
            [hiccup.util :as util]
            [speculoos-hiccup :refer :all]))


(def P0-UUID #uuid "ac8bef0d-96ba-443f-a21b-4b280b171ac4")

(spit "resources/html/P0_test_template.html"
      (page-template
       "FAQ"
       P0-UUID
       [:body
        (nav-bar "faq")
        [:article
         [:h1 "Page 0 --- FAQ: Test Template TufteCSS Speculoos"]
         [:section
          [:p "When in the course of human events..."]
          [:p [:a {:href "example.com"} "Lorem ipsum dolor sit amet, consectetur adipiscing elit."] [:br]]
          [:p [:a {:href "example.com"} "Amet justo donec enim diam vulputate ut pharetra. "] [:br]]
          [:p [:a {:href "example.com"} "Vitae suscipit tellus mauris a."] [:br]]
          [:p [:a {:href "example.com"} "Ante metus dictum at tempor commodo."] [:br]]
          [:p [:a {:href "example.com"} "Vel pretium lectus quam id leo."] [:br]]
          [:p [:a {:href "example.com"} "Pharetra magna ac placerat vestibulum lectus mauris ultrices eros."] [:br]]
          [:p [:a {:href "example.com"} "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."] [:br]]
          [:p [:a {:href "example.com"} "Tristique sollicitudin nibh sit amet commodo nulla facilisi."] [:br]]
          [:p "Libero enim sed faucibus turpis. Mi proin sed libero enim sed faucibus. Malesuada pellentesque elit eget gravida cum. Nisl pretium fusce id velit. Nisl rhoncus mattis rhoncus urna neque viverra justo nec. Sit amet mattis vulputate enim nulla aliquet porttitor lacus. In hac habitasse platea dictumst vestibulum rhoncus est pellentesque elit. Fringilla ut morbi tincidunt augue. Pretium quam vulputate dignissim suspendisse in est ante. Nibh nisl condimentum id venenatis a condimentum. Venenatis lectus magna fringilla urna porttitor. Quis viverra nibh cras pulvinar mattis nunc sed. Neque convallis a cras semper. Vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt ornare massa. Potenti nullam ac tortor vitae purus faucibus ornare. Scelerisque in dictum non consectetur a erat nam at. Dui ut ornare lectus sit amet est."]
          [:p "Porta nibh venenatis cras sed felis eget velit. Eget gravida cum sociis natoque. Arcu vitae elementum curabitur vitae. Nisi vitae suscipit tellus mauris a diam maecenas. Integer eget aliquet nibh praesent tristique magna sit. Augue neque gravida in fermentum. Magnis dis parturient montes nascetur ridiculus mus mauris vitae. Ac auctor augue mauris augue neque gravida in fermentum. Lectus proin nibh nisl condimentum id venenatis a condimentum. Id velit ut tortor pretium. Risus viverra adipiscing at in tellus. Arcu vitae elementum curabitur vitae. Enim nulla aliquet porttitor lacus luctus accumsan tortor. In nulla posuere sollicitudin aliquam ultrices sagittis orci a scelerisque. Cursus in hac habitasse platea dictumst quisque sagittis purus sit. Purus ut faucibus pulvinar elementum integer enim neque volutpat ac. Rhoncus mattis rhoncus urna neque viverra justo nec ultrices. Tempus quam pellentesque nec nam. Pharetra diam sit amet nisl suscipit adipiscing bibendum. Neque viverra justo nec ultrices."]
          [:p "Aliquam malesuada bibendum arcu vitae. Arcu ac tortor dignissim convallis aenean et. Vel pretium lectus quam id leo in vitae turpis massa. Erat pellentesque adipiscing commodo elit at imperdiet dui accumsan. Amet mauris commodo quis imperdiet massa. Dictum at tempor commodo ullamcorper. Ipsum dolor sit amet consectetur adipiscing elit. Maecenas sed enim ut sem. Facilisis mauris sit amet massa vitae tortor. Turpis massa sed elementum tempus egestas."]
          [:p "Integer vitae justo eget magna fermentum iaculis eu non. Malesuada fames ac turpis egestas maecenas. Commodo nulla facilisi nullam vehicula. Id consectetur purus ut faucibus pulvinar. Ipsum suspendisse ultrices gravida dictum fusce ut placerat. Tellus in hac habitasse platea dictumst vestibulum. Etiam tempor orci eu lobortis elementum. Enim nunc faucibus a pellentesque sit amet porttitor. Metus aliquam eleifend mi in nulla posuere. Nec sagittis aliquam malesuada bibendum arcu vitae."]]

         ]]))


(def page-1 (page/html5
           {:lang "en"}
           [:head
            (page/include-css "tufte.css")
            [:title "Page 1 --- Speculoos Title"]
            [:meta {"charset"  "utf-8"
                    "name" "viewport"
                    "content" "width=device-width, initial-scale=1"}]]
           [:body
            [:article
             [:h1#tufte-css "Page 1 --- Tufte CSS Speculoos!"]
             [:p.subtitle "Dave Liepmann Brad Losavio"]

             [:section
              [:p "Speculoos provides..."]
              [:p "Speculoos was created by "
               [:a {:href "http://example.com"} "Brad Losavio"]
               " and is an open source project. Inspirations include "
               [:a {:href "https://clojure.org/guides/spec"} "clojure/spec.alpha"]
               ", "
               [:a {:href "https://github.com/plumatic/schema"} "Plumatic Schema"]
               ", and "
               [:a {:href "https://github.com/jamii/strucjure"} "strucjure"]
               "."]]

             [:section
              [:h2#getting-started "Getting Started"]
              [:p "To use Speculoos, declare the deps in deps.edn or lein, then "
               [:code "(require)"]
               " it."]]

             [:section
              [:h2#fundamentals "Fundamentals"]
              [:h3#fundamentals--scalars-specs "Scalar Specs"]
              [:p "The most basic speculoos form is a "
               [:em "scalar spec"]
               "."]
              [:blockquote "Why can't I just..."]
              [:footer
               [:a {:href "https://example.com"} "Naïve Me, while wrestling with spec.alpha"]]
              [:p "Let's try"
               [:label {:class "margin-toggle sidenote-number"
                        :for "side-note-test"}]
               " a sidenote. Foo bar baz. Lorem ipsum. "
               [:input {:type "checkbox"
                        :id "side-note-test"
                        :class "margin-toggle"}
                [:span.sidenote "Sidenote text"]]
               "Now, let's try some bare Clojure form eval: "
               (+ 1 2)
               ". And now, let's try a function to make that ergonomic: "
               (print-form-then-eval '(+ 1 2))]
              [:p "Explore typography basics. This is "
               [:strong "strong"]
               " and this is "
               [:em "emphasis"]
               "."]
              [:p "Let's check that underlining clears descenders: "
               [:a {:href "https://example.com"} "dummy example xyz"]
               "."]
              [:p "Another sidenote example."
               [:label {:class "margin-toggle sidenote-number"
                        :for "second-side-note-test"}]
               " This is the second one."
               [:input {:type "checkbox"
                        :id "second-side-note-test"
                        :class "margin-toggle"}
                [:span.sidenote "Second sidenote"]]
               " Lorem ipsum."]]

             [:section
              [:h2#sidenotes "Sidenotes, Footnotes, and Marginal Notes"]
              [:p "Another example of a sidenote."
               [:label {:class "margin-toggle sidenote-number"
                        :for "third-side-note-test"}]
               " Every label and target must match exactly. I oughta make a pair of helper functions to make the boilerplate disappear. Also, it might be a good policy to prepend 'sidenote-' or 'sn-' to all the ids."
               [:input {:type "checkbox"
                        :id "third-side-note-test"
                        :class "margin-toggle"}
                [:span.sidenote "Third sidenote lorem ipsum dolor imet"]]]]

             [:section
              [:h2#figures "Figures"]
              [:p "Tight integration of graphics with text. Use the "
               [:code "figure"]
               " element."]
              [:figure
               [:label {:for "mn-exports-imports"
                        :class "margin-toggle"}
                "&#8853;"]
               [:input {:type "checkbox"
                        :id "mn-exports-imports"
                        :class "margin-toggle"}]
               [:span.marginnote "From Brad Losavio, " [:em "Speculoos Project"], " page 92."]
               [:img {:src "img/exports-imports.png"
                      :alt "Exports and Imports to and from Denmark & Norway from 1700 to 1780"}]]
              [:p "Gravida arcu ac tortor dignissim convallis aenean et tortor. Morbi tincidunt ornare massa eget egestas purus. Commodo quis imperdiet massa tincidunt nunc."]

              [:p "A margin figure. "
               [:label {:for "mn-rhino"
                        :class "margin-toggle"}
                "&#8853;"]
               [:input {:type "checkbox"
                        :id "mn-rhino"
                        :class "margin-toggle"}]
               [:span.marginnote
                [:img {:src "img/rhino.png"
                       :alt "Image of Rhinoceros"}]
                "F.J. Cole, 'The History of Albrecht Dürer’s Rhinoceros in Zooological Literature,' "
                [:em "Science, Medicine, and History: Essays on the Evolution of Scientific Thought and Medical Practice"]
                " (London, 1953), ed. E. Ashworth Underwood, 337-356. From page 71 of Edward Tufte’s "
                [:em "Visual Explanations"]
                "."]

               "Imperdiet sed euismod nisi porta lorem. Phasellus faucibus scelerisque eleifend donec. Orci sagittis eu volutpat odio facilisis mauris. Lobortis scelerisque fermentum dui faucibus in. Sed cras ornare arcu dui vivamus arcu."]]

             [:section
              [:h2#code "Code"]
              [:p "Proglang handling examples, with the "
               [:code "code"]
               " class. Here's an extended example with a "
               [:code "pre"]
               " element: "
               [:pre [:code "
;; applying a function to every item in the collection
(map tufte-css blog-posts)
;;;; if unfamiliar, see http://www.lispcast.com/annotated-map

;; side-effecty loop (unformatted, causing text overflow) - from https://clojuredocs.org/clojure.core/doseq
(doseq [[[a b] [c d]] (map list (sorted-map :1 1 :2 2) (sorted-map :3 3 :4 4))] (prn (* b d)))

;; that same side-effecty loop, formatted
(doseq [[[a b] [c d]] (map list
                           (sorted-map :1 1 :2 2)
                           (sorted-map :3 3 :4 4))]
  (prn (* b d)))
"]]]]

             [:section
              [:h2#epilogue "Epilogue"]
              [:p "Many thanks go to Edward Tufte and Dave Liepmann for their work. All errors are mine."]]]]))


(spit "resources/html/P1_speculoos.html" page-1)


(def page-2 (page/html5
             {:lang "en"}
             [:head
              (page/include-css "tufte.css")
              (page/include-css "speculoos.css")
              [:title "Page 2 --- Test hiccup-TufteCSS convenience functions"]
              [:meta {"charset"  "utf-8"
                      "name" "viewport"
                      "content" "width=device-width, initial-scale=1"}]]

             [:body
              (nav-bar "Details")
              [:article
               [:h1#convenience-functions "Page 2 --- Clojure Hiccup Tufte CSS Convenience Functions"]
               [:section
                [:p "Using the Tufte-CSS side notes, margin notes, and footnotes require too much boilerplate for my tastes. Since hiccup is Clojure, let's make some convenience functions."]

                [:p "First, we'll try"
                 [:label {:class "margin-toggle sidenote-number"
                          :for "side-note-test"}]
                 " a sidenote the manual way. Foo bar baz. Lorem ipsum. "
                 [:input {:type "checkbox"
                          :id "side-note-test"
                          :class "margin-toggle"}
                  [:span.sidenote "Manual sidenote text"]]

                 "That does, indeed, have a lot of boilerplate. Here's a slick label"
                 (label "test-target")
                 " and a slick side-note."
                 (side-note "test-target" "Slickified sidenote text.")
                 " Looks pretty good."]]

               [:section
                [:p "Now, let's make some image injection functions. An inline image figure. "]
                (inline-img "mn-exports-imports"
                            (h2/html "From Brad Losavio, " [:em "Speculoos Project"], " page 92.")
                            "img/exports-imports.png"
                            "Exports and Imports to and from Denmark & Norway from 1700 to 1780")
                [:p  "Eros in cursus turpis massa tincidunt. Cras tincidunt lobortis feugiat vivamus. Viverra vitae congue eu consequat ac felis donec et. Egestas purus viverra accumsan in. Nec ullamcorper sit amet risus nullam."]

                [:p "And now a margin image figure. "
                 (margin-img "mn-rhino"
                             (h2/html "F.J. Cole, 'The History of Albrecht Dürer’s Rhinoceros in Zooological Literature,' "
                                      [:em "Science, Medicine, and History: Essays on the Evolution of Scientific Thought and Medical Practice"]
                                      " (London, 1953), ed. E. Ashworth Underwood, 337-356. From page 71 of Edward Tufte’s "
                                      [:em "Visual Explanations"]
                                      ".")
                             "img/rhino.png"
                             "Image of Rhinoceros")

                 "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Tortor id aliquet lectus proin nibh nisl condimentum id venenatis. Pellentesque dignissim enim sit amet venenatis urna cursus eget nunc."]

                [:section
                 [:h2#code "Code"]
                 [:p "Proglang handling examples, with the "
                  [:code "code"]
                  " class. Here's an extended example with a "
                  [:code "pre"]
                  " element: "
                  [:pre [:code "
;; applying a function to every item in the collection
(map tufte-css blog-posts)
;;;; if unfamiliar, see http://www.lispcast.com/annotated-map

;; side-effecty loop (unformatted, causing text overflow) - from https://clojuredocs.org/clojure.core/doseq
(doseq [[[a b] [c d]] (map list (sorted-map :1 1 :2 2) (sorted-map :3 3 :4 4))] (prn (* b d)))

;; that same side-effecty loop, formatted
(doseq [[[a b] [c d]] (map list
                           (sorted-map :1 1 :2 2)
                           (sorted-map :3 3 :4 4))]
  (prn (* b d)))
"]]]]

                [:section
                 [:h2#epilogue "Epilogue"]
                 [:p "Many thanks go to Edward Tufte and Dave Liepmann for their work. All errors are mine."]]                
                ]]]))


(spit "resources/html/P2_hiccup_tufte.html" page-2)


(def page-3 (page/html5
             {:lang "en"}
             [:head
              (page/include-css "tufte.css")
              (page/include-css "speculoos.css")
              [:title "Page 3 --- Test Sidebar TufteCSS Speculoos"]
              [:meta {"charset"  "utf-8"
                      "name" "viewport"
                      "content" "width=device-width, initial-scale=1"}]]
             [:body
              [:article
               [:h1 "Page 3 --- Testing TufteCSS Speculoos Navigation Bar"]
               [:div
                (nav-bar "faq")
                [:section
                 [:p "How can we put a navigation bar in the left margin?"]]]]]
             ))


(spit "resources/html/P3_nav_bar_tufte_speculoos.html" page-3)




(def page-4 (page/html5
             {:lang "en"}
             [:head
              (page/include-css "tufte.css")
              (page/include-css "speculoos.css")
              [:title "Page 4 --- Test Sidebar TufteCSS Speculoos"]
              [:meta {"charset"  "utf-8"
                      "name" "viewport"
                      "content" "width=device-width, initial-scale=1"}]]

             [:body
              [:div#main
               (nav-bar "Recipes")]
              [:article
               [:h1 "Page 4 --- Testing TufteCSS Speculoos Main and Box"]
               [:section
                [:p "Can I get the nav bar to not be clipped?"]
                [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Quis ipsum suspendisse ultrices gravida dictum fusce ut placerat orci. Pulvinar elementum integer enim neque volutpat ac tincidunt. Sodales ut eu sem integer vitae justo eget magna. Est sit amet facilisis magna etiam tempor orci. Nunc congue nisi vitae suscipit tellus mauris a diam. Ullamcorper a lacus vestibulum sed arcu non odio euismod. Nunc eget lorem dolor sed viverra ipsum nunc. Est lorem ipsum dolor sit amet consectetur adipiscing elit pellentesque. Aenean vel elit scelerisque mauris pellentesque pulvinar. Consequat id porta nibh venenatis cras sed felis eget. Egestas purus viverra accumsan in nisl nisi scelerisque eu ultrices. Amet est placerat in egestas erat imperdiet sed euismod nisi. Non consectetur a erat nam. Pharetra et ultrices neque ornare aenean euismod. Malesuada fames ac turpis egestas sed. Ut sem viverra aliquet eget sit. Tellus orci ac auctor augue mauris augue. Arcu dictum varius duis at consectetur.

Quis risus sed vulputate odio ut. Lacus laoreet non curabitur gravida arcu ac tortor dignissim convallis. Sagittis nisl rhoncus mattis rhoncus urna neque viverra justo. Morbi tincidunt ornare massa eget egestas purus viverra accumsan. Aenean vel elit scelerisque mauris pellentesque pulvinar pellentesque habitant morbi. Scelerisque varius morbi enim nunc faucibus a pellentesque sit amet. Est ullamcorper eget nulla facilisi etiam dignissim diam quis enim. Erat imperdiet sed euismod nisi porta. Sed pulvinar proin gravida hendrerit lectus a. A arcu cursus vitae congue. Velit laoreet id donec ultrices. Arcu felis bibendum ut tristique. Non pulvinar neque laoreet suspendisse interdum consectetur libero id. Pretium vulputate sapien nec sagittis aliquam malesuada. Gravida rutrum quisque non tellus orci ac. Lobortis mattis aliquam faucibus purus in.

Enim nulla aliquet porttitor lacus luctus accumsan tortor posuere ac. Nisi porta lorem mollis aliquam ut porttitor leo. Consequat ac felis donec et odio pellentesque diam volutpat commodo. Nullam vehicula ipsum a arcu cursus vitae. Enim praesent elementum facilisis leo. Aliquam purus sit amet luctus venenatis lectus magna fringilla urna. Consequat nisl vel pretium lectus quam. Nisl nisi scelerisque eu ultrices vitae auctor eu. Velit laoreet id donec ultrices tincidunt arcu non. Eu mi bibendum neque egestas congue quisque egestas. Euismod in pellentesque massa placerat duis ultricies. Sed vulputate mi sit amet mauris commodo.

Magna fermentum iaculis eu non diam phasellus vestibulum lorem. Amet nulla facilisi morbi tempus iaculis urna id volutpat lacus. Egestas sed tempus urna et. Morbi tincidunt augue interdum velit euismod in pellentesque. Proin fermentum leo vel orci porta non pulvinar neque. Est lorem ipsum dolor sit amet consectetur adipiscing. Commodo sed egestas egestas fringilla phasellus faucibus scelerisque. Amet facilisis magna etiam tempor orci. Luctus accumsan tortor posuere ac ut consequat semper viverra. In aliquam sem fringilla ut morbi tincidunt. Risus sed vulputate odio ut enim blandit volutpat. Purus sit amet luctus venenatis lectus magna fringilla urna porttitor. Fermentum iaculis eu non diam. Neque convallis a cras semper auctor. Sollicitudin tempor id eu nisl nunc mi. Feugiat in fermentum posuere urna. Proin sed libero enim sed faucibus turpis in.

Pellentesque nec nam aliquam sem et. Consequat id porta nibh venenatis cras sed felis. Interdum velit euismod in pellentesque massa placerat. Erat nam at lectus urna duis convallis convallis tellus id. Cras ornare arcu dui vivamus arcu felis bibendum. Ut aliquam purus sit amet luctus venenatis. Tempus quam pellentesque nec nam aliquam sem et tortor consequat. Amet luctus venenatis lectus magna fringilla urna. Mi ipsum faucibus vitae aliquet. Cursus eget nunc scelerisque viverra mauris in aliquam sem fringilla. Augue neque gravida in fermentum. Consectetur adipiscing elit pellentesque habitant morbi tristique. Posuere sollicitudin aliquam ultrices sagittis orci a. At tempor commodo ullamcorper a."]]]]
             ))


(spit "resources/html/P4_box_and_main.html" page-4)




(def P5-UUID #uuid "e6a69e49-081a-4510-8585-cbb154816a3b")

(spit "resources/html/P5_side_by_side.html"
      (page-template
       "Page 5 --- Test Side-by-Side in TufteCSS Speculoos Project"
       P5-UUID       
       [:body
        (nav-bar [:code "fn* fn-in*"])
        [:article
         [:h1 "Page 5 --- Testing Speculoos Side-by-Side divs"]
         [:section
          [:p "Here's a side-by-side code example..."]
          [:div.side-by-side-container
           [:div.side-by-side [:pre [:code "(+ 1 2 3)"]]]
           [:div.side-by-side [:pre [:code "(* 11 22 33)"]]]]
          [:p "Nisl tincidunt eget nullam non nisi est sit amet. Eros donec ac odio tempor orci dapibus ultrices in. Libero justo laoreet sit amet cursus sit amet dictum. Egestas integer eget aliquet nibh praesent tristique. Integer vitae justo eget magna fermentum iaculis. Consectetur adipiscing elit duis tristique sollicitudin nibh sit amet. Purus semper eget duis at tellus at urna condimentum. Sed arcu non odio euismod lacinia. Massa tempor nec feugiat nisl pretium fusce. Ornare arcu odio ut sem nulla pharetra diam sit. Laoreet suspendisse interdum consectetur libero. Massa eget egestas purus viverra accumsan in nisl nisi scelerisque. Id leo in vitae turpis massa sed elementum tempus egestas. Malesuada fames ac turpis egestas integer eget aliquet nibh praesent. Mauris commodo quis imperdiet massa tincidunt nunc pulvinar sapien et. Vestibulum rhoncus est pellentesque elit ullamcorper dignissim cras tincidunt lobortis. Imperdiet nulla malesuada pellentesque elit eget. Iaculis eu non diam phasellus vestibulum. Ut morbi tincidunt augue interdum velit euismod."]]]]))