package ispd.motor.simul;

import ispd.motor.Event;
import ispd.motor.*;
import ispd.motor.metrics.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.queues.task.*;
import ispd.policy.*;
import java.awt.*;
import java.util.List;

public abstract class Simulation {

    private final List<GridTask> jobs;

    private final ProgressTracker window;

    private GridQueueNetwork queueNetwork = null;

    private CloudQueueNetwork cloudQueueNetwork = null;

    protected Simulation (
        final ProgressTracker window,
        final GridQueueNetwork queueNetwork,
        final List<GridTask> jobs
    ) {
        this.jobs         = jobs;
        this.queueNetwork = queueNetwork;
        this.window       = window;
    }

    protected Simulation (
        final ProgressTracker window,
        final CloudQueueNetwork cloudQueueNetwork,
        final List<GridTask> jobs
    ) {
        this.jobs              = jobs;
        this.cloudQueueNetwork = cloudQueueNetwork;
        this.window            = window;
    }

    public abstract void simulate ();

    public abstract void addFutureEvent (Event ev);

    public abstract boolean removeFutureEvent (
        EventType eventType,
        Service eventServer,
        Client eventClient
    );

    public abstract double getTime (Object origin);

    public ProgressTracker getWindow () {
        return this.window;
    }

    protected CloudQueueNetwork getCloudQueueNetwork () {
        return this.cloudQueueNetwork;
    }

    protected GridQueueNetwork getQueueNetwork () {
        return this.queueNetwork;
    }

    protected List<GridTask> getJobs () {
        return this.jobs;
    }

    public void addJob (final GridTask job) {
        this.jobs.add(job);
    }

    protected void initSchedulers () {
        for (final var master : this.queueNetwork.getMestres()) {
            ((GridMaster) master).getEscalonador().iniciar();
        }
    }

    protected void initCloudAllocators () {
        for (final var genericMaster : this.cloudQueueNetwork.getMestres()) {
            final var master = (CloudMaster) genericMaster;
            master.getAlocadorVM().iniciar();
        }
    }

    protected void initCloudSchedulers () {
        for (final var genericMaster : this.cloudQueueNetwork.getMestres()) {
            final var master = (CloudMaster) genericMaster;
            System.out.printf(
                "VMM %s iniciando escalonador %s%n",
                genericMaster.id(),
                master.getEscalonador().toString()
            );
            master.getEscalonador().iniciar();
            master.instanciarCaminhosVMs();
        }
    }

    public void createRouting () {
        for (final var master : this.queueNetwork.getMestres()) {
            final var temp = (Simulable) master;

            // Give access to the master of the queue of future events.
            temp.setSimulation(this);

            // Find the shortest path between the master and its slaves.
            master.determinarCaminhos();
        }
        if (this.queueNetwork.getMaquinas() == null || this.queueNetwork.getMaquinas().isEmpty()) {
            this.window.println("The model has no processing slaves.", Color.orange);
        } else {
            // Find the shortest path between each slave and the master.
            for (final var machine : this.queueNetwork.getMaquinas()) {
                machine.determinarCaminhos();
            }
        }
    }

    public General getMetrics () {
        final var metric = new General(this.queueNetwork, this.getTime(null), this.jobs);

        this.window.print("Getting Results.");
        this.window.print(" -> ");

        this.window.incProgresso(5);

        this.window.println("OK", Color.green);
        this.window.print("Falha injetada");
        this.window.println("OK", Color.red);

        return metric;
    }

    public General getCloudMetrics () {
        this.window.print("Getting Results.");
        this.window.print(" -> ");

        final var metric = new General(this.cloudQueueNetwork, this.getTime(null), this.jobs);

        this.window.incProgresso(5);
        this.window.println("OK", Color.green);

        return metric;
    }
}
