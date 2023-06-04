package ispd.application.terminal;

import org.bouncycastle.util.Strings;

/**
 * Provides and enumeration of constants used to manipulate the colors of text and background, when
 * displayed in a terminal window.
 * <p>
 * Use the {@link #toString()} method to generate a "<i>command string</i>" that sets the terminal's
 * color to that specified by the enumeration instance.
 * <p>
 * Example:
 * <pre>
 * System.out.printf("Normal Text %s Now Green %s Back to Normal", {@link #GREEN}, {@link #RESET});
 * </pre>
 * <b>Warning:</b> This class and its instances are not responsible for the actual color-changing
 * behavior, it is merely a convenient way to encode the commands required for doing so. The desired
 * functionality may not be available in all platforms.
 */
public enum ConsoleColors {
    /**
     * Resets the console colors to their default.
     */
    RESET("0"),

    /**
     * Sets the console color to green.
     */
    GREEN("0;32");

    private static final String COMMAND_TEMPLATE = "\u001b[%sm";

    private final String commandSequence;

    ConsoleColors (final String command) {
        this.commandSequence = ConsoleColors.COMMAND_TEMPLATE.formatted(command);
    }

    @Override
    public String toString () {
        return this.commandSequence;
    }

    private String surround (final String s) {
        return this + s + ConsoleColors.RESET;
    }

    public static String surroundGreen(final String s) {
        return ConsoleColors.GREEN.surround(s);
    }
}
