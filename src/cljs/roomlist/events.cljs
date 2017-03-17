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

(defn updatetable [tableconfig changeData]
  (let [dataTable (get-in tableconfig [:data] (:data mydb/init-tableconfig))
        newDataTable (assoc-in dataTable (subvec changeData 0 2) (js/parseFloat (nth changeData 3)))
        tableconfig (assoc-in tableconfig [:data] newDataTable)]
    tableconfig))


(re-frame/reg-event-db
  :set-tablevalue
  (fn [db [_ inchangeDatas]]
    (let [changeDatas (js->clj inchangeDatas)
          tableconfig (get-in db [:tableconfig] mydb/init-tableconfig)
          newtableconfig (if (nil? changeDatas) nil (reduce updatetable tableconfig changeDatas))
          dbout (if (nil? newtableconfig) db (assoc db :tableconfig newtableconfig))]
      (if (some? changeDatas) (do
                                (println "set-tablevalue")
                                (println changeDatas)))
                                ;(println dbout)))
      dbout)))
