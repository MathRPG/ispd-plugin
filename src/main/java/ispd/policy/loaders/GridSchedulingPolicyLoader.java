package ispd.policy.loaders;

import ispd.policy.scheduling.grid.*;
import ispd.policy.scheduling.grid.impl.*;
import java.util.*;
import java.util.function.*;

public class GridSchedulingPolicyLoader extends GenericPolicyLoader<GridSchedulingPolicy> {

    private static final String CLASS_PATH = "ispd.policy.scheduling.grid.impl.";

    @Override
    protected String getClassPath () {
        return CLASS_PATH;
    }

    @Override
    public GridSchedulingPolicy loadPolicy (final String policyName) {
        final var map = Map.<String, Supplier<GridSchedulingPolicy>>of(
            "RoundRobin", () -> new RoundRobin(),
            "Workqueue", () -> new Workqueue(),
            "WQR", () -> new WQR(),
            "DynamicFPLTF", () -> new DynamicFPLTF(),
            "HOSEP", () -> new HOSEP(),
            "OSEP", () -> new OSEP(),
            "EHOSEP", () -> new EHOSEP()
        );
        return super.loadPolicy(policyName);
    }
}
