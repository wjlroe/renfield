(ns renfield.systems
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [renfield.webapp :refer [make-handler]]
            [renfield.queue :as queue]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn http-port
  []
  (or (env :http-port)
      8744))

(defn queue-directory
  []
  (or (env :queue-dir)
      "/tmp/renfield-q"))

(defrecord WebServer [queue server port handler prone-enabled?]
  component/Lifecycle
  (start [component]
    (assoc component
           :server (run-jetty (make-handler queue prone-enabled?)
                              {:port port
                               :join? false})))
  (stop [component]
    (when server
      (.stop server))
    component))

(defn new-web-server
  [port prone-enabled?]
  (component/using
   (map->WebServer {:port port :prone-enabled? prone-enabled?})
   [:queue]))

(defn dev-system
  []
  (component/system-map
   :queue (queue/new-queue (queue-directory))
   :web (new-web-server (http-port) true)))

(defn prod-system
  []
  (component/system-map
   :queue (queue/new-queue (queue-directory))
   :web (new-web-server (http-port) false)))
