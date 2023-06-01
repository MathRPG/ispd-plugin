package ispd.motor.metricas;

import java.io.Serializable;

public class MetricasAlocacao implements Serializable {

    private final String id;
    private       int    numVMs;

    public MetricasAlocacao (final String id) {
        this.id     = id;
        this.numVMs = 0;
    }

    public void incVMsAlocadas () {
        this.numVMs++;
    }

    public int getNumVMs () {
        return this.numVMs;
    }

    public String getId () {
        return this.id;
    }
}
