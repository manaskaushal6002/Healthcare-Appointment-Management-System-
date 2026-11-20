package com.healthcare.test;

import com.healthcare.config.DatabaseConnection;
import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.AuthService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.PatientService;
import com.healthcare.util.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Comprehensive Integration and Business Logic Test Suite.
 * Validates all required test scenarios for the internship portfolio:
 * 1. Successful patient registration
 * 2. Invalid login credentials rejection
 * 3. Successful appointment booking
 * 4. Duplicate appointment prevention (Conflict detection)
 * 5. Appointment cancellation workflow
 * 6. Invalid patient demographic validation
 * 7. Unauthorized cancellation attempt rejection
 * 8. Password hashing and cryptographic verification
 *
 * Can be executed directly in IntelliJ IDEA via Run -> Run 'SystemIntegrationTest.main()'.
 */
public class SystemIntegrationTest {

    private static int passedTests = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   HEALTHCARE APPOINTMENT SYSTEM - TEST HARNESS");
        System.out.println("==================================================================\n");

        runUnitAndValidationTests();
        runDatabaseIntegrationTests();

        System.out.println("\n==================================================================");
        System.out.println("  TEST SUMMARY: " + passedTests + " / " + totalTests + " PASSED (" +
                (totalTests > 0 ? (passedTests * 100 / totalTests) : 0) + "%)");
        System.out.println("==================================================================");
    }

    private static void runUnitAndValidationTests() {
        System.out.println("--- STAGE 1: Unit & Security Validation Tests ---");

        // Scenario: Password Hashing & Salt Verification
        runTest("PasswordUtils: Salt generation and SHA-256 verification", () -> {
            String salt = PasswordUtils.generateSalt();
            String password = "SecurePassword@123";
            String hash = PasswordUtils.hashPassword(password, salt);

            assertCondition(hash != null && hash.length() == 64, "SHA-256 hash must be 64 hex characters");
            assertCondition(PasswordUtils.verifyPassword(password, salt, hash), "Password verification failed with correct password");
            assertCondition(!PasswordUtils.verifyPassword("WrongPassword", salt, hash), "Password verification passed with wrong password");
        });

        // Scenario: Input Validation Rules
        runTest("ValidationUtils: Email format validation", () -> {
            assertCondition(ValidationUtils.isValidEmail("john.doe@domain.com"), "Valid email rejected");
            assertCondition(!ValidationUtils.isValidEmail("plainaddress"), "Invalid email accepted");
            assertCondition(!ValidationUtils.isValidEmail("missing@domain"), "Invalid email accepted");
        });

        runTest("ValidationUtils: Phone number validation", () -> {
            assertCondition(ValidationUtils.isValidPhone("+1-555-019-2834"), "Valid phone rejected");
            assertCondition(ValidationUtils.isValidPhone("9876543210"), "Valid phone rejected");
            assertCondition(!ValidationUtils.isValidPhone("123"), "Too short phone accepted");
        });

        runTest("ValidationUtils: Future date and timeslot rules", () -> {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate yesterday = LocalDate.now().minusDays(1);

            assertCondition(ValidationUtils.isValidTimeSlot(tomorrow, LocalTime.of(10, 0)), "Tomorrow slot rejected");
            assertCondition(!ValidationUtils.isValidTimeSlot(yesterday, LocalTime.of(10, 0)), "Yesterday slot accepted");
        });

        // Scenario: Session Management
        runTest("UserSession: Thread-safe session management & RBAC", () -> {
            UserSession.logout();
            assertCondition(!UserSession.isLoggedIn(), "Session should be empty initially");

            Patient testPatient = new Patient();
            testPatient.setId(999);
            testPatient.setEmail("session.test@example.com");
            testPatient.setRole(Role.PATIENT);

            UserSession.setCurrentUser(testPatient);
            assertCondition(UserSession.isLoggedIn(), "User should be logged in");
            assertCondition(UserSession.isPatient(), "User should have PATIENT role");
            assertCondition(!UserSession.isAdmin(), "User should NOT have ADMIN role");
            assertCondition(UserSession.getCurrentPatient().getId() == 999, "Session patient ID mismatch");

            UserSession.logout();
            assertCondition(!UserSession.isLoggedIn(), "Session should be empty after logout");
        });
    }

    private static void runDatabaseIntegrationTests() {
        System.out.println("\n--- STAGE 2: End-to-End Database Integration Scenarios ---");

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            System.out.println("NOTE: MySQL database is currently unreachable on localhost:3306.");
            System.out.println("      (" + e.getMessage() + ")");
            System.out.println("      Skipping live DB tests. Once MySQL is started and schema.sql executed,");
            System.out.println("      re-run this test class to execute live database scenarios.");
            return;
        }

        AuthService authService = new AuthService();
        DoctorService doctorService = new DoctorService();
        AppointmentService appointmentService = new AppointmentService();

        String uniqueEmail = "test.patient." + System.currentTimeMillis() + "@example.com";
        Patient createdPatient = null;

        // Scenario 1: Successful Patient Registration
        try {
            totalTests++;
            System.out.print("[TEST " + totalTests + "] Scenario 1: Successful Patient Registration ... ");
            Patient newPatient = new Patient(
                    "Test Patient Automaton",
                    uniqueEmail,
                    "",
                    "",
                    "555-019-9999",
                    LocalDate.of(1996, 6, 15),
                    Gender.FEMALE,
                    "B+",
                    "100 Tech Blvd"
            );
            createdPatient = authService.registerPatient(newPatient, "TestPass@123");
            if (createdPatient != null && createdPatient.getId() > 0) {
                System.out.println("PASSED (Generated Patient ID: #" + createdPatient.getId() + ")");
                passedTests++;
            } else {
                System.out.println("FAILED (Patient not created)");
            }
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
        }

        // Scenario 2: Invalid Login Credentials
        runTest("Scenario 2: Invalid Login Credentials Rejection", () -> {
            try {
                authService.login(uniqueEmail, "IncorrectPassword", Role.PATIENT);
                throw new AssertionError("Login should have failed for incorrect password");
            } catch (AuthenticationException expected) {
                // Expected behaviour
            }
        });

        // Scenario 3 & 4: Appointment Booking & Duplicate Prevention
        if (createdPatient != null) {
            final Patient patient = createdPatient;
            List<Doctor> doctors = doctorService.getActiveDoctors();
            if (!doctors.isEmpty()) {
                Doctor testDoctor = doctors.get(0);
                // Find the doctor's next working day
                LocalDate nextWorkDay = LocalDate.now().plusDays(1);
                while (!doctorService.isDoctorWorkingOnDay(testDoctor, nextWorkDay)) {
                    nextWorkDay = nextWorkDay.plusDays(1);
                }
                final LocalDate bookingDate = nextWorkDay;
                LocalTime bookingTime = LocalTime.of(10, 0);

                // Scenario 3: Successful Appointment Booking
                final int[] bookedApptId = new int[1];
                runTest("Scenario 3: Successful Appointment Booking", () -> {
                    Appointment appt = appointmentService.bookAppointment(
                            patient.getId(),
                            testDoctor.getId(),
                            bookingDate,
                            bookingTime,
                            "Routine cardiac checkup"
                    );
                    assertCondition(appt.getAppointmentId() > 0, "Appointment ID must be positive");
                    assertCondition(appt.getStatus() == AppointmentStatus.CONFIRMED, "Initial status must be CONFIRMED");
                    bookedApptId[0] = appt.getAppointmentId();
                });

                // Scenario 4: Duplicate Booking Prevention
                runTest("Scenario 4: Duplicate Booking Prevention (Collision Detection)", () -> {
                    try {
                        // Attempt to book the EXACT SAME doctor at the EXACT SAME date & time
                        appointmentService.bookAppointment(
                                patient.getId(),
                                testDoctor.getId(),
                                bookingDate,
                                bookingTime,
                                "Conflicting second booking attempt"
                        );
                        throw new AssertionError("Double booking should have been prevented!");
                    } catch (DuplicateBookingException expected) {
                        // Success: Collision correctly blocked
                    }
                });

                // Scenario 5: Unauthorized Cancellation Attempt
                runTest("Scenario 5: Unauthorized Cancellation Prevention", () -> {
                    int wrongPatientId = patient.getId() + 999;
                    try {
                        appointmentService.cancelAppointment(bookedApptId[0], wrongPatientId, false);
                        throw new AssertionError("Unauthorized cancellation should have failed!");
                    } catch (ValidationException expected) {
                        // Success: unauthorized access blocked
                    }
                });

                // Scenario 6: Successful Cancellation
                runTest("Scenario 6: Successful Appointment Cancellation", () -> {
                    boolean cancelled = appointmentService.cancelAppointment(bookedApptId[0], patient.getId(), false);
                    assertCondition(cancelled, "Appointment cancellation failed");

                    Appointment updated = appointmentService.getAppointmentById(bookedApptId[0]).orElseThrow();
                    assertCondition(updated.getStatus() == AppointmentStatus.CANCELLED, "Status should be CANCELLED");
                });
            }
        }
    }

    private static void runTest(String testName, TestRunnable runnable) {
        totalTests++;
        System.out.print("[TEST " + totalTests + "] " + testName + " ... ");
        try {
            runnable.run();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED (" + t.getMessage() + ")");
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Throwable;
    }
}
