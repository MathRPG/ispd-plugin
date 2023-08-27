package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class Hardware {

    public static void showMessage (final ProgressTracker progressTracker) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        progressTracker.println("Hardware failure created.");
        progressTracker.print(" -> ");
    }
}