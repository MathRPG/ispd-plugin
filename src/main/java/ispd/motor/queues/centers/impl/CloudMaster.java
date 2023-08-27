package ispd.motor.queues.centers.impl;

import ispd.motor.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import ispd.policy.*;
import ispd.policy.allocation.vm.*;
import ispd.policy.loaders.*;
import ispd.policy.scheduling.cloud.*;
import java.util.*;

public class CloudMaster extends Processing implements VmMaster,
    ispd.policy.scheduling.cloud.CloudMaster, RequestHandler, Vertex {

    private final List<Communication> conexoesSaida = new ArrayList<>();

    private final CloudSchedulingPolicy escalonador;

    private final VmAllocationPolicy alocadorVM;

    private final List<GridTask> filaTarefas = new ArrayList<>();

    private final List<VirtualMachine> maquinasVirtuais = new ArrayList<>();

    private boolean vmsAlocadas = false;

    private boolean escDisponivel = false;

    private boolean alocDisponivel = true;

    private Set<Condition> tipoEscalonamento = Conditions.WHILE_MUST_DISTRIBUTE;

    private Set<Condition> tipoAlocacao = Conditions.WHILE_MUST_DISTRIBUTE;

    private List<List> caminhoEscravo = null;

    private List<List> caminhoVMs = null;

    private Simulation simulacao = null;

    public CloudMaster (
        final String id,
        final String owner,
        final double computationalPower,
        final double ignoredMemory,
        final double ignoredDisk,
        final double loadFactor,
        final String schedulingPolicyName,
        final String allocationPolicyName
    ) {
        super(id, owner, computationalPower, 1, loadFactor, 0);
        this.alocadorVM = new VmAllocationLoader().loadPolicy(allocationPolicyName);
        this.alocadorVM.setMestre(this);
        this.escalonador = new CloudSchedulingLoader().loadPolicy(schedulingPolicyName);
        this.escalonador.setMestre(this);
    }

    @Override
    public void clientEnter (final Simulation simulacao, final GridTask cliente) {
        if (cliente instanceof final CloudTask trf) {
            final var vm = trf.getVM_enviada();
            if (cliente.getCaminho().isEmpty()) {
                if (!this.maquinasVirtuais.contains(vm)) {
                    this.maquinasVirtuais.add(vm);
                    if (this.alocDisponivel) {
                        this.alocDisponivel = false;
                        this.alocadorVM.addVM(vm);
                        this.escalonador.addEscravo(vm);
                        this.executeAllocation();
                    } else {
                        this.alocadorVM.addVM(vm);
                        this.escalonador.addEscravo(vm);
                    }
                }
            } else {// se não for ele a origem ele precisa encaminhá-la
                final var evtFut = new Event(
                    simulacao.getTime(this),
                    EventType.ARRIVAL,
                    cliente.getCaminho().remove(0),
                    cliente
                );
                simulacao.addFutureEvent(evtFut);
            }
        } else { // cliente é tarefa comum
            if (cliente.getEstado() != TaskState.CANCELLED) {
                // Tarefas concluida possuem tratamento diferencial
                if (cliente.getEstado() == TaskState.DONE) {

                    // se não for origem da tarefa ela deve ser encaminhada
                    if (!cliente.getOrigem().equals(this)) {
                        // encaminhar tarefa!
                        // Gera evento para chegada da tarefa no proximo servidor
                        final var evtFut = new Event(
                            simulacao.getTime(this),
                            EventType.ARRIVAL,
                            cliente.getCaminho().remove(0),
                            cliente
                        );
                        // Adicionar na lista de eventos futuros
                        simulacao.addFutureEvent(evtFut);
                    }
                    // caso seja este o centro de serviço de origem

                    this.escalonador.addTarefaConcluida(cliente);

                    if (this.tipoEscalonamento.contains(Condition.WHEN_RECEIVES_RESULT)) {
                        if (this.escalonador.getFilaTarefas().isEmpty()) {
                            this.escDisponivel = true;
                        } else {
                            this.executeScheduling();
                        }
                    }
                } // Caso a tarefa está chegando pra ser escalonada
                else {
                    if (cliente.getCaminho() != null) {
                        final var evtFut = new Event(
                            simulacao.getTime(this),
                            EventType.ARRIVAL,
                            cliente.getCaminho().remove(0),
                            cliente
                        );
                        simulacao.addFutureEvent(evtFut);
                    } else {
                        if (this.escDisponivel) {

                            this.escDisponivel = false;
                            // escalonador adiciona nova tarefa
                            this.escalonador.adicionarTarefa(cliente);
                            // como o escalonador está disponível vai executar o escalonamento diretamente
                            this.executeScheduling();
                        } else {
                            // escalonador apenas adiciona a tarefa
                            this.escalonador.adicionarTarefa(cliente);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clientProcessing (final Simulation simulacao, final GridTask cliente) {
        // o VMM não irá processar tarefas... apenas irá escaloná-las..
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clientExit (final Simulation simulacao, final GridTask cliente) {
        if (cliente instanceof CloudTask) {
            final var evtFut = new Event(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                cliente.getCaminho().remove(0),
                cliente
            );
            // Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (this.tipoAlocacao.contains(Condition.WHILE_MUST_DISTRIBUTE)) {
                if (!this.alocadorVM.getMaquinasVirtuais().isEmpty()) {
                    this.executeAllocation();
                } else {
                    this.alocDisponivel = true;
                }
            }
        } else {
            final var evtFut = new Event(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                cliente.getCaminho().remove(0),
                cliente
            );
            // Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (this.tipoEscalonamento.contains(Condition.WHILE_MUST_DISTRIBUTE)) {
                // se fila de tarefas do servidor não estiver vazia escalona
                // proxima tarefa
                if (!this.escalonador.getFilaTarefas().isEmpty()) {
                    this.executeScheduling();
                } else {
                    this.escDisponivel = true;
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
        } else if (tipo == EventType.ALLOCATION) {
            this.alocadorVM.escalonar();// realizar a rotina de alocar a máquina virtual
        } else if (request != null) {
            if (request.getTipo() == RequestType.UPDATE) {
                this.handleUpdate(simulacao, request);
            } else if (request.getTipo() == RequestType.ALLOCATION_ACK) {
                this.handleAllocationAck(simulacao, request);
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
                // encaminhando request para o destino
                this.sendMessage(
                    request.getTarefa(),
                    (Processing) request.getTarefa().getLocalProcessamento(),
                    request.getTipo()
                );
            }
        }
        // deve incluir requisição para alocar..
    }

    @Override
    public List<Communication> connections () {
        return this.conexoesSaida;
    }

    @Override
    public void handleReturn (final Simulation simulacao, final Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handlePreemptiveReturn (final Simulation simulacao, final Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleUpdate (final Simulation simulacao, final Request request) {
        // atualiza metricas dos usuarios globais
        // enviar resultados
        final List<Service> caminho = new ArrayList<>(Objects.requireNonNull(
            Processing.getMenorCaminhoIndireto(this, (Processing) request.getOrigem())
        ));
        final var novaRequest = new Request(
            this, request.getTamComunicacao(), RequestType.UPDATE_RESULT
        );
        // Obtem informações dinâmicas
        novaRequest.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaRequest.getFilaEscravo().addAll(this.escalonador.getFilaTarefas());
        novaRequest.setCaminho(caminho);
        final var evtFut = new Event(
            simulacao.getTime(this),
            EventType.MESSAGE,
            novaRequest.getCaminho().remove(0),
            novaRequest
        );
        // Event adicionado a lista de evntos futuros
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
        // se este VMM for o de origem ele deve atender senão deve encaminhar a request para frente
        final var trf    = (CloudTask) request.getTarefa();
        final var auxVM  = trf.getVM_enviada();
        final var auxMaq = auxVM.getMaquinaHospedeira();

        if (auxVM.getVmmResponsavel().equals(this)) {
            // se o VMM responsável da VM for este.. tratar o ack
            // primeiro encontrar o caminho pra máquina onde a vm está alocada

            final var index = this.alocadorVM.getEscravos().indexOf(auxMaq);
            // busca índice da maquina na lista de máquinas físicas do vmm
            final ArrayList<Service> caminho;
            if (index == -1) {
                caminho = new ArrayList<>(Objects.requireNonNull(
                    Processing.getMenorCaminhoIndiretoCloud(this, auxMaq)
                ));
            } else {
                caminho = new ArrayList<Service>(this.caminhoEscravo.get(index));
            }

            this.determinarCaminhoVM(auxVM, caminho);
            auxVM.setStatus(VirtualMachineState.ALLOCATED);
            auxVM.setInstanteAloc(simulacao.getTime(this));
            if (!this.vmsAlocadas) {
                this.vmsAlocadas   = true;
                this.escDisponivel = true;
            }
        } else {
            // passar adiante, encontrando antes o caminho intermediário para poder escalonar tarefas desse VMM tbm
            //  para a vm hierarquica

            if (this.escalonador.getEscravos().contains(auxVM)) {
                final var index =
                    this.alocadorVM.getEscravos().indexOf(auxMaq);
                final ArrayList<Service> caminho;
                if (index == -1) {
                    caminho = new ArrayList<>(Objects.requireNonNull(
                        Processing.getMenorCaminhoIndiretoCloud(this, auxMaq)
                    ));
                } else {
                    caminho = new ArrayList<Service>(this.caminhoEscravo.get(index));
                }

                this.determinarCaminhoVM(auxVM, caminho);
            }
            final var evt = new Event(
                simulacao.getTime(this),
                EventType.MESSAGE,
                request.getCaminho().remove(0),
                request
            );
            simulacao.addFutureEvent(evt);
        }
    }

    @Override
    public void handleStop (final Simulation simulacao, final Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleCancel (final Simulation simulacao, final Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void executeAllocation () {
        final var evtFut = new Event(
            this.simulacao.getTime(this), EventType.ALLOCATION, this, null
        );
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public Set<Condition> getAllocationConditions () {
        return this.tipoAlocacao;
    }

    @Override
    public void setAllocationConditions (final Set<Condition> tipo) {
        this.tipoAlocacao = tipo;
    }

    @Override
    public void executeScheduling () {
        final var evtFut = new Event(
            this.simulacao.getTime(this), EventType.SCHEDULING, this, null
        );
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void setSchedulingConditions (final Set<Condition> newConditions) {
        this.tipoEscalonamento = newConditions;
    }

    @Override
    public void sendTask (final GridTask task) {
        // Gera evento para atender proximo cliente da lista
        final var evtFut = new Event(
            this.simulacao.getTime(this), EventType.EXIT, this, task
        );
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public GridTask cloneTask (final GridTask task) {
        final var tarefa = new GridTask(task);
        this.simulacao.addJob(tarefa);
        return tarefa;
    }

    @Override
    public void sendMessage (
        final GridTask task,
        final Processing slave,
        final RequestType requestType
    ) {
        final var msg = new Request(this, requestType, task);
        msg.setCaminho(this.escalonador.escalonarRota(slave));
        final var evtFut = new Event(
            this.simulacao.getTime(this), EventType.MESSAGE, msg.getCaminho().remove(0), msg
        );
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void freeScheduler () {
        this.escDisponivel = true;
    }

    @Override
    public void sendVm (final VirtualMachine vm) {
        final var tarefa = new CloudTask(vm.getVmmResponsavel(), vm, 300.0, 0.0);
        tarefa.setCaminho(vm.getCaminho());
        final var evtFut =
            new Event(this.simulacao.getTime(this), EventType.EXIT, this, tarefa);
        // Event adicionado a lista de evntos futuros
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
    public void addConexoesSaida (final Link link) {
        this.conexoesSaida.add(link);
    }

    @Override
    public void determinarCaminhos ()
        throws LinkageError {
        final var escravos = this.alocadorVM.getEscravos();
        this.caminhoEscravo = new ArrayList<>(escravos.size());
        // Busca pelo melhor caminho
        for (var i = 0; i < escravos.size(); i++) {
            this.caminhoEscravo.add(i, Processing.getMenorCaminho(this, escravos.get(i)));
        }
        // verifica se todos os escravos são alcansaveis
        for (var i = 0; i < escravos.size(); i++) {
            if (this.caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }

        this.alocadorVM.setCaminhoEscravo(this.caminhoEscravo);
    }

    private void determinarCaminhoVM (
        final Processing vm,
        final List<Service> caminhoVM
    ) {
        final var indVM = this.escalonador.getEscravos().indexOf(vm);
        System.out.println("indice da vm: " + indVM);
        if (indVM >= this.caminhoVMs.size()) {
            this.caminhoVMs.add(indVM, caminhoVM);
        } else {
            this.caminhoVMs.set(indVM, caminhoVM);
        }
        System.out.println("Lista atualizada de caminho para as vms:");
        for (var i = 0; i < this.caminhoVMs.size(); i++) {
            System.out.println(this.escalonador.getEscravos().get(i).id());
            System.out.println(this.caminhoVMs.get(i).toString());
        }
        this.escalonador.setCaminhoEscravo(this.caminhoVMs);
        System.out.println("------------------------------");
    }

    public CloudSchedulingPolicy getEscalonador () {
        return this.escalonador;
    }

    public VmAllocationPolicy getAlocadorVM () {
        return this.alocadorVM;
    }

    public void addConexoesSaida (final Communication Switch) {
        this.conexoesSaida.add(Switch);
    }

    public void addEscravo (final Processing maquina) {
        this.alocadorVM.addEscravo(maquina);
    }

    public void addVM (final VirtualMachine vm) {
        this.maquinasVirtuais.add(vm);
        this.alocadorVM.addVM(vm);
        this.escalonador.addEscravo(vm);
    }

    public void instanciarCaminhosVMs () {
        this.caminhoVMs = new ArrayList<>(this.escalonador.getEscravos().size());
        for (var i = 0; i < this.escalonador.getEscravos().size(); i++) {
            this.caminhoVMs.add(i, new ArrayList());
        }
    }
}
