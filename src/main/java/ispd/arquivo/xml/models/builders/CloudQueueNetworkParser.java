package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.motor.filas.servidores.implementacao.*;
import ispd.policy.scheduling.cloud.*;
import java.util.*;

/**
 * Class to build a cloud queue network from a model in a {@link WrappedDocument}. The usage is the
 * same as in {@link GridQueueNetworkParser}.
 *
 * @see ispd.arquivo.xml.IconicoXML
 * @see GridQueueNetworkParser
 */
public class CloudQueueNetworkParser extends GridQueueNetworkParser {

    /**
     * Overridden from superclass to support {@link CS_MaquinaCloud}s.
     */
    private final Map<CentroServico, List<CS_MaquinaCloud>> clusterSlaves = new HashMap<>();

    private final List<CS_MaquinaCloud> cloudMachines = new ArrayList<>();

    private final List<CS_VirtualMac> virtualMachines = new ArrayList<>();

    private final List<CS_Processamento> virtualMachineMasters = new ArrayList<>();

    /**
     * Process the represented cluster in {@link WrappedElement} very similarly to the superclass,
     * but adapted to take into account cloud machines and virtual machines.
     *
     * @param e
     *     {@link WrappedElement} representing a cluster.
     */
    @Override
    protected void processClusterElement (final WrappedElement e) {
        if (e.isMaster()) {
            final var clust = ServiceCenterFactory.aVmmNoLoad(e);

            this.virtualMachineMasters.add(clust);
            this.serviceCenters.put(e.globalIconId(), clust);

            final int slaveCount = e.nodes();

            final double power = clust.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(clust.getProprietario(), power);

            final var theSwitch = ServiceCenterFactory.aSwitch(e);

            this.links.add(theSwitch);

            SwitchConnection.toVirtualMachineMaster(theSwitch, clust);

            for (int j = 0; j < slaveCount; j++) {
                final var machine = ServiceCenterFactory.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(theSwitch, machine);

                machine.addMestre(clust);
                clust.addEscravo(machine);

                this.cloudMachines.add(machine);
            }
        } else {
            final var theSwitch = ServiceCenterFactory.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(e.globalIconId(), theSwitch);

            this.increaseUserPower(e.owner(), e.power() * e.nodes());

            final int slaveCount = e.nodes();

            final List<CS_MaquinaCloud> slaves = new ArrayList<>(slaveCount);

            for (int j = 0; j < slaveCount; j++) {
                final var machine = ServiceCenterFactory.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(theSwitch, machine);
                slaves.add(machine);
            }

            this.cloudMachines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    /**
     * Parse the required {@link CentroServico}s and {@link CS_VirtualMac}s from the given
     * {@link WrappedDocument}.
     *
     * @param doc
     *     The {@link WrappedDocument} to be processed. Must contain a valid <b>cloud</b> model.
     *
     * @return The called instance itself, so the call can be chained into a {@link #build()} if so
     * desired.
     *
     * @throws IllegalStateException
     *     If this instance was already used to parse a {@link WrappedDocument}.
     */
    @Override
    public GridQueueNetworkParser parseDocument (final WrappedDocument doc) {
        super.parseDocument(doc);
        doc.virtualMachines().forEach(this::processVirtualMachineElement);
        return this;
    }

    /**
     * Build and process the cloud machine represented by the {@link WrappedElement} {@code e}.
     * Since the machine may or may not be a master, it can be added to either the collection of
     * {@link #virtualMachineMasters} or {@link #cloudMachines}.
     *
     * @param e
     *     {@link WrappedElement} representing a {@link CS_Processamento}.
     *
     * @return The interpreted {@link CS_Processamento} from the given {@link WrappedElement}. May
     * either be a {@link CS_VMM} or a {@link CS_MaquinaCloud}.
     */
    @Override
    protected CS_Processamento makeAndAddMachine (final WrappedElement e) {
        final CS_Processamento machine;

        if (e.hasMasterAttribute()) {
            machine = ServiceCenterFactory.aVirtualMachineMaster(e);
            this.virtualMachineMasters.add(machine);
        } else {
            machine = ServiceCenterFactory.aCloudMachine(e);
            this.cloudMachines.add((CS_MaquinaCloud) machine);
        }

        return machine;
    }

    /**
     * Differences from the overridden method:
     * <ul>
     *     <li>Always interprets {@code master} as an instance of
     *     {@link CS_VMM}</li>
     *     <li>{@code slave} may be a {@link CS_MaquinaCloud} instead of a
     *     {@link CS_Maquina}</li>
     * </ul>
     *
     * @param master
     *     the <b>master</b> {@link CS_Processamento}.
     * @param slave
     *     <b>slave</b> {@link CentroServico}.
     */
    @Override
    protected void addSlavesToProcessingCenter (
        final CS_Processamento master,
        final CentroServico slave
    ) {
        final var theMaster = (CS_VMM) master;
        if (slave instanceof CS_Processamento proc) {
            theMaster.addEscravo(proc);
            if (slave instanceof CS_MaquinaCloud machine) {
                machine.addMestre(theMaster);
            }
        } else if (slave instanceof CS_Switch) {
            for (final var clusterSlave : this.clusterSlaves.get(slave)) {
                clusterSlave.addMestre(theMaster);
                theMaster.addEscravo(clusterSlave);
            }
        }
    }

    /**
     * Differently from the overridden method, iterates over {@link CS_VMM}s and updates
     * {@link CloudSchedulingPolicy}.
     *
     * @param helper
     *     {@link UserPowerLimit} with the power limit information.
     */
    @Override
    protected void setSchedulersUserMetrics (final UserPowerLimit helper) {
        this.virtualMachineMasters.stream()
            .map(CS_VMM.class::cast)
            .map(CS_VMM::getEscalonador)
            .forEach(helper::setSchedulerUserMetrics);
    }

    /**
     * Constructs a {@link RedeDeFilasCloud}. <b>It does not take power limit information into
     * account.</b>
     *
     * @return initialized {@link RedeDeFilasCloud}.
     */
    @Override
    protected RedeDeFilas initQueueNetwork () {
        return new RedeDeFilasCloud(
            this.virtualMachineMasters,
            this.cloudMachines, this.virtualMachines,
            this.links, this.internets
        );
    }

    private void processVirtualMachineElement (final WrappedElement e) {
        final var virtualMachine = ServiceCenterFactory.aVirtualMachine(e);

        final var masterId = e.vmm();

        this.virtualMachineMasters.stream()
            .filter(cs -> cs.getId().equals(masterId))
            .map(CS_VMM.class::cast)
            .forEach(master -> {
                virtualMachine.addVMM(master);
                master.addVM(virtualMachine);
            });

        this.virtualMachines.add(virtualMachine);
    }
}
