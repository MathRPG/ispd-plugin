package ispd.motor.workload.impl;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import ispd.motor.random.*;
import ispd.motor.workload.*;
import ispd.utils.*;
import java.util.*;

/**
 * Generates a workload with randomly-decided sizes from a collection of uniform intervals, and
 * evenly distributes the tasks of such workload across all masters in a {@link GridQueueNetwork}.<br>
 * Some particularities:
 * <ul>
 *     <li>This class generates task ids sequentially, starting from {@code 0
 *     }.</li>
 *     <li>This class generates task creation times from an exponential
 *     distribution.</li>
 * </ul>
 *
 * @see RandomicWorkloadGenerator
 * @see SequentialIntSupplier
 * @see Distribution#nextExponential(double)
 */
public class GlobalWorkloadGenerator extends RandomicWorkloadGenerator {

    private final int taskCreationTime;

    /**
     * Instantiate a generator with the given properties and interval bounds .<br> Parameters
     * relating to the generation of the computation and communication size of the task are used in
     * a two-stage uniform distribution.
     *
     * @param taskCount
     *     number of tasks to generate
     * @param compMin
     *     computation distribution minimum
     * @param compMax
     *     computation distribution maximum
     * @param compAvg
     *     computation distribution interval split
     * @param compProb
     *     computation distribution first interval probability
     * @param commMin
     *     communication distribution minimum
     * @param commMax
     *     communication distribution maximum
     * @param commAvg
     *     communication distribution interval split
     * @param commProb
     *     communication distribution first interval probability
     * @param taskCreationTime
     *     task creation time ({@code beta} for an exponential distribution)
     *
     * @see TwoStageUniform
     * @see Distribution#nextExponential(double)
     */
    public GlobalWorkloadGenerator (
        final int taskCount,
        final double compMin, final double compMax, final double compAvg, final double compProb,
        final double commMin, final double commMax, final double commAvg, final double commProb,
        final int taskCreationTime
    ) {
        this(
            taskCount, taskCreationTime,
            new TwoStageUniform(compMin, compAvg, compMax, compProb),
            new TwoStageUniform(commMin, commAvg, commMax, commProb)
        );
    }

    /**
     * Instantiate a generator with the given properties, and that uses the given
     * {@link TwoStageUniform}s to generate task computation and communication sizes, respectively.
     *
     * @param taskCount
     *     number of tasks to generate
     * @param taskCreationTime
     *     task creation time ({@code beta} for an exponential distribution)
     * @param computation
     *     {@link TwoStageUniform} that will generate task computation size
     * @param communication
     *     {@link TwoStageUniform} that will generate task computation size
     *
     * @see TwoStageUniform
     * @see Distribution#nextExponential(double)
     */
    public GlobalWorkloadGenerator (
        final int taskCount, final int taskCreationTime,
        final TwoStageUniform computation, final TwoStageUniform communication
    ) {
        super(
            taskCount, computation, communication,
            new SequentialIntSupplier(), new Distribution()
        );
        this.taskCreationTime = taskCreationTime;
    }

    /**
     * Generates tasks as configured, and distributes then evenly among the masters in the given
     * {@link GridQueueNetwork}.
     *
     * @see ispd.motor.workload.impl.task.TaskBuilder#makeTasksDistributedAmongMasters(GridQueueNetwork,
     * int)
     */
    @Override
    public List<GridTask> makeTaskList (final GridQueueNetwork qn) {
        return this.makeTasksDistributedAmongMasters(qn, this.taskCount);
    }

    /**
     * @return {@link WorkloadGeneratorType#RANDOM}.
     */
    @Override
    public WorkloadGeneratorType getType () {
        return WorkloadGeneratorType.RANDOM;
    }

    /**
     * The iconic model format for this workload generator consists of:
     * <ul>
     *     <li>The configuration for computation and communication
     *     (respectively) distributions for task size. Their values (except for
     *     {@link TwoStageUniform#firstIntervalProbability()} <b>are
     *     formatted as integral values</b>. Furthermore, the order the
     *     values are outputted is <b>inconsistent between them</b></li>
     *     <li>An extra {@code 0}</li>
     *     <li>The {@link #taskCreationTime}</li>
     *     <li>The {@link #taskCount}</li>
     * </ul>
     *
     * @see TwoStageUniform
     */
    @Override
    public String formatForIconicModel () {
        return String.format("%d %d %d %f%n%d %d %d %f%n%d %d %d",
                             (int) this.computation.minimum(),
                             (int) this.computation.intervalSplit(),
                             (int) this.computation.maximum(),
                             this.computation.firstIntervalProbability(),
                             (int) this.communication.minimum(),
                             (int) this.communication.maximum(),
                             (int) this.communication.intervalSplit(),
                             this.communication.firstIntervalProbability(),
                             0, this.taskCreationTime, this.taskCount
        );
    }

    /**
     * The string representation for this workload generator includes its task count, task creation
     * time parameter, and distributions for generating computation and communication sizes.
     */
    @Override
    public String toString () {
        return """
               GlobalWorkloadGenerator{
                   taskCount=%d,
                   taskCreationTime=%d,
                   computation=%s,
                   communication=%s,
               }""".formatted(
            this.taskCount,
            this.taskCreationTime,
            this.computation.toString(),
            this.communication.toString()
        );
    }

    /**
     * The task user decided by this workload generator is simply the owner of the
     * {@link Processing} that will host the task.
     *
     * @param master
     *     {@link Processing} that hosts information about which user the task will be linked
     *     with.
     *
     * @return user id of the owner of the given master, to be used to generate a new task.
     *
     * @see Processing#getProprietario()
     */
    @Override
    protected String makeTaskUser (final Processing master) {
        return master.getProprietario();
    }

    /**
     * The task creation time generation involves an exponential distribution, with {@code beta}
     * equaling the task creation time configured for this instance.
     *
     * @return task creation time (in seconds), to be used to make a new task.
     */
    @Override
    protected double makeTaskCreationTime () {
        return this.random.nextExponential(this.taskCreationTime);
    }

    /**
     * @return task creation time configured for this instance.
     */
    public Integer getTaskCreationTime () {
        return this.taskCreationTime;
    }
}