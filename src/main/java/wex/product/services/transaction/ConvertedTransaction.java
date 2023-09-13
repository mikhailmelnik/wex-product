package wex.product.services.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertedTransaction(
        UUID id,
        LocalDate date,
        String description,
        BigDecimal amount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount
) {
}
