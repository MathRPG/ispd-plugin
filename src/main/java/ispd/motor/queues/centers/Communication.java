package ispd.motor.queues.centers;

import ispd.motor.metrics.*;

/**
 * Classe abstrata que representa os servidores de comunicação do modelo de fila, Esta classe possui
 * atributos referente a este ripo de servidor, e indica como calcular o tempo gasto para transmitir
 * uma tarefa.
 */
public abstract class Communication implements Service {

    private final double ocupacao;

    private final double latencia;

    private final CommunicationMetrics metrica;

    private final double larguraBandaDisponivel;

    protected Communication (
        final String id, final double LarguraBanda, final double Ocupacao, final double Latencia
    ) {
        this.ocupacao               = Ocupacao;
        this.latencia               = Latencia;
        this.metrica = new CommunicationMetrics(id);
        this.larguraBandaDisponivel = LarguraBanda * (1.0 - this.ocupacao);
    }

    @Override
    public String id () {
        return this.metrica.getId();
    }

    public CommunicationMetrics getMetrica () {
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
