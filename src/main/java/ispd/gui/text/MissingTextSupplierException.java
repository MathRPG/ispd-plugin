package ispd.gui.text;

public class MissingTextSupplierException extends IllegalStateException {

    public MissingTextSupplierException () {
        super("Attempting to call .getText() without call to .setInstance() first.");
    }
}
