package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.approvaltests.Approvals;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalApplicationCharacterizationTest {

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private static TerminalApplication createTerminalApplication (final String... args) {
        return new TerminalApplication(args);
    }

    private static void runTerminalApplicationWith (final String... args) {
        createTerminalApplication(args).run();
    }

    private static @NotNull String pathToModel (final String modelName) {
        return Paths.get("src", "test", "resources", "models", modelName).toString();
    }

    @BeforeEach
    void replaceSystemOut () {
        System.setOut(new PrintStream(this.outStream));
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

        verify(this.outStream);
    }

    @Test
    void givenValidArgs_whenConstructed_thenDoesNothing () {
        createTerminalApplication("-h");

        verify(this.outStream);
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
        final var args = joinedArgs.split(" ");
        runTerminalApplicationWith(args);

        verify(this.outStream, Approvals.NAMES.withParameters(joinedArgs));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            // Empty and incomplete Files
            "emptyFile",
            "emptyFile.txt",
            "emptyFile.imsx",
            "incompleteIaaSModel.imsx",
            // Grid models
            "emptyGridModel.imsx",
            "emptyGridModelWithSingleUser.imsx",
            "gridModelWithSingleMachineIcon.imsx",
            "gridModelWithSingleTask.imsx",
            // Iaas models
            "emptyIaaSModel.imsx",
            "emptyIaaSModelWithSingleUser.imsx",
            "iaasModelWithSingleMachineIcon.imsx",
            "iaasModelWithSingleTask.imsx",
            // Paas models
            "emptyPaaSModel.imsx",
            "emptyPaaSModelWithSingleUser.imsx",
            "paasModelWithSingleMachineIcon.imsx",
            "paasModelWithSingleTask.imsx",
        }
    )
    void givenArgsWithModels_whenRun_thenBehavesAsValidated (final String modelName) {
        runTerminalApplicationWith(pathToModel(modelName));

        verify(this.outStream, Approvals.NAMES.withParameters(modelName));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "gridModelWithSingleMaster.imsx",
            "iaasModelWithSingleMaster.imsx",
            "paasModelWithSingleMaster.imsx",
        }
    )
    void givenModelWithInvalidPolicy_whenRun_thenThrowsException (final String modelName) {
        final var path = pathToModel(modelName);

        final var exception = assertThrows(
            RuntimeException.class,
            () -> runTerminalApplicationWith(path)
        );

        verify(
            Map.of(
                "ex", exception,
                "out", this.outStream
            ),
            Approvals.NAMES.withParameters(modelName)
        );
    }
}
