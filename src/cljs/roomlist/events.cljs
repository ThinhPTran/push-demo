(ns roomlist.events
  (:require [re-frame.core :as re-frame]
            [roomlist.db :as mydb]))

(re-frame/reg-event-db
  :initialize-db
  (fn  [_ _]
    mydb/default-db))

(re-frame/reg-event-db
  :set-usernames
  (fn [db [_ data]]
    ;(println (str "data: " data))
    (assoc db :user/names data)))


