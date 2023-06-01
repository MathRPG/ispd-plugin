package ispd.policy.allocation;

import java.util.Set;

import ispd.policy.PolicyCondition;
import ispd.policy.PolicyMaster;

public interface AllocationMaster extends PolicyMaster {

    void executeAllocation ();

    Set<PolicyCondition> getAllocationConditions ();

    void setAllocationConditions (Set<PolicyCondition> tipo);
}
