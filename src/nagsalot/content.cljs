(ns nagsalot.content
  (:require [khroma.runtime :as runtime]
            [khroma.log :as console]
            [cljs.core.async :refer [>! <!]]
            [dommy.utils :as utils]
            [dommy.core :as dommy])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.core :refer [sel sel1]]))
(def modal-id "nags-a-lot-modal")


(defn allow []
  (console/log "Attempting to hide the modal")
  (-> (sel1 :body)
      (dommy/remove! (sel1 (str "#" modal-id)))))

(def yes-button (-> (dommy/create-element "button")
                    (dommy/set-text! "Yes")
                    (dommy/listen! :click allow)))

(def no-button (-> (dommy/create-element "button")
                    (dommy/set-text! "No")))

(def button-wrapper (-> (dommy/create-element "div")
                        (dommy/append! yes-button)
                        (dommy/append! no-button)))

(def modal-content (-> (dommy/create-element "div")
                       (dommy/set-attr! :id "nags-a-lot-content")
                       (dommy/set-text! "Do you really need to go here?")
                       (dommy/append! button-wrapper)))

(def modal (-> (dommy/create-element "div")
               (dommy/set-attr! :id modal-id)
               (dommy/append! modal-content)))

(defn init []
  (let [bg (runtime/connect)]
    (go (>! bg :lol-i-am-a-content-script)
        (console/log "Background said: " (<! bg))))
  (console/log modal)
  (-> (sel1 :body)
      (dommy/append! modal))
  (console/log "Hello from the content script"))