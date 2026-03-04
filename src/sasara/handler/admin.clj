(ns sasara.handler.admin
  (:require [sasara.model.post :as post]
            [sasara.model.user :as user]
            [sasara.view.admin.posts :as posts-view]
            [sasara.view.admin.layout :as admin-layout]
            [ring.util.response :as response]))

;; --- Auth ---

(defn login-page [request]
  (-> (posts-view/login-page {:error (get-in request [:flash :error])})
      (response/response)
      (response/content-type "text/html; charset=utf-8")))

(defn login-submit [request]
  (let [{:strs [username password]} (:form-params request)]
    (if-let [user (user/authenticate username password)]
      (-> (response/redirect "/admin")
          (assoc-in [:session :identity] {:id (:id user)
                                          :username (:username user)}))
      (-> (response/redirect "/admin/login")
          (assoc :flash {:error "Invalid username or password"})))))

(defn logout [_request]
  (-> (response/redirect "/admin/login")
      (assoc :session nil)))

;; --- Dashboard ---

(defn dashboard [_request]
  (let [posts (post/find-all {:limit 5})]
    (-> (admin-layout/admin-layout
         {:title "Dashboard"}
         [:div {:class "grid grid-cols-1 md:grid-cols-3 gap-6 mb-8"}
          [:div {:class "bg-white p-6 rounded-lg shadow"}
           [:p {:class "text-sm text-gray-500"} "Total Posts"]
           [:p {:class "text-3xl font-bold"} (count (post/find-all))]]
          [:div {:class "bg-white p-6 rounded-lg shadow"}
           [:p {:class "text-sm text-gray-500"} "Published"]
           [:p {:class "text-3xl font-bold"} (count (post/find-published))]]
          [:div {:class "bg-white p-6 rounded-lg shadow"}
           [:p {:class "text-sm text-gray-500"} "Drafts"]
           [:p {:class "text-3xl font-bold"} (count (post/find-all {:status "draft"}))]]]
         [:h2 {:class "text-lg font-semibold mb-4"} "Recent Posts"]
         [:div
          (for [{:keys [id title status]} posts]
            [:div {:class "flex justify-between items-center py-2 border-b"}
             [:a {:href (str "/admin/posts/" id) :class "hover:text-blue-600"} title]
             [:span {:class "text-sm text-gray-500"} status]])])
        (response/response)
        (response/content-type "text/html; charset=utf-8"))))

;; --- Posts CRUD ---

(defn posts-index [_request]
  (let [posts (post/find-all)]
    (-> (posts-view/list-page {:posts posts})
        (response/response)
        (response/content-type "text/html; charset=utf-8"))))

(defn posts-new [_request]
  (-> (posts-view/form-page {:post nil})
      (response/response)
      (response/content-type "text/html; charset=utf-8")))

(defn posts-create [request]
  (let [{:strs [title slug content excerpt status]} (:form-params request)
        author-id (get-in request [:session :identity :id])]
    (post/create! {:title     title
                   :slug      (when-not (empty? slug) slug)
                   :content   content
                   :excerpt   excerpt
                   :status    status
                   :author-id author-id})
    (response/redirect "/admin/posts")))

(defn posts-edit [request]
  (let [id   (parse-long (get-in request [:path-params :id]))
        post (post/find-by-id id)]
    (if post
      (-> (posts-view/form-page {:post post})
          (response/response)
          (response/content-type "text/html; charset=utf-8"))
      (response/not-found "Post not found"))))

(defn posts-update [request]
  (let [id (parse-long (get-in request [:path-params :id]))
        {:strs [title slug content excerpt status]} (:form-params request)]
    (post/update! id {:title   title
                      :slug    slug
                      :content content
                      :excerpt excerpt
                      :status  status})
    (response/redirect "/admin/posts")))

(defn posts-delete [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (post/delete! id)
    (response/redirect "/admin/posts")))
