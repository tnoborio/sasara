(ns sasara.model.user-site-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [sasara.test-helper :as helper]
            [sasara.model.user :as user]
            [sasara.model.site :as site]
            [sasara.model.user-site :as user-site]))

(use-fixtures :each helper/with-test-db)

(defn- setup!
  "テスト用にユーザーとサイトを作成"
  []
  (let [u  (user/create! {:username "member" :email "m@example.com" :password "pass"})
        s1 (site/create! {:name "Site A" :slug "site-a"})
        s2 (site/create! {:name "Site B" :slug "site-b"})]
    {:user u :site-a s1 :site-b s2}))

(deftest add-user-to-site-test
  (let [{:keys [user site-a]} (setup!)]
    (testing "ユーザーをサイトに追加"
      (user-site/add-user! (:id user) (:id site-a) "admin")
      (is (= "admin" (user-site/get-role (:id user) (:id site-a)))))

    (testing "ロール更新（upsert）"
      (user-site/add-user! (:id user) (:id site-a) "editor")
      (is (= "editor" (user-site/get-role (:id user) (:id site-a)))))))

(deftest find-sites-for-user-test
  (let [{:keys [user site-a site-b]} (setup!)]
    (user-site/add-user! (:id user) (:id site-a) "admin")
    (user-site/add-user! (:id user) (:id site-b) "editor")

    (testing "ユーザーの所属サイト一覧"
      (let [sites (user-site/find-sites-for-user (:id user))]
        (is (= 2 (count sites)))
        (is (= #{"admin" "editor"} (set (map :role sites))))))))

(deftest find-users-for-site-test
  (let [{:keys [user site-a]} (setup!)
        u2 (user/create! {:username "other" :email "o@example.com" :password "pass"})]
    (user-site/add-user! (:id user) (:id site-a) "admin")
    (user-site/add-user! (:id u2) (:id site-a) "editor")

    (testing "サイトの所属ユーザー一覧"
      (let [users (user-site/find-users-for-site (:id site-a))]
        (is (= 2 (count users)))
        (is (= #{"admin" "editor"} (set (map :role users))))))))

(deftest remove-user-from-site-test
  (let [{:keys [user site-a]} (setup!)]
    (user-site/add-user! (:id user) (:id site-a) "admin")
    (user-site/remove-user! (:id user) (:id site-a))

    (testing "削除後はロールがnil"
      (is (nil? (user-site/get-role (:id user) (:id site-a)))))))
