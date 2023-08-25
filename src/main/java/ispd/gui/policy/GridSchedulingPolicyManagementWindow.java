package ispd.gui.policy;

import ispd.policy.managers.*;

public class GridSchedulingPolicyManagementWindow extends GenericPolicyManagementWindow {

    public GridSchedulingPolicyManagementWindow (final GridSchedulingPolicyManager manager) {
        super(manager);
    }

    @Override
    protected String getButtonOpenTooltip () {
        return "Opens an existing policy";
    }

    @Override
    protected String getButtonNewTooltip () {
        return "Creates a new policy";
    }

    @Override
    protected String getPolicyListTitle () {
        return "Policies";
    }

    @Override
    protected String getWindowTitle () {
        return "Manage Grid Scheduling Policies";
    }
}