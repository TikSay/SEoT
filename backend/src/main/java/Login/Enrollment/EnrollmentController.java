package Login.Enrollment;

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
        try {
            EnrollmentRequest request = ctx.bodyAsClass(EnrollmentRequest.class);
            BigDecimal pris = handler.enroll(request);
            ctx.status(201).json(Map.of("success", true, "pris", pris));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            ctx.status(500).json(Map.of("success", false, "message", "Enrollment is unavailable right now."));
        }
    }
}