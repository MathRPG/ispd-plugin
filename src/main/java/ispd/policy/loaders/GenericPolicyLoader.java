package ispd.policy.loaders;

import static java.util.stream.Collectors.*;

import ispd.policy.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.jetbrains.annotations.*;

public abstract class GenericPolicyLoader <T extends Policy<?>> implements PolicyLoader<T> {

    protected static @NotNull <T> Map<String, Supplier<T>> makePolicyMap (
        final @NotNull Stream<? extends Map.Entry<String, Supplier<T>>> entryStream
    ) {
        return entryStream.collect(toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (x, y) -> y,
            LinkedHashMap::new
        ));
    }

    protected abstract @NotNull Map<String, Supplier<T>> getPolicies ();

    @Override
    public T loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(this.getPolicies()::get)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }
}
