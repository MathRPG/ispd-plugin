package ispd.policy.managers;

import ispd.policy.*;
import java.util.*;

/**
 * Manages storing, retrieving and compiling scheduling policies
 */
public class GridSchedulingPolicyManager implements PolicyManager {

    /**
     * Scheduling policies available by default
     */
    public static final List<String> NATIVE_POLICIES = List.of(
        PolicyManager.NO_POLICY,
        "RoundRobin",
        "Workqueue",
        "WQR",
        "DynamicFPLTF",
        "HOSEP",
        "OSEP",
        "EHOSEP"
    );
}
