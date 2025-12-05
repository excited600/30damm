ALTER TABLE gatherings
    DROP CONSTRAINT gatherings_apply_type_check,
    ADD CONSTRAINT gatherings_apply_type_check
        CHECK (apply_type IN ('FIRST_IN', 'APPROVAL'));

ALTER TABLE gatherings
    DROP CONSTRAINT gatherings_category_check,
    ADD CONSTRAINT gatherings_category_check
        CHECK (category IN (
            'PARTY',
            'FOOD_DRINK',
            'STUDY',
            'INVEST',
            'LANGUAGE',
            'ACTIVITY',
            'CULTURE',
            'LOVE'
        ));