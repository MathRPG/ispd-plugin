package ispd.motor.filas;

import java.util.List;

import ispd.motor.filas.servidores.CentroServico;

public interface Client {

    double getTamComunicacao ();

    double getTamProcessamento ();

    double getTimeCriacao ();

    CentroServico getOrigem ();

    List<CentroServico> getCaminho ();

    void setCaminho (List<CentroServico> caminho);
}
