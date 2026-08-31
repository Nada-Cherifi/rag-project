-- Create the table to store our documents and embeddings
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content text,
    metadata JSONB,
    embedding vector(768),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- HNSW (Hierarchical Navigable Small Worlds) 
-- is an indexing algorithm that significantly speeds up nearest-neighbor searches in high-dimensional spaces.
CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);