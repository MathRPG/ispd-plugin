package ispd.policy.allocation;

import ispd.policy.*;
import java.util.*;

public interface AllocationMaster extends Simulable {

    void executeAllocation ();

    Set<Condition> getAllocationConditions ();

    void setAllocationConditions (Set<Condition> tipo);
}
