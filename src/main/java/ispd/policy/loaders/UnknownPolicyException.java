package ispd.policy.loaders;

import java.text.*;

public class UnknownPolicyException extends RuntimeException {

    private static final String PATTERN = "Unknown policy ''{0}''!";

    public UnknownPolicyException (final String name) {
        super(MessageFormat.format(PATTERN, name));
    }
}
