(ns todo.core
  (:require [om.core       :as om :include-macros true]
            [sablono.core  :as sablono :refer-macros [html]]
            [domina        :as d]))

(enable-console-print!)

; http://stackoverflow.com/a/18319708/1311454
(defn remove-nth
  "Remove the `n`th element in `coll`"
  [coll n]
  (vec (concat (subvec coll 0 n) (subvec coll (inc n)))))

(defn remove-key
  "Remove `key` from `coll`, where `key` is either a map key or an array index"
  [coll key]
  (cond
    (and (coll? coll) (not (map? coll)) (number? key))
      (remove-nth coll key)
    :else
      (dissoc coll key)))

(defn parent-cursor
  "Return the cursor one level up in the path of `child-cursor`"
  [child-cursor]
  (let [new-path (vec (butlast (om/path child-cursor)))
        state    (om/state child-cursor)
        val      (get-in @state new-path)
        cursor   (om/-derive child-cursor val state new-path)]
    cursor))

(defn key-in-parent
  "Return the key to get from a parent cursor to `child-cursor`, such that:
   (get (parent-cursor child-cursor) (key-in-parent child-cursor))
     => child-cursor"
  [child-cursor]
  (last (om/path child-cursor)))

(defn remove-item! [cursor]
  "Remove `cursor` from its parent"
  (let [parent (parent-cursor cursor)
        key    (key-in-parent cursor)]
    (om/transact! parent #(remove-key % key))))



(defn new-item []
  {:title "New item"
   :completed? false})



(defn add-item! [items item]
  (om/transact! items #(vec (concat % [item]))))

(defn add-new-item! [items]
  (add-item! items (new-item)))

(defn update-item-completion! [item completed-now?]
  (om/transact! item #(assoc % :completed? completed-now?)))

(defn update-item-title! [item new-title]
  (om/transact! item #(assoc % :title new-title)))

(defn finalize-item! [item]
  (if (empty? (:title @item))
    (remove-item! item)))



(defn list-item [item]
  [:li
    [:input {:type      "checkbox"
             :checked   (:completed? item)
             :on-change #(update-item-completion! item
                                                  (-> % .-target .-checked))
             :tab-index -1}]
   [:input {:type       "text"
            :value      (:title item)
            :on-change  #(update-item-title! item (-> % .-target .-value))
            :on-blur    #(finalize-item! item)}]])

(defn list-items [items]
  [:ol#todo-list (map list-item items)])

(defn add-item-button [items]
  [:button {:on-click (partial add-new-item! items)}
    "Add item"])

(defn todo-list [app owner]
  (om/component
    (html
      [:section#to-do
        [:h1 "To-Do List"]
        (list-items (:items app))
        (add-item-button (:items app))])))



(def app-state (atom {:items [(new-item)]}))

(defn setup []
  (om/root todo-list app-state {:target (d/by-id "app")}))

(.addEventListener js/window "load" setup)
