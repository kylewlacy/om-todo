(ns todo.core
  (:require [om.core       :as om :include-macros true]
            [sablono.core  :as sablono :refer-macros [html]]
            [domina        :as d]))

(enable-console-print!)

(defn new-item []
  {:title "New item"
   :completed? false
   :focus? false})


(defn focus [item]
  (assoc item :focus? true))

(defn unfocus! [item]
  (om/transact! item #(assoc % :focus? false)))

(defn finalize-item! [item]
  (let [current-title (:title @item)
        new-title     (if (empty? current-title) "New item" current-title)]
    (update-title! item new-title)
  (unfocus! item)))

(defn add-item! [items item]
  (om/transact! items #(vec (concat % [item]))))

(defn add-new-item! [items]
  (add-item! items (focus (new-item))))

(defn update-completed! [item completed-now?]
  (om/transact! item #(assoc % :completed? completed-now?)))

(defn update-title! [item new-title]
  (om/transact! item #(assoc % :title new-title)))

(defn list-item [item]
  [:li
    [:input {:type      "checkbox"
             :checked   (if (:completed? item) "checked" "")
             :on-change #(update-completed! item (-> % .-target .-checked))
             :tab-index -1}]
   [:input {:type       "text"
            :value      (:title item)
            :on-change  #(update-title! item (-> % .-target .-value))
            :on-blur    #(finalize-item! item)
            :auto-focus (:focus? item)}]])

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
