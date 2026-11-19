package Login.Enrollment;

/** S-014: påmeldingen kolliderer med det som allerede er lagret (fullt kurs eller duplikat). EnrollmentController svarer med HTTP 409. */
public class EnrollmentConflictException extends RuntimeException {

    public EnrollmentConflictException(String message) {
        super(message);
    }
}
