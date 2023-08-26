package ispd.policy.loaders;

import static java.util.Collections.*;

import ispd.policy.scheduling.cloud.*;
import ispd.policy.scheduling.cloud.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class CloudSchedulingPolicyLoader extends PolicyLoader<CloudSchedulingPolicy> {

    private static final Map<String, Supplier<CloudSchedulingPolicy>> POLICIES;

    static {
        POLICIES = new LinkedHashMap<>();
        POLICIES.put("RoundRobin", RoundRobin::new);
    }

    public static String[] getPolicyNames () {
        return POLICIES.keySet().toArray(String[]::new);
    }

    @Override
    protected @NotNull Map<String, Supplier<CloudSchedulingPolicy>> policyMap () {
        return unmodifiableMap(POLICIES);
    }
}
