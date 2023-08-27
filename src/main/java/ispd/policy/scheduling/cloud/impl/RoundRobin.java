package ispd.policy.scheduling.cloud.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.cloud.*;
import java.util.*;

/**
 * Implementation of the RoundRobin scheduling algorithm. Hands over the next task on the FIFO
 * queue, for the next resource in a circular queue of resources.
 */
public class RoundRobin extends CloudSchedulingPolicy {

    private ListIterator<Processing> resources = null;

    private LinkedList<Processing> slavesUser = null;

    public RoundRobin () {
        this.tarefas  = new ArrayList<>();
        this.escravos = new LinkedList<>();
    }

    @Override
    public void iniciar () {
        this.slavesUser = new LinkedList<>();
        this.resources  = this.slavesUser.listIterator(0);
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final var destination = (Processing) destino;
        final int index       = this.escravos.indexOf(destination);

        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        final var task      = this.escalonarTarefa();
        final var taskOwner = task.getProprietario();
        this.slavesUser = (LinkedList<Processing>) this.getVMsAdequadas(taskOwner);

        if (this.slavesUser.isEmpty()) {
            this.noAllocatedVms(task);
        } else {
            this.scheduleTask(task);
        }
    }

    @Override
    public Processing escalonarRecurso () {
        if (!this.resources.hasNext()) {
            this.resources = this.slavesUser.listIterator(0);
        }
        return this.resources.next();
    }

    @Override
    public GridTask escalonarTarefa () {
        return this.tarefas.remove(0);
    }

    private void noAllocatedVms (final GridTask task) {
        this.adicionarTarefa(task);
        this.mestre.freeScheduler();
    }

    private void scheduleTask (final GridTask task) {
        final var resource = this.escalonarRecurso();
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        this.mestre.sendTask(task);
    }
}