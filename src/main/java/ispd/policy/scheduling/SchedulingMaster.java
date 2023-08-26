package ispd.policy.scheduling;

import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.policy.*;
import java.util.*;

public interface SchedulingMaster extends Simulable {

    void executeScheduling ();

    void setSchedulingConditions (Set<Condition> newConditions);

    void sendTask (Tarefa task);

    Tarefa cloneTask (Tarefa task);

    void sendMessage (Tarefa task, CS_Processamento slave, int messageType);
}
