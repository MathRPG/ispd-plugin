package ispd.policy;

import java.util.*;

public interface PolicyLoader <T extends Policy<?>> {

    String NO_POLICY = "---";

    /**
     * Cloud scheduling policies available by default.
     */
    List<String> NATIVE_CLOUD_POLICIES = List.of(NO_POLICY, "RoundRobin");

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

    T loadPolicy (String policyName);
}
