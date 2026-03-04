(ns sasara.view.admin.posts
  (:require [sasara.view.admin.layout :as admin]))

(defn list-page
  "Admin: post listing."
  [{:keys [posts]}]
  (admin/admin-layout
   {:title "Posts"}
   [:div {:class "flex justify-between items-center mb-6"}
    [:p {:class "text-gray-600"} (str (count posts) " posts")]
    [:a {:href "/admin/posts/new"
         :class "px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"}
     "+ New Post"]]
   [:table {:class "w-full bg-white rounded-lg shadow"}
    [:thead
     [:tr {:class "border-b text-left text-sm text-gray-500"}
      [:th {:class "px-4 py-3"} "Title"]
      [:th {:class "px-4 py-3"} "Status"]
      [:th {:class "px-4 py-3"} "Created"]
      [:th {:class "px-4 py-3"} "Actions"]]]
    [:tbody
     (for [{:keys [id title status created-at]} posts]
       [:tr {:class "border-b hover:bg-gray-50"}
        [:td {:class "px-4 py-3 font-medium"} title]
        [:td {:class "px-4 py-3"}
         [:span {:class (str "px-2 py-1 rounded text-xs "
                             (case status
                               "published" "bg-green-100 text-green-800"
                               "draft"     "bg-yellow-100 text-yellow-800"
                               "bg-gray-100 text-gray-800"))}
          status]]
        [:td {:class "px-4 py-3 text-sm text-gray-500"} (str created-at)]
        [:td {:class "px-4 py-3"}
         [:a {:href (str "/admin/posts/" id) :class "text-blue-600 hover:underline text-sm"} "Edit"]]])]]))

(defn form-page
  "Admin: new/edit post form."
  [{:keys [post]}]
  (let [editing? (boolean (:id post))]
    (admin/admin-layout
     {:title (if editing? "Edit Post" "New Post")}
     [:form {:method "post"
             :action (if editing?
                       (str "/admin/posts/" (:id post))
                       "/admin/posts")
             :class "max-w-3xl space-y-6"}
      ;; Title
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Title"]
       [:input {:type "text" :name "title"
                :value (or (:title post) "")
                :required true
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      ;; Slug
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Slug"]
       [:input {:type "text" :name "slug"
                :value (or (:slug post) "")
                :placeholder "auto-generated-from-title"
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      ;; Content (Markdown)
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Content (Markdown)"]
       [:textarea {:name "content" :rows 20
                   :class "w-full px-3 py-2 border border-gray-300 rounded-lg font-mono text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}
        (or (:content post) "")]]
      ;; Excerpt
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Excerpt"]
       [:textarea {:name "excerpt" :rows 3
                   :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}
        (or (:excerpt post) "")]]
      ;; Status
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Status"]
       [:select {:name "status"
                 :class "px-3 py-2 border border-gray-300 rounded-lg"}
        (for [s ["draft" "published" "archived"]]
          [:option (merge {:value s}
                          (when (= s (:status post)) {:selected true}))
           s])]]
      ;; Submit
      [:div {:class "flex gap-3"}
       [:button {:type "submit"
                 :class "px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"}
        (if editing? "Update" "Create")]
       [:a {:href "/admin/posts"
            :class "px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"}
        "Cancel"]]])))

(defn login-page
  "Login page."
  [{:keys [error]}]
  (str
   (hiccup2.core/html
    [:html {:lang "ja"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title "Login | Sasara Admin"]
      [:script {:src "https://cdn.tailwindcss.com"}]]
     [:body {:class "bg-gray-100 flex items-center justify-center min-h-screen"}
      [:div {:class "bg-white p-8 rounded-lg shadow-md w-96"}
       [:h1 {:class "text-2xl font-bold text-gray-900 mb-6"} "Sasara Admin"]
       (when error
         [:div {:class "bg-red-50 text-red-600 px-4 py-2 rounded mb-4 text-sm"} error])
       [:form {:method "post" :action "/admin/login" :class "space-y-4"}
        [:div
         [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Username"]
         [:input {:type "text" :name "username" :required true
                  :class "w-full px-3 py-2 border border-gray-300 rounded-lg"}]]
        [:div
         [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Password"]
         [:input {:type "password" :name "password" :required true
                  :class "w-full px-3 py-2 border border-gray-300 rounded-lg"}]]
        [:button {:type "submit"
                  :class "w-full px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-700"}
         "Login"]]]]])))
