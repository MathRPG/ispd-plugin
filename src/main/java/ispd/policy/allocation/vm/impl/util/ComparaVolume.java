package ispd.policy.allocation.vm.impl.util;

import ispd.motor.queues.centers.impl.*;

public class ComparaVolume extends MachineComparator<CloudMachine> {

    private static final int VOLUME = 1_000_000;

    protected int calculateMachineValue (final CloudMachine m) {
        return VOLUME * (
            m.getProcessadoresDisponiveis()
            * (int) m.getMemoriaDisponivel()
            * (int) m.getDiscoDisponivel()
        );
    }
}
