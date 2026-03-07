(ns sasara.model.page
  (:require [sasara.db :as db]
            [sasara.markdown :as md]
            [clojure.string :as str]))

(defn find-all [site-id]
  (db/execute!
   ["SELECT * FROM pages WHERE site_id = ?
     ORDER BY sort_order ASC, created_at DESC"
    site-id]))

(defn find-published [site-id]
  (db/execute!
   ["SELECT * FROM pages WHERE site_id = ? AND status = 'published'
     ORDER BY sort_order ASC"
    site-id]))

(defn find-by-id [id]
  (db/execute-one!
   ["SELECT * FROM pages WHERE id = ?" id]))

(defn find-by-slug [site-id slug]
  (db/execute-one!
   ["SELECT * FROM pages WHERE site_id = ? AND slug = ?" site-id slug]))

(defn- slugify [s]
  (-> s
      str/lower-case
      (str/replace #"[^\w\s-]" "")
      (str/replace #"\s+" "-")
      (str/replace #"-+" "-")))

(defn create! [{:keys [title slug content status sort-order site-id]
                :or   {status "draft" sort-order 0}}]
  (let [slug         (or (when-not (str/blank? slug) slug) (slugify title))
        content-html (md/md->html content)]
    (db/execute-one!
     ["INSERT INTO pages (title, slug, content, content_html, status, sort_order, site_id)
       VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *"
      title slug content content-html status sort-order site-id])))

(defn update! [id {:keys [title slug content status sort-order]}]
  (let [content-html (when content (md/md->html content))]
    (db/execute-one!
     ["UPDATE pages
       SET title        = COALESCE(?, title),
           slug         = COALESCE(?, slug),
           content      = COALESCE(?, content),
           content_html = COALESCE(?, content_html),
           status       = COALESCE(?, status),
           sort_order   = COALESCE(?, sort_order),
           updated_at   = NOW()
       WHERE id = ? RETURNING *"
      title slug content content-html status sort-order id])))

(defn delete! [id]
  (db/execute-one!
   ["DELETE FROM pages WHERE id = ? RETURNING id" id]))
