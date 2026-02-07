-- Add expiry_at column to url_mapping table
ALTER TABLE url_mapping 
ADD COLUMN expiry_at DATETIME NULL;
