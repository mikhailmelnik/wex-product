package wex.product.services.exchange;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateService {
    BigDecimal getExchangeRate(String currency, LocalDate date);
}
