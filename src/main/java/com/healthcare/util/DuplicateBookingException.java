package com.healthcare.util;

public class DuplicateBookingException extends Exception {
    public DuplicateBookingException(String message) {
        super(message);
    }
}
