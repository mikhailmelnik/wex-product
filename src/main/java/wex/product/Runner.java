package wex.product;

import wex.product.interactor.Interactor;
import wex.product.interactor.commands.CommandException;
import wex.product.interactor.commands.ResetCommand;
import wex.product.interactor.commands.RetrieveCommand;
import wex.product.interactor.commands.StoreCommand;
import wex.product.services.storage.StorageService;
import wex.product.services.transaction.TransactionService;

public final class Runner {

    private final Interactor interactor;
    private final StorageService storageService;
    private final TransactionService transactionService;

    public Runner(Interactor interactor, StorageService storageService, TransactionService transactionService) {
        this.interactor = interactor;
        this.storageService = storageService;
        this.transactionService = transactionService;
    }

    public void run() {
        try {
            var command = interactor.getCommand();
            switch (command) {
                case null -> interactor.printUsage();
                case ResetCommand ignored -> resetStorage();
                case StoreCommand c -> storeTransaction(c);
                case RetrieveCommand c -> retrieveTransaction(c);
                default -> throw new IllegalStateException(String.format("Unexpected command: %s", command.getClass().getName()));
            }
        } catch (CommandException e) {
            interactor.printError(e);
            interactor.printUsage();
        } catch (Throwable e) {
            interactor.printError(e);
        }
    }

    private void resetStorage() {
        storageService.reset();
        interactor.printResult("Done");
    }

    private void storeTransaction(StoreCommand command) {
        var transaction = transactionService.storeTransaction(command.amount(), command.date(), command.description());
        interactor.printResult(transaction.id());
    }

    private void retrieveTransaction(RetrieveCommand command) {
        var transaction = transactionService.retrieveTransaction(command.id(), command.currency());
        interactor.printResult(transaction);
    }
}
