(ns sasara.view.components)

(defn post-card
  "Blog post card component."
  [{:keys [title slug excerpt published-at]}]
  [:article {:class "border-b border-gray-100 py-6"}
   [:a {:href (str "/blog/" slug) :class "group"}
    [:h2 {:class "text-xl font-semibold text-gray-900 group-hover:text-blue-600 mb-2"}
     title]
    (when excerpt
      [:p {:class "text-gray-600 mb-2"} excerpt])
    (when published-at
      [:time {:class "text-sm text-gray-400"}
       (str published-at)])]])

(defn page-header
  "Page title + optional subtitle."
  ([title] (page-header title nil))
  ([title subtitle]
   [:div {:class "max-w-6xl mx-auto px-4 pt-12 pb-8"}
    [:h1 {:class "text-3xl font-bold text-gray-900 mb-2"} title]
    (when subtitle
      [:p {:class "text-lg text-gray-600"} subtitle])]))
