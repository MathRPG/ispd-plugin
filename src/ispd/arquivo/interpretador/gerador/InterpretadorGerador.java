package ispd.arquivo.interpretador.gerador;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Classe de interface entre arquivos gerados pelo javaCC para interpretar a gramatica do gerador de
 * escalonadores e o iSPD.
 */
public class InterpretadorGerador {

    private final InputStream inputStream;

    private Interpretador parser = null;

    /**
     * @param code
     *     Texto com código do gerador de escalonadores
     */
    public InterpretadorGerador (final String code) {
        this.inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Inicia a analise do código gerador de escalonadores
     *
     * @return erros encontrados no código
     */
    public boolean executarParse () {
        try {
            this.parser = new Interpretador(this.inputStream);
            this.parser.Escalonador();
            return this.parser.isErroEncontrado();
        } catch (final ParseException ex) {
            JOptionPane.showMessageDialog(
                null,
                "Foram encontrados os seguintes erros:\n" + ex.getMessage(),
                "Erros Encontrados",
                JOptionPane.ERROR_MESSAGE
            );
            Logger.getLogger(InterpretadorGerador.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
    }

    /**
     * @return Retorna nome do escalonador gerado
     */
    public String getNome () {
        return this.parser.getArquivoNome();
    }

    /**
     * @return Retorna código java do escalonador gerado a partir do código interpretado
     */
    public String getCodigo () {
        return this.parser.getCodigo();
    }
}
