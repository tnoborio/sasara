-- Users
CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  username      VARCHAR(255) UNIQUE NOT NULL,
  email         VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(50) DEFAULT 'admin',
  created_at    TIMESTAMPTZ DEFAULT NOW(),
  updated_at    TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Site settings (key-value)
CREATE TABLE site_settings (
  key        VARCHAR(255) PRIMARY KEY,
  value      TEXT,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Blog posts
CREATE TABLE posts (
  id           SERIAL PRIMARY KEY,
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) UNIQUE NOT NULL,
  content      TEXT,
  content_html TEXT,
  excerpt      TEXT,
  status       VARCHAR(50) DEFAULT 'draft',
  author_id    INTEGER REFERENCES users(id),
  published_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Static pages
CREATE TABLE pages (
  id           SERIAL PRIMARY KEY,
  title        VARCHAR(500) NOT NULL,
  slug         VARCHAR(500) UNIQUE NOT NULL,
  content      TEXT,
  content_html TEXT,
  template     VARCHAR(255) DEFAULT 'default',
  sort_order   INTEGER DEFAULT 0,
  status       VARCHAR(50) DEFAULT 'draft',
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  updated_at   TIMESTAMPTZ DEFAULT NOW()
);

--;;

-- Portfolio works
CREATE TABLE works (
  id           SERIAL PRIMARY KEY,
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
  id   SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL
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

CREATE INDEX idx_posts_status ON posts(status);
--;;
CREATE INDEX idx_posts_published_at ON posts(published_at DESC);
--;;
CREATE INDEX idx_posts_slug ON posts(slug);
--;;
CREATE INDEX idx_pages_slug ON pages(slug);
--;;
CREATE INDEX idx_works_slug ON works(slug);
--;;
CREATE INDEX idx_services_slug ON services(slug);
