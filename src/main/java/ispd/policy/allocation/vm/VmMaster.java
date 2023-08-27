package ispd.policy.allocation.vm;

import ispd.motor.queues.centers.impl.*;
import ispd.policy.allocation.*;

public interface VmMaster extends AllocationMaster {

    void sendVm (VirtualMachine vm);
}
