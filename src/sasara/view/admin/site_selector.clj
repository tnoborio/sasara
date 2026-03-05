(ns sasara.view.admin.site-selector
  (:require [hiccup2.core :as h]
            [hiccup.page :refer [doctype]]))

(defn page
  "サイト選択画面"
  [{:keys [sites is-superadmin]}]
  (str
   (:html5 doctype)
   (h/html
    [:html {:lang "ja"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title "Select Site | Sasara Admin"]
      [:script {:src "https://cdn.tailwindcss.com"}]]
     [:body {:class "bg-gray-100 flex items-center justify-center min-h-screen"}
      [:div {:class "bg-white p-8 rounded-lg shadow-md w-full max-w-lg"}
       [:h1 {:class "text-2xl font-bold text-gray-900 mb-6"} "Select Site"]
       (if (seq sites)
         [:div {:class "space-y-3"}
          (for [{:keys [id name slug role]} sites]
            [:form {:method "post" :action (str "/admin/select-site/" id)}
             [:button {:type "submit"
                       :class "w-full text-left px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 flex justify-between items-center"}
              [:div
               [:div {:class "font-medium"} name]
               [:div {:class "text-sm text-gray-500"} slug]]
              [:span {:class (str "px-2 py-1 rounded text-xs "
                                  (if (= role "admin")
                                    "bg-blue-100 text-blue-800"
                                    "bg-gray-100 text-gray-800"))}
               role]]])]
         [:p {:class "text-gray-500"} "No sites assigned."])
       (when is-superadmin
         [:div {:class "mt-6 pt-6 border-t"}
          [:a {:href "/admin/super/sites"
               :class "block text-center px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-700 text-sm"}
           "Super Admin"]])
       [:div {:class "mt-4"}
        [:form {:method "post" :action "/admin/logout"}
         [:button {:type "submit"
                   :class "text-sm text-red-600 hover:underline"}
          "Logout"]]]]]])))
