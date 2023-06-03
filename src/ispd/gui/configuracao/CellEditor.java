package ispd.gui.configuracao;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class CellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    private final JComponent item;

    public CellEditor (final JComponent item) {
        this.item = item;
    }

    @Override
    public Component getTableCellEditorComponent (
            final JTable table, final Object value, final boolean isSelected, final int row, final int column
    ) {
        return this.item;
    }

    @Override
    public Component getTableCellRendererComponent (
            final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
            final int row, final int column
    ) {
        return this.item;
    }

    @Override
    public Object getCellEditorValue () {
        return this.item;
    }
}