package ispd.motor.queues.request;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import java.util.*;

public class Request implements Client {

    private static final double DEFAULT_COMMUNICATION_SIZE = 0.011_444_091_796_875;

    private final RequestType tipo;

    private final Service origem;

    private final double tamComunicacao;

    private GridTask tarefa = null;

    private List<Service> caminho = null;

    private List<GridTask> filaEscravo = null;

    private List<GridTask> processadorEscravo = null;

    public Request (final Processing origem, final RequestType tipo) {
        this(origem, DEFAULT_COMMUNICATION_SIZE, tipo);
    }

    public Request (final Processing origem, final RequestType tipo, final GridTask tarefa) {
        this(origem, tipo);
        this.tarefa  = tarefa;
        this.caminho = new ArrayList<>();
    }

    public Request (
        final Processing origem,
        final double tamComunicacao,
        final RequestType tipo
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
    public Service getOrigem () {
        return this.origem;
    }

    @Override
    public List<Service> getCaminho () {
        return this.caminho;
    }

    @Override
    public void setCaminho (final List<Service> caminho) {
        this.caminho = caminho;
    }

    public List<GridTask> getFilaEscravo () {
        return this.filaEscravo;
    }

    public void setFilaEscravo (final List<GridTask> filaEscravo) {
        this.filaEscravo = filaEscravo;
    }

    public List<GridTask> getProcessadorEscravo () {
        return this.processadorEscravo;
    }

    public void setProcessadorEscravo (final List<GridTask> processadorEscravo) {
        this.processadorEscravo = processadorEscravo;
    }

    public RequestType getTipo () {
        return this.tipo;
    }

    public GridTask getTarefa () {
        return this.tarefa;
    }
}
