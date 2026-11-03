package Login.Enrollment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S-014: statuskoder og feilmeldinger for POST /api/enrollments.
 * Ruten registreres her i testen, siden koblingen i WebServer hører til S-013-integrasjonen.
 */
class EnrollmentHttpTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String VALID = "{\"courseId\": 1, \"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\","
            + " \"alder\": 30, \"email\": \"ola@example.com\", \"erMedlem\": false}";

    private FakeEnrollmentRegistry registry;
    private Javalin app;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        registry = new FakeEnrollmentRegistry();
        registry.addCourse(1, "500.00", 10);
        registry.addCourse(2, "300.00", 1);
        EnrollmentController controller = new EnrollmentController(new EnrollmentHandler(registry));

        app = Javalin.create(config -> {
            config.jetty.port = 0;
            config.routes.post("/api/enrollments", controller::enroll);
        }).start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void validEnrollmentGives201() throws Exception {
        HttpResponse<String> response = post(VALID);

        assertEquals(201, response.statusCode(), response.body());
        assertTrue(json(response).get("success").asBoolean());
    }

    // --- 400: ugyldig JSON, feil datatype og ugyldig kurs-ID ---

    @ParameterizedTest(name = "body [{0}] gir 400")
    @CsvSource(quoteCharacter = '\'', value = {"''", "'{\"courseId\": '", "'null'", "'[]'"})
    void malformedJsonGives400(String body) throws Exception {
        assertError(post(body), 400, "Invalid request.");
        assertTrue(registry.enrollments.isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', quoteCharacter = '\'', value = {
            "'{\"courseId\": \"abc\", \"fornavn\": \"Ola\", \"etternavn\": \"N\", \"alder\": 30, \"email\": \"ola@example.com\", \"erMedlem\": false}'",
            "'{\"courseId\": 1, \"fornavn\": \"Ola\", \"etternavn\": \"N\", \"alder\": \"tretti\", \"email\": \"ola@example.com\", \"erMedlem\": false}'",
            "'{\"courseId\": 1, \"fornavn\": \"Ola\", \"etternavn\": \"N\", \"alder\": 30, \"email\": \"ola@example.com\", \"erMedlem\": \"kanskje\"}'",
            "'{\"courseId\": 1, \"fornavn\": {}, \"etternavn\": \"N\", \"alder\": 30, \"email\": \"ola@example.com\", \"erMedlem\": false}'"
    })
    void wrongDataTypeGives400(String body) throws Exception {
        assertError(post(body), 400, "Invalid request.");
    }

    @Test
    void missingCourseIdGives400() throws Exception {
        String body = "{\"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\", \"alder\": 30, \"email\": \"ola@example.com\", \"erMedlem\": false}";

        assertError(post(body), 400, "Invalid course ID.");
    }

    @Test
    void tooLongNameGives400() throws Exception {
        assertError(post(VALID.replace("\"Ola\"", "\"" + "a".repeat(51) + "\"")), 400,
                "First and last name cannot be longer than 50 characters.");
    }

    // --- 404 og 409 ---

    @Test
    void unknownCourseGives404() throws Exception {
        assertError(post(VALID.replace("\"courseId\": 1", "\"courseId\": 999")), 404, "Course not found.");
    }

    @Test
    void fullCourseGives409() throws Exception {
        post(VALID.replace("\"courseId\": 1", "\"courseId\": 2"));

        HttpResponse<String> response = post(VALID.replace("\"courseId\": 1", "\"courseId\": 2")
                .replace("ola@example.com", "per@example.com"));

        assertError(response, 409, "Sorry, this course is full.");
    }

    @Test
    void duplicateEnrollmentGives409() throws Exception {
        post(VALID);

        assertError(post(VALID), 409, "This email is already enrolled in this course.");
        assertEquals(1, registry.enrollments.size());
    }

    // --- 500 ---

    @Test
    void databaseErrorGives500WithGenericMessage() throws Exception {
        registry.dbDown = true;

        HttpResponse<String> response = post(VALID);

        assertError(response, 500, "Enrollment is unavailable right now. Please try again later.");
        assertFalse(response.body().contains("Communications link failure"), response.body());
    }

    /** En intern feil mens forespørselen leses (her: ødelagt JSON-oppsett) er ikke brukerens feil, og skal gi 500, ikke 400. */
    @Test
    void internalErrorWhileReadingRequestGives500() throws Exception {
        EnrollmentController controller = new EnrollmentController(new EnrollmentHandler(registry));
        JavalinJackson realMapper = new JavalinJackson();
        JsonMapper brokenMapper = new JsonMapper() {
            @Override
            public String toJsonString(Object obj, Type type) {
                return realMapper.toJsonString(obj, type);
            }

            @Override
            public <T> T fromJsonString(String json, Type targetType) {
                throw new IllegalStateException("JSON-oppsettet er ødelagt");
            }
        };
        Javalin brokenApp = Javalin.create(config -> {
            config.jetty.port = 0;
            config.jsonMapper(brokenMapper);
            config.routes.post("/api/enrollments", controller::enroll);
        }).start();
        try {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(
                            URI.create("http://localhost:" + brokenApp.port() + "/api/enrollments"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(VALID)).build(), HttpResponse.BodyHandlers.ofString());

            assertError(response, 500, "Enrollment is unavailable right now. Please try again later.");
            assertFalse(response.body().contains("ødelagt"), response.body());
        } finally {
            brokenApp.stop();
        }
    }

    // --- Hjelpemetoder ---

    private void assertError(HttpResponse<String> response, int status, String message) throws Exception {
        assertEquals(status, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"), response.body());
        assertFalse(json(response).get("success").asBoolean());
        assertEquals(message, json(response).get("message").asText());
    }

    private HttpResponse<String> post(String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + "/api/enrollments"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static JsonNode json(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body());
    }
}
