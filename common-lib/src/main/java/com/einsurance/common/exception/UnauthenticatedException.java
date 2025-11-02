package com.einsurance.common.exception;

/**
 * Thrown when a client attempts to access a resource without authentication.
 */
public class UnauthenticatedException extends BusinessException {

    public UnauthenticatedException(String message) {
        super(message, "UNAUTHENTICATED");
    }

    public UnauthenticatedException() {
        super("Authentication required", "UNAUTHENTICATED");
    }
}
