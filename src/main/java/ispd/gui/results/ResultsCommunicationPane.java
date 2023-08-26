package ispd.gui.results;

import ispd.gui.utils.components.*;
import java.util.*;

/**
 * A {@link ResultsCommunicationPane} is a class that represents a multipane
 * containing the bar and pie chart of the performed communication obtained
 * from the performed simulation.
 */
public class ResultsCommunicationPane extends Multipane {

    /**
     * Constructor which creates a pane that contains results of communication
     * performed for each network link being shown in a bar and pie chart.
     *
     * @param charts
     *         the simulation chart maker
     */
    public ResultsCommunicationPane (final SimulationResultChartMaker charts) {
        super(List.of(
                new MultipaneButton("Bar Chart", charts.getCommunicationBarChart()),
                new MultipaneButton("Pie Chart", charts.getCommunicationPieChart())
        ));
    }
}
