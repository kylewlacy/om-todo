(ns todo.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype]]))

(defn app-page []
  (html
    (doctype :html5)
    [:html
      [:head
        [:title "To-Do List"]
        [:script {:src "http://fb.me/react-0.8.0.js"}]
        [:script {:src "scripts/out/goog/base.js"}]
        [:script {:src "scripts/app.js"}]
        [:script {:type "application/javascript"} "goog.require('todo.core');"]]
     [:body
       [:div {:id "app"}]]]))

(defroutes app-routes
  (GET "/" [] (app-page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
