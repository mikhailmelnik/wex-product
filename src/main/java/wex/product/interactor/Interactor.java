package wex.product.interactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import wex.product.interactor.commands.*;
import wex.product.utils.LaunchParameters;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static wex.product.utils.Utils.readTextResource;

public final class Interactor {

    private final LaunchParameters launchParameters;
    private final PrintStream printStream;
    private final ObjectMapper objectMapper;

    public Interactor(LaunchParameters launchParameters, PrintStream printStream, ObjectMapper objectMapper) {
        this.launchParameters = launchParameters;
        this.printStream = printStream;
        this.objectMapper = objectMapper;
    }

    public void printUsage() {
        printStream.print(readTextResource("usage.txt"));
    }

    public void printResult(Object result) {
        try {
            printStream.printf("Result: %s%n", objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can not serialize result to JSON.", e);
        }
    }

    public void printError(Throwable e) {
        printStream.printf("Error: %s%n", e.getMessage());
    }

    public Command getCommand() throws CommandException {
        return switch (launchParameters.command()) {
            case null -> null;
            case "reset" -> buildResetCommand();
            case "store" -> buildStoreCommand();
            case "retrieve" -> buildRetrieveCommand();
            case default -> throw new CommandException(String.format("Unknown command %s.", launchParameters.command()));
        };
    }

    private ResetCommand buildResetCommand() {
        return new ResetCommand();
    }

    private StoreCommand buildStoreCommand() throws CommandException {
        return new StoreCommand(
                getBigDecimalArgument("amount"),
                getIsoDateArgument("date"),
                getStringArgument("description")
        );
    }

    private RetrieveCommand buildRetrieveCommand() throws CommandException {
        return new RetrieveCommand(
                getUuidArgument("id"),
                getStringArgument("currency")
        );
    }

    private UUID getUuidArgument(@SuppressWarnings("SameParameterValue") String argumentName) throws CommandException {
        var value = getStringArgument(argumentName);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new CommandException(String.format("Argument %s is not a valid UUID.", argumentName));
        }
    }

    private BigDecimal getBigDecimalArgument(@SuppressWarnings("SameParameterValue") String argumentName) throws CommandException {
        var value = getStringArgument(argumentName);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new CommandException(String.format("Argument %s is not a valid decimal number.", argumentName));
        }
    }

    private LocalDate getIsoDateArgument(@SuppressWarnings("SameParameterValue") String argumentName) throws CommandException {
        var value = getStringArgument(argumentName);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new CommandException(String.format("Argument %s is not a valid ISO 8601 date.", argumentName));
        }
    }

    private String getStringArgument(String argumentName) throws CommandException {
        var value = launchParameters.arguments().get(argumentName);
        if (value == null) {
            throw new CommandException(String.format("Argument %s is not provided.", argumentName));
        } else {
            return value;
        }
    }
}
