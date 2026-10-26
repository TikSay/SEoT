package Login.Enrollment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Reglene for påmelding. Prisen regnes ut her på serveren.
 * Kaster IllegalArgumentException med en melding når noe er feil i skjemaet.
 */
public class EnrollmentHandler {

    private static final BigDecimal MEDLEMSRABATT = new BigDecimal("0.80"); // medlemmer betaler 80 %

    private final EnrollmentRegistry registry;

    public EnrollmentHandler(EnrollmentRegistry registry) {
        this.registry = registry;
    }

    /** Melder personen på kurset og returnerer prisen de skal betale. */
    public BigDecimal enroll(EnrollmentRequest request) {
        if (isBlank(request.fornavn()) || isBlank(request.etternavn())) {
            throw new IllegalArgumentException("First and last name are required.");
        }
        if (request.alder() < 1 || request.alder() > 120) {
            throw new IllegalArgumentException("Age must be between 1 and 120.");
        }
        if (isBlank(request.email()) || !request.email().contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        BigDecimal ordinaerPris = registry.findCoursePrice(request.courseId());
        if (ordinaerPris == null) {
            throw new IllegalArgumentException("Course not found.");
        }
        if (registry.countEnrollments(request.courseId()) >= registry.findCapacity(request.courseId())) {
            throw new IllegalArgumentException("Sorry, this course is full.");
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