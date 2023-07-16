package ispd.motor;

import ispd.motor.filas.Client;
import ispd.motor.filas.servidores.CentroServico;

/**
 * Classe que representa os eventos que alteram o estado do modelo simulado
 */
public class FutureEvent implements Comparable<FutureEvent> {

    public static final int CHEGADA = 1;

    public static final int ATENDIMENTO = 2;

    public static final int SAIDA = 3;

    public static final int ESCALONAR = 4;

    public static final int MENSAGEM = 5;

    public static final int SAIDA_MENSAGEM = 6;

    public static final int ALOCAR_VMS = 7;

    private final double creationTime;

    private final int eventType;

    private final CentroServico resource;

    private final Client client;

    /**
     * Criacao de novo evento
     *
     * @param time
     *     tempo do relógio em que foi criada
     * @param eventType
     *     tipo do evento criado
     * @param resource
     *     servidor que executará o evento
     * @param client
     *     cliente do evento
     */
    public FutureEvent (
        final double time,
        final int eventType,
        final CentroServico resource,
        final Client client
    ) {
        this.creationTime = time;
        this.resource     = resource;
        this.eventType    = eventType;
        this.client       = client;
    }

    /**
     * Comparação necessaria para utilizar PriorityQueue
     *
     * @param o
     *     evento que será comparado
     *
     * @return 0 se valores iguais, um menor que 0 se "o" inferior, e maior que 0 se "o" for maior.
     */
    public int compareTo (final FutureEvent o) {
        return Double.compare(this.creationTime, o.creationTime);
    }

    /**
     * Informa o tipo do evento
     *
     * @return Retorna o tipo do evento de acordo com as constantes da classe
     */
    public int getType () {
        return this.eventType;
    }

    /**
     * Retorna recurso que realiza a ação
     *
     * @return recurso que deve executar ação
     */
    public CentroServico getServidor () {
        return this.resource;
    }

    /**
     * Retorna tarefa alvo da ação
     *
     * @return cliente do evento
     */
    public Client getClient () {
        return this.client;
    }

    public double getCreationTime () {
        return this.creationTime;
    }
}
