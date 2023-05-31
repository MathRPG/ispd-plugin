package ispd.motor.metricas;

import java.io.Serializable;

public class MetricasCusto implements Serializable {

    //lista de atributos
    private String id; //id da máquina virtual que se está calculando as métricas
    private double custoProc;
    private double custoMem;
    private double custoDisco;

    public MetricasCusto (String id) {
        this.id         = id;
        this.custoProc  = 0;
        this.custoMem   = 0;
        this.custoDisco = 0;
    }

    public String getId () {
        return id;
    }

    public double getCustoProc () {
        return custoProc;
    }

    public void setCustoProc (double custoProc) {
        this.custoProc = custoProc;
    }

    public double getCustoMem () {
        return custoMem;
    }

    public void setCustoMem (double custoMem) {
        this.custoMem = custoMem;
    }

    public double getCustoDisco () {
        return custoDisco;
    }

    public void setCustoDisco (double custoDisco) {
        this.custoDisco = custoDisco;
    }


}
