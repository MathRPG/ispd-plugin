package ispd.policy.managers;

import ispd.policy.*;
import java.util.*;

/**
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class CloudSchedulingPolicyManager implements PolicyManager {

    public static final List<String> NATIVE_POLICIES =
        List.of(PolicyManager.NO_POLICY, "RoundRobin");
}
