(ns todo.core
  (:require [om.core       :as om :include-macros true]
            [sablono.core  :as sablono :refer-macros [html]]
            [domina        :as d]
            [domina.events :as e]))

(enable-console-print!)

(defn new-item []
  {:title "New item"
   :completed? false
   :editing? false})

(defn editing-item [item]
  (assoc item :editing? true))

(defn stop-editing-item [item]
  (assoc item :editing? false))

(defn stop-editing-all-items [items]
  (map stop-editing-item items))

(defn start-editing-item! [item]
  (om/transact! item editing-item))

(defn stop-editing-item! [item]
  (om/transact! item stop-editing-item))

(defn stop-editing-all-items! [items]
  (om/transact! items stop-editing-all-items))

(defn add-item! [items item]
  (om/transact! items #(vec (concat % [item]))))

(defn add-new-item! [items]
  (stop-editing-all-items! items)
  (add-item! items (editing-item (new-item))))

(defn update-completed! [item completed-now?]
  (om/transact! item #(assoc % :completed? completed-now?)))

(defn update-title! [item new-title]
  (om/transact! item #(assoc % :title new-title)))

(defn list-item [item]
  [:li
    [:input {:type      "checkbox"
             :checked   (if (:completed? item) "checked" "")
             :on-change #(update-completed! item (-> % .-target .-checked))}]
    (if (:editing? item)
      [:input {:type       "text"
               :value      (:title item)
               :on-change  #(update-title! item (-> % .-target .-value))
               :on-blur    #(stop-editing-item! item)
               :auto-focus true}]
      [:span {:on-click #(start-editing-item! item)} (:title item)])])

(defn list-items [items]
  [:ol#todo-list (map list-item items)])

(defn todo-list [app owner]
  (om/component
    (html
      [:section#to-do
        [:h1 "To-Do List"]
        (list-items (:items app))
        [:button
          {:on-click (partial add-new-item! (:items app))}
          "Add item"]])))



(def app-state (atom {:items [(new-item)]}))

(defn setup []
  (om/root todo-list app-state {:target (d/by-id "app")}))

(.addEventListener js/window "load" setup)
