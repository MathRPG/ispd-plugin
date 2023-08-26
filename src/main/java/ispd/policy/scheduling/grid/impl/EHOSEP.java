package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.policy.scheduling.grid.impl.util.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class EHOSEP extends AbstractHOSEP<UserEnergyControl> {

    private static double calculateEnergyConsumptionForTask (
        final CS_Processamento machine,
        final Tarefa task
    ) {
        return task.getTamProcessamento()
               / machine.getPoderComputacional()
               * machine.getConsumoEnergia();
    }

    private static Comparator<UserEnergyControl> compareConsumptionWeightedByEfficiency () {
        return Comparator
            .comparingDouble(UserEnergyControl::currentConsumptionWeightedByEfficiency)
            .thenComparing(UserEnergyControl::excessProcessingPower);
    }

    @Override
    protected UserEnergyControl makeUserControlFor (final String userId) {
        return new UserEnergyControl(
            userId, this.escravos,
            this.metricaUsuarios.getLimites().get(userId)
        );
    }

    @Override
    protected Optional<UserEnergyControl> findUserToPreemptFor (final UserEnergyControl taskOwner) {
        return this.userControls.values().stream()
            .filter(UserEnergyControl::hasExcessProcessingPower)
            .max(compareConsumptionWeightedByEfficiency())
            .filter(taskOwner::hasLessEnergyConsumptionThan);
    }

    @Override
    protected Comparator<UserEnergyControl> getUserComparator () {
        return super.getUserComparator()
            .thenComparing(UserEnergyControl::getOwnedMachinesEnergyConsumption);
    }

    @Override
    protected Stream<CS_Processamento> availableMachinesFor (final UserEnergyControl taskOwner) {
        return super.availableMachinesFor(taskOwner)
            .filter(taskOwner::canUseMachineWithoutExceedingEnergyLimit);
    }

    @Override
    protected Comparator<CS_Processamento> compareAvailableMachinesFor (final Tarefa task) {
        // Extracted as a variable to aid type inference
        final ToDoubleFunction<CS_Processamento> energyConsumption =
            m -> calculateEnergyConsumptionForTask(m, task);

        return Comparator
            .comparingDouble(energyConsumption)
            .reversed()
            .thenComparing(super.compareAvailableMachinesFor(task));
    }

    @Override
    protected Optional<CS_Processamento> findMachineToPreemptFor (final UserEnergyControl taskOwner) {
        return this.findUserToPreemptFor(taskOwner)
            .flatMap(userToPreempt -> this.findMachineToTransferBetween(userToPreempt, taskOwner));
    }

    @Override
    protected Stream<CS_Processamento> machinesTransferableBetween (
        final UserEnergyControl userToPreempt, final UserEnergyControl taskOwner
    ) {
        return super.machinesTransferableBetween(userToPreempt, taskOwner)
            .filter(taskOwner::canUseMachineWithoutExceedingEnergyLimit);
    }

    @Override
    protected Comparator<CS_Processamento> compareOccupiedMachines () {
        return Comparator
            .comparingDouble(this::wastedProcessingIfPreempted)
            .thenComparing(super.compareOccupiedMachines());
    }

    private double wastedProcessingIfPreempted (final CS_Processamento machine) {
        final var preemptedTask = this.taskToPreemptIn(machine);
        final var startTimeList = preemptedTask.getTempoInicial();
        final var taskStartTime = startTimeList.get(startTimeList.size() - 1);
        final var currTime      = this.mestre.getSimulation().getTime(this);

        // Se for alterado o tempo de checkpoint, alterar também no métricas linha 832, cálculo da energia desperdiçada
        return (currTime - taskStartTime) * machine.getPoderComputacional();
    }
}
