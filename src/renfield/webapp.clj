(ns renfield.webapp
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [prone
             [debug :refer [debug]]
             [middleware :as prone]]
            [renfield.queue :as queue]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [redirect-after-post]]
            [taoensso.timbre :as timbre]))

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
  (let [stats (queue/q-stats (::queue request))]
    (default-layout
      [:h1 "Send URLS to /goto"]
      [:p "Sent URLS will (eventually) be openned in your browser of choice"]
      [:pre {} stats]
      [:form {:method "post"
              :action "/goto"}
       [:input {:type "text" :name "url"}]
       [:input {:type "submit"}]])))

(defn goto
  [url request]
  (timbre/info "got url:" url)
  (queue/enqueue-url (::queue request) url)
  (redirect-after-post "/"))

(defroutes app-routes
  (POST "/goto" [url :as req] (goto url req))
  (GET "/" request (index request)))

(defn wrap-app-component [f queue]
  (fn [req]
    (f (assoc req ::queue queue))))

(defn make-handler [queue prone-enabled?]
  (cond-> (site app-routes)
    prone-enabled? prone/wrap-exceptions
    true (wrap-app-component queue)
    true (wrap-defaults api-defaults)))
