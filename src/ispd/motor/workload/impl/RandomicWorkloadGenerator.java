package ispd.motor.workload.impl;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.impl.task.TaskBuilder;
import jdk.jfr.Unsigned;

/**
 * Base class for {@link WorkloadGenerator}s that involve random generation
 * of task attributes, such as computation and communication size.<br>
 * It also standardizes the use of a {@link Supplier<Integer>} for generating
 * task ids.
 */
/* package-private */
abstract class RandomicWorkloadGenerator extends TaskBuilder implements WorkloadGenerator {

    /**
     * The number of tasks to generate.
     */
    @Unsigned
    protected final int             taskCount;
    /**
     * {@link TwoStageUniform} to be used to generate task computation sizes.
     */
    protected final TwoStageUniform computation;
    /**
     * {@link TwoStageUniform} to be used to generate task communication sizes.
     */
    protected final TwoStageUniform communication;
    /**
     * {@link Distribution} to be used to roll random values, using the
     * fields {@link #computation} and {@link #communication}.
     */
    protected final Distribution    random;

    /**
     * Object, or even Function, to be invoked to generate a new task id.
     */
    private final IntSupplier idSupplier;

    /**
     * Initialize the class' fields with the given parameters.
     *
     * @param taskCount
     *         the number of tasks to generate.
     * @param computation
     *         {@link TwoStageUniform} for the computation size.
     * @param communication
     *         {@link TwoStageUniform} for the communication size.
     * @param idSupplier
     *         supplier of ids for the generated tasks. It must
     *         supply <b>at least {@code taskCount} unique</b>
     *         ids, lest the results be undefined.
     * @param random
     *         {@link Distribution} to be invoked to generate
     *         random values when needed.
     */
    /* package-private */ RandomicWorkloadGenerator (
            final int taskCount,
            final TwoStageUniform computation,
            final TwoStageUniform communication,
            final IntSupplier idSupplier,
            final Distribution random
    ) {
        this.computation   = computation;
        this.communication = communication;
        this.taskCount     = taskCount;
        this.idSupplier    = idSupplier;
        this.random        = random;
    }

    /**
     * @return an id generated by the {@link #idSupplier}, to be used to make
     *         a new task.
     */
    @Override
    protected int makeTaskId () {
        return this.idSupplier.getAsInt();
    }

    /**
     * @return random communication size (in MBits), generated in accordance
     *         to the {@link TwoStageUniform} set for it.
     */
    @Override
    protected double makeTaskCommunicationSize () {
        return this.communication.generateValue(this.random);
    }

    /**
     * @return random computation size (in MFlops), generated in accordance
     *         to the {@link TwoStageUniform} set for it.
     */
    @Override
    protected double makeTaskComputationSize () {
        return this.computation.generateValue(this.random);
    }

    public double getComputationAverage () {
        return this.computation.intervalSplit();
    }

    public double getCommunicationAverage () {
        return this.communication.intervalSplit();
    }

    public double getComputationProbability () {
        return this.computation.firstIntervalProbability();
    }

    public double getCommunicationProbability () {
        return this.communication.firstIntervalProbability();
    }

    public double getComputationMaximum () {
        return this.computation.maximum();
    }

    public double getCommunicationMaximum () {
        return this.communication.maximum();
    }

    public double getComputationMinimum () {
        return this.computation.minimum();
    }

    public double getCommunicationMinimum () {
        return this.communication.minimum();
    }

    public int getTaskCount () {
        return this.taskCount;
    }
}