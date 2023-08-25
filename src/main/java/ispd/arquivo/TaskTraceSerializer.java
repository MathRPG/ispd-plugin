package ispd.arquivo;

import ispd.motor.filas.*;
import ispd.utils.constants.*;
import java.io.*;
import java.nio.charset.*;
import java.text.*;
import java.util.*;

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