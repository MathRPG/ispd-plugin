package ispd.motor.faults;

import ispd.motor.metrics.*;
import ispd.motor.queues.*;
import ispd.motor.simul.*;
import java.util.*;
import javax.swing.*;

public class Value {

    //Método para inserção de falha de resposta
    public void FIValue1 (
        final ProgressTracker janela,
        final CloudQueueNetwork redeDeFilas,
        final Global global
    ) {
        //Mensagens com a inserção da falha
        JOptionPane.showMessageDialog(null, "Response failure detected.");
        janela.println("Response fault created.");
        janela.println("->");

        //Criação de filas vazias para armazenamento das máquinas antes da falha

        //Processo de falha e tratamento
        if (redeDeFilas.getVMs() == null) {

        } else if (redeDeFilas.getVMs() != null) {
            //Variáveis para recuperação
            final double OciosidadeComputacaoOri  = global.getOciosidadeComputacao();
            final double OciosidadeComunicacaoOri = global.getOciosidadeComunicacao();
            final double SatisfacaoMediaOri       = global.getSatisfacaoMedia();
            final double EficienciaOri            = global.getEficiencia();

            //Criação de números aleatórios para alterações das respostas para usuários
            final Random cloudMachines = new Random(); //Máquinas da nuvem

            final double metricsCloud = cloudMachines.nextInt(redeDeFilas.getMaquinasCloud().size());

            global.setOciosidadeComputacao(metricsCloud / 100);
            global.setOciosidadeComunicacao(metricsCloud);
            global.setSatisfacaoMedia((metricsCloud / 100) * (metricsCloud / 100));
            global.setEficiencia(metricsCloud);

            //Recuperação via checkpoint
            if (OciosidadeComputacaoOri != global.getOciosidadeComputacao()
                || OciosidadeComunicacaoOri != global.getOciosidadeComunicacao()
                || SatisfacaoMediaOri != global.getSatisfacaoMedia()
                || EficienciaOri != global.getEficiencia()) {

                global.setOciosidadeComputacao(OciosidadeComputacaoOri);
                global.setOciosidadeComunicacao(OciosidadeComunicacaoOri);
                global.setSatisfacaoMedia(SatisfacaoMediaOri);
                global.setEficiencia(EficienciaOri);
            }
        }
    }
}
