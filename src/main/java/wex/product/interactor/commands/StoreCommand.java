package wex.product.interactor.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StoreCommand(BigDecimal amount, LocalDate date, String description) implements Command {
}
