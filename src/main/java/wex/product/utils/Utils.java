package wex.product.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Utils {

    private Utils() {
    }

    public static String readTextResource(String path) {
        try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(path)) {
            Objects.requireNonNull(inputStream);
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Can not read resource %s.", path), e);
        }
    }
}
