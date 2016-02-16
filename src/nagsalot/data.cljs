(ns nagsalot.data
  (:require [khroma.log :as console]
            [khroma.storage :as storage]))

(def really-far-out-there 253402232400000)
(def local js/chrome.storage.local)
(def domain-regex #"^(?:https?://)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/\n]+)")

(defn domain [url]
  (second (re-find domain-regex url)))

(defrecord Entry [url expr])


(defn now []
  (.getTime (js/Date.)))

(defn entry
  ([url]
    (entry url really-far-out-there))
  ([url expires]
    (->Entry url expires)))


(defn qc [msg obj]
  (console/log msg ": " obj)
  obj)

(defn only-active [v config]
  (let [before (now)]
    (console/log "before line " before)
    (qc "actives were " (filter #(> (:expr %) before) v))))

(defn mirror [list]
  (if (= :approved list) :blocked :approved))

(defn load []
  (console/log "Attempting to load configuration")
  (storage/get))

(defn save [options]
  (console/log "Attempting to save " options)
  ;; The lists should not be lists. They really need to be sets.
  (.set 
    local 
    (clj->js (-> options
               (update-in [:approved] 
                          #(set (only-active (:approved options) options)))
               (update-in [:blocked] set)))
    (fn [e]
      (console/log "value: " e)))
  (when runtime 
    (when runtime.lastError 
      (console/log "error was " runtime.lastError))))

(defn update-lists 
  ([config url target-list]
    (update-lists config url target-list really-far-out-there))
  ([config url target-list duration]
    (console/log "Attempting to update " config)
    (-> config
      (update-in [target-list] conj (entry url duration))
      (update-in [(mirror target-list)] #(remove (fn [v] (= (:url v) %2)) %1) url)
      (save))))

(defn init[]
  (when js/chrome.storage
    (when js/chrome.storage.onChanged
      (.addListener js/chrome.storage.onChanged (fn [changes, area] (console/log "Something changed " changes))))))