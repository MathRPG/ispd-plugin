package ispd.policy;

import ispd.policy.loaders.*;
import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public abstract class Loader <T extends Policy<?>> {

    private static final String INFO_PATTERN = "Loading policy ''{0}''";

    private static final Logger LOGGER = Logger.getLogger(Loader.class.getName());

    protected abstract @NotNull Map<String, Supplier<T>> policyMap ();

    public T loadPolicy (final String name) {
        LOGGER.info(() -> MessageFormat.format(INFO_PATTERN, name));

        return Optional.of(name)
            .map(this::getSupplierByName)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(name));
    }

    private Supplier<T> getSupplierByName (final String name) {
        return this.policyMap().get(name);
    }
}
