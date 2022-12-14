
# mongo-db-api

> "Think, think, think." – Winnie the Pooh

### Overview

The <strong>mongo-db-api</strong> is a MongoDB implementation for Clojure projects
based on the [michaelklishin / monger] library with some extra features such as
error handling, input checking, output checking, order handling, prototype handling, etc.

> This libary designed for working with namespaced documents!

### deps.edn

```
{:deps {bithandshake/mongo-db-api {:git/url "https://github.com/bithandshake/mongo-db-api"
                                   :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/mongo-db-api/tree/release).

### Documentation

The <strong>mongo-db-api</strong> functional documentation is [available here](documentation/COVER.md).

### Changelog

You can track the changes of the <strong>mongo-db-api</strong> library [here](CHANGES.md).

### Index

- [How to insert a document?](#how-to-insert-a-document)
- [How to insert more than one document?](#how-to-insert-more-than-one-document)
- [How to save a document?](#how-to-save-a-document-upserting-by-id)
- [How to save more than one document?](#how-to-save-more-than-one-document)
- [How to update a document?](#how-to-update-a-document)
- [How to update more than one document?](#how-to-update-more-than-one-document)
- [How to upsert a document?](#how-to-upsert-a-document)
- [How to upsert more than one document?](#how-to-upsert-more-than-one-document)
- [How to apply a function on a document?](#how-to-apply-a-function-on-a-document)
- [How to apply a function on a collection?](#how-to-apply-a-function-on-a-collection)
- [How to remove a document?](#how-to-remove-a-document)
- [How to remove more than one document?](#how-to-remove-more-than-one-document)
- [How to remove all documents of a collection?](#how-to-remove-all-documents-of-a-collection)
- [How to duplicate a document?](#how-to-duplicate-a-document)
- [How to duplicate more than one document?](#how-to-duplicate-more-than-one-document)
- [How to reorder documents?](#how-to-reorder-documents)
- [How to check whether the database is connected?](#how-to-reorder-documents)
- [How to generate a compatible document id?](#how-to-reorder-documents)
- [How to make a pipeline for getting documents?](#how-to-make-a-pipeline-for-getting-documents)
- [How to make a pipeline for counting documents?](#how-to-make-a-pipeline-for-counting-documents)
- [How to get the names of collections?](#how-get-the-names-of-collections)
- [How to get the namespace of a collection?](#how-to-the-namespace-of-a-collection)
- [How to check whether a collection is empty?](#how-to-check-whether-a-collection-is-empty)
- [How to count all the documents in a collection?](#how-to-count-all-the-documents-in-a-collection)
- [How to count documents by pipeline?](#how-to-count-document-by-pipeline)
- [How to count documents by query?](#how-to-count-documents-by-query)
- [How to get all the documents of a collection?](#how-to-get-all-the-documents-of-a-collection)
- [How to get a document by query?](#how-to-get-a-document-by-query)
- [How to get documents by query?](#how-to-get-documents-by-query)
- [How to get a document by id?](#how-to-get-document-by-id)
- [How to get documents by pipeline?](#how-to-get-documents-by-pipeline)
- [How to check whether a document exists?](#how-to-check-whether-a-document-exists)
- [How to collect the values of specific keys from all documents in a collection?](#how-to-collect-the-values-of-specific-keys-from-all-documents-in-a-collection)

# Usage

> Some functions and some parameters of the following functions won't be discussed.
  To learn more about the available functionality, check out the
  [functional documentation](documentation/COVER.md)!

### How to insert a document?

The [`mongo-db.api/insert-document!`](documentation/clj/mongo-db/API.md#insert-document)
function inserts the given document to the end of the collection.

- If the given document doesn't have the `:namespace/id` key, the function will
  generate it.
- If the collection has a document with the same `:namespace/id` value, the function
  will ignore the inserting!
- If the `{:ordered? true}` setting passed, the inserted document will get the
  last position in the collection: `{:namespace/order 123}`.
- In case of successfully inserting, the return value will be the inserted document.

```
(insert-document! "my_collection" {:namespace/my-keyword  :my-value
                                   :namespace/your-string "your-value"
                                   :namespace/id          "MyObjectId"})
```

```
(defn my-prepare-f
  [document]
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(insert-document! "my_collection" {:namespace/my-keyword :my-value}
                                  {:prepare-f my-prepare-f})
```

```
(insert-document! "my_collection" {:namespace/my-keyword :my-value}
                                  {:ordered? true})
```

### How to insert more than one document?

The [`mongo-db.api/insert-documents!`](documentation/clj/mongo-db/API.md#insert-documents)
function inserts the given documents to the end of the collection.

> The `insert-documents!` function applies the `insert-document!` function.
  You can find more information in the previous section.

- In case of successfully inserting, the return value will be a vector with the
  inserted documents.

```
(insert-documents! "my_collection" [{:namespace/my-keyword :my-value}
                                    {:namespace/my-keyword :your-value}])
```

### How to save a document? (upserting by id)

The [`mongo-db.api/save-document!`](documentation/clj/mongo-db/API.md#save-document)
function updates the given document if it exists in the collection with the same
`:namespace/id` value, otherwise it inserts it to the end of the collection.

- If the given document doesn't have the `:namespace/id` key, the function will
  generate it.
- If the collection has a document with the same `:namespace/id` value, the
  function will update it!
- If the `{:ordered? true}` setting passed and the document not existed before,
  the inserted document will get the last position in the collection.
- In case of successfully saving, the return value will be the saved document.

```
(save-document! "my_collection" {:namespace/my-keyword  :my-value
                                 :namespace/your-string "your-value"
                                 :namespace/id          "MyObjectId"})
```

```
(defn my-prepare-f
  [document]
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(save-document! "my_collection" {:namespace/my-keyword :my-value}
                                {:prepare-f my-prepare-f})
```

```
(save-document! "my_collection" {:namespace/my-keyword :my-value}
                                {:ordered? true})
```

### How to save more than one document?

The [`mongo-db.api/save-documents!`](documentation/clj/mongo-db/API.md#save-documents)
function updates the given documents if it exists in the collection with the same
`:namespace/id` value, otherwise it inserts them to the end of the collection.

> The `insert-documents!` function applies the `insert-document!` function.
  You can find more information in the previous section.

- In case of successfully saving, the return value will be a vector with the
  saved documents.

```
(save-documents! "my_collection" [{:namespace/my-keyword :my-value}
                                  {:namespace/my-keyword :your-value}])
```

### How to update a document?

The [`mongo-db.api/update-document!`](documentation/clj/mongo-db/API.md#update-document)
function updates the first document in the collection found by the given query.

- If the function cannot find a document in the collection by the given query,
  it will ignore the updating!
- The given query can contains the `:namespace/id` key.
- The given document cannot contains the `:namespace/id` key!
- In case of successfully updating, the return value will be `TRUE`.

```
(update-document! "my_collection" {:namespace/id "MyObjectId"}
                                  {:namespace/my-keyword  :my-value
                                   :namespace/your-string "your-value"})
```

```
(defn my-prepare-f
  [document]
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(update-document! "my_collection" {:namespace/id "MyObjectId"}
                                  {:namespace/my-keyword :my-value}
                                  {:prepare-f my-prepare-f})
```

### How to update more than one document?

The [`mongo-db.api/update-documents!`](documentation/clj/mongo-db/API.md#update-documents)
function updates documents in the collection found by the given query.

> The `update-documents!` function applies the `update-document!` function.
  You can find more information in the previous section.

- In case of successfully updating, the return value will be `TRUE`.

```
(update-documents! "my_collection" {:namespace/id "MyObjectId"}
                                   {:namespace/my-keyword :my-value})
```

### How to upsert a document?

The [`mongo-db.api/upsert-document!`](documentation/clj/mongo-db/API.md#upsert-document)
function updates the first document in the collection found by the given query,
otherwise inserts it as a new document to the end of the collection.

- If the function cannot find a document in the collection by the given query,
  it will insert it as a new document to the end of the collection!
- The given query can contains the `:namespace/id` key.
- The given document cannot contains the `:namespace/id` key!
- In case of successfully upserting, the return value will be `TRUE`.

```
(upsert-document! "my_collection" {:namespace/id "MyObjectId"}
                                  {:namespace/my-keyword  :my-value
                                   :namespace/your-string "your-value"})
```

```
(defn my-prepare-f
  [document]
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(upsert-document! "my_collection" {:namespace/id "MyObjectId"}
                                  {:namespace/my-keyword :my-value}
                                  {:prepare-f my-prepare-f})
```

### How to upsert more than one document?

The [`mongo-db.api/upsert-documents!`](documentation/clj/mongo-db/API.md#upsert-documents)
function updates documents in the collection found by the given query, otherwise
inserts it as a new document to the end of the collection.

> The `upsert-documents!` function applies the `upsert-document!` function.
  You can find more information in the previous section.

- In case of successfully upserting, the return value will be `TRUE`.

```
(upsert-documents! "my_collection" {:namespace/id "MyObjectId"}
                                   {:namespace/my-keyword :my-value})
```

### How to apply a function on a document?

The [`mongo-db.api/apply-on-document!`](documentation/clj/mongo-db/API.md#apply-on-document)
function applies the given function on a document found by the given id.

- If the function cannot find a document in the collection by the given id,
  it will ignore the applying!
- In case of successfully applying, the return value will be the modified document.

```
(defn my-modifier-f
  [document]
  (assoc document :namespace/my-keyword :my-value))

(apply-on-document! "my_collection" "MyObjectId" my-modifier-f)
```

```
(defn my-prepare-f
  [document]
  ; This function will be applied before the given modifier function!
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(defn my-postpare-f
  [document]
  ; This function will be applied after the given modifier function!
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(defn my-modifier-f
  [document]
  (assoc document :namespace/my-keyword :my-value))

(apply-on-document! "my_collection" "MyObjectId" my-modifier-f
                                    {:prepare-f  my-prepare-f
                                     :postpare-f my-postpare-f})
```

### How to apply a function on a collection?

The [`mongo-db.api/apply-on-collection!`](documentation/clj/mongo-db/API.md#apply-on-collection)
function applies the given function on all documents in a collection.

> The `apply-on-collection!` function works similar to the `apply-on-document!` function.
  You can find more information in the previous section.

- In case of successfully applying, the return value will be the modified collection.

```
(defn my-modifier-f
  [document]
  (assoc document :namespace/my-keyword :my-value))

(apply-on-collection! "my_collection" my-modifier-f)
```

### How to remove a document?

The [`mongo-db.api/remove-document!`](documentation/clj/mongo-db/API.md#remove-document)
function removes the document found by the given id.

- If the function cannot find a document in the collection by the given id,
  it will ignore the removing!
- If the `{:ordered? true}` setting passed, the function will update the
  `:namespace/order` value of the documents which come after the removed document.
- In case of successfully removing, the return value will be the id of the
  removed document.

```
(remove-document! "my_collection" "MyObjectId")
```

```
(remove-document! "my_collection" "MyObjectId" {:ordered? true})
```

### How to remove more than one document?

The [`mongo-db.api/remove-documents!`](documentation/clj/mongo-db/API.md#remove-documents)
function removes documents from the collection found by the given id-s.

> The `remove-documents!` function applies the `remove-document!` function.
  You can find more information in the previous section.

- In case of successfully removing, the return value will be a vector of the
  removed documents' id-s.

```
(remove-documents! "my_collection" ["MyObjectId" "YourObjectId"])
```

### How to remove all documents of a collection?

The [`mongo-db.api/remove-all-documents!`](documentation/clj/mongo-db/API.md#remove-all-documents)
function removes all documents of a collection.

```
(remove-all-documents! "my_collection")
```

### How to duplicate a document?

The [`mongo-db.api/duplicate-document!`](documentation/clj/mongo-db/API.md#duplicate-document)
function duplicates the document found by the given id.

- If the function cannot find a document in the collection by the given id,
  it will ignore the duplicating!
- If the `{:ordered? true}` setting passed, the copy document will get the
  next position in the collection and the function updates the `:namespace/order`
  value of the documents which come after the original document.
- If the `{:label-key ...}` setting passed, the copy document will get the
  `"#2"` suffix on its label. In case of the second copy label (`"My label #2"`)
  is not available, the suffix will contain the next available number.
- If the `{:changes {...}}` setting passed, the given changes will be merged into
  the copy document. If the changes map contains the id of the original document,
  the function will remove it before the changes merged into the copy document.
- In case of successfully duplicating, the return value will be the copy document.

```
(duplicate-document! "my_collection" "MyObjectId")
```

```
(defn my-prepare-f
  [document]
  ; This function will be applied before the copy document got its id
  ; and updated label, and the changes merged into it.
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(defn my-postpare-f
  [document]
  ; This function will be applied after the copy document got its id
  ; and updated label, and the changes merged into it.
  (assoc document :namespace/modified-by {:user/id "my-user"}))

(duplicate-document! "my_collection" "MyObjectId" {:changes    {:namespace/your-string "I'm changed!"}
                                                   :label-key  :namespace/label
                                                   :ordered?   true
                                                   :prepare-f  my-prepare-f
                                                   :postpare-f my-postpare-f})
```

### How to duplicate more than one document?

The [`mongo-db.api/duplicate-documents!`](documentation/clj/mongo-db/API.md#duplicate-documents)
function duplicates the documents found by the given id-s.

- In case of successfully duplicating, the return value will be a vector of the
  copy documents.

```
(duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"]
```

### How to reorder documents?

The [`mongo-db.api/reorder-documents!`](documentation/clj/mongo-db/API.md#reorder-documents)
function updates the `:namespace/order` value with the given values of the documents
found by the given id-s.

- If the function cannot find a document in the collection by a given id,
  it will ignore that certain updating!
- Not necessarry to update all documents in one time, this function can updates
  only certain documents of the collection.
- In case of successfully updating, the return value will be a vector of the updated
  documents' id-s and their updated positions.

```
(reorder-documents! "my_collection" [["MyObjectId" 5] ["YourObjectId" 3]]
```

### How to check whether the database is connected?

...

### How to generate a compatible document id?

...

### How to make a pipeline for getting documents?

...

### How to make a pipeline for counting documents?

...

### How to get the names of collections?

...

### How to get the namespace of a collection?

...

### How to check whether a collection is empty?

...

### How to count all the documents in a collection?

...

### How to count documents by pipeline?

...

### How to count documents by query?

...

### How to get all the documents of a collection?

...

### How to get a document by query?

...

### How to get documents by query?

...

### How to get a document by id?

...

### How to get documents by pipeline?

...

### How to check whether a document exists?

...

### How to collect the values of specific keys from all documents in a collection?

...
