package ispd.motor.queues.centers;

import ispd.motor.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;

/**
 * Elemento servidor do modelo de fila. Podendo representar: Recursos de processamento: Maquina,
 * cluster Recurso de comunicação: Link, internet Esta classe abstrata indica todos os eventos que
 * um servidor pode realizar no modelo de fila desenvolvido
 */
public interface Service {

    /**
     * Executa as ações necessárias durante a chegada de um cliente na fila do servidor
     *
     * @param simulacao
     *     obtem acesso a lista de eventos futuros para adicionar eventos criados durante a execução
     *     deste evento
     * @param cliente
     *     cliente que acabou de chegar, neste caso uma tarefa
     */
    void clientEnter (Simulation simulacao, GridTask cliente);

    /**
     * Executa as ações necessárias durante o atendimento de um cliente pelo servidor
     *
     * @param simulacao
     *     obtem acesso a lista de eventos futuros para adicionar eventos criados durante a execução
     *     deste evento
     * @param cliente
     *     cliente atendido, neste caso uma tarefa
     */
    void clientProcessing (Simulation simulacao, GridTask cliente);

    /**
     * Executa as ações necessárias durante a saida de um cliente após ser atendido pelo servidor
     *
     * @param simulacao
     *     obtem acesso a lista de eventos futuros para adicionar eventos criados durante a execução
     *     deste evento
     * @param cliente
     *     cliente saiu do servidor, neste caso uma tarefa
     */
    void clientExit (Simulation simulacao, GridTask cliente);

    /**
     * Evento que possibilita o atendimento de uma requisição, diferente de um cliente, mas pode
     * alterar o estado de um cliente, por exemplo cancelando seu atendimento
     *
     * @param simulacao
     *     obtem acesso a lista de eventos futuros para adicionar eventos criados durante a execução
     *     deste evento
     * @param cliente
     *     cliente que será alterado pela requisição
     * @param tipo
     *     constante que indica tipo de requisição
     */
    void requestProcessing (Simulation simulacao, Request cliente, EventType tipo);

    String id ();

    /**
     * Retorna conexões de saida do recurso
     */
    Object connections ();
}
