package ispd.gui.configuracao;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CheckListRenderer extends DefaultListSelectionModel implements ListCellRenderer {

    private static final Color BACKGROUND = null;

    private final JCheckBox checkBox = new JCheckBox();

    private final Color selectionBackground;

    public CheckListRenderer (final JList<?> list) {
        this.selectionBackground = list.getSelectionBackground();
        list.setCellRenderer(this);
        list.setSelectionModel(this);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addMouseListener(new CheckListRendererMouseAdapter());
    }

    @Override
    public Component getListCellRendererComponent (
        final JList list,
        final Object value,
        final int index,
        final boolean isSelected,
        final boolean hasFocus
    ) {
        this.checkBox.setSelected(isSelected);
        if (isSelected) {
            this.checkBox.setBackground(this.selectionBackground);
        } else {
            this.checkBox.setBackground(BACKGROUND);
        }
        this.checkBox.setText(value.toString());
        return this.checkBox;
    }

    private static class CheckListRendererMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked (final MouseEvent event) {
            final var list  = (JList<?>) event.getSource();
            final int index = list.locationToIndex(event.getPoint());
            if (list.isSelectedIndex(index)) {
                list.removeSelectionInterval(index, index);
            } else {
                list.addSelectionInterval(index, index);
            }
        }
    }
}