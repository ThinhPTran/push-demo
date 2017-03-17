(ns roomlist.views
  (:require [reagent.core :as reagent]
            [reagent.ratom :as ratom]
            [re-frame.core :refer [subscribe dispatch]]))

(defn user-item [name]
  [:li (str (:user/name name))])

(defn users-list []
  (let [usernames @(subscribe [:user/names])]
    [:ul
     (cond
       (= nil usernames) [:li "There is no users"]
       :else (for [name usernames]
                ^{:key name} [user-item {:user/name name}]))]))

(defn main-panel []
  (let [name (subscribe [:name])]
    (fn []
      [:div "Hello from " @name ". Vui qua ta"]
      [users-list])))