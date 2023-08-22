package ispd.gui.configuracao;

import ispd.gui.iconico.grade.*;
import ispd.policy.managers.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.jetbrains.annotations.*;

public class MachineTableIaaS extends AbstractTableModel {

    private static final int TYPE = 0;

    private static final int VALUE = 1;

    private static final int LABEL = 0;

    private static final int OWNER = 1;

    private static final int PROCESSOR = 2;

    private static final int LOAD_FACTOR = 3;

    private static final int CORES = 4;

    private static final int RAM = 5;

    private static final int DISK = 6;

    private static final int COST_PER_PROCESSOR = 7;

    private static final int COST_PER_MEMORY = 8;

    private static final int COST_PER_DISK = 9;

    private static final int VMM = 10;

    private static final int SCHEDULER = 11;

    private static final int VMM_POLICY = 12;

    private static final int SLAVE = 13;

    private static final int ROW_COUNT = 14;

    private static final int COLUMN_COUNT = 2;

    private static final String[] NO_USERS = {};

    private final JButton slaves = this.setButton();

    private final JComboBox<?> schedulers =
        toolTippedComboBox(
            CloudSchedulingPolicyManager.NATIVE_POLICIES.toArray(String[]::new),
            "Select the task scheduling policy"
        );

    private final JComboBox<String> users =
        toolTippedComboBox(NO_USERS, "Select the resource owner");

    private final JComboBox<String> allocators = toolTippedComboBox(
        VmAllocationPolicyManager.NATIVE_POLICIES.toArray(String[]::new),
        "Select the virtual machine allocation policy"
    );

    private final JList<GridItem> slaveList = new JList<>();

    private ResourceBundle words;

    private Machine machine = null;

    public MachineTableIaaS (final ResourceBundle words) {
        this.words = words;
        new CheckListRenderer(this.slaveList);
    }

    private static <T> JComboBox<T> toolTippedComboBox (final T[] items, final String text) {
        final var box = new JComboBox<>(items);
        box.setToolTipText(text);
        return box;
    }

    @Override
    public int getRowCount () {
        return ROW_COUNT;
    }

    @Override
    public int getColumnCount () {
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt (final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case TYPE -> {
                final var name = this.returnNameForIndex(rowIndex);
                if (name != null) {
                    return name;
                }
            }
            case VALUE -> {
                final var val = this.getValueForMachine(rowIndex);
                if (val != null) {
                    return val;
                }
            }
        }
        throw new IndexOutOfBoundsException("ColumnIndex out of bounds");
    }

    @Override
    public String getColumnName (final int columnIndex) {
        return switch (columnIndex) {
            case TYPE -> this.getString("Properties");
            case VALUE -> this.getString("Values");
            default -> null;
        };
    }

    private @NotNull String getString (final String Properties) {
        return this.words.getString(Properties);
    }

    @Override
    public boolean isCellEditable (final int rowIndex, final int columnIndex) {
        return columnIndex != TYPE;
    }

    @Override
    public void setValueAt (final Object aValue, final int rowIndex, final int columnIndex) {
        if (columnIndex != VALUE || this.machine == null) {
            return;
        }

        this.setValueAtIndex(aValue, rowIndex);
        this.fireTableCellUpdated(rowIndex, VALUE);
    }

    private JButton setButton () {
        final var button = new JButton();
        button.addActionListener(new ButtonActionListener());
        button.setToolTipText("Select the nodes that will be coordinated by this VMM");
        return button;
    }

    void setMaquina (final Machine machine, final Iterable<String> users) {
        this.machine = machine;
        this.allocators.setSelectedItem(this.machine.getVmmAllocationPolicy());

        this.schedulers.setSelectedItem(this.machine.getSchedulingAlgorithm());
        this.users.removeAllItems();
        for (final var s : users) {
            this.users.addItem(s);
        }
        this.users.setSelectedItem(machine.getOwner());
        this.slaveList.setVisible(false);
        this.slaves.setText(machine.getSlaves().toString());
    }

    private String returnNameForIndex (final int rowIndex) {
        return switch (rowIndex) {
            case LABEL -> this.getString("Label");
            case OWNER -> this.getString("Owner");
            case PROCESSOR -> "%s (Mflop/s)".formatted(this.getString("Computing power"));
            case LOAD_FACTOR -> this.getString("Load Factor");
            case RAM -> "Primary Storage";
            case DISK -> "Secondary Storage";
            case CORES -> "Cores";
            case VMM -> "VMM";
            case SCHEDULER -> this.getString("Scheduling algorithm");
            case SLAVE -> "Slave Nodes";
            case COST_PER_PROCESSOR -> "Cost per Processing";
            case COST_PER_MEMORY -> "Cost per Memory";
            case COST_PER_DISK -> "Cost per Disk";
            case VMM_POLICY -> "VMM allocated policy";
            default -> null;
        };
    }

    private Object getValueForMachine (final int rowIndex) {
        if (this.machine == null) {
            return switch (rowIndex) {
                case OWNER -> this.users;
                case SCHEDULER -> this.schedulers;
                case SLAVE -> this.slaves;
                case VMM_POLICY -> this.allocators;
                default -> "null";
            };
        }

        return switch (rowIndex) {
            case LABEL -> this.machine.getId().getName();
            case OWNER -> this.users;
            case PROCESSOR -> this.machine.getComputationalPower();
            case LOAD_FACTOR -> this.machine.getLoadFactor();
            case RAM -> this.machine.getRam();
            case DISK -> this.machine.getHardDisk();
            case CORES -> this.machine.getCoreCount();
            case VMM -> this.machine.isMaster();
            case SCHEDULER -> this.schedulers;
            case SLAVE -> this.slaves;
            case COST_PER_PROCESSOR -> this.machine.getCostPerProcessing();
            case COST_PER_MEMORY -> this.machine.getCostPerMemory();
            case COST_PER_DISK -> this.machine.getCostPerDisk();
            case VMM_POLICY -> this.allocators;
            default -> null;
        };
    }

    private void setValueAtIndex (final Object aValue, final int rowIndex) {
        switch (rowIndex) {
            case LABEL -> this.machine.getId().setName(aValue.toString());
            case OWNER -> this.machine.setOwner(this.users
                                                                     .getSelectedItem()
                                                                     .toString());
            case PROCESSOR -> this.machine.setComputationalPower(Double.valueOf(
                aValue.toString()));
            case LOAD_FACTOR -> this.machine.setLoadFactor(Double.valueOf(aValue.toString()));
            case RAM -> this.machine.setRam(Double.valueOf(aValue.toString()));
            case DISK -> this.machine.setHardDisk(Double.valueOf(aValue.toString()));
            case CORES -> this.machine.setCoreCount(Integer.valueOf(aValue.toString()));
            case VMM -> this.machine.setMaster(Boolean.valueOf(aValue.toString()));
            case SCHEDULER -> this.machine.setSchedulingAlgorithm(
                this.schedulers.getSelectedItem().toString());
            case COST_PER_PROCESSOR -> this.machine.setCostPerProcessing(
                Double.valueOf(aValue.toString()));
            case COST_PER_MEMORY -> this.machine.setCostPerMemory(Double.valueOf(
                aValue.toString()));
            case COST_PER_DISK -> this.machine.setCostPerDisk(Double.valueOf(aValue.toString()));
            case VMM_POLICY -> this.machine.setVmmAllocationPolicy(
                this.allocators.getSelectedItem().toString());
        }
    }

    public JComboBox getEscalonadores () {
        return this.schedulers;
    }

    public JComboBox getAlocadores () {
        return this.allocators;
    }

    public void setPalavras (final ResourceBundle words) {
        this.words = words;
        this.fireTableStructureChanged();
    }

    private class ButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed (final ActionEvent evt) {
            this.calculateThings();
            this.updateThings();
        }

        private void calculateThings () {
            if (MachineTableIaaS.this.slaveList.isVisible()) {
                return;
            }

            final var modelList = new DefaultListModel<GridItem>();
            final var connectedList = MachineTableIaaS.this.machine.connectedSchedulableNodes();

            for (final var item : connectedList) {
                modelList.addElement(item);
            }

            MachineTableIaaS.this.slaveList.setModel(modelList);

            MachineTableIaaS.this.machine.getSlaves().stream()
                .mapToInt(connectedList::indexOf)
                .forEachOrdered(
                    i -> MachineTableIaaS.this.slaveList.addSelectionInterval(i, i));

            MachineTableIaaS.this.slaveList.setVisible(true);
        }

        private void updateThings () {
            if (MachineTableIaaS.this.slaveList.getModel().getSize() <= 0) {
                return;
            }

            final int option = JOptionPane.showConfirmDialog(
                MachineTableIaaS.this.slaves, MachineTableIaaS.this.slaveList, "Select the slaves",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            MachineTableIaaS.this.machine.setSlaves(
                new ArrayList<>(MachineTableIaaS.this.slaveList.getSelectedValuesList()));

            MachineTableIaaS.this.slaves.setText(MachineTableIaaS.this.machine
                                                     .getSlaves()
                                                     .toString());
        }
    }
}