[:section#setup
 [:h2 "Setup"]
 [:h3 "Leiningen/Boot"]
 [:pre [:code "[com.sagevisuals/speculoos \"2\"]"]]
 [:h3 "Clojure CLI/deps.edn"]
 [:pre [:code "com.sagevisuals/speculoos {:mvn/version \"2\"}"]]
 [:h3 "Require"]
 [:pre (print-form-then-eval "(require '[speculoos.core :refer [valid-scalars? valid-collections?]])")]]