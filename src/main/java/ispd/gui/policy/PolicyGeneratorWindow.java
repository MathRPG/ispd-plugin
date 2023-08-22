package ispd.gui.policy;

import static ispd.gui.TextSupplier.*;
import static ispd.gui.utils.ButtonBuilder.*;

import ispd.arquivo.interpretador.gerador.*;
import ispd.gui.utils.fonts.*;
import ispd.policy.*;
import ispd.utils.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.List;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

public class PolicyGeneratorWindow extends JDialog {

    private static final Dimension MAXIMUM_BUTTON_SIZE = new Dimension(37, 50);

    private static final int START = 0;

    private static final int VARIABLE = 1;

    private static final int OPERATOR = 2;

    private static final int OPEN_BRACKET = 3;

    private static final int CLOSE_BRACKET = 4;

    private static final Color FOREGROUND_RED = new Color(204, 0, 0);

    private static final Dimension MINIMUM_BUTTON_SIZE = new Dimension(37, 23);

    private static final Dimension PANEL_PREFERRED_SIZE = new Dimension(600, 350);

    private final String path;

    private final LinkedList<String> tFormula = new SpacedPrintList();

    private final LinkedList<String> rFormula = new SpacedPrintList();

    private final JScrollPane jScrollPanePrincipal = new JScrollPane();

    private final PolicyManager manager;

    private int currentStep = 1;

    private String ordering = "Random";

    private String tOrdering = "Random";

    private String rOrdering = "Random";

    private LinkedList<String> formula = this.tFormula;

    private int buttonType = START;

    private int tButtonType = START;

    private int rButtonType = START;

    private int parentAccount = 0;

    private int tParentAccount = 0;

    private int rParentAccount = 0;

    private InterpretadorGerador parse = null;

    private JButton buttonFinish;

    private JButton buttonNext;

    private JButton buttonPrevious;

    private JFormattedTextField jFormattedTextFieldP2Tempo;

    private JFormattedTextField jFormattedTextP4DigitaConst;

    private JFormattedTextField jFormattedTextP5DigitaConst;

    private JLabel jLabelP1Informacao;

    private JLabel jLabelP2Forma;

    private JLabel jLabelP6_1;

    private JLabel jLabelP6_2;

    private JLabel jLabelPasso1;

    private JLabel jLabelPasso2;

    private JLabel jLabelPasso3;

    private JLabel jLabelPasso4;

    private JLabel jLabelPasso5;

    private JLabel jLabelPasso6;

    private JLabel jLabelPasso7;

    private JList<String> jListRecurso;

    private JList<String> jListTarefa;

    private JRadioButton jOpAvancada;

    private JRadioButton jOpSimples;

    private JPanel jPanelPasso1;

    private JPanel jPanelPasso2;

    private JPanel jPanelPasso3;

    private JPanel jPanelPasso4;

    private JPanel jPanelPasso5;

    private JPanel jPanelPasso6;

    private JPanel jPanelPasso7;

    private JPanel jPanelPassoSimples;

    private JRadioButton jRadioButtonP2Centralizada;

    private JRadioButton jRadioButtonP2Chegada;

    private JRadioButton jRadioButtonP2Dinamica;

    private JRadioButton jRadioButtonP2Distribuida;

    private JRadioButton jRadioButtonP2Estatica;

    private JRadioButton jRadioButtonP2Saida;

    private JRadioButton jRadioButtonP2Tempo;

    private JRadioButton jRadioButtonP2concluida;

    private JRadioButton jRadioButtonP4Crescente;

    private JRadioButton jRadioButtonP4Decrescente;

    private JRadioButton jRadioButtonP4FIFO;

    private JRadioButton jRadioButtonP4Random;

    private JRadioButton jRadioButtonP5Crescente;

    private JRadioButton jRadioButtonP5Decrescente;

    private JRadioButton jRadioButtonP5FIFO;

    private JRadioButton jRadioButtonP5Random;

    private JRadioButton jRadioButtonP6PorRecurso;

    private JRadioButton jRadioButtonP6PorUsuario;

    private JRadioButton jRadioButtonP6SemRestricao;

    private JTextField jTextFieldP1LocalArq;

    private JTextField jTextFieldP1NomeEsc;

    private JTextField jTextFieldP4Formula;

    private JTextField jTextFieldP5Formula;

    private JFormattedTextField jTextFieldP6Num;

    private JTextPane jTextPaneP7Gramatica;

    public PolicyGeneratorWindow (
        final Frame parent, final boolean modal, final String path,
        final PolicyManager manager
    ) {
        super(parent, modal);
        this.path       = path;
        this.initComponents();
        this.manager = manager;
        this.jScrollPanePrincipal.setViewportView(this.jPanelPasso1);
        this.startStepOne();
    }

    private void initComponents () {
        this.initStepOneComponents();
        this.initStepTwoComponents();
        this.initStepThreeComponents();

        this.jPanelPasso4 = new JPanel();
        this.jPanelPasso4.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Advanced") + " - " + getText("Tasks distribution order"),
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            )
        );
        this.jPanelPasso4.setPreferredSize(PANEL_PREFERRED_SIZE);

        final JLabel jLabelP4Formula = new JLabel();
        jLabelP4Formula.setFont(ComicSansMS.PLAIN);

        jLabelP4Formula.setText(getText("Formula:"));

        this.jTextFieldP4Formula = new JTextField();
        this.jTextFieldP4Formula.setEditable(false);
        this.jTextFieldP4Formula.setFont(Verdana.BOLD);
        this.jTextFieldP4Formula.setText("Random");

        final JPanel jPanel1 = new JPanel();
        jPanel1.setBorder(BorderFactory.createTitledBorder(getText("Operators and precedence")));

        final var button11 = basicButton("+", this::jButtonP4AddActionPerformed);
        button11.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        final JButton jButtonP4Add = button11;
        jButtonP4Add.setMinimumSize(MINIMUM_BUTTON_SIZE);

        final var button10 = basicButton("-", this::jButtonP4SubActionPerformed);
        button10.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        final JButton jButtonP4Sub = button10;

        final var button9 = basicButton("(", this::jButtonP4AbreParentActionPerformed);
        button9.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        final JButton jButtonP4AbreParent = button9;

        final var button8 = basicButton(")", this::jButtonP4FechaParentActionPerformed);
        button8.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        final JButton jButtonP4FechaParent = button8;

        final var button7 = basicButton("/", this::jButtonP4DivActionPerformed);
        button7.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        final JButton jButtonP4Div = button7;

        final var button6 = basicButton("*", this::jButtonP4MultActionPerformed);
        button6.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        final JButton jButtonP4Mult = button6;
        jButtonP4Mult.setMinimumSize(MINIMUM_BUTTON_SIZE);

        final JButton jButtonP4Voltar = basicButton("←", this::jButtonP4VoltarActionPerformed);

        final GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanel1Layout.createParallelGroup(
                                      GroupLayout.Alignment.TRAILING)
                                            .addComponent(jButtonP4AbreParent,
                                                          GroupLayout.Alignment.LEADING
                                                , GroupLayout.DEFAULT_SIZE,
                                                          50, Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4Add,
                                                          GroupLayout.Alignment.LEADING
                                                , GroupLayout.DEFAULT_SIZE,
                                                          50, Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4Mult,
                                                          GroupLayout.Alignment.LEADING
                                                , GroupLayout.DEFAULT_SIZE,
                                                          50, Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4Div,
                                                          GroupLayout.Alignment.LEADING
                                                , GroupLayout.DEFAULT_SIZE,
                                                          50, Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4Sub,
                                                          GroupLayout.Alignment.LEADING
                                                , GroupLayout.DEFAULT_SIZE,
                                                          50, Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4FechaParent,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 50, Short.MAX_VALUE
                                            ))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(jButtonP4Voltar,
                                            GroupLayout.PREFERRED_SIZE, 52,
                                            GroupLayout.PREFERRED_SIZE
                              )
                              .addGap(28, 28, 28))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanel1Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING, false)
                                            .addGroup(
                                                jPanel1Layout.createSequentialGroup()
                                                    .addComponent(
                                                        jButtonP4Add,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        25,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        jButtonP4Sub,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        25,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        jButtonP4Mult,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addGap(8, 8, 8)
                                                    .addComponent(
                                                        jButtonP4Div,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addGap(8, 8, 8)
                                                    .addComponent(
                                                        jButtonP4AbreParent,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addComponent(
                                                        jButtonP4FechaParent,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    ))
                                            .addComponent(
                                                jButtonP4Voltar,
                                                GroupLayout.PREFERRED_SIZE,
                                                177,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addContainerGap(27, Short.MAX_VALUE))
        );

        final JPanel jPanel2 = new JPanel();
        jPanel2.setBorder(BorderFactory.createTitledBorder(getText("Variables")));

        final JButton jButtonP4TComputacao = aButton(
            getText("Computational size") + " - TCP",
            this::jButtonP4TComputacaoActionPerformed
        )
            .withToolTip(getText("Computational size"))
            .build();

        final JButton jButtonP4TComunicacao = aButton(
            getText("Communication size") + " - TC",
            this::jButtonP4TComunicacaoActionPerformed
        )
            .withToolTip(getText("Communication size"))
            .build();

        final JButton jButtonP4NTSubmetidas = aButton(
            getText("Number of submitted tasks") + " - NTS",
            this::jButtonP4NTSubmetidasActionPerformed
        )
            .withToolTip(getText("Number of submitted tasks by the user"))
            .build();

        final JButton jButtonP4NTConcluidas = aButton(
            getText("Number of completed tasks") + " - NTC",
            this::jButtonP4NTConcluidasActionPerformed
        )
            .withToolTip("Número de tarefas concluídas do usuário")
            .build();

        final JButton jButtonP4PComputUser = aButton(
            getText("User's computational power") + " - PCU",
            this::jButtonP4PComputUserActionPerformed
        )
            .withToolTip(getText("Computational power given by the user to grid"))
            .build();

        final JButton jButtonP4Const = aButton("Const", this::jButtonP4ConstActionPerformed)
            .withToolTip(getText("Numerical constant"))
            .build();

        this.jFormattedTextP4DigitaConst = new JFormattedTextField();
        this.jFormattedTextP4DigitaConst.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
        this.jFormattedTextP4DigitaConst.setText("1");

        final JButton jButtonP4PTempoCriacao =
            aButton(
                getText("Task creation time") + " - TCR",
                this::jButtonP4PTempoCriacaoActionPerformed
            )
                .withToolTip(getText("Task creation time"))
                .build();

        final GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                              .addGroup(jPanel2Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(jButtonP4TComputacao,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4TComunicacao,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4NTSubmetidas,
                                                          GroupLayout.Alignment.TRAILING,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4PComputUser,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP4NTConcluidas,
                                                          GroupLayout.Alignment.TRAILING,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addGroup(
                                                jPanel2Layout.createSequentialGroup()
                                                    .addComponent(
                                                        jButtonP4Const,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        184,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        this.jFormattedTextP4DigitaConst,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        81,
                                                        Short.MAX_VALUE
                                                    ))
                                            .addComponent(jButtonP4PTempoCriacao,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 271, Short.MAX_VALUE
                                            ))
                              .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                              .addComponent(jButtonP4TComputacao)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP4TComunicacao)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP4NTSubmetidas)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP4NTConcluidas)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP4PComputUser)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP4PTempoCriacao)
                              .addPreferredGap(
                                  LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                              .addGroup(jPanel2Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(jButtonP4Const)
                                            .addComponent(
                                                this.jFormattedTextP4DigitaConst,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addContainerGap())
        );

        final JPanel jPanel3 = new JPanel();
        jPanel3.setBorder(BorderFactory.createTitledBorder(getText("Order")));

        this.jRadioButtonP4Crescente = new JRadioButton();
        this.jRadioButtonP4Crescente.setText(getText("Crescent"));

        this.jRadioButtonP4Crescente.setToolTipText(getText(
            "This option schedules by the generated formula in crescent order"));

        this.jRadioButtonP4Crescente.addActionListener(this::jRadioButtonP4CrescenteActionPerformed);

        this.jRadioButtonP4Decrescente = new JRadioButton();
        this.jRadioButtonP4Decrescente.setText(getText("Decrescent"));

        this.jRadioButtonP4Decrescente.setToolTipText(getText(
            "This option schedules by the generated formula in decrescent order"));

        this.jRadioButtonP4Decrescente.addActionListener(this::jRadioButtonP4DecrescenteActionPerformed);

        this.jRadioButtonP4Random = new JRadioButton();
        this.jRadioButtonP4Random.setSelected(true);
        this.jRadioButtonP4Random.setText(getText("Random"));

        this.jRadioButtonP4Random.setToolTipText(getText("This option schedules randomly"));
        this.jRadioButtonP4Random.addActionListener(this::jRadioButtonP4RandomActionPerformed);

        this.jRadioButtonP4FIFO = new JRadioButton();
        this.jRadioButtonP4FIFO.setText("FIFO");
        this.jRadioButtonP4FIFO.addActionListener(this::jRadioButtonP4FIFOActionPerformed);

        final GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanel3Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(this.jRadioButtonP4Crescente)
                                            .addComponent(this.jRadioButtonP4Decrescente)
                                            .addComponent(this.jRadioButtonP4Random)
                                            .addComponent(this.jRadioButtonP4FIFO))
                              .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                )
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jRadioButtonP4Crescente)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jRadioButtonP4Decrescente)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(this.jRadioButtonP4Random)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jRadioButtonP4FIFO)
                              .addContainerGap(113, Short.MAX_VALUE))
        );

        final GroupLayout jPanelPasso4Layout = new GroupLayout(this.jPanelPasso4);
        this.jPanelPasso4.setLayout(jPanelPasso4Layout);
        jPanelPasso4Layout.setHorizontalGroup(
            jPanelPasso4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    jPanelPasso4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPasso4Layout.createParallelGroup(
                                GroupLayout.Alignment.LEADING)
                                      .addGroup(jPanelPasso4Layout
                                                    .createSequentialGroup()
                                                    .addComponent(
                                                        jLabelP4Formula)
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        this.jTextFieldP4Formula,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        523,
                                                        Short.MAX_VALUE
                                                    ))
                                      .addGroup(
                                          GroupLayout.Alignment.TRAILING,
                                          jPanelPasso4Layout
                                              .createSequentialGroup()
                                              .addComponent(
                                                  jPanel2,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  Short.MAX_VALUE
                                              )
                                              .addGap(2, 2, 2)
                                              .addComponent(
                                                  jPanel1,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE
                                              )
                                              .addPreferredGap(
                                                  LayoutStyle.ComponentPlacement.UNRELATED)
                                              .addComponent(
                                                  jPanel3,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE
                                              )
                                      ))
                        .addContainerGap()
                )
        );
        jPanelPasso4Layout.setVerticalGroup(
            jPanelPasso4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso4Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanelPasso4Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabelP4Formula)
                                            .addComponent(
                                                this.jTextFieldP4Formula,
                                                GroupLayout.PREFERRED_SIZE,
                                                27,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(jPanelPasso4Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING,
                                      false
                                  )
                                            .addComponent(jPanel3,
                                                          GroupLayout.DEFAULT_SIZE
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jPanel1,
                                                          GroupLayout.DEFAULT_SIZE
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jPanel2,
                                                          GroupLayout.DEFAULT_SIZE
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            ))
                              .addContainerGap(36, Short.MAX_VALUE))
        );

        this.jPanelPasso5 = new JPanel();
        this.jPanelPasso5.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Advanced") + " - " +
                getText("Resource aloccation order"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));
        this.jPanelPasso5.setPreferredSize(PANEL_PREFERRED_SIZE);

        final JLabel jLabelP5Formula = new JLabel();
        jLabelP5Formula.setFont(ComicSansMS.PLAIN);

        jLabelP5Formula.setText(getText("Formula:"));

        this.jTextFieldP5Formula = new JTextField();
        this.jTextFieldP5Formula.setEditable(false);
        this.jTextFieldP5Formula.setFont(Verdana.BOLD);
        this.jTextFieldP5Formula.setText("Random");

        final JPanel jPanel4 = new JPanel();
        jPanel4.setBorder(BorderFactory.createTitledBorder(getText("Operators and precedence")));

        final var button5 = basicButton("+", this::jButtonP5AddActionPerformed);
        button5.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        button5.setMinimumSize(MINIMUM_BUTTON_SIZE);

        final var button4 = basicButton("-", this::jButtonP5SubActionPerformed);
        button4.setMaximumSize(MAXIMUM_BUTTON_SIZE);

        final var button3 = basicButton("(", this::jButtonP5AbreParentActionPerformed);
        button3.setMaximumSize(MAXIMUM_BUTTON_SIZE);

        final var button2 = basicButton(")", this::jButtonP5FechaParentActionPerformed);
        button2.setMaximumSize(MAXIMUM_BUTTON_SIZE);

        final var button1 = basicButton("/", this::jButtonP5DivActionPerformed);
        button1.setMaximumSize(MAXIMUM_BUTTON_SIZE);

        final var button = basicButton("*", this::jButtonP5MultActionPerformed);
        button.setMaximumSize(MAXIMUM_BUTTON_SIZE);
        button.setMinimumSize(MINIMUM_BUTTON_SIZE);

        final JButton jButtonP5Voltar = basicButton("←", this::jButtonP4VoltarActionPerformed);

        final GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanel4Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(button3,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 50, Short.MAX_VALUE
                                            )
                                            .addGroup(jPanel4Layout.createParallelGroup(
                                                    GroupLayout.Alignment.TRAILING,
                                                    false
                                                )
                                                          .addComponent(
                                                              button4,
                                                              GroupLayout.Alignment.LEADING,
                                                              GroupLayout.DEFAULT_SIZE,
                                                              GroupLayout.DEFAULT_SIZE,
                                                              Short.MAX_VALUE
                                                          )
                                                          .addComponent(
                                                              button5,
                                                              GroupLayout.Alignment.LEADING,
                                                              GroupLayout.DEFAULT_SIZE,
                                                              50,
                                                              Short.MAX_VALUE
                                                          ))
                                            .addComponent(button,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 50, Short.MAX_VALUE
                                            )
                                            .addComponent(button1,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 50, Short.MAX_VALUE
                                            )
                                            .addComponent(button2,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 50, Short.MAX_VALUE
                                            ))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP5Voltar,
                                            GroupLayout.PREFERRED_SIZE, 52,
                                            GroupLayout.PREFERRED_SIZE
                              )
                              .addGap(32, 32, 32))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanel4Layout.createParallelGroup(
                                      GroupLayout.Alignment.TRAILING,
                                      false
                                  )
                                            .addComponent(jButtonP5Voltar,
                                                          GroupLayout.Alignment.LEADING
                                                , GroupLayout.DEFAULT_SIZE,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            )
                                            .addGroup(
                                                GroupLayout.Alignment.LEADING,
                                                jPanel4Layout.createSequentialGroup()
                                                    .addComponent(
                                                        button5,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        25,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        button4,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        25,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        button,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        25,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        button1,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        25,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        button3,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        button2,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                            ))
                              .addContainerGap(28, Short.MAX_VALUE))
        );

        final JPanel jPanel5 = new JPanel();
        jPanel5.setBorder(BorderFactory.createTitledBorder(getText("Variables")));

        final JButton jButtonP5PProcessamento = aButton(
            getText("Processing power") + " - PP",
            this::jButtonP5PProcessamentoActionPerformed
        )
            .withToolTip(getText("Resource processing power"))
            .build();

        final JButton jButtonP5LinkComunicacao = aButton(
            getText("Communication link") + " - LC",
            this::jButtonP5LinkComunicacaoActionPerformed
        )
            .withToolTip(getText("Band of the communication link"))
            .build();

        final JButton jButtonP5TCompTarefa = aButton(
            getText("Task computational size") + " - TCT",
            this::jButtonP5TCompTarefaActionPerformed
        )
            .withToolTip(getText("Computational size of the submitted task"))
            .build();

        final JButton jButtonP5NumTExec = aButton(
            "%s - NTE".formatted(getText("Number of running taks")),
            this::jButtonP5NumTExecActionPerformed
        )
            .withToolTip(getText("Number of running tasks in the resource"))
            .build();

        final JButton jButtonP5TComunTarefa = aButton(
            getText("Task communication size") + " - TCMT",
            this::jButtonP5TComunTarefaActionPerformed
        )
            .withToolTip(getText("Commnication size of the submmited task"))
            .build();

        final JButton jButtonP5Const1 = aButton("Const", this::jButtonP5Const1ActionPerformed)
            .withToolTip(getText("Numerical constant"))
            .build();

        this.jFormattedTextP5DigitaConst = new JFormattedTextField();
        this.jFormattedTextP5DigitaConst.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
        this.jFormattedTextP5DigitaConst.setText("1");

        final JButton jButtonP5MflopExec = basicButton(
            getText("Running Mflops") + " - MFE",
            this::jButtonP5MflopExecActionPerformed
        );

        final GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                              .addGroup(jPanel5Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(jButtonP5PProcessamento
                                                ,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 271, Short.MAX_VALUE)
                                            .addComponent(
                                                jButtonP5LinkComunicacao,
                                                GroupLayout.DEFAULT_SIZE,
                                                271,
                                                Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP5NumTExec,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 271, Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP5TCompTarefa,
                                                          GroupLayout.Alignment.TRAILING,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(jButtonP5TComunTarefa,
                                                          GroupLayout.Alignment.TRAILING,
                                                          GroupLayout.DEFAULT_SIZE, 271,
                                                          Short.MAX_VALUE
                                            )
                                            .addGroup(
                                                jPanel5Layout.createSequentialGroup()
                                                    .addGap(2, 2, 2)
                                                    .addComponent(
                                                        jButtonP5Const1
                                                        ,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        190,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        this.jFormattedTextP5DigitaConst,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        73,
                                                        Short.MAX_VALUE
                                                    ))
                                            .addComponent(jButtonP5MflopExec,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 271, Short.MAX_VALUE
                                            ))
                              .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                              .addComponent(jButtonP5PProcessamento)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP5LinkComunicacao)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP5TCompTarefa)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP5TComunTarefa)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP5NumTExec)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jButtonP5MflopExec)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(jPanel5Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(jButtonP5Const1)
                                            .addComponent(
                                                this.jFormattedTextP5DigitaConst,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addContainerGap(18, Short.MAX_VALUE))
        );

        final JPanel jPanel6 = new JPanel();
        jPanel6.setBorder(BorderFactory.createTitledBorder(getText("Order")));

        this.jRadioButtonP5Crescente = new JRadioButton();
        this.jRadioButtonP5Crescente.setText(getText("Crescent"));

        this.jRadioButtonP5Crescente.setToolTipText(getText(
            "This option schedules by the generated formula in crescent order"));

        this.jRadioButtonP5Crescente.addActionListener(this::jRadioButtonP5CrescenteActionPerformed);

        this.jRadioButtonP5Decrescente = new JRadioButton();
        this.jRadioButtonP5Decrescente.setText(getText("Decrescent"));

        this.jRadioButtonP5Decrescente.setToolTipText(getText(
            "This option schedules by the generated formula in decrescent order"));

        this.jRadioButtonP5Decrescente.addActionListener(this::jRadioButtonP5DecrescenteActionPerformed);

        this.jRadioButtonP5Random = new JRadioButton();
        this.jRadioButtonP5Random.setSelected(true);
        this.jRadioButtonP5Random.setText(getText("Random"));

        this.jRadioButtonP5Random.setToolTipText(getText("This option schedules randomly"));
        this.jRadioButtonP5Random.addActionListener(this::jRadioButtonP5RandomActionPerformed);

        this.jRadioButtonP5FIFO = new JRadioButton();
        this.jRadioButtonP5FIFO.setText("FIFO");
        this.jRadioButtonP5FIFO.setToolTipText("");
        this.jRadioButtonP5FIFO.addActionListener(this::jRadioButtonP5FIFOActionPerformed);

        final GroupLayout jPanel6Layout =
            new GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanel6Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(this.jRadioButtonP5Crescente)
                                            .addComponent(this.jRadioButtonP5Random)
                                            .addComponent(this.jRadioButtonP5Decrescente)
                                            .addComponent(this.jRadioButtonP5FIFO))
                              .addContainerGap(
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE
                              ))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jRadioButtonP5Crescente)
                              .addGap(3, 3, 3)
                              .addComponent(this.jRadioButtonP5Decrescente)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jRadioButtonP5Random)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jRadioButtonP5FIFO)
                              .addContainerGap(113, Short.MAX_VALUE))
        );

        final GroupLayout jPanelPasso5Layout = new GroupLayout(this.jPanelPasso5);
        this.jPanelPasso5.setLayout(jPanelPasso5Layout);
        jPanelPasso5Layout.setHorizontalGroup(
            jPanelPasso5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso5Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanelPasso5Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelPasso5Layout
                                                          .createSequentialGroup()
                                                          .addComponent(
                                                              jLabelP5Formula)
                                                          .addPreferredGap(
                                                              LayoutStyle.ComponentPlacement.RELATED)
                                                          .addComponent(
                                                              this.jTextFieldP5Formula,
                                                              GroupLayout.DEFAULT_SIZE,
                                                              523,
                                                              Short.MAX_VALUE
                                                          ))
                                            .addGroup(
                                                GroupLayout.Alignment.TRAILING,
                                                jPanelPasso5Layout
                                                    .createSequentialGroup()
                                                    .addComponent(
                                                        jPanel5,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        jPanel4,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        jPanel6,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                            ))
                              .addContainerGap())
        );
        jPanelPasso5Layout.setVerticalGroup(
            jPanelPasso5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    jPanelPasso5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPasso5Layout.createParallelGroup(
                                GroupLayout.Alignment.BASELINE)
                                      .addComponent(jLabelP5Formula)
                                      .addComponent(
                                          this.jTextFieldP5Formula,
                                          GroupLayout.PREFERRED_SIZE,
                                          27,
                                          GroupLayout.PREFERRED_SIZE
                                      ))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelPasso5Layout.createParallelGroup(
                                GroupLayout.Alignment.LEADING,
                                false
                            )
                                      .addComponent(jPanel6,
                                                    GroupLayout.DEFAULT_SIZE
                                          ,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE
                                      )
                                      .addComponent(jPanel4,
                                                    GroupLayout.DEFAULT_SIZE
                                          ,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE
                                      )
                                      .addComponent(jPanel5,
                                                    GroupLayout.DEFAULT_SIZE
                                          ,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE
                                      ))
                        .addContainerGap(36, Short.MAX_VALUE)
                )
        );

        this.jPanelPasso5.getAccessibleContext().setAccessibleName("Ordem de alocação de recursos");

        this.jPanelPassoSimples = new JPanel();
        this.jPanelPassoSimples.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Simple") + " - " +
                getText("Scheduling options"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));

        final JPanel jPanel7 = new JPanel();
        jPanel7.setBorder(BorderFactory.createTitledBorder(
            new javax.swing.border.LineBorder(Color.BLACK, 1, true),
            getText("Resource Scheduler")
        ));

        this.jListRecurso = new JList<>();
        this.jListRecurso.setBorder(BorderFactory.createTitledBorder(getText(
            "Select the policy used:")));
        this.jListRecurso.setModel(new SimpleResourceModel());
        this.jListRecurso.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        this.jListRecurso.setSelectedIndex(0);
        final JScrollPane jScrollPane2 = new JScrollPane();
        jScrollPane2.setViewportView(this.jListRecurso);

        final GroupLayout jPanel7Layout =
            new GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(
                                  jScrollPane2,
                                  GroupLayout.DEFAULT_SIZE,
                                  280,
                                  Short.MAX_VALUE
                              )
                              .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                              .addGap(43, 43, 43)
                              .addComponent(
                                  jScrollPane2,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE
                              )
                              .addContainerGap(33, Short.MAX_VALUE))
        );

        final JPanel jPanel8 = new JPanel();
        jPanel8.setBorder(BorderFactory.createTitledBorder(
            new javax.swing.border.LineBorder(Color.BLACK, 1, true),
            getText("Task Scheduler")
        ));

        this.jListTarefa = new JList<>();
        this.jListTarefa.setBorder(BorderFactory.createTitledBorder(getText(
            "Select the policy used:")));
        this.jListTarefa.setModel(new SimpleTaskModel());
        this.jListTarefa.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        this.jListTarefa.setSelectedIndex(0);
        final JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(this.jListTarefa);

        final GroupLayout jPanel8Layout =
            new GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(
                                  jScrollPane1,
                                  GroupLayout.DEFAULT_SIZE,
                                  297,
                                  Short.MAX_VALUE
                              )
                              .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                              .addGap(43, 43, 43)
                              .addComponent(
                                  jScrollPane1,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE
                              )
                              .addContainerGap(33, Short.MAX_VALUE))
        );

        final GroupLayout jPanelPassoSimplesLayout =
            new GroupLayout(this.jPanelPassoSimples);
        this.jPanelPassoSimples.setLayout(jPanelPassoSimplesLayout);
        jPanelPassoSimplesLayout.setHorizontalGroup(
            jPanelPassoSimplesLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    jPanelPassoSimplesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(
                            jPanel8,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE
                        )
                        .addPreferredGap(
                            LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(
                            jPanel7,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addContainerGap()
                )
        );
        jPanelPassoSimplesLayout.setVerticalGroup(
            jPanelPassoSimplesLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    jPanelPassoSimplesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPassoSimplesLayout
                                      .createParallelGroup(
                                          GroupLayout.Alignment.TRAILING)
                                      .addComponent(
                                          jPanel7,
                                          GroupLayout.Alignment.LEADING,
                                          GroupLayout.DEFAULT_SIZE,
                                          GroupLayout.DEFAULT_SIZE,
                                          Short.MAX_VALUE
                                      )
                                      .addComponent(
                                          jPanel8,
                                          GroupLayout.Alignment.LEADING,
                                          GroupLayout.DEFAULT_SIZE,
                                          GroupLayout.DEFAULT_SIZE,
                                          Short.MAX_VALUE
                                      ))
                        .addContainerGap()
                )
        );

        this.jPanelPasso6 = new JPanel();
        this.jPanelPasso6.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Restrictions"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));

        final JSeparator jSeparatorP6 = new JSeparator();
        jSeparatorP6.setForeground(Color.BLACK);

        this.jRadioButtonP6SemRestricao = new JRadioButton();
        this.jRadioButtonP6SemRestricao.setSelected(true);
        this.jRadioButtonP6SemRestricao.setText(getText("No restrictions"));
        this.jRadioButtonP6SemRestricao.addActionListener(this::jRadioButtonP6SemRestricaoActionPerformed);

        this.jRadioButtonP6PorRecurso = new JRadioButton();
        this.jRadioButtonP6PorRecurso.setText(getText(
            "Limit the number of tasks submitted by resource"));
        this.jRadioButtonP6PorRecurso.addActionListener(this::jRadioButtonP6PorRecursoActionPerformed);

        this.jRadioButtonP6PorUsuario = new JRadioButton();
        this.jRadioButtonP6PorUsuario.setText(getText("Limit the number of tasks submitted by user"));
        this.jRadioButtonP6PorUsuario.addActionListener(this::jRadioButtonP6PorUsuarioActionPerformed);

        this.jLabelP6_1 = new JLabel();
        this.jLabelP6_1.setText(getText("The scheduler stop when there are more than"));

        this.jLabelP6_1.setEnabled(false);

        this.jTextFieldP6Num = new JFormattedTextField();
        this.jTextFieldP6Num.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(NumberFormat.getIntegerInstance())));
        this.jTextFieldP6Num.setText("1");
        this.jTextFieldP6Num.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.jTextFieldP6Num.setEnabled(false);

        this.jLabelP6_2 = new JLabel();
        this.jLabelP6_2.setText(getText("tasks in all resources."));
        this.jLabelP6_2.setEnabled(false);

        final GroupLayout jPanelPasso6Layout = new GroupLayout(this.jPanelPasso6);
        this.jPanelPasso6.setLayout(jPanelPasso6Layout);
        jPanelPasso6Layout.setHorizontalGroup(
            jPanelPasso6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jRadioButtonP6SemRestricao)
                              .addContainerGap(359, Short.MAX_VALUE))
                .addGroup(jPanelPasso6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jRadioButtonP6PorUsuario)
                              .addContainerGap(219, Short.MAX_VALUE))
                .addComponent(jSeparatorP6,
                              GroupLayout.Alignment.TRAILING,
                              GroupLayout.DEFAULT_SIZE, 460,
                              Short.MAX_VALUE
                )
                .addGroup(jPanelPasso6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jLabelP6_1)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jTextFieldP6Num,
                                            GroupLayout.PREFERRED_SIZE, 57,
                                            GroupLayout.PREFERRED_SIZE
                              )
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelP6_2)
                              .addContainerGap(62, Short.MAX_VALUE))
                .addGroup(jPanelPasso6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jRadioButtonP6PorRecurso)
                              .addContainerGap(199, Short.MAX_VALUE))
        );
        jPanelPasso6Layout.setVerticalGroup(
            jPanelPasso6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso6Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.jRadioButtonP6SemRestricao)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(this.jRadioButtonP6PorRecurso)
                              .addGap(4, 4, 4)
                              .addComponent(this.jRadioButtonP6PorUsuario)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(
                                  jSeparatorP6,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE
                              )
                              .addGap(18, 18, 18)
                              .addGroup(jPanelPasso6Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(this.jLabelP6_1)
                                            .addComponent(
                                                this.jTextFieldP6Num,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE
                                            )
                                            .addComponent(
                                                this.jLabelP6_2))
                              .addContainerGap(
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE
                              ))
        );

        this.jPanelPasso7 = new JPanel();
        this.jPanelPasso7.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Finish"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));

        this.jTextPaneP7Gramatica = new JTextPane();
        this.jTextPaneP7Gramatica.setFont(Tahoma.BOLD_LARGE);

        final JScrollPane jScrollPane3 = new JScrollPane();
        jScrollPane3.setViewportView(this.jTextPaneP7Gramatica);

        final GroupLayout jPanelPasso7Layout = new GroupLayout(this.jPanelPasso7);
        this.jPanelPasso7.setLayout(jPanelPasso7Layout);
        jPanelPasso7Layout.setHorizontalGroup(
            jPanelPasso7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso7Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(
                                  jScrollPane3,
                                  GroupLayout.DEFAULT_SIZE,
                                  458,
                                  Short.MAX_VALUE
                              )
                              .addContainerGap())
        );
        jPanelPasso7Layout.setVerticalGroup(
            jPanelPasso7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso7Layout.createSequentialGroup()
                              .addComponent(
                                  jScrollPane3,
                                  GroupLayout.DEFAULT_SIZE,
                                  233,
                                  Short.MAX_VALUE
                              )
                              .addContainerGap())
        );

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(getText("New Scheduler"));
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.setIconImage(
            Toolkit
                .getDefaultToolkit()
                .getImage(this.getClass().getResource("../imagens/Logo_iSPD_25.png")));
        this.setLocation(new Point(0, 0));

        final JPanel jPanelPassos = new JPanel();
        jPanelPassos.setBackground(Color.WHITE);
        jPanelPassos.setBorder(new javax.swing.border.MatteBorder(null));

        final JLabel jLabelPassos = new JLabel();
        jLabelPassos.setFont(ComicSansMS.BOLD);

        jLabelPassos.setText("<html><b>"
                             + getText("Steps")
                             + "<br"
                             + ">----------------</b></html>");

        this.jLabelPasso1 = new JLabel();
        this.jLabelPasso1.setText("1 - " + getText("Enter the name"));

        this.jLabelPasso2 = new JLabel();
        this.jLabelPasso2.setText("2 - " + getText("Characteristics"));
        this.jLabelPasso2.setEnabled(false);

        this.jLabelPasso3 = new JLabel();
        this.jLabelPasso3.setText("3 - " + getText("Generator type"));

        this.jLabelPasso3.setEnabled(false);

        this.jLabelPasso4 = new JLabel();
        this.jLabelPasso4.setText("4 - " + getText("Task Scheduler"));

        this.jLabelPasso4.setEnabled(false);

        this.jLabelPasso5 = new JLabel();
        this.jLabelPasso5.setText("5 - " + getText("Resource Scheduler"));
        this.jLabelPasso5.setEnabled(false);

        this.jLabelPasso6 = new JLabel();
        this.jLabelPasso6.setText("6 - " + getText("Restrictions"));

        this.jLabelPasso6.setEnabled(false);

        this.jLabelPasso7 = new JLabel();
        this.jLabelPasso7.setText("7 - " + getText("Finish"));

        this.jLabelPasso7.setEnabled(false);

        final GroupLayout jPanelPassosLayout = new GroupLayout(jPanelPassos);
        jPanelPassos.setLayout(jPanelPassosLayout);
        jPanelPassosLayout.setHorizontalGroup(
            jPanelPassosLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPassosLayout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanelPassosLayout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelPassos,
                                                          GroupLayout.DEFAULT_SIZE
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(
                                                this.jLabelPasso1)
                                            .addComponent(
                                                this.jLabelPasso2)
                                            .addComponent(
                                                this.jLabelPasso4)
                                            .addComponent(
                                                this.jLabelPasso5)
                                            .addComponent(
                                                this.jLabelPasso3)
                                            .addComponent(
                                                this.jLabelPasso6)
                                            .addComponent(
                                                this.jLabelPasso7))
                              .addContainerGap())
        );
        jPanelPassosLayout.setVerticalGroup(
            jPanelPassosLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPassosLayout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(jLabelPassos)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso1)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso2)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso3)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso4)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso5)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso6)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jLabelPasso7)
                              .addContainerGap(262, Short.MAX_VALUE))
        );

        final JPanel jPanelControle = new JPanel();
        jPanelControle.setBorder(BorderFactory.createEtchedBorder());

        this.buttonPrevious = aButton(
            "< %s".formatted(getText("Back")),
            this::onPreviousClick
        )
            .disabled()
            .build();

        this.buttonNext = aButton(
            "%s >".formatted(getText("Next")),
            this::onNextClick
        )
            .withActionCommand("%s >".formatted(getText("Next")))
            .build();

        this.buttonFinish = aButton(getText("Finish"), this::onFinishClick)
            .disabled()
            .build();

        final var cancel = basicButton(getText("Cancel"), this::onCancelClick);

        final GroupLayout jPanelControleLayout = new GroupLayout(jPanelControle);
        jPanelControle.setLayout(jPanelControleLayout);
        jPanelControleLayout.setHorizontalGroup(
            jPanelControleLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelControleLayout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(this.buttonPrevious)
                              .addPreferredGap(
                                  LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.buttonNext)
                              .addPreferredGap(
                                  LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.buttonFinish)
                              .addPreferredGap(
                                  LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(cancel)
                              .addContainerGap(380, Short.MAX_VALUE))
        );
        jPanelControleLayout.setVerticalGroup(
            jPanelControleLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelControleLayout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanelControleLayout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.buttonPrevious)
                                            .addComponent(
                                                this.buttonNext)
                                            .addComponent(
                                                this.buttonFinish)
                                            .addComponent(cancel))
                              .addContainerGap(
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE
                              ))
        );

        this.makeLayoutAndPack(jPanelPassos, jPanelControle);
    }

    private void initStepThreeComponents () {
        this.jPanelPasso3 = new JPanel();
        this.jPanelPasso3.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Generator type"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));

        this.jOpSimples = new JRadioButton();
        this.jOpSimples.setSelected(true);
        this.jOpSimples.setText(getText("Simple"));
        this.jOpSimples.addActionListener(this::jOpSimplesActionPerformed);

        this.jOpAvancada = new JRadioButton();
        this.jOpAvancada.setText(getText("Advanced"));
        this.jOpAvancada.addActionListener(this::jOpAvancadaActionPerformed);

        final JLabel jLabel3 = new JLabel();
        jLabel3.setText(getText("Select a option of scheduler generator:"));

        final JLabel jLabel4 = new JLabel();
        jLabel4.setText(getText("This option provides common standards of scheduling policies"));

        final JLabel jLabel5 = new JLabel();
        jLabel5.setText(getText(
            "This option allows to create scheduling policies through mathematical formulation"));

        final GroupLayout jPanelPasso3Layout = new GroupLayout(this.jPanelPasso3);
        this.jPanelPasso3.setLayout(jPanelPasso3Layout);
        jPanelPasso3Layout.setHorizontalGroup(
            jPanelPasso3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso3Layout.createSequentialGroup()
                              .addGap(19, 19, 19)
                              .addGroup(jPanelPasso3Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(this.jOpSimples)
                                            .addComponent(
                                                this.jOpAvancada)
                                            .addGroup(jPanelPasso3Layout
                                                          .createSequentialGroup()
                                                          .addGap(
                                                              21,
                                                              21,
                                                              21
                                                          )
                                                          .addComponent(
                                                              jLabel4))
                                            .addGroup(jPanelPasso3Layout
                                                          .createSequentialGroup()
                                                          .addGap(
                                                              21,
                                                              21,
                                                              21
                                                          )
                                                          .addComponent(
                                                              jLabel5)))
                              .addContainerGap(132, Short.MAX_VALUE))
        );
        jPanelPasso3Layout.setVerticalGroup(
            jPanelPasso3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso3Layout.createSequentialGroup()
                              .addGap(33, 33, 33)
                              .addComponent(jLabel3)
                              .addGap(18, 18, 18)
                              .addComponent(this.jOpSimples)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jLabel4)
                              .addGap(18, 18, 18)
                              .addComponent(this.jOpAvancada)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(jLabel5)
                              .addContainerGap(45, Short.MAX_VALUE))
        );
    }

    private void initStepTwoComponents () {
        this.jPanelPasso2 = new JPanel();
        this.jPanelPasso2.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Enter the characteristics"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));

        final JLabel jLabelP2Informacao = new JLabel();
        jLabelP2Informacao.setText(getText("Search for information:"));

        this.jRadioButtonP2Estatica = new JRadioButton();
        this.jRadioButtonP2Estatica.setText(getText("Static"));
        this.jRadioButtonP2Estatica.addActionListener(this::jRadioButtonP2EstaticaActionPerformed);
        this.jRadioButtonP2Estatica.setSelected(true);

        this.jRadioButtonP2Dinamica = new JRadioButton();
        this.jRadioButtonP2Dinamica.setText(getText("Dynamic"));
        this.jRadioButtonP2Dinamica.addActionListener(this::jRadioButtonP2DinamicaActionPerformed);

        this.jLabelP2Forma = new JLabel();
        this.jLabelP2Forma.setText(getText("How to refresh:"));
        this.jLabelP2Forma.setEnabled(false);

        this.jRadioButtonP2Tempo = new JRadioButton();
        this.jRadioButtonP2Tempo.setSelected(true);
        this.jRadioButtonP2Tempo.setText(getText("Time interval"));
        this.jRadioButtonP2Tempo.setToolTipText(getText(
            "Variables will be refreshed after the passing of specified time"));
        this.jRadioButtonP2Tempo.setEnabled(false);
        this.jRadioButtonP2Tempo.addActionListener(this::jRadioButtonP2TempoActionPerformed);

        this.jRadioButtonP2Chegada = new JRadioButton();
        this.jRadioButtonP2Chegada.setText(getText("Task arrival"));
        this.jRadioButtonP2Chegada.setEnabled(false);
        this.jRadioButtonP2Chegada.addActionListener(this::jRadioButtonP2ChegadaActionPerformed);

        this.jRadioButtonP2Saida = new JRadioButton();
        this.jRadioButtonP2Saida.setText(getText("Task output"));
        this.jRadioButtonP2Saida.setEnabled(false);
        this.jRadioButtonP2Saida.addActionListener(this::jRadioButtonP2SaidaActionPerformed);

        this.jFormattedTextFieldP2Tempo = new JFormattedTextField();
        this.jFormattedTextFieldP2Tempo.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
        this.jFormattedTextFieldP2Tempo.setText("1");
        this.jFormattedTextFieldP2Tempo.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.jFormattedTextFieldP2Tempo.setEnabled(false);

        final JLabel jLabelP2Topologia = new JLabel();
        jLabelP2Topologia.setText(getText("Topology:"));
        jLabelP2Topologia.setVisible(false);

        this.jRadioButtonP2Centralizada = new JRadioButton();
        this.jRadioButtonP2Centralizada.setSelected(true);
        this.jRadioButtonP2Centralizada.setText(getText("Centralized"));
        this.jRadioButtonP2Centralizada.addActionListener(this::jRadioButtonP2CentralizadaActionPerformed);
        this.jRadioButtonP2Centralizada.setVisible(false);

        this.jRadioButtonP2Distribuida = new JRadioButton();
        this.jRadioButtonP2Distribuida.setText(getText("Distributed"));
        this.jRadioButtonP2Distribuida.addActionListener(this::jRadioButtonP2DistribuidaActionPerformed);
        this.jRadioButtonP2Distribuida.setVisible(false);

        this.jRadioButtonP2concluida = new JRadioButton();
        this.jRadioButtonP2concluida.setText(getText("Task completed"));
        this.jRadioButtonP2concluida.setEnabled(false);
        this.jRadioButtonP2concluida.addActionListener(this::jRadioButtonP2concluidaActionPerformed);

        final GroupLayout jPanelPasso2Layout = new GroupLayout(this.jPanelPasso2);
        this.jPanelPasso2.setLayout(jPanelPasso2Layout);
        jPanelPasso2Layout.setHorizontalGroup(
            jPanelPasso2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso2Layout.createSequentialGroup()
                              .addGroup(jPanelPasso2Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelPasso2Layout
                                                          .createSequentialGroup()
                                                          .addGroup(
                                                              jPanelPasso2Layout
                                                                  .createParallelGroup(
                                                                      GroupLayout.Alignment.LEADING)
                                                                  .addGroup(
                                                                      jPanelPasso2Layout
                                                                          .createSequentialGroup()
                                                                          .addContainerGap()
                                                                          .addComponent(
                                                                              jLabelP2Informacao))
                                                                  .addGroup(
                                                                      jPanelPasso2Layout
                                                                          .createSequentialGroup()
                                                                          .addGap(
                                                                              26,
                                                                              26,
                                                                              26
                                                                          )
                                                                          .addGroup(
                                                                              jPanelPasso2Layout
                                                                                  .createParallelGroup(
                                                                                      GroupLayout.Alignment.LEADING)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Estatica)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Dinamica)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Tempo)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Chegada)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Saida)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2concluida))
                                                                          .addPreferredGap(
                                                                              LayoutStyle.ComponentPlacement.RELATED)))
                                                          .addGap(
                                                              81,
                                                              81,
                                                              81
                                                          )
                                                          .addGroup(
                                                              jPanelPasso2Layout
                                                                  .createParallelGroup(
                                                                      GroupLayout.Alignment.LEADING)
                                                                  .addComponent(
                                                                      this.jFormattedTextFieldP2Tempo,
                                                                      GroupLayout.PREFERRED_SIZE,
                                                                      57,
                                                                      GroupLayout.PREFERRED_SIZE
                                                                  )
                                                                  .addComponent(
                                                                      jLabelP2Topologia)
                                                                  .addGroup(
                                                                      jPanelPasso2Layout
                                                                          .createSequentialGroup()
                                                                          .addGap(
                                                                              10,
                                                                              10,
                                                                              10
                                                                          )
                                                                          .addGroup(
                                                                              jPanelPasso2Layout
                                                                                  .createParallelGroup(
                                                                                      GroupLayout.Alignment.LEADING)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Distribuida)
                                                                                  .addComponent(
                                                                                      this.jRadioButtonP2Centralizada)))))
                                            .addGroup(jPanelPasso2Layout
                                                          .createSequentialGroup()
                                                          .addContainerGap()
                                                          .addComponent(
                                                              this.jLabelP2Forma)))
                              .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanelPasso2Layout.setVerticalGroup(
            jPanelPasso2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso2Layout.createSequentialGroup()
                              .addGroup(jPanelPasso2Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelPasso2Layout
                                                          .createSequentialGroup()
                                                          .addGap(
                                                              32,
                                                              32,
                                                              32
                                                          )
                                                          .addComponent(
                                                              this.jRadioButtonP2Estatica)
                                                          .addGap(
                                                              0,
                                                              0,
                                                              0
                                                          )
                                                          .addComponent(
                                                              this.jRadioButtonP2Dinamica))
                                            .addGroup(jPanelPasso2Layout
                                                          .createSequentialGroup()
                                                          .addContainerGap()
                                                          .addGroup(
                                                              jPanelPasso2Layout
                                                                  .createParallelGroup(
                                                                      GroupLayout.Alignment.BASELINE)
                                                                  .addComponent(
                                                                      jLabelP2Informacao)
                                                                  .addComponent(
                                                                      jLabelP2Topologia))
                                                          .addPreferredGap(
                                                              LayoutStyle.ComponentPlacement.RELATED)
                                                          .addComponent(
                                                              this.jRadioButtonP2Centralizada)
                                                          .addPreferredGap(
                                                              LayoutStyle.ComponentPlacement.UNRELATED)
                                                          .addComponent(
                                                              this.jRadioButtonP2Distribuida)))
                              .addGap(18, 18, 18)
                              .addComponent(this.jLabelP2Forma)
                              .addGap(10, 10, 10)
                              .addGroup(jPanelPasso2Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.jRadioButtonP2Tempo)
                                            .addComponent(
                                                this.jFormattedTextFieldP2Tempo,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(this.jRadioButtonP2Chegada)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(this.jRadioButtonP2Saida)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(this.jRadioButtonP2concluida)
                              .addContainerGap(
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE
                              ))
        );
    }

    private void initStepOneComponents () {
        this.jPanelPasso1 = new JPanel();
        this.jPanelPasso1.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                getText("Enter the name of the scheduler"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                ComicSansMS.BOLD
            ));

        final JLabel jLabelP1NomeEsc = new JLabel();
        jLabelP1NomeEsc.setFont(ComicSansMS.PLAIN);
        jLabelP1NomeEsc.setText(getText("Scheduler name"));

        this.jTextFieldP1NomeEsc = new JTextField();
        this.jTextFieldP1NomeEsc.setText("NewScheduler");
        this.jTextFieldP1NomeEsc.addKeyListener(new SchedulerNameKeyAdapter());

        final JLabel jLabelP1LocalArq = new JLabel();
        jLabelP1LocalArq.setFont(ComicSansMS.PLAIN);
        jLabelP1LocalArq.setText(getText("File"));

        this.jTextFieldP1LocalArq = new JTextField();
        this.jTextFieldP1LocalArq.setEditable(false);
        this.jTextFieldP1LocalArq.setText("%sNewScheduler.java".formatted(this.path));

        final JSeparator jSeparatorP1 = new JSeparator();
        jSeparatorP1.setForeground(Color.BLACK);

        this.jLabelP1Informacao = new JLabel();
        this.jLabelP1Informacao.setForeground(FOREGROUND_RED);

        final GroupLayout jPanelPasso1Layout =
            new GroupLayout(this.jPanelPasso1);
        this.jPanelPasso1.setLayout(jPanelPasso1Layout);
        jPanelPasso1Layout.setHorizontalGroup(
            jPanelPasso1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso1Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanelPasso1Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelP1NomeEsc)
                                            .addComponent(
                                                jLabelP1LocalArq))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(jPanelPasso1Layout.createParallelGroup(
                                      GroupLayout.Alignment.LEADING)
                                            .addComponent(
                                                this.jTextFieldP1LocalArq,
                                                GroupLayout.DEFAULT_SIZE
                                                ,
                                                381,
                                                Short.MAX_VALUE
                                            )
                                            .addComponent(
                                                this.jTextFieldP1NomeEsc,
                                                GroupLayout.DEFAULT_SIZE
                                                ,
                                                381,
                                                Short.MAX_VALUE
                                            ))
                              .addContainerGap())
                .addComponent(jSeparatorP1, GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                .addGroup(jPanelPasso1Layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(
                                  this.jLabelP1Informacao,
                                  GroupLayout.DEFAULT_SIZE,
                                  466,
                                  Short.MAX_VALUE
                              )
                              .addContainerGap())
        );
        jPanelPasso1Layout.setVerticalGroup(
            jPanelPasso1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanelPasso1Layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(jPanelPasso1Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabelP1NomeEsc)
                                            .addComponent(
                                                this.jTextFieldP1NomeEsc,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(jPanelPasso1Layout.createParallelGroup(
                                      GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                jLabelP1LocalArq)
                                            .addComponent(
                                                this.jTextFieldP1LocalArq,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE
                                            ))
                              .addGap(18, 18, 18)
                              .addComponent(jSeparatorP1,
                                            GroupLayout.PREFERRED_SIZE, 10,
                                            GroupLayout.PREFERRED_SIZE
                              )
                              .addGap(18, 18, 18)
                              .addComponent(this.jLabelP1Informacao,
                                            GroupLayout.PREFERRED_SIZE, 24,
                                            GroupLayout.PREFERRED_SIZE
                              )
                              .addContainerGap(56, Short.MAX_VALUE))
        );
    }

    private void makeLayoutAndPack (final JPanel jPanelPassos, final JPanel jPanelControle) {
        final GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                              .addContainerGap()
                              .addComponent(
                                  jPanelPassos,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE
                              )
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(this.jScrollPanePrincipal,
                                                          GroupLayout.DEFAULT_SIZE
                                                , 646, Short.MAX_VALUE
                                            )
                                            .addComponent(jPanelControle,
                                                          GroupLayout.DEFAULT_SIZE
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            ))
                              .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(
                                                GroupLayout.Alignment.TRAILING,
                                                layout.createSequentialGroup()
                                                    .addComponent(
                                                        this.jScrollPanePrincipal,
                                                        GroupLayout.DEFAULT_SIZE, 373,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(
                                                        jPanelControle,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )
                                            )
                                            .addComponent(jPanelPassos,
                                                          GroupLayout.DEFAULT_SIZE
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            ))
                              .addContainerGap())
        );

        this.pack();
    }

    private void startStepOne () {
        if (this.jTextFieldP1NomeEsc.getText().isEmpty()) {
            this.jLabelP1Informacao.setText(getText("Provide a valid name for a Java class"));
            this.jTextFieldP1LocalArq.setText("");
            this.buttonNext.setEnabled(false);
        } else if (NameValidator.isValidClassName(this.jTextFieldP1NomeEsc.getText())) {
            this.jLabelP1Informacao.setText("");

            final var fileName =
                String.join(File.separator, this.path, this.jTextFieldP1NomeEsc.getText());

            this.jTextFieldP1LocalArq.setText(fileName);

            this.buttonNext.setEnabled(true);
        } else {
            this.jLabelP1Informacao.setText(getText("The class name is invalid"));
            this.buttonNext.setEnabled(false);
        }
        final var file =
            new File("%s%s%s.java".formatted(
                this.path,
                File.separator,
                this.jTextFieldP1NomeEsc.getText()
            ));
        if (file.exists()) {
            this.jLabelP1Informacao.setText("%s\n%s".formatted(
                this.jLabelP1Informacao.getText(),
                getText("This scheduler name already exists")
            ));
        }
    }

    private void onNextClick (final ActionEvent evt) {
        switch (this.currentStep) {
            case 1 -> {
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso2);
                this.buttonPrevious.setEnabled(true);
                this.jLabelPasso1.setEnabled(false);
                this.jLabelPasso2.setEnabled(true);
                this.currentStep = 2;
            }
            case 2 -> {
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso3);
                this.jLabelPasso2.setEnabled(false);
                this.jLabelPasso3.setEnabled(true);
                this.currentStep = 3;
            }
            case 3 -> {
                this.jLabelPasso3.setEnabled(false);
                if (this.jOpSimples.isSelected()) {
                    this.jScrollPanePrincipal.setViewportView(this.jPanelPassoSimples);
                    this.jLabelPasso4.setEnabled(true);
                    this.jLabelPasso5.setEnabled(true);
                    this.currentStep = 5;
                } else {
                    this.jScrollPanePrincipal.setViewportView(this.jPanelPasso4);
                    this.jLabelPasso4.setEnabled(true);
                    this.currentStep = 4;
                }
            }
            case 4 -> {
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso5);
                this.jLabelPasso4.setEnabled(false);
                this.jLabelPasso5.setEnabled(true);
                this.currentStep    = 5;
                this.tOrdering      = this.ordering;
                this.tParentAccount = this.parentAccount;
                this.tButtonType    = this.buttonType;
                this.ordering       = this.rOrdering;
                this.formula        = this.rFormula;
                this.buttonType     = this.rButtonType;
                this.parentAccount  = this.rParentAccount;
                this.escreverFormula();
            }
            case 5 -> {
                this.rOrdering      = this.ordering;
                this.rParentAccount = this.parentAccount;
                this.rButtonType    = this.buttonType;
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso6);
                this.jLabelPasso4.setEnabled(false);
                this.jLabelPasso5.setEnabled(false);
                this.jLabelPasso6.setEnabled(true);
                this.currentStep = 6;
            }
            case 6 -> {
                this.escreverGramatica();
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso7);
                this.jLabelPasso6.setEnabled(false);
                this.jLabelPasso7.setEnabled(true);
                this.currentStep = 7;
                this.buttonNext.setEnabled(false);
                this.buttonFinish.setEnabled(true);
            }
        }
    }

    private void onPreviousClick (final ActionEvent evt) {
        switch (this.currentStep) {
            case 2 -> {
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso1);
                this.buttonPrevious.setEnabled(false);
                this.jLabelPasso1.setEnabled(true);
                this.jLabelPasso2.setEnabled(false);
                this.currentStep = 1;
            }
            case 3 -> {
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso2);
                this.jLabelPasso2.setEnabled(true);
                this.jLabelPasso3.setEnabled(false);
                this.currentStep = 2;
            }
            case 4 -> {
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso3);
                this.jLabelPasso3.setEnabled(true);
                this.jLabelPasso4.setEnabled(false);
                this.currentStep = 3;
            }
            case 5 -> {
                this.jLabelPasso5.setEnabled(false);
                if (this.jOpSimples.isSelected()) {
                    this.jScrollPanePrincipal.setViewportView(this.jPanelPasso3);
                    this.jLabelPasso4.setEnabled(false);
                    this.jLabelPasso3.setEnabled(true);
                    this.currentStep = 3;
                } else {
                    this.jScrollPanePrincipal.setViewportView(this.jPanelPasso4);
                    this.jLabelPasso4.setEnabled(true);
                    this.currentStep = 4;
                }
                this.rOrdering      = this.ordering;
                this.rParentAccount = this.parentAccount;
                this.rButtonType    = this.buttonType;
                this.ordering       = this.tOrdering;
                this.formula        = this.tFormula;
                this.buttonType     = this.tButtonType;
                this.parentAccount  = this.tParentAccount;
                this.escreverFormula();
            }
            case 6 -> {
                this.jLabelPasso6.setEnabled(false);
                this.jLabelPasso5.setEnabled(true);
                if (this.jOpSimples.isSelected()) {
                    this.jScrollPanePrincipal.setViewportView(this.jPanelPassoSimples);
                    this.jLabelPasso4.setEnabled(true);
                } else {
                    this.jScrollPanePrincipal.setViewportView(this.jPanelPasso5);
                }
                this.currentStep = 5;
            }
            case 7 -> {
                this.jLabelPasso7.setEnabled(false);
                this.jLabelPasso6.setEnabled(true);
                this.jScrollPanePrincipal.setViewportView(this.jPanelPasso6);
                this.buttonNext.setEnabled(true);
                this.buttonFinish.setEnabled(false);
                this.currentStep = 6;
            }
        }
    }

    private void jRadioButtonP4RandomActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP4FIFO.setSelected(false);
        this.jRadioButtonP4Random.setSelected(true);
        this.jRadioButtonP4Crescente.setSelected(false);
        this.jRadioButtonP4Decrescente.setSelected(false);
        this.ordering = "Random";
        this.escreverFormula();
    }

    private void jRadioButtonP4DecrescenteActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP4FIFO.setSelected(false);
        this.jRadioButtonP4Random.setSelected(false);
        this.jRadioButtonP4Crescente.setSelected(false);
        this.jRadioButtonP4Decrescente.setSelected(true);
        this.ordering = "Decrescente";
        this.escreverFormula();
    }

    private void jRadioButtonP4CrescenteActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP4FIFO.setSelected(false);
        this.jRadioButtonP4Random.setSelected(false);
        this.jRadioButtonP4Crescente.setSelected(true);
        this.jRadioButtonP4Decrescente.setSelected(false);
        this.ordering = "Crescente";
        this.escreverFormula();
    }

    private void jButtonP4TComputacaoActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[TCP]");
    }

    private void jButtonP4AddActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("+");
    }

    private void jButtonP4VoltarActionPerformed (final ActionEvent evt) {
        final String operador = "+ - / * ";
        if (!this.formula.isEmpty()) {
            if (this.buttonType == OPEN_BRACKET) {
                this.parentAccount--;
            } else if (this.buttonType == CLOSE_BRACKET) {
                this.parentAccount++;
            }
            this.formula.removeLast();
            if (this.formula.isEmpty()) {
                this.buttonType    = START;
                this.parentAccount = 0;
            } else if (operador.contains(this.formula.getLast())) {
                this.buttonType = OPERATOR;
            } else if (this.formula.getLast().contains("(")) {
                this.buttonType = OPEN_BRACKET;
            } else if (this.formula.getLast().contains(")")) {
                this.buttonType = CLOSE_BRACKET;
            } else {
                this.buttonType = VARIABLE;
            }
        } else {
            this.buttonType    = START;
            this.parentAccount = 0;
        }
        this.escreverFormula();
    }

    private void jButtonP4NTSubmetidasActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[NTS]");
    }

    private void jButtonP4NTConcluidasActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[NTC]");
    }

    private void jButtonP4TComunicacaoActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[TC]");
    }

    private void jButtonP4PComputUserActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[PCU]");
    }

    private void jButtonP4SubActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("-");
    }

    private void jButtonP4MultActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("*");
    }

    private void jButtonP4DivActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("/");
    }

    private void jButtonP4AbreParentActionPerformed (final ActionEvent evt) {
        if (this.buttonType == START
            || this.buttonType == OPERATOR
            ||
            this.buttonType == OPEN_BRACKET) {
            this.parentAccount++;
            this.buttonType = OPEN_BRACKET;
            this.formula.add("(");
        }
        this.escreverFormula();
    }

    private void jButtonP4FechaParentActionPerformed (final ActionEvent evt) {
        if (this.parentAccount != 0 && (
            this.buttonType == VARIABLE ||
            this.buttonType == CLOSE_BRACKET
        )) {
            this.parentAccount--;
            this.buttonType = CLOSE_BRACKET;
            this.formula.add(")");
        }
        this.escreverFormula();
    }

    private void jButtonP4ConstActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[%s]".formatted(this.jFormattedTextP4DigitaConst.getText()));
    }

    private void jButtonP5AddActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("+");
    }

    private void jButtonP5SubActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("-");
    }

    private void jButtonP5AbreParentActionPerformed (final ActionEvent evt) {
        this.jButtonP4AbreParentActionPerformed(evt);
    }

    private void jButtonP5FechaParentActionPerformed (final ActionEvent evt) {
        this.jButtonP4FechaParentActionPerformed(evt);
    }

    private void jButtonP5DivActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("/");
    }

    private void jButtonP5MultActionPerformed (final ActionEvent evt) {
        this.pressionarOperador("*");
    }

    private void jButtonP5PProcessamentoActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[PP]");
    }

    private void jButtonP5LinkComunicacaoActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[LC]");
    }

    private void jButtonP5TCompTarefaActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[TCT]");
    }

    private void jButtonP5NumTExecActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[NTE]");
    }

    private void jButtonP5TComunTarefaActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[TCMT]");
    }

    private void jButtonP5Const1ActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[" + this.jFormattedTextP5DigitaConst.getText() + "]");
    }

    private void jRadioButtonP5CrescenteActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP5FIFO.setSelected(false);
        this.jRadioButtonP5Random.setSelected(false);
        this.jRadioButtonP5Crescente.setSelected(true);
        this.jRadioButtonP5Decrescente.setSelected(false);
        this.ordering = "Crescente";
        this.escreverFormula();
    }

    private void jRadioButtonP5DecrescenteActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP5FIFO.setSelected(false);
        this.jRadioButtonP5Random.setSelected(false);
        this.jRadioButtonP5Crescente.setSelected(false);
        this.jRadioButtonP5Decrescente.setSelected(true);
        this.ordering = "Decrescente";
        this.escreverFormula();
    }

    private void jRadioButtonP5RandomActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP5FIFO.setSelected(false);
        this.jRadioButtonP5Random.setSelected(true);
        this.jRadioButtonP5Crescente.setSelected(false);
        this.jRadioButtonP5Decrescente.setSelected(false);
        this.ordering = "Random";
        this.escreverFormula();
    }

    private void jOpSimplesActionPerformed (final ActionEvent evt) {
        if (this.jOpSimples.isSelected()) {
            this.jOpAvancada.setSelected(false);
            this.jOpSimples.setSelected(true);
        } else {
            this.jOpAvancada.setSelected(true);
            this.jOpSimples.setSelected(false);
        }
    }

    private void jOpAvancadaActionPerformed (final ActionEvent evt) {
        if (this.jOpAvancada.isSelected()) {
            this.jOpSimples.setSelected(false);
            this.jOpAvancada.setSelected(true);
        } else {
            this.jOpAvancada.setSelected(false);
            this.jOpSimples.setSelected(true);
        }
    }

    private void jRadioButtonP2EstaticaActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Estatica.setSelected(true);
        this.jRadioButtonP2Dinamica.setSelected(false);
        this.setEnableDinamica(false);
    }

    private void jRadioButtonP2DinamicaActionPerformed (final ActionEvent evt) {

        this.jRadioButtonP2Estatica.setSelected(false);
        this.jRadioButtonP2Dinamica.setSelected(true);
        this.setEnableDinamica(true);
    }

    private void jRadioButtonP2TempoActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Tempo.setSelected(true);
        this.jRadioButtonP2Chegada.setSelected(false);
        this.jRadioButtonP2Saida.setSelected(false);
        this.jRadioButtonP2concluida.setSelected(false);
    }

    private void jRadioButtonP2ChegadaActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Tempo.setSelected(false);
        this.jRadioButtonP2Chegada.setSelected(true);
        this.jRadioButtonP2Saida.setSelected(false);
        this.jRadioButtonP2concluida.setSelected(false);
    }

    private void jRadioButtonP2SaidaActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Tempo.setSelected(false);
        this.jRadioButtonP2Chegada.setSelected(false);
        this.jRadioButtonP2Saida.setSelected(true);
        this.jRadioButtonP2concluida.setSelected(false);
    }

    private void jRadioButtonP2CentralizadaActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Centralizada.setSelected(true);
        this.jRadioButtonP2Distribuida.setSelected(false);
    }

    private void jRadioButtonP2DistribuidaActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Centralizada.setSelected(false);
        this.jRadioButtonP2Distribuida.setSelected(true);
    }

    private void onFinishClick (final ActionEvent evt) {
        final var code = this.jTextPaneP7Gramatica.getText();
        this.parse = new InterpretadorGerador(code);
        this.tryParseAndSaveResult();
    }

    private void tryParseAndSaveResult () {
        if (this.parse.executarParse()) {
            return;
        }

        if (this.manager == null) {
            this.dispose();
            return;
        }

        this.manager.escrever(this.parse.getNome(), this.parse.getCodigo());
        final var errors = this.manager.compilar(this.parse.getNome());
        if (errors != null) {
            JOptionPane.showMessageDialog(this, errors, "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (this.manager.listar().contains(this.parse.getNome())) {
            this.dispose();
        } else {
            this.parse = null;
        }
    }

    private void onCancelClick (final ActionEvent evt) {
        this.dispose();
    }

    private void jButtonP5MflopExecActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[MFE]");
    }

    private void jButtonP4PTempoCriacaoActionPerformed (final ActionEvent evt) {
        this.pressionarVariavel("[TCR]");
    }

    private void jRadioButtonP2concluidaActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP2Tempo.setSelected(false);
        this.jRadioButtonP2Chegada.setSelected(false);
        this.jRadioButtonP2Saida.setSelected(false);
        this.jRadioButtonP2concluida.setSelected(true);
    }

    private void jRadioButtonP4FIFOActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP4FIFO.setSelected(true);
        this.jRadioButtonP4Random.setSelected(false);
        this.jRadioButtonP4Crescente.setSelected(false);
        this.jRadioButtonP4Decrescente.setSelected(false);
        this.ordering = "FIFO";
        this.escreverFormula();
    }

    private void jRadioButtonP5FIFOActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP5FIFO.setSelected(true);
        this.jRadioButtonP5Random.setSelected(false);
        this.jRadioButtonP5Crescente.setSelected(false);
        this.jRadioButtonP5Decrescente.setSelected(false);
        this.ordering = "FIFO";
        this.escreverFormula();
    }

    private void jRadioButtonP6SemRestricaoActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP6SemRestricao.setSelected(true);
        this.jRadioButtonP6PorRecurso.setSelected(false);
        this.jRadioButtonP6PorUsuario.setSelected(false);
        this.jLabelP6_1.setEnabled(false);
        this.jTextFieldP6Num.setEnabled(false);
        this.jLabelP6_2.setEnabled(false);
    }

    private void jRadioButtonP6PorRecursoActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP6SemRestricao.setSelected(false);
        this.jRadioButtonP6PorRecurso.setSelected(true);
        this.jRadioButtonP6PorUsuario.setSelected(false);
        this.jLabelP6_1.setEnabled(true);
        this.jTextFieldP6Num.setEnabled(true);
        this.jLabelP6_2.setEnabled(true);
        this.jLabelP6_2.setText(getText("tasks in all resources."));
    }

    private void jRadioButtonP6PorUsuarioActionPerformed (final ActionEvent evt) {
        this.jRadioButtonP6SemRestricao.setSelected(false);
        this.jRadioButtonP6PorRecurso.setSelected(false);
        this.jRadioButtonP6PorUsuario.setSelected(true);
        this.jLabelP6_1.setEnabled(true);
        this.jTextFieldP6Num.setEnabled(true);
        this.jLabelP6_2.setEnabled(true);
        this.jLabelP6_2.setText(getText("tasks by all user."));
    }

    private void setEnableDinamica (final boolean b) {
        this.jLabelP2Forma.setEnabled(b);
        this.jRadioButtonP2Tempo.setEnabled(b);
        this.jRadioButtonP2Chegada.setEnabled(b);
        this.jRadioButtonP2Saida.setEnabled(b);
        this.jRadioButtonP2concluida.setEnabled(b);
        this.jFormattedTextFieldP2Tempo.setEnabled(b);
    }

    private void escreverFormula () {
        if ("Random".equals(this.ordering) || "FIFO".equals(this.ordering)) {
            this.jTextFieldP4Formula.setText(this.ordering);
            this.jTextFieldP5Formula.setText(this.ordering);
        } else {
            this.jTextFieldP4Formula.setText(this.ordering + "( " + this.formula + ")");
            this.jTextFieldP5Formula.setText(this.ordering + "( " + this.formula + ")");
        }
    }

    private void pressionarOperador (final String token) {
        if (this.buttonType == VARIABLE ||
            this.buttonType == CLOSE_BRACKET) {
            this.buttonType = OPERATOR;
            this.formula.add(token);
        } else if (this.buttonType == OPERATOR) {
            this.formula.set(this.formula.size() - 1, token);
        }
        this.escreverFormula();
    }

    private void pressionarVariavel (final String token) {
        if (this.buttonType == START
            || this.buttonType == OPERATOR
            ||
            this.buttonType == OPEN_BRACKET) {
            this.buttonType = VARIABLE;
            this.formula.add(token);
        } else if (this.buttonType == VARIABLE) {
            this.formula.set(this.formula.size() - 1, token);
        }
        this.escreverFormula();
    }

    private void escreverGramatica () {
        this.jTextPaneP7Gramatica.setText("");
        this.print("SCHEDULER ", Color.blue);
        this.println(this.jTextFieldP1NomeEsc.getText());
        if (!this.jRadioButtonP6SemRestricao.isSelected()) {
            this.print("RESTRICT ", Color.blue);
            this.print(this.jTextFieldP6Num.getText(), Color.green);
            if (this.jRadioButtonP6PorRecurso.isSelected()) {
                this.println(" TASKPER RESOURCE", Color.blue);
            } else {
                this.println(" TASKPER USER", Color.blue);
            }
        }
        if (this.jRadioButtonP2Estatica.isSelected()) {
            this.println("STATIC", Color.blue);
        } else {
            this.print("DYNAMIC ", Color.blue);
            if (this.jRadioButtonP2Chegada.isSelected()) {
                this.println("TASK ENTRY", Color.blue);
            } else if (this.jRadioButtonP2Saida.isSelected()) {
                this.println("TASK DISPACTH", Color.blue);
            } else if (this.jRadioButtonP2concluida.isSelected()) {
                this.println("TASK COMPLETED", Color.blue);
            } else {
                this.print("TIME INTERVAL ", Color.blue);
                this.println(
                    this.jFormattedTextFieldP2Tempo.getText(),
                    Color.green
                );
            }
        }
        this.print("TASK SCHEDULER: ", Color.blue);
        if (this.jOpSimples.isSelected()) {
            switch (this.jListTarefa.getSelectedIndex()) {
                case 0 -> this.println("FIFO", Color.blue);
                case 1 -> {
                    this.print("DECREASING", Color.blue);
                    this.println(" ( [TCP] )");
                }
                case 2 -> {
                    this.print("CRESCENT", Color.blue);
                    this.println(" ( [TCP] )");
                }
                case 3 -> {
                    this.print("DECREASING", Color.blue);
                    this.println(" ( [TC] )");
                }
                case 4 -> {
                    this.print("CRESCENT", Color.blue);
                    this.println(" ( [TC] )");
                }
                case 5 -> {
                    this.print("CRESCENT", Color.blue);
                    this.println(" ( [NTS] - [NTC] )");
                }
            }
        } else {
            switch (this.tOrdering) {
                case "Random" -> this.println("RANDOM", Color.blue);
                case "FIFO" -> this.println("FIFO", Color.blue);
                case "Crescente" -> {
                    this.print("CRESCENT", Color.blue);
                    this.print(" ( ");
                    this.print(this.tFormula.toString());
                    this.println(")");
                }
                default -> {
                    this.print("DECREASING", Color.blue);
                    this.print(" ( ");
                    this.print(this.tFormula.toString());
                    this.println(")");
                }
            }
        }
        this.print("RESOURCE SCHEDULER: ", Color.blue);
        if (this.jOpSimples.isSelected()) {
            switch (this.jListRecurso.getSelectedIndex()) {
                case 0 -> this.println("FIFO", Color.blue);
                case 1 -> {
                    this.print("DECREASING", Color.blue);
                    this.println(" ( [PP] - [MFE] )");
                }
                case 2 -> {
                    this.print("CRESCENT", Color.blue);
                    this.println(" ( [MFE] )");
                }
                case 3 -> {
                    this.print("DECREASING", Color.blue);
                    this.println(" ( [LC] / ( [NTE] + [1] ) )");
                }
            }
        } else {
            switch (this.rOrdering) {
                case "Random" -> this.println("RANDOM", Color.blue);
                case "FIFO" -> this.println("FIFO", Color.blue);
                case "Crescente" -> {
                    this.print("CRESCENT", Color.blue);
                    this.print(" ( ");
                    this.print(this.rFormula.toString());
                    this.println(")");
                }
                default -> {
                    this.print("DECREASING", Color.blue);
                    this.print(" ( ");
                    this.print(this.rFormula.toString());
                    this.println(")");
                }
            }
        }
    }

    private void println (final String text, final Color cor) {
        this.print(text, cor);
        this.print("\n", cor);
    }

    private void println (final String text) {
        this.println(text, Color.black);
    }

    private void print (final String text) {
        this.print(text, Color.black);
    }

    private void print (final String text, final Color cor) {
        final SimpleAttributeSet configuraCor = new SimpleAttributeSet();
        final Document           doc          = this.jTextPaneP7Gramatica.getDocument();
        try {
            if (cor != null) {
                StyleConstants.setForeground(configuraCor, cor);
            } else {
                StyleConstants.setForeground(configuraCor, Color.black);
            }
            doc.insertString(doc.getLength(), text, configuraCor);
        } catch (final BadLocationException ex) {
            Logger.getLogger(PolicyGeneratorWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InterpretadorGerador getParse () {
        return this.parse;
    }

    private static class SpacedPrintList extends LinkedList<String> {

        @Override
        public String toString () {
            return this.stream()
                .map(String::toString)
                .collect(Collectors.joining(" "));
        }
    }

    private class SimpleResourceModel extends AbstractListModel<String> {

        private final List<String> ss = Stream.of(
                "Round-Robin (circular queue)",
                "The most computational power resource",
                "Resource with less workload",
                "Resource with better communication link"
            ).map(text -> getText(text))
            .toList();

        public int getSize () {
            return this.ss.size();
        }

        public String getElementAt (final int i) {
            return this.ss.get(i);
        }
    }

    private class SimpleTaskModel extends AbstractListModel<String> {

        private final List<String> strings = Stream.of(
            getText("FIFO (First In, First Out)"),
            this.translateAndMerge("Largest Task First", "(Cost of Processing)"),
            this.translateAndMerge("Lowest Task First", "(Cost of Processing)"),
            this.translateAndMerge("Largest Task First", "(Cost of Communication)"),
            this.translateAndMerge("Lowest Task First", "(Cost of Communication)"),
            getText("User with Less Use of Grid First")
        ).toList();

        public int getSize () {
            return this.strings.size();
        }

        public String getElementAt (final int i) {
            return this.strings.get(i);
        }

        private String translateAndMerge (final String s1, final String s2) {
            return "%s %s".formatted(
                getText(s1),
                getText(s2)
            );
        }
    }

    private class SchedulerNameKeyAdapter extends KeyAdapter {

        public void keyReleased (final KeyEvent evt) {
            PolicyGeneratorWindow.this.startStepOne();
        }
    }
}