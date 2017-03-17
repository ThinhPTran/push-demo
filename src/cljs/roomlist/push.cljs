(ns roomlist.push
  (:require [reagent.core :as r]
            [taoensso.sente :as sente :refer (cb-success?)]
            [roomlist.events]
            [re-frame.core :as re-frame]))

; generate a list of usernames
(def possible-usernames
  (let [first-names ["Happy" "Gleeful" "Joyful" "Cheerful" "Merry" "Jolly"]
        last-names ["Aardvark" "Turtle" "Woodchuck" "Cheetah" "Frog" "Hedgehog"]]
    (for [name1 first-names
          name2 last-names]
      (str name1 " " name2))))

; sente js setup
(def ws-connection (sente/make-channel-socket! "/channel" {:type :auto}))
(let [{:keys [ch-recv send-fn]}
      ws-connection] 
  (def receive-channel (:ch-recv ws-connection))
  (def send-channel! (:send-fn ws-connection)))

; handle application-specific events
(defn- app-message-received [[msgType data]]
  (case msgType
    :room/join (re-frame/dispatch [:set-usernames data]) ; (reset! events {:data data})
    (.log js/console "Unmatched application event")))

; handle websocket-connection-specific events
(defn- channel-state-message-received [state]
  (if (:first-open? state)
    (send-channel! [:room/ident {:name (rand-nth possible-usernames)}])))

; main router for websocket events
(defn- event-handler [[id data] _]
  (.log js/console "received message" data)
  (case id
    :chsk/state (channel-state-message-received data)
    :chsk/recv (app-message-received data)
    (.log js/console "Unmatched connection event")))

(sente/start-chsk-router-loop! event-handler receive-channel)
