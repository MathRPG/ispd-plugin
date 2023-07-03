package ispd.motor.metricas;

import java.io.Serializable;

public class MetricasCusto implements Serializable {

    /**
     * id da máquina virtual que se está calculando as métricas
     */
    private final String id;

    private double custoProc = 0;

    private double custoMem = 0;

    private double custoDisco = 0;

    public MetricasCusto (final String id) {
        this.id = id;
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
