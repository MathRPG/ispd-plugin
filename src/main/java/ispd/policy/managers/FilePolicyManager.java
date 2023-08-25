package ispd.policy.managers;

import ispd.policy.*;
import java.io.*;
import java.util.*;

public abstract class FilePolicyManager implements PolicyManager {

    private static final String JAR_PREFIX = "jar:";

    private final ArrayList<String> policies = new ArrayList<>();

    protected FilePolicyManager () {
    }

    public static void createDirectory (final File dir)
        throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    protected abstract String className ();

    /**
     * Lists all available allocation policies.
     *
     * @return {@code ArrayList} with all allocation policies' names
     */
    @Override
    public ArrayList<String> listar () {
        return this.policies;
    }

    private boolean isExecutingFromJar () {
        return this.getExecutableName().startsWith(JAR_PREFIX);
    }

    private String getExecutableName () {
        return Objects.requireNonNull(this.getClass().getResource(this.className())).toString();
    }
}
