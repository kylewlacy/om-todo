(defproject todo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [garden "1.2.1"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [om "0.7.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [sablono "0.2.22"]
                 [domina "1.0.2"]]
  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.3"]]
  :ring {:handler todo.handler/app}
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/scripts/app.js"
                                   :output-dir "resources/public/scripts/out"
                                   :optimizations :none
                                   :source-map true}}]}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
