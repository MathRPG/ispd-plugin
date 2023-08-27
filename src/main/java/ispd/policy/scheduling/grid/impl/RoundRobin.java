package ispd.policy.scheduling.grid.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

/**
 * Implementation of the RoundRobin scheduling algorithm.<br> Hands over the next task on the FIFO
 * queue, for the next resource in a circular queue of resources.
 */
public class RoundRobin extends GridSchedulingPolicy {

    private ListIterator<Processing> resources = null;

    public RoundRobin () {
        this.tarefas  = new ArrayList<>(0);
        this.escravos = new LinkedList<>();
    }

    @Override
    public void iniciar () {
        this.resources = this.escravos.listIterator(0);
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        final var task     = this.escalonarTarefa();
        final var resource = this.escalonarRecurso();
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        this.mestre.sendTask(task);
    }

    @Override
    public Processing escalonarRecurso () {
        if (!this.resources.hasNext()) {
            this.resources = this.escravos.listIterator(0);
        }
        return this.resources.next();
    }

    @Override
    public GridTask escalonarTarefa () {
        return this.tarefas.remove(0);
    }
}