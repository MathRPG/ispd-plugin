package ispd.motor.workload.impl;

import ispd.motor.queues.*;
import ispd.motor.queues.task.*;
import ispd.motor.workload.*;
import java.util.*;
import java.util.stream.*;

/**
 * Represents a workload from a homogeneous collection of other workloads. Specifically, when used
 * to host a collection of per-node tasks, the method {@link #makeTaskList(GridQueueNetwork)} can be used
 * to collect all per-node tasks into a single new workload.
 */
public class CollectionWorkloadGenerator implements WorkloadGenerator {

    private final List<PerNodeWorkloadGenerator> list;

    /**
     * Instantiate a CollectionWorkloadGenerator from a homogeneous {@link List} of other
     * {@link WorkloadGenerator}s.
     *
     * @param list
     *     list of {@link WorkloadGenerator}s
     */
    public CollectionWorkloadGenerator (
        final List<PerNodeWorkloadGenerator> list
    ) {
        this.list = list;
    }

    private static String makeStringForList (final Collection<? extends WorkloadGenerator> list) {
        return list.stream()
            .map(WorkloadGenerator::toString)
            .map(CollectionWorkloadGenerator::adaptStringRepresentation)
            .collect(Collectors.joining());
    }

    private static String adaptStringRepresentation (final String s) {
        return String.format("\t\t%s,\n", s);
    }

    /**
     * Make a task list from the inner list of {@link WorkloadGeneratorType#PER_NODE} workloads, or
     * an <b>empty</b> {@link ArrayList} if the list is of another type of
     * {@link WorkloadGenerator}.
     *
     * @return {@link List} with all tasks in the workloads in {@link #list}, or an empty
     * {@code ArrayList} if the type of workloads in the list is not
     * {@link PerNodeWorkloadGenerator}.
     */
    @Override
    public List<GridTask> makeTaskList (final GridQueueNetwork qn) {
        return this.list.stream()
            .flatMap(load -> load.makeTaskList(qn).stream())
            .collect(Collectors.toList());
    }

    /**
     * @return the type of workload in the inner list {@link #list}.
     */
    @Override
    public WorkloadGeneratorType getType () {
        return WorkloadGeneratorType.PER_NODE;
    }

    /**
     * The iconic model format for this workload generator consists of the format for all the models
     * on its {@link #list}, concatenated together by newlines.
     */
    @Override
    public String formatForIconicModel () {
        return this.list.stream()
            .map(WorkloadGenerator::formatForIconicModel)
            .collect(Collectors.joining("\n"));
    }

    /**
     * The string representation for workloads of this class contains the type of the workload
     * generators in the inner list, and a representation for such list.
     */
    @Override
    public String toString () {
        return """
               CollectionWorkloadGenerator{
                   list=[
               %s
                   ],
               }""".formatted(
            makeStringForList(this.list)
        );
    }

    /**
     * @return the inner workload generator list.
     */
    public List<PerNodeWorkloadGenerator> getList () {
        return this.list;
    }
}