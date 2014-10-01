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
  {:title      ""
   :completed? false
   :new?       true
   :dragging?  false})

(defn mark-as-old [item]
  (assoc item :new? false))

(defn add-item! [items item]
  (om/transact! items #(-> (map mark-as-old %)
                           (concat [item])
                           vec)))

(defn add-new-item! [items]
  (add-item! items (new-item)))

(defn update-item-completion! [item completed-now?]
  (om/transact! item #(-> % mark-as-old (assoc :completed? completed-now?))))

(defn update-item-title! [item new-title]
  (if (:new? @item)
    (add-item! (parent-cursor item) (new-item)))
  (om/transact! item #(-> % mark-as-old (assoc :title new-title))))

(defn update-dragging-item! [item dragging-now?]
  (om/transact! item #(assoc % :dragging? dragging-now?)))

(defn finalize-item! [item]
  (if (and (empty? (:title @item)) (not (:new? @item)))
    (remove-item! item)))

(defn list-item [item owner]
  (om/component
    (html
      (let [new?       (:new? item)
            draggable? (not new?)
            complete?  (:completed? item)
            title      (:title item)]
        [:li {:class     [(if new? "new")
                          (if complete? "complete")]
              :draggable draggable?}
          [:span {:class ["drag-handle"
                          (if (not draggable?) "disabled")]}]
          [:input {:type      "checkbox"
                   :disabled  (if new? "disabled" "")
                   :checked   complete?
                   :on-change #(update-item-completion! item (-> %
                                                                 .-target
                                                                 .-checked))
                   :tab-index -1}]
          [:input {:type        "text"
                   :value       title
                   :on-change   #(update-item-title! item (-> %
                                                              .-target
                                                              .-value))
                   :on-blur     #(finalize-item! item)
                   :placeholder (if title "New item" "")}]]))))

(defn list-items [items owner]
  (om/component
    (html [:ol#todo-list (om/build-all list-item items)])))

(defn todo-list [app owner]
  (om/component
    (html
      [:section#to-do
        [:h1 "To-Do List"]
        (om/build list-items (:items app))])))



(def app-state (atom {:items [(new-item)]}))

(defn setup []
  (om/root todo-list app-state {:target (d/by-id "app")}))

(.addEventListener js/window "load" setup)
