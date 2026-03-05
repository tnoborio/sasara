(ns sasara.handler.public
  (:require [sasara.model.post :as post]
            [sasara.view.public.home :as home-view]
            [sasara.view.public.about :as about-view]
            [sasara.view.public.blog :as blog-view]
            [ring.util.response :as response]))

(defn- site-id [request]
  (get-in request [:current-site :id]))

(defn home [request]
  (let [recent-posts (post/find-published (site-id request) {:limit 5})]
    (-> (home-view/page {:recent-posts recent-posts})
        (response/response)
        (response/content-type "text/html; charset=utf-8"))))

(defn about [_request]
  (-> (about-view/page {})
      (response/response)
      (response/content-type "text/html; charset=utf-8")))

(defn blog-index [request]
  (let [posts (post/find-published (site-id request))]
    (-> (blog-view/list-page {:posts posts})
        (response/response)
        (response/content-type "text/html; charset=utf-8"))))

(defn blog-show [request]
  (let [slug (get-in request [:path-params :slug])
        p    (post/find-by-slug (site-id request) slug)]
    (if (and p (= "published" (:status p)))
      (-> (blog-view/show-page {:post p})
          (response/response)
          (response/content-type "text/html; charset=utf-8"))
      (-> (response/not-found "Not Found")
          (response/content-type "text/html; charset=utf-8")))))
