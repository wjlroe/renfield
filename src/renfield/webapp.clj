(ns renfield.webapp
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [prone.middleware :as prone]
            [renfield.queue :as queue]))

(defn default-layout
  [& body]
  (html
   [:head
    [:meta {:http-equiv "Content-type"
            :content "text/html; charset=utf-8"}]
    [:title "Renfield"]]
   [:body
    body]))

(defn index
  [request]
  (default-layout
    [:h1 "Send URLS to /goto"]
    [:p "Sent URLS will (eventually) be openned in your browser of choice"]
    [:pre {} (queue/q-stats (::queue request))]))

(defroutes app-routes
  (GET "/" request (index request)))

(defn wrap-app-component [f queue]
  (fn [req]
    (f (assoc req ::queue queue))))

(defn make-handler [queue]
  (-> (site app-routes)
      prone/wrap-exceptions
      (wrap-app-component queue)))
