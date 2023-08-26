package ispd.policy.loaders;

import ispd.policy.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public abstract class Loader <T extends Policy<?>> {

    protected abstract @NotNull Map<String, Supplier<T>> policyMap ();

    public T loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(this::getSupplierByName)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }

    private Supplier<T> getSupplierByName (final String name) {
        return this.policyMap().get(name);
    }
}
