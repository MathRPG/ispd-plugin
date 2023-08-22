package ispd.gui.configuracao;

import static ispd.gui.TextSupplier.*;

import ispd.gui.*;
import ispd.gui.iconico.grade.*;
import ispd.gui.utils.*;
import ispd.policy.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.table.*;

public class JPanelConfigIcon extends JPanel {

    private static final int ROW_HEIGHT = 20;

    private final JLabel jLabelIconName = new JLabel("Configuration for the icon # 0");

    private final JLabel jLabelTitle = makeTitleLabel();

    private final JScrollPane jScrollPane = new JScrollPane();

    private final VariedRowTable machineTable =
        this.createTableWith(MachineVariedRowTable::new, MachineTable::new);

    private final VariedRowTable iassMachineTable =
        this.createTableWith(IaasMachineVariedRowTable::new, MachineTableIaaS::new);

    private final VariedRowTable clusterTable =
        this.createTableWith(ClusterVariedRowTable::new, ClusterTable::new);

    private final VariedRowTable iassClusterTable =
        this.createTableWith(IaasClusterVariedRowTable::new, ClusterTableIaaS::new);

    private final VariedRowTable linkTable =
        this.createTableWith(LinkVariedRowTable::new, LinkTable::new);

    private PolicyManager schedulers = null;

    private PolicyManager cloudSchedulers = null;

    private PolicyManager allocators = null;

    public JPanelConfigIcon () {
        this.setLayout();
    }

    private static JLabel makeTitleLabel () {
        final JLabel label = new JLabel("Machine icon configuration");
        label.setFont(Fonts.Tahoma.BOLD_12);
        return label;
    }

    private void setLayout () {
        final var layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                      .addComponent(
                                          this.jScrollPane,
                                          GroupLayout.PREFERRED_SIZE,
                                          0,
                                          Short.MAX_VALUE
                                      )
                                      .addGroup(layout
                                                    .createSequentialGroup()
                                                    .addContainerGap()
                                                    .addGroup(
                                                        layout
                                                            .createParallelGroup(
                                                                GroupLayout.Alignment.LEADING,
                                                                false
                                                            )
                                                            .addComponent(
                                                                this.jLabelTitle,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE
                                                            )
                                                            .addComponent(
                                                                this.jLabelIconName,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE
                                                            ))
                                                    .addContainerGap(
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE
                                                    )));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
            layout.createSequentialGroup().addContainerGap().addComponent(this.jLabelTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(this.jLabelIconName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(this.jScrollPane, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)));
    }

    private VariedRowTable createTableWith (
        final Supplier<? extends VariedRowTable> makeTable,
        final Supplier<? extends TableModel> makeModel
    ) {
        final var t = makeTable.get();
        t.setModel(makeModel.get());
        t.setRowHeight(ROW_HEIGHT);
        return t;
    }

    public void setEscalonadores (final PolicyManager schedulers) {
        this.schedulers = schedulers;
        schedulers.listar().forEach(sch -> {
            this.getTabelaMaquina().getEscalonadores().addItem(sch);
            this.getTabelaCluster().getEscalonadores().addItem(sch);
        });
    }

    private MachineTable getTabelaMaquina () {
        return (MachineTable) this.machineTable.getModel();
    }

    private ClusterTable getTabelaCluster () {
        return (ClusterTable) this.clusterTable.getModel();
    }

    public void setEscalonadoresCloud (final PolicyManager cloudSchedulers) {
        this.cloudSchedulers = cloudSchedulers;
        cloudSchedulers.listar().forEach(sch -> {
            this.getTabelaMaquinaIaaS().getEscalonadores().addItem(sch);
            this.getTabelaClusterIaaS().getEscalonadores().addItem(sch);
        });
    }

    private MachineTableIaaS getTabelaMaquinaIaaS () {
        return (MachineTableIaaS) this.iassMachineTable.getModel();
    }

    private ClusterTableIaaS getTabelaClusterIaaS () {
        return (ClusterTableIaaS) this.iassClusterTable.getModel();
    }

    public void setAlocadores (final PolicyManager allocators) {
        this.allocators = allocators;
        allocators.listar().forEach(alloc -> {
            this.getTabelaMaquinaIaaS().getAlocadores().addItem(alloc);
            this.getTabelaClusterIaaS().getAlocadores().addItem(alloc);
        });
    }

    public void setIcone (final GridItem icon) {
        if (icon instanceof Link) {
            this.jLabelTitle.setText(getText("Network icon configuration"));
        } else if (icon instanceof Internet) {
            this.jLabelTitle.setText(getText("Internet icon configuration"));
        }
        this.jLabelIconName.setText(
            "%s#: %d".formatted(
                getText("Configuration for the icon"),
                icon.getId().getGlobalId()
            ));
        this.getTabelaLink().setLink(icon);
        this.jScrollPane.setViewportView(this.linkTable);
    }

    private LinkTable getTabelaLink () {
        return (LinkTable) this.linkTable.getModel();
    }

    public void setIcone (final GridItem icon, final Iterable<String> users, final int choice) {
        if (choice == PickModelTypeDialog.GRID) {
            if (!this.schedulers.listarRemovidos().isEmpty()) {
                for (final Object escal : this.schedulers.listarRemovidos()) {
                    this.getTabelaMaquina().getEscalonadores().removeItem(escal);
                }
                this.schedulers.listarRemovidos().clear();
            }
            if (!this.schedulers.listarAdicionados().isEmpty()) {
                for (final Object escal : this.schedulers.listarAdicionados()) {
                    this.getTabelaMaquina().getEscalonadores().addItem(escal);
                }
                this.schedulers.listarAdicionados().clear();
            }
            this.jLabelIconName.setText(
                "%s#: %d".formatted(
                    getText("Configuration for the icon"),
                    icon.getId().getGlobalId()
                ));
            if (icon instanceof Machine) {
                this.jLabelTitle.setText(getText("Machine icon configuration"));
                this.getTabelaMaquina().setMaquina((Machine) icon, users);
                this.jScrollPane.setViewportView(this.machineTable);
            }
            if (icon instanceof Cluster) {
                this.jLabelTitle.setText(getText("Cluster icon configuration"));
                this.getTabelaCluster().setCluster((Cluster) icon, users);
                this.jScrollPane.setViewportView(this.clusterTable);
            }
        } else if (choice == PickModelTypeDialog.IAAS) {
            if (!this.cloudSchedulers.listarRemovidos().isEmpty()) {
                for (final Object escal : this.cloudSchedulers.listarRemovidos()) {
                    this.getTabelaMaquinaIaaS().getEscalonadores().removeItem(escal);
                    this.getTabelaClusterIaaS().getEscalonadores().removeItem(escal);
                }
                this.cloudSchedulers.listarRemovidos().clear();
            }
            if (!this.cloudSchedulers.listarAdicionados().isEmpty()) {
                for (final Object escal : this.cloudSchedulers.listarAdicionados()) {
                    this.getTabelaMaquinaIaaS().getEscalonadores().addItem(escal);
                    this.getTabelaClusterIaaS().getEscalonadores().addItem(escal);
                }
                this.cloudSchedulers.listarAdicionados().clear();
            }

            if (!this.allocators.listarRemovidos().isEmpty()) {
                for (final Object alloc : this.allocators.listarRemovidos()) {
                    this.getTabelaMaquinaIaaS().getAlocadores().removeItem(alloc);
                    this.getTabelaClusterIaaS().getAlocadores().removeItem(alloc);
                }
                this.allocators.listarRemovidos().clear();
            }
            if (!this.allocators.listarAdicionados().isEmpty()) {
                for (final Object alloc : this.allocators.listarAdicionados()) {
                    this.getTabelaMaquinaIaaS().getAlocadores().addItem(alloc);
                    this.getTabelaClusterIaaS().getAlocadores().addItem(alloc);
                }
                this.allocators.listarAdicionados().clear();
            }

            this.jLabelIconName.setText(
                "%s#: %d".formatted(
                    getText("Configuration for the icon"),
                    icon.getId().getGlobalId()
                ));
            if (icon instanceof Machine) {
                this.jLabelTitle.setText(getText("Machine icon configuration"));
                this.getTabelaMaquinaIaaS().setMaquina((Machine) icon, users);
                this.jScrollPane.setViewportView(this.iassMachineTable);
            }
            if (icon instanceof Cluster) {
                this.jLabelTitle.setText(getText("Cluster icon configuration"));
                this.getTabelaClusterIaaS().setCluster((Cluster) icon, users);
                this.jScrollPane.setViewportView(this.iassClusterTable);
            }
        }
    }

    public String getTitle () {
        return this.jLabelTitle.getText();
    }

    private static class MachineVariedRowTable extends VariedRowTable {

        private static final String[] TOOL_TIPS = {
            "Insert the label name of the resource",
            "Select the resource owner",
            "Insert the amount of computing power of the resource in MFlops",
            "Insert the percentage of background computing in decimal notation",
            "Insert the number of precessing cores of the resource",
            "Insert the amount of memory of the resource in MBytes",
            "Insert the amount of hard disk of the resource in GBytes",
            "Select if the resource is master node",
            "Select the task scheduling policy of the master",
            "Select the slave nodes that will be coordinated by this master",
        };

        private static String getRowToolTip (final int rowIndex) {
            if (rowIndex >= TOOL_TIPS.length) {
                return null;
            }
            return TOOL_TIPS[rowIndex];
        }

        public String getToolTipText (final MouseEvent e) {
            final Point p        = e.getPoint();
            final int   rowIndex = this.rowAtPoint(p);
            final int   colIndex = this.columnAtPoint(p);
            return this.getToolTip(rowIndex, colIndex);
        }

        private String getToolTip (final int rowIndex, final int colIndex) {
            try {
                if (colIndex != 1) {
                    return null;
                }
                return getRowToolTip(rowIndex);
            } catch (final RuntimeException ignored) {
                return null;
            }
        }
    }

    private static class IaasMachineVariedRowTable extends VariedRowTable {

        public String getToolTipText (final MouseEvent e) {
            String      tip      = null;
            final Point p        = e.getPoint();
            final int   rowIndex = this.rowAtPoint(p);
            final int   colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Select the resource owner";
                    } else if (rowIndex == 2) {
                        tip = "Insert the amount of computing power of the resource in MFlops";
                    } else if (rowIndex == 3) {
                        tip = "Insert the percentage of background computing in decimal notation";
                    } else if (rowIndex == 4) {
                        tip = "Insert the number of precessing cores of the resource";
                    } else if (rowIndex == 5) {
                        tip = "Insert the amount of memory of the resource in MBytes";
                    } else if (rowIndex == 6) {
                        tip = "Insert the amount of hard disk of the resource in GBytes";
                    } else if (rowIndex == 7) {
                        tip = "Insert the cost of processing utilization ($/cores/h)";
                    } else if (rowIndex == 8) {
                        tip = "Insert the cost of memory utilization ($/MB/h)";
                    } else if (rowIndex == 9) {
                        tip = "Insert the cost of disk utilization ($/GB/h)";
                    } else if (rowIndex == 10) {
                        tip = "Select if the resource is a virtual machine monitor";
                    } else if (rowIndex == 11) {
                        tip = "Select the task scheduling policy of the VMM";
                    } else if (rowIndex == 12) {
                        tip = "Select the virtual machine allocation policy of the VMM";
                    } else if (rowIndex == 13) {
                        tip = "Select the nodes that will be coordinated by this VMM";
                    }
                }
            } catch (final RuntimeException ignored) {
            }

            return tip;
        }
    }

    private static class ClusterVariedRowTable extends VariedRowTable {

        public String getToolTipText (final MouseEvent e) {
            String      tip      = null;
            final Point p        = e.getPoint();
            final int   rowIndex = this.rowAtPoint(p);
            final int   colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Select the resource owner";
                    } else if (rowIndex == 2) {
                        tip = "Insert the number of nodes that composes the cluster";
                    } else if (rowIndex == 3) {
                        tip = "Insert the amount of computing power of the resource in MFlops";
                    } else if (rowIndex == 4) {
                        tip = "Insert the number of precessing cores of the resource";
                    } else if (rowIndex == 5) {
                        tip = "Insert the amount of memory of the resource in MBytes";
                    } else if (rowIndex == 6) {
                        tip = "Insert the amount of hard disk of the resource in GBytes";
                    } else if (rowIndex == 7) {
                        tip =
                            "Insert the amount of bandwidth that connect the cluster nodes in Mbps";
                    } else if (rowIndex == 8) {
                        tip =
                            "Insert the latency time of the links that connect the cluster nodes in seconds";
                    } else if (rowIndex == 9) {
                        tip = "Select if the resource is a master node";
                    } else if (rowIndex == 10) {
                        tip = "Select the task scheduling policy of the master node";
                    } else if (rowIndex == 11) {
                        tip = "Select the slave nodes that will be coordinated by this master";
                    }
                }
            } catch (final RuntimeException ignored) {
            }

            return tip;
        }
    }

    private static class IaasClusterVariedRowTable extends VariedRowTable {

        public String getToolTipText (final MouseEvent e) {
            String      tip      = null;
            final Point p        = e.getPoint();
            final int   rowIndex = this.rowAtPoint(p);
            final int   colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Select the resource owner";
                    } else if (rowIndex == 2) {
                        tip = "Insert the number of nodes that composes the cluster";
                    } else if (rowIndex == 3) {
                        tip = "Insert the amount of computing power of the resource in MFlops";
                    } else if (rowIndex == 4) {
                        tip = "Insert the number of precessing cores of the resource";
                    } else if (rowIndex == 5) {
                        tip = "Insert the amount of memory of the resource in MBytes";
                    } else if (rowIndex == 6) {
                        tip = "Insert the amount of hard disk of the resource in GBytes";
                    } else if (rowIndex == 7) {
                        tip = "Insert the cost of processing utilization ($/cores/h)";
                    } else if (rowIndex == 8) {
                        tip = "Insert the cost of memory utilization ($/MB/h)";
                    } else if (rowIndex == 9) {
                        tip = "Insert the cost of disk utilization ($/GB/h)";
                    } else if (rowIndex == 10) {
                        tip =
                            "Insert the amount of bandwidth that connect the cluster nodes in Mbps";
                    } else if (rowIndex == 11) {
                        tip =
                            "Insert the latency time of the links that connect the cluster nodes in seconds";
                    } else if (rowIndex == 12) {
                        tip = "Select if the resource is a virtual machine monitor";
                    } else if (rowIndex == 13) {
                        tip = "Select the task scheduling policy of the VMM";
                    } else if (rowIndex == 14) {
                        tip = "Select the virtual machine allocation policy of the VMM";
                    } else if (rowIndex == 15) {
                        tip = "Select the nodes that will be coordinated by this VMM";
                    }
                }
            } catch (final RuntimeException ignored) {
            }

            return tip;
        }
    }

    private static class LinkVariedRowTable extends VariedRowTable {

        public String getToolTipText (final MouseEvent e) {
            String      tip      = null;
            final Point p        = e.getPoint();
            final int   rowIndex = this.rowAtPoint(p);
            final int   colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Insert the latency time of the resource in seconds";
                    } else if (rowIndex == 2) {
                        tip =
                            "Insert the percentage of background communication in decimal notation";
                    } else if (rowIndex == 3) {
                        tip = "Insert the amount of bandwidth of the resource in seconds";
                    }
                }
            } catch (final RuntimeException ignored) {
            }

            return tip;
        }
    }
}
