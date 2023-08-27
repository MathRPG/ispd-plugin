package ispd.motor.queues.centers.impl;

import ispd.motor.*;
import ispd.motor.metrics.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import java.util.*;

public class CloudMachine extends Processing implements RequestHandler, Vertex {

    public static final int DESLIGADO = 2;

    private final List<Communication> conexoesSaida = new ArrayList<>();

    private final List<GridTask> filaTarefas = new ArrayList<>();

    private final List<GridTask> tarefaEmExecucao;

    private final List<Double> recuperacao = new ArrayList<>();

    private final List<VirtualMachine> VMs = new ArrayList<>();

    private final List<Processing> mestres = new ArrayList<>();

    private List<List> caminhoMestre = null;

    private int processadoresDisponiveis;

    private double memoriaDisponivel;

    private double discoDisponivel;

    private final double custoProc;

    private final double custoMemoria;

    private final double custoDisco;

    private double custoTotalDisco = 0.0;

    private double custoTotalMemoria = 0.0;

    private double custoTotalProc = 0.0;

    private Allocation metricaAloc = null;

    public CloudMachine (
        final String id,
        final String proprietario,
        final double PoderComputacional,
        final int numeroProcessadores,
        final double Ocupacao,
        final double memoria,
        final double disco,
        final double custoProc,
        final double custoMem,
        final double custoDisco
    ) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, 0);
        this.metricaAloc = new Allocation(id);
        this.processadoresDisponiveis = numeroProcessadores;
        this.tarefaEmExecucao         = new ArrayList<>(numeroProcessadores);
        this.memoriaDisponivel        = memoria;
        this.discoDisponivel          = disco;
        this.custoProc                = custoProc;
        this.custoMemoria             = custoMem;
        this.custoDisco               = custoDisco;
    }

    public CloudMachine (
        final String id,
        final String proprietario,
        final double PoderComputacional,
        final int numeroProcessadores,
        final double memoria,
        final double disco,
        final double custoProc,
        final double custoMem,
        final double custoDisco,
        final double Ocupacao,
        final int numeroMaquina
    ) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, numeroMaquina);
        this.processadoresDisponiveis = numeroProcessadores;
        this.memoriaDisponivel        = memoria;
        this.discoDisponivel          = disco;
        this.custoProc                = custoProc;
        this.custoMemoria             = custoMem;
        this.custoDisco               = custoDisco;
        this.tarefaEmExecucao         = new ArrayList<>(numeroProcessadores);
    }

    @Override
    public void clientEnter (final Simulation simulacao, final GridTask cliente) {
        if (cliente instanceof final CloudTask trf) {
            final var vm = trf.getVM_enviada();
            if (this.isHosting(vm)) {
                if (this.VMs.contains(vm)) {
                } else {
                    final var evtFut = new Event(
                        simulacao.getTime(this), EventType.SERVICE, this, cliente
                    );
                    simulacao.addFutureEvent(evtFut);
                }
            } else {
                final var evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.ARRIVAL,
                    cliente.getCaminho().remove(0),
                    cliente
                );
                simulacao.addFutureEvent(evtFut);
            }
        } else {
            //procedimento caso cliente seja uma tarefa!
            final var vm = (VirtualMachine) cliente.getLocalProcessamento();
            final Event evtFut;
            if (this.isHosting(vm)) {
                //se a tarefa é endereçada pra uma VM qu está alocada nessa máquina
                evtFut = new Event(
                    simulacao.getTime(this), EventType.ARRIVAL, vm, cliente
                );
            } else {
                evtFut = new Event(
                    simulacao.getTime(this), EventType.EXIT, this, cliente
                );
            }
            simulacao.addFutureEvent(evtFut);
        }
    }

    private boolean isHosting (final VirtualMachine vm) {
        return vm.getMaquinaHospedeira().equals(this);
    }

    @Override
    public void clientProcessing (final Simulation simulacao, final GridTask cliente) {
        final var trf = (CloudTask) cliente;
        final var vm  = trf.getVM_enviada();
        this.addVM(vm); //incluir a VM na lista de VMs
        this.metricaAloc.incVMsAlocadas();
        //Setar o caminho da vm para o VMM e o caminho do ACK da mensagem >>>
        final var vmm   = vm.getVmmResponsavel();
        final var index = this.mestres.indexOf(vmm);
        final var msg   = new Request(this, RequestType.ALLOCATION_ACK, cliente);

        if (index == -1) {
            final var caminhoVMM =
                new ArrayList<Service>(getMenorCaminhoIndiretoCloud(this, vmm));
            final var caminhoMsg =
                new ArrayList<Service>(getMenorCaminhoIndiretoCloud(this, vmm));

            for (final var cs : caminhoVMM) {
                System.out.println(cs.id());
            }

            vm.setCaminhoVMM(caminhoVMM);
            msg.setCaminho(caminhoMsg);
        } else {
            final var caminhoVMM =
                new ArrayList<Service>(this.caminhoMestre.get(index));
            final var caminhoMsg =
                new ArrayList<Service>(this.caminhoMestre.get(index));

            for (final var cs : caminhoVMM) {
                System.out.println(cs.id());
            }
            vm.setCaminhoVMM(caminhoVMM);
            msg.setCaminho(caminhoMsg);
        }

        //enviar mensagem de ACK para o VMM
        final var NovoEvt =
            new Event(simulacao.getTime(this), EventType.MESSAGE, this, msg);
        simulacao.addFutureEvent(NovoEvt);

        //Gerenciamento de custos
        this.custoTotalProc    =
            this.custoTotalProc + (vm.getProcessadoresDisponiveis() * this.custoProc);
        this.custoTotalMemoria =
            this.custoTotalMemoria + (vm.getMemoriaDisponivel() * this.custoMemoria);
        this.custoTotalDisco   = this.custoTotalDisco + (vm.getDiscoDisponivel() * this.custoDisco);
        //setar o poder de processamento da VM.
        vm.setPoderProcessamentoPorNucleo(this.getPoderComputacional());
    }

    @Override
    public void clientExit (final Simulation simulacao, final GridTask cliente) {
        System.out.println("--------------------------------------");
        System.out.println(this.id() + ": Saída de cliente");
        System.out.println("--------------------------------------");
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
            } else if (request.getTipo()
                       == RequestType.ALLOCATION_ACK) { //a máquina é só um intermediário
                //esse tipo de request só é atendido por um VMM
                this.handleAllocationAck(simulacao, request);
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
                    simulacao.getTime(this), EventType.SERVICE, this, proxCliente
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
        final var index = this.mestres.indexOf(request.getOrigem());
        final List<Service> caminho =
            new ArrayList<>((List<Service>) this.caminhoMestre.get(index));
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
        final double tempoRec = this.recuperacao.remove(0);
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
                if (false) {
                    //Reiniciar atendimento da tarefa
                    tar.iniciarEsperaProcessamento(simulacao.getTime(this));
                    //cria evento para iniciar o atendimento imediatamente
                    final var novoEvt = new Event(
                        simulacao.getTime(this) + tempoRec,
                        EventType.SERVICE, this, tar
                    );
                    simulacao.addFutureEvent(novoEvt);
                } else {
                    tar.setEstado(TaskState.FAILED);
                }
            }
        }
        this.processadoresDisponiveis += this.tarefaEmExecucao.size();
        this.filaTarefas.clear();
        this.tarefaEmExecucao.clear();
    }

    @Override
    public void handleAllocationAck (final Simulation simulacao, final Request request) {
        //quem deve resolver esse método é o VMM de origem
        //portanto as maquinas só encaminham pro próximo centro de serviço.
        System.out.println("--------------------------------------");
        System.out.println("Encaminhando ACK de alocação para " + request.getOrigem().id());
        final var evt = new Event(
            simulacao.getTime(this), EventType.MESSAGE, request.getCaminho().remove(0), request
        );
        simulacao.addFutureEvent(evt);
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
                final var evtFut =
                    new Event(
                        simulacao.getTime(this),
                        EventType.SERVICE,
                        this,
                        proxCliente
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
            remover = simulacao.removeFutureEvent(EventType.EXIT, this, request.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final var proxCliente = this.filaTarefas.remove(0);
                final var evtFut =
                    new Event(
                        simulacao.getTime(this),
                        EventType.SERVICE,
                        this,
                        proxCliente
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
        //Instancia objetos
        this.caminhoMestre = new ArrayList<>(this.mestres.size());
        System.out.println(
            "maquina "
            + this.id()
            + " determinando caminhos para "
            + this.mestres.size()
            + " mestres");

        //Busca pelos caminhos
        for (var i = 0; i < this.mestres.size(); i++) {
            this.caminhoMestre.add(i, getMenorCaminhoCloud(
                this,
                this.mestres.get(i)
            ));
        }

        //verifica se todos os mestres são alcansaveis
        for (var i = 0; i < this.mestres.size(); i++) {
            if (this.caminhoMestre.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
    }

    @Override
    public void addConexoesSaida (final Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    private void addVM (final VirtualMachine vm) {
        this.VMs.add(vm);
    }

    public Allocation getMetricaAloc () {
        return this.metricaAloc;
    }

    public double getMemoriaDisponivel () {
        return this.memoriaDisponivel;
    }

    public void setMemoriaDisponivel (final double memoriaDisponivel) {
        this.memoriaDisponivel = memoriaDisponivel;
    }

    public double getDiscoDisponivel () {
        return this.discoDisponivel;
    }

    public void setDiscoDisponivel (final double discoDisponivel) {
        this.discoDisponivel = discoDisponivel;
    }

    public int getProcessadoresDisponiveis () {
        return this.processadoresDisponiveis;
    }

    public void setProcessadoresDisponiveis (final int processadoresDisponiveis) {
        this.processadoresDisponiveis = processadoresDisponiveis;
    }

    public void addConexoesSaida (final Switch conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addMestre (final Processing mestre) {
        this.mestres.add(mestre);
    }

    public void desligar (final Simulation simulacao) {
        for (final var vm : this.VMs) {
            vm.setStatus(VirtualMachineState.DESTROYED);
            vm.setTempoDeExec(simulacao.getTime(this));
            vm
                .getMetricaCusto()
                .setCustoDisco(this.custoDisco * vm.getDiscoDisponivel() * (
                    vm.getTempoDeExec()
                    / 60
                ));
            vm.getMetricaCusto()
                .setCustoMem(this.custoMemoria
                             * (vm.getMemoriaDisponivel() / 1024)
                             * (vm.getTempoDeExec() / 60));
            vm.getMetricaCusto()
                .setCustoProc(this.custoProc
                              * vm.getProcessadoresDisponiveis()
                              * (vm.getTempoDeExec() / 60));
        }
    }
}
