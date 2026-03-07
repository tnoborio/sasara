(ns sasara.storage
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defprotocol Storage
  (put-html! [this path html])
  (delete-html! [this path]))

;; ── ローカルファイルシステム ──────────────────────────────────────────

(defrecord LocalStorage [base-path]
  Storage
  (put-html! [_ path html]
    (let [file (io/file base-path path)]
      (io/make-parents file)
      (spit file html :encoding "UTF-8")
      (log/debug (str "Written: " (.getPath file)))))
  (delete-html! [_ path]
    (let [file (io/file base-path path)]
      (when (.exists file)
        (.delete file)
        (log/debug (str "Deleted: " (.getPath file)))))))

;; ── ファクトリ ────────────────────────────────────────────────────────

(defn create-storage
  "設定からStorageインスタンスを生成する。
   :type :local → LocalStorage
   :type :gcs   → GCSStorage（未実装）"
  [{:keys [type path] :or {type :local path "public"}}]
  (case (keyword type)
    :local (->LocalStorage path)
    (->LocalStorage path)))

;; ── グローバル状態 ────────────────────────────────────────────────────

(defonce ^:private state (atom nil))

(defn init!
  "アプリ起動時にStorageを初期化する。"
  [config]
  (reset! state (create-storage config))
  (log/info (str "Storage initialized: " (:type config) " → " (:path config))))

(defn get-storage
  "現在のStorageインスタンスを返す。"
  []
  @state)
