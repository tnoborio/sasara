(ns sasara.view.public.about
  (:require [sasara.view.layout :as layout]
            [sasara.view.components :as c]))

(defn page [_]
  (layout/base-layout
   {:title "About"
    :description "登尾 徳誠 — IT業界25年超のフルスタックエンジニア"}
   (c/page-header "About" "登尾 徳誠（Tokusei Noborio）")
   [:div {:class "max-w-6xl mx-auto px-4 pb-12"}
    [:div {:class "prose prose-lg max-w-3xl"}
     [:p "IT業界25年超のフルスタックエンジニア（1999年〜）。"]
     [:p "ニャンパス株式会社 設立・代表（2010年〜）。コワーキングスペース「HaLake」運営、プログラミング教室開催。"]
     [:p "著書5冊（技術評論社・工学社ほか）、WEB+DB Press連載、Schoo講師。"]
     [:p "2025年つくば市に移転、個人事業主として事業化を目指しています。"]

     [:h2 {:class "text-2xl font-bold mt-8 mb-4"} "スキル"]
     [:p "Java, Python, Ruby, TypeScript, Scala, Clojure, Swift, Go, Rust ほか多数。"]
     [:p "IoT（基板設計〜ソフトウェア）、AI/ディープラーニング、クラウド（AWS/GCP）。"]

     [:h2 {:class "text-2xl font-bold mt-8 mb-4"} "実績"]
     [:ul
      [:li "技術書 5冊出版"]
      [:li "建築業界スタートアップの技術支援 7年間"]
      [:li "リーガルテック開発"]
      [:li "コワーキングスペース運営、プログラミング教室"]]]]))
