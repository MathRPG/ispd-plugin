package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class Software {

    public void FISfotware1 (final ProgressTracker janela) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Falha de Omissão de software selecionada.");
        janela.println("Software failure created.");
    }
}