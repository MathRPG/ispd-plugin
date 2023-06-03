package ispd.motor.filas;

import java.util.ArrayList;
import java.util.List;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;

public class Mensagem implements Client {

    private final int                 tipo;
    private final CentroServico       origem;
    private final double              tamComunicacao;
    private       Tarefa              tarefa;
    private       List<CentroServico> caminho;
    private       List<Tarefa>        filaEscravo;
    private       List<Tarefa>        processadorEscravo;

    public Mensagem (final CS_Processamento origem, final int tipo) {
        this.origem         = origem;
        this.tipo           = tipo;
        this.tamComunicacao = 0.011444091796875;
    }

    public Mensagem (final CS_Processamento origem, final int tipo, final Tarefa tarefa) {
        this.origem         = origem;
        this.tipo           = tipo;
        this.tamComunicacao = 0.011444091796875;
        this.tarefa         = tarefa;
        this.caminho        = new ArrayList<CentroServico>();
    }

    public Mensagem (final CS_Processamento origem, final double tamComunicacao, final int tipo) {
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

    public int getTipo () {
        return this.tipo;
    }

    public Tarefa getTarefa () {
        return this.tarefa;
    }
}
