(ns sasara.model.setting
  (:require [sasara.db :as db]))

(defn get-setting [key]
  (:value (db/execute-one!
           ["SELECT value FROM site_settings WHERE key = ?" key])))

(defn set-setting! [key value]
  (db/execute-one!
   ["INSERT INTO site_settings (key, value, updated_at)
     VALUES (?, ?, NOW())
     ON CONFLICT (key) DO UPDATE SET value = ?, updated_at = NOW()"
    key value value]))

(defn get-all []
  (into {}
        (map (juxt :key :value))
        (db/execute!
         ["SELECT key, value FROM site_settings ORDER BY key"])))
