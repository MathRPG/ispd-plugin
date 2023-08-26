package ispd.motor.filas.servidores.implementacao;

import ispd.motor.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import java.util.*;

public class CS_Link extends CS_Comunicacao {

    private final List<Tarefa> filaPacotes = new ArrayList<>();

    private final List<Mensagem> filaMensagens = new ArrayList<>();

    private CentroServico conexoesSaida = null;

    private boolean linkDisponivel = true;

    private boolean linkDisponivelMensagem = true;

    public CS_Link (
        final String id, final double LarguraBanda, final double Ocupacao, final double Latencia
    ) {
        super(id, LarguraBanda, Ocupacao, Latencia);
    }

    @Override
    public void chegadaDeCliente (final Simulation simulacao, final Tarefa cliente) {
        cliente.iniciarEsperaComunicacao(simulacao.getTime(this));
        if (this.linkDisponivel) {
            //indica que recurso está ocupado
            this.linkDisponivel = false;
            //cria evento para iniciar o atendimento imediatamente
            final var novoEvt =
                new FutureEvent(simulacao.getTime(this), EventType.SERVICE, this, cliente);
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
            throw new IllegalArgumentException(
                "O destino da mensagem é um recurso sem conexão com este link");
        } else {
            cliente.finalizarEsperaComunicacao(simulacao.getTime(this));
            cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
            //Gera evento para atender proximo cliente da lista
            final var evtFut = new FutureEvent(
                simulacao.getTime(this) + this.tempoTransmitir(cliente.getTamComunicacao()),
                EventType.EXIT, this, cliente
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
        final var tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        var evtFut =
            new FutureEvent(
                simulacao.getTime(this),
                EventType.ARRIVAL,
                cliente.getCaminho().remove(0),
                cliente
            );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
        if (this.filaPacotes.isEmpty()) {
            //Indica que está livre
            this.linkDisponivel = true;
        } else {
            //Gera evento para atender proximo cliente da lista
            final var proxCliente = this.filaPacotes.remove(0);
            evtFut = new FutureEvent(
                simulacao.getTime(this),
                EventType.SERVICE,
                this,
                proxCliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void requisicao (
        final Simulation simulacao,
        final Mensagem cliente,
        final EventType tipo
    ) {
        if (tipo == EventType.MESSAGE_EXIT) {
            this.tempoTransmitir(cliente.getTamComunicacao());
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
            //Incrementa o tempo de transmissão
            final var tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
            this.getMetrica().incSegundosDeTransmissao(tempoTrans);
            //Gera evento para chegada da mensagem no proximo servidor
            var evtFut = new FutureEvent(
                simulacao.getTime(this) + tempoTrans,
                EventType.MESSAGE,
                cliente.getCaminho().remove(0),
                cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (!this.filaMensagens.isEmpty()) {
                //Gera evento para chegada da mensagem no proximo servidor
                evtFut = new FutureEvent(
                    simulacao.getTime(this) + tempoTrans,
                    EventType.MESSAGE_EXIT, this, this.filaMensagens.remove(0)
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            } else {
                this.linkDisponivelMensagem = true;
            }
        } else if (this.linkDisponivelMensagem) {
            this.linkDisponivelMensagem = false;
            //Gera evento para chegada da mensagem no proximo servidor
            final var evtFut =
                new FutureEvent(simulacao.getTime(this), EventType.MESSAGE_EXIT, this, cliente);
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
}
