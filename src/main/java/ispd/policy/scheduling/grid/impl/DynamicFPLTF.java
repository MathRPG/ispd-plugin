package ispd.policy.scheduling.grid.impl;

import ispd.motor.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.motor.filas.servidores.implementacao.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

public class DynamicFPLTF extends GridSchedulingPolicy {

    private List<Double> tempoTornaDisponivel = null;

    private Tarefa tarefaSelecionada = null;

    public DynamicFPLTF () {
        this.tarefas     = new ArrayList<>();
        this.escravos    = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public void iniciar () {
        this.tempoTornaDisponivel = new ArrayList<>(this.escravos.size());
        for (int i = 0; i < this.escravos.size(); i++) {
            this.tempoTornaDisponivel.add(0.0);
            this.filaEscravo.add(new ArrayList());
        }
    }

    @Override
    public List<CentroServico> escalonarRota (final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        final Tarefa trf = this.escalonarTarefa();
        this.tarefaSelecionada = trf;
        if (trf != null) {
            final CS_Processamento rec   = this.escalonarRecurso();
            final int              index = this.escravos.indexOf(rec);
            final double           custo = rec.tempoProcessar(trf.getTamProcessamento());
            this.tempoTornaDisponivel.set(
                index,
                this.tempoTornaDisponivel.get(index) + custo
            );
            trf.setLocalProcessamento(rec);
            trf.setCaminho(this.escalonarRota(rec));
            this.mestre.sendTask(trf);
        }
    }

    @Override
    public CS_Processamento escalonarRecurso () {
        int index = 0;
        double menorTempo = this.escravos.get(index).tempoProcessar(
            this.tarefaSelecionada.getTamProcessamento());
        for (int i = 1; i < this.escravos.size(); i++) {
            final double tempoEscravoI = this.escravos.get(i).tempoProcessar(
                this.tarefaSelecionada.getTamProcessamento());
            if (this.tempoTornaDisponivel.get(index) + menorTempo
                > this.tempoTornaDisponivel.get(i) + tempoEscravoI) {
                menorTempo = tempoEscravoI;
                index      = i;
            }
        }
        return this.escravos.get(index);
    }

    @Override
    public Double getTempoAtualizar () {
        return 60.0;
    }

    @Override
    public Tarefa escalonarTarefa () {
        return this.tarefas.remove(0);
    }

    @Override
    public void addTarefaConcluida (final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final int index = this.escravos.indexOf(tarefa.getLocalProcessamento());
        if (index != -1) {
            final double custo =
                this.escravos.get(index).tempoProcessar(tarefa.getTamProcessamento());
            if (this.tempoTornaDisponivel.get(index) - custo > 0) {
                this.tempoTornaDisponivel.set(
                    index,
                    this.tempoTornaDisponivel.get(index) - custo
                );
            }
        }
        for (int i = 0; i < this.escravos.size(); i++) {
            if (this.escravos.get(i) instanceof CS_Maquina) {
                final CS_Processamento escravo = this.escravos.get(i);
                for (int j = 0; j < this.filaEscravo.get(i).size(); j++) {
                    final Tarefa trf = (Tarefa) this.filaEscravo.get(i).get(j);
                    final double custo =
                        escravo.tempoProcessar(trf.getTamProcessamento());
                    if (this.tempoTornaDisponivel.get(i) - custo > 0) {
                        this.tempoTornaDisponivel.set(
                            i,
                            this.tempoTornaDisponivel.get(i) - custo
                        );
                    }
                    this.mestre.sendMessage(trf, escravo,
                                            MessageType.RETURN
                    );
                }
                this.filaEscravo.get(i).clear();
            }
        }
    }

    @Override
    public void adicionarTarefa (final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        int k = 0;
        while (k < this.tarefas.size()
               && this.tarefas.get(k).getTamProcessamento() > tarefa.getTamProcessamento()) {
            k++;
        }
        this.tarefas.add(k, tarefa);
    }
}
