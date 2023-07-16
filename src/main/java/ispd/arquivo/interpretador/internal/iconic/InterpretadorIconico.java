package ispd.arquivo.interpretador.internal.iconic;

import java.io.File;
import java.io.FileInputStream;
import javax.swing.JOptionPane;

public class InterpretadorIconico {

    private Interpretador parser = null;

    public boolean leArquivo (final File arquivo) {
        try {
            try {
                this.parser = new Interpretador(new FileInputStream(arquivo));
                this.parser.setVerbose(false);
                this.parser.printv("Modo verbose ligado");
                this.parser.Modelo();
            } catch (final ParseException e) {
                this.parser.setErroEncontrado(true);
                JOptionPane.showOptionDialog(
                    null,
                    "Foram encontrados os seguintes erros:\n" + e.getMessage(),
                    "Erros Encontrados",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null
                );
                return this.parser.isErroEncontrado();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return this.parser.isErroEncontrado();
        }

        return this.parser.isErroEncontrado();
    }

    public void escreveArquivo () {
        this.parser.escreveArquivo();
    }
}