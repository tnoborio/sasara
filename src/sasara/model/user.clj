(ns sasara.model.user
  (:require [sasara.db :as db]
            [buddy.hashers :as hashers]))

(defn find-by-username [username]
  (db/execute-one!
   ["SELECT * FROM users WHERE username = ?" username]))

(defn find-by-id [id]
  (db/execute-one!
   ["SELECT * FROM users WHERE id = ?" id]))

(defn authenticate
  "Authenticate a user by username and password.
   Returns the user map (without password_hash) on success, nil on failure."
  [username password]
  (when-let [user (find-by-username username)]
    (when (hashers/check password (:password-hash user))
      (dissoc user :password-hash))))

(defn create!
  "Create a new user. Password will be hashed."
  [{:keys [username email password role]
    :or   {role "admin"}}]
  (db/execute-one!
   ["INSERT INTO users (username, email, password_hash, role)
     VALUES (?, ?, ?, ?)
     RETURNING id, username, email, role, created_at"
    username email (hashers/derive password) role]))
