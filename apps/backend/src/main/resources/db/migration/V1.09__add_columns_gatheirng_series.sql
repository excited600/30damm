ALTER TABLE gatherings
    ADD COLUMN host_uuid UUID NOT NULL;


ALTER TABLE series
    ADD COLUMN host_uuid UUID NOT NULL;
