package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Map;
import org.approvaltests.Approvals;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalApplicationCharacterizationTest {

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private static TerminalApplication initTerminalApplicationWith (final String... args) {
        return new TerminalApplication(args);
    }

    private static void runTerminalApplicationWith (final String... args) {
        initTerminalApplicationWith(args).run();
    }

    private static void runTerminalApplicationWithJoinedArgs (final String joinedArgs) {
        final var args = joinedArgs.split(" ");
        runTerminalApplicationWith(args);
    }

    private static @NotNull String pathToModel (final String modelName) {
        return Paths.get("src", "test", "resources", "models", modelName).toString();
    }

    private @NotNull Map<String, Object> mapOfExceptionAndOut (final Exception exception) {
        return Map.of(
            "ex", exception,
            "out", this.outStream
        );
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
    void givenInvalidArgs_whenInitialized_thenThrowsAndPrintsError () {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            TerminalApplicationCharacterizationTest::initTerminalApplicationWith
        );

        verify(this.mapOfExceptionAndOut(exception));
    }

    @Test
    void givenValidArgs_whenInitialized_thenDoesNotPrintToOut () {
        initTerminalApplicationWith("-h");

        assertEquals(
            "",
            this.outStream.toString(),
            "Should not print to system out after intialization."
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-h",
            "--help",
            "-h -v",
            "-v -h",
            "-h doesNotExist.imsx",
            "doesNotExist.imsx -h",
        }
    )
    void givenHelpArg_whenRun_thenPrintsHelp (final String joinedArgs) {
        runTerminalApplicationWithJoinedArgs(joinedArgs);

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-v",
            "--version",
            "-v doesNotExist.imsx",
            "doesNotExist.imsx -v",
        }
    )
    void givenVersionArg_whenRun_thenPrintsVersionInfo (final String joinedArgs) {
        runTerminalApplicationWithJoinedArgs(joinedArgs);

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            // Non-existent file
            "doesNotExist.imsx",
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
    void givenInvalidModel_whenRun_thenPrintsErrors (final String modelName) {
        final var path = pathToModel(modelName);

        runTerminalApplicationWith(path);

        verify(this.outStream, Approvals.NAMES.withParameters(modelName));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "gridModelWithSingleMaster.imsx",
            "iaasModelWithSingleMaster.imsx",
            "paasModelWithSingleMaster.imsx",
            "iaasModelWithInvalidAllocation.imsx",
            "paasModelWithInvalidAllocation.imsx",
        }
    )
    void givenModelWithInvalidPolicies_whenRun_thenThrowsException (final String modelName) {
        final var path = pathToModel(modelName);

        final var exception = assertThrows(
            RuntimeException.class,
            () -> runTerminalApplicationWith(path)
        );

        verify(
            this.mapOfExceptionAndOut(exception),
            Approvals.NAMES.withParameters(modelName)
        );
    }
}
