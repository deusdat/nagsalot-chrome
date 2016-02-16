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


(defn update-list [domain list]
  (let [config-ch (data/load)] 
    (.sendMessage js/chrome.runtime (clj->js {:url domain, :action list}))
    (go 
      (let [config (<! config-ch)]
        (data/update-lists config domain list)
        (.close js/window)))))


(defn reload [tab]
   (.reload js/chrome.tabs (:id tab)))

(defn add-site-bind []
  (button-react-to-tab 
    :#approve-site 
    (fn [tab] 
      (update-list (data/domain (:url tab)) :approved)
      (reload tab))))

(defn block-site-bind []
  (button-react-to-tab 
    :#block-site 
   (fn [tab] 
     (let [url (:url tab)]
       (update-list (data/domain url) :blocked)
       (reload tab)))))

(defn bind[]
  (add-site-bind)
  (block-site-bind ))

(defn init []
  (let [bg (runtime/connect)]
    (go (>! bg :lol-i-am-a-popup)
        (bind)
        (console/log "Background said: " (<! bg)))))
