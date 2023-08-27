package ispd.motor.queues.centers.impl;

import ispd.motor.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import java.util.*;

public class GridMachine extends Processing implements RequestHandler, Vertex {

    private final List<Communication> conexoesSaida = new ArrayList<>();

    private final List<GridTask> filaTarefas = new ArrayList<>();

    private final List<Processing> mestres = new ArrayList<>();

    private final List<GridTask> tarefaEmExecucao;

    private final List<Double> falhas = new ArrayList<>();

    private final List<Double> recuperacao = new ArrayList<>();

    private final List<GridTask> historicoProcessamento = new ArrayList<>();

    private List<List> caminhoMestre;

    private int processadoresDisponiveis;

    /**
     * Constructor which specifies the machine configuration, specifying the id, owner,
     * computational power, core count and load factor.
     * <p><br />
     * Using this constructor the machine number and the energy consumption are both set as default
     * to 0.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param computationalPower
     *     the computational power
     * @param coreCount
     *     the core count
     * @param loadFactor
     *     the load factor
     *
     * @see #GridMachine(String, String, double, int, double, int, double) for specify the machine
     * number and energy consumption.
     */
    public GridMachine (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor
    ) {
        this(id, owner, computationalPower, coreCount, loadFactor, 0, 0.0);
    }

    /**
     * Constructor which specifies the machine configuration, specifying the id, owner,
     * computational power, core count, load factor, machine number and energy consumption.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param computationalPower
     *     the computational power
     * @param coreCount
     *     the core count
     * @param loadFactor
     *     the load factor
     * @param machineNumber
     *     the machine number
     * @param energy
     *     the energy consumption
     */
    public GridMachine (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor, final int machineNumber, final double energy
    ) {
        super(id, owner, computationalPower, coreCount, loadFactor, machineNumber, energy);
        this.processadoresDisponiveis = coreCount;
        this.tarefaEmExecucao         = new ArrayList<>(coreCount);
    }

    /**
     * Constructor which specifies the machine configuration, specifying the id, owner,
     * computational power, core count, load factor and energy consumption.
     * <p><br />
     * Using this constructor the machine number is set as default to 0.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param computationalPower
     *     the computational power
     * @param coreCount
     *     the core count
     * @param loadFactor
     *     the load factor
     * @param energy
     *     the energy consumption.
     *
     * @see #GridMachine(String, String, double, int, double, int, double) for specify the machine
     * number.
     */
    public GridMachine (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor, final double energy
    ) {
        this(id, owner, computationalPower, coreCount, loadFactor, 0, energy);
    }

    /**
     * Constructor which specifies the machine configuration, specifying the id, owner,
     * computational power, core count, load factor and machine number.
     * <p><br />
     * Using this constructor the energy consumption is set as default to 0.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param computationalPower
     *     the computational power
     * @param coreCount
     *     the core count
     * @param loadFactor
     *     the load factor
     * @param machineNumber
     *     the machine number
     *
     * @see #GridMachine(String, String, double, int, double, int, double) for specify the energy
     * consumption
     */
    public GridMachine (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor, final int machineNumber
    ) {
        this(id, owner, computationalPower, coreCount, loadFactor, machineNumber, 0.0);
    }

    @Override
    public void addConexoesSaida (final Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    @Override
    public void clientEnter (final Simulation simulacao, final GridTask cliente) {
        if (cliente.getEstado() != TaskState.CANCELLED) {
            cliente.iniciarEsperaProcessamento(simulacao.getTime(this));
            if (this.processadoresDisponiveis != 0) {
                //indica que recurso está ocupado
                this.processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                final Event novoEvt = new Event(
                    simulacao.getTime(this), EventType.SERVICE, this, cliente
                );
                simulacao.addFutureEvent(novoEvt);
            } else {
                this.filaTarefas.add(cliente);
            }
            this.historicoProcessamento.add(cliente);
        }
    }

    @Override
    public void clientProcessing (final Simulation simulacao, final GridTask cliente) {
        cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
        this.tarefaEmExecucao.add(cliente);
        final Double next = simulacao.getTime(this)
                            + this.tempoProcessar(cliente.getTamProcessamento()
                                                  - cliente.getMflopsProcessado());
        if (!this.falhas.isEmpty() && next > this.falhas.get(0)) {
            Double tFalha = this.falhas.remove(0);
            if (tFalha < simulacao.getTime(this)) {
                tFalha = simulacao.getTime(this);
            }
            final Request msg = new Request(this, RequestType.FAILURE, cliente);
            final Event evt = new Event(
                tFalha, EventType.MESSAGE, this, msg
            );
            simulacao.addFutureEvent(evt);
        } else {
            //Gera evento para atender proximo cliente da lista
            final Event evtFut = new Event(next, EventType.EXIT, this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void clientExit (final Simulation simulacao, final GridTask cliente) {
        //Incrementa o número de Mbits transmitido por este link
        this
            .getMetrica()
            .incMflopsProcessados(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        //Incrementa o tempo de processamento
        final double tempoProc =
            this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
        this.tarefaEmExecucao.remove(cliente);
        //eficiencia calculada apenas nas classes GridMachine
        cliente.calcEficiencia(this.getPoderComputacional());
        //Devolve tarefa para o mestre
        if (this.mestres.contains(cliente.getOrigem())) {
            final int index = this.mestres.indexOf(cliente.getOrigem());
            final List<Service> caminho =
                new ArrayList<>((List<Service>) this.caminhoMestre.get(index));
            cliente.setCaminho(caminho);
            //Gera evento para chegada da tarefa no proximo servidor
            final Event evtFut = new Event(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                cliente.getCaminho().remove(0),
                cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            //buscar menor caminho!!!
            final Processing novoMestre = (Processing) cliente.getOrigem();
            final List<Service> caminho =
                new ArrayList<>(getMenorCaminhoIndireto(this, novoMestre));
            this.addMestre(novoMestre);
            this.caminhoMestre.add(caminho);
            cliente.setCaminho(new ArrayList<>(caminho));
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
        if (this.filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
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

    @Override
    public void requestProcessing (
        final Simulation simulacao,
        final Request request,
        final EventType tipo
    ) {
        if (request != null) {
            if (request.getTipo() == RequestType.UPDATE) {
                this.handleUpdate(simulacao, request);
            } else if (request.getTarefa() != null && request
                .getTarefa()
                .getLocalProcessamento()
                .equals(this)) {
                switch (request.getTipo()) {
                    case STOP -> this.handleStop(simulacao, request);
                    case CANCEL -> this.handlePreemptiveReturn(
                        simulacao,
                        request
                    );
                    case RETURN -> this.handleReturn(
                        simulacao,
                        request
                    );
                    case PREEMPTIVE_RETURN -> this.handleCancel(
                        simulacao, request);
                    case FAILURE -> this.handleFailure(simulacao, request);
                }
            }
        }
    }

    @Override
    public List<Communication> connections () {
        return this.conexoesSaida;
    }

    @Override
    public void handleReturn (final Simulation simulacao, final Request request) {
        final boolean remover = this.filaTarefas.remove(request.getTarefa());
        if (remover) {
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
    public void handlePreemptiveReturn (final Simulation simulacao, final Request request) {
        if (request.getTarefa().getEstado() == TaskState.PROCESSING) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(EventType.EXIT, this, request.getTarefa());
            this.tarefaEmExecucao.remove(request.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
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
        //Incrementa porcentagem da tarefa processada
        request.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void handleUpdate (final Simulation simulacao, final Request request) {
        //enviar resultados
        final int index = this.mestres.indexOf(request.getOrigem());
        final List<Service> caminho =
            new ArrayList<>((List<Service>) this.caminhoMestre.get(index));
        final Request novaRequest =
            new Request(this, request.getTamComunicacao(), RequestType.UPDATE_RESULT);
        //Obtem informações dinâmicas
        novaRequest.setProcessadorEscravo(new ArrayList<>(this.tarefaEmExecucao));
        novaRequest.setFilaEscravo(new ArrayList<>(this.filaTarefas));
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleFailure (final Simulation simulacao, final Request request) {
        this.recuperacao.remove(0);
        for (final GridTask tar : this.tarefaEmExecucao) {
            if (tar.getEstado() == TaskState.PROCESSING) {
                final double inicioAtendimento = tar.parar(simulacao.getTime(this));
                final double tempoProc         = simulacao.getTime(this) - inicioAtendimento;
                final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
                //Incrementa o número de Mflops processados por este recurso
                this.getMetrica().incMflopsProcessados(mflopsProcessados);
                //Incrementa o tempo de processamento
                this.getMetrica().incSegundosDeProcessamento(tempoProc);
                //Incrementa procentagem da tarefa processada
                // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
                // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
                final double numCP = ((int) (mflopsProcessados / 0.0)) * 0.0;
                tar.setMflopsProcessado(numCP);
                tar.setEstado(TaskState.FAILED);
            }
        }
        this.processadoresDisponiveis += this.tarefaEmExecucao.size();
        this.filaTarefas.clear();
        this.tarefaEmExecucao.clear();
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
                this.processadoresDisponiveis++;
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
            this.tarefaEmExecucao.remove(request.getTarefa());
            this.filaTarefas.add(request.getTarefa());
        }
    }

    @Override
    public void handleCancel (final Simulation simulacao, final Request request) {
        boolean remover = false;
        if (request.getTarefa().getEstado() == TaskState.BLOCKED) {
            remover = this.filaTarefas.remove(request.getTarefa());
        } else if (request.getTarefa().getEstado() == TaskState.PROCESSING) {
            remover = simulacao.removeFutureEvent(EventType.EXIT, this, request.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final GridTask proxCliente = this.filaTarefas.remove(0);
                final Event evtFut =
                    new Event(
                        simulacao.getTime(this),
                        EventType.SERVICE,
                        this,
                        proxCliente
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
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            final double numCP = ((int) (mflopsProcessados / 0.0)) *
                                 0.0;
            request.getTarefa().setMflopsProcessado(numCP);
            //Incrementa desperdicio
            this.tarefaEmExecucao.remove(request.getTarefa());
        }
        if (remover) {
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
    public void determinarCaminhos ()
        throws LinkageError {
        //Instancia objetos
        this.caminhoMestre = new ArrayList<>(this.mestres.size());
        //Busca pelos caminhos
        for (int i = 0; i < this.mestres.size(); i++) {
            this.caminhoMestre.add(i, getMenorCaminho(this, this.mestres.get(i)));
        }
        //verifica se todos os mestres são alcansaveis
        for (int i = 0; i < this.mestres.size(); i++) {
            if (this.caminhoMestre.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
    }

    public void addConexoesSaida (final Switch conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addMestre (final Processing mestre) {
        this.mestres.add(mestre);
    }
}
