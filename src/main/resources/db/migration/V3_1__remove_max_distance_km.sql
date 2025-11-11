-- Remove max_distance_km column from events table
-- This field is a search criteria, not an event property

ALTER TABLE events DROP COLUMN IF EXISTS max_distance_km;
