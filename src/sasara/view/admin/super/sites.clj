(ns sasara.view.admin.super.sites
  (:require [sasara.view.admin.layout :as admin]))

(defn list-page
  "Superadmin: site listing."
  [{:keys [sites]}]
  (admin/admin-layout
   {:title "Sites" :super? true}
   [:div {:class "flex justify-between items-center mb-6"}
    [:p {:class "text-gray-600"} (str (count sites) " sites")]
    [:a {:href "/admin/super/sites/new"
         :class "px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"}
     "+ New Site"]]
   [:table {:class "w-full bg-white rounded-lg shadow"}
    [:thead
     [:tr {:class "border-b text-left text-sm text-gray-500"}
      [:th {:class "px-4 py-3"} "Name"]
      [:th {:class "px-4 py-3"} "Slug"]
      [:th {:class "px-4 py-3"} "Domain"]
      [:th {:class "px-4 py-3"} "Actions"]]]
    [:tbody
     (for [{:keys [id name slug domain]} sites]
       [:tr {:class "border-b hover:bg-gray-50"}
        [:td {:class "px-4 py-3 font-medium"} name]
        [:td {:class "px-4 py-3 text-sm text-gray-500"} slug]
        [:td {:class "px-4 py-3 text-sm text-gray-500"} (or domain "-")]
        [:td {:class "px-4 py-3"}
         [:a {:href (str "/admin/super/sites/" id) :class "text-blue-600 hover:underline text-sm"} "Edit"]]])]]))

(defn form-page
  "Superadmin: new/edit site form."
  [{:keys [site]}]
  (let [editing? (boolean (:id site))]
    (admin/admin-layout
     {:title (if editing? "Edit Site" "New Site") :super? true}
     [:form {:method "post"
             :action (if editing?
                       (str "/admin/super/sites/" (:id site))
                       "/admin/super/sites")
             :class "max-w-xl space-y-6"}
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Name"]
       [:input {:type "text" :name "name"
                :value (or (:name site) "")
                :required true
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Slug"]
       [:input {:type "text" :name "slug"
                :value (or (:slug site) "")
                :placeholder "auto-generated-from-name"
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Domain"]
       [:input {:type "text" :name "domain"
                :value (or (:domain site) "")
                :placeholder "example.com"
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      [:div {:class "flex gap-3"}
       [:button {:type "submit"
                 :class "px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"}
        (if editing? "Update" "Create")]
       [:a {:href "/admin/super/sites"
            :class "px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"}
        "Cancel"]]])))
