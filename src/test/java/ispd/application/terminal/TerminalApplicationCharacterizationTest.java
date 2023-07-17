package ispd.application.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalApplicationCharacterizationTest {

    private static final String[] NO_ARGS = {};

    private final PrintStream           standardOut  = System.out;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @BeforeEach
    void replaceSystemOut () {
        System.setOut(new PrintStream(this.outputStream));
    }

    @Test
    void emptyArgs () {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TerminalApplication(TerminalApplicationCharacterizationTest.NO_ARGS)
        );

        final var expected = "It needs a model to simulate.";
        final var actual   = exception.getMessage();

        assertEquals(expected, actual, "Exception message doesn't match");
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }
}
