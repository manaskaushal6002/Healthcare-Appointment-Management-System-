package com.healthcare.service;

import com.healthcare.dao.IAppointmentDAO;
import com.healthcare.dao.IDoctorDAO;
import com.healthcare.dao.IPatientDAO;
import com.healthcare.dao.impl.AppointmentDAOImpl;
import com.healthcare.dao.impl.DoctorDAOImpl;
import com.healthcare.dao.impl.PatientDAOImpl;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.model.Doctor;
import com.healthcare.util.DuplicateBookingException;
import com.healthcare.util.ValidationException;
import com.healthcare.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing appointment scheduling workflows, validation rules,
 * duplicate collision prevention, and lifecycle status changes.
 */
public class AppointmentService {

    private final IAppointmentDAO appointmentDAO;
    private final IDoctorDAO doctorDAO;
    private final IPatientDAO patientDAO;
    private final DoctorService doctorService;

    public AppointmentService() {
        this(new AppointmentDAOImpl(), new DoctorDAOImpl(), new PatientDAOImpl(), new DoctorService());
    }

    public AppointmentService(IAppointmentDAO appointmentDAO, IDoctorDAO doctorDAO, IPatientDAO patientDAO, DoctorService doctorService) {
        this.appointmentDAO = appointmentDAO;
        this.doctorDAO = doctorDAO;
        this.patientDAO = patientDAO;
        this.doctorService = doctorService;
    }

    /**
     * Books an appointment after verifying doctor active status, working day/shift hours,
     * and preventing duplicate slot collisions.
     */
    public Appointment bookAppointment(int patientId, int doctorId, LocalDate date, LocalTime time, String reason)
            throws ValidationException, DuplicateBookingException {

        if (patientId <= 0) {
            throw new ValidationException("Invalid patient identification.");
        }
        if (doctorId <= 0) {
            throw new ValidationException("Please select a doctor.");
        }
        if (date == null || time == null) {
            throw new ValidationException("Appointment date and time are required.");
        }
        if (!ValidationUtils.isValidTimeSlot(date, time)) {
            throw new ValidationException("Appointments must be scheduled for a future date and time.");
        }
        if (!ValidationUtils.isNonEmpty(reason)) {
            throw new ValidationException("Please specify the reason for the visit / consultation.");
        }

        // Verify Patient exists
        if (patientDAO.findById(patientId).isEmpty()) {
            throw new ValidationException("Patient record not found.");
        }

        // Verify Doctor exists & is active
        Optional<Doctor> optDoctor = doctorDAO.findById(doctorId);
        if (optDoctor.isEmpty() || !optDoctor.get().isActive()) {
            throw new ValidationException("The selected doctor is currently unavailable or inactive.");
        }
        Doctor doctor = optDoctor.get();

        // Verify Doctor's working day
        if (!doctorService.isDoctorWorkingOnDay(doctor, date)) {
            throw new ValidationException(doctor.getFullName() + " does not hold clinics on " + date.getDayOfWeek() + "s. Available days: " + doctor.getAvailableDays());
        }

        // Verify Doctor's shift hours
        if (time.isBefore(doctor.getShiftStartTime()) || time.isAfter(doctor.getShiftEndTime())) {
            throw new ValidationException("Selected time " + time + " is outside doctor's shift (" + doctor.getShiftStartTime() + " - " + doctor.getShiftEndTime() + ").");
        }

        // CRITICAL: Slot collision prevention
        if (appointmentDAO.isSlotBooked(doctorId, date, time)) {
            throw new DuplicateBookingException("Time slot " + time + " on " + date + " is already booked for " + doctor.getFullName() + ". Please select another slot.");
        }

        Appointment appt = new Appointment(patientId, doctorId, date, time, reason.trim());
        appt.setStatus(AppointmentStatus.CONFIRMED);

        int id = appointmentDAO.create(appt);
        appt.setAppointmentId(id);

        return appt;
    }

    public List<Appointment> getAppointmentsForPatient(int patientId) {
        return appointmentDAO.findByPatientId(patientId);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.findAll();
    }

    public Optional<Appointment> getAppointmentById(int appointmentId) {
        return appointmentDAO.findById(appointmentId);
    }

    /**
     * Cancels an appointment enforcing role-based permissions and lifecycle constraints.
     */
    public boolean cancelAppointment(int appointmentId, int requestingUserId, boolean isAdmin)
            throws ValidationException {

        Optional<Appointment> opt = appointmentDAO.findById(appointmentId);
        if (opt.isEmpty()) {
            throw new ValidationException("Appointment #" + appointmentId + " not found.");
        }
        Appointment appt = opt.get();

        // Enforce ownership if called by patient
        if (!isAdmin && appt.getPatientId() != requestingUserId) {
            throw new ValidationException("Unauthorized: You cannot cancel another patient's appointment.");
        }

        if (!appt.getStatus().canBeCancelled()) {
            throw new ValidationException("Cannot cancel an appointment that is already " + appt.getStatus().getLabel() + ".");
        }

        return appointmentDAO.cancel(appointmentId);
    }

    /**
     * Updates status (e.g. COMPLETED or CANCELLED) with administrative notes.
     */
    public boolean updateAppointmentStatus(int appointmentId, AppointmentStatus status, String notes)
            throws ValidationException {

        Optional<Appointment> opt = appointmentDAO.findById(appointmentId);
        if (opt.isEmpty()) {
            throw new ValidationException("Appointment #" + appointmentId + " not found.");
        }

        return appointmentDAO.updateStatus(appointmentId, status, notes);
    }
}
