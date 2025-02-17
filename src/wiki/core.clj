(ns wiki.core
  (:require [org.httpkit.server :as hk-server]
            [ring.middleware.params :as params]
            [ring.util.response :as response]
            [clojure.java.io :as io]
            [clj-simple-router.core :as router])
  (:gen-class))

(def resources-dir "resources/wiki/")

(defn read-page [page-name]
  (let [file (io/file (str resources-dir page-name ".txt"))]
    (when (.exists file)
      (slurp file))))

(defn write-page [page-name content]
  (let [file (io/file (str resources-dir page-name ".txt"))]
    (io/make-parents file)
    (spit file content)))

(defn render-page [page-name]
  (if-let [content (read-page page-name)]
    (response/response (str "<html><body><h1>" page-name "</h1><p>" content "</p></body></html"))
    (response/response (str "<html><body><h1>Create: " page-name "</h1><form method='post'><textarea name='content'></textarea><button type='submit'>Save</button></form></body></html>"))))

(defn save-page [page-name content]
  (write-page page-name content)
  (response/response (str "<html><body><h1>" page-name " saved</h1><a href='/w/" page-name "'>View page</a></body></html>")))

(def routes
  (router/routes
    "GET /" []
    {:status 200
     :body "<html><body><h1>Welcome to the Wiki!</h1><a href='/w/Home'>Home</a></body></html>"}

    "GET /w/*" [page-name]
    (render-page page-name)

    "POST /w/*" req
    (let [page-name (first (:path-params req))
          content (get (:form-params req) "content")]
      (save-page page-name content))))

(defn handler []
  (-> (router/router routes)
      (params/wrap-params)))

(defonce server (atom nil))

(defn start-server []
  (when (nil? @server)
    (reset! server (hk-server/run-server (handler) {:port 3001}))
    (println "Server started on port 3001")))

(defn stop-server []
  (when (some? @server)
    (@server :timeout 100)
    (reset! server nil)
    (println "Server stopped.")))

(defn -main [& _args]
  (start-server))

(comment
  (-main)
  (start-server)
  (stop-server))
