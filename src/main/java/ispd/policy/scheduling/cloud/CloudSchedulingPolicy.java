package ispd.policy.scheduling.cloud;

import ispd.motor.filas.servidores.*;
import ispd.motor.filas.servidores.implementacao.*;
import ispd.policy.scheduling.*;
import java.util.*;
import java.util.stream.*;

public abstract class CloudSchedulingPolicy extends SchedulingPolicy<CloudMaster> {

    protected List<CS_Processamento> getVMsAdequadas (final String user) {
        return this.escravos.stream()
            .filter(s -> s.getProprietario().equals(user))
            .map(CS_VirtualMac.class::cast)
            .filter(s -> s.getStatus() == VirtualMachineState.ALLOCATED)
            .collect(Collectors.toCollection(LinkedList::new));
    }
}
