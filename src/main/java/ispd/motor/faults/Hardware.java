package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.queues.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class Hardware {

    public void FIHardware1 (
        final ProgressTracker janela,
        final CloudQueueNetwork gridQueueNetwork
    ) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        janela.println("Hardware failure created.");
        janela.print(" -> ");
    }
}