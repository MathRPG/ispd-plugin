package ispd.motor.filas;

import java.util.List;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 * Possui listas de todos os icones presentes no modelo utilizado para buscas e
 * para o motor de simulação
 */
public class RedeDeFilasCloud extends RedeDeFilas {

    public  int                   length;
    /**
     * Todas as máquinas que não são mestres
     */
    private List<CS_MaquinaCloud> maquinasCloud;
    /**
     * Mantem a lista de máquinas virtuais
     */
    private List<CS_VirtualMac>   VMs;

    /**
     * Armazena listas com a arquitetura de todo o sistema modelado, utilizado
     * para buscas das métricas e pelo motor de simulação
     *
     * @param mestres
     * @param maquinas
     * @param vms
     * @param links
     * @param internets
     */
    public RedeDeFilasCloud (
            final List<CS_Processamento> mestres, final List<CS_MaquinaCloud> maquinas, final List<CS_VirtualMac> vms,
            final List<CS_Comunicacao> links, final List<CS_Internet> internets
    ) {
        super(mestres, null, links, internets);
        this.maquinasCloud = maquinas;
        this.VMs           = vms;
    }

    public List<CS_MaquinaCloud> getMaquinasCloud () {
        return this.maquinasCloud;
    }

    public void setMaquinasCloud (final List<CS_MaquinaCloud> maquinasCloud) {
        this.maquinasCloud = maquinasCloud;
    }

    public List<CS_VirtualMac> getVMs () {
        return this.VMs;
    }

    public void setVMs (final List<CS_VirtualMac> VMs) {
        this.VMs = VMs;
    }


}
