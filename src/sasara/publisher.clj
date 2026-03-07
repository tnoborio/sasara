(ns sasara.publisher
  (:require [sasara.storage :as storage]
            [sasara.model.post :as post]
            [sasara.model.page :as page]
            [sasara.view.template :as template]
            [clojure.tools.logging :as log]))

;; ── Page output ───────────────────────────────────────────────────────

(defn publish-page!
  "Write a single page as an HTML file.
   'home' slug → public/index.html; others → public/<slug>/index.html"
  [stor site-id p]
  (let [tmpl (template/resolve-template site-id)
        html (template/render tmpl :render-page
                              (template/build-ctx site-id {:page p}))
        path (if (= "home" (:slug p))
               "index.html"
               (str (:slug p) "/index.html"))]
    (storage/put-html! stor path html)))

(defn publish-pages!
  "Write all published pages for a site."
  [stor site-id]
  (doseq [p (page/find-published site-id)]
    (publish-page! stor site-id p)))

;; ── Blog output ───────────────────────────────────────────────────────

(defn publish-post!
  "Write a single post as an HTML file.
   Output: public/blog/<slug>/index.html"
  [stor site-id p]
  (let [tmpl (template/resolve-template site-id)
        html (template/render tmpl :render-blog-show
                              (template/build-ctx site-id {:post p}))]
    (storage/put-html! stor (str "blog/" (:slug p) "/index.html") html)))

(defn publish-blog-index!
  "Write the blog listing page.
   Output: public/blog/index.html"
  [stor site-id]
  (let [posts (post/find-published site-id)
        tmpl  (template/resolve-template site-id)
        html  (template/render tmpl :render-blog-list
                               (template/build-ctx site-id {:posts posts}))]
    (storage/put-html! stor "blog/index.html" html)))

;; ── Full publish ──────────────────────────────────────────────────────

(defn publish-site!
  "Generate and write static HTML for the entire site."
  [stor site-id]
  (log/info (str "Publishing site " site-id "..."))
  (publish-pages! stor site-id)
  (publish-blog-index! stor site-id)
  (doseq [p (post/find-published site-id)]
    (publish-post! stor site-id p))
  (log/info "Publishing complete."))

;; ── Incremental publish (called on save) ─────────────────────────────

(defn on-post-save!
  "Called after a post is saved. Regenerates related pages if published."
  [stor site-id p]
  (when (= "published" (:status p))
    (publish-post! stor site-id p)
    (publish-blog-index! stor site-id)
    ;; Regenerate home page if it exists (latest post list may have changed)
    (when-let [home (page/find-by-slug site-id "home")]
      (publish-page! stor site-id home))))

(defn on-page-save!
  "Called after a page is saved. Regenerates the static file if published."
  [stor site-id p]
  (when (= "published" (:status p))
    (publish-page! stor site-id p)))
