-- User Service - Initial Migration
-- File location: user-service/src/main/resources/db/migration/V1__Create_users_table.sql

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    date_of_birth DATE,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Create indexes for performance
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Create trigger for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample admin user (will be synced with Keycloak)
-- Note: keycloak_id should match actual Keycloak user ID
INSERT INTO users (keycloak_id, email, first_name, last_name, phone, role) 
VALUES 
    ('admin-keycloak-id', 'admin@einsurance.com', 'System', 'Administrator', '+387123456789', 'ADMIN')
ON CONFLICT (keycloak_id) DO NOTHING;

-- Comments for documentation
COMMENT ON TABLE users IS 'Stores user profile information synced with Keycloak';
COMMENT ON COLUMN users.keycloak_id IS 'References the user ID in Keycloak IAM';
COMMENT ON COLUMN users.role IS 'User role: CUSTOMER or ADMIN';
COMMENT ON COLUMN users.is_active IS 'Flag for soft deletion and account status';