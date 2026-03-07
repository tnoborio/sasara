(ns sasara.view.admin.media
  (:require [sasara.view.admin.layout :as admin]))

(defn- image? [content-type]
  (and content-type (clojure.string/starts-with? content-type "image/")))

(defn list-page [{:keys [media site-name]}]
  (admin/admin-layout
   {:title "Media" :site-name site-name}
   ;; Upload form
   [:div {:class "mb-8 bg-white p-6 rounded-lg shadow"}
    [:h2 {:class "text-base font-semibold text-gray-700 mb-4"} "Upload Image"]
    [:form {:method "post"
            :action "/admin/media/upload"
            :enctype "multipart/form-data"
            :class "flex items-end gap-4"}
     [:div {:class "flex-1"}
      [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "File"]
      [:input {:type "file" :name "file" :accept "image/*" :required true
               :class "block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4
                       file:rounded file:border-0 file:text-sm file:font-medium
                       file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"}]]
     [:div {:class "w-64"}
      [:label {:class "block text-sm font-medium text-gray-700 mb-1"} "Alt Text"]
      [:input {:type "text" :name "alt-text" :placeholder "画像の説明"
               :class "w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"}]]
     [:button {:type "submit"
               :class "px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm whitespace-nowrap"}
      "Upload"]]]

   ;; Image list
   (if (empty? media)
     [:p {:class "text-gray-500 text-sm"} "まだ画像がありません。"]
     [:div {:class "grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4"}
      (for [{:keys [id url original-name alt-text content-type size-bytes]} media]
        [:div {:class "bg-white rounded-lg shadow overflow-hidden group"}
         ;; Thumbnail
         [:div {:class "aspect-square bg-gray-100 flex items-center justify-center overflow-hidden"}
          (if (image? content-type)
            [:img {:src url :alt (or alt-text "") :class "w-full h-full object-cover"}]
            [:span {:class "text-gray-400 text-xs"} "FILE"])]
         ;; Info and actions
         [:div {:class "p-2"}
          [:p {:class "text-xs text-gray-600 truncate mb-1"} (or original-name url)]
          [:p {:class "text-xs text-gray-400 mb-2"}
           (when size-bytes (str (quot size-bytes 1024) " KB"))]
          [:div {:class "flex gap-1"}
           ;; Copy URL button
           [:button {:type "button"
                     :onclick (str "navigator.clipboard.writeText('" url "').then(()=>alert('URLをコピーしました'))")
                     :class "flex-1 px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded text-gray-700"}
            "Copy URL"]
           ;; Delete button
           [:form {:method "post" :action (str "/admin/media/" id "/delete")
                   :class "inline"
                   :onsubmit "return confirm('削除しますか？')"}
            [:button {:type "submit"
                      :class "px-2 py-1 text-xs bg-red-50 hover:bg-red-100 rounded text-red-600"}
             "✕"]]]]
         ;; Copy Markdown button
         [:div {:class "px-2 pb-2"}
          [:button {:type "button"
                    :onclick (str "navigator.clipboard.writeText('!["
                                  (or alt-text "") "](" url ")').then(()=>alert('Markdownをコピーしました'))")
                    :class "w-full px-2 py-1 text-xs bg-blue-50 hover:bg-blue-100 rounded text-blue-700"}
           "Copy Markdown"]]])])))

;; ── Picker mode (used in image insertion modal on post/page edit forms) ─────

(defn picker-page [{:keys [media]}]
  (str
   "<!DOCTYPE html><html><head>"
   "<meta charset='utf-8'>"
   "<script src='https://cdn.tailwindcss.com'></script>"
   "</head><body class='bg-gray-50 p-4'>"
   "<div class='grid grid-cols-3 gap-3'>"
   (apply str
          (for [{:keys [url original-name alt-text content-type]} media
                :when (image? content-type)]
            (str "<div class='bg-white rounded shadow overflow-hidden cursor-pointer hover:ring-2 hover:ring-blue-400'"
                 " onclick=\"window.parent.insertImageFromPicker('" url "','" (or alt-text "") "')\">"
                 "<div class='aspect-square bg-gray-100'>"
                 "<img src='" url "' alt='" (or alt-text "") "' class='w-full h-full object-cover'>"
                 "</div>"
                 "<p class='text-xs text-gray-500 truncate p-1'>" (or original-name "") "</p>"
                 "</div>")))
   "</div></body></html>"))
