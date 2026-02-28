-- Final attempt at seeding with 1024 dimensions to match original config
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS vector_store CASCADE;

CREATE TABLE vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata jsonb,
    embedding vector(1024)
);

CREATE INDEX ON vector_store USING hnsw (embedding vector_cosine_ops);

-- Create a helper to generate a zero vector of 1024 dims
-- Then we just update 1 element to make them unique
INSERT INTO vector_store (id, content, metadata, embedding)
VALUES 
(uuid_generate_v4(), 'The bunny peeks out from its hole.', '{"timestamp": 2, "filename": "video.mp4"}', (SELECT array_fill(0, ARRAY[1024])::float4[]::vector)),
(uuid_generate_v4(), 'The bunny starts walking through the grass.', '{"timestamp": 5, "filename": "video.mp4"}', (SELECT (array_fill(0, ARRAY[1023]) || ARRAY[0.1])::float4[]::vector)),
(uuid_generate_v4(), 'The bunny looks at the butterflies.', '{"timestamp": 8, "filename": "video.mp4"}', (SELECT (array_fill(0, ARRAY[1022]) || ARRAY[0.2, 0.2])::float4[]::vector));
