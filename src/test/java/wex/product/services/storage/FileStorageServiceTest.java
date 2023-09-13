package wex.product.services.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import wex.product.mapper.ObjectMapperFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    private final Transaction sampleTransaction = new Transaction(
            UUID.fromString("8a43c3eb-7210-47dd-8e7e-7ae3c92971d7"),
            BigDecimal.valueOf(15.67),
            LocalDate.parse("2023-08-24"),
            "New Transaction");

    private final String contentWithoutSampleTransaction = """
            [{"id":"8a522a6c-28e3-4b94-aada-078e7fde20e6","amount":81.12,"date":"2023-07-31","description":"Initial Transaction"}]""";

    private final String contentWithSampleTransaction = """
            [{"id":"8a522a6c-28e3-4b94-aada-078e7fde20e6","amount":81.12,"date":"2023-07-31","description":"Initial Transaction"},{"id":"8a43c3eb-7210-47dd-8e7e-7ae3c92971d7","amount":15.67,"date":"2023-08-24","description":"New Transaction"}]""";

    @Mock
    private FileStorageService.FileAccessor fileAccessor;

    private FileStorageService service;

    @BeforeEach
    public void setUp() {
        service = new FileStorageService(fileAccessor, objectMapper);
    }

    @AfterEach
    public void tearDown() {
        var mocks = new Object[]{fileAccessor};
        verifyNoMoreInteractions(mocks);
        Mockito.reset(mocks);
    }

    @Test
    public void constructor_WorksWithDefaults() {
        new FileStorageService("test.txt", objectMapper);
    }

    @Test
    public void reset_DeletesFileStorage() {
        service.reset();
        verify(fileAccessor).deleteFile();
    }

    @Test
    public void storeTransaction_AppendsFileWithTheTransactionToStore() throws IOException {
        var inputStream = new ByteArrayInputStream(contentWithoutSampleTransaction.getBytes());
        var outputStream = new ByteArrayOutputStream();

        when(fileAccessor.getInputStream()).thenReturn(inputStream);
        when(fileAccessor.getOutputStream()).thenReturn(outputStream);

        service.storeTransaction(sampleTransaction);

        assertEquals(contentWithSampleTransaction, outputStream.toString());

        verify(fileAccessor, times(2)).ensureFileExists();
    }

    @Test
    public void storeTransaction_ThrowsUncheckedException_WhenCatchesCheckedException() throws IOException {
        var inputStream = new ByteArrayInputStream(contentWithoutSampleTransaction.getBytes());

        when(fileAccessor.getInputStream()).thenReturn(inputStream);
        doThrow(new FileNotFoundException()).when(fileAccessor).getOutputStream();

        assertThrows(RuntimeException.class, () -> service.storeTransaction(sampleTransaction));
        verify(fileAccessor, times(2)).ensureFileExists();
    }

    @Test
    public void findTransaction_ReturnsNull_WhenTransactionWithGivenIdDoesNotExist() throws IOException {
        var inputStream = new ByteArrayInputStream(contentWithoutSampleTransaction.getBytes());

        when(fileAccessor.getInputStream()).thenReturn(inputStream);

        var result = service.findTransaction(UUID.randomUUID());

        assertNull(result);

        verify(fileAccessor).ensureFileExists();
    }

    @Test
    public void findTransaction_ReturnsFoundTransaction_WhenTransactionWithGivenIdExists() throws IOException {
        var inputStream = new ByteArrayInputStream(contentWithSampleTransaction.getBytes());

        when(fileAccessor.getInputStream()).thenReturn(inputStream);

        var result = service.findTransaction(sampleTransaction.id());

        assertEquals(sampleTransaction, result);

        verify(fileAccessor).ensureFileExists();
    }

    @Test
    public void findTransaction_ThrowsUncheckedException_WhenCatchesCheckedException() throws IOException {
        doThrow(new FileNotFoundException()).when(fileAccessor).getInputStream();
        assertThrows(RuntimeException.class, () -> service.findTransaction(sampleTransaction.id()));
        verify(fileAccessor).ensureFileExists();
    }
}
