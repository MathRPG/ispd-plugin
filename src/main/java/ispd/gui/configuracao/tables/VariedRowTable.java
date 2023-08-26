package ispd.gui.configuracao.tables;

import ispd.gui.configuracao.CellEditor;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class VariedRowTable extends JTable {

    private final HashMap<Integer, CellEditor> editors = new HashMap<>();

    @Override
    public void setModel (final TableModel dataModel) {
        super.setModel(dataModel);
        for (int i = 0; i < this.getModel().getRowCount(); i++) {
            final var item = this.getModel().getValueAt(i, 1);
            if (item instanceof final JComponent component) {
                this.editors.put(i, new CellEditor(component));
            }
        }
    }

    @Override
    public TableCellRenderer getCellRenderer (final int row, final int column) {
        final var value = this.getModel().getValueAt(row, column);
        if (value != null) {
            return this.getDefaultRenderer(value.getClass(), row);
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor (final int row, final int column) {
        final var value = this.getModel().getValueAt(row, column);
        if (value != null) {
            return this.getDefaultEditor(value.getClass(), row);
        }
        return super.getCellEditor(row, column);
    }

    private TableCellEditor getDefaultEditor (final Class<?> columnClass, final int row) {
        if (columnClass == JComboBox.class || columnClass == JButton.class) {
            return this.editors.get(row);
        } else {
            return this.getDefaultEditor(columnClass);
        }
    }

    private TableCellRenderer getDefaultRenderer (final Class<?> columnClass, final int row) {
        if (columnClass == JComboBox.class || columnClass == JButton.class) {
            return this.editors.get(row);
        } else {
            return this.getDefaultRenderer(columnClass);
        }
    }
}