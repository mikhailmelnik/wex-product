package wex.product;

import wex.product.interactor.Interactor;
import wex.product.mapper.ObjectMapperFactory;
import wex.product.services.exchange.FiscalDataExchangeRateService;
import wex.product.services.storage.FileStorageService;
import wex.product.services.transaction.TransactionService;
import wex.product.utils.LaunchParameters;

public final class Main {
    /**
     * Entry point for the application.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        var objectMapper = ObjectMapperFactory.create();
        var parameters = new LaunchParameters(args);
        var interactor = new Interactor(parameters, System.out, objectMapper);
        var storageService = new FileStorageService("storage.json", objectMapper);
        var exchangeRateService = new FiscalDataExchangeRateService(objectMapper);
        var transactionService = new TransactionService(storageService, exchangeRateService);
        new Runner(interactor, storageService, transactionService).run();
    }
}
