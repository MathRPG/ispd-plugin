package ispd.policy.managers;

import ispd.policy.*;
import ispd.utils.constants.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public abstract class FilePolicyManager implements PolicyManager {

    private static final String JAR_PREFIX = "jar:";

    private final ArrayList<String> policies = new ArrayList<>();

    protected FilePolicyManager () {
        this.initialize();
    }

    public static void createDirectory (final File dir)
        throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    private static void severeLog (final Throwable e) {
        Logger.getLogger(FilePolicyManager.class.getName()).log(Level.SEVERE, null, e);
    }

    private static String removeDotClassSuffix (final String s) {
        return removeSuffix(s, FileExtensions.JAVA_CLASS);
    }

    private static String removeSuffix (final String str, final String suffix) {
        return str.substring(0, str.length() - suffix.length());
    }

    protected abstract String packageName ();

    protected abstract String className ();

    protected abstract File directory ();

    /**
     * Lists all available allocation policies.
     *
     * @return {@code ArrayList} with all allocation policies' names
     */
    @Override
    public ArrayList<String> listar () {
        return this.policies;
    }

    private void initialize () {
    }

    private void loadPoliciesFromFoundDotClassFiles () {
        final FilenameFilter f = (b, name) -> name.endsWith(FileExtensions.JAVA_CLASS);

        /*
         * {@link File#list()} returns {@code null} on I/O error
         * (or if the given {@link File} is not a directory that exists,
         * but that situation has already been accounted for).
         */
        final var dotClassFiles = Objects.requireNonNull(this.directory().list(f));

        Arrays.stream(dotClassFiles)
            .map(FilePolicyManager::removeDotClassSuffix)
            .forEach(this.policies::add);
    }

    private boolean isExecutingFromJar () {
        return this.getExecutableName().startsWith(JAR_PREFIX);
    }

    private String getExecutableName () {
        return Objects.requireNonNull(this.getClass().getResource(this.className())).toString();
    }
}
