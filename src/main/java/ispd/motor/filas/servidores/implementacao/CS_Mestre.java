package ispd.motor.filas.servidores.implementacao;

import ispd.motor.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.policy.*;
import ispd.policy.loaders.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

public class CS_Mestre extends CS_Processamento implements GridMaster, Mensagens, Vertice {

    private final List<CS_Comunicacao> conexoesEntrada = new ArrayList<>();

    private final List<CS_Comunicacao> conexoesSaida = new ArrayList<>();

    private final GridSchedulingPolicy escalonador;

    private final List<Tarefa> filaTarefas = new ArrayList<>();

    private boolean maqDisponivel = true;

    private boolean escDisponivel = true;

    private Set<Condition> tipoEscalonamento = Conditions.WHILE_MUST_DISTRIBUTE;

    private Simulation simulacao = null;

    public CS_Mestre (
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
    public void chegadaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
            //Tarefas concluida possuem tratamento diferencial
            if (cliente.getEstado() == Tarefa.CONCLUIDO) {
                //se não for origem da tarefa ela deve ser encaminhada
                if (!cliente.getOrigem().equals(this)) {
                    //encaminhar tarefa!
                    //Gera evento para chegada da tarefa no proximo servidor
                    final FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.CHEGADA,
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
    public void atendimento (final Simulation simulacao, final Tarefa cliente) {
        //o atendimento pode realiza o processamento da tarefa como em uma
        // maquina qualquer
        if (this.maqDisponivel) {
            this.maqDisponivel = false;
            cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
            cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para saida do cliente do servidor
            final FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this)
                + this.tempoProcessar(cliente.getTamProcessamento()
                                      - cliente.getMflopsProcessado()),
                FutureEvent.SAIDA, this, cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            this.filaTarefas.add(cliente);
        }
    }

    @Override
    public void saidaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        if (cliente.getEstado() == Tarefa.PROCESSANDO) {
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
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this), FutureEvent.ATENDIMENTO, this, proxCliente
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        } else {
            //Gera evento para chegada da tarefa no proximo servidor
            final FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.CHEGADA,
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
    public void requisicao (final Simulation simulacao, final Mensagem mensagem, final int tipo) {
        if (tipo == FutureEvent.ESCALONAR) {
            this.escalonador.escalonar();
        } else if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                this.atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null && mensagem
                .getTarefa()
                .getLocalProcessamento()
                .equals(this)) {
                switch (mensagem.getTipo()) {
                    case Mensagens.PARAR -> this.atenderParada(simulacao, mensagem);
                    case Mensagens.CANCELAR -> this.atenderCancelamento(simulacao, mensagem);
                    case Mensagens.DEVOLVER -> this.atenderDevolucao(simulacao, mensagem);
                    case Mensagens.DEVOLVER_COM_PREEMPCAO -> this.atenderDevolucaoPreemptiva(
                        simulacao,
                        mensagem
                    );
                }
            } else if (mensagem.getTipo() == Mensagens.RESULTADO_ATUALIZAR) {
                this.atenderRetornoAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null) {
                //encaminhando mensagem para o destino
                this.sendMessage(
                    mensagem.getTarefa(),
                    (CS_Processamento) mensagem.getTarefa().getLocalProcessamento(),
                    mensagem.getTipo()
                );
            }
        }
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida () {
        return this.conexoesSaida;
    }

    @Override
    public void atenderCancelamento (
        final Simulation simulacao, final Mensagem mensagem
    ) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(FutureEvent.SAIDA, this,
                                        mensagem.getTarefa()
            );
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this), FutureEvent.ATENDIMENTO, this, proxCliente
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
        //Incrementa procentagem da tarefa processada
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
                this.maqDisponivel = true;
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
        }
    }

    @Override
    public void atenderDevolucao (final Simulation simulacao, final Mensagem mensagem) {
        final boolean temp1 = this.filaTarefas.remove(mensagem.getTarefa());
        final boolean temp2 = this.escalonador.getFilaTarefas().remove(mensagem.getTarefa());
        if (temp1 || temp2) {
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
        boolean temp1 = false;
        boolean temp2 = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            temp1 = this.filaTarefas.remove(mensagem.getTarefa());
            temp2 = this.escalonador.getFilaTarefas().remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            temp1 = simulacao.removeFutureEvent(FutureEvent.SAIDA, this,
                                                mensagem.getTarefa()
            );
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
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
            final double inicioAtendimento =
                mensagem.getTarefa().parar(simulacao.getTime(this));
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
            mensagem.getTarefa().setMflopsProcessado(numCP);
            //Incrementa desperdicio
        }

        if (temp1 || temp2) {
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
    public void atenderAtualizacao (
        final Simulation simulacao,
        final Mensagem mensagem
    ) {
        //atualiza metricas dos usuarios globais
        //enviar resultados
        final List<CentroServico> caminho = new ArrayList<>(Objects.requireNonNull(
            CS_Processamento.getMenorCaminhoIndireto(this, (CS_Processamento) mensagem.getOrigem())
        ));

        final Mensagem novaMensagem =
            new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        novaMensagem.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaMensagem.getFilaEscravo().addAll(this.escalonador.getFilaTarefas());
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
        this.escalonador.resultadoAtualizar(mensagem);
    }

    @Override
    public void atenderFalha (final Simulation simulacao, final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderAckAlocacao (final Simulation simulacao, final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void executeScheduling () {
        final FutureEvent evtFut = new FutureEvent(
            this.simulacao.getTime(this),
            FutureEvent.ESCALONAR,
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
    public void sendTask (final Tarefa task) {
        //Gera evento para atender proximo cliente da lista
        final FutureEvent evtFut = new FutureEvent(
            this.simulacao.getTime(this), FutureEvent.SAIDA, this, task
        );
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public Tarefa cloneTask (final Tarefa task) {
        final Tarefa tarefa = new Tarefa(task);
        this.simulacao.addJob(tarefa);
        return tarefa;
    }

    @Override
    public void sendMessage (
        final Tarefa task,
        final CS_Processamento slave,
        final int messageType
    ) {
        final Mensagem msg = new Mensagem(this, messageType, task);
        msg.setCaminho(this.escalonador.escalonarRota(slave));
        final FutureEvent evtFut = new FutureEvent(
            this.simulacao.getTime(this), FutureEvent.MENSAGEM, msg.getCaminho().remove(0), msg
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
    public void addConexoesEntrada (final CS_Link link) {
        this.conexoesEntrada.add(link);
    }

    @Override
    public void addConexoesSaida (final CS_Link link) {
        this.conexoesSaida.add(link);
    }

    /**
     * Encontra caminhos para chegar até um escravo e adiciona no caminhoEscravo
     */
    @Override
    public void determinarCaminhos ()
        throws LinkageError {
        final List<CS_Processamento> escravos = this.escalonador.getEscravos();
        //Instancia objetos
        /**
         * Armazena os caminhos possiveis para alcançar cada escravo
         */
        final List<List> caminhoEscravo = new ArrayList<>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            caminhoEscravo.add(i, CS_Processamento.getMenorCaminho(this, escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
        this.escalonador.setCaminhoEscravo(caminhoEscravo);
    }

    public void atualizar (final CentroServico escravo, final Double time) {
        final Mensagem msg = new Mensagem(this, 0.011444091796875,
                                          Mensagens.ATUALIZAR
        );
        msg.setCaminho(this.escalonador.escalonarRota(escravo));
        final FutureEvent evtFut = new FutureEvent(
            time,
            FutureEvent.MENSAGEM,
            msg.getCaminho().remove(0),
            msg
        );
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    public GridSchedulingPolicy getEscalonador () {
        return this.escalonador;
    }

    public void addConexoesSaida (final CS_Comunicacao Switch) {
        this.conexoesSaida.add(Switch);
    }

    public void addConexoesEntrada (final CS_Comunicacao Switch) {
        this.conexoesEntrada.add(Switch);
    }

    public void addEscravo (final CS_Processamento maquina) {
        this.escalonador.addEscravo(maquina);
    }
}
