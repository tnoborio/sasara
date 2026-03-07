(ns sasara.core
  (:require [ring.adapter.jetty :as jetty]
            [sasara.config :as config]
            [sasara.db :as db]
            [sasara.storage :as storage]
            [sasara.routes :as routes]
            [sasara.middleware :as middleware]
            [migratus.core :as migratus]
            [clojure.tools.logging :as log])
  (:gen-class))

(defonce ^:private server (atom nil))

(defn start-server!
  "Start the Jetty server."
  [{:keys [server database migratus] :as config}]
  ;; Initialize database
  (log/info "Initializing database connection...")
  (db/init! database)

  ;; Initialize storage
  (log/info "Initializing storage...")
  (storage/init! (:storage config))

  ;; Run migrations
  (log/info "Running database migrations...")
  (migratus/migrate migratus)

  ;; Start server
  (let [{:keys [port host]} server
        default-site-id (:default-site-id config)
        handler (-> (routes/app-routes)
                    (middleware/wrap-base {:default-site-id default-site-id}))]
    (log/info (str "Starting Sasara on http://" host ":" port))
    (reset! sasara.core/server
            (jetty/run-jetty handler
                             {:port  port
                              :host  host
                              :join? false}))))

(defn stop-server!
  "Stop the Jetty server."
  []
  (when-let [s @server]
    (log/info "Stopping Sasara server...")
    (.stop s)
    (reset! server nil))
  (db/shutdown!))

(defn -main
  "Application entry point."
  [& _args]
  (let [config (config/load-config)]
    (start-server! config)
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. ^Runnable stop-server!))))
