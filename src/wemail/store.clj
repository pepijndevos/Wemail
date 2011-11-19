(ns wemail.store
  (:require [clojure.string :as s]))

(defn field-map [cls]
  (into {}
    (for [field (:fields (bean cls))]
      [(keyword (s/lower-case (.getName field)))
       (.get field cls)])))

(defn properties [props]
  (doto (java.util.Properties.)
    (.putAll props)))

(def folder-mode (field-map javax.mail.Folder))

(def recipient-type (field-map javax.mail.Message$RecipientType))

(def ^:dynamic *session* (javax.mail.Session/getDefaultInstance (System/getProperties) nil))

(defn connection [host id pass]
  (doto (.getStore *session* "imaps")
    (.connect host id pass)))

(defn open
  ([store]
   (open store "INBOX"))
  ([store folder]
   (open store folder :read_write))
  ([store folder mode]
   (let [folder (.getFolder store folder)
         mode (get folder-mode mode)]
     (.open folder mode)
     folder)))
