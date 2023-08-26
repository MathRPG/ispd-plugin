package ispd.policy.loaders;

import ispd.policy.allocation.vm.*;
import ispd.policy.allocation.vm.impl.*;
import java.util.*;
import java.util.function.*;

public class VmAllocationPolicyLoader extends GenericPolicyLoader<VmAllocationPolicy> {

    private static final Map<String, Supplier<VmAllocationPolicy>> SUPPLIER_MAP = Map.of(
        "RoundRobin", RoundRobin::new,
        "FirstFit", FirstFit::new,
        "FirstFitDecreasing", FirstFitDecreasing::new,
        "Volume", Volume::new
    );

    private static final String CLASS_PATH = "ispd.policy.allocation.vm.impl.";

    @Override
    protected String getClassPath () {
        return CLASS_PATH;
    }

    @Override
    public VmAllocationPolicy loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(SUPPLIER_MAP::get)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }
}