CREATE TABLE generation_job (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    job_type VARCHAR(64) NOT NULL,
    payload VARCHAR(2048),
    idempotency_key VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_for TIMESTAMP WITH TIME ZONE NOT NULL,
    lease_until TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(2000)
);

CREATE INDEX idx_generation_job_user_id ON generation_job (user_id);
CREATE INDEX idx_generation_job_status ON generation_job (status);
CREATE UNIQUE INDEX idx_generation_job_idempotency_key ON generation_job (user_id, idempotency_key);
