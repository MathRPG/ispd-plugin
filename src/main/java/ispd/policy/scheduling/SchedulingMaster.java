package ispd.policy.scheduling;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.policy.*;
import java.util.*;

public interface SchedulingMaster extends Simulable {

    void executeScheduling ();

    void setSchedulingConditions (Set<Condition> newConditions);

    void sendTask (GridTask task);

    GridTask cloneTask (GridTask task);

    void sendMessage (GridTask task, Processing slave, RequestType requestType);
}
