package ispd.policy.managers;

import ispd.arquivo.xml.*;
import ispd.policy.*;
import java.io.*;
import java.util.*;

/**
 * Manages storing, retrieving and compiling allocation policies
 */
public class VmAllocationPolicyManager implements PolicyManager {

    /**
     * Allocation policies available by default
     */
    public static final List<String> NATIVE_POLICIES = List.of(
        PolicyManager.NO_POLICY,
        "RoundRobin",
        "FirstFit",
        "FirstFitDecreasing",
        "Volume"
    );

    private static final String VM_DIR_PATH =
        String.join(File.separator, "policies", "allocation", "vm");

    private static final File VM_DIRECTORY =
        new File(ConfiguracaoISPD.DIRETORIO_ISPD, VM_DIR_PATH);

    protected String className () {
        return "VmAllocationPolicyManager.class";
    }
}
