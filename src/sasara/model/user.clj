(ns sasara.model.user
  (:require [sasara.db :as db]
            [buddy.hashers :as hashers]))

(defn find-all []
  (db/execute!
   ["SELECT id, username, email, role, is_superadmin, created_at, updated_at
     FROM users ORDER BY username"]))

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
  [{:keys [username email password role is-superadmin]
    :or   {role "admin" is-superadmin false}}]
  (db/execute-one!
   ["INSERT INTO users (username, email, password_hash, role, is_superadmin)
     VALUES (?, ?, ?, ?, ?)
     RETURNING id, username, email, role, is_superadmin, created_at"
    username email (hashers/derive password) role is-superadmin]))

(defn update!
  "Update user profile. Password is optional."
  [id {:keys [username email password is-superadmin]}]
  (when (and password (seq password))
    (db/execute-one!
     ["UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?"
      (hashers/derive password) id]))
  (db/execute-one!
   ["UPDATE users
     SET username = COALESCE(?, username),
         email = COALESCE(?, email),
         is_superadmin = COALESCE(?, is_superadmin),
         updated_at = NOW()
     WHERE id = ?
     RETURNING id, username, email, role, is_superadmin"
    username email is-superadmin id]))

(defn delete! [id]
  (db/execute-one!
   ["DELETE FROM users WHERE id = ? RETURNING id" id]))
