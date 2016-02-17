(ns nagsalot.settings
  (:require  [nagsalot.data :as data]
             [khroma.log :as console]
             [cljs.core.async :refer [>! <!]]
             [dommy.core :as dommy :refer-macros [sel1]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def form (atom nil))

(declare add-to-list bind-form!)

(defn update-form [prop value]
  (reset! form (assoc @form prop value))
 (data/save @form))

(defn add-from-ui! [e]
  (let [ ev-elem (.-target e)
        k-prop (keyword (dommy/attr ev-elem :data-list))
        mirrored-prop (data/mirror k-prop)
        text-box (sel1 (dommy/parent ev-elem) :.addable)
        url-value (dommy/value text-box)]
    (if url-value 
      (do 
        (update-form k-prop (conj (get @form k-prop) (data/entry url-value)))
         (update-form mirrored-prop (filter #(not= (:url %) url-value) (get @form mirrored-prop)))
        (.sendMessage js/chrome.runtime (clj->js {:url url-value, :action k-prop}))
        (dommy/set-value! text-box "")
        (bind-form!)) nil)))

(defn enable-add [id]
  (-> (sel1 id)
    (dommy/unlisten! :click add-from-ui!)
    (dommy/listen! :click add-from-ui!)))

(defn build-url-column! [list-name url]
  (-> (dommy/create-element :td)
    (dommy/append! (-> (dommy/create-element :div)
                                                   (dommy/set-attr! :style "overflow: auto; width:100%")
                                                    (dommy/append! (-> (dommy/create-text-node url)))))))

(defn remove-from-ui! [e]
  (let [elem (.-target e)
             list-attr (keyword (dommy/attr elem :data-list))
             url-attr (dommy/attr elem :data-url)
             l (get @form list-attr)]
    (update-form list-attr (filter #(not= (:url %) url-attr) l))
    (bind-form!)))

(defn build-url-edit! [list-name url]
  (-> (dommy/create-element :td)
         (dommy/append!  (-> (dommy/create-element :img)
                                                         (dommy/unlisten! :click remove-from-ui!)
                                                         (dommy/listen! :click remove-from-ui!)
                                                         (dommy/set-attr! :src "ic_clear_black_24dp_1x.png" 
                                                                                                 :data-url url
                                                                                                 :data-list (name list-name))))))

(defn build-row! [list-name url]
  (-> (dommy/create-element :tr)
    (dommy/append! (build-url-column! list-name url))
    (dommy/append! (build-url-edit! list-name url))))

(defn build-table! [list-name config]
  (let [table (sel1 (keyword (str "#" (name list-name) "-table")))]
    (dommy/clear! table)
    (reduce #(dommy/append! %1 (build-row! list-name (:url %2))) table (get config list-name))))


(defn bind-approved! [config]
  (enable-add :#nags-a-lot-approve-site)
  (build-table! :approved config))

(defn bind-blocked! [config]
  (enable-add :#nags-a-lot-block-site)
  (build-table! :blocked config))

(defn bind-name! [config]
  (let [name-elem  (sel1 :#user-name)]
    (-> name-elem
      (dommy/set-value! (:name config ""))
      (dommy/listen! :blur #(update-form :name (dommy/value name-elem))))))

(defn bind-form! []
  (go 
    (-> (or @form (<! (data/load)))
      ((juxt bind-name! 
             bind-blocked! 
             bind-approved!
             #(reset! form %))))))

(defn add-to-list [list value]
  (->> (data.entry value)
    (conj (list @form))
    (update-form list))
  (bind-form!))

(defn init[] 
  (bind-form! ))