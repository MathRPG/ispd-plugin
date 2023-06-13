package ispd.policy.scheduling;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.PolicyCondition;
import ispd.policy.PolicyMaster;
import java.util.Set;

public interface SchedulingMaster extends PolicyMaster {

    void executeScheduling ();

    void setSchedulingConditions (Set<PolicyCondition> newConditions);

    void sendTask (Tarefa task);

    Tarefa cloneTask (Tarefa task);

    void sendMessage (Tarefa task, CS_Processamento slave, int messageType);
}
