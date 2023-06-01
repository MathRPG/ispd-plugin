package ispd.motor.falhas;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JOptionPane;

import ispd.motor.ProgressoSimulacao;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.metricas.MetricasGlobais;


public class FIValue {

    private ArrayList<CS_VirtualMac>     VMs;
    private LinkedList<CS_Processamento> PMs;


    /*
     * @param janela
     * @param redeDeFilas
     * @param global
     *
     */

    //Método para inserção de falha de resposta
    public void FIValue1 (ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas, MetricasGlobais global) {

        //Mensagens com a inserção da falha
        JOptionPane.showMessageDialog(null, "Response failure detected.");
        janela.println("Response fault created.");
        janela.println("->");

        //Criação de filas vazias para armazenamento das máquinas antes da falha
        this.VMs = new ArrayList<CS_VirtualMac>(); //Lista vazia de máquinas virtuais
        this.PMs = new LinkedList<CS_Processamento>(); //Lista vazia de máquians físicas

        //Processo de falha e tratamento
        if (redeDeFilas.getVMs() == null) {
            System.out.println("---------------------------------------");
            System.out.println("Rede de filas é nula.");

        }// if(redeDeFilas.getVMs() == null)
        else if (redeDeFilas.getVMs() != null) {

            //Variáveis para recuperação
            double OciosidadeComputacaoOri  = global.getOciosidadeComputacao();
            double OciosidadeComunicacaoOri = global.getOciosidadeComunicacao();
            double SatisfacaoMediaOri       = global.getSatisfacaoMedia();
            double EficienciaOri            = global.getEficiencia();

            //Criação de números aleatórios para alterações das respostas para usuários
            Random cloudMachines = new Random(); //Máquinas da nuvem

            double metricsCloud = cloudMachines.nextInt(redeDeFilas.getMaquinasCloud().size());

            global.setOciosidadeComputacao(metricsCloud / 100);
            global.setOciosidadeComunicacao(metricsCloud);
            global.setSatisfacaoMedia((metricsCloud / 100) * (metricsCloud / 100));
            global.setEficiencia(metricsCloud);

            //Recuperação via checkpoint
            if (OciosidadeComputacaoOri != global.getOciosidadeComputacao() ||
                OciosidadeComunicacaoOri != global.getOciosidadeComunicacao() ||
                SatisfacaoMediaOri != global.getSatisfacaoMedia() || EficienciaOri != global.getEficiencia()) {

                global.setOciosidadeComputacao(OciosidadeComputacaoOri);
                global.setOciosidadeComunicacao(OciosidadeComunicacaoOri);
                global.setSatisfacaoMedia(SatisfacaoMediaOri);
                global.setEficiencia(EficienciaOri);


            } else {

            } //if(Checkpoint para recupreação de valores da simulação)


        } //else if(redeDeFilas.getVMs() != null)


    }

}
