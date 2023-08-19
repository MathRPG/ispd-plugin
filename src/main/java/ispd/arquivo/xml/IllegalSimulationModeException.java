package ispd.arquivo.xml;

public class IllegalSimulationModeException extends IllegalArgumentException {

    public IllegalSimulationModeException (final String mode) {
        super("Invalid simulation mode '%s' found in configuration file.".formatted(mode));
    }
}
