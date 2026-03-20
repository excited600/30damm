CREATE TABLE reports (
    uuid UUID PRIMARY KEY,
    reporter_uuid UUID NOT NULL,
    target_type TEXT NOT NULL CHECK (target_type IN ('USER', 'GATHERING')),
    target_uuid UUID NOT NULL,
    reason TEXT NOT NULL CHECK (reason IN ('OFFENSIVE_CONTENT', 'ILLEGAL_OR_FALSE_INFO', 'OTHER')),
    description TEXT NULL,
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED')),
    reviewed_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_report_per_target UNIQUE (reporter_uuid, target_type, target_uuid)
);

CREATE TABLE user_blocked_gatherings (
    uuid UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    gathering_uuid UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_blocked_gathering UNIQUE (user_uuid, gathering_uuid)
);
