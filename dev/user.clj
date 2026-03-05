(ns user
  (:require [sasara.core :as core]
            [sasara.config :as config]
            [sasara.db :as db]
            [sasara.model.user :as user-model]
            [sasara.model.post :as post-model]
            [sasara.model.site :as site-model]
            [sasara.model.user-site :as user-site-model]
            [migratus.core :as migratus]))

(defn start!
  "Start the server from REPL."
  []
  (let [cfg (config/load-config)]
    (core/start-server! cfg)))

(defn stop!
  "Stop the server from REPL."
  []
  (core/stop-server!))

(defn restart!
  "Restart the server."
  []
  (stop!)
  (start!))

(defn migrate!
  "Run pending migrations."
  []
  (let [cfg (config/load-config)]
    (migratus/migrate (:migratus cfg))))

(defn rollback!
  "Rollback last migration."
  []
  (let [cfg (config/load-config)]
    (migratus/rollback (:migratus cfg))))

(defn seed!
  "Create initial superadmin user, default site, and sample data."
  []
  ;; デフォルトサイト作成
  (println "Creating default site...")
  (let [site (site-model/create! {:name   "My Site"
                                   :slug   "my-site"
                                   :domain "localhost:3000"})
        site-id (:id site)]
    ;; superadmin ユーザー作成
    (println "Creating superadmin user...")
    (let [admin-user (user-model/create! {:username      "admin"
                                           :email         "admin@example.com"
                                           :password      "admin"
                                           :is-superadmin true})]
      ;; サイトに admin ロールで割り当て
      (user-site-model/add-user! (:id admin-user) site-id "admin")

      ;; サンプル記事作成
      (println "Creating sample posts...")
      (post-model/create! {:title     "Hello, Sasara!"
                            :content   "# Welcome\n\nThis is your first post on **Sasara**, a Clojure-powered CMS.\n\n## Features\n\n- Markdown support\n- Tailwind CSS\n- Admin panel\n\nEnjoy writing!"
                            :excerpt   "Welcome to Sasara, a Clojure-powered CMS."
                            :status    "published"
                            :author-id (:id admin-user)
                            :site-id   site-id})
      (post-model/create! {:title     "Clojure CMSを作っている話"
                            :content   "# Clojure CMS開発記\n\nClojure製のCMSを一から作っています。\n\n技術スタック:\n- Ring + Reitit\n- Hiccup + Tailwind CSS\n- PostgreSQL\n- Flexmark (Markdown)"
                            :excerpt   "Clojure製CMSの開発過程を公開します。"
                            :status    "draft"
                            :author-id (:id admin-user)
                            :site-id   site-id})))
  (println "Seed complete! Login with: admin / admin"))

(comment
  ;; Quick start:
  (start!)        ; Start server on http://localhost:3000
  (stop!)         ; Stop server
  (restart!)      ; Restart
  (migrate!)      ; Run migrations
  (seed!)         ; Create superadmin + default site + sample posts

  ;; Login with: admin / admin
  ;; Admin panel: http://localhost:3000/admin
  )
