package ispd.arquivo.xml.utils;

import ispd.arquivo.xml.models.builders.*;
import ispd.motor.queues.centers.impl.*;

/**
 * Class with utility methods to interconnect service centers and switches.
 *
 * @see GridQueueNetworkParser
 * @see CloudQueueNetworkParser
 */
public enum SwitchConnection {
    ;

    /**
     * Connect switch to master
     */
    public static void toMaster (final Switch theSwitch, final GridMaster master) {
        master.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(master);
    }

    /**
     * Connect switch to machine.
     */
    public static void toMachine (final Switch theSwitch, final GridMachine machine) {
        machine.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(machine);
    }

    /**
     * Connect switch to cloud machine.
     */
    public static void toCloudMachine (final Switch theSwitch, final CloudMachine maq) {
        maq.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(maq);
    }

    /**
     * Connect switch to a vmm.
     */
    public static void toVirtualMachineMaster (final Switch theSwitch, final CloudMaster vmm) {
        vmm.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(vmm);
    }
}