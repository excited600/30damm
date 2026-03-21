CREATE TABLE refresh_tokens (
    uuid UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_refresh_token UNIQUE (token)
);

CREATE INDEX idx_refresh_tokens_user_uuid ON refresh_tokens (user_uuid);
