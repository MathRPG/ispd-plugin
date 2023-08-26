package ispd.policy.loaders;

import ispd.policy.scheduling.cloud.*;
import ispd.policy.scheduling.cloud.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class CloudSchedulingPolicyLoader extends GenericPolicyLoader<CloudSchedulingPolicy> {

    private static final Map<String, Supplier<CloudSchedulingPolicy>> POLICIES = Map.of(
        "RoundRobin", RoundRobin::new
    );

    @Override
    protected @NotNull Map<String, Supplier<CloudSchedulingPolicy>> getPolicies () {
        return POLICIES;
    }
}
