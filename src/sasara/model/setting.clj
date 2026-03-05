(ns sasara.model.setting
  (:require [sasara.db :as db]))

(defn get-setting [site-id key]
  (:value (db/execute-one!
           ["SELECT value FROM site_settings WHERE site_id = ? AND key = ?"
            site-id key])))

(defn set-setting! [site-id key value]
  (db/execute-one!
   ["INSERT INTO site_settings (site_id, key, value, updated_at)
     VALUES (?, ?, ?, NOW())
     ON CONFLICT (site_id, key) DO UPDATE SET value = ?, updated_at = NOW()"
    site-id key value value]))

(defn get-all [site-id]
  (into {}
        (map (juxt :key :value))
        (db/execute!
         ["SELECT key, value FROM site_settings WHERE site_id = ? ORDER BY key"
          site-id])))
