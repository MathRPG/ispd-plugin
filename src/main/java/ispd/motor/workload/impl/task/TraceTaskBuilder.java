package ispd.motor.workload.impl.task;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.task.*;
import java.util.*;

/**
 * Builds tasks from a {@link List} of {@link TraceTaskInfo}s.<br> The given {@link TraceTaskInfo}s
 * are converted into tasks, one by one, and distributed between the masters given as argument to
 * {@link #makeTaskFor(Processing)}, in the invoked call order.<br>
 */
public class TraceTaskBuilder extends TaskBuilder {

    private final List<TraceTaskInfo> traceTaskInfos;

    /**
     * Holds the current {@link TraceTaskInfo} being processed in the
     * {@link #makeTaskFor(Processing)} method. It is initialized with {@code null}.
     */
    protected TraceTaskInfo currTaskInfo = null;

    /**
     * Initialize an instance with the given {@link List} of {@link TraceTaskInfo}s.
     *
     * @param traceTaskInfos
     *     list of task information.
     *
     * @throws NullPointerException
     *     if given list is {@code null}.
     */
    public TraceTaskBuilder (final List<TraceTaskInfo> traceTaskInfos) {
        this.traceTaskInfos = new LinkedList<>(traceTaskInfos);
    }

    @Override
    protected String makeTaskUser (final Processing master) {
        return this.currTaskInfo.user();
    }

    @Override
    protected int makeTaskId () {
        return this.currTaskInfo.id();
    }

    /**
     * Pops a {@link TraceTaskInfo} object from the inner list {@link #traceTaskInfos} and converts
     * it into a task originating from the given {@link Processing}.
     *
     * @param master
     *     {@link Processing} that will host the task.
     *
     * @return created {@link GridTask}.
     *
     * @throws IndexOutOfBoundsException
     *     if there are no more usable task information instances in the inner list.
     * @apiNote this method can only be called successfully at most {@code n} times, where {@code n}
     * is the size of the {@link List} this instance was initialized with.
     */
    @Override
    public GridTask makeTaskFor (final Processing master) {
        this.currTaskInfo = this.traceTaskInfos.remove(0);
        return super.makeTaskFor(master);
    }

    @Override
    protected double makeTaskCommunicationSize () {
        return this.currTaskInfo.communicationSize();
    }

    @Override
    protected double makeTaskComputationSize () {
        return this.currTaskInfo.computationSize();
    }

    @Override
    protected double makeTaskCreationTime () {
        return this.currTaskInfo.creationTime();
    }
}