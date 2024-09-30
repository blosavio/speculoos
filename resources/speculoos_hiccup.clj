(ns speculoos-hiccup
  "Convenience functions for generating Speculoos Project webpages"
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


(def ^:dynamic *wrap-at* 80)


(def fn-map-additions {"all-paths" :arg1
                       "validate-scalars" :hang
                       "valid-scalars?" :hang
                       "validate-collections" :hang
                       "valid-collections?" :hang})


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


(def fn-obj-regex #"#function[;\n\ ]*\[[\w\.\-\?]*\/([\w\?\=]*(\-?(?!\-)[\w\?]*)*)(?:--\d+)?\]")


(defn revert-fn-obj-rendering
  "Given string `s`, swap out nREPL function object rendering for original
  form."
  {:UUIDv4 #uuid "ca3e8813-3398-4663-b96a-b8289346794e"}
  [s]
  (clojure.string/replace s fn-obj-regex "$1"))


(comment

  ;; explore matching and negative lookahead at https://regexr.com/85b0a

  #"#function[;\n\ ]*\[[\w\.\-\?]*\/([\w\?\=]*(\-?(?!\-)[\w\?]*)*)(?:--\d+)?\]"

  ;; #function   match literal #function
  ;; [;\n\ ]*    match zero-or-more semicolons, newlines, or spaces
  ;; \[          match literal open bracket
  ;; [\w\.\-\?]* match zero-or-more word, periods, hyphens, or question marks
  ;; \/          match literal forward slash
  ;; (           begin capture group #1
  ;; [\w\?\=]*   match zero-or-more word, question mark, or equal signs
  ;; (           begin capture group #2
  ;; \-          match literal hyphen, but...
  ;; (?!\-)      negative lookahead, ...only if not followed by another hyphen
  ;; [\w\?]*     match zero-or-more word or question marks
  ;; )*          end capture group #2, zero-or-more
  ;; )*          end capture group #1, zero-or-more
  ;; (?:--\d+)?  zero-or-one non-capturing groups, two hyphens followed by one-or-more digits
  ;; \]          match literal close bracket

  ;; sample function object rendering strings

  "#function[clojure.core/int?]"
  "#function [clojure.core/int?]"
  "#function[clojure.core/map?--5477]"
  "#function [clojure.core/map?--5477]"
  "#function [clojure.core/map--5477]"
  "#function[speculoos.core/reversed?]"
  "#function[speculoos.function-specs/validate-fn-with]"
  "#function [speculoos.function-specs/validate-fn-with]"

  "#function ;;
  [speculoos-project-readme-generator/reversed?]"

  "#function
  ;; [speculoos-project-readme-generator/reversed?]"

  "#function
   ;;                   [speculoos-project-readme-generator/reversed?]"

  )


(defn render-fn-obj-str
  "Helper function to convert string `s`, representing a clojure.core predicate, into a string-ized nREPL function obj rendering."
  {:UUIDv4 #uuid "65d6b999-4628-43bb-97ac-f8b775829470"}
  [s]
  (-> s read-string eval pr-str))


(defn prettyfy
  "Apply zprint formatting to string s."
  {:UUIDv4 #uuid "a419ba9f-3aaa-4be2-837f-9cc75c51dbe9"}
  [s & width]
  (zp/zprint-str s {:width (or (first width) *wrap-at*)
                    :vector {:wrap-coll? true}
                    :parse-string? true
                    :fn-map fn-map-additions}))


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
  ([str-form] (print-form-then-eval str-form " => " 80 40))
  ([str-form separator] (print-form-then-eval str-form separator 80 40))
  ([str-form width-fn width-output] (print-form-then-eval str-form " => " width-fn width-output))
  ([str-form separator width-fn width-output]
   (let [def? (re-find #"^\((s\/)?defn?(macro)?(pred)? " str-form)
         require? (re-find #"^\(require " str-form)
         form (read-string str-form)
         evaled-form (eval form)
         evaled-str (revert-fn-obj-rendering (pr-str evaled-form))]
     (if (or def? require?)
       [:code (prettyfy str-form)]
       (let [combo-str (str (prettyfy str-form width-fn) " ;;" separator (prettyfy evaled-str width-output))]
         (if (<= (count combo-str) *wrap-at*)
           [:code combo-str]
           [:code (str (prettyfy str-form width-fn)
                       "\n"
                       (comment-newlines (prettyfy evaled-str width-output)
                                         separator
                                         ";;"))]))))))


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


(defn nav
  "Create navigation links. `sections` is a vector of maps, each map with key
  `:section-name` and optionally `:href`. If `href` is not supplied, one is
  generated from `:section-name`."
  {:UUIDv4 #uuid "9e2c9562-adb6-4a56-b1fe-4482c9da83fc"}
  [sections]
  (let [link-fn (fn [m] (vector :a
                                {:href (if (:skip-section-load? m)
                                         (:section-href m)
                                         (if (:section-href m)
                                           (str "#" (:section-href m))
                                           (str "#" (clojure.string/lower-case (:section-name m)))))}
                                (:section-name m)))]
    (interleave (map link-fn sections) (repeat [:br]))))


(defn section-blocks
  "Create hiccup html section blocks given a vector of `sections`."
  {:UUIDv4 #uuid "15893381-f284-4b5e-9680-c8095161c3d9"}
  [sections]
  (let [filenamer (fn [m] (str "resources/readme_sections/"
                               (clojure.string/replace (or (:section-href m)
                                                           (clojure.string/lower-case (:section-name m))) "-" "_")
                               ".clj"))
        section-fn (fn [m] (if (:skip-section-load? m)
                             nil
                             (load-file (filenamer m))))]
    (map section-fn sections)))


(defn page-template
  "Generate a webpage with header title t, hiccup/html dialect body b, and
  UUIDv4 uuid."
  {:UUIDv4 #uuid "80dd93eb-0c26-41a0-9e6c-2d88352ea4e5"}
  [title uuid body]
  (page/html5
   {:lang "en"}
   [:head
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


(def html-non-breaking-space "&nbsp;")
(def pre-code-block-regex #"<pre><code>[\s\S]*<\/code><\/pre>")


(defn line-leading-space-to-non-breaking-space
  "Given a string `s`, replace all occurances of a line-leading space with an
  html non-breaking space."
  {:UUIDv4 #uuid "ec0dff15-9a32-4eb0-89b7-58b515b4154d"}
  [s]
  (clojure.string/replace s #"\n " (str "\n" html-non-breaking-space )))


(defn non-breaking-space-ize
  "GitHub markdown processing collapses non-breaking spaces, even within
  <pre><code> blocks. This destroys the nice hanging indent arranged by zprint.
  This function accepts a string representing html and replaces all line-leading
  spaces within a preformatted code block with an html non-breaking space
  `&nbsp;`."
  {:UUIDv4 #uuid "67da63e5-d7ab-4427-86ef-0e03beef5e3d"}
  [html-str]
  (clojure.string/replace html-str pre-code-block-regex line-leading-space-to-non-breaking-space))


(defn escape-markdowners
  "Replace all underscores/asterisks in string `html-str` with escaped
  characters. GitHub markdown processing treats underscores and asterisks within
  pre-formatted code blocks as italicizing delimiters."
  {:UUIDv4 #uuid "2450029a-08b3-4eb0-9dfe-827342543d0e"}
  [html-str]
  (clojure.string/replace html-str #"(_|\*)" "\\\\$1"))


(defn page-ize
  "Given hiccup/html form `body`, insert a page number into the panel-footer of
  each panel."
  {:UUIDv4 #uuid "c30b6a0b-3092-4e84-a7a5-7fd07ab248b1"}
  [body]
  (let [total (dec (count body))]
    (concat [(first body)] (vec (map-indexed #(vec (conj (vec (butlast %2)) (conj (last %2) [:span.panel-number (str (inc %1) "/" total)] [:span.footer-link "https://github.com/blosavio/speculoos"]))) (vec (rest body)))))))


(defn screencast-template
  "Generate a screencast with header title t, hiccup/html dialect body b, and
  UUIDv4 uuid."
  {:UUIDv4 #uuid "9eac9c34-c44c-4921-97f3-4418b37e15c9"}
  [title uuid body]
  (page/html5
   {:lang "en"}
   [:head
    (page/include-css "speculoos_screencast.css")
    (page/include-js "jquery-3.7.1.min.js")
    (page/include-js "speculoos_screencast.js")
    [:title title]
    [:meta {"charset"  "utf-8"
            "name" "viewport"
            "content" "width=device-width, initial-scale=1"
            "compile-date" (long-date)}]
    (conj
     (vec (page-ize body))
     [:p#page-footer
      (copyright)
      [:br]
      (str "Compiled " (short-date) ".")
      [:span#uuid [:br] uuid]])]))


(defn prettyfy-form-prettyfy-eval
  "Returns a hiccup [:pre [:code]] block wrapping a Clojure stringified form
  str-form, then a [:pre [:code]] block wrapping a separator sep
  (default ' => '), and evaluated value.

  `def`, `defn`, `s/def/`, `defmacro`, `defpred`, and `require` expressions are
  only evaled; their output is not captured.

  Note: Evaluated output can not contain an anonymous function of either
  (fn [x] ...) nor #(...) because zprint requires an internal reference
  to attempt a backtrack. Since the rendering of an anonymous function
  changes from one invocation to the next, there is no stable reference."
  {:UUIDv4 #uuid "0d6c7ba9-a9a5-4980-b449-ea1b27230d47"}
  ([str-form] (prettyfy-form-prettyfy-eval str-form " => " 80 40))
  ([str-form separator] (prettyfy-form-prettyfy-eval str-form separator 80 40))
  ([str-form width-fn width-output] (prettyfy-form-prettyfy-eval str-form " => " width-fn width-output))
  ([str-form separator width-fn width-output]
   (let [def? (re-find #"^\((s\/)?defn?(macro)?(pred)? " str-form)
         require? (re-find #"^\(require " str-form)
         form (read-string str-form)
         evaled-form (eval form)
         evaled-str (revert-fn-obj-rendering (pr-str evaled-form))]
     (if (or def? require?)
       [:pre [:code (prettyfy str-form)]]
       [:pre
        [:code.form (prettyfy str-form width-fn)]
        [:br]
        [:code.eval (comment-newlines (prettyfy evaled-str width-output)
                                      separator
                                      ";;")]]))))


(defn panel
  "Generate a screencast panel, with zero or more hiccup forms."
  {:UUIDv4 #uuid "1ba78b65-4568-4517-9d98-5b21fc39e0f8"}
  [& hiccups]
  (conj
   (into [:div.panel
          [:div.panel-header]]
         hiccups)
   [:div.panel-footer]))


(defn screencast-title
  "Construct a screencast title element from index `idx` and topic
  `screencast-title`."
  {:UUIDv4 #uuid "ba991108-a524-496b-88c6-851587363b20"}
  [idx screencast-title]
  [:h1 (str "Speculoos Screencast " (inc idx) " — " screencast-title)])