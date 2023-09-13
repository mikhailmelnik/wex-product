package wex.product.services.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class FileStorageService implements StorageService {

    private final FileAccessor fileAccessor;
    private final ObjectMapper objectMapper;

    public FileStorageService(String storagePath, ObjectMapper objectMapper) {
        this(new FileAccessor(storagePath), objectMapper);
    }

    public FileStorageService(FileAccessor fileAccessor, ObjectMapper objectMapper) {
        this.fileAccessor = fileAccessor;
        this.objectMapper = objectMapper;
    }

    @Override
    public void reset() {
        fileAccessor.deleteFile();
    }

    @Override
    public void storeTransaction(Transaction transaction) {
        var transactions = readTransactions();
        transactions.add(transaction);
        writeTransactions(transactions);
    }

    @Override
    public Transaction findTransaction(UUID id) {
        return readTransactions().stream().filter(t -> t.id().equals(id)).findAny().orElse(null);
    }

    private List<Transaction> readTransactions() {
        try {
            fileAccessor.ensureFileExists();
            var typeReference = new TypeReference<List<Transaction>>() {};
            return objectMapper.readValue(fileAccessor.getInputStream(), typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Can not read transactions from the file storage.", e);
        }
    }

    private void writeTransactions(List<Transaction> transactions) {
        try {
            fileAccessor.ensureFileExists();
            objectMapper.writeValue(fileAccessor.getOutputStream(), transactions);
        } catch (IOException e) {
            throw new RuntimeException("Can not write transactions to the file storage.", e);
        }
    }

    public static class FileAccessor {

        private final File file;

        public FileAccessor(String path) {
            this.file = new File(path);
        }

        public void ensureFileExists() throws IOException {
            if (!file.exists()) {
                try (var fileWriter = new FileWriter(file)) {
                    fileWriter.write("[]");
                }
            }
        }

        public void deleteFile() {
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }

        public InputStream getInputStream() throws FileNotFoundException {
            return new FileInputStream(file);
        }

        public OutputStream getOutputStream() throws FileNotFoundException {
            return new FileOutputStream(file);
        }
    }
}
