(ns todo.core
  (:require [om.core       :as om :include-macros true]
            [sablono.core  :as sablono :refer-macros [html]]
            [domina        :as d]
            [domina.events :as e]))

(enable-console-print!)

(defn new-item []
  {:title "New item"
   :completed? false})

(defn add-item [items]
  (let [item (new-item)]
    (om/transact! items #(conj % item))))


(defn list-item [item]
  [:li
    [:input {:type "checkbox"
             :checked (if (:completed? item) "checked" "")}]
    [:span (:title item)]])

(defn list-items [items]
  [:ol#todo-list (map list-item items)])

(defn todo-list [app owner]
  (om/component
    (html
      [:section#to-do
        [:h1 "To-Do List"]
        (list-items (:items app))
        [:button {:on-click (partial add-item (:items app))} "Add item"]])))



(def app-state (atom {:items [(new-item)]}))

(defn setup []
  (om/root todo-list app-state {:target (d/by-id "app")}))

(.addEventListener js/window "load" setup)
