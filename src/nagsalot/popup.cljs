(ns nagsalot.popup
  (:require [khroma.runtime :as runtime]
            [khroma.log :as console]
            [cljs.core.async :refer [>! <!]]
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

(defn add-site-bind []
  (button-react-to-tab 
    :#approve-site 
    (fn [tab] 
      (js/alert (str "You want to approve " (:url tab))))))

(defn block-site-bind []
  (button-react-to-tab 
    :#block-site 
    (fn [tab] 
      (js/alert (str "You want to block " (:url tab))))))

(defn save-bind[]
  )

(defn cancel-bind[]
  )

(defn init []
  (add-site-bind)
  (block-site-bind )
  (save-bind)
  (cancel-bind)
  
  (let [bg (runtime/connect)]
    (go (>! bg :lol-i-am-a-popup)
        (console/log "Background said: " (<! bg)))))
