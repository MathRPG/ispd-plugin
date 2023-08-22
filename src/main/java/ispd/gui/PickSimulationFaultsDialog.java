package ispd.gui;

import ispd.gui.utils.fonts.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PickSimulationFaultsDialog extends JFrame {

    public JCheckBox cbkOmissaoHardware;

    public JCheckBox cbkOmissaoSoftware;

    public JCheckBox cbxDesenhoIncorreto;

    public JCheckBox cbxEstado;

    public JCheckBox cbxFPermanentes;

    public JCheckBox cbxHDCheio;

    public JCheckBox cbxIncompatibilidade;

    public JCheckBox cbxInterdependencia;

    public JCheckBox cbxNegacaoService;

    public JCheckBox cbxPrecoce;

    public JCheckBox cbxSobrecargaTempo;

    public JCheckBox cbxTardia;

    public JCheckBox cbxTransiente;

    public JCheckBox cbxValores;

    private JCheckBox cbxEnvelhecimento;

    private JCheckBox cbxFalhaResposta;

    private JCheckBox cbxInteracao;

    private JCheckBox cbxSoftware;

    private JCheckBox ckbOmissao;

    private JCheckBox cbxDiversas;

    public PickSimulationFaultsDialog () {
        this.initComponents();
    }

    private void initComponents () {
        final JPanel panelJSelecionarFalhas = new JPanel();
        this.ckbOmissao          = new JCheckBox();
        this.cbkOmissaoHardware  = new JCheckBox();
        this.cbkOmissaoSoftware  = new JCheckBox();
        this.cbxEnvelhecimento   = new JCheckBox();
        this.cbxNegacaoService   = new JCheckBox();
        this.cbxHDCheio          = new JCheckBox();
        this.cbxFalhaResposta    = new JCheckBox();
        this.cbxValores          = new JCheckBox();
        this.cbxEstado           = new JCheckBox();
        this.cbxInteracao        = new JCheckBox();
        this.cbxSobrecargaTempo  = new JCheckBox();
        this.cbxInterdependencia = new JCheckBox();
        this.cbxDiversas         = new JCheckBox();
        this.cbxFPermanentes     = new JCheckBox();
        this.cbxDesenhoIncorreto = new JCheckBox();
        final JCheckBox cbxTempoResposta = new JCheckBox();
        this.cbxPrecoce           = new JCheckBox();
        this.cbxTardia            = new JCheckBox();
        this.cbxIncompatibilidade = new JCheckBox();
        this.cbxSoftware          = new JCheckBox();
        this.cbxTransiente        = new JCheckBox();
        final JButton jButtonSelecionarFalhas = new JButton();
        final JLabel  jLabel22                = new JLabel();

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("SelectFaults");
        this.setLocation(new Point(300, 100));

        panelJSelecionarFalhas.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        panelJSelecionarFalhas.setMaximumSize(new Dimension(159, 32769));
        panelJSelecionarFalhas.setOpaque(false);

        this.ckbOmissao.setFont(Tahoma.BOLD_MEDIUM);
        this.ckbOmissao.setText("Omission");
        this.ckbOmissao.addActionListener(this::ckbOmissaoActionPerformed);

        this.cbkOmissaoHardware.setText("Hardware");

        this.cbkOmissaoSoftware.setText("Software");
        this.cbkOmissaoSoftware.setEnabled(false);

        this.cbxEnvelhecimento.setFont(Tahoma.BOLD_MEDIUM);
        this.cbxEnvelhecimento.setText("Aging related");
        this.cbxEnvelhecimento.addActionListener(this::cbxEnvelhecimentoActionPerformed);

        this.cbxNegacaoService.setText("Denial of Service");
        this.cbxNegacaoService.setEnabled(false);

        this.cbxHDCheio.setText("Full HD");
        this.cbxHDCheio.setEnabled(false);

        this.cbxFalhaResposta.setFont(Tahoma.BOLD_MEDIUM);
        this.cbxFalhaResposta.setText("Response faults");
        this.cbxFalhaResposta.addActionListener(this::cbxFalhaRespostaActionPerformed);

        this.cbxValores.setText("Values faults");
        this.cbxValores.setEnabled(false);

        this.cbxEstado.setText("State transimission");
        this.cbxEstado.setEnabled(false);

        this.cbxInteracao.setFont(Tahoma.BOLD_MEDIUM);
        this.cbxInteracao.setText("Interaction");
        this.cbxInteracao.addActionListener(this::cbxInteracaoActionPerformed);

        this.cbxSobrecargaTempo.setText("Timing overheads");
        this.cbxSobrecargaTempo.setEnabled(false);

        this.cbxInterdependencia.setText("Service interdependence");
        this.cbxInterdependencia.setEnabled(false);

        this.cbxDiversas.setFont(Tahoma.BOLD_MEDIUM);
        this.cbxDiversas.setText("Miscellaneous");
        this.cbxDiversas.addActionListener(this::cbxDiversasActionPerformed);

        this.cbxFPermanentes.setText("Permanet fauls");
        this.cbxFPermanentes.setEnabled(false);

        this.cbxDesenhoIncorreto.setText("Incorrect desing");
        this.cbxDesenhoIncorreto.setEnabled(false);

        cbxTempoResposta.setFont(Tahoma.BOLD_MEDIUM);
        cbxTempoResposta.setText("Timing faults");
        cbxTempoResposta.addActionListener(this::cbxTempoRespostaActionPerformed);

        this.cbxPrecoce.setText("Early");
        this.cbxPrecoce.setEnabled(false);

        this.cbxTardia.setText("Late");
        this.cbxTardia.setEnabled(false);

        this.cbxIncompatibilidade.setText("Protocol Incompatibilities");
        this.cbxIncompatibilidade.setToolTipText("");
        this.cbxIncompatibilidade.setEnabled(false);

        this.cbxSoftware.setFont(Tahoma.BOLD_MEDIUM);
        this.cbxSoftware.setText("Software");
        this.cbxSoftware.addActionListener(this::cbxSoftwareActionPerformed);

        this.cbxTransiente.setText("Transient or intermitent");
        this.cbxTransiente.setEnabled(false);

        final GroupLayout PanelJSelecionarFalhasLayout = new GroupLayout(panelJSelecionarFalhas);
        panelJSelecionarFalhas.setLayout(PanelJSelecionarFalhasLayout);
        PanelJSelecionarFalhasLayout.setHorizontalGroup(
            PanelJSelecionarFalhasLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(PanelJSelecionarFalhasLayout.createSequentialGroup()
                              .addGap(25, 25, 25)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.LEADING)
                                            .addComponent(
                                                this.ckbOmissao)
                                            .addComponent(
                                                this.cbkOmissaoHardware)
                                            .addComponent(
                                                this.cbkOmissaoSoftware)
                                            .addComponent(
                                                this.cbxInteracao)
                                            .addComponent(
                                                this.cbxSobrecargaTempo)
                                            .addComponent(
                                                this.cbxInterdependencia)
                                            .addComponent(
                                                this.cbxIncompatibilidade)
                                            .addComponent(
                                                this.cbxSoftware))
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.LEADING)
                                            .addGroup(
                                                PanelJSelecionarFalhasLayout
                                                    .createSequentialGroup()
                                                    .addGap(
                                                        18,
                                                        18,
                                                        18
                                                    )
                                                    .addComponent(
                                                        this.cbxTransiente)
                                                    .addContainerGap(
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE
                                                    ))
                                            .addGroup(
                                                GroupLayout.Alignment.TRAILING,
                                                PanelJSelecionarFalhasLayout
                                                    .createSequentialGroup()
                                                    .addPreferredGap(
                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        34,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addGroup(
                                                        PanelJSelecionarFalhasLayout
                                                            .createParallelGroup(
                                                                GroupLayout.Alignment.LEADING)
                                                            .addComponent(
                                                                this.cbxEnvelhecimento)
                                                            .addComponent(
                                                                this.cbxNegacaoService)
                                                            .addComponent(
                                                                this.cbxHDCheio)
                                                            .addComponent(
                                                                this.cbxDiversas)
                                                            .addComponent(
                                                                this.cbxFPermanentes)
                                                            .addComponent(
                                                                this.cbxDesenhoIncorreto))
                                                    .addGap(
                                                        40,
                                                        40,
                                                        40
                                                    )
                                                    .addGroup(
                                                        PanelJSelecionarFalhasLayout
                                                            .createParallelGroup(
                                                                GroupLayout.Alignment.LEADING)
                                                            .addComponent(
                                                                this.cbxTardia)
                                                            .addComponent(
                                                                this.cbxPrecoce)
                                                            .addComponent(
                                                                cbxTempoResposta)
                                                            .addComponent(
                                                                this.cbxEstado)
                                                            .addComponent(
                                                                this.cbxValores)
                                                            .addComponent(
                                                                this.cbxFalhaResposta))
                                                    .addGap(
                                                        51,
                                                        51,
                                                        51
                                                    )
                                            )))
        );
        PanelJSelecionarFalhasLayout.setVerticalGroup(
            PanelJSelecionarFalhasLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(PanelJSelecionarFalhasLayout.createSequentialGroup()
                              .addGap(41, 41, 41)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.LEADING)
                                            .addComponent(
                                                this.ckbOmissao,
                                                GroupLayout.PREFERRED_SIZE,
                                                23,
                                                GroupLayout.PREFERRED_SIZE
                                            )
                                            .addGroup(
                                                PanelJSelecionarFalhasLayout
                                                    .createParallelGroup(
                                                        GroupLayout.Alignment.BASELINE)
                                                    .addComponent(
                                                        this.cbxEnvelhecimento)
                                                    .addComponent(
                                                        this.cbxFalhaResposta)))
                              .addPreferredGap(
                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.cbkOmissaoHardware)
                                            .addComponent(
                                                this.cbxNegacaoService)
                                            .addComponent(
                                                this.cbxValores))
                              .addPreferredGap(
                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.cbkOmissaoSoftware)
                                            .addComponent(
                                                this.cbxHDCheio)
                                            .addComponent(
                                                this.cbxEstado))
                              .addGap(18, 18, 18)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.cbxInteracao)
                                            .addComponent(
                                                this.cbxDiversas)
                                            .addComponent(
                                                cbxTempoResposta))
                              .addPreferredGap(
                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.cbxSobrecargaTempo)
                                            .addComponent(
                                                this.cbxFPermanentes)
                                            .addComponent(
                                                this.cbxPrecoce))
                              .addPreferredGap(
                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.cbxInterdependencia)
                                            .addComponent(
                                                this.cbxDesenhoIncorreto)
                                            .addComponent(
                                                this.cbxTardia))
                              .addPreferredGap(
                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(
                                  this.cbxIncompatibilidade)
                              .addGap(18, 18, 18)
                              .addGroup(PanelJSelecionarFalhasLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.BASELINE)
                                            .addComponent(
                                                this.cbxSoftware)
                                            .addComponent(
                                                this.cbxTransiente))
                              .addContainerGap(
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE
                              ))
        );

        jButtonSelecionarFalhas.setText("Select the faults and return to the Main screen");
        jButtonSelecionarFalhas.addActionListener(this::jButtonSelecionarFalhasActionPerformed);

        jLabel22.setFont(Tahoma.BOLD_MEDIUM);
        jLabel22.setText("Select Faults");

        final GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(layout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.LEADING,
                                                false
                                            )
                                            .addComponent(jLabel22,
                                                          GroupLayout.PREFERRED_SIZE,
                                                          219, GroupLayout.PREFERRED_SIZE
                                            )
                                            .addComponent(jButtonSelecionarFalhas
                                                ,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          GroupLayout.DEFAULT_SIZE,
                                                          Short.MAX_VALUE
                                            )
                                            .addComponent(
                                                panelJSelecionarFalhas,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE
                                            ))
                              .addContainerGap(31, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel22,
                                      GroupLayout.PREFERRED_SIZE,
                                      23, GroupLayout.PREFERRED_SIZE
                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(
                            panelJSelecionarFalhas,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addGap(18, 18, 18)
                        .addComponent(jButtonSelecionarFalhas)
                        .addContainerGap(48, Short.MAX_VALUE)
                )
        );

        this.pack();
    }

    private void ckbOmissaoActionPerformed (final ActionEvent evt) {
        if (this.ckbOmissao.isSelected()) {
            this.cbkOmissaoHardware.setEnabled(true);
            this.cbkOmissaoSoftware.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Omission Faults.");
        } else {
            JOptionPane.showMessageDialog(null, "Omission faults not selected.");
        }
    }

    private void cbxEnvelhecimentoActionPerformed (final ActionEvent evt) {
        if (this.cbxEnvelhecimento.isSelected()) {
            this.cbxNegacaoService.setEnabled(true);
            this.cbxHDCheio.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Development failure");
        } else {
            JOptionPane.showMessageDialog(null, "Failure not selected");
        }
    }

    private void cbxFalhaRespostaActionPerformed (final ActionEvent evt) {
        if (this.cbxFalhaResposta.isSelected()) {
            this.cbxValores.setEnabled(true);
            this.cbxEstado.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Response faults.");
        } else {
            JOptionPane.showMessageDialog(null, "Not response faults.");
        }
    }

    private void cbxInteracaoActionPerformed (final ActionEvent evt) {
        if (this.cbxInteracao.isSelected()) {
            this.cbxSobrecargaTempo.setEnabled(true);
            this.cbxInterdependencia.setEnabled(true);
            this.cbxIncompatibilidade.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Development failure");
        } else {
            JOptionPane.showMessageDialog(null, "Failure not selected");
        }
    }

    private void cbxDiversasActionPerformed (final ActionEvent evt) {

        if (this.cbxDiversas.isSelected()) {
            this.cbxFPermanentes.setEnabled(true);
            this.cbxDesenhoIncorreto.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Development failure");
        } else {
            JOptionPane.showMessageDialog(null, "Failure not selected");
        }
    }

    private void cbxTempoRespostaActionPerformed (final ActionEvent evt) {
        if (this.cbxDiversas.isSelected()) {
            this.cbxPrecoce.setEnabled(true);
            this.cbxTardia.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Development failure.");
        } else {
            JOptionPane.showMessageDialog(null, "Failure not selected");
        }
    }

    private void cbxSoftwareActionPerformed (final ActionEvent evt) {
        if (this.cbxSoftware.isSelected()) {
            this.cbxTransiente.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Development failure");
        } else {
            JOptionPane.showMessageDialog(null, "Failure not selected.");
        }
    }

    private void jButtonSelecionarFalhasActionPerformed (final ActionEvent evt) {
        this.cbkOmissaoHardware.isSelected();
        this.dispose();
    }
}