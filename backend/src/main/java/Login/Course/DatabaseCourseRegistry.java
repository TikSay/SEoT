package Login.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCourseRegistry implements CourseRegistry {

    // Tabellen courses lages i database/migrations/003_kurs_og_pamelding.sql (S-010).
    private static final String SQL_UPCOMING =
            "SELECT id, tittel, beskrivelse, lokallag, sted, dato, starttid, kapasitet, pris, nivaa "
                    + "FROM courses "
                    + "WHERE dato >= CURDATE() "
                    + "ORDER BY dato, starttid";

    private final String url;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseCourseRegistry(String url, String dbUser, String dbPassword) {
        this.url = url;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public List<Course> findUpcomingCourses() {
        List<Course> courses = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(SQL_UPCOMING);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date dato = rs.getDate("dato");
                Time starttid = rs.getTime("starttid");

                courses.add(new Course(
                        rs.getInt("id"),
                        rs.getString("tittel"),
                        rs.getString("beskrivelse"),
                        rs.getString("lokallag"),
                        rs.getString("sted"),
                        dato == null ? null : dato.toLocalDate().toString(),
                        starttid == null ? null : starttid.toLocalTime().toString(),
                        rs.getInt("kapasitet"),
                        rs.getBigDecimal("pris"),
                        rs.getString("nivaa")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while fetching courses: " + e.getMessage(),
                    e
            );
        }

        return courses;
    }
}