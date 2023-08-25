package ispd.policy.managers;

import ispd.policy.*;
import java.util.*;

public abstract class FilePolicyManager implements PolicyManager {

    private final ArrayList<String> policies = new ArrayList<>();

    protected FilePolicyManager () {
    }

    /**
     * Lists all available allocation policies.
     *
     * @return {@code ArrayList} with all allocation policies' names
     */
    @Override
    public ArrayList<String> listar () {
        return this.policies;
    }
}
