package com.einsurance.common.util;

import com.einsurance.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
@Slf4j
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{10,15}$");

    /**
     * Validate email format
     */
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "Invalid email format");
        }
    }

    /**
     * Validate phone number format
     */
    public static void validatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                throw new ValidationException("phone", "Invalid phone number format");
            }
        }
    }

    /**
     * Validate UUID
     */
    public static UUID validateAndParseUUID(String uuidString, String fieldName) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(fieldName, "Invalid " + fieldName + " format");
        }
    }

    /**
     * Validate amount is positive
     */
    public static void validatePositiveAmount(Double amount, String fieldName) {
        if (amount == null) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
        if (amount <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be positive");
        }
    }

    /**
     * Validate date is not in future
     */
    public static void validateDateNotFuture(LocalDate date, String fieldName) {
        if (date == null) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(fieldName, fieldName + " cannot be in the future");
        }
    }

    /**
     * Validate date range
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("date", "Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("date", "End date must be after start date");
        }
    }

    /**
     * Validate string not blank
     */
    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
    }

    /**
     * Validate string length
     */
    public static void validateLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(fieldName, 
                fieldName + " must not exceed " + maxLength + " characters");
        }
    }

    /**
     * Validate enum value
     */
    public static <E extends Enum<E>> void validateEnum(String value, Class<E> enumClass, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
        try {
            Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(fieldName, "Invalid " + fieldName + " value: " + value);
        }
    }
}