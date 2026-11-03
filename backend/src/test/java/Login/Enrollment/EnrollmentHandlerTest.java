package Login.Enrollment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** S-014: feilhåndtering i EnrollmentHandler. Øvrige regler (pris, rabatt, alder) testes systematisk i S-015. */
class EnrollmentHandlerTest {

    private FakeEnrollmentRegistry registry;
    private EnrollmentHandler handler;

    @BeforeEach
    void setUp() {
        registry = new FakeEnrollmentRegistry();
        registry.addCourse(1, "500.00", 10);
        registry.addCourse(2, "300.00", 1);
        registry.addMember("kari@example.com");
        handler = new EnrollmentHandler(registry);
    }

    // --- Kurs-ID og kurs som ikke finnes ---

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void invalidCourseIdIsRejectedWithoutQueryingDatabase(int courseId) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.enroll(request(courseId, "Ola", "Nordmann", "ola@example.com", false)));

        assertEquals("Invalid course ID.", e.getMessage());
        assertEquals(0, registry.callCount);
    }

    @Test
    void unknownCourseThrowsCourseNotFound() {
        CourseNotFoundException e = assertThrows(CourseNotFoundException.class,
                () -> handler.enroll(request(999, "Ola", "Nordmann", "ola@example.com", false)));

        assertEquals("Course not found.", e.getMessage());
    }

    // --- Konflikter: fullt kurs og duplikat ---

    @Test
    void fullCourseThrowsConflict() {
        handler.enroll(request(2, "Ola", "Nordmann", "ola@example.com", false));

        EnrollmentConflictException e = assertThrows(EnrollmentConflictException.class,
                () -> handler.enroll(request(2, "Per", "Hansen", "per@example.com", false)));

        assertEquals("Sorry, this course is full.", e.getMessage());
        assertEquals(1, registry.enrollments.size());
    }

    // --- Navn og e-post etter databasens grenser ---

    @Test
    void namesOf50CharactersAreAccepted() {
        assertDoesNotThrow(() -> handler.enroll(request(1, "a".repeat(50), "b".repeat(50), "ola@example.com", false)));
    }

    @Test
    void firstNameLongerThan50CharactersIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.enroll(request(1, "a".repeat(51), "Nordmann", "ola@example.com", false)));

        assertEquals("First and last name cannot be longer than 50 characters.", e.getMessage());
        assertEquals(0, registry.callCount);
    }

    @Test
    void lastNameLongerThan50CharactersIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.enroll(request(1, "Ola", "b".repeat(51), "ola@example.com", false)));

        assertEquals("First and last name cannot be longer than 50 characters.", e.getMessage());
    }

    @Test
    void emailOf100CharactersIsAccepted() {
        String email = "b".repeat(88) + "@example.com";
        assertDoesNotThrow(() -> handler.enroll(request(1, "Ola", "Nordmann", email, false)));
    }

    @Test
    void emailLongerThan100CharactersIsRejected() {
        String email = "b".repeat(89) + "@example.com";

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.enroll(request(1, "Ola", "Nordmann", email, false)));

        assertEquals("Email cannot be longer than 100 characters.", e.getMessage());
        assertEquals(0, registry.callCount);
    }

    // --- Normalisering ---

    @Test
    void emailIsNormalizedBeforeMemberCheckAndStorage() {
        handler.enroll(request(1, "  Kari ", " Nordmann  ", "  Kari@Example.COM ", true));

        EnrollmentRequest stored = registry.enrollments.get(0);
        assertEquals("kari@example.com", stored.email());
        assertEquals("Kari", stored.fornavn());
        assertEquals("Nordmann", stored.etternavn());
    }

    @Test
    void sameEmailWithDifferentCaseIsDuplicate() {
        handler.enroll(request(1, "Ola", "Nordmann", "ola@example.com", false));

        assertThrows(EnrollmentConflictException.class,
                () -> handler.enroll(request(1, "Ola", "Nordmann", " OLA@example.com", false)));
        assertEquals(1, registry.enrollments.size());
    }

    private static EnrollmentRequest request(int courseId, String fornavn, String etternavn, String email, boolean erMedlem) {
        return new EnrollmentRequest(courseId, fornavn, etternavn, 30, email, erMedlem);
    }
}
