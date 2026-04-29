CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE technologies (
      id SERIAL PRIMARY KEY,
      name VARCHAR(255) UNIQUE NOT NULL,
      slug VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE tracked_repositories (
      id SERIAL PRIMARY KEY,
      owner VARCHAR(255) NOT NULL,
      name VARCHAR(255) NOT NULL,
      is_active BOOLEAN DEFAULT TRUE,
      tech_id INTEGER REFERENCES technologies(id),
      created_at TIMESTAMP DEFAULT NOW(),
      last_synced_at TIMESTAMP WITH TIME ZONE,
      UNIQUE(owner, name)
);

CREATE TABLE artifacts (
    id SERIAL PRIMARY KEY,
    repo_id INTEGER REFERENCES tracked_repositories(id),
    tech_id INTEGER REFERENCES technologies(id),

    source VARCHAR(50) DEFAULT 'github',
    external_id VARCHAR(255) NOT NULL,
    type VARCHAR(50)  NOT NULL,

    title TEXT,
    body TEXT,
    url TEXT,
    author VARCHAR(255),
    state VARCHAR(50),

    source_created_at TIMESTAMP WITH TIME ZONE,
    source_updated_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP DEFAULT NOW(),

    UNIQUE(source, type, repo_id, external_id)
);

CREATE TABLE artifact_payloads (
   artifact_id INTEGER PRIMARY KEY REFERENCES artifacts(id) ON DELETE CASCADE,
   raw_payload JSONB
);

CREATE TABLE artifact_intelligence (
    artifact_id INTEGER PRIMARY KEY REFERENCES artifacts(id) ON DELETE CASCADE,
    change_type VARCHAR(50), 
    risk_level VARCHAR(50),
    confidence VARCHAR(20),

    embedding_model VARCHAR(50),
    summarizer_model VARCHAR(50),

    ai_summary JSONB,
    embedding vector(768),
    processing_status VARCHAR(20) DEFAULT 'PENDING',
    enriched_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50),
    description TEXT
);

CREATE TABLE intelligence_tags (
    intel_id INTEGER REFERENCES artifact_intelligence(artifact_id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(id) ON DELETE CASCADE,
    confidence_score FLOAT DEFAULT 1.0,
    PRIMARY KEY (intel_id, tag_id)
);

CREATE INDEX ON artifact_intelligence USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_intel_risk_type ON artifact_intelligence (risk_level, change_type);
CREATE INDEX idx_artifacts_repo_id ON artifacts(repo_id);
CREATE INDEX idx_artifacts_type ON artifacts(type);


-- SEED DATA
INSERT INTO technologies (id, name, slug) VALUES
    (1, 'LangChain', 'langchain'),
    (2, 'Ollama', 'ollama'),
    (3, 'Spring Boot', 'spring-boot'),
    (4, 'React', 'react'),
    (5, 'tRPC', 'trpc'),
    (6, 'Next.js', 'nextjs'),
    (7, 'TypeScript', 'typescript')
    ON CONFLICT (id) DO NOTHING;

SELECT setval('technologies_id_seq', (SELECT MAX(id) FROM technologies));

INSERT INTO tracked_repositories (owner, name, tech_id) VALUES
    ('langchain-ai', 'langchain', 1),
    ('ollama', 'ollama', 2),
    ('spring-projects', 'spring-boot', 3),
    ('facebook', 'react', 4),
    ('trpc', 'trpc', 5),
    ('vercel', 'next.js', 6),
    ('microsoft', 'TypeScript', 7)
    ON CONFLICT (owner, name) DO NOTHING;

SELECT setval('tracked_repositories_id_seq', (SELECT MAX(id) FROM tracked_repositories));