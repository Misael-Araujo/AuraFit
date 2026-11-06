CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    user_id UUID,
    event_type VARCHAR(64) NOT NULL,
    event_details TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX audit_event_user_id_idx ON audit_event (user_id, created_at DESC);
CREATE INDEX audit_event_type_idx ON audit_event (event_type, created_at DESC);

CREATE TABLE content_report (
    id UUID PRIMARY KEY,
    reported_by_user_id UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    target_type VARCHAR(32) NOT NULL,
    target_id UUID NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    reviewed_by_user_id UUID,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX content_report_status_idx ON content_report (status, created_at DESC);
CREATE INDEX content_report_target_idx ON content_report (target_type, target_id, created_at DESC);
