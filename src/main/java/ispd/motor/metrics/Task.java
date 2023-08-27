package ispd.motor.metrics;

public class Task {

    /**
     * Recebe tempo total que a tarefa permaneceu em um fila de um recurso de computação
     */
    private double tempoEsperaProc = 0.0;

    /**
     * Recebe tempo total que a tarefa permaneceu em um fila de um recurso de comunicação
     */
    private double tempoEsperaComu = 0.0;

    /**
     * Recebe tempo total que a tarefa gastou sendo computada no modelo
     */
    private double tempoProcessamento = 0.0;

    /**
     * Recebe tempo total que a tarefa gastou sendo transferida na rede modelada
     */
    private double tempoComunicacao = 0.0;

    private double eficiencia = 0.0;

    public void incTempoComunicacao (final double tempoComunicacao) {
        this.tempoComunicacao += tempoComunicacao;
    }

    public void incTempoEsperaComu (final double tempoEsperaComu) {
        this.tempoEsperaComu += tempoEsperaComu;
    }

    public void incTempoEsperaProc (final double tempoEsperaProc) {
        this.tempoEsperaProc += tempoEsperaProc;
    }

    public void incTempoProcessamento (final double tempoProcessamento) {
        this.tempoProcessamento += tempoProcessamento;
    }

    public void calcEficiencia (final double capacidadeRecebida, final double tamanhoTarefa) {
        this.eficiencia = capacidadeRecebida / (tamanhoTarefa * this.tempoProcessamento);
    }

    public double getTempoComunicacao () {
        return this.tempoComunicacao;
    }

    public double getTempoEsperaComu () {
        return this.tempoEsperaComu;
    }

    public double getTempoEsperaProc () {
        return this.tempoEsperaProc;
    }

    public double getTempoProcessamento () {
        return this.tempoProcessamento;
    }

    public double getEficiencia () {
        return this.eficiencia;
    }
}
