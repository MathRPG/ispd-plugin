package ispd.gui;

import ispd.arquivo.*;
import ispd.gui.utils.*;
import ispd.gui.utils.components.*;
import ispd.gui.utils.fonts.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.motor.filas.servidores.implementacao.*;
import ispd.motor.metricas.*;
import ispd.utils.constants.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.table.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;

public class CloudResultsDialog extends JDialog {

    private static final int BUFFERED_IMAGE_WIDTH = 1200;

    private static final int BUFFERED_IMAGE_HEIGHT = 600;

    private final List<? extends Tarefa> tarefas;

    private final Object[][] tabelaRecurso;

    private final ChartPanel graficoProcessamentoTempoUser1;

    private final ChartPanel graficoProcessamentoTempoUser2;

    private final SalvarResultadosHTML html = new SalvarResultadosHTML();

    private JButton jButtonProcessamentoMaquina;

    private JButton jButtonProcessamentoTarefa;

    private JScrollPane jScrollPaneAlocacao;

    private JScrollPane jScrollPaneComunicacao;

    private JScrollPane jScrollPaneCustos;

    private JScrollPane jScrollPaneProcessamento;

    private JScrollPane jScrollPaneProcessamentoTempo;

    private JScrollPane jScrollPaneUsuario;

    private JTabbedPane jTabbedPaneGrid;

    private JTextArea jTextAreaGlobal;

    private JTextArea jTextAreaTarefa;

    private JTextArea jTextAreaUsuario;

    private ChartPanel graficoBarraProcessamento;

    private ChartPanel graficoBarraComunicacao;

    private ChartPanel graficoBarraAlocacao;

    private ChartPanel graficoPizzaAlocacao;

    private ChartPanel graficoPizzaProcessamento;

    private ChartPanel graficoPizzaComunicacao;

    private ChartPanel graficoProcessamentoTempo = null;

    private ChartPanel graficoProcessamentoTempoTarefa = null;

    private ChartPanel graficoBarraCustoTotal;

    private ChartPanel graficoBarraCustoDisco;

    private ChartPanel graficoBarraCustoMem;

    private ChartPanel graficoBarraCustoProc;

    private double poderComputacionalTotal = 0;

    public CloudResultsDialog (
        final Frame parent, final Metricas metricas, final RedeDeFilasCloud rdf,
        final List<? extends Tarefa> tarefas
    ) {
        super(parent, true);
        this.tarefas = tarefas;
        this.gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        this.gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        this.gerarGraficosAlocacao(metricas.getMetricasAlocacao());
        this.gerarGraficosCusto(metricas.getMetricasCusto());
        this.tabelaRecurso = setTabelaRecurso(metricas);
        this.initComponents();
        this.jTextAreaGlobal.setText(getResultadosGlobais(metricas.getMetricasGlobais()));
        this.html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.jTextAreaTarefa.setText(getResultadosTarefas(metricas));
        this.html.setMetricasTarefas(metricas);
        final var mestre = (CS_VMM) rdf.getMestres().get(0);
        this.setResultadosUsuario(mestre.getEscalonador().getMetricaUsuarios(), metricas);

        if (rdf.getVMs().size() < 21) {
            this.graficoProcessamentoTempo =
                new ChartPanel(this.criarGraficoProcessamentoTempo(rdf));
            this.graficoProcessamentoTempo.setPreferredSize(new Dimension(600, 300));
        } else {
            this.jButtonProcessamentoMaquina.setVisible(false);
            for (final CS_Processamento maq : rdf.getVMs()) {
                this.poderComputacionalTotal +=
                    (
                        maq.getPoderComputacional() - (
                            maq.getOcupacao()
                            * maq.getPoderComputacional()
                        )
                    );
            }
        }
        if (tarefas.size() < 50) {
            this.graficoProcessamentoTempoTarefa =
                new ChartPanel(this.criarGraficoProcessamentoTempoTarefa(tarefas));
            this.graficoProcessamentoTempoTarefa.setPreferredSize(new Dimension(600, 300));
        } else {
            this.jButtonProcessamentoTarefa.setVisible(false);
        }
        final var temp = this.gerarGraficoProcessamentoTempoUser(tarefas, rdf);
        this.graficoProcessamentoTempoUser1 = new ChartPanel(temp[0]);
        this.graficoProcessamentoTempoUser1.setPreferredSize(new Dimension(600, 300));
        this.graficoProcessamentoTempoUser2 = new ChartPanel(temp[1]);
        this.graficoProcessamentoTempoUser2.setPreferredSize(new Dimension(600, 300));
        this.jScrollPaneAlocacao.setViewportView(this.graficoBarraAlocacao);
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoTotal);
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);
    }

    private static Object[][] setTabelaRecurso (final Metricas metricas) {
        final List<Object[]> tabela = new ArrayList<>();
        //linha [Nome] [Proprietario] [Processamento] [comunicacao]
        String nome;
        String prop;
        double proc;
        double comu;
        if (metricas.getMetricasProcessamento() != null) {
            for (final var entry : metricas
                .getMetricasProcessamento()
                .entrySet()) {
                final var maq = entry.getValue();
                if (maq.getnumeroMaquina() == 0) {
                    nome = maq.getId();
                } else {
                    nome = maq.getId() + " node " + maq.getnumeroMaquina();
                }
                prop = maq.getProprietario();
                proc = maq.getSegundosDeProcessamento();
                comu = 0.0;
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
            }
        }
        if (metricas.getMetricasComunicacao() != null) {
            for (final var entry : metricas
                .getMetricasComunicacao()
                .entrySet()) {
                final var link = entry.getValue();
                nome = link.getId();
                prop = "---";
                proc = 0.0;
                comu = link.getSegundosDeTransmissao();
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
            }
        }
        final var temp = new Object[tabela.size()][4];
        for (var i = 0; i < tabela.size(); i++) {
            temp[i] = tabela.get(i);
        }
        return temp;
    }

    private static String getResultadosGlobais (final MetricasGlobais globais) {
        var texto = "\t\tSimulation Results:\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", globais.getTempoSimulacao());
        texto += String.format("\tSatisfaction = %g %%\n", globais.getSatisfacaoMedia());
        texto += String.format(
            "\tIdleness of processing resources = %g %%\n",
            globais.getOciosidadeComputacao()
        );
        texto += String.format(
            "\tIdleness of communication resources = %g " + "%%\n",
            globais.getOciosidadeComunicacao()
        );
        texto += String.format("\tEfficiency = %g %%\n", globais.getEficiencia());
        if (globais.getEficiencia() > 70.0) {
            texto += "\tEfficiency GOOD\n\n ";
        } else if (globais.getEficiencia() > 40.0) {
            texto += "\tEfficiency MEDIA\n\n ";
        } else {
            texto += "\tEfficiency BAD\n\n ";
        }
        texto += "\t\tCost Results:\n\n";
        texto += String.format("\tCost Total Processing = %g $\n", globais.getCustoTotalProc());
        texto += String.format("\tCost Total Memory = %g $\n", globais.getCustoTotalMem());
        texto += String.format("\tCost Total Disk = %g $\n\n", globais.getCustoTotalDisco());
        texto += "\t\tVM Alocation Results:\n\n";
        texto += String.format("\tTotal of VMs alocated = %d \n", globais.getNumVMsAlocadas());
        texto += String.format("\tTotal of VMs rejected = %d \n", globais.getNumVMsRejeitadas());

        return texto;
    }

    private static String getResultadosTarefas (final Metricas metrica) {
        var texto = "\n\n\t\tTASKS\n ";
        final var tempoMedioSistemaComunicacao =
            metrica.getTempoMedioFilaComunicacao() + metrica.getTempoMedioComunicacao();
        final var tempoMedioSistemaProcessamento =
            metrica.getTempoMedioFilaProcessamento() + metrica.getTempoMedioProcessamento();
        texto += "\n Communication \n";
        texto += String.format(
            "    Queue average time: %g seconds.\n",
            metrica.getTempoMedioFilaComunicacao()
        );
        texto +=
            String.format(
                "    Communication average time: %g seconds" + ".\n",
                metrica.getTempoMedioComunicacao()
            );
        texto +=
            String.format("    System average time: %g seconds.\n", tempoMedioSistemaComunicacao);
        texto += "\n Processing \n";
        texto += String.format(
            "    Queue average time: %g seconds.\n",
            metrica.getTempoMedioFilaProcessamento()
        );
        texto += String.format(
            "    Processing average time: %g seconds.\n",
            metrica.getTempoMedioProcessamento()
        );
        texto +=
            String.format("    System average time: %g seconds.\n", tempoMedioSistemaProcessamento);
        if (metrica.getNumTarefasCanceladas() > 0) {
            texto += "\n Tasks Canceled \n";
            texto += String.format("    Number: %d \n", metrica.getNumTarefasCanceladas());
            texto +=
                String.format("    Wasted Processing: %g Mflops", metrica.getMflopsDesperdicio());
        }
        return texto;
    }

    private void gerarGraficosProcessamento (final Map<String, MetricasProcessamento> mProcess) {
        final var dadosGraficoProcessamento      = new DefaultCategoryDataset();
        final var dadosGraficoPizzaProcessamento = new DefaultPieDataset();

        if (mProcess != null) {
            for (final var entry : mProcess.entrySet()) {
                final var mt = entry.getValue();
                if (mt.getnumeroMaquina() == 0) {
                    dadosGraficoProcessamento.addValue(
                        mt.getMFlopsProcessados(),
                        "vermelho",
                        mt.getId()
                    );
                    dadosGraficoPizzaProcessamento.insertValue(
                        0,
                        mt.getId(),
                        mt.getMFlopsProcessados()
                    );
                } else {
                    dadosGraficoProcessamento.addValue(
                        mt.getMFlopsProcessados(),
                        "vermelho",
                        mt.getId() + " node " + mt.getnumeroMaquina()
                    );
                    dadosGraficoPizzaProcessamento.insertValue(
                        0,
                        mt.getId() + " node " + mt.getnumeroMaquina(),
                        mt.getMFlopsProcessados()
                    );
                }
            }
        }

        var jfc = ChartFactory.createBarChart(
            "Total processed on each resource",
            //Titulo
            "Resource",
            // Eixo X
            "Mflops",
            //Eixo Y
            dadosGraficoProcessamento,
            // Dados para o grafico
            PlotOrientation.VERTICAL,
            //Orientacao do grafico
            false,
            false,
            false
        ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mProcess.size() > 10) {
            jfc
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraProcessamento = new ChartPanel(jfc);
        this.graficoBarraProcessamento.setPreferredSize(new Dimension(600, 300));

        jfc                            =
            ChartFactory.createPieChart("Total processed on each resource", //Titulo
                                        dadosGraficoPizzaProcessamento,
                                        // Dados para o grafico
                                        true, false, false
            );
        this.graficoPizzaProcessamento = new ChartPanel(jfc);
        this.graficoPizzaProcessamento.setPreferredSize(new Dimension(600, 300));
    }

    private void gerarGraficosComunicacao (final Map<String, MetricasComunicacao> mComunicacao) {
        final var dadosGraficoComunicacao      = new DefaultCategoryDataset();
        final var dadosGraficoPizzaComunicacao = new DefaultPieDataset();

        if (mComunicacao != null) {
            for (final var entry : mComunicacao.entrySet()) {
                final var link = entry.getValue();
                dadosGraficoComunicacao.addValue(
                    link.getMbitsTransmitidos(),
                    "vermelho",
                    link.getId()
                );
                dadosGraficoPizzaComunicacao.insertValue(
                    0,
                    link.getId(),
                    link.getMbitsTransmitidos()
                );
            }
        }

        var jfc =
            ChartFactory.createBarChart("Total communication in each resource", //Titulo
                                        "Resource", // Eixo X
                                        "Mbits", //Eixo Y
                                        dadosGraficoComunicacao, // Dados para o grafico
                                        PlotOrientation.VERTICAL, //Orientacao do grafico
                                        false, false, false
            ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mComunicacao != null && mComunicacao.size() > 10) {
            jfc
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraComunicacao = new ChartPanel(jfc);
        this.graficoBarraComunicacao.setPreferredSize(new Dimension(600, 300));

        jfc                          =
            ChartFactory.createPieChart("Total communication in each resource", //Titulo
                                        dadosGraficoPizzaComunicacao, // Dados para o grafico
                                        true, false, false
            );
        this.graficoPizzaComunicacao = new ChartPanel(jfc);
        this.graficoPizzaComunicacao.setPreferredSize(new Dimension(600, 300));
    }

    private void gerarGraficosAlocacao (final Map<String, MetricasAlocacao> mAloc) {
        final var dadosGraficoAloc      = new DefaultCategoryDataset();
        final var dadosGraficoPizzaAloc = new DefaultPieDataset();

        if (mAloc != null) {
            for (final var entry : mAloc.entrySet()) {
                final var mt = entry.getValue();
                dadosGraficoAloc.addValue(mt.getNumVMs(), "vermelho", mt.getId());
                dadosGraficoPizzaAloc.insertValue(0, mt.getId(), mt.getNumVMs());
            }
        }

        var jfc = ChartFactory.createBarChart(
            "Total of virtual machines allocated in each resource",
            //Titulo
            "Resource",
            // Eixo X
            "Number of VMs",
            //Eixo Y
            dadosGraficoAloc,
            // Dados para o grafico
            PlotOrientation.VERTICAL,
            //Orientacao do grafico
            false,
            false,
            false
        ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mAloc.size() > 10) {
            jfc
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraAlocacao = new ChartPanel(jfc);
        this.graficoBarraAlocacao.setPreferredSize(new Dimension(600, 300));

        jfc                       =
            ChartFactory.createPieChart(
                "Total of virtual machines allocated in each resource",
                //Titulo
                dadosGraficoPizzaAloc,
                // Dados para o grafico
                true,
                false,
                false
            );
        this.graficoPizzaAlocacao = new ChartPanel(jfc);
        this.graficoPizzaAlocacao.setPreferredSize(new Dimension(600, 300));
    }

    private void gerarGraficosCusto (final Map<String, MetricasCusto> mCusto) {
        final var dadosGraficoCustoTotal = new DefaultCategoryDataset();
        final var dadosGraficoCustoDisco = new DefaultCategoryDataset();
        final var dadosGraficoCustoMem   = new DefaultCategoryDataset();
        final var dadosGraficoCustoProc  = new DefaultCategoryDataset();

        if (mCusto != null) {
            for (final var entry : mCusto.entrySet()) {
                final var mt = entry.getValue();
                dadosGraficoCustoTotal.addValue(
                    mt.getCustoDisco() + mt.getCustoMem() + mt.getCustoProc(),
                    "vermelho",
                    mt.getId()
                );
                dadosGraficoCustoDisco.addValue(mt.getCustoDisco(), "vermelho", mt.getId());
                dadosGraficoCustoMem.addValue(mt.getCustoMem(), "vermelho", mt.getId());
                dadosGraficoCustoProc.addValue(mt.getCustoProc(), "vermelho", mt.getId());
            }
        }

        final var jfct =
            ChartFactory.createBarChart("Total cost of utilization per virtual machine", //Titulo
                                        "Virtual machines", // Eixo X
                                        "Cost ($)", //Eixo Y
                                        dadosGraficoCustoTotal, // Dados para o grafico
                                        PlotOrientation.VERTICAL, //Orientacao do grafico
                                        false, false, false
            ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfct
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraCustoTotal = new ChartPanel(jfct);
        this.graficoBarraAlocacao.setPreferredSize(new Dimension(600, 300));

        final var jfcd =
            ChartFactory.createBarChart("Cost of disk utilization per virtual machine", //Titulo
                                        "Virtual machines", // Eixo X
                                        "Cost ($)", //Eixo Y
                                        dadosGraficoCustoDisco, // Dados para o grafico
                                        PlotOrientation.VERTICAL, //Orientacao do grafico
                                        false, false, false
            ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfcd
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraCustoDisco = new ChartPanel(jfcd);
        this.graficoBarraCustoDisco.setPreferredSize(new Dimension(600, 300));

        final var jfcm =
            ChartFactory.createBarChart("Cost of memory utilization per virtual machine", //Titulo
                                        "Virtual machines", // Eixo X
                                        "Cost ($)", //Eixo Y
                                        dadosGraficoCustoMem, // Dados para o grafico
                                        PlotOrientation.VERTICAL, //Orientacao do grafico
                                        false, false, false
            ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfcm
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraCustoMem = new ChartPanel(jfcm);
        this.graficoBarraCustoMem.setPreferredSize(new Dimension(600, 300));

        final var jfcp =
            ChartFactory.createBarChart(
                "Cost of processing utilization per virtual machine",
                //Titulo
                "Virtual machines",
                // Eixo X
                "Cost ($)",
                //Eixo Y
                dadosGraficoCustoProc,
                // Dados para o grafico
                PlotOrientation.VERTICAL,
                //Orientacao do grafico
                false,
                false,
                false
            ); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfcp
                .getCategoryPlot()
                .getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraCustoProc = new ChartPanel(jfcp);
        this.graficoBarraCustoProc.setPreferredSize(new Dimension(600, 300));
    }

    private void initComponents () {

        this.jTabbedPaneGrid = new JTabbedPane();
        final var jPanelGlobal        = new JPanel();
        final var jToolBar1           = new JToolBar();
        final var jButtonSalvar       = new JButton();
        final var jButtonSalvarTraces = new JButton();
        final var jScrollPaneGobal    = new JScrollPane();
        this.jTextAreaGlobal = new JTextArea();
        final var jScrollPaneTarefa = new JScrollPane();
        this.jTextAreaTarefa    = new JTextArea();
        this.jScrollPaneUsuario = new JScrollPane();
        this.jTextAreaUsuario   = new JTextArea();
        final var jScrollPaneRecurso = new JScrollPane();
        final Object[] colunas =
            { "Label", "Owner", "Processing performed", "Communication performed" };
        final var jTableRecurso         = new JTable();
        final var jPanelProcessamento   = new JPanel();
        final var jToolBarProcessamento = new JToolBar();
        final var jButtonPBarra         = new JButton();
        final var jButtonPPizza         = new JButton();
        this.jScrollPaneProcessamento = new JScrollPane();
        final var jPanelComunicacao   = new JPanel();
        final var jToolBarComunicacao = new JToolBar();
        final var jButtonCBarra       = new JButton();
        final var jButtonCPizza       = new JButton();
        this.jScrollPaneComunicacao = new JScrollPane();
        final var jPanelProcessamentoTempo   = new JPanel();
        final var jToolBarProcessamentoTempo = new JToolBar();
        final var jButtonProcessamentoUser   = new JButton();
        this.jButtonProcessamentoMaquina   = new JButton();
        this.jButtonProcessamentoTarefa    = new JButton();
        this.jScrollPaneProcessamentoTempo = new JScrollPane();
        final var jPanelAlocacao   = new JPanel();
        final var jToolBarAlocacao = new JToolBar();
        final var jButtonABarra    = new JButton();
        final var jButtonAPizza    = new JButton();
        this.jScrollPaneAlocacao = new JScrollPane();
        final var jPanelCusto   = new JPanel();
        final var jToolBarCusto = new JToolBar();
        final var jButtonTotal  = new JButton();
        final var jButtonDisc   = new JButton();
        final var jButtonMem    = new JButton();
        final var jButtonProc   = new JButton();
        this.jScrollPaneCustos = new JScrollPane();
        final Component jTabbedPane2 = new JTabbedPane();

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Simulation Results");
        this.setIconImage(Toolkit
                              .getDefaultToolkit()
                              .getImage(this.getResource("imagens/Logo_iSPD_25.png")));

        jToolBar1.setRollover(true);

        final var name = "/ispd/gui/imagens/document-save_1.png";
        jButtonSalvar.setIcon(new ImageIcon(this.getResource(name)));
        jButtonSalvar.setToolTipText("Save results as HTML");
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jToolBar1.add(jButtonSalvar);

        jButtonSalvarTraces.setText("Save traces");
        jButtonSalvarTraces.setToolTipText("Save a trace file of simulaton");
        jButtonSalvarTraces.setFocusable(false);
        jButtonSalvarTraces.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSalvarTraces.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSalvarTraces.addActionListener(this::jButtonSalvarTracesActionPerformed);
        jToolBar1.add(jButtonSalvarTraces);

        this.jTextAreaGlobal.setEditable(false);
        this.jTextAreaGlobal.setColumns(20);
        this.jTextAreaGlobal.setFont(CourierNew.BOLD); //

        this.jTextAreaGlobal.setRows(5);
        jScrollPaneGobal.setViewportView(this.jTextAreaGlobal);

        final var jPanelGlobalLayout = new GroupLayout(jPanelGlobal);
        jPanelGlobal.setLayout(jPanelGlobalLayout);
        jPanelGlobalLayout.setHorizontalGroup(jPanelGlobalLayout
                                                  .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                  .addComponent(
                                                      jToolBar1,
                                                      GroupLayout.Alignment.TRAILING,
                                                      GroupLayout.DEFAULT_SIZE,
                                                      651,
                                                      Short.MAX_VALUE
                                                  )
                                                  .addGroup(
                                                      jPanelGlobalLayout
                                                          .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                          .addGroup(
                                                              GroupLayout.Alignment.TRAILING,
                                                              jPanelGlobalLayout
                                                                  .createSequentialGroup()
                                                                  .addContainerGap(
                                                                      GroupLayout.DEFAULT_SIZE,
                                                                      Short.MAX_VALUE
                                                                  )
                                                                  .addComponent(
                                                                      jScrollPaneGobal,
                                                                      GroupLayout.PREFERRED_SIZE,
                                                                      627,
                                                                      GroupLayout.PREFERRED_SIZE
                                                                  )
                                                                  .addContainerGap(
                                                                      GroupLayout.DEFAULT_SIZE,
                                                                      Short.MAX_VALUE
                                                                  )
                                                          )));
        jPanelGlobalLayout.setVerticalGroup(jPanelGlobalLayout
                                                .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanelGlobalLayout.createSequentialGroup()
                                                              .addComponent(
                                                                  jToolBar1,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  25,
                                                                  GroupLayout.PREFERRED_SIZE
                                                              ).addGap(
                                                        0,
                                                        316,
                                                        Short.MAX_VALUE
                                                    ))
                                                .addGroup(
                                                    jPanelGlobalLayout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                            GroupLayout.Alignment.TRAILING,
                                                            jPanelGlobalLayout
                                                                .createSequentialGroup()
                                                                .addContainerGap(
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    Short.MAX_VALUE
                                                                )
                                                                .addComponent(
                                                                    jScrollPaneGobal,
                                                                    GroupLayout.PREFERRED_SIZE,
                                                                    298,
                                                                    GroupLayout.PREFERRED_SIZE
                                                                )
                                                                .addContainerGap(
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    Short.MAX_VALUE
                                                                )
                                                        )));

        this.jTabbedPaneGrid.addTab("Global", jPanelGlobal);

        this.jTextAreaTarefa.setColumns(20);
        this.jTextAreaTarefa.setEditable(false);
        this.jTextAreaTarefa.setFont(CourierNew.PLAIN); //

        this.jTextAreaTarefa.setRows(5);
        jScrollPaneTarefa.setViewportView(this.jTextAreaTarefa);

        this.jTabbedPaneGrid.addTab("Tasks", jScrollPaneTarefa);

        this.jTextAreaUsuario.setColumns(20);
        this.jTextAreaUsuario.setEditable(false);
        this.jTextAreaUsuario.setFont(CourierNew.PLAIN); //

        this.jTextAreaUsuario.setRows(5);
        this.jScrollPaneUsuario.setViewportView(this.jTextAreaUsuario);

        this.jTabbedPaneGrid.addTab("User", this.jScrollPaneUsuario);

        jTableRecurso.setModel(new DefaultTableModel(this.tabelaRecurso, colunas));
        jScrollPaneRecurso.setViewportView(jTableRecurso);

        this.jTabbedPaneGrid.addTab("Resources", jScrollPaneRecurso);

        jToolBarProcessamento.setRollover(true);

        jButtonPBarra.setText("Bar Chart");
        jButtonPBarra.setFocusable(false);
        jButtonPBarra.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonPBarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonPBarra.addActionListener(this::jButtonPBarraActionPerformed);
        jToolBarProcessamento.add(jButtonPBarra);

        jButtonPPizza.setText("Pie chart");
        jButtonPPizza.setFocusable(false);
        jButtonPPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonPPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonPPizza.addActionListener(this::jButtonPPizzaActionPerformed);
        jToolBarProcessamento.add(jButtonPPizza);

        final var jPanelProcessamentoLayout = new GroupLayout(jPanelProcessamento);
        jPanelProcessamento.setLayout(jPanelProcessamentoLayout);
        jPanelProcessamentoLayout.setHorizontalGroup(
            jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jToolBarProcessamento, GroupLayout.DEFAULT_SIZE, 651,
                              Short.MAX_VALUE
                ).addComponent(this.jScrollPaneProcessamento, GroupLayout.DEFAULT_SIZE, 651,
                               Short.MAX_VALUE
                ));
        jPanelProcessamentoLayout.setVerticalGroup(
            jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                jPanelProcessamentoLayout.createSequentialGroup()
                    .addComponent(jToolBarProcessamento, GroupLayout.PREFERRED_SIZE, 25,
                                  GroupLayout.PREFERRED_SIZE
                    ).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(this.jScrollPaneProcessamento, GroupLayout.DEFAULT_SIZE,
                                  291, Short.MAX_VALUE
                    )));

        this.jTabbedPaneGrid.addTab("Chart of the processing", jPanelProcessamento);

        jToolBarComunicacao.setRollover(true);

        jButtonCBarra.setText("Bar Chart");
        jButtonCBarra.setFocusable(false);
        jButtonCBarra.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonCBarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonCBarra.addActionListener(this::jButtonCBarraActionPerformed);
        jToolBarComunicacao.add(jButtonCBarra);

        jButtonCPizza.setText("Pie chart");
        jButtonCPizza.setFocusable(false);
        jButtonCPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonCPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonCPizza.addActionListener(this::jButtonCPizzaActionPerformed);
        jToolBarComunicacao.add(jButtonCPizza);

        final var jPanelComunicacaoLayout = new GroupLayout(jPanelComunicacao);
        jPanelComunicacao.setLayout(jPanelComunicacaoLayout);
        jPanelComunicacaoLayout.setHorizontalGroup(
            jPanelComunicacaoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jToolBarComunicacao, GroupLayout.DEFAULT_SIZE, 651,
                              Short.MAX_VALUE
                ).addComponent(this.jScrollPaneComunicacao, GroupLayout.DEFAULT_SIZE, 651,
                               Short.MAX_VALUE
                ));
        jPanelComunicacaoLayout.setVerticalGroup(
            jPanelComunicacaoLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                jPanelComunicacaoLayout.createSequentialGroup()
                    .addComponent(jToolBarComunicacao, GroupLayout.PREFERRED_SIZE, 25,
                                  GroupLayout.PREFERRED_SIZE
                    ).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(this.jScrollPaneComunicacao, GroupLayout.DEFAULT_SIZE, 291,
                                  Short.MAX_VALUE
                    )));

        this.jTabbedPaneGrid.addTab("Chart of the communication", jPanelComunicacao);

        jToolBarProcessamentoTempo.setRollover(true);

        jButtonProcessamentoUser.setText("Per user");
        jButtonProcessamentoUser.setFocusable(false);
        jButtonProcessamentoUser.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonProcessamentoUser.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonProcessamentoUser.addActionListener(this::jButtonProcessamentoUserActionPerformed);
        jToolBarProcessamentoTempo.add(jButtonProcessamentoUser);

        this.jButtonProcessamentoMaquina.setText("Per machine");
        this.jButtonProcessamentoMaquina.setFocusable(false);
        this.jButtonProcessamentoMaquina.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonProcessamentoMaquina.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonProcessamentoMaquina.addActionListener(this::jButtonProcessamentoMaquinaActionPerformed);
        jToolBarProcessamentoTempo.add(this.jButtonProcessamentoMaquina);

        this.jButtonProcessamentoTarefa.setText("Per task");
        this.jButtonProcessamentoTarefa.setFocusable(false);
        this.jButtonProcessamentoTarefa.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonProcessamentoTarefa.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonProcessamentoTarefa.addActionListener(this::jButtonProcessamentoTarefaActionPerformed);
        jToolBarProcessamentoTempo.add(this.jButtonProcessamentoTarefa);

        final var jPanelProcessamentoTempoLayout =
            new GroupLayout(jPanelProcessamentoTempo);
        jPanelProcessamentoTempo.setLayout(jPanelProcessamentoTempoLayout);
        jPanelProcessamentoTempoLayout.setHorizontalGroup(
            jPanelProcessamentoTempoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jToolBarProcessamentoTempo, GroupLayout.DEFAULT_SIZE, 651,
                              Short.MAX_VALUE
                ).addComponent(this.jScrollPaneProcessamentoTempo,
                               GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE
                ));
        jPanelProcessamentoTempoLayout.setVerticalGroup(
            jPanelProcessamentoTempoLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    jPanelProcessamentoTempoLayout
                        .createSequentialGroup()
                        .addComponent(
                            jToolBarProcessamentoTempo,
                            GroupLayout.PREFERRED_SIZE,
                            25,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(this.jScrollPaneProcessamentoTempo,
                                      GroupLayout.DEFAULT_SIZE, 291,
                                      Short.MAX_VALUE
                        )));

        this.jTabbedPaneGrid.addTab(
            "Use of computing power through time",
            jPanelProcessamentoTempo
        );

        jToolBarAlocacao.setRollover(true);

        jButtonABarra.setText("Bar Chart");
        jButtonABarra.setFocusable(false);
        jButtonABarra.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonABarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonABarra.addActionListener(this::jButtonABarraActionPerformed);
        jToolBarAlocacao.add(jButtonABarra);

        jButtonAPizza.setText("Pie Chat");
        jButtonAPizza.setFocusable(false);
        jButtonAPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonAPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonAPizza.addActionListener(this::jButtonAPizzaActionPerformed);
        jToolBarAlocacao.add(jButtonAPizza);

        final var jPanelAlocacaoLayout = new GroupLayout(jPanelAlocacao);
        jPanelAlocacao.setLayout(jPanelAlocacaoLayout);
        jPanelAlocacaoLayout.setHorizontalGroup(jPanelAlocacaoLayout
                                                    .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(jToolBarAlocacao,
                                                                  GroupLayout.DEFAULT_SIZE, 651,
                                                                  Short.MAX_VALUE
                                                    )
                                                    .addComponent(this.jScrollPaneAlocacao));
        jPanelAlocacaoLayout.setVerticalGroup(jPanelAlocacaoLayout
                                                  .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                  .addGroup(jPanelAlocacaoLayout
                                                                .createSequentialGroup()
                                                                .addComponent(
                                                                    jToolBarAlocacao,
                                                                    GroupLayout.PREFERRED_SIZE,
                                                                    25,
                                                                    GroupLayout.PREFERRED_SIZE
                                                                )
                                                                .addPreferredGap(
                                                                    LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(
                                                                    this.jScrollPaneAlocacao,
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    291,
                                                                    Short.MAX_VALUE
                                                                )));

        this.jTabbedPaneGrid.addTab("Chart of virtual machine allocation", jPanelAlocacao);

        jToolBarCusto.setRollover(true);

        jButtonTotal.setText("Total");
        jButtonTotal.setFocusable(false);
        jButtonTotal.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonTotal.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonTotal.addActionListener(this::jButtonTotalActionPerformed);
        jToolBarCusto.add(jButtonTotal);

        jButtonDisc.setText("Per disk");
        jButtonDisc.setFocusable(false);
        jButtonDisc.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonDisc.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonDisc.addActionListener(this::jButtonDiscActionPerformed);
        jToolBarCusto.add(jButtonDisc);

        jButtonMem.setText("Per Memory");
        jButtonMem.setFocusable(false);
        jButtonMem.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonMem.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonMem.addActionListener(this::jButtonMemActionPerformed);
        jToolBarCusto.add(jButtonMem);

        jButtonProc.setText("Per processing");
        jButtonProc.setFocusable(false);
        jButtonProc.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonProc.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonProc.addActionListener(this::jButtonProcActionPerformed);
        jToolBarCusto.add(jButtonProc);

        final var jPanelCustoLayout = new GroupLayout(jPanelCusto);
        jPanelCusto.setLayout(jPanelCustoLayout);
        jPanelCustoLayout.setHorizontalGroup(jPanelCustoLayout
                                                 .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                 .addComponent(
                                                     jToolBarCusto,
                                                     GroupLayout.DEFAULT_SIZE,
                                                     651,
                                                     Short.MAX_VALUE
                                                 )
                                                 .addComponent(this.jScrollPaneCustos));
        jPanelCustoLayout.setVerticalGroup(jPanelCustoLayout
                                               .createParallelGroup(GroupLayout.Alignment.LEADING)
                                               .addGroup(jPanelCustoLayout
                                                             .createSequentialGroup()
                                                             .addComponent(
                                                                 jToolBarCusto,
                                                                 GroupLayout.PREFERRED_SIZE,
                                                                 25,
                                                                 GroupLayout.PREFERRED_SIZE
                                                             )
                                                             .addPreferredGap(
                                                                 LayoutStyle.ComponentPlacement.RELATED)
                                                             .addComponent(
                                                                 this.jScrollPaneCustos,
                                                                 GroupLayout.DEFAULT_SIZE, 291,
                                                                 Short.MAX_VALUE
                                                             )));

        this.jTabbedPaneGrid.addTab("Chart of the cost of utilization", jPanelCusto);

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.jTabbedPaneGrid)
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                        layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(jTabbedPane2, GroupLayout.PREFERRED_SIZE, 100,
                                          GroupLayout.PREFERRED_SIZE
                            ).addGap(0, 0, Short.MAX_VALUE))));
        layout.setVerticalGroup(layout
                                    .createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(
                                        this.jTabbedPaneGrid,
                                        GroupLayout.PREFERRED_SIZE,
                                        386,
                                        Short.MAX_VALUE
                                    )
                                    .addGroup(layout
                                                  .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                  .addGroup(
                                                      layout
                                                          .createSequentialGroup()
                                                          .addGap(0, 0, Short.MAX_VALUE)
                                                          .addComponent(
                                                              jTabbedPane2,
                                                              GroupLayout.PREFERRED_SIZE,
                                                              100,
                                                              GroupLayout.PREFERRED_SIZE
                                                          )
                                                          .addGap(0, 0, Short.MAX_VALUE))));

        this.pack();
    }

    private void setResultadosUsuario (
        final MetricasUsuarios metricasUsuarios,
        final Metricas metricas
    ) {
        if (metricasUsuarios == null || metricasUsuarios.getUsuarios().size() <= 1) {
            this.jTabbedPaneGrid.remove(this.jScrollPaneUsuario);
            return;
        }

        final var texto = new StringBuilder();
        for (var i = 0; i < metricasUsuarios.getUsuarios().size(); i++) {
            final var userName = metricasUsuarios.getUsuarios().get(i);
            texto.append("\n\n\t\tUser ").append(userName).append("\n");
            final HashSet set = metricasUsuarios.getTarefasConcluidas(userName);
            texto.append("\nNumber of task: ").append(set.size()).append("\n");
            //Applications: Name: Number of task: Mflops:
            var tempoMedioFilaComunicacao   = 0.0;
            var tempoMedioComunicacao       = 0.0;
            var tempoMedioFilaProcessamento = 0.0;
            var tempoMedioProcessamento     = 0.0;
            var numTarefas                  = 0;
            for (final var no : metricasUsuarios.getTarefasConcluidas(userName)) {
                tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                tempoMedioFilaProcessamento = no.getMetricas().getTempoEsperaProc();
                tempoMedioProcessamento     = no.getMetricas().getTempoProcessamento();
                numTarefas++;
            }
            tempoMedioFilaComunicacao   = tempoMedioFilaComunicacao / numTarefas;
            tempoMedioComunicacao       = tempoMedioComunicacao / numTarefas;
            tempoMedioFilaProcessamento = tempoMedioFilaProcessamento / numTarefas;
            tempoMedioProcessamento     = tempoMedioProcessamento / numTarefas;
            final var tempoMedioSistemaComunicacao =
                tempoMedioFilaComunicacao + tempoMedioComunicacao;
            final var tempoMedioSistemaProcessamento =
                tempoMedioFilaProcessamento + tempoMedioProcessamento;
            texto.append("\n Communication \n");
            texto.append(String.format(
                "    Queue average time: %g seconds" + ".\n",
                tempoMedioFilaComunicacao
            ));
            texto.append(String.format(
                "    Communication average time: %g " + "seconds.\n",
                tempoMedioComunicacao
            ));
            texto.append(String.format(
                "    System average time: %g seconds" + ".\n",
                tempoMedioSistemaComunicacao
            ));
            texto.append("\n Processing \n");
            texto.append(String.format(
                "    Queue average time: %g seconds" + ".\n",
                tempoMedioFilaProcessamento
            ));
            texto.append(String.format(
                "    Processing average time: %g " + "seconds.\n",
                tempoMedioProcessamento
            ));
            texto.append(String.format(
                "    System average time: %g seconds" + ".\n",
                tempoMedioSistemaProcessamento
            ));
        }
        texto.append("""

                     Satisfação dos usuários em porcentagem
                     """);
        for (final var entry : metricas
            .getMetricasSatisfacao()
            .entrySet()) {
            final var user       = entry.getKey();
            final var satisfacao = entry.getValue();
            texto.append(user).append(" : ").append(satisfacao).append(" %\n");
        }
        this.jTextAreaUsuario.setText(texto.toString());
    }

    /**
     * Cria o gráfico que demonstra o uso de cada recurso do sistema através do tempo. Ele recebe
     * como parâmetro a lista com as maquinas que processaram durante a simulação.
     */
    private JFreeChart criarGraficoProcessamentoTempo (final RedeDeFilasCloud rdf) {
        final var dadosGrafico = new XYSeriesCollection();
        //Se tiver alguma máquina na lista.
        if (rdf.getVMs() != null) {
            //Laço foreach que percorre as máquinas.
            for (final CS_Processamento maq : rdf.getVMs()) {
                //Lista que recebe os pares de intervalo de tempo em que a
                // máquina executou.
                final var lista = maq.getListaProcessamento();
                this.poderComputacionalTotal +=
                    (
                        maq.getPoderComputacional() - (
                            maq.getOcupacao()
                            * maq.getPoderComputacional()
                        )
                    );
                //Se a máquina tiver intervalos.
                if (!lista.isEmpty()) {
                    //Cria o objeto do tipo XYSeries.
                    final XYSeries tmp_series;
                    //Se o atributo numeroMaquina for 0, ou seja, não for um
                    // nó de um cluster.
                    if (maq.getnumeroMaquina() == 0) //Estancia com o nome puro.
                    {
                        tmp_series = new XYSeries(maq.getId());
                    } //Se for 1 ou mais, ou seja, é um nó de cluster.
                    else //Estancia tmp_series com o nome concatenado com a
                    // palavra node e seu numero.
                    {
                        tmp_series = new XYSeries(maq.getId() + " node " + maq.getnumeroMaquina());
                    }
                    //Laço que vai adicionando os pontos para a criação do gráfico.
                    for (var i = 0; i < lista.size(); i++) {
                        //Calcula o uso, que é 100% - taxa de ocupação inicial.
                        Double uso = 100 - (maq.getOcupacao() * 100);
                        //Adiciona ponto inicial.
                        tmp_series.add(lista.get(i).getInicio(), uso);
                        //Adiciona ponto final.
                        tmp_series.add(lista.get(i).getFim(), uso);
                        if (i + 1 != lista.size()) {
                            uso = 0.0000;
                            tmp_series.add(lista.get(i).getFim(), uso);
                            tmp_series.add(lista.get(i + 1).getInicio(), uso);
                        }
                    }
                    //Add no gráfico.
                    dadosGrafico.addSeries(tmp_series);
                }
            }
        }

        return ChartFactory.createXYAreaChart(
            "Use of computing power through time " + "\nMachines",
            //Titulo
            "Time (seconds)",
            // Eixo X
            "Rate of use of computing power for each node (%)",
            //Eixo Y
            dadosGrafico,
            // Dados para o grafico
            PlotOrientation.VERTICAL,
            //Orientacao do grafico
            true,
            true,
            false
        );
    }

    private JFreeChart criarGraficoProcessamentoTempoTarefa (final Collection<? extends Tarefa> tarefas) {
        final var dadosGrafico = new XYSeriesCollection();
        if (!tarefas.isEmpty()) {
            for (final Tarefa task : tarefas) {
                final XYSeries tmp_series;
                tmp_series = new XYSeries("task " + task.getIdentificador());
                final var temp = (CS_Processamento) task.getLocalProcessamento();
                if (temp != null) {
                    final Double uso =
                        (temp.getPoderComputacional() / this.poderComputacionalTotal) * 100;
                    for (var j = 0; j < task.getTempoInicial().size(); j++) {
                        tmp_series.add(task.getTempoInicial().get(j), (Double) 0.0);
                        tmp_series.add(task.getTempoInicial().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), (Double) 0.0);
                    }
                    dadosGrafico.addSeries(tmp_series);
                }
            }
        }

        return ChartFactory.createXYAreaChart("Use of total computing power through time "
                                              + "\nTasks", //Titulo
                                              "Time (seconds)", // Eixo X
                                              "Rate of total use of computing power (%)", //Eixo Y
                                              dadosGrafico, // Dados para o grafico
                                              PlotOrientation.VERTICAL, //Orientacao do grafico
                                              true, true, false
        );
    }

    private JFreeChart[] gerarGraficoProcessamentoTempoUser (
        final Collection<? extends Tarefa> tarefas, final RedeDeFilas rdf
    ) {
        final List<UserOperationTime> lista           = new ArrayList<>();
        final var                     numberUsers     = rdf.getUsuarios().size();
        final Map<String, Integer>    users           = new HashMap<>();
        final var                     tmp_series      = new XYSeries[numberUsers];
        final var                     tmp_series1     = new XYSeries[numberUsers];
        final var                     utilizacaoUser  = new Double[numberUsers];
        final var                     utilizacaoUser1 = new Double[numberUsers];
        final var                     dadosGrafico    = new XYSeriesCollection();
        final var                     dadosGrafico1   = new XYSeriesCollection();
        for (var i = 0; i < numberUsers; i++) {
            users.put(rdf.getUsuarios().get(i), i);
            utilizacaoUser[i]  = 0.0;
            tmp_series[i]      = new XYSeries(rdf.getUsuarios().get(i));
            utilizacaoUser1[i] = 0.0;
            tmp_series1[i]     = new XYSeries(rdf.getUsuarios().get(i));
        }
        if (!tarefas.isEmpty()) {
            //Insere cada tarefa como dois pontos na lista
            for (final Tarefa task : tarefas) {
                final var local = (CS_Processamento) task.getLocalProcessamento();
                if (local != null) {
                    for (var i = 0; i < task.getTempoInicial().size(); i++) {
                        final Double uso = (
                                               task
                                                   .getHistoricoProcessamento()
                                                   .get(i)
                                                   .getPoderComputacional() /
                                               this.poderComputacionalTotal
                                           ) * 100;
                        final var provisorio1 =
                            new UserOperationTime(task.getTempoInicial().get(i), true, uso,
                                                  users.get(task.getProprietario())
                            );
                        lista.add(provisorio1);
                        final var provisorio2 =
                            new UserOperationTime(task.getTempoFinal().get(i), false, uso,
                                                  users.get(task.getProprietario())
                            );
                        lista.add(provisorio2);
                    }
                }
            }
            //Ordena lista
            Collections.sort(lista);
        }
        for (final var UserOperationTime : lista) {
            final var usuario = UserOperationTime.getUserId();
            //Altera os valores do usuario atual e todos acima dele
            for (var j = usuario; j < numberUsers; j++) {
                //Salva valores anteriores
                tmp_series[j].add(UserOperationTime.getTime(), utilizacaoUser[j]);
                if (UserOperationTime.isStartTime()) {
                    utilizacaoUser[j] += UserOperationTime.getNodeUse();
                } else {
                    utilizacaoUser[j] -= UserOperationTime.getNodeUse();
                }
                //Novo valor
                tmp_series[j].add(UserOperationTime.getTime(), utilizacaoUser[j]);
            }
            //Grafico1
            tmp_series1[usuario].add(UserOperationTime.getTime(), utilizacaoUser1[usuario]);
            if (UserOperationTime.isStartTime()) {
                utilizacaoUser1[usuario] += UserOperationTime.getNodeUse();
            } else {
                utilizacaoUser1[usuario] -= UserOperationTime.getNodeUse();
            }
            tmp_series1[usuario].add(UserOperationTime.getTime(), utilizacaoUser1[usuario]);
        }
        for (var i = 0; i < numberUsers; i++) {
            dadosGrafico.addSeries(tmp_series[i]);
            dadosGrafico1.addSeries(tmp_series1[i]);
        }
        final var saida = new JFreeChart[2];
        saida[0] =
            ChartFactory.createXYAreaChart(
                "Use of total computing power through time" + "\nUsers",
                //Titulo
                "Time (seconds)",
                // Eixo X
                "Rate of total use of computing power (%)",
                //Eixo Y
                dadosGrafico1,
                // Dados para o grafico
                PlotOrientation.VERTICAL,
                //Orientacao do grafico
                true,
                true,
                false
            ); // exibir: legendas, tooltips, url

        saida[1] =
            ChartFactory.createXYLineChart(
                "Use of total computing power through time" + "\nUsers",
                //Titulo
                "Time (seconds)",
                // Eixo X
                "Rate of total use of computing power (%)",
                //Eixo Y
                dadosGrafico,
                // Dados para o grafico
                PlotOrientation.VERTICAL,
                //Orientacao do grafico
                true,
                true,
                false
            ); // exibir: legendas, tooltips, url
        final var xyplot = (XYPlot) saida[1].getPlot();
        xyplot.setDomainPannable(true);
        final AbstractXYItemRenderer xysteparearenderer = new XYStepAreaRenderer(2);
        xysteparearenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xysteparearenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        xysteparearenderer.setDefaultEntityRadius(6);
        xyplot.setRenderer(xysteparearenderer);

        return saida;
    }

    private URL getResource (final String name) {
        return this.getClass().getResource(name);
    }

    private void jButtonSalvarActionPerformed (final ActionEvent evt) {
        final var jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final var returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final var file = jFileChooser.getSelectedFile();
            this.salvarHTML(file);
            try {
                HtmlPane.openDefaultBrowser(new URL("file://"
                                                    + file.getAbsolutePath()
                                                    + "/result.html"));
            } catch (final MalformedURLException ex) {
                Logger.getLogger(CloudResultsDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void jButtonSalvarTracesActionPerformed (final ActionEvent evt) {
        final var filtro =
            new MultipleExtensionFileFilter("Workload Model of Simulation",
                                            FileExtensions.WORKLOAD_MODEL, true);
        final var jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(filtro);
        final var returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            var file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(FileExtensions.WORKLOAD_MODEL)) {
                file = new File(file + FileExtensions.WORKLOAD_MODEL);
            }
            TaskTraceSerializer.outputTaskTraceToFile(this.tarefas, file);
        }
    }

    private void jButtonPBarraActionPerformed (final ActionEvent evt) {
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
    }

    private void jButtonPPizzaActionPerformed (final ActionEvent evt) {
        this.jScrollPaneProcessamento.setViewportView(this.graficoPizzaProcessamento);
    }

    private void jButtonCBarraActionPerformed (final ActionEvent evt) {
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
    }

    private void jButtonCPizzaActionPerformed (final ActionEvent evt) {
        this.jScrollPaneComunicacao.setViewportView(this.graficoPizzaComunicacao);
    }

    private void jButtonProcessamentoUserActionPerformed (final ActionEvent evt) {
        if (this.jScrollPaneProcessamentoTempo.getViewport().getView()
            != this.graficoProcessamentoTempoUser1) {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser1);
        } else {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser2);
        }
    }

    private void jButtonProcessamentoMaquinaActionPerformed (final ActionEvent evt) {
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);
    }

    private void jButtonProcessamentoTarefaActionPerformed (final ActionEvent evt) {
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoTarefa);
    }

    private void jButtonABarraActionPerformed (final ActionEvent evt) {
        this.jScrollPaneAlocacao.setViewportView(this.graficoBarraAlocacao);
    }

    private void jButtonAPizzaActionPerformed (final ActionEvent evt) {
        this.jScrollPaneAlocacao.setViewportView(this.graficoPizzaAlocacao);
    }

    private void jButtonTotalActionPerformed (final ActionEvent evt) {
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoTotal);
    }

    private void jButtonDiscActionPerformed (final ActionEvent evt) {
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoDisco);
    }

    private void jButtonMemActionPerformed (final ActionEvent evt) {
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoMem);
    }

    private void jButtonProcActionPerformed (final ActionEvent evt) {
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoProc);
    }

    /**
     * Salva resultados obtidos em um arquivo html
     *
     * @param file
     *     diretório destino
     */
    private void salvarHTML (final File file) {
        this.html.setTabela(this.tabelaRecurso);

        final var finalImages = Stream.of(
                this.graficoBarraProcessamento,
                this.graficoPizzaProcessamento,
                this.graficoBarraComunicacao,
                this.graficoPizzaComunicacao,
                this.graficoProcessamentoTempo,
                this.graficoProcessamentoTempoTarefa,
                this.graficoProcessamentoTempoUser1,
                this.graficoProcessamentoTempoUser2
            )
            .filter(Objects::nonNull)
            .map(chart -> chart
                .getChart()
                .createBufferedImage(
                    BUFFERED_IMAGE_WIDTH,
                    BUFFERED_IMAGE_HEIGHT
                ))
            .toArray(BufferedImage[]::new);

        this.html.setCharts(finalImages);
        try {
            this.html.gerarHTML(file);
        } catch (final IOException ex) {
            Logger.getLogger(CloudResultsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}