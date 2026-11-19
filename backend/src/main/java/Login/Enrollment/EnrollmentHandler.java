package Login.Enrollment;

import Login.InputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Reglene for påmelding. Prisen regnes ut her på serveren.
 * Kaster IllegalArgumentException med en melding når noe er feil i skjemaet (400),
 * CourseNotFoundException når kurset ikke finnes (404) og EnrollmentConflictException ved fullt kurs eller duplikat (409).
 */
public class EnrollmentHandler {

    private static final BigDecimal MEDLEMSRABATT = new BigDecimal("0.80"); // medlemmer betaler 80 %
    private static final int MAX_NAME_LENGTH = 50;    // samme som enrollments.fornavn / etternavn VARCHAR(50)

    private final EnrollmentRegistry registry;

    public EnrollmentHandler(EnrollmentRegistry registry) {
        this.registry = registry;
    }

    /** Melder personen på kurset og returnerer prisen de skal betale. */
    public BigDecimal enroll(EnrollmentRequest input) {
        // S-014: courseId er 0 hvis feltet mangler i JSON
        if (input.courseId() <= 0) {
            throw new IllegalArgumentException("Invalid course ID.");
        }
        if (isBlank(input.fornavn()) || isBlank(input.etternavn())) {
            throw new IllegalArgumentException("First and last name are required.");
        }
        if (input.fornavn().trim().length() > MAX_NAME_LENGTH || input.etternavn().trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("First and last name cannot be longer than 50 characters.");
        }
        if (input.alder() < 1 || input.alder() > 120) {
            throw new IllegalArgumentException("Age must be between 1 and 120.");
        }
        if (isBlank(input.email())) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        InputValidator.requireValidEmail(input.email());   // S-006: samme regler for e-post (format, maks 100 tegn) som ved registrering

        // S-014: samme e-post skal gi samme person, uansett store/små bokstaver og mellomrom
        EnrollmentRequest request = new EnrollmentRequest(input.courseId(), input.fornavn().trim(),
                input.etternavn().trim(), input.alder(), input.email().trim().toLowerCase(Locale.ROOT), input.erMedlem());

        BigDecimal ordinaerPris = registry.findCoursePrice(request.courseId());
        if (ordinaerPris == null) {
            throw new CourseNotFoundException("Course not found.");
        }
        if (registry.countEnrollments(request.courseId()) >= registry.findCapacity(request.courseId())) {
            throw new EnrollmentConflictException("Sorry, this course is full.");
        }
        if (request.erMedlem() && !registry.isMember(request.email())) {
            throw new IllegalArgumentException("No member found with this email.");
        }

        BigDecimal pris = request.erMedlem() ? ordinaerPris.multiply(MEDLEMSRABATT) : ordinaerPris;
        pris = pris.setScale(2, RoundingMode.HALF_UP);

        registry.addEnrollment(request, pris);
        return pris;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}