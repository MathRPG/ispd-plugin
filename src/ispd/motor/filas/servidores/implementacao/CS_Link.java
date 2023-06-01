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

    private CentroServico  conexoesEntrada;
    private CentroServico  conexoesSaida;
    private List<Tarefa>   filaPacotes;
    private List<Mensagem> filaMensagens;
    private boolean        linkDisponivel;
    private boolean        linkDisponivelMensagem;
    private double         tempoTransmitirMensagem;

    public CS_Link (String id, double LarguraBanda, double Ocupacao, double Latencia) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada         = null;
        this.conexoesSaida           = null;
        this.linkDisponivel          = true;
        this.filaPacotes             = new ArrayList<Tarefa>();
        this.filaMensagens           = new ArrayList<Mensagem>();
        this.tempoTransmitirMensagem = 0;
        this.linkDisponivelMensagem  = true;
    }

    public CentroServico getConexoesEntrada () {
        return conexoesEntrada;
    }

    public void setConexoesEntrada (CentroServico conexoesEntrada) {
        this.conexoesEntrada = conexoesEntrada;
    }

    @Override
    public void chegadaDeCliente (Simulation simulacao, Tarefa cliente) {
        cliente.iniciarEsperaComunicacao(simulacao.getTime(this));
        if (linkDisponivel) {
            //indica que recurso está ocupado
            linkDisponivel = false;
            //cria evento para iniciar o atendimento imediatamente
            FutureEvent novoEvt = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.ATENDIMENTO,
                    this,
                    cliente
            );
            simulacao.addFutureEvent(novoEvt);
        } else {
            filaPacotes.add(cliente);
        }
    }

    @Override
    public void atendimento (Simulation simulacao, Tarefa cliente) {
        if (!conexoesSaida.equals(cliente.getCaminho().get(0))) {
            System.out.println("link " + this.getId() + " tarefa " + cliente.getIdentificador() + " tempo " +
                               simulacao.getTime(this) + " local " + cliente.getCaminho().get(0).getId());
            throw new IllegalArgumentException("O destino da mensagem é um recurso sem conexão com este link");
        } else {
            cliente.finalizarEsperaComunicacao(simulacao.getTime(this));
            cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
            //Gera evento para atender proximo cliente da lista
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this) + tempoTransmitir(cliente.getTamComunicacao()),
                    FutureEvent.SAIDA,
                    this, cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void saidaDeCliente (Simulation simulacao, Tarefa cliente) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.CHEGADA,
                cliente.getCaminho().remove(0), cliente
        );
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
        if (filaPacotes.isEmpty()) {
            //Indica que está livre
            this.linkDisponivel = true;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaPacotes.remove(0);
            evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.ATENDIMENTO,
                    this, proxCliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void requisicao (Simulation simulacao, Mensagem cliente, int tipo) {
        if (tipo == FutureEvent.SAIDA_MENSAGEM) {
            tempoTransmitirMensagem += tempoTransmitir(cliente.getTamComunicacao());
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
            //Incrementa o tempo de transmissão
            double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
            this.getMetrica().incSegundosDeTransmissao(tempoTrans);
            //Gera evento para chegada da mensagem no proximo servidor
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this) + tempoTrans,
                    FutureEvent.MENSAGEM,
                    cliente.getCaminho().remove(0), cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (!filaMensagens.isEmpty()) {
                //Gera evento para chegada da mensagem no proximo servidor
                evtFut = new FutureEvent(
                        simulacao.getTime(this) + tempoTrans,
                        FutureEvent.SAIDA_MENSAGEM,
                        this, filaMensagens.remove(0)
                );
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            } else {
                linkDisponivelMensagem = true;
            }
        } else if (linkDisponivelMensagem) {
            linkDisponivelMensagem = false;
            //Gera evento para chegada da mensagem no proximo servidor
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.SAIDA_MENSAGEM,
                    this, cliente
            );
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            filaMensagens.add(cliente);
        }
    }

    @Override
    public CentroServico getConexoesSaida () {
        return conexoesSaida;
    }

    public void setConexoesSaida (CentroServico conexoesSaida) {
        this.conexoesSaida = conexoesSaida;
    }

    @Override
    public Integer getCargaTarefas () {
        if (linkDisponivel && linkDisponivelMensagem) {
            return 0;
        } else {
            return (filaMensagens.size() + filaPacotes.size()) + 1;
        }
    }
}
