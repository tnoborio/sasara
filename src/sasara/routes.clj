(ns sasara.routes
  (:require [reitit.ring :as ring]
            [sasara.handler.public :as public]
            [sasara.handler.admin :as admin]
            [sasara.auth :as auth]
            [ring.middleware.file :as file]))

(defn app-routes []
  (-> (ring/ring-handler
   (ring/router
    [;; Public routes (SSR fallback when no static file exists)
     ["/" {:get public/home}]
     ["/blog" {:get public/blog-index}]
     ["/blog/:slug" {:get public/blog-show}]

     ;; Admin auth (no auth required)
     ["/admin/login" {:get  admin/login-page
                      :post admin/login-submit}]

     ;; Admin routes (auth required)
     ["/admin" {:middleware [auth/require-auth]}
      ["" {:get admin/site-selector}]
      ["/select-site/:id" {:post admin/select-site}]
      ["/dashboard" {:get admin/dashboard}]
      ["/logout" {:post admin/logout}]
      ["/publish" {:post admin/publish-all}]
      ["/posts" {:get  admin/posts-index
                 :post admin/posts-create}]
      ["/posts/new" {:get admin/posts-new}]
      ["/posts/:id" {:get  admin/posts-edit
                     :post admin/posts-update}]
      ["/posts/:id/delete" {:post admin/posts-delete}]
      ["/pages" {:get  admin/pages-index
                 :post admin/pages-create}]
      ["/pages/new" {:get admin/pages-new}]
      ["/pages/:id" {:get  admin/pages-edit
                     :post admin/pages-update}]
      ["/pages/:id/delete" {:post admin/pages-delete}]
      ["/media" {:get  admin/media-index}]
      ["/media/upload" {:post admin/media-upload}]
      ["/media/picker" {:get admin/media-picker}]
      ["/media/:id/delete" {:post admin/media-delete}]
      ["/settings" {:get  admin/settings-index
                    :post admin/settings-update}]

      ;; Superadmin routes
      ["/super" {:middleware [auth/require-superadmin]}
       ["/sites" {:get  admin/super-sites-index
                  :post admin/super-sites-create}]
       ["/sites/new" {:get admin/super-sites-new}]
       ["/sites/:id" {:get  admin/super-sites-edit
                      :post admin/super-sites-update}]
       ["/sites/:id/delete" {:post admin/super-sites-delete}]
       ["/users" {:get  admin/super-users-index
                  :post admin/super-users-create}]
       ["/users/new" {:get admin/super-users-new}]
       ["/users/:id" {:get  admin/super-users-edit
                      :post admin/super-users-update}]
       ["/users/:id/delete" {:post admin/super-users-delete}]]]]
    {:conflicts nil})

   ;; Default handler (static files + 404)
   (ring/routes
    (ring/create-resource-handler {:path "/"
                                   :root "public"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404
                              :headers {"Content-Type" "text/html"}
                              :body "<h1>404 Not Found</h1>"})})))
      (file/wrap-file "public")))
