package ispd.policy.managers;

import ispd.arquivo.xml.*;
import ispd.policy.*;
import java.io.*;
import java.util.*;

/**
 * Manages storing, retrieving and compiling scheduling policies
 */
public class GridSchedulingPolicyManager extends FilePolicyManager {

    /**
     * Scheduling policies available by default
     */
    public static final List<String> NATIVE_POLICIES = List.of(
        PolicyManager.NO_POLICY,
        "RoundRobin",
        "Workqueue",
        "WQR",
        "DynamicFPLTF",
        "HOSEP",
        "OSEP",
        "EHOSEP"
    );

    private static final String GRID_DIR_PATH =
        String.join(File.separator, "policies", "scheduling", "grid");

    private static final File GRID_DIRECTORY =
        new File(ConfiguracaoISPD.DIRETORIO_ISPD, GRID_DIR_PATH);

    @Override
    public File directory () {
        return GRID_DIRECTORY;
    }

    @Override
    protected String packageName () {
        return "escalonador";
    }

    @Override
    protected String className () {
        return "GridSchedulingPolicyManager.class";
    }
}
