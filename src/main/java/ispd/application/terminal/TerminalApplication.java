package ispd.application.terminal;

import ispd.application.*;
import ispd.arquivo.*;
import ispd.arquivo.xml.*;
import ispd.gui.results.*;
import ispd.motor.metrics.*;
import ispd.motor.queues.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import ispd.motor.simul.impl.*;
import ispd.utils.constants.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;

/**
 * A class for setting up the terminal part of iSPD and run the simulations.
 */
public class TerminalApplication implements Application {

    private static final int DEFAULT_PORT = 2004;

    private final Optional<File> inputFile;

    private final Optional<File> outputFolder;

    private final Mode mode;

    private final int nThreads;

    private final boolean parallel;

    private final int serverPort;

    private final Inet4Address serverAddress;

    private final int nExecutions;

    private final ProgressTracker simulationProgress = new ProgressTracker();

    private final @NotNull SystemTimeProvider systemTimeProvider;

    /**
     * Pre run of the terminal application, adding the necessary flags to the class before it runs.
     *
     * @param options
     *     Options from the command line.
     */
    public TerminalApplication (final String[] options) {
        this(options, new DefaultSystemTimeProvider());
    }

    /**
     * Pre run of the terminal application, adding the necessary flags to the class before it runs.
     *
     * @param options
     *     Options from the command line.
     * @param systemTimeProvider
     *     Provider for the system time. Used when presenting simulation results. Must not be
     *     {@code null}.
     */
    public TerminalApplication (
        final String[] options,
        final @NotNull SystemTimeProvider systemTimeProvider
    ) {
        final var cmd = this.commandLinePreparation(OptionsHolder.ALL_OPTIONS, options);

        this.mode          = getActiveMode(cmd);
        this.serverPort    = getIntOptionOr(cmd, "P", DEFAULT_PORT);
        this.nExecutions   = getIntOptionOr(cmd, "e", 1);
        this.nThreads      = this.setNThreads(cmd);
        this.inputFile     = setInputFile(cmd);
        this.outputFolder  = setOutputFolder(cmd);
        this.parallel      = isParallelSimulation(cmd);
        this.serverAddress = setServerAddress(cmd);

        if (this.mode.requiresModel() && this.inputFile.isEmpty()) {
            final var message = "It needs a model to simulate.";
            System.out.println(message);
            throw new IllegalArgumentException(message);
        }

        this.systemTimeProvider = Objects.requireNonNull(systemTimeProvider);
    }

    /**
     * Set the mode for running the terminal application.
     *
     * @param cmd
     *     CommandLine used in the application.
     *
     * @return An int representing a mode based on the options.
     */
    private static Mode getActiveMode (final CommandLine cmd) {
        return Arrays.stream(Mode.values())
            .filter(v -> v.isActive(cmd))
            .findFirst()
            .orElse(Mode.SIMULATE);
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
     * Get the name of the model file from the command line.
     *
     * @param cmd
     *     The command line class from Common Cli.
     *
     * @return A configuration file with information for the simulation
     */
    private static Optional<File> setInputFile (final CommandLine cmd) {
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
    private static Optional<File> setOutputFolder (final CommandLine cmd) {
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
    private static boolean isParallelSimulation (final CommandLine cmd) {
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
    private static Inet4Address setServerAddress (final CommandLine cmd) {
        try {
            final var hostName = cmd.getOptionValue("a", StringConstants.LOCALHOST);
            return (Inet4Address) InetAddress.getByName(hostName);
        } catch (final UnknownHostException e) {
            System.out.printf(
                "Error at getting the server address from command line. (%s)%n",
                e.getMessage()
            );
            throw new IllegalArgumentException(e);
        }
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
    private static List<GridTask> createJobsList (
        final Document model,
        final GridQueueNetwork queueNetwork
    ) {
        System.out.print("  Creating tasks: ");
        final var jobs =
            WorkloadGeneratorFactory
                .fromDocument(model)
                .makeTaskList(queueNetwork);
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
    private static GridQueueNetwork createQueueNetwork (final Document model) {
        System.out.print("  Mounting network queue: ");
        final var queueNetwork = GridQueueNetworkFactory.fromDocument(model);
        System.out.println(ConsoleColors.surroundGreen("OK!"));
        return queueNetwork;
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
        if (this.inputFile.isEmpty()) {
            return;
        }

        final var file = this.inputFile.get();

        if (file.getName().endsWith(FileExtensions.ICONIC_MODEL) && file.exists()) {
            this.runNSimulations();
        } else {
            System.out.printf("iSPD can not open the file: %s%n", file.getName());
        }
    }

    /**
     * A simple method for creating the Common Cli's command line class.
     *
     * @param options
     *     The class Common Cli's Options class for the command line options.
     * @param cmdOptions
     *     The options received from the command line.
     *
     * @return The command line class with the chosen options.
     */
    private CommandLine commandLinePreparation (final Options options, final String[] cmdOptions) {
        try {
            return (new DefaultParser()).parse(options, cmdOptions);
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

        final var threads = setIntValueFromOption(cmd, "t");

        if (this.nExecutions < 1) {
            System.out.printf("Number of executions is invalid (%d)%n", this.nExecutions);
            throw new IllegalArgumentException("Invalid Number of Executions.");
        }

        return Math.min(threads, this.nExecutions);
    }

    /**
     * Run a number of simulations and calculate the time for each one, printing the results of them
     * at the end.
     */
    private void runNSimulations () {
        System.out.println("Simulation Initiated.");
        System.out.print("Opening iconic model. ->");

        final var model = this.getModelFromFile();

        if (model == null) {
            return;
        }

        final var metrics = new General(IconicModelFactory.userListFromDocument(model));
        var       totalDuration = 0.0;
        for (var i = 1; i <= this.nExecutions; i++) {
            System.out.printf("* Simulation %d%n", i);

            final var preSimInstant    = this.systemTimeProvider.getSystemTime();
            final var simMetric        = this.runASimulation(model);
            final var postSimInstant   = this.systemTimeProvider.getSystemTime();
            final var totalSimDuration = (double) (postSimInstant - preSimInstant) / 1000.0;

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
    private void printSimulationResults (final General metrics, final double totalDuration) {
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
            final var model = ManipuladorXML.readModelFromFile(this.inputFile.get());
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
    private General runASimulation (final Document model) {
        final var queueNetwork = createQueueNetwork(model);
        final var jobs         = createJobsList(model, queueNetwork);

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
    private Simulation selectSimulation (
        final GridQueueNetwork queueNetwork,
        final List<GridTask> jobs
    ) {
        return this.parallel
               ? new Parallel(this.simulationProgress, queueNetwork, jobs, this.nThreads)
               : new GridSequential(this.simulationProgress, queueNetwork, jobs);
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
    private enum Mode {
        HELP("h"),
        VERSION("v"),
        SIMULATE("", true),
        CLIENT("c", true),
        SERVER("s");

        private final String str;

        private final boolean requiresModel;

        Mode (final String s) {
            this(s, false);
        }

        Mode (final String s, final boolean requiresModel) {
            this.str           = s;
            this.requiresModel = requiresModel;
        }

        private boolean isActive (final CommandLine cmd) {
            return cmd.hasOption(this.str);
        }

        private boolean requiresModel () {
            return this.requiresModel;
        }
    }

    private static class OptionsHolder {

        private static final Option HELP =
            Option.builder("h").longOpt("help").desc("print this help message.").build();

        private static final Option VERSION =
            Option.builder("v").longOpt("version").desc("print the version of iSPD.").build();

        private static final Options ALL_OPTIONS = makeAllOptions();

        /**
         * Set options to use in the command line for the configuration of the simulation.
         *
         * @return An object for options of the command line from Common Cli.
         */
        private static Options makeAllOptions () {
            return new Options()
                .addOption(HELP)
                .addOption(VERSION)
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

    private static class DefaultSystemTimeProvider implements SystemTimeProvider {

    }
}
