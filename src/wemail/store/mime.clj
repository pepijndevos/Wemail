(ns wemail.store.mime
  (:use wemail.store))

(defmethod parse-content :image/* [part]
  [[(content-type part)
    (javax.imageio.ImageIO/read (.getContent part))]])
