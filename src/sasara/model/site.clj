(ns sasara.model.site
  (:require [sasara.db :as db]))

(defn find-all []
  (db/execute!
   ["SELECT * FROM sites ORDER BY name"]))

(defn find-by-id [id]
  (db/execute-one!
   ["SELECT * FROM sites WHERE id = ?" id]))

(defn find-by-slug [slug]
  (db/execute-one!
   ["SELECT * FROM sites WHERE slug = ?" slug]))

(defn find-by-domain [domain]
  (db/execute-one!
   ["SELECT * FROM sites WHERE domain = ?" domain]))

(defn- slugify [s]
  (-> s
      clojure.string/lower-case
      (clojure.string/replace #"[^\w\s-]" "")
      (clojure.string/replace #"\s+" "-")
      (clojure.string/replace #"-+" "-")))

(defn create! [{:keys [name slug domain]}]
  (let [slug (or slug (slugify name))]
    (db/execute-one!
     ["INSERT INTO sites (name, slug, domain)
       VALUES (?, ?, ?)
       RETURNING *"
      name slug domain])))

(defn update! [id {:keys [name slug domain]}]
  (db/execute-one!
   ["UPDATE sites
     SET name = COALESCE(?, name),
         slug = COALESCE(?, slug),
         domain = COALESCE(?, domain),
         updated_at = NOW()
     WHERE id = ?
     RETURNING *"
    name slug domain id]))

(defn delete! [id]
  (db/execute-one!
   ["DELETE FROM sites WHERE id = ? RETURNING id" id]))
