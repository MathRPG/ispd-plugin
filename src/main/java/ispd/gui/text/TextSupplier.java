package ispd.gui.text;

import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public class TextSupplier {

    private static final Logger LOGGER = Logger.getLogger(TextSupplier.class.getName());

    private static final String MISSING_KEY_TEXT_FORMAT = "Missing text for key \"{0}\"";

    private static Optional<TextSupplier> theInstance = Optional.empty();

    private final ResourceBundle bundle;

    private final Logger logger;

    private TextSupplier (final ResourceBundle bundle, final Logger logger) {
        this.bundle = Objects.requireNonNull(bundle);
        this.logger = Objects.requireNonNull(logger);
    }

    public static @Nls String getText (final @NonNls @NotNull String key) {
        return theInstance
            .map(ins -> ins.getBundleText(key))
            .orElseThrow(NoConfiguredBundleException::new);
    }

    private static String missingTextMessage (final String key) {
        return MessageFormat.format(MISSING_KEY_TEXT_FORMAT, key);
    }

    public static void configure (final @NotNull ResourceBundle newBundle) {
        configure(newBundle, LOGGER);
    }

    public static void configure (
        final @NotNull ResourceBundle newBundle,
        final @NotNull Logger newLogger
    ) {
        theInstance = Optional.of(new TextSupplier(newBundle, newLogger));
    }

    private String logMissingTextWarning (final String key) {
        this.logger.warning(() -> missingTextMessage(key));
        return key;
    }

    private String getBundleText (final String key) {
        return Optional.of(key)
            .filter(this.bundle::containsKey)
            .map(this.bundle::getString)
            .orElseGet(() -> this.logMissingTextWarning(key));
    }
}
