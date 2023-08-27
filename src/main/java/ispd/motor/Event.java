package ispd.motor;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;

/**
 * Classe que representa os eventos que alteram o estado do modelo simulado
 */
public class Event implements Comparable<Event> {

    private final double creationTime;

    private final EventType eventType;

    private final Service resource;

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
    public Event (
        final double time,
        final EventType eventType,
        final Service resource,
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
    public int compareTo (final Event o) {
        return Double.compare(this.creationTime, o.creationTime);
    }

    /**
     * Informa o tipo do evento
     *
     * @return Retorna o tipo do evento de acordo com as constantes da classe
     */
    public EventType getType () {
        return this.eventType;
    }

    /**
     * Retorna recurso que realiza a ação
     *
     * @return recurso que deve executar ação
     */
    public Service getServidor () {
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
