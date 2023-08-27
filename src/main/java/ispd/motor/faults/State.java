package ispd.motor.faults;

import ispd.motor.simul.*;
import javax.swing.*;

public enum State {
    ;

    public static void showMessage (final ProgressTracker progressTracker) {
        JOptionPane.showMessageDialog(null, "State transmission failure detected.");
        progressTracker.println("State transmission fault created.");
        progressTracker.println("->");
    }
}
