(ns nagsalot.data
  (:require [khroma.log :as console]))


(defn init[]
  (.addListener js/chrome.storage.onChange (fn [changes, area] (console/log "Something changed " changes))))