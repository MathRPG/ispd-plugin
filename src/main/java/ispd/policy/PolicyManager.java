package ispd.policy;

import java.util.*;

public interface PolicyManager {

    String NO_POLICY = "---";

    /**
     * Allocation policies available by default.
     */
    List<String> NATIVE_VM_POLICIES = List.of(
        NO_POLICY,
        "RoundRobin",
        "FirstFit",
        "FirstFitDecreasing",
        "Volume"
    );

    /**
     * Cloud scheduling policies available by default.
     */
    List<String> NATIVE_CLOUD_POLICIES = List.of(NO_POLICY, "RoundRobin");

    /**
     * Grid scheduling policies available by default.
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
