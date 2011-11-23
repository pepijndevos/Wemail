(ns wemail.store
  (:require [clojure.string :as s]))

(defn field-map [cls]
  (into {}
    (for [field (:fields (bean cls))]
      [(-> field
         .getName
         s/lower-case
         (s/replace #"_" "-")
         keyword)
       (.get field cls)])))

(defn properties [props]
  (doto (java.util.Properties.)
    (.putAll props)))

(def folder-mode (field-map javax.mail.Folder))

(def recipient-type (field-map javax.mail.Message$RecipientType))

(def message-flags (field-map javax.mail.Flags$Flag))

(def ^:dynamic *session* (javax.mail.Session/getDefaultInstance (System/getProperties) nil))

(defn connection [host id pass]
  (doto (.getStore *session* "imaps")
    (.connect host id pass)))

(defn open
  ([store]
   (open store "INBOX"))
  ([store folder]
   (open store folder :read-write))
  ([store folder mode]
   (let [folder (.getFolder store folder)
         mode (get folder-mode mode)]
     (.open folder mode)
     folder)))
