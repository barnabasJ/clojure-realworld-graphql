(ns crg.server
    (:require [com.stuartsierra.component :as component]
      [com.walmartlabs.lacinia.pedestal :as lp]
      [io.pedestal.http :as http]))

(defn placeholder [map]
  (print map)
  map)

(defn merge-values [map host]
  (merge map {::http/host host}))

(defrecord Server [schema-provider server port host]

           component/Lifecycle
           (start [this]
                  (assoc this :server (-> schema-provider
                                          :schema
                                          (lp/service-map {:graphiql true
                                                           :port     port})
                                          (merge-values host)
                                          (placeholder)
                                          http/create-server
                                          http/start)))
           (stop [this]
                 (http/stop server)
                 (assoc this :server nil)))

(defn new-server
      []
      {:server (component/using (map->Server {
                                              :port 8888
                                              :host "0.0.0.0"})
                                [:schema-provider])})
