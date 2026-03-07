(ns sasara.handler.admin-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [sasara.test-helper :as helper]
            [sasara.model.user :as user]
            [sasara.model.site :as site]
            [sasara.model.user-site :as user-site]
            [sasara.handler.admin :as admin]))

(use-fixtures :each helper/with-test-db)

(defn- setup! []
  (let [s (site/create! {:name "Admin Site" :slug "admin-site"})
        u (user/create! {:username      "admin"
                         :email         "admin@example.com"
                         :password      "admin"
                         :is-superadmin true})]
    (user-site/add-user! (:id u) (:id s) "admin")
    {:site s :user u}))

(defn- auth-request
  "認証済みリクエストを作成"
  [{:keys [user site]} & [extra]]
  (merge {:session {:identity        {:id            (:id user)
                                      :username      (:username user)
                                      :is-superadmin (:is-superadmin user)}
                    :current-site-id (:id site)}
          :current-site site
          :current-site-role "admin"}
         extra))

(deftest login-submit-test
  (setup!)

  (testing "正しい認証情報でログイン"
    (let [req {:form-params {"username" "admin" "password" "admin"}}
          res (admin/login-submit req)]
      (is (= 302 (:status res)))
      (is (= "/admin" (get-in res [:headers "Location"])))
      (is (some? (get-in res [:session :identity :id])))))

  (testing "間違ったパスワード"
    (let [req {:form-params {"username" "admin" "password" "wrong"}}
          res (admin/login-submit req)]
      (is (= 302 (:status res)))
      (is (= "/admin/login" (get-in res [:headers "Location"]))))))

(deftest site-selector-test
  (let [{:keys [user] :as ctx} (setup!)]
    (testing "サイト選択画面表示"
      (let [req {:session {:identity {:id            (:id user)
                                      :username      "admin"
                                      :is-superadmin true}}}
            res (admin/site-selector req)]
        (is (= 200 (:status res)))
        (is (.contains (:body res) "Admin Site"))))))

(deftest select-site-test
  (let [ctx (setup!)]
    (testing "サイト選択でセッションにsite-id保存"
      (let [req (auth-request ctx {:path-params {:id (str (get-in ctx [:site :id]))}})
            res (admin/select-site req)]
        (is (= 302 (:status res)))
        (is (= "/admin/dashboard" (get-in res [:headers "Location"])))
        (is (= (get-in ctx [:site :id])
               (get-in res [:session :current-site-id])))
        ;; identity should be preserved in session
        (is (some? (get-in res [:session :identity])))))))

(deftest dashboard-test
  (let [ctx (setup!)]
    (testing "サイト選択済みでダッシュボード表示"
      (let [res (admin/dashboard (auth-request ctx))]
        (is (= 200 (:status res)))))

    (testing "サイト未選択でリダイレクト"
      (let [req (-> (auth-request ctx)
                    (update :session dissoc :current-site-id))
            res (admin/dashboard req)]
        (is (= 302 (:status res)))))))

(deftest posts-crud-test
  (let [ctx (setup!)]
    (testing "記事一覧"
      (let [res (admin/posts-index (auth-request ctx))]
        (is (= 200 (:status res)))))

    (testing "記事作成"
      (let [req (auth-request ctx {:form-params {"title"   "Test"
                                                  "content" "# Test"
                                                  "status"  "draft"}})
            res (admin/posts-create req)]
        (is (= 302 (:status res)))))))

(deftest superadmin-sites-test
  (let [_ (setup!)]
    (testing "サイト一覧"
      (let [res (admin/super-sites-index {})]
        (is (= 200 (:status res)))
        (is (.contains (:body res) "Admin Site"))))))
