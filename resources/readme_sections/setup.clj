[:section#setup
 [:h2 "Setup"]
 [:h3 "Leiningen/Boot"]
 [:pre [:code (str "[com.sagevisuals/speculoos \"" project-version "\"]")]]
 [:h3 "Clojure CLI/deps.edn"]
 [:pre [:code (str "com.sagevisuals/speculoos {:mvn/version \"" project-version "\"}")]]
 [:h3 "Require"]
 [:pre (print-form-then-eval "(require '[speculoos.core :refer [valid-scalars? valid-collections?]])")]]