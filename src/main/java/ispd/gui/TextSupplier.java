package ispd.gui;

import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public class TextSupplier {

    private static final String MISSING_KEY_TEXT = "Missing text for key \"{0}\"";

    private static final Logger DEFAULT_LOGGER = Logger.getLogger(TextSupplier.class.getName());

    private static Optional<TextSupplier> theInstance = Optional.empty();

    private final ResourceBundle bundle;

    private final Logger logger;

    private TextSupplier (final ResourceBundle bundle, final Logger logger) {
        this.bundle = Objects.requireNonNull(bundle);
        this.logger = Objects.requireNonNull(logger);
    }

    public static String getText (final String key) {
        return theInstance
            .map(i -> i.getBundleText(key))
            .orElseThrow(MissingInstanceException::new);
    }

    private static @NotNull @NonNls String keyMissingMessage (final String key) {
        return MessageFormat.format(MISSING_KEY_TEXT, key);
    }

    public static void setInstance (final @NotNull ResourceBundle bundle) {
        setInstance(bundle, DEFAULT_LOGGER);
    }

    public static void setInstance (final @NotNull ResourceBundle bundle, final Logger logger) {
        theInstance = Optional.of(new TextSupplier(bundle, logger));
    }

    private void emitMissingKeyWarning (final String key) {
        this.logger.warning(() -> keyMissingMessage(key));
    }

    private String getBundleText (final String key) {
        if (!this.bundle.containsKey(key)) {
            this.emitMissingKeyWarning(key);
            return key;
        }

        return this.bundle.getString(key);
    }

    public static class MissingInstanceException extends IllegalStateException {

        private MissingInstanceException () {
            super("Attempting to call .getText() without call to .setInstance() first.");
        }
    }
}
