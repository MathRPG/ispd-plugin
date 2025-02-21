package ispd.motor.workload.impl.task;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import ispd.motor.random.*;
import java.util.*;

/**
 * Specialization of task building from traces for external models. Its behavioral differences are:
 * <ul>
 *     <li>When making a task, it checks if such task must be set to be
 *     cancelled, and processes it accordingly.</li>
 *     <li>Task computation size is normalized with the average computational
 *     power of the queue network.</li>
 *     <li>Task communication size is randomly generated from a
 *     {@link TwoStageUniform} distribution.</li>
 * </ul>
 *
 * @see #makeTaskFor(Processing)
 * @see #makeTaskComputationSize()
 * @see #makeTaskCommunicationSize()
 */
public class ExternalTraceTaskBuilder extends TraceTaskBuilder {

    private static final TwoStageUniform TASK_COMM_SIZE = new TwoStageUniform(200, 5000, 25000);

    private final Distribution random;

    private final double averageComputationPower;

    /**
     * Initialize an instance with the given {@link List} of {@link TraceTaskInfo}s to be converted
     * into tasks, {@link Distribution} to generate the task communication size, and average
     * computation power to normalize the task computation size.
     *
     * @param traceTaskInfos
     *     list of task information to be converted into tasks.
     * @param random
     *     random double generator to produce communication sizes.
     * @param averageCompPower
     *     average computational power of the {@link GridQueueNetwork} containing the masters which tasks
     *     will be generated for.
     */
    public ExternalTraceTaskBuilder (
        final List<TraceTaskInfo> traceTaskInfos,
        final Distribution random,
        final double averageCompPower
    ) {
        super(traceTaskInfos);
        this.random                  = random;
        this.averageComputationPower = averageCompPower;
    }

    /**
     * Generate a task with information from the inner list. If it is determined that the task
     * should be cancelled, its processing local is set to the given {@link Processing} and it
     * is set to be cancelled instantly (on instant {@literal 0.0}).
     *
     * @param master
     *     {@link Processing} that will host the task.
     *
     * @return created (and possibly cancelled) {@link GridTask}.
     *
     * @see TraceTaskInfo#shouldBeCanceled()
     * @see GridTask#setLocalProcessamento(Service)
     * @see GridTask#cancelar(double)
     */
    @Override
    public GridTask makeTaskFor (final Processing master) {
        final var task = super.makeTaskFor(master);

        if (this.currTaskInfo.shouldBeCanceled()) {
            task.setLocalProcessamento(master);
            task.cancelar(0);
        }

        return task;
    }

    /**
     * Make the task's communication size, randomly generated from the two stage uniform
     * distribution {@link #TASK_COMM_SIZE}.
     *
     * @return the randomly generated task communication size (in MBits).
     */
    @Override
    protected double makeTaskCommunicationSize () {
        return TASK_COMM_SIZE.generateValue(this.random);
    }

    /**
     * Make the task's computation size, calculated from the value found in the file for such
     * property, but normalized to account for the average computational power of the machines in
     * the model.
     *
     * @return the calculated task computation size (in MFlops).
     */
    @Override
    protected double makeTaskComputationSize () {
        return super.makeTaskComputationSize() * this.averageComputationPower;
    }
}