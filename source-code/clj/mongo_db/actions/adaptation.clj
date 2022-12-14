
(ns mongo-db.actions.adaptation
    (:import  org.bson.types.ObjectId)
    (:require [json.api                   :as json]
              [mongo-db.core.helpers      :as core.helpers]
              [mongo-db.reader.adaptation :as reader.adaptation]
              [time.api                   :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document-id-input
  ; @param (string) document-id
  ;
  ; @example
  ; (document-id-input "MyObjectId")
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (org.bson.types.ObjectId object)
  [document-id]
  (reader.adaptation/document-id-input document-id))

(defn document-id-output
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @example
  ; (document-id-output #<ObjectId MyObjectId>)
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  [document-id]
  (if document-id (str document-id)))

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-input
  ; @param (namespaced map) document
  ;
  ; @example
  ; (insert-input {:namespace/id            "MyObjectId"
  ;                :namespace/my-keyword    :my-value
  ;                :namespace/your-string   "your-value"
  ;                :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentum :namespace/id tulajdonságának átnevezése :_id tulajdonságra
  ;    A dokumentum string típusú azonosítójának átalakítása objektum típusra
  ; 2. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 3. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document (core.helpers/id->_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn insert-output
  ; @param (namespaced map) document
  ;
  ; @example
  ; (insert-output {"_id"                     #<ObjectId MyObjectId>
  ;                 "namespace/my-keyword"    "*:my-value"
  ;                 "namespace/your-string"   "your-value"
  ;                 "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>})
  ; =>
  ; {:namespace/id            "MyObjectId"
  ;  :namespace/my-keyword    :my-value
  ;  :namespace/your-string   "your-value"
  ;  :namespace/our-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentumban használt string típusra alakított kulcsok és értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  ;    A dokumentum objektum típusú azonosítójának átalakítása string típusra
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-date-time (core.helpers/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
  ; @param (namespaced map) document
  ;
  ; @example
  ; (save-input {:namespace/id            "MyObjectId"
  ;              :namespace/my-keyword    :my-value
  ;              :namespace/your-string   "your-value"
  ;              :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentum :namespace/id tulajdonságának átnevezése :_id tulajdonságra
  ;    A dokumentum string típusú azonosítójának átalakítása objektum típusra
  ; 2. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 3. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document (core.helpers/id->_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn save-output
  ; @param (namespaced map) document
  ;
  ; @example
  ; (save-output {"_id"                     #<ObjectId MyObjectId>
  ;               "namespace/my-keyword"    "*:my-value"
  ;               "namespace/your-string"   "your-value"
  ;               "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>})
  ; =>
  ; {:namespace/id            "MyObjectId"
  ;  :namespace/my-keyword    :my-value
  ;  :namespace/your-string   "your-value"
  ;  :namespace/our-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentumban használt string típusra alakított kulcsok és értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  ;    A dokumentum objektum típusú azonosítójának átalakítása string típusra
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-date-time (core.helpers/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
  ; @param (namespaced map) document
  ;
  ; @example
  ; (update-input {:namespace/my-keyword    :my-value
  ;                :namespace/your-string   "your-value"
  ;                :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 2. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn update-query
  ; @param (map) query
  ;
  ; @return (map)
  [query]
  (reader.adaptation/find-query query))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
  ; @param (namespaced map) document
  ;
  ; @example
  ; (upsert-input {:namespace/my-keyword    :my-value
  ;                :namespace/your-string   "your-value"
  ;                :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  (update-input document))

;; -- Duplicating document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn duplicate-input
  ; @param (namespaced map) document
  ;
  ; @example
  ; (duplicate-input {:namespace/id            "MyObjectId"
  ;                   :namespace/my-keyword    :my-value
  ;                   :namespace/your-string   "your-value"
  ;                   :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentum :namespace/id tulajdonságának átnevezése :_id tulajdonságra
  ;    A dokumentum string típusú azonosítójának átalakítása objektum típusra
  ; 2. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 3. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document (core.helpers/id->_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn duplicate-output
  ; @param (namespaced map) document
  ;
  ; @example
  ; (insert-output {"_id"                     #<ObjectId MyObjectId>
  ;                 "namespace/my-keyword"    "*:my-value"
  ;                 "namespace/your-string"   "your-value"
  ;                 "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>})
  ; =>
  ; {:namespace/id            "MyObjectId"
  ;  :namespace/my-keyword    :my-value
  ;  :namespace/your-string   "your-value"
  ;  :namespace/our-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentumban használt string típusra alakított kulcsok és értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  ;    A dokumentum objektum típusú azonosítójának átalakítása string típusra
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-date-time (core.helpers/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))
