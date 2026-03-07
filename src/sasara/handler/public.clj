(ns sasara.handler.public
  (:require [sasara.model.post :as post]
            [sasara.model.page :as page]
            [sasara.view.template :as template]
            [ring.util.response :as response]))

(defn- site-id [request]
  (get-in request [:current-site :id]))

(defn- html [body]
  (-> (response/response body)
      (response/content-type "text/html; charset=utf-8")))

(defn home [request]
  (let [sid  (site-id request)
        tmpl (template/resolve-template sid)
        p    (page/find-by-slug sid "home")]
    (if (and p (= "published" (:status p)))
      (html (template/render tmpl :render-page
                             (template/build-ctx sid {:page p})))
      ;; No home page found: render site name + latest posts
      (let [posts (post/find-published sid)]
        (html (template/render tmpl :render-home
                               (template/build-ctx sid {:posts posts})))))))

(defn blog-index [request]
  (let [sid   (site-id request)
        posts (post/find-published sid)
        tmpl  (template/resolve-template sid)]
    (html (template/render tmpl :render-blog-list
                           (template/build-ctx sid {:posts posts})))))

(defn blog-show [request]
  (let [sid  (site-id request)
        slug (get-in request [:path-params :slug])
        p    (post/find-by-slug sid slug)
        tmpl (template/resolve-template sid)]
    (if (and p (= "published" (:status p)))
      (html (template/render tmpl :render-blog-show
                             (template/build-ctx sid {:post p})))
      (html (template/render tmpl :render-not-found
                             (template/build-ctx sid {}))))))
