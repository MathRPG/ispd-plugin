package ispd.policy;

import java.util.*;

public interface PolicyManager {

    String NO_POLICY = "---";

    /**
     * Scheduling policies available by default
     */
    List<String> NATIVE_GRID_POLICIES = List.of(
        NO_POLICY,
        "RoundRobin",
        "Workqueue",
        "WQR",
        "DynamicFPLTF",
        "HOSEP",
        "OSEP",
        "EHOSEP"
    );
}
