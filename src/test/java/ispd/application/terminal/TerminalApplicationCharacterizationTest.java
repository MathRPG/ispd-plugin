package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    void givenEmptyFileWithNoExtension_whenRun_thenPrintsError () {
        runTerminalApplicationWith("src/test/resources/models/emptyFile");

        verify(this.outputStream);
    }

    @Test
    void givenEmptyFileWithWrongExtension_whenRun_thenPrintsError () {
        runTerminalApplicationWith("src/test/resources/models/emptyFile.txt");

        verify(this.outputStream);
    }

    @Test
    void givenEmptyFileWithRightExtension_whenRun_thenPrintsError () {
        runTerminalApplicationWith("src/test/resources/models/emptyFile.imsx");

        verify(this.outputStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-h",
            "-v",
            "-h -v",
            "-v ",
            "",
        }
    )
    void givenArgs_whenRun_thenBehavesAsVerified (final String joinedArgs) {
        final String[] args = joinedArgs.split(" ");
        runTerminalApplicationWith(args);

        verify(this.outputStream, Approvals.NAMES.withParameters(joinedArgs));
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }
}
