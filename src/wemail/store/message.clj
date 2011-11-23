(ns wemail.store.message
  (:use [clojure.set :only [map-invert]])
  (:require [clojure.string :as s]
            [clojure.walk :as w]
            [wemail.store :as store]))

(defn headers [message*]
  (into {}
    (for [h (enumeration-seq (.getAllHeaders message*))]
      [(s/lower-case (.getName h)) (.getValue h)])))

(defn parse-content-type [ct]
  (take 2 (s/split ct #"[;/]")))

(defn derive-content-type [primary sub]
  (let [general (keyword primary "*")
        specific (keyword primary sub)]
    (derive specific general)
    specific))

(defn content-type [part]
  (->> part
    .getContentType
    parse-content-type
    (apply derive-content-type)))

(defn part? [c]
  (or
    (instance? javax.mail.Part c)
    (instance? javax.mail.Multipart c)))

(declare message* parse-content parse-part)

(defn parse-content [content]
  (if (part? content)
    (message* content)
    content))

(defn message* [part]
  (assoc
    (headers part)
    :content (parse-part part)))

(defmulti parse-part content-type :default :*/*)

(defmethod parse-part :*/* [part]
  (parse-content (.getContent part)))

(defmethod parse-part :multipart/* [part]
  (let [multipart (.getContent part)]
    (for [idx (range (.getCount multipart))]
      (parse-content (.getBodyPart multipart idx)))))

(let [inverse-flags (map-invert store/message-flags)]
  (defn get-flags [message]
    (set (map
      (partial get inverse-flags)
      (-> message .getFlags .getSystemFlags)))))

(defn message [msg]
  (assoc
    (w/keywordize-keys
      (message* msg))
    :flags (get-flags msg)))
