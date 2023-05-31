package ispd.motor.metricas;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

public class MetricasSatisfacao {

    private String usuario;
    private Double satisfacao;

    public MetricasSatisfacao (String usuario) {
        this.usuario    = usuario;
        this.satisfacao = 0.0;
    }

    public Double getSatisfacao (Metricas metricas) {

        return satisfacao / metricas.getNumeroDeSimulacoes();

    }

    public Double getSatisfacao () {

        return satisfacao;

    }

    public void addSatisfacao (MetricasUsuarios metricasUsuarios) {
        Double suij = 0.0;
        for (Tarefa j : metricasUsuarios.getTarefasConcluidas(usuario)) {
            CS_Processamento maq = (CS_Processamento) j.getHistoricoProcessamento().get(0);
            suij += (
                            (j.getTimeCriacao() + maq.tempoProcessar(j.getTamProcessamento())) /
                            (j.getTempoFinal().get(j.getTempoFinal().size() - 1) - j.getTimeCriacao())
                    ) * (100);
        }
        this.satisfacao += suij / metricasUsuarios.getTarefasConcluidas(usuario).size();
    }

    public void addSatisfacao (MetricasUsuarios metricasUsuarios, CS_Processamento maqMedia) {
        Double suij = 0.0;
        for (Tarefa j : metricasUsuarios.getTarefasConcluidas(usuario)) {
            suij += (
                            (j.getTimeCriacao() + maqMedia.tempoProcessar(j.getTamProcessamento())) /
                            (j.getTempoFinal().get(j.getTempoFinal().size() - 1) - j.getTimeCriacao())
                    ) * (100);
        }
        this.satisfacao += suij / metricasUsuarios.getTarefasConcluidas(usuario).size();
    }
}
