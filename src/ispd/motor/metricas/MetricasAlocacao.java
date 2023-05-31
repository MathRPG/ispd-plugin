package ispd.motor.metricas;

import java.io.Serializable;

public class MetricasAlocacao implements Serializable {

    //Lista de atributos
    private String id;
    private int    numVMs;
    //métodos

    //construtor da classe
    public MetricasAlocacao (String id) {
        this.id     = id;
        this.numVMs = 0;
    }

    public void incVMsAlocadas () {
        this.numVMs++;
    }

    public int getNumVMs () {
        return numVMs;
    }

    public String getId () {
        return id;
    }


}
