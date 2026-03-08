-- 智慧垃圾清运系统 Database Schema
-- Based on Requirement Specification v4.1
-- PostgreSQL Dialect

-- =============================================
-- 1. System & User Module
-- =============================================

CREATE TABLE sys_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('individual', 'property', 'street', 'station', 'fleet', 'driver', 'vip', 'admin')),
    status SMALLINT DEFAULT 0, -- 0: Pending, 1: Active, 2: Rejected, 3: Disabled
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES sys_users(id),
    real_name VARCHAR(50),
    id_card_no VARCHAR(20),
    company_name VARCHAR(100),
    license_img_url VARCHAR(255),
    address VARCHAR(200),
    audit_remark VARCHAR(255),
    audit_time TIMESTAMP
);

-- =============================================
-- 2. Core Business Module (Stations, Fleets)
-- =============================================

CREATE TABLE biz_stations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type SMALLINT NOT NULL, -- 1: Clearance (Terminal), 2: Transfer
    manager_id BIGINT REFERENCES sys_users(id),
    address VARCHAR(255),
    lat DECIMAL(9,6),
    lon DECIMAL(9,6),
    region_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_fleets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    manager_id BIGINT REFERENCES sys_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_vehicles (
    id BIGSERIAL PRIMARY KEY,
    fleet_id BIGINT REFERENCES biz_fleets(id),
    plate_no VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(20),
    load_capacity DECIMAL(10,2), -- Tons
    status SMALLINT DEFAULT 1, -- 1: Active, 2: Maintenance
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Driver-Fleet Relation (Many-to-Many or One-to-Many depending on logic, spec implies simple assignment)
-- Assuming a driver belongs to one fleet at a time for simplicity or defined in user profile.
-- Let's add fleet_id to user profile or a specific table if needed.
-- For now, we link driver to fleet via an association table for flexibility.
CREATE TABLE biz_fleet_drivers (
    fleet_id BIGINT REFERENCES biz_fleets(id),
    driver_id BIGINT REFERENCES sys_users(id),
    status SMALLINT DEFAULT 1, -- 1: Active
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fleet_id, driver_id)
);

-- =============================================
-- 3. Order Module
-- =============================================

CREATE TABLE biz_orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    creator_id BIGINT REFERENCES sys_users(id),
    type SMALLINT NOT NULL, -- 1: Smart, 2: Property, 3: Street, 4: VIP, 5: Individual Self
    status SMALLINT DEFAULT 10, -- 10: Pending, 20: Assigned, 30: Loading, 40: Transit, 50: Arrived, 60: Done, 90: Cancelled
    
    -- Waste Info
    waste_type VARCHAR(50),
    waste_desc TEXT,
    est_weight DECIMAL(10,2),
    est_volume DECIMAL(10,2),
    
    -- Location
    pickup_address VARCHAR(255) NOT NULL,
    pickup_lat DECIMAL(9,6),
    pickup_lon DECIMAL(9,6),
    station_id BIGINT REFERENCES biz_stations(id),
    
    -- Assignment
    fleet_id BIGINT REFERENCES biz_fleets(id),
    driver_id BIGINT REFERENCES sys_users(id),
    vehicle_id BIGINT REFERENCES biz_vehicles(id),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_order_photos (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES biz_orders(id),
    node_type VARCHAR(20) NOT NULL, -- create, load, station_entry, unload
    file_url VARCHAR(255) NOT NULL,
    gps_lat DECIMAL(9,6),
    gps_lon DECIMAL(9,6),
    taken_at TIMESTAMP NOT NULL,
    is_watermarked BOOLEAN DEFAULT FALSE,
    exif_data JSONB
);

-- =============================================
-- 4. Mall & VIP Module
-- =============================================

CREATE TABLE biz_products (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT REFERENCES biz_stations(id),
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock DECIMAL(10,2) DEFAULT 0,
    unit VARCHAR(10),
    status SMALLINT DEFAULT 1, -- 1: On Shelf
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_vip_contracts (
    id BIGSERIAL PRIMARY KEY,
    vip_user_id BIGINT REFERENCES sys_users(id),
    contract_no VARCHAR(50) UNIQUE,
    start_date DATE,
    end_date DATE,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 5. AI Customer Service Module
-- =============================================

CREATE TABLE ai_chat_sessions (
    id VARCHAR(64) PRIMARY KEY, -- UUID
    user_id BIGINT REFERENCES sys_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) REFERENCES ai_chat_sessions(id),
    sender_type VARCHAR(10) NOT NULL CHECK (sender_type IN ('user', 'ai', 'human')),
    content TEXT NOT NULL,
    msg_type VARCHAR(10) DEFAULT 'text',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_orders_user ON biz_orders(creator_id);
CREATE INDEX idx_orders_driver ON biz_orders(driver_id);
CREATE INDEX idx_orders_status ON biz_orders(status);
CREATE INDEX idx_chat_session ON ai_chat_messages(session_id);
