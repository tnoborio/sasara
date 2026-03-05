(ns sasara.auth
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [ring.util.response :as response]))

(def backend (session-backend))

(defn wrap-auth
  "Wrap authentication middleware."
  [handler]
  (-> handler
      (wrap-authorization backend)
      (wrap-authentication backend)))

(defn require-auth
  "Middleware that redirects to login if not authenticated."
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (response/redirect "/admin/login"))))

(defn require-superadmin
  "Middleware that requires superadmin privileges."
  [handler]
  (fn [request]
    (if (get-in request [:session :identity :is-superadmin])
      (handler request)
      {:status 403
       :headers {"Content-Type" "text/html"}
       :body "<h1>403 Forbidden</h1>"})))

(defn require-site-role
  "Middleware that requires at least the given role for the current site.
   admin includes editor permissions."
  [required-role]
  (fn [handler]
    (fn [request]
      (let [role (:current-site-role request)]
        (if (or (get-in request [:session :identity :is-superadmin])
                (case required-role
                  "editor" (contains? #{"admin" "editor"} role)
                  "admin"  (= role "admin")
                  false))
          (handler request)
          {:status 403
           :headers {"Content-Type" "text/html"}
           :body "<h1>403 Forbidden</h1>"})))))
