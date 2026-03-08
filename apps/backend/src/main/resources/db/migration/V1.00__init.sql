CREATE TABLE users (
    uuid UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255) NOT NULL,
    age integer NOT NULL,
    gender TEXT NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    introduction TEXT NULL,
    password TEXT NOT NULL,
    phone_number TEXT NULL,
    phone_authenticated BOOLEAN NULL,
    hearts INTEGER NOT NULL DEFAULT 0,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    provider TEXT NOT NULL CHECK (provider IN ('THIRTY_FORTY', 'KAKAO')),
    profile_image_url TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (email)
);

CREATE TABLE gatherings (
    uuid UUID PRIMARY KEY,
    host_uuid UUID NOT NULL,
    min_capacity INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    gender_ratio_enabled BOOLEAN NOT NULL,
    max_female_count INTEGER NULL,
    max_male_count INTEGER NULL,
    total_guests INTEGER NOT NULL,
    fee INTEGER NOT NULL,
    is_split BOOLEAN NOT NULL,
    place TEXT NULL,
    category TEXT NOT NULL CHECK (category IN (
        'NONE',
        'PARTY',
        'FOOD_DRINK',
        'ACTIVITY'
    )),
    status TEXT NOT NULL CHECK (status IN ('OPEN_PENDING', 'OPEN', 'CLOSED', 'IN_PROGRESS', 'FINISHED')),
    image_url TEXT NULL,
    title TEXT NOT NULL,
    description TEXT NULL,
    click_count INTEGER NOT NULL DEFAULT 0,
    start_date_time TIMESTAMP NULL,
    duration INTERVAL NULL,
    day_of_week TEXT NULL CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    score INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE Guests(
    gathering_uuid UUID NOT NULL,
    user_uuid UUID NOT NULL,
    joined_at TIMESTAMP,
    PRIMARY KEY (gathering_uuid, user_uuid)
);

CREATE TABLE series (
    uuid UUID PRIMARY KEY,
    host_uuid UUID NOT NULL,
    min_capacity INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    gender_ratio_enabled BOOLEAN NOT NULL,
    max_female_count INTEGER NULL,
    max_male_count INTEGER NULL,
    fee INTEGER NOT NULL,
    is_split BOOLEAN NOT NULL,
    place TEXT NULL,
    category TEXT NOT NULL CHECK (category IN ('NONE', 'PARTY', 'FOOD_DRINK', 'ACTIVITY')),
    image_url TEXT NULL,
    title TEXT NOT NULL,
    description TEXT NULL,
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

CREATE TABLE payments (
    uuid UUID PRIMARY KEY,
    payment_id VARCHAR(100) NOT NULL UNIQUE,
    product_type VARCHAR(255) NOT NULL,
    product_uuid UUID NOT NULL,
    amount INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status in ('PENDING', 'READY', 'VIRTUAL_ACCOUNT_ISSUED', 'PAID', 'FAILED', 'CANCELLED', 'PARTIAL_CANCELLED')),
    product_name VARCHAR(200) NOT NULL,
    buyer_uuid UUID NOT NULL,
    buyer_email VARCHAR(100) NULL,
    buyer_name VARCHAR(50) NULL,
    buyer_phone VARCHAR(20) NULL,
    transaction_id VARCHAR(100) NULL,
    cancelled_amount INTEGER NOT NULL DEFAULT 0,
    paid_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancel_reason VARCHAR(500) NULL,
    failed_at TIMESTAMP NULL,
    fail_reason VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
