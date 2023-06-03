package ispd.motor.metricas;

import java.io.Serializable;

public class MetricasCusto implements Serializable {

    private final String id; // id da máquina virtual que se está calculando as métricas
    private       double custoProc;
    private       double custoMem;
    private       double custoDisco;

    public MetricasCusto (final String id) {
        this.id         = id;
        this.custoProc  = 0;
        this.custoMem   = 0;
        this.custoDisco = 0;
    }

    public String getId () {
        return this.id;
    }

    public double getCustoProc () {
        return this.custoProc;
    }

    public void setCustoProc (final double custoProc) {
        this.custoProc = custoProc;
    }

    public double getCustoMem () {
        return this.custoMem;
    }

    public void setCustoMem (final double custoMem) {
        this.custoMem = custoMem;
    }

    public double getCustoDisco () {
        return this.custoDisco;
    }

    public void setCustoDisco (final double custoDisco) {
        this.custoDisco = custoDisco;
    }
}
