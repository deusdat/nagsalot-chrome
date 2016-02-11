(ns nagsalot.data
  (:require [khroma.log :as console]))

(def local js/chrome.storage.local)

(defn load  [])

(defn save 
  ([config]
    (save config 
          (fn[] 
            (console/log "completed save " (.-lastError js/chrome.runtime)))))
  ([config on-complete]
    (let [js-obj (clj->js config)]
       (.set local js-obj on-complete))
    (.get local nil (fn[t] (console/log "Loaded " t )))))

(defn init[]
  (.addListener js/chrome.storage.onChange (fn [changes, area] (console/log "Something changed " changes))))