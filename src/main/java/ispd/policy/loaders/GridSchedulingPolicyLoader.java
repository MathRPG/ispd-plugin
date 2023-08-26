package ispd.policy.loaders;

import ispd.policy.scheduling.grid.*;

public class GridSchedulingPolicyLoader extends GenericPolicyLoader<GridSchedulingPolicy> {

    private static final String CLASS_PATH = "ispd.policy.scheduling.grid.impl.";

    @Override
    protected String getClassPath () {
        return CLASS_PATH;
    }

    @Override
    public GridSchedulingPolicy loadPolicy (final String policyName) {
        return super.loadPolicy(policyName);
    }
}
