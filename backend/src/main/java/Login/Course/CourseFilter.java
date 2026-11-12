package Login.course;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CourseFilter(
        String lokallag,
        String sted,
        String nivaa,
        LocalDate fra,
        LocalDate til,
        BigDecimal prisMin,
        BigDecimal prisMax,
        boolean inkluderFerdige
) {
}