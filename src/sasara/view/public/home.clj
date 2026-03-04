(ns sasara.view.public.home
  (:require [sasara.view.layout :as layout]
            [sasara.view.components :as c]))

(defn page [{:keys [recent-posts]}]
  (layout/base-layout
   {:title nil}
   ;; Hero
   [:section {:class "max-w-6xl mx-auto px-4 py-20"}
    [:h1 {:class "text-4xl md:text-5xl font-bold text-gray-900 mb-4"}
     "つくば発・なんでも作れるエンジニア"]
    [:p {:class "text-xl text-gray-600 mb-8 max-w-2xl"}
     "IT業界25年超のフルスタックエンジニア。コードも本も教室も作る、ものづくりのプロフェッショナル。"]
    [:div {:class "flex gap-4"}
     [:a {:href "/about"
          :class "px-6 py-3 bg-gray-900 text-white rounded-lg hover:bg-gray-700"}
      "About"]
     [:a {:href "/contact"
          :class "px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50"}
      "お問い合わせ"]]]
   ;; Recent posts
   (when (seq recent-posts)
     [:section {:class "max-w-6xl mx-auto px-4 py-12"}
      [:h2 {:class "text-2xl font-bold text-gray-900 mb-6"} "最新の記事"]
      [:div
       (for [post recent-posts]
         (c/post-card post))]
      [:div {:class "mt-6"}
       [:a {:href "/blog" :class "text-blue-600 hover:underline"} "すべての記事を見る →"]]])))
