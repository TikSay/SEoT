package Login.Enrollment;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.util.Map;

/** POST /api/enrollments: meld deg på et kurs. */
public class EnrollmentController {

    private final EnrollmentHandler handler;

    public EnrollmentController(EnrollmentHandler handler) {
        this.handler = handler;
    }

    public void enroll(Context ctx) {
        // S-014: bare feil i selve forespørselen (ugyldig JSON, feil datatype, ukjent felt) gir 400. Interne feil gir 500.
        // JSON-verdien null er gyldig JSON, men ingen påmelding. Javalin ville kastet NullPointerException for den.
        if (ctx.body().trim().equals("null")) {
            ctx.status(400).json(Map.of("success", false, "message", "Invalid request."));
            return;
        }
        EnrollmentRequest request;
        try {
            // Jackson kaster JsonProcessingException, som Java ikke lar oss fange direkte her, derfor instanceof under
            request = ctx.bodyAsClass(EnrollmentRequest.class);
        } catch (Exception e) {
            if (e instanceof JsonProcessingException) {
                ctx.status(400).json(Map.of("success", false, "message", "Invalid request."));
            } else {
                System.err.println("Enrollment error: " + e);
                ctx.status(500).json(Map.of("success", false, "message", "Enrollment is unavailable right now. Please try again later."));
            }
            return;
        }

        try {
            BigDecimal pris = handler.enroll(request);
            ctx.status(201).json(Map.of("success", true, "pris", pris));

        } catch (CourseNotFoundException e) {
            ctx.status(404).json(Map.of("success", false, "message", e.getMessage()));

        } catch (EnrollmentConflictException e) {
            ctx.status(409).json(Map.of("success", false, "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));

        } catch (RuntimeException e) {
            System.err.println("Enrollment error: " + e.getMessage());
            ctx.status(500).json(Map.of("success", false, "message", "Enrollment is unavailable right now. Please try again later."));
        }
    }
}
