package ispd.policy;

import java.util.*;

public interface PolicyManager {

    String NO_POLICY = "---";

    /**
     * Allocation policies available by default
     */
    List<String> NATIVE_VM_POLICIES = List.of(
        NO_POLICY,
        "RoundRobin",
        "FirstFit",
        "FirstFitDecreasing",
        "Volume"
    );

    List<String> NATIVE_CLOUD_POLICIES = List.of(NO_POLICY, "RoundRobin");

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
