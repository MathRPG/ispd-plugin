package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.queues.centers.*;
import java.util.*;

public class UserEnergyControl extends UserProcessingControl {

    private final double energyEfficiencyRatioAgainstSystem;

    private final double energyConsumptionLimit;

    private final double ownedMachinesEnergyConsumption;

    private double currentEnergyConsumption = 0.0;

    public UserEnergyControl (
        final String userId,
        final Collection<? extends Processing> systemMachines,
        final double energyConsPercentage
    ) {
        super(userId, systemMachines);

        this.ownedMachinesEnergyConsumption = this
            .ownedNonMasterMachinesIn(systemMachines)
            .mapToDouble(Processing::getConsumoEnergia)
            .sum();

        this.energyConsumptionLimit =
            this.calculateEnergyConsumptionLimit(energyConsPercentage);

        this.energyEfficiencyRatioAgainstSystem =
            this.calculateEnergyEfficiencyRatioAgainst(systemMachines);
    }

    @Override
    public boolean isEligibleForTask () {
        return super.isEligibleForTask()
               && !this.hasExceededEnergyLimit();
    }

    @Override
    public void stopTaskFrom (final Processing machine) {
        super.stopTaskFrom(machine);
        this.decreaseEnergyConsumption(machine.getConsumoEnergia());
    }

    @Override
    public void startTaskFrom (final Processing machine) {
        super.startTaskFrom(machine);
        this.increaseEnergyConsumption(machine.getConsumoEnergia());
    }

    private double calculateEnergyConsumptionLimit (final double energyConsPercentage) {
        return this.ownedMachinesEnergyConsumption * energyConsPercentage / 100;
    }

    private double calculateEnergyEfficiencyRatioAgainst (
        final Collection<? extends Processing> machines
    ) {
        final var sysComputationPower = machines.stream()
            .mapToDouble(Processing::getPoderComputacional)
            .sum();

        final var sysEnergyConsumption = machines.stream()
            .mapToDouble(Processing::getConsumoEnergia)
            .sum();

        final var sysEnergyEff = sysComputationPower / sysEnergyConsumption;
        return sysEnergyEff / this.energyEfficiency();
    }

    private double energyEfficiency () {
        return this.getOwnedMachinesProcessingPower() / this.ownedMachinesEnergyConsumption;
    }

    private void increaseEnergyConsumption (final double amount) {
        this.currentEnergyConsumption += amount;
    }

    private void decreaseEnergyConsumption (final double amount) {
        this.currentEnergyConsumption -= amount;
    }

    private boolean hasExceededEnergyLimit () {
        return this.currentEnergyConsumption >= this.energyConsumptionLimit;
    }

    public double currentConsumptionWeightedByEfficiency () {
        return this.currentEnergyConsumption * this.energyEfficiencyRatioAgainstSystem;
    }

    public boolean hasLessEnergyConsumptionThan (final UserEnergyControl other) {
        return this.energyConsumptionLimit <= other.energyConsumptionLimit;
    }

    public boolean canUseMachineWithoutExceedingEnergyLimit (final Processing machine) {
        return this.currentEnergyConsumption + machine.getConsumoEnergia()
               <= this.energyConsumptionLimit;
    }

    public double getOwnedMachinesEnergyConsumption () {
        return this.ownedMachinesEnergyConsumption;
    }
}
