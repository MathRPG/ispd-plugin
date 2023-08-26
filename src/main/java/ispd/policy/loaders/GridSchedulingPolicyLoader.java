package ispd.policy.loaders;

import ispd.policy.scheduling.grid.*;
import ispd.policy.scheduling.grid.impl.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class GridSchedulingPolicyLoader extends GenericPolicyLoader<GridSchedulingPolicy> {

    private static final Map<String, Supplier<GridSchedulingPolicy>> SUPPLIER_MAP = Map.of(
        "RoundRobin", RoundRobin::new,
        "Workqueue", Workqueue::new,
        "WQR", WQR::new,
        "DynamicFPLTF", DynamicFPLTF::new,
        "HOSEP", HOSEP::new,
        "OSEP", OSEP::new,
        "EHOSEP", EHOSEP::new
    );

    private static final String CLASS_PATH = "ispd.policy.scheduling.grid.impl.";

    @Override
    public GridSchedulingPolicy loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(SUPPLIER_MAP::get)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }

    @Override
    protected String getClassPath () {
        return CLASS_PATH;
    }

    private @NotNull Map<String, Supplier<GridSchedulingPolicy>> getSupplierMap () {
        return SUPPLIER_MAP;
    }
}
