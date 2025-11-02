-- Policy Service - Initial Migration
-- File location: policy-service/src/main/resources/db/migration/V1__Create_policy_tables.sql

-- Policy Templates Table (Catalog of available insurance policies)
CREATE TABLE policy_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    coverage_amount DECIMAL(12,2),
    duration_months INT NOT NULL,
    terms_conditions TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_policy_type CHECK (type IN ('LIFE', 'TRAVEL', 'PROPERTY', 'HEALTH', 'AUTO')),
    CONSTRAINT chk_price_positive CHECK (price > 0),
    CONSTRAINT chk_coverage_positive CHECK (coverage_amount IS NULL OR coverage_amount > 0),
    CONSTRAINT chk_duration_positive CHECK (duration_months > 0)
);

-- Customer Policies Table (Purchased policies by users)
CREATE TABLE customer_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    policy_template_id UUID NOT NULL REFERENCES policy_templates(id),
    purchase_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    payment_transaction_id UUID,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_policy_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'SUSPENDED')),
    CONSTRAINT chk_dates CHECK (expiry_date > start_date),
    CONSTRAINT chk_amount_positive CHECK (total_amount > 0)
);

-- Create indexes
CREATE INDEX idx_policy_templates_type ON policy_templates(type);
CREATE INDEX idx_policy_templates_is_active ON policy_templates(is_active);
CREATE INDEX idx_customer_policies_user_id ON customer_policies(user_id);
CREATE INDEX idx_customer_policies_status ON customer_policies(status);
CREATE INDEX idx_customer_policies_policy_number ON customer_policies(policy_number);
CREATE INDEX idx_customer_policies_expiry_date ON customer_policies(expiry_date);

-- Create sequence for policy numbers
CREATE SEQUENCE policy_number_seq START WITH 1000 INCREMENT BY 1;

-- Function to generate policy number
CREATE OR REPLACE FUNCTION generate_policy_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    year_part VARCHAR(4);
    seq_part VARCHAR(10);
BEGIN
    year_part := TO_CHAR(CURRENT_DATE, 'YYYY');
    seq_part := LPAD(nextval('policy_number_seq')::TEXT, 6, '0');
    RETURN 'POL-' || year_part || '-' || seq_part;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-generate policy number if not provided
CREATE OR REPLACE FUNCTION set_policy_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.policy_number IS NULL OR NEW.policy_number = '' THEN
        NEW.policy_number := generate_policy_number();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_policy_number
    BEFORE INSERT ON customer_policies
    FOR EACH ROW
    EXECUTE FUNCTION set_policy_number();

-- Trigger for updating updated_at
CREATE TRIGGER update_policy_templates_updated_at
    BEFORE UPDATE ON policy_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customer_policies_updated_at
    BEFORE UPDATE ON customer_policies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample policy templates
INSERT INTO policy_templates (name, type, description, price, coverage_amount, duration_months, terms_conditions, is_active) VALUES
('Basic Life Insurance', 'LIFE', 'Comprehensive life insurance coverage with death benefit and optional riders', 500.00, 50000.00, 12, 'Standard terms and conditions apply. Coverage starts 30 days after purchase.', true),
('Premium Life Insurance', 'LIFE', 'Enhanced life insurance with higher coverage and additional benefits', 1200.00, 150000.00, 12, 'Premium coverage with immediate effect. Includes critical illness rider.', true),
('Travel Insurance - Europe', 'TRAVEL', 'Coverage for travel within European countries including medical emergencies', 50.00, 25000.00, 1, 'Valid for single trip up to 30 days. Medical and baggage coverage included.', true),
('Travel Insurance - Worldwide', 'TRAVEL', 'Global travel insurance with comprehensive coverage', 120.00, 50000.00, 1, 'Valid for single trip up to 60 days. Includes trip cancellation insurance.', true),
('Home Insurance Basic', 'PROPERTY', 'Basic property insurance covering fire, theft, and natural disasters', 300.00, 100000.00, 12, 'Covers building structure and contents. Deductible applies.', true),
('Home Insurance Premium', 'PROPERTY', 'Comprehensive property insurance with enhanced coverage', 600.00, 250000.00, 12, 'Full coverage including liability protection and natural disasters.', true);

-- Comments
COMMENT ON TABLE policy_templates IS 'Catalog of available insurance policies offered by the company';
COMMENT ON TABLE customer_policies IS 'Records of policies purchased by customers';
COMMENT ON COLUMN customer_policies.policy_number IS 'Unique identifier for the policy in format POL-YYYY-XXXXXX';
COMMENT ON COLUMN customer_policies.status IS 'Current status of the policy';