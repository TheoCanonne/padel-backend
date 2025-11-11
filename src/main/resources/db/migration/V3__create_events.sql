-- Create enums for events
CREATE TYPE event_type AS ENUM ('MATCH', 'TOURNAMENT', 'TRAINING', 'FRIENDLY');
CREATE TYPE event_status AS ENUM ('OPEN', 'FULL', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'POSTPONED');
CREATE TYPE event_visibility AS ENUM ('PUBLIC', 'PRIVATE');
CREATE TYPE sport AS ENUM ('PADEL', 'TENNIS', 'FUTSAL', 'BASKETBALL', 'VOLLEYBALL');

-- Events table
CREATE TABLE events (
    id UUID PRIMARY KEY,

    -- Type and sport
    event_type event_type NOT NULL DEFAULT 'MATCH',
    sport sport NOT NULL DEFAULT 'PADEL',

    -- References
    club_id UUID NOT NULL REFERENCES clubs(id) ON DELETE RESTRICT,
    organizer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

    -- Schedule
    start_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,

    -- Capacity
    total_slots INTEGER NOT NULL DEFAULT 4,
    occupied_slots INTEGER NOT NULL DEFAULT 0,

    -- Eligibility rules
    min_level INTEGER CHECK (min_level >= 1 AND min_level <= 10),
    max_level INTEGER CHECK (max_level >= 1 AND max_level <= 10),
    max_distance_km INTEGER CHECK (max_distance_km > 0),

    -- Visibility and status
    visibility event_visibility NOT NULL DEFAULT 'PUBLIC',
    status event_status NOT NULL DEFAULT 'OPEN',

    -- Description
    description TEXT,

    -- Options
    waiting_list_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    auto_accept BOOLEAN NOT NULL DEFAULT TRUE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason TEXT,

    -- Constraints
    CONSTRAINT check_dates CHECK (end_date_time > start_date_time),
    CONSTRAINT check_levels CHECK (min_level IS NULL OR max_level IS NULL OR min_level <= max_level),
    CONSTRAINT check_slots CHECK (occupied_slots >= 0 AND occupied_slots <= total_slots)
);

-- Indexes for performance
CREATE INDEX idx_events_club_id ON events(club_id);
CREATE INDEX idx_events_organizer_id ON events(organizer_id);
CREATE INDEX idx_events_sport ON events(sport);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_start_date_time ON events(start_date_time);
CREATE INDEX idx_events_visibility ON events(visibility);

-- Composite indexes for common queries
CREATE INDEX idx_events_status_start_date ON events(status, start_date_time);
CREATE INDEX idx_events_club_status ON events(club_id, status);
CREATE INDEX idx_events_organizer_status ON events(organizer_id, status);

-- Trigger to automatically update updated_at
CREATE TRIGGER update_events_updated_at BEFORE UPDATE ON events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE events IS 'Événements (parties, tournois, entraînements)';
COMMENT ON COLUMN events.event_type IS 'Type d''événement : MATCH, TOURNAMENT, TRAINING, FRIENDLY';
COMMENT ON COLUMN events.sport IS 'Sport pratiqué';
COMMENT ON COLUMN events.total_slots IS 'Nombre total de places disponibles';
COMMENT ON COLUMN events.occupied_slots IS 'Nombre de places occupées (incluant l''organisateur)';
COMMENT ON COLUMN events.min_level IS 'Niveau minimum requis (1-10)';
COMMENT ON COLUMN events.max_level IS 'Niveau maximum accepté (1-10)';
COMMENT ON COLUMN events.max_distance_km IS 'Distance maximale en km depuis le club';
COMMENT ON COLUMN events.visibility IS 'PUBLIC : visible par tous, PRIVATE : sur invitation uniquement';
COMMENT ON COLUMN events.status IS 'État de l''événement';
COMMENT ON COLUMN events.waiting_list_enabled IS 'Active la liste d''attente quand l''événement est complet';
COMMENT ON COLUMN events.auto_accept IS 'Acceptation automatique des candidatures (sinon validation manuelle)';
