(defproject todo "0.1.0-SNAPSHOT"
  :description "A simple to-do app written in Om and Clojure"
  :url "http://om-todo.herokuapp.com/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [garden "1.2.1"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [om "0.7.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [sablono "0.2.22"]
                 [domina "1.0.2"]
                 [javax.servlet/servlet-api "2.5"]]
  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.3"]]
  :ring {:handler todo.handler/app}
  :source-paths ["src/clj"]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:id "dev"
                        :jar true
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/scripts/app.js"
                                   :output-dir "resources/public/scripts/out"
                                   :optimizations :none
                                   :source-map true}}]}
  :profiles {:dev     {:dependencies [[ring-mock "0.1.5"]]}
             :uberjar {:main todo.handler
                       :aot  :all}})
