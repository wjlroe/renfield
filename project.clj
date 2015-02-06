(defproject renfield "0.1.0-SNAPSHOT"
  :description "Send URLS to other computers"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.danielsz/system "0.1.4"]
                 [compojure "1.3.1"]
                 [hiccup "1.0.5"]
                 [environ "1.0.0"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [factual/durable-queue "0.1.3"]
                 [prone "0.8.0"]
                 [com.taoensso/timbre "3.3.1"]]
  :plugins [[lein-ring "0.9.1"]
            [lein-environ "1.0.0"]])
