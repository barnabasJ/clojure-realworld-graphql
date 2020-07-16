(ns crg.db
  (:require [hugsql.core :as hugsql]
            [com.stuartsierra.component :as component])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn ^:private pooled-data-source
  [host dbname user password port]
  {:datasource
   (doto (ComboPooledDataSource.)
     (.setDriverClass "org.postgresql.Driver")
     (.setJdbcUrl (str "jdbc:postgresql://" host ":" port "/" dbname))
     (.setUser user)
     (.setPassword password))})

(defrecord ClojureGameGeekDb [ds]

  component/Lifecycle

  (start [this]
    (assoc this
      :ds (pooled-data-source
            (or (System/getenv "DB_HOST") "localhost")
            (or (System/getenv "DB_NAME") "real_world_dev")
            (or (System/getenv "DB_USER") "postgres")
            (or (System/getenv "DB_PASS") "postgres")
            (or (System/getenv "DB_PORT") 5432))))

  (stop [this]
    (-> ds :datasource .close)
    (assoc this :ds nil)))

(defn new-db
  []
  {:db (map->ClojureGameGeekDb {})})

(hugsql/def-db-fns "sql/content.sql")

(hugsql/def-sqlvec-fns "sql/content.sql")
