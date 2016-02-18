(ns nagsalot.content
  (:require [khroma.runtime :as runtime]
            [khroma.log :as console]
            [cljs.core.async :refer [>! <!]]
            [dommy.utils :as utils]
            [nagsalot.data :as data]
            [dommy.core :as dommy])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.core :refer [sel sel1]]))

(def modal-id "nags-a-lot-modal")

(defn clear-modal []
  (-> (sel1 :html)
    (dommy/remove-class! :stop-scrolling))
  (-> (sel1 :body)
    (dommy/remove! (sel1 (str "#" modal-id)))))


(defn allow []
  (go (let [config (<! (data/load))
            timeout (+ (* 60000 (:timeout config 10)) (data/now))] 
        (data/update-lists config (data/domain window.location.href) :approved timeout)))
  (clear-modal))

(defn decline []
  (set! js/window.location.href (js/chrome.extension.getURL "opt-out.html")))

(def top-hat (-> (dommy/create-element :img)
              (dommy/set-attr! :src (js/chrome.extension.getURL "top-hat1-small.png") 
                                                      :style "margin: auto; display: block; width: auto")))

(def yes-button (-> (dommy/create-element :div)
                  (dommy/set-text! "Yes")
                  (dommy/set-attr! :style "display: inline-block; width: 48%; background-color: white; text-align: center")
                  (dommy/add-class! :nags-a-lot-button)
                  (dommy/listen! :click allow)))

(def no-button (-> (dommy/create-element :div)
                 (dommy/set-text! "No")
                  (dommy/set-attr! :style "display: inline-block; width: 48%; background-color: white; text-align: center")
                 (dommy/add-class! :nags-a-lot-button)
                 (dommy/listen! :click decline)))

(def button-wrapper (-> (dommy/create-element "div")
                      (dommy/append! yes-button)
                      (dommy/append! no-button)))

(def modal-content (-> (dommy/create-element "div")
                     (dommy/set-attr! :id "nags-a-lot-content"
                                      :style "verticle-align: middle")
                     (dommy/append! top-hat)
                     (dommy/append! (->  (dommy/create-element :h2)
                                      (dommy/set-text! "Do you really need to go here?")))
                     (dommy/append! button-wrapper)))

(def modal (-> (dommy/create-element "div")
             (dommy/set-attr! :id modal-id)
             (dommy/append! modal-content)))

(defn should-inquire? [approved-list config]
  (not (some #(re-matches (re-pattern (str "(.*)" (:url %) "(.*)")) 
                          document.location.href) 
             (data/only-active approved-list config))))

(defn attach-modal []
  (go (let [config (<! (data/load))
            approved-list (:approved config)]
        (when (should-inquire? approved-list config)
          (-> (sel1 :html)
            (dommy/add-class! :stop-scrolling))
          (-> (sel1 :body)
            (dommy/append! modal))))))

(defn init []
  (let [bg (runtime/connect)]
    (go (>! bg :lol-i-am-a-content-script)
        (console/log "Background said: " (<! bg))))
  (attach-modal))