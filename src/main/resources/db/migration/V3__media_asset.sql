CREATE TABLE media_asset (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    kind VARCHAR(32) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    sha256 VARCHAR(128) NOT NULL UNIQUE,
    moderation_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    retention_until TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_media_asset_user_id ON media_asset (user_id);
