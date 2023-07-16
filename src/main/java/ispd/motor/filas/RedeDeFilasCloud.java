package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.List;

/**
 * Possui listas de todos os icones presentes no modelo utilizado para buscas e para o motor de
 * simulação
 */
public class RedeDeFilasCloud extends RedeDeFilas {

    /**
     * Todas as máquinas que não são mestres
     */
    private final List<CS_MaquinaCloud> maquinasCloud;

    /**
     * Mantem a lista de máquinas virtuais
     */
    private final List<CS_VirtualMac> VMs;

    /**
     * Armazena listas com a arquitetura de todo o sistema modelado, utilizado para buscas das
     * métricas e pelo motor de simulação
     */
    public RedeDeFilasCloud (
        final List<CS_Processamento> mestres,
        final List<CS_MaquinaCloud> maquinas,
        final List<CS_VirtualMac> vms,
        final List<CS_Comunicacao> links,
        final List<CS_Internet> internets
    ) {
        super(mestres, null, links, internets);
        this.maquinasCloud = maquinas;
        this.VMs           = vms;
    }

    public List<CS_MaquinaCloud> getMaquinasCloud () {
        return this.maquinasCloud;
    }

    public List<CS_VirtualMac> getVMs () {
        return this.VMs;
    }
}
