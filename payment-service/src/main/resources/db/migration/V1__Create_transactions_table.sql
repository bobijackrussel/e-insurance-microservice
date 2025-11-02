-- Payment Service - Initial Migration
-- File location: payment-service/src/main/resources/db/migration/V1__Create_transactions_table.sql

-- Transactions table for payment records
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    customer_policy_id UUID,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    payment_method VARCHAR(50),
    
    -- Stripe specific fields
    stripe_payment_intent_id VARCHAR(255),
    stripe_session_id VARCHAR(255),
    stripe_charge_id VARCHAR(255),
    
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    
    -- Metadata stored as JSONB for flexibility
    metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_currency_code CHECK (currency ~* '^[A-Z]{3}$')
);

-- Create indexes for performance
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_customer_policy_id ON transactions(customer_policy_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_stripe_payment_intent_id ON transactions(stripe_payment_intent_id);
CREATE INDEX idx_transactions_stripe_session_id ON transactions(stripe_session_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_metadata ON transactions USING GIN (metadata);

-- Trigger for updating updated_at
CREATE TRIGGER update_transactions_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to set completed_at when status changes to COMPLETED
CREATE OR REPLACE FUNCTION set_completed_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        NEW.completed_at := CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_completed_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION set_completed_at();

-- View for successful transactions summary
CREATE VIEW successful_transactions_summary AS
SELECT 
    DATE_TRUNC('day', created_at) AS transaction_date,
    currency,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS average_amount
FROM transactions
WHERE status = 'COMPLETED'
GROUP BY DATE_TRUNC('day', created_at), currency
ORDER BY transaction_date DESC;

-- View for user payment history
CREATE VIEW user_payment_history AS
SELECT 
    user_id,
    COUNT(*) AS total_transactions,
    COUNT(*) FILTER (WHERE status = 'COMPLETED') AS successful_transactions,
    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed_transactions,
    SUM(amount) FILTER (WHERE status = 'COMPLETED') AS total_spent,
    MAX(created_at) FILTER (WHERE status = 'COMPLETED') AS last_payment_date
FROM transactions
GROUP BY user_id;

-- Comments
COMMENT ON TABLE transactions IS 'Payment transaction records from Stripe';
COMMENT ON COLUMN transactions.stripe_payment_intent_id IS 'Stripe PaymentIntent ID for tracking';
COMMENT ON COLUMN transactions.stripe_session_id IS 'Stripe Checkout Session ID';
COMMENT ON COLUMN transactions.metadata IS 'Additional payment metadata in JSON format';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED';