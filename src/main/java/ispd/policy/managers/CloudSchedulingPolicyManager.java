package ispd.policy.managers;

import ispd.arquivo.xml.*;
import ispd.policy.*;
import java.io.*;
import java.util.*;

/**
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class CloudSchedulingPolicyManager extends FilePolicyManager {

    public static final List<String> NATIVE_POLICIES =
        List.of(PolicyManager.NO_POLICY, "RoundRobin");

    private static final String CLOUD_DIR_PATH =
        String.join(File.separator, "policies", "scheduling", "cloud");

    private static final File CLOUD_DIRECTORY =
        new File(ConfiguracaoISPD.DIRETORIO_ISPD, CLOUD_DIR_PATH);

    protected String className () {
        return "CloudSchedulingPolicyManager.class";
    }
}
