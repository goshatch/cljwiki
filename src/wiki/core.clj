(ns wiki.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response]]
            [clojure.java.io :as io]
            [clj-simple-router.core :as router])
  (:gen-class))

(def resources-dir "resources/wiki/")

(defn read-page [page-name]
  (let [file (io/file (str resources-dir page-name ".txt"))]
    (when (.exists file)
      (slurp file))))

(defn write-page [page-name content]
  (spit (io/file (str resources-dir page-name ".txt")) content))

(defn render-page [page-name]
  (if-let [content (read-page page-name)]
    (response (str "<html><body><h1>" page-name "</h1><p>" content "</p></body></html"))
    (response (str "<html><body><h1>Create: " page-name "</h1><form method='post'><textarea name='content'></textarea><button type='submit'>Save</button></form></body></html>"))))

(defn save-page [page-name content]
  (write-page page-name content)
  (response (str "<html><body><h1>" page-name " saved</h1><a href='/w/" page-name "'>View page</a></body></html>")))

(def routes
  {"GET /"
   (fn [_req]
     {:status 200
      :body "<html><body><h1>Welcome to the Wiki!</h1><a href='/w/Home'>Home</a></body></html>"})
   "GET /w/*"
   (fn [req]
     (let [page-name (first (:path-params req))]
       (render-page page-name)))
   "POST /w/*"
   (fn [req]
     (let [page-name (first (:path-params req))
           content (:content (:params req))]
       (save-page page-name content)))})

(defn handler []
  (-> (router/router routes)
      (wrap-params)))


(defn -main [& _args]
  (run-jetty (handler) {:port 3001 :join? false}))

(comment
  (-main))
