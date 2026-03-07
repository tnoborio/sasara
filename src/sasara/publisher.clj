(ns sasara.publisher
  (:require [sasara.storage :as storage]
            [sasara.model.post :as post]
            [sasara.view.public.home :as home-view]
            [sasara.view.public.blog :as blog-view]
            [clojure.tools.logging :as log]))

;; ── 個別ページ出力 ────────────────────────────────────────────────────

(defn publish-post!
  "単一記事をHTMLファイルとして出力する。
   出力先: public/blog/<slug>/index.html"
  [stor post]
  (let [html (blog-view/show-page {:post post})]
    (storage/put-html! stor (str "blog/" (:slug post) "/index.html") html)))

(defn publish-blog-index!
  "ブログ一覧ページを出力する。
   出力先: public/blog/index.html"
  [stor site-id]
  (let [posts (post/find-published site-id)
        html  (blog-view/list-page {:posts posts})]
    (storage/put-html! stor "blog/index.html" html)))

(defn publish-home!
  "トップページを出力する。
   出力先: public/index.html"
  [stor site-id]
  (let [recent-posts (post/find-published site-id {:limit 5})
        html         (home-view/page {:recent-posts recent-posts})]
    (storage/put-html! stor "index.html" html)))

;; ── 全体パブリッシュ ──────────────────────────────────────────────────

(defn publish-site!
  "サイト全体の静的HTMLを生成・出力する。"
  [stor site-id]
  (log/info (str "Publishing site " site-id "..."))
  (publish-home! stor site-id)
  (publish-blog-index! stor site-id)
  (doseq [p (post/find-published site-id)]
    (publish-post! stor p))
  (log/info "Publishing complete."))

;; ── 増分パブリッシュ（記事作成・更新時に呼ぶ） ────────────────────────

(defn on-post-save!
  "記事保存後に呼び出す。publishedなら関連ページを再生成する。"
  [stor site-id post]
  (when (= "published" (:status post))
    (publish-post! stor post)
    (publish-blog-index! stor site-id)
    (publish-home! stor site-id)))
