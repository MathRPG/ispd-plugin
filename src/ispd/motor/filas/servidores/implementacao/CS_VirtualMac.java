package ispd.motor.filas.servidores.implementacao;

import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasCusto;
import java.util.ArrayList;
import java.util.List;

public class CS_VirtualMac extends CS_Processamento implements Client, Mensagens {

    public static final int ALOCADA = 2;

    public static final int REJEITADA = 3;

    public static final int DESTRUIDA = 4;

    private static final int LIVRE = 1;

    private final List<List> caminhoIntermediarios = new ArrayList<>();

    private final List<Tarefa> filaTarefas = new ArrayList<>();

    private final List<Tarefa> tarefaEmExecucao;

    private final List<CS_VMM> VMMsIntermediarios = new ArrayList<>();

    private final MetricasCusto metricaCusto;

    private final List<Double> falhas = new ArrayList<>();

    private final List<Double> recuperacao = new ArrayList<>();

    private final double memoriaDisponivel;

    private final double discoDisponivel;

    private CS_VMM vmmResponsavel = null;

    private int processadoresDisponiveis;

    private double instanteAloc = 0.0;

    private double tempoDeExec = 0;

    private CS_MaquinaCloud maquinaHospedeira = null;

    private List<CentroServico> caminho = null;

    private List<CentroServico> caminhoVMM = null;

    private int status = CS_VirtualMac.LIVRE;

    public CS_VirtualMac (
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
        this.metricaCusto             = new MetricasCusto(id);
        this.tarefaEmExecucao         = new ArrayList<>(numeroProcessadores);
    }

    @Override
    public void chegadaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) { //se a tarefa estiver parada ou executando
            cliente.iniciarEsperaProcessamento(simulacao.getTime(this));
            if (this.processadoresDisponiveis != 0) {
                //indica que recurso está ocupado
                this.processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                final FutureEvent novoEvt = new FutureEvent(
                    simulacao.getTime(this), FutureEvent.ATENDIMENTO, this, cliente
                );
                simulacao.addFutureEvent(novoEvt);
            } else {
                this.filaTarefas.add(cliente);
            }
        }
    }

    @Override
    public void atendimento (final Simulation simulacao, final Tarefa cliente) {
        cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
        if (cliente == null) {
            System.out.println("cliente nao existe");
        } else {
            System.out.println("cliente é a tarefa " + cliente.getIdentificador());
        }
        this.tarefaEmExecucao.add(cliente);
        final Double next = simulacao.getTime(this)
                            + this.tempoProcessar(cliente.getTamProcessamento()
                                                  - cliente.getMflopsProcessado());
        if (!this.falhas.isEmpty() && next > this.falhas.get(0)) {
            Double tFalha = this.falhas.remove(0);
            if (tFalha < simulacao.getTime(this)) {
                tFalha = simulacao.getTime(this);
            }
            final Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            final FutureEvent evt = new FutureEvent(
                tFalha, FutureEvent.MENSAGEM, this, msg
            );
            simulacao.addFutureEvent(evt);
        } else {
            //Gera evento para atender proximo cliente da lista
            final FutureEvent evtFut = new FutureEvent(
                next, FutureEvent.SAIDA, this, cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void saidaDeCliente (final Simulation simulacao, final Tarefa cliente) {
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
        //eficiencia calculada apenas nas classes CS_Maquina
        cliente.calcEficiencia(this.getPoderComputacional());
        //Devolve tarefa para o mestre

        final CentroServico Origem = cliente.getOrigem();
        final ArrayList<CentroServico> caminho;
        if (Origem.equals(this.vmmResponsavel)) {
            caminho = new ArrayList<>(this.caminhoVMM);
        } else {
            System.out.println("A tarefa não saiu do vmm desta vm!!!!!");
            final int index = this.VMMsIntermediarios.indexOf((CS_VMM) Origem);
            if (index == -1) {
                final CS_MaquinaCloud auxMaq = this.maquinaHospedeira;
                final ArrayList<CentroServico> caminhoInter =
                    new ArrayList<>(getMenorCaminhoIndiretoCloud(
                        auxMaq,
                        (CS_Processamento) Origem
                    ));
                caminho = new ArrayList<>(caminhoInter);
                this.VMMsIntermediarios.add((CS_VMM) Origem);
                final int idx = this.VMMsIntermediarios.indexOf((CS_VMM) Origem);
                this.caminhoIntermediarios.add(idx, caminhoInter);
            } else {
                caminho = new ArrayList<CentroServico>(this.caminhoIntermediarios.get(index));
            }
        }
        cliente.setCaminho(caminho);
        System.out.println("Saida -" + this.getId() + "- caminho size:" + caminho.size());

        //Gera evento para chegada da tarefa no proximo servidor
        final FutureEvent evtFut = new FutureEvent(
            simulacao.getTime(this),
            FutureEvent.CHEGADA,
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
            final Tarefa proxCliente = this.filaTarefas.remove(0);
            final FutureEvent NovoEvt = new FutureEvent(
                simulacao.getTime(this), FutureEvent.ATENDIMENTO, this, proxCliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(NovoEvt);
        }
    }

    @Override
    public void requisicao (final Simulation simulacao, final Mensagem mensagem, final int tipo) {
        if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                this.atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null && mensagem
                .getTarefa()
                .getLocalProcessamento()
                .equals(this)) {
                switch (mensagem.getTipo()) {
                    case ispd.motor.Mensagens.PARAR -> this.atenderParada(simulacao, mensagem);
                    case ispd.motor.Mensagens.CANCELAR -> this.atenderCancelamento(
                        simulacao,
                        mensagem
                    );
                    case ispd.motor.Mensagens.DEVOLVER -> this.atenderDevolucao(
                        simulacao,
                        mensagem
                    );
                    case ispd.motor.Mensagens.DEVOLVER_COM_PREEMPCAO -> this.atenderDevolucaoPreemptiva(
                        simulacao, mensagem);
                    case ispd.motor.Mensagens.FALHAR -> this.atenderFalha(simulacao, mensagem);
                }
            }
        }
    }

    @Override
    public Object getConexoesSaida () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderCancelamento (final Simulation simulacao, final Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(FutureEvent.SAIDA, this, mensagem.getTarefa());
            this.tarefaEmExecucao.remove(mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.ATENDIMENTO,
                    this, proxCliente
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
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(FutureEvent.SAIDA, this, mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.ATENDIMENTO,
                    this, proxCliente
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
                FutureEvent.CHEGADA,
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
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            remover = this.filaTarefas.remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            remover = simulacao.removeFutureEvent(
                FutureEvent.SAIDA,
                this,
                mensagem.getTarefa()
            );
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.ATENDIMENTO,
                    this, proxCliente
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
                FutureEvent.CHEGADA,
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
        final List<CentroServico> caminho = new ArrayList<>(this.caminhoVMM);
        final Mensagem novaMensagem =
            new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        novaMensagem.setProcessadorEscravo(new ArrayList<>(this.tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaMensagem.setCaminho(caminho);
        final FutureEvent evtFut = new FutureEvent(
            simulacao.getTime(this),
            FutureEvent.MENSAGEM,
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
        this.recuperacao.remove(0);
        for (final Tarefa tar : this.tarefaEmExecucao) {
            if (tar.getEstado() == Tarefa.PROCESSANDO) {
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
                tar.setEstado(ispd.motor.filas.Tarefa.FALHA);
            }
        }
        this.processadoresDisponiveis += this.tarefaEmExecucao.size();
        this.filaTarefas.clear();
        this.tarefaEmExecucao.clear();
    }

    @Override
    public void atenderAckAlocacao (final Simulation simulacao, final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public CentroServico getOrigem () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<CentroServico> getCaminho () {
        return this.caminho;
    }

    @Override
    public void setCaminho (final List<CentroServico> caminho) {
        this.caminho = caminho;
    }

    public CS_MaquinaCloud getMaquinaHospedeira () {
        return this.maquinaHospedeira;
    }

    public void setMaquinaHospedeira (final CS_MaquinaCloud maquinaHospedeira) {
        this.maquinaHospedeira = maquinaHospedeira;
    }

    public CS_VMM getVmmResponsavel () {
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

    public void setCaminhoVMM (final List<CentroServico> caminhoMestre) {
        this.caminhoVMM = caminhoMestre;
    }

    public void addVMM (final CS_VMM vmmResponsavel) {
        this.vmmResponsavel = vmmResponsavel;
    }

    public int getStatus () {
        return this.status;
    }

    public void setStatus (final int status) {
        this.status = status;
    }

    public MetricasCusto getMetricaCusto () {
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
