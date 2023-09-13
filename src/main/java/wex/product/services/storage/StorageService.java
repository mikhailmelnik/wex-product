package wex.product.services.storage;

import java.util.UUID;

public interface StorageService {
    void reset();

    void storeTransaction(Transaction transaction);

    Transaction findTransaction(UUID id);
}
