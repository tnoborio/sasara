-- Sites
CREATE TABLE sites (
  id          SERIAL PRIMARY KEY,
  name        VARCHAR(255) NOT NULL,
  slug        VARCHAR(255) UNIQUE NOT NULL,
  domain      VARCHAR(255),
  created_at  TIMESTAMPTZ DEFAULT NOW(),
  updated_at  TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Users
CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  username      VARCHAR(255) UNIQUE NOT NULL,
  email         VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(50) DEFAULT 'admin',
  is_superadmin BOOLEAN DEFAULT FALSE,
  created_at    TIMESTAMPTZ DEFAULT NOW(),
  updated_at    TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- User-Site many-to-many with role
CREATE TABLE user_sites (
  user_id  INTEGER REFERENCES users(id) ON DELETE CASCADE,
  site_id  INTEGER REFERENCES sites(id) ON DELETE CASCADE,
  role     VARCHAR(50) NOT NULL DEFAULT 'editor',
  PRIMARY KEY (user_id, site_id)
);

--;;

-- Site settings (key-value, per site)
CREATE TABLE site_settings (
  site_id    INTEGER REFERENCES sites(id),
  key        VARCHAR(255) NOT NULL,
  value      TEXT,
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  PRIMARY KEY (site_id, key)
);

--;;

-- Blog posts
CREATE TABLE posts (
  id           SERIAL PRIMARY KEY,
  site_id      INTEGER REFERENCES sites(id),
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) NOT NULL,
  content      TEXT,
  content_html TEXT,
  excerpt      TEXT,
  status       VARCHAR(50) DEFAULT 'draft',
  author_id    INTEGER REFERENCES users(id),
  published_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (site_id, slug)
);

--;;

-- Static pages
CREATE TABLE pages (
  id           SERIAL PRIMARY KEY,
  site_id      INTEGER REFERENCES sites(id),
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) NOT NULL,
  content      TEXT,
  content_html TEXT,
  template     VARCHAR(255) DEFAULT 'default',
  sort_order   INTEGER DEFAULT 0,
  status       VARCHAR(50) DEFAULT 'draft',
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (site_id, slug)
);

--;;

-- Portfolio works
CREATE TABLE works (
  id           SERIAL PRIMARY KEY,
  site_id      INTEGER REFERENCES sites(id),
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) UNIQUE NOT NULL,
  description  TEXT,
  content      TEXT,
  content_html TEXT,
  tech_stack   TEXT[],
  url          VARCHAR(500),
  image_url    VARCHAR(500),
  sort_order   INTEGER DEFAULT 0,
  status       VARCHAR(50) DEFAULT 'draft',
  start_date   DATE,
  end_date     DATE,
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Services
CREATE TABLE services (
  id           SERIAL PRIMARY KEY,
  site_id      INTEGER REFERENCES sites(id),
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) UNIQUE NOT NULL,
  description  TEXT,
  content      TEXT,
  content_html TEXT,
  icon         VARCHAR(255),
  sort_order   INTEGER DEFAULT 0,
  status       VARCHAR(50) DEFAULT 'draft',
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Tags
CREATE TABLE tags (
  id      SERIAL PRIMARY KEY,
  site_id INTEGER REFERENCES sites(id),
  name    VARCHAR(255) NOT NULL,
  slug    VARCHAR(255) NOT NULL,
  UNIQUE (site_id, slug)
);

--;;

-- Post-Tag many-to-many
CREATE TABLE post_tags (
  post_id INTEGER REFERENCES posts(id) ON DELETE CASCADE,
  tag_id  INTEGER REFERENCES tags(id) ON DELETE CASCADE,
  PRIMARY KEY (post_id, tag_id)
);

--;;

-- Media uploads
CREATE TABLE media (
  id            SERIAL PRIMARY KEY,
  site_id       INTEGER REFERENCES sites(id),
  filename      VARCHAR(500) NOT NULL,
  original_name VARCHAR(500),
  content_type  VARCHAR(255),
  size_bytes    BIGINT,
  url           VARCHAR(500),
  alt_text      TEXT,
  uploaded_by   INTEGER REFERENCES users(id),
  created_at    TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Indexes
CREATE INDEX idx_posts_site_id ON posts(site_id);
--;;
CREATE INDEX idx_posts_status ON posts(status);
--;;
CREATE INDEX idx_posts_published_at ON posts(published_at DESC);
--;;
CREATE INDEX idx_posts_slug ON posts(site_id, slug);
--;;
CREATE INDEX idx_pages_site_id ON pages(site_id);
--;;
CREATE INDEX idx_pages_slug ON pages(site_id, slug);
--;;
CREATE INDEX idx_works_slug ON works(slug);
--;;
CREATE INDEX idx_services_slug ON services(slug);
--;;
CREATE INDEX idx_user_sites_user_id ON user_sites(user_id);
--;;
CREATE INDEX idx_user_sites_site_id ON user_sites(site_id);
