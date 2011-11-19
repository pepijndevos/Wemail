(ns wemail.store.mime
  (:use wemail.store.message))

(defmethod parse-part :image/* [part]
   (javax.imageio.ImageIO/read (.getContent part)))
