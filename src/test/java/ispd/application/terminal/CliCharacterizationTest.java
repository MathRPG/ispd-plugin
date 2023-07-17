package ispd.application.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CliCharacterizationTest {

    private static final String[] NO_ARGS = {};

    @Test
    void emptyArgs () {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TerminalApplication(CliCharacterizationTest.NO_ARGS)
        );

        final var expected = "It needs a model to simulate.";
        final var actual   = exception.getMessage();

        assertEquals(expected, actual, "Exception message doesn't match");
    }
}
