package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import org.approvaltests.Approvals;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
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

    private static @NotNull @NonNls String pathToModel (final @NonNls String modelName) {
        return Paths.get("src", "test", "resources", "models", modelName).toString();
    }

    private static @NotNull String[] makePathOfFirstValue (final String[] rawArgs) {
        return Stream.concat(
            Arrays.stream(rawArgs, 0, 1)
                .map(TerminalApplicationCharacterizationTest::pathToModel),
            Arrays.stream(rawArgs, 1, rawArgs.length)
        ).toArray(String[]::new);
    }

    @BeforeEach
    void replaceSystemOut () {
        System.setOut(new PrintStream(this.outputStream));
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
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

    @ParameterizedTest
    @ValueSource(
        strings = {
            "emptyFile",
            "emptyFile.txt",
            "emptyFile.imsx",
        }
    )
    void givenArgsWithModels_whenRun_thenBehavesAsValidated (final String joinedArgs) {
        final var rawArgs       = joinedArgs.split(" ");
        final var processedArgs = makePathOfFirstValue(rawArgs);

        runTerminalApplicationWith(processedArgs);

        verify(this.outputStream, Approvals.NAMES.withParameters(joinedArgs));
    }
}
