package ispd.motor.metricas;

import java.io.Serializable;

/**
 * Cada centro de serviço usado para processamento deve ter um objeto desta classe Responsavel por
 * armazenar o total de processamento realizado em MFlops e segundos
 */
public class MetricasProcessamento implements Serializable {

    private final String id;

    private final String proprietario;

    private final int numeroMaquina;

    /**
     * Armazena o total de processamento realizado em MFlops
     */
    private double MFlopsProcessados = 0;

    /**
     * armazena o total de processamento realizado em segundos
     */
    private double SegundosDeProcessamento = 0;

    public MetricasProcessamento (
        final String id,
        final int numeroMaquina,
        final String proprietario
    ) {
        this.id            = id;
        this.numeroMaquina = numeroMaquina;
        this.proprietario  = proprietario;
    }

    public void incMflopsProcessados (final double MflopsProcessados) {
        this.MFlopsProcessados += MflopsProcessados;
    }

    public void incSegundosDeProcessamento (final double SegundosProcessados) {
        this.SegundosDeProcessamento += SegundosProcessados;
    }

    public double getMFlopsProcessados () {
        return this.MFlopsProcessados;
    }

    public double getSegundosDeProcessamento () {
        return this.SegundosDeProcessamento;
    }

    void setSegundosDeProcessamento (final double d) {
        this.SegundosDeProcessamento = d;
    }

    public String getId () {
        return this.id;
    }

    public String getProprietario () {
        return this.proprietario;
    }

    public int getnumeroMaquina () {
        return this.numeroMaquina;
    }

    void setMflopsProcessados (final double d) {
        this.MFlopsProcessados = d;
    }
}
