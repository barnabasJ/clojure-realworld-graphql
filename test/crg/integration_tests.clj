(ns crg.integration-tests
     (:require
       [clojure.test :refer [deftest is]]
       [crg.system :as system]
       [crg.test-utils :refer [simplify]]
       [com.stuartsierra.component :as component]
       [com.walmartlabs.lacinia :as lacinia]))

  (defn ^:private test-system
                  "Creates a new system suitable for testing, and ensures that
                  the HTTP port won't conflict with a default running system."
                  []
                  (-> (system/new-system)
                      (assoc-in [:server :port] 8989)))

  (defn ^:private q
                  "Extracts the compiled schema and executes a query."
                  [system query variables]
                  (-> system
                      (get-in [:schema-provider :schema])
                      (lacinia/execute query variables nil)
                      simplify))

  (deftest can-query-for-article-by-slug
           (let [system (component/start-system (test-system))
                 results (q system
                            "{ article(slug: \"website\") { id title tags  }}"
                            nil)]
             (is (= {:data {:article {:id 280, :title "Sunset Strip", :tags ["Stand-alone"]}}}
                    results))
             (component/stop-system system)))
