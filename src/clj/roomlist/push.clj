(ns roomlist.push
  (:require [taoensso.sente :as sente]
            [clojure.tools.logging :as log]
            [datomic.api :as d :refer [db q]]))

;datomic setup
(defn create-db [url]
  (d/create-database url)
  (let [schema (read-string (slurp "resources/roomlist.edn"))
        conn (d/connect url)]
    (d/transact conn schema)
    {:db-connection conn
     :change-queue (d/tx-report-queue conn)}))

;sente setup, This function will be called whenever a new channel is open
(defn- get-user-id [request] 
  (str (java.util.UUID/randomUUID))) ;; Random user

(def ws-connection (sente/make-channel-socket! {:user-id-fn get-user-id}))
(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      ws-connection]
  (def ring-ws-post ajax-post-fn)
  (def ring-ws-handoff ajax-get-or-ws-handshake-fn)
  (def receive-channel ch-recv)
  (def channel-send! send-fn)
  (def connected-uids connected-uids))

;;; We can watch this atom for changes if we like
;(add-watch connected-uids :connected-uids
;           (fn [_ _ old new]
;             (when (not= old new)
;               (log/info "Connected uids change: %s" new))))

(defn- ws-msg-handler [db-connection]
  (fn [{:keys [event] :as msg} _]
    (let [[id data :as ev] event]
      (case id
        :room/ident @(d/transact db-connection
                                 [{:db/id #db/id[:db.part/user]
                                   :user/name (:name data)}])
        (log/warn "Unmatched event: " id)))))

(defn ws-message-router [db-connection]
  (sente/start-chsk-router-loop! (ws-msg-handler db-connection) 
                                 receive-channel))

(defn- read-changes [{:keys [db-after tx-data] :as report}]
  (q '[:find ?name
       :in $ [[?e ?a ?v]]
       :where [_ :user/name ?name]]
     db-after
     tx-data))

(defn change-monitor [change-queue]
  (log/info "starting monitor")
  (while true
    (log/info "monitor loop")
    (let [report (.take change-queue)
          rawdata (read-changes report)
          [[_ ?e ?v]] (:tx-data report)
          changes (vec (flatten (vec (into #{} rawdata))))]
      (log/warn "report: " report)
      (log/warn "_ : " _)
      (log/warn "?e: " ?e)
      (log/warn "?v: " ?v)
      (log/warn "rawdata: " rawdata)
      (log/warn "changes: " changes)
      (log/warn "connected-uids: " @connected-uids)
      (doseq [uid (:any @connected-uids)]
        (channel-send! uid [:room/join changes]))))
  (log/info "monitoring complete"))
