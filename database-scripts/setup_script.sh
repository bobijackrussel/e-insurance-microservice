#!/bin/bash

# E-Insurance Project Structure Setup Script
# This script creates the complete directory structure for all microservices

echo "Creating E-Insurance Microservices Project Structure..."

# Create main project directory
mkdir -p e-insurance-parent
cd e-insurance-parent

# Create documentation directories
mkdir -p docs/{api,postman,diagrams}

# Common Library Module
echo "Setting up common-lib..."
mkdir -p common-lib/src/main/java/com/einsurance/common/{dto,exception,config,util,security}
mkdir -p common-lib/src/main/resources
mkdir -p common-lib/src/test/java/com/einsurance/common

# User Service
echo "Setting up user-service..."
mkdir -p user-service/src/main/java/com/einsurance/user/{controller,service,repository,entity,dto,mapper,config,security}
mkdir -p user-service/src/main/resources/{db/migration,templates}
mkdir -p user-service/src/test/java/com/einsurance/user/{controller,service,repository}

# Policy Service
echo "Setting up policy-service..."
mkdir -p policy-service/src/main/java/com/einsurance/policy/{controller,service,repository,entity,dto,mapper,config}
mkdir -p policy-service/src/main/resources/{db/migration,templates}
mkdir -p policy-service/src/test/java/com/einsurance/policy/{controller,service,repository}

# Payment Service
echo "Setting up payment-service..."
mkdir -p payment-service/src/main/java/com/einsurance/payment/{controller,service,repository,entity,dto,mapper,config,stripe}
mkdir -p payment-service/src/main/resources/{db/migration,templates}
mkdir -p payment-service/src/test/java/com/einsurance/payment/{controller,service,repository}

# Claims Service
echo "Setting up claims-service..."
mkdir -p claims-service/src/main/java/com/einsurance/claims/{controller,service,repository,entity,dto,mapper,config}
mkdir -p claims-service/src/main/resources/{db/migration,templates}
mkdir -p claims-service/src/test/java/com/einsurance/claims/{controller,service,repository}

# Notification Service
echo "Setting up notification-service..."
mkdir -p notification-service/src/main/java/com/einsurance/notification/{controller,service,config,pdf,email,template}
mkdir -p notification-service/src/main/resources/{db/migration,templates/email,static/images}
mkdir -p notification-service/src/test/java/com/einsurance/notification/{service,pdf}

# Create placeholder README files
echo "# Common Library" > common-lib/README.md
echo "# User Service" > user-service/README.md
echo "# Policy Service" > policy-service/README.md
echo "# Payment Service" > payment-service/README.md
echo "# Claims Service" > claims-service/README.md
echo "# Notification Service" > notification-service/README.md

# Create application.yml templates for each service
cat > user-service/src/main/resources/application.yml <<EOL
server:
  port: 8081

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/user_service_db
    username: \${DB_USERNAME:postgres}
    password: \${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true

keycloak:
  auth-server-url: \${KEYCLOAK_URL:http://localhost:9098}
  realm: \${KEYCLOAK_REALM:e-insurance}
  resource: \${KEYCLOAK_CLIENT_ID:e-insurance-backend}

logging:
  level:
    com.einsurance: DEBUG
    org.springframework.security: DEBUG
EOL

cat > policy-service/src/main/resources/application.yml <<EOL
server:
  port: 8082

spring:
  application:
    name: policy-service
  datasource:
    url: jdbc:postgresql://localhost:5432/policy_service_db
    username: \${DB_USERNAME:postgres}
    password: \${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true

keycloak:
  auth-server-url: \${KEYCLOAK_URL:http://localhost:9098}
  realm: \${KEYCLOAK_REALM:e-insurance}
  resource: \${KEYCLOAK_CLIENT_ID:e-insurance-backend}

logging:
  level:
    com.einsurance: DEBUG
EOL

cat > payment-service/src/main/resources/application.yml <<EOL
server:
  port: 8084

spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_service_db
    username: \${DB_USERNAME:postgres}
    password: \${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true

keycloak:
  auth-server-url: \${KEYCLOAK_URL:http://localhost:9098}
  realm: \${KEYCLOAK_REALM:e-insurance}
  resource: \${KEYCLOAK_CLIENT_ID:e-insurance-backend}

stripe:
  secret-key: \${STRIPE_SECRET_KEY}
  publishable-key: \${STRIPE_PUBLISHABLE_KEY}
  webhook-secret: \${STRIPE_WEBHOOK_SECRET}

services:
  policy-service:
    url: http://localhost:8082

logging:
  level:
    com.einsurance: DEBUG
EOL

cat > claims-service/src/main/resources/application.yml <<EOL
server:
  port: 8083

spring:
  application:
    name: claims-service
  datasource:
    url: jdbc:postgresql://localhost:5432/claims_service_db
    username: \${DB_USERNAME:postgres}
    password: \${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true

keycloak:
  auth-server-url: \${KEYCLOAK_URL:http://localhost:9098}
  realm: \${KEYCLOAK_REALM:e-insurance}
  resource: \${KEYCLOAK_CLIENT_ID:e-insurance-backend}

services:
  policy-service:
    url: http://localhost:8082
  notification-service:
    url: http://localhost:8085

logging:
  level:
    com.einsurance: DEBUG
EOL

cat > notification-service/src/main/resources/application.yml <<EOL
server:
  port: 8085

spring:
  application:
    name: notification-service
  mail:
    host: \${SMTP_HOST:smtp.gmail.com}
    port: \${SMTP_PORT:587}
    username: \${SMTP_USERNAME}
    password: \${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

services:
  user-service:
    url: http://localhost:8081
  policy-service:
    url: http://localhost:8082

pdf:
  output-path: ./generated-pdfs
  company-name: E-Insurance Co.
  company-logo: /static/images/logo.png

logging:
  level:
    com.einsurance: DEBUG
EOL

echo ""
echo "✅ Project structure created successfully!"
echo ""
echo "Next steps:"
echo "1. Copy parent pom.xml to e-insurance-parent/"
echo "2. Create child pom.xml for each module"
echo "3. Run: mvn clean install"
echo ""
echo "Directory structure:"
tree -L 3 -I 'target'

echo ""
echo "Done! 🚀"