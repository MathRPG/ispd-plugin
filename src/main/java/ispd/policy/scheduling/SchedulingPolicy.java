package ispd.policy.scheduling;

import ispd.motor.metrics.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.policy.*;
import java.util.*;

public abstract class SchedulingPolicy <T extends SchedulingMaster> extends Policy<T> {

    protected List<GridTask> tarefas = null;

    protected User metricaUsuarios = null;

    protected List<List> filaEscravo = null;

    public abstract GridTask escalonarTarefa ();

    public void addTarefaConcluida (final GridTask tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }

    public User getMetricaUsuarios () {
        return this.metricaUsuarios;
    }

    public void setMetricaUsuarios (final User metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public List<GridTask> getFilaTarefas () {
        return this.tarefas;
    }

    public void resultadoAtualizar (final Request request) {
        final int index = this.escravos.indexOf(request.getOrigem());
        this.filaEscravo.set(index, request.getFilaEscravo());
    }

    public void adicionarTarefa (final GridTask tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        this.tarefas.add(tarefa);
    }
}
