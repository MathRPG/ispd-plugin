package ispd.motor.metricas;

import java.util.ArrayList;
import java.util.List;

import ispd.motor.filas.Tarefa;

public class MetricasConsumo {

    private String       usuario;
    private List<Double> consumo;

    public MetricasConsumo (String user) {

        this.usuario = user;
        consumo      = new ArrayList<Double>();

    }

    public double getConsumo () {

        return consumo.get(consumo.size() - 1);
    }

    public double getDesvioPadrao (Metricas metricas) {

        Double desvio       = 0.0;
        Double consumoMedio = getConsumoMedio(metricas);
        int    i;

        for (i = 0; i < consumo.size(); i++) {
            desvio += Math.pow(consumo.get(i) - consumoMedio, 2);
        }
        desvio = desvio / (consumo.size() - 1);
        desvio = Math.sqrt(desvio);

        return desvio;

    }

    public double getConsumoMedio (Metricas metricas) {
        Double consumoMedio = 0.0;
        int    i;

        for (i = 0; i < consumo.size(); i++) {
            consumoMedio += consumo.get(i);
        }

        return consumoMedio / metricas.getNumeroDeSimulacoes();
    }

    public void addConsumo (MetricasUsuarios metricas) {

        Double consumoAdd = 0.0;
        int    i;
        for (Tarefa j : metricas.getTarefasConcluidas(usuario)) {
            for (i = 0; i < j.getHistoricoProcessamento().size(); i++) {
                consumoAdd += (j.getTempoFinal().get(i) - j.getTempoInicial().get(i)) *
                              j.getHistoricoProcessamento().get(i).getConsumoEnergia();
            }
        }

        consumo.add(consumoAdd);

    }


}
