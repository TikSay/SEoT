package Login.Course;

import java.util.List;

/**
 * Henter kurs. Samme mønster som DataRegistry: én ekte versjon mot MySQL
 * (DatabaseCourseRegistry) og eventuelt en falsk versjon i minnet for tester.
 */
public interface CourseRegistry {

    /** Kurs som ikke har startet ennå, sortert med det første kurset øverst. */
    List<Course> findUpcomingCourses();
}
