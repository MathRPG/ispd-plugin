package ispd.motor.faults;

import ispd.motor.queues.*;
import ispd.motor.simul.*;
import javax.swing.*;

public class State {

    public void FIState1 (final ProgressTracker janela, final CloudQueueNetwork redeDeFilas) {
        // Mensagens com a inserção de falha
        JOptionPane.showMessageDialog(null, "State transmission failure detected.");
        janela.println("State transmission fault created.");
        janela.println("->");

        // Criação de filas vazias para armazenamento das máquinas antes da falha

        // Processo de falha e tratamento
        if (redeDeFilas.getVMs() == null) {
        } else if (redeDeFilas.getVMs() != null) {
            // Criação de números aleatórios para impedir o estado de transmissão
            //Máquinas da nuvem

            // Máquina desligada para impedir que a transmissão seja feita

            // Nova máquina para ser replicada

            // Número de VMs na simulação
            final double machineNumber = redeDeFilas.getMaquinasCloud().size();

            // Método de recuperação - Replicação
            for (int i = 0; i < machineNumber; i++) {
            }
        }
    }
}
