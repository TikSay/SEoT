package Login;

import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

/**
 * GET /api/courses
 *
 * Svar (samme stil som resten av API-et, med success og message):
 * {
 *   "success": true,
 *   "message": "4 courses found.",
 *   "courses": [ { "id": 1, "tittel": "...", "beskrivelse": "...",
 *                  "bildeUrl": "...", "sted": "...", "startTid": "2026-11-05T18:00" } ]
 * }
 */
public class CourseController {

    private final CourseRegistry registry;

    public CourseController(CourseRegistry registry) {
        this.registry = registry;
    }

    public void getCourses(Context ctx) {
        try {
            List<Course> courses = registry.findUpcomingCourses();

            ctx.status(200).json(Map.of(
                    "success", true,
                    "message", courses.size() + " courses found.",
                    "courses", courses
            ));

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());

            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Courses are unavailable right now. Please try again later."
            ));
        }
    }
}
