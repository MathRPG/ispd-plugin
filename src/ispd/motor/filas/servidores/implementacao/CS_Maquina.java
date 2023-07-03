package ispd.motor.filas.servidores.implementacao;

import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

public class CS_Maquina extends CS_Processamento implements Mensagens, Vertice {

    private final List<CS_Comunicacao> conexoesEntrada = new ArrayList<>();

    private final List<CS_Comunicacao> conexoesSaida = new ArrayList<>();

    private final List<Tarefa> filaTarefas = new ArrayList<>();

    private final List<CS_Processamento> mestres = new ArrayList<>();

    private final List<Tarefa> tarefaEmExecucao;

    private final List<Double> falhas = new ArrayList<>();

    private final List<Double> recuperacao = new ArrayList<>();

    private final List<Tarefa> historicoProcessamento = new ArrayList<>();

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
     * @see #CS_Maquina(String, String, double, int, double, int, double) for specify the machine
     * number and energy consumption.
     */
    public CS_Maquina (
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
    public CS_Maquina (
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
     * @see #CS_Maquina(String, String, double, int, double, int, double) for specify the machine
     * number.
     */
    public CS_Maquina (
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
     * @see #CS_Maquina(String, String, double, int, double, int, double) for specify the energy
     * consumption
     */
    public CS_Maquina (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor, final int machineNumber
    ) {
        this(id, owner, computationalPower, coreCount, loadFactor, machineNumber, 0.0);
    }

    @Override
    public void addConexoesEntrada (final CS_Link conexao) {
        this.conexoesEntrada.add(conexao);
    }

    @Override
    public void addConexoesSaida (final CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    @Override
    public void chegadaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
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
            this.historicoProcessamento.add(cliente);
        }
    }

    @Override
    public void atendimento (final Simulation simulacao, final Tarefa cliente) {
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
            final Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            final FutureEvent evt = new FutureEvent(
                tFalha, FutureEvent.MENSAGEM, this, msg
            );
            simulacao.addFutureEvent(evt);
        } else {
            //Gera evento para atender proximo cliente da lista
            final FutureEvent evtFut = new FutureEvent(next, FutureEvent.SAIDA, this, cliente);
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
        if (this.mestres.contains(cliente.getOrigem())) {
            final int index = this.mestres.indexOf(cliente.getOrigem());
            final List<CentroServico> caminho =
                new ArrayList<>((List<CentroServico>) this.caminhoMestre.get(index));
            cliente.setCaminho(caminho);
            //Gera evento para chegada da tarefa no proximo servidor
            final FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.CHEGADA,
                cliente.getCaminho().remove(0),
                cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            //buscar menor caminho!!!
            final CS_Processamento novoMestre = (CS_Processamento) cliente.getOrigem();
            final List<CentroServico> caminho =
                new ArrayList<>(getMenorCaminhoIndireto(this, novoMestre));
            this.addMestre(novoMestre);
            this.caminhoMestre.add(caminho);
            cliente.setCaminho(new ArrayList<>(caminho));
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
        if (this.filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
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
    public List<CS_Comunicacao> getConexoesSaida () {
        return this.conexoesSaida;
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
            remover = simulacao.removeFutureEvent(FutureEvent.SAIDA, this, mensagem.getTarefa());
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
                        FutureEvent.ATENDIMENTO,
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
            // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
            final double numCP = ((int) (mflopsProcessados / 0.0)) *
                                 0.0;
            mensagem.getTarefa().setMflopsProcessado(numCP);
            //Incrementa desperdicio
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
        final int index = this.mestres.indexOf(mensagem.getOrigem());
        final List<CentroServico> caminho =
            new ArrayList<>((List<CentroServico>) this.caminhoMestre.get(index));
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
                // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
                final double numCP = ((int) (mflopsProcessados / 0.0)) * 0.0;
                tar.setMflopsProcessado(numCP);
                tar.setEstado(Tarefa.FALHA);
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

    public void addConexoesEntrada (final CS_Switch conexao) {
        this.conexoesEntrada.add(conexao);
    }

    public void addConexoesSaida (final CS_Switch conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addMestre (final CS_Processamento mestre) {
        this.mestres.add(mestre);
    }
}
