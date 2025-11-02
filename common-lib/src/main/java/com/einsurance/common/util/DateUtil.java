package com.einsurance.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility helper for commonly used date operations.
 */
public final class DateUtil {

    private DateUtil() {
        // Utility class
    }

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static LocalDate addMonths(int months) {
        return LocalDate.now().plusMonths(months);
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        return date.plusMonths(months);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    public static LocalDate parseDate(String dateString) {
        return dateString != null ? LocalDate.parse(dateString, DATE_FORMATTER) : null;
    }

    public static boolean isExpired(LocalDate expiryDate) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public static long daysUntilExpiry(LocalDate expiryDate) {
        if (expiryDate == null) {
            return 0;
        }
        return LocalDate.now().until(expiryDate, ChronoUnit.DAYS);
    }
}
