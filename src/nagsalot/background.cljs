(ns nagsalot.background
  (:require [khroma.log :as console]
            [khroma.runtime :as runtime]
            [cljs.core.async :refer [>! <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def block-list (atom [{:url "google.com", :expr 100000}]))

(defn should-block? [url]
  (some #(> (.indexOf url (:url %)) -1)
        @block-list))

(defn blocking-fn [details]
    (clj->js 
      (let [url (.-url  details)]
        (if (should-block? url)
          {:redirectUrl (js/chrome.extension.getURL "nope.html")}
          {:cancel false}))))

(defn bind-to-request []
  (.addListener js/chrome.webRequest.onBeforeRequest 
                blocking-fn
                (clj->js {:urls ["<all_urls>"]})
                (clj->js ["blocking"])))

(defn init []
  (bind-to-request)
  (go (let [conns (runtime/connections)
            content (<! conns)]
        (console/log "Content script said: " (<! content))
        (>! content :fml-i-am-the-background-script)
        (init))))
