package ispd.gui;

import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public class TextSupplier {

    private static final Logger DEFAULT_LOGGER = Logger.getLogger(TextSupplier.class.getName());

    private static final String MISSING_KEY_TEXT_FORMAT = "Missing text for key \"{0}\"";

    private static Optional<TextSupplier> theInstance = Optional.empty();

    private final ResourceBundle bundle;

    private final Logger logger;

    private TextSupplier (final ResourceBundle bundle, final Logger logger) {
        this.bundle = Objects.requireNonNull(bundle);
        this.logger = Objects.requireNonNull(logger);
    }

    public static @Nls String getText (final @NonNls String key) {
        return theInstance
            .map(ins -> ins.getBundleText(key))
            .orElseThrow(MissingSupplierException::new);
    }

    private static @NotNull String keyMissingMessage (final String key) {
        return MessageFormat.format(MISSING_KEY_TEXT_FORMAT, key);
    }

    public static void setInstance (final @NotNull ResourceBundle bundle) {
        setInstance(bundle, DEFAULT_LOGGER);
    }

    public static void setInstance (
        final @NotNull ResourceBundle bundle,
        final @NotNull Logger logger
    ) {
        theInstance = Optional.of(new TextSupplier(bundle, logger));
    }

    private void logMissingKeyWarning (final String key) {
        this.logger.warning(() -> keyMissingMessage(key));
    }

    private String getBundleText (final String key) {
        return Optional.of(key)
            .filter(this.bundle::containsKey)
            .map(this.bundle::getString)
            .orElseGet(() -> {
                this.logMissingKeyWarning(key);
                return key;
            });
    }

    public static class MissingSupplierException extends IllegalStateException {

        private MissingSupplierException () {
            super("Attempting to call .getText() without call to .setInstance() first.");
        }
    }
}
