(ns sasara.model.media
  (:require [sasara.db :as db]))

(defn find-all [site-id]
  (db/execute!
   ["SELECT * FROM media WHERE site_id = ? ORDER BY created_at DESC" site-id]))

(defn find-by-id [id]
  (db/execute-one!
   ["SELECT * FROM media WHERE id = ?" id]))

(defn create! [{:keys [site-id filename original-name content-type size-bytes url alt-text uploaded-by]}]
  (db/execute-one!
   ["INSERT INTO media (site_id, filename, original_name, content_type, size_bytes, url, alt_text, uploaded_by)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING *"
    site-id filename original-name content-type size-bytes url alt-text uploaded-by]))

(defn delete! [id]
  (db/execute-one!
   ["DELETE FROM media WHERE id = ? RETURNING id, url" id]))
