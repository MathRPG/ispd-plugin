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

        if (redeDeFilas.getVMs() == null) {
            return;
        }

        //sorteio de máquina alocada ao mestre  Falha de omissão de hardware:
        final Random random = new Random();
        int          draw   = random.nextInt(redeDeFilas.getMaquinasCloud().size());
        //tornar a posição sorteada ==Desligada
        draw = CloudMachine.DESLIGADO;

        final int NovoRedeDeFilas = redeDeFilas.getMaquinasCloud().size() - 1;

        //escreva o vetor redeDeFilas com a posição [draw} com status == DESLIGADO
        for (int i = 0; i <= NovoRedeDeFilas; i++) {
        }
        //Refazer o escalonamento: Técnica: Regate do Workflow??? ou Redistribuição das tarefas??

    }
}