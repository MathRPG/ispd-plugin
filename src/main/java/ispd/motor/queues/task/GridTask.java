package ispd.motor.queues.task;

import ispd.motor.metrics.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.utils.constants.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Classe que representa o cliente do modelo de filas, ele será atendo pelos centros de serviços Os
 * clientes podem ser: Tarefas
 */
public class GridTask implements Client {

    private final String proprietario;

    private final String aplicacao;

    private final int identificador;

    private final boolean copia;

    private final List<Processing> historicoProcessamento = new ArrayList<>();

    /**
     * Tamanho do arquivo em Mbits que será enviado para o escravo.
     */
    private final double arquivoEnvio;

    /**
     * Tamanho do arquivo em Mbits que será devolvido para o mestre.
     */
    private final double arquivoRecebimento;

    /**
     * Tamanho em Mflops para processar.
     */
    private final double tamProcessamento;

    /**
     * Local de origem da mensagem/tarefa.
     */
    private final Service origem;

    private final Task metricas = new Task();

    private final double tempoCriacao;

    /**
     * Criando o tempo em que a tarefa acabou.
     */
    private final List<Double> tempoFinal = new ArrayList<>();

    /**
     * Criando o tempo em que a tarefa começou a ser executada.
     */
    private final List<Double> tempoInicial = new ArrayList<>();

    /**
     * Indica a quantidade de mflops já processados no momento de um bloqueio
     */
    private double mflopsProcessado = 0.0;

    /**
     * Local de destino da mensagem/tarefa
     */
    private Service localProcessamento = null;

    /**
     * Caminho que o pacote deve percorrer até o destino O destino é o ultimo item desta lista
     */
    private List<Service> caminho = null;

    private double inicioEspera = 0.0;

    private TaskState estado = TaskState.BLOCKED;

    private double tamComunicacao;

    public GridTask (
        final int id, final String proprietario, final String aplicacao, final Service origem,
        final double arquivoEnvio, final double tamProcessamento, final double tempoCriacao
    ) {
        this(
            id,
            proprietario,
            aplicacao,
            origem,
            arquivoEnvio,
            0,
            tamProcessamento,
            tempoCriacao
        );
    }

    public GridTask (
        final int id, final String proprietario, final String aplicacao, final Service origem,
        final double arquivoEnvio, final double arquivoRecebimento, final double tamProcessamento,
        final double tempoCriacao
    ) {
        this.aplicacao          = aplicacao;
        this.arquivoEnvio       = arquivoEnvio;
        this.arquivoRecebimento = arquivoRecebimento;
        this.copia              = false;
        this.identificador      = id;
        this.origem             = origem;
        this.proprietario       = proprietario;
        this.tamComunicacao     = arquivoEnvio;
        this.tamProcessamento   = tamProcessamento;
        this.tempoCriacao       = tempoCriacao;
    }

    public GridTask (final GridTask tarefa) {
        this.aplicacao          = tarefa.aplicacao;
        this.arquivoEnvio       = tarefa.arquivoEnvio;
        this.arquivoRecebimento = tarefa.arquivoRecebimento;
        this.copia              = true;
        this.identificador      = tarefa.identificador;
        this.origem             = tarefa.origem;
        this.proprietario       = tarefa.proprietario;
        this.tamComunicacao     = tarefa.arquivoEnvio;
        this.tamProcessamento   = tarefa.tamProcessamento;
        this.tempoCriacao       = tarefa.tempoCriacao;
    }

    private static String serializeSingleTask (final GridTask task) {
        return StringConstants.TASK_TAG_TEMPLATE.formatted(
            task.identificador, task.tempoCriacao, task.tamProcessamento,
            task.arquivoEnvio, task.proprietario
        );
    }

    public static String serializeTaskCollection (final Collection<? extends GridTask> tasks) {
        return tasks.stream()
            .filter(Predicate.not(GridTask::isCopy))
            .map(GridTask::serializeSingleTask)
            .collect(Collectors.joining());
    }

    public double lastFinalizationTime () {
        return this.tempoFinal.get(this.tempoFinal.size() - 1);
    }

    public double getTamComunicacao () {
        return this.tamComunicacao;
    }

    public double getTamProcessamento () {
        return this.tamProcessamento;
    }

    public double getTimeCriacao () {
        return this.tempoCriacao;
    }

    public Service getOrigem () {
        return this.origem;
    }

    public List<Service> getCaminho () {
        return this.caminho;
    }

    public void setCaminho (final List<Service> caminho) {
        this.caminho = caminho;
    }

    public String getProprietario () {
        return this.proprietario;
    }

    public Service getLocalProcessamento () {
        return this.localProcessamento;
    }

    public void setLocalProcessamento (final Service localProcessamento) {
        this.localProcessamento = localProcessamento;
    }

    public Processing getCSLProcessamento () {
        return (Processing) this.localProcessamento;
    }

    public void iniciarEsperaComunicacao (final double tempo) {
        this.inicioEspera = tempo;
    }

    public void finalizarEsperaComunicacao (final double tempo) {
        this.metricas.incTempoEsperaComu(tempo - this.inicioEspera);
    }

    public void iniciarAtendimentoComunicacao (final double tempo) {
        this.inicioEspera = tempo;
    }

    public void finalizarAtendimentoComunicacao (final double tempo) {
        this.metricas.incTempoComunicacao(tempo - this.inicioEspera);
    }

    public void iniciarEsperaProcessamento (final double tempo) {
        this.inicioEspera = tempo;
    }

    public void finalizarEsperaProcessamento (final double tempo) {
        this.metricas.incTempoEsperaProc(tempo - this.inicioEspera);
    }

    public void iniciarAtendimentoProcessamento (final double tempo) {
        this.estado = TaskState.PROCESSING;
        this.inicioEspera = tempo;
        this.tempoInicial.add(tempo);
        this.historicoProcessamento.add((Processing) this.localProcessamento);
    }

    public List<Processing> getHistoricoProcessamento () {
        return this.historicoProcessamento;
    }

    public void finalizarAtendimentoProcessamento (final double tempo) {
        this.estado = TaskState.DONE;
        this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
        if (this.tempoFinal.size() < this.tempoInicial.size()) {
            this.tempoFinal.add(tempo);
        }
        this.tamComunicacao = this.arquivoRecebimento;
    }

    public double cancelar (final double tempo) {
        if (this.estado == TaskState.BLOCKED || this.estado == TaskState.PROCESSING) {
            this.estado = TaskState.CANCELLED;
            this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
            if (this.tempoFinal.size() < this.tempoInicial.size()) {
                this.tempoFinal.add(tempo);
            }
            return this.inicioEspera;
        } else {
            this.estado = TaskState.CANCELLED;
            return tempo;
        }
    }

    public double parar (final double tempo) {
        if (this.estado == TaskState.PROCESSING) {
            this.estado = TaskState.BLOCKED;
            this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
            if (this.tempoFinal.size() < this.tempoInicial.size()) {
                this.tempoFinal.add(tempo);
            }
            return this.inicioEspera;
        } else {
            return tempo;
        }
    }

    public void calcEficiencia (final double capacidadeRecebida) {
        this.metricas.calcEficiencia(capacidadeRecebida, this.tamProcessamento);
    }

    public List<Double> getTempoInicial () {
        return this.tempoInicial;
    }

    public List<Double> getTempoFinal () {
        return this.tempoFinal;
    }

    public Task getMetricas () {
        return this.metricas;
    }

    public TaskState getEstado () {
        return this.estado;
    }

    public void setEstado (final TaskState estado) {
        this.estado = estado;
    }

    public int getIdentificador () {
        return this.identificador;
    }

    public boolean isCopy () {
        return this.copia;
    }

    public boolean isCopyOf (final GridTask tarefa) {
        return this.identificador == tarefa.identificador && !this.equals(tarefa);
    }

    public double getMflopsProcessado () {
        return this.mflopsProcessado;
    }

    public void setMflopsProcessado (final double mflopsProcessado) {
        this.mflopsProcessado = mflopsProcessado;
    }

    public double getArquivoEnvio () {
        return this.arquivoEnvio;
    }
}
