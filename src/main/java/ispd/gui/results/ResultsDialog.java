package ispd.gui.results;

import ispd.motor.metrics.*;
import ispd.motor.queues.*;
import ispd.motor.queues.task.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * A {@link ResultsDialog} class is used to display a window containing all types of information
 * from the performed simulation.
 */
public class ResultsDialog extends JDialog {

    public static final Dimension CHART_PREFERRED_SIZE = new Dimension(600, 400);

    private final General metrics;

    private final GridQueueNetwork queueNetwork;

    private final List<GridTask> tasks;

    private final SimulationResultChartMaker charts;

    /**
     * Constructor which specifies the frame who is owner of this result dialog, the metrics, the
     * queue network and the task list, these three last variables are used to construct the results
     * to be displayed in this dialog.
     *
     * @param owner
     *     the owner
     * @param metrics
     *     the metrics
     * @param queueNetwork
     *     the queue network
     * @param tasks
     *     the task list
     *
     * @throws NullPointerException
     *     if metrics, queue network or task list are {@code null}
     */
    public ResultsDialog (
        final Frame owner,
        final General metrics,
        final GridQueueNetwork queueNetwork,
        final List<GridTask> tasks
    ) {
        super(owner, true);

        if (metrics == null) {
            throw new NullPointerException(
                "metrics is null. It was not possible to show the results.");
        }

        if (queueNetwork == null) {
            throw new NullPointerException(
                "queue network is null. It was not possible to show the results.");
        }

        /* Ensure the task list is not null */
        if (tasks == null) {
            throw new NullPointerException(
                "task list is null. It was not possible to show the results.");
        }

        this.metrics      = metrics;
        this.queueNetwork = queueNetwork;
        this.tasks        = tasks;
        this.charts       = new SimulationResultChartMaker(metrics, queueNetwork, tasks);

        this.initDialogComponents();
        this.pack();
    }

    /**
     * It initializes all components that is going to be displayed in this dialog.
     */
    private void initDialogComponents () {
        final var mainPane           = new JTabbedPane();
        final var globalPane         = new ResultsGlobalPane(this.metrics, this.charts, this.tasks);
        final var tasksPane          = new ResultsTasksPane(this.metrics);
        final var usersPane          = new ResultsUsersPane(this.queueNetwork);
        final var resourcesPane      = new ResultsResourcePane(this.metrics);
        final var processingPane     = new ResultsProcessingPane(this.charts);
        final var communicationPane  = new ResultsCommunicationPane(this.charts);
        final var computingPowerPane = new ResultsComputingPowerPane(this.charts);

        mainPane.addTab("Global", globalPane);
        mainPane.addTab("Tasks", tasksPane);
        mainPane.addTab("Users", usersPane);
        mainPane.addTab("Resources", resourcesPane);
        mainPane.addTab("Chart of the processing", processingPane);
        mainPane.addTab("Chart of the communication", communicationPane);
        mainPane.addTab("Use of computing power through time", computingPowerPane);

        this.add(mainPane);
    }
}
