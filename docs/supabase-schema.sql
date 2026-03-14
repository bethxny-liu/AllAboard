-- =============================================================================
-- AllAboard Database Schema for Supabase
-- =============================================================================
-- Run this script in the Supabase SQL Editor to create all tables
-- =============================================================================
-- Enable UUID extension (usually already enabled in Supabase)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- =============================================================================
-- ENUMS
-- =============================================================================
CREATE TYPE budget_level AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE travel_vibe AS ENUM ('RELAXED', 'ADVENTUROUS', 'BALANCED');
CREATE TYPE trip_status AS ENUM ('UPCOMING', 'ONGOING', 'COMPLETED');
CREATE TYPE activity_type AS ENUM ('LANDMARK', 'RESTAURANT', 'EXPERIENCES');
CREATE TYPE vote_type AS ENUM ('YES', 'NO', 'SUPER');
CREATE TYPE member_role AS ENUM ('OWNER', 'MEMBER');
-- =============================================================================
-- TABLES
-- =============================================================================
-- USERS table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    budget_level budget_level DEFAULT 'MEDIUM',
    travel_vibe travel_vibe DEFAULT 'BALANCED',
    interests TEXT[] DEFAULT '{}',
    image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
-- TRIPS table
CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    region VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    image_url TEXT,
    status trip_status DEFAULT 'UPCOMING',
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
-- TRIP_MEMBERS junction table
CREATE TABLE trip_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role member_role DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(trip_id, user_id)
);
-- ACTIVITIES table
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT DEFAULT '',
    rating REAL DEFAULT 0.0,
    price_level VARCHAR(10) DEFAULT '$$',
    map_pin_label VARCHAR(255),
    image_url TEXT,
    link TEXT,
    activity_type activity_type NOT NULL,
    added_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
-- VOTES table
CREATE TABLE votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    activity_id UUID NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    vote_type vote_type NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(activity_id, user_id)
);
-- ITINERARY_DAYS table
CREATE TABLE itinerary_days (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    day_date DATE NOT NULL,
    day_number INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(trip_id, day_date)
);
-- SCHEDULED_ACTIVITIES table
CREATE TABLE scheduled_activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    itinerary_day_id UUID NOT NULL REFERENCES itinerary_days(id) ON DELETE CASCADE,
    activity_id UUID NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    notes TEXT DEFAULT '',
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
-- =============================================================================
-- INDEXES
-- =============================================================================
CREATE INDEX idx_trips_created_by ON trips(created_by);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trip_members_trip_id ON trip_members(trip_id);
CREATE INDEX idx_trip_members_user_id ON trip_members(user_id);
CREATE INDEX idx_activities_trip_id ON activities(trip_id);
CREATE INDEX idx_activities_type ON activities(activity_type);
CREATE INDEX idx_votes_activity_id ON votes(activity_id);
CREATE INDEX idx_votes_user_id ON votes(user_id);
CREATE INDEX idx_votes_trip_id ON votes(trip_id);
CREATE INDEX idx_itinerary_days_trip_id ON itinerary_days(trip_id);
CREATE INDEX idx_scheduled_activities_day_id ON scheduled_activities(itinerary_day_id);
-- =============================================================================
-- VIEWS
-- =============================================================================
-- View for aggregated vote results per activity
CREATE OR REPLACE VIEW activity_vote_results AS
SELECT 
    a.id AS activity_id,
    a.trip_id,
    a.title,
    a.location,
    a.description,
    a.rating,
    a.price_level,
    a.map_pin_label,
    a.image_url,
    a.link,
    a.activity_type,
    COUNT(v.id) FILTER (WHERE v.vote_type = 'YES') AS yes_votes,
    COUNT(v.id) FILTER (WHERE v.vote_type = 'NO') AS no_votes,
    COUNT(v.id) AS total_votes,
    COALESCE(
        ROUND(
            (COUNT(v.id) FILTER (WHERE v.vote_type = 'YES')::NUMERIC / 
            NULLIF((SELECT COUNT(*) FROM trip_members tm WHERE tm.trip_id = a.trip_id), 0)) * 100, 
            2
        ), 
        0
    ) AS yes_percentage,
    (COUNT(v.id) >= (SELECT COUNT(*) FROM trip_members tm WHERE tm.trip_id = a.trip_id)) AS is_complete,
    (
        COUNT(v.id) >= (SELECT COUNT(*) FROM trip_members tm WHERE tm.trip_id = a.trip_id)
        AND COUNT(v.id) FILTER (WHERE v.vote_type = 'YES') > 
            (SELECT COUNT(*) FROM trip_members tm WHERE tm.trip_id = a.trip_id) / 2.0
    ) AS is_confirmed
FROM activities a
LEFT JOIN votes v ON a.id = v.activity_id
GROUP BY a.id, a.trip_id, a.title, a.location, a.description, a.rating, 
         a.price_level, a.map_pin_label, a.image_url, a.link, a.activity_type;
-- View for trips with member count
CREATE OR REPLACE VIEW trips_with_member_count AS
SELECT 
    t.*,
    COUNT(tm.id) AS member_count
FROM trips t
LEFT JOIN trip_members tm ON t.id = tm.trip_id
GROUP BY t.id;
-- =============================================================================
-- FUNCTIONS
-- =============================================================================
-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS \$\$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
\$\$ language 'plpgsql';
-- =============================================================================
-- TRIGGERS
-- =============================================================================
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_trips_updated_at
    BEFORE UPDATE ON trips
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_activities_updated_at
    BEFORE UPDATE ON activities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_itinerary_days_updated_at
    BEFORE UPDATE ON itinerary_days
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_scheduled_activities_updated_at
    BEFORE UPDATE ON scheduled_activities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
-- =============================================================================
-- ROW LEVEL SECURITY (RLS)
-- =============================================================================
-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE trips ENABLE ROW LEVEL SECURITY;
ALTER TABLE trip_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE itinerary_days ENABLE ROW LEVEL SECURITY;
ALTER TABLE scheduled_activities ENABLE ROW LEVEL SECURITY;
-- USERS policies
CREATE POLICY "Users can view all users" ON users
    FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON users
    FOR UPDATE USING (auth.uid()::text = id::text);
-- TRIPS policies
CREATE POLICY "Users can view trips they are members of" ON trips
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = trips.id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Users can create trips" ON trips
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);
CREATE POLICY "Trip owners can update trips" ON trips
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = trips.id 
            AND tm.user_id::text = auth.uid()::text 
            AND tm.role = 'OWNER'
        )
    );
CREATE POLICY "Trip owners can delete trips" ON trips
    FOR DELETE USING (created_by::text = auth.uid()::text);
-- TRIP_MEMBERS policies
CREATE POLICY "Members can view trip membership" ON trip_members
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM trip_members tm2 
            WHERE tm2.trip_id = trip_members.trip_id 
            AND tm2.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Owners can manage trip membership" ON trip_members
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = trip_members.trip_id 
            AND tm.user_id::text = auth.uid()::text 
            AND tm.role = 'OWNER'
        )
    );
-- ACTIVITIES policies
CREATE POLICY "Members can view activities" ON activities
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = activities.trip_id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Members can add activities" ON activities
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = activities.trip_id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Members can update activities" ON activities
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = activities.trip_id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
-- VOTES policies
CREATE POLICY "Members can view votes" ON votes
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = votes.trip_id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Users can submit their own votes" ON votes
    FOR INSERT WITH CHECK (user_id::text = auth.uid()::text);
CREATE POLICY "Users can update their own votes" ON votes
    FOR UPDATE USING (user_id::text = auth.uid()::text);
-- ITINERARY_DAYS policies
CREATE POLICY "Members can view itinerary days" ON itinerary_days
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = itinerary_days.trip_id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Members can manage itinerary days" ON itinerary_days
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM trip_members tm 
            WHERE tm.trip_id = itinerary_days.trip_id 
            AND tm.user_id::text = auth.uid()::text
        )
    );
-- SCHEDULED_ACTIVITIES policies
CREATE POLICY "Members can view scheduled activities" ON scheduled_activities
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM itinerary_days id
            JOIN trip_members tm ON tm.trip_id = id.trip_id
            WHERE id.id = scheduled_activities.itinerary_day_id
            AND tm.user_id::text = auth.uid()::text
        )
    );
CREATE POLICY "Members can manage scheduled activities" ON scheduled_activities
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM itinerary_days id
            JOIN trip_members tm ON tm.trip_id = id.trip_id
            WHERE id.id = scheduled_activities.itinerary_day_id
            AND tm.user_id::text = auth.uid()::text
        )
    );
