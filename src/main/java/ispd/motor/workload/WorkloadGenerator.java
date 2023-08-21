package ispd.motor.workload;

import ispd.motor.filas.*;
import java.util.*;

/**
 * Represents a workload generator from some data source.<br> Workloads are represented as a
 * {@link List} of {@link Tarefa}s.
 */
public interface WorkloadGenerator {

    /**
     * Create a {@link Tarefa} list as currently configured, distributed between the masters in the
     * given {@link RedeDeFilas}.
     *
     * @param qn
     *     {@link RedeDeFilas} with masters that will host the {@link Tarefa}s.
     *
     * @return {@link List} of {@link Tarefa}s generated.
     */
    List<Tarefa> makeTaskList (RedeDeFilas qn);

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