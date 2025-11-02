-- E-Insurance Database Initialization Script
-- Run this script as PostgreSQL superuser to create all databases

-- Drop existing databases (if you want a clean start)
-- DROP DATABASE IF EXISTS user_service_db;
-- DROP DATABASE IF EXISTS policy_service_db;
-- DROP DATABASE IF EXISTS payment_service_db;
-- DROP DATABASE IF EXISTS claims_service_db;

-- Create databases for each microservice
CREATE DATABASE user_service_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE policy_service_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE payment_service_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE claims_service_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- Create application user (optional, for better security)
-- CREATE USER einsurance_user WITH PASSWORD 'your_secure_password';

-- Grant privileges to application user
-- GRANT ALL PRIVILEGES ON DATABASE user_service_db TO einsurance_user;
-- GRANT ALL PRIVILEGES ON DATABASE policy_service_db TO einsurance_user;
-- GRANT ALL PRIVILEGES ON DATABASE payment_service_db TO einsurance_user;
-- GRANT ALL PRIVILEGES ON DATABASE claims_service_db TO einsurance_user;

-- Verify databases created
\l

-- Connect to each database and enable UUID extension
\c user_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c policy_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c payment_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c claims_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Print success message
SELECT 'All databases created successfully!' AS status;