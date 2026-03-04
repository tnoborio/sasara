(ns sasara.routes
  (:require [reitit.ring :as ring]
            [sasara.handler.public :as public]
            [sasara.handler.admin :as admin]
            [sasara.auth :as auth]))

(defn app-routes []
  (ring/ring-handler
   (ring/router
    [;; Public routes
     ["/" {:get public/home}]
     ["/about" {:get public/about}]
     ["/blog" {:get public/blog-index}]
     ["/blog/:slug" {:get public/blog-show}]

     ;; Admin auth (no auth required)
     ["/admin/login" {:get  admin/login-page
                      :post admin/login-submit}]

     ;; Admin routes (auth required)
     ["/admin" {:middleware [auth/require-auth]}
      ["" {:get admin/dashboard}]
      ["/logout" {:post admin/logout}]
      ["/posts" {:get  admin/posts-index
                 :post admin/posts-create}]
      ["/posts/new" {:get admin/posts-new}]
      ["/posts/:id" {:get  admin/posts-edit
                     :post admin/posts-update}]
      ["/posts/:id/delete" {:post admin/posts-delete}]]]
    ;; Allow conflicting routes (e.g. /posts/new vs /posts/:id)
    {:conflicts nil})

   ;; Default handler (static files + 404)
   (ring/routes
    (ring/create-resource-handler {:path "/"
                                   :root "public"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404
                              :headers {"Content-Type" "text/html"}
                              :body "<h1>404 Not Found</h1>"})}))))
