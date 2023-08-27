package ispd.motor.filas.servidores.implementacao;

import ispd.motor.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.motor.metricas.*;
import java.util.*;

public class CS_MaquinaCloud extends CS_Processamento implements Mensagens, Vertice {

    public static final int DESLIGADO = 2;

    private final List<CS_Comunicacao> conexoesSaida = new ArrayList<>();

    private final List<Tarefa> filaTarefas = new ArrayList<>();

    private final List<Tarefa> tarefaEmExecucao;

    private final List<Double> recuperacao = new ArrayList<>();

    private final List<CS_VirtualMac> VMs = new ArrayList<>();

    private final List<CS_Processamento> mestres = new ArrayList<>();

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

    private MetricasAlocacao metricaAloc = null;

    public CS_MaquinaCloud (
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
        this.metricaAloc              = new MetricasAlocacao(id);
        this.processadoresDisponiveis = numeroProcessadores;
        this.tarefaEmExecucao         = new ArrayList<>(numeroProcessadores);
        this.memoriaDisponivel        = memoria;
        this.discoDisponivel          = disco;
        this.custoProc                = custoProc;
        this.custoMemoria             = custoMem;
        this.custoDisco               = custoDisco;
    }

    public CS_MaquinaCloud (
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
    public void chegadaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        System.out.println("----------------------------------------------");
        System.out.println("Chegada de evento na  máquina " + this.getId());
        if (cliente instanceof final TarefaVM trf) {
            final CS_VirtualMac vm = trf.getVM_enviada();
            if (vm.getMaquinaHospedeira().equals(this)) {
                if (this.VMs.contains(vm)) {
                    System.out.println("Cliente duplicado!");
                } else {
                    System.out.println(vm.getId()
                                       + " enviada para evento de atendimento nesta máquina");
                    System.out.println("----------------------------------------------");
                    final FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this), EventType.SERVICE, this, cliente
                    );
                    simulacao.addFutureEvent(evtFut);
                }
            } else {
                System.out.println(vm.getId()
                                   + " encaminhada para seu destino, esta máquina é intermediária");
                System.out.println("----------------------------------------------");
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    EventType.ARRIVAL,
                    cliente.getCaminho().remove(0),
                    cliente
                );
                simulacao.addFutureEvent(evtFut);
            }
        } else {
            //procedimento caso cliente seja uma tarefa!
            final CS_VirtualMac vm = (CS_VirtualMac) cliente.getLocalProcessamento();
            if (vm.getMaquinaHospedeira().equals(this)) {
                //se a tarefa é endereçada pra uma VM qu está alocada nessa máquina
                System.out.println(this.getId() + ": Tarefa " + cliente.getIdentificador() +
                                   " sendo enviada para execução na vm " + vm.getId());
                System.out.println("----------------------------------------------");
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this), EventType.ARRIVAL, vm, cliente
                );
                simulacao.addFutureEvent(evtFut);
            } else {
                System.out.println(
                    this.getId()
                    + ": Tarefa "
                    + cliente.getIdentificador()
                    + " sendo encaminhada para próximo CS");
                System.out.println("----------------------------------------------");
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this), EventType.EXIT, this, cliente
                );
                simulacao.addFutureEvent(evtFut);
            }
        }
    }

    @Override
    public void atendimento (final Simulation simulacao, final Tarefa cliente) {
        final TarefaVM      trf = (TarefaVM) cliente;
        final CS_VirtualMac vm  = trf.getVM_enviada();
        System.out.println("--------------------------------------------------");
        System.out.println("atendimento da vm:" + vm.getId() + "na maquina:" + this.getId());
        this.addVM(vm); //incluir a VM na lista de VMs
        this.metricaAloc.incVMsAlocadas();
        //Setar o caminho da vm para o VMM e o caminho do ACK da mensagem >>>
        final CS_VMM   vmm   = vm.getVmmResponsavel();
        final int      index = this.mestres.indexOf(vmm);
        final Mensagem msg   = new Mensagem(this, MessageType.ALLOCATION_ACK, cliente);

        if (index == -1) {
            final ArrayList<CentroServico> caminhoVMM =
                new ArrayList<>(getMenorCaminhoIndiretoCloud(this, vmm));
            final ArrayList<CentroServico> caminhoMsg =
                new ArrayList<>(getMenorCaminhoIndiretoCloud(this, vmm));

            System.out.println("Imprimindo caminho para o mestre:");
            for (final CentroServico cs : caminhoVMM) {
                System.out.println(cs.getId());
            }

            vm.setCaminhoVMM(caminhoVMM);
            msg.setCaminho(caminhoMsg);
        } else {
            final ArrayList<CentroServico> caminhoVMM =
                new ArrayList<CentroServico>(this.caminhoMestre.get(index));
            final ArrayList<CentroServico> caminhoMsg =
                new ArrayList<CentroServico>(this.caminhoMestre.get(index));

            System.out.println("Imprimindo caminho para o mestre:");
            for (final CentroServico cs : caminhoVMM) {
                System.out.println(cs.getId());
            }
            vm.setCaminhoVMM(caminhoVMM);
            msg.setCaminho(caminhoMsg);
        }

        //enviar mensagem de ACK para o VMM
        final FutureEvent NovoEvt =
            new FutureEvent(simulacao.getTime(this), EventType.MESSAGE, this, msg);
        simulacao.addFutureEvent(NovoEvt);

        //Gerenciamento de custos
        this.custoTotalProc    =
            this.custoTotalProc + (vm.getProcessadoresDisponiveis() * this.custoProc);
        this.custoTotalMemoria =
            this.custoTotalMemoria + (vm.getMemoriaDisponivel() * this.custoMemoria);
        this.custoTotalDisco   = this.custoTotalDisco + (vm.getDiscoDisponivel() * this.custoDisco);
        //setar o poder de processamento da VM.
        vm.setPoderProcessamentoPorNucleo(this.getPoderComputacional());
        System.out.println("----------------------------------------------------");
    }

    @Override
    public void saidaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        System.out.println("--------------------------------------");
        System.out.println(this.getId() + ": Saída de cliente");
        System.out.println("--------------------------------------");
    }

    @Override
    public void requisicao (
        final Simulation simulacao,
        final Mensagem mensagem,
        final EventType tipo
    ) {
        if (mensagem != null) {
            if (mensagem.getTipo() == MessageType.UPDATE) {
                this.atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTipo()
                       == MessageType.ALLOCATION_ACK) { //a máquina é só um intermediário
                //esse tipo de mensagem só é atendido por um VMM
                this.atenderAckAlocacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null && mensagem
                .getTarefa()
                .getLocalProcessamento()
                .equals(this)) {
                switch (mensagem.getTipo()) {
                    case STOP -> this.atenderParada(simulacao, mensagem);
                    case CANCEL -> this.atenderCancelamento(
                        simulacao,
                        mensagem
                    );
                    case RETURN -> this.atenderDevolucao(
                        simulacao,
                        mensagem
                    );
                    case PREEMPTIVE_RETURN -> this.atenderDevolucaoPreemptiva(
                        simulacao, mensagem);
                    case FAIL -> this.atenderFalha(simulacao, mensagem);
                }
            }
        }
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida () {
        return this.conexoesSaida;
    }

    @Override
    public void atenderCancelamento (final Simulation simulacao, final Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == TaskState.PROCESSING) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(EventType.EXIT, this, mensagem.getTarefa());
            this.tarefaEmExecucao.remove(mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this), EventType.SERVICE, this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        }
        final double inicioAtendimento = mensagem.getTarefa().cancelar(simulacao.getTime(this));
        final double tempoProc         = simulacao.getTime(this) - inicioAtendimento;
        final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
        //Incrementa o número de Mflops processados por este recurso
        this.getMetrica().incMflopsProcessados(mflopsProcessados);
        //Incrementa o tempo de processamento
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa porcentagem da tarefa processada
        mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void atenderParada (final Simulation simulacao, final Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == TaskState.PROCESSING) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(EventType.EXIT, this, mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut =
                    new FutureEvent(
                        simulacao.getTime(this),
                        EventType.SERVICE,
                        this,
                        proxCliente
                    );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final double inicioAtendimento = mensagem.getTarefa().parar(simulacao.getTime(this));
            final double tempoProc         = simulacao.getTime(this) - inicioAtendimento;
            final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
            this.tarefaEmExecucao.remove(mensagem.getTarefa());
            this.filaTarefas.add(mensagem.getTarefa());
        }
    }

    @Override
    public void atenderDevolucao (final Simulation simulacao, final Mensagem mensagem) {
        final boolean remover = this.filaTarefas.remove(mensagem.getTarefa());
        if (remover) {
            final FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                mensagem.getTarefa().getOrigem(),
                mensagem.getTarefa()
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void atenderDevolucaoPreemptiva (final Simulation simulacao, final Mensagem mensagem) {
        boolean remover = false;
        if (mensagem.getTarefa().getEstado() == TaskState.BLOCKED) {
            remover = this.filaTarefas.remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == TaskState.PROCESSING) {
            remover = simulacao.removeFutureEvent(EventType.EXIT, this, mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut =
                    new FutureEvent(
                        simulacao.getTime(this),
                        EventType.SERVICE,
                        this,
                        proxCliente
                    );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final double inicioAtendimento = mensagem.getTarefa().parar(simulacao.getTime(this));
            final double tempoProc         = simulacao.getTime(this) - inicioAtendimento;
            final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            final int numCP = (int) (mflopsProcessados / 0.0);
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            mensagem.getTarefa().setMflopsProcessado(numCP * 0.0);
            this.tarefaEmExecucao.remove(mensagem.getTarefa());
        }
        if (remover) {
            final FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                mensagem.getTarefa().getOrigem(),
                mensagem.getTarefa()
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void atenderAtualizacao (final Simulation simulacao, final Mensagem mensagem) {
        //enviar resultados
        final int index = this.mestres.indexOf(mensagem.getOrigem());
        final List<CentroServico> caminho =
            new ArrayList<>((List<CentroServico>) this.caminhoMestre.get(index));
        final Mensagem novaMensagem =
            new Mensagem(this, mensagem.getTamComunicacao(), MessageType.UPDATE_RESULT);
        //Obtem informações dinâmicas
        novaMensagem.setProcessadorEscravo(new ArrayList<>(this.tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaMensagem.setCaminho(caminho);
        final FutureEvent evtFut = new FutureEvent(
            simulacao.getTime(this),
            EventType.MESSAGE,
            novaMensagem.getCaminho().remove(0),
            novaMensagem
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void atenderRetornoAtualizacao (final Simulation simulacao, final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderFalha (final Simulation simulacao, final Mensagem mensagem) {
        final double tempoRec = this.recuperacao.remove(0);
        for (final Tarefa tar : this.tarefaEmExecucao) {
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
                final int numCP = (int) (mflopsProcessados / 0.0);
                // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
                tar.setMflopsProcessado(numCP * 0.0);
                if (false) {
                    //Reiniciar atendimento da tarefa
                    tar.iniciarEsperaProcessamento(simulacao.getTime(this));
                    //cria evento para iniciar o atendimento imediatamente
                    final FutureEvent novoEvt = new FutureEvent(
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
    public void atenderAckAlocacao (final Simulation simulacao, final Mensagem mensagem) {
        //quem deve resolver esse método é o VMM de origem
        //portanto as maquinas só encaminham pro próximo centro de serviço.
        System.out.println("--------------------------------------");
        System.out.println("Encaminhando ACK de alocação para " + mensagem.getOrigem().getId());
        final FutureEvent evt = new FutureEvent(
            simulacao.getTime(this), EventType.MESSAGE, mensagem.getCaminho().remove(0), mensagem
        );
        simulacao.addFutureEvent(evt);
    }

    @Override
    public void determinarCaminhos ()
        throws LinkageError {
        //Instancia objetos
        this.caminhoMestre = new ArrayList<>(this.mestres.size());
        System.out.println(
            "maquina "
            + this.getId()
            + " determinando caminhos para "
            + this.mestres.size()
            + " mestres");

        //Busca pelos caminhos
        for (int i = 0; i < this.mestres.size(); i++) {
            this.caminhoMestre.add(i, getMenorCaminhoCloud(
                this,
                this.mestres.get(i)
            ));
        }

        //verifica se todos os mestres são alcansaveis
        for (int i = 0; i < this.mestres.size(); i++) {
            if (this.caminhoMestre.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
    }

    @Override
    public void addConexoesSaida (final CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    private void addVM (final CS_VirtualMac vm) {
        this.VMs.add(vm);
    }

    public MetricasAlocacao getMetricaAloc () {
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

    public void addConexoesSaida (final CS_Switch conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addMestre (final CS_Processamento mestre) {
        this.mestres.add(mestre);
    }

    public void desligar (final Simulation simulacao) {
        for (final CS_VirtualMac vm : this.VMs) {
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
