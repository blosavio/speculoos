(ns generate-all-html
  "Process all hiccup files into html for the Speculoos Project.
   Implementation note: This abuses the load-file function by taking advantage of the fact that loading the file evaluates the namespace, and thus triggers the side-effect of the html file spit.")


(def hiccup-filenames
  ["contact"
   "diff"
   "documentation"
   "four_oh_four"
   "home"
   "ideas"
   "pros_cons"
   "recipes"
   "source"])


(defn -main
  [& _]
  (for [x hiccup-filenames]
    (load-file (str "resources/" x ".clj"))))


(comment
  (-main)
  )