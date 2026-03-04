(ns sasara.view.layout
  (:require [hiccup2.core :as h]
            [hiccup.page :refer [doctype]]))

(defn navigation []
  [:nav {:class "bg-white border-b border-gray-200"}
   [:div {:class "max-w-6xl mx-auto px-4 py-4 flex items-center justify-between"}
    [:a {:href "/" :class "text-xl font-bold text-gray-900"} "Sasara"]
    [:div {:class "flex gap-6 text-sm"}
     [:a {:href "/about" :class "text-gray-600 hover:text-gray-900"} "About"]
     [:a {:href "/services" :class "text-gray-600 hover:text-gray-900"} "Services"]
     [:a {:href "/works" :class "text-gray-600 hover:text-gray-900"} "Works"]
     [:a {:href "/blog" :class "text-gray-600 hover:text-gray-900"} "Blog"]
     [:a {:href "/contact" :class "text-gray-600 hover:text-gray-900"} "Contact"]]]])

(defn footer []
  [:footer {:class "bg-gray-50 border-t border-gray-200 mt-16"}
   [:div {:class "max-w-6xl mx-auto px-4 py-8 text-center text-sm text-gray-500"}
    [:p (str "\u00a9 2026 Powered by Sasara")]]])

(defn base-layout
  "Base HTML layout with Tailwind CSS CDN."
  [{:keys [title description]} & content]
  (str
   (:html5 doctype)
   (h/html
    [:html {:lang "ja"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title (if title (str title " | Sasara") "Sasara")]
      (when description
        [:meta {:name "description" :content description}])
      [:script {:src "https://cdn.tailwindcss.com"}]
      [:link {:rel "stylesheet" :href "/css/custom.css"}]]
     [:body {:class "bg-white text-gray-900 min-h-screen flex flex-col"}
      (navigation)
      [:main {:class "flex-1"}
       content]
      (footer)]])))
