package ispd.gui;

import ispd.gui.utils.*;
import ispd.gui.utils.fonts.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PickModelTypeDialog extends JDialog {

    private final JRadioButton jRadioGrid;

    private final JRadioButton jRadioIaaS;

    private final JRadioButton jRadioPaaS;

    private ModelType choice = ModelType.GRID;

    public PickModelTypeDialog (final Frame owner, final boolean modal) {
        super(owner, modal);
        this.initWindowProperties();
        this.jRadioGrid = configuredRadioButton("Grid", this::gridButtonClicked);
        this.jRadioIaaS = configuredRadioButton("Cloud - IaaS", this::iaasButtonClicked);
        this.jRadioPaaS = configuredRadioButton("Cloud - PaaS", this::paasButtonClicked);
        this.jRadioGrid.setSelected(true);
        this.makeLayoutAndPack();
    }

    private static JRadioButton configuredRadioButton (
        final String text,
        final ActionListener action
    ) {
        final var button = new JRadioButton();
        button.setText(text);
        button.addActionListener(action);
        return button;
    }

    private void initWindowProperties () {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setFont(Tahoma.PLAIN_LARGE);
    }

    private void gridButtonClicked (final ActionEvent evt) {
        this.selectOnly(this.jRadioGrid);
    }

    private void selectOnly (final JRadioButton button) {
        this.jRadioGrid.setSelected(false);
        this.jRadioIaaS.setSelected(false);
        this.jRadioPaaS.setSelected(false);
        button.setSelected(true);
    }

    private void iaasButtonClicked (final ActionEvent evt) {
        this.selectOnly(this.jRadioIaaS);
    }

    private void paasButtonClicked (final ActionEvent evt) {
        this.selectOnly(this.jRadioPaaS);
    }

    private void makeLayoutAndPack () {
        final var ok            = ButtonBuilder.basicButton("OK!", this::onOkClick);
        final var title         = new JLabel("Choose the service that do you want to model");
        final var pickModelType = new JPanel();
        final var groupLayout   = new GroupLayout(pickModelType);
        pickModelType.setLayout(groupLayout);

        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    groupLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ok)
                        .addGap(33, 33, 33)
                )
                .addGroup(groupLayout.createSequentialGroup()
                              .addGroup(groupLayout
                                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(groupLayout.createSequentialGroup()
                                                          .addGap(132, 132, 132)
                                                          .addGroup(groupLayout
                                                                        .createParallelGroup(
                                                                            GroupLayout.Alignment.LEADING)
                                                                        .addComponent(
                                                                            this.jRadioGrid)
                                                                        .addComponent(
                                                                            this.jRadioIaaS)
                                                                        .addComponent(
                                                                            this.jRadioPaaS)))
                                            .addGroup(groupLayout.createSequentialGroup()
                                                          .addGap(22, 22, 22)
                                                          .addComponent(title)))
                              .addContainerGap(114, Short.MAX_VALUE))
        );

        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                              .addContainerGap(21, Short.MAX_VALUE)
                              .addComponent(title)
                              .addGap(18, 18, 18)
                              .addComponent(this.jRadioGrid)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jRadioIaaS)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(this.jRadioPaaS)
                              .addGap(18, 18, 18)
                              .addComponent(ok)
                              .addContainerGap())
        );

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                              .addGap(0, 0, Short.MAX_VALUE)
                              .addComponent(
                                  pickModelType,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE
                              )
                              .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                              .addGap(0, 0, Short.MAX_VALUE)
                              .addComponent(
                                  pickModelType,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE
                              )
                              .addGap(0, 0, Short.MAX_VALUE))
        );

        this.pack();
    }

    private void onOkClick (final ActionEvent evt) {
        this.choice = this.getChoiceForSelectedButton();
        this.setVisible(false);
    }

    private ModelType getChoiceForSelectedButton () {
        if (this.jRadioGrid.isSelected()) {
            return ModelType.GRID;
        }

        if (this.jRadioIaaS.isSelected()) {
            return ModelType.IAAS;
        }

        if (this.jRadioPaaS.isSelected()) {
            return ModelType.PAAS;
        }

        return this.choice;
    }

    public ModelType getEscolha () {
        return this.choice;
    }
}