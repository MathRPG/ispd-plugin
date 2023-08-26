package ispd.policy.loaders;

import ispd.policy.scheduling.grid.*;
import ispd.policy.scheduling.grid.impl.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.jetbrains.annotations.*;

public class GridSchedulingPolicyLoader extends GenericPolicyLoader<GridSchedulingPolicy> {

    private static final Map<String, Supplier<GridSchedulingPolicy>> POLICIES =
        makePolicyMap(policyEntries());

    private static @NotNull Stream<Map.Entry<String, Supplier<GridSchedulingPolicy>>> policyEntries () {
        return Stream.of(
            Map.entry("RoundRobin", RoundRobin::new),
            Map.entry("Workqueue", Workqueue::new),
            Map.entry("WQR", WQR::new),
            Map.entry("DynamicFPLTF", DynamicFPLTF::new),
            Map.entry("HOSEP", HOSEP::new),
            Map.entry("OSEP", OSEP::new),
            Map.entry("EHOSEP", EHOSEP::new)
        );
    }

    public static Map<String, Supplier<GridSchedulingPolicy>> getNativePolicies () {
        return Collections.unmodifiableMap(POLICIES);
    }

    @Override
    protected @NotNull Map<String, Supplier<GridSchedulingPolicy>> getPolicies () {
        return POLICIES;
    }
}
