(ns nagsalot.data
  (:require [khroma.log :as console]
            [khroma.storage :as storage]))

(def really-far-out-there 253402232400000)
(def local js/chrome.storage.local)

(defrecord Entry [url expr])

(defn entry [url]
  (->Entry url really-far-out-there))

(defn load []
  (console/log "Attempting to load configuration")
  (storage/get))

(defn save [options]
  (console/log "Attempting to save " options)
  ;; The lists should not be lists. They really need to be sets.
  (.set 
    local 
    (clj->js (-> options
               (update-in [:approved] set)
               (update-in [:blocked] set)))
    (fn [e]
      (console/log "value: " e)))
  (when runtime 
    (when runtime.lastError 
      (console/log "error was " runtime.lastError))))

(defn init[]
  (when js/chrome.storage
    (when js/chrome.storage.onChanged
      (.addListener js/chrome.storage.onChanged (fn [changes, area] (console/log "Something changed " changes))))))