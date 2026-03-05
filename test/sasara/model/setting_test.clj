(ns sasara.model.setting-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [sasara.test-helper :as helper]
            [sasara.model.site :as site]
            [sasara.model.setting :as setting]))

(use-fixtures :each helper/with-test-db)

(deftest setting-crud-test
  (let [s (site/create! {:name "Settings Site" :slug "settings-site"})
        sid (:id s)]

    (testing "設定値の保存と取得"
      (setting/set-setting! sid "site-name" "My Site")
      (is (= "My Site" (setting/get-setting sid "site-name"))))

    (testing "設定値の上書き"
      (setting/set-setting! sid "site-name" "New Name")
      (is (= "New Name" (setting/get-setting sid "site-name"))))

    (testing "存在しないキー"
      (is (nil? (setting/get-setting sid "nonexistent"))))

    (testing "全設定の取得"
      (setting/set-setting! sid "ga4-id" "G-TEST123")
      (let [all (setting/get-all sid)]
        (is (= "New Name" (get all "site-name")))
        (is (= "G-TEST123" (get all "ga4-id")))))

    (testing "別サイトの設定は分離される"
      (let [s2 (site/create! {:name "Other" :slug "other-site"})]
        (setting/set-setting! (:id s2) "site-name" "Other Site")
        (is (= "New Name" (setting/get-setting sid "site-name")))
        (is (= "Other Site" (setting/get-setting (:id s2) "site-name")))))))
