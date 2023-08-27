package ispd.policy.scheduling.grid.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.grid.impl.util.*;
import java.util.*;

public class OSEP extends AbstractOSEP<UserProcessingControl> {

    private final List<GridTask> tasksInWaiting = new ArrayList<>();

    private final List<PreemptionEntry> preemptionEntries = new ArrayList<>();

    private GridTask selectedTask = null;

    private int slaveCount = 0;

    @Override
    public void escalonar () {
        final var task = this.escalonarTarefa();

        if (task == null) {
            return;
        }

        this.selectedTask = task;
        final var userStatus = this.userControls.get(task.getProprietario());
        final var resource   = this.escalonarRecurso();

        if (resource == null) {
            this.tarefas.add(task);
            this.selectedTask = null;
            return;
        }

        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));

        // Verifica se não é caso de preempção
        final var sc = this.slaveControls.get(resource);
        if (sc.isFree()) {
            userStatus.decreaseTaskDemand();
            userStatus.increaseUsedMachines();
            sc.setAsBlocked();
            this.mestre.sendTask(task);
        } else if (sc.isOccupied()) {
            this.tasksInWaiting.add(task);

            final var t2 =
                this.slaveControls.get(resource).firstTaskInProcessing();
            this.preemptionEntries.add(new PreemptionEntry(t2, task));

            sc.setAsBlocked();
        }
    }

    @Override
    public Processing escalonarRecurso () {
        String user;
        // Buscando recurso livre
        Processing selec = null;

        for (int i = 0; i < this.escravos.size(); i++) {
            final var slave = this.escravos.get(i);

            if (this.slaveControls.get(slave).isFree()) {
                //Garantir que o escravo está de fato livre e que não há
                // nenhuma tarefa em trânsito para o escravo
                selec = slave;
                break;
            }
        }

        if (selec != null) {
            // Inidcar que uma tarefa será enviada e que , portanto , este
            // escravo deve ser bloqueada até a próxima atualização
            return selec;
        }

        String usermax = null;
        long   diff    = -1;

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            user = this.metricaUsuarios.getUsuarios().get(i);
            final var uc = this.userControls.get(user);

            if (uc.currentlyUsedMachineCount() > uc.getOwnedMachinesCount() &&
                !user.equals(this.selectedTask.getProprietario())) {

                if (diff == -1) {
                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff    = uc.currentlyUsedMachineCount() - uc.getOwnedMachinesCount();
                } else {
                    if (uc.currentlyUsedMachineCount() - uc.getOwnedMachinesCount() > diff) {
                        usermax = user;
                        diff    = uc.currentlyUsedMachineCount() - uc.getOwnedMachinesCount();
                    }
                }
            }
        }

        int index = -1;
        if (usermax != null) {
            for (int i = 0; i < this.escravos.size(); i++) {
                final var s  = this.escravos.get(i);
                final var sc = this.slaveControls.get(s);
                if (sc.isOccupied() && sc
                    .firstTaskInProcessing()
                    .getProprietario()
                    .equals(usermax)) {
                    index = i;
                    break;
                }
            }
        }

        //Fazer a preempção
        if (index != -1) {
            final Processing processingCenter = this.escravos.get(index);
            this.mestre.sendMessage(
                this.slaveControls.get(processingCenter).firstTaskInProcessing(),
                processingCenter,
                RequestType.PREEMPTIVE_RETURN
            );
            return processingCenter;
        }

        return null;
    }

    @Override
    public GridTask escalonarTarefa () {
        return this.getBestUserForSomeTask()
            .flatMap(this::findAnyTaskOf)
            .map(this::popTaskFromQueue)
            .or(this::firstAvailableTask)
            .orElse(null);
    }

    @Override
    public void addTarefaConcluida (final GridTask tarefa) {
        super.addTarefaConcluida(tarefa);
        final var maq = tarefa.getCSLProcessamento();
        final var uc  = this.userControls.get(tarefa.getProprietario());

        uc.decreaseUsedMachines();
        this.slaveControls.get(maq).setAsFree();
    }

    @Override
    public void resultadoAtualizar (final Request request) {
        super.resultadoAtualizar(request);

        this.slaveControls.get((Processing) request.getOrigem())
            .setTasksInProcessing(request.getProcessadorEscravo());

        this.slaveCount++;

        if (this.slaveCount != this.escravos.size()) {
            return;
        }

        this.slaveCount = 0;

        boolean shouldSchedule = false;
        for (final Processing s : this.escravos) {
            final var sc = this.slaveControls.get(s);
            if (sc.isBlocked()) {
                sc.setAsUncertain();
            }
            if (sc.isUncertain()) {
                if (sc.hasTasksInProcessing()) {
                    sc.setAsOccupied();
                } else {
                    sc.setAsFree();
                    shouldSchedule = true;
                }
            }
        }

        if (!this.tarefas.isEmpty() && shouldSchedule) {
            this.mestre.executeScheduling();
        }
    }

    @Override
    public void adicionarTarefa (final GridTask tarefa) {
        super.adicionarTarefa(tarefa);
        final Processing maq        = tarefa.getCSLProcessamento();
        final var        estadoUser = this.userControls.get(tarefa.getProprietario());

        if (tarefa.getLocalProcessamento() == null) {
            this.mestre.executeScheduling();
            estadoUser.increaseTaskDemand();
            return;
        }

        // Em caso de preempção, é procurada a tarefa correspondente para ser enviada ao escravo agora desocupado
        int j;
        int indexControle = -1;
        for (j = 0; j < this.preemptionEntries.size(); j++) {
            if (this.preemptionEntries.get(j).preemptedTaskId() == tarefa.getIdentificador() &&
                this.preemptionEntries
                    .get(j)
                    .preemptedTaskUser()
                    .equals(tarefa.getProprietario())) {
                indexControle = j;
                break;
            }
        }

        final var pe = this.preemptionEntries.get(indexControle);

        for (int i = 0; i < this.tasksInWaiting.size(); i++) {
            if (this.tasksInWaiting.get(i).getProprietario().equals(pe.scheduledTaskUser()) &&
                this.tasksInWaiting.get(i).getIdentificador() == this.preemptionEntries
                    .get(j)
                    .scheduledTaskId()) {

                this.mestre.sendTask(this.tasksInWaiting.get(i));

                this.userControls.get(pe.scheduledTaskUser()).increaseUsedMachines();

                this.userControls.get(pe.preemptedTaskUser()).increaseTaskDemand();
                this.userControls.get(pe.preemptedTaskUser()).decreaseUsedMachines();

                this.slaveControls.get(maq).setAsBlocked();

                this.tasksInWaiting.remove(i);
                this.preemptionEntries.remove(j);
                break;
            }
        }
    }

    private Optional<UserProcessingControl> getBestUserForSomeTask () {
        return this.userControls.values().stream()
            .filter(this::isUserEligibleForTask)
            .max(Comparator
                     .comparingLong(UserProcessingControl::excessMachines));
    }

    private Optional<GridTask> findAnyTaskOf (final UserProcessingControl uc) {
        return this.tarefas.stream()
            .filter(uc::isOwnerOf)
            .findAny();
    }

    private GridTask popTaskFromQueue (final GridTask task) {
        this.tarefas.remove(task);
        return task;
    }

    private Optional<GridTask> firstAvailableTask () {
        return this.tarefas.stream()
            .findFirst()
            .map(this::popTaskFromQueue);
    }

    private boolean isUserEligibleForTask (final UserProcessingControl user) {
        return user.hasExcessMachines() && user.isEligibleForTask();
    }
}
