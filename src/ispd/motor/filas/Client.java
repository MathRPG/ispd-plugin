package ispd.motor.filas;

import java.util.List;

import ispd.motor.filas.servidores.CentroServico;

public interface Client {

    public double getTamComunicacao ();

    public double getTamProcessamento ();

    public double getTimeCriacao ();

    public CentroServico getOrigem ();

    public List<CentroServico> getCaminho ();

    public void setCaminho (List<CentroServico> caminho);
}
