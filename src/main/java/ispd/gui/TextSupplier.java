package ispd.gui;

import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public enum TextSupplier {
    ;

    private static ResourceBundle BUNDLE = null;

    private static Logger LOGGER = null;

    public static String getText (final String key) {
        if (BUNDLE == null) {
            throw new MissingResourceException("", "", "");
        }

        if (!BUNDLE.containsKey(key)) {
            emitMissingKeyWarning(key);
            return key;
        }

        return BUNDLE.getString(key);
    }

    private static void emitMissingKeyWarning (final String key) {
        LOGGER.warning(() -> keyMissingMessage(key));
    }

    private static @NotNull @NonNls String keyMissingMessage (final String key) {
        return MessageFormat.format("Missing text for key \"{0}\"", key);
    }

    public static void setInstance (final @NotNull ResourceBundle bundle) {
        setInstance(bundle, Logger.getLogger(TextSupplier.class.getName()));
    }

    public static void setInstance (final ResourceBundle bundle, final Logger logger) {
        BUNDLE = Objects.requireNonNull(bundle);
        LOGGER = logger;
    }
}
