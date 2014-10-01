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

(defn insert-at
  "Insert `item` at index `n` in `coll`"
  [coll n item]
  (vec (concat (subvec coll 0 n) [item] (subvec coll n))))

(defn remove-key
  "Remove `key` from `coll`, where `key` is either a map key or an array index"
  [coll key]
  (cond
    (and (coll? coll) (not (map? coll)) (number? key))
      (remove-nth coll key)
    :else
      (dissoc coll key)))

(defn insert-key
  [coll key item]
  "Insert `item` into `key` of `coll`, where `key` is either a map key or array
   index"
  (cond
    (and (coll? coll) (not (map? coll)) (number? key))
      (insert-at coll key item)
    :else
      (assoc coll key)))

(defn parent-cursor
  "Return the cursor one level up in the path of `child-cursor`"
  [child-cursor]
  (let [new-path (vec (butlast (om/path child-cursor)))
        state    (om/state child-cursor)
        val      (get-in @state new-path)]
    (om/-derive child-cursor val state new-path)))

(defn child-cursor
  "Return the cursor within `parent-cursor` with key `child-key`- either a
   vector index or map key"
  [parent-cursor child-key]
  (let [new-path (vec (concat (om/path parent-cursor) [child-key]))
        state    (om/state parent-cursor)
        val      (get-in @state new-path)]
    (om/-derive parent-cursor val state new-path)))

(defn key-in-parent
  "Return the key to get from a parent cursor to `child-cursor`, such that:
   (get (parent-cursor child-cursor) (key-in-parent child-cursor))
     => child-cursor"
  [child-cursor]
  (last (om/path child-cursor)))

(defn remove-item!
  "Remove `cursor` from its parent"
  [cursor]
  (let [parent (parent-cursor cursor)
        key    (key-in-parent cursor)]
    (om/transact! parent #(remove-key % key))))

(defn move-item! [cursor new-key]
  "`assoc` the value of cursor with the key `new-key` in its parent"
  (let [value       @cursor
        current-key (key-in-parent cursor)
        parent      (parent-cursor cursor)]
    (if (not= new-key current-key)
      (om/transact! parent #(-> %
                                (remove-key current-key)
                                (insert-key new-key value))))))

; http://stackoverflow.com/a/23528539/1311454
(defn index-of-node
  "Return the index of `node` in its parent"
  [node]
  (let [parent   (.-parentNode node)
        siblings (.-children parent)]
    (-> js/Array .-prototype .-indexOf (.call siblings node))))

(defn matches-selector?
  "Return true if `node` matches `sel`"
  [node sel]
  (.matches node sel))

(defn closest-ancestor
  "Return the earliest ancestor of the DOM `node` which matches `sel`,
   or return `nil` if none is found"
  [node sel]
  (loop [ancestor node]
    (if (or (nil? ancestor) (matches-selector? ancestor sel))
      ancestor
      (recur (.-parentElement ancestor)))))

; http://stackoverflow.com/a/8642069/1311454
(defn indicies [pred coll]
  "Return all indicies for which `pred` is true in `coll`"
  (keep-indexed #(when (pred %2) %1) coll))



(defn new-item []
  {:title       ""
   :completed?    false
   :new?          true
   :dragging-from nil})

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

(defn start-dragging-item! [item]
  (let [drag-start (key-in-parent item)]
    (om/transact! item #(assoc % :dragging-from drag-start))))

(defn drag-item-to! [item index]
  (move-item! item index))

(defn stop-dragging-item! [item]
  (if-let [drag-start (:dragging-from @item)]
    (move-item! item drag-start))
  (om/transact! item #(assoc % :dragging-from nil)))

(defn drop-item! [drag-item]
  (om/transact! item #(assoc % :dragging-from nil)))

(defn dragging-item? [item]
  (:dragging-from item))

(defn finalize-item! [item]
  (if (and (empty? (:title @item)) (not (:new? @item)))
    (remove-item! item)))

(defn drag-item-over-event! [over-item event]
  (let [swappable?    (d/has-class? (.-target event) "drag-swappable")
        items         (parent-cursor over-item)
        drag-item-key (first (indicies dragging-item? @items))
        drag-item     (child-cursor items drag-item-key)
        over-item-key (key-in-parent over-item)]
    (.preventDefault event)
    (when swappable?
      (drag-item-to! drag-item
                     over-item-key))))

(defn drag-item-leave-event! [over-item event]
  (let [items (parent-cursor over-item)
        drag-item-key (first (indicies dragging-item? @items))
        drag-item (child-cursor items drag-item-key)
        original-key (:dragging-from @drag-item)]
    (move! drag-item original-key)))

(defn drag-item-stop-event! [item event]
  #_(.preventDefault event)
  (stop-dragging-item! item))

(defn drop-item-event! [item event]
  (.preventDefault event)
  (drop-item! item))


(defn dbg [& args] (apply println args) (last args))
(defn list-item [item owner]
  (om/component
    (html
      (let [new?            (:new? item)
            draggable?      (not new?)
            complete?       (:completed? item)
            title           (:title item)
            dragging?       (dragging-item? item)
            dropzone?       dragging?
            drag-swappable? (and (not dragging?) (not new?))]
        [:li {:class     [(if new? "new")
                          (if complete? "complete")
                          (if dragging? "dragging")
                          (if dropzone? "dropzone")
                          (if drag-swappable? "drag-swappable")]
              :draggable draggable?
              :on-drag-start #(start-dragging-item! item)
              :on-drag-over  (partial drag-item-over-event! item)
              :on-drag-end   (partial drag-item-stop-event! item)
              :on-drop       (partial drop-item-event! item)}
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
