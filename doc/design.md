# Sasara — Clojure CMS 基本設計

> 作成日: 2026-03-04 / ステータス: Phase 0（プロトタイプ）

## 概要

Sasaraは、Clojure製の軽量セルフホストCMSです。
エンジニア・フリーランス向けのポートフォリオサイト構築ツールとして開発し、
OSS化・事業化を目指します。

### なぜ作るのか

- Clojure CMS領域には「バッテリー同梱型」のプロダクトが存在しない
- 管理UI付きのセルフホスト型CMSがClojure界にない
- 『はじめてのClojure』著者 + Tokyo.clj主催者としてのブランドと一致
- サイト自体がポートフォリオであり、プロダクトのライブデモになる

---

## 1. 技術スタック

| レイヤー | 技術 | ライブラリ |
|---------|------|-----------|
| HTTP Server | Ring + Jetty | `ring/ring-jetty-adapter 1.13.0` |
| Routing | Reitit | `metosin/reitit-ring 0.7.2` |
| HTML生成 | Hiccup | `hiccup 2.0.0-RC4` |
| CSS | Tailwind CSS | CDN（Phase 0）→ ビルド（後のPhase） |
| Database | PostgreSQL | `next.jdbc 1.3.939` + `HikariCP 6.2.1` |
| Migrations | Migratus | `migratus 1.5.8` |
| Markdown | Flexmark | `flexmark-all 0.64.8` |
| Auth | Buddy | `buddy-auth 3.0.323` + `buddy-hashers 2.0.167` |
| Config | Aero | `aero 1.1.6` |
| Build | deps.edn | Clojure CLI |

---

## 2. プロジェクト構造

```
sasara/
├── deps.edn                      # 依存関係定義
├── config.edn                    # Aero設定ファイル
├── doc/
│   └── design.md                 # このファイル
├── resources/
│   ├── migrations/               # SQLマイグレーション
│   │   ├── 001-init.up.sql
│   │   └── 001-init.down.sql
│   └── public/                   # 静的アセット
│       ├── css/custom.css
│       ├── js/
│       └── images/
├── src/sasara/
│   ├── core.clj                  # エントリポイント、サーバー起動/停止
│   ├── config.clj                # Aero設定読み込み
│   ├── db.clj                    # HikariCPコネクションプール、SQL実行ヘルパー
│   ├── routes.clj                # Reititルート定義
│   ├── middleware.clj             # Ring middleware構成
│   ├── auth.clj                  # Buddy認証（セッション）
│   ├── markdown.clj              # Flexmark Markdown→HTML変換
│   ├── handler/
│   │   ├── public.clj            # 公開ページハンドラ
│   │   └── admin.clj             # 管理画面ハンドラ
│   ├── model/
│   │   ├── post.clj              # 記事CRUD
│   │   ├── user.clj              # ユーザー認証
│   │   └── setting.clj           # サイト設定KV
│   └── view/
│       ├── layout.clj            # 共通HTMLレイアウト
│       ├── components.clj        # 共通UIコンポーネント
│       ├── public/
│       │   ├── home.clj          # トップページ
│       │   ├── about.clj         # About
│       │   └── blog.clj          # ブログ一覧・詳細
│       └── admin/
│           ├── layout.clj        # 管理画面レイアウト
│           └── posts.clj         # 記事管理UI・ログイン
├── test/sasara/
│   ├── model/
│   └── handler/
└── dev/
    └── user.clj                  # REPL開発ヘルパー
```

---

## 3. データベーススキーマ

### コアテーブル

| テーブル | 用途 | Phase |
|---------|------|-------|
| `users` | 管理者ユーザー | 0 |
| `site_settings` | サイト設定（KV） | 0 |
| `posts` | ブログ記事 | 0 |
| `pages` | 固定ページ | 1 |
| `works` | 実績・ポートフォリオ | 1 |
| `services` | サービスメニュー | 1 |
| `tags` | タグ | 1 |
| `post_tags` | 記事-タグ多対多 | 1 |
| `media` | メディアファイル | 2 |

### 主要テーブル定義

#### users
```sql
CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  username      VARCHAR(255) UNIQUE NOT NULL,
  email         VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(50) DEFAULT 'admin',
  created_at    TIMESTAMPTZ DEFAULT NOW(),
  updated_at    TIMESTAMPTZ DEFAULT NOW()
);
```

#### posts
```sql
CREATE TABLE posts (
  id           SERIAL PRIMARY KEY,
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) UNIQUE NOT NULL,
  content      TEXT,              -- Markdown原文
  content_html TEXT,              -- レンダリング済みHTML（キャッシュ）
  excerpt      TEXT,
  status       VARCHAR(50) DEFAULT 'draft',  -- draft / published / archived
  author_id    INTEGER REFERENCES users(id),
  published_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW()
);
```

#### works
```sql
CREATE TABLE works (
  id           SERIAL PRIMARY KEY,
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) UNIQUE NOT NULL,
  description  TEXT,
  content      TEXT,
  content_html TEXT,
  tech_stack   TEXT[],            -- PostgreSQL配列
  url          VARCHAR(500),
  image_url    VARCHAR(500),
  sort_order   INTEGER DEFAULT 0,
  status       VARCHAR(50) DEFAULT 'draft',
  start_date   DATE,
  end_date     DATE,
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW()
);
```

全テーブルの定義は `resources/migrations/001-init.up.sql` を参照。

---

## 4. ルーティング設計

### 公開ページ
| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/` | トップページ |
| GET | `/about` | About |
| GET | `/services` | サービス一覧 |
| GET | `/services/:slug` | サービス詳細 |
| GET | `/works` | 実績一覧 |
| GET | `/works/:slug` | 実績詳細 |
| GET | `/blog` | ブログ一覧 |
| GET | `/blog/:slug` | ブログ記事 |
| GET | `/blog/tag/:slug` | タグ別一覧 |
| GET | `/contact` | お問い合わせ |
| GET | `/feed.xml` | RSSフィード |

### 管理画面（セッション認証必須）
| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/admin` | ダッシュボード |
| GET/POST | `/admin/login` | ログイン |
| POST | `/admin/logout` | ログアウト |
| GET | `/admin/posts` | 記事一覧 |
| GET | `/admin/posts/new` | 新規作成 |
| POST | `/admin/posts` | 記事作成 |
| GET/POST | `/admin/posts/:id` | 編集/更新 |
| POST | `/admin/posts/:id/delete` | 削除 |

同パターンで pages, works, services, media, settings。

---

## 5. アーキテクチャ方針

### レイヤー構造
```
Request → Reitit Router → Handler → Model → DB
                              ↓
                           View (Hiccup) → HTML Response
```

### 設計原則
- **サーバーサイドレンダリング**: Hiccup + Tailwind CDN。SPAは後のPhaseで検討
- **Markdownベース**: 記事はMarkdown原文を保存、保存時にHTMLキャッシュも生成
- **セッション認証**: Buddy session backendでシンプルに
- **コネクションプール**: HikariCPで管理
- **マイグレーション**: Migratusで管理、起動時に自動実行
- **REPL駆動開発**: dev/user.cljでサーバー起動/停止をREPLから操作

---

## 6. 開発ロードマップ

### Phase 0（現在）: 基本設計 + 最小プロトタイプ
- プロジェクト構造、依存関係、DB接続
- トップ / About / ブログ（公開ページ）
- 記事CRUD（管理画面）
- Markdown→HTML変換

### Phase 1: 自分のサイトとして運用開始
- pages / works / services の管理機能
- noteの既存記事をインポート
- ドメイン取得、デプロイ

### Phase 2: CMS機能の充実
- テーマシステム
- RSS / OGP / SEO対応
- メディア管理（画像アップロード）
- 管理UIの改善

### Phase 3: OSS公開
- ドキュメント整備
- GitHubでOSS公開
- Clojureコミュニティへの告知

### Phase 4: 事業化
- 有料テーマ・ホスティング
- ユーザーフィードバックを元に改善
