<p align="center">
  <img src="sasara.png" alt="Sasara" width="200">
</p>

<h1 align="center">Sasara</h1>

<p align="center">
  A lightweight, self-hosted CMS built with Clojure.
</p>

---

## What is Sasara?

Sasara is a server-side rendered CMS designed for building corporate websites, organization pages, portfolios, and blogs — all manageable from an admin panel. No JavaScript framework required.

### Key Features

- **Page Management** — Create and organize pages with hierarchical (parent-child) structure
- **Blog** — Write and publish articles in Markdown with draft/published/archived workflow
- **SEO** — Per-page meta tags, OGP, sitemap.xml, robots.txt, canonical URLs, and structured data (JSON-LD)
- **Google Analytics** — Set your GA4 measurement ID from the admin panel
- **Site Configuration** — Manage site name, description, logo, favicon, and navigation from the admin panel

### Tech Stack

| Layer | Technology |
|-------|-----------|
| HTTP Server | Ring + Jetty |
| Routing | Reitit |
| HTML | Hiccup (server-side rendering) |
| CSS | Tailwind CSS |
| Database | PostgreSQL (next.jdbc + HikariCP) |
| Migrations | Migratus |
| Markdown | Flexmark |
| Auth | Buddy (session-based) |
| Config | Aero |

## Getting Started

### Prerequisites

- Java 11+
- Clojure CLI (`clj`)
- PostgreSQL

### Setup

1. Create a PostgreSQL database:

```sql
CREATE DATABASE sasara;
```

2. Start a REPL:

```bash
clj -A:dev
```

3. In the REPL:

```clojure
(migrate!)  ; Run database migrations
(seed!)     ; Create admin user (admin/admin)
(start!)    ; Start server at http://localhost:3000
```

4. Visit `http://localhost:3000/admin/login` and log in with `admin` / `admin`.

## License

TBD
