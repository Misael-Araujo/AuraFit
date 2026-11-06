CREATE TABLE garment (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    source VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    category VARCHAR(64),
    subcategory VARCHAR(64),
    color_family VARCHAR(64),
    pattern VARCHAR(128),
    material VARCHAR(128),
    brand VARCHAR(128),
    size VARCHAR(32),
    notes TEXT,
    purchase_url TEXT,
    price NUMERIC(10, 2),
    original_media_ids TEXT,
    digital_garment_media_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE garment_color (
    garment_id UUID NOT NULL REFERENCES garment(id) ON DELETE CASCADE,
    color_value VARCHAR(64) NOT NULL,
    PRIMARY KEY (garment_id, color_value)
);

CREATE TABLE garment_season (
    garment_id UUID NOT NULL REFERENCES garment(id) ON DELETE CASCADE,
    season VARCHAR(32) NOT NULL,
    PRIMARY KEY (garment_id, season)
);

CREATE TABLE garment_occasion (
    garment_id UUID NOT NULL REFERENCES garment(id) ON DELETE CASCADE,
    occasion VARCHAR(32) NOT NULL,
    PRIMARY KEY (garment_id, occasion)
);

CREATE INDEX idx_garment_owner_created_at ON garment(owner_user_id, created_at DESC);
CREATE INDEX idx_garment_owner_status ON garment(owner_user_id, status);
CREATE INDEX idx_garment_category ON garment(category);
CREATE INDEX idx_garment_source ON garment(source);
