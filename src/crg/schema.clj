(ns crg.schema
  "Conatains custom resolvers and a function to provide the full schema.",
  (:require
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.stuartsierra.component :as component]
    [crg.db :as db]
    [clojure.edn :as edn]
    ))


(defn resolve-articel-by-slug
  [db]
  (fn [_, args, _]
    (db/article-by-slug (:ds db) args)))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/article-by-slug (resolve-articel-by-slug db)}))

(defn load-schema
  [component]
  (-> (io/resource "crg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [schema]
  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:db]))})
