-- Initialize Databases for SmartMoldGuard Microservices

CREATE DATABASE device_db;
CREATE DATABASE control_db;
CREATE DATABASE ai_db;
CREATE DATABASE subscription_db;
CREATE DATABASE report_db;

\c device_db

CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    status VARCHAR(50),
    icon VARCHAR(255),
    location VARCHAR(255),
    mac_address VARCHAR(255) UNIQUE NOT NULL,
    firmware_version VARCHAR(50),
    last_online_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS button_mappings (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT REFERENCES devices(id),
    switch_position INTEGER,
    device_type VARCHAR(50),
    device_name VARCHAR(255),
    icon VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS work_orders (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT REFERENCES devices(id),
    type VARCHAR(50),
    status VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS asset_compensations (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT REFERENCES devices(id),
    work_order_id BIGINT REFERENCES work_orders(id),
    amount DECIMAL(10, 2),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alarms (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT REFERENCES devices(id),
    severity VARCHAR(50),
    message TEXT,
    status VARCHAR(50),
    timestamp TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

\c ai_db

CREATE TABLE IF NOT EXISTS climate_configurations (
    id BIGSERIAL PRIMARY KEY,
    zone_code VARCHAR(50) UNIQUE NOT NULL,
    zone_name VARCHAR(255),
    temp_threshold DOUBLE PRECISION,
    humidity_threshold DOUBLE PRECISION,
    risk_factor_multiplier DOUBLE PRECISION,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

\c subscription_db

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    plan_name VARCHAR(50),
    status VARCHAR(50),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loyalty_points (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    points INTEGER,
    total_earned INTEGER,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS points_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    amount INTEGER,
    type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

\c report_db

CREATE TABLE IF NOT EXISTS daily_risk_reports (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE,
    total_risks_detected INTEGER,
    critical_risks INTEGER,
    warning_risks INTEGER,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS risk_event_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT,
    risk_level VARCHAR(50),
    detected_at TIMESTAMP
);
