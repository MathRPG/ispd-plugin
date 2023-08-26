package ispd.policy.loaders;

import ispd.policy.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public abstract class GenericPolicyLoader <T extends Policy<?>> implements PolicyLoader<T> {

    protected abstract @NotNull Map<String, Supplier<T>> getPolicies ();

    @Override
    public T loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(this.getPolicies()::get)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }
}
