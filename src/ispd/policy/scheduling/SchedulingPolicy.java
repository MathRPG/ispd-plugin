package ispd.policy.scheduling;

import java.util.List;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.policy.Policy;

public abstract class SchedulingPolicy <T extends SchedulingMaster> extends Policy<T> {

    protected List<Tarefa>     tarefas         = null;
    protected MetricasUsuarios metricaUsuarios = null;
    protected List<List>       filaEscravo     = null;

    public abstract Tarefa escalonarTarefa ();

    public void addTarefaConcluida (final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }

    public MetricasUsuarios getMetricaUsuarios () {
        return this.metricaUsuarios;
    }

    public void setMetricaUsuarios (final MetricasUsuarios metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public List<Tarefa> getFilaTarefas () {
        return this.tarefas;
    }

    public void resultadoAtualizar (final Mensagem mensagem) {
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.filaEscravo.set(index, mensagem.getFilaEscravo());
    }

    public void adicionarTarefa (final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        this.tarefas.add(tarefa);
    }
}
