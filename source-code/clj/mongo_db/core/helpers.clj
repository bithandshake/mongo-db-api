
(ns mongo-db.core.helpers
    (:import  org.bson.types.ObjectId)
    (:require [candy.api            :refer [return]]
              [keyword.api          :as keyword]
              [map.api              :as map]
              [monger.conversion    :as mcv]
              [monger.core          :as mcr]
              [mongo-db.core.errors :as core.errors]
              [re-frame.api         :as r]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn command
  ; @param (map) options
  ;  {:warn? (boolean)(opt)
  ;    Default: true}
  ;
  ; @return (DBObject)
  [{:keys [warn?] :or {warn? true} :as options}]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcr/command database options)
            (catch Exception e (if warn? (println (str e "\n" {:options options})))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn operator?
  ; @param (*) n
  ;
  ; @example
  ; (operator? :$or)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [n]
  (and (keyword? n)
       (->       n second str (= "$"))))

(defn document?
  ; @param (*) n
  ;
  ; @example
  ; (document? {:namespace/my-key "..."})
  ; =>
  ; false
  ;
  ; @example
  ; (document? {:namespace/my-key "..."
  ;             :namespace/id     "..."})
  ; =>
  ; true
  ;
  ; @return (boolean)
  [n]
  (and (-> n map?)
       (if-let [namespace (map/get-namespace n)]
               (get n (keyword/add-namespace namespace :id)))))

(defn DBObject->edn
  ; @param (DBObject) n
  ;
  ; @return (map)
  [n]
  (try (mcv/from-db-object n true)
       (catch Exception e (println e))))

;; -- Document ID -------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-id
  ; @usage
  ; (mongo-db/generate-id)
  ;
  ; @return (string)
  []
  (str (ObjectId.)))

(defn assoc-id
  ; @param (map) n
  ;
  ; @example
  ; (assoc-id {:namespace/my-key "my-value"})
  ; =>
  ; {:namespace/id "MyObjectId" :namespace/my-key "my-value"}
  ;
  ; @return (map)
  [n]
  (if-let [namespace (map/get-namespace n)]
          (let [document-id (generate-id)]
               (assoc n (keyword/add-namespace namespace :id) document-id))
          (return n)))

(defn dissoc-id
  ; @param (map) n
  ; {:namespace/id (string)(opt)}
  ;
  ; @example
  ; (dissoc-id {:namespace/id "MyObjectId" :namespace/my-key "my-value"})
  ; =>
  ; {:namespace/my-key "my-value"}
  ;
  ; @return (map)
  [n]
  (if-let [namespace (map/get-namespace n)]
          (dissoc n (keyword/add-namespace namespace :id))
          (return n)))

(defn id->_id
  ; @param (map) n
  ; {:namespace/id (*)(opt)}
  ; @param (map)(opt) options
  ; {:parse? (boolean)(opt)
  ;   Default: false}
  ;
  ; @example
  ; (id->_id {:namespace/id 1})
  ; =>
  ; {:_id 1}
  ;
  ; @example
  ; (id->_id {:namespace/id "MyObjectId"} {:parse? true})
  ; =>
  ; {:_id #<ObjectId MyObjectId>}
  ;
  ; @return (map)
  ; {:_id (org.bson.types.ObjectId object)}
  ([n]
   (id->_id n {}))

  ([n {:keys [parse?]}]
   ; The n map (given as a parameter) doesn't have to contain the :namespace/id key!
   (if-let [namespace (map/get-namespace n)]
           (let [id-key (keyword/add-namespace namespace :id)]
                (if-let [value (get n id-key)]
                        (if parse? (let [object-id (ObjectId. value)]
                                        (-> n (assoc  :_id object-id)
                                              (dissoc id-key)))
                                   (-> n (assoc  :_id value)
                                         (dissoc id-key)))
                        (return n)))
           (return n))))

(defn _id->id
  ; @param (map) n
  ; {:_id (*)(opt)}
  ; @param (map)(opt) options
  ; {:unparse? (boolean)(opt)
  ;   Default: false}
  ;
  ; @example
  ; (_id->id {:_id #<ObjectId MyObjectId>} {:unparse? true})
  ; =>
  ; {:namespace/id "MyObjectId"}
  ;
  ; @return (map)
  ; {:namespace/id (string)}
  ([n]
   (_id->id n {}))

  ([n {:keys [unparse?]}]
   ; The n map (given as a parameter) doesn't have to contain the :_id key!
   (if-let [namespace (map/get-namespace n)]
           (let [id-key (keyword/add-namespace namespace :id)]
                (if-let [value (get n :_id)]
                        (if unparse? (let [document-id (str value)]
                                          (-> n (assoc  id-key document-id)
                                                (dissoc :_id)))
                                     (-> n (assoc  id-key value)
                                           (dissoc :_id)))
                        (return n)))
           (return n))))

(defn id->>_id
  ; @param (*) n
  ; {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ; {:parse? (boolean)(opt)
  ;   Default: false}
  ;
  ; @example
  ; (id->>_id {:$or [{...} {:namespace/id "MyObjectId"}]})
  ; =>
  ; {:$or [{...} {:_id #<ObjectId MyObjectId>}]}
  ;
  ; @return (map)
  ; {:_id (org.bson.types.ObjectId object)}
  ([n]
   (id->>_id n {}))

  ([n options]
   (cond (map?    n) (reduce-kv #(assoc %1 %2 (id->>_id %3 options)) {} (id->_id n options))
         (vector? n) (reduce    #(conj  %1    (id->>_id %2 options)) []          n)
         :return  n)))

;; -- Document order ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document->order
  ; @param (namespaced map) document
  ;
  ; @example
  ; (document->order {:namespace/order 3})
  ; =>
  ; 3
  ;
  ; @return (integer)
  [document]
  (if-let [namespace (map/get-namespace document)]
          (get document (keyword/add-namespace namespace :order))
          (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR))))
