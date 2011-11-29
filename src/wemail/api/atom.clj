(ns wemail.api.atom
  (:use [ring.util.response :only [response]]
        wemail.store.folder
        [wemail.store.message :only [simple-content]]
        wemail.store
        [clojure.set :only [rename-keys]]
        armagedom))

(def header
  (map-syntax
    {:id "http://wishfulcoding.nl"
     :title "inbox"
     :updated "2003-12-13T18:30:02Z"}))

(defn item [message]
  (let [mapping {:subject :title
                 :message-id :id
                 :date :updated
                 :content :summary}]
    (-> message
      (rename-keys mapping)
      (select-keys (vals mapping))
      (update-in [:summary] simple-content)
      map-syntax
      (->> (cons :entry)))))

(defn feed [header messages]
  (apply xml :feed "http://www.w3.org/2005/Atom" []
         (concat header
                 (map item messages))))

(defn atom-response [dom]
  {:status 200 :headers {"Content-Type" "application/atom+xml"} :body (xml-str dom)})

(defn mailbox [{session :login :as req} path]
  (when session
    (let [mbox (if (seq path)
                 (open session (apply str (interpose \/ path)))
                 (open session))]
    (atom-response
      (feed header (messages mbox))))))
