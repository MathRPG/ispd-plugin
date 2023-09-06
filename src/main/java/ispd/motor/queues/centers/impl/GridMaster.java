package ispd.motor.queues.centers.impl;

import ispd.motor.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import ispd.policy.*;
import ispd.policy.loaders.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

public class GridMaster extends Processing
    implements ispd.policy.scheduling.grid.GridMaster, RequestHandler, Vertex {

    private final List<Communication> conexoesSaida = new ArrayList<>();

    private final GridSchedulingPolicy escalonador;

    private final List<GridTask> filaTarefas = new ArrayList<>();

    private boolean maqDisponivel = true;

    private boolean escDisponivel = true;

    private Set<Condition> tipoEscalonamento = Conditions.WHILE_MUST_DISTRIBUTE;

    private Simulation simulacao = null;

    public GridMaster (
        final String id,
        final String proprietario,
        final double PoderComputacional,
        final double Ocupacao,
        final String Escalonador,
        final Double energia
    ) {
        super(id, proprietario, PoderComputacional, 1, Ocupacao, 0, energia);
        this.escalonador = new GridSchedulingLoader().loadPolicy(Escalonador);
        this.escalonador.setMestre(this);
    }

    @Override
    public void clientEnter (final Simulation simulacao, final GridTask cliente) {
        if (cliente.getEstado() != TaskState.CANCELLED) {
            //Tarefas concluida possuem tratamento diferencial
            if (cliente.getEstado() == TaskState.DONE) {
                //se não for origem da tarefa ela deve ser encaminhada
                if (!cliente.getOrigem().equals(this)) {
                    //encaminhar tarefa!
                    //Gera evento para chegada da tarefa no proximo servidor
                    final Event evtFut = new Event(
                        simulacao.getTime(this),
                        EventType.ARRIVAL,
                        cliente.getCaminho().remove(0),
                        cliente
                    );
                    //Event adicionado a lista de evntos futuros
                    simulacao.addFutureEvent(evtFut);
                }
                this.escalonador.addTarefaConcluida(cliente);
                if (this.tipoEscalonamento.contains(Condition.WHEN_RECEIVES_RESULT)) {
                    if (this.escalonador.getFilaTarefas().isEmpty()) {
                        this.escDisponivel = true;
                    } else {
                        this.executeScheduling();
                    }
                }
            } else if (this.escDisponivel) {
                this.escDisponivel = false;
                //escalonador decide qual ação tomar na chegada de uma tarefa
                this.escalonador.adicionarTarefa(cliente);
                //Se não tiver tarefa na fila a primeira tarefa será escalonada
                this.executeScheduling();
            } else {
                //escalonador decide qual ação tomar na chegada de uma tarefa
                this.escalonador.adicionarTarefa(cliente);
            }
        }
    }

    @Override
    public void clientProcessing (final Simulation simulacao, final GridTask cliente) {
        //o atendimento pode realiza o processamento da tarefa como em uma
        // maquina qualquer
        if (this.maqDisponivel) {
            this.maqDisponivel = false;
            cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
            cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para saida do cliente do servidor
            final Event evtFut = new Event(
                simulacao.getTime(this)
                + this.tempoProcessar(cliente.getTamProcessamento()
                                      - cliente.getMflopsProcessado()),
                EventType.EXIT, this, cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            this.filaTarefas.add(cliente);
        }
    }

    @Override
    public void clientExit (final Simulation simulacao, final GridTask cliente) {
        if (cliente.getEstado() == TaskState.PROCESSING) {
            //Incrementa o número de Mbits transmitido por este link
            this
                .getMetrica()
                .incMflopsProcessados(cliente.getTamProcessamento()
                                      - cliente.getMflopsProcessado());
            //Incrementa o tempo de transmissão
            final double tempoProc =
                this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa o tempo de transmissão no pacote
            cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para chegada da tarefa no proximo servidor
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                ////Indica que está livre
                this.maqDisponivel = true;
                //Gera evento para atender proximo cliente da lista
                final GridTask proxCliente = this.filaTarefas.remove(0);
                final Event evtFut = new Event(
                    simulacao.getTime(this), EventType.SERVICE, this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        } else {
            //Gera evento para chegada da tarefa no proximo servidor
            final Event evtFut = new Event(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                cliente.getCaminho().remove(0),
                cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (this.tipoEscalonamento.contains(Condition.WHILE_MUST_DISTRIBUTE)) {
                //se fila de tarefas do servidor não estiver vazia escalona
                // proxima tarefa
                if (this.escalonador.getFilaTarefas().isEmpty()) {
                    this.escDisponivel = true;
                } else {
                    this.executeScheduling();
                }
            }
        }
    }

    @Override
    public void requestProcessing (
        final Simulation simulacao,
        final Request request,
        final EventType tipo
    ) {
        if (tipo == EventType.SCHEDULING) {
            this.escalonador.escalonar();
        } else if (request != null) {
            if (request.getTipo() == RequestType.UPDATE) {
                this.handleUpdate(simulacao, request);
            } else if (request.getTarefa() != null && request
                .getTarefa()
                .getLocalProcessamento()
                .equals(this)) {
                switch (request.getTipo()) {
                    case STOP -> this.handleStop(simulacao, request);
                    case CANCEL -> this.handlePreemptiveReturn(simulacao, request);
                    case RETURN -> this.handleReturn(simulacao, request);
                    case PREEMPTIVE_RETURN -> this.handleCancel(
                        simulacao,
                        request
                    );
                }
            } else if (request.getTipo() == RequestType.UPDATE_RESULT) {
                this.handleUpdateResult(simulacao, request);
            } else if (request.getTarefa() != null) {
                //encaminhando request para o destino
                this.sendMessage(
                    request.getTarefa(),
                    (Processing) request.getTarefa().getLocalProcessamento(),
                    request.getTipo()
                );
            }
        }
    }

    @Override
    public List<Communication> connections () {
        return this.conexoesSaida;
    }

    @Override
    public void handleReturn (final Simulation simulacao, final Request request) {
        final boolean temp1 = this.filaTarefas.remove(request.getTarefa());
        final boolean temp2 = this.escalonador.getFilaTarefas().remove(request.getTarefa());
        if (temp1 || temp2) {
            final Event evtFut = new Event(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                request.getTarefa().getOrigem(),
                request.getTarefa()
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void handlePreemptiveReturn (
        final Simulation simulacao, final Request request
    ) {
        if (request.getTarefa().getEstado() == TaskState.PROCESSING) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(EventType.EXIT, this,
                                        request.getTarefa()
            );
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final GridTask proxCliente = this.filaTarefas.remove(0);
                final Event evtFut = new Event(
                    simulacao.getTime(this), EventType.SERVICE, this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        }
        final double inicioAtendimento = request.getTarefa().cancelar(simulacao.getTime(this));
        final double tempoProc         = simulacao.getTime(this) - inicioAtendimento;
        final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
        //Incrementa o número de Mflops processados por este recurso
        this.getMetrica().incMflopsProcessados(mflopsProcessados);
        //Incrementa o tempo de processamento
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa procentagem da tarefa processada
        request.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void handleUpdate (
        final Simulation simulacao,
        final Request request
    ) {
        //atualiza metricas dos usuarios globais
        //enviar resultados
        final List<Service> caminho = new ArrayList<>(Objects.requireNonNull(
            Processing.getMenorCaminhoIndireto(this, (Processing) request.getOrigem())
        ));

        final Request novaRequest =
            new Request(this, request.getTamComunicacao(), RequestType.UPDATE_RESULT);
        //Obtem informações dinâmicas
        novaRequest.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaRequest.getFilaEscravo().addAll(this.escalonador.getFilaTarefas());
        novaRequest.setCaminho(caminho);
        final Event evtFut = new Event(
            simulacao.getTime(this),
            EventType.MESSAGE,
            novaRequest.getCaminho().remove(0),
            novaRequest
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void handleUpdateResult (final Simulation simulacao, final Request request) {
        this.escalonador.resultadoAtualizar(request);
    }

    @Override
    public void handleFailure (final Simulation simulacao, final Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleAllocationAck (final Simulation simulacao, final Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleStop (final Simulation simulacao, final Request request) {
        if (request.getTarefa().getEstado() == TaskState.PROCESSING) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(EventType.EXIT, this, request.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final GridTask proxCliente = this.filaTarefas.remove(0);
                final Event evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.SERVICE,
                    this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final double inicioAtendimento = request.getTarefa().parar(simulacao.getTime(this));
            final double tempoProc         = simulacao.getTime(this) - inicioAtendimento;
            final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            request.getTarefa().setMflopsProcessado(mflopsProcessados);
        }
    }

    @Override
    public void handleCancel (final Simulation simulacao, final Request request) {
        boolean temp1 = false;
        boolean temp2 = false;
        if (request.getTarefa().getEstado() == TaskState.BLOCKED) {
            temp1 = this.filaTarefas.remove(request.getTarefa());
            temp2 = this.escalonador.getFilaTarefas().remove(request.getTarefa());
        } else if (request.getTarefa().getEstado() == TaskState.PROCESSING) {
            //remover evento de saida do cliente do servidor
            temp1 = simulacao.removeFutureEvent(EventType.EXIT, this,
                                                request.getTarefa()
            );
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final GridTask proxCliente = this.filaTarefas.remove(0);
                final Event evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.SERVICE,
                    this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final double inicioAtendimento =
                request.getTarefa().parar(simulacao.getTime(this));
            final double tempoProc =
                simulacao.getTime(this) - inicioAtendimento;
            final double mflopsProcessados =
                this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            final double numCP =
                ((int) (mflopsProcessados / 0.0)) *
                0.0;
            request.getTarefa().setMflopsProcessado(numCP);
            //Incrementa desperdicio
        }

        if (temp1 || temp2) {
            final Event evtFut = new Event(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                request.getTarefa().getOrigem(),
                request.getTarefa()
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void executeScheduling () {
        final Event evtFut = new Event(
            this.simulacao.getTime(this),
            EventType.SCHEDULING,
            this, null
        );
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void setSchedulingConditions (final Set<Condition> newConditions) {
        this.tipoEscalonamento = newConditions;
    }

    @Override
    public void sendTask (final GridTask task) {
        //Gera evento para atender proximo cliente da lista
        final Event evtFut = new Event(
            this.simulacao.getTime(this), EventType.EXIT, this, task
        );
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public GridTask cloneTask (final GridTask task) {
        final GridTask tarefa = new GridTask(task);
        this.simulacao.addJob(tarefa);
        return tarefa;
    }

    @Override
    public void sendMessage (
        final GridTask task,
        final Processing slave,
        final RequestType requestType
    ) {
        final Request msg = new Request(this, requestType, task);
        msg.setCaminho(this.escalonador.escalonarRota(slave));
        final Event evtFut = new Event(
            this.simulacao.getTime(this), EventType.MESSAGE, msg.getCaminho().remove(0), msg
        );
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public Simulation getSimulation () {
        return this.simulacao;
    }

    @Override
    public void setSimulation (final Simulation newSimulation) {
        this.simulacao = newSimulation;
    }

    @Override
    public void addOutboundConnection (final Link link) {
        this.conexoesSaida.add(link);
    }

    /**
     * Encontra caminhos para chegar até um escravo e adiciona no caminhoEscravo
     */
    @Override
    public void determinarCaminhos ()
        throws LinkageError {
        final List<Processing> escravos = this.escalonador.getEscravos();
        //Instancia objetos
        /**
         * Armazena os caminhos possiveis para alcançar cada escravo
         */
        final List<List> caminhoEscravo = new ArrayList<>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            caminhoEscravo.add(i, Processing.getMenorCaminho(this, escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
        this.escalonador.setCaminhoEscravo(caminhoEscravo);
    }

    public void atualizar (final Service escravo, final Double time) {
        final Request msg = new Request(this, 0.011444091796875,
                                        RequestType.UPDATE
        );
        msg.setCaminho(this.escalonador.escalonarRota(escravo));
        final Event evtFut = new Event(
            time,
            EventType.MESSAGE,
            msg.getCaminho().remove(0),
            msg
        );
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    public GridSchedulingPolicy getEscalonador () {
        return this.escalonador;
    }

    public void addConexoesSaida (final Communication Switch) {
        this.conexoesSaida.add(Switch);
    }

    public void addEscravo (final Processing maquina) {
        this.escalonador.addEscravo(maquina);
    }
}
