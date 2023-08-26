package ispd.policy.loaders;

import ispd.policy.allocation.vm.*;
import ispd.policy.allocation.vm.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class VmAllocationPolicyLoader extends PolicyLoader<VmAllocationPolicy> {

    private static final Map<String, Supplier<VmAllocationPolicy>> POLICIES = Map.of(
        "RoundRobin", RoundRobin::new,
        "FirstFit", FirstFit::new,
        "FirstFitDecreasing", FirstFitDecreasing::new,
        "Volume", Volume::new
    );

    public static String[] getPolicyNames () {
        return POLICIES.keySet().toArray(String[]::new);
    }

    @Override
    protected @NotNull Map<String, Supplier<VmAllocationPolicy>> getPolicies () {
        return POLICIES;
    }
}