(ns nagsalot.background
  (:require 
    [ khroma.log :refer [log]]
    [cljs.pprint :refer [pprint]]
    [khroma.runtime :as runtime]
    [cljs.core.async :refer [>! <! take!]]
    [nagsalot.data :as data])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def block-list (atom []))

(defn prepend-wild [url-str]
  (if (.startsWith url-str "(.*)")
    url-str
    (str "(.*)" url-str)))

(defn append-wild [url-str]
  (if (.endsWith url-str ".*")
    url-str
    (str url-str ".*" )))

(defn create-pattern [url]
  (-> (:url url)
    (prepend-wild)
    (append-wild)
    (re-pattern)))

(defn should-block? [url]
  ( log "Block list" @block-list)
  (some #(re-matches (create-pattern %) url)
        @block-list))

(defn blocking-fn [details]
  (clj->js 
    (let [url (.-url  details)]
      (if (should-block? url)
        {:redirectUrl (js/chrome.extension.getURL "nope.html")}
        {:cancel false}))))

(defn bind-to-request []
  (when js/chrome.webRequest
    (.addListener js/chrome.webRequest.onBeforeRequest 
      blocking-fn
      (clj->js {:urls ["<all_urls>"]})
      (clj->js ["blocking"]))))

(defn react-to-allowance [url action]
  (log "Reacting to " url action)
  (if (= action "blocked")
    (swap! block-list #(conj % (data/entry url)))
    (swap! block-list (fn [list]
                        (remove #(= url (:url %)) list)))))

(defn ^:export init []
  (go
    (let [list (<! (data/load))]
      (swap! block-list #(:blocked list))))
  (.addListener js/chrome.runtime.onMessage 
    (fn [r s]
      (let [as-map (js->clj r :keywordize-keys true)
            url (get as-map "url")
            action (get as-map "action")]
        (log "Got  "  as-map)
        (react-to-allowance url action)))))

(bind-to-request)
