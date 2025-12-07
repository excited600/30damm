CREATE TABLE series (
    uuid UUID PRIMARY KEY,
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
    click_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE series_schedules(
    seq BIGSERIAL PRIMARY KEY,
    schedule_type TEXT NOT NULL CHECK (schedule_type IN ('WEEKLY', 'DATE')),
    day_of_week TEXT NULL CHECK (day_of_week IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
    schedule_start_date date NULL,
    schedule_end_date date NULL,
    date date NULL,
    time time NOT NULL,
    duration INTERVAL NULL,
    schedule_uuid UUID,

    CONSTRAINT valid_weekly_schedule CHECK (
            schedule_type != 'WEEKLY' OR (
                day_of_week IS NOT NULL AND
                schedule_start_date IS NOT NULL AND
                schedule_end_date IS NOT NULL AND
                date IS NULL
            )
        ),

        CONSTRAINT valid_date_schedule CHECK (
            schedule_type != 'DATE' OR (
                day_of_week IS NULL AND
                schedule_start_date IS NULL AND
                schedule_end_date IS NULL AND
                date IS NOT NULL
            )
        )
);