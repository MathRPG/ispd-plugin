package ispd.motor;

import ispd.arquivo.interpretador.internal.iconic.InterpretadorIconico;
import ispd.arquivo.interpretador.internal.simulable.InterpretadorSimulavel;
import ispd.arquivo.xml.IconicoXML;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 * Classe de conexão entre interface de usuario e motor de simulação
 */
public abstract class ProgressoSimulacao {

    public abstract void incProgresso (int n);

    public abstract void print (String text, Color cor);

    /**
     * Escreve os arquivos com os modelos icônicos e simuláveis, e realiza a analise e validação dos
     * mesmos
     *
     * @param iconicModel
     *     Texto contendo o modelo icônico que será analisado
     */
    public void AnalisarModelos (final String iconicModel) {
        final var file = new File("modeloiconico");

        this.doTask("Writing iconic model.", () -> this.writeIconicModel(iconicModel, file));

        final var parser = new InterpretadorIconico();

        this.doTask("Interpreting iconic model.", () -> parser.leArquivo(file));

        this.doTask("Writing simulation model.", parser::escreveArquivo);

        this.doTask("Interpreting simulation model.", this::interpretSimulationModel);
    }

    private void doTask (final String taskName, final Runnable task) {
        this.printTaskName(taskName);
        task.run();
        this.incProgresso(5);
        this.println("OK", Color.green);
    }

    private void writeIconicModel (final String model, final File file) {
        try (
            final var fw = new FileWriter(file, StandardCharsets.UTF_8);
            final var pw = new PrintWriter(fw, true)
        ) {
            pw.print(model);
        } catch (final IOException ex) {
            Logger.getLogger(ProgressoSimulacao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void interpretSimulationModel () {
        final var parser = new InterpretadorSimulavel();
        final var stdout = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        parser.leArquivo(new File("modelosimulavel"));
        System.setOut(stdout);
    }

    private void printTaskName (final String text) {
        this.print(text);
        this.print(" -> ");
    }

    public void println (final String text, final Color cor) {
        this.print(text, cor);
        this.print("\n", cor);
    }

    public void print (final String text) {
        this.print(text, Color.black);
    }

    /**
     * @throws IllegalArgumentException
     */
    public void validarInicioSimulacao (final Document model) {
        this.printTaskName("Verifying configuration of the icons.");

        if (null == model) {
            this.printAndThrow(new IllegalArgumentException("The model has no icons."));
            return;
        }

        try {
            IconicoXML.validarModelo(model);
        } catch (final IllegalArgumentException e) {
            this.printAndThrow(e);
        }

        this.incProgresso(5);
        this.println("OK", Color.green);
    }

    private void printAndThrow (final IllegalArgumentException x) {
        this.println("Error!", Color.red);
        throw x;
    }

    public void println (final String text) {
        this.print(text, Color.black);
        this.print("\n", Color.black);
    }
}