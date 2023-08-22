package ispd.gui.configuracao;

import static ispd.gui.BundleManager.*;

import ispd.gui.iconico.grade.*;
import java.util.*;
import javax.swing.table.*;

public class LinkTable extends AbstractTableModel {

    private static final int TYPE = 0;

    private static final int VALUE = 1;

    private static final int LABEL = 0;

    private static final int BANDWIDTH = 1;

    private static final int LATENCY = 2;

    private static final int LOAD_FACTOR = 3;

    private static final int ROW_COUNT = 4;

    private static final int COLUMN_COUNT = 2;

    private GridItem link = null;

    public LinkTable () {
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
                final var name = this.getRowName(rowIndex);
                if (name != null) {
                    return name;
                }
            }
            case VALUE -> {
                final var value = this.getRowValue(rowIndex);
                if (value != null) {
                    return value;
                }
            }
        }

        throw new IndexOutOfBoundsException("columnIndex out of bounds");
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
        if (columnIndex != VALUE || this.link == null) {
            return;
        }

        this.updateValue(aValue, rowIndex);
        this.fireTableCellUpdated(rowIndex, VALUE);
    }

    public void setLink (final GridItem link) {
        this.link = link;
    }

    private String getRowName (final int rowIndex) {
        return switch (rowIndex) {
            case LABEL -> getText("Label");
            case BANDWIDTH -> getText("Bandwidth");
            case LATENCY -> getText("Latency");
            case LOAD_FACTOR -> getText("Load Factor");
            default -> null;
        };
    }

    private Object getRowValue (final int rowIndex) {
        if (this.link == null) {
            return "null";
        }

        switch (rowIndex) {
            case LABEL:
                return this.link.getId().getName();

            case BANDWIDTH:
                if (this.link instanceof Link) {
                    return ((Link) this.link).getBandwidth();
                } else {
                    return ((Internet) this.link).getBandwidth();
                }

            case LATENCY:
                if (this.link instanceof Link) {
                    return ((Link) this.link).getLatency();
                } else {
                    return ((Internet) this.link).getLatency();
                }

            case LOAD_FACTOR:
                if (this.link instanceof Link) {
                    return ((Link) this.link).getLoadFactor();
                } else {
                    return ((Internet) this.link).getLoadFactor();
                }
        }

        return null;
    }

    private void updateValue (final Object aValue, final int rowIndex) {

        if (rowIndex == LABEL) {
            this.link.getId().setName(aValue.toString());
            return;
        }

        final var value = Double.parseDouble(aValue.toString());

        switch (rowIndex) {
            case BANDWIDTH:
                if (this.link instanceof Link) {
                    ((Link) this.link).setBandwidth(value);
                } else {
                    ((Internet) this.link).setBandwidth(value);
                }
                break;
            case LATENCY:
                if (this.link instanceof Link) {
                    ((Link) this.link).setLatency(value);
                } else {
                    ((Internet) this.link).setLatency(value);
                }
                break;
            case LOAD_FACTOR:
                if (this.link instanceof Link) {
                    ((Link) this.link).setLoadFactor(value);
                } else {
                    ((Internet) this.link).setLoadFactor(value);
                }
                break;
        }
    }

    public void setPalavras (final ResourceBundle words) {
        this.fireTableStructureChanged();
    }
}