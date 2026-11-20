package com.healthcare.ui.admin;

import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.PatientService;
import com.healthcare.ui.auth.LoginFrame;
import com.healthcare.ui.common.UIUtils;
import com.healthcare.util.UserSession;
import com.healthcare.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Administrative console allowing hospital management of doctors,
 * global appointment oversight, and patient registry inspection.
 */
public class AdminDashboardFrame extends JFrame {

    private final Admin currentAdmin;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    // KPI Labels
    private JLabel lblKpiDoctors;
    private JLabel lblKpiPatients;
    private JLabel lblKpiAppointments;

    // Doctor Tab Components
    private JTable tblDoctors;
    private DefaultTableModel doctorTableModel;

    // Appointment Tab Components
    private JTable tblAppointments;
    private DefaultTableModel appointmentTableModel;
    private JComboBox<String> comboFilterStatus;

    // Patient Tab Components
    private JTable tblPatients;
    private DefaultTableModel patientTableModel;

    public AdminDashboardFrame() {
        this.currentAdmin = UserSession.getCurrentAdmin();
        if (this.currentAdmin == null) {
            JOptionPane.showMessageDialog(null, "Session expired. Please log in as an administrator.");
            dispose();
            new LoginFrame().setVisible(true);
        }

        this.doctorService = new DoctorService();
        this.appointmentService = new AppointmentService();
        this.patientService = new PatientService();

        initUI();
        refreshAllData();
    }

    private void initUI() {
        setTitle("Healthcare System - Administrator Control Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.COLOR_BG);

        // Header Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIUtils.COLOR_PRIMARY_DARK);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblAdmin = new JLabel("Administrator: " + currentAdmin.getFullName() + " (" + currentAdmin.getUsername() + ")");
        lblAdmin.setFont(UIUtils.FONT_SUBTITLE);
        lblAdmin.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UIUtils.FONT_BOLD);
        btnLogout.setBackground(new Color(211, 47, 47));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        topBar.add(lblAdmin, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Center Content
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBackground(UIUtils.COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // KPI Summary Bar
        contentPanel.add(createKpiPanel(), BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        tabbedPane.addTab("Manage Doctors", createDoctorManagementPanel());
        tabbedPane.addTab("All Appointments", createAppointmentManagementPanel());
        tabbedPane.addTab("Patient Registry", createPatientRegistryPanel());

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createKpiPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiPanel.setBackground(UIUtils.COLOR_BG);
        kpiPanel.setPreferredSize(new Dimension(0, 70));

        lblKpiDoctors = new JLabel("Total Doctors: -", JLabel.CENTER);
        lblKpiPatients = new JLabel("Registered Patients: -", JLabel.CENTER);
        lblKpiAppointments = new JLabel("Total Appointments: -", JLabel.CENTER);

        kpiPanel.add(styleKpiCard(lblKpiDoctors, UIUtils.COLOR_PRIMARY));
        kpiPanel.add(styleKpiCard(lblKpiPatients, UIUtils.COLOR_SECONDARY));
        kpiPanel.add(styleKpiCard(lblKpiAppointments, new Color(103, 58, 183)));

        return kpiPanel;
    }

    private JPanel styleKpiCard(JLabel label, Color accentColor) {
        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(8, 12, 8, 12)
        ));
        label.setFont(UIUtils.FONT_SUBTITLE);
        label.setForeground(UIUtils.COLOR_TEXT_MAIN);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDoctorManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "Name", "Specialization", "Qualifications", "Experience", "Fee ($)", "Days", "Shift", "Status"};
        doctorTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblDoctors = new JTable(doctorTableModel);
        UIUtils.styleTable(tblDoctors);
        panel.add(new JScrollPane(tblDoctors), BorderLayout.CENTER);

        // Doctor Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setBackground(UIUtils.COLOR_BG);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add Doctor");
        btnAdd.addActionListener(e -> onAddDoctor());

        JButton btnEdit = UIUtils.createSecondaryButton("Edit Doctor");
        btnEdit.addActionListener(e -> onEditDoctor());

        JButton btnDeactivate = UIUtils.createDangerButton("Deactivate Doctor");
        btnDeactivate.addActionListener(e -> onDeactivateDoctor());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshDoctors());

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDeactivate);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAppointmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(UIUtils.COLOR_BG);
        filterBar.add(new JLabel("Filter Status:"));
        comboFilterStatus = new JComboBox<>(new String[]{"All", "CONFIRMED", "PENDING", "COMPLETED", "CANCELLED"});
        comboFilterStatus.setFont(UIUtils.FONT_REGULAR);
        comboFilterStatus.addActionListener(e -> refreshAppointments());
        filterBar.add(comboFilterStatus);
        panel.add(filterBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Patient", "Phone", "Doctor", "Specialty", "Date", "Time", "Status", "Reason", "Notes"};
        appointmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblAppointments = new JTable(appointmentTableModel);
        UIUtils.styleTable(tblAppointments);
        panel.add(new JScrollPane(tblAppointments), BorderLayout.CENTER);

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setBackground(UIUtils.COLOR_BG);

        JButton btnUpdateStatus = UIUtils.createPrimaryButton("Update Appointment Status");
        btnUpdateStatus.addActionListener(e -> onUpdateAppointmentStatus());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshAppointments());

        toolbar.add(btnUpdateStatus);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPatientRegistryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "Full Name", "Email", "Phone", "Age", "Gender", "Blood Group", "Address"};
        patientTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblPatients = new JTable(patientTableModel);
        UIUtils.styleTable(tblPatients);
        panel.add(new JScrollPane(tblPatients), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setBackground(UIUtils.COLOR_BG);
        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshPatients());
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    private void onAddDoctor() {
        DoctorDialog dialog = new DoctorDialog(this, doctorService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            UIUtils.showSuccess(this, "New doctor successfully added to the registry.");
            refreshDoctors();
            refreshKpis();
        }
    }

    private void onEditDoctor() {
        int row = tblDoctors.getSelectedRow();
        if (row == -1) {
            UIUtils.showError(this, "Please select a doctor to edit.");
            return;
        }
        int docId = (int) doctorTableModel.getValueAt(row, 0);
        Optional<Doctor> opt = doctorService.getDoctorById(docId);
        if (opt.isPresent()) {
            DoctorDialog dialog = new DoctorDialog(this, doctorService, opt.get());
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                UIUtils.showSuccess(this, "Doctor profile updated.");
                refreshDoctors();
            }
        }
    }

    private void onDeactivateDoctor() {
        int row = tblDoctors.getSelectedRow();
        if (row == -1) {
            UIUtils.showError(this, "Please select a doctor to deactivate.");
            return;
        }
        int docId = (int) doctorTableModel.getValueAt(row, 0);
        String docName = (String) doctorTableModel.getValueAt(row, 1);

        if (UIUtils.confirmAction(this, "Are you sure you want to deactivate " + docName + "?\nThey will no longer be available for new bookings.", "Confirm Deactivation")) {
            doctorService.deactivateDoctor(docId);
            UIUtils.showSuccess(this, "Doctor deactivated.");
            refreshDoctors();
            refreshKpis();
        }
    }

    private void onUpdateAppointmentStatus() {
        int row = tblAppointments.getSelectedRow();
        if (row == -1) {
            UIUtils.showError(this, "Please select an appointment from the table.");
            return;
        }

        int apptId = (int) appointmentTableModel.getValueAt(row, 0);
        String currentStatus = (String) appointmentTableModel.getValueAt(row, 7);

        JComboBox<AppointmentStatus> comboStatus = new JComboBox<>(AppointmentStatus.values());
        comboStatus.setSelectedItem(AppointmentStatus.fromString(currentStatus));

        JTextField txtNotes = new JTextField(20);
        Object currentNotes = appointmentTableModel.getValueAt(row, 9);
        if (currentNotes != null) {
            txtNotes.setText(currentNotes.toString());
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("New Status:"));
        panel.add(comboStatus);
        panel.add(new JLabel("Admin Notes:"));
        panel.add(txtNotes);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Appointment #" + apptId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            AppointmentStatus newStatus = (AppointmentStatus) comboStatus.getSelectedItem();
            String notes = txtNotes.getText().trim();
            try {
                appointmentService.updateAppointmentStatus(apptId, newStatus, notes);
                UIUtils.showSuccess(this, "Appointment #" + apptId + " status updated to " + newStatus);
                refreshAppointments();
                refreshKpis();
            } catch (ValidationException ex) {
                UIUtils.showError(this, ex.getMessage());
            }
        }
    }

    private void refreshAllData() {
        refreshDoctors();
        refreshAppointments();
        refreshPatients();
        refreshKpis();
    }

    private void refreshDoctors() {
        doctorTableModel.setRowCount(0);
        List<Doctor> doctors = doctorService.getAllDoctors();
        for (Doctor d : doctors) {
            doctorTableModel.addRow(new Object[]{
                    d.getId(),
                    d.getFullName(),
                    d.getSpecialization(),
                    d.getQualification(),
                    d.getExperienceYears() + " yrs",
                    d.getConsultationFee(),
                    d.getAvailableDays(),
                    d.getShiftStartTime() + " - " + d.getShiftEndTime(),
                    d.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void refreshAppointments() {
        appointmentTableModel.setRowCount(0);
        String filter = (String) comboFilterStatus.getSelectedItem();
        List<Appointment> list = appointmentService.getAllAppointments();

        for (Appointment a : list) {
            if ("All".equalsIgnoreCase(filter) || a.getStatus().name().equalsIgnoreCase(filter)) {
                appointmentTableModel.addRow(new Object[]{
                        a.getAppointmentId(),
                        a.getPatientName(),
                        a.getPatientPhone(),
                        a.getDoctorName(),
                        a.getDoctorSpecialization(),
                        a.getAppointmentDate(),
                        a.getAppointmentTime(),
                        a.getStatus().name(),
                        a.getReason(),
                        a.getNotes() != null ? a.getNotes() : ""
                });
            }
        }
    }

    private void refreshPatients() {
        patientTableModel.setRowCount(0);
        List<Patient> patients = patientService.getAllPatients();
        for (Patient p : patients) {
            patientTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getFullName(),
                    p.getEmail(),
                    p.getPhone(),
                    p.getAge() + " yrs",
                    p.getGender().getLabel(),
                    p.getBloodGroup() != null ? p.getBloodGroup() : "-",
                    p.getAddress() != null ? p.getAddress() : "-"
            });
        }
    }

    private void refreshKpis() {
        int docCount = doctorService.getAllDoctors().size();
        int activeDocCount = doctorService.getActiveDoctors().size();
        int patientCount = patientService.getAllPatients().size();
        int apptCount = appointmentService.getAllAppointments().size();

        lblKpiDoctors.setText("Doctors: " + activeDocCount + " active (" + docCount + " total)");
        lblKpiPatients.setText("Registered Patients: " + patientCount);
        lblKpiAppointments.setText("Total Appointments: " + apptCount);
    }

    private void handleLogout() {
        UserSession.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
