package ispd.application.terminal;

import static ispd.application.terminal.HasMessageIn.*;
import static org.approvaltests.Approvals.verify;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import ispd.annotations.*;
import ispd.policy.loaders.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.commons.cli.*;
import org.approvaltests.*;
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

    private void runApplicationOnModelWith (final String icons) {
        this.runApplicationOnModelWith("oneUser", icons, "oneTaskGlobalLoad");
    }

    private String systemOutContents () {
        return this.outStream.toString();
    }

    private <T> CombinableMatcher<Throwable> hasMessageInSysOut_andIsOfType (final Class<T> type) {
        return both(hasMessageIn(this.systemOutContents()))
            .and(is(instanceOf(type)));
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
    void givenEmptyOrNullOptions_whenInit_thenThrowsAndPrints (final String options) {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> this.initTerminalApplication(options)
        );

        assertThat(exception, hasMessageIn(this.systemOutContents()));

        verify(this.outStream);
    }

    @Test
    void givenUnrecognizedOption_whenInit_thenThrowsAndPrints () {
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
    void givenOptionWithMissingArgument_whenInit_thenThrowsAndPrints (final String options) {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> this.initTerminalApplication(options)
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(MissingArgumentException.class));
    }

    @Test
    void givenInvalidAddress_whenInit_thenThrowsAndPrints () {
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
    void givenValidOptions_whenInit_thenPrintsNothingToStandardOut () {
        final var ignored = this.initTerminalApplication("-h");

        assertThat(
            "Should not print anything to standard out on valid initialization.",
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
    void givenHelpOption_whenRun_thenPrintsHelp (final String options) {
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
    void givenVersionOption_whenRun_thenPrintsVersionInfo (final String options) {
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
            this.systemOutContents(),
            containsString(modelName)
        );

        verify(
            this.systemOutContents().replace(modelName, "")
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
        this.runApplicationOnModelWith("oneMachineIcon");

        verify(this.outStream);
    }

    @Test
    void givenModelWithInvalidSchedulingPolicy_thenThrowsWhileInterpretingModel () {
        final var exception = assertThrowsExactly(
            UnknownPolicyException.class,
            () -> this.runApplicationOnModelWith("oneMachineMasterIcon")
        );

        assertThat(exception, hasProperty("message", containsString("---")));

        verify(this.outStream);
    }

    @Test
    void givenModelWithMistake_thenThrowsAfterCreatingTasks () {
        assertThrows(
            RuntimeException.class,
            () -> this.runApplicationOnModelWith("oneMachineSchedulerIcon")
        );

        verify(this.outStream);
    }

    @Test
    void givenModelWithImproperLinks_thenThrowsAfterCreatingTasks () {
        assertThrows(
            LinkageError.class,
            () -> this.runApplicationOnModelWith("schedulerDefaultLinkSlaveIcons")
        );

        verify(this.outStream);
    }

    @Test
    void givenModelWithImproperMachineConfig_thenThrowsNumberFormatException () {
        assertThrows(
            NumberFormatException.class,
            () -> this.runApplicationOnModelWith("schedulerValidLinkSlaveIcons")
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "oneTaskGlobalLoad",
            "twoTasksGlobalLoad",
            "oneTaskNodeLoad",
            "twoTasksNodeLoad",
        }
    )
    void givenValidModels_thenSimulates (final String load) {
        given(this.systemTimeProvider.getSystemTime()).willReturn(0L, 1L);

        this.runApplicationOnModelWith(
            "oneUser", "schedulerValidLinkValidSlaveIcons", load);

        verify(this.outStream, Approvals.NAMES.withParameters(load));
    }
}
