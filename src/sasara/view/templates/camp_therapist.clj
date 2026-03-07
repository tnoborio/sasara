(ns sasara.view.templates.camp-therapist
  (:require [hiccup2.core :as h]
            [hiccup.page :refer [doctype]]))

;; Color palette
;;   primary:    #2a5c45  (forest green)
;;   accent:     #8b5e3c  (earth brown)
;;   bg:         #faf8f4  (cream)
;;   bg-subtle:  #f0ede8  (light cream)
;;   text:       #1c1917  (charcoal)
;;   muted:      #78716c  (stone-500)

;; ── Shared components ──────────────────────────────────────────────────────

(defn- navigation [{:keys [site-name nav-items]}]
  [:header {:style "background:#ffffff; border-bottom:1px solid #e7e5e4;"}
   [:div {:class "max-w-5xl mx-auto px-6 py-5 flex items-center justify-between"}
    [:a {:href "/" :style "color:#2a5c45; font-size:1.1rem; font-weight:600; letter-spacing:0.05em; text-decoration:none;"}
     (or site-name "")]
    [:nav {:class "hidden md:flex gap-8"}
     (for [{:keys [title slug]} nav-items]
       [:a {:href (str "/" slug)
            :style "color:#78716c; font-size:0.8rem; letter-spacing:0.1em; text-transform:uppercase; text-decoration:none;"
            :onmouseover "this.style.color='#2a5c45'"
            :onmouseout  "this.style.color='#78716c'"}
        title])
     [:a {:href "/blog"
          :style "color:#78716c; font-size:0.8rem; letter-spacing:0.1em; text-transform:uppercase; text-decoration:none;"
          :onmouseover "this.style.color='#2a5c45'"
          :onmouseout  "this.style.color='#78716c'"}
      "ブログ"]]
    ;; Mobile fallback (simple links)
    [:div {:class "md:hidden flex gap-4"}
     [:a {:href "/blog" :style "color:#78716c; font-size:0.85rem;"} "ブログ"]]]])

(defn- footer [{:keys [site-name]}]
  [:footer {:style "margin-top:6rem; border-top:1px solid #e7e5e4; background:#faf8f4;"}
   [:div {:class "max-w-5xl mx-auto px-6 py-12 text-center"}
    [:p {:style "font-size:0.8rem; color:#a8a29e; letter-spacing:0.05em;"}
     (str "© 2026 " (or site-name "") ". Powered by Sasara")]]])

(defn- base-layout [{:keys [title site-name description] :as ctx} & content]
  (str
   (:html5 doctype)
   (h/html
    [:html {:lang "ja"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title (if title (str title " | " (or site-name "")) (or site-name ""))]
      (when description
        [:meta {:name "description" :content description}])
      [:script {:src "https://cdn.tailwindcss.com"}]
      [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
      [:link {:rel "stylesheet"
              :href "https://fonts.googleapis.com/css2?family=Noto+Serif+JP:wght@300;400;500&display=swap"}]
      [:link {:rel "stylesheet" :href "/css/custom.css"}]
      [:style "body { font-family: 'Noto Serif JP', 'Hiragino Mincho ProN', serif; }"]]
     [:body {:class "min-h-screen flex flex-col" :style "background:#faf8f4; color:#1c1917;"}
      (navigation ctx)
      [:main {:class "flex-1"} content]
      (footer ctx)]])))

(defn- section-header
  "Section heading with a small underline accent."
  [text]
  [:div {:class "mb-8"}
   [:h2 {:style "font-size:0.75rem; font-weight:500; letter-spacing:0.2em; text-transform:uppercase; color:#2a5c45;"}
    text]
   [:div {:style "width:2rem; height:1px; background:#2a5c45; margin-top:0.5rem;"}]])

;; ── Page renderers ────────────────────────────────────────────────────────

(defn- render-home [{:keys [site-name posts] :as ctx}]
  (base-layout
   (assoc ctx :title nil)
   ;; Hero section
   [:section {:style "background:linear-gradient(135deg,#2a5c45 0%,#3d7a5e 60%,#4a8f70 100%); padding:6rem 1.5rem; text-align:center;"}
    [:div {:class "max-w-3xl mx-auto"}
     [:h1 {:style "font-size:2.5rem; font-weight:300; color:#ffffff; line-height:1.4; letter-spacing:0.05em; margin-bottom:1.5rem;"}
      site-name]
     [:div {:style "width:3rem; height:1px; background:rgba(255,255,255,0.5); margin:0 auto;"}]]]

   ;; Latest posts
   (when (seq posts)
     [:div {:class "max-w-5xl mx-auto px-6 py-16"}
      (section-header "最新記事")
      [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-6"}
       (for [{:keys [title slug excerpt published-at]} (take 4 posts)]
         [:article {:style "background:#ffffff; border:1px solid #e7e5e4; border-radius:2px; padding:1.5rem; transition:box-shadow 0.2s;"}
          [:a {:href (str "/blog/" slug) :style "text-decoration:none; color:inherit;"}
           [:h3 {:style "font-size:1rem; font-weight:500; color:#1c1917; margin-bottom:0.5rem; line-height:1.5;"}
            title]
           (when excerpt
             [:p {:style "font-size:0.85rem; color:#78716c; line-height:1.7; margin-bottom:0.75rem;"}
              excerpt])
           (when published-at
             [:time {:style "font-size:0.75rem; color:#a8a29e; letter-spacing:0.05em;"}
              (str published-at)])]])]])))

(defn- page-hero
  "Hero headline at the top of a page (title + horizontal rule)."
  [title]
  [:div {:style "background:linear-gradient(180deg,#f0ede8 0%,#faf8f4 100%); padding:3rem 1.5rem 2rem;"}
   [:div {:class "max-w-3xl mx-auto"}
    [:h1 {:style "font-size:2rem; font-weight:300; color:#2a5c45; letter-spacing:0.05em; margin-bottom:0.75rem;"}
     title]
    [:div {:style "width:2.5rem; height:1px; background:#2a5c45;"}]]])

(defn- render-page [{:keys [page] :as ctx}]
  (base-layout
   (assoc ctx :title (:title page) :description (:excerpt page))
   (page-hero (:title page))
   [:article {:class "max-w-3xl mx-auto px-6 py-10"}
    [:div {:class "prose prose-stone prose-lg max-w-none"}
     (h/raw (or (:content-html page) ""))]]))

(defn- render-blog-list [{:keys [posts] :as ctx}]
  (base-layout
   (assoc ctx :title "ブログ")
   (page-hero "ブログ")
   [:div {:class "max-w-3xl mx-auto px-6 py-10"}
    (if (seq posts)
      [:div {:class "space-y-6"}
       (for [{:keys [title slug excerpt published-at]} posts]
         [:article {:style "background:#ffffff; border:1px solid #e7e5e4; border-radius:2px; padding:1.5rem;"}
          [:a {:href (str "/blog/" slug) :style "text-decoration:none; color:inherit;"}
           [:h2 {:style "font-size:1.15rem; font-weight:500; color:#1c1917; margin-bottom:0.5rem; line-height:1.5;"}
            title]
           (when excerpt
             [:p {:style "font-size:0.85rem; color:#78716c; line-height:1.7; margin-bottom:0.75rem;"}
              excerpt])
           (when published-at
             [:time {:style "font-size:0.75rem; color:#a8a29e; letter-spacing:0.05em;"}
              (str published-at)])]])]
      [:p {:style "color:#78716c;"} "まだ記事がありません。"])]))

(defn- render-blog-show [{:keys [post] :as ctx}]
  (base-layout
   (assoc ctx :title (:title post) :description (:excerpt post))
   (page-hero (:title post))
   [:article {:class "max-w-3xl mx-auto px-6 py-10"}
    (when (:published-at post)
      [:time {:style "font-size:0.75rem; color:#a8a29e; letter-spacing:0.05em; display:block; margin-bottom:2rem;"}
       (str (:published-at post))])
    [:div {:class "prose prose-stone prose-lg max-w-none"}
     (h/raw (or (:content-html post) ""))]]))

(defn- render-not-found [ctx]
  (base-layout
   (assoc ctx :title "404")
   [:div {:class "max-w-3xl mx-auto px-6 py-24 text-center"}
    [:p {:style "font-size:5rem; font-weight:300; color:#2a5c45; line-height:1;"} "404"]
    [:p {:style "color:#78716c; margin:1.5rem 0 2rem;"} "ページが見つかりませんでした。"]
    [:a {:href "/"
         :style "font-size:0.8rem; letter-spacing:0.15em; text-transform:uppercase; color:#2a5c45; text-decoration:none;"}
     "← トップへ戻る"]]))

;; ── Template definition ───────────────────────────────────────────────────

(def template
  {:render-home      render-home
   :render-page      render-page
   :render-blog-list render-blog-list
   :render-blog-show render-blog-show
   :render-not-found render-not-found})
