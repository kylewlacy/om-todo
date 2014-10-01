(ns todo.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype]]
            [garden.core :refer [css]]
            [garden.selectors :as s]
            [garden.units :as u]
            [garden.arithmetic :as a]))

(defn app-style []
  (css {:vendors ["webkit"]}
    [:body
      {:font-family ["Helvetica" "Arial" "sans-serif"]
       :margin "12px"}
      [:h1
        {:font-size "60px"
         :width "100%"
         :text-align :center}]
      [:#todo-list
        {:list-style-type :none
         :margin "6px 0"}
        (let [inner-margin-size    (u/px 6)
              inner-border-size    (u/px 3)
              item-height          (u/px 42)
              inner-height         (a/- item-height (a/* 2 inner-margin-size))
              font-size            (u/px 24)
              textbox-padding      (u/px 3)
              textbox-properties   {:border-width     inner-border-size
                                    :border-style     :solid
                                    :color            "#000"
                                    :border-color     "#000"
                                    :background-color "#fff"
                                    :padding-left     textbox-padding
                                    :height           (a/-
                                                        inner-height
                                                        (a/*
                                                          2
                                                          inner-border-size))
                                    :outline :none}]
          [:li
            {:height item-height
             :background-color "#eee"
             :box-sizing :border-box
             :display #{:flex :-webkit-flex}}
            [:.drag-handle
              {:width          inner-height
               :height         inner-height
               :margin         inner-margin-size
               :display        :inline-block
               :vertical-align :middle}
              [(s/& (s/not :.disabled))
                {:background-image "url(/images/handle.svg)"
                 :cursor           #{:grab :-webkit-grab}}]
              [:&.disabled
                {:background-image "url(/images/handle-disabled.svg)"}]]
            [(s/input (s/attr= :type :checkbox))
              {:display :inline-block
               :vertical-align :middle
               :-webkit-appearance :none
               :border-width inner-border-size
               :border-style "solid"
               :border-color "#999"
               :background-color "#fff"
               :margin inner-margin-size
               :width inner-height
               :height inner-height
               :cursor #{:pointer}}
              [:&:checked
                {:background-image "url(/images/check.svg)"}]
              [:&:disabled
                {:background-color "#ddd"}]]
            [(s/input (s/attr= :type :text))
              ^:prefix {:flex-grow 1}
              {:display :inline-block
               :vertical-align :middle
               :background-color :transparent
               :-webkit-appearance :none
               :border :none
               :margin inner-margin-size
               :width "300px"
               :height inner-height
               :padding-left (a/+ textbox-padding inner-border-size)
               :font-size font-size}
              [:&:focus :&:hover
                textbox-properties]
              [:&:focus
                {:border-color "#f00"}]]
            [:&.new
              [(s/input (s/attr= :type :text))
                textbox-properties]]
            [:&.complete
              [(s/input (s/attr= :type :text))
                {:color "#999"}]]
            [:&.dropzone
              {:height (a/+ inner-height (a/* 2 inner-margin-size))
               :border "5px dashed #666"
               :background-color "#999"}
              ["*"
                {:opacity 0
                 :display :none}]]])]]))

(defn app-page []
  (html
    (doctype :html5)
    [:html
      [:head
        [:title "To-Do List"]
        [:link {:rel  "stylesheet"
                :type "text/css"
                :href "http://meyerweb.com/eric/tools/css/reset/reset.css"}]
        [:link {:rel "stylesheet"
                :type "text/css"
                :href "/styles/app.css"}]
        [:script {:src "http://fb.me/react-0.8.0.js"}]
        [:script {:src "/scripts/out/goog/base.js"}]
        [:script {:src "/scripts/app.js"}]
        [:script {:type "application/javascript"} "goog.require('todo.core');"]]
     [:body
       [:div {:id "app"}
         [:noscript
           [:a {:href "http://sighjavascript.tumblr.com"} "Sorry!"]]]]]))

(defroutes app-routes
  (GET "/" [] (app-page))
  (GET "/styles/app.css" [] {:status 200
                             :header {"Content-Type" "text/css"}
                             :body   (app-style)})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
