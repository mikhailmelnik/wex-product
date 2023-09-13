package wex.product.interactor.commands;

import java.util.UUID;

public record RetrieveCommand(UUID id, String currency) implements Command {
}
