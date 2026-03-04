(ns user
  (:require [sasara.core :as core]
            [sasara.config :as config]
            [sasara.db :as db]
            [sasara.model.user :as user-model]
            [sasara.model.post :as post-model]
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
  "Create initial admin user and sample data."
  []
  (println "Creating admin user...")
  (user-model/create! {:username "admin"
                        :email    "admin@example.com"
                        :password "admin"})
  (println "Creating sample posts...")
  (post-model/create! {:title     "Hello, Sasara!"
                        :content   "# Welcome\n\nThis is your first post on **Sasara**, a Clojure-powered CMS.\n\n## Features\n\n- Markdown support\n- Tailwind CSS\n- Admin panel\n\nEnjoy writing!"
                        :excerpt   "Welcome to Sasara, a Clojure-powered CMS."
                        :status    "published"
                        :author-id 1})
  (post-model/create! {:title     "Clojure CMSを作っている話"
                        :content   "# Clojure CMS開発記\n\nClojure製のCMSを一から作っています。\n\n技術スタック:\n- Ring + Reitit\n- Hiccup + Tailwind CSS\n- PostgreSQL\n- Flexmark (Markdown)"
                        :excerpt   "Clojure製CMSの開発過程を公開します。"
                        :status    "draft"
                        :author-id 1})
  (println "Seed complete!"))

(comment
  ;; Quick start:
  (start!)        ; Start server on http://localhost:3000
  (stop!)         ; Stop server
  (restart!)      ; Restart
  (seed!)         ; Create admin user + sample posts

  ;; Login with: admin / admin
  ;; Admin panel: http://localhost:3000/admin
  )
