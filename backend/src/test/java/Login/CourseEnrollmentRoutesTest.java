package Login;

import Login.ConsoleResetCodeSender.ConsoleResetCodeSender;
import Login.Course.Course;
import Login.Course.CourseController;
import Login.Enrollment.EnrollmentController;
import Login.Enrollment.EnrollmentHandler;
import Login.Enrollment.EnrollmentRegistry;
import Login.Enrollment.EnrollmentRequest;
import Login.Password.ForgotPassword.ForgotPasswordController;
import Login.Password.ForgotPassword.ForgotPasswordHandler;
import Login.Password.Login.LoginController;
import Login.Password.Login.LoginHandler;
import Login.Password.Register.RegisterController;
import Login.Password.Register.RegisterHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrasjon av S-012 (GET /api/courses) og S-013/S-014 (POST /api/enrollments) med WebServer-oppsettet
 * fra S-005–S-007 (JSON-mapper, sentral feilhåndtering). Bruker falske registre, ingen database.
 */
class CourseEnrollmentRoutesTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String VALID_ENROLLMENT = "{\"courseId\": 1, \"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\","
            + " \"alder\": 30, \"email\": \"ola@example.com\", \"erMedlem\": false}";

    private boolean dbDown;
    private final List<EnrollmentRequest> enrollments = new ArrayList<>();
    private Javalin app;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        dbDown = false;
        FakeDataRegistry users = new FakeDataRegistry();

        CourseController courseController = new CourseController(() -> {
            if (dbDown) {
                throw new RuntimeException("Database error while fetching courses: Communications link failure");
            }
            return List.of(new Course(1, "Sytreff i Råde", "Vi syr sammen", "råde husflidslag", "Råde",
                    "2026-12-01", "18:00", 10, new BigDecimal("500.00"), "Nybegynner"));
        });
        EnrollmentController enrollmentController = new EnrollmentController(new EnrollmentHandler(new EnrollmentRegistry() {
            @Override
            public BigDecimal findCoursePrice(int courseId) {
                return courseId == 1 ? new BigDecimal("500.00") : null;
            }

            @Override
            public int findCapacity(int courseId) {
                return 10;
            }

            @Override
            public int countEnrollments(int courseId) {
                return enrollments.size();
            }

            @Override
            public boolean isMember(String email) {
                return false;
            }

            @Override
            public void addEnrollment(EnrollmentRequest request, BigDecimal pris) {
                enrollments.add(request);
            }
        }));

        app = Javalin.create(config -> {
            WebServer.configure(config,
                    new LoginController(new LoginHandler(users), new SessionUser(users)),
                    new RegisterController(new RegisterHandler(users)),
                    new ForgotPasswordController(new ForgotPasswordHandler(users, new ConsoleResetCodeSender(), Clock.systemUTC())),
                    null);
            WebServer.registerCourseRoutes(config, courseController, enrollmentController);
            config.jetty.port = 0;
        }).start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    // --- S-012: kursvisning ---

    @Test
    void getCoursesReturnsCourseList() throws Exception {
        HttpResponse<String> response = get("/api/courses");

        assertEquals(200, response.statusCode(), response.body());
        JsonNode course = json(response).get("courses").get(0);
        assertEquals("Sytreff i Råde", course.get("tittel").asText());
        assertEquals("2026-12-01", course.get("dato").asText());
        assertEquals("18:00", course.get("starttid").asText());
        assertEquals(500.00, course.get("pris").asDouble());
    }

    @Test
    void getCoursesWithDatabaseErrorGives500() throws Exception {
        dbDown = true;

        HttpResponse<String> response = get("/api/courses");

        assertEquals(500, response.statusCode());
        assertFalse(json(response).get("success").asBoolean());
        assertFalse(response.body().contains("Communications link failure"), response.body());
    }

    // --- S-013/S-014: påmelding ---

    @Test
    void enrollmentRouteIsRegisteredAndGives201() throws Exception {
        HttpResponse<String> response = post("/api/enrollments", VALID_ENROLLMENT);

        assertEquals(201, response.statusCode(), response.body());
        assertEquals(1, enrollments.size());
    }

    /** Regresjon: den globale 404-handleren (S-007) skal ikke overskrive controllerens melding. */
    @Test
    void enrollmentInUnknownCourseGives404() throws Exception {
        HttpResponse<String> response = post("/api/enrollments", VALID_ENROLLMENT.replace("\"courseId\": 1", "\"courseId\": 99"));

        assertEquals(404, response.statusCode(), response.body());
        assertFalse(json(response).get("success").asBoolean());
        assertEquals("Course not found.", json(response).get("message").asText());
    }

    /** S-006: "a@" ble godtatt av den gamle sjekken (contains("@")), men avvises nå av InputValidator. */
    @Test
    void enrollmentUsesS006EmailValidation() throws Exception {
        HttpResponse<String> response = post("/api/enrollments", VALID_ENROLLMENT.replace("ola@example.com", "ola@"));

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("Please enter a valid email address.", json(response).get("message").asText());
        assertTrue(enrollments.isEmpty());
    }

    /** S-006-mapperen i WebServer: tall i et tekstfelt gir 400 også for påmelding. */
    @Test
    void enrollmentWithNumberInTextFieldGives400() throws Exception {
        HttpResponse<String> response = post("/api/enrollments", VALID_ENROLLMENT.replace("\"Ola\"", "123"));

        assertEquals(400, response.statusCode(), response.body());
        assertTrue(enrollments.isEmpty());
    }

    // --- Hjelpemetoder ---

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + app.port() + path);
    }

    private static JsonNode json(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body());
    }
}
