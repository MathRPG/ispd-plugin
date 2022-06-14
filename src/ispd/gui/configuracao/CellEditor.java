/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 *  USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * CellEditor.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.gui.configuracao;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

public class CellEditor extends AbstractCellEditor
        implements TableCellEditor, TableCellRenderer {

    private final JComponent item;

    CellEditor(final JComponent item) {
        this.item = item;
    }

    @Override
    public Component getTableCellEditorComponent(
            final JTable jTable,
            final Object o,
            final boolean b,
            final int i,
            final int i1) {
        return this.item;
    }

    @Override
    public Component getTableCellRendererComponent(
            final JTable jTable,
            final Object o,
            final boolean b,
            final boolean b1,
            final int i,
            final int i1) {
        return this.item;
    }

    @Override
    public Object getCellEditorValue() {
        return this.item;
    }
}
