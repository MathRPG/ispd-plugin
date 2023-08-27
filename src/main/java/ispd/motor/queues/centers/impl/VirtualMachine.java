package ispd.motor.queues.centers.impl;

import ispd.motor.*;
import ispd.motor.metrics.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import java.util.*;

public class VirtualMachine extends Processing implements Client, RequestHandler {

    private final List<List> caminhoIntermediarios = new ArrayList<>();

    private final List<GridTask> filaTarefas = new ArrayList<>();

    private final List<GridTask> tarefaEmExecucao;

    private final List<CloudMaster> VMMsIntermediarios = new ArrayList<>();

    private final Cost metricaCusto;

    private final List<Double> falhas = new ArrayList<>();

    private final List<Double> recuperacao = new ArrayList<>();

    private final double memoriaDisponivel;

    private final double discoDisponivel;

    private CloudMaster vmmResponsavel = null;

    private int processadoresDisponiveis;

    private double instanteAloc = 0.0;

    private double tempoDeExec = 0;

    private CloudMachine maquinaHospedeira = null;

    private List<Service> caminho = null;

    private List<Service> caminhoVMM = null;

    private VirtualMachineState status = VirtualMachineState.FREE;

    public VirtualMachine (
        final String id,
        final String proprietario,
        final int numeroProcessadores,
        final double memoria,
        final double disco
    ) {
        super(id, proprietario, 0, numeroProcessadores, 0, 0);
        this.processadoresDisponiveis = numeroProcessadores;
        this.memoriaDisponivel        = memoria;
        this.discoDisponivel          = disco;
        this.metricaCusto = new Cost(id);
        this.tarefaEmExecucao         = new ArrayList<>(numeroProcessadores);
    }

    @Override
    public void clientEnter (final Simulation simulacao, final GridTask cliente) {
        if (cliente.getEstado() != TaskState.CANCELLED) { //se a tarefa estiver parada ou executando
            cliente.iniciarEsperaProcessamento(simulacao.getTime(this));
            if (this.processadoresDisponiveis != 0) {
                //indica que recurso está ocupado
                this.processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                final var novoEvt = new Event(
                    simulacao.getTime(this), EventType.SERVICE, this, cliente
                );
                simulacao.addFutureEvent(novoEvt);
            } else {
                this.filaTarefas.add(cliente);
            }
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
            var tFalha = this.falhas.remove(0);
            if (tFalha < simulacao.getTime(this)) {
                tFalha = simulacao.getTime(this);
            }
            final var msg = new Request(this, RequestType.FAILURE, cliente);
            final var evt = new Event(
                tFalha, EventType.MESSAGE, this, msg
            );
            simulacao.addFutureEvent(evt);
        } else {
            //Gera evento para atender proximo cliente da lista
            final var evtFut = new Event(
                next, EventType.EXIT, this, cliente
            );
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
        final var tempoProc =
            this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
        this.tarefaEmExecucao.remove(cliente);
        //eficiencia calculada apenas nas classes GridMachine
        cliente.calcEficiencia(this.getPoderComputacional());
        //Devolve tarefa para o mestre

        final var Origem = cliente.getOrigem();
        final ArrayList<Service> caminho;
        if (Origem.equals(this.vmmResponsavel)) {
            caminho = new ArrayList<>(this.caminhoVMM);
        } else {
            System.out.println("A tarefa não saiu do vmm desta vm!!!!!");
            final var index = this.VMMsIntermediarios.indexOf((CloudMaster) Origem);
            if (index == -1) {
                final var auxMaq = this.maquinaHospedeira;
                final var caminhoInter =
                    new ArrayList<Service>(getMenorCaminhoIndiretoCloud(
                        auxMaq,
                        (Processing) Origem
                    ));
                caminho = new ArrayList<>(caminhoInter);
                this.VMMsIntermediarios.add((CloudMaster) Origem);
                final var idx = this.VMMsIntermediarios.indexOf((CloudMaster) Origem);
                this.caminhoIntermediarios.add(idx, caminhoInter);
            } else {
                caminho = new ArrayList<Service>(this.caminhoIntermediarios.get(index));
            }
        }
        cliente.setCaminho(caminho);
        System.out.println("Saida -" + this.id() + "- caminho size:" + caminho.size());

        //Gera evento para chegada da tarefa no proximo servidor
        final var evtFut = new Event(
            simulacao.getTime(this),
            EventType.ARRIVAL,
            cliente.getCaminho().remove(0),
            cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);

        if (this.filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
        } else {
            //Gera evento para atender proximo cliente da lista
            final var proxCliente = this.filaTarefas.remove(0);
            final var NovoEvt = new Event(
                simulacao.getTime(this), EventType.SERVICE, this, proxCliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(NovoEvt);
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
    public Object connections () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleReturn (final Simulation simulacao, final Request request) {
        final var remover = this.filaTarefas.remove(request.getTarefa());
        if (remover) {
            final var evtFut = new Event(
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
                final var proxCliente = this.filaTarefas.remove(0);
                final var evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.SERVICE,
                    this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        }
        final var inicioAtendimento = request.getTarefa().cancelar(simulacao.getTime(this));
        final var tempoProc         = simulacao.getTime(this) - inicioAtendimento;
        final var mflopsProcessados = this.getMflopsProcessados(tempoProc);
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
        final List<Service> caminho = new ArrayList<>(this.caminhoVMM);
        final var novaRequest =
            new Request(this, request.getTamComunicacao(), RequestType.UPDATE_RESULT);
        //Obtem informações dinâmicas
        novaRequest.setProcessadorEscravo(new ArrayList<>(this.tarefaEmExecucao));
        novaRequest.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaRequest.setCaminho(caminho);
        final var evtFut = new Event(
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
        for (final var tar : this.tarefaEmExecucao) {
            if (tar.getEstado() == TaskState.PROCESSING) {
                final var inicioAtendimento = tar.parar(simulacao.getTime(this));
                final var tempoProc         = simulacao.getTime(this) - inicioAtendimento;
                final var mflopsProcessados = this.getMflopsProcessados(tempoProc);
                //Incrementa o número de Mflops processados por este recurso
                this.getMetrica().incMflopsProcessados(mflopsProcessados);
                //Incrementa o tempo de processamento
                this.getMetrica().incSegundosDeProcessamento(tempoProc);
                //Incrementa procentagem da tarefa processada
                // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
                final var numCP = (int) (mflopsProcessados / 0.0);
                // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
                tar.setMflopsProcessado(numCP * 0.0);
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
                final var proxCliente = this.filaTarefas.remove(0);
                final var evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.SERVICE,
                    this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final var inicioAtendimento = request.getTarefa().parar(simulacao.getTime(this));
            final var tempoProc         = simulacao.getTime(this) - inicioAtendimento;
            final var mflopsProcessados = this.getMflopsProcessados(tempoProc);
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
        var remover = false;
        if (request.getTarefa().getEstado() == TaskState.BLOCKED) {
            remover = this.filaTarefas.remove(request.getTarefa());
        } else if (request.getTarefa().getEstado() == TaskState.PROCESSING) {
            remover = simulacao.removeFutureEvent(
                EventType.EXIT,
                this,
                request.getTarefa()
            );
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final var proxCliente = this.filaTarefas.remove(0);
                final var evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.SERVICE,
                    this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final var inicioAtendimento = request.getTarefa().parar(simulacao.getTime(this));
            final var tempoProc         = simulacao.getTime(this) - inicioAtendimento;
            final var mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            final var numCP = (int) (mflopsProcessados / 0.0);
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            request.getTarefa().setMflopsProcessado(numCP * 0.0);
            this.tarefaEmExecucao.remove(request.getTarefa());
        }
        if (remover) {
            final var evtFut = new Event(
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getTamComunicacao () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getTamProcessamento () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getTimeCriacao () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Service getOrigem () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Service> getCaminho () {
        return this.caminho;
    }

    @Override
    public void setCaminho (final List<Service> caminho) {
        this.caminho = caminho;
    }

    public CloudMachine getMaquinaHospedeira () {
        return this.maquinaHospedeira;
    }

    public void setMaquinaHospedeira (final CloudMachine maquinaHospedeira) {
        this.maquinaHospedeira = maquinaHospedeira;
    }

    public CloudMaster getVmmResponsavel () {
        return this.vmmResponsavel;
    }

    public int getProcessadoresDisponiveis () {
        return this.processadoresDisponiveis;
    }

    public void setPoderProcessamentoPorNucleo (final double poderProcessamento) {
        this.setPoderComputacionalDisponivelPorProcessador(poderProcessamento);
        this.setPoderComputacional(poderProcessamento);
    }

    public double getMemoriaDisponivel () {
        return this.memoriaDisponivel;
    }

    public double getDiscoDisponivel () {
        return this.discoDisponivel;
    }

    public void setCaminhoVMM (final List<Service> caminhoMestre) {
        this.caminhoVMM = caminhoMestre;
    }

    public void addVMM (final CloudMaster vmmResponsavel) {
        this.vmmResponsavel = vmmResponsavel;
    }

    public VirtualMachineState getStatus () {
        return this.status;
    }

    public void setStatus (final VirtualMachineState status) {
        this.status = status;
    }

    public Cost getMetricaCusto () {
        return this.metricaCusto;
    }

    public double getTempoDeExec () {
        return this.tempoDeExec;
    }

    public void setTempoDeExec (final double tempoDestruir) {
        this.tempoDeExec = tempoDestruir - this.instanteAloc;
    }

    public void setInstanteAloc (final double instanteAloc) {
        this.instanteAloc = instanteAloc;
    }
}
