package com.healthcare;

import com.healthcare.ui.auth.LoginFrame;
import com.healthcare.ui.common.UIUtils;

import javax.swing.*;

/**
 * Main application entry point for the Healthcare Appointment Management System.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  Healthcare Appointment Management System (Pure Java & Swing)");
        System.out.println("==========================================================");

        // Apply clean modern Look-and-Feel
        UIUtils.setupLookAndFeel();

        // Launch UI on Swing's Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to launch UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
