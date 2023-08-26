package ispd.policy.loaders;

import ispd.policy.scheduling.grid.*;
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
        final var map = Map.<String, Supplier<GridSchedulingPolicy>>of();
        return super.loadPolicy(policyName);
    }
}
