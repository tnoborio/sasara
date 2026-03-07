(ns sasara.view.admin.pages
  (:require [sasara.view.admin.layout :as admin]
            [clojure.string :as str]
            [hiccup2.core]))

(defn list-page [{:keys [pages site-name]}]
  (admin/admin-layout
   {:title "Pages" :site-name site-name}
   [:div {:class "flex justify-between items-center mb-6"}
    [:p {:class "text-gray-600"} (str (count pages) " pages")]
    [:a {:href "/admin/pages/new"
         :class "px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"}
     "+ New Page"]]
   [:table {:class "w-full bg-white rounded-lg shadow"}
    [:thead
     [:tr {:class "border-b text-left text-sm text-gray-500"}
      [:th {:class "px-4 py-3"} "Title"]
      [:th {:class "px-4 py-3"} "Slug"]
      [:th {:class "px-4 py-3"} "Status"]
      [:th {:class "px-4 py-3"} "Order"]
      [:th {:class "px-4 py-3"} "Actions"]]]
    [:tbody
     (for [{:keys [id title slug status sort-order]} pages]
       [:tr {:class "border-b hover:bg-gray-50"}
        [:td {:class "px-4 py-3 font-medium"} title]
        [:td {:class "px-4 py-3 text-sm text-gray-500 font-mono"} (str "/" slug)]
        [:td {:class "px-4 py-3"}
         [:span {:class (str "px-2 py-1 rounded text-xs "
                             (case status
                               "published" "bg-green-100 text-green-800"
                               "draft"     "bg-yellow-100 text-yellow-800"
                               "bg-gray-100 text-gray-800"))}
          status]]
        [:td {:class "px-4 py-3 text-sm text-gray-500"} sort-order]
        [:td {:class "px-4 py-3 flex gap-3"}
         [:a {:href (str "/admin/pages/" id)
              :class "text-blue-600 hover:underline text-sm"} "Edit"]
         [:form {:method "post" :action (str "/admin/pages/" id "/delete")
                 :class "inline"
                 :onsubmit "return confirm('削除しますか？')"}
          [:button {:type "submit"
                    :class "text-red-500 hover:underline text-sm"}
           "Delete"]]]])]]))

(defn- image-picker-modal [media]
  (list
   [:div {:id "image-picker-modal"
          :class "fixed inset-0 z-50 hidden"
          :onclick "if(event.target===this) closeImagePicker()"}
    [:div {:class "absolute inset-0 bg-black opacity-50"}]
    [:div {:class "absolute inset-4 md:inset-16 bg-white rounded-lg shadow-xl flex flex-col overflow-hidden"}
     [:div {:class "flex justify-between items-center px-4 py-3 border-b"}
      [:span {:class "font-semibold text-gray-700"} "画像を選択"]
      [:button {:type "button" :onclick "closeImagePicker()"
                :class "text-gray-400 hover:text-gray-600 text-xl leading-none"}
       "✕"]]
     [:div {:class "flex-1 overflow-y-auto p-4"}
      (if (empty? media)
        [:p {:class "text-gray-400 text-sm"} "まだ画像がありません。Media ページからアップロードしてください。"]
        [:div {:class "grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-3"}
         (for [{:keys [url original-name alt-text content-type]} media
               :when (and content-type (str/starts-with? content-type "image/"))]
           [:div {:class "cursor-pointer hover:ring-2 hover:ring-blue-400 rounded overflow-hidden bg-gray-100"
                  :onclick (str "insertImageFromPicker('" url "','" (or alt-text "") "')")}
            [:div {:class "aspect-square"}
             [:img {:src url :alt (or alt-text "") :class "w-full h-full object-cover"}]]
            [:p {:class "text-xs text-gray-500 truncate px-1 py-0.5"} (or original-name "")]])])]
     [:div {:class "px-4 py-3 border-t text-xs text-gray-400"}
      [:a {:href "/admin/media" :target "_blank" :class "hover:underline text-blue-500"}
       "Media ページで管理する →"]]]]
   [:script
    (hiccup2.core/raw
     "function openImagePicker() {
        document.getElementById('image-picker-modal').classList.remove('hidden');
      }
      function closeImagePicker() {
        document.getElementById('image-picker-modal').classList.add('hidden');
      }
      function insertImageFromPicker(url, alt) {
        var ta = document.getElementById('content');
        var text = '![' + alt + '](' + url + ')';
        var start = ta.selectionStart;
        var end   = ta.selectionEnd;
        ta.value = ta.value.substring(0, start) + text + ta.value.substring(end);
        ta.selectionStart = ta.selectionEnd = start + text.length;
        ta.focus();
        closeImagePicker();
      }")]))

(defn form-page [{:keys [page site-name media]}]
  (let [editing? (boolean (:id page))]
    (admin/admin-layout
     {:title (if editing? "Edit Page" "New Page") :site-name site-name}
     (image-picker-modal (or media []))
     [:form {:method "post"
             :action (if editing?
                       (str "/admin/pages/" (:id page))
                       "/admin/pages")
             :class "max-w-3xl space-y-6"}
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Title"]
       [:input {:type "text" :name "title"
                :value (or (:title page) "")
                :required true
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"}
        "Slug "
        [:span {:class "text-gray-400 font-normal text-xs"}
         "「home」にするとトップページ (/) になります"]]
       [:input {:type "text" :name "slug"
                :value (or (:slug page) "")
                :placeholder "auto-generated-from-title"
                :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"}]]
      [:div
       [:div {:class "flex justify-between items-center mb-1"}
        [:label {:class "block text-sm font-medium text-gray-700"} "Content (Markdown)"]
        [:button {:type "button" :onclick "openImagePicker()"
                  :class "px-3 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded text-gray-600 border border-gray-300"}
         "📷 Insert Image"]]
       [:textarea {:id "content" :name "content" :rows 20
                   :class "w-full px-3 py-2 border border-gray-300 rounded-lg font-mono text-sm focus:ring-2 focus:ring-blue-500"}
        (or (:content page) "")]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Excerpt / Description"]
       [:textarea {:name "excerpt" :rows 2
                   :class "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"}
        (or (:excerpt page) "")]]
      [:div {:class "flex gap-4"}
       [:div {:class "flex-1"}
        [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Status"]
        [:select {:name "status"
                  :class "w-full px-3 py-2 border border-gray-300 rounded-lg"}
         (for [s ["draft" "published"]]
           [:option (merge {:value s}
                           (when (= s (:status page)) {:selected true}))
            s])]]
       [:div {:class "w-32"}
        [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Sort Order"]
        [:input {:type "number" :name "sort-order"
                 :value (or (:sort-order page) 0)
                 :class "w-full px-3 py-2 border border-gray-300 rounded-lg"}]]]
      [:div {:class "flex gap-3"}
       [:button {:type "submit"
                 :class "px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"}
        (if editing? "Update" "Create")]
       [:a {:href "/admin/pages"
            :class "px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"}
        "Cancel"]]])))
