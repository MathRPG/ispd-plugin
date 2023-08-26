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

    @Override
    protected @NotNull Map<String, Supplier<CloudSchedulingPolicy>> getSupplierMap () {
        return SUPPLIER_MAP;
    }
}
