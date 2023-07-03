package ispd.motor.metricas;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.io.Serializable;
import java.util.List;

public class MetricasGlobais implements Serializable {

    private double tempoSimulacao;

    private double satisfacaoMedia;

    private double ociosidadeComputacao;

    private double ociosidadeComunicacao;

    private double eficiencia;

    private double custoTotalDisco = 0.0;

    private double custoTotalProc = 0.0;

    private double custoTotalMem = 0.0;

    private int totaldeVMs = 0;

    private int numVMsRejeitadas = 0;

    public MetricasGlobais (
        final RedeDeFilas redeDeFilas, final double tempoSimulacao, final List<Tarefa> tarefas
    ) {
        this.tempoSimulacao        = tempoSimulacao;
        this.satisfacaoMedia       = 100;
        this.ociosidadeComputacao  = this.getOciosidadeComputacao(redeDeFilas);
        this.ociosidadeComunicacao = this.getOciosidadeComunicacao(redeDeFilas);
        this.eficiencia            = this.getEficiencia(tarefas);
    }

    public MetricasGlobais (
        final RedeDeFilasCloud redeDeFilas, final double tempoSimulacao, final List<Tarefa> tarefas
    ) {
        this.tempoSimulacao        = tempoSimulacao;
        this.satisfacaoMedia       = 100;
        this.ociosidadeComputacao  = this.getOciosidadeComputacaoCloud(redeDeFilas);
        this.ociosidadeComunicacao = this.getOciosidadeComunicacao(redeDeFilas);
        this.eficiencia            = this.getEficiencia(tarefas);
        this.custoTotalDisco       = this.getCustoTotalDisco(redeDeFilas);
        this.custoTotalMem         = this.getCustoTotalMem(redeDeFilas);
        this.custoTotalProc        = this.getCustoTotalProc(redeDeFilas);
        this.totaldeVMs            = this.getTotalVMs(redeDeFilas);
        this.numVMsRejeitadas      = this.getNumVMsRejeitadas(redeDeFilas);
    }

    public MetricasGlobais () {
        this.tempoSimulacao        = 0;
        this.satisfacaoMedia       = 0;
        this.ociosidadeComputacao  = 0;
        this.ociosidadeComunicacao = 0;
        this.eficiencia            = 0;
        this.custoTotalDisco       = 0;
        this.custoTotalMem         = 0;
        this.custoTotalProc        = 0;
    }

    @Override
    public String toString () {
        String    texto     = "\t\tSimulation Results\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", this.tempoSimulacao);
        texto += String.format("\tSatisfaction = %g %%\n", this.satisfacaoMedia);
        texto += String.format(
            "\tIdleness of processing resources = %g %%\n",
            this.ociosidadeComputacao
        );
        texto += String.format(
            "\tIdleness of communication resources = %g %%\n",
            this.ociosidadeComunicacao
        );
        texto += String.format("\tEfficiency = %g %%\n", this.eficiencia);
        if (this.eficiencia > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (this.eficiencia > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
        texto += "\t\tCost Results:\n\n";
        texto += String.format("\tCost Total de Processing = %g %%\n", this.custoTotalProc);
        texto += String.format("\tCost Total de Memory = %g %%\n", this.custoTotalMem);
        texto += String.format("\tCost Total de Disk = %g %%\n", this.custoTotalDisco);
        texto += "\t\tVM Alocation results:";
        texto += String.format(
            "\tTotal of VMs alocated = %d %%\n",
            (this.totaldeVMs - this.numVMsRejeitadas)
        );
        texto += String.format("\tTotal of VMs rejected = %d %%\n", this.numVMsRejeitadas);
        return texto;
    }

    private double getOciosidadeComputacao (final RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (final CS_Processamento maquina : redeDeFilas.getMaquinas()) {
            double aux = maquina.getMetrica().getSegundosDeProcessamento();
            aux = (this.tempoSimulacao - aux);
            tempoLivreMedio += aux; // tempo livre
            aux = maquina.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getMaquinas().size();
        return (tempoLivreMedio * 100) / this.tempoSimulacao;
    }

    private double getOciosidadeComunicacao (final RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (final CS_Comunicacao link : redeDeFilas.getLinks()) {
            double aux = link.getMetrica().getSegundosDeTransmissao();
            aux = (this.tempoSimulacao - aux);
            tempoLivreMedio += aux; //tempo livre
            aux = link.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getLinks().size();
        return (tempoLivreMedio * 100) / this.tempoSimulacao;
    }

    private double getEficiencia (final List<Tarefa> tarefas) {
        double somaEfic = 0;
        for (final Tarefa tar : tarefas) {
            somaEfic += tar.getMetricas().getEficiencia();
        }
        return somaEfic / tarefas.size();
    }

    private double getOciosidadeComputacaoCloud (final RedeDeFilasCloud redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (final CS_Processamento auxVM : redeDeFilas.getVMs()) {
            final CS_VirtualMac vm = (CS_VirtualMac) auxVM;
            if (vm.getStatus() == CS_VirtualMac.ALOCADA) {
                double aux = auxVM.getMetrica().getSegundosDeProcessamento();
                aux = (this.tempoSimulacao - aux);
                tempoLivreMedio += aux;//tempo livre
                aux = auxVM.getOcupacao() * aux;
                tempoLivreMedio -= aux;
            }
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getVMs().size();
        return (tempoLivreMedio * 100) / this.tempoSimulacao;
    }

    private double getCustoTotalDisco (final RedeDeFilasCloud redeDeFilas) {
        // calcula o custo total de uso de disco na nuvem
        for (final CS_VirtualMac auxVM : redeDeFilas.getVMs()) {
            if (auxVM.getStatus() == CS_VirtualMac.DESTRUIDA) {
                this.custoTotalDisco =
                    this.custoTotalDisco + auxVM.getMetricaCusto().getCustoDisco();
            }
        }
        return this.custoTotalDisco;
    }

    private double getCustoTotalMem (final RedeDeFilasCloud redeDeFilas) {
        // calcula custo total de uso de memória pela nuvem
        for (final CS_VirtualMac auxVM : redeDeFilas.getVMs()) {
            if (auxVM.getStatus() == CS_VirtualMac.DESTRUIDA) {
                this.custoTotalMem = this.custoTotalMem + auxVM.getMetricaCusto().getCustoMem();
            }
        }
        return this.custoTotalMem;
    }

    private double getCustoTotalProc (final RedeDeFilasCloud redeDeFilas) {
        // calcula o custo total de uso de processamento da nuvem
        for (final CS_VirtualMac auxVM : redeDeFilas.getVMs()) {
            if (auxVM.getStatus() == CS_VirtualMac.DESTRUIDA) {
                this.custoTotalProc = this.custoTotalProc + auxVM.getMetricaCusto().getCustoProc();
            }
        }
        return this.custoTotalProc;
    }

    private int getTotalVMs (final RedeDeFilasCloud redeDeFilas) {
        // calcula o total de vms do modelo
        int total = 0;
        for (final CS_Processamento aux : redeDeFilas.getMestres()) {
            final CS_VMM auxVMM = (CS_VMM) aux;
            total += auxVMM.getEscalonador().getEscravos().size();
        }
        return total;
    }

    private int getNumVMsRejeitadas (final RedeDeFilasCloud redeDeFilas) {
        // calcula número de VMs rejeitadas
        int totalRejeitadas = 0;
        for (final CS_Processamento aux : redeDeFilas.getMestres()) {
            final CS_VMM auxVMM = (CS_VMM) aux;
            totalRejeitadas += auxVMM.getAlocadorVM().getVMsRejeitadas().size();
        }
        return totalRejeitadas;
    }

    public double getTempoSimulacao () {
        return this.tempoSimulacao;
    }

    public void setTempoSimulacao (final double tempoSimulacao) {
        this.tempoSimulacao = tempoSimulacao;
    }

    public double getCustoTotalDisco () {
        return this.custoTotalDisco;
    }

    public double getCustoTotalProc () {
        return this.custoTotalProc;
    }

    public double getCustoTotalMem () {
        return this.custoTotalMem;
    }

    public int getNumVMsAlocadas () {
        return this.totaldeVMs - this.numVMsRejeitadas;
    }

    public int getNumVMsRejeitadas () {
        return this.numVMsRejeitadas;
    }

    public double getSatisfacaoMedia () {
        return this.satisfacaoMedia;
    }

    public void setSatisfacaoMedia (final double satisfacaoMedia) {
        this.satisfacaoMedia = satisfacaoMedia;
    }

    public double getOciosidadeComputacao () {
        return this.ociosidadeComputacao;
    }

    public void setOciosidadeComputacao (final double ociosidadeComputacao) {
        this.ociosidadeComputacao = ociosidadeComputacao;
    }

    public double getOciosidadeComunicacao () {
        return this.ociosidadeComunicacao;
    }

    public void setOciosidadeComunicacao (final double ociosidadeComunicacao) {
        this.ociosidadeComunicacao = ociosidadeComunicacao;
    }

    public double getEficiencia () {
        return this.eficiencia;
    }

    public void setEficiencia (final double eficiencia) {
        this.eficiencia = eficiencia;
    }
}
