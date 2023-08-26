package ispd.policy.loaders;

import static java.util.Collections.*;

import ispd.policy.scheduling.grid.*;
import ispd.policy.scheduling.grid.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class GridSchedulingPolicyLoader extends GenericPolicyLoader<GridSchedulingPolicy> {

    private static final Map<String, Supplier<GridSchedulingPolicy>> POLICIES;

    static {
        POLICIES = new LinkedHashMap<>();
        POLICIES.put("RoundRobin", RoundRobin::new);
        POLICIES.put("Workqueue", Workqueue::new);
        POLICIES.put("WQR", WQR::new);
        POLICIES.put("DynamicFPLTF", DynamicFPLTF::new);
        POLICIES.put("HOSEP", HOSEP::new);
        POLICIES.put("OSEP", OSEP::new);
        POLICIES.put("EHOSEP", EHOSEP::new);
    }

    public static String[] getPolicyNames () {
        return POLICIES.keySet().toArray(String[]::new);
    }

    @Override
    protected @Unmodifiable @NotNull Map<String, Supplier<GridSchedulingPolicy>> getPolicies () {
        return unmodifiableMap(POLICIES);
    }
}
