package ispd.policy.allocation.vm.impl.util;

import ispd.motor.queues.centers.*;
import java.io.*;
import java.util.*;

public abstract class MachineComparator <T extends Processing>
    implements Comparator<T>, Serializable {

    protected abstract int calculateMachineValue (T m);

    @Override
    public int compare (final T m1, final T m2) {
        return this.calculateMachineValue(m1) - this.calculateMachineValue(m2);
    }
}
