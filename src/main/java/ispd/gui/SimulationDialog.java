package ispd.gui;

import static ispd.gui.TextSupplier.*;
import static ispd.gui.utils.ButtonBuilder.*;

import ispd.arquivo.xml.*;
import ispd.gui.results.*;
import ispd.gui.utils.fonts.*;
import ispd.motor.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import org.w3c.dom.Document;

/**
 * Makes calls to simulation engine. Presents the steps taken so far and simulation progress (%).
 */
public class SimulationDialog extends JDialog implements Runnable {

    private final MutableAttributeSet colorConfig = new SimpleAttributeSet();

    private final Document model;

    private final ProgressoSimulacao progressTracker = new BasicProgressTracker();

    private final int gridOrCloud;

    private JProgressBar progressBar;

    private JTextPane notificationArea;

    private Thread simThread = null;

    private int progressPercent = 0;

    public SimulationDialog (
        final Frame parent,
        final boolean modal,
        final Document model,
        final int gridOrCloud
    ) {
        super(parent, modal);
        this.gridOrCloud = gridOrCloud;
        this.model       = model;
        this.initComponents();
        this.addWindowListener(new SomeWindowAdapter());
    }

    @Override
    public void run () {
        this.progressTracker.println("Simulation Initiated.");
        try {
            //0%
            //Verifica se foi construido modelo na area de desenho
            this.progressTracker.validarInicioSimulacao(this.model);//[5%] --> 5%

            this.progressTracker.print("Mounting network queue.");
            this.progressTracker.print(" -> ");

            if (this.gridOrCloud == PickModelTypeDialog.GRID) {
                final var queueNetwork = GridQueueNetworkFactory.fromDocument(this.model);
                this.incrementProgress(10);//[10%] --> 35%
                this.progressTracker.println("OK", Color.green);
                //criar tarefas
                this.progressTracker.print("Creating tasks.");
                this.progressTracker.print(" -> ");
                final var tasks = WorkloadGeneratorFactory
                    .fromDocument(this.model).makeTaskList(queueNetwork);
                this.incrementProgress(10);//[10%] --> 45%
                this.progressTracker.println("OK", Color.green);
                //Verifica recursos do modelo e define roteamento
                final var sim = new SimulacaoSequencial(
                    this.progressTracker,
                    queueNetwork,
                    tasks
                );//[10%] --> 55 %
                //Realiza asimulação
                this.progressTracker.println("Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a
                // simulação
                final double t1 = System.currentTimeMillis();

                sim.simulate();//[30%] --> 85%

                //Recebe instnte de tempo em milissegundos ao fim da execução
                // da simulação
                final double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                //Obter Resultados
                final var metrica = sim.getMetrics();
                //[5%] --> 90%
                //Apresentar resultados
                this.progressTracker.print("Showing results.");
                this.progressTracker.print(" -> ");
                final Window janelaResultados =
                    new ResultsDialog(null, metrica, queueNetwork, tasks);
                this.incrementProgress(10);//[10%] --> 100%
                this.progressTracker.println("OK", Color.green);
                final var tempototal = (t2 - t1) / 1000;
                this.progressTracker.println(
                    "Simulation Execution Time = " + tempototal + "seconds");
                janelaResultados.setLocationRelativeTo(this);
                janelaResultados.setVisible(true);
            } else if (this.gridOrCloud == PickModelTypeDialog.IAAS) {

                final var cloudQueueNetwork = CloudQueueNetworkFactory.fromDocument(this.model);

                this.incrementProgress(10);//[10%] --> 35%
                this.progressTracker.println("OK", Color.green);
                //criar tarefas
                this.progressTracker.print("Creating tasks.");
                this.progressTracker.print(" -> ");
                final var tasks = WorkloadGeneratorFactory
                    .fromDocument(this.model).makeTaskList(cloudQueueNetwork);
                this.incrementProgress(10);//[10%] --> 45%
                this.progressTracker.println("OK", Color.green);
                //Verifica recursos do modelo e define roteamento
                final var sim =
                    new SimulacaoSequencialCloud(
                        this.progressTracker,
                        cloudQueueNetwork,
                        tasks
                    );//[10%] --> 55 %
                //Realiza asimulação
                this.progressTracker.println("Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a
                // simulação
                final double t1 = System.currentTimeMillis();

                sim.simulate();//[30%] --> 85%

                //Recebe instnte de tempo em milissegundos ao fim da execução
                // da simulação
                final double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                //Obter Resultados
                final var metrica = sim.getCloudMetrics();
                //[5%] --> 90%
                //Apresentar resultados
                this.progressTracker.print("Showing results.");
                this.progressTracker.print(" -> ");
                final var janelaResultados =
                    new CloudResultsDialog(null, metrica, cloudQueueNetwork, tasks);
                this.incrementProgress(10);//[10%] --> 100%
                this.progressTracker.println("OK", Color.green);
                final var tempototal = (t2 - t1) / 1000;
                this.progressTracker.println(
                    "Simulation Execution Time = " + tempototal + "seconds");
                janelaResultados.setLocationRelativeTo(this);
                janelaResultados.setVisible(true);
            }
        } catch (final IllegalArgumentException erro) {
            Logger.getLogger(SimulationDialog.class.getName())
                .log(Level.SEVERE, null, erro);
            this.progressTracker.println(erro.getMessage(), Color.red);
            this.progressTracker.print("Simulation Aborted", Color.red);
            this.progressTracker.println("!", Color.red);
        }
    }

    private void initComponents () {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(getText("Running Simulation"));

        this.progressBar = new JProgressBar();
        this.progressBar.setDebugGraphicsOptions(DebugGraphics.NONE_OPTION);

        this.notificationArea = new JTextPane();
        this.notificationArea.setEditable(false);
        this.notificationArea.setFont(Arial.BOLD);

        this.makeLayoutAndPack();
    }

    private void makeLayoutAndPack () {
        final var scrollPane   = new JScrollPane(this.notificationArea);
        final var cancelButton = basicButton(getText("Cancel"), this::onCancel);

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                      .addComponent(cancelButton,
                                                    GroupLayout.Alignment.LEADING,
                                                    GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE
                                      )
                                      .addComponent(scrollPane,
                                                    GroupLayout.Alignment.LEADING,
                                                    GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE
                                      )
                                      .addComponent(this.progressBar,
                                                    GroupLayout.Alignment.LEADING,
                                                    GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE
                                      ))
                        .addContainerGap()
                )
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPane,
                                      GroupLayout.DEFAULT_SIZE,
                                      227, Short.MAX_VALUE
                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(this.progressBar,
                                      GroupLayout.PREFERRED_SIZE,
                                      42, GroupLayout.PREFERRED_SIZE
                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton,
                                      GroupLayout.PREFERRED_SIZE,
                                      41, GroupLayout.PREFERRED_SIZE
                        )
                        .addContainerGap()
                )
        );

        this.pack();
    }

    private void onCancel (final ActionEvent evt) {
        if (this.simThread != null) {
            this.simThread = null;
        }
        this.dispose();
    }

    void iniciarSimulacao () {
        this.simThread = new Thread(this);
        this.simThread.start();
    }

    private void incrementProgress (final int add) {
        this.progressPercent += add;
        this.progressBar.setValue(this.progressPercent);
    }

    private class SomeWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing (final WindowEvent e) {

            SimulationDialog.this.simThread = null;
            SimulationDialog.this.dispose();
        }
    }

    private class BasicProgressTracker extends ProgressoSimulacao {

        @Override
        public void incProgresso (final int n) {
            SimulationDialog.this.incrementProgress(n);
        }

        @Override
        public void print (final String text, final Color cor) {
            try {
                final var doc    = SimulationDialog.this.notificationArea.getDocument();
                final var config = SimulationDialog.this.colorConfig;

                StyleConstants.setForeground(config, Optional.ofNullable(cor).orElse(Color.black));
                doc.insertString(doc.getLength(), getText(text), config);
            } catch (final BadLocationException ex) {
                Logger.getLogger(SimulationDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}