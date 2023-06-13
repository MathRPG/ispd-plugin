package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasTarefa;
import ispd.utils.constants.StringConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Classe que representa o cliente do modelo de filas, ele será atendo pelos centros de serviços Os
 * clientes podem ser: Tarefas
 */
public class Tarefa implements Client {

    //Estados que a tarefa pode estar
    public static final int PARADO = 1;

    public static final int PROCESSANDO = 2;

    public static final int CANCELADO = 3;

    public static final int CONCLUIDO = 4;

    public static final int FALHA = 5;

    private final String proprietario;

    private final String aplicacao;

    private final int identificador;

    private final boolean copia;

    private final List<CS_Processamento> historicoProcessamento = new ArrayList<>();

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
    private final CentroServico origem;

    private final MetricasTarefa metricas = new MetricasTarefa();

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
    private CentroServico localProcessamento = null;

    /**
     * Caminho que o pacote deve percorrer até o destino O destino é o ultimo item desta lista
     */
    private List<CentroServico> caminho = null;

    private double inicioEspera = 0.0;

    private int estado = Tarefa.PARADO;

    private double tamComunicacao;

    public Tarefa (
        final int id, final String proprietario, final String aplicacao, final CentroServico origem,
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

    public Tarefa (
        final int id, final String proprietario, final String aplicacao, final CentroServico origem,
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

    public Tarefa (final Tarefa tarefa) {
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

    private static String serializeSingleTask (final Tarefa task) {
        return StringConstants.TASK_TAG_TEMPLATE.formatted(
            task.identificador, task.tempoCriacao, task.tamProcessamento,
            task.arquivoEnvio, task.proprietario
        );
    }

    public static String serializeTaskCollection (final Collection<? extends Tarefa> tasks) {
        return tasks.stream()
            .filter(Predicate.not(Tarefa::isCopy))
            .map(Tarefa::serializeSingleTask)
            .collect(Collectors.joining());
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

    public CentroServico getOrigem () {
        return this.origem;
    }

    public List<CentroServico> getCaminho () {
        return this.caminho;
    }

    public void setCaminho (final List<CentroServico> caminho) {
        this.caminho = caminho;
    }

    public String getProprietario () {
        return this.proprietario;
    }

    public CentroServico getLocalProcessamento () {
        return this.localProcessamento;
    }

    public void setLocalProcessamento (final CentroServico localProcessamento) {
        this.localProcessamento = localProcessamento;
    }

    public CS_Processamento getCSLProcessamento () {
        return (CS_Processamento) this.localProcessamento;
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
        this.estado       = Tarefa.PROCESSANDO;
        this.inicioEspera = tempo;
        this.tempoInicial.add(tempo);
        this.historicoProcessamento.add((CS_Processamento) this.localProcessamento);
    }

    public List<CS_Processamento> getHistoricoProcessamento () {
        return this.historicoProcessamento;
    }

    public void finalizarAtendimentoProcessamento (final double tempo) {
        this.estado = Tarefa.CONCLUIDO;
        this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
        if (this.tempoFinal.size() < this.tempoInicial.size()) {
            this.tempoFinal.add(tempo);
        }
        this.tamComunicacao = this.arquivoRecebimento;
    }

    public double cancelar (final double tempo) {
        if (this.estado == Tarefa.PARADO || this.estado == Tarefa.PROCESSANDO) {
            this.estado = Tarefa.CANCELADO;
            this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
            if (this.tempoFinal.size() < this.tempoInicial.size()) {
                this.tempoFinal.add(tempo);
            }
            return this.inicioEspera;
        } else {
            this.estado = Tarefa.CANCELADO;
            return tempo;
        }
    }

    public double parar (final double tempo) {
        if (this.estado == Tarefa.PROCESSANDO) {
            this.estado = Tarefa.PARADO;
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

    public MetricasTarefa getMetricas () {
        return this.metricas;
    }

    public int getEstado () {
        return this.estado;
    }

    public void setEstado (final int estado) {
        this.estado = estado;
    }

    public int getIdentificador () {
        return this.identificador;
    }

    public boolean isCopy () {
        return this.copia;
    }

    public boolean isCopyOf (final Tarefa tarefa) {
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
