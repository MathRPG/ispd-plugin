package ispd.gui;

import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public enum TextSupplier {
    ;

    private static final Logger LOGGER = Logger.getLogger(TextSupplier.class.getName());

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("text.gui");

    public static String getText (final String key) {
        if (!BUNDLE.containsKey(key)) {
            emitMissingKeyWarning(key);
            return key;
        }

        return BUNDLE.getString(key);
    }

    private static void emitMissingKeyWarning (final String key) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning(keyMissingMessage(key));
        }
    }

    private static @NotNull @NonNls String keyMissingMessage (final String key) {
        return MessageFormat.format("Missing text for key \"{0}\"", key);
    }
}
