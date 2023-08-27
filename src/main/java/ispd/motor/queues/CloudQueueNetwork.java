package ispd.motor.queues;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import java.util.*;

/**
 * Possui listas de todos os icones presentes no modelo utilizado para buscas e para o motor de
 * simulação
 */
public class CloudQueueNetwork extends GridQueueNetwork {

    /**
     * Todas as máquinas que não são mestres
     */
    private final List<CloudMachine> maquinasCloud;

    /**
     * Mantem a lista de máquinas virtuais
     */
    private final List<VirtualMachine> VMs;

    /**
     * Armazena listas com a arquitetura de todo o sistema modelado, utilizado para buscas das
     * métricas e pelo motor de simulação
     */
    public CloudQueueNetwork (
        final List<Processing> mestres,
        final List<CloudMachine> maquinas,
        final List<VirtualMachine> vms,
        final List<Communication> links,
        final List<Internet> internets
    ) {
        super(mestres, null, links, internets);
        this.maquinasCloud = maquinas;
        this.VMs           = vms;
    }

    public List<CloudMachine> getMaquinasCloud () {
        return this.maquinasCloud;
    }

    public List<VirtualMachine> getVMs () {
        return this.VMs;
    }
}
