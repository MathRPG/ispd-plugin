package ispd.motor.filas.servidores;

import ispd.motor.metricas.MetricasComunicacao;

/**
 * Classe abstrata que representa os servidores de comunicação do modelo de fila,
 * Esta classe possui atributos referente a este ripo de servidor, e indica como
 * calcular o tempo gasto para transmitir uma tarefa.
 */
public abstract class CS_Comunicacao extends CentroServico {

    /**
     * Identificador do centro de serviço, deve ser o mesmo do modelo icônico
     */
    private final double              larguraBanda;
    private final double              ocupacao;
    private final double              latencia;
    private final MetricasComunicacao metrica;
    private final double              larguraBandaDisponivel;

    protected CS_Comunicacao (
            final String id, final double LarguraBanda, final double Ocupacao, final double Latencia
    ) {
        this.larguraBanda           = LarguraBanda;
        this.ocupacao               = Ocupacao;
        this.latencia               = Latencia;
        this.metrica                = new MetricasComunicacao(id);
        this.larguraBandaDisponivel = this.larguraBanda - (this.larguraBanda * this.ocupacao);
    }

    public MetricasComunicacao getMetrica () {
        return this.metrica;
    }

    @Override
    public String getId () {
        return this.metrica.getId();
    }

    public double getLarguraBanda () {
        return this.larguraBanda;
    }

    public double getLatencia () {
        return this.latencia;
    }

    public double getOcupacao () {
        return this.ocupacao;
    }

    /**
     * Retorna o tempo gasto
     *
     * @param Mbits
     */
    public double tempoTransmitir (final double Mbits) {
        return (Mbits / this.larguraBandaDisponivel) + this.latencia;
    }
}
