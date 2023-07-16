package ispd.utils;

import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public enum StringUtils {
    ;

    /**
     * Replaces unprintable characters by their escaped (or unicode escaped) equivalents in the
     * given char sequence.
     */
    public static String escapeString (final CharSequence cs) {
        return cs.chars()
            .mapToObj(StringUtils::escapeCharacter)
            .collect(Collectors.joining());
    }

    public static @NotNull String escapeCharacter (final int character) {
        return switch (character) {
            case 0 -> "";
            case '\b' -> "\\b";
            case '\t' -> "\\t";
            case '\n' -> "\\n";
            case '\f' -> "\\f";
            case '\r' -> "\\r";
            case '\"' -> "\\\"";
            case '\'' -> "\\'";
            case '\\' -> "\\\\";
            default -> {
                if (character < 0x20 || character > 0x7e) {
                    final var s = "0000" + Integer.toString(character, 16);
                    yield "\\u" + s.substring(s.length() - 4);
                } else {
                    yield String.valueOf(character);
                }
            }
        };
    }
}
