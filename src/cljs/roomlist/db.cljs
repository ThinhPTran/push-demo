(ns roomlist.db)


(def init-tableconfig {
                       :colHeaders ["" "Kia" "Nissian" "Toyota" "Honda"]
                       :data        [
                                     ;;["" "Kia" "Nissan" "Toyota" "Honda"]
                                     ["2008" 0 0 0 0]
                                     ["2009" 0 0 0 0]
                                     ["2010" 0 0 0 0]]
                       :rowHeaders  false
                       :contextMenu true})

(def default-db
  {:name "re-frame"})