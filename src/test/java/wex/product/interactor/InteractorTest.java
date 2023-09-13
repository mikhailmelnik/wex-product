package wex.product.interactor;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wex.product.interactor.commands.CommandException;
import wex.product.interactor.commands.ResetCommand;
import wex.product.interactor.commands.RetrieveCommand;
import wex.product.interactor.commands.StoreCommand;
import wex.product.mapper.ObjectMapperFactory;
import wex.product.utils.LaunchParameters;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static wex.product.utils.Utils.readTextResource;

public class InteractorTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    @Test
    public void printUsage_PrintsUsage() {
        var outputStream = new ByteArrayOutputStream();
        setupInteractor(outputStream).printUsage();
        assertEquals(readTextResource("usage.txt"), outputStream.toString());
    }

    @Test
    public void printResult_ThrowsRuntimeException_WhenCanNotSerializeToJson() throws JsonProcessingException {
        var objectMapper = Mockito.mock(ObjectMapper.class);
        var interactor = new Interactor(new LaunchParameters(), new PrintStream(new ByteArrayOutputStream()), objectMapper);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonParseException("test"));
        var exception = assertThrows(RuntimeException.class, () -> interactor.printResult("result"));
        assertEquals("Can not serialize result to JSON.", exception.getMessage());
    }

    @Test
    public void printResult_PrintsJson() {
        var outputStream = new ByteArrayOutputStream();
        var command = new RetrieveCommand(UUID.fromString("06cbf119-2111-4a08-a50e-f71c1afebfae"), "currency");
        setupInteractor(outputStream).printResult(command);
        assertEquals(String.format("Result: {\"id\":\"06cbf119-2111-4a08-a50e-f71c1afebfae\",\"currency\":\"currency\"}%n"), outputStream.toString());
    }

    @Test
    public void printError_PrintsMessage() {
        var outputStream = new ByteArrayOutputStream();
        var exception = new Exception("Exception message.");
        setupInteractor(outputStream).printError(exception);
        assertEquals(String.format("Error: Exception message.%n"), outputStream.toString());
    }

    @Test
    public void getCommand_ReturnsNull_WhenNoArgsProvided() throws CommandException {
        var result = setupInteractor().getCommand();
        assertNull(result);
    }

    @Test
    public void getCommand_ReturnsResetCommand() throws CommandException {
        var interactor = setupInteractor("reset");
        var result = interactor.getCommand();
        assertEquals(new ResetCommand(), result);
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenUnknownCommandProvided() {
        var interactor = setupInteractor("hey");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Unknown command hey.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenNoAmountArgumentProvidedForStoreCommand() {
        var interactor = setupInteractor("store");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument amount is not provided.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenAmountArgumentIsNotADecimalNumber() {
        var interactor = setupInteractor("store", "--amount=x");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument amount is not a valid decimal number.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenNoDateArgumentProvidedForStoreCommand() {
        var interactor = setupInteractor("store", "--amount=11.45");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument date is not provided.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenDateArgumentIsARandomString() {
        var interactor = setupInteractor("store", "--amount=11.45", "--date=tomorrow");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument date is not a valid ISO 8601 date.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenDateArgumentIsNotAnISODate() {
        var interactor = setupInteractor("store", "--amount=11.45", "--date=2023-08-31 02:22");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument date is not a valid ISO 8601 date.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenNoDescriptionArgumentProvidedForStoreCommand() {
        var interactor = setupInteractor("store", "--amount=11.45", "--date=2023-08-31");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument description is not provided.", exception.getMessage());
    }

    @Test
    public void getCommand_ReturnsStoreCommand_WhenAllArgumentsProvided() throws CommandException {
        var interactor = setupInteractor("store", "--amount=11.45", "--date=2023-08-31", "--description=test");
        var result = interactor.getCommand();
        assertEquals(new StoreCommand(BigDecimal.valueOf(11.45), LocalDate.parse("2023-08-31"), "test"), result);
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenNoIdArgumentProvidedForRetrieveCommand() {
        var interactor = setupInteractor("retrieve");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument id is not provided.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenIdArgumentIsNotAUuid() {
        var interactor = setupInteractor("retrieve", "--id=x");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument id is not a valid UUID.", exception.getMessage());
    }

    @Test
    public void getCommand_ThrowsCommandException_WhenNoCurrencyArgumentProvidedForRetrieveCommand() {
        var interactor = setupInteractor("retrieve", "--id=06cbf119-2111-4a08-a50e-f71c1afebfae");
        var exception = assertThrows(CommandException.class, interactor::getCommand);
        assertEquals("Argument currency is not provided.", exception.getMessage());
    }

    @Test
    public void getCommand_ReturnsRetrieveCommand_WhenAllArgumentsProvided() throws CommandException {
        var interactor = setupInteractor("retrieve", "--id=06cbf119-2111-4a08-a50e-f71c1afebfae", "--currency=dollar");
        var result = interactor.getCommand();
        assertEquals(new RetrieveCommand(UUID.fromString("06cbf119-2111-4a08-a50e-f71c1afebfae"), "dollar"), result);
    }

    private Interactor setupInteractor(String... args) {
        return setupInteractor(new ByteArrayOutputStream(), args);
    }

    private Interactor setupInteractor(ByteArrayOutputStream outputStream, String... args) {
        return new Interactor(new LaunchParameters(args), new PrintStream(outputStream), objectMapper);
    }

}
