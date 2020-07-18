(ns crg.schema
  "Conatains custom resolvers and a function to provide the full schema.",
  (:require
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.stuartsierra.component :as component]
    [crg.db :as db]
    [clojure.edn :as edn]
    )
  (:import java.util.Base64))

(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn encode [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn decode [to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))

(defn article->edge
  [[offset article]]
  {:cursor (encode (str offset))
   :node   article})

(defn articles->article-edges
  [articles start_offset end_offset]
  (->> articles
       (map vector (range start_offset end_offset))
       (map article->edge)))

(defn resolve-articles
  [db]
  (fn [_ args _]
    (let [offset (if-let [cursor (:cursor args)]
                   (parse-int (decode cursor))
                   0)
          total_count (dec ((db/article-count (:ds db) args) :count))
          articles (db/articles (:ds db) (merge args {:offset offset}))
          end_offset (+ offset (count articles))]
      {:edges       (articles->article-edges articles offset end_offset)
       :page_info   {:end_cursor    (encode (str end_offset))
                     :has_next_page (< end_offset total_count)}
       :total_count total_count})))

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
    {:query/article-by-slug    (resolve-article-by-slug db)
     :query/articles           (resolve-articles db)
     :article/tags-for-article (resolve-tags-for-article db)}))

(defn scalar-map
  []
  {
   :scalar/date-time-parse     serialze-date-time
   :scalar/date-time-serialize serialze-date-time
   })

(defn enum-map
  []
  {:sort_order {:parse     identity
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
