package com.healthcare.model;

/**
 * Biological sex / gender representation for patients.
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Gender fromString(String text) {
        for (Gender g : values()) {
            if (g.name().equalsIgnoreCase(text) || g.label.equalsIgnoreCase(text)) {
                return g;
            }
        }
        return OTHER;
    }
}
