package wex.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import wex.product.interactor.Interactor;
import wex.product.interactor.commands.*;
import wex.product.services.storage.StorageService;
import wex.product.services.storage.Transaction;
import wex.product.services.transaction.ConvertedTransaction;
import wex.product.services.transaction.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RunnerTest {

    @Mock
    private Interactor interactor;

    @Mock
    private StorageService storageService;

    @Mock
    private TransactionService transactionService;

    private Runner runner;

    @BeforeEach
    public void setUp() {
        runner = new Runner(interactor, storageService, transactionService);
    }

    @AfterEach
    public void tearDown() {
        var mocks = new Object[]{interactor, storageService, transactionService};
        verifyNoMoreInteractions(mocks);
        Mockito.reset(mocks);
    }

    @Test
    public void run_PrintsErrorAndUsage_WhenCommandExceptionIsThrown() throws CommandException {
        var exception = new CommandException("test");
        when(interactor.getCommand()).thenThrow(exception);
        runner.run();
        verify(interactor).printError(exception);
        verify(interactor).printUsage();
    }

    @Test
    public void run_PrintsError_WhenNonCommandExceptionIsThrown() throws CommandException {
        var exception = new RuntimeException("test");
        when(interactor.getCommand()).thenThrow(exception);
        runner.run();
        verify(interactor).printError(exception);
    }

    @Test
    public void run_ThrowsAndPrintsError_WhenUnknownCommandIsReceived() throws CommandException {
        when(interactor.getCommand()).thenReturn(new UnknownCommand());
        doNothing().when(interactor).printError(argThat((RuntimeException arg) -> arg.getMessage().equals("Unexpected command: wex.product.RunnerTest$UnknownCommand")));
        runner.run();
    }

    @Test
    public void run_PrintsUsage_WhenNoCommandProvided() throws CommandException {
        when(interactor.getCommand()).thenReturn(null);
        runner.run();
        verify(interactor).printUsage();
    }

    @Test
    public void run_ResetsStorage_WhenResetCommandIsPassed() throws CommandException {
        when(interactor.getCommand()).thenReturn(new ResetCommand());
        runner.run();
        verify(storageService).reset();
        verify(interactor).printResult("Done");
    }

    @Test
    public void run_StoresTransactionAndReturnsItsId_WhenStoreCommandIsPassed() throws CommandException {
        var command = new StoreCommand(BigDecimal.valueOf(52.11), LocalDate.now(), "Description");
        var transaction = new Transaction(UUID.randomUUID(), command.amount(), command.date(), command.description());

        when(interactor.getCommand()).thenReturn(command);
        when(transactionService.storeTransaction(command.amount(), command.date(), command.description())).thenReturn(transaction);

        runner.run();

        verify(interactor).printResult(transaction.id());
    }

    @Test
    public void run_RetrievesTransactionAndReturnsIt_WhenRetrieveCommandIsPassed() throws CommandException {
        var command = new RetrieveCommand(UUID.randomUUID(), "currency");
        var convertedTransaction = new ConvertedTransaction(
                UUID.randomUUID(),
                LocalDate.now(),
                "Description",
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(6)
        );

        when(interactor.getCommand()).thenReturn(command);
        when(transactionService.retrieveTransaction(command.id(), command.currency())).thenReturn(convertedTransaction);

        runner.run();

        verify(interactor).printResult(convertedTransaction);
    }

    private static class UnknownCommand implements Command {}
}
