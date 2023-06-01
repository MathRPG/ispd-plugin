package ispd.motor.metricas;

import java.io.Serializable;

/**
 * Cada centro de serviço usado para conexão deve ter um objeto desta classe
 * Responsavel por armazenar o total de comunicação realizada em Mbits e segundos
 */
public class MetricasComunicacao implements Serializable {

    private final String id;
    /**
     * Armazena o total de comunicação realizada em Mbits
     */
    private       double MbitsTransmitidos;
    /**
     * Armazena o total de comunicação realizada em segundos
     */
    private       double SegundosDeTransmissao;

    public MetricasComunicacao (final String id) {
        this.id                    = id;
        this.MbitsTransmitidos     = 0;
        this.SegundosDeTransmissao = 0;
    }

    public void incMbitsTransmitidos (final double MbitsTransmitidos) {
        this.MbitsTransmitidos += MbitsTransmitidos;
    }

    public void incSegundosDeTransmissao (final double SegundosDeTransmissao) {
        this.SegundosDeTransmissao += SegundosDeTransmissao;
    }

    public double getMbitsTransmitidos () {
        return this.MbitsTransmitidos;
    }

    void setMbitsTransmitidos (final double d) {
        this.MbitsTransmitidos = d;
    }

    public double getSegundosDeTransmissao () {
        return this.SegundosDeTransmissao;
    }

    void setSegundosDeTransmissao (final double d) {
        this.SegundosDeTransmissao = d;
    }

    public String getId () {
        return this.id;
    }
}
