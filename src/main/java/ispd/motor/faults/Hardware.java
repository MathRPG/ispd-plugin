package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.queues.*;
import ispd.motor.simul.*;
import java.util.*;
import javax.swing.*;

public class Hardware {

    private static void selectFaults (final ProgressTracker janela) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        janela.println("Hardware failure created.");
        janela.print(" -> ");
    }

    public void FIHardware1 (
        final ProgressTracker janela,
        final CloudQueueNetwork gridQueueNetwork
    ) {
        selectFaults(janela);

        final var vms = gridQueueNetwork.getVMs();


        if (vms == null) {
            return;
        }

        final var machines = gridQueueNetwork.getMaquinasCloud();
        final int id       = new Random().nextInt(machines.size());
    }
}