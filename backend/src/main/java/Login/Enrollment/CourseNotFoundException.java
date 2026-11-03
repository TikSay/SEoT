package Login.Enrollment;

/** S-014: kurset finnes ikke. EnrollmentController svarer med HTTP 404. */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String message) {
        super(message);
    }
}
