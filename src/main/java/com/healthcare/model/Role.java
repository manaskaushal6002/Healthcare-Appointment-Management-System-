package com.healthcare.model;

/**
 * Represents system roles for access control and authorization.
 */
public enum Role {
    PATIENT("Patient"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String roleStr) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(roleStr) || role.displayName.equalsIgnoreCase(roleStr)) {
                return role;
            }
        }
        return PATIENT;
    }
}
