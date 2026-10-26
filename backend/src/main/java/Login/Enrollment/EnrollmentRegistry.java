package Login.Enrollment;

import java.math.BigDecimal;

public interface EnrollmentRegistry {

    /** Ordinær pris for kurset, eller null hvis kurset ikke finnes. */
    BigDecimal findCoursePrice(int courseId);

    int findCapacity(int courseId);

    int countEnrollments(int courseId);

    boolean isMember(String email);

    /** Kaster IllegalArgumentException hvis e-posten allerede er påmeldt kurset. */
    void addEnrollment(EnrollmentRequest request, BigDecimal pris);
}
