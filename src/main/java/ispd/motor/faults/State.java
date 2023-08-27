package ispd.motor.faults;

import ispd.motor.simul.*;
import javax.swing.*;

public class State {

    public void FIState1 (final ProgressTracker progressTracker) {
        JOptionPane.showMessageDialog(null, "State transmission failure detected.");
        progressTracker.println("State transmission fault created.");
        progressTracker.println("->");
    }
}
