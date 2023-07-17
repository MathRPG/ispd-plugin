package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalApplicationCharacterizationTest {

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private static void createTerminalApplication (final String... args) {
        new TerminalApplication(args);
    }

    @BeforeEach
    void replaceSystemOut () {
        System.setOut(new PrintStream(this.outputStream));
    }

    @Test
    void givenEmptyArgs_whenConstructed_thenThrowsException () {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            TerminalApplicationCharacterizationTest::createTerminalApplication
        );

        verify(exception);
    }

    @Test
    void givenEmptyArgs_whenConstructed_thenPrintsMessageToStandardOut () {
        try {
            createTerminalApplication();
        } catch (final RuntimeException ignored) {
            // out of test scope
        }

        verify(this.outputStream);
    }

    @Test
    void givenValidArgs_whenConstructed_thenDoesNothing () {
        createTerminalApplication("-h");

        verify(this.outputStream);
    }

    @Test
    void givenHelpArg_whenRun_thenPrintsHelp () {
        new TerminalApplication(new String[]{"-h"}).run();

        verify(this.outputStream);
    }

    @Test
    void givenHelpAndVersionArgs_whenRun_thenPrintsHelp () {
        runTerminalApplicationWith("-h", "-v");

        verify(this.outputStream);
    }

    private static void runTerminalApplicationWith (final String... args) {
        new TerminalApplication(args).run();
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }
}
