package ispd.motor;

import ispd.motor.filas.Mensagem;

public interface Mensagens {

    int CANCELAR               = 1;

    int PARAR                  = 2;

    int DEVOLVER               = 3;

    int DEVOLVER_COM_PREEMPCAO = 4;

    int ATUALIZAR              = 5;

    int RESULTADO_ATUALIZAR    = 6;

    int FALHAR                 = 7;

    int ALOCAR_ACK             = 8;

    void atenderCancelamento (Simulation simulacao, Mensagem mensagem);

    void atenderParada (Simulation simulacao, Mensagem mensagem);

    void atenderDevolucao (Simulation simulacao, Mensagem mensagem);

    void atenderDevolucaoPreemptiva (Simulation simulacao, Mensagem mensagem);

    void atenderAtualizacao (Simulation simulacao, Mensagem mensagem);

    void atenderRetornoAtualizacao (Simulation simulacao, Mensagem mensagem);

    void atenderFalha (Simulation simulacao, Mensagem mensagem);

    void atenderAckAlocacao (Simulation simulacao, Mensagem mensagem);
}
