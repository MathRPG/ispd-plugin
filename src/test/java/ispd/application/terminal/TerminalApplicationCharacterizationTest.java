package ispd.application.terminal;

import static org.approvaltests.Approvals.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class TerminalApplicationCharacterizationTest {

    private static final String[] NO_OPTIONS = {};

    private static final Pattern SPACE_MATCHER = Pattern.compile(" ");

    private static final Path MODEL_FOLDER_PATH = Path.of("src", "test", "resources", "models");

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private static String @Nullable [] convertToOptionArray (final CharSequence spacedOptions) {
        if (spacedOptions == null) {
            return null;
        }
        if (spacedOptions.isEmpty()) {
            return NO_OPTIONS;
        }
        return SPACE_MATCHER.split(spacedOptions);
    }

    private static TerminalApplication initTerminalApplication (final CharSequence spacedOptions) {
        return new TerminalApplication(convertToOptionArray(spacedOptions));
    }

    private static void runTerminalApplication (final CharSequence spacedOptions) {
        initTerminalApplication(spacedOptions).run();
    }

    private static @NotNull String makePathToModel (final String modelName) {
        return Path.of("src", "test", "resources", "models", modelName).toString();
    }

    private @NotNull Map<String, Object> mapOfExceptionAndOut (final Exception exception) {
        return Map.of(
            "ex", exception, // TODO: Change
            "out", this.outStream
        );
    }

    private String stdOutContents () {
        return this.outStream.toString();
    }

    @BeforeEach
    void replaceSystemOut () {
        System.setOut(new PrintStream(this.outStream, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenEmptyOrNullArgs_thenThrowsOnInit (final String options) {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> initTerminalApplication(options)
        );

        verify(this.mapOfExceptionAndOut(exception));
    }

    @Test
    void givenUnrecognizedOption_thenThrowsOnInit () {
        final var exception = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication("-z")
        );

        assertInstanceOf(UnrecognizedOptionException.class, exception.getCause());

        verify(this.mapOfExceptionAndOut(exception));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-P",
            "-e",
            "-t",
            "-a",
        }
    )
    void givenOptionWithMissingArgument_thenThrowsOnInit (final String options) {
        final var underlyingException = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication(options)
        ).getCause();

        assertInstanceOf(MissingArgumentException.class, underlyingException);

        assertTrue(
            this.stdOutContents().contains(underlyingException.getMessage()),
            "Should print exception cause to out."
        );
    }

    @Test
    void givenInvalidAddress_thenThrowsOnInit () {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> initTerminalApplication("-a NotAnAddress")
        );

        assertInstanceOf(UnknownHostException.class, exception.getCause());

        verify(this.mapOfExceptionAndOut(exception));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-P NaN",
            "-e NaN",
            "-t NaN",
        }
    )
    void givenOptionWithInvalidNumberArgument_thenThrowsOnInit (final String options) {
        final var exception = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication(options)
        );

        assertInstanceOf(NumberFormatException.class, exception.getCause());

        verify(this.mapOfExceptionAndOut(exception));
    }

    @Test
    void givenValidArgs_thenPrintsNothingToOutOnInit () {
        initTerminalApplication("-h");

        assertThat(
            "Should not print anything to out on valid initialization.",
            this.stdOutContents(),
            is(emptyString())
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
    void givenHelpArg_whenRun_thenPrintsHelp (final String options) {
        runTerminalApplication(options);

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
    void givenVersionArg_whenRun_thenPrintsVersionInfo (final String options) {
        runTerminalApplication(options);

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "nonexistent.imsx",
            "wrongExtension",
            "wrongExtension.txt",
        }
    )
    void givenNonexistentOrWrongExtensionModel_thenPrintsErrorToOutOnRun (final String modelName) {
        runTerminalApplication(ModelFolder.NO_TYPE.pathTo(modelName));

        assertThat(
            "Error message printed to out should contain model name",
            this.stdOutContents(),
            containsString(modelName)
        );

        verify(
            this.outStream.toString().replace(modelName, "")
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "emptyFile.imsx",
            // Grid models
            "gridMalformedModel.imsx",
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
        runTerminalApplication(makePathToModel(modelName));

        verify(this.outStream, NAMES.withParameters(modelName));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            // Grid Models
            "gridModelWithSingleMaster.imsx",
            "gridModelWithNoSlaves.imsx",
            // IaaS Models
            "iaasModelWithSingleMaster.imsx",
            "iaasModelWithInvalidAllocation.imsx",
            "iaasModelWithNoSlaves.imsx",
            // PaaS Models
            "paasModelWithSingleMaster.imsx",
            "paasModelWithInvalidAllocation.imsx",
            "paasModelWithNoSlaves.imsx",
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
            NAMES.withParameters(modelName)
        );
    }

    private enum ModelFolder {
        NO_TYPE("notype");

        private final Path path;

        ModelFolder (final String folderName) {
            this.path = MODEL_FOLDER_PATH.resolve(folderName);
        }

        private String pathTo (final String modelName) {
            return this.path.resolve(modelName).toString();
        }
    }
}
