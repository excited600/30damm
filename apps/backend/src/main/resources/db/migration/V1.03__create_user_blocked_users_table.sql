CREATE TABLE user_blocked_users (
    uuid UUID PRIMARY KEY,
    blocker_uuid UUID NOT NULL,
    blocked_uuid UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_blocked_user UNIQUE (blocker_uuid, blocked_uuid)
);
