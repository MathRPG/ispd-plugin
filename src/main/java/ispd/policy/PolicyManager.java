package ispd.policy;

import java.util.*;

public interface PolicyManager {

    String NO_POLICY = "---";

    /**
     * Lists all available allocation policies.
     *
     * @return {@code ArrayList} with all allocation policies' names
     */
    default ArrayList<String> listar () {
        return new ArrayList<>();
    }
}
