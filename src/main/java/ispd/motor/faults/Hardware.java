package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class Hardware {

    public static void showMessage (final ProgressTracker janela) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        janela.println("Hardware failure created.");
        janela.print(" -> ");
    }
}