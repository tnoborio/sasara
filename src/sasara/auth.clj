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
