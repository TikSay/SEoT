package Login.Enrollment;

import java.math.BigDecimal;
import java.sql.*;

public class DatabaseEnrollmentRegistry implements EnrollmentRegistry {

    /** MySQL-feilkode for "Duplicate entry" (brudd på UNIQUE-nøkkel). */
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private final String url;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseEnrollmentRegistry(String url, String dbUser, String dbPassword) {
        this.url = url;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public BigDecimal findCoursePrice(int courseId) {
        String sql = "SELECT pris FROM courses WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getBigDecimal("pris") : null;

        } catch (SQLException e) {
            throw new RuntimeException("Database error during course lookup: " + e.getMessage(), e);
        }
    }

    @Override
    public int findCapacity(int courseId) {
        return queryInt("SELECT kapasitet FROM courses WHERE id = ?", courseId);
    }

    @Override
    public int countEnrollments(int courseId) {
        return queryInt("SELECT COUNT(*) FROM enrollments WHERE course_id = ?", courseId);
    }

    @Override
    public boolean isMember(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            throw new RuntimeException("Database error during member lookup: " + e.getMessage(), e);
        }
    }

    @Override
    public void addEnrollment(EnrollmentRequest request, BigDecimal pris) {
        String sql = "INSERT INTO enrollments (course_id, fornavn, etternavn, alder, email, er_medlem, pris) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, request.courseId());
            ps.setString(2, request.fornavn());
            ps.setString(3, request.etternavn());
            ps.setInt(4, request.alder());
            ps.setString(5, request.email());
            ps.setBoolean(6, request.erMedlem());
            ps.setBigDecimal(7, pris);

            ps.executeUpdate();

        } catch (SQLException e) {
            // S-014: bare en ekte duplikat gir 409. Andre integritetsbrudd (f.eks. kurset er slettet) er databasefeil (500).
            if (isDuplicateEntry(e)) {
                throw new EnrollmentConflictException("This email is already enrolled in this course.");
            }
            throw new RuntimeException("Database error during enrollment: " + e.getMessage(), e);
        }
    }

    /** True bare når MySQL sier "Duplicate entry", ikke ved andre integritetsbrudd som fremmednøkkel eller NOT NULL. */
    static boolean isDuplicateEntry(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException && e.getErrorCode() == MYSQL_DUPLICATE_ENTRY;
    }

    /** Kjører en spørring som gir ett tall tilbake, f.eks. COUNT(*). */
    private int queryInt(String sql, int courseId) {
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
