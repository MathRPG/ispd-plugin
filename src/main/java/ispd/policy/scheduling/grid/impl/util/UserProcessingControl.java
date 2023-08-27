package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import jdk.jfr.*;

public class UserProcessingControl {

    private final String userId;

    @Unsigned
    private final long ownedMachinesCount;

    @Unsigned
    private final double ownedMachinesProcessingPower;

    @Unsigned
    private int taskDemand = 0;

    @Unsigned
    private int usedMachineCount = 0;

    @Unsigned
    private double usedProcessingPower = 0.0;

    public UserProcessingControl (
        final String userId,
        final Collection<? extends Processing> systemMachines
    ) {
        this.userId = userId;

        this.ownedMachinesProcessingPower = this
            .ownedNonMasterMachinesIn(systemMachines)
            .mapToDouble(Processing::getPoderComputacional)
            .sum();

        this.ownedMachinesCount = systemMachines.stream()
            .filter(this::hasMachine)
            .toList().size();
    }

    protected Stream<? extends Processing> ownedNonMasterMachinesIn (
        final Collection<? extends Processing> systemMachines
    ) {
        return systemMachines.stream()
            .filter(this::hasMachine)
            .filter(Predicate.not(GridMaster.class::isInstance));
    }

    private boolean hasMachine (final Processing machine) {
        return machine.getProprietario().equals(this.userId);
    }

    public boolean isEligibleForTask () {
        return this.taskDemand > 0;
    }

    public double penaltyWithProcessing (final double delta) {
        return (this.usedProcessingPower + delta - this.ownedMachinesProcessingPower)
               / this.ownedMachinesProcessingPower;
    }

    public void stopTaskFrom (final Processing machine) {
        this.decreaseUsedMachines();
        this.decreaseUsedProcessingPower(machine.getPoderComputacional());
    }

    public void decreaseUsedMachines () {
        this.usedMachineCount--;
    }

    public void decreaseUsedProcessingPower (final double amount) {
        this.usedProcessingPower -= amount;
    }

    public boolean canConcedeProcessingPower (final Processing machine) {
        return this.excessProcessingPower() >= machine.getPoderComputacional();
    }

    public double excessProcessingPower () {
        return this.ownedMachinesProcessingPower - this.usedProcessingPower;
    }

    public boolean isOwnerOf (final GridTask task) {
        return this.userId.equals(task.getProprietario());
    }

    public void startTaskFrom (final Processing machine) {
        this.increaseUsedMachines();
        this.increaseUsedProcessingPower(machine.getPoderComputacional());
    }

    public void increaseUsedMachines () {
        this.usedMachineCount++;
    }

    public void increaseUsedProcessingPower (final double amount) {
        this.usedProcessingPower += amount;
    }

    public void decreaseTaskDemand () {
        this.taskDemand--;
    }

    public void increaseTaskDemand () {
        this.taskDemand++;
    }

    public int currentlyUsedMachineCount () {
        return this.usedMachineCount;
    }

    @Percentage
    public double percentageOfProcessingPowerUsed () {
        return this.usedProcessingPower / this.ownedMachinesProcessingPower;
    }

    public double getOwnedMachinesProcessingPower () {
        return this.ownedMachinesProcessingPower;
    }

    public long getOwnedMachinesCount () {
        return this.ownedMachinesCount;
    }

    public boolean hasExcessProcessingPower () {
        return this.excessProcessingPower() >= 0;
    }

    public double currentlyUsedProcessingPower () {
        return this.usedProcessingPower;
    }

    public boolean hasExcessMachines () {
        return this.excessMachines() > 0;
    }

    public long excessMachines () {
        return this.ownedMachinesCount - this.usedMachineCount;
    }
}
