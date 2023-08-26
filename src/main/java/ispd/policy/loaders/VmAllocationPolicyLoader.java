package ispd.policy.loaders;

import ispd.policy.allocation.vm.*;
import ispd.policy.allocation.vm.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class VmAllocationPolicyLoader extends GenericPolicyLoader<VmAllocationPolicy> {

    private static final Map<String, Supplier<VmAllocationPolicy>> SUPPLIER_MAP = Map.of(
        "RoundRobin", RoundRobin::new,
        "FirstFit", FirstFit::new,
        "FirstFitDecreasing", FirstFitDecreasing::new,
        "Volume", Volume::new
    );

    @Override
    protected @NotNull Map<String, Supplier<VmAllocationPolicy>> getSupplierMap () {
        return SUPPLIER_MAP;
    }
}