package ispd.motor.filas;

import java.util.ArrayList;
import java.util.List;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasTarefa;

/**
 * Classe que representa o cliente do modelo de filas, ele será atendo pelos
 * centros de serviços Os clientes podem ser: Tarefas
 */
public class Tarefa implements Client {

    //Estados que a tarefa pode estar
    public static final int                    PARADO                 = 1;
    public static final int                    PROCESSANDO            = 2;
    public static final int                    CANCELADO              = 3;
    public static final int                    CONCLUIDO              = 4;
    public static final int                    FALHA                  = 5;
    private final       String                 proprietario;
    private final       String                 aplicacao;
    private final       int                    identificador;
    private final       boolean                copia;
    private final       List<CS_Processamento> historicoProcessamento = new ArrayList<>();
    /**
     * Tamanho do arquivo em Mbits que será enviado para o escravo.
     */
    private final       double                 arquivoEnvio;
    /**
     * Tamanho do arquivo em Mbits que será devolvido para o mestre.
     */
    private final       double                 arquivoRecebimento;
    /**
     * Tamanho em Mflops para processar.
     */
    private final       double                 tamProcessamento;
    /**
     * Local de origem da mensagem/tarefa.
     */
    private final       CentroServico          origem;
    private final       MetricasTarefa         metricas;
    private final       double                 tempoCriacao;
    private final       List<Double>           tempoFinal; //Criando o tempo em que a tarefa acabou.
    private final       List<Double>           tempoInicial; //Criando o tempo em que a tarefa começou a ser executada.
    /**
     * Indica a quantidade de mflops já processados no momento de um bloqueio
     */
    private             double                 mflopsProcessado;
    /**
     * Indica a quantidade de mflops desperdiçados por uma preempção ou cancelamento
     */
    private             double                 mflopsDesperdicados    = 0;
    /**
     * Local de destino da mensagem/tarefa
     */
    private             CentroServico          localProcessamento;
    /**
     * Caminho que o pacote deve percorrer até o destino O destino é o ultimo
     * item desta lista
     */
    private             List<CentroServico>    caminho;
    private             double                 inicioEspera;
    private             int                    estado;
    private             double                 tamComunicacao;

    public Tarefa (
            final int id, final String proprietario, final String aplicacao, final CentroServico origem,
            final double arquivoEnvio, final double tamProcessamento, final double tempoCriacao
    ) {
        this.proprietario       = proprietario;
        this.aplicacao          = aplicacao;
        this.identificador      = id;
        this.copia              = false;
        this.origem             = origem;
        this.tamComunicacao     = arquivoEnvio;
        this.arquivoEnvio       = arquivoEnvio;
        this.arquivoRecebimento = 0;
        this.tamProcessamento   = tamProcessamento;
        this.metricas           = new MetricasTarefa();
        this.tempoCriacao       = tempoCriacao;
        this.estado             = ispd.motor.filas.Tarefa.PARADO;
        this.mflopsProcessado   = 0;
        this.tempoInicial       = new ArrayList<>();
        this.tempoFinal         = new ArrayList<>();
    }

    public Tarefa (
            final int id, final String proprietario, final String aplicacao, final CentroServico origem,
            final double arquivoEnvio, final double arquivoRecebimento, final double tamProcessamento,
            final double tempoCriacao
    ) {
        this.identificador      = id;
        this.proprietario       = proprietario;
        this.aplicacao          = aplicacao;
        this.copia              = false;
        this.origem             = origem;
        this.tamComunicacao     = arquivoEnvio;
        this.arquivoEnvio       = arquivoEnvio;
        this.arquivoRecebimento = arquivoRecebimento;
        this.tamProcessamento   = tamProcessamento;
        this.metricas           = new MetricasTarefa();
        this.tempoCriacao       = tempoCriacao;
        this.estado             = ispd.motor.filas.Tarefa.PARADO;
        this.mflopsProcessado   = 0;
        this.tempoInicial       = new ArrayList<>();
        this.tempoFinal         = new ArrayList<>();
    }

    public Tarefa (final Tarefa tarefa) {
        this.proprietario       = tarefa.proprietario;
        this.aplicacao          = tarefa.aplicacao;
        this.identificador      = tarefa.identificador;
        this.copia              = true;
        this.origem             = tarefa.origem;
        this.tamComunicacao     = tarefa.arquivoEnvio;
        this.arquivoEnvio       = tarefa.arquivoEnvio;
        this.arquivoRecebimento = tarefa.arquivoRecebimento;
        this.tamProcessamento   = tarefa.tamProcessamento;
        this.metricas           = new MetricasTarefa();
        this.tempoCriacao       = tarefa.tempoCriacao;
        this.estado             = ispd.motor.filas.Tarefa.PARADO;
        this.mflopsProcessado   = 0;
        this.tempoInicial       = new ArrayList<>();
        this.tempoFinal         = new ArrayList<>();
    }

    public String getAplicacao () {
        return this.aplicacao;
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
        this.estado       = ispd.motor.filas.Tarefa.PROCESSANDO;
        this.inicioEspera = tempo;
        this.tempoInicial.add(tempo);
        this.historicoProcessamento.add((CS_Processamento) this.localProcessamento);
    }

    public List<CS_Processamento> getHistoricoProcessamento () {
        return this.historicoProcessamento;
    }

    public void finalizarAtendimentoProcessamento (final double tempo) {
        this.estado = ispd.motor.filas.Tarefa.CONCLUIDO;
        this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
        if (this.tempoFinal.size() < this.tempoInicial.size()) {
            this.tempoFinal.add(tempo);
        }
        this.tamComunicacao = this.arquivoRecebimento;
    }

    public double cancelar (final double tempo) {
        if (this.estado == ispd.motor.filas.Tarefa.PARADO || this.estado == ispd.motor.filas.Tarefa.PROCESSANDO) {
            this.estado = ispd.motor.filas.Tarefa.CANCELADO;
            this.metricas.incTempoProcessamento(tempo - this.inicioEspera);
            if (this.tempoFinal.size() < this.tempoInicial.size()) {
                this.tempoFinal.add(tempo);
            }
            return this.inicioEspera;
        } else {
            this.estado = ispd.motor.filas.Tarefa.CANCELADO;
            return tempo;
        }
    }

    public double parar (final double tempo) {
        if (this.estado == ispd.motor.filas.Tarefa.PROCESSANDO) {
            this.estado = ispd.motor.filas.Tarefa.PARADO;
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

    public double getMflopsDesperdicados () {
        return this.mflopsDesperdicados;
    }

    public void incMflopsDesperdicados (final double mflopsDesperdicados) {
        this.mflopsDesperdicados += mflopsDesperdicados;
    }

    public double getCheckPoint () {
        //Se for alterado o tempo de checkpoint, alterar também no métricas
        //Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
        return 0.0;
    }

    public double getArquivoEnvio () {
        return this.arquivoEnvio;
    }
}
