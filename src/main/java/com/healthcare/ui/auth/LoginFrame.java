package com.healthcare.ui.auth;

import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.AuthService;
import com.healthcare.ui.admin.AdminDashboardFrame;
import com.healthcare.ui.common.UIUtils;
import com.healthcare.ui.patient.PatientDashboardFrame;
import com.healthcare.util.AuthenticationException;
import com.healthcare.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern login screen supporting role-based authentication (Patient & Administrator).
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JComboBox<String> roleCombo;
    private JTextField txtIdentifier;
    private JPasswordField txtPassword;
    private JLabel lblIdentifier;
    private JButton btnLogin;
    private JButton btnRegister;

    public LoginFrame() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Healthcare System - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UIUtils.COLOR_BG);
        JLabel titleLabel = new JLabel("Healthcare Portal", JLabel.CENTER);
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Appointment Management System", JLabel.CENTER);
        subtitleLabel.setFont(UIUtils.FONT_REGULAR);
        subtitleLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Role Selector
        gbc.gridy = 0;
        JLabel lblRole = new JLabel("Login Role:");
        lblRole.setFont(UIUtils.FONT_BOLD);
        cardPanel.add(lblRole, gbc);

        gbc.gridy = 1;
        roleCombo = new JComboBox<>(new String[]{"Patient", "Administrator"});
        roleCombo.setFont(UIUtils.FONT_REGULAR);
        roleCombo.setPreferredSize(new Dimension(roleCombo.getPreferredSize().width, 32));
        roleCombo.addActionListener(e -> updateRoleLabels());
        cardPanel.add(roleCombo, gbc);

        // Identifier (Email or Username)
        gbc.gridy = 2;
        lblIdentifier = new JLabel("Email Address:");
        lblIdentifier.setFont(UIUtils.FONT_BOLD);
        cardPanel.add(lblIdentifier, gbc);

        gbc.gridy = 3;
        txtIdentifier = UIUtils.createTextField(20);
        cardPanel.add(txtIdentifier, gbc);

        // Password
        gbc.gridy = 4;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(UIUtils.FONT_BOLD);
        cardPanel.add(lblPassword, gbc);

        gbc.gridy = 5;
        txtPassword = UIUtils.createPasswordField(20);
        cardPanel.add(txtPassword, gbc);

        // Actions
        gbc.gridy = 6;
        gbc.insets = new Insets(16, 8, 8, 8);
        btnLogin = UIUtils.createPrimaryButton("Sign In");
        btnLogin.addActionListener(e -> handleLogin());
        cardPanel.add(btnLogin, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(4, 8, 8, 8);
        btnRegister = UIUtils.createSecondaryButton("Register New Patient Account");
        btnRegister.addActionListener(e -> openRegisterScreen());
        cardPanel.add(btnRegister, gbc);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Allow pressing Enter to submit
        getRootPane().setDefaultButton(btnLogin);

        add(mainPanel);
    }

    private void updateRoleLabels() {
        boolean isPatient = roleCombo.getSelectedIndex() == 0;
        lblIdentifier.setText(isPatient ? "Email Address:" : "Username / Email:");
        btnRegister.setVisible(isPatient);
    }

    private void handleLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password = new String(txtPassword.getPassword());
        boolean isPatient = roleCombo.getSelectedIndex() == 0;
        Role role = isPatient ? Role.PATIENT : Role.ADMIN;

        try {
            User user = authService.login(identifier, password, role);
            dispose();

            if (role == Role.PATIENT) {
                new PatientDashboardFrame().setVisible(true);
            } else {
                new AdminDashboardFrame().setVisible(true);
            }
        } catch (ValidationException | AuthenticationException ex) {
            UIUtils.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "An unexpected system error occurred: " + ex.getMessage());
        }
    }

    private void openRegisterScreen() {
        dispose();
        new RegisterFrame().setVisible(true);
    }
}
