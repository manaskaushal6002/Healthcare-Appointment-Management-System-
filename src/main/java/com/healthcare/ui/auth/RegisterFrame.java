package com.healthcare.ui.auth;

import com.healthcare.model.Gender;
import com.healthcare.model.Patient;
import com.healthcare.service.AuthService;
import com.healthcare.ui.common.UIUtils;
import com.healthcare.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Registration screen for new patients with client-side and business validation.
 */
public class RegisterFrame extends JFrame {

    private final AuthService authService;

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtPhone;
    private JTextField txtDob;
    private JComboBox<Gender> comboGender;
    private JComboBox<String> comboBloodGroup;
    private JTextField txtAddress;

    public RegisterFrame() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Healthcare System - Patient Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(20, 32, 20, 32));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UIUtils.COLOR_BG);
        JLabel titleLabel = new JLabel("Create Patient Account", JLabel.CENTER);
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Fill in your details to schedule and manage appointments", JLabel.CENTER);
        subtitleLabel.setFont(UIUtils.FONT_REGULAR);
        subtitleLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Form Card
        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new GridLayout(9, 2, 12, 10));

        formCard.add(createFieldLabel("Full Name:"));
        txtFullName = UIUtils.createTextField(20);
        formCard.add(txtFullName);

        formCard.add(createFieldLabel("Email Address:"));
        txtEmail = UIUtils.createTextField(20);
        formCard.add(txtEmail);

        formCard.add(createFieldLabel("Password (min 6 chars):"));
        txtPassword = UIUtils.createPasswordField(20);
        formCard.add(txtPassword);

        formCard.add(createFieldLabel("Phone Number:"));
        txtPhone = UIUtils.createTextField(20);
        formCard.add(txtPhone);

        formCard.add(createFieldLabel("Date of Birth (YYYY-MM-DD):"));
        txtDob = UIUtils.createTextField(20);
        txtDob.setText("1995-01-01");
        formCard.add(txtDob);

        formCard.add(createFieldLabel("Gender:"));
        comboGender = new JComboBox<>(Gender.values());
        comboGender.setFont(UIUtils.FONT_REGULAR);
        formCard.add(comboGender);

        formCard.add(createFieldLabel("Blood Group:"));
        comboBloodGroup = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});
        comboBloodGroup.setFont(UIUtils.FONT_REGULAR);
        formCard.add(comboBloodGroup);

        formCard.add(createFieldLabel("Residential Address:"));
        txtAddress = UIUtils.createTextField(20);
        formCard.add(txtAddress);

        mainPanel.add(formCard, BorderLayout.CENTER);

        // Footer Actions
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonPanel.setBackground(UIUtils.COLOR_BG);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnBack = UIUtils.createSecondaryButton("Back to Login");
        btnBack.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JButton btnSubmit = UIUtils.createPrimaryButton("Register Account");
        btnSubmit.addActionListener(e -> handleRegistration());

        buttonPanel.add(btnBack);
        buttonPanel.add(btnSubmit);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnSubmit);
        add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_MAIN);
        return label;
    }

    private void handleRegistration() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String phone = txtPhone.getText().trim();
        String dobStr = txtDob.getText().trim();
        Gender gender = (Gender) comboGender.getSelectedItem();
        String bloodGroup = (String) comboBloodGroup.getSelectedItem();
        String address = txtAddress.getText().trim();

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            UIUtils.showError(this, "Date of birth must be in valid YYYY-MM-DD format (e.g. 1995-05-20).");
            return;
        }

        Patient patient = new Patient(fullName, email, "", "", phone, dob, gender, bloodGroup, address);

        try {
            authService.registerPatient(patient, password);
            UIUtils.showSuccess(this, "Registration successful! You can now log in with your email.");
            dispose();
            new LoginFrame().setVisible(true);
        } catch (ValidationException ex) {
            UIUtils.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Registration failed: " + ex.getMessage());
        }
    }
}
