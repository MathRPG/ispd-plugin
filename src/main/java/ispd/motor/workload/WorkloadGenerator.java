package ispd.motor.workload;

import ispd.motor.queues.*;
import ispd.motor.queues.task.*;
import java.util.*;

/**
 * Represents a workload generator from some data source.<br> Workloads are represented as a
 * {@link List} of {@link GridTask}s.
 */
public interface WorkloadGenerator {

    /**
     * Create a {@link GridTask} list as currently configured, distributed between the masters in the
     * given {@link GridQueueNetwork}.
     *
     * @param qn
     *     {@link GridQueueNetwork} with masters that will host the {@link GridTask}s.
     *
     * @return {@link List} of {@link GridTask}s generated.
     */
    List<GridTask> makeTaskList (GridQueueNetwork qn);

    /**
     * @return the generator type of this instance.
     *
     * @see WorkloadGeneratorType
     */
    WorkloadGeneratorType getType ();

    /**
     * Outputs the current workload generator configuration, formatted appropriately for inclusion
     * in an iconic model file. For a more
     * <i>human-readable</i> representation, use {@link #toString()}.<br>
     *
     * @return iconic-model fitting string representation of how the generator is currently
     * configured.
     */
    String formatForIconicModel ();

    /**
     * @return human-readable string representation of how the generator is currently configured.
     */
    @Override
    String toString ();
}