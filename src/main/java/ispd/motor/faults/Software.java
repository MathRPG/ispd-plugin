package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.queues.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class Software {

    public void FISfotware1 (final ProgressTracker janela, final CloudQueueNetwork redeDeFilas) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Falha de Omissão de software selecionada.");

        janela.println("Software failure created.");

        if (redeDeFilas.getVMs() == null) {
            return;
        }

        final int NovoRedeDeFilas = redeDeFilas.getMaquinasCloud().size() - 1;

        for (int i = 0; i <= NovoRedeDeFilas; i++) {
        }
    }
}