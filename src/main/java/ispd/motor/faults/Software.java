package ispd.motor.faults;

import ispd.gui.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.simul.*;
import java.util.*;
import javax.swing.*;

public class Software {

    public void FISfotware1 (final ProgressTracker janela, final CloudQueueNetwork redeDeFilas) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Falha de Omissão de software selecionada.");

        janela.println("Software failure created.");

        if (redeDeFilas.getVMs() != null && (redeDeFilas.getVMs() != null)) {
            System.out.println("---------------------------------------");
            System.out.println("Rede de filas das VMs não é nula na classe CloudSequential.java");
            System.out.println("Listagem da rede de filas: ");
            System.out.println("Rede de Filas: " + redeDeFilas);
            System.out.println("Rede de Filas get VMs: " + redeDeFilas.getVMs());
            System.out.println("Há máquinas alocadas no redeDeFilas");
            System.out.println("Rede de Filas Cloud get VMs: " + redeDeFilas.getVMs());
            System.out.println("Rede de Filas Cloud: getMaquinasCloud: "
                               + redeDeFilas.getMaquinasCloud());
            System.out.println("Quantidade de Máquinas alocadas ao mestre: " + redeDeFilas
                .getMaquinasCloud()
                .size());
            //sorteio de máquina alocada ao mestre  Falha de omissão de hardware:
            final Random random = new Random();
            int          draw   = random.nextInt(redeDeFilas.getMaquinasCloud().size());
            System.out.println("Número da posição da maquina sorteada: " + draw); //Exemplo: [27]
            System.out.println("Máquina sorteada desligada: " + draw);
            //tornar a posição sorteada ==Desligada
            draw = CloudMachine.DESLIGADO;
            System.out.println(
                "Status da máquina desligada => public static final int DESLIGADO = 2: " + draw);

            final int NovoRedeDeFilas = redeDeFilas.getMaquinasCloud().size() - 1;
            System.out.println("Novo redeDeFilas: " + NovoRedeDeFilas);

            //escreva o vetor redeDeFilas com a posição [draw} com status == DESLIGADO
            for (int i = 0; i <= NovoRedeDeFilas; i++) {
                System.out.println("Novo Rede de Filas: " + NovoRedeDeFilas);
                System.out.println("Novo Rede de Filas Cloud get VMs: " + redeDeFilas.getVMs());
                System.out.println("Novo Rede de Filas Cloud: getMaquinasCloud: "
                                   + redeDeFilas.getMaquinasCloud());
            }
            //Refazer o escalonamento: Técnica: Regate do Workflow??? ou Redistribuição das tarefas??

        }
    }
}