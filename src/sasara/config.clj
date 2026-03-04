(ns sasara.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(defn load-config
  "Load configuration from config.edn using Aero."
  ([]
   (load-config "config.edn"))
  ([path]
   (aero/read-config (io/file path))))
