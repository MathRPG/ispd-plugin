package ispd.motor;

import ispd.arquivo.xml.utils.*;
import java.awt.*;
import org.w3c.dom.*;

/**
 * Classe de conexão entre interface de usuario e motor de simulação
 */
public abstract class ProgressoSimulacao {

    /**
     * Checks the integrity of the model in the {@link Document}. Performs very simple checks such
     * as if the model has at least one user and machine.
     *
     * @param doc
     *     {@link Document} containing iconic model
     *
     * @throws IllegalArgumentException
     *     if the model is incomplete
     */
    private static void validateIconicModel (final Document doc) {
        final var document = new WrappedDocument(doc);

        if (document.hasNoOwners()) {
            throw new IllegalArgumentException("The model has no users.");
        }

        if (document.hasNoMachines() && document.hasNoClusters()) {
            throw new IllegalArgumentException("The model has no icons.");
        }

        if (document.hasNoLoads()) {
            throw new IllegalArgumentException("One or more workloads have not been configured.");
        }

        if (document.hasNoMasters()) {
            throw new IllegalArgumentException("One or more parameters have not been configured.");
        }
    }

    public abstract void incProgresso (int n);

    public abstract void print (String text, Color cor);

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

        if (model == null) {
            this.printAndThrow(new IllegalArgumentException("The model has no icons."));
            return;
        }

        try {
            validateIconicModel(model);
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