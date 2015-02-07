(ns renfield.queue
  (:require [com.stuartsierra.component :as component]
            [durable-queue :refer :all]))

;; TODO: configrable queue directory - prod would be /var/queues/...
;; in dev - local (tmp?)

(defrecord Queue [directory queue]
  component/Lifecycle
  (start [this]
    (assoc this :queue (queues directory)))
  (stop [this]
    (fsync queue)
    this))

(defn new-queue
  [directory]
  (map->Queue {:directory directory}))

(defn enqueue-url
  [q url]
  (let [{:keys [queue]} q]
    (put! queue :urls {:url url})))

(defn get-url
  [q]
  (take! q :urls 10 :timed-out!))

(def mark-done complete!)
(def retry-task retry!)

(defn q-stats
  [q]
  (let [{:keys [queue]} q]
    (stats queue)))
