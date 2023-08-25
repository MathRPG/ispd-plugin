package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.*;
import ispd.gui.iconico.*;

/**
 * Utility class with static methods to build grid items for the gui, from {@link WrappedElement}s
 *
 * @see IconicModelBuilder
 * @see WrappedElement
 * @see GridItem
 */
public enum IconBuilder {
    ;

    /**
     * @return link icon from given element, with given origination and destination
     */
    public static Link aLink (
        final WrappedElement e,
        final Vertex origination,
        final Vertex destination
    ) {
        final var link = new Link(origination, destination, e.iconId().local(), e.globalIconId());

        link.setSelected(false);

        link.getId().setName(e.id());
        link.setBandwidth(e.bandwidth());
        link.setLoadFactor(e.load());
        link.setLatency(e.latency());

        return link;
    }

    /**
     * @return cluster icon from given element
     */
    public static Cluster aCluster (final WrappedElement e) {
        final var info = IconInfo.fromElement(e);

        final var cluster =
            new Cluster(info.x(), info.y(), info.localId(), info.globalId(), e.power());

        cluster.getId().setName(e.id());
        cluster.setComputationalPower(e.power());
        setProcessingCenterCharacteristics(cluster, e);
        cluster.setSlaveCount(e.nodes());
        cluster.setBandwidth(e.bandwidth());
        cluster.setLatency(e.latency());
        cluster.setSchedulingAlgorithm(e.scheduler());
        cluster.setVmmAllocationPolicy(e.vmAlloc());
        cluster.setOwner(e.owner());
        cluster.setMaster(e.isMaster());

        return cluster;
    }

    private static void setProcessingCenterCharacteristics (
        final GridItem item,
        final WrappedElement e
    ) {
        if (!e.hasCharacteristicAttribute()) {
            return;
        }

        final var characteristic = e.characteristics();

        if (item instanceof final Cluster cluster) {
            cluster.setComputationalPower(characteristic.processor().power());
            cluster.setCoreCount(characteristic.processor().number());
            cluster.setRam(characteristic.memory().size());
            cluster.setHardDisk(characteristic.hardDisk().size());

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.costs();

            cluster.setCostPerProcessing(co.costProcessing());
            cluster.setCostPerMemory(co.costMemory());
            cluster.setCostPerDisk(co.costDisk());
        } else if (item instanceof final Machine machine) {
            machine.setComputationalPower(characteristic.processor().power());
            machine.setCoreCount(characteristic.processor().number());
            machine.setRam(characteristic.memory().size());
            machine.setHardDisk(characteristic.hardDisk().size());

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.costs();

            machine.setCostPerProcessing(co.costProcessing());
            machine.setCostPerMemory(co.costMemory());
            machine.setCostPerDisk(co.costDisk());
        }
    }

    /**
     * @return internet icon from given element
     */
    public static Internet anInternet (final WrappedElement e) {
        final var info = IconInfo.fromElement(e);

        final Internet net = new Internet(info.x(), info.y(), info.localId(), info.globalId());

        net.getId().setName(e.id());

        net.setBandwidth(e.bandwidth());
        net.setLoadFactor(e.load());
        net.setLatency(e.latency());
        return net;
    }

    /**
     * @return machine icon from given element
     */
    public static Machine aMachine (final WrappedElement m) {
        final var info = IconInfo.fromElement(m);

        final var machine =
            new Machine(info.x(), info.y(), info.localId(), info.globalId(), m.energy());

        machine.getId().setName(m.id());
        machine.setComputationalPower(m.power());
        setProcessingCenterCharacteristics(machine, m);
        machine.setLoadFactor(m.load());
        machine.setOwner(m.owner());
        return machine;
    }

    /**
     * Simple record to contain information in common between all icon objects
     */
    private record IconInfo(int x, int y, int globalId, int localId) {

        private static IconInfo fromElement (final WrappedElement e) {
            final var position = e.position();
            final var iconId   = e.iconId();

            return new IconInfo(position.x(), position.y(), iconId.global(), iconId.local());
        }
    }
}