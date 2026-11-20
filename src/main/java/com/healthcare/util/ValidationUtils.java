package com.healthcare.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Validates domain constraints and user inputs.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private ValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isStrongPassword(String password) {
        // Minimum 6 characters
        return password != null && password.length() >= 6;
    }

    public static boolean isValidBirthDate(LocalDate date) {
        if (date == null) return false;
        LocalDate now = LocalDate.now();
        return date.isBefore(now) && date.isAfter(now.minusYears(130));
    }

    public static boolean isValidFutureDate(LocalDate date) {
        if (date == null) return false;
        return !date.isBefore(LocalDate.now());
    }

    public static boolean isValidTimeSlot(LocalDate date, LocalTime time) {
        if (date == null || time == null) return false;
        if (date.isAfter(LocalDate.now())) {
            return true;
        }
        if (date.isEqual(LocalDate.now())) {
            return time.isAfter(LocalTime.now());
        }
        return false;
    }

    public static boolean isPositiveFee(BigDecimal fee) {
        return fee != null && fee.compareTo(BigDecimal.ZERO) >= 0;
    }
}
