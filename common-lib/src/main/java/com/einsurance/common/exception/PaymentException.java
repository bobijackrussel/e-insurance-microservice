package com.einsurance.common.exception;

/**
 * Thrown when a payment-related operation fails.
 */
public class PaymentException extends BusinessException {

    public PaymentException(String message) {
        super(message, "PAYMENT_ERROR");
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
