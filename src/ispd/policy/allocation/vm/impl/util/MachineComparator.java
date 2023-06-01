package ispd.policy.allocation.vm.impl.util;

import java.io.Serializable;
import java.util.Comparator;

import ispd.motor.filas.servidores.CS_Processamento;

public abstract class MachineComparator <T extends CS_Processamento> implements Comparator<T>, Serializable {

    @Override
    public int compare (final T m1, final T m2) {
        return this.calculateMachineValue(m1) - this.calculateMachineValue(m2);
    }

    protected abstract int calculateMachineValue (T m);
}
