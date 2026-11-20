package com.healthcare.model;

/**
 * Lifecycle states of a medical appointment.
 */
public enum AppointmentStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Business rule: only PENDING and CONFIRMED appointments can be cancelled.
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Business rule: only CONFIRMED appointments can be completed.
     */
    public boolean canBeCompleted() {
        return this == CONFIRMED;
    }

    public static AppointmentStatus fromString(String text) {
        for (AppointmentStatus s : values()) {
            if (s.name().equalsIgnoreCase(text) || s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}
