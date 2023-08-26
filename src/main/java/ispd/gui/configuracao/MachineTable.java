package ispd.gui.configuracao;

import static ispd.gui.TextSupplier.*;

import ispd.gui.iconico.*;
import ispd.policy.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MachineTable extends AbstractTableModel {

    private static final int TYPE = 0;

    private static final int VALUE = 1;

    private static final int LABEL = 0;

    private static final int OWNER = 1;

    private static final int PROCESSOR = 2;

    private static final int LOAD_FACTOR = 3;

    private static final int CORES = 4;

    private static final int RAM = 5;

    private static final int DISK = 6;

    private static final int MASTER = 7;

    private static final int SCHEDULER = 8;

    private static final int SLAVE = 9;

    private static final int ENERGY = 10;

    private static final int ROW_COUNT = 11;

    private static final int COLUMN_COUNT = 2;

    private final JButton slaves = this.setButton();

    private final JComboBox<?> schedulers =
        new JComboBox<>(PolicyLoader.NATIVE_GRID_POLICIES.toArray(String[]::new));

    private final JComboBox<String> users = new JComboBox<>();

    private final JList<GridItem> slaveList = new JList<>();

    private Machine machine = null;

    public MachineTable () {
        new CheckListRenderer(this.slaveList);
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
            case TYPE -> getText("Properties");
            case VALUE -> getText("Values");
            default -> null;
        };
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
        return button;
    }

    void setMaquina (final Machine machine, final Iterable<String> users) {
        this.machine = machine;
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
            case LABEL -> getText("Label");
            case OWNER -> getText("Owner");
            case PROCESSOR -> "%s (Mflop/s)".formatted(getText("Computing power"));
            case LOAD_FACTOR -> getText("Load Factor");
            case RAM -> "Primary Storage";
            case DISK -> "Secondary Storage";
            case CORES -> "Cores";
            case MASTER -> getText("Master");
            case SCHEDULER -> getText("Scheduling algorithm");
            case SLAVE -> "Slave Nodes";
            case ENERGY -> "Energy consumption";
            default -> null;
        };
    }

    private Object getValueForMachine (final int rowIndex) {
        if (this.machine == null) {
            return switch (rowIndex) {
                case OWNER -> this.users;
                case SCHEDULER -> this.schedulers;
                case SLAVE -> this.slaves;
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
            case MASTER -> this.machine.isMaster();
            case SCHEDULER -> this.schedulers;
            case SLAVE -> this.slaves;
            case ENERGY -> this.machine.getEnergyConsumption();
            default -> null;
        };
    }

    private void setValueAtIndex (final Object value, final int rowIndex) {
        switch (rowIndex) {
            case LABEL -> this.machine.getId().setName(value.toString());
            case OWNER -> this.machine.setOwner(this.users
                                                                 .getSelectedItem()
                                                                 .toString());
            case PROCESSOR -> this.machine.setComputationalPower(Double.valueOf(value.toString()));
            case LOAD_FACTOR -> this.machine.setLoadFactor(Double.valueOf(value.toString()));
            case RAM -> this.machine.setRam(Double.valueOf(value.toString()));
            case DISK -> this.machine.setHardDisk(Double.valueOf(value.toString()));
            case CORES -> this.machine.setCoreCount(Integer.valueOf(value.toString()));
            case ENERGY -> this.machine.setEnergyConsumption(Double.valueOf(value.toString()));
            case MASTER -> this.machine.setMaster(Boolean.valueOf(value.toString()));
            case SCHEDULER -> this.machine.setSchedulingAlgorithm(
                this.schedulers.getSelectedItem().toString());
        }
    }

    public JComboBox getEscalonadores () {
        return this.schedulers;
    }

    private class ButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed (final ActionEvent evt) {
            this.calculateThings();
            this.updateThings();
        }

        private void calculateThings () {
            if (MachineTable.this.slaveList.isVisible()) {
                return;
            }

            final var modelList = new DefaultListModel<GridItem>();
            final var connectedList = MachineTable.this.machine.connectedSchedulableNodes();

            for (final var item : connectedList) {
                modelList.addElement(item);
            }

            MachineTable.this.slaveList.setModel(modelList);

            MachineTable.this.machine.getSlaves().stream()
                .mapToInt(connectedList::indexOf)
                .forEachOrdered(i -> MachineTable.this.slaveList.addSelectionInterval(i, i));

            MachineTable.this.slaveList.setVisible(true);
        }

        private void updateThings () {
            if (MachineTable.this.slaveList.getModel().getSize() <= 0) {
                return;
            }

            final int option = JOptionPane.showConfirmDialog(
                MachineTable.this.slaves, MachineTable.this.slaveList, "Select the slaves",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            MachineTable.this.machine.setSlaves(new ArrayList<>(MachineTable.this.slaveList.getSelectedValuesList()));

            MachineTable.this.slaves.setText(MachineTable.this.machine.getSlaves().toString());
        }
    }
}