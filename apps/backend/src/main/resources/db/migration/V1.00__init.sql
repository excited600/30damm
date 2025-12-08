CREATE TABLE users (
    uuid UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255) NOT NULL,
    age integer NULL,
    gender TEXT NOT NULL CHECK (gender IN ('M', 'F')),
    introduction TEXT NULL,
    password TEXT NOT NULL,
    phone_number TEXT NOT NULL,
    phone_authenticated BOOLEAN NOT NULL,
    hearts INTEGER NOT NULL DEFAULT 0,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    provider TEXT NOT NULL CHECK (provider IN ('THIRTY_FORTY', 'KAKAO')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (email),
    UNIQUE (phone_number)
);

CREATE TABLE gatherings (
    uuid UUID PRIMARY KEY,
    host_uuid UUID NOT NULL,
    approve_type TEXT NOT NULL CHECK (approve_type IN ('FIRST_IN', 'APPROVAL')),
    min_capacity INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    gender_ratio_enabled BOOLEAN NOT NULL,
    min_age INTEGER NOT NULL,
    max_age INTEGER NOT NULL,
    max_female_count INTEGER NULL,
    max_male_count INTEGER NULL,
    current_female_count INTEGER NULL,
    current_male_count INTEGER NULL,
    total_guests INTEGER NOT NULL,
    fee INTEGER NOT NULL,
    discount_enabled BOOLEAN NOT NULL,
    offline BOOLEAN NOT NULL,
    place TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN (
        'PARTY',
        'FOOD_DRINK',
        'STUDY',
        'INVEST',
        'LANGUAGE',
        'ACTIVITY',
        'CULTURE',
        'LOVE'
    )),
    sub_category TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('CLOSED', 'OPEN', 'CANCELLED')),
    image_url TEXT NOT NULL,
    title TEXT NOT NULL,
    introduction TEXT NULL,
    click_count INTEGER NOT NULL DEFAULT 0,
    start_date_time TIMESTAMP NOT NULL,
    duration INTERVAL NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE participants(
    gathering_uuid UUID NOT NULL,
    user_uuid UUID NOT NULL,
    is_host BOOLEAN NOT NULL,
    PRIMARY KEY (gathering_uuid, user_uuid)
);

CREATE TABLE series (
    uuid UUID PRIMARY KEY,
    host_uuid UUID NOT NULL,
    approve_type TEXT NOT NULL CHECK (approve_type IN ('FIRST_IN', 'APPROVAL')),
    min_capacity INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    gender_ratio_enabled BOOLEAN NOT NULL,
    min_age INTEGER NOT NULL,
    max_age INTEGER NOT NULL,
    max_female_count INTEGER NULL,
    max_male_count INTEGER NULL,
    fee INTEGER NOT NULL,
    discount_enabled BOOLEAN NOT NULL,
    offline BOOLEAN NOT NULL,
    place TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('PARTY', 'FOOD_DRINK','STUDY','INVEST','LANGUAGE','ACTIVITY','CULTURE','LOVE')),
    sub_category TEXT NOT NULL CHECK (sub_category IN ('HOME_PARTY')),
    image_url TEXT NOT NULL,
    title TEXT NOT NULL,
    introduction TEXT NULL,
    click_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE series_schedules(
    seq BIGSERIAL PRIMARY KEY,
    schedule_type TEXT NOT NULL CHECK (schedule_type IN ('WEEKLY', 'DATE')),
    open_day_of_week TEXT NULL CHECK (open_day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    start_day_of_week TEXT NULL CHECK (start_day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    schedule_start_date date NULL,
    schedule_end_date date NULL,
    open_date date NULL,
    start_date date NULL,
    start_time time NOT NULL,
    duration INTERVAL NULL,
    series_uuid UUID NOT NULL,

    CONSTRAINT valid_weekly_schedule CHECK (
        schedule_type != 'WEEKLY' OR (
            open_day_of_week IS NOT NULL AND
            start_day_of_week IS NOT NULL AND
            schedule_start_date IS NOT NULL AND
            schedule_end_date IS NOT NULL AND
            start_date IS NULL AND
            open_date IS NULL
        )
    ),

    CONSTRAINT valid_date_schedule CHECK (
        schedule_type != 'DATE' OR (
            open_day_of_week IS NULL AND
            start_day_of_week IS NULL AND
            schedule_start_date IS NULL AND
            schedule_end_date IS NULL AND
            start_date IS NOT NULL AND
            open_date IS NOT NULL
        )
    )
);

--CREATE TABLE user_keywords (
--    uuid UUID PRIMARY KEY,
--    user_uuid UUID NOT NULL,
--    key TEXT NOT NULL,
--    value TEXT NOT NULL,
--    UNIQUE (user_uuid, key)
--);

--CREATE TABLE gathering_tags(
--    gathering_uuid UUID NOT NULL,
--    hash_tag_uuid UUID NOT NULL,
--    PRIMARY KEY (gathering_uuid, hash_tag_uuid)
--);
--
--CREATE TABLE hash_tags(
--    uuid UUID PRIMARY KEY,
--    tag_value TEXT NOT NULL
--);

--CREATE TABLE follows(
--    follower_uuid UUID NOT NULL,
--    followee_uuid UUID NOT NULL,
--    status SMALLINT NOT NULL,
--    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
--    PRIMARY KEY (follower_uuid, followee_uuid)
--);
--
--CREATE TABLE chat_room(
--    uuid UUID PRIMARY KEY,
--    type VARCHAR(20) NOT NULL CHECK (type in ('DM', 'GROUP')),
--    title TEXT NOT NULL,
--    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
--);
--
--CREATE TABLE chat_room_participants(
--    room_uuid UUID NOT NULL,
--    user_uuid UUID NOT NULL,
--    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
--    last_read_msg_uuid UUID,
--    UNIQUE(room_uuid, user_uuid)
--);
--
--CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'LOCATION');
--
--CREATE TABLE chat_messages(
--    uuid UUID PRIMARY KEY,
--    room_uuid UUID NOT NULL,
--    sender_uuid UUID NOT NULL,
--    type message_type NOT NULL,
--    content TEXT,
--    payload JSONB,
--    created_at TIMESTAMP NOT NULL DEFAULT NOW()
--);
--
--CREATE TABLE feeds(
--    uuid UUID PRIMARY KEY,
--    user_uuid UUID NOT NULL,
--    image_url TEXT NULL,
--    body TEXT NOT NULL,
--    location TEXT NULL,
--    gathering_uuid UUID NULL,
--    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
--);
--
--CREATE TABLE feed_tags(
--    feed_uuid UUID NOT NULL,
--    hash_tag_uuid UUID NOT NULL,
--    PRIMARY KEY (feed_uuid, hash_tag_uuid)
--);
--
--CREATE TYPE media_type AS ENUM('IMAGE', 'VIDEO');
--
--CREATE TABLE stories (
--    uuid UUID PRIMARY KEY,
--    user_uuid UUID NOT NULL,
--    media_type media_type NOT NULL,
--    storage_key TEXT NOT NULL,
--    mime_type VARCHAR(50) NOT NULL,
--    width INT,
--    height INT,
--    duration_ms INT,
--    thumbnail_key TEXT,
--    sort_order INT NOT NULL DEFAULT 0,
--    expires_at TIMESTAMP NOT NULL,
--    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
--);
--
--CREATE TABLE feed_users(
--    feed_uuid UUID NOT NULL,
--    user_uuid UUID NOT NULL,
--    PRIMARY KEY (feed_uuid, user_uuid)
--);
--
--CREATE TABLE comment_on_users(
--    from_uuid UUID NOT NULL,
--    to_uuid UUID NOT NULL,
--    comment TEXT NOT NULL,
--    created_at TIMESTAMP NOT NULL,
--    updated_at TIMESTAMP NOT NULL,
--    PRIMARY KEY(from_uuid, to_uuid)
--);
--
--CREATE TABLE self_matched_date(
--    uuid UUID NOT NULL,
--    host_uuid UUID NOT NULL,
--    image_url TEXT NOT NULL,
--    introduction TEXT NOT NULL,
--    residence TEXT NULL,
--    PRIMARY KEY (uuid)
--);
--
--CREATE TABLE self_matched_date_keywords(
--    self_matched_date_uuid UUID NOT NULL,
--    key TEXT NOT NULL,
--    value TEXT NOT NULL,
--    PRIMARY KEY (self_matched_date_uuid, key)
--);
--
--CREATE TABLE blocked_users(
--    from_uuid UUID NOT NULL,
--    to_uuid UUID NOT NULL,
--    PRIMARY KEY (from_uuid, to_uuid)
--);
--
--CREATE TABLE male_discount(
--    gathering_uuid UUID PRIMARY KEY,
--    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
--    discount_rate INTEGER NULL,
--    discount_amount INTEGER NULL,
--    CHECK (
--        (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
--        (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
--    )
--);
--
--CREATE TABLE female_discount(
--    gathering_uuid UUID PRIMARY KEY,
--    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
--    discount_rate INTEGER NULL,
--    discount_amount INTEGER NULL,
--    CHECK (
--        (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
--        (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
--    )
--);
--
--CREATE TABLE loyal_discount(
--    gathering_uuid UUID PRIMARY KEY,
--    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
--    discount_rate INTEGER NULL,
--    discount_amount INTEGER NULL,
--    loyal_condition_count INTEGER NOT NULL,
--    CHECK (
--        (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
--        (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
--    )
--);
--
--CREATE TABLE early_discount(
--    gathering_uuid UUID PRIMARY KEY,
--    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
--    discount_rate INTEGER NULL,
--    discount_amount INTEGER NULL,
--    early_limit_count INTEGER NOT NULL,
--    CHECK (
--        (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
--        (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
--    )
--);
--
--CREATE TABLE newbie_discount(
--    gathering_uuid UUID PRIMARY KEY,
--    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
--    discount_rate INTEGER NULL,
--    discount_amount INTEGER NULL,
--    host_uuid UUID NOT NULL,
--    CHECK (
--        (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
--        (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
--    )
--);
--
--CREATE TABLE heart_discount(
--    gathering_uuid UUID PRIMARY KEY,
--    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
--    discount_rate INTEGER NULL,
--    discount_amount INTEGER NULL,
--    heart_condition_count INTEGER NOT NULL,
--    CHECK (
--        (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
--        (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
--    )
--);

--CREATE TYPE payment_target_type AS ENUM (
--    'GATHERING',
--    'SELF_MATCHED_DATE'
--);

--CREATE TABLE payment (
--    uuid UUID PRIMARY KEY,
--    buyer_uuid UUID NOT NULL,
--    seller_uuid UUID NOT NULL,
--    target_type payment_target_type NOT NULL,
--    target_uuid UUID NOT NULL,
--    title TEXT NOT NULL,
--    amount INTEGER NOT NULL,
--    currency CHAR(3) NOT NULL DEFAULT 'KRW',
--    pg_provider TEXT,
--    pg_transaction_id TEXT,
--    status TEXT NOT NULL CHECK (status IN ('PENDING','PAID','FAILED','REFUNDED')),
--    created_at TIMESTAMP NOT NULL DEFAULT now()
--);
