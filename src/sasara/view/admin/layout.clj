(ns sasara.view.admin.layout
  (:require [hiccup2.core :as h]
            [hiccup.page :refer [doctype]]))

(defn sidebar
  "Admin sidebar. Shows superadmin nav when super? is true."
  [{:keys [site-name super?]}]
  [:aside {:class "w-56 bg-gray-900 text-white min-h-screen p-4"}
   [:div {:class "text-lg font-bold mb-2"} "Sasara Admin"]
   (when site-name
     [:div {:class "text-xs text-gray-400 mb-6 truncate"} site-name])
   (when-not site-name
     [:div {:class "mb-6"}])
   [:nav {:class "space-y-2 text-sm"}
    (if super?
      ;; Superadmin nav
      (list
       [:a {:href "/admin/super/sites" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Sites"]
       [:a {:href "/admin/super/users" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Users"]
       [:hr {:class "border-gray-700 my-4"}]
       [:a {:href "/admin" :class "block px-3 py-2 rounded hover:bg-gray-800"} "← Site Selector"])
      ;; Site management nav
      (list
       [:a {:href "/admin/dashboard" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Dashboard"]
       [:a {:href "/admin/posts" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Posts"]
       [:a {:href "/admin/pages" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Pages"]
       [:a {:href "/admin/media" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Media"]
       [:a {:href "/admin/settings" :class "block px-3 py-2 rounded hover:bg-gray-800"} "Settings"]
       [:hr {:class "border-gray-700 my-4"}]
       [:a {:href "/admin" :class "block px-3 py-2 rounded hover:bg-gray-800"} "← Switch Site"]
       [:a {:href "/" :class "block px-3 py-2 rounded hover:bg-gray-800"} "← View Site"]))
    [:form {:method "post" :action "/admin/logout"}
     [:button {:type "submit" :class "block w-full text-left px-3 py-2 rounded hover:bg-gray-800 text-red-400"}
      "Logout"]]]])

(defn admin-layout
  "Admin panel layout."
  [{:keys [title site-name super?]} & content]
  (str
   (:html5 doctype)
   (h/html
    [:html {:lang "ja"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title (str (when title (str title " | ")) "Sasara Admin")]
      [:script {:src "https://cdn.tailwindcss.com"}]]
     [:body {:class "bg-gray-100"}
      [:div {:class "flex"}
       (sidebar {:site-name site-name :super? super?})
       [:div {:class "flex-1 p-8"}
        (when title
          [:h1 {:class "text-2xl font-bold text-gray-900 mb-6"} title])
        content]]]])))
