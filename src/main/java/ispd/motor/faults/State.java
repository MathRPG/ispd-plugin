package ispd.motor.faults;

import ispd.motor.queues.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class State {

    public void FIState1 (final ProgressTracker janela, final CloudQueueNetwork redeDeFilas) {
        JOptionPane.showMessageDialog(null, "State transmission failure detected.");
        janela.println("State transmission fault created.");
        janela.println("->");
    }
}
