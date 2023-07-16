package ispd.arquivo.interpretador.cargas;

import ispd.motor.filas.Tarefa;
import ispd.utils.constants.StringConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;

/**
 * Responsible for outputting a simulation trace to a file path, from a collection of tasks
 */
public enum TaskTraceSerializer {
    ;

    /**
     * Outputs simulation trace from collection of tasks.
     * <p>
     * Does not serialize tasks which return {@code true} on {@link Tarefa#isCopy()}.
     */
    public static void outputTaskTraceToFile (
        final Collection<? extends Tarefa> tasks,
        final File outputFile
    ) {
        try (
            final var out = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))
        ) {
            final var outputText = MessageFormat.format(
                StringConstants.TASK_TRACE_TEMPLATE,
                Tarefa.serializeTaskCollection(tasks)
            );
            out.write(outputText);
        } catch (final IOException ignored) {
        }
    }
}