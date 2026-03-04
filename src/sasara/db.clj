(ns sasara.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import [com.zaxxer.hikari HikariDataSource]))

(defonce ^:private datasource (atom nil))

(defn ->pool
  "Create a HikariCP connection pool from a db-spec map."
  [{:keys [dbtype host port dbname user password]}]
  (let [ds (doto (HikariDataSource.)
             (.setJdbcUrl (str "jdbc:" dbtype "://" host ":" port "/" dbname))
             (.setUsername user)
             (.setMaximumPoolSize 10)
             (.setMinimumIdle 2))]
    (when (and password (seq password))
      (.setPassword ds password))
    ds))

(defn init!
  "Initialize the connection pool."
  [db-config]
  (reset! datasource (->pool db-config)))

(defn shutdown!
  "Shut down the connection pool."
  []
  (when-let [ds @datasource]
    (.close ds)
    (reset! datasource nil)))

(defn ds
  "Get the current datasource."
  []
  @datasource)

(def ^:private default-opts
  {:builder-fn rs/as-unqualified-kebab-maps})

(defn execute!
  "Execute a SQL statement."
  [sql-params]
  (jdbc/execute! (ds) sql-params default-opts))

(defn execute-one!
  "Execute a SQL statement, returning a single row."
  [sql-params]
  (jdbc/execute-one! (ds) sql-params default-opts))
