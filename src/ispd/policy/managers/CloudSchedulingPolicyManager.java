package ispd.policy.managers;

import java.io.File;
import java.util.List;

import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.policy.PolicyManager;

/**
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class CloudSchedulingPolicyManager extends FilePolicyManager {

    public static final  List<String> NATIVE_POLICIES = List.of(PolicyManager.NO_POLICY, "RoundRobin");
    private static final String       CLOUD_DIR_PATH  = String.join(File.separator, "policies", "scheduling", "cloud");
    private static final File         CLOUD_DIRECTORY =
            new File(ConfiguracaoISPD.DIRETORIO_ISPD, CloudSchedulingPolicyManager.CLOUD_DIR_PATH);

    @Override
    public File directory () {
        return CloudSchedulingPolicyManager.CLOUD_DIRECTORY;
    }

    @Override
    protected String packageName () {
        return "escalonadorCloud";
    }

    @Override
    protected String className () {
        return "CloudSchedulingPolicyManager.class";
    }

    protected String getTemplate () {
        //language=JAVA
        return """
               package ispd.policy.externo;
                               
               import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;
               import ispd.motor.filas.Tarefa;
               import ispd.motor.filas.servidores.CS_Processamento;
               import ispd.motor.filas.servidores.CentroServico;
                               
               import java.util.List;
                               
               public class __POLICY_NAME__ extends CloudSchedulingPolicy {
                   @Override
                   public void iniciar() {
                       throw new UnsupportedOperationException("Not supported yet.");
                   }
                               
                   @Override
                   public List<CentroServico> escalonarRota(final CentroServico destino) {
                       throw new UnsupportedOperationException("Not supported yet.");
                   }
                               
                   @Override
                   public void escalonar() {
                       throw new UnsupportedOperationException("Not supported yet.");
                   }
                               
                   @Override
                   public CS_Processamento escalonarRecurso() {
                       throw new UnsupportedOperationException("Not supported yet.");
                   }
                               
                   @Override
                   public Tarefa escalonarTarefa() {
                       throw new UnsupportedOperationException("Not supported yet.");
                   }
               }
               """;
    }
}
