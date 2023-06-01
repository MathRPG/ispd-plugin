package ispd.motor.filas.servidores.implementacao;

import java.util.ArrayList;
import java.util.List;

import ispd.motor.FutureEvent;
import ispd.motor.Simulation;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CentroServico;


public class CS_Link extends CS_Comunicacao {

    private final List<Tarefa>   filaPacotes;
    private final List<Mensagem> filaMensagens;
    private       CentroServico  conexoesEntrada;
    private       CentroServico  conexoesSaida;
    private       boolean        linkDisponivel;
    private       boolean        linkDisponivelMensagem;

    public CS_Link (final String id, final double LarguraBanda, final double Ocupacao, final double Latencia) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada        = null;
        this.conexoesSaida          = null;
        this.linkDisponivel         = true;
        this.filaPacotes            = new ArrayList<>();
        this.filaMensagens          = new ArrayList<>();
        this.linkDisponivelMensagem = true;
    }

    public CentroServico getConexoesEntrada () {
        return this.conexoesEntrada;
    }

    public void setConexoesEntrada (final CentroServico conexoesEntrada) {
        this.conexoesEntrada = conexoesEntrada;
    }

    @Override
    public void chegadaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        cliente.iniciarEsperaComunicacao(simulacao.getTime(this));
        if (this.linkDisponivel) {
            //indica que recurso está ocupado
            this.linkDisponivel = false;
            //cria evento para iniciar o atendimento imediatamente
            final FutureEvent novoEvt =
                    new FutureEvent(simulacao.getTime(this), FutureEvent.ATENDIMENTO, this, cliente);
            simulacao.addFutureEvent(novoEvt);
        } else {
            this.filaPacotes.add(cliente);
        }
    }

    @Override
    public void atendimento (final Simulation simulacao, final Tarefa cliente) {
        if (!this.conexoesSaida.equals(cliente.getCaminho().get(0))) {
            System.out.println(
                    "link " + this.getId() + " tarefa " + cliente.getIdentificador() + " tempo " +
                    simulacao.getTime(this) + " local " + cliente.getCaminho().get(0).getId()
            );
            throw new IllegalArgumentException("O destino da mensagem é um recurso sem conexão com este link");
        } else {
            cliente.finalizarEsperaComunicacao(simulacao.getTime(this));
            cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
            //Gera evento para atender proximo cliente da lista
            final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this) + this.tempoTransmitir(cliente.getTamComunicacao()),
                    FutureEvent.SAIDA, this, cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void saidaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        final double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        FutureEvent evtFut =
                new FutureEvent(simulacao.getTime(this), FutureEvent.CHEGADA, cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
        if (this.filaPacotes.isEmpty()) {
            //Indica que está livre
            this.linkDisponivel = true;
        } else {
            //Gera evento para atender proximo cliente da lista
            final Tarefa proxCliente = this.filaPacotes.remove(0);
            evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.ATENDIMENTO, this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void requisicao (final Simulation simulacao, final Mensagem cliente, final int tipo) {
        if (tipo == FutureEvent.SAIDA_MENSAGEM) {
            this.tempoTransmitir(cliente.getTamComunicacao());
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
            //Incrementa o tempo de transmissão
            final double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
            this.getMetrica().incSegundosDeTransmissao(tempoTrans);
            //Gera evento para chegada da mensagem no proximo servidor
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this) + tempoTrans, FutureEvent.MENSAGEM, cliente.getCaminho().remove(0), cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (!this.filaMensagens.isEmpty()) {
                //Gera evento para chegada da mensagem no proximo servidor
                evtFut = new FutureEvent(
                        simulacao.getTime(this) + tempoTrans,
                        FutureEvent.SAIDA_MENSAGEM, this, this.filaMensagens.remove(0)
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            } else {
                this.linkDisponivelMensagem = true;
            }
        } else if (this.linkDisponivelMensagem) {
            this.linkDisponivelMensagem = false;
            //Gera evento para chegada da mensagem no proximo servidor
            final FutureEvent evtFut =
                    new FutureEvent(simulacao.getTime(this), FutureEvent.SAIDA_MENSAGEM, this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            this.filaMensagens.add(cliente);
        }
    }

    @Override
    public CentroServico getConexoesSaida () {
        return this.conexoesSaida;
    }

    public void setConexoesSaida (final CentroServico conexoesSaida) {
        this.conexoesSaida = conexoesSaida;
    }

    @Override
    public Integer getCargaTarefas () {
        if (this.linkDisponivel && this.linkDisponivelMensagem) {
            return 0;
        } else {
            return (this.filaMensagens.size() + this.filaPacotes.size()) + 1;
        }
    }
}
