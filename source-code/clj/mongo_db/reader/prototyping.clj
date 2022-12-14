
(ns mongo-db.reader.prototyping
    (:require [candy.api :refer [return]]))

;; -- Find document -----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-output
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [document {:keys [prototype-f] :as options}]
  (try (if prototype-f (prototype-f document)
                       (return      document))
       (catch Exception e (println (str e "\n" {:document document :options options})))))
