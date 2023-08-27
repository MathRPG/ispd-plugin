package ispd.arquivo.xml.utils;

import ispd.arquivo.xml.models.builders.*;
import ispd.motor.filas.servidores.implementacao.*;

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
    public static void toMaster (final CS_Switch theSwitch, final CS_Mestre master) {
        master.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(master);
    }

    /**
     * Connect switch to machine.
     */
    public static void toMachine (final CS_Switch theSwitch, final CS_Maquina machine) {
        machine.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(machine);
    }

    /**
     * Connect switch to cloud machine.
     */
    public static void toCloudMachine (final CS_Switch theSwitch, final CS_MaquinaCloud maq) {
        maq.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(maq);
    }

    /**
     * Connect switch to a vmm.
     */
    public static void toVirtualMachineMaster (final CS_Switch theSwitch, final CS_VMM vmm) {
        vmm.addConexoesSaida(theSwitch);
        theSwitch.addConexoesSaida(vmm);
    }
}