(ns roomlist.views
  (:require [reagent.core :as reagent]
            [reagent.ratom :as ratom]
            [re-frame.core :refer [subscribe dispatch]]))

(defn sampleTable [tableconfig]
  (let [table (atom nil)]
    (reagent/create-class {:reagent-render
                           (fn [] [:div {:style {:min-width "310px" :max-width "800px" :margin "0 auto"}}])
                           :component-did-mount
                           (fn [this]
                             (do
                                (reset! table (js/Handsontable (reagent/dom-node this) (clj->js tableconfig)))))
                           :should-component-update
                           (fn [this [_ old-config] [_ new-config]]
                             (.destroy @table)
                             (reset! table (js/Handsontable (reagent/dom-node this) (clj->js tableconfig)))
                             true)})))

(defn sampleTableWrapper []
  (let [tableconfig (subscribe [:tableconfig])
        usernames (subscribe [:user/names])]
    [:div "My Table"
     [sampleTable @tableconfig @usernames]]))

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
      [:div "Hello from " @name ". Vui qua ta"
       [users-list]
       [sampleTableWrapper]])))