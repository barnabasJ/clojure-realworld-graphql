(ns crg.core
  (:gen-class)
  (:require [crg.system :as system]
            [com.stuartsierra.component :as component]))

(defn -main [& args]
  (component/start (system/new-system)))
