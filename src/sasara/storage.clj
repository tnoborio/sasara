(ns sasara.storage
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defprotocol Storage
  (put-html!   [this path html])
  (delete-html! [this path])
  (put-image!  [this rel-path input-stream]))

;; ── Local filesystem ─────────────────────────────────────────────────

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
        (log/debug (str "Deleted: " (.getPath file))))))
  (put-image! [_ rel-path input-stream]
    (let [file (io/file base-path rel-path)]
      (io/make-parents file)
      (io/copy input-stream file)
      (log/debug (str "Image written: " (.getPath file))))))

;; ── Factory ───────────────────────────────────────────────────────────

(defn create-storage
  "Create a Storage instance from config.
   :type :local → LocalStorage
   :type :gcs   → GCSStorage (not yet implemented)"
  [{:keys [type path] :or {type :local path "public"}}]
  (case (keyword type)
    :local (->LocalStorage path)
    (->LocalStorage path)))

;; ── Global state ──────────────────────────────────────────────────────

(defonce ^:private state (atom nil))

(defn init!
  "Initialize storage at application startup."
  [config]
  (reset! state (create-storage config))
  (log/info (str "Storage initialized: " (:type config) " → " (:path config))))

(defn get-storage
  "Return the current Storage instance."
  []
  @state)
