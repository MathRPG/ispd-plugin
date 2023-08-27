package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.*;
import ispd.arquivo.xml.utils.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.GridMaster;
import ispd.motor.queues.centers.impl.*;
import ispd.policy.scheduling.grid.*;
import java.util.*;

/**
 * Class to build a queue network from a model in a {@link WrappedDocument}.
 * Usage should be as follows: <pre>{@code
 * new GridQueueNetworkParser()
 * .parseDocument(doc)
 * .build();
 * }</pre>
 * See methods {@link #parseDocument(WrappedDocument)} and {@link #build()} for further details.
 *
 * @see IconicModelDocumentBuilder
 */
public class GridQueueNetworkParser {

    /**
     * Map or {@link Service}s parsed from the document, indexed by id.
     */
    protected final Map<Integer, Service> serviceCenters = new HashMap<>();

    /**
     * Map of {@link Internet}s parsed from the document.
     */
    protected final List<Internet> internets = new ArrayList<>();

    /**
     * Map of {@link Link}s parsed from the document.
     */
    protected final List<Communication> links = new ArrayList<>();

    private final Map<String, Double> powerLimits = new HashMap<>();

    private final List<GridMachine> machines = new ArrayList<>();

    private final List<Processing> masters = new ArrayList<>();

    private final Map<Service, List<GridMachine>> clusterSlaves = new HashMap<>();

    /**
     * Whether this instance has already parsed a document successfully. Each instance should be
     * responsible for parsing <b>only one</b> document.
     */
    private boolean hasParsedADocument = false;

    private static void connectLinkAndVertices (
        final Link link, final Vertex origination, final Vertex destination
    ) {
        link.setConexoesSaida((Service) destination);
        origination.addConexoesSaida(link);
        destination.addConexoesEntrada(link);
    }

    /**
     * Process a {@link WrappedElement} that is representing a cluster of {@link Service}s.
     * The {@link GridMaster}, {@link GridMachine}s and {@link Link}s in the cluster are
     * differentiated and all processed individually.
     *
     * @param e
     *     {@link WrappedElement} representing a cluster.
     */
    protected void processClusterElement (final WrappedElement e) {
        if (e.isMaster()) {
            final var cluster = ServiceCenterFactory.aMasterWithNoLoadFactor(e);

            this.masters.add(cluster);
            this.serviceCenters.put(e.globalIconId(), cluster);

            final int slaveCount = e.nodes();

            final double power = cluster.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(cluster.getProprietario(), power);

            final var theSwitch = ServiceCenterFactory.aSwitch(e);

            this.links.add(theSwitch);

            SwitchConnection.toMaster(theSwitch, cluster);

            for (int i = 0; i < slaveCount; i++) {
                final var machine = ServiceCenterFactory.aMachineWithNumber(e, i);
                SwitchConnection.toMachine(theSwitch, machine);

                machine.addMestre(cluster);
                cluster.addEscravo(machine);

                this.machines.add(machine);
            }
        } else {
            final var theSwitch = ServiceCenterFactory.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(e.globalIconId(), theSwitch);

            this.increaseUserPower(e.owner(), e.power() * e.nodes());

            final int slaveCount = e.nodes();

            final var slaves = new ArrayList<GridMachine>(slaveCount);

            for (int i = 0; i < slaveCount; i++) {
                final var machine = ServiceCenterFactory.aMachineWithNumber(e, i);
                SwitchConnection.toMachine(theSwitch, machine);
                slaves.add(machine);
            }

            this.machines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    /**
     * Parse the required {@link Service}s and user power limits from the given
     * {@link WrappedDocument}.
     *
     * @param doc
     *     the {@link WrappedDocument} to be processed. Must contain a valid model.
     *
     * @return the called instance itself, so the call can be chained into a {@link #build()} if so
     * desired.
     *
     * @throws IllegalStateException
     *     if this instance was already used to parse a {@link WrappedDocument}.
     */
    public GridQueueNetworkParser parseDocument (final WrappedDocument doc) {
        if (this.hasParsedADocument) {
            throw new IllegalStateException(".parseDocument(doc) method called twice.");
        }

        doc.owners().forEach(o -> this.powerLimits.put(o.id(), 0.0));
        doc.machines().forEach(this::processMachineElement);
        doc.clusters().forEach(this::processClusterElement);
        doc.internets().forEach(this::processInternetElement);
        doc.links().forEach(this::processLinkElement);
        doc.masters().forEach(this::addSlavesToMachine);

        this.hasParsedADocument = true;

        return this;
    }

    private void processMachineElement (final WrappedElement e) {
        final var machine = this.makeAndAddMachine(e);

        this.serviceCenters.put(e.globalIconId(), machine);

        this.increaseUserPower(machine.getProprietario(), machine.getPoderComputacional());
    }

    private void addSlavesToMachine (final WrappedElement e) {
        final var master = (Processing) this.serviceCenters.get(e.globalIconId());

        e.master().slaves()
            .map(WrappedElement::id)
            .map(Integer::parseInt)
            .map(this.serviceCenters::get)
            .forEach(sc -> this.addSlavesToProcessingCenter(master, sc));
    }

    /**
     * Build and process the machine (more specifically, the {@link Processing} represented by
     * the {@link WrappedElement} {@code e}. Since the machine may or may not be a master, it can be
     * added to either the collection of {@link #masters} or {@link #machines}.
     *
     * @param e
     *     {@link WrappedElement} representing a {@link Processing}.
     *
     * @return the interpreted {@link Processing} from the given {@link WrappedElement}. May
     * either be a {@link GridMaster} or a {@link GridMachine}.
     */
    protected Processing makeAndAddMachine (final WrappedElement e) {
        final Processing machine;

        if (e.hasMasterAttribute()) {
            machine = ServiceCenterFactory.aMaster(e);
            this.masters.add(machine);
        } else {
            machine = ServiceCenterFactory.aMachine(e);
            this.machines.add((GridMachine) machine);
        }

        return machine;
    }

    /**
     * Increase the power limit of the user with given id by the given amount.
     *
     * @param userId
     *     id of the user whose power limit will be increased.
     * @param increment
     *     amount to increment the power limit by. Should be
     *     <b>positive</b>.
     */
    protected void increaseUserPower (final String userId, final double increment) {
        final var oldValue = this.powerLimits.get(userId);
        this.powerLimits.put(userId, oldValue + increment);
    }

    /**
     * Add {@link Service} {@code slave} to the list of slaves of the {@link Processing}
     * {@code master} (which is always interpreted as an instance of {@link GridMaster}. Note that
     * {@code master} <b>is a master</b>, and {@link Processing} may either be:
     * <ul>
     *      <li>another master</li>
     *      <li>a non-master machine</li>
     *      <li>a switch</li>
     * </ul>
     * In any case, the method process the element appropriately and updates
     * the necessary master-slave relations.
     *
     * @param master
     *     an instance of {@link GridMaster}.
     * @param slave
     *     <b>slave</b> {@link Service}.
     *
     * @throws ClassCastException
     *     if the given {@link Processing} {@code master} is not an instance of
     *     {@link GridMaster}.
     * @apiNote the parameter {@code master} is typed as a {@link Processing} to support
     * overrides that deal with other types of masters. See
     * {@link CloudQueueNetworkParser#addSlavesToProcessingCenter} for an example.
     */
    protected void addSlavesToProcessingCenter (
        final Processing master,
        final Service slave
    ) {
        final var theMaster = (GridMaster) master;
        if (slave instanceof final Processing proc) {
            theMaster.addEscravo(proc);
            if (slave instanceof final GridMachine machine) {
                machine.addMestre(theMaster);
            }
        } else if (slave instanceof Switch) {
            for (final var clusterSlave : this.clusterSlaves.get(slave)) {
                clusterSlave.addMestre(theMaster);
                theMaster.addEscravo(clusterSlave);
            }
        }
    }

    /**
     * Create a {@link GridQueueNetwork} with the collections parsed from the document. The method
     * {@link #parseDocument(WrappedDocument)} must already have been called on the instance.
     *
     * @return {@link GridQueueNetwork} with the appropriate service centers, links, and user
     * configurations found in the document.
     */
    public GridQueueNetwork build () {
        this.throwIfNoDocumentWasParsed();

        final var helper = new UserPowerLimit(this.powerLimits);
        this.setSchedulersUserMetrics(helper);

        final var queueNetwork = this.initQueueNetwork();
        queueNetwork.setUsuarios(helper.getOwners());
        return queueNetwork;
    }

    private void throwIfNoDocumentWasParsed () {
        if (!this.hasParsedADocument) {
            throw new IllegalStateException(
                ".build() method called without a document parsed.");
        }
    }

    /**
     * For all {@link GridMaster}s parsed from the document, update its
     * {@link GridSchedulingPolicy}'s user metrics with the obtained user power limit information.
     *
     * @param helper
     *     {@link UserPowerLimit} with the power limit information.
     */
    protected void setSchedulersUserMetrics (final UserPowerLimit helper) {
        this.masters.stream()
            .map(GridMaster.class::cast)
            .map(GridMaster::getEscalonador)
            .forEach(helper::setSchedulerUserMetrics);
    }

    /**
     * Construct a {@link GridQueueNetwork} with the parsed {@link Service}s and user power limit
     * information.
     *
     * @return initialized {@link GridQueueNetwork}.
     */
    protected GridQueueNetwork initQueueNetwork () {
        return new GridQueueNetwork(
            this.masters,
            this.machines,
            this.links,
            this.internets,
            this.powerLimits
        );
    }

    private void processInternetElement (final WrappedElement e) {
        final var net = ServiceCenterFactory.anInternet(e);

        this.internets.add(net);
        this.serviceCenters.put(e.globalIconId(), net);
    }

    private void processLinkElement (final WrappedElement e) {
        final var link = ServiceCenterFactory.aLink(e);

        connectLinkAndVertices(
            link,
            this.getVertex(e.origination()),
            this.getVertex(e.destination())
        );

        this.links.add(link);
    }

    private Vertex getVertex (final int e) {
        return (Vertex) this.serviceCenters.get(e);
    }
}