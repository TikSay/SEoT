package Login.Enrollment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Påmeldingsregister i minnet for testene. Oppfører seg som DatabaseEnrollmentRegistry, uten MySQL. */
class FakeEnrollmentRegistry implements EnrollmentRegistry {

    private final Map<Integer, BigDecimal> prices = new HashMap<>();
    private final Map<Integer, Integer> capacities = new HashMap<>();
    private final Set<String> members = new HashSet<>();
    final List<EnrollmentRequest> enrollments = new ArrayList<>();
    boolean dbDown = false;
    int callCount = 0;

    void addCourse(int id, String price, int capacity) {
        prices.put(id, new BigDecimal(price));
        capacities.put(id, capacity);
    }

    void addMember(String email) {
        members.add(email);
    }

    @Override
    public BigDecimal findCoursePrice(int courseId) {
        touch();
        return prices.get(courseId);
    }

    @Override
    public int findCapacity(int courseId) {
        touch();
        return capacities.getOrDefault(courseId, 0);
    }

    @Override
    public int countEnrollments(int courseId) {
        touch();
        return (int) enrollments.stream().filter(e -> e.courseId() == courseId).count();
    }

    @Override
    public boolean isMember(String email) {
        touch();
        return members.contains(email);
    }

    /** Samme regel som UNIQUE (email, course_id) i databasen. */
    @Override
    public void addEnrollment(EnrollmentRequest request, BigDecimal pris) {
        touch();
        boolean duplicate = enrollments.stream()
                .anyMatch(e -> e.courseId() == request.courseId() && e.email().equalsIgnoreCase(request.email()));
        if (duplicate) {
            throw new EnrollmentConflictException("This email is already enrolled in this course.");
        }
        enrollments.add(request);
    }

    private void touch() {
        callCount++;
        if (dbDown) {
            throw new RuntimeException("Database error during course lookup: Communications link failure");
        }
    }
}
