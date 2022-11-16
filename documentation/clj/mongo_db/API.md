
# <strong>mongo-db.api</strong> namespace
<p>Documentation of the <strong>mongo_db/api.clj</strong> file</p>

<strong>[README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > mongo-db.api</strong>



### add-fields-query

```
@param (map) field-pattern
```

```
@example
(add-fields-query {:namespace/name  {:$concat [:$namespace/first-name " " :$namespace/last-name]}
                   :namespace/total {:$sum     :$namespace/all-result}})
=>
{"namespace/name"  {"$concat" ["$namespace/first-name" " " "$namespace/last-name"]}
 "namespace/total" {"$sum"     "$namespace/all-result"}}}
```

```
@return (map)
```

<details>
<summary>Source code</summary>

```
(defn add-fields-query
  [field-pattern]
  (map/->>kv field-pattern json/unkeywordize-key json/unkeywordize-key))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [add-fields-query]]))

(mongo-db/add-fields-query ...)
(add-fields-query          ...)
```

</details>

---

### apply-document!

```
@param (string) collection-name
@param (string) document-id
@param (function) f
@param (map)(opt) options
```

```
@usage
(apply-document! "my_collection" "MyObjectId" #(assoc % :namespace/color "Blue") {...})
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn apply-document!
  ([collection-name document-id f]
   (apply-document! collection-name document-id f {}))

  ([collection-name document-id f options]
   (if-let [document (reader/get-document-by-id collection-name document-id)]
           (if-let [document (preparing/apply-input collection-name document options)]
                   (if-let [document (f document)]
                           (if-let [document (postparing/apply-input collection-name document options)]
                                   (if-let [document (adaptation/save-input document)]
                                           (let [result (save-and-return! collection-name document)]
                                                (adaptation/save-output result)))))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [apply-document!]]))

(mongo-db/apply-document! ...)
(apply-document!          ...)
```

</details>

---

### apply-documents!

```
@param (string) collection-name
@param (function) f
@param (map)(opt) options
```

```
@usage
(apply-document! "my_collection" #(assoc % :namespace/color "Blue") {...})
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn apply-documents!
  ([collection-name f]
   (apply-documents! collection-name f {}))

  ([collection-name f options]
   (if-let [collection (reader/get-collection collection-name)]
           (letfn [(fi [result document]
                       (if-let [document (f document)]
                               (let [document (save-document! collection-name document options)]
                                    (conj result document))
                               (return result)))]
                  (reduce fi [] collection)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [apply-documents!]]))

(mongo-db/apply-documents! ...)
(apply-documents!          ...)
```

</details>

---

### collection-empty?

```
@param (string) collection-name
```

```
@usage
(collection-empty? "my_collection")
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn collection-empty?
  [collection-name]
  (= 0 (count-documents collection-name)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [collection-empty?]]))

(mongo-db/collection-empty? ...)
(collection-empty?          ...)
```

</details>

---

### count-documents-by-pipeline

```
@param (string) collection-name
@param (maps in vector) pipeline
```

```
@usage
(count-documents-by-pipeline "my_collection" [...])
```

```
@usage
(count-documents-by-pipeline "my_collection" (count-pipeline {...}))
```

```
@return (integer)
```

<details>
<summary>Source code</summary>

```
(defn count-documents-by-pipeline
  [collection-name pipeline]
  (if-let [documents (aggregation/process collection-name pipeline)]
          (count  documents)
          (return 0)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [count-documents-by-pipeline]]))

(mongo-db/count-documents-by-pipeline ...)
(count-documents-by-pipeline          ...)
```

</details>

---

### count-pipeline

```
@param (map) pipeline-props
```

```
@usage
(count-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
                 :filter-pattern {:namespace/my-keyword :my-value
                                  :$or [{:namespace/my-boolean   false}
                                        {:namespace/my-boolean   nil}]}
                 :search-pattern {:$or [{:namespace/my-string   "My value"}]
                                        {:namespace/your-string "Your value"}]}})
```

```
@return (maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn count-pipeline
  [{:keys [field-pattern filter-pattern search-pattern]}]
  (cond-> [] field-pattern (conj {"$addFields"      (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query filter-pattern)
                                                    (search-query search-pattern)]}})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [count-pipeline]]))

(mongo-db/count-pipeline ...)
(count-pipeline          ...)
```

</details>

---

### document-exists?

```
@param (string) collection-name
@param (string) document-id
```

```
@usage
(document-exists? "my_collection" "MyObjectId")
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn document-exists?
  [collection-name document-id]
  (boolean (if-let [document-id (adaptation/document-id-input document-id)]
                   (find-map-by-id collection-name document-id))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [document-exists?]]))

(mongo-db/document-exists? ...)
(document-exists?          ...)
```

</details>

---

### duplicate-document!

```
@param (string) collection-name
@param (string) document-id
@param (map)(opt) options
```

```
@example
(duplicate-document! "my_collection" "MyObjectId" {...})
=>
{:namespace/id "MyObjectId" :namespace/label "My document"}
```

```
@example
(duplicate-document! "my_collection" "MyObjectId" {:label-key :namespace/label})
=>
{:namespace/id "MyObjectId" :namespace/label "My document #2"}
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn duplicate-document!
  ([collection-name document-id]
   (duplicate-document! collection-name document-id {}))

  ([collection-name document-id {:keys [ordered?] :as options}]
   (if ordered? (duplicate-ordered-document!   collection-name document-id options)
                (duplicate-unordered-document! collection-name document-id options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [duplicate-document!]]))

(mongo-db/duplicate-document! ...)
(duplicate-document!          ...)
```

</details>

---

### duplicate-documents!

```
@param (string) collection-name
@param (strings in vector) document-ids
@param (map)(opt) options
```

```
@example
(duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
=>
[{...} {...}]
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn duplicate-documents!
  ([collection-name document-ids]
   (duplicate-documents! collection-name document-ids {}))

  ([collection-name document-ids options]
   (vector/->items document-ids #(duplicate-document! collection-name % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [duplicate-documents!]]))

(mongo-db/duplicate-documents! ...)
(duplicate-documents!          ...)
```

</details>

---

### filter-query

```
@param (map) filter-pattern
```

```
@example
(filter-query {:namespace/my-keyword :my-value
               :$or  [{:namespace/my-boolean   false}
                      {:namespace/my-boolean   nil}]
               :$and [{:namespace/your-boolean true}]})
=>
{"namespace/my-keyword" "*:my-value"
 "$or"  [{"namespace/my-boolean"   false}
         {"namespace/my-boolean"   nil}]
 "$and" [{"namespace/your-boolean" true}]}
```

```
@return (maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn filter-query
  [filter-pattern]
  (adaptation/find-query filter-pattern))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [filter-query]]))

(mongo-db/filter-query ...)
(filter-query          ...)
```

</details>

---

### generate-id

```
@usage
(mongo-db/generate-id)
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn generate-id
  []
  (str (ObjectId.)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [generate-id]]))

(mongo-db/generate-id)
(generate-id)
```

</details>

---

### get-all-document-count

```
@param (string) collection-name
```

```
@usage
(get-all-document-count "my_collection")
```

```
@return (integer)
```

<details>
<summary>Source code</summary>

```
(defn get-all-document-count
  [collection-name]
  (count-documents collection-name))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-all-document-count]]))

(mongo-db/get-all-document-count ...)
(get-all-document-count          ...)
```

</details>

---

### get-collection

```
@param (string) collection-name
@param (namespaced map)(opt) projection
```

```
@example
(get-collection "my_collection" {:namespace/my-keyword  0
                                 :namespace/your-string 1})
=>
[{:namespace/my-keyword  :my-value
  :namespace/your-string "Your value"
  :namespace/id          "MyObjectId"}]
```

```
@return (maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-collection
  ([collection-name]
   (if-let [collection (find-maps collection-name {})]
           (vector/->items collection #(adaptation/find-output %))))

  ([collection-name projection]
   (if-let [projection (adaptation/find-projection projection)]
           (if-let [collection (find-maps collection-name {} projection)]
                   (vector/->items collection #(adaptation/find-output %))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-collection]]))

(mongo-db/get-collection ...)
(get-collection          ...)
```

</details>

---

### get-collection-names

```
@usage
(get-collection-names)
```

```
@return (strings in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-collection-names
  []
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (-> database mdb/get-collection-names vec)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-collection-names]]))

(mongo-db/get-collection-names)
(get-collection-names)
```

</details>

---

### get-collection-namespace

```
@param (string) collection-name
```

```
@usage
(get-collection-namespace "my_collection")
```

```
@return (keyword)
```

<details>
<summary>Source code</summary>

```
(defn get-collection-namespace
  [collection-name]
  (let [collection (find-maps collection-name {})]
       (-> collection first map/get-namespace)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-collection-namespace]]))

(mongo-db/get-collection-namespace ...)
(get-collection-namespace          ...)
```

</details>

---

### get-document-by-id

```
@param (string) collection-name
@param (string) document-id
@param (namespaced map)(opt) projection
```

```
@example
(get-document-by-id "my_collection" "MyObjectId" {:namespace/my-keyword  0
                                                  :namespace/your-string 1})
=>
{:namespace/your-string "Your value"
 :namespace/id          "MyObjectId"}
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn get-document-by-id
  ([collection-name document-id]
   (if-let [document-id (adaptation/document-id-input document-id)]
           (if-let [document (find-map-by-id collection-name document-id)]
                   (adaptation/find-output document))))

  ([collection-name document-id projection]
   (if-let [document-id (adaptation/document-id-input document-id)]
           (if-let [projection (adaptation/find-projection projection)]
                   (if-let [document (find-map-by-id collection-name document-id projection)]
                           (adaptation/find-output document))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-document-by-id]]))

(mongo-db/get-document-by-id ...)
(get-document-by-id          ...)
```

</details>

---

### get-document-by-query

```
@param (string) collection-name
@param (map) query
@param (namespaced map)(opt) projection
```

```
@usage
(get-document-by-query "my_collection" {:namespace/my-keyword :my-value})
```

```
@usage
(get-document-by-query "my_collection" {:$or [{...} {...}]})
```

```
@example
(get-document-by-query "my_collection" {:namespace/my-keyword :my-value}
                                       {:namespace/my-keyword  0
                                        :namespace/your-string 1})
=>
{:namespace/your-string "Your value"
 :namespace/id          "MyObjectId"}
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn get-document-by-query
  ([collection-name query]
   (if-let [query (-> query checking/find-query adaptation/find-query)]
           (if-let [document (find-one-as-map collection-name query)]
                   (adaptation/find-output document))))

  ([collection-name query projection]
   (if-let [query (-> query checking/find-query adaptation/find-query)]
           (if-let [projection (adaptation/find-projection projection)]
                   (if-let [document (find-one-as-map collection-name query projection)]
                           (adaptation/find-output document))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-document-by-query]]))

(mongo-db/get-document-by-query ...)
(get-document-by-query          ...)
```

</details>

---

### get-document-count-by-query

```
@param (string) collection-name
@param (map) query
```

```
@usage
(get-document-count-by-query "my_collection" {:namespace/my-keyword :my-value})
```

```
@usage
(get-document-count-by-query "my_collection" {:$or [{...} {...}]})
```

```
@usage
(get-document-count-by-query "my_collection" {:namespace/my-keyword  :my-value}
                                              :namespace/your-string "Your value"})
```

```
@return (integer)
```

<details>
<summary>Source code</summary>

```
(defn get-document-count-by-query
  [collection-name query]
  (if-let [query (-> query checking/find-query adaptation/find-query)]
          (count-documents-by-query collection-name query)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-document-count-by-query]]))

(mongo-db/get-document-count-by-query ...)
(get-document-count-by-query          ...)
```

</details>

---

### get-documents-by-pipeline

```
@param (string) collection-name
@param (maps in vector) pipeline
```

```
@usage
(get-documents-by-pipeline "my_collection" [...])
```

```
@usage
(get-documents-by-pipeline "my_collection" (get-pipeline {...}))
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-documents-by-pipeline
  [collection-name pipeline]
  (if-let [documents (aggregation/process collection-name pipeline)]
          (vector/->items documents #(adaptation/find-output %))
          (return [])))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-documents-by-pipeline]]))

(mongo-db/get-documents-by-pipeline ...)
(get-documents-by-pipeline          ...)
```

</details>

---

### get-documents-by-query

```
@param (string) collection-name
@param (map) query
@param (namespaced map)(opt) projection
```

```
@usage
(get-documents-by-query "my_collection" {:namespace/my-keyword :my-value})
```

```
@usage
(get-documents-by-query "my_collection" {:$or [{...} {...}]})
```

```
@example
(get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
                                        {:namespace/my-keyword  0
                                         :namespace/your-string 1})
=>
[{:namespace/your-string "Your value"
  :namespace/id          "MyObjectId"}]
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-documents-by-query
  ([collection-name query]
   (if-let [query (-> query checking/find-query adaptation/find-query)]
           (if-let [documents (find-maps collection-name query)]
                   (vector/->items documents #(adaptation/find-output %)))))

  ([collection-name query projection]
   (if-let [query (-> query checking/find-query adaptation/find-query)]
           (if-let [projection (adaptation/find-projection projection)]
                   (if-let [documents (find-maps collection-name query projection)]
                           (vector/->items documents #(adaptation/find-output %)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-documents-by-query]]))

(mongo-db/get-documents-by-query ...)
(get-documents-by-query          ...)
```

</details>

---

### get-first-document

```
@param (string) collection-name
```

```
@usage
(get-first-document "my_collection")
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn get-first-document
  [collection-name]
  (let [collection (get-collection collection-name)]
       (first collection)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-first-document]]))

(mongo-db/get-first-document ...)
(get-first-document          ...)
```

</details>

---

### get-last-document

```
@param (string) collection-name
```

```
@usage
(get-last-document "my_collection")
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn get-last-document
  [collection-name]
  (let [collection (get-collection collection-name)]
       (last collection)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-last-document]]))

(mongo-db/get-last-document ...)
(get-last-document          ...)
```

</details>

---

### get-pipeline

```
@param (map) pipeline-props
```

```
@usage
(get-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
               :filter-pattern {:namespace/my-keyword :my-value
                                :$or [{:namespace/my-boolean   false}
                                      {:namespace/my-boolean   nil}]}
               :search-pattern {:$or [{:namespace/my-string   "My value"}
                                      {:namespace/your-string "Your value"}]}
               :sort-pattern   {:namespace/my-string -1}
               :unset-pattern  [:namespace/my-string :namespace/your-string]
               :max-count 20
               :skip      40})
```

```
@return (maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-pipeline
  [{:keys [field-pattern filter-pattern max-count search-pattern skip sort-pattern unset-pattern]}]
  (cond-> [] field-pattern (conj {"$addFields"      (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query     filter-pattern)
                                                    (search-query     search-pattern)]}})
             sort-pattern  (conj {"$sort"           (sort-query       sort-pattern)})
             unset-pattern (conj {"$unset"          (unset-query      unset-pattern)})
             skip          (conj {"$skip"           (param            skip)})
             max-count     (conj {"$limit"          (param            max-count)})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-pipeline]]))

(mongo-db/get-pipeline ...)
(get-pipeline          ...)
```

</details>

---

### get-specified-values

```
@param (string) collection-name
@param (keywords in vector) specified-keys
@param (function)(opt) test-f
```

```
@example
(get-specified-values "my_collection" [:my-key :your-key] string?)
=>
{:my-key   ["..." "..."]
 :your-key ["..." "..."]}
```

```
@return (map)
```

<details>
<summary>Source code</summary>

```
(defn get-specified-values
  ([collection-name specified-keys]
   (get-specified-values collection-name specified-keys some?))

  ([collection-name specified-keys test-f]
   (letfn [(f [result document]
              (letfn [(f [result k]
                         (let [v (get document k)]
                              (if (test-f v)
                                  (update result k vector/conj-item-once v)
                                  (return result))))]
                     (reduce f result specified-keys)))]
          (let [collection (get-collection collection-name)]
               (reduce f {} collection)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [get-specified-values]]))

(mongo-db/get-specified-values ...)
(get-specified-values          ...)
```

</details>

---

### insert-document!

```
@param (string) collection-name
@param (namespaced map) document
@param (map)(opt) options
```

```
@example
(insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
=>
{:namespace/id "MyObjectId" ...}
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn insert-document!
  ([collection-name document]
   (insert-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (checking/insert-input %)
                                      (preparing/insert-input collection-name % options)
                                      (adaptation/insert-input %))]
           (if-let [result (insert-and-return! collection-name document)]
                   (adaptation/insert-output result)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [insert-document!]]))

(mongo-db/insert-document! ...)
(insert-document!          ...)
```

</details>

---

### insert-documents!

```
@param (string) collection-name
@param (namespaced maps in vector) documents
@param (map)(opt) options
```

```
@example
(insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
=>
[{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}]
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn insert-documents!
  ([collection-name documents]
   (insert-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(insert-document! collection-name % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [insert-documents!]]))

(mongo-db/insert-documents! ...)
(insert-documents!          ...)
```

</details>

---

### remove-all-documents!

```
@param (string) collection-name
```

```
@usage
(remove-all-documents! "my_collection")
```

```
@return (?)
```

<details>
<summary>Source code</summary>

```
(defn remove-all-documents!
  [collection-name]
  (drop! collection-name))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [remove-all-documents!]]))

(mongo-db/remove-all-documents! ...)
(remove-all-documents!          ...)
```

</details>

---

### remove-document!

```
@param (string) collection-name
@param (string) document-id
@param (map)(opt) options
```

```
@example
(remove-document "my_collection" "MyObjectId" {...})
=>
"MyObjectId"
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn remove-document!
  ([collection-name document-id]
   (remove-document! collection-name document-id {}))

  ([collection-name document-id {:keys [ordered?] :as options}]
   (if ordered? (remove-ordered-document!   collection-name document-id options)
                (remove-unordered-document! collection-name document-id options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [remove-document!]]))

(mongo-db/remove-document! ...)
(remove-document!          ...)
```

</details>

---

### remove-documents!

```
@param (string) collection-name
@param (strings in vector) document-ids
@param (map)(opt) options
```

```
@example
(remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
=>
["MyObjectId" "YourObjectId"]
```

```
@return (strings in vector)
```

<details>
<summary>Source code</summary>

```
(defn remove-documents!
  ([collection-name document-ids]
   (remove-documents! collection-name document-ids {}))

  ([collection-name document-ids options]
   (vector/->items document-ids #(remove-document! collection-name % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [remove-documents!]]))

(mongo-db/remove-documents! ...)
(remove-documents!          ...)
```

</details>

---

### reorder-documents!

```
@param (string) collection-name
@param (vectors in vector) document-order
```

```
@usage
(reorder-documents "my_collection" [["MyObjectId" 1] ["YourObjectId" 2]])
```

```
@return (vectors in vector)
```

<details>
<summary>Source code</summary>

```
(defn reorder-documents!
  [collection-name document-order]
  (let [namespace (reader/get-collection-namespace collection-name)
        order-key (keyword/add-namespace namespace :order)]
       (letfn [(f [[document-id document-dex]]
                  (if-let [document-id (adaptation/document-id-input document-id)]
                          (let [result (update! collection-name {:_id document-id}
                                                                {"$set" {order-key document-dex}})]
                               (if (mrt/acknowledged? result)
                                   (return [document-id document-dex])))))]
              (vector/->items document-order f))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [reorder-documents!]]))

(mongo-db/reorder-documents! ...)
(reorder-documents!          ...)
```

</details>

---

### save-document!

```
@param (string) collection-name
@param (namespaced map) document
@param (map)(opt) options
```

```
@example
(save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
=>
{:namespace/id "MyObjectId" ...}
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn save-document!
  ([collection-name document]
   (save-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (checking/save-input %)
                                      (preparing/save-input collection-name % options)
                                      (adaptation/save-input %))]
           (if-let [result (save-and-return! collection-name document)]
                   (adaptation/save-output result)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [save-document!]]))

(mongo-db/save-document! ...)
(save-document!          ...)
```

</details>

---

### save-documents!

```
@param (string) collection-name
@param (namespaced maps in vector) documents
@param (map)(opt) options
```

```
@example
(save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
=>
[{:namespace/id "MyObjectId" ...}]
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn save-documents!
  ([collection-name documents]
   (save-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(save-document! collection-name % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [save-documents!]]))

(mongo-db/save-documents! ...)
(save-documents!          ...)
```

</details>

---

### search-query

```
@param (map) search-pattern
```

```
@example
(search-query {:$or [{:namespace/my-string   "My value"}
                     {:namespace/your-string "Your value"}]})
=>
{"$or" [{"namespace/my-string"   {"$regex" "My value" "$options" "i"}}
        {"namespace/your-string" {"$regex" "Your value" "$options" "i"}}]}
```

```
@return (map)
```

<details>
<summary>Source code</summary>

```
(defn search-query
  [{:keys [$and $or]}]
  (cond-> {} $and (assoc "$and" (vector/->items $and #(-> % checking/search-query adaptation/search-query)))
             $or  (assoc "$or"  (vector/->items $or  #(-> % checking/search-query adaptation/search-query)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [search-query]]))

(mongo-db/search-query ...)
(search-query          ...)
```

</details>

---

### sort-query

```
@param (map) sort-pattern
```

```
@example
(sort-query {:namespace/my-string -1 ...})
=>
{"namespace/my-string" -1 ...}
```

```
@return (map)
```

<details>
<summary>Source code</summary>

```
(defn sort-query
  [sort-pattern]
  (map/->keys sort-pattern json/unkeywordize-key))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [sort-query]]))

(mongo-db/sort-query ...)
(sort-query          ...)
```

</details>

---

### unset-query

```
@param (namespaced keywords in vector) unset-pattern
```

```
@example
(unset-query [:namespace/my-string :namespace/your-string])
=>
["namespace/my-string" "namespace/your-string"]
```

```
@return (strings in vector)
```

<details>
<summary>Source code</summary>

```
(defn unset-query
  [unset-pattern]
  (vector/->items unset-pattern json/unkeywordize-key))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [unset-query]]))

(mongo-db/unset-query ...)
(unset-query          ...)
```

</details>

---

### update-document!

```
@param (string) collection-name
@param (map) query
@param (map or namespaced map) document
@param (map)(opt) options
```

```
@usage
(update-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(update-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(update-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn update-document!
  ([collection-name query document]
   (update-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/update-input %)
                                               (preparing/update-input collection-name % options)
                                               (adaptation/update-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            (let [result (update! collection-name query document {:multi false :upsert false})]
                                 (mrt/updated-existing? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [update-document!]]))

(mongo-db/update-document! ...)
(update-document!          ...)
```

</details>

---

### update-documents!

```
@param (string) collection-name
@param (map) query
@param (namespaced map) document
@param (map)(opt) options
```

```
@usage
(update-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(update-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(update-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn update-documents!
  ([collection-name query document]
   (update-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/update-input %)
                                               (preparing/update-input collection-name % options)
                                               (adaptation/update-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            (let [result (update! collection-name query document {:multi true :upsert false})]
                                 (mrt/updated-existing? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [update-documents!]]))

(mongo-db/update-documents! ...)
(update-documents!          ...)
```

</details>

---

### upsert-document!

```
@param (string) collection-name
@param (map) query
@param (map or namespaced map) document
@param (map)(opt) options
```

```
@usage
(upsert-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(upsert-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(upsert-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn upsert-document!
  ([collection-name query document]
   (upsert-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/upsert-input %)
                                               (preparing/upsert-input collection-name % options)
                                               (adaptation/upsert-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            (let [result (upsert! collection-name query document {:multi false})]
                                 (mrt/acknowledged? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [upsert-document!]]))

(mongo-db/upsert-document! ...)
(upsert-document!          ...)
```

</details>

---

### upsert-documents!

```
@param (string) collection-name
@param (map) query
@param (namespaced map) document
@param (map)(opt) options
```

```
@usage
(upsert-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(upsert-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(upsert-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn upsert-documents!
  ([collection-name query document]
   (upsert-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/upsert-input %)
                                               (preparing/upsert-input collection-name % options)
                                               (adaptation/upsert-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            (let [result (upsert! collection-name query document {:multi true})]
                                 (mrt/acknowledged? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :as mongo-db :refer [upsert-documents!]]))

(mongo-db/upsert-documents! ...)
(upsert-documents!          ...)
```

</details>