(ns wemail.api
  (:use
    baard
    [wemail.store :only [connection]]
    wemail.api.atom
    ring.adapter.jetty
    [ring.middleware
       http-basic-auth
       reload
       stacktrace]))

(def domain "wishfulcoding.mailgun.org")

(def authenticator
  (fnil (partial connection domain) "" ""))

(def api
  (->
    (app
      [:get "mailbox" & path]
        (-> mailbox
          (delegate path)
          (wrap-basic-auth "mailbox" authenticator)))
    (fnil (constantly {:status 404, :headers {}, :body "not found"}))))

(defn -main [ & args]
  (run-jetty api {:port 8080}))

(defn debug []
  (run-jetty
    (-> #'api
      (wrap-reload ['wemail.api])
      wrap-stacktrace)
    {:port 8080}))


