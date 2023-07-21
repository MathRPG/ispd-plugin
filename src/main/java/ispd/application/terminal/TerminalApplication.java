package ispd.application.terminal;

import ispd.application.Application;
import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.xml.IconicoXML;
import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.SilentSimulationProgress;
import ispd.motor.SimulacaoParalela;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.Simulation;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;
import ispd.utils.constants.FileExtensions;
import ispd.utils.constants.StringConstants;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;

/**
 * A class for setting up the terminal part of iSPD and run the simulations.
 */
public class TerminalApplication implements Application {

    private static final int DEFAULT_PORT = 2004;

    private static final String UNREACHABLE_STATEMENT = "Unreachable Statement";

    private final Optional<File> inputFile;

    private final Optional<File> outputFolder;

    private final Modes mode;

    private final int nThreads;

    private final boolean parallel;

    private final int serverPort;

    private final Inet4Address serverAddress;

    private final int nExecutions;

    private final ProgressoSimulacao simulationProgress = new SilentSimulationProgress();

    /**
     * Pre run of the terminal application, adding the necessary flags to the class before it runs.
     *
     * @param args
     *     Arguments from the command line.
     */
    public TerminalApplication (final String[] args) {
        final var cmd = this.commandLinePreparation(OptionsHolder.ALL_OPTIONS, args);

        this.mode          = setMode(cmd);
        this.serverPort    = getIntOptionOr(cmd, "P", TerminalApplication.DEFAULT_PORT);
        this.nExecutions   = getIntOptionOr(cmd, "e", 1);
        this.nThreads      = this.setNThreads(cmd);
        this.inputFile     = this.setInputFile(cmd);
        this.outputFolder  = this.setOutputFolder(cmd);
        this.parallel      = this.setParallelSimulation(cmd);
        this.serverAddress = this.setServerAddress(cmd);

        if (this.mode.requiresModel() && this.inputFile.isEmpty()) {
            final var message = "It needs a model to simulate.";
            System.out.println(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Set the mode for running the terminal application.
     *
     * @param cmd
     *     CommandLine used in the application.
     *
     * @return An int representing a mode based on the options.
     */
    private static Modes setMode (final CommandLine cmd) {
        return Arrays.stream(Modes.values())
            .filter(v -> v.isOptionMode(cmd))
            .findFirst()
            .orElse(Modes.SIMULATE);
    }

    private static Optional<File> getFileFromFirstArgument (final CommandLine cmd) {
        return cmd.getArgList().stream()
            .findFirst()
            .map(File::new);
    }

    private static void printVersion () {
        System.out.print(
            """
            iSPD version 3.1
              Iconic Simulator of Parallel and Distributed System
              Copyright 2010-2022, by GSPD from UNESP.
              Project Info: https://dcce.ibilce.unesp.br/spd
              Source Code: https://github.com/gspd/ispd
            """
        );
    }

    private static void printHelp () {
        new HelpFormatter().printHelp("java -jar iSPD.jar", OptionsHolder.ALL_OPTIONS);
    }

    private static Optional<File> getFileFromOption (final CommandLine cmd, final String opt) {
        return Optional.ofNullable(cmd.getOptionValue(opt)).map(File::new);
    }

    private static int getIntOptionOr (
        final CommandLine cmd,
        final String opt,
        final int defaultValue
    ) {
        return cmd.hasOption(opt)
               ? setIntValueFromOption(cmd, opt)
               : defaultValue;
    }

    /**
     * Get a value from an option from the command line.
     *
     * @param cmd
     *     The CommandLine that is being used.
     * @param op
     *     The string relative to the argument from the command line.
     *
     * @return The value of the option (if it exists and is valid).
     */
    private static int setIntValueFromOption (final CommandLine cmd, final String op) {
        try {
            return Integer.parseInt(cmd.getOptionValue(op));
        } catch (final NumberFormatException ignored) {
            System.out.printf("\"%s\" is not a valid number%n", cmd.getOptionValue(op));
            throw new RuntimeException(ignored);
        }
    }

    /**
     * Method for running the simulation based on the configuration done before.
     */
    @Override
    public void run () {
        switch (this.mode) {
            case HELP -> printHelp();
            case VERSION -> printVersion();
            case SIMULATE -> this.attemptLocalSimulation();
            case SERVER -> this.serverSimulation();
            case CLIENT -> this.clientSimulation();
        }
    }

    private void attemptLocalSimulation () {
        if (this.inputFile.isPresent()) {
            final var file = this.inputFile.get();

            if (file.getName().endsWith(FileExtensions.ICONIC_MODEL) && file.exists()) {
                this.runNSimulations();
            } else {
                System.out.printf("iSPD can not open the file: %s%n", file.getName());
            }
        }
    }

    /**
     * A simple method for creating the Common Cli's command line class.
     *
     * @param options
     *     The class Common Cli's Options class for the command line options.
     * @param args
     *     The arguments got from the command line.
     *
     * @return The command line class with the chosen options.
     */
    private CommandLine commandLinePreparation (final Options options, final String[] args) {
        try {
            return (new DefaultParser()).parse(options, args);
        } catch (final ParseException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the number of threads to use from the command line.
     *
     * @param cmd
     *     The command line class from Common Cli.
     *
     * @return A number got from the command line or the default 1.
     */
    private int setNThreads (final CommandLine cmd) {
        if (!cmd.hasOption("t")) {
            return 1;
        }

        final int threads = setIntValueFromOption(cmd, "t");

        if (this.nExecutions < 1) {
            System.out.printf("Number of executions is invalid (%d)%n", this.nExecutions);
            System.exit(1);
        }

        return Math.min(threads, this.nExecutions);
    }

    /**
     * Get the name of the model file from the command line.
     *
     * @param cmd
     *     The command line class from Common Cli.
     *
     * @return A configuration file with information for the simulation
     */
    private Optional<File> setInputFile (final CommandLine cmd) {
        return getFileFromOption(cmd, "in")
            .or(() -> getFileFromFirstArgument(cmd));
    }

    /**
     * Get the name of the output folder for the html export from the command line.
     *
     * @param cmd
     *     The command line class from Common Cli.
     *
     * @return The folder coming from the command line argument or an empty optional.
     */
    private Optional<File> setOutputFolder (final CommandLine cmd) {
        return getFileFromOption(cmd, "o");
    }

    /**
     * Get the option of parallel simulation from the command line and configure it.
     *
     * @param cmd
     *     The command line class from Command Cli.
     *
     * @return True if there is "p" in the command line of false otherwise.
     */
    private boolean setParallelSimulation (final CommandLine cmd) {
        return cmd.hasOption("p");
    }

    /**
     * Set the mode for running the terminal application.
     *
     * @param cmd
     *     CommandLine used in the application.
     *
     * @return An int representing a mode based on the options.
     */
    private Inet4Address setServerAddress (final CommandLine cmd) {
        try {
            final var hostName = cmd.getOptionValue("a", StringConstants.LOCALHOST);
            return (Inet4Address) InetAddress.getByName(hostName);
        } catch (final UnknownHostException e) {
            System.out.printf(
                "Error at getting the server address from command line. (%s)%n",
                e.getMessage()
            );
            System.exit(1);
            throw new AssertionError(TerminalApplication.UNREACHABLE_STATEMENT);
        }
    }

    /**
     * Run a number of simulations and calculate the time for each one, printing the results of them
     * at the end.
     */
    private void runNSimulations () {
        System.out.println("Simulation Initiated.");
        System.out.print("Opening iconic model. ->");

        final Document model         = this.getModelFromFile();

        if (model == null) {
            return;
        }

        final var      metrics       = new Metricas(IconicoXML.newListUsers(model));
        double         totalDuration = 0.0;
        for (int i = 1; i <= this.nExecutions; i++) {
            System.out.printf("* Simulation %d%n", i);

            final var    preSimInstant    = System.currentTimeMillis();
            final var    simMetric        = this.runASimulation(model);
            final var    postSimInstant   = System.currentTimeMillis();
            final double totalSimDuration = (double) (postSimInstant - preSimInstant) / 1000.0;

            System.out.printf("Simulation Execution Time = %f seconds%n", totalSimDuration);

            totalDuration += totalSimDuration;
            metrics.addMetrica(simMetric);
        }

        this.printSimulationResults(metrics, totalDuration);
    }

    /**
     * Print the simulation results
     *
     * @param metrics
     *     The metrics from the simulations
     * @param totalDuration
     *     The total duration of the simulations
     */
    private void printSimulationResults (final Metricas metrics, final double totalDuration) {
        System.out.println("Results:");
        metrics.calculaMedia();

        System.out.printf("  Total Simulation Execution Time = %sseconds%n", totalDuration);
        System.out.println(metrics.getMetricasGlobais());

        if (this.outputFolder.isPresent()) {
            final var html       = new SalvarResultadosHTML();
            final var chartMaker = new SimulationResultChartMaker(metrics);

            html.setMetricasTarefas(metrics);
            html.setMetricasGlobais(metrics.getMetricasGlobais());
            html.setTabela(metrics.makeResourceTable());

            html.saveHtml(this.outputFolder.get(), chartMaker);

            System.out.printf("Results were exported to %s%n", this.outputFolder.get().getName());
        }
    }

    /**
     * Get a model from the xml file containing the configuration of the simulation.
     *
     * @return The model coming from the configuration file
     */
    private Document getModelFromFile () {
        if (this.inputFile.isEmpty()) {
            System.out.println("The "); // FIXME
            System.exit(-1);
        }

        try {
            final var model = IconicoXML.ler(this.inputFile.get());
            System.out.println(ConsoleColors.GREEN + "OK" + ConsoleColors.RESET);
            this.simulationProgress.validarInicioSimulacao(model);
            return model;
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Run a simulation from a model
     *
     * @param model
     *     A model with configurations for a simulation
     *
     * @return The metrics resulted from the simulation
     */
    private Metricas runASimulation (final Document model) {
        final var queueNetwork = this.createQueueNetwork(model);
        final var jobs         = this.createJobsList(model, queueNetwork);

        final var sim = this.selectSimulation(queueNetwork, jobs);
        sim.createRouting();
        sim.simulate();

        return sim.getMetrics();
    }

    /**
     * Select a simulation type based on the <i>parallel</i> field from the class.
     *
     * @param queueNetwork
     *     The queueNetwork created from the model of a simulation
     * @param jobs
     *     The job list
     *
     * @return The chosen simulation
     */
    private Simulation selectSimulation (final RedeDeFilas queueNetwork, final List<Tarefa> jobs) {
        return this.parallel
               ? new SimulacaoParalela(this.simulationProgress, queueNetwork, jobs, this.nThreads)
               : new SimulacaoSequencial(this.simulationProgress, queueNetwork, jobs);
    }

    /**
     * Create a job list from the model and the queue network from it.
     *
     * @param model
     *     The model from the simulation
     * @param queueNetwork
     *     The queue network from the model
     *
     * @return The respective job list from the model
     */
    private List<Tarefa> createJobsList (final Document model, final RedeDeFilas queueNetwork) {
        System.out.print("  Creating tasks: ");
        final var jobs = IconicoXML.newGerarCarga(model).makeTaskList(queueNetwork);
        System.out.println(ConsoleColors.surroundGreen("OK!"));
        return jobs;
    }

    /**
     * Create a queue network from a simulation model.
     *
     * @param model
     *     The model from a simulation
     *
     * @return A queue network from the model
     */
    private RedeDeFilas createQueueNetwork (final Document model) {
        System.out.print("  Mounting network queue: ");
        final var queueNetwork = IconicoXML.newRedeDeFilas(model);
        System.out.println(ConsoleColors.surroundGreen("OK!"));
        return queueNetwork;
    }

    /**
     * Hosts a server for simulating models coming from clients
     */
    private void serverSimulation () {
        while (true) {
            try {
                final var simServer    = new Server(this.serverPort);
                final var newModel     = simServer.getMetricsFromClient();
                final var modelMetrics = this.runASimulation(newModel);
                simServer.returnMetricsToClient(modelMetrics);
            } catch (final UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sends a model for a server to simulate and receives back its metrics, printing the results at
     * the end.
     */
    private void clientSimulation () {
        final var simClient = new Client(this.serverAddress, this.serverPort);
        final var model     = this.getModelFromFile();

        simClient.sendModelToServer(model);
        final var metrics = simClient.receiveMetricsFromServer();

        System.out.println(metrics.getMetricasGlobais());
    }

    /**
     * An enum for run modes for the terminal application.
     */
    private enum Modes {
        HELP("h"),
        VERSION("v"),
        SIMULATE("", true),
        CLIENT("c", true),
        SERVER("s");

        private final String str;

        private final boolean requiresModel;

        Modes (final String s) {
            this.str           = s;
            this.requiresModel = false;
        }

        Modes (final String s, final boolean requiresModel) {
            this.str           = s;
            this.requiresModel = requiresModel;
        }

        private boolean isOptionMode (final CommandLine cmd) {
            return cmd.hasOption(this.str);
        }

        private boolean requiresModel () {
            return this.requiresModel;
        }
    }

    private static class OptionsHolder {

        private static final Options ALL_OPTIONS = makeAllOptions();

        /**
         * Set options to use in the command line for the configuration of the simulation.
         *
         * @return An object for options of the command line from Common Cli.
         */
        private static Options makeAllOptions () {
            return new Options()
                .addOption("h", "help", false, "print this help message.")
                .addOption("v", "version", false, "print the version of iSPD.")
                .addOption("s", "server", false, "run iSPD as a server.")
                .addOption("c", "client", false, "run iSPD as a client.")
                .addOption("P", "port", true, "specify a port.")
                .addOption("t", "threads", true, "specify the number of threads.")
                .addOption("conf", "conf", true, "specify a configuration file.")
                .addOption("in", "input", true, "specify the input file of the model to simulate.")
                .addOption("o", "output", true, "specify an output folder for the html export.")
                .addOption("e", "executions", true, "specify the number of executions.")
                .addOption("a", "address", true, "specify the server address.")
                .addOption("p", "parallel", false, "runs the simulation parallel.");
        }
    }
}
