package Login.Enrollment;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** S-014: bare MySQL-feil 1062 (Duplicate entry) skal regnes som duplikatpåmelding (409). Krever ingen database. */
class DatabaseEnrollmentRegistryTest {

    @ParameterizedTest(name = "MySQL-feil {0} er duplikat: {1}")
    @CsvSource({
            "1062, true",    // Duplicate entry: samme e-post på samme kurs
            "1452, false",   // fremmednøkkel: kurset finnes ikke lenger
            "1048, false"    // NOT NULL-kolonne mangler verdi
    })
    void onlyDuplicateEntryCountsAsDuplicate(int mysqlErrorCode, boolean expected) {
        SQLException e = new SQLIntegrityConstraintViolationException("integrity violation", "23000", mysqlErrorCode);

        assertEquals(expected, DatabaseEnrollmentRegistry.isDuplicateEntry(e));
    }
}
