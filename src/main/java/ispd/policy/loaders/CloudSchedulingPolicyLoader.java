package ispd.policy.loaders;

import ispd.policy.scheduling.cloud.*;
import ispd.policy.scheduling.cloud.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class CloudSchedulingPolicyLoader extends GenericPolicyLoader<CloudSchedulingPolicy> {

    private static final Map<String, Supplier<CloudSchedulingPolicy>> SUPPLIER_MAP = Map.of(
        "RoundRobin", RoundRobin::new
    );

    private static final String CLASS_PATH = "ispd.policy.scheduling.cloud.impl.";

    @Override
    protected String getClassPath () {
        return CLASS_PATH;
    }

    @Override
    protected @NotNull Map<String, Supplier<CloudSchedulingPolicy>> getSupplierMap () {
        return SUPPLIER_MAP;
    }

    @Override
    public CloudSchedulingPolicy loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(SUPPLIER_MAP::get)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }
}
