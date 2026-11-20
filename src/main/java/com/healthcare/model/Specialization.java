package com.healthcare.model;

/**
 * Standard medical specializations supported in the doctor directory.
 */
public enum Specialization {
    CARDIOLOGY("Cardiology"),
    DERMATOLOGY("Dermatology"),
    NEUROLOGY("Neurology"),
    PEDIATRICS("Pediatrics"),
    ORTHOPEDICS("Orthopedics"),
    GENERAL_MEDICINE("General Medicine"),
    GYNECOLOGY("Gynecology"),
    OPHTHALMOLOGY("Ophthalmology"),
    PSYCHIATRY("Psychiatry"),
    ENT("ENT (Ear, Nose, Throat)");

    private final String displayName;

    Specialization(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Specialization fromString(String text) {
        for (Specialization s : values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return GENERAL_MEDICINE;
    }
}
