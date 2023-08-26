package ispd.policy.loaders;

import static java.util.Collections.*;

import ispd.policy.scheduling.cloud.*;
import ispd.policy.scheduling.cloud.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class CloudSchedulingPolicyLoader extends GenericPolicyLoader<CloudSchedulingPolicy> {

    private static final Map<String, Supplier<CloudSchedulingPolicy>> POLICIES;

    static {
        POLICIES = new LinkedHashMap<>();
        POLICIES.put("RoundRobin", RoundRobin::new);
    }

    @Override
    protected @NotNull Map<String, Supplier<CloudSchedulingPolicy>> getPolicies () {
        return unmodifiableMap(POLICIES);
    }
}
