package ispd.gui.results;

import ispd.motor.metrics.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A {@link ResultsResourcePane} is a class that represents a pane containing
 * the information about all the processing and communication performed of each
 * simulated component.
 */
public class ResultsResourcePane extends JScrollPane {

    /**
     * Constructor which creates a pane in which the performed results in total
     * for each machine and each network link are shown in a tabular view.
     *
     * @param metrics
     *         the simulation metrics
     */
    public ResultsResourcePane (final General metrics) {
        final var table = new JTable();
        final var columns = new Object[] {
                "Label", "Owner", "Processing performed", "Communication performed"
        };

        this.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.setViewportView(table);

        table.setModel(new DefaultTableModel(metrics.makeResourceTable(), columns));
    }
}
