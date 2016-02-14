(ns nagsalot.popup
  (:require [khroma.runtime :as runtime]
            [khroma.log :as console]
            [cljs.core.async :refer [>! <!]]
            [nagsalot.data :as data]
            [dommy.core :as dommy :refer-macros [sel1]]
            [khroma.tabs :refer [get-active]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def domain-regex #"^(?:https?://)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/\n]+)")

(defn react-to-current-tab [reaction]
  (fn[e] 
    (let [tab (get-active)]
      (go (reaction (<! tab))))))

(defn button-react-to-tab 
  "Causes a button to react to the current tab. 
    id should be a keywork :#id, reaction should be an fn that takes a tab."
  [id reaction]
  (dommy/listen! (sel1 id) :click (react-to-current-tab reaction)))

(defn mirror [list]
  (if (= :approved list) :blocked :approved))

(defn update-list [domain list]
  (let [config-ch (data/load)] 
    (.sendMessage js/chrome.runtime (clj->js {:url domain, :action list}))
    (go 
      (let [mirrored (mirror list)
            config (<! config-ch)]
        (console/log "about to modify " config)
        (-> config
          (update-in [list] conj (data/entry domain))
          (update-in [mirrored] #(remove (fn [v] (= (:url v) %2)) %1) domain)
          (data/save))
        ;;(.close js/window)
        ))))

(defn domain [url]
  (second (re-find domain-regex url)))

(defn add-site-bind []
  (button-react-to-tab 
    :#approve-site 
    (fn [tab] 
      (update-list (domain (:url tab)) :approved))))

(defn block-site-bind []
  (button-react-to-tab 
    :#block-site 
   (fn [tab] 
     (let [url (:url tab)]
       (update-list (domain url) :blocked)))))

(defn bind[]
  (add-site-bind)
  (block-site-bind ))

(defn init []
  (let [bg (runtime/connect)]
    (go (>! bg :lol-i-am-a-popup)
        (bind)
        (console/log "Background said: " (<! bg)))))
