-- Remove validation constraints from events table
-- Validation should be handled at application level, not database level

-- Remove check constraints
ALTER TABLE events DROP CONSTRAINT IF EXISTS check_dates;
ALTER TABLE events DROP CONSTRAINT IF EXISTS check_levels;
ALTER TABLE events DROP CONSTRAINT IF EXISTS check_slots;
ALTER TABLE events DROP CONSTRAINT IF EXISTS events_min_level_check;
ALTER TABLE events DROP CONSTRAINT IF EXISTS events_max_level_check;
ALTER TABLE events DROP CONSTRAINT IF EXISTS events_max_distance_km_check;
