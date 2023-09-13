package wex.product.services.storage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Transaction(
        UUID id,
        BigDecimal amount,
        LocalDate date,
        String description
) {
}
