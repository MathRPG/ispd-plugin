package ispd.policy.allocation.vm;

import ispd.motor.queues.centers.impl.*;
import ispd.policy.allocation.*;
import java.util.*;

public abstract class VmAllocationPolicy extends AllocationPolicy<VmMaster> {

    protected List<VirtualMachine> maquinasVirtuais = null;

    protected List<VirtualMachine> VMsRejeitadas = null;

    protected List<List> infoMaquinas = null;

    public abstract VirtualMachine escalonarVM ();

    public void addVM (final VirtualMachine vm) {
        this.maquinasVirtuais.add(vm);
    }

    public List<VirtualMachine> getMaquinasVirtuais () {
        return this.maquinasVirtuais;
    }

    public List<VirtualMachine> getVMsRejeitadas () {
        return this.VMsRejeitadas;
    }
}
