package ispd.gui;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class LogExceptions implements Thread.UncaughtExceptionHandler {

    private static final String ERROR_FOLDER_PATH = "Erros";

    private static final String ERROR_FILE_PREFIX = "Error_ISPD";

    private static final String ERROR_CODE_DATE_FORMAT = "yyyyMMddHHmmss";

    private static final String ERROR_FILE_MESSAGE_FORMAT =
        """

        ---------- error description ----------
        %s
        ---------- error description ----------
        """;

    private static final String ERROR_GUI_MESSAGE_FORMAT =
        """
        Error encountered during system operation.
        Error saved in the file: %s
        Please send the error to the developers.
        %s
        """;

    private static final int SCROLL_PANE_PREFERRED_WIDTH = 500;

    private static final int SCROLL_PANE_PREFERRED_HEIGHT = 300;

    private final JTextArea textArea = readonlyTextArea();

    private final JScrollPane scrollPane = resizedScrollPaneFrom(this.textArea);

    private Component parentComponent = null;

    public LogExceptions () {
        createErrorFolderIfNonExistent();
    }

    private static void createErrorFolderIfNonExistent () {
        final var aux = new File(ERROR_FOLDER_PATH);

        if (aux.exists()) {
            return;
        }

        aux.mkdir();
    }

    private static JTextArea readonlyTextArea () {
        final JTextArea area = new JTextArea();
        area.setEditable(false);
        return area;
    }

    private static JScrollPane resizedScrollPaneFrom (final JTextArea textArea) {
        final JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(
            SCROLL_PANE_PREFERRED_WIDTH,
            SCROLL_PANE_PREFERRED_HEIGHT
        ));
        return scroll;
    }

    private static String buildErrorFilePath (final Date date) {
        final var errorCode = buildErrorFileTimestamp(date);
        return String.format(
            "%s%s%s_%s",
            ERROR_FOLDER_PATH,
            File.separator,
            ERROR_FILE_PREFIX,
            errorCode
        );
    }

    private static void printErrorToFile (
        final String errorMessage, final File file
    )
        throws IOException {
        try (
            final var fw = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8);
            final var pw = new PrintWriter(fw, true)
        ) {
            pw.print(errorMessage);
        }
    }

    private static String buildErrorFileTimestamp (final Date date) {
        final var dateFormat = new SimpleDateFormat(ERROR_CODE_DATE_FORMAT);
        return dateFormat.format(date);
    }

    @Override
    public void uncaughtException (final Thread t, final Throwable e) {
        final var errStream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(errStream));
        this.processError(errStream);
    }

    public void setParentComponent (final Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    private void processError (final ByteArrayOutputStream errorStream) {
        if (errorStream.size() == 0) {
            return;
        }

        try {
            final var errorMessage =
                String.format(ERROR_FILE_MESSAGE_FORMAT, errorStream);
            this.displayError(errorMessage);

            errorStream.reset();
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(
                this.parentComponent,
                e.getMessage(),
                "Warning",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void displayError (final String errorMessage)
        throws IOException {
        final var errorFile = new File(buildErrorFilePath(new Date()));

        printErrorToFile(errorMessage, errorFile);
        this.displayErrorInGui(errorMessage, errorFile);
    }

    private void displayErrorInGui (final String errorMessage, final File file) {
        final var path = file.getAbsolutePath();
        final var formattedMessage =
            String.format(ERROR_GUI_MESSAGE_FORMAT, path, errorMessage);

        this.textArea.setText(formattedMessage);

        JOptionPane.showMessageDialog(
            this.parentComponent,
            this.scrollPane,
            "System Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}