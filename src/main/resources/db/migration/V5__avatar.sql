CREATE TABLE avatar (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    source_media_id UUID NOT NULL,
    result_media_id UUID,
    version VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    body_measurements VARCHAR(1024)
);

CREATE INDEX idx_avatar_owner_user_id ON avatar (owner_user_id);
