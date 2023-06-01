package ispd.motor.filas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;

/**
 * Possui listas de todos os icones presentes no modelo utilizado para buscas e
 * para o motor de simulação
 */
public class RedeDeFilas {

    /**
     * Lista dos limites de consumo, em porcentagem, de cada usuário
     */
    private final Map<String, Double>    limiteConsumo;
    /**
     * Todos os mestres existentes no sistema incluindo o front-node dos
     * clusters
     */
    private final List<CS_Processamento> mestres;
    /**
     * Todas as máquinas que não são mestres
     */
    private final List<CS_Maquina>       maquinas;
    /**
     * Todas as conexões
     */
    private final List<CS_Comunicacao>   links;
    /**
     * Todos icones de internet do modelo
     */
    private final List<CS_Internet>      internets;
    /**
     * Mantem lista dos usuarios da rede de filas
     */
    private       List<String>           usuarios;

    /**
     * Constructor which specifies the model of architecture's network of queue
     * used for metric searches and by simulation engine. It is specified the
     * masters, the machines, the links and the internets.
     * <p><br />
     * Using this constructor the power limit for each user is not specified.
     * <strong>Note</strong> that when it is said that the power limit is not
     * specified this does not mean that the power limit for all users is set
     * as default to 0.
     *
     * @param masterList
     *         the master list
     * @param machineList
     *         the machine list
     * @param linkList
     *         the link list
     * @param internetList
     *         the internet list
     *
     * @see #RedeDeFilas(List, List, List, List, Map)
     *         for specify the power limit for each user
     */
    public RedeDeFilas (
            final List<CS_Processamento> masterList, final List<CS_Maquina> machineList,
            final List<CS_Comunicacao> linkList, final List<CS_Internet> internetList
    ) {
        this(masterList, machineList, linkList, internetList, new HashMap<>());
    }

    /**
     * Constructor which specifies the model of architecture's network of queue
     * used for metric searches and by simulation engine. It is specified the
     * masters, the machines, the links, the internets and the power limit for
     * each user.
     *
     * @param masterList
     *         the master list
     * @param machineList
     *         the machine list
     * @param linkList
     *         the link list
     * @param internetList
     *         the internet list
     * @param powerLimitMap
     *         the power limit map, specifying the power limit
     *         for each user in the simulation.
     */
    public RedeDeFilas (
            final List<CS_Processamento> masterList, final List<CS_Maquina> machineList,
            final List<CS_Comunicacao> linkList, final List<CS_Internet> internetList,
            final Map<String, Double> powerLimitMap
    ) {
        this.mestres       = masterList;
        this.maquinas      = machineList;
        this.links         = linkList;
        this.internets     = internetList;
        this.limiteConsumo = powerLimitMap;
    }

    public double averageComputationalPower () {
        return this.maquinas.stream()
                            .mapToDouble(CS_Processamento::getPoderComputacional)
                            .average()
                            .orElse(0.0);
    }

    public List<CS_Internet> getInternets () {
        return this.internets;
    }

    public List<CS_Comunicacao> getLinks () {
        return this.links;
    }

    public List<CS_Maquina> getMaquinas () {
        return this.maquinas;
    }

    public List<CS_Processamento> getMestres () {
        return this.mestres;
    }

    public List<String> getUsuarios () {
        return this.usuarios;
    }

    public void setUsuarios (final List<String> usuarios) {
        this.usuarios = usuarios;
    }

    public Map<String, Double> getLimites () {
        return this.limiteConsumo;
    }
}
