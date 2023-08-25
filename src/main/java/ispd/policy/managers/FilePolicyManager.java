package ispd.policy.managers;

import ispd.policy.*;
import ispd.policy.managers.util.*;
import ispd.utils.constants.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public abstract class FilePolicyManager implements PolicyManager {

    private static final String JAR_PREFIX = "jar:";

    private static final String POLICY_NAME_REPL = "__POLICY_NAME__";

    private final ArrayList<String> policies = new ArrayList<>();

    private final List<String> addedPolicies = new ArrayList<>();

    private final List<String> removedPolicies = new ArrayList<>();

    protected FilePolicyManager () {
        this.initialize();
    }

    public static void createDirectory (final File dir)
        throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    public static void severeLog (final Throwable e) {
        Logger.getLogger(FilePolicyManager.class.getName()).log(Level.SEVERE, null, e);
    }

    private static String removeDotClassSuffix (final String s) {
        return removeSuffix(s, FileExtensions.JAVA_CLASS);
    }

    private static String removeSuffix (final String str, final String suffix) {
        return str.substring(0, str.length() - suffix.length());
    }

    private static String compile (final File target) {
        return new CompilationHelper(target).compile();
    }

    private static boolean canDeleteFile (final File classFile) {
        return classFile.exists() && classFile.delete();
    }

    private static void transferFileContents (final File src, final File dest) {
        if (src.getPath().equals(dest.getPath())) {
            return;
        }

        try (
            final var srcFs = new FileInputStream(src);
            final var destFs = new FileOutputStream(dest)
        ) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            severeLog(ex);
        }
    }

    protected abstract String packageName ();

    protected abstract String className ();

    protected abstract String getTemplate ();

    /**
     * Lists all available allocation policies.
     *
     * @return {@code ArrayList} with all allocation policies' names
     */
    @Override
    public ArrayList<String> listar () {
        return this.policies;
    }

    @Override
    public List listarAdicionados () {
        return this.addedPolicies;
    }

    @Override
    public List listarRemovidos () {
        return this.removedPolicies;
    }

    private void initialize () {
        if (this.directory().exists()) {
            this.loadPoliciesFromFoundDotClassFiles();
            return;
        }

        try {
            createDirectory(this.directory());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (this.isExecutingFromJar()) {
            try {
                new JarExtractor(this.packageName()).extractDirsFromJar();
            } catch (final IOException e) {
                severeLog(e);
            }
        }
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

    private boolean checkIfDotClassExists (final String policyName) {
        return this.policyDotClassFile(policyName).exists();
    }

    private void addPolicy (final String policyName) {
        if (this.policies.contains(policyName)) {
            return;
        }

        this.policies.add(policyName);
        this.addedPolicies.add(policyName);
    }

    private File policyDotClassFile (final String policyName) {
        return this.fileWithExtension(policyName, FileExtensions.JAVA_CLASS);
    }

    private void removePolicy (final String policyName) {
        if (!this.policies.contains(policyName)) {
            return;
        }

        this.policies.remove(policyName);
        this.removedPolicies.add(policyName);
    }

    private File policyJavaFile (final String name) {
        return this.fileWithExtension(name, FileExtensions.JAVA_SOURCE);
    }

    private File fileWithExtension (final String policyName, final String ext) {
        return new File(this.directory(), policyName + ext);
    }
}
