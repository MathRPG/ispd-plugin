package ispd.motor.metricas;

import java.io.Serializable;

/**
 * Cada centro de serviço usado para conexão deve ter um objeto desta classe
 * Responsavel por armazenar o total de comunicação realizada em Mbits e segundos
 */
public class MetricasComunicacao implements Serializable {

    /**
     * Armazena o total de comunicação realizada em Mbits
     */
    private double MbitsTransmitidos;
    /**
     * Armazena o total de comunicação realizada em segundos
     */
    private double SegundosDeTransmissao;
    private String id;

    public MetricasComunicacao (String id) {
        this.id                    = id;
        this.MbitsTransmitidos     = 0;
        this.SegundosDeTransmissao = 0;
    }

    public void incMbitsTransmitidos (double MbitsTransmitidos) {
        this.MbitsTransmitidos += MbitsTransmitidos;
    }

    public void incSegundosDeTransmissao (double SegundosDeTransmissao) {
        this.SegundosDeTransmissao += SegundosDeTransmissao;
    }

    public double getMbitsTransmitidos () {
        return MbitsTransmitidos;
    }

    void setMbitsTransmitidos (double d) {
        this.MbitsTransmitidos = d;
    }

    public double getSegundosDeTransmissao () {
        return SegundosDeTransmissao;
    }

    void setSegundosDeTransmissao (double d) {
        this.SegundosDeTransmissao = d;
    }

    public String getId () {
        return id;
    }
}
