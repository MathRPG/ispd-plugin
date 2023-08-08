package ispd.application.terminal;

import static ispd.application.terminal.HasMessageIn.*;
import static org.approvaltests.Approvals.verify;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import ispd.annotations.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.commons.cli.*;
import org.hamcrest.core.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class TerminalApplicationCharacterizationTest {

    public static final Path MODEL_FOLDER_PATH = Path.of("src", "test", "resources", "models");

    private static final CharSequence FILE_NAME_DELIMITER = "_";

    private static final String[] NO_OPTIONS = {};

    private static final Pattern SPACE_MATCHER = Pattern.compile(" ");

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private final SystemTimeProvider systemTimeProvider = mock();

    @RefactorOnNextJavaLts(reason = "Use Pattern Matching.")
    private static String @Nullable [] convertToOptionArray (final CharSequence spacedOptions) {
        if (spacedOptions == null) {
            return null;
        }
        if (spacedOptions.isEmpty()) {
            return NO_OPTIONS;
        }
        return SPACE_MATCHER.split(spacedOptions);
    }

    private TerminalApplication initTerminalApplication (final CharSequence spacedOptions) {
        return new TerminalApplication(
            convertToOptionArray(spacedOptions),
            this.systemTimeProvider
        );
    }

    private void runTerminalApplication (final CharSequence spacedOptions) {
        this.initTerminalApplication(spacedOptions).run();
    }

    private void runApplicationOnModelWith (
        final String users,
        final String icons,
        final String load
    ) {
        final String modelName = String.join(FILE_NAME_DELIMITER, users, icons, load);
        this.runTerminalApplication(ModelFolder.GRID.pathToModel(modelName));
    }

    private String systemOutContents () {
        return this.outStream.toString();
    }

    private <T> CombinableMatcher<Throwable> hasMessageInSysOut_andIsOfType (final Class<T> type) {
        return both(hasMessageIn(this.outStream.toString())).and(is(instanceOf(type)));
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
            () -> this.initTerminalApplication(options)
        );

        assertThat(exception, hasMessageIn(this.systemOutContents()));

        verify(this.outStream);
    }

    @Test
    void givenUnrecognizedOption_thenThrowsOnInit () {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> this.initTerminalApplication("-z")
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(UnrecognizedOptionException.class));

        verify(this.outStream);
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
            () -> this.initTerminalApplication(options)
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(MissingArgumentException.class));
    }

    @Test
    void givenOptionWithMissingArgument_thenPrintsErrorOnInit () {
        try {
            this.initTerminalApplication("-P");
        } catch (final RuntimeException ignored) {
            // ... throwing behavior already tested
        }

        verify(this.outStream);
    }

    @Test
    void givenInvalidAddress_thenThrowsOnInit () {
        final var cause = assertThrows(
            IllegalArgumentException.class,
            () -> this.initTerminalApplication("-a NotAnAddress")
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(UnknownHostException.class));

        verify(this.outStream);
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
            () -> this.initTerminalApplication(options)
        ).getCause();

        assertThat(cause, is(instanceOf(NumberFormatException.class)));

        verify(this.outStream);
    }

    @Test
    void givenValidOptions_thenPrintsNothingOnInit () {
        this.initTerminalApplication("-h");

        assertThat(
            "Should not print anything to out on valid initialization.",
            this.outStream.toString(),
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
        this.runTerminalApplication(options);

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
        this.runTerminalApplication(options);

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
        this.runTerminalApplication(ModelFolder.NO_TYPE.pathToFile(modelName));

        assertThat(
            "Error message printed to out should contain model name",
            this.outStream.toString(),
            containsString(modelName)
        );

        verify(
            this.outStream.toString().replace(modelName, "")
        );
    }

    @Test
    void givenEmptyModelFile_thenPrintsErrorWhileOpeningModel () {
        this.runTerminalApplication(ModelFolder.NO_TYPE.pathToModel("empty"));

        verify(this.outStream);
    }

    @Test
    void givenModelFileWithInvalidXml_thenPrintsErrorWhileOpeningModel () {
        this.runTerminalApplication(ModelFolder.GRID.pathToModel("malformed"));

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoUsers_thenPrintsErrorAfterOpeningModel () {
        this.runApplicationOnModelWith("noUsers", "noIcons", "noLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoIcons_thenPrintsErrorAfterOpeningModel () {
        this.runApplicationOnModelWith("oneUser", "noIcons", "noLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoLoad_thenPrintsErrorAfterOpeningModel () {
        this.runApplicationOnModelWith("oneUser", "oneMachineIcon", "noLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoMasters_thenPrintsErrorAfterOpeningModel () {
        this.runApplicationOnModelWith("oneUser", "oneMachineIcon", "oneTaskGlobalLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithInvalidSchedulingPolicy_thenThrowsWhileInterpretingModel () {
        final var cause = assertThrowsExactly(
            RuntimeException.class,
            () -> this.runApplicationOnModelWith(
                "oneUser", "oneMachineMasterIcon", "oneTaskGlobalLoad"
            )
        ).getCause();

        assertThat(
            cause,
            both(is(instanceOf(ClassNotFoundException.class)))
                .and(hasProperty("message", containsString("---")))
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(strings = "oneMachineSchedulerIcon")
    void givenModelWithMistake_thenThrowsAfterCreatingTasks (final String icons) {
        assertThrows(
            RuntimeException.class,
            () -> this.runApplicationOnModelWith(
                "oneUser", icons, "oneTaskGlobalLoad"
            )
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "schedulerDefaultLinkSlaveIcons",
            "schedulerDefaultBiLinkSlaveIcons",
        }
    )
    void givenModelWithImproperLinks_thenThrowsAfterCreatingTasks (final String icons) {
        assertThrows(
            LinkageError.class,
            () -> this.runApplicationOnModelWith(
                "oneUser", icons, "oneTaskGlobalLoad"
            )
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(strings = "schedulerValidLinkSlaveIcons")
    void givenModelWithImproperMachineConfig_thenThrowsNumberFormatException (final String icons) {
        assertThrows(
            NumberFormatException.class,
            () -> this.runApplicationOnModelWith(
                "oneUser", icons, "oneTaskGlobalLoad"
            )
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(strings = "schedulerValidLinkValidSlaveIcons")
    void givenValidModel_thenSimulates (final String icons) {
        given(this.systemTimeProvider.getSystemTime()).willReturn(0L, 1L);

        this.runApplicationOnModelWith("oneUser", icons, "oneTaskGlobalLoad");

        verify(this.outStream);
    }
}
