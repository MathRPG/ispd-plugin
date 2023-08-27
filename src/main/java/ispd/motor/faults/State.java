package ispd.motor.faults;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.simul.*;
import java.util.*;
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
            System.out.println("---------------------------------------");
            System.out.println("Rede de filas é nula.");
        } else if (redeDeFilas.getVMs() != null) {
            // Criação de números aleatórios para impedir o estado de transmissão
            final Random cloudMachines = new Random(); //Máquinas da nuvem
            double       machineDown   = cloudMachines.nextInt(redeDeFilas.getMaquinasCloud().size());

            // Máquina desligada para impedir que a transmissão seja feita
            machineDown = CloudMachine.DESLIGADO;

            // Nova máquina para ser replicada
            double newMachine = machineDown;

            // Número de VMs na simulação
            final double machineNumber = redeDeFilas.getMaquinasCloud().size();

            // Método de recuperação - Replicação
            for (int i = 0; i < machineNumber; i++) {
                if (machineDown == 2) {
                    machineDown = newMachine;
                    newMachine = CloudMachine.DESLIGADO;
                } else {
                    janela.println("Nenhuma máquina com falha encontrada!");
                }
            }
        }
    }
}
