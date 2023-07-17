package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TerminalApplicationCharacterizationTest {

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private static TerminalApplication createTerminalApplication (final String... args) {
        return new TerminalApplication(args);
    }

    private static void runTerminalApplicationWith (final String... args) {
        createTerminalApplication(args).run();
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
        runTerminalApplicationWith("-h");

        verify(this.outputStream);
    }

    @Test
    void givenHelpAndVersionArgs_whenRun_thenPrintsHelp () {
        runTerminalApplicationWith("-h", "-v");

        verify(this.outputStream);
    }

    @Test
    void givenVersionArg_whenRun_thenPrintsVersion () {
        runTerminalApplicationWith("-v");

        verify(this.outputStream);
    }

    @Test
    void givenVersionAndEmptyFilePathArg_whenRun_thenPrintsVersion () {
        runTerminalApplicationWith("-v", "");

        verify(this.outputStream);
    }

    @Test
    void givenEmptyFilePath_whenRun_thenPrintsError () {
        runTerminalApplicationWith("");

        verify(this.outputStream);
    }

    @Test
    void givenEmptyFileWithNoExtension_whenRun_thenPrintsError () {
        runTerminalApplicationWith("src/test/resources/models/emptyFile");

        verify(this.outputStream);
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }
}
