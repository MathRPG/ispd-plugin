package ispd.arquivo.interpretador.simgrid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;

public class InterpretadorSimGrid {

    private static String fname = null;

    private Document modelo = null;

    public static String getFileName () {
        return InterpretadorSimGrid.fname;
    }

    private void setFileName (final File f) {
        InterpretadorSimGrid.fname = f.getName();
    }

    public void interpreta (final File file1, final File file2) {
        try {
            try {
                final var application_file = new FileInputStream(file1);
                final var plataform_file   = new FileInputStream(file2);
                final var parser           = SimGrid.getInstance(application_file);
                this.setFileName(file1);
                SimGrid.ReInit(application_file);
                SimGrid.modelo();
                this.setFileName(file2);
                SimGrid.ReInit(plataform_file);
                SimGrid.modelo();
                final var error = parser.resultadoParser();
                if (!error) {
                    this.modelo = parser.getModelo().getDescricao();
                }
                parser.reset();
            } catch (final ParseException ex) {
                JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (final IOException ex) {
            JOptionPane.showMessageDialog(
                null,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public Document getModelo () {
        return this.modelo;
    }
}
