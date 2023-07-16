package ispd.policy;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum PolicyConditions {
    ;

    public static final Set<PolicyCondition> ALL =
        Collections.unmodifiableSet(EnumSet.allOf(PolicyCondition.class));

    public static final Set<PolicyCondition> WHILE_MUST_DISTRIBUTE =
        Collections.unmodifiableSet(EnumSet.of(PolicyCondition.WHILE_MUST_DISTRIBUTE));
}
