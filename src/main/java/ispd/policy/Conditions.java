package ispd.policy;

import java.util.*;

public enum Conditions {
    ;

    public static final Set<Condition> ALL =
        Collections.unmodifiableSet(EnumSet.allOf(Condition.class));

    public static final Set<Condition> WHILE_MUST_DISTRIBUTE =
        Collections.unmodifiableSet(EnumSet.of(Condition.WHILE_MUST_DISTRIBUTE));
}
