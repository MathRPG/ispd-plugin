package ispd.gui.results;

import ispd.gui.utils.fonts.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.impl.*;
import javax.swing.*;

/**
 * A {@link ResultsUsersPane} is a class that represents a pane containing the
 * information about processing and communication performed for resolve the tasks
 * for each user.
 */
public class ResultsUsersPane extends JScrollPane {

    /**
     * Constructor which creates a pane that contains the processing and
     * communication results for each user.
     *
     * @param queueNetwork
     *         the simulation queue network
     */
    public ResultsUsersPane (final GridQueueNetwork queueNetwork) {
        final var textArea = new JTextArea();

        this.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.setViewportView(textArea);

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setFont(CourierNew.BOLD);
        textArea.setText(this.makeUsersResultText(queueNetwork));
    }

    /**
     * It creates the users results text, this text contains the queue time and
     * the time for processing and communication for each user.
     *
     * @param queueNetwork
     *         the simulation queue network
     *
     * @return this text contains the queue time and time for processing and
     *         communication for each user.
     */
    private String makeUsersResultText (final GridQueueNetwork queueNetwork) {
        final var master = (GridMaster) queueNetwork.getMestres().get(0);
        final var userMetrics = master.getEscalonador().getMetricaUsuarios();

        final var sb = new StringBuilder();

        for (final var userName : userMetrics.getUsuarios()) {
            final var completedTasks = userMetrics.getTarefasConcluidas(userName);

            sb.append("\n\n\t\tUser ").append(userName).append('\n');
            sb.append("\nNumber of task: ").append(completedTasks.size()).append('\n');

            var avgQueueTimeProcessing    = 0.0;
            var avgQueueTimeCommunication = 0.0;

            var avgTimeProcessing    = 0.0;
            var avgTimeCommunication = 0.0;

            var avgSystemTimeProcessing    = 0.0;
            var avgSystemTimeCommunication = 0.0;

            var tasksAmount = 0;

            for (final var task : userMetrics.getTarefasConcluidas(userName)) {
                avgQueueTimeProcessing = task.getMetricas().getTempoEsperaProc();
                avgQueueTimeCommunication += task.getMetricas().getTempoEsperaComu();

                avgTimeProcessing = task.getMetricas().getTempoProcessamento();
                avgTimeCommunication += task.getMetricas().getTempoComunicacao();

                tasksAmount++;
            }

            avgQueueTimeProcessing /= tasksAmount;
            avgQueueTimeCommunication /= tasksAmount;

            avgTimeProcessing /= tasksAmount;
            avgTimeCommunication /= tasksAmount;

            avgSystemTimeProcessing    = avgQueueTimeProcessing + avgTimeProcessing;
            avgSystemTimeCommunication = avgQueueTimeCommunication + avgTimeCommunication;

            sb.append("\n Communication \n");
            sb.append(String.format("    Queue average time: %g seconds.\n", avgQueueTimeCommunication));
            sb.append(String.format("    Communication average time: %g seconds.\n", avgTimeCommunication));
            sb.append(String.format("    System average time: %g seconds.\n", avgSystemTimeCommunication));
            sb.append("\n Processing \n");
            sb.append(String.format("    Queue average time: %g seconds.\n", avgQueueTimeProcessing));
            sb.append(String.format("    Processing average time: %g seconds.\n", avgTimeProcessing));
            sb.append(String.format("    System average time: %g seconds.\n", avgSystemTimeProcessing));
        }

        return sb.toString();
    }
}
