package ispd.motor.filas.servidores;

import ispd.motor.metricas.MetricasComunicacao;

/**
 * Classe abstrata que representa os servidores de comunicação do modelo de fila, Esta classe possui
 * atributos referente a este ripo de servidor, e indica como calcular o tempo gasto para transmitir
 * uma tarefa.
 */
public abstract class CS_Comunicacao extends CentroServico {

    private final double ocupacao;

    private final double latencia;

    private final MetricasComunicacao metrica;

    private final double larguraBandaDisponivel;

    protected CS_Comunicacao (
        final String id, final double LarguraBanda, final double Ocupacao, final double Latencia
    ) {
        this.ocupacao               = Ocupacao;
        this.latencia               = Latencia;
        this.metrica                = new MetricasComunicacao(id);
        this.larguraBandaDisponivel = LarguraBanda * (1.0 - this.ocupacao);
    }

    @Override
    public String getId () {
        return this.metrica.getId();
    }

    public MetricasComunicacao getMetrica () {
        return this.metrica;
    }

    public double getOcupacao () {
        return this.ocupacao;
    }

    /**
     * Retorna o tempo gasto
     */
    public double tempoTransmitir (final double Mbits) {
        return (Mbits / this.larguraBandaDisponivel) + this.latencia;
    }
}
