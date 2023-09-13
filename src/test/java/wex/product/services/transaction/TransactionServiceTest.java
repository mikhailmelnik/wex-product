package wex.product.services.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import wex.product.services.exchange.ExchangeRateService;
import wex.product.services.storage.StorageService;
import wex.product.services.storage.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ExchangeRateService exchangeRateService;

    private TransactionService service;

    @BeforeEach
    public void setUp() {
        service = new TransactionService(storageService, exchangeRateService);
    }

    @AfterEach
    public void tearDown() {
        var mocks = new Object[]{storageService, exchangeRateService};
        verifyNoMoreInteractions(mocks);
        Mockito.reset(mocks);
    }

    @Test
    public void storeTransaction_StoresTransaction() {
        var amount = BigDecimal.valueOf(17.32);
        var date = LocalDate.parse("2023-08-25");
        var description = "Description";

        var transaction = service.storeTransaction(amount, date, description);

        assertEquals(amount, transaction.amount());
        assertEquals(date, transaction.date());
        assertEquals(description, transaction.description());
        verify(storageService).storeTransaction(transaction);
    }

    @Test
    public void storeTransaction_RendersUniqueIdForEachTransaction() {
        var transaction1 = service.storeTransaction(BigDecimal.ONE, LocalDate.now(), "Transaction 1");
        var transaction2 = service.storeTransaction(BigDecimal.ONE, LocalDate.now(), "Transaction 2");
        assertNotEquals(transaction1.id(), transaction2.id());
        verify(storageService, times(2)).storeTransaction(any());
    }

    @Test
    public void storeTransaction_RoundsTransactionAmountToCents() {
        var transaction1 = service.storeTransaction(BigDecimal.valueOf(45.7425), LocalDate.now(), "Transaction 1");
        var transaction2 = service.storeTransaction(BigDecimal.valueOf(21.5252), LocalDate.now(), "Transaction 2");
        assertEquals(BigDecimal.valueOf(45.74), transaction1.amount());
        assertEquals(BigDecimal.valueOf(21.53), transaction2.amount());
        verify(storageService, times(2)).storeTransaction(any());
    }

    @Test
    public void storeTransaction_TruncatesLongDescriptions() {
        var transaction1 = service.storeTransaction(BigDecimal.ONE, LocalDate.now(), "Transaction with short description");
        var transaction2 = service.storeTransaction(BigDecimal.ONE, LocalDate.now(), "Transaction with a description exceeding the limit of 50 characters");
        assertEquals("Transaction with short description", transaction1.description());
        assertEquals("Transaction with a description exceeding the limit", transaction2.description());
        verify(storageService, times(2)).storeTransaction(any());
    }

    @Test
    public void retrieveTransaction_EnhancesStoredTransactionWithConversionResult() {
        var currency = "currency";
        var exchangeRate = BigDecimal.valueOf(1.254);
        var storedTransaction = new Transaction(
                UUID.randomUUID(),
                BigDecimal.valueOf(12.64),
                LocalDate.now(),
                "Description"
        );
        var convertedTransaction = new ConvertedTransaction(
                storedTransaction.id(),
                storedTransaction.date(),
                storedTransaction.description(),
                storedTransaction.amount(),
                exchangeRate,
                BigDecimal.valueOf(15.85)
        );

        when(storageService.findTransaction(storedTransaction.id())).thenReturn(storedTransaction);
        when(exchangeRateService.getExchangeRate(currency, storedTransaction.date())).thenReturn(exchangeRate);

        var result = service.retrieveTransaction(storedTransaction.id(), currency);

        assertEquals(convertedTransaction, result);
    }

    @Test
    public void retrieveTransaction_ThrowsRuntimeException_WhenTransactionIsNotFound() {
        var id = UUID.fromString("9c54da8b-94ec-4ead-b480-7e271470a9fd");
        var currency = "currency";

        when(storageService.findTransaction(id)).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () -> service.retrieveTransaction(id, currency));

        assertEquals("Can not find the purchase with id 9c54da8b-94ec-4ead-b480-7e271470a9fd.", exception.getMessage());
    }

    @Test
    public void retrieveTransaction_ThrowsRuntimeException_WhenNoRatesProvided() {
        var currency = "currency";
        var storedTransaction = new Transaction(
                UUID.randomUUID(),
                BigDecimal.valueOf(12.64),
                LocalDate.now(),
                "Description"
        );

        when(storageService.findTransaction(storedTransaction.id())).thenReturn(storedTransaction);
        when(exchangeRateService.getExchangeRate(currency, storedTransaction.date())).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () -> service.retrieveTransaction(storedTransaction.id(), currency));

        assertEquals("The purchase cannot be converted to the target currency.", exception.getMessage());
    }
}
