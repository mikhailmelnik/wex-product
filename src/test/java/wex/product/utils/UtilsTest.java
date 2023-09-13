package wex.product.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static wex.product.utils.Utils.readTextResource;

public class UtilsTest {
    @Test
    public void readTextResource_ThrowsRuntimeException_WhenResourceIsNotFound() {
        var exception = assertThrows(RuntimeException.class, () -> readTextResource("bad-resource.txt"));
        assertEquals("Can not read resource bad-resource.txt.", exception.getMessage());
    }

    @Test
    public void readTextResource_ReturnsTextResourceContent() {
        var result = readTextResource("test-resource.txt");
        assertEquals(String.format("Multiline%nResource%nText"), result);
    }
}
