(ns renfield.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [renfield.systems :as systems]))

(defn -main [& args]
  (component/start (systems/prod-system)))
