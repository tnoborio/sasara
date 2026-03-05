(ns sasara.model.user-site
  (:require [sasara.db :as db]))

(defn find-sites-for-user
  "ユーザーが所属するサイト一覧（ロール付き）"
  [user-id]
  (db/execute!
   ["SELECT s.*, us.role
     FROM sites s
     JOIN user_sites us ON s.id = us.site_id
     WHERE us.user_id = ?
     ORDER BY s.name"
    user-id]))

(defn find-users-for-site
  "サイトに所属するユーザー一覧（ロール付き）"
  [site-id]
  (db/execute!
   ["SELECT u.id, u.username, u.email, u.is_superadmin, us.role
     FROM users u
     JOIN user_sites us ON u.id = us.user_id
     WHERE us.site_id = ?
     ORDER BY u.username"
    site-id]))

(defn get-role
  "ユーザーのサイト内ロールを取得"
  [user-id site-id]
  (:role (db/execute-one!
          ["SELECT role FROM user_sites WHERE user_id = ? AND site_id = ?"
           user-id site-id])))

(defn add-user!
  "サイトにユーザーを追加"
  [user-id site-id role]
  (db/execute-one!
   ["INSERT INTO user_sites (user_id, site_id, role)
     VALUES (?, ?, ?)
     ON CONFLICT (user_id, site_id) DO UPDATE SET role = ?
     RETURNING *"
    user-id site-id role role]))

(defn remove-user!
  "サイトからユーザーを削除"
  [user-id site-id]
  (db/execute-one!
   ["DELETE FROM user_sites WHERE user_id = ? AND site_id = ? RETURNING user_id"
    user-id site-id]))

(defn update-role!
  "ユーザーのサイト内ロールを変更"
  [user-id site-id role]
  (db/execute-one!
   ["UPDATE user_sites SET role = ? WHERE user_id = ? AND site_id = ? RETURNING *"
    role user-id site-id]))
