-- Create club_type enum
CREATE TYPE club_type AS ENUM ('PADEL');

-- Clubs table
CREATE TABLE clubs (
    id UUID PRIMARY KEY,
    google_place_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    formatted_address TEXT NOT NULL,

    -- Club type
    type club_type NOT NULL DEFAULT 'PADEL',

    -- Geographic coordinates using PostGIS
    location GEOGRAPHY(POINT, 4326) NOT NULL,

    -- Address components
    street_number VARCHAR(50),
    route VARCHAR(255),
    locality VARCHAR(255),
    postal_code VARCHAR(20),
    country VARCHAR(100),

    -- Google Places data
    rating DECIMAL(2, 1),
    total_ratings INTEGER,
    phone_number VARCHAR(50),
    website VARCHAR(500),
    google_maps_url VARCHAR(500),
    photo_url VARCHAR(500),

    -- Business hours (JSON format)
    opening_hours JSONB,

    -- Status
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),

    -- Google Places sync
    last_synced_at TIMESTAMP
);

-- Spatial index on location for fast geo queries
CREATE INDEX idx_clubs_location ON clubs USING GIST(location);

-- Index on google_place_id for lookups
CREATE INDEX idx_clubs_google_place_id ON clubs(google_place_id);

-- Index on name for text search
CREATE INDEX idx_clubs_name ON clubs(name);

-- Index on locality for city-based search
CREATE INDEX idx_clubs_locality ON clubs(locality);

-- Index on is_active for filtering
CREATE INDEX idx_clubs_is_active ON clubs(is_active);

-- Function to update updated_at timestamp (if not already exists)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to automatically update updated_at
CREATE TRIGGER update_clubs_updated_at BEFORE UPDATE ON clubs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();