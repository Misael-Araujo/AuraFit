CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    status VARCHAR(255),
    completion_date TIMESTAMP WITH TIME ZONE,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    completion_attempts INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX event_publication_by_publication_date_idx
    ON event_publication (publication_date);
CREATE INDEX event_publication_by_completion_date_idx
    ON event_publication (completion_date);
