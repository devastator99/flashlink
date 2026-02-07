-- Add analytics fields to url_mapping table
ALTER TABLE url_mapping 
ADD COLUMN owner_id VARCHAR(255),
ADD COLUMN redirect_count BIGINT DEFAULT 0,
ADD COLUMN ttl_seconds INT,
ADD COLUMN last_redirect_at TIMESTAMP,
ADD COLUMN metadata TEXT;

-- Create indexes for performance optimization
CREATE INDEX idx_owner_id ON url_mapping(owner_id);
CREATE INDEX idx_created_at ON url_mapping(created_at);
CREATE INDEX idx_expiry_at ON url_mapping(expiry_at);
CREATE INDEX idx_redirect_count ON url_mapping(redirect_count);
CREATE INDEX idx_last_redirect_at ON url_mapping(last_redirect_at);

-- Create composite index for common queries
CREATE INDEX idx_owner_created ON url_mapping(owner_id, created_at);
