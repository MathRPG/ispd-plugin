package ispd.policy.allocation.vm.impl.util;

import ispd.motor.queues.centers.impl.*;

public class ComparaRequisitos extends MachineComparator<VirtualMachine> {

    private static final int MULTIPLIER = 100_000;

    protected int calculateMachineValue (final VirtualMachine m) {
        return MULTIPLIER * (
            m.getProcessadoresDisponiveis()
            + (int) m.getMemoriaDisponivel()
            + (int) m.getDiscoDisponivel()
        );
    }
}
