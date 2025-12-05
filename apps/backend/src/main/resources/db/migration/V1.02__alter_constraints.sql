ALTER TABLE users
    DROP CONSTRAINT users_provider_check;

ALTER TABLE users
    ADD CONSTRAINT users_provider_check
        CHECK (provider IN ('THIRTY_FORTY', 'KAKAO'));