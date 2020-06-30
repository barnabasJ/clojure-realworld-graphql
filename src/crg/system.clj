(ns crg.system
  (:require
    [com.stuartsierra.component :as component]
    [crg.schema :as schema]
    [crg.db :as db]
    [crg.server :as server]))

(defn new-system
  []
  (merge (component/system-map)
         (db/new-db)
         (server/new-server)
         (schema/new-schema-provider)))

