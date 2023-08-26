package ispd.gui.results;

import ispd.gui.utils.components.*;
import java.util.*;

/**
 * A {@link ResultsProcessingPane} is a class that represents a multipane
 * containing the bar and pie chart of the performed processing obtained from
 * the performed simulation.
 */
public class ResultsProcessingPane extends Multipane {

    /**
     * Constructor which creates a pane that contains results of processing
     * performed for each machine being shown in a bar and a pie chart.
     *
     * @param charts
     *         the simulation chart maker
     */
    public ResultsProcessingPane (final SimulationResultChartMaker charts) {
        super(List.of(
                new MultipaneButton("Bar Chart", charts.getProcessingBarChart()),
                new MultipaneButton("Pie Chart", charts.getProcessingPieChart())
        ));
    }
}
