package ispd.arquivo.xml;

import ispd.arquivo.*;
import ispd.motor.queues.task.*;
import ispd.utils.constants.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Class responsible for converting traces into XML files.
 */
public class TraceXML {

    private static final char FILE_PATH_DELIMITER = File.separatorChar;

    private static final char FILE_EXT_DELIMITER = '.';

    private static final Pattern TABS = Pattern.compile("\t");

    private static final Pattern WHITE_SPACE = Pattern.compile("\\s\\s++");

    private static final int NON_TASK_RELATED_LINES = 7;

    private final String path;

    private String type;

    private String output;

    private int taskCount = 0;

    public TraceXML (final String path) {
        this.path = path;

        final var split = splitAtLast(path, FILE_EXT_DELIMITER);

        this.output = split[0] + FileExtensions.WORKLOAD_MODEL;
        this.type   = split[1].toUpperCase();
    }

    private static String[] splitAtLast (final String str, final char delimiter) {
        final int i = str.lastIndexOf(delimiter);
        return new String[] {
            str.substring(0, i), str.substring(i + 1) // Skip delimiter
        };
    }

    private static boolean shouldSkipLine (final String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        return str.charAt(0) == ';' || str.charAt(0) == '#';
    }

    private static String makeSwfTaskTag (final String str) {
        final var fields = WHITE_SPACE.matcher(str).replaceAll(" ").trim().split(" ");

        return """
               <task id="%s" arr="%s" sts="%s" cpsz ="%s" cmsz="-1" usr="user%s" />
               """.formatted(fields[0], fields[1], fields[10], fields[3], fields[11]);
    }

    private static String makeGwfTaskTag (final String line, final int firstTaskArrival) {

        final var fields = TABS.matcher(line).replaceAll(" ").trim().split(" ");

        if ("-1".equals(fields[3])) {
            return "";
        }

        return """
               <task id="%s" arr="%d" sts="%s" cpsz ="%s" cmsz="%s" usr="%s" />
               """.formatted(
            fields[0],
            Integer.parseInt(fields[1]) - firstTaskArrival,
            fields[10],
            fields[3],
            fields[20],
            fields[11]
        );
    }

    private static int parseArrivalTime (final String line) {
        final var fields = TABS.matcher(line).replaceAll(" ").trim().split(" ");

        return Integer.parseInt(fields[1]);
    }

    private static String makeTaskDescription (final GridTask t) {
        return """
               <task id="%d" arr="%s" sts="1" cpsz ="%s" cmsz="%s" usr="%s" />
               """.formatted(
            t.getIdentificador(),
            t.getTimeCriacao(),
            t.getTamProcessamento(),
            t.getArquivoEnvio(),
            t.getProprietario()
        );
    }

    /**
     * @return debug info from last conversion (if successful)
     */
    @Override
    public String toString () {
        this.output = splitAtLast(this.output, FILE_PATH_DELIMITER)[1];

        return """
               File %s was generated sucessfully:
                   - Generated from the format: %s
                   - File has a workload of %d tasks""".formatted(
            this.output,
            this.type,
            this.taskCount
        );
    }

    /**
     * @return output path of convertions
     */
    public String getSaida () {
        return this.output;
    }

    /**
     * @return type of last conversion
     */
    public String getTipo () {
        return this.type;
    }

    /**
     * @return number of tasks parsed in last conversion
     */
    public int getNum_Tasks () {
        return this.taskCount;
    }

    /**
     * Convert task file into simulation trace
     */
    public void convert () {
        try (
            final var in = new BufferedReader(new FileReader(this.path, StandardCharsets.UTF_8));
            final var out = new BufferedWriter(new FileWriter(this.output, StandardCharsets.UTF_8))
        ) {
            out.write("""
                      <?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
                      <!DOCTYPE system SYSTEM " iSPDcarga.dtd">
                      <system>
                      <trace>
                      <format kind="%s" />
                      %s
                      </trace>
                      </system>""".formatted(this.type, this.joinTasksTags(in)));
        } catch (final IOException ignored) {
        }
    }

    private String joinTasksTags (final BufferedReader in)
        throws IOException {
        final var sb = new StringBuilder(0);

        final var firstTaskArrival = Optional.<Integer>empty();

        for (this.taskCount = 0; in.ready(); this.taskCount++) {
            final var line = in.readLine();

            if (shouldSkipLine(line)) {
                continue;
            }

            final String str;

            if (this.isSwfType()) {
                str = makeSwfTaskTag(line);
            } else if (this.isGwfType()) {
                str =
                    makeGwfTaskTag(line, firstTaskArrival.orElseGet(() -> parseArrivalTime(line)));
            } else {
                str = "";
            }

            sb.append(str);
        }

        return sb.toString();
    }

    private boolean isSwfType () {
        return "SWF".equals(this.type);
    }

    private boolean isGwfType () {
        return "GWF".equals(this.type);
    }

    /**
     * Read Wms load from file path passed in constructor.
     *
     * @return Text describing the procedure or errors, if any
     */
    public String LerCargaWMS () {
        try (
            final var in = new BufferedReader(new FileReader(this.path, StandardCharsets.UTF_8))
        ) {
            final var fileName = splitAtLast(this.path, FILE_PATH_DELIMITER)[1];

            final var sb =
                new StringBuilder("File %s was opened sucessfully:\n".formatted(fileName));

            int i;
            for (i = 0; in.ready(); ++i) {
                final var line = in.readLine();

                if (i != 4) {
                    continue;
                }

                final var fields = line.split(" ")[1].split("\"");

                sb.append("\t- File was extracted of trace in the format: %s\n".formatted(fields[1]));

                this.type = fields[1];
            }

            // Some lines on the file are not tasks, so discounted.
            this.taskCount = i - NON_TASK_RELATED_LINES;

            sb.append("\t- File has a workload of %d tasks".formatted(i));

            return sb.toString();
        } catch (final IOException ex) {
            Logger.getLogger(TraceXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "File has no correct format";
    }

    /**
     * Output simulation trace from collection of tasks
     *
     * @see TaskTraceSerializer
     */
    public void geraTraceSim (final Collection<? extends GridTask> tasks) {
        try (
            final var out = new BufferedWriter(new FileWriter(this.path, StandardCharsets.UTF_8))
        ) {
            this.type = "iSPD";

            final var sb = new StringBuilder("""
                                             <?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
                                             <!DOCTYPE system SYSTEM "iSPDcarga.dtd">
                                             <system>
                                             <trace>
                                             <format kind="%s" />
                                             """.formatted(this.type));

            final var descriptions =
                tasks
                    .stream()
                    .filter(Predicate.not(GridTask::isCopy))
                    .map(TraceXML::makeTaskDescription)
                    .toList();

            descriptions.forEach(sb::append);

            this.taskCount = descriptions.size();

            sb.append("""
                      </trace>
                      </system>""");

            out.write(sb.toString());

            this.output = this.path;
        } catch (final IOException ignored) {
        }
    }
}


