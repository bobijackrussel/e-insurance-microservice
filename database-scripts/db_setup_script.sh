#!/bin/bash

# E-Insurance Database Setup Script
# This script initializes all PostgreSQL databases and runs Flyway migrations

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Database configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}E-Insurance Database Setup${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check if PostgreSQL is running
echo -e "${YELLOW}Checking PostgreSQL connection...${NC}"
if ! PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c '\l' > /dev/null 2>&1; then
    echo -e "${RED}Error: Cannot connect to PostgreSQL!${NC}"
    echo "Please ensure PostgreSQL is running on $DB_HOST:$DB_PORT"
    exit 1
fi
echo -e "${GREEN}✓ PostgreSQL connection successful${NC}"
echo ""

# Create databases
echo -e "${YELLOW}Creating databases...${NC}"
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -f init-databases.sql
echo -e "${GREEN}✓ Databases created${NC}"
echo ""

# Copy migration files to respective service resources
echo -e "${YELLOW}Setting up Flyway migrations...${NC}"

# User Service
mkdir -p user-service/src/main/resources/db/migration
echo -e "${GREEN}✓ User Service migration directory ready${NC}"

# Policy Service
mkdir -p policy-service/src/main/resources/db/migration
echo -e "${GREEN}✓ Policy Service migration directory ready${NC}"

# Payment Service
mkdir -p payment-service/src/main/resources/db/migration
echo -e "${GREEN}✓ Payment Service migration directory ready${NC}"

# Claims Service
mkdir -p claims-service/src/main/resources/db/migration
echo -e "${GREEN}✓ Claims Service migration directory ready${NC}"

echo ""
echo -e "${YELLOW}Testing database connections...${NC}"

# Test each database
databases=("user_service_db" "policy_service_db" "payment_service_db" "claims_service_db")

for db in "${databases[@]}"; do
    if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $db -c "SELECT 1;" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ $db - Connected${NC}"
    else
        echo -e "${RED}✗ $db - Connection failed${NC}"
    fi
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Database setup completed!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Next steps:"
echo "1. Place migration SQL files in respective service directories:"
echo "   - user-service/src/main/resources/db/migration/V1__Create_users_table.sql"
echo "   - policy-service/src/main/resources/db/migration/V1__Create_policy_tables.sql"
echo "   - payment-service/src/main/resources/db/migration/V1__Create_transactions_table.sql"
echo "   - claims-service/src/main/resources/db/migration/V1__Create_claims_tables.sql"
echo ""
echo "2. Run services with Maven:"
echo "   mvn spring-boot:run -pl user-service"
echo ""
echo "3. Flyway will automatically apply migrations on service startup"
echo ""