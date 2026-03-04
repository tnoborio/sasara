(ns sasara.model.post
  (:require [sasara.db :as db]
            [sasara.markdown :as md]))

(defn find-all
  "Get all posts, ordered by created_at desc."
  ([]
   (find-all {}))
  ([{:keys [status limit offset]
     :or   {limit 20 offset 0}}]
   (if status
     (db/execute!
      ["SELECT * FROM posts WHERE status = ?
        ORDER BY created_at DESC LIMIT ? OFFSET ?"
       status limit offset])
     (db/execute!
      ["SELECT * FROM posts ORDER BY created_at DESC LIMIT ? OFFSET ?"
       limit offset]))))

(defn find-published
  "Get published posts, ordered by published_at desc."
  ([]
   (find-published {}))
  ([{:keys [limit offset] :or {limit 20 offset 0}}]
   (db/execute!
    ["SELECT * FROM posts WHERE status = 'published'
      ORDER BY published_at DESC LIMIT ? OFFSET ?"
     limit offset])))

(defn find-by-id [id]
  (db/execute-one!
   ["SELECT * FROM posts WHERE id = ?" id]))

(defn find-by-slug [slug]
  (db/execute-one!
   ["SELECT * FROM posts WHERE slug = ?" slug]))

(defn- slugify [s]
  (-> s
      clojure.string/lower-case
      (clojure.string/replace #"[^\w\s-]" "")
      (clojure.string/replace #"\s+" "-")
      (clojure.string/replace #"-+" "-")))

(defn create!
  [{:keys [title slug content excerpt status author-id]
    :or   {status "draft"}}]
  (let [slug         (or slug (slugify title))
        content-html (md/md->html content)]
    (db/execute-one!
     ["INSERT INTO posts (title, slug, content, content_html, excerpt, status, author_id, published_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, CASE WHEN ? = 'published' THEN NOW() ELSE NULL END)
       RETURNING *"
      title slug content content-html excerpt status author-id status])))

(defn update!
  [id {:keys [title slug content excerpt status]}]
  (let [content-html (when content (md/md->html content))]
    (db/execute-one!
     ["UPDATE posts
       SET title = COALESCE(?, title),
           slug = COALESCE(?, slug),
           content = COALESCE(?, content),
           content_html = COALESCE(?, content_html),
           excerpt = COALESCE(?, excerpt),
           status = COALESCE(?, status),
           published_at = CASE
             WHEN ? = 'published' AND published_at IS NULL THEN NOW()
             ELSE published_at
           END,
           updated_at = NOW()
       WHERE id = ?
       RETURNING *"
      title slug content content-html excerpt status status id])))

(defn delete! [id]
  (db/execute-one!
   ["DELETE FROM posts WHERE id = ? RETURNING id" id]))
