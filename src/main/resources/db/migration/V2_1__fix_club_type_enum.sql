-- Remove default value first
ALTER TABLE clubs ALTER COLUMN type DROP DEFAULT;

-- Convert the column to VARCHAR
ALTER TABLE clubs ALTER COLUMN type TYPE VARCHAR(50);

-- Drop the enum type
DROP TYPE club_type;

-- Add back a default value
ALTER TABLE clubs ALTER COLUMN type SET DEFAULT 'PADEL';

-- Add a check constraint to ensure valid values
ALTER TABLE clubs ADD CONSTRAINT check_club_type CHECK (type IN ('PADEL'));
