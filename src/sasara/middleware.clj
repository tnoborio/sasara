(ns sasara.middleware
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.memory :refer [memory-store]]
            [sasara.auth :as auth]))

(defn wrap-base
  "Base middleware stack for the application."
  [handler]
  (let [session-store (memory-store)]
    (-> handler
        auth/wrap-auth
        (wrap-defaults
         (-> site-defaults
             (assoc-in [:session :store] session-store)
             (assoc-in [:session :cookie-attrs :same-site] :lax)
             (assoc-in [:security :anti-forgery] false))))))
