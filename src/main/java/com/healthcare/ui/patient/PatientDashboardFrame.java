package com.healthcare.ui.patient;

import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.PatientService;
import com.healthcare.ui.auth.LoginFrame;
import com.healthcare.ui.common.UIUtils;
import com.healthcare.util.DuplicateBookingException;
import com.healthcare.util.UserSession;
import com.healthcare.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Main dashboard for authenticated patients:
 * Allows browsing doctors, booking appointments with collision prevention,
 * viewing appointment history, and profile management.
 */
public class PatientDashboardFrame extends JFrame {

    private final Patient currentPatient;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    private JTabbedPane tabbedPane;

    // Booking Tab Components
    private JComboBox<String> comboSpecialty;
    private JComboBox<Doctor> comboDoctors;
    private JTextField txtApptDate;
    private JComboBox<LocalTime> comboTimeSlots;
    private JTextField txtReason;
    private JLabel lblDoctorFee;
    private JLabel lblDoctorDays;
    private JLabel lblDoctorShift;

    // History Tab Components
    private JTable tblAppointments;
    private DefaultTableModel appointmentTableModel;

    // Profile Tab Components
    private JTextField txtProfileName;
    private JTextField txtProfilePhone;
    private JTextField txtProfileDob;
    private JComboBox<Gender> comboProfileGender;
    private JComboBox<String> comboProfileBlood;
    private JTextField txtProfileAddress;

    public PatientDashboardFrame() {
        this.currentPatient = UserSession.getCurrentPatient();
        if (this.currentPatient == null) {
            JOptionPane.showMessageDialog(null, "Session expired. Please log in again.");
            dispose();
            new LoginFrame().setVisible(true);
        }

        this.doctorService = new DoctorService();
        this.appointmentService = new AppointmentService();
        this.patientService = new PatientService();

        initUI();
        loadAppointments();
    }

    private void initUI() {
        setTitle("Healthcare System - Patient Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.COLOR_BG);

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIUtils.COLOR_PRIMARY);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblWelcome = new JLabel("Welcome, " + currentPatient.getFullName() + " (" + currentPatient.getEmail() + ")");
        lblWelcome.setFont(UIUtils.FONT_SUBTITLE);
        lblWelcome.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UIUtils.FONT_BOLD);
        btnLogout.setBackground(UIUtils.COLOR_PRIMARY_DARK);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        topBar.add(lblWelcome, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Book Appointment", createBookingPanel());
        tabbedPane.addTab("My Appointments", createAppointmentsPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.weightx = 0.3;

        // Specialty Filter
        gbc.gridy = 0;
        formCard.add(createLabel("1. Filter by Specialization:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboSpecialty = new JComboBox<>();
        comboSpecialty.setFont(UIUtils.FONT_REGULAR);
        comboSpecialty.addItem("All Specializations");
        for (String spec : doctorService.getAllSpecializations()) {
            comboSpecialty.addItem(spec);
        }
        comboSpecialty.addActionListener(e -> updateDoctorDropdown());
        formCard.add(comboSpecialty, gbc);

        // Doctor Selector
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formCard.add(createLabel("2. Choose Doctor:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboDoctors = new JComboBox<>();
        comboDoctors.setFont(UIUtils.FONT_REGULAR);
        comboDoctors.addActionListener(e -> onDoctorSelected());
        formCard.add(comboDoctors, gbc);

        // Appointment Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formCard.add(createLabel("3. Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtApptDate = UIUtils.createTextField(15);
        txtApptDate.setText(LocalDate.now().plusDays(1).toString());
        formCard.add(txtApptDate, gbc);

        // Check Slots Button & Slot dropdown
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JButton btnCheckSlots = UIUtils.createSecondaryButton("Check Available Slots");
        btnCheckSlots.addActionListener(e -> refreshAvailableSlots());
        formCard.add(btnCheckSlots, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboTimeSlots = new JComboBox<>();
        comboTimeSlots.setFont(UIUtils.FONT_REGULAR);
        formCard.add(comboTimeSlots, gbc);

        // Reason / Symptoms
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        formCard.add(createLabel("4. Reason for Visit:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtReason = UIUtils.createTextField(25);
        formCard.add(txtReason, gbc);

        // Confirm Button
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(16, 8, 8, 8);
        JButton btnBook = UIUtils.createPrimaryButton("Confirm Appointment Booking");
        btnBook.addActionListener(e -> handleBookAppointment());
        formCard.add(btnBook, gbc);

        panel.add(formCard, BorderLayout.CENTER);

        // Right side Doctor Info Card
        JPanel doctorInfoCard = UIUtils.createCardPanel();
        doctorInfoCard.setPreferredSize(new Dimension(280, 0));
        doctorInfoCard.setLayout(new GridLayout(6, 1, 4, 8));

        JLabel lblInfoTitle = new JLabel("Doctor Details");
        lblInfoTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblInfoTitle.setForeground(UIUtils.COLOR_PRIMARY);

        lblDoctorFee = new JLabel("Consultation Fee: -");
        lblDoctorFee.setFont(UIUtils.FONT_REGULAR);

        lblDoctorDays = new JLabel("Working Days: -");
        lblDoctorDays.setFont(UIUtils.FONT_REGULAR);

        lblDoctorShift = new JLabel("Clinic Hours: -");
        lblDoctorShift.setFont(UIUtils.FONT_REGULAR);

        doctorInfoCard.add(lblInfoTitle);
        doctorInfoCard.add(lblDoctorFee);
        doctorInfoCard.add(lblDoctorDays);
        doctorInfoCard.add(lblDoctorShift);

        panel.add(doctorInfoCard, BorderLayout.EAST);

        // Initialize doctors
        updateDoctorDropdown();

        return panel;
    }

    private void updateDoctorDropdown() {
        comboDoctors.removeAllItems();
        String selectedSpec = (String) comboSpecialty.getSelectedItem();
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(selectedSpec);
        for (Doctor doc : doctors) {
            comboDoctors.addItem(doc);
        }
        onDoctorSelected();
    }

    private void onDoctorSelected() {
        Doctor selected = (Doctor) comboDoctors.getSelectedItem();
        if (selected != null) {
            lblDoctorFee.setText("Consultation Fee: $" + selected.getConsultationFee());
            lblDoctorDays.setText("<html>Working Days:<br><b>" + selected.getAvailableDays() + "</b></html>");
            lblDoctorShift.setText("Shift: " + selected.getShiftStartTime() + " - " + selected.getShiftEndTime());
            refreshAvailableSlots();
        } else {
            lblDoctorFee.setText("Consultation Fee: -");
            lblDoctorDays.setText("Working Days: -");
            lblDoctorShift.setText("Clinic Hours: -");
            comboTimeSlots.removeAllItems();
        }
    }

    private void refreshAvailableSlots() {
        comboTimeSlots.removeAllItems();
        Doctor selectedDoc = (Doctor) comboDoctors.getSelectedItem();
        if (selectedDoc == null) return;

        String dateStr = txtApptDate.getText().trim();
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            List<LocalTime> slots = doctorService.getAvailableTimeSlots(selectedDoc, date);
            if (slots.isEmpty()) {
                UIUtils.showError(this, "No available timeslots for " + selectedDoc.getFullName() + " on " + date + ".\nPlease choose another date or doctor.");
            } else {
                for (LocalTime slot : slots) {
                    comboTimeSlots.addItem(slot);
                }
            }
        } catch (DateTimeParseException ex) {
            UIUtils.showError(this, "Please enter a valid date in YYYY-MM-DD format.");
        }
    }

    private void handleBookAppointment() {
        Doctor doctor = (Doctor) comboDoctors.getSelectedItem();
        if (doctor == null) {
            UIUtils.showError(this, "Please select a doctor.");
            return;
        }

        String dateStr = txtApptDate.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            UIUtils.showError(this, "Please enter a valid date in YYYY-MM-DD format.");
            return;
        }

        LocalTime time = (LocalTime) comboTimeSlots.getSelectedItem();
        if (time == null) {
            UIUtils.showError(this, "Please select an available timeslot.");
            return;
        }

        String reason = txtReason.getText().trim();

        try {
            Appointment appt = appointmentService.bookAppointment(currentPatient.getId(), doctor.getId(), date, time, reason);
            UIUtils.showSuccess(this, "Appointment successfully scheduled!\nBooking ID: #" + appt.getAppointmentId() +
                    "\nDoctor: " + doctor.getFullName() + "\nDate: " + date + " at " + time);
            txtReason.setText("");
            loadAppointments();
            tabbedPane.setSelectedIndex(1); // Switch to history tab
        } catch (ValidationException | DuplicateBookingException ex) {
            UIUtils.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Booking failed: " + ex.getMessage());
        }
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] columns = {"ID", "Doctor", "Specialization", "Date", "Time", "Fee ($)", "Status", "Reason", "Notes"};
        appointmentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblAppointments = new JTable(appointmentTableModel);
        UIUtils.styleTable(tblAppointments);
        JScrollPane scrollPane = new JScrollPane(tblAppointments);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setBackground(UIUtils.COLOR_BG);

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh List");
        btnRefresh.addActionListener(e -> loadAppointments());

        JButton btnCancel = UIUtils.createDangerButton("Cancel Selected Appointment");
        btnCancel.addActionListener(e -> handleCancelAppointment());

        actionPanel.add(btnRefresh);
        actionPanel.add(btnCancel);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadAppointments() {
        appointmentTableModel.setRowCount(0);
        List<Appointment> list = appointmentService.getAppointmentsForPatient(currentPatient.getId());
        for (Appointment a : list) {
            appointmentTableModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getDoctorName(),
                    a.getDoctorSpecialization(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getConsultationFee(),
                    a.getStatus().getLabel(),
                    a.getReason(),
                    a.getNotes() != null ? a.getNotes() : ""
            });
        }
    }

    private void handleCancelAppointment() {
        int selectedRow = tblAppointments.getSelectedRow();
        if (selectedRow == -1) {
            UIUtils.showError(this, "Please select an appointment from the table to cancel.");
            return;
        }

        int appointmentId = (int) appointmentTableModel.getValueAt(selectedRow, 0);
        String status = (String) appointmentTableModel.getValueAt(selectedRow, 6);

        if (!status.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Confirmed")) {
            UIUtils.showError(this, "Only Pending or Confirmed appointments can be cancelled.");
            return;
        }

        if (UIUtils.confirmAction(this, "Are you sure you want to cancel appointment #" + appointmentId + "?", "Confirm Cancellation")) {
            try {
                appointmentService.cancelAppointment(appointmentId, currentPatient.getId(), false);
                UIUtils.showSuccess(this, "Appointment #" + appointmentId + " has been cancelled.");
                loadAppointments();
            } catch (ValidationException ex) {
                UIUtils.showError(this, ex.getMessage());
            } catch (Exception ex) {
                UIUtils.showError(this, "Cancellation failed: " + ex.getMessage());
            }
        }
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new GridLayout(7, 2, 12, 12));

        formCard.add(createLabel("Full Name:"));
        txtProfileName = UIUtils.createTextField(20);
        txtProfileName.setText(currentPatient.getFullName());
        formCard.add(txtProfileName);

        formCard.add(createLabel("Email (Cannot be changed):"));
        JTextField txtProfileEmail = UIUtils.createTextField(20);
        txtProfileEmail.setText(currentPatient.getEmail());
        txtProfileEmail.setEditable(false);
        formCard.add(txtProfileEmail);

        formCard.add(createLabel("Phone:"));
        txtProfilePhone = UIUtils.createTextField(20);
        txtProfilePhone.setText(currentPatient.getPhone());
        formCard.add(txtProfilePhone);

        formCard.add(createLabel("Date of Birth:"));
        txtProfileDob = UIUtils.createTextField(20);
        txtProfileDob.setText(currentPatient.getDateOfBirth() != null ? currentPatient.getDateOfBirth().toString() : "");
        formCard.add(txtProfileDob);

        formCard.add(createLabel("Gender:"));
        comboProfileGender = new JComboBox<>(Gender.values());
        comboProfileGender.setSelectedItem(currentPatient.getGender());
        formCard.add(comboProfileGender);

        formCard.add(createLabel("Blood Group:"));
        comboProfileBlood = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});
        comboProfileBlood.setSelectedItem(currentPatient.getBloodGroup());
        formCard.add(comboProfileBlood);

        formCard.add(createLabel("Residential Address:"));
        txtProfileAddress = UIUtils.createTextField(20);
        txtProfileAddress.setText(currentPatient.getAddress());
        formCard.add(txtProfileAddress);

        panel.add(formCard, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIUtils.COLOR_BG);
        JButton btnSaveProfile = UIUtils.createPrimaryButton("Save Profile Updates");
        btnSaveProfile.addActionListener(e -> handleUpdateProfile());
        btnPanel.add(btnSaveProfile);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void handleUpdateProfile() {
        String name = txtProfileName.getText().trim();
        String phone = txtProfilePhone.getText().trim();
        String dobStr = txtProfileDob.getText().trim();
        Gender gender = (Gender) comboProfileGender.getSelectedItem();
        String blood = (String) comboProfileBlood.getSelectedItem();
        String address = txtProfileAddress.getText().trim();

        try {
            LocalDate dob = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);
            currentPatient.setFullName(name);
            currentPatient.setPhone(phone);
            currentPatient.setDateOfBirth(dob);
            currentPatient.setGender(gender);
            currentPatient.setBloodGroup(blood);
            currentPatient.setAddress(address);

            patientService.updateProfile(currentPatient);
            UIUtils.showSuccess(this, "Profile updated successfully!");
        } catch (DateTimeParseException ex) {
            UIUtils.showError(this, "Invalid date format. Use YYYY-MM-DD.");
        } catch (ValidationException ex) {
            UIUtils.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Update failed: " + ex.getMessage());
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_MAIN);
        return label;
    }

    private void handleLogout() {
        UserSession.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
