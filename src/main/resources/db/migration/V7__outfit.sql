CREATE TABLE outfit (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    current_render_media_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE outfit_item (
    outfit_id UUID NOT NULL REFERENCES outfit(id) ON DELETE CASCADE,
    garment_id UUID NOT NULL,
    slot VARCHAR(16) NOT NULL,
    item_order INTEGER NOT NULL,
    PRIMARY KEY (outfit_id, item_order)
);

CREATE TABLE outfit_season (
    outfit_id UUID NOT NULL REFERENCES outfit(id) ON DELETE CASCADE,
    season VARCHAR(32) NOT NULL,
    PRIMARY KEY (outfit_id, season)
);

CREATE TABLE outfit_occasion (
    outfit_id UUID NOT NULL REFERENCES outfit(id) ON DELETE CASCADE,
    occasion VARCHAR(32) NOT NULL,
    PRIMARY KEY (outfit_id, occasion)
);

CREATE INDEX idx_outfit_owner_created_at ON outfit(owner_user_id, created_at DESC);
CREATE INDEX idx_outfit_owner_favorite ON outfit(owner_user_id, favorite);
CREATE INDEX idx_outfit_owner_name ON outfit(owner_user_id, name);
