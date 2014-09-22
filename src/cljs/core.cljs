(ns todo.core
  (:require [om.core       :as om :include-macros true]
            [sablono.core  :as sablono :refer-macros [html]]
            [domina        :as d]
            [domina.events :as e]))

(def app-state (atom {:text "Hello wrold!"}))

(defn todo-list [data owner]
  (om/component
    (html [:h1 (:text data)])))



(defn setup []
  (om/root todo-list app-state {:target (d/by-id "app")}))

(.addEventListener js/window "load" setup)
