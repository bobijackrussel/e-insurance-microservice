package com.einsurance.common.exception;

/**
 * Thrown when the client is authenticated but lacks permissions.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("You are not authorized to perform this action", "UNAUTHORIZED");
    }
}
