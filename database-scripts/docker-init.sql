-- E-Insurance Database Initialization Script for Docker
-- This script runs automatically when PostgreSQL container starts

-- Create databases for each microservice
CREATE DATABASE user_service_db;
CREATE DATABASE policy_service_db;
CREATE DATABASE payment_service_db;
CREATE DATABASE claims_service_db;

-- Connect to each database and enable UUID extension
\c user_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c policy_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c payment_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c claims_service_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
