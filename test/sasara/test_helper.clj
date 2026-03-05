(ns sasara.test-helper
  (:require [sasara.config :as config]
            [sasara.db :as db]
            [migratus.core :as migratus]))

(def test-config
  "テスト用設定。DB名を sasara_test にする"
  (-> (config/load-config)
      (assoc-in [:database :dbname] "sasara_test")))

(defn init-test-db!
  "テストDB初期化（マイグレーション実行）"
  []
  (db/init! (:database test-config))
  (migratus/migrate (assoc (:migratus test-config)
                           :db (:database test-config))))

(defn clean-tables!
  "全テーブルのデータを削除（テスト間のリセット用）"
  []
  (db/execute! ["DELETE FROM post_tags"])
  (db/execute! ["DELETE FROM tags"])
  (db/execute! ["DELETE FROM media"])
  (db/execute! ["DELETE FROM posts"])
  (db/execute! ["DELETE FROM pages"])
  (db/execute! ["DELETE FROM works"])
  (db/execute! ["DELETE FROM services"])
  (db/execute! ["DELETE FROM site_settings"])
  (db/execute! ["DELETE FROM user_sites"])
  (db/execute! ["DELETE FROM users"])
  (db/execute! ["DELETE FROM sites"]))

(defn with-test-db
  "テストフィクスチャ: DB初期化 → テスト実行 → クリーンアップ"
  [f]
  (init-test-db!)
  (try
    (f)
    (finally
      (clean-tables!))))
