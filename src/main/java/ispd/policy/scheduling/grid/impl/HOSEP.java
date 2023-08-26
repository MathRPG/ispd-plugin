package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.servidores.*;
import ispd.policy.scheduling.grid.impl.util.*;
import java.util.*;

public class HOSEP extends AbstractHOSEP<UserProcessingControl> {

    @Override
    protected Optional<UserProcessingControl> findUserToPreemptFor (final UserProcessingControl taskOwner) {
        return Optional.of(this.theBestUser());
    }

    @Override
    protected boolean shouldTransferMachine (
        final CS_Processamento machine, final UserProcessingControl machineOwner,
        final UserProcessingControl nextOwner
    ) {
        if (super.shouldTransferMachine(machine, machineOwner, nextOwner)) {
            return true;
        }

        final double machineOwnerPenalty =
            machineOwner.penaltyWithProcessing(-machine.getPoderComputacional());
        final double nextOwnerPenalty =
            nextOwner.penaltyWithProcessing(machine.getPoderComputacional());

        return machineOwnerPenalty >= nextOwnerPenalty;
    }
}
