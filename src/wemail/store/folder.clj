(ns wemail.store.folder
  (:require [wemail.store.message :as m]
            [wemail.store :as s]))

(defn messages
  ([folder] (messages folder (.getMessageCount folder)))
  ([folder end] (messages folder 0 end))
  ([folder start end]
   (map m/message (.getMessages folder))))

(defn status
  "(filter (status :seen) (messages i))"
  [flag]
  (fn [message]
    (get-in message [:flags flag])))

