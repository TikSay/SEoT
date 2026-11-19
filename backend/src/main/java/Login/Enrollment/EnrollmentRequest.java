package Login.Enrollment;

/** Det påmeldingsskjemaet sender inn som JSON. */
public record EnrollmentRequest(
        int courseId,
        String fornavn,
        String etternavn,
        int alder,
        String email,
        boolean erMedlem
) {
}
