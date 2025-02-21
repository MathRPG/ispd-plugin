package ispd.motor.workload.impl;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.queues.task.*;
import ispd.motor.random.*;
import ispd.motor.workload.*;
import ispd.motor.workload.impl.task.*;
import ispd.policy.scheduling.*;
import ispd.utils.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import jdk.jfr.*;

/**
 * Generates a workload from parsing a trace file, and evenly distributes the tasks of such workload
 * among all masters in a {@link GridQueueNetwork}.<br> Some particularities:
 * <ul>
 *     <li>How the class interprets information gathered from the trace file
 *     about a task depends on its {@link #traceType}. See
 *     {@link TraceTaskBuilder} and {@link ExternalTraceTaskBuilder} for
 *     their differences.</li>
 *     <li>It <b>updates the {@link GridQueueNetwork}</b> with information about
 *     the users found in the tasks read from the trace file.</li>
 * </ul>
 *
 * @see TraceTaskInfo
 * @see TraceTaskBuilder
 * @see ExternalTraceTaskBuilder
 */
public class TraceFileWorkloadGenerator implements WorkloadGenerator {

    private static final int HEADER_LINE_COUNT = 5;

    @Unsigned
    private final int taskCount;

    private final File traceFile;

    private final String traceType;

    private final List<TraceTaskInfo> tasks;

    /**
     * Instantiate a generator with the given properties of a trace file.
     *
     * @param traceFile
     *     the trace {@link File} that will be processed. The file should exist.
     * @param taskCount
     *     the number of tasks to be processed from the trace file. Must be positive and
     *     non-strictly less than the number of actual tasks present in the trace file.
     * @param traceType
     *     the type of trace being processed; it will affect the interpretation of certain task
     *     properties read from the file. It must be one of {@code {iSPD, SWF, GWF}}
     *
     * @see #makeTaskList(GridQueueNetwork)
     * @see #makeTaskBuilder(GridQueueNetwork)
     */
    public TraceFileWorkloadGenerator (
        final File traceFile,
        final int taskCount,
        final String traceType
    ) {
        this.traceFile = traceFile;
        this.taskCount = taskCount;
        this.traceType = traceType;
        this.tasks     = this.getTraceTaskInfoFromFile();
    }

    /**
     * Update the {@link GridQueueNetwork}'s scheduler metrics with the given user ids. All added users
     * have, by default, {@literal 0} computational power and {@literal 100} usage limit.
     *
     * @param qn
     *     {@link GridQueueNetwork} with schedulers to be updated.
     * @param users
     *     {@link List} of user ids.
     */
    private static void updateSchedulerUserMetrics (
        final GridQueueNetwork qn,
        final List<String> users
    ) {
        final var count     = users.size();
        final var compPower = CollectionUtils.filledList(0.0, count);
        final var limits    = CollectionUtils.filledList(100.0, count);

        qn.getMestres().stream()
            .map(GridMaster.class::cast)
            .map(GridMaster::getEscalonador)
            .map(SchedulingPolicy::getMetricaUsuarios)
            .forEach(m -> m.addAllUsuarios(users, compPower, limits));
    }

    /**
     * Creates a task list from the trace file, updates the given {@link GridQueueNetwork} and its
     * schedulers with information about the users found in the trace, and distributes the generated
     * tasks evenly among all masters.
     *
     * @throws IndexOutOfBoundsException
     *     if the number of tasks to be generated (i.e., {@link #taskCount}) exceeds the number of
     *     tasks in the trace file; which may happen if there was an error collecting the tasks from
     *     the file.
     * @see #getTraceTaskInfoFromFile()
     * @see TraceTaskBuilder
     */
    @Override
    public List<GridTask> makeTaskList (final GridQueueNetwork qn) {
        this.updateQueueNetworkWithTaskUsers(qn);

        return this.makeTaskBuilder(qn)
            .makeTasksDistributedAmongMasters(qn, this.taskCount);
    }

    /**
     * @return {@link WorkloadGeneratorType#TRACE}
     */
    @Override
    public WorkloadGeneratorType getType () {
        return WorkloadGeneratorType.TRACE;
    }

    /**
     * The iconic model format for this workload generator consists only of the absolute path of the
     * {@link #traceFile} used.
     */
    @Override
    public String formatForIconicModel () {
        return this.traceFile.getAbsolutePath();
    }

    /**
     * The string representation for this workload generator includes its task count, type of the
     * trace file used and its path.
     */
    @Override
    public String toString () {
        return """
               TraceFileWorkloadGenerator{
                   taskCount=%d,
                   type='%s',
                   path=%s,
               }""".formatted(
            this.taskCount,
            this.traceType,
            this.traceFile.getAbsolutePath()
        );
    }

    /**
     * Attempts to gather task information from a trace file, and collect it to a list.
     *
     * @return a list of {@link TraceTaskInfo}s gathered from the trace file, or an empty
     * {@link List} if an {@link IOException} happens during the process.
     */
    private List<TraceTaskInfo> getTraceTaskInfoFromFile () {
        try (
            final var br = new BufferedReader(new FileReader(
                this.traceFile,
                StandardCharsets.UTF_8
            ))
        ) {
            return br.lines()
                .skip(HEADER_LINE_COUNT)
                .map(TraceTaskInfo::new)
                .toList();
        } catch (final IOException | UncheckedIOException ex) {
            Logger.getLogger(TraceFileWorkloadGenerator.class.getName())
                .log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }

    /**
     * Update the given {@link GridQueueNetwork} and its scheduler metrics with all user ids (found in
     * the trace file) that don't exist in it already.
     *
     * @param qn
     *     {@link GridQueueNetwork} to be updated.
     */
    private void updateQueueNetworkWithTaskUsers (final GridQueueNetwork qn) {
        final var userIds = this.tasks.stream()
            .map(TraceTaskInfo::user)
            .distinct()
            .filter(Predicate.not(qn.getUsuarios()::contains))
            .toList();

        updateSchedulerUserMetrics(qn, userIds);
        qn.getUsuarios().addAll(userIds);
    }

    /**
     * Make a task builder, which is different depending on the {@link #traceType} this instance was
     * initialized with.
     *
     * @param qn
     *     {@link GridQueueNetwork} that will host tasks found in the trace file, necessary in this
     *     method for normalizing the computational size of external trace models.
     *
     * @return appropriate {@link TraceTaskBuilder} to interpret {@link TraceTaskInfo}s found in the
     * trace file.
     *
     * @throws IllegalArgumentException
     *     if {@link #traceType} is none of {@code {iSPD, SWF, GWF}}.
     */
    private TraceTaskBuilder makeTaskBuilder (final GridQueueNetwork qn) {
        return switch (this.traceType) {
            case "iSPD" -> new TraceTaskBuilder(this.tasks);
            case "SWF", "GWF" -> new ExternalTraceTaskBuilder(
                this.tasks,
                new Distribution(System.currentTimeMillis()),
                qn.averageComputationalPower()
            );
            default -> throw new IllegalArgumentException(
                "Unrecognized trace type '%s'".formatted(this.traceType));
        };
    }

    public int getTaskCount () {
        return this.taskCount;
    }

    public File getTraceFile () {
        return this.traceFile;
    }

    public String getTraceType () {
        return this.traceType;
    }
}