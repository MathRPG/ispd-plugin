package ispd.application.terminal;

import static org.approvaltests.Approvals.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.approvaltests.Approvals;
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
    void givenEmptyArgs_whenConstructed_thenThrowsException () {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TerminalApplication(TerminalApplicationCharacterizationTest.NO_ARGS)
        );

        verify(exception);
    }

    @Test
    void givenEmptyArgs_whenConstructed_thenPrintsMessageToStandardOut () {
        try {
            new TerminalApplication(TerminalApplicationCharacterizationTest.NO_ARGS);
        } catch (final RuntimeException ignored) {
            // out of test scope
        }

        final var expected = "It needs a model to simulate.";
        final var actual   = this.outputStream.toString().trim();

        assertEquals(expected, actual, "Should print message");

        verify(this.outputStream);
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }
}
