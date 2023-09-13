package wex.product.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LaunchParametersTest {

    @Test
    public void instance_ReturnsNullCommandAndNoArguments_WhenNoArgsProvided() {
        var result = new LaunchParameters();
        assertNull(result.command());
        assertEquals(0, result.arguments().size());
    }

    @Test
    public void instance_ReturnsTheFirstArgumentAsACommand() {
        var result = new LaunchParameters("--test", "--key=value");
        assertEquals("--test", result.command());
        assertEquals(1, result.arguments().size());
        assertEquals("value", result.arguments().get("key"));
    }

    @Test
    public void instance_ReturnsOnlyValidArguments() {
        var result = new LaunchParameters("command", "a=b=c", "x", "-y=z", "--", "--param", "--key=value", "--amount=10");
        assertEquals("command", result.command());
        assertEquals(3, result.arguments().size());
        assertEquals("value", result.arguments().get("key"));
        assertEquals("10", result.arguments().get("amount"));
        assertTrue(result.arguments().containsKey("param"));
        assertNull(result.arguments().get("param"));
    }
}
