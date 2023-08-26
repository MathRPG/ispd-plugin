package ispd.motor.filas.servidores.implementacao;

import ispd.motor.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import java.util.*;

public class CS_Internet extends CS_Comunicacao implements Vertice {

    private final List<CS_Link> conexoesEntrada = new ArrayList<>();

    private final List<CS_Link> conexoesSaida = new ArrayList<>();

    public CS_Internet (
        final String id, final double LarguraBanda, final double Ocupacao, final double Latencia
    ) {
        super(id, LarguraBanda, Ocupacao, Latencia);
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
        //cria evento para iniciar o atendimento imediatamente
        final var novoEvt = new FutureEvent(
            simulacao.getTime(this), EventType.SERVICE, this, cliente
        );
        simulacao.addFutureEvent(novoEvt);
    }

    @Override
    public void atendimento (final Simulation simulacao, final Tarefa cliente) {
        cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para atender proximo cliente da lista
        final var evtFut = new FutureEvent(
            simulacao.getTime(this) + this.tempoTransmitir(cliente.getTamComunicacao()),
            EventType.EXIT, this, cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
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
        final var evtFut = new FutureEvent(
            simulacao.getTime(this), EventType.ARRIVAL, cliente.getCaminho().remove(0), cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void requisicao (
        final Simulation simulacao,
        final Mensagem cliente,
        final EventType tipo
    ) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        final var tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Gera evento para chegada da tarefa no proximo servidor
        final var evtFut = new FutureEvent(
            simulacao.getTime(this) + tempoTrans,
            EventType.MESSAGE, cliente.getCaminho().remove(0), cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public List<CS_Link> getConexoesSaida () {
        return this.conexoesSaida;
    }
}
