package ispd.motor.filas;

import ispd.motor.*;
import ispd.motor.filas.servidores.*;
import java.util.*;

public class Mensagem implements Client {

    private static final double DEFAULT_COMMUNICATION_SIZE = 0.011_444_091_796_875;

    private final MessageType tipo;

    private final CentroServico origem;

    private final double tamComunicacao;

    private Tarefa tarefa = null;

    private List<CentroServico> caminho = null;

    private List<Tarefa> filaEscravo = null;

    private List<Tarefa> processadorEscravo = null;

    public Mensagem (final CS_Processamento origem, final MessageType tipo) {
        this(origem, DEFAULT_COMMUNICATION_SIZE, tipo);
    }

    public Mensagem (final CS_Processamento origem, final MessageType tipo, final Tarefa tarefa) {
        this(origem, tipo);
        this.tarefa  = tarefa;
        this.caminho = new ArrayList<>();
    }

    public Mensagem (
        final CS_Processamento origem,
        final double tamComunicacao,
        final MessageType tipo
    ) {
        this.origem         = origem;
        this.tipo           = tipo;
        this.tamComunicacao = tamComunicacao;
    }

    @Override
    public double getTamComunicacao () {
        return this.tamComunicacao;
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
        return this.origem;
    }

    @Override
    public List<CentroServico> getCaminho () {
        return this.caminho;
    }

    @Override
    public void setCaminho (final List<CentroServico> caminho) {
        this.caminho = caminho;
    }

    public List<Tarefa> getFilaEscravo () {
        return this.filaEscravo;
    }

    public void setFilaEscravo (final List<Tarefa> filaEscravo) {
        this.filaEscravo = filaEscravo;
    }

    public List<Tarefa> getProcessadorEscravo () {
        return this.processadorEscravo;
    }

    public void setProcessadorEscravo (final List<Tarefa> processadorEscravo) {
        this.processadorEscravo = processadorEscravo;
    }

    public MessageType getTipo () {
        return this.tipo;
    }

    public Tarefa getTarefa () {
        return this.tarefa;
    }
}
