package ispd.motor.queues.centers.impl;

import ispd.motor.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import java.util.*;

public class Internet extends Communication implements Vertex {

    private final List<Link> conexoesSaida = new ArrayList<>();

    public Internet (
        final String id, final double LarguraBanda, final double Ocupacao, final double Latencia
    ) {
        super(id, LarguraBanda, Ocupacao, Latencia);
    }

    @Override
    public void addOutboundConnection (final Link link) {
        this.conexoesSaida.add(link);
    }

    @Override
    public void clientEnter (final Simulation simulacao, final GridTask cliente) {
        //cria evento para iniciar o atendimento imediatamente
        final var novoEvt = new Event(
            simulacao.getTime(this), EventType.SERVICE, this, cliente
        );
        simulacao.addFutureEvent(novoEvt);
    }

    @Override
    public void clientProcessing (final Simulation simulacao, final GridTask cliente) {
        cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para atender proximo cliente da lista
        final var evtFut = new Event(
            simulacao.getTime(this) + this.tempoTransmitir(cliente.getTamComunicacao()),
            EventType.EXIT, this, cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void clientExit (final Simulation simulacao, final GridTask cliente) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        final var tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        final var evtFut = new Event(
            simulacao.getTime(this), EventType.ARRIVAL, cliente.getCaminho().remove(0), cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void requestProcessing (
        final Simulation simulacao,
        final Request cliente,
        final EventType tipo
    ) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        final var tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Gera evento para chegada da tarefa no proximo servidor
        final var evtFut = new Event(
            simulacao.getTime(this) + tempoTrans,
            EventType.MESSAGE, cliente.getCaminho().remove(0), cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public List<Link> connections () {
        return this.conexoesSaida;
    }
}
