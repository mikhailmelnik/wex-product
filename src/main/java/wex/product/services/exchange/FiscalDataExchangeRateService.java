package wex.product.services.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public final class FiscalDataExchangeRateService implements ExchangeRateService {

    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public FiscalDataExchangeRateService(ObjectMapper objectMapper) {
        this("https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange", objectMapper);
    }

    public FiscalDataExchangeRateService(String apiUrl, ObjectMapper objectMapper) {
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public BigDecimal getExchangeRate(String currency, LocalDate date) {
        try {
            ExchangeRateResponse response = fetchExchangeRate(currency, date);
            return response.data.size() > 0 ? response.data.get(0).exchangeRate : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch exchange rate.", e);
        }
    }

    private ExchangeRateResponse fetchExchangeRate(String currency, LocalDate date) throws IOException, InterruptedException {
        var url = String.format(apiUrl +
                        "?fields=record_date,exchange_rate" +
                        "&filter=country_currency_desc:eq:%s,record_date:gte:%s,record_date:lte:%s" +
                        "&sort=-record_date" +
                        "&page[number]=1" +
                        "&page[size]=1",
                currency, date.minusMonths(6), date);
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), ExchangeRateResponse.class);
    }

    private record ExchangeRateResponse(
            @JsonProperty(value = "data", required = true)
            List<Record> data
    ) {
        private record Record(
                @JsonProperty(value = "exchange_rate", required = true)
                BigDecimal exchangeRate
        ) {
        }
    }
}
