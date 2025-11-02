package com.einsurance.common.exception;

/**
 * Thrown when policy business rules are violated.
 */
public class PolicyException extends BusinessException {

    public PolicyException(String message) {
        super(message, "POLICY_ERROR");
    }
}
