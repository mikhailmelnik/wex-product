package wex.product.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public record LaunchParameters(
        String command,
        Map<String, String> arguments
) {

    public LaunchParameters(String... args) {
        this(getCommand(args), getCommandArguments(args));
    }

    private static String getCommand(String[] args) {
        return args.length > 0 ? args[0] : null;
    }

    private static Map<String, String> getCommandArguments(String[] args) {
        return Arrays.stream(args)
                .skip(1)
                .map(arg -> Arrays.stream(arg.split("=")).map(String::trim).toList())
                .filter(list -> list.size() <= 2 && list.get(0).startsWith("--") && list.get(0).length() > 2)
                .collect(HashMap::new, (map, list) -> map.put(list.get(0).substring(2), list.size() > 1 ? list.get(1) : null), HashMap::putAll);
    }
}
