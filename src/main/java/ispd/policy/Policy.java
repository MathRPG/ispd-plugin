package ispd.policy;

import ispd.motor.queues.centers.*;
import java.util.*;

public abstract class Policy <T extends Simulable> {

    protected T mestre = null;

    protected List<List> caminhoEscravo = null;

    protected List<Processing> escravos = null;

    public abstract void iniciar ();

    public abstract List<Service> escalonarRota (Service destino);

    public abstract void escalonar ();

    public abstract Processing escalonarRecurso ();

    public Double getTempoAtualizar () {
        return null;
    }

    public T getMestre () {
        return this.mestre;
    }

    public void setMestre (final T mestre) {
        this.mestre = mestre;
    }

    public List<List> getCaminhoEscravo () {
        return this.caminhoEscravo;
    }

    public void setCaminhoEscravo (final List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public List<Processing> getEscravos () {
        return this.escravos;
    }

    public void addEscravo (final Processing newSlave) {
        this.escravos.add(newSlave);
    }
}
