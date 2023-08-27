package ispd.policy.scheduling.grid.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

public class WQR extends GridSchedulingPolicy {

    private GridTask ultimaTarefaConcluida = null;

    private List<GridTask> tarefaEnviada = null;

    private int servidoresOcupados = 0;

    private int cont = 0;

    public WQR () {
        this.tarefas  = new ArrayList<>();
        this.escravos = new ArrayList<>();
    }

    @Override
    public void iniciar () {
        this.tarefaEnviada = new ArrayList<>(this.escravos.size());
        for (int i = 0; i < this.escravos.size(); i++) {
            this.tarefaEnviada.add(null);
        }
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        final Processing rec  = this.escalonarRecurso();
        boolean          sair = false;
        if (rec != null) {
            final GridTask trf = this.escalonarTarefa();
            if (trf != null) {
                if (this.tarefaEnviada.get(this.escravos.indexOf(rec)) != null) {
                    this.mestre.sendMessage(
                        this.tarefaEnviada.get(this.escravos.indexOf(rec)),
                        rec,
                        RequestType.CANCEL
                    );
                } else {
                    this.servidoresOcupados++;
                }
                this.tarefaEnviada.set(this.escravos.indexOf(rec), trf);
                this.ultimaTarefaConcluida = null;
                trf.setLocalProcessamento(rec);
                trf.setCaminho(this.escalonarRota(rec));
                this.mestre.sendTask(trf);
            } else if (this.tarefas.isEmpty()) {
                sair = true;
            }
        }
        if (this.servidoresOcupados > 0
            && this.servidoresOcupados < this.escravos.size()
            && this.tarefas.isEmpty()
            &&
            !sair) {
            for (final GridTask tar : this.tarefaEnviada) {
                if (tar != null && tar.getOrigem().equals(this.mestre)) {
                    this.mestre.executeScheduling();
                    break;
                }
            }
        }
    }

    @Override
    public Processing escalonarRecurso () {
        final int index =
            this.tarefaEnviada.indexOf(this.ultimaTarefaConcluida);
        if (this.ultimaTarefaConcluida != null && index != -1) {
            return this.escravos.get(index);
        } else {
            for (int i = 0; i < this.tarefaEnviada.size(); i++) {
                if (this.tarefaEnviada.get(i) == null) {
                    return this.escravos.get(i);
                }
            }
        }
        for (int i = 0; i < this.tarefaEnviada.size(); i++) {
            if (this.tarefaEnviada.get(i) != null && this.tarefaEnviada.get(i).isCopy()) {
                return this.escravos.get(i);
            }
        }
        return null;
    }

    @Override
    public GridTask escalonarTarefa () {
        if (!this.tarefas.isEmpty()) {
            return this.tarefas.remove(0);
        }
        if (this.cont >= this.tarefaEnviada.size()) {
            this.cont = 0;
        }
        if (this.servidoresOcupados >= this.escravos.size()) {
            return null;
        }
        for (int i = this.cont; i < this.tarefaEnviada.size(); i++) {
            if (this.tarefaEnviada.get(i) != null) {
                this.cont = i;
                if (!this.tarefaEnviada.get(i).getOrigem().equals(this.mestre)) {
                    this.cont++;
                    return null;
                }
                return this.mestre.cloneTask(this.tarefaEnviada.get(i));
            }
        }
        return null;
    }

    @Override
    public void addTarefaConcluida (final GridTask tarefa) {
        super.addTarefaConcluida(tarefa);
        final int index = this.tarefaEnviada.indexOf(tarefa);
        if (index != -1) {
            this.servidoresOcupados--;
            this.tarefaEnviada.set(index, null);
        }
        for (int i = 0; i < this.tarefaEnviada.size(); i++) {
            if (this.tarefaEnviada.get(i) != null && this.tarefaEnviada.get(i).isCopyOf(tarefa)) {
                this.mestre.sendMessage(this.tarefaEnviada.get(i),
                                        this.escravos.get(i), RequestType.CANCEL
                );
                this.servidoresOcupados--;
                this.tarefaEnviada.set(i, null);
            }
        }
        this.ultimaTarefaConcluida = tarefa;
        if ((this.servidoresOcupados > 0 && this.servidoresOcupados < this.escravos.size()) ||
            !this.tarefas.isEmpty()) {
            this.mestre.executeScheduling();
        }
    }
}
