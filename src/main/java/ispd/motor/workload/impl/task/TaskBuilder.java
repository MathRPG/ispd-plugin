package ispd.motor.workload.impl.task;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import java.util.*;
import java.util.stream.*;

/**
 * <p>Abstract class with the purpose to create a task from some data source.
 * In particular, by overriding the {@code protected} methods such as {@link #makeTaskId()}, a
 * subclass can determine how to transform the information gathered from such data source into a
 * task.</p> For an example, see {@link TraceTaskBuilder}.
 *
 * @see TraceTaskBuilder
 */
public abstract class TaskBuilder {

    /**
     * Size of the file returned to the master, in Mbits.
     */
    private static final double FILE_MASTER_RECEIVE_SIZE = 0.000_976_562_5;

    /**
     * Create a suitable id for a new task.
     *
     * @return an integral value representing a task id.
     */
    protected abstract int makeTaskId ();

    /**
     * Select a suitable user for a new task. Such generation may involve the given
     * {@link Processing}, or not.
     *
     * @param master
     *     {@link Processing} that may host information about which user the task will be
     *     linked with.
     *
     * @return a user id for the new task.
     */
    protected abstract String makeTaskUser (Processing master);

    /**
     * @return the task communication size (in MBits), usually random.
     */
    protected abstract double makeTaskCommunicationSize ();

    /**
     * @return the task computation size (in MFlops), usually random.
     */
    protected abstract double makeTaskComputationSize ();

    /**
     * @return the task creation time (in seconds), usually random.
     */
    protected abstract double makeTaskCreationTime ();

    /**
     * Make the given {@code taskCount} number of tasks, distributed as fairly as possible among the
     * masters in the given {@link GridQueueNetwork}.<br> The distribution functions as follows:
     * <ul>
     *     <li>If there are {@code k * n} tasks to distribute and {@code n}
     *     masters, each master receives {@code k} tasks, as expected.
     *     However, each master <b>does not receive the <i>first</i></b>
     *     {@code k} tasks, but every {@code k}th one. (The distribution is
     *     non-sequential).</li>
     *     <li>If there are {@code k * n + m} tasks to distribute and {@code
     *     n} masters, the first {@code m} masters receive {@code k + 1}
     *     tasks, while the remaining {@code n - m} masters receive {@code k}
     *     . Again, each master receives a task every {@code k}th task
     *     processed.</li>
     * </ul>
     *
     * @param qn
     *     {@link GridQueueNetwork} with the masters that will host the tasks.
     * @param taskCount
     *     amount of tasks to be created. <b>Must be positive</b>.
     *
     * @return collection of created {@link GridTask}s.
     */
    public List<GridTask> makeTasksDistributedAmongMasters (
        final GridQueueNetwork qn,
        final int taskCount
    ) {
        final var masters = qn.getMestres();

        return IntStream.range(0, taskCount)
            .map(i -> i % masters.size())
            .mapToObj(masters::get)
            .map(this::makeTaskFor)
            .collect(Collectors.toList());
    }

    /**
     * Create a {@link GridTask} originating at the given {@link Processing} instance.
     *
     * @param master
     *     {@link Processing} that will host the task.
     *
     * @return a generated {@link GridTask}.
     */
    public GridTask makeTaskFor (final Processing master) {
        return new GridTask(
            this.makeTaskId(),
            this.makeTaskUser(master),
            this.makeTaskApplication(),
            master,
            this.makeTaskCommunicationSize(),
            FILE_MASTER_RECEIVE_SIZE,
            this.makeTaskComputationSize(),
            this.makeTaskCreationTime()
        );
    }

    /**
     * @return the application which a new task will be associated with; by default,
     * {@code "application1"}.
     */
    protected String makeTaskApplication () {
        return "application1";
    }
}