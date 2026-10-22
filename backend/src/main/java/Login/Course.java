package Login;

import java.math.BigDecimal;

public record Course(
        int id,
        String tittel,
        String beskrivelse,
        String lokallag,
        String sted,
        String dato,
        String starttid,
        int kapasitet,
        BigDecimal pris,
        String nivaa
) {
}