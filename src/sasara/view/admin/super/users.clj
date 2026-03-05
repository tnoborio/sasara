(ns sasara.view.admin.super.users
  (:require [sasara.view.admin.layout :as admin]))

(defn list-page
  "Superadmin: user listing."
  [{:keys [users]}]
  (admin/admin-layout
   {:title "Users" :super? true}
   [:div {:class "flex justify-between items-center mb-6"}
    [:p {:class "text-gray-600"} (str (count users) " users")]
    [:a {:href "/admin/super/users/new"
         :class "px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"}
     "+ New User"]]
   [:table {:class "w-full bg-white rounded-lg shadow"}
    [:thead
     [:tr {:class "border-b text-left text-sm text-gray-500"}
      [:th {:class "px-4 py-3"} "Username"]
      [:th {:class "px-4 py-3"} "Email"]
      [:th {:class "px-4 py-3"} "Superadmin"]
      [:th {:class "px-4 py-3"} "Actions"]]]
    [:tbody
     (for [{:keys [id username email is-superadmin]} users]
       [:tr {:class "border-b hover:bg-gray-50"}
        [:td {:class "px-4 py-3 font-medium"} username]
        [:td {:class "px-4 py-3 text-sm text-gray-500"} email]
        [:td {:class "px-4 py-3"}
         (when is-superadmin
           [:span {:class "px-2 py-1 rounded text-xs bg-purple-100 text-purple-800"} "superadmin"])]
        [:td {:class "px-4 py-3"}
         [:a {:href (str "/admin/super/users/" id) :class "text-blue-600 hover:underline text-sm"} "Edit"]]])]]))

(defn form-page
  "Superadmin: new/edit user form."
  [{:keys [user sites user-sites]}]
  (let [editing?       (boolean (:id user))
        assigned-sites (set (map :site-id user-sites))]
    (admin/admin-layout
     {:title (if editing? "Edit User" "New User") :super? true}
     [:form {:method "post"
             :action (if editing?
                       (str "/admin/super/users/" (:id user))
                       "/admin/super/users")
             :class "max-w-xl space-y-6"}
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Username"]
       [:input {:type "text" :name "username"
                :value (or (:username user) "")
                :required true
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Email"]
       [:input {:type "email" :name "email"
                :value (or (:email user) "")
                :required true
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"}
        (if editing? "Password (leave blank to keep)" "Password")]
       [:input {:type "password" :name "password"
                :required (not editing?)
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"}]]
      [:div
       [:label {:class "flex items-center gap-2 text-sm font-medium text-gray-700"}
        [:input {:type "checkbox" :name "is-superadmin" :value "true"
                 :checked (:is-superadmin user)
                 :class "rounded border-gray-300"}]
        "Superadmin"]]
      ;; サイト割り当て
      (when (seq sites)
        [:div
         [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "Site Assignments"]
         [:div {:class "space-y-2 bg-gray-50 p-4 rounded-lg"}
          (for [{:keys [id name]} sites]
            (let [current-role (some (fn [us] (when (= (:site-id us) id) (:role us))) user-sites)]
              [:div {:class "flex items-center gap-3"}
               [:input {:type "checkbox"
                        :name (str "site-" id)
                        :value "true"
                        :checked (contains? assigned-sites id)
                        :class "rounded border-gray-300"}]
               [:span {:class "text-sm flex-1"} name]
               [:select {:name (str "role-" id)
                         :class "px-2 py-1 border border-gray-300 rounded text-sm"}
                (for [r ["admin" "editor"]]
                  [:option (merge {:value r}
                                  (when (= r (or current-role "editor")) {:selected true}))
                   r])]]))]])
      [:div {:class "flex gap-3"}
       [:button {:type "submit"
                 :class "px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"}
        (if editing? "Update" "Create")]
       [:a {:href "/admin/super/users"
            :class "px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"}
        "Cancel"]]])))
