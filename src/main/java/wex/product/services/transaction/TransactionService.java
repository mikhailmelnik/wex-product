package wex.product.services.transaction;

import wex.product.services.exchange.ExchangeRateService;
import wex.product.services.storage.StorageService;
import wex.product.services.storage.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public final class TransactionService {

    private static final int AMOUNT_SCALE = 2;
    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final StorageService storageService;
    private final ExchangeRateService exchangeRateService;

    public TransactionService(StorageService storageService, ExchangeRateService exchangeRateService) {
        this.storageService = storageService;
        this.exchangeRateService = exchangeRateService;
    }

    public Transaction storeTransaction(BigDecimal amount, LocalDate date, String description) {
        var truncatedDescription = description.substring(0, Math.min(MAX_DESCRIPTION_LENGTH, description.length()));
        var transaction = new Transaction(UUID.randomUUID(), scale(amount), date, truncatedDescription);
        storageService.storeTransaction(transaction);
        return transaction;
    }

    public ConvertedTransaction retrieveTransaction(UUID id, String currency) {
        var transaction = storageService.findTransaction(id);
        if (transaction == null) {
            throw new IllegalStateException(String.format("Can not find the purchase with id %s.", id));
        }
        var exchangeRate = exchangeRateService.getExchangeRate(currency, transaction.date());
        if (exchangeRate == null) {
            throw new IllegalStateException("The purchase cannot be converted to the target currency.");
        }
        return new ConvertedTransaction(
                transaction.id(),
                transaction.date(),
                transaction.description(),
                transaction.amount(),
                exchangeRate,
                scale(transaction.amount().multiply(exchangeRate))
        );
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
}
