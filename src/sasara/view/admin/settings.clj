(ns sasara.view.admin.settings
  (:require [sasara.view.admin.layout :as admin]))

(def ^:private templates
  [{"default"        "Default（シンプル・グレー）"}
   {"camp-therapist" "Camp Therapist（ナチュラル・深緑）"}])

(defn settings-page
  [{:keys [site-name current-template]}]
  (admin/admin-layout
   {:title "Settings"}
   [:div {:class "max-w-2xl space-y-8"}

    ;; Site basic settings
    [:form {:method "post" :action "/admin/settings" :class "space-y-6"}
     [:h2 {:class "text-lg font-semibold text-gray-900"} "サイト基本設定"]

     ;; Site name
     [:div
      [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "サイト名"]
      [:input {:type "text" :name "site-name"
               :value (or site-name "")
               :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"}]]

     ;; Template selection
     [:div
      [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "テンプレート"]
      [:div {:class "grid grid-cols-1 gap-3"}
       (for [tmpl templates
             :let [[key label] (first tmpl)]]
         [:label {:class (str "flex items-center gap-3 p-4 border rounded-lg cursor-pointer transition-colors "
                              (if (= key (or current-template "default"))
                                "border-blue-500 bg-blue-50"
                                "border-gray-200 hover:border-gray-300"))}
          [:input {:type "radio" :name "template" :value key
                   :class "text-blue-600"
                   (when (= key (or current-template "default")) :checked) true}]
          [:div
           [:p {:class "font-medium text-sm text-gray-900"} label]]])]]

     [:button {:type "submit"
               :class "px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"}
      "保存"]]]))
