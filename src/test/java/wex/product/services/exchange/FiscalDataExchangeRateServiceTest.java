package wex.product.services.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wex.product.mapper.ObjectMapperFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static wex.product.services.exchange.FiscalDataExchangeRateServiceTest.WIREMOCK_PORT;

@WireMockTest(httpPort = WIREMOCK_PORT)
public class FiscalDataExchangeRateServiceTest {

    public static final int WIREMOCK_PORT = 8011;

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    private FiscalDataExchangeRateService service;

    @BeforeEach
    public void setUp() {
        service = new FiscalDataExchangeRateService("http://localhost:8011", objectMapper);
    }

    @AfterEach
    public void tearDown() {
        WireMock.reset();
    }

    @Test
    public void constructor_WorksWithDefaults() {
        new FiscalDataExchangeRateService(objectMapper);
    }

    @Test
    public void getExchangeRate_RequestsAndFetchesAndReturnsCorrectExchangeRate() {
        var expectedUrl = "/?fields=record_date,exchange_rate&filter=country_currency_desc:eq:test_currency,record_date:gte:2023-02-17,record_date:lte:2023-08-17&sort=-record_date&page[number]=1&page[size]=1";
        stubFor(get(expectedUrl).willReturn(ok().withBody("{ \"data\": [{ \"exchange_rate\": 12.345 }, { \"exchange_rate\": 99.101 }] }")));
        var result = service.getExchangeRate("test_currency", LocalDate.parse("2023-08-17"));
        assertEquals(BigDecimal.valueOf(12.345), result);
    }

    @Test
    public void getExchangeRate_ReturnsNullIfNoExchangeRateFetched() {
        stubFor(any(anyUrl()).willReturn(ok().withBody("{ \"data\": [] }")));
        var result = service.getExchangeRate("test_currency", LocalDate.now());
        assertNull(result);
    }

    @Test
    public void getExchangeRate_ThrowsRuntimeException_WhenRequestFails() {
        stubFor(any(anyUrl()).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        var exception = assertThrows(RuntimeException.class, () -> service.getExchangeRate("test_currency", LocalDate.now()));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    public void getExchangeRate_ThrowsRuntimeException_WhenCanNotParseResponse() {
        stubFor(any(anyUrl()).willReturn(ok().withBody("{ \"key\": \"value\" }")));
        var exception = assertThrows(RuntimeException.class, () -> service.getExchangeRate("test_currency", LocalDate.now()));
        assertInstanceOf(MismatchedInputException.class, exception.getCause());
    }
}
