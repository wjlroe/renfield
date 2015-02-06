(ns renfield.receiver
  (:require [clojure.core.async :as async
             :refer [chan timeout alts! go >!]]
            [clojure.java.shell :refer [sh]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.string :refer [join]]
            [com.stuartsierra.component :as component]
            [renfield
             [queue :refer [get-url mark-done retry-task]]]
            [taoensso.timbre :as timbre]))

(defn successful?
  [result]
  (= 0 (:exit result)))

(defmacro do-cond->
  [form & clauses]
  `(let [result# ~form]
     (do (cond-> result#
           ~@clauses)
         result#)))

(defn log-cmd
  [cmd result]
  (do-cond->
   (assoc result :cmd cmd)
   successful? #(timbre/info %)
   :else #(timbre/error %)))

(defn open-url
  [url]
  (let [cmd ["open" url]]
    (log-cmd (join " " cmd)
             (apply sh cmd))))

(defn dequeue-url
  [queue]
  (let [item (get-url queue)]
    (when-not (keyword? item)
      (try
        (open-url (:url @item))
        (mark-done item)
        (catch Exception e
          (println e)
          (print-stack-trace e)
          (retry-task item))))))

(defn shuttle-urls-from-queue
  [_ queue-component recv-agent cmd-chan]
  (let [t (timeout 10)]
    (go
      (let [[v ch] (alts! [cmd-chan t])]
        (when-not (and (= ch cmd-chan)
                       (= v :stop))
          (let [{:keys [queue]} queue-component]
            (dequeue-url queue)
            (send-off recv-agent
                      shuttle-urls-from-queue
                      queue-component
                      recv-agent
                      cmd-chan)))))))

(defn log-error
  [_ err]
  (timbre/error err))

(defrecord Receiver [queue recv-agent cmd-chan]
  component/Lifecycle
  (start [this]
    (let [recv-agent (agent [] :error-handler log-error)
          cmd-chan (chan)]
      (send-off recv-agent
                shuttle-urls-from-queue
                queue
                recv-agent
                cmd-chan)
      (assoc this
             :recv-agent recv-agent
             :cmd-chan cmd-chan)))
  (stop [this]
    (when cmd-chan
      (go (>! cmd-chan :stop))
      (when recv-agent
        (await recv-agent)))))

(defn new-receiver
  []
  (component/using
   (map->Receiver {})
   [:queue]))
