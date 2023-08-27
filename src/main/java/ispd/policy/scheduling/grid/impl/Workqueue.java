package ispd.policy.scheduling.grid.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

public class Workqueue extends GridSchedulingPolicy {

    private final LinkedList<GridTask> ultimaTarefaConcluida = new LinkedList<>();

    private List<GridTask> tarefaEnviada = null;

    public Workqueue () {
        this.tarefas  = new ArrayList<>();
        this.escravos = new ArrayList<>();
    }

    @Override
    public void iniciar () {
        this.tarefaEnviada = new ArrayList<>(this.escravos.size());
        for (int i = 0; i < this.escravos.size(); i++) {
            this.tarefaEnviada.add(null);
        }
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        final Processing rec = this.escalonarRecurso();

        if (rec == null) {
            return;
        }

        final GridTask trf = this.escalonarTarefa();

        if (trf == null) {
            return;
        }

        this.tarefaEnviada.set(this.escravos.indexOf(rec), trf);
        if (!this.ultimaTarefaConcluida.isEmpty()) {
            this.ultimaTarefaConcluida.removeLast();
        }
        trf.setLocalProcessamento(rec);
        trf.setCaminho(this.escalonarRota(rec));
        this.mestre.sendTask(trf);
    }

    @Override
    public Processing escalonarRecurso () {
        if (!this.ultimaTarefaConcluida.isEmpty() && !this.ultimaTarefaConcluida
            .getLast()
            .isCopy()) {
            final int index =
                this.tarefaEnviada.indexOf(this.ultimaTarefaConcluida.getLast());
            return this.escravos.get(index);
        } else {
            for (int i = 0; i < this.tarefaEnviada.size(); i++) {
                if (this.tarefaEnviada.get(i) == null) {
                    return this.escravos.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public GridTask escalonarTarefa () {
        if (!this.tarefas.isEmpty()) {
            return this.tarefas.remove(0);
        }
        return null;
    }

    @Override
    public void addTarefaConcluida (final GridTask tarefa) {
        super.addTarefaConcluida(tarefa);
        this.ultimaTarefaConcluida.add(tarefa);
        if (!this.tarefas.isEmpty()) {
            this.mestre.executeScheduling();
        }
    }

    @Override
    public void adicionarTarefa (final GridTask tarefa) {
        super.adicionarTarefa(tarefa);
        if (this.tarefaEnviada.contains(tarefa)) {
            final int index = this.tarefaEnviada.indexOf(tarefa);
            this.tarefaEnviada.set(index, null);
            this.mestre.executeScheduling();
        }
    }
}
