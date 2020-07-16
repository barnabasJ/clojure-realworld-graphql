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

(defn resolve-article-by-slug
  [db]
  (fn [context, args, container]
    (db/article-by-slug (:ds db) args)))

(defn resolve-tags-for-article
  [db]
  (fn [context, args, container]
    (->> container
         (db/tags-for-article (:ds db))
         (map #(:name %)))))


(defn serialze-date-time [date-time]
  (. date-time toString))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/article-by-slug (resolve-article-by-slug db)
     :article/tags-for-article (resolve-tags-for-article db)}))

(defn scalar-map
  []
  {
   :scalar/date-time-parse     serialze-date-time
   :scalar/date-time-serialize  serialze-date-time
   })

(defn enum-map
  []
  {:sort_order {:parse identity
   :serialize identity}})

(defn load-schema
  [component]
  (-> (io/resource "crg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      (util/attach-scalar-transformers (scalar-map))
      (util/inject-enum-transformers (enum-map))
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
