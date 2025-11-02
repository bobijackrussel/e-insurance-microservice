-- Claims Service - Initial Migration
-- File location: claims-service/src/main/resources/db/migration/V1__Create_claims_tables.sql

-- Claims table
CREATE TABLE claims (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    claim_number VARCHAR(50) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    customer_policy_id UUID NOT NULL,
    
    -- Claim details
    amount DECIMAL(10,2) NOT NULL,
    description TEXT NOT NULL,
    incident_date DATE NOT NULL,
    
    -- Status and workflow
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_date TIMESTAMP,
    reviewed_by UUID,
    admin_notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_claim_status CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'PAID')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_incident_date CHECK (incident_date <= CURRENT_DATE)
);

-- Claim documents table (for supporting evidence)
CREATE TABLE claim_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_file_size CHECK (file_size > 0 AND file_size <= 10485760) -- Max 10MB
);

-- Claim history/audit trail
CREATE TABLE claim_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by UUID NOT NULL,
    change_reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_old_status CHECK (old_status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'PAID')),
    CONSTRAINT chk_new_status CHECK (new_status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'PAID'))
);

-- Create indexes
CREATE INDEX idx_claims_user_id ON claims(user_id);
CREATE INDEX idx_claims_customer_policy_id ON claims(customer_policy_id);
CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_claim_number ON claims(claim_number);
CREATE INDEX idx_claims_submitted_date ON claims(submitted_date DESC);
CREATE INDEX idx_claim_documents_claim_id ON claim_documents(claim_id);
CREATE INDEX idx_claim_status_history_claim_id ON claim_status_history(claim_id);

-- Create sequence for claim numbers
CREATE SEQUENCE claim_number_seq START WITH 1000 INCREMENT BY 1;

-- Function to generate claim number
CREATE OR REPLACE FUNCTION generate_claim_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    year_part VARCHAR(4);
    seq_part VARCHAR(10);
BEGIN
    year_part := TO_CHAR(CURRENT_DATE, 'YYYY');
    seq_part := LPAD(nextval('claim_number_seq')::TEXT, 6, '0');
    RETURN 'CLM-' || year_part || '-' || seq_part;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-generate claim number
CREATE OR REPLACE FUNCTION set_claim_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.claim_number IS NULL OR NEW.claim_number = '' THEN
        NEW.claim_number := generate_claim_number();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_claim_number
    BEFORE INSERT ON claims
    FOR EACH ROW
    EXECUTE FUNCTION set_claim_number();

-- Trigger for updating updated_at
CREATE TRIGGER update_claims_updated_at
    BEFORE UPDATE ON claims
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to log status changes to history table
CREATE OR REPLACE FUNCTION log_claim_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO claim_status_history (
            claim_id, 
            old_status, 
            new_status, 
            changed_by, 
            change_reason
        ) VALUES (
            NEW.id,
            OLD.status,
            NEW.status,
            NEW.reviewed_by,
            NEW.admin_notes
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_claim_status_change
    AFTER UPDATE ON claims
    FOR EACH ROW
    EXECUTE FUNCTION log_claim_status_change();

-- Trigger to set reviewed_date when status changes from PENDING
CREATE OR REPLACE FUNCTION set_reviewed_date()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status != 'PENDING' AND OLD.status = 'PENDING' AND NEW.reviewed_date IS NULL THEN
        NEW.reviewed_date := CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_reviewed_date
    BEFORE UPDATE ON claims
    FOR EACH ROW
    EXECUTE FUNCTION set_reviewed_date();

-- View for claims summary by status
CREATE VIEW claims_summary_by_status AS
SELECT 
    status,
    COUNT(*) AS claim_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS average_amount,
    MIN(submitted_date) AS oldest_claim,
    MAX(submitted_date) AS newest_claim
FROM claims
GROUP BY status;

-- View for pending claims requiring admin action
CREATE VIEW pending_claims_for_review AS
SELECT 
    c.id,
    c.claim_number,
    c.user_id,
    c.customer_policy_id,
    c.amount,
    c.description,
    c.incident_date,
    c.submitted_date,
    CURRENT_DATE - c.submitted_date::date AS days_pending
FROM claims c
WHERE c.status = 'PENDING'
ORDER BY c.submitted_date ASC;

-- Comments
COMMENT ON TABLE claims IS 'Insurance claims submitted by customers';
COMMENT ON TABLE claim_documents IS 'Supporting documents uploaded with claims';
COMMENT ON TABLE claim_status_history IS 'Audit trail of claim status changes';
COMMENT ON COLUMN claims.claim_number IS 'Unique identifier in format CLM-YYYY-XXXXXX';
COMMENT ON COLUMN claims.status IS 'Claim status: PENDING, UNDER_REVIEW, APPROVED, REJECTED, PAID';
COMMENT ON COLUMN claims.reviewed_by IS 'Admin user ID who reviewed the claim';