-- Convert enum columns to VARCHAR to avoid Hibernate mapping issues
-- Similar to what was done for clubs in V2_1

ALTER TABLE events ALTER COLUMN event_type TYPE VARCHAR(50);
ALTER TABLE events ALTER COLUMN sport TYPE VARCHAR(50);
ALTER TABLE events ALTER COLUMN visibility TYPE VARCHAR(50);
ALTER TABLE events ALTER COLUMN status TYPE VARCHAR(50);
