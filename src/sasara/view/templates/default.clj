(ns sasara.view.templates.default
  (:require [hiccup2.core :as h]
            [hiccup.page :refer [doctype]]))

;; ── Shared components ──────────────────────────────────────────────────────

(defn- navigation [{:keys [site-name nav-items]}]
  [:nav {:class "bg-white border-b border-gray-200"}
   [:div {:class "max-w-6xl mx-auto px-4 py-4 flex items-center justify-between"}
    [:a {:href "/" :class "text-xl font-bold text-gray-900"} (or site-name "Sasara")]
    [:div {:class "flex gap-6 text-sm"}
     (for [{:keys [title slug]} nav-items]
       [:a {:href (str "/" slug) :class "text-gray-600 hover:text-gray-900"} title])
     [:a {:href "/blog" :class "text-gray-600 hover:text-gray-900"} "Blog"]]]])

(defn- footer [{:keys [site-name]}]
  [:footer {:class "bg-gray-50 border-t border-gray-200 mt-16"}
   [:div {:class "max-w-6xl mx-auto px-4 py-8 text-center text-sm text-gray-500"}
    [:p (str "© 2026 " (or site-name "Sasara") ". Powered by Sasara")]]])

(defn- base-layout [{:keys [title site-name description] :as ctx} & content]
  (str
   (:html5 doctype)
   (h/html
    [:html {:lang "ja"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title (if title (str title " | " (or site-name "Sasara")) (or site-name "Sasara"))]
      (when description
        [:meta {:name "description" :content description}])
      [:script {:src "https://cdn.tailwindcss.com"}]
      [:link {:rel "stylesheet" :href "/css/custom.css"}]]
     [:body {:class "bg-white text-gray-900 min-h-screen flex flex-col"}
      (navigation ctx)
      [:main {:class "flex-1"} content]
      (footer ctx)]])))

;; ── Page renderers ────────────────────────────────────────────────────────

(defn- render-home [{:keys [site-name posts] :as ctx}]
  (base-layout
   (assoc ctx :title nil)
   [:div {:class "max-w-6xl mx-auto px-4 py-12"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-8"} site-name]
    (when (seq posts)
      [:section
       [:h2 {:class "text-xl font-semibold mb-4"} "最新記事"]
       [:div
        (for [{:keys [title slug excerpt published-at]} (take 3 posts)]
          [:article {:class "border-b border-gray-100 py-4"}
           [:a {:href (str "/blog/" slug)}
            [:h3 {:class "font-semibold hover:text-blue-600"} title]
            (when excerpt
              [:p {:class "text-gray-600 text-sm mt-1"} excerpt])
            (when published-at
              [:time {:class "text-xs text-gray-400 mt-1 block"} (str published-at)])]])]])]))

(defn- render-page [{:keys [page] :as ctx}]
  (base-layout
   (assoc ctx :title (:title page) :description (:excerpt page))
   [:article {:class "max-w-3xl mx-auto px-4 py-12"}
    [:h1 {:class "text-3xl font-bold text-gray-900 mb-8"} (:title page)]
    [:div {:class "prose prose-lg"}
     (h/raw (or (:content-html page) ""))]]))

(defn- render-blog-list [{:keys [posts] :as ctx}]
  (base-layout
   (assoc ctx :title "Blog")
   [:div {:class "max-w-6xl mx-auto px-4 py-12"}
    [:h1 {:class "text-3xl font-bold text-gray-900 mb-8"} "Blog"]
    (if (seq posts)
      [:div
       (for [{:keys [title slug excerpt published-at]} posts]
         [:article {:class "border-b border-gray-100 py-6"}
          [:a {:href (str "/blog/" slug) :class "group"}
           [:h2 {:class "text-xl font-semibold text-gray-900 group-hover:text-blue-600 mb-2"} title]
           (when excerpt
             [:p {:class "text-gray-600 mb-2"} excerpt])
           (when published-at
             [:time {:class "text-sm text-gray-400"} (str published-at)])]])]
      [:p {:class "text-gray-500"} "まだ記事がありません。"])]))

(defn- render-blog-show [{:keys [post] :as ctx}]
  (base-layout
   (assoc ctx :title (:title post) :description (:excerpt post))
   [:article {:class "max-w-3xl mx-auto px-4 py-12"}
    [:h1 {:class "text-3xl font-bold text-gray-900 mb-4"} (:title post)]
    (when (:published-at post)
      [:time {:class "text-sm text-gray-400 block mb-8"} (str (:published-at post))])
    [:div {:class "prose prose-lg"}
     (h/raw (or (:content-html post) ""))]]))

(defn- render-not-found [ctx]
  (base-layout
   (assoc ctx :title "404 Not Found")
   [:div {:class "max-w-3xl mx-auto px-4 py-20 text-center"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-4"} "404"]
    [:p {:class "text-gray-600 mb-8"} "ページが見つかりませんでした。"]
    [:a {:href "/" :class "text-blue-600 hover:underline"} "トップへ戻る"]]))

;; ── Template definition ───────────────────────────────────────────────────

(def template
  {:render-home      render-home
   :render-page      render-page
   :render-blog-list render-blog-list
   :render-blog-show render-blog-show
   :render-not-found render-not-found})
