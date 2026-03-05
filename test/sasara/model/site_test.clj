(ns sasara.model.site-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [sasara.test-helper :as helper]
            [sasara.model.site :as site]))

(use-fixtures :each helper/with-test-db)

(deftest create-site-test
  (testing "サイト作成"
    (let [s (site/create! {:name "Test Site" :slug "test-site"})]
      (is (some? (:id s)))
      (is (= "Test Site" (:name s)))
      (is (= "test-site" (:slug s)))))

  (testing "ドメイン付きサイト作成"
    (let [s (site/create! {:name   "Domain Site"
                           :slug   "domain-site"
                           :domain "example.com"})]
      (is (= "example.com" (:domain s))))))

(deftest find-site-test
  (let [s (site/create! {:name   "Find Site"
                         :slug   "find-site"
                         :domain "find.example.com"})]
    (testing "IDで検索"
      (is (= "Find Site" (:name (site/find-by-id (:id s))))))

    (testing "slugで検索"
      (is (= "Find Site" (:name (site/find-by-slug "find-site")))))

    (testing "ドメインで検索"
      (is (= "Find Site" (:name (site/find-by-domain "find.example.com")))))

    (testing "存在しないドメイン"
      (is (nil? (site/find-by-domain "nope.example.com"))))))

(deftest update-site-test
  (testing "サイト更新"
    (let [s (site/create! {:name "Before" :slug "before"})
          updated (site/update! (:id s) {:name "After" :slug "after"})]
      (is (= "After" (:name updated)))
      (is (= "after" (:slug updated))))))

(deftest delete-site-test
  (testing "サイト削除"
    (let [s (site/create! {:name "Gone" :slug "gone"})]
      (site/delete! (:id s))
      (is (nil? (site/find-by-id (:id s)))))))
