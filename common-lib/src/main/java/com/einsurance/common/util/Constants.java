package com.einsurance.common.util;

/**
 * Shared application-wide constants.
 */
public final class Constants {

    private Constants() {
    }

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // Policy Types
    public static final String POLICY_TYPE_LIFE = "LIFE";
    public static final String POLICY_TYPE_TRAVEL = "TRAVEL";
    public static final String POLICY_TYPE_PROPERTY = "PROPERTY";
    public static final String POLICY_TYPE_HEALTH = "HEALTH";
    public static final String POLICY_TYPE_AUTO = "AUTO";

    // Policy Status
    public static final String POLICY_STATUS_ACTIVE = "ACTIVE";
    public static final String POLICY_STATUS_EXPIRED = "EXPIRED";
    public static final String POLICY_STATUS_CANCELLED = "CANCELLED";
    public static final String POLICY_STATUS_SUSPENDED = "SUSPENDED";

    // Transaction Status
    public static final String TRANSACTION_STATUS_PENDING = "PENDING";
    public static final String TRANSACTION_STATUS_PROCESSING = "PROCESSING";
    public static final String TRANSACTION_STATUS_COMPLETED = "COMPLETED";
    public static final String TRANSACTION_STATUS_FAILED = "FAILED";
    public static final String TRANSACTION_STATUS_REFUNDED = "REFUNDED";
    public static final String TRANSACTION_STATUS_CANCELLED = "CANCELLED";

    // Claim Status
    public static final String CLAIM_STATUS_PENDING = "PENDING";
    public static final String CLAIM_STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String CLAIM_STATUS_APPROVED = "APPROVED";
    public static final String CLAIM_STATUS_REJECTED = "REJECTED";
    public static final String CLAIM_STATUS_PAID = "PAID";

    // Currency
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_USD = "USD";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // Error Codes
    public static final String ERROR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_PAYMENT = "PAYMENT_ERROR";
    public static final String ERROR_POLICY = "POLICY_ERROR";
    public static final String ERROR_CLAIM = "CLAIM_ERROR";
}
