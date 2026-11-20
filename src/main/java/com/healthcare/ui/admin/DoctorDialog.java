package com.healthcare.ui.admin;

import com.healthcare.model.Doctor;
import com.healthcare.model.Specialization;
import com.healthcare.service.DoctorService;
import com.healthcare.ui.common.UIUtils;
import com.healthcare.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for creating or updating a Doctor's record in the hospital directory.
 */
public class DoctorDialog extends JDialog {

    private final DoctorService doctorService;
    private final Doctor doctorToEdit;
    private boolean saved = false;

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JComboBox<String> comboSpecialty;
    private JTextField txtQualification;
    private JTextField txtExperience;
    private JTextField txtFee;
    private JTextField txtDays;
    private JTextField txtShiftStart;
    private JTextField txtShiftEnd;
    private JCheckBox chkActive;

    public DoctorDialog(Frame parent, DoctorService doctorService, Doctor doctorToEdit) {
        super(parent, doctorToEdit == null ? "Add New Doctor" : "Edit Doctor Profile", true);
        this.doctorService = doctorService;
        this.doctorToEdit = doctorToEdit;
        initUI();
    }

    private void initUI() {
        setSize(500, 620);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new GridLayout(11, 2, 8, 8));

        formCard.add(createLabel("Full Name:"));
        txtFullName = UIUtils.createTextField(20);
        formCard.add(txtFullName);

        formCard.add(createLabel("Professional Email:"));
        txtEmail = UIUtils.createTextField(20);
        formCard.add(txtEmail);

        formCard.add(createLabel("Phone Number:"));
        txtPhone = UIUtils.createTextField(20);
        formCard.add(txtPhone);

        formCard.add(createLabel("Specialization:"));
        comboSpecialty = new JComboBox<>();
        comboSpecialty.setFont(UIUtils.FONT_REGULAR);
        for (Specialization s : Specialization.values()) {
            comboSpecialty.addItem(s.getDisplayName());
        }
        formCard.add(comboSpecialty);

        formCard.add(createLabel("Qualifications:"));
        txtQualification = UIUtils.createTextField(20);
        formCard.add(txtQualification);

        formCard.add(createLabel("Experience (Years):"));
        txtExperience = UIUtils.createTextField(20);
        formCard.add(txtExperience);

        formCard.add(createLabel("Consultation Fee ($):"));
        txtFee = UIUtils.createTextField(20);
        formCard.add(txtFee);

        formCard.add(createLabel("Working Days (comma separated):"));
        txtDays = UIUtils.createTextField(20);
        txtDays.setText("Monday,Tuesday,Wednesday,Thursday,Friday");
        formCard.add(txtDays);

        formCard.add(createLabel("Shift Start (HH:MM):"));
        txtShiftStart = UIUtils.createTextField(20);
        txtShiftStart.setText("09:00");
        formCard.add(txtShiftStart);

        formCard.add(createLabel("Shift End (HH:MM):"));
        txtShiftEnd = UIUtils.createTextField(20);
        txtShiftEnd.setText("17:00");
        formCard.add(txtShiftEnd);

        formCard.add(createLabel("Status:"));
        chkActive = new JCheckBox("Active Doctor", true);
        chkActive.setFont(UIUtils.FONT_REGULAR);
        chkActive.setBackground(Color.WHITE);
        formCard.add(chkActive);

        mainPanel.add(formCard, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UIUtils.COLOR_BG);

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIUtils.createPrimaryButton("Save Doctor Record");
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // Pre-fill if editing
        if (doctorToEdit != null) {
            txtFullName.setText(doctorToEdit.getFullName());
            txtEmail.setText(doctorToEdit.getEmail());
            txtPhone.setText(doctorToEdit.getPhone());
            comboSpecialty.setSelectedItem(doctorToEdit.getSpecialization());
            txtQualification.setText(doctorToEdit.getQualification());
            txtExperience.setText(String.valueOf(doctorToEdit.getExperienceYears()));
            txtFee.setText(doctorToEdit.getConsultationFee().toPlainString());
            txtDays.setText(doctorToEdit.getAvailableDays());
            txtShiftStart.setText(doctorToEdit.getShiftStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            txtShiftEnd.setText(doctorToEdit.getShiftEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            chkActive.setSelected(doctorToEdit.isActive());
        }

        add(mainPanel);
    }

    private void handleSave() {
        try {
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String spec = (String) comboSpecialty.getSelectedItem();
            String qual = txtQualification.getText().trim();
            int exp = Integer.parseInt(txtExperience.getText().trim());
            BigDecimal fee = new BigDecimal(txtFee.getText().trim());
            String days = txtDays.getText().trim();
            LocalTime start = LocalTime.parse(txtShiftStart.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(txtShiftEnd.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            boolean active = chkActive.isSelected();

            Doctor doctor = (doctorToEdit != null) ? doctorToEdit : new Doctor();
            doctor.setFullName(fullName);
            doctor.setEmail(email);
            doctor.setPhone(phone);
            doctor.setSpecialization(spec);
            doctor.setQualification(qual);
            doctor.setExperienceYears(exp);
            doctor.setConsultationFee(fee);
            doctor.setAvailableDays(days);
            doctor.setShiftStartTime(start);
            doctor.setShiftEndTime(end);
            doctor.setActive(active);

            doctorService.saveDoctor(doctor);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Please enter valid numeric values for Experience and Fee.");
        } catch (DateTimeParseException ex) {
            UIUtils.showError(this, "Please enter shift times in valid HH:MM 24-hour format (e.g., 09:00).");
        } catch (ValidationException ex) {
            UIUtils.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Error saving doctor: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_MAIN);
        return label;
    }
}
