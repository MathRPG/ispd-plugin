package ispd.application.terminal;

import static org.approvaltests.Approvals.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.stream.events.Namespace;
import org.approvaltests.Approvals;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalApplicationCharacterizationTest {

    private static final String[] NO_ARGS = {};

    private static final Pattern SPACE_MATCHER = Pattern.compile(" ");

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private static String[] convertToArgList (final CharSequence spaceSeparatedArgs) {
        if (spaceSeparatedArgs == null) {
            return null;
        }
        if (spaceSeparatedArgs.isEmpty()) {
            return TerminalApplicationCharacterizationTest.NO_ARGS;
        }
        return TerminalApplicationCharacterizationTest.SPACE_MATCHER.split(spaceSeparatedArgs);
    }

    private static TerminalApplication initTerminalApplication (final CharSequence argString) {
        return new TerminalApplication(convertToArgList(argString));
    }

    private static void runTerminalApplication (final CharSequence argString) {
        initTerminalApplication(argString).run();
    }

    private static @NotNull String makePathToModel (final String modelName) {
        return Paths.get("src", "test", "resources", "models", modelName).toString();
    }

    private @NotNull Map<String, Object> mapOfExceptionAndOut (final Exception exception) {
        return Map.of(
            "ex", exception, // TODO: Change
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings = {
            "-z", // Unrecognized command
            "-P",
            "-P NaN",
            "-e",
            "-e NaN",
            "-t",
            "-t NaN",
//            "-e 0 -t 0", // system.exit

//            "-t 0",
//            "-t -1",
//            "-a",
//            "-a NotAnAddress",
//            "-e -1", // should probably fail
//            "-t -1", // should probably fail
//            "-h -P -1", // can construct, probably fails at run
        }
    )
    void givenInvalidArgs_whenInitialized_thenThrowsAndPrintsError (final String args) {
        final var exception = assertThrows(
            Exception.class,
            () -> initTerminalApplication(args)
        );

        verify(this.mapOfExceptionAndOut(exception), Approvals.NAMES.withParameters(args));
    }

    @Test
    void givenValidArgs_whenInitialized_thenDoesNotPrintToOut () {
        initTerminalApplication("-h");

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
        runTerminalApplication(joinedArgs);

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
        runTerminalApplication(joinedArgs);

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
        final var path = makePathToModel(modelName);

        initTerminalApplication(path).run();

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
        final var path = makePathToModel(modelName);

        final var exception = assertThrows(
            RuntimeException.class,
            () -> runTerminalApplication(path)
        );

        verify(
            this.mapOfExceptionAndOut(exception),
            Approvals.NAMES.withParameters(modelName)
        );
    }
}
