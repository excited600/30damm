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
    provider TEXT NOT NULL CHECK (provider IN ('3040', 'kakao', 'naver', 'google')),
    UNIQUE (email),
    UNIQUE (phone_number)
);

CREATE TABLE user_keywords (
    uuid UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    UNIQUE (user_uuid, key)
);

CREATE TABLE gatherings (
    uuid UUID PRIMARY KEY,
    apply_type TEXT NOT NULL CHECK (apply_type IN ('first_in', 'approval')),
    min_capacity INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    gender_ratio_enabled BOOLEAN NOT NULL,
    min_age INTEGER NOT NULL,
    max_age INTEGER NOT NULL,
    max_female_count INTEGER NULL,
    max_male_count INTEGER NULL,
    current_female_count INTEGER NULL,
    current_male_count INTEGER NULL,
    total_attendees INTEGER NOT NULL,
    fee INTEGER NOT NULL,
    discount_enabled BOOLEAN NOT NULL,
    offline BOOLEAN NOT NULL,
    place TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('party', 'food,drink','study','invest','language','activity','culture','love')),
    sub_category TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('CLOSED', 'OPEN', 'CANCELLED')),
    image_url TEXT NOT NULL,
    title TEXT NOT NULL,
    introduction TEXT NULL,
    click_count INTEGER NOT NULL DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE gathering_tags(
    gathering_uuid UUID NOT NULL,
    hash_tag_uuid UUID NOT NULL,
    PRIMARY KEY (gathering_uuid, hash_tag_uuid)
);

CREATE TABLE hash_tags(
    uuid UUID PRIMARY KEY,
    tag_value TEXT NOT NULL
);

CREATE TABLE participants(
    gathering_uuid UUID NOT NULL,
    user_uuid UUID NOT NULL,
    is_host BOOLEAN NOT NULL,
    PRIMARY KEY (gathering_uuid, user_uuid)
);

CREATE TABLE follows(
    follower_uuid UUID NOT NULL,
    followee_uuid UUID NOT NULL,
    status SMALLINT NOT NULL, -- 0: pending, 1: accepted, 2: blocked 등
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_uuid, followee_uuid)
);

CREATE TABLE chat_room(
    uuid UUID PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type in ('DM', 'GROUP')),
    title TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_room_participants(
    room_uuid UUID NOT NULL,
    user_uuid UUID NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_read_msg_uuid UUID, -- 이거 어떻게 쓰는건지... 읽음표시라는데.
    UNIQUE(room_uuid, user_uuid)
);

CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'LOCATION'); -- location 도 text는 아닌지 확인

CREATE TABLE chat_messages(
    uuid UUID PRIMARY KEY,
    room_uuid UUID NOT NULL,
    sender_uuid UUID NOT NULL,
    type message_type NOT NULL,
    content TEXT,
    payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE feeds(
    uuid UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    image_url TEXT NULL,
    body TEXT NOT NULL,
    location TEXT NULL, -- Text 타입이 맞는지 확인
    gathering_uuid UUID NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE feed_tags(
    feed_uuid UUID NOT NULL,
    hash_tag_uuid UUID NOT NULL,
    PRIMARY KEY (feed_uuid, hash_tag_uuid)
);

CREATE TYPE media_type AS ENUM('IMAGE', 'VIDEO');

CREATE TABLE stories (
    uuid             UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    media_type       media_type NOT NULL,     -- IMAGE or VIDEO

    storage_key      TEXT NOT NULL, -- s3 key (예: "stories/2025/11/27/abc123.mp4")
    mime_type        VARCHAR(50) NOT NULL,

    width            INT,
    height           INT,
    duration_ms      INT,
    thumbnail_key    TEXT,

    sort_order       INT NOT NULL DEFAULT 0,
    expires_at       TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE feed_users(
    feed_uuid UUID NOT NULL,
    user_uuid UUID NOT NULL,
    PRIMARY KEY (feed_uuid, user_uuid)
);

CREATE TABLE comment_on_users(
    from_uuid UUID NOT NULL,
    to_uuid UUID NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(from_uuid, to_uuid)
);

CREATE TABLE self_matched_date(
    uuid UUID NOT NULL,
    host_uuid UUID NOT NULL,
    image_url TEXT NOT NULL,
    introduction TEXT NOT NULL,
    residence TEXT NULL,
    PRIMARY KEY (uuid)
);

CREATE TABLE self_matched_date_keywords(
    self_matched_date_uuid UUID NOT NULL,
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    PRIMARY KEY (self_matched_date_uuid, key)
);


CREATE TABLE blocked_users(
    from_uuid UUID NOT NULL,
    to_uuid UUID NOT NULL,
    PRIMARY KEY (from_uuid, to_uuid)
);

CREATE TABLE male_discount(
    gathering_uuid UUID PRIMARY KEY,
    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
    discount_rate INTEGER NULL,
    discount_amount INTEGER NULL,
    CHECK (
          (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
          (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
        )
);

CREATE TABLE female_discount(
    gathering_uuid UUID PRIMARY KEY,
    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
    discount_rate INTEGER NULL,
    discount_amount INTEGER NULL,
    CHECK (
              (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
              (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
            )
);

CREATE TABLE loyal_discount(
    gathering_uuid UUID PRIMARY KEY,
    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
    discount_rate INTEGER NULL,
    discount_amount INTEGER NULL,
    loyal_condition_count INTEGER NOT NULL,
    CHECK (
              (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
              (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
            )
);

CREATE TABLE early_discount(
    gathering_uuid UUID PRIMARY KEY,
    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
    discount_rate INTEGER NULL,
    discount_amount INTEGER NULL,
    early_limit_count INTEGER NOT NULL,
    CHECK (
              (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
              (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
            )
);

CREATE TABLE newbie_discount(
    gathering_uuid UUID PRIMARY KEY,
    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
    discount_rate INTEGER NULL,
    discount_amount INTEGER NULL,
    host_uuid UUID NOT NULL,
    CHECK (
              (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
              (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
            )
);

CREATE TABLE heart_discount(
    gathering_uuid UUID PRIMARY KEY,
    discount_type TEXT NOT NULL CHECK (discount_type in ('AMOUNT', 'PERCENT')),
    discount_rate INTEGER NULL,
    discount_amount INTEGER NULL,
    heart_condition_count INTEGER NOT NULL,
    CHECK (
              (discount_type = 'AMOUNT'  AND discount_amount IS NOT NULL AND discount_rate IS NULL) OR
              (discount_type = 'PERCENT' AND discount_rate   IS NOT NULL AND discount_amount IS NULL)
            )
);

CREATE TYPE payment_target_type AS ENUM (
  'GATHERING',
  'SELF_MATCHED_DATE'
);

CREATE TABLE payment (
    uuid            UUID PRIMARY KEY,
    buyer_uuid        UUID NOT NULL,
    seller_uuid       UUID NOT NULL,
    target_type     payment_target_type NOT NULL,
    target_uuid      UUID NOT NULL,
    title  TEXT NOT NULL,
    amount   INTEGER NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    pg_provider     TEXT,                          -- 'KAKAOPAY', 'TOSS', 'CARD' 등
    pg_transaction_id TEXT,                        -- PG에서 받은 키
    status          TEXT NOT NULL CHECK (status IN ('PENDING','PAID','FAILED','REFUNDED')),                 -- 'PAID', 'FAILED'
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);