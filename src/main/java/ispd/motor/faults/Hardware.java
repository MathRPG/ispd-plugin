package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.simul.*;
import java.util.*;
import javax.swing.*;

public class Hardware {

    private static void printNewQueueNetwork (
        final List<VirtualMachine> vms,
        final List<CloudMachine> machines
    ) {
        final int qn = machines.size() - 1;
        for (int i = 0; i <= qn; i++) {
        }
    }

    private static void selectFaults (final ProgressTracker janela) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        janela.println("Hardware failure created.");
        janela.print(" -> ");
    }

    public void FIHardware1 (
        final ProgressTracker janela,
        final CloudQueueNetwork gridQueueNetwork
    ) {
        selectFaults(janela);

        final var vms = gridQueueNetwork.getVMs();

        System.out.println("---------------------------------------");

        if (vms == null) {
            System.out.println("Rede de filas é nula na classe " + "CloudSequential.java");
            return;
        }

        final var machines = gridQueueNetwork.getMaquinasCloud();
        final int id       = new Random().nextInt(machines.size());

        System.out.printf("""
                          Rede de filas das VMs não é nula na classe CloudSequential.java
                          Listagem da rede de filas:
                          Rede de Filas: %s
                          Rede de Filas get VMs: %s
                          Há máquinas alocadas no redeDeFilas
                          Rede de Filas Cloud get PMs: %s
                          Rede de Filas Cloud: getMaquinasCloud: %s
                          Quantidade de Máquinas alocadas ao mestre: %d
                          Número da posição da maquina sorteada: %d
                          Máquina sorteada desligada: %d
                          Máquina que o status é igual a 2: %d
                          """,
                          gridQueueNetwork,
                          vms,
                          vms,
                          machines,
                          machines.size(),
                          id,
                          id,
                          CloudMachine.DESLIGADO
        );

        printNewQueueNetwork(vms, machines);
    }
}