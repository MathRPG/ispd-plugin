package ispd.motor;

import ispd.motor.filas.*;

public interface Mensagens {

    void atenderCancelamento (Simulation simulacao, Mensagem mensagem);

    void atenderParada (Simulation simulacao, Mensagem mensagem);

    void atenderDevolucao (Simulation simulacao, Mensagem mensagem);

    void atenderDevolucaoPreemptiva (Simulation simulacao, Mensagem mensagem);

    void atenderAtualizacao (Simulation simulacao, Mensagem mensagem);

    void atenderRetornoAtualizacao (Simulation simulacao, Mensagem mensagem);

    void atenderFalha (Simulation simulacao, Mensagem mensagem);

    void atenderAckAlocacao (Simulation simulacao, Mensagem mensagem);
}
