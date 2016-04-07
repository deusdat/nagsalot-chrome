(ns nagsalot.background
  (:require [khroma.log :as console]
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
  (if (.endsWith url-str "(.*)")
    url-str
    (str url-str "(.*)" )))
(defn pass-print [ob]
  (console/log "obj was " ob)
  ob)

(defn create-pattern [url]
 (-> (:url url)
  (prepend-wild)
  (append-wild)
  (pass-print)
  (re-pattern)))

(defn should-block? [url]
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
  (if (= action "blocked")
    (swap! block-list #(conj % (data/entry url)))
    (swap! block-list (fn [list]
                        (remove #(= url (:url %)) list)))))

(defn ^:export init []
  (go
    (let [list (<! (data/load))]
      (console/log "blocked list " list)
      (swap! block-list #(:blocked list))))
  (.addListener js/chrome.runtime.onMessage 
    (fn [r s]
      (console/log "got " r " " s)
      (let [as-map (js->clj r :keywordize-keys true)
            url (:url as-map)
            action (:action as-map)]
        (console/log "action was " action)
        (react-to-allowance url action)
        (console/log "list is " @block-list)))))


(bind-to-request)
