package ispd.utils.constants;

import ispd.motor.filas.Tarefa;

/**
 * Hosts various {@link String} constants used throughout the project.
 */
public enum StringConstants {
    ;

    /**
     * IP address of the local host, namely {@value #LOCALHOST}.
     */
    public static final String LOCALHOST = "127.0.0.1";

    /**
     * XML Template for a Task Trace File.
     * <p>
     * Should be used in conjunction with {@link java.text.MessageFormat MessageFormat} to replace
     * the placeholder text ({@code "{0}"}) with the text for the serialized tasks.
     */
    //language=XML
    public static final String TASK_TRACE_TEMPLATE =
        """
        <?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
        <!DOCTYPE system SYSTEM "iSPDcarga.dtd">
        <system>
        <trace>
        <format />
        {0}
        </trace>
        </system>
        """;

    /**
     * XML Tag Template for a single serialized task.
     * <p>
     * Should be used in conjunction with {@link String#format(String, Object...) String.format} to
     * fill in the task's attributes appropriately.
     * <p>
     * The expected attribute order is:
     * <ol>
     *     <li>The task's ID, as returned from {@link Tarefa#getIdentificador() getIdentificador}.</li>
     *     <li>The task's arrival/creation time, returned from {@link Tarefa#getTimeCriacao() getTimeCriacao}.</li>
     *     <li>The task's computation load size, from {@link Tarefa#getTamProcessamento() getTamProcessamento}.</li>
     *     <li>The task's communication load size, from {@link Tarefa#getTamComunicacao()} getTamComunicacao}.</li>
     *     <li>Finally, the tasks's user, from {@link Tarefa#getProprietario() getProprietario}.</li>
     * </ol>
     */
    public static final String TASK_TAG_TEMPLATE =
        """
        <task id="%d" arr="%s" sts="1" cpsz ="%s" cmsz="%s" usr="%s" />
        """;
}
