(ns sasara.model.user-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [sasara.test-helper :as helper]
            [sasara.model.user :as user]))

(use-fixtures :each helper/with-test-db)

(deftest create-user-test
  (testing "ユーザー作成"
    (let [u (user/create! {:username "testuser"
                           :email    "test@example.com"
                           :password "secret123"})]
      (is (some? (:id u)))
      (is (= "testuser" (:username u)))
      (is (= "test@example.com" (:email u)))
      (is (false? (:is-superadmin u)))))

  (testing "superadmin ユーザー作成"
    (let [u (user/create! {:username      "superuser"
                           :email         "super@example.com"
                           :password      "secret123"
                           :is-superadmin true})]
      (is (true? (:is-superadmin u))))))

(deftest authenticate-test
  (testing "正しいパスワードで認証成功"
    (user/create! {:username "auth-user"
                   :email    "auth@example.com"
                   :password "correct-pass"})
    (let [result (user/authenticate "auth-user" "correct-pass")]
      (is (some? result))
      (is (= "auth-user" (:username result)))
      (is (nil? (:password-hash result)))))

  (testing "間違ったパスワードで認証失敗"
    (is (nil? (user/authenticate "auth-user" "wrong-pass"))))

  (testing "存在しないユーザーで認証失敗"
    (is (nil? (user/authenticate "nobody" "password")))))

(deftest find-user-test
  (testing "ID / username で検索"
    (let [u (user/create! {:username "findme"
                           :email    "find@example.com"
                           :password "pass"})]
      (is (= "findme" (:username (user/find-by-id (:id u)))))
      (is (= "findme" (:username (user/find-by-username "findme"))))
      (is (nil? (user/find-by-username "nonexistent"))))))

(deftest update-user-test
  (testing "ユーザー情報更新"
    (let [u (user/create! {:username "original"
                           :email    "orig@example.com"
                           :password "pass"})
          updated (user/update! (:id u) {:username "renamed"
                                         :email    "new@example.com"
                                         :is-superadmin true})]
      (is (= "renamed" (:username updated)))
      (is (= "new@example.com" (:email updated)))
      (is (true? (:is-superadmin updated)))))

  (testing "パスワード変更"
    (let [u (user/create! {:username "passchange"
                           :email    "pc@example.com"
                           :password "oldpass"})]
      (user/update! (:id u) {:password "newpass"})
      (is (some? (user/authenticate "passchange" "newpass")))
      (is (nil? (user/authenticate "passchange" "oldpass"))))))

(deftest delete-user-test
  (testing "ユーザー削除"
    (let [u (user/create! {:username "deleteme"
                           :email    "del@example.com"
                           :password "pass"})]
      (user/delete! (:id u))
      (is (nil? (user/find-by-id (:id u)))))))
