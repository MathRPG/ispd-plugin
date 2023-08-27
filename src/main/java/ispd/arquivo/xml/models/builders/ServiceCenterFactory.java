package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.*;
import ispd.motor.queues.centers.impl.*;

/**
 * Utility class with static methods to build service centers for simulable models and,
 * exceptionally, virtual machines for cloud models
 */
public enum ServiceCenterFactory {
    ;

    /**
     * @return a master from the given {@link WrappedElement}
     */
    public static GridMaster aMaster (final WrappedElement e) {
        return new GridMaster(
            e.id(),
            e.owner(),
            e.power(),
            e.load(),
            e.master().scheduler(),
            e.energy()
        );
    }

    /**
     * @return a master (with load set to 0) from the given {@link WrappedElement}
     */
    public static GridMaster aMasterWithNoLoadFactor (final WrappedElement e) {
        return new GridMaster(e.id(), e.owner(), e.power(), 0.0, e.scheduler(), e.energy());
    }

    /**
     * @return a machine (with one core) from the given {@link WrappedElement}
     */
    public static GridMachine aMachine (final WrappedElement e) {
        return new GridMachine(e.id(), e.owner(), e.power(), 1, e.load(), e.energy());
    }

    /**
     * @return a switch (with load set to 0) from the given {@link WrappedElement}
     */
    public static Switch aSwitch (final WrappedElement e) {
        return new Switch(e.id(), e.bandwidth(), 0.0, e.latency());
    }

    /**
     * @return a machine with the given number from the given {@link WrappedElement}. Its core count
     * is set to 1 and its load factor, to zero.
     */
    public static GridMachine aMachineWithNumber (final WrappedElement e, final int number) {
        return new GridMachine(e.id(), e.owner(), e.power(), 1, 0.0, number + 1, e.energy());
    }

    /**
     * @return an internet from the given {@link WrappedElement}
     */
    public static Internet anInternet (final WrappedElement e) {
        return new Internet(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    /**
     * @return a link from the given {@link WrappedElement}
     */
    public static Link aLink (final WrappedElement e) {
        return new Link(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    /**
     * @return a cloud machine from the given {@link WrappedElement}
     */
    public static CloudMachine aCloudMachine (final WrappedElement e) {
        final var characteristics = e.characteristics();
        final var processor       = characteristics.processor();
        final var costs           = characteristics.costs();

        return new CloudMachine(
            e.id(),
            e.owner(),
            processor.power(),
            processor.number(),
            e.load(),
            characteristics.memory().size(),
            characteristics.hardDisk().size(),
            costs.costProcessing(),
            costs.costMemory(),
            costs.costDisk()
        );
    }

    /**
     * @return a vmm from the given {@link WrappedElement}
     */
    public static CloudMaster aVirtualMachineMaster (final WrappedElement e) {
        final var characteristics = e.characteristics();

        return new CloudMaster(
            e.id(),
            e.owner(),
            characteristics.processor().power(),
            characteristics.memory().size(),
            characteristics.hardDisk().size(),
            e.load(),
            e.master().scheduler(),
            e.master().vmAlloc()
        );
    }

    /**
     * @return a vmm (with load set to 0) from the given {@link WrappedElement}
     */
    public static CloudMaster aVmmNoLoad (final WrappedElement e) {
        final var characteristics = e.characteristics();
        return new CloudMaster(
            e.id(),
            e.owner(),
            characteristics.processor().power(),
            characteristics.memory().size(),
            characteristics.hardDisk().size(),
            0.0,
            e.scheduler(),
            e.vmAlloc()
        );
    }

    /**
     * @return a cloud machine with the given id from the given {@link WrappedElement}. Its load is
     * set to 0.
     */
    public static CloudMachine aCloudMachineWithId (final WrappedElement e, final int j) {
        final var characteristics = e.characteristics();
        final var costs           = characteristics.costs();

        return new CloudMachine(
            "%s.%d".formatted(e.id(), j),
            e.owner(),
            characteristics.processor().power(),
            characteristics.processor().number(),
            characteristics.memory().size(),
            characteristics.hardDisk().size(),
            costs.costProcessing(),
            costs.costMemory(),
            costs.costDisk(),
            0.0,
            j + 1
        );
    }

    /**
     * @return a virtual machine from the given {@link WrappedElement}. Its power is treated as an
     * integral value.
     */
    public static VirtualMachine aVirtualMachine (final WrappedElement e) {
        return new VirtualMachine(
            e.id(),
            e.owner(),
            (int) e.power(),
            e.memAlloc(),
            e.diskAlloc()
        );
    }

    /**
     * @return a virtual machine from the given {@link WrappedElement}. Its power is treated as an
     * integral value and it fetches its master's id.
     */
    public static ispd.gui.drawing.VirtualMachine aVirtualMachineWithVmm (final WrappedElement e) {
        return new ispd.gui.drawing.VirtualMachine(
            e.id(),
            e.owner(),
            e.vmm(),
            (int) e.power(),
            e.memAlloc(),
            e.diskAlloc(),
            e.opSystem()
        );
    }
}
