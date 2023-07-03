package ispd.utils;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

public enum NameValidator {
    ;

    private static final Collection<String> JAVA_RESERVED_KEYWORDS = Set.of(
        "abstract", "assert",
        "boolean", "break", "byte",
        "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double",
        "else", "enum", "extends",
        "false", "final", "finally", "float", "for",
        "goto",
        "if", "implements", "import", "instanceof", "int", "interface",
        "long",
        "native", "new", "null",
        "package", "private", "protected", "public",
        "return",
        "short", "static", "strictfp", "super", "switch", "synchronized",
        "this", "throw", "throws", "transient", "true", "try",
        "void", "volatile",
        "while"
    );

    private static final Pattern VALID_CLASS_IDENTIFIER =
        Pattern.compile("[a-zA-Z$_][a-zA-Z\\d$_]*");

    public static boolean isValidClassName (final String name) {
        return isValidClassIdentifier(name) && !isReservedKeyword(name);
    }

    private static boolean isValidClassIdentifier (final CharSequence sequence) {
        return NameValidator.VALID_CLASS_IDENTIFIER.matcher(sequence).matches();
    }

    private static boolean isReservedKeyword (final String word) {
        return NameValidator.JAVA_RESERVED_KEYWORDS.contains(word);
    }
}
