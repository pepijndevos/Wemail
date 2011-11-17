(ns wemail.store
  (:require [clojure.string :as s]
            [clojure.walk :as w]))

(def ^:dynamic *session* (javax.mail.Session/getDefaultInstance (System/getProperties) nil))

(defn properties [props]
  (doto (java.util.Properties.)
    (.putAll props)))

(defn connection [host id pass]
  (doto (.getStore *session* "imaps")
    (.connect host id pass)))

(defn headers [message]
  (into {}
    (for [h (enumeration-seq (.getAllHeaders message))]
      [(s/lower-case (.getName h)) (.getValue h)])))

(defn content-type [part]
  (let [[primary sub] (s/split (.getContentType part) #"[;/]")
        general (keyword primary "*")
        specific (keyword primary sub)]
    (derive specific general)
    specific))

(defmulti parse-content content-type)

(defmethod parse-content :default [part]
  [[(content-type part)
    (.getContent part)]])

(defmethod parse-content :multipart/* [part]
  (let [multipart (.getContent part)]
    (for [idx (range (.getCount multipart))]
      (.getBodyPart multipart idx))))

(defn part? [c]
  (instance? javax.mail.Part c))

(defn part-seq [part]
  (tree-seq
    part?
    parse-content
    part))

(defn message-content [message]
  (let [parts (into {}
                (filter vector? (part-seq message)))]
    (or (:text/html parts) (:text/plain parts))))

(defn message->map [message]
  (w/keywordize-keys
    (assoc
      (headers message)
      ;:parts (part-seq message)
      :content (message-content message))))

(defn field-map [cls]
  (into {}
    (for [field (:fields (bean cls))]
      [(keyword (s/lower-case (.getName field)))
       (.get field cls)])))

(defn folder-seq [folder]
  (tree-seq
    (constantly true)
    #(.list %)
    folder))
