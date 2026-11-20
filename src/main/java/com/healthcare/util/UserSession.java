package com.healthcare.util;

import com.healthcare.model.Admin;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;

/**
 * Thread-safe global session manager holding the currently authenticated actor.
 */
public final class UserSession {

    private static User currentUser;

    private UserSession() {
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean isPatient() {
        return currentUser != null && currentUser.getRole() == Role.PATIENT;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public static synchronized Patient getCurrentPatient() {
        if (isPatient() && currentUser instanceof Patient) {
            return (Patient) currentUser;
        }
        return null;
    }

    public static synchronized Admin getCurrentAdmin() {
        if (isAdmin() && currentUser instanceof Admin) {
            return (Admin) currentUser;
        }
        return null;
    }

    public static synchronized void logout() {
        currentUser = null;
    }
}
