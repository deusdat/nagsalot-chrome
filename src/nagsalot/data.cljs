(ns nagsalot.data
  (:require [khroma.log :as console]
            [khroma.storage :as storage]))

(def really-far-out-there 253402232400000)

(defrecord Entry [url expr])

(defn entry [url]
  (->Entry url really-far-out-there))

(defn load []
  (console/log "Attempting to load configuration")
  (storage/get))

(defn save [options]
  (console/log "Attempting to save " options)
  ;; The lists should not be lists. They really need to be sets.
  (storage/set (-> options
                 (update-in [:approved] set)
                 (update-in [:blocked] set))))

(defn init[]
  (.addListener js/chrome.storage.onChange (fn [changes, area] (console/log "Something changed " changes))))