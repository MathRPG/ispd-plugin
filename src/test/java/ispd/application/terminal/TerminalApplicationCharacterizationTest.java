package ispd.application.terminal;

import static ispd.application.terminal.HasMessageIn.*;
import static org.approvaltests.Approvals.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import ispd.annotations.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.cli.*;
import org.hamcrest.core.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class TerminalApplicationCharacterizationTest {

    public static final Path MODEL_FOLDER_PATH = Path.of("src", "test", "resources", "models");

    private static final String[] NO_OPTIONS = {};

    private static final Pattern SPACE_MATCHER = Pattern.compile(" ");

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    @RefactorOnJava20(reason = "Use Pattern Matching.")
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

    private String systemOutContents () {
        return this.outStream.toString();
    }

    private <T> CombinableMatcher<Throwable> hasMessageInSysOut_andIsOfType (final Class<T> type) {
        return both(hasMessageIn(this.systemOutContents())).and(is(instanceOf(type)));
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
    void givenEmptyOrNullOptions_thenThrowsOnInit (final String options) {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> initTerminalApplication(options)
        );

        assertThat(exception, hasMessageIn(this.systemOutContents()));

        verify(this.systemOutContents());
    }

    @Test
    void givenUnrecognizedOption_thenThrowsOnInit () {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication("-z")
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(UnrecognizedOptionException.class));

        verify(this.systemOutContents());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-P",
            "--port",
            "-e",
            "-t",
            "-a",
        }
    )
    void givenOptionWithMissingArgument_thenThrowsOnInit (final String options) {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication(options)
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(MissingArgumentException.class));
    }

    @Test
    void givenOptionWithMissingArgument_thenPrintsErrorOnInit () {
        try {
            initTerminalApplication("-P");
        } catch (final RuntimeException ignored) {
            // ... throwing behavior already tested
        }

        verify(this.systemOutContents());
    }

    @Test
    void givenInvalidAddress_thenThrowsOnInit () {
        final var cause = assertThrows(
            IllegalArgumentException.class,
            () -> initTerminalApplication("-a NotAnAddress")
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(UnknownHostException.class));

        verify(this.systemOutContents());
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
        final var cause = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication(options)
        ).getCause();

        assertThat(cause, is(instanceOf(NumberFormatException.class)));

        verify(this.systemOutContents());
    }

    @Test
    void givenValidOptions_thenPrintsNothingOnInit () {
        initTerminalApplication("-h");

        assertThat(
            "Should not print anything to out on valid initialization.",
            this.systemOutContents(),
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
            "-h nonexistent.imsx",
            "nonexistent.imsx -h",
        }
    )
    void givenHelpOption_thenPrintsHelpWhenRun (final String options) {
        runTerminalApplication(options);

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-v",
            "--version",
            "-v nonexistent.imsx",
            "nonexistent.imsx -v",
        }
    )
    void givenVersionOption_thenPrintsVersionInfoWhenRun (final String options) {
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
    void givenNonexistentOrWrongExtensionModel_thenPrintsErrorWhenRun (final String modelName) {
        runTerminalApplication(ModelFolder.NO_TYPE.pathToFile(modelName));

        assertThat(
            "Error message printed to out should contain model name",
            this.systemOutContents(),
            containsString(modelName)
        );

        verify(
            this.outStream.toString().replace(modelName, "")
        );
    }

    @Test
    void givenEmptyModelFile_thenPrintsErrorWhileOpeningModel () {
        runTerminalApplication(ModelFolder.NO_TYPE.pathToModel("empty"));

        verify(this.outStream);
    }

    @Test
    void givenModelFileWithInvalidXml_thenPrintsErrorWhileOpeningModel () {
        runTerminalApplication(ModelFolder.GRID.pathToModel("malformed"));

        verify(this.outStream);
    }

    @ParameterizedTest
    @CsvSource(
        {
            "noIcons,noLoad",
            "noIcons,oneTaskGlobalLoad",
            "oneMasterIcon,noLoad",
            "oneMasterIcon,oneTaskGlobalLoad",
        }
    )
    void givenModelWithNoUsers_thenPrintsErrorAfterOpeningModel (
        final String icons, final String load
    ) {
        final String modelName = String.join("_", "noUsers", icons, load);
        runTerminalApplication(ModelFolder.GRID.pathToModel(modelName));

        verify(this.outStream);
    }

    @ParameterizedTest
    @CsvSource(
        {
            "oneUser,noLoad",
            "oneUser,oneTaskGlobalLoad",
            "twoUsers,noLoad",
            "twoUsers,oneTaskGlobalLoad",
        }
    )
    void givenModelWithNoIcons_thenPrintsErrorAfterOpeningModel (
        final String users, final String load
    ) {
        final String modelName = String.join("_", users, "noIcons", load);
        runTerminalApplication(ModelFolder.GRID.pathToModel(modelName));

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            // Grid models
            "gridModelWithSingleMachineIcon.imsx",
            "gridModelWithSingleTask.imsx",
            // Iaas models
            "iaasModelWithSingleMachineIcon.imsx",
            "iaasModelWithSingleTask.imsx",
            // Paas models
            "paasModelWithSingleMachineIcon.imsx",
            "paasModelWithSingleTask.imsx",
        }
    )
    void givenInvalidModel_thenPrintsErrorsWhenRun (final String modelName) {
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
    void givenModelWithInvalidPolicies_thenThrowsExceptionWhenRun (final String modelName) {
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
}
