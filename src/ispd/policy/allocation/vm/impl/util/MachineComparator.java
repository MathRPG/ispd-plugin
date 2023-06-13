package ispd.policy.allocation.vm.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;
import java.io.Serializable;
import java.util.Comparator;

public abstract class MachineComparator <T extends CS_Processamento>
    implements Comparator<T>, Serializable {

    protected abstract int calculateMachineValue (T m);

    @Override
    public int compare (final T m1, final T m2) {
        return this.calculateMachineValue(m1) - this.calculateMachineValue(m2);
    }
}
