(ns sasara.handler.admin
  (:require [sasara.model.post :as post]
            [sasara.model.page :as page]
            [sasara.model.media :as media]
            [sasara.model.user :as user]
            [sasara.model.site :as site]
            [sasara.model.user-site :as user-site]
            [sasara.view.admin.posts :as posts-view]
            [sasara.view.admin.pages :as pages-view]
            [sasara.view.admin.media :as media-view]
            [sasara.view.admin.layout :as admin-layout]
            [sasara.view.admin.site-selector :as site-selector-view]
            [sasara.view.admin.settings :as settings-view]
            [sasara.view.admin.super.sites :as super-sites-view]
            [sasara.view.admin.super.users :as super-users-view]
            [sasara.publisher :as publisher]
            [sasara.storage :as storage]
            [sasara.model.setting :as setting]
            [ring.util.response :as response]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.util UUID]))

(defn- html-response [body]
  (-> (response/response body)
      (response/content-type "text/html; charset=utf-8")))

(defn- current-site-id [request]
  (get-in request [:session :current-site-id]))

(defn- site-name [request]
  (:name (:current-site request)))

;; --- Auth ---

(defn login-page [request]
  (html-response
   (posts-view/login-page {:error (get-in request [:flash :error])})))

(defn login-submit [request]
  (let [{:strs [username password]} (:form-params request)]
    (if-let [user (user/authenticate username password)]
      (-> (response/redirect "/admin")
          (assoc-in [:session :identity] {:id            (:id user)
                                          :username      (:username user)
                                          :is-superadmin (:is-superadmin user)}))
      (-> (response/redirect "/admin/login")
          (assoc :flash {:error "Invalid username or password"})))))

(defn logout [_request]
  (-> (response/redirect "/admin/login")
      (assoc :session nil)))

;; --- Site Selector ---

(defn site-selector [request]
  (let [user-id      (get-in request [:session :identity :id])
        is-super     (get-in request [:session :identity :is-superadmin])
        sites        (user-site/find-sites-for-user user-id)]
    ;; Superadmin with no sites assigned: redirect to super admin panel
    (if (and is-super (empty? sites))
      (response/redirect "/admin/super/sites")
      (html-response
       (site-selector-view/page {:sites          sites
                                 :is-superadmin  is-super})))))

(defn select-site [request]
  (let [site-id (parse-long (get-in request [:path-params :id]))]
    (-> (response/redirect "/admin/dashboard")
        (assoc :session (assoc (:session request) :current-site-id site-id)))))

;; --- Dashboard ---

(defn dashboard [request]
  (let [site-id (current-site-id request)]
    (if-not site-id
      (response/redirect "/admin")
      (let [posts (post/find-all site-id {:limit 5})]
        (html-response
         (admin-layout/admin-layout
          {:title "Dashboard" :site-name (site-name request)}
          [:div {:class "grid grid-cols-1 md:grid-cols-3 gap-6 mb-8"}
           [:div {:class "bg-white p-6 rounded-lg shadow"}
            [:p {:class "text-sm text-gray-500"} "Total Posts"]
            [:p {:class "text-3xl font-bold"} (count (post/find-all site-id))]]
           [:div {:class "bg-white p-6 rounded-lg shadow"}
            [:p {:class "text-sm text-gray-500"} "Published"]
            [:p {:class "text-3xl font-bold"} (count (post/find-published site-id))]]
           [:div {:class "bg-white p-6 rounded-lg shadow"}
            [:p {:class "text-sm text-gray-500"} "Drafts"]
            [:p {:class "text-3xl font-bold"} (count (post/find-all site-id {:status "draft"}))]]]
          [:h2 {:class "text-lg font-semibold mb-4"} "Recent Posts"]
          [:div
           (for [{:keys [id title status]} posts]
             [:div {:class "flex justify-between items-center py-2 border-b"}
              [:a {:href (str "/admin/posts/" id) :class "hover:text-blue-600"} title]
              [:span {:class "text-sm text-gray-500"} status]])]))))))

;; --- Posts CRUD ---

(defn posts-index [request]
  (let [site-id (current-site-id request)
        posts   (post/find-all site-id)]
    (html-response
     (posts-view/list-page {:posts posts :site-name (site-name request)}))))

(defn posts-new [request]
  (let [site-id (current-site-id request)]
    (html-response
     (posts-view/form-page {:post nil :site-name (site-name request)
                            :media (media/find-all site-id)}))))

(defn posts-create [request]
  (let [{:strs [title slug content excerpt status]} (:form-params request)
        author-id (get-in request [:session :identity :id])
        site-id   (current-site-id request)
        saved     (post/create! {:title     title
                                 :slug      (when-not (empty? slug) slug)
                                 :content   content
                                 :excerpt   excerpt
                                 :status    status
                                 :author-id author-id
                                 :site-id   site-id})]
    (publisher/on-post-save! (storage/get-storage) site-id saved)
    (response/redirect "/admin/posts")))

(defn posts-edit [request]
  (let [id      (parse-long (get-in request [:path-params :id]))
        site-id (current-site-id request)
        p       (post/find-by-id id)]
    (if p
      (html-response
       (posts-view/form-page {:post p :site-name (site-name request)
                              :media (media/find-all site-id)}))
      (response/not-found "Post not found"))))

(defn posts-update [request]
  (let [id      (parse-long (get-in request [:path-params :id]))
        site-id (current-site-id request)
        {:strs [title slug content excerpt status]} (:form-params request)
        saved   (post/update! id {:title   title
                                  :slug    slug
                                  :content content
                                  :excerpt excerpt
                                  :status  status})]
    (publisher/on-post-save! (storage/get-storage) site-id saved)
    (response/redirect "/admin/posts")))

(defn publish-all [request]
  (let [site-id (current-site-id request)]
    (publisher/publish-site! (storage/get-storage) site-id)
    (response/redirect "/admin/dashboard")))

(defn posts-delete [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (post/delete! id)
    (response/redirect "/admin/posts")))

;; --- Pages ---

(defn pages-index [request]
  (let [site-id (current-site-id request)
        pages   (page/find-all site-id)]
    (html-response
     (pages-view/list-page {:pages pages :site-name (site-name request)}))))

(defn pages-new [request]
  (let [site-id (current-site-id request)]
    (html-response
     (pages-view/form-page {:page nil :site-name (site-name request)
                            :media (media/find-all site-id)}))))

(defn pages-create [request]
  (let [{:strs [title slug content excerpt status sort-order]} (:form-params request)
        site-id (current-site-id request)
        saved   (page/create! {:title      title
                               :slug       slug
                               :content    content
                               :excerpt    excerpt
                               :status     status
                               :sort-order (when sort-order (parse-long sort-order))
                               :site-id    site-id})]
    (publisher/on-page-save! (storage/get-storage) site-id saved)
    (response/redirect "/admin/pages")))

(defn pages-edit [request]
  (let [id      (parse-long (get-in request [:path-params :id]))
        site-id (current-site-id request)
        p       (page/find-by-id id)]
    (if p
      (html-response
       (pages-view/form-page {:page p :site-name (site-name request)
                              :media (media/find-all site-id)}))
      (response/not-found "Page not found"))))

(defn pages-update [request]
  (let [id      (parse-long (get-in request [:path-params :id]))
        site-id (current-site-id request)
        {:strs [title slug content excerpt status sort-order]} (:form-params request)
        saved   (page/update! id {:title      title
                                  :slug       slug
                                  :content    content
                                  :excerpt    excerpt
                                  :status     status
                                  :sort-order (when sort-order (parse-long sort-order))})]
    (publisher/on-page-save! (storage/get-storage) site-id saved)
    (response/redirect "/admin/pages")))

(defn pages-delete [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (page/delete! id)
    (response/redirect "/admin/pages")))

;; --- Media ---

(defn- ext-from-content-type [content-type]
  (case content-type
    "image/jpeg"    ".jpg"
    "image/png"     ".png"
    "image/gif"     ".gif"
    "image/webp"    ".webp"
    "image/svg+xml" ".svg"
    ".bin"))

(defn media-index [request]
  (let [site-id (current-site-id request)
        items   (media/find-all site-id)]
    (html-response
     (media-view/list-page {:media items :site-name (site-name request)}))))

(defn media-upload [request]
  (let [site-id    (current-site-id request)
        user-id    (get-in request [:session :identity :id])
        file-param (get-in request [:params "file"])
        alt-text   (get-in request [:params "alt-text"])
        {:keys [tempfile filename content-type size]} file-param
        ext        (ext-from-content-type content-type)
        uuid-name  (str (UUID/randomUUID) ext)
        rel-path   (str "uploads/" site-id "/" uuid-name)
        public-url (str "/" rel-path)]
    (storage/put-image! (storage/get-storage) rel-path (io/input-stream tempfile))
    (media/create! {:site-id       site-id
                    :filename      uuid-name
                    :original-name filename
                    :content-type  content-type
                    :size-bytes    size
                    :url           public-url
                    :alt-text      (when-not (str/blank? alt-text) alt-text)
                    :uploaded-by   user-id})
    (response/redirect "/admin/media")))

(defn media-delete [request]
  (let [id      (parse-long (get-in request [:path-params :id]))
        deleted (media/delete! id)]
    (when-let [url (:url deleted)]
      (let [file (io/file (str "public" url))]
        (when (.exists file) (.delete file))))
    (response/redirect "/admin/media")))

(defn media-picker [request]
  (let [site-id (current-site-id request)
        items   (media/find-all site-id)]
    (-> (response/response (media-view/picker-page {:media items}))
        (response/content-type "text/html; charset=utf-8"))))

;; --- Settings ---

(defn settings-index [request]
  (let [site-id (current-site-id request)]
    (html-response
     (settings-view/settings-page
      {:site-name        (setting/get-setting site-id "site-name")
       :current-template (setting/get-setting site-id "template")}))))

(defn settings-update [request]
  (let [site-id  (current-site-id request)
        {:strs [site-name template]} (:form-params request)]
    (when-not (empty? site-name)
      (setting/set-setting! site-id "site-name" site-name))
    (when-not (empty? template)
      (setting/set-setting! site-id "template" template))
    (response/redirect "/admin/settings")))

;; --- Superadmin: Sites ---

(defn super-sites-index [_request]
  (html-response
   (super-sites-view/list-page {:sites (site/find-all)})))

(defn super-sites-new [_request]
  (html-response
   (super-sites-view/form-page {:site nil})))

(defn super-sites-create [request]
  (let [{:strs [name slug domain]} (:form-params request)]
    (site/create! {:name   name
                   :slug   (when-not (empty? slug) slug)
                   :domain (when-not (empty? domain) domain)})
    (response/redirect "/admin/super/sites")))

(defn super-sites-edit [request]
  (let [id (parse-long (get-in request [:path-params :id]))
        s  (site/find-by-id id)]
    (if s
      (html-response
       (super-sites-view/form-page {:site s}))
      (response/not-found "Site not found"))))

(defn super-sites-update [request]
  (let [id (parse-long (get-in request [:path-params :id]))
        {:strs [name slug domain]} (:form-params request)]
    (site/update! id {:name   name
                      :slug   slug
                      :domain (when-not (empty? domain) domain)})
    (response/redirect "/admin/super/sites")))

(defn super-sites-delete [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (site/delete! id)
    (response/redirect "/admin/super/sites")))

;; --- Superadmin: Users ---

(defn super-users-index [_request]
  (html-response
   (super-users-view/list-page {:users (user/find-all)})))

(defn super-users-new [_request]
  (html-response
   (super-users-view/form-page {:user       nil
                                :sites      (site/find-all)
                                :user-sites []})))

(defn- parse-site-assignments
  "Parse site role assignments from form params."
  [form-params sites]
  (for [{:keys [id]} sites
        :let [checked (get form-params (str "site-" id))
              role    (get form-params (str "role-" id) "editor")]
        :when checked]
    {:site-id id :role role}))

(defn super-users-create [request]
  (let [{:strs [username email password is-superadmin]} (:form-params request)
        sites       (site/find-all)
        assignments (parse-site-assignments (:form-params request) sites)
        new-user    (user/create! {:username       username
                                   :email          email
                                   :password       password
                                   :is-superadmin  (= is-superadmin "true")})]
    (doseq [{:keys [site-id role]} assignments]
      (user-site/add-user! (:id new-user) site-id role))
    (response/redirect "/admin/super/users")))

(defn super-users-edit [request]
  (let [id    (parse-long (get-in request [:path-params :id]))
        u     (user/find-by-id id)
        sites (site/find-all)
        us    (when u
                (for [s (user-site/find-sites-for-user id)]
                  {:site-id (:id s) :role (:role s)}))]
    (if u
      (html-response
       (super-users-view/form-page {:user       (dissoc u :password-hash)
                                    :sites      sites
                                    :user-sites (vec us)}))
      (response/not-found "User not found"))))

(defn super-users-update [request]
  (let [id (parse-long (get-in request [:path-params :id]))
        {:strs [username email password is-superadmin]} (:form-params request)
        sites       (site/find-all)
        assignments (parse-site-assignments (:form-params request) sites)]
    ;; Update user info
    (user/update! id {:username      username
                      :email         email
                      :password      password
                      :is-superadmin (= is-superadmin "true")})
    ;; Update site assignments: delete all then re-add
    (doseq [{site-id :id} sites]
      (user-site/remove-user! id site-id))
    (doseq [{:keys [site-id role]} assignments]
      (user-site/add-user! id site-id role))
    (response/redirect "/admin/super/users")))

(defn super-users-delete [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (user/delete! id)
    (response/redirect "/admin/super/users")))
