package ispd.policy.scheduling.cloud;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.policy.scheduling.*;
import java.util.*;
import java.util.stream.*;

public abstract class CloudSchedulingPolicy extends SchedulingPolicy<CloudMaster> {

    protected List<Processing> getVMsAdequadas (final String user) {
        return this.escravos.stream()
            .filter(s -> s.getProprietario().equals(user))
            .map(VirtualMachine.class::cast)
            .filter(s -> s.getStatus() == VirtualMachineState.ALLOCATED)
            .collect(Collectors.toCollection(LinkedList::new));
    }
}
