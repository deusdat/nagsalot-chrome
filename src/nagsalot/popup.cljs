(ns nagsalot.popup
  (:require [khroma.runtime :as runtime]
            [khroma.log :as console]
            [cljs.core.async :refer [>! <!]]
            [nagsalot.data :as data]
            [dommy.core :as dommy :refer-macros [sel1]]
            [khroma.tabs :refer [get-active]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn react-to-current-tab [reaction]
  (fn[e] 
    (let [tab (get-active)]
      (go (reaction (<! tab))))))

(defn button-react-to-tab 
  "Causes a button to react to the current tab. 
    id should be a keywork :#id, reaction should be an fn that takes a tab."
  [id reaction]
  (dommy/listen! (sel1 id) :click (react-to-current-tab reaction)))

(defn update-list [url list]
  (let [config-ch (data/load)] 
    (data/save (update-in {} [list] conj url))
    (console/log "Got past config read")))

(defn add-site-bind []
  (button-react-to-tab 
    :#approve-site 
    (fn [tab] 
      (update-list (:url tab)  :approved))))

(defn block-site-bind []
  (button-react-to-tab 
    :#block-site 
   (fn [tab] 
      (update-list (:url tab)  :blocked))))

(defn bind[]
  (add-site-bind)
  (block-site-bind ))

(defn init []
  (bind)
  (let [bg (runtime/connect)]
    (go (>! bg :lol-i-am-a-popup)
        (console/log "Background said: " (<! bg)))))
