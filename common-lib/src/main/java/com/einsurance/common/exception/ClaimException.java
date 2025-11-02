package com.einsurance.common.exception;

/**
 * Thrown when claim business rules are violated.
 */
public class ClaimException extends BusinessException {

    public ClaimException(String message) {
        super(message, "CLAIM_ERROR");
    }
}
