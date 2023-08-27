package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.queues.task.*;

public record PreemptionEntry(
    String preemptedTaskUser, int preemptedTaskId, String scheduledTaskUser, int scheduledTaskId
) {

    public PreemptionEntry (final GridTask preempted, final GridTask scheduled) {
        this(preempted.getProprietario(), preempted.getIdentificador(), scheduled.getProprietario(),
             scheduled.getIdentificador()
        );
    }

    public boolean willPreemptTask (final GridTask task) {
        return this.preemptedTaskId == task.getIdentificador()
               && this.preemptedTaskUser.equals(task.getProprietario());
    }

    public boolean willScheduleTask (final GridTask task) {
        return this.scheduledTaskId == task.getIdentificador()
               && this.scheduledTaskUser.equals(task.getProprietario());
    }
}
