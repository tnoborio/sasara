(ns sasara.model.post-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [sasara.test-helper :as helper]
            [sasara.model.user :as user]
            [sasara.model.site :as site]
            [sasara.model.post :as post]))

(use-fixtures :each helper/with-test-db)

(defn- setup! []
  (let [s (site/create! {:name "Blog Site" :slug "blog-site"})
        u (user/create! {:username "author" :email "a@example.com" :password "pass"})]
    {:site s :user u}))

(deftest create-post-test
  (let [{:keys [site user]} (setup!)]
    (testing "下書き記事作成"
      (let [p (post/create! {:title     "Test Post"
                              :content   "# Hello"
                              :excerpt   "Hello world"
                              :author-id (:id user)
                              :site-id   (:id site)})]
        (is (some? (:id p)))
        (is (= "test-post" (:slug p)))
        (is (= "draft" (:status p)))
        (is (some? (:content-html p)))
        (is (nil? (:published-at p)))))

    (testing "公開記事作成（published_at が自動設定）"
      (let [p (post/create! {:title     "Published"
                              :content   "content"
                              :status    "published"
                              :author-id (:id user)
                              :site-id   (:id site)})]
        (is (= "published" (:status p)))
        (is (some? (:published-at p)))))))

(deftest find-posts-test
  (let [{:keys [site user]} (setup!)
        s2 (site/create! {:name "Other" :slug "other"})]
    (post/create! {:title "Pub1" :content "c" :status "published"
                   :author-id (:id user) :site-id (:id site)})
    (post/create! {:title "Draft1" :content "c" :status "draft"
                   :author-id (:id user) :site-id (:id site)})
    (post/create! {:title "Other Site" :content "c" :status "published"
                   :author-id (:id user) :site-id (:id s2)})

    (testing "サイト内の全記事"
      (is (= 2 (count (post/find-all (:id site))))))

    (testing "サイト内の公開記事のみ"
      (is (= 1 (count (post/find-published (:id site))))))

    (testing "他サイトの記事は含まれない"
      (is (= 1 (count (post/find-all (:id s2))))))))

(deftest find-by-slug-test
  (let [{:keys [site user]} (setup!)]
    (post/create! {:title "Slug Test" :content "c"
                   :author-id (:id user) :site-id (:id site)})

    (testing "サイトスコープでslug検索"
      (is (some? (post/find-by-slug (:id site) "slug-test")))
      (is (nil? (post/find-by-slug 9999 "slug-test"))))))

(deftest update-post-test
  (let [{:keys [site user]} (setup!)
        p (post/create! {:title "Original" :content "old"
                         :author-id (:id user) :site-id (:id site)})]
    (testing "タイトル・ステータス更新"
      (let [updated (post/update! (:id p) {:title "Updated" :status "published"})]
        (is (= "Updated" (:title updated)))
        (is (= "published" (:status updated)))
        (is (some? (:published-at updated)))))))

(deftest delete-post-test
  (let [{:keys [site user]} (setup!)
        p (post/create! {:title "Delete Me" :content "c"
                         :author-id (:id user) :site-id (:id site)})]
    (post/delete! (:id p))
    (is (nil? (post/find-by-id (:id p))))))
