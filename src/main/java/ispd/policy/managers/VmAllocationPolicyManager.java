package ispd.policy.managers;

import ispd.policy.*;
import java.util.*;

/**
 * Manages storing, retrieving and compiling allocation policies
 */
public class VmAllocationPolicyManager implements PolicyManager {

    /**
     * Allocation policies available by default
     */
    public static final List<String> NATIVE_POLICIES = List.of(
        PolicyManager.NO_POLICY,
        "RoundRobin",
        "FirstFit",
        "FirstFitDecreasing",
        "Volume"
    );
}
