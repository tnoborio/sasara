(ns sasara.view.public.blog
  (:require [sasara.view.layout :as layout]
            [sasara.view.components :as c]
            [hiccup2.core :as h]))

(defn list-page
  "Blog listing page."
  [{:keys [posts]}]
  (layout/base-layout
   {:title "Blog"}
   (c/page-header "Blog" "技術の実践知と事業化のリアル")
   [:div {:class "max-w-6xl mx-auto px-4 pb-12"}
    (if (seq posts)
      [:div
       (for [post posts]
         (c/post-card post))]
      [:p {:class "text-gray-500"} "まだ記事がありません。"])]))

(defn show-page
  "Individual blog post page."
  [{:keys [post]}]
  (layout/base-layout
   {:title (:title post)
    :description (:excerpt post)}
   [:article {:class "max-w-3xl mx-auto px-4 py-12"}
    [:h1 {:class "text-3xl font-bold text-gray-900 mb-4"} (:title post)]
    (when (:published-at post)
      [:time {:class "text-sm text-gray-400 block mb-8"}
       (str (:published-at post))])
    [:div {:class "prose prose-lg"}
     (h/raw (or (:content-html post) ""))]]))
