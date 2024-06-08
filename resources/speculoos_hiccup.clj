(ns speculoos-hiccup
  "Convenience functions for generating Speculoos Project webpage with hiccup and TufteCSS."
  (:require
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [clojure.test.check.generators :as gen]
   [zprint.core :as zp]
   [hiccup2.core :as h2]
   [hiccup.page :as page]
   [hiccup.element :as element]
   [hiccup.form :as form]
   [hiccup.util :as util])
  (:import java.util.Date))


;; FireFox apparently won't follow symlinks to css or font files


(def ^:dynamic *wrap-at* 60)


(defn comment-newlines
  "Given string s, arrow a, and comment symbol c, linebreak and indent the text.
   Arrow is applied at the head and any trailing newlines are indented to
   maintain formatting."
  {:UUIDv4 #uuid "3ea3a186-6870-4b3f-b569-d0d7ac90f975"}
  [s a c]
  (let [commented-arrow (str c a)
        arrow-prefixed-str (str commented-arrow s)
        equivalent-blanks (clojure.string/join "" (repeat (count a) " "))
        indent (str "\n" c equivalent-blanks)]
    (clojure.string/replace arrow-prefixed-str "\n" indent)))


(defn prettyfy
  "Apply zprint formatting to string s."
  {:UUIDv4 #uuid "a419ba9f-3aaa-4be2-837f-9cc75c51dbe9"}
  [s]
  (zp/zprint-str s {:width *wrap-at*
                    :vector {:wrap-coll? true}
                    :parse-string? true}))


(defn print-form-then-eval
  "Returns a hiccup [:code] block wrapping a Clojure stringified form str-form,
  separator sep (default ' => '), and evaluated value. `def`, `defn`, `s/def/`,
  `defmacro`, `defpred`, and `require` expressions are only evaled; their output
  is not captured.

  Note: Evaluated output can not contain an anonymous function of either
  (fn [x] ...) nor #(...) because zprint requires an internal reference
  to attempt a backtrack. Since the rendering of an anonymous function
  changes from one invocation to the next, there is no stable reference."
  {:UUIDv4 #uuid "39dcd66b-f919-41a2-8376-4c2364bf3c59"}
  ([str-form] (print-form-then-eval str-form " => "))
  ([str-form separator]
   (let [def? (re-find #"^\((s\/)?defn?(macro)?(pred)? " str-form)
         require? (re-find #"^\(require " str-form)
         form (read-string str-form)
         evaled-form (eval form)
         evaled-str (pr-str evaled-form)]
     (if (or def? require?)
       [:code (prettyfy str-form)]
       (let [combo-str (str (prettyfy str-form) " ;;" separator (prettyfy evaled-str))]
         (if (<= (count combo-str) *wrap-at*)
           [:code combo-str]
           [:code (str (prettyfy str-form)
                       "\n"
                       (comment-newlines (prettyfy evaled-str)
                                         separator
                                         ";;"))]))))))


(defn label
  "Generate a TufteCSS numbered sidenote label for target t."
  {:UUIDv4 #uuid "824e0d40-ad61-4142-bbf4-739035849fae"}
  [t]
  (h2/html (form/label {:class "margin-toggle sidenote-number"} t nil)))


(defn side-note
  "Generate a TufteCSS numbered sidenote for target id, with text txt."
  {:UUIDv4 #uuid "3a8e0a85-807c-4f94-9013-6d9bd49aae77"
   :implementation-note "Can't use (form/check-box) b/c it assumes checkbox doesn't contain text, but TufteCSS abuses that feature."}
  [id txt]
  (h2/html [:input {:type "checkbox" :id id :class "margin-toggle"} [:span.sidenote txt]]))


(defn inline-img
  "Generate a TufteCSS inline image figure with target label id, margin note m-note, source src, and alternative text alt. Note: This elemenent may not appear in a [:p]."
  {:UUIDv4 #uuid "de8047ab-80b8-4fff-888a-61af48dcfe14"}
  [id m-note src alt]
  (h2/html [:figure
            (form/label {:class "margin-toggle"} id "&#8853;")
            (form/check-box {:type "checkbox" :class "margin-toggle"} id)
            [:span.marginnote m-note]
            (element/image src alt)]))


(defn margin-img
  "Generate a TufteCSS margin image figure with target label id, margin note m-note, source src, and alternative text alt. Note: This element may not appear in a [:p]."
  {:UUIDv4 #uuid "829dd15f-25fd-4a06-afa3-5f78f005ee30"}
  [id m-note src alt]
  (h2/html (form/label {:class "margin-toggle"} id "&#8853;")
           (form/check-box {:type "checkbox" :class "margin-toggle"} id)
           [:span.marginnote (element/image src alt) m-note]))


(def raw-nav-bar (with-meta [:nav#nav-bar
                             [:ul
                              [:li [:a {:href "home.html"} "Home"]]
                              [:li [:a {:href "ideas.html"} "Ideas"]]
                              [:li [:a {:href "documentation.html"} "Documentation"]]
                              [:li [:a {:href "recipes.html"} "Recipes"]]
                              [:li [:a {:href "diff.html"} [:code "diff"]]]
                              [:li [:a {:href "pros_cons.html"} "Pros, Cons, & Alts"]]
                              [:li.small-caps [:a {:href "index.html"} "api"]]
                              [:li [:a {:href "source.html"} "Source"]]
                              [:li [:a {:href "contact.html"} "Contact"]]]]
                   {:UUIDv4 #uuid "a573aee1-8702-436c-a8ca-3e6954d2bd08"}))


(defn this-page-index
  "Returns index of page within raw-nav-bar structure."
  {:UUIDv4 #uuid "f8736e03-8926-494f-bc5c-5dede6ea84c6"}
  [page]
  (.indexOf (map  #(= page (get-in % [1 2])) (next (get-in raw-nav-bar [1]))) true))


(defn nav-bar
  "Generate a TufteCSS compatible navigation bar in the left margin. current-page will be un-link-ified."
  {:UUIDv4 #uuid "0df46fd8-ae9a-4d02-bc21-5434299f2d6f"}
  [current-page]
  (h2/html (assoc-in raw-nav-bar [1 (inc (this-page-index current-page)) 1] current-page)))


(defn long-date
  "Long-form date+time, with time zone removed."
  {:UUIDv4 #uuid "392e226b-17ed-474e-a44d-a9efcf4b86f4"}
  []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (java.util.Date.)))


(defn short-date
  "Short-form date, named month."
  {:UUIDv4 #uuid "c3c185c1-220a-4a33-838e-91784ab7380e"}
  []
  (.format (java.text.SimpleDateFormat. "yyyy LLLL dd") (java.util.Date.)))


(defn copyright
  "Formated copyright with updated year."
  []
  (let [year (.format (java.text.SimpleDateFormat. "yyyy") (java.util.Date.))]
    (str "Copyright © " (if (= "2024" year) year (str "2024–" year)) " Brad Losavio.")))


(defn page-template
  "Generate a webpage with TufteCSS, compatible nav-bar, header title t,
   hiccup/html dialect body b, and UUIDv4 uuid."
  {:UUIDv4 #uuid "80dd93eb-0c26-41a0-9e6c-2d88352ea4e5"}
  [title uuid body]
  (page/html5
   {:lang "en"}
   [:head
    (page/include-css "tufte.css")
    (page/include-css "speculoos.css")
    [:title title]
    [:meta {"charset"  "utf-8"
            "name" "viewport"
            "content" "width=device-width, initial-scale=1"
            "compile-date" (long-date)}]
    (conj body [:p#page-footer
                (copyright)
                [:br]
                (str "Compiled " (short-date) ".")
                [:span#uuid [:br] uuid]])]))


(defn section
  "Generate a hiccup [:section] with a supplied section-name, h# header level,
   and contents."
  {:UUIDv4 #uuid "a45cd2ee-21d6-4401-bcf2-171c03addc93"}
  [h# section-name & contents]
  (into [(keyword (str "section#" section-name)) [h# section-name]] contents))


(defn random-sentence
  "Generates a random alpha-numeric sentence."
  {:UUIDv4 #uuid "369a6a02-3f26-4ec2-b533-81594d8edcba"}
  []
  (str (->> (gen/sample gen/string-alphanumeric (+ 5 (rand-int 15)))
            (clojure.string/join " " )
            (clojure.string/trim)
            (clojure.string/capitalize)
            )
       "."))


(defn random-paragraph
  "Generate a random alpha-numberic paragraph."
  {:UUIDv4 #uuid "06d489f4-7e29-4de5-bb33-1cb8d0e72088"}
  []
  (loop [num (+ 2 (rand-int 3))
         p ""]
    (if (zero? num)
      (clojure.string/trim p)
      (recur (dec num) (str p " " (random-sentence))))))


(defn section-nav
  "Given a sequence of hiccup [:section#id [:h_ section-name]], return that
   sequence, prepended with a navigation list to each section. #id provides
   the anchor :href, the string immediately following :h_ provides the anchor
   text."
  {:UUIDv4 #uuid "226b7415-0db6-4e50-8106-fc6879fc457e"}
  [& sections]
  (let [section-name (fn [s] (-> s (get 1) (get 1)))
        section-tag (fn [s] (-> s (get 0) str (clojure.string/split #"#") last))
        f (fn [s] [:a {:href (str "#" (section-tag s))} (section-name s)])]
    (into [[:section.nav-section (reduce #(conj %1 (f %2) [:br]) [:p] sections)] sections])))
