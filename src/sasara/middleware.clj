(ns sasara.middleware
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.memory :refer [memory-store]]
            [sasara.auth :as auth]
            [sasara.model.site :as site]
            [sasara.model.user-site :as user-site]))

(defn wrap-current-site
  "管理画面用: セッションの site-id からサイト情報とロールをリクエストに注入"
  [handler]
  (fn [request]
    (let [site-id (get-in request [:session :current-site-id])
          user-id (get-in request [:session :identity :id])]
      (if (and site-id user-id)
        (let [current-site (site/find-by-id site-id)
              role         (user-site/get-role user-id site-id)]
          (handler (assoc request
                          :current-site current-site
                          :current-site-role role)))
        (handler request)))))

(defn wrap-resolve-site
  "公開サイト用: ドメインまたはデフォルト設定からサイトを解決"
  [default-site-id]
  (fn [handler]
    (fn [request]
      (let [host    (get-in request [:headers "host"])
            ;; ドメインからサイトを検索、見つからなければデフォルト
            current-site (or (when host (site/find-by-domain host))
                             (when default-site-id (site/find-by-id default-site-id)))]
        (handler (assoc request :current-site current-site))))))

(defn wrap-base
  "Base middleware stack for the application."
  [handler {:keys [default-site-id]}]
  (let [session-store (memory-store)]
    (-> handler
        wrap-current-site
        ((wrap-resolve-site default-site-id))
        auth/wrap-auth
        (wrap-defaults
         (-> site-defaults
             (assoc-in [:session :store] session-store)
             (assoc-in [:session :cookie-attrs :same-site] :lax)
             (assoc-in [:security :anti-forgery] false))))))
